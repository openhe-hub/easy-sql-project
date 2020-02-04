package org.easysql.session;

import lombok.Getter;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.easysql.helper.*;
import org.easysql.info.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Session<T> {
    @Getter
    private ClassInfo classInfo;
    private IdInfo idInfo;
    @Getter
    private String className;
    @Getter
    private Class BeanClass;
    @Getter
    private String tableName;
    private String xmlConfigName;
    private SessionHandler<T> sessionHandler;
    @Getter
    private int field_length;
    private Logger logger=Logger.getLogger(Session.class);

    public Session(String className){
        this.className =className;
        try {
            BeanClass = Class.forName(Configuration.getBean_pkg() + "." + className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        SessionConfiguration sessionConfiguration=Configuration.getConfiguration(className);
        if (sessionConfiguration!=null){
            logger.info(CommonValue.PROCESS+"getting session "+className+" 's configuration finished." );
        }
        else {
            logger.fatal("getting session "+className+" 's configuration failed.");
        }
        tableName =sessionConfiguration.getTableName();
        xmlConfigName =sessionConfiguration.getConfigXmlName();
        SessionManager.registerSession(this);
        init();
    }

    public SessionHandler<T> getHandler(){
        if (sessionHandler!=null){
            return sessionHandler;
        }
        else {
            System.out.println("error:Sessionhandler is null.Please init first!");
            return null;
        }
    }

    public Object getInstance(){
        try {
            return  BeanClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void init(){
        getConfig();
        this.sessionHandler=new SessionHandler<>(this);
        if (classInfo!=null&&sessionHandler!=null){
            logger.info(CommonValue.PROCESS+"Init finished.");
        }
        else{
            logger.fatal(" Configuration failed.");
            logger.debug(CommonValue.SUGGESTION+" Please check your configuration file");
        }
    }

    /*自动创建或更新表：
    *  1.确保已初始化
    *  2.自动检测是否表已经创建，若已创建，则更新
    *  3.若表结构不需更新，无需用此方法
    *  4.若需更改表名，请调用alter_table_name方法*/
    public void create(){
        if (!sessionHandler.if_table_exists()){
            sessionHandler.create_table();
        }
        else {
            sessionHandler.update_table();
        }
    }

    //更改表名：仅用于表名的更改
    public void alter_table_name(String old_name){
        getConfig();
        sessionHandler=new SessionHandler<T>(this);
        sessionHandler.alter_table_name(old_name);
    }

    //删除所有数据
    public void clear(){
        sessionHandler.delete("*","1=1");
    }

    //删除所有数据
    public void destroy(){
        sessionHandler.delete_table();
    }

    public void close(){
        sessionHandler=null;
        classInfo=null;
        System.gc();
        DBConnector.close();
    }

    //private method
    private void getConfig() {
        if (xmlConfigName.equals("")) {
            System.out.println("error:mapping xml not found!Please check your mapping xml and center_config.xml");
        } else {
            Element root=XmlHelper.getRootElement(xmlConfigName);
            Element class_element=root.element("class");
            Element set=class_element.element("set");
            getIdInfo(class_element.element("id"));
            LinkedHashMap<String,String[]> class_map = getClassInfo(class_element);
            ArrayList<LinkedHashMap<String,FieldInfo>>  field_maps= getFieldInfo(class_element.element("fields"));
            LinkedHashMap<String,FieldInfo> field_map=field_maps.get(0);
            LinkedHashMap<String,FieldInfo> column_map=field_maps.get(1);
            ArrayList<ForeignKeyInfo> fk_list= getForeignKeyInfo(set);
            ArrayList<IndexInfo> index_list=getIndexInfo(set);
            LinkedHashMap<String,Join> join_list= getJoinMap(set);
            classInfo=new ClassInfo(class_map,field_map,column_map,idInfo,fk_list,index_list,join_list);
        }
    }
    private LinkedHashMap<String,String[]> getClassInfo(Element class_element ) {
        className =class_element.attributeValue("class_name");
        tableName =class_element.attributeValue("table_name");
        LinkedHashMap<String,String[]> class_map=new LinkedHashMap<String, String[]>();
        class_map.put(className,new String[]{className, tableName});
        return class_map;
    }

    private void getIdInfo(Element id_element){
        String field_name=id_element.attributeValue("field_name");
        String id_name=id_element.attributeValue("id_name");
        String field_type=id_element.attributeValue("field_type");
        String id_type=id_element.attributeValue("id_type");
        String type=id_element.attributeValue("type");
        ConstraintType[] constraintTypes=getConstraintTypes(id_element);
        String[] finished_info=generate_info(new String[]{field_name,field_type,id_name,id_type});
        idInfo=new IdInfo(finished_info,constraintTypes,type);
    }

    private ArrayList<LinkedHashMap<String, FieldInfo>>  getFieldInfo(Element field_element)
    {
        List<Element> field_list=field_element.elements("field");
        field_length=field_list.size();
        LinkedHashMap<String,FieldInfo> field_map=new LinkedHashMap<>();
        LinkedHashMap<String,FieldInfo> column_map=new LinkedHashMap<>();

        for (Element e:field_list){
            String field_name=e.attributeValue("field_name");
            String column_name=e.attributeValue("column_name");
            String field_type=e.attributeValue("field_type");
            String column_type=e.attributeValue("column_type");
            String[] finished_info=generate_info(new String[]{field_name,field_type,column_name,column_type});
            ConstraintType[] constraintTypes = getConstraintTypes(e);
            FieldInfo fieldInfo=new FieldInfo(finished_info,constraintTypes);
            field_map.put(field_name,fieldInfo);
            column_map.put(column_name,fieldInfo);
        }
        ArrayList<LinkedHashMap<String, FieldInfo>> ans=new ArrayList<>();
        ans.add(field_map);
        ans.add(column_map);
        return ans;
    }

    private ArrayList<ForeignKeyInfo> getForeignKeyInfo(Element set){
        List<Element> fk_elements=set.elements("foreign_key");
        ArrayList<ForeignKeyInfo> fk_list=new ArrayList<>();
        for (Element fk_element:fk_elements) {
            String from_table= tableName;
            String from_column=fk_element.attributeValue("from");
            String to_info=fk_element.attributeValue("to");
            String[] to_infos=to_info.split("\\.");
            ConstraintType type=ConstraintType.fromConstraintType(fk_element.attributeValue("type"));
            String name=null;
            if ((name=fk_element.attributeValue("name"))==null){
                name="fk_"+from_table+"_"+to_infos[0]+"_to_"+to_infos[1];
            }
            fk_list.add(new ForeignKeyInfo(from_table,to_infos[0],from_column,to_infos[1],type,name));
        }
        return fk_list;

    }

    private ArrayList<IndexInfo> getIndexInfo(Element set){
        List<Element> index_elements=set.elements("index");
        ArrayList<IndexInfo> index_list=new ArrayList<>();
        for (Element index_element:index_elements) {
            String field_name=index_element.attributeValue("field");
            ConstraintType type=ConstraintType.fromConstraintType(index_element.attributeValue("type"));
            String name=null;
            if ((name=index_element.attributeValue("name"))==null){
                name="index_"+field_name;
            }
            index_list.add(new IndexInfo(field_name,name,type));
        }
        return index_list;
    }

    private LinkedHashMap<String,Join> getJoinMap(Element set){
        List<Element> join_elements=set.elements("join");
        LinkedHashMap<String,Join> join_list=new LinkedHashMap<>();
        for (Element join_element:join_elements) {
           String type=join_element.attributeValue("type");
           String form=join_element.attributeValue("form");
           String from_field=join_element.attributeValue("from_field");
           String to_class=join_element.attributeValue("to_class");
           String[] point=join_element.attributeValue("point").split("->");
           String condition=join_element.attributeValue("cond");
           String from_class_name= getClassName();
           condition= (condition==null)?"=":condition;
           join_list.put(to_class,new Join(from_class_name,from_field,to_class,
                   ConstraintType.fromConstraintType(type),ConstraintType.fromConstraintType(form)
                   ,point,condition));
        }
        return join_list;
    }

    //解析sql约束
    private ConstraintType[] getConstraintTypes(Element e) {
        ConstraintType[] constraintTypes=null;
        if (e.attributeValue("constraint")!=null){
            String[] constraints=e.attributeValue("constraint").split(",");
            constraintTypes=new ConstraintType[constraints.length];

            for (int i = 0; i < constraintTypes.length; i++) {
                constraintTypes[i]=ConstraintType.fromConstraintType(constraints[i]);
            }
        }
        return constraintTypes;
    }

    private String[] generate_info(String[] src){//field_name,filed_type,column_name,column_type
        if (src[2]==null){//如果默认配置column_name
            src[2]=src[0];//将field_name作为column_name
        }
        if (src[3]==null){//如果默认配置column_type
            src[3]=judge_type(src[1]);//根据filed_type智能识别column_type
        }
        return src;
    }

    private String judge_type(String origin_type){
        if (origin_type==null){
            System.out.println("error!:field_type is null!Please complete field type!");
            return null;
        }
        else {
            switch (origin_type) {
                case "int": {
                    return "int";
                }
                case "long": {
                    return "bigint";
                }
                case "double": {
                    return "double";
                }
                case "float": {
                    return "float";
                }
                case "java.lang.String": {
                    return "varchar(255)";
                }
                default:{
                    logger.error(" This field type isn't supported.");
                    logger.info(CommonValue.SUGGESTION+" Please set it in your mapping.xml.");
                    return null;
                }
            }
        }
    }



}
