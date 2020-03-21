package org.easysql.session;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.easysql.helper.*;
import org.easysql.info.FillData;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;


/**
 * @program: EasySqlProject
 * @description sql parser
 * @author: he
 * @create: 2020-03-17 20:58
 **/
public class SqlSession<T>{
    private Session<T> session;
    private SessionHandler<T> sessionHandler;
    private XmlHelper<T> parser;
    private Logger logger;

    public SqlSession(Session<T> session,SessionHandler<T> sessionHandler){
        this.session = session;
        this.sessionHandler = sessionHandler;
        this.logger= Configuration.createLogger(SqlSession.class);
        parser=new XmlHelper<>();
        parser.initSqlParser(session.getSqlFileName(),session,sessionHandler);
    }

    public StringBuilder where(String id){
        Element element=parser.findSqlElementByID(id, CommonValue.WHERE_ELEMENT_NAME);
        StringBuilder str=new StringBuilder(element.getText());
        LoggerHelper.sqlOutput(str.toString(),logger);
        return str;
    }

    public void insert(String id){
        StringBuilder sql = getInsertSql(id);
        DBConnector.executeSQL(sql.toString());
        LoggerHelper.sqlOutput(sql.toString(),logger);
    }

    public void insert(String id, FillData fillData){
        StringBuilder sql=getInsertSql(id);
        LoggerHelper.sqlOutput(sql.toString(),logger);
        PreparedStatement preparedStatement=DBConnector.getPreparedStatement(sql.toString());
        ArrayList<String> paramList=fillData.getParamList();
        try {
            for (int i = 0; i < paramList.size(); i++) {
                preparedStatement.setString(i+1,paramList.get(i));
            }
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private StringBuilder getInsertSql(String id) {
        StringBuilder sql= new StringBuilder();
        sql.append("insert into ").append(session.getTableName());
        Element element=parser.findSqlElementByID(id, CommonValue.INSERT_ELEMENT_NAME);
        String col=element.element("col").getText();
        String values = element.element("values").getText();
        if (col.equals(CommonValue.ALL)) {
            sql.append(" ");
        }else {
            sql.append(" (").append(col).append(") ");
        }
        sql.append("values ").append(values);
        return sql;
    }




}
