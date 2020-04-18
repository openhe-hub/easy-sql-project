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
public class EasySqlDao<T> {
    public Logger logger;
    public Class<T> clazz;
    public SessionHandler<T> handler;
    public Session<T> session;

    public EasySqlDao(Class<T> clazz){
        this.clazz = clazz;
        DaoManager.init(clazz);
        DaoManager.registerDao(clazz,this);
        logger=Configuration.createLogger(clazz);
        handler=SessionManager.getHandlerByClass(clazz);
        session=SessionManager.getSessionByClass(clazz);
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
