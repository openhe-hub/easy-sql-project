package org.easysql.helper;

import lombok.Getter;
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


    public static void configure(String path){
        XmlHelper.setCONFIG_PATH(path);
        root=XmlHelper.getRootElement("center_config");//中央配置仓库
        class_root=root.element("class_config");
        db_root=root.element("db_config");
        sql_root=root.element("sql_config");
        bean_pkg=class_root.attributeValue("bean_pkg");
        sql_pkg=sql_root.attributeValue("sql_pkg");
        connection=DBConnector.getConnection();
    }

    public static void configure(Class main_class){
        try {
            String pkg=main_class.getPackage().getName();
            String project=new File("").getCanonicalPath();
            String[] pkgs=pkg.split("\\.");
            String config_path="";
            for (int i=0;i<pkgs.length-1;i++){
                config_path+=pkgs[i]+"\\";
            }
            String path = project + JAVA_SRC_PATH + config_path + CONFIG_PKG_NAME;
            configure(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //@para1 class_name Bean类名
    public static String[] getConfiguration(String class_name){
        String config_xml_name=null;//返回class配置文件名
        String table_name=null;
        List<Element> class_list= class_root.elements("class");
        for (Element e:class_list) {
            String searched_class_name=e.attributeValue("class_name");//searched_class_name:searched class name(not para1!)
            if (searched_class_name.equals(class_name)){
                config_xml_name=e.attributeValue("config_name");
                table_name=e.attributeValue("table_name");
            }
        }

        if (config_xml_name!=null&&table_name!=null){
            return new String[]{table_name,config_xml_name};
        }
        else {
            System.out.println("error:class name not found!\nPlease check your class name and center_config.xml");
            return null;
        }
    }
}
