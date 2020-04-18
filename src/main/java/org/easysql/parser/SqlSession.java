package org.easysql.parser;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.easysql.configuration.Configuration;
import org.easysql.utils.*;
import org.easysql.info.orm.ClassInfo;
import org.easysql.info.fill.FillData;
import org.easysql.session.Session;
import org.easysql.session.SessionHandler;

import java.lang.reflect.InvocationTargetException;
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
    private ClassInfo classInfo;
    private XmlHelper<T> parser;
    private Logger logger;

    public SqlSession(Session<T> session,SessionHandler<T> sessionHandler){
        this.session = session;
        this.sessionHandler = sessionHandler;
        this.classInfo =session.getClassInfo();
        this.logger= Configuration.createLogger(SqlSession.class);
        parser=new XmlHelper<>();
        String sqlFileName = session.getSqlFileName();
        if ("".equals(sqlFileName)){
            logger.warn("No sql.xml exists.SqlSession is not available.");
        }else {
            parser.initSqlParser(sqlFileName,session,sessionHandler);
        }
    }

    public StringBuilder where(String id){
        Element element=parser.findSqlElementByID(id, CommonValue.WHERE_ELEMENT_NAME);
        StringBuilder str=new StringBuilder(element.getText());
        LoggerUtils.sqlOutput(str.toString(),logger);
        return str;
    }

    public StringBuilder where(String id,FillData fillData){
        Element element=parser.findSqlElementByID(id, CommonValue.WHERE_ELEMENT_NAME);
        StringBuilder str=new StringBuilder(element.getText());
        LoggerUtils.sqlOutput(str.toString(),logger);
        for (int i = 0; i < str.length(); i++) {
            char ch=str.charAt(i);
            if (ch == CommonValue.PLACEHOLDER){
                str.replace(i,i+1,fillData.getNext());
            }
        }
        return str;
    }

    public void insert(String id){
        StringBuilder sql = getInsertSql(id);
        DatabaseConnector.executeSQL(sql.toString());
        LoggerUtils.sqlOutput(sql.toString(),logger);
    }

    public void insert(String id, FillData fillData){
        StringBuilder sql=getInsertSql(id);
        LoggerUtils.sqlOutput(sql.toString(),logger);
        PreparedStatement preparedStatement= DatabaseConnector.getPreparedStatement(sql.toString());
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

    /**
     *
     * @param id sql id
     * @param bean data to fill
     * this method fill data into your sql in sql.xml
     * where corresponds columns in the element <col></col>
     * col:* is accepted in this method
     */
    public void insert(String id,T bean){
        Element element=parser.findSqlElementByID(id, CommonValue.INSERT_ELEMENT_NAME);
        String[] cols=element.element(CommonValue.COLUMN_ELEMENT_NAME).getTextTrim().split(",");
        ArrayList<String> fields=analyzeFields(cols);
        FillData fillData=new FillData();
        try {
            for (String field : fields) {
                fillData.add(BeanUtils.getProperty(bean,field));
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        insert(id,fillData);
    }


    /**
     *
     * @param id sql id
     * @param beans data list to fill
     * this method fill data list into your sql in sql.xml
     * where corresponds columns in the element <col></col>
     * and use batch inserting to execute sql
     * col:* is accepted in this method
     */
    public void insertList(String id,ArrayList<T> beans){
        Element element=parser.findSqlElementByID(id, CommonValue.INSERT_ELEMENT_NAME);
        String[] cols=element.element(CommonValue.COLUMN_ELEMENT_NAME).getTextTrim().split(",");
        ArrayList<String> fields=analyzeFields(cols);
        StringBuilder sql=getInsertSql(id);
        LoggerUtils.sqlOutput(sql.toString(),logger);
        PreparedStatement preparedStatement= DatabaseConnector.getPreparedStatement(sql.toString());
        try {
            for (T bean : beans) {
                for (int i = 0; i < fields.size(); i++) {
                    preparedStatement.setString(i+1,BeanUtils.getProperty(bean,fields.get(i)));
                }
                preparedStatement.addBatch();
            }
        } catch (SQLException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
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

    private ArrayList<String> analyzeFields(String[] cols) {
        ArrayList<String> fields=new ArrayList<>();
        if (cols.length==1&&cols[0].equals(CommonValue.ALL)){
            fields.add(classInfo.getIdInfo().getFieldName());
            fields.addAll(classInfo.getFieldInfo().keySet());
        }else {
            for (String col : cols) {
                if (col.equals(classInfo.getIdInfo().getColumnName())) {
                    fields.add(classInfo.getIdInfo().getFieldName());
                } else {
                    fields.add(classInfo.getColumnInfo().get(col).getFieldName());
                }
            }
        }
        return fields;
    }

    public void update(String id){
        StringBuilder sql=getUpdateSql(id);
        DatabaseConnector.executeSQL(sql.toString());
        LoggerUtils.sqlOutput(sql.toString(),logger);
    }

    public void update(String id,FillData bean,FillData condition){
        StringBuilder sql=getUpdateSql(id);
        LoggerUtils.sqlOutput(sql.toString(),logger);
        PreparedStatement preparedStatement= DatabaseConnector.getPreparedStatement(sql.toString());
        if (preparedStatement != null) {
            ArrayList<String> beanParamList=bean.getParamList();
            ArrayList<String> conditionParamList=condition.getParamList();
            try {
                for (int i = 0; i < beanParamList.size(); i++) {
                    preparedStatement.setString(i+1,beanParamList.get(i));
                }
                for (int j = 0; j < conditionParamList.size(); j++) {
                    preparedStatement.setString(j+1+beanParamList.size(),conditionParamList.get(j));
                }
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void update(String id,T bean,FillData condition){

    }

    public void update(String id,T bean){

    }

    public void updateList(String id,ArrayList<T> beans,FillData condition){

    }

    public void updateList(String id,ArrayList<T> beans){

    }

    private StringBuilder getUpdateSql(String id){
        StringBuilder sql= new StringBuilder();
        sql.append("update ").append(session.getTableName()).append(" set");
        Element element=parser.findSqlElementByID(id, CommonValue.UPDATE_ELEMENT_NAME);
        Element conditionElement=element.element(CommonValue.WHERE_ELEMENT_NAME);
        String values = element.element("values").getText();
        String condition=conditionElement.getText();
        sql.append(values).append(" where ").append(condition);
        return sql;
    }





}
