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
public class EncryptedS3PropertiesException extends Exception {
  private static final long serialVersionUID = 3020153677087987685L;

  public EncryptedS3PropertiesException(String message) {
    super(message);
  }
  
  public EncryptedS3PropertiesException(String message, Throwable e) {
    super(message, e);
  }
}