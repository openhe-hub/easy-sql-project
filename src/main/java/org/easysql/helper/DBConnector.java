package org.easysql.helper;

import lombok.Getter;
import org.dom4j.Element;

import java.sql.*;

public class DBConnector {
    private static Connection conn;
    private static Statement stmt;
    private static PreparedStatement pstmt;
    private static ResultSet rs;
    private static ResultSetMetaData rsmd;
    @Getter
    private static String db_name;
    private static String driver_class;
    private static String user;
    private static String pwd;
    private static String url;

    public static Connection getConnection(Element databaseElement){
        //解析数据库配置用户信息
        getUserInfo(databaseElement);
        //获取数据库连接
        try {
            Class.forName(driver_class);
            conn=DriverManager.getConnection(url,user,pwd);
            getStatement();
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

    private static void getUserInfo(Element databaseElement) {
        db_name=databaseElement.element("database").attributeValue("name");
        user=databaseElement.element("user").attributeValue("name");
        pwd=databaseElement.element("pwd").attributeValue("pwd");
        driver_class=databaseElement.element("driver_class").attributeValue("class_name");
        Element url_element=databaseElement.element("url");
        url=url_element.attributeValue("url1")+db_name+url_element.attributeValue("url2");
    }
}
