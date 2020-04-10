package org.easysql.helper;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import org.easysql.info.DataBaseInfo;

import javax.sql.DataSource;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;


public class DataBaseConnector {
    @Getter
    private static DataSource dataSource;
    private static Logger logger;
    @Getter
    @Setter
    private static DataBaseInfo dataBaseInfo;

    public static void init(Element databaseElement) {
        logger = Configuration.createLogger(DataBaseConnector.class);
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
        } finally {
            close(conn, stmt);
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
        } finally {
            close(conn, stmt);
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
            props.load(new FileReader(Objects.requireNonNull(DataBaseConnector.class.getClassLoader().getResource("druid.properties")).getPath()));
            dataSource = DruidDataSourceFactory.createDataSource(props);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadXMLDataBaseConfiguration(Element databaseElement) {
        //necessary configuration
        String databaseName = databaseElement.element("database").attributeValue("name");
        String userName = databaseElement.element("user").attributeValue("name");
        String password = databaseElement.element("pwd").attributeValue("pwd");
        dataBaseInfo = DataBaseInfo.builder()
                .databaseName(databaseName)
                .username(userName)
                .password(password)
                .build();

        //extra configuration
        Element databaseUrl, databaseDriverClass;
        String url, urlParams, driverClass;
        if ((databaseUrl = databaseElement.element("url")) != null) {
            url = databaseUrl.attributeValue("url");
            if (url != null && !url.isEmpty()) {
                dataBaseInfo.setUrl(url);
            }
            urlParams = databaseUrl.attributeValue("url-params");
            if (urlParams != null && !urlParams.isEmpty()) {
                dataBaseInfo.setUrlParameters(urlParams.trim());
            }
        }
        if ((databaseDriverClass = databaseElement.element("driver-class")) != null) {
            driverClass = databaseDriverClass.attributeValue("class_name");
            if (driverClass != null && !driverClass.isEmpty()) {
                dataBaseInfo.setDriverClassName(driverClass);
            }
        }
        //dynamic data source configuration
        createDataSource(dataBaseInfo);
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
                if ((fieldName.equals("url") || fieldName.equals("urlParameters") || fieldName.equals("databaseName")) && !infoMap.containsKey("url")) {
                    infoMap.put("url", BeanUtils.getProperty(dataBaseInfo, "url")
                            .concat("?")
                            .concat(BeanUtils.getProperty(dataBaseInfo, "databaseName"))
                            .concat(BeanUtils.getProperty(dataBaseInfo, "urlParameters")));
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
