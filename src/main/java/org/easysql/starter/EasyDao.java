package org.easysql.starter;

import org.apache.log4j.Logger;
import org.easysql.configuration.Configuration;
import org.easysql.utils.LoggerUtils;
import org.easysql.session.Session;
import org.easysql.session.SessionHandler;
import org.easysql.session.SessionManager;
import org.easysql.utils.values.CommonValue;

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

    public void insert(T bean){
        handler.insert(bean);
    }

    public void insert(ArrayList<T> bean){
        handler.insertListToTable(bean);
    }

    public void updateById(T bean){
        handler.updateByID(bean);
    }

    public void update(ArrayList<T> bean){
        handler.updateListByID(bean);
    }

    public void update(T bean,String condition){
        handler.updateByID(bean,condition);
    }

    public ArrayList<T> selectAll(){
        return handler.selectAll();
    }

    public T selectById(String id){
        return handler.selectByID(id);
    }

    public ArrayList<T> select(String condition){
        return handler.select(new StringBuilder(CommonValue.ALL),new StringBuilder(condition), null);
    }

    public void logAll() {
        LoggerUtils.DataOutput(selectAll(),logger);
    }

    public void DataOutput(T bean){
        LoggerUtils.DataOutput(bean,logger);
    }

    public void DataOutput(ArrayList<T> beans){
        LoggerUtils.DataOutput(beans,logger);
    }
}
