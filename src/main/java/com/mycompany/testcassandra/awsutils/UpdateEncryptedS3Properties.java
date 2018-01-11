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
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

public class UpdateEncryptedS3Properties {

  public static void main(String[] args) {
    if (args.length != 5) {
      usage();
      System.exit(0);
    }

    String bucketName = args[0];
    String filename = args[1];
    String kmsKeyId = args[2];
    String propertyKey = args[3];
    // TODO Catch error if not parsing properly.
    boolean localCreds = Boolean.valueOf(args[4]);

    EncryptedS3Properties props = null;
    try {
      props = new EncryptedS3Properties(bucketName, filename, localCreds);
    } catch (Exception e) {
      System.out.println("Unable to obtain S3 Properties.");
      e.printStackTrace();
      System.exit(1);
    }

    String newValue = getValueFromUser();

    try {
      props.put(propertyKey, newValue, true, kmsKeyId);
    } catch (EncryptedS3PropertiesException e) {
      System.out.println("Unable to encrypt and update S3 Properties.");
      e.printStackTrace();
    }
  }

  private static void usage() {
    System.out.println(
        "Usage: UpdateEncryptedS3Properties s3-bucket-name properties-file-name kms-key-id property-key localCreds(true|false)");
    System.out.println(
        "The last parameter is a boolean that indicates whether to use local aws credential or instance credentials. Use true for local.");
    System.out.println("You will be prompted for the new value for the property");
  }

  private static String getValueFromUser() {
    String newValue1 = readLine("Please enter the new value: ");
    String newValue2 = readLine("Please enter the new value again: ");

    if (newValue1 == null || !newValue1.equals(newValue2)) {
      System.out.println("Values do not match. Please try again.");
      System.exit(1);
    }

    return newValue1;
  }

  private static String readLine(String prompt) {
    String line = null;
    Console c = System.console();
    if (c != null) {
      line = new String(c.readPassword(prompt));
    } else {
      System.out.print(prompt);
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
      try {
        line = bufferedReader.readLine();
      } catch (IOException e) {
        // Ignore
      }
    }
    return line;
  }
}