/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.testcassandra.awsutils;

/**
 *
 * @author prade
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.EncryptRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

/**
 * Reads and updates Java Properties in an AWS S3 bucket, with the option to
 * encrypt the values with AWS KMS. Setup You will need the following: - An S3
 * bucket - An object (file) in the bucket. Initial upload can be done through
 * the AWS console. - To decrypt KMS encrypted values, your instance will need
 * access to the decrypt function in KMS for the key. - This is typically done
 * through a key policy, IAM policy, and IAM role. - If you plan to update the
 * encrypted value, you will need the Key Id and access to the encrypt function
 * of the key.
 *
 * If using local AWS credentials, use localCreds=true. For production it is
 * recommended to use instance credential providers. This means that your EC2
 * instance is assigned an IAM Role that has permission to read the S3 bucket,
 * and also to use the KMS key to decrypt values.
 *
 * TODO: policy and role examples
 *
 * Problems solved by this class: - We no longer store passwords in clear text -
 * We no longer need to keep a decryption key in code or other insecure place -
 * No operator is needed to type in a password to 'unlock' or otherwise give
 * access to the decryption - Autoscaling instances can be setup to
 * automatically get the appropriate role and hence access to the key.
 *
 * Risks: - The KMS keys, policies, and role must set up correctly to protect
 * the encrypted secrets. - Config files are no longer part of the war file, but
 * need to be put in S3. - If new values are added to the properties file, e.g.
 * after a new release, any values that were updated (e.g. new passwords) will
 * need to be merged into the new file.
 *
 * Encrypted values are enclosed in KMS( and ), eg.
 * password=KMS(AQECAHjinKASExJQFpnfP8q/
 * LJSdQWns2Pa3cTQyUCEeH8QF2wAAAGMwYQYJKoZIhvcNAQcGoFQwUgIBADBNBgkqhkiG9w0BBwEwHgYJYIZIAWUDBAEuMBEEDBNNU
 * +2a4TPHAYZrRAIBEIAg1ofrruY9/QGNPl32+gJrMFBhEocKu5kWP6ErJ/Yr/v8=)
 * 
 * Usage examples: EncrypedS3Properties props = new
 * EncryptedS3Properties("my-bucket-name", "my-app.properties", false); String
 * value = props.get("key"); // read value. If encrypted, you will receive the
 * decrypted value
 *
 * // To update a value, e.g a password, and encrypt it with a KMS key, you'll
 * need the key Id and access to encrypt props.put("mykey", "mynewsecret", true,
 * "my-kms-key-id");
 *
 * // To update other values: props.put("mykey", "myvalue", false, null);
 *
 * 
 */
public class EncryptedS3Properties {
  private String bucketName;
  private String filename;
  private boolean localCreds;

  private static final String AWS_S3_BUCKET = "aws.s3.bucket";
  private static final String AWS_S3_FILENAME = "aws.s3.filename";
  private static final String AWS_LOCAL_CREDS = "aws.local.creds";

  private AWSKMSClient kms;
  private AWSCredentials credentials;

  private static final String PROPERTIES_FILE = "encrypted-s3";

  private Properties props;

  /**
   * Loads bucket, filename and localCreds properties from
   * encrypted-s3.properties.
   * 
   * @throws EncryptedS3PropertiesException
   */
  public EncryptedS3Properties() throws EncryptedS3PropertiesException {
    String bucketNameFromFile = null;
    String filenameFromFile = null;
    boolean localCredsFromFile = false;
    try {
      ResourceBundle aws = ResourceBundle.getBundle(PROPERTIES_FILE);
      bucketNameFromFile = aws.getString(AWS_S3_BUCKET);
      filenameFromFile = aws.getString(AWS_S3_FILENAME);
      localCredsFromFile = "true".equals(aws.getString(AWS_LOCAL_CREDS));

      this.init(bucketNameFromFile, filenameFromFile, localCredsFromFile);
    } catch (MissingResourceException e) {
      EncryptedS3PropertiesException ex = new EncryptedS3PropertiesException(
          "The " + PROPERTIES_FILE + " file could not be "
              + "found. The application cannot function without this file. Please verify the correct location of "
              + "the file and restart the application. (" + e.toString() + ")",
          e);
      throw ex;
    } catch (Exception e) {
      throw new EncryptedS3PropertiesException("Unable to load properties. Bucket=" + bucketNameFromFile + ", file="
          + filenameFromFile + ", localCreds=" + localCredsFromFile + ", " + e.getMessage(), e);
    }
  }

