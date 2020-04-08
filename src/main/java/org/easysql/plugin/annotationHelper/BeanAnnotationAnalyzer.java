package org.easysql.plugin.annotationHelper;

import org.apache.log4j.Logger;
import org.easysql.annotation.*;
import org.easysql.helper.Configuration;
import org.easysql.info.*;
import org.easysql.info.JoinInfo;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

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
                    ArrayList<LinkedHashMap<String,AnnotationFieldInfo>> fieldInfos=new ArrayList<>();
                    IdInfo idInfo = null;
                    Field[] fields = clazz.getDeclaredFields();
                    /*UNFINISHED CODE
                    for (Field field : fields) {
                        if (field.isAnnotationPresent(Id.class)){
                            idInfo=handleId(field.getAnnotation(Id.class),field);
                        }else if (field.isAnnotationPresent(Column.class)){
                            fieldInfos.add(handleColumn(field.getAnnotation(Column.class),field));
                        }
                    }*/
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


    /* UNFINISHED CODE
    private LinkedHashMap<String,AnnotationFieldInfo> handleColumn(Column column,Field field) {
        String fieldName=field.getName();
        String fieldType=field.getType().getName();
        String columnName=column.columnName();
        String columnType=column.columnType();
        ConstraintType[] constraintTypes=column.constraintTypes();
        ForeignKey[] foreignKeyInfos=column.foreignKey();
        Index[] indexInfos = column.index();
        Join[] joinInfos = column.join();

    }

    private IdInfo handleId(Id id,Field field) {

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
