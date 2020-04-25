package org.easysql.starter;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.easysql.configuration.Configuration;
import org.easysql.utils.LoggerUtils;
import org.easysql.session.Session;
import org.easysql.session.SessionHandler;
import org.easysql.session.SessionManager;
import org.easysql.parser.SqlSession;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-03-21 10:10
 **/
public class EasyApplication {
    public Logger logger;

    public <T> void init(Class<T> clazz) {
        Configuration.autoConfigure(clazz);
        logger = Configuration.createLogger(clazz);
    }

    public <T> EasyDao<T> dao(Class<T> clazz) {
        return DaoManager.dao(clazz);
    }

    public <T> SessionHandler<T> handler(Class<T> clazz) {
        return SessionManager.getHandlerByClass(clazz);
    }

    public <T> Session<T> session(Class<T> clazz) {
        return SessionManager.getSessionByClass(clazz);
    }

    public <T> SqlSession<T> sql(Class<T> clazz) {
        return SessionManager.getSqlSessionByClass(clazz);
    }

    public <T> void DataOutput(T bean) {
        LoggerUtils.DataOutput(bean, logger);
    }

    public <T> void DataOutput(ArrayList<T> beans) {
        LoggerUtils.DataOutput(beans, logger);
    }
}
