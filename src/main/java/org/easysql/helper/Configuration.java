package org.easysql.helper;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.easysql.session.Session;
import org.easysql.session.SessionManager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class Configuration {
    private static Element rootElement;
    private static Element classRoot;
    private static Element dbRoot;
    private static Element sqlRoot;
    @Getter
    private static Connection connection;
    @Getter
    private static String beanPkg;
    @Getter
    private static String sqlPkg;
    private static final String JAVA_SRC_PATH="\\src\\main\\java\\";//适用于maven
    private static final String CONFIG_PKG_NAME="config\\";
    @Getter@Setter
    private static Logger logger;


    private static void configure(String path){
        XmlHelper.setCONFIG_PATH(path);
        rootElement =XmlHelper.getRootElement("center_config");//中央配置仓库
        classRoot = rootElement.element("class_config");
        dbRoot = rootElement.element("db_config");
        sqlRoot = rootElement.element("sql_config");
        beanPkg = classRoot.attributeValue("bean_pkg");
        sqlPkg = sqlRoot.attributeValue("sql_pkg");
        if (rootElement !=null){
            logger.info(CommonValue.PROCESS+"Getting central configuration finished.");
        }else {
            logger.fatal(CommonValue.ERROR+"Getting central configuration failed.");
            logger.info(CommonValue.SUGGESTION+"Please check your center_config.xml.");
        }
        connection=DBConnector.getConnection();
    }

    public static void configure(Class<?> mainClass){
        try {
            String pkg=mainClass.getPackage().getName();
            String project=new File("").getCanonicalPath();
            String[] pkgs=pkg.split("\\.");
            StringBuilder configPath= new StringBuilder();
            for (int i=0;i<pkgs.length-1;i++){
                configPath.append(pkgs[i]).append("\\");
            }
            String path = project + JAVA_SRC_PATH + configPath + CONFIG_PKG_NAME;
            setLogger(createLogger(Configuration.class));
            configure(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void AutoConfigure(Class<?> mainClass){
        configure(mainClass);
        SessionManager.AutoScanBeans();
    }

    //@para1 className Bean类名
    public static SessionConfiguration getConfiguration(String className){
        String configXmlName=null;//返回class配置文件名
        String tableName=null;
        List<Element> class_list= classRoot.elements("class");
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
            logger.fatal(CommonValue.ERROR+"Class name not found.");
            logger.info(CommonValue.SUGGESTION+"Please check your class name and center_config.xml.");
            return null;
        }
    }

    public static ArrayList<String> ScanAllClass(){
        List<Element> classElementList=classRoot.elements("class");
        ArrayList<String> classList=new ArrayList<>();
        for (Element element : classElementList) {
            String className=beanPkg+"."+element.attributeValue("class_name");
            classList.add(className);
        }
        return classList;
    }

    public static Logger createLogger(Class clazz){
        return Logger.getLogger(clazz);
    }
}


