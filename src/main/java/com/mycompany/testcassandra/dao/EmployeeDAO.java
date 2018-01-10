/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.testcassandra.dao;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author prade
 */
public class EmployeeDAO extends CassandraClusterData{
    
    private String departmentName ;
    private int employee_list_length_in_seconds;
    private List<Employee> employeeList = new ArrayList<>();
    Properties dbProperties ;
    
    public EmployeeDAO(Properties dbProperties)
    {
        super(dbProperties) ;
        this.dbProperties = dbProperties ;
        
    }
    public static class Employee {
        int empID;
        String empFirst;
        String empLast ;
        String deptName ;
        
        public Employee(Row row)
        {
            this.empID = row.getInt("empid");
            this.empFirst = row.getString("emp_first");
            this.empLast = row.getString("emp_last");
            this.deptName = row.getString("emp_dept");
        }
        
        public int getEmpID()
        {
            return this.empID ;
        }
        public String getEmpFirst()
        {
            return this.empFirst ;
        }
        public String getEmpLast()
        {
            return this.empLast ;
        }
        public String getDeptName()
        {
            return this.deptName ;
        }
    }
    public  List<Employee> getEmployeesByDept(String departmentName)
    {
        // Create a new empty playlist object
        //EmployeeDAO employeeDAO = new EmployeeDAO(dbProperties);

        //insert into emp (empid, emp_first, emp_last, emp_dept)
        // Read the tracks from the database
        ResultSet resultSet = null ;
        try
        {
            PreparedStatement statement = getSession().prepare("SELECT empid, emp_first, emp_last, emp_dept " +
                    "FROM emp WHERE emp_dept = ?");

            BoundStatement boundStatement = statement.bind(departmentName);
            resultSet = getSession().execute(boundStatement);
            for (Row row : resultSet)  {
            this.employeeList.add(new Employee(row));

            // Pre-aggregate the playlist length in seconds;
            //this.employee_list_length_in_seconds += row.getInt("track_length_in_seconds");
        }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        

        // Return it
        return this.employeeList;

    }
    public List<Employee> getEmployeeList()
    {
        return this.employeeList ;
    }
}
