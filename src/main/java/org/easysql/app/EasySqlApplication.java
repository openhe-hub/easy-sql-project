package org.easysql.app;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.easysql.helper.Configuration;
import org.easysql.helper.LoggerHelper;
import org.easysql.session.Session;
import org.easysql.session.SessionHandler;
import org.easysql.session.SessionManager;
import org.easysql.session.SqlSession;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-03-21 10:10
 **/
public class EasySqlApplication {
    public static Logger logger;
    public static <T> void init(Class<T> clazz) {
        Configuration.autoConfigure(clazz);
        logger= Configuration.createLogger(clazz);
    }

    public static <T> SessionHandler<T> handler(Class<T> clazz){
        return SessionManager.getHandlerByClass(clazz);
    }

    public static <T> Session<T> session(Class<T> clazz){
        return SessionManager.getSessionByClass(clazz);
    }

    public static <T> SqlSession<T> sql(Class<T> clazz){
        return SessionManager.getSqlSessionByClass(clazz);
    }

    public static <T> void DataOutput(T bean){
        LoggerHelper.DataOutput(bean,logger);
    }

    public static<T> void DataOutput(ArrayList<T> beans){
        LoggerHelper.DataOutput(beans,logger);
    }
}
