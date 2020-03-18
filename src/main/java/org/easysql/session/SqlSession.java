package org.easysql.session;

import org.easysql.helper.XmlHelper;
import org.easysql.session.Session;
import org.easysql.session.SessionHandler;

import java.util.ArrayList;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-03-17 20:58
 **/
public class SqlSession<T>{
    private Session<T> session;
    private SessionHandler<T> sessionHandler;
    private XmlHelper<T> parser;

    public SqlSession(Session<T> session,SessionHandler<T> sessionHandler){
        this.session = session;
        this.sessionHandler = sessionHandler;
        parser=new XmlHelper<>();
        parser.initSqlParser(session.getSqlFileName(),session,sessionHandler);
    }
}