  public EncryptedS3Properties(String bucketName, String filename, boolean localCreds)
      throws EncryptedS3PropertiesException {
    this.init(bucketName, filename, localCreds);
  }

  private void init(String bucketName, String filename, boolean localCreds) throws EncryptedS3PropertiesException {
    if (bucketName == null || bucketName.isEmpty()) {
      throw new IllegalArgumentException("bucketName cannot be null or empty");
    }
    if (filename == null || filename.isEmpty()) {
      throw new IllegalArgumentException("filename cannot be null or empty");
    }

    this.bucketName = bucketName;
    this.filename = filename;
    this.localCreds = localCreds;

    // AWS Credential from either local file ~/.aws/credentials, or from
    // InstanceProvider
    // Check that we can load the KMS Client
    initAWSClient();

    props = loadPropertiesFromS3(this.bucketName, this.filename);

    if (!testDecrypt()) {
      throw new EncryptedS3PropertiesException(
          "Unable to decrypt all KMS properties. Please check key decrypt permissions.");
    }
  }

  /**
   * Looks up all KMS encrypted properties in the properties file and verifies
   * that they can be decrypted.
   * 
   * @return true if all KMS encrypted properties can be decrypted, or if none
   *         are present.
   * @throws EncryptedS3PropertiesException
   */
  public boolean testDecrypt() throws EncryptedS3PropertiesException {
    // TODO: lookup all KMS properties and see if they can be decrypted
    return true;
  }

  public Set<Object> keySet() {
    return this.props.keySet();
  }

  public String get(String key) throws EncryptedS3PropertiesException {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("Key cannot be null or empty");
    }

    if (props == null) {
      throw new EncryptedS3PropertiesException("Not initialized correctly, no properties loaded");
    }

