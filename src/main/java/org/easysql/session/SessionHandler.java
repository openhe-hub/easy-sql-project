package org.easysql.session;

import lombok.Getter;
import org.apache.commons.beanutils.BeanUtils;
import org.easysql.helper.Configuration;
import org.easysql.helper.DBConnector;
import org.easysql.info.ClassInfo;
import org.easysql.info.ConstraintType;
import org.easysql.info.FieldInfo;
import org.easysql.info.IdInfo;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;


public class SessionHandler {
     private  Session session;
     private  ClassInfo classInfos;

     @Getter
     private  LinkedHashMap<String,String[]> class_info;
     @Getter
     private  LinkedHashMap<String, FieldInfo> fields_info;
     @Getter
     private  IdInfo idInfo;
     private  String  table_name;
     private  Class  BeanClass;
     private ResultSet rs;
     private ResultSetMetaData rsmd;
     private PreparedStatement pstmt;

     public SessionHandler(Session session){//将session里的数据解包
         this.session=session;
         classInfos=session.getClassInfo();
         class_info=classInfos.getClass_info();
         fields_info=classInfos.getField_infos();
         idInfo=classInfos.getIdInfo();
         String[] class_str=class_info.get(session.getClass_name());
         table_name=class_str[1];
         try {
             BeanClass=Class.forName(Configuration.getBean_pkg()+"."+session.getClass_name());
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
     }

     //DDL
    public  void create_table(){
        this.create_table(this.table_name);
     }

     public void create_table(String table_name){
         StringBuilder sql=new StringBuilder("create table ");
         sql.append(table_name+"(\n");//拼接表名
         sql.append(idInfo.getColumn_name()+" "+idInfo.getColumn_type()+" primary key "+idInfo.getPk_type()+" "
                 +appendConstraints(idInfo)+",\n");//拼接主键信息
         for (String key:fields_info.keySet()){//拼接字段信息
             FieldInfo fieldInfo=fields_info.get(key);
             String column_name=fieldInfo.getColumn_name();
             String column_type=fieldInfo.getColumn_type();
             String constraint_type= appendConstraints(fieldInfo);
             sql.append(column_name+" "+column_type+" "+constraint_type+",\n");
         }

         sql.deleteCharAt(sql.length()-2);
         sql.append(");");
         DBConnector.executeSQL(sql.toString());
     }

     //拼接sql约束
    private String appendConstraints(FieldInfo fieldInfo) {
         String constraint_type="";
        ConstraintType[] constraints=fieldInfo.getConstraints();
        if(constraints!=null){
            for (ConstraintType type:constraints){
                constraint_type+=type.getConstraint_type()+" ";
            }
        }
        return constraint_type;
    }

    //更新表结构
    public void update_table(){//表名未改变，未增减列
         String temp_name="temp"+new Random().nextInt(10000);
         create_table(temp_name);
             DBConnector.executeSQL("insert into "+temp_name+" select * from "+table_name+";");
             DBConnector.executeSQL("drop table "+table_name+";");
             DBConnector.executeSQL("rename table "+temp_name+" to "+table_name+";");
     }

     //更改表名
     public void alter_table_name(String old_name){
         DBConnector.executeSQL("rename table "+old_name+" to "+table_name+";");
     }

     //表是否存在
     public boolean if_table_exists(){
       String db_name=DBConnector.get_db_Name();
       boolean ans=false;
         try {
             rs=DBConnector.executeQuery("select count(*) from information_schema.TABLES where TABLE_SCHEMA=\'"+db_name+"\' and TABLE_NAME=\'"+table_name+"\';");
             while (rs.next()) {
                 ans = (rs.getInt(1) == 0) ? false : true;
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return ans;
     }
     /*增减列配置请先delete再create*/

    //删除表
    public void delete_table(){
        StringBuilder sql=new StringBuilder("drop table "+table_name+";");
        DBConnector.executeSQL(sql.toString());
    }

     //DML
     //insert
    public void insertAll(Object bean){
         StringBuffer sql=new StringBuffer("insert into "+table_name+" values(");
        try {
            String id_value=BeanUtils.getProperty(bean,idInfo.getField_name());//先填充主键数据
            sql.append("\'"+id_value+"\',");
            for (String key:fields_info.keySet()) {//填充其它数据
                    String values= BeanUtils.getProperty(bean,key);
                    sql.append("\'"+values+"\',");
            }
            sql.deleteCharAt(sql.length()-1);
            sql.append(");");
            DBConnector.executeSQL(sql.toString());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /*注意：该方法用于向部分字段插入数据
    * 1.对于有数据的属性，插入数据
    * 2.对于主键，不插入数据时，mapping.xml中设置type="auto_increment",也会自增
    * 3.对于无数据的属性：
    *   若是基本数据类型，会赋初始值（如double会插入数据：0.0）
    *   若是对象，不会执行数据插入
    * */
    public void insert(Object bean){
         StringBuffer sql=new StringBuffer("insert into "+table_name);
         StringBuffer insert_columns=new StringBuffer("(");
         StringBuffer insert_values=new StringBuffer(" values(");

        try {
            String id_value=BeanUtils.getProperty(bean,idInfo.getField_name());//先填充主键数据
            if (id_value!=null){//拼接id字段和数据
                insert_columns.append(idInfo.getColumn_name()+",");
                insert_values.append("\'"+id_value+"\',");
            }
            for (String key : fields_info.keySet()) {
                String value=BeanUtils.getProperty(bean,key);
                if (value!=null){//拼接其余字段和数据
                    insert_columns.append(fields_info.get(key).getColumn_name()+",");
                    insert_values.append("\'"+value+"\',");
                }
            }
            insert_columns.deleteCharAt(insert_columns.length()-1).append(")");
            insert_values.deleteCharAt(insert_values.length()-1).append(")");
            sql.append(insert_columns).append(insert_values).append(";");//拼接sql
            DBConnector.executeSQL(sql.toString());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public <T> void insertListToTable(ArrayList<T> data_list){
        for (T obj : data_list) {
           insert(obj);
        }
    }

    //update
    public <T> void updateAsID(Object id_value,T bean){
        update(idInfo.getColumn_name()+"="+id_value,bean);
    }

    public <T> void update(String condition,T bean){
        StringBuilder sql=new StringBuilder("update "+table_name+" set ");
        ArrayList<String> column_name=getColumnList();
        ArrayList<String> field_name=getFieldList();
        for (int i = 1; i < column_name.size(); i++) {//主键不更改
            sql.append(column_name.get(i)+"=?,");
        }
        sql.deleteCharAt(sql.length()-1);
        sql.append(" where "+condition+";");
        pstmt=DBConnector.getPreparedStatement(sql.toString());
        try {
            for (int i=1;i<field_name.size();i++){
                pstmt.setObject(i,BeanUtils.getProperty(bean,field_name.get(i)));
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public <T> void updateListToTable(String condition,ArrayList<T> beans){
        for (T bean : beans) {
            update(condition,bean);
        }
    }

    public <T> void updateListToTableAsID(ArrayList<T> beans){
        for (T bean : beans) {
            try {
                updateAsID(BeanUtils.getProperty(bean,idInfo.getColumn_name()),bean);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    //select
    /*
    * @para1:Bean.class
    * @para2:like "=value" or ">value" or... You can append like "and column>value"*/
    public  ArrayList<? extends Object> select(String toSelect,String condition){
        StringBuffer sql=new StringBuffer("select "+toSelect+" from " + table_name);
        if (condition.equals("") == true) {
            sql.append(";");
        } else {
            sql.append(" where " + condition);
        }
        rs=DBConnector.executeQuery(sql.toString());

        ArrayList<String> list=null;
        if (toSelect.equals("*")) {
            list=new ArrayList<>(fields_info.keySet());
            list.add(0,idInfo.getColumn_name());
        } else {
            String[] toSelectColumns=toSelect.split(",");
            list = new ArrayList<String>(Arrays.asList(toSelectColumns));
        }
        ArrayList<Object> beans= (ArrayList<Object>) ResultSetToBean(rs,list);
        return beans;
    }

    public ArrayList<? extends Object> selectAll(){
        return select("*","");
    }

    public Object selectAsID(Object id_value) {
        rs = DBConnector.executeQuery("select * from " + table_name + " where " + idInfo.getColumn_name() + "=" + id_value + ";");
        ArrayList<String> list = getFieldList();
        return ResultSetToBean(rs,list).get(0);
    }

    //get a column string list
    private ArrayList<String> getColumnList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add(idInfo.getField_name());
        for (String key:fields_info.keySet()){
            list.add(fields_info.get(key).getColumn_name());
        }
        return list;
    }

    //get a filed string list
    private ArrayList<String> getFieldList(){
        ArrayList<String> list = new ArrayList<>(fields_info.keySet());
        list.add(0, idInfo.getColumn_name());
        return list;
    }


    private ArrayList<? extends Object> ResultSetToBean(ResultSet rs,ArrayList<String> data_name){//convert resultset to list
        ArrayList<Object> objects=new ArrayList<Object>();
        try {
            ArrayList<ArrayList<String>> origin_data=new ArrayList<ArrayList<String>>();
            rsmd=DBConnector.getRsmd(rs);
            int row_count=0;

            while (rs.next()){//遍历resultset
                ArrayList<String> row_data=new ArrayList<String>();
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    row_data.add(rs.getString(i+1));
                }
                origin_data.add(row_data);
                row_count++;
            }


            for (int i=0;i<row_count;i++) {//将数据进行对应的强制类型转换
                Object obj=BeanClass.newInstance();
                ArrayList<String> strings = origin_data.get(i);
                for (int j = 0; j< strings.size(); j++){
                    BeanUtils.setProperty(obj,data_name.get(j), strings.get(j));
                }
                objects.add(obj);
            }
            return objects;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }


     //delete

    public <T> void deleteAsID(T bean){
        Object id_value= null;
        try {
            id_value = BeanUtils.getProperty(bean,idInfo.getField_name());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        StringBuilder sql=new StringBuilder("delete from "+table_name+" where "+idInfo.getColumn_name()+"="+id_value+";");
        DBConnector.executeSQL(sql.toString());
    }

    public void delete(String condition){
        StringBuilder sql=new StringBuilder("delete from "+table_name+" where "+condition+";");
        DBConnector.executeSQL(sql.toString());
    }

    public <T> void deleteListAsID(ArrayList<T> id_values){
        for (Object id_value : id_values) {
            deleteAsID(id_value);
        }
    }


    //cache
    public <T> Cache startCache(ArrayList<T> data,int mode){
        Cache<T> cache=new Cache<T>(data,mode,this);
        return cache;
    }

}
