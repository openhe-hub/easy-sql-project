package org.easysql.helper;

import lombok.*;
import org.apache.commons.beanutils.BeanUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.easysql.info.ConstraintType;
import org.easysql.info.FieldInfo;
import org.easysql.info.Join;
import org.easysql.session.Session;
import org.easysql.session.SessionHandler;
import org.easysql.session.SessionManager;
import org.w3c.dom.NodeList;


import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class XmlHelper {
    @Setter
    @Getter
    private static String CONFIG_PATH = "";
    @Getter
    private static final String CONFIG_FILE_TYPE = ".xml";
    private static String center_config_path = "";
    private static String sql_path = "";
    @Setter
    public static String sql_xml_name = "";
    private static SAXReader saxReader;

    private static Document configure_doc;
    private static Document sql_doc;
    private static Element sql_root;
    @Setter
    private Session session;
    @Setter
    private SessionHandler handler;
    @Getter
    @Setter
    private StringBuilder sql;
    @Getter
    private ArrayList<String> paras;
    @Getter
    private int paras_cursor;

    public static Element getRootElement(String config_name) {
        Element element = null;
        center_config_path = CONFIG_PATH + config_name + CONFIG_FILE_TYPE;
        saxReader = new SAXReader();
        File configure_file = new File(center_config_path);
        try {
            configure_doc = saxReader.read(configure_file);
            element = configure_doc.getRootElement();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        if (element != null) {
            return element;
        } else {
            System.out.println("config name error!");
            return null;
        }
    }

    //动态sql解析
    public void init_sql_parser(String sql_xml_name, Session default_session, SessionHandler default_handler) {
        session = default_session;
        handler = default_handler;
        paras = new ArrayList<>();
        StringBuilder config_pkg = new StringBuilder(CONFIG_PATH);
        sql_path = config_pkg.replace(config_pkg.length() - 7, config_pkg.length() - 1, "sql").toString();
        File sql_file = new File(sql_path + sql_xml_name + CONFIG_FILE_TYPE);
        try {
            sql_doc = saxReader.read(sql_file);
            sql_root = sql_doc.getRootElement();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    //where条件解析
    public XmlHelper parseCondition(String id) {
        Element where_element = findSqlElementByID(id, "where");
        if (where_element != null) {
            sql = new StringBuilder(where_element.getText());
            fillAnd();
            return this;
        } else {
            System.out.println("error:Condition not found!Please check your sql.xml and id.");
            return null;
        }
    }

    public XmlHelper parseColumns(String id) {
        Element column_element = findSqlElementByID(id, "fields");
        if (column_element != null) {
            sql = new StringBuilder(column_element.getText());
            return this;
        } else {
            System.out.println("error:Condition not found!Please check your sql.xml and id.");
            return null;
        }
    }

    //单表查询
    public <T> ArrayList<T> parseSelect(String id, T bean, String[] datas) {
        Element select_element = findSqlElementByID(id, "select");
        String select_class = select_element.attributeValue("class");
        String[] classes = select_class.split(",");
        int num = classes.length;
        if (num == 1) {
            return select_one_table(select_element, bean, datas);
        } else if (num > 1) {
            return select_tables(select_element, bean, datas, classes);
        } else {
            System.out.println("error:No class to select!Please add 'class' attribute value.");
            return null;
        }
    }

    private <T> ArrayList<T> select_one_table(Element select_element, T bean, String[] datas) {
        ArrayList<T> result_list;
        AnalyseSelectPackage analyseSelectPackage = analyze_select_sql(select_element, bean, datas, new ArrayList<>());
        result_list = handler.select(analyseSelectPackage.getTo_select(), analyseSelectPackage.getCondition(),
                analyseSelectPackage.getSelect_paras());
        return result_list;
    }

    //!!! unfinished method
    private <T> ArrayList<T> select_tables(Element select_element, T bean, String[] datas, String[] classes) {
        ArrayList<T> result_list=null;
        ResultSet rs=null;
        ResultSetMetaData rsmd=null;
        //analyze
        AnalyseMultiSelectPackage analyseMultiSelectPackage=analyse_multi_select_sql(select_element,bean,datas,new ArrayList<>());
        //fill paras and get result
        PreparedStatement pstmt=DBConnector.getPreparedStatement(analyseMultiSelectPackage.getSql().toString());
        ArrayList<Object> paras=analyseMultiSelectPackage.getSelect_paras();
        LinkedHashMap<Session,ColumnCursor> columnCursor=analyseMultiSelectPackage.getColumn_cursor();

        try {
            if (paras!=null&&paras.size()>0) {
                for (int i = 0; i < paras.size(); i++) {
                    pstmt.setObject(i + 1, paras.get(i));
                }
            }
            rs = pstmt.executeQuery();
            if (rs!=null){
                rsmd=rs.getMetaData();
                while (rs.next()){
                    for(Map.Entry<Session,ColumnCursor> cursorEntry:columnCursor.entrySet()){
                        Session session=cursorEntry.getKey();
                        ColumnCursor cursor=cursorEntry.getValue();
                        Object new_bean=session.getInstance();
                        for(int i=cursor.start;i<=cursor.end;i++){
                            Object obj=rs.getObject(i+1);
                            BeanUtils.setProperty(new_bean,cursor.getFields().get(i),obj);
                        }
                    }

                }
            }
        } catch (SQLException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private <T> AnalyseMultiSelectPackage analyse_multi_select_sql(Element select_element,T bean,String[] datas,ArrayList<Object> select_paras){
        ArrayList<String> table_names=new ArrayList<>();
        int data_cursor = 0;
        StringBuilder to_select = new StringBuilder();
        StringBuilder condition = new StringBuilder();
        StringBuilder sql=new StringBuilder("select ");
        LinkedHashMap<String,Session> sessions=new LinkedHashMap<>();
        LinkedHashMap<Session,ColumnCursor>  column_cursor=new LinkedHashMap<>();

        String main_class=select_element.attributeValue("return");
        Session main_session = SessionManager.select_session_by_class_name(main_class);
        String main_table= main_session.getTable_name();

        String[] classes=select_element.attributeValue("class").split(",");
        sessions.put(main_class,main_session);
        for (String class_name : classes) {
            Session session=SessionManager.select_session_by_class_name(class_name);
            if (session==null) {
                System.out.println("error:class:"+class_name+" not found!Please check your sql.xml");
                return null;
            }
            if (!session.getClass_name().equals(main_class)) {
                sessions.put(class_name,session);
                table_names.add(session.getTable_name());
            }
        }


        List<Element> sub_elements = select_element.elements();
        for (Element sub_element : sub_elements) {
            String element_name = sub_element.getName();
            String text = sub_element.getText();
            switch (element_name) {
                case "fields": {
                    setSql(new StringBuilder(text));
                    String[] fields=getSql().toString().split(",");
                    column_cursor=record_select_field(sessions,fields);
                    to_select =multi_fill(fields,sessions);
                    sql.append(to_select);
                    sql.append("\nfrom "+SessionManager.select_session_by_class_name(main_class).getTable_name()+"\n");
                }
                break;
                case "join":{
                    String join_class=sub_element.attributeValue("join");
                    Join join =SessionManager.getJoin(main_class,join_class);
                    String form=join.getForm().getConstraint_type();
                    String join_table=SessionManager.select_session_by_class_name(join_class).getTable_name();
                    String[] point=join.getPoint();
                    String join_condition=join.getCondition();
                    condition=new StringBuilder(" "+form+" "+join_table+" on "+main_table+"."+point[0]+join_condition+join_table+"."+point[1]);

                    Element where=sub_element.element("where");
                    if(where!=null){
                        condition.append(" and ");
                        setSql(new StringBuilder(where.getText()));
                        fillAnd();
                        String[] sub_datas = Arrays.copyOfRange(datas, data_cursor, datas.length);
                        if (bean!=null&&datas!=null){
                            fill(bean, sub_datas);
                        }else if(bean==null&&datas!=null){
                            fill(sub_datas);
                        }else if(bean==null&&datas==null){
                            fill();
                        }
                        data_cursor = getParas_cursor();
                        condition.append(getSql());
                        select_paras.addAll(getParas());
                    }
                    sql.append(condition+"\n");
                }
                break;
            }
        }
        sql.append(";\n");
        return new AnalyseMultiSelectPackage(sessions,column_cursor,sql);
    }

    private <T> AnalyseSelectPackage analyze_select_sql(Element select_element, T bean, String[] datas, ArrayList<Object> select_paras) {
        String table_name;
        int data_cursor = 0;
        StringBuilder to_select = new StringBuilder();
        StringBuilder condition = new StringBuilder();
        List<Element> sub_elements = select_element.elements();
        for (Element sub_element : sub_elements) {
            String element_name = sub_element.getName();
            String text = sub_element.getText();
            switch (element_name) {
                case "fields": {
                    setSql(new StringBuilder(text));
                    to_select = fill().getSql().append("\n");
                }
                break;
                case "from": {
                    String class_name = text;
                    table_name = session.getTable_name();
                    if (!class_name.equals(session.getClass_name()) ||
                            !table_name.equals(handler.getTable_name())) {
                        System.out.println("error:session or handler not suit!");
                        return null;
                    }
                }
                break;
                case "where": {
                    setSql(new StringBuilder(text));
                    fillAnd();
                    String[] sub_datas = Arrays.copyOfRange(datas, data_cursor, datas.length);
                    if (bean!=null&&datas!=null){
                        fill(bean, sub_datas);
                    }else if(bean==null&&datas!=null){
                        fill(sub_datas);
                    }else if(bean==null&&datas==null){
                        fill();
                    }
                    data_cursor = getParas_cursor();
                    condition = getSql();
                    select_paras.addAll(getParas());
                }
                break;
                case "order": {
                    String type = sub_element.attributeValue("type");
                    setSql(new StringBuilder(text));
                    fill();
                    condition.append("\n order by ").append(getSql()).append(" ").append(type);
                }
                break;
                case "limit": {
                    setSql(new StringBuilder(text));
                    fill(Arrays.copyOfRange(datas, data_cursor, datas.length));
                    condition.append("\n limit ").append(text);
                    ArrayList<Integer> list = new ArrayList<>();
                    for (String para : paras) {
                        list.add(Integer.parseInt(para));
                    }
                    select_paras.addAll(list);
                }
                break;
            }
        }
        return new AnalyseSelectPackage(to_select, condition, select_paras);
    }


    public <T> XmlHelper fill(T bean, String[] datas) {
        paras.clear();
        if (session != null) {
            //填充datas数据
            if (datas != null) {
                paras_cursor = 0;
                for (int i = 0; i < sql.length(); i++) {
                    char c = sql.charAt(i);
                    if (c == '?') {
                        paras.add(datas[paras_cursor++]);
                    }
                }
            } else {
                System.out.println("warning:no datas to fill!");
            }
            //填充bean数据
            if (bean != null) {
                if (bean.getClass().equals(session.getBeanClass())) {
                    int start_fill = 0;
                    int end_fill = 0;
                    boolean is_fill_data = false;
                    boolean is_fill_column = false;
                    for (int i = 0; i < sql.length(); i++) {
                        char c = sql.charAt(i);
                        if (c == '@') {//获取开始填充标记@
                            start_fill = i + 2;
                            if (sql.charAt(i + 1) == '{') {//获取开始填充数据标记{
                                is_fill_data = true;
                            } else if (sql.charAt(i + 1) == '(') {//获取开始填充字段标记(
                                is_fill_column = true;
                            }
                        } else if (c == '}' && is_fill_data) {//获取结束填充数据标记}，并填充数据
                            end_fill = i - 1;
                            i = fill_bean_data(bean, sql, start_fill, end_fill, i);
                            is_fill_data = false;
                        } else if (c == ')' && is_fill_column) {//获取结束填充字段标记)，并填充字段
                            end_fill = i - 1;
                            i = fill_column(start_fill, end_fill, i);
                            is_fill_column = false;
                        }
                    }
                } else {
                    System.out.println("error:Class type not equal!");
                }
            } else {
                System.out.println("warning:no bean to fill!");
            }
        } else {
            System.out.println("error:No session to get class info!");
        }

        return this;
    }

    //only fill columns
    public XmlHelper fill() {
        int start_fill = 0;
        int end_fill = 0;
        boolean is_fill_column = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '@' && sql.charAt(i + 1) == '(') {
                start_fill = i + 2;
                is_fill_column = true;
            } else if (c == ')' && is_fill_column) {
                end_fill = i - 1;
                i = fill_column(start_fill, end_fill, i);
                is_fill_column = false;
            }
        }
        return this;
    }

    //fill columns and datas
    public XmlHelper fill(String[] datas) {
        paras.clear();
        fill();
        fill_data(datas, sql);
        return this;
    }


    private int fill_column(int start_fill, int end_fill, int i) {
        String column_name = null;
        String field = sql.substring(start_fill, end_fill + 1);
        if (field.equals(session.getClassInfo().getIdInfo().getField_name())) {//主键
            column_name = session.getClassInfo().getIdInfo().getColumn_name();
        } else if (field.equals("#")) {//类名
            column_name = session.getTable_name();
        }else if(field.charAt(0)=='#'){//多表类名
            String class_name=field.substring(1);
            column_name= SessionManager.select_session_by_class_name(class_name).getTable_name();
        }
        else {//字段名
            column_name = session.getClassInfo().getField_infos().get(field).getColumn_name();
        }
        if (column_name != null) {
            sql.replace(start_fill - 2, end_fill + 2, column_name);//注入字段
            i = start_fill - 2 + column_name.length();
        } else {
            System.out.println("error:Field not found!Please check your sql.xml.");
        }
        return i;
    }

    private <T> int fill_bean_data(T bean, StringBuilder sql, int start_fill, int end_fill, int i) {
        String field = sql.substring(start_fill, end_fill + 1);
        try {
            String data = "\'" + BeanUtils.getProperty(bean, field) + "\'";
            paras.add(data);//注入数据到参数集
            sql.replace(start_fill - 2, end_fill + 2, "?");//先使用占位符替代，到handler层注入
            i = start_fill - 2 + data.length();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return i;
    }

    private StringBuilder fill_data(String[] datas, StringBuilder sql) {
        if (datas.length > 0) {
            paras_cursor = 0;
            for (int i = 0; i < sql.length(); i++) {
                char c = sql.charAt(i);
                if (c == '?') {
                    paras.add(datas[paras_cursor++]);
                }
            }
        } else {
            System.out.println("warning:no datas to fill data!");
        }
        return sql;
    }

    //multi-tables fill column
    private StringBuilder multi_fill(String[] fields,LinkedHashMap<String,Session> sessions){
        StringBuilder sql=new StringBuilder();
        for (String field : fields) {
            String[] src = field.split("\\.");
            String class_name = src[0];
            String field_name = src[1];
            Session session = null;

            if (class_name.substring(0, 3).equals("@(#") &&
                    class_name.charAt(class_name.length() - 1) == ')') {
                class_name = class_name.substring(3,class_name.length()-1);
                session = sessions.get(class_name);
                class_name = session.getTable_name();
            }
            sql.append(class_name + ".");

            if(!field_name.equals("*")){
                if (field_name.substring(0, 2).equals("@(") &&
                        field_name.charAt(field_name.length() - 1) == ')') {
                    field_name = field_name.substring(2,field_name.length()-1);
                    field_name = field_name.equals(session.getClassInfo().getIdInfo().getColumn_name()) ?
                            session.getClassInfo().getIdInfo().getColumn_name() :
                            session.getClassInfo().getField_infos().get(field_name).getColumn_name();
                }
            }
            sql.append(field_name + ",");
        }
        sql.deleteCharAt(sql.length()-1);
        return sql;
    }

    //undebuged
    private LinkedHashMap<Session,ColumnCursor> record_select_field(LinkedHashMap<String,Session> sessions,String[] fields){
        LinkedHashMap<Session,ColumnCursor> ans=new LinkedHashMap<>();
        String curr_class="";
        int curr_length=0,start=0,end=0;
        Session curr_session=null;
        ArrayList<String> curr_fields=new ArrayList<>();
        for (int i=0;i<fields.length;i++) {
            String field=fields[i];
            String[] src = field.split("\\.");
            String class_name = src[0];
            String field_name = src[1];

            if (class_name.substring(0, 3).equals("@(#") &&
                    class_name.charAt(class_name.length() - 1) == ')') {
                class_name = class_name.substring(3,class_name.length()-1);
                if(!class_name.equals(curr_class)){
                    if (i!=0) {
                        end=start+curr_length-1;
                        ans.put(curr_session,new ColumnCursor(start,end,curr_class,new ArrayList<>(curr_fields)));
                        start=end+1;
                        curr_fields.clear();
                        curr_length=0;
                    }
                    curr_class=class_name;
                    curr_session=sessions.get(class_name);
                 }
            }

            if(!field_name.equals("*")){
                if (field_name.substring(0, 2).equals("@(") &&
                        field_name.charAt(field_name.length() - 1) == ')') {
                    field_name = field_name.substring(2,field_name.length()-1);
                    curr_fields.add(field_name);
                    curr_length++;
                }
            }else{
                LinkedHashMap<String, FieldInfo> all_field = curr_session.getClassInfo().getField_infos();
                for(Map.Entry<String,FieldInfo> entry:all_field.entrySet()){
                    curr_fields.add(entry.getValue().getField_name());
                }
                curr_length+=curr_session.getField_length();
            }
        }
        end=start+curr_length-1;
        ans.put(curr_session,new ColumnCursor(start,end,curr_class,curr_fields));
        return ans;
    }

    private void fillAnd() {
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == ',') {
                sql.replace(i, i + 1, " and ");
            }
        }
    }

    private Element findSqlElementByID(String id, String element_type) {
        List<Element> elements = sql_root.elements(element_type);
        for (Element element : elements) {
            if (element.attributeValue("id").equals(id)) {
                return element;
            }
        }
        System.out.println("error:Condition not found!Please check your sql.xml and id.");
        return null;
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class AnalyseSelectPackage {
    StringBuilder to_select;
    StringBuilder condition;
    ArrayList<Object> select_paras;
}

@Data@AllArgsConstructor@NoArgsConstructor
class AnalyseMultiSelectPackage extends AnalyseSelectPackage{
     LinkedHashMap<String,Session> sessions;
     LinkedHashMap<Session,ColumnCursor>  column_cursor;
     StringBuilder sql;

    public AnalyseMultiSelectPackage(StringBuilder to_select, StringBuilder condition, ArrayList<Object> select_paras, LinkedHashMap<String, Session> sessions, LinkedHashMap<Session, ColumnCursor> column_cursor, StringBuilder sql) {
        super(to_select, condition, select_paras);
        this.sessions = sessions;
        this.column_cursor = column_cursor;
        this.sql = sql;
    }
}

@Data@NoArgsConstructor@AllArgsConstructor
class ColumnCursor{
    int start;//start(inclusive)
    int end;//2->end(inclusive)
    String class_name;
    ArrayList<String> fields;
}


