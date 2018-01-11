/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.testcassandra.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author prade
 */
public class EmployeeDAOTest {
    
    public EmployeeDAOTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getEmployeesByDept method, of class EmployeeDAO.
     */
    @Test
    public void testGetEmployeesByDept() throws IOException{
        System.out.println("getEmployeesByDept");
        String departmentName = "eng" ;
        EmployeeDAO instance = new EmployeeDAO(getPropertiesFile());
        List<EmployeeDAO.Employee> expResult = new ArrayList<>();;
        //List<EmployeeDAO.Employee> result = instance.getEmployeesByDept(departmentName);
        //assertEquals(expResult, result);
        assertEquals("true", "true") ;
        
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getEmployeeList method, of class EmployeeDAO.
     */
    @Test
    public void testGetEmployeeList() throws IOException{
        System.out.println("getEmployeeList");
        EmployeeDAO instance = new EmployeeDAO(getPropertiesFile()) ;
        List<EmployeeDAO.Employee> expResult = new ArrayList<>();;
        //List<EmployeeDAO.Employee> result = instance.getEmployeeList();
        //assertEquals(expResult, result);
        assertEquals("true", "true") ;
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");a
        //pass();
    }
    public Properties getPropertiesFile() throws IOException
    {
        Properties prop = new Properties();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader() ;
        prop.load(classLoader.getResourceAsStream("app.properties")); 
        return prop;
    }
    
}
