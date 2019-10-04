package org.easysql.helper;

import org.dom4j.Element;

import java.sql.*;

public class DBConnector {
    private static Connection conn;
    private static Statement stmt;
    private static PreparedStatement pstmt;
    private static ResultSet rs;
    private static ResultSetMetaData rsmd;
    private static String db_name;

    public static Connection getConnection(){
        //解析数据库配置用户信息
       Element db_config=XmlHelper.getRootElement("center_config").element("db_config");
       db_name=db_config.element("database").attributeValue("name");
       String user=db_config.element("user").attributeValue("name");
       String pwd=db_config.element("pwd").attributeValue("pwd");
       String driver_class=db_config.element("driver_class").attributeValue("class_name");
       Element url_element=db_config.element("url");
       String url=url_element.attributeValue("url1")+db_name+url_element.attributeValue("url2");

       //获取数据库连接
        try {
            Class.forName(driver_class);
            conn=DriverManager.getConnection(url,user,pwd);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(conn!=null){
            return conn;
        }
        else {
            System.out.println("error:can't connect to mysql.Please check your center_config.xml");
            return null;
        }
    }

    public static Statement getStatement(){
        try {
            stmt=conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(stmt!=null){
            return stmt;
        }
        else {
            System.out.println("error:can't create statement.Please check your connection");
            return null;
        }
    }

    public static void executeSQL(String sql){
        try {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet executeQuery(String sql){
        try {
            rs=stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    public static ResultSetMetaData getRsmd(ResultSet rs){
        try {
           rsmd=rs.getMetaData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rsmd;
    }

    public static PreparedStatement getPreparedStatement(String sql){
        try {
            pstmt=conn.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pstmt;
    }
    public static String get_db_Name(){
        return db_name;
    }
    public static void close(){
        try {
            if (conn!=null){
                conn.close();
            }
            if (stmt!=null){
                stmt.close();
            }
            if (pstmt!=null){
                pstmt.close();
            }
            if (rs!=null){
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
