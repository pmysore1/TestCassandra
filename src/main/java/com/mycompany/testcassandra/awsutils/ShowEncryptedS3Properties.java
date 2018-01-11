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
public class ShowEncryptedS3Properties {

  public static void main(String[] args) {
    if (args.length != 3) {
      usage();
      System.exit(0);
    }

    String bucketName = args[0];
    String filename = args[1];

    // TODO Catch error if not parsing properly.
    boolean localCreds = Boolean.valueOf(args[2]);

    EncryptedS3Properties props = null;
    try {
      props = new EncryptedS3Properties(bucketName, filename, localCreds);
    } catch (Exception e) {
      System.out.println("Unable to obtain S3 Properties.");
      e.printStackTrace();
      System.exit(1);
    }
    
    props.keySet().forEach((key) -> {
        System.out.println(key);
    });
  }

  private static void usage() {
    System.out.println(
        "Usage: ShowEncryptedS3Properties s3-bucket-name properties-file-name localCreds(true|false)");
    System.out.println(
        "The last parameter is a boolean that indicates whether to use local aws credential or instance credentials. Use true for local.");
  }
}