    String value = (String) props.get(key);
    if (value != null && value.startsWith(KMS_PREFIX) && value.endsWith(KMS_POSTFIX)) {
      String stripped = value.substring(KMS_PREFIX.length(), value.length() - KMS_POSTFIX.length());
      return decrypt(stripped);
    } else {
      return value;
    }
  }

  public void put(String key, String value, boolean encrypt, String keyId) throws EncryptedS3PropertiesException {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key cannot be null or empty");
    }
    if (encrypt) {
      if (keyId == null || keyId.isEmpty()) {
        throw new IllegalArgumentException("keyId cannot be null or empty");
      }
      String encryptedValue = encrypt(value, keyId);
      String wrapped = KMS_PREFIX + encryptedValue + KMS_POSTFIX;
      props.setProperty(key, wrapped);
    } else {
      props.setProperty(key, value);
    }
    storePropertiesInS3();
  }

  private void storePropertiesInS3() throws EncryptedS3PropertiesException {
    AmazonS3 s3Client = new AmazonS3Client(this.credentials);
    // Write properties file
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try {
      this.props.store(output, null);
    } catch (Exception e) {
      throw new EncryptedS3PropertiesException("Unable to output updated properties", e);
    }
    ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

    Long contentLength = Long.valueOf(output.toByteArray().length);
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(contentLength);

    try {
      s3Client.putObject(new PutObjectRequest(this.bucketName, this.filename, inputStream, metadata));
    } catch (AmazonServiceException ase) {
      String message = "Unable to store property in S3. Error Message: " + ase.getMessage() + "HTTP Status Code: "
          + ase.getStatusCode() + "AWS Error Code:   " + ase.getErrorCode() + "Error Type:       " + ase.getErrorType()
          + "Request ID:       " + ase.getRequestId();
      throw new EncryptedS3PropertiesException(message, ase);
    } catch (Exception e) {
      throw new EncryptedS3PropertiesException("Unable to store properties in S3", e);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (Exception e) {
          throw new EncryptedS3PropertiesException("Unable to close stream after updating properties in S3", e);
        }
      }
    }
  }

  private Properties loadPropertiesFromS3(String bucketName, String key) throws EncryptedS3PropertiesException {
    Properties p = new Properties();
    AmazonS3 s3Client = new AmazonS3Client(this.credentials);
    S3Object s3object = null;
    try {
      s3object = s3Client.getObject(new GetObjectRequest(bucketName, key));
      p.load(s3object.getObjectContent());
    } catch (AmazonServiceException ase) {
      String message = "Unable to read properties from S3. Error Message:    " + ase.getMessage() + "HTTP Status Code: "
          + ase.getStatusCode() + "AWS Error Code:   " + ase.getErrorCode() + "Error Type:       " + ase.getErrorType()
          + "Request ID:       " + ase.getRequestId();
      throw new EncryptedS3PropertiesException(message, ase);
    } catch (Exception e) {
      throw new EncryptedS3PropertiesException("Unable to read properties from S3." + e.getMessage(), e);
    } finally {
      try {
        s3object.close();
      } catch (IOException e) {
        // ignore
      }
    }

    return p;
  }

  private static final String KMS_PREFIX = "KMS(";
  private static final String KMS_POSTFIX = ")";

  private static String bbToStr(ByteBuffer b) {
    return new String(b.array());
  }

  private void initAWSClient() throws EncryptedS3PropertiesException {
    this.credentials = null;
    this.kms = null;
    String message = "";
    try {
      if (this.localCreds) {
        /*
         * The ProfileCredentialsProvider will return your [default] credential
         * profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        message = "Using local creds";
        // local file creds use this:
        this.credentials = new ProfileCredentialsProvider().getCredentials();
      } else {
        message = "Using instance creds";
        // EC2 instance use this:
        this.credentials = new InstanceProfileCredentialsProvider().getCredentials();
      }
      
      this.kms = new AWSKMSClient(this.credentials);
      this.kms.setEndpoint("https://kms.us-east-1.amazonaws.com");
    } catch (Exception e) {
      throw new EncryptedS3PropertiesException("Cannot load AWS credentials. " + message, e);
    }
  }

  private String decrypt(String ciphertextBase64) throws EncryptedS3PropertiesException {
    ByteBuffer cipherBB = ByteBuffer.wrap(ciphertextBase64.getBytes());
    ByteBuffer ciphertext = Base64.getDecoder().decode(cipherBB);

    DecryptRequest req = new DecryptRequest().withCiphertextBlob(ciphertext);
    ByteBuffer plainText = null;
    try {
      plainText = this.kms.decrypt(req).getPlaintext();
    } catch (AmazonServiceException e) {
      // Token is possibly expired.
      // We could possibly narrow this down to this exception:
      // The security token included in the request is expired (Service: AWSKMS;
      // Status Code: 400; Error Code: ExpiredTokenException; Request ID: xxxxx)
      // For now just re-init KMS and hope that works
      initAWSClient();
      plainText = this.kms.decrypt(req).getPlaintext();
    }

    return bbToStr(plainText);
  }

  private String encrypt(String text, String keyId) throws EncryptedS3PropertiesException {
    // For small text use master key directly.
    // For large > 4kb generate a data key first and encrypt with that. Store
    // encrypted data key with encrypted text.
    ByteBuffer plaintext = ByteBuffer.wrap(text.getBytes());

    EncryptRequest req = new EncryptRequest().withKeyId(keyId).withPlaintext(plaintext);
    ByteBuffer ciphertext = kms.encrypt(req).getCiphertextBlob();
    ByteBuffer ciphertextBase64 = Base64.getEncoder().encode(ciphertext);

    return bbToStr(ciphertextBase64);
  }
}
