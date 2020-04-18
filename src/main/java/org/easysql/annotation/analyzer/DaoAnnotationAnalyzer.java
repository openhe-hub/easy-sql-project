package org.easysql.annotation.analyzer;

import org.apache.log4j.Logger;
import org.easysql.annotation.starter.EasySqlDao;
import org.easysql.configuration.Configuration;
import org.easysql.info.orm.ClassInfo;
import org.easysql.starter.DaoManager;
import org.easysql.starter.EasyDao;

import java.io.File;
import java.util.Objects;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-08 11:22
 **/
public class DaoAnnotationAnalyzer {
    private final String daoPackage;
    private final Class<?> mainClass;
    private File beanFolder;
    private final Logger logger;

    public DaoAnnotationAnalyzer(){
        this.daoPackage= Configuration.getDaoPkg();
        mainClass= Configuration.getMainClass();
        logger=Configuration.createLogger(DaoAnnotationAnalyzer.class);
    }

    public void analyzeBeanAnnotation(){
        getDaoFolder();
        for (File file : Objects.requireNonNull(beanFolder.listFiles())) {
            final String classType = ".class";
            String className=file.getName().substring(0,file.getName().length()- classType.length());
            className=daoPackage.concat(".").concat(className);
            try {
                Class<?> daoClass=Class.forName(className);
                if (daoClass.isAnnotationPresent(EasySqlDao.class)){
                    String pojoClass=daoClass.getAnnotation(EasySqlDao.class).pojoName();
                    EasyDao<?> dao = (EasyDao<?>)daoClass.newInstance();
                    dao.init(Class.forName(pojoClass));
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    private void getDaoFolder(){
        beanFolder=new File(Objects.requireNonNull(mainClass.getClassLoader().getResource(daoPackage)).getPath());
    }
}
