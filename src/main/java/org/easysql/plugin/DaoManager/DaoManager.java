package org.easysql.plugin.DaoManager;

import org.easysql.helper.Configuration;

import java.util.LinkedHashMap;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-01 21:12
 **/
public class DaoManager {
    private static boolean isInitialized=false;
    private static LinkedHashMap<Class<?>,EasySqlDao<?>> classToDao;

    public static void init(Class<?> clazz) {
        if (!isInitialized){
            isInitialized = true;
            Configuration.autoConfigure(clazz);
            classToDao = new LinkedHashMap<>();
        }
    }

    public static <T> void registerDao(Class<T> clazz,EasySqlDao<T> dao){
        classToDao.put(clazz, dao);
    }

    public static <T>  EasySqlDao<T> getDao(Class<T> clazz){
        return (EasySqlDao<T>) classToDao.get(clazz);
    }
}
