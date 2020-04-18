package org.easysql.starter;

import org.apache.log4j.Logger;
import org.easysql.configuration.Configuration;
import org.easysql.utils.LoggerUtils;
import org.easysql.session.Session;
import org.easysql.session.SessionHandler;
import org.easysql.session.SessionManager;

import java.util.ArrayList;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-01 21:12
 **/
public class EasyDao<T> {
    protected Logger logger;
    protected Class<T> pojoClass;
    protected SessionHandler<T> handler;
    protected Session<T> session;

    public void init(Class<?> clazz){
        this.pojoClass = (Class<T>) clazz;
        DaoManager.registerDao(pojoClass,this);
        logger=Configuration.createLogger(clazz);
        handler= SessionManager.getHandlerByClass(pojoClass);
        session= SessionManager.getSessionByClass(pojoClass);
    }

    public ArrayList<T> selectAll(){
        return handler.selectAll();
    }

    public void DataOutput(T bean){
        LoggerUtils.DataOutput(bean,logger);
    }

    public void DataOutput(ArrayList<T> beans){
        LoggerUtils.DataOutput(beans,logger);
    }
}
