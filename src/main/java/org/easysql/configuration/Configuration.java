package org.easysql.configuration;

import com.alibaba.druid.sql.visitor.functions.If;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Element;
import org.easysql.annotation.starter.EasySqlApplication;
import org.easysql.session.SessionManager;
import org.easysql.utils.*;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//Fixme: xml tag name changed
@Data
public class Configuration {
    private static Element rootElement;
    private static Element classRoot;
    private static Element dbRoot;
    private static Element logRoot;
    private static Element bannerElement;
    @Getter
    private static Element sqlRoot;
    @Getter
    private static String beanPkg;
    @Getter
    private static String daoPkg;
    @Getter
    private static String sqlPkg;
    @Getter
    private static String idFile;
    @Setter
    private static String CONFIG_PACKAGE = "easy_sql_config";
    @Setter
    private static String CENTER_CONFIG_NAME = "center_config";
    private final static String CONFIG_FILE_TYPE = ".xml";
    @Getter@Setter
    private static Logger logger;
    @Getter@Setter
    public static Class<?> mainClass;
    private static boolean isAnnotationConfiguration;

    public static void configure(Class<?> mainClass) {
        setMainClass(mainClass);
        File file = getFile(mainClass, CENTER_CONFIG_NAME);
        rootElement = XmlHelper.getRootElement(file);
        classRoot = rootElement.element("bean-config");
        dbRoot = rootElement.element("db-config");
        sqlRoot = rootElement.element("sql-config");
        bannerElement = rootElement.element("banner-config");
        beanPkg = classRoot.attributeValue("bean-pkg");
        daoPkg=rootElement.element("dao-config").attributeValue("dao-pkg");
        if (sqlRoot != null) {
            sqlPkg = sqlRoot.attributeValue("sql_pkg");
            idFile = sqlRoot.attributeValue("id_file");
        }
        if (daoPkg==null){
            daoPkg= DefaultParameters.DAO_PACKAGE;
        }
        setLogger(createLogger(Configuration.class));
        if (rootElement != null) {
            if (bannerElement!=null){
                bannerOutput(bannerElement.attributeValue("file"));
            }
            logger.info(CommonValue.PROCESS + "Getting central configuration finished.");
        } else {
            logger.fatal(CommonValue.ERROR + "Getting central configuration failed.");
            logger.info(CommonValue.SUGGESTION + "Please check your center_config.xml.");
        }
         DatabaseConnector.init(dbRoot);
    }

    public static File getFile(Class<?> mainClass, String configName) {
        String path=CONFIG_PACKAGE + "/" + configName + CONFIG_FILE_TYPE;
        URL url=mainClass.getClassLoader().getResource(path);
        return new File(Objects.requireNonNull(url).getFile());
    }

    public static void autoConfigure(Class<?> mainClass) {
        configure(mainClass);
        if (mainClass.isAnnotationPresent(EasySqlApplication.class)){
            isAnnotationConfiguration=true;
            AnnotationConfiguration.configure();
        }
        isAnnotationConfiguration=false;
        SessionManager.autoScanBeans();
    }

    //@para1 className Bean类名
    public static SessionConfiguration getConfiguration(String className) {
        String configXmlName = null;//返回class配置文件名
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

    public static ArrayList<String> scanAllClass(){
        return loadPojoByXml();
    }

    private static ArrayList<String> loadPojoByXml(){
        List<Element> classElementList=classRoot.elements("class");
        ArrayList<String> classList=new ArrayList<>();
        for (Element element : classElementList) {
            String className=beanPkg+"."+element.attributeValue("class_name");
            classList.add(className);
        }
        return classList;
    }


    public static Logger createLogger(Class<?> clazz){
        return Logger.getLogger(clazz);
    }

    private static void configLogger(){
        logRoot = rootElement.element("log_config");
        if (logRoot!=null){
            PropertyConfigurator.configure(logRoot.getTextTrim());
        }
    }

    private static void bannerOutput(String file){
        try {
            File bannerFile =new File(Objects.requireNonNull(mainClass.getClassLoader().getResource(file)).getFile());
            BufferedReader reader=new BufferedReader(new FileReader(bannerFile));
            String line=null;
            logger.info(" banner:\t\t"+CommonValue.PRINT_SEPERATOR);
            while ((line=reader.readLine()) != null){
                logger.info(" banner:\t\t\t\t\t\t\t\t\t\t"+line);
            }
            logger.info(" banner:\t\t\t\t\t\t\t\t\t\t\t\t"+"{ EasySql -version 3.6.0 -beta }");
            logger.info(" banner:\t\t"+CommonValue.PRINT_SEPERATOR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


