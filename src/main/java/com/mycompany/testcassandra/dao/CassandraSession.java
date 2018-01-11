/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.testcassandra.dao;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.mycompany.testcassandra.awsutils.EncryptedS3Properties;
import com.mycompany.testcassandra.awsutils.EncryptedS3PropertiesException;
import java.util.Properties;
/**
 *
 * @author prade
 */
public class CassandraSession {
    private static Session cassandraSession = null;
    private static String cassandraDBHostName ;
    private static String cassandraKeySpace ;
    private static String userNanme = "cassandra"; 
    private static String password  ="cassandra";
    Properties dbProperties ;
    EncryptedS3Properties S3DBProperties ;
    CassandraSession(Properties dbProperties)
    {
        this.dbProperties = dbProperties ;
        this.cassandraDBHostName = (String)dbProperties.get("cassandraDBHostName");
        this.cassandraKeySpace   = (String)dbProperties.get("keyspaceName");
    }
    CassandraSession(EncryptedS3Properties S3DBProperties) throws EncryptedS3PropertiesException
    {
        this.S3DBProperties = S3DBProperties ;
        this.cassandraDBHostName = (String)S3DBProperties.get("cassandraDBHostName");
        this.cassandraKeySpace   = (String)S3DBProperties.get("keyspaceName");
        this.userNanme           = (String)S3DBProperties.get("db-user");
        this.userNanme           = (String)S3DBProperties.get("db-password");
    }
    public static Session getSession() {

        if (cassandraSession == null) {
          cassandraSession = createSession();
        }

        return cassandraSession;

  }
  /**
   *
   * Create a new cassandra Cluster() and Session().  Returns the Session.
   *
   * @return A new Cassandra session
   */

  protected static Session createSession() {
    Cluster cluster = Cluster.builder().addContactPoint(CassandraSession.cassandraDBHostName).withCredentials(userNanme, password).build();
    return cluster.connect(CassandraSession.cassandraKeySpace);
  }
  
  
}
