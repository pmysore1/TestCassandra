/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.testcassandra.dao;

import com.datastax.driver.core.Row;
import java.util.Properties;

/**
 *
 * @author prade
 */
public class CassandraClusterData extends CassandraSession{
    private final String clusterName;
    private final String cassandraVersion;
    private Properties dbProperties ;

  /**
   * Constructor to query cassandra for the release_version and cluster_name
   */

  public CassandraClusterData(Properties dbProperties) {

    super(dbProperties) ;
    Row row = getSession().execute("select cluster_name, release_version from system.local").one();
    cassandraVersion = row.getString("release_version");
    clusterName = row.getString("cluster_name");

  }

  public String getClusterName() {
    return clusterName;
  }

  public String getCassandraVersion() {
    return cassandraVersion;
  }
}
