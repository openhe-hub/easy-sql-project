package org.easysql.plugin.DaoManager;

import org.apache.log4j.Logger;
import org.easysql.helper.Configuration;
import org.easysql.helper.LoggerHelper;
import org.easysql.session.Session;
import org.easysql.session.SessionHandler;
import org.easysql.session.SessionManager;
import org.easysql.session.SqlSession;

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
        LoggerHelper.DataOutput(bean,logger);
    }

    public void DataOutput(ArrayList<T> beans){
        LoggerHelper.DataOutput(beans,logger);
    }
}
