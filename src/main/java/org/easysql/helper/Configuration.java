package org.easysql.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.List;

public class Configuration {
    private static Element root;
    private static Element class_root;
    private static Element db_root;
    private static Element sql_root;
    @Getter
    private static Connection connection;
    @Getter
    private static String bean_pkg;
    @Getter
    private static String sql_pkg;
    private static final String JAVA_SRC_PATH="\\src\\main\\java\\";//适用于maven
    private static final String CONFIG_PKG_NAME="config\\";
    private static Logger logger;


    private static void configure(String path){
        XmlHelper.setCONFIG_PATH(path);
        root=XmlHelper.getRootElement("center_config");//中央配置仓库
        class_root=root.element("class_config");
        db_root=root.element("db_config");
        sql_root=root.element("sql_config");
        bean_pkg=class_root.attributeValue("bean_pkg");
        sql_pkg=sql_root.attributeValue("sql_pkg");
        if (root!=null){
            logger.info(CommonValue.PROCESS+"getting central configuration finished.");
        }
        connection=DBConnector.getConnection();
    }

    public static void configure(Class mainClass){
        try {
            String pkg=mainClass.getPackage().getName();
            String project=new File("").getCanonicalPath();
            String[] pkgs=pkg.split("\\.");
            String configPath="";
            for (int i=0;i<pkgs.length-1;i++){
                configPath+=pkgs[i]+"\\";
            }
            String path = project + JAVA_SRC_PATH + configPath + CONFIG_PKG_NAME;
            setLogger(mainClass);
            configure(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //@para1 className Bean类名
    public static SessionConfiguration getConfiguration(String className){
        String configXmlName=null;//返回class配置文件名
        String tableName=null;
        List<Element> class_list= class_root.elements("class");
        for (Element e:class_list) {
            if (e.attributeValue("class_name").equals(className)){
                configXmlName=e.attributeValue("config_name");
                tableName=e.attributeValue("table_name");
            }
        }

        if (configXmlName!=null&&tableName!=null){
            return new SessionConfiguration(configXmlName,tableName,className);
        }
        else {
            System.out.println("error:class name not found!\nPlease check your class name and center_config.xml");
            return null;
        }
    }

    public static void setLogger(Class clazz){
        logger=Logger.getLogger(clazz);
    }

    public static Logger getLogger(){
        return logger;
    }
}


