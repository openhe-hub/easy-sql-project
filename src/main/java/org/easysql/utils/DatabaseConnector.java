package org.easysql.utils;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import org.easysql.configuration.Configuration;
import org.easysql.info.DataBaseInfo;

import javax.sql.DataSource;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;


public class DatabaseConnector {
    @Getter
    private static DataSource dataSource;
    private static Logger logger;
    @Getter
    @Setter
    private static DataBaseInfo dataBaseInfo;

    public static void init(Element databaseElement) {
        logger = Configuration.createLogger(DatabaseConnector.class);
        loadDataSource(databaseElement);
    }

    public static Connection getConnection() {
        try {
            Connection conn = dataSource.getConnection();
            if (conn != null) {
                return conn;
            } else {
                logger.error("get connection failed.");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Statement getStatement(Connection conn) {
        Statement stmt = null;
        try {
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (stmt != null) {
            return stmt;
        } else {
            System.out.println("error:can't create statement.Please check your connection");
            return null;
        }
    }

    public static void executeSQL(String sql) {
        Connection conn = getConnection();
        Statement stmt = getStatement(conn);
        try {
            if (stmt != null) {
                stmt.execute(sql);
            } else {
                logger.error("sql error.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void executeSQL(StringBuilder sql) {
        executeSQL(sql.toString());
    }

    public static ResultSet executeQuery(String sql) {
        Connection conn = getConnection();
        Statement stmt = getStatement(conn);
        ResultSet rs = null;
        try {
            if (stmt != null) {
                rs = stmt.executeQuery(sql);
                return rs;
            } else {
                logger.error("sql error.");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet executeQuery(StringBuilder sql) {
        return executeQuery(sql.toString());
    }

    public static ResultSetMetaData getResultSetMetaData(ResultSet rs) {
        ResultSetMetaData rsmd = null;
        try {
            rsmd = rs.getMetaData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rsmd;
    }

    public static PreparedStatement getPreparedStatement(String sql) {
        Connection conn = getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            return pstmt;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PreparedStatement getPreparedStatement(StringBuilder sql) {
        return getPreparedStatement(sql.toString());
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closePreparedStatement(PreparedStatement pstmt) {
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Connection conn, Statement stmt) {
        closeConnection(conn);
        closeStatement(stmt);
    }

    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        closeConnection(conn);
        closeStatement(stmt);
        closeResultSet(rs);
    }

    public static void close(Connection conn, PreparedStatement pstmt) {
        closeConnection(conn);
        closePreparedStatement(pstmt);
    }

    private static void loadDataSource(Element databaseElement) {
        if (databaseElement.element("load") != null) {
            loadPropertiesDataBaseConfiguration(databaseElement.element("load").attributeValue("file"));
        } else {
            loadXMLDataBaseConfiguration(databaseElement);
        }
    }

    private static void loadPropertiesDataBaseConfiguration(String propertiesFileName) {
        Properties props = new Properties();
        try {
            props.load(new FileReader(Objects.requireNonNull(DatabaseConnector.class.getClassLoader().getResource("druid.properties")).getPath()));
            dataSource = DruidDataSourceFactory.createDataSource(props);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadXMLDataBaseConfiguration(Element databaseElement) {
        /*
         * key[0]:class fields
         * key[1]:xml element
         */
        HashMap<String[], String> map = new HashMap<>();
        map.put(new String[]{"databaseName", "database"}, "");
        map.put(new String[]{"username", "username"}, "");
        map.put(new String[]{"password", "password"}, "");
        map.put(new String[]{"url", "url"}, DefaultParameters.URL);
        map.put(new String[]{"urlParameters", "url-params"}, DefaultParameters.URL_PARAMETERS);
        map.put(new String[]{"driverClassName", "driver-class"}, DefaultParameters.DRIVER_CLASS);
        map.put(new String[]{"maxActive", "max"}, DefaultParameters.MAX_ACTIVE_CONNECTION + "");
        map.put(new String[]{"initialSize", "initial"}, DefaultParameters.INITIAL_CONNECTION + "");
        map.put(new String[]{"maxWait", "wait-time"}, DefaultParameters.MAX_WAIT_TIME + "");
        DataBaseInfo dataBaseInfo = setXMLConfigurationValue(map, databaseElement);
        //dynamic data source configuration
        createDataSource(dataBaseInfo);
    }

    private static DataBaseInfo setXMLConfigurationValue(HashMap<String[], String> map, Element databaseElement) {
        DataBaseInfo dataBaseInfo = new DataBaseInfo();
        map.forEach((k,v)->{
            Element temp = null;
            try {
                if ((temp = databaseElement.element(k[1])) != null) {
                    String tempStr = temp.getTextTrim();
                    if (tempStr != null && !tempStr.isEmpty()) {
                        BeanUtils.setProperty(dataBaseInfo, k[0], tempStr);
                    } else {
                        //error,empty configuration
                        logger.error("Please configure " + k[1] + " first.");
                    }
                } else {
                    //use default settings
                    if (v.isEmpty()) {
                        //error,user must configure it first
                        logger.error("Please configure " + k[1] + " first.");
                    } else {
                        BeanUtils.setProperty(dataBaseInfo, k[0], v);
                    }
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        return dataBaseInfo;
    }

    /**
     * @description for xml configuration
     */
    private static void createDataSource(DataBaseInfo dataBaseInfo) {
        HashMap<String, String> infoMap = new HashMap<>();
        for (Field field : DataBaseInfo.class.getDeclaredFields()) {
            String fieldName = field.getName();
            try {
                //concat database url
                if ((fieldName.equals("url") || fieldName.equals("urlParameters") || fieldName.equals("databaseName"))) {
                    if (!infoMap.containsKey("url")) {
                        infoMap.put("url", BeanUtils.getProperty(dataBaseInfo, "url")
                                .concat(BeanUtils.getProperty(dataBaseInfo, "databaseName"))
                                .concat("?")
                                .concat(BeanUtils.getProperty(dataBaseInfo, "urlParameters")));
                    }
                } else {
                    infoMap.put(fieldName, BeanUtils.getProperty(dataBaseInfo, fieldName));
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        try {
            dataSource = DruidDataSourceFactory.createDataSource(infoMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
