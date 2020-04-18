package org.easysql.configuration;

import org.easysql.annotation.analyzer.BeanAnnotationAnalyzer;
import org.easysql.annotation.analyzer.DaoAnnotationAnalyzer;
import org.easysql.info.orm.ClassInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-18 19:47
 **/
public class AnnotationConfiguration {
    private static BeanAnnotationAnalyzer beanAnalyzer;
    private static DaoAnnotationAnalyzer daoAnalyzer;
    public static void configure(){
        beanAnalyzer=new BeanAnnotationAnalyzer();
        daoAnalyzer=new DaoAnnotationAnalyzer();
        configureBean();
        configureDao();
    }

    public static LinkedHashMap<Class<?>, ClassInfo> getPojoConfiguration(){
        return beanAnalyzer.getClassToConfiguration();
    }

    public static void configureBean() {
        beanAnalyzer.analyzeBeanAnnotation();
    }

    public static void configureDao(){
       daoAnalyzer.analyzeBeanAnnotation();
    }
}
