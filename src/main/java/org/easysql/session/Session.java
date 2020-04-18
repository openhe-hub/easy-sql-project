package org.easysql.session;

import lombok.Getter;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.easysql.configuration.Configuration;
import org.easysql.utils.*;
import org.easysql.info.constraint.ConstraintType;
import org.easysql.info.orm.*;
import org.easysql.parser.SqlSession;

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
    private final Class<T> beanClass;
    @Getter
    private String tableName;
    @Getter
    private String sqlFileName;
    private String xmlConfigName;
    private SessionHandler<T> sessionHandler;
    private SqlSession<T> sqlSession;
    @Getter
    private int field_length;
    private final Logger logger;

    public Session(Class<T> beanClass){
        this.beanClass=beanClass;
        String canonicalName =beanClass.getCanonicalName();
        String[] pathArr=canonicalName.split("\\.");
        this.className =pathArr[pathArr.length-1];
        logger = Configuration.createLogger(Session.class);
        SessionConfiguration sessionConfiguration=Configuration.getConfiguration(className);
        if (sessionConfiguration!=null){
            logger.info(CommonValue.PROCESS + "Getting session(" + className + ") 's configuration finished.");
            tableName = sessionConfiguration.getTableName();
            xmlConfigName = sessionConfiguration.getConfigXmlName();
            init();
            SessionManager.registerSession(this);
        } else {
            logger.fatal(CommonValue.ERROR + "Getting session " + className + " 's configuration failed.");
        }
    }

    public SessionHandler<T> getHandler(){
        if (sessionHandler!=null){
            logger.info(CommonValue.PROCESS + "SessionHandler(" + className + ") has been built successfully.");
            return sessionHandler;
        }
        else {
            logger.error(CommonValue.ERROR + "SessionHandler(" + className + ") is null.");
            logger.info(CommonValue.SUGGESTION + "Please init first.");
            return null;
        }
    }

    public SqlSession<T> getSqlSession(){
        if (sqlSession!=null){
            logger.info(CommonValue.PROCESS + "SqlSession(" + className + ") has been built successfully.");
            return sqlSession;
        }
        else {
            logger.warn(CommonValue.WARNING + "SqlSession(" + className + ") is null.");
            return null;
        }
    }

    public Object getInstance(){
        try {
            return  beanClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void init(){
        loadXMLConfig();
        this.sessionHandler=new SessionHandler<>(this);
        if (sqlFileName!=null) {
            this.sqlSession=new SqlSession<>(this,sessionHandler);
        }
        if (classInfo != null) {
            logger.info(CommonValue.PROCESS + "Initiating session(" + className + ") finished.");
        }
        else{
            logger.fatal(CommonValue.ERROR + "Initiating session(" + className + ") failed.");
            logger.debug(CommonValue.SUGGESTION+" Please check your configuration file");
        }
    }

    /*自动创建或更新表：
    *  1.确保已初始化
    *  2.自动检测是否表已经创建，若已创建，则更新
    *  3.若表结构不需更新，无需用此方法
    *  4.若需更改表名，请调用alter_table_name方法*/
    public void create(){
        sessionHandler.createTable();
        logger.info(CommonValue.PROCESS + "Table(" + tableName + ") has been created successfully.");
    }

    //删除所有数据
    public void clear(){
        sessionHandler.delete("*","1=1");
        logger.info(CommonValue.PROCESS+"Table("+tableName+") 's data have been deleted successfully.");
    }

    //删除所有数据
    public void destroy(){
        sessionHandler.deleteTable();
        logger.info(CommonValue.PROCESS+"Table("+tableName+") has been deleted successfully.");
    }

    public void close(){
        sessionHandler=null;
        classInfo=null;
        System.gc();
    }

    //private method
    private void loadXMLConfig() {
        if (xmlConfigName.equals("")) {
            logger.error(CommonValue.ERROR + "Mapping xml not found.Can't read Session(" + className + ") 's configuration.");
            logger.info(CommonValue.SUGGESTION + "Please check your mapping xml and center_config.xml.");
        } else {
            Element root=XmlHelper.getRootElement(Configuration.getFile(this.getClass(),xmlConfigName));
            Element classElement=root.element("class");
            Element set=classElement.element("set");
            getIdInfo(classElement.element("id"));
            LinkedHashMap<String,String[]> classMap = getClassInfo(classElement);
            ArrayList<LinkedHashMap<String, FieldInfo>>  fieldMaps= getFieldInfo(classElement.element("fields"));
            LinkedHashMap<String,FieldInfo> fieldMap=fieldMaps.get(0);
            LinkedHashMap<String,FieldInfo> columnMap=fieldMaps.get(1);
            ArrayList<ForeignKeyInfo> foreignKeyList= null;
            ArrayList<IndexInfo> indexList= null;
            LinkedHashMap<String, JoinInfo> joinList= null;
            if (set!=null) {
                foreignKeyList = getForeignKeyInfo(set);
                indexList = getIndexInfo(set);
                joinList = getJoinMap(set);
            }
            classInfo=new ClassInfo(classMap,fieldMap,columnMap,idInfo,foreignKeyList,indexList,joinList);
        }
    }
    private LinkedHashMap<String,String[]> getClassInfo(Element classElement ) {
        className =classElement.attributeValue("class_name");
        tableName =classElement.attributeValue("table_name");
        sqlFileName= classElement.attributeValue("sql_file");
        LinkedHashMap<String,String[]> classMap=new LinkedHashMap<>();
        classMap.put(className,new String[]{className, tableName,sqlFileName});
        return classMap;
    }

    private void getIdInfo(Element idElement){
        String field_name=idElement.attributeValue("field_name");
        String id_name=idElement.attributeValue("id_name");
        String field_type=idElement.attributeValue("field_type");
        String id_type=idElement.attributeValue("id_type");
        String type=idElement.attributeValue("type");
        ConstraintType[] constraintTypes=getConstraintTypes(idElement);
        String[] finished_info=generate_info(new String[]{field_name,field_type,id_name,id_type});
        idInfo=new IdInfo(finished_info,constraintTypes,type);
    }

    private ArrayList<LinkedHashMap<String, FieldInfo>>  getFieldInfo(Element field_element)
    {
        List<Element> field_list=field_element.elements("field");
        field_length=field_list.size();
        LinkedHashMap<String,FieldInfo> fieldMap=new LinkedHashMap<>();
        LinkedHashMap<String,FieldInfo> columnMap=new LinkedHashMap<>();

        for (Element e:field_list){
            String field_name=e.attributeValue("field_name");
            String column_name=e.attributeValue("column_name");
            String field_type=e.attributeValue("field_type");
            String column_type=e.attributeValue("column_type");
            String[] finished_info=generate_info(new String[]{field_name,field_type,column_name,column_type});
            ConstraintType[] constraintTypes = getConstraintTypes(e);
            FieldInfo fieldInfo=new FieldInfo(finished_info,constraintTypes);
            fieldMap.put(fieldInfo.getFieldName(),fieldInfo);
            columnMap.put(fieldInfo.getColumnName(),fieldInfo);
        }
        ArrayList<LinkedHashMap<String, FieldInfo>> ans=new ArrayList<>();
        ans.add(fieldMap);
        ans.add(columnMap);
        return ans;
    }

    private ArrayList<ForeignKeyInfo> getForeignKeyInfo(Element set){
        List<Element>  foreignKeys=set.elements("foreign_key");
        if (foreignKeys!=null) {
            ArrayList<ForeignKeyInfo> fkList=new ArrayList<>();
            for (Element fkElement:foreignKeys) {
                String fromTable= tableName;
                String fromColumn=fkElement.attributeValue("from");
                String toInfo=fkElement.attributeValue("to");
                String[] toInfos=toInfo.split("\\.");
                ConstraintType type=ConstraintType.fromConstraintType(fkElement.attributeValue("type"));
                String name=null;
                if ((name=fkElement.attributeValue("name"))==null){
                    name="fk_"+fromTable+"_"+toInfos[0]+"_to_"+toInfos[1];
                }
                fkList.add(new ForeignKeyInfo(fromTable,toInfos[0],fromColumn,toInfos[1],type,name));
            }
            return fkList;
        }
        return null;
    }

    private ArrayList<IndexInfo> getIndexInfo(Element set){
        List<Element> indexElements=set.elements("index");
        if (indexElements!=null) {
            ArrayList<IndexInfo> index_list=new ArrayList<>();
            for (Element index_element:indexElements) {
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
        return null;
    }

    private LinkedHashMap<String, JoinInfo> getJoinMap(Element set){
        List<Element> joinElements=set.elements("join");
        if (joinElements!=null) {
            LinkedHashMap<String, JoinInfo> join_list=new LinkedHashMap<>();
            for (Element join_element:joinElements) {
               String type=join_element.attributeValue("type");
               String form=join_element.attributeValue("form");
               String from_field=join_element.attributeValue("from_field");
               String to_class=join_element.attributeValue("to_class");
               String[] point=join_element.attributeValue("point").split("->");
               String condition=join_element.attributeValue("cond");
               String from_class_name= getClassName();
               condition= (condition==null)?"=":condition;
               join_list.put(to_class,new JoinInfo(from_class_name,from_field,to_class,
                       ConstraintType.fromConstraintType(type),ConstraintType.fromConstraintType(form)
                       ,point,condition));
            }
            return join_list;
        }
        return null;
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
        if (src[3]==null){//if choose default setting
            src[3] = judgeType(src[1], src[0]);//EasySql will
        }
        return src;
    }

    private String judgeType(String originType, String originName) {
        if (originType == null) {
            logger.error(CommonValue.ERROR + "Field type(" + originName + ") is null.Generating column type automatically failed.");
            return null;
        }
        else {
            switch (originType) {
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
                case "java.util.Date":{
                    return "datetime";//default date type in EasySql is datetime.
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
