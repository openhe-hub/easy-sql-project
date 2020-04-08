package org.easysql.plugin.annotationHelper;

import com.sun.org.apache.bcel.internal.generic.RET;
import org.apache.log4j.Logger;
import org.easysql.annotation.Column;
import org.easysql.annotation.EasySqlPojo;
import org.easysql.annotation.EasySqlSmartPojo;
import org.easysql.annotation.Id;
import org.easysql.helper.Configuration;
import org.easysql.helper.LoggerHelper;
import org.easysql.info.ClassInfo;
import org.easysql.info.FieldInfo;
import org.easysql.info.IdInfo;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-08 11:21
 **/
public class BeanAnnotationAnalyzer {
    private String beanPackage;
    private LinkedHashMap<Class<?>, ClassInfo> classToConfiguration;
    private Class<?> mainClass;
    private File beanFolder;
    private Logger logger;
    private final String classType=".class";
    public BeanAnnotationAnalyzer(){
        this.beanPackage=Configuration.getBeanPkg();
        mainClass= Configuration.getMainClass();
        classToConfiguration=new LinkedHashMap<>();
        logger=Configuration.createLogger(BeanAnnotationAnalyzer.class);
    }

    public void analyzeBeanAnnotation(){
        getBeanFolder();
        for (File file : Objects.requireNonNull(beanFolder.listFiles())) {
            String className=file.getName().substring(0,file.getName().length()-classType.length());
            className=beanPackage.concat(".").concat(className);
            try {
                Class<?> clazz=Class.forName(className);
                if (clazz.isAnnotationPresent(EasySqlPojo.class)){
                    LinkedHashMap<String,String[]> pojoInfo=handleBean(clazz.getAnnotation(EasySqlPojo.class),className);
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.isAnnotationPresent(Id.class)){

                        }else if (field.isAnnotationPresent(Column.class)){

                        }
                    }
                }else if (clazz.isAnnotationPresent(EasySqlSmartPojo.class)){

                }else {

                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void getBeanFolder(){
        String beanPackagePath=beanPackage.replaceAll("\\.","/");
        beanFolder=new File(Objects.requireNonNull(mainClass.getClassLoader().getResource(beanPackagePath)).getPath());
    }

   /* private FieldInfo handleFile(Column column) {

    }

    private IdInfo handleId(Id id) {

    }*/

    private LinkedHashMap<String,String[]> handleBean(EasySqlPojo pojo,String className){
        String tableName=pojo.tableName();
        if (tableName.equals("")) {
            logger.error("Bean "+className+" mapping table not found.");
        }
        String sqlFileName=pojo.sqlFile();
        LinkedHashMap<String,String[]> pojoInfo=new LinkedHashMap<>();
        pojoInfo.put(className,new String[]{className,tableName,sqlFileName});
        return pojoInfo;
    }
}
