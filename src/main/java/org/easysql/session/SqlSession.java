package org.easysql.session;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.easysql.helper.*;
import org.easysql.info.ClassInfo;
import org.easysql.info.FillData;

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
        ArrayList<String> fields=new ArrayList<>();
        if (cols.length==1&&cols[0].equals(CommonValue.ALL)){
            fields.add(classInfo.getIdInfo().getFieldName());
            fields.addAll(classInfo.getFieldInfos().keySet());
        }else {
            for (String col : cols) {
                if (col.equals(classInfo.getIdInfo().getColumnName())) {
                    fields.add(classInfo.getIdInfo().getFieldName());
                } else {
                    fields.add(classInfo.getColumnInfos().get(col).getFieldName());
                }
            }
        }
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
