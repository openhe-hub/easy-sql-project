package org.easysql.starter;

import org.easysql.configuration.AnnotationConfiguration;
import org.easysql.configuration.Configuration;

import java.util.LinkedHashMap;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-01 21:12
 **/
public class DaoManager {
    private static LinkedHashMap<Class<?>, EasyDao<?>> classToDao;
    static {
        classToDao = new LinkedHashMap<>();
    }

    public static void init(Class<?> clazz) {
        Configuration.autoConfigure(clazz);
    }

    public static <T> void registerDao(Class<T> clazz, EasyDao<T> dao){
        classToDao.put(clazz, dao);
    }

    public static <T> EasyDao<T> dao(Class<T> clazz){
        return (EasyDao<T>) classToDao.get(clazz);
    }
}
