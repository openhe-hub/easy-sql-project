package org.easysql.annotation.analyzer;

import lombok.Getter;
import org.apache.log4j.Logger;
import org.easysql.annotation.pojo.*;
import org.easysql.configuration.Configuration;
import org.easysql.info.constraint.ConstraintType;
import org.easysql.info.orm.*;

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
    private final String beanPackage;
    @Getter
    private LinkedHashMap<Class<?>, ClassInfo> classToConfiguration;
    private final Class<?> mainClass;
    private File beanFolder;
    private final Logger logger;

    public BeanAnnotationAnalyzer(){
        this.beanPackage=Configuration.getBeanPkg();
        mainClass= Configuration.getMainClass();
        classToConfiguration=new LinkedHashMap<>();
        logger=Configuration.createLogger(BeanAnnotationAnalyzer.class);
    }

    public void analyzeBeanAnnotation(){
        getBeanFolder();
        for (File file : Objects.requireNonNull(beanFolder.listFiles())) {
            final String classType = ".class";
            String className=file.getName().substring(0,file.getName().length()- classType.length());
            className=beanPackage.concat(".").concat(className);
            try {
                Class<?> pojoClass=Class.forName(className);
                ClassInfo classInfo=new PojoAnnotationAnalyzer(pojoClass).analyzePojo();
                if (classInfo != null) {
                    classToConfiguration.put(pojoClass,classInfo);
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
}
