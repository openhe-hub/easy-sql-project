package org.easysql.helper;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.beanutils.BeanUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.easysql.session.Session;
import org.easysql.session.SessionHandler;
import org.w3c.dom.NodeList;


import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XmlHelper {
    @Setter
    @Getter
    private static String CONFIG_PATH = "";
    @Getter
    private static final String CONFIG_FILE_TYPE = ".xml";
    private static String center_config_path="";
        private static String sql_path = "";
    @Setter
    public static String sql_xml_name = "";
    private static SAXReader saxReader;

    private static Document configure_doc;
    private static Document sql_doc;
    private static Element sql_root;
    @Setter
    private  Session session;
    @Setter
    private  SessionHandler handler;
    @Getter@Setter
    private  StringBuilder sql;
    @Getter
    private ArrayList<String> paras;
    @Getter
    private int paras_cursor;

    public static Element getRootElement(String config_name) {
        Element element = null;
        center_config_path=CONFIG_PATH+config_name+CONFIG_FILE_TYPE;
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
    public void init_sql_parser(String sql_xml_name, Session default_session,SessionHandler default_handler) {
        session=default_session;
        handler=default_handler;
        paras=new ArrayList<>();
        StringBuilder config_pkg=new StringBuilder(CONFIG_PATH);
        sql_path=config_pkg.replace(config_pkg.length()-7,config_pkg.length()-1,"sql").toString();
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
        Element where_element=findSqlElementByID(id,"where");
        if(where_element!=null){
            sql=new StringBuilder(where_element.getText());
            fillAnd();
            return this;
        }
        else {
            System.out.println("error:Condition not found!Please check your sql.xml and id.");
            return null;
        }
    }

    public XmlHelper parseColumns(String id){
        Element column_element=findSqlElementByID(id,"fields");
        if(column_element!=null){
            sql=new StringBuilder(column_element.getText());
            return this;
        }
        else {
            System.out.println("error:Condition not found!Please check your sql.xml and id.");
            return null;
        }
    }

    //单表查询
    public <T> ArrayList<T> parseSelect(String id,T bean,String[] datas){
        Element select_element=findSqlElementByID(id,"select");
        StringBuilder to_select=new StringBuilder();
        StringBuilder condition=new StringBuilder();
        String table_name="";
        int data_cursor=0;
        ArrayList<Object> select_paras=new ArrayList<>();
        ArrayList<T> result_list=new ArrayList<>();
        List<Element> sub_elements=select_element.elements();
        for (Element sub_element : sub_elements) {
            String element_name=sub_element.getName();
            String text = sub_element.getText();
            switch (element_name){
                case "fields":{
                    setSql(new StringBuilder(text));
                    to_select=fill().getSql().append("\n");
                }break;
                case "from":{
                    String class_name= text;
                    table_name=session.getTable_name();
                    if(!class_name.equals(session.getClass_name())||
                       !table_name.equals(handler.getTable_name())){
                        System.out.println("error:session or handler not suit!");
                        return null;
                    }
                }break;
                case "where":{
                    setSql(new StringBuilder(text));
                    fillAnd();
                    fill(bean,Arrays.copyOfRange(datas,data_cursor,datas.length));
                    data_cursor=getParas_cursor();
                    condition=getSql();
                    select_paras.addAll(getParas());
                }break;
                case "order":{
                    String type=sub_element.attributeValue("type");
                    setSql(new StringBuilder(text));
                    fill();
                    condition.append("\n order by ").append(getSql()).append(" ").append(type);
                }break;
                case "limit":{
                    setSql(new StringBuilder(text));
                    fill(Arrays.copyOfRange(datas,data_cursor,datas.length));
                    condition.append("\n limit ").append(text);
                    ArrayList<Integer> list=new ArrayList<>();
                    for (String para : paras) {
                        list.add(Integer.parseInt(para));
                    }
                    select_paras.addAll(list);
                }break;
            }
        }
        result_list=handler.select(to_select,condition,select_paras);
        return result_list;
    }





    public <T> XmlHelper fill(T bean, String[] datas) {
        paras.clear();
        if (session != null) {
            //填充bean数据
            if (bean != null&&datas!=null&&datas.length>0) {
                if (bean.getClass().equals(session.getBeanClass())) {
                    int start_fill = 0;
                    int end_fill = 0;
                    paras_cursor=0;
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
                            i = fill_column( start_fill, end_fill, i);
                            is_fill_column = false;
                        }else if(c=='?'){
                            paras.add(datas[paras_cursor++]);
                        }
                    }
                } else {
                    System.out.println("error:Class type not equal!");
                }
            } else {
                System.out.println("warning:no data to fill!");
            }
        } else {
            System.out.println("error:No session to get class info!");
        }
        return this;
    }

    //only fill columns
    public XmlHelper fill(){
        int start_fill = 0;
        int end_fill = 0;
        boolean is_fill_column = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '@'&&sql.charAt(i+1)=='(') {
                start_fill = i + 2;
                is_fill_column = true;
            }
            else if (c == ')' && is_fill_column) {
                end_fill = i - 1;
                i = fill_column(start_fill, end_fill, i);
                is_fill_column = false;
            }
        }
        return this;
    }

    //fill columns and datas
    public XmlHelper fill(String[] datas){
        paras.clear();
        fill();
        fill_data(datas,sql);
        return this;
    }


    private int fill_column( int start_fill, int end_fill, int i) {
        String column_name = null;
        String field = sql.substring(start_fill, end_fill + 1);
        if (field.equals(session.getClassInfo().getIdInfo().getField_name())) {
            column_name = session.getClassInfo().getIdInfo().getColumn_name();
        } else {
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
        String field = sql.substring(start_fill, end_fill+1);
        try {
            String data= "\'" + BeanUtils.getProperty(bean, field) + "\'";
            paras.add(data);//注入数据到参数集
            sql.replace(start_fill-2, end_fill+2, "?");//先使用占位符替代，到handler层注入
            i=start_fill-2+data.length();
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

    private void fillAnd() {
        for(int i=0;i<sql.length();i++){
            if(sql.charAt(i)==','){
                sql.replace(i,i+1," and ");
            }
        }
    }

    private Element findSqlElementByID(String id,String element_type){
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


