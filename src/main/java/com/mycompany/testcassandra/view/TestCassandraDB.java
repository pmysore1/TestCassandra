/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.testcassandra.view;

import com.mycompany.testcassandra.awsutils.EncryptedS3Properties;
import com.mycompany.testcassandra.dao.EmployeeDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author prade
 */
public class TestCassandraDB extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String departmentName = request.getParameter("departname_name") ;
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>All Employees</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>List of Employees at " + departmentName + "</h1>");
            displayEmployeeList(out, departmentName) ;
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    private void displayEmployeeList(PrintWriter out, String departmentName) throws IOException
    {
       Properties prop = new Properties();
       List<EmployeeDAO.Employee> employeeList;
       ClassLoader classLoader = Thread.currentThread().getContextClassLoader() ;
       EmployeeDAO.Employee employee ;
       String app_env= System.getProperty("app_env") ;
       if(app_env != null)
       {
           out.println("App environment ::" +app_env) ;
       }
       else 
            out.println("App environment is null ::") ;
       out.println("</br>") ;
       
       prop.load(classLoader.getResourceAsStream("app.properties")); 
       //String dbHostName = prop.getProperty("cassandraDBHostName") ;
       //String keySpace = prop.getProperty("keyspaceName") ;
       String s3BucketName = prop.getProperty("aws.s3.bucket") ;
       String s3FileName = prop.getProperty("aws.s3.filename") ;
       out.println("s3BucketName :: " + s3BucketName) ;
       out.println("</br>") ;
       out.println("s3FileName :: " + s3FileName) ;
       out.println("</br>") ;
       EmployeeDAO employeeDAO = new EmployeeDAO(prop) ;
       out.println("CassandraVersion :: " + employeeDAO.getCassandraVersion()) ;
       out.println("</br>") ;
       out.println("ClusterName :: " + employeeDAO.getClusterName()) ;
       out.println("</br>") ;
      
       out.println("</br>") ;
       employeeList = employeeDAO.getEmployeesByDept(departmentName) ;
       Iterator<EmployeeDAO.Employee> iterator = employeeList.iterator();
       out.println("Employee List Size :: " + employeeList.size()) ;
       out.println("</br>") ;
        while(iterator.hasNext()){
            employee = (EmployeeDAO.Employee)iterator.next();
            out.println("Employee First Nmae :: " + employee.getEmpFirst()) ;
            out.println("Employee Last Nmae :: " + employee.getEmpLast()) ;
        }
       
       
    }
    private EncryptedS3Properties getAWSS3PropertyFileForDBDetails(PrintWriter out, String bucketName, String filename, boolean localCreds ) {
    
        EncryptedS3Properties props = null;
        try {
          props = new EncryptedS3Properties(bucketName, filename, localCreds);
        } catch (Exception e) {
          out.println("</br>") ;
          out.println("Error :: Unable to obtain S3 Properties") ;
          System.out.println("Unable to obtain S3 Properties.");
          e.printStackTrace();
          
        }
    
        props.keySet().forEach((key) -> {
            System.out.println(key);
        });
        return props;
    }
}
