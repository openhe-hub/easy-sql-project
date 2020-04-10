package org.easysql.helper;

import lombok.*;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.easysql.info.*;
import org.easysql.session.Session;
import org.easysql.session.SessionHandler;
import org.easysql.session.SessionManager;


import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class XmlHelper<T> {
    @Getter
    private static final String CONFIG_FILE_TYPE = ".xml";
    @Setter
    public static String sqlXmlName = "";
    private static SAXReader saxReader;
    private static Logger logger = Configuration.createLogger(XmlHelper.class);;

    private Element sqlRoot;
    @Setter
    private Session<T> session;
    @Setter
    private SessionHandler<T> handler;
    @Getter
    @Setter
    private StringBuilder sql;
    @Getter
    private ArrayList<String> paras;
    @Getter
    private int parasCursor;

    public static Element getRootElement(File file) {
        Element element = null;
        saxReader = new SAXReader();
        try {
            Document configureDoc = saxReader.read(file);
            element = configureDoc.getRootElement();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        if (element != null) {
            return element;
        } else {
            logger.error("Configure name error.");
            return null;
        }
    }

    //动态sql解析
    public void initSqlParser(String sqlXmlName, Session<T> defaultSession, SessionHandler<T> defaultHandler) {
        session = defaultSession;
        handler = defaultHandler;
        paras = new ArrayList<>();
        String CONFIG_PATH = Configuration.getSqlRoot().attributeValue("sql_pkg");
        File sqlFile =new File(Objects.requireNonNull(Configuration.getMainClass().getClassLoader().
                getResource(CONFIG_PATH + "/" + sqlXmlName + CONFIG_FILE_TYPE)).getFile());
        try {
            Document sqlDoc = saxReader.read(sqlFile);
            sqlRoot = sqlDoc.getRootElement();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    //where条件解析
    public XmlHelper<T> parseCondition(String id) {
        Element whereElement = findSqlElementByID(id, "where");
        if (whereElement != null) {
            sql = new StringBuilder(whereElement.getText());
            fillAnd();
            return this;
        } else {
            logger.error("Condition not found.");
            logger.info(CommonValue.SUGGESTION + "Please check your sql.xml and id.");
            return null;
        }
    }

    public XmlHelper<T> parseColumns(String id) {
        Element columnElement = findSqlElementByID(id, "fields");
        if (columnElement != null) {
            sql = new StringBuilder(columnElement.getText());
            return this;
        } else {
            logger.error("Column not found.");
            logger.info(CommonValue.SUGGESTION + "Please check your sql.xml and id.");
            return null;
        }
    }

    //单表查询
    public ArrayList<T> parseSelect(String id, T bean, String[] datas) {
        Element selectElement = findSqlElementByID(id, "select");
        if (selectElement != null) {
            String selectClass = selectElement.attributeValue("class");
            String[] classes = selectClass.split(",");
            int num = classes.length;
            if (num == 1) {
                return selectOneTable(selectElement, bean, datas);
            } else if (num > 1) {
                return selectTables(selectElement, bean, datas);
            } else {
                logger.error("No class to select.");
                logger.info(CommonValue.SUGGESTION + "Please add class attribute value.");
                return null;
            }
        } else {
            logger.error("Select sql not found.");
            logger.info(CommonValue.SUGGESTION + "Please check your sql.xml and id.");
            return null;
        }
    }

    private ArrayList<T> selectOneTable(Element selectElement, T bean, String[] datas) {
        ArrayList<T> resultList;
        AnalyseSelectPackage analyseSelectPackage = analyzeSelectSql(selectElement, bean, datas, new ArrayList<>());
        resultList = handler.select(analyseSelectPackage.getToSelect(), analyseSelectPackage.getCondition(),
                analyseSelectPackage.getSelectParas());
        if (resultList != null) {
            return resultList;
        } else {
            return null;
        }
    }


    private ArrayList<T> selectTables(Element selectElement, T bean, String[] datas) {
        ArrayList<T> resultList = new ArrayList<>();
        ArrayList<LinkedHashMap<Session<?>,Object>> tableData=new ArrayList<>();
        String merge = selectElement.attributeValue("merge");
        String mainClass = selectElement.attributeValue("return");
        ResultSet rs;
        //analyze
        AnalyseMultiSelectPackage analyseMultiSelectPackage = analyseMultiSelectSql(selectElement, bean, datas, new ArrayList<>());
        //fill paras and get result
        PreparedStatement preparedStatement = DataBaseConnector.getPreparedStatement(analyseMultiSelectPackage.getSql().toString());
        ArrayList<Object> paras = analyseMultiSelectPackage.getSelectParas();
        LinkedHashMap<Session<?>, ColumnCursor> columnCursor = analyseMultiSelectPackage.getColumnCursor();
        LinkedHashMap<String,Session<?>> sessions=analyseMultiSelectPackage.getSessions();
        try {
            if (paras != null && paras.size() > 0) {
                for (int i = 0; i < paras.size(); i++) {
                    preparedStatement.setObject(i + 1, paras.get(i));
                }
            }
            rs = preparedStatement.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    T data = null;
                    LinkedHashMap<Session<?>,Object> objs=new LinkedHashMap<>();
                    for (Map.Entry<Session<?>, ColumnCursor> cursorEntry : columnCursor.entrySet()) {
                        Session<?> session = cursorEntry.getKey();
                        ColumnCursor cursor = cursorEntry.getValue();
                        if (session.getClassName().equals(mainClass)) {
                            data = (T) session.getInstance();
                            for (int i = cursor.getStart(); i <= cursor.getEnd(); i++) {
                                Object obj = rs.getObject(i+1);
                                BeanUtils.setProperty(data, cursor.getFields().get(i - cursor.getStart()), obj);
                            }
                        }else {
                            Object newBean = session.getInstance();
                            for (int i = cursor.getStart()-1; i < cursor.getEnd(); i++) {
                                Object obj = rs.getObject(i + 1);
                                BeanUtils.setProperty(newBean, cursor.getFields().get(i - cursor.getStart()+1), obj);
                            }
                            objs.put(session,newBean);
                        }
                    }
                    tableData.add(objs);
                    resultList.add(data);
                }
                analyzeData(resultList, tableData, mainClass, merge);
            }
        } catch (SQLException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    private ArrayList<T> analyzeData(ArrayList<T> resultList, ArrayList<LinkedHashMap<Session<?>, Object>> tableData, String mainClass, String merge) {
        for (int i=0;i<tableData.size();i++) {
            LinkedHashMap<Session<?>,Object> objs=tableData.get(i);
            T data=resultList.get(i);
            for (Map.Entry<Session<?>,Object> entry:objs.entrySet()) {
                Object obj=entry.getValue();
                Session<?> session=entry.getKey();
                JoinInfo joinInfo =SessionManager.getJoin(mainClass,session.getClassName());
                ConstraintType type= joinInfo.getType();
                try {
                    String injectPoint = joinInfo.getFromField();;//inject point where data inject into main bean
                    switch (type){
                        case ONE_TO_ONE:
                        case MANY_TO_ONE: {
                            BeanUtils.setProperty(data, injectPoint,obj);
                        }break;

                        case ONE_TO_MANY:{
                            ArrayList<Object> list=new ArrayList<>();
                            list.add(obj);
                            Object refer=BeanUtils.getProperty(data,merge);
                            for(int j=i+1;j<resultList.size();j++){
                               if(BeanUtils.getProperty(resultList.get(j),merge).equals(refer)){
                                   list.add(tableData.get(j).get(session));
                                   if(j==resultList.size()-1){
                                       for (int k = i+1; k <=j; k++) {
                                           resultList.remove(i+1);
                                           tableData.remove(i+1);
                                       }
                                   }
                               }else {
                                  for (int k = i+1; k <j; k++) {
                                       resultList.remove(i+1);
                                       tableData.remove(i+1);
                                  }
                                  break;
                               }
                            }
                            BeanUtils.setProperty(data, injectPoint,list);
                        }

                        default:{}break;
                    }
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
        return resultList;
    }

    private AnalyseMultiSelectPackage analyseMultiSelectSql(Element selectElement, T bean, String[] datas, ArrayList<Object> selectParas) {
        ArrayList<String> tableNames = new ArrayList<>();
        int dataCursor = 0;
        StringBuilder toSelect = new StringBuilder();
        StringBuilder condition = new StringBuilder();
        StringBuilder sql = new StringBuilder("select ");
        LinkedHashMap<String, Session<?>> sessions = new LinkedHashMap<>();
        LinkedHashMap<Session<?>, ColumnCursor> columnCursor = new LinkedHashMap<>();

        String mainClass = selectElement.attributeValue("return");
        Session<?> mainSession = SessionManager.selectSessionByClassName(mainClass);
        String mainTable = mainSession.getTableName();

        String[] classes = selectElement.attributeValue("class").split(",");
        sessions.put(mainClass, mainSession);
        for (String className : classes) {
            Session<?> session = SessionManager.selectSessionByClassName(className);
            if (session == null) {
                logger.error("Class:" + className + " not found.");
                logger.info(CommonValue.SUGGESTION+"Please check your sql.xml.");
                return null;
            }
            if (!session.getClassName().equals(mainClass)) {
                sessions.put(className, session);
                tableNames.add(session.getTableName());
            }
        }


        List<Element> subElements = selectElement.elements();
        for (Element subElement : subElements) {
            String subElementName = subElement.getName();
            String text = subElement.getText();
            switch (subElementName) {
                case "fields": {
                    setSql(new StringBuilder(text));
                    String[] fields = getSql().toString().split(",");
                    columnCursor = recordSelectField(sessions, fields);
                    toSelect = multiFill(fields, sessions);
                    sql.append(toSelect);
                    sql.append("\nfrom ").append(SessionManager.selectSessionByClassName(mainClass).getTableName()).append("\n");
                }
                break;
                case "join": {
                    String joinClass = subElement.attributeValue("join");
                    JoinInfo joinInfo = SessionManager.getJoin(mainClass, joinClass);
                    String form = joinInfo.getForm().getConstraintType();
                    String joinTable = SessionManager.selectSessionByClassName(joinClass).getTableName();
                    String[] point = joinInfo.getPoint();
                    String joinCondition = joinInfo.getCondition();
                    condition = new StringBuilder(" " + form + " " + joinTable + " on " + mainTable + "." + point[0] + joinCondition + joinTable + "." + point[1]);

                    Element where = subElement.element("where");
                    if (where != null) {
                        condition.append(" and ");
                        setSql(new StringBuilder(where.getText()));
                        dataCursor = getDataCursor(bean, datas, dataCursor);
                        condition.append(getSql());
                        selectParas.addAll(getParas());
                    }
                    sql.append(condition).append("\n");
                }
                break;
            }
        }
        sql.append(";\n");
        return new AnalyseMultiSelectPackage(sessions, columnCursor, sql);
    }

    private  AnalyseSelectPackage analyzeSelectSql(Element selectElement, T bean, String[] datas, ArrayList<Object> selectParas) {
        String tableName;
        int dataCursor = 0;
        StringBuilder toSelect = new StringBuilder();
        StringBuilder condition = new StringBuilder();
        List<Element> subElements = (List<Element>) selectElement.elements();
        for (Element subElement : subElements) {
            String subElementName = subElement.getName();
            String text = subElement.getText();
            switch (subElementName) {
                case "fields": {
                    setSql(new StringBuilder(text));
                    toSelect = fill().getSql().append("\n");
                }
                break;
                case "from": {
                    tableName = session.getTableName();
                    if (!text.equals(session.getClassName()) ||
                            !tableName.equals(handler.getTableName())) {
                        logger.error("Session or handler not suit.");
                        return null;
                    }
                }
                break;
                case "where": {
                    setSql(new StringBuilder(text));
                    dataCursor = getDataCursor(bean, datas, dataCursor);
                    condition = getSql();
                    selectParas.addAll(getParas());
                }
                break;
                case "order": {
                    String type = subElement.attributeValue("type");
                    setSql(new StringBuilder(text));
                    fill();
                    condition.append("\n order by ").append(getSql()).append(" ").append(type);
                }
                break;
                case "limit": {
                    setSql(new StringBuilder(text));
                    fill(Arrays.copyOfRange(datas, dataCursor, datas.length));
                    condition.append("\n limit ").append(text);
                    ArrayList<Integer> list = new ArrayList<>();
                    for (String para : paras) {
                        list.add(Integer.parseInt(para));
                    }
                    selectParas.addAll(list);
                }
                break;
            }
        }
        return new AnalyseSelectPackage(toSelect, condition, selectParas);
    }

    private int getDataCursor(T bean, String[] datas, int dataCursor) {
        fillAnd();
        String[] subDatas = Arrays.copyOfRange(datas, dataCursor, datas.length);
        if (bean != null && datas != null) {
            fill( bean, subDatas);
        } else if (bean == null && datas != null) {
            fill(subDatas);
        } else if (bean == null && datas == null) {
            fill();
        }
        dataCursor = getParasCursor();
        return dataCursor;
    }


    public XmlHelper<T> fill(T bean, String[] datas) {
        paras.clear();
        if (session != null) {
            //填充datas数据
            if (datas != null) {
                parasCursor = 0;
                for (int i = 0; i < sql.length(); i++) {
                    char c = sql.charAt(i);
                    if (c == '?') {
                        paras.add(datas[parasCursor++]);
                    }
                }
            } else {
                logger.warn("No datas to fill.");
            }
            //填充bean数据
            if (bean != null) {
                if (bean.getClass().equals(session.getBeanClass())) {
                    int startFill = 0;
                    int endFill;
                    boolean isFillData = false;
                    boolean isFillColumn = false;
                    for (int i = 0; i < sql.length(); i++) {
                        char c = sql.charAt(i);
                        if (c == '@') {//获取开始填充标记@
                            startFill = i + 2;
                            if (sql.charAt(i + 1) == '{') {//获取开始填充数据标记{
                                isFillData = true;
                            } else if (sql.charAt(i + 1) == '(') {//获取开始填充字段标记(
                                isFillColumn = true;
                            }
                        } else if (c == '}' && isFillData) {//获取结束填充数据标记}，并填充数据
                            endFill = i - 1;
                            i = fillBeanData(bean, sql, startFill, endFill, i);
                            isFillData = false;
                        } else if (c == ')' && isFillColumn) {//获取结束填充字段标记)，并填充字段
                            endFill = i - 1;
                            i = fillColumn(startFill, endFill, i);
                            isFillColumn = false;
                        }
                    }
                } else {
                    logger.error("Class type not equal.");
                }
            } else {
                logger.warn("No bean to fill.");
            }
        } else {
            logger.warn("No session to get class info.");
        }

        return this;
    }

    //only fill columns
    public XmlHelper<T> fill() {
        int startFill = 0;
        int endFill = 0;
        boolean isFillColumn = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '@' && sql.charAt(i + 1) == '(') {
                startFill = i + 2;
                isFillColumn = true;
            } else if (c == ')' && isFillColumn) {
                endFill = i - 1;
                i = fillColumn(startFill, endFill, i);
                isFillColumn = false;
            }
        }
        return this;
    }

    //fill columns and datas
    public XmlHelper<T> fill(String[] datas) {
        paras.clear();
        fill();
        fillData(datas, sql);
        return this;
    }


    private int fillColumn(int startFill, int endFill, int i) {
        String columnName;
        String field = sql.substring(startFill, endFill + 1);
        if (field.equals(session.getClassInfo().getIdInfo().getFieldName())) {//主键
            columnName = session.getClassInfo().getIdInfo().getColumnName();
        } else if (field.equals("#")) {//类名
            columnName = session.getTableName();
        } else if (field.charAt(0) == '#') {//多表类名
            String className = field.substring(1);
            columnName = SessionManager.selectSessionByClassName(className).getTableName();
        } else {//字段名
            columnName = session.getClassInfo().getFieldInfos().get(field).getColumnName();
        }
        if (columnName != null) {
            sql.replace(startFill - 2, endFill + 2, columnName);//注入字段
            i = startFill - 2 + columnName.length();
        } else {

            logger.error("Field not found.");
            logger.info(CommonValue.SUGGESTION+"Please check your sql.xml.");
        }
        return i;
    }

    private <T> int fillBeanData(T bean, StringBuilder sql, int startFill, int endFill, int i) {
        String field = sql.substring(startFill, endFill + 1);
        try {
            String data = "\'" + BeanUtils.getProperty(bean, field) + "\'";
            paras.add(data);//注入数据到参数集
            sql.replace(startFill - 2, endFill + 2, "?");//先使用占位符替代，到handler层注入
            i = startFill - 2 + data.length();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return i;
    }

    private StringBuilder fillData(String[] datas, StringBuilder sql) {
        if (datas.length > 0) {
            parasCursor = 0;
            for (int i = 0; i < sql.length(); i++) {
                char c = sql.charAt(i);
                if (c == '?') {
                    paras.add(datas[parasCursor++]);
                }
            }
        } else {
            logger.warn("No datas to fill data.");
        }
        return sql;
    }

    //multi-tables fill column
    private StringBuilder multiFill(String[] fields, LinkedHashMap<String, Session<?>> sessions) {
        StringBuilder sql = new StringBuilder();
        for (String field : fields) {
            String[] src = field.split("\\.");
            String className = src[0];
            String fieldName = src[1];
            Session<?> session = null;

            if (className.substring(0, 3).equals("@(#") &&
                    className.charAt(className.length() - 1) == ')') {
                className = className.substring(3, className.length() - 1);
                session = sessions.get(className);
                className = session.getTableName();
            }
            sql.append(className).append(".");

            if (!fieldName.equals("*")) {
                if (fieldName.substring(0, 2).equals("@(") &&
                        fieldName.charAt(fieldName.length() - 1) == ')') {
                    fieldName = fieldName.substring(2, fieldName.length() - 1);
                    fieldName = fieldName.equals(session.getClassInfo().getIdInfo().getColumnName()) ?
                            session.getClassInfo().getIdInfo().getColumnName() :
                            session.getClassInfo().getFieldInfos().get(fieldName).getColumnName();
                }
            }
            sql.append(fieldName).append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        return sql;
    }

    private LinkedHashMap<Session<?>, ColumnCursor> recordSelectField(LinkedHashMap<String, Session<?>> sessions, String[] fields) {
        LinkedHashMap<Session<?>, ColumnCursor> ans = new LinkedHashMap<>();
        String currClass = "";
        int currLength = 0, start = 0, end = 0;
        Session<?> currSession = null;
        ArrayList<String> currFields = new ArrayList<>();
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            String[] src = field.split("\\.");
            String className = src[0];
            String fieldName = src[1];

            if (className.substring(0, 3).equals("@(#") &&
                    className.charAt(className.length() - 1) == ')') {
                className = className.substring(3, className.length() - 1);
                if (!className.equals(currClass)) {
                    if (i != 0) {
                        end = start + currLength - 1;
                        ans.put(currSession, new ColumnCursor(start, end, currClass, new ArrayList<>(currFields)));
                        start = end + 1;
                        currFields.clear();
                        currLength = 0;
                    }
                    currClass = className;
                    currSession = sessions.get(className);
                }
            }

            if (!fieldName.equals("*")) {
                if (fieldName.substring(0, 2).equals("@(") &&
                        fieldName.charAt(fieldName.length() - 1) == ')') {
                    fieldName = fieldName.substring(2, fieldName.length() - 1);
                    currFields.add(fieldName);
                    currLength++;
                }
            } else {
                LinkedHashMap<String, FieldInfo> allField = currSession.getClassInfo().getFieldInfos();
                currFields.add(session.getClassInfo().getIdInfo().getFieldName());
                for (Map.Entry<String, FieldInfo> entry : allField.entrySet()) {
                    currFields.add(entry.getValue().getFieldName());
                }
                currLength += currSession.getField_length() + 1;
            }
        }
        end = start + currLength - 1;
        ans.put(currSession, new ColumnCursor(start, end, currClass, currFields));
        return ans;
    }

    private void fillAnd() {
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == ',') {
                sql.replace(i, i + 1, " and ");
            }
        }
    }

    public Element findSqlElementByID(String id, String elementType) {
        List<Element> elements = (List<Element>) sqlRoot.elements(elementType);
        for (Element element : elements) {
            if (element.attributeValue("id").equals(id)) {
                return element;
            }
        }
        logger.error("sql not found.");
        logger.info(CommonValue.SUGGESTION+"Please check your sql.xml and id.");
        return null;
    }
}

