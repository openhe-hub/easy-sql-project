package org.easysql.session;

import lombok.Getter;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.easysql.helper.CommonValue;
import org.easysql.helper.Configuration;
import org.easysql.helper.DBConnector;
import org.easysql.helper.LoggerHelper;
import org.easysql.info.*;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SessionHandler<T> {
    @Getter
    private LinkedHashMap<String, String[]> class_info;
    @Getter
    private LinkedHashMap<String, FieldInfo> fields_info;
    @Getter
    private LinkedHashMap<String, FieldInfo> columns_info;
    @Getter
    private ArrayList<ForeignKeyInfo> fk_list;
    @Getter
    private ArrayList<IndexInfo> index_list;
    @Getter
    private IdInfo idInfo;
    @Getter
    private String tableName;
    private Class BeanClass;
    private ResultSet rs;
    private ResultSetMetaData resultSetMetaData;
    private PreparedStatement preparedStatement;
    private Logger logger=Logger.getLogger(SessionHandler.class);

    public SessionHandler(Session session) {//将session里的数据解包
        ClassInfo classInfos = session.getClassInfo();
        class_info = classInfos.getClass_info();
        fields_info = classInfos.getField_infos();
        columns_info = classInfos.getColumn_infos();
        fk_list = classInfos.getForeignKeyInfos();
        idInfo = classInfos.getIdInfo();
        index_list = classInfos.getIndexInfos();
        tableName = session.getTableName();
        BeanClass = session.getBeanClass();
    }

    //DDL
    public void createTable() {
        this.createTable(this.tableName);
    }

    public void createTable(String table_name) {
        StringBuilder sql = new StringBuilder("create table ");
        sql.append(table_name).append("(\n");//拼接表名
        sql.append(idInfo.getColumn_name()).append(" ")
                .append(idInfo.getColumn_type()).append(" primary key ")
                .append(idInfo.getPk_type()).append(" ")
                .append(appendConstraints(idInfo))
                .append(",\n");//拼接主键信息
        for (String key : fields_info.keySet()) {//拼接字段信息
            FieldInfo fieldInfo = fields_info.get(key);
            String column_name = fieldInfo.getColumn_name();
            String column_type = fieldInfo.getColumn_type();
            String constraint_type = appendConstraints(fieldInfo);
            sql.append(column_name + " " + column_type + " " + constraint_type + ",\n");
        }

        for (ForeignKeyInfo fk_info : fk_list) {
            sql.append(appendFkConstraints(fk_info));
        }


        if (index_list != null) {
            for (IndexInfo index_info : index_list) {
                sql.append(appendIndex(index_info));
            }
        }

        sql.deleteCharAt(sql.length() - 2);
        sql.append(");");
        DBConnector.executeSQL(sql.toString());
    }

    private String appendFkConstraints(ForeignKeyInfo fk_info) {
        if (SessionManager.check_fk_connect(fk_info)) {
            return "constraint " + fk_info.getName() + " foreign key(" + fk_info.getFromColumn() + ") references " +
                    fk_info.getToTable() + "(" + fk_info.getToColumn() + "),\n";
        } else {
            System.out.println("error:if type is many_to_one,foreign key can't be created in table " + tableName);
            System.out.println("if type is one_to_many,foreign key will be created then.Don't worry.");
            return "";
        }
    }

    private String appendIndex(IndexInfo index_info) {
        return index_info.getType().getConstraint_type() + " " + index_info.getName() + "(" +
                fields_info.get(index_info.getField_name()).getColumn_name() + "),\n";
    }


    //拼接sql约束
    private String appendConstraints(FieldInfo fieldInfo) {
        String constraint_type = "";
        ConstraintType[] constraints = fieldInfo.getConstraints();
        if (constraints != null) {
            for (ConstraintType type : constraints) {
                constraint_type += type.getConstraint_type() + " ";
            }
        }
        return constraint_type;
    }

    //更新表结构
    public void updateTable(String flag, String[] columns) {
        for (String column : columns) {
            String sql = null;
            String columnType = columns_info.get(column).getColumn_type();
            String type = flag.concat(":");
            switch (type) {
                case CommonValue.ADD_COLUMN: {
                    sql = "alter table " + tableName + " add " + column + " " + columnType + ";";
                }
                break;
                case CommonValue.DELETE_COLUMN: {
                    sql = "alter table " + tableName + " drop column " + column + ";";
                }
                break;
                case CommonValue.ALTER_COLUMN_NAME: {
                    sql = "alter table " + tableName + " modify " + column + " " + columnType + ";";
                }
                break;
                case CommonValue.ALTER_COLUMN_TYPE: {
                    sql = "alter table " + tableName + " alter column " + column + " " + columnType + ";";
                }
                break;
                case CommonValue.ALTER_TABLE_NAME: {
                    String oldTableName = column;
                    sql = "rename table " + oldTableName + " to " + tableName + ";";//here column is the old table name
                }
                break;
                default:{
                    logger.error(CommonValue.ERROR+"Can't find your update type.");
                }
            }
            DBConnector.executeSQL(sql);
            LoggerHelper.sqlOutput(sql, logger);
        }
    }

    //表是否存在
    public boolean ifTableExists() {
        String db_name = DBConnector.getDb_name();
        boolean ans = false;
        try {
            String sql = "select count(*) from information_schema.TABLES where TABLE_SCHEMA=\'" + db_name + "\' and TABLE_NAME=\'" + tableName + "\';";
            LoggerHelper.sqlOutput(sql, logger);
            rs = DBConnector.executeQuery(sql);
            while (rs.next()) {
                ans = rs.getInt(1) != 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ans;
    }
    /*增减列配置请先delete再create*/

    //删除表
    public void delete_table() {
        StringBuilder sql = new StringBuilder("drop table " + tableName + ";");
        DBConnector.executeSQL(sql.toString());
    }

    //DML
    //insert
    public void insertAll(T bean) {
        StringBuffer sql = new StringBuffer("insert into " + tableName + " values(");
        try {
            String id_value = BeanUtils.getProperty(bean, idInfo.getField_name());//先填充主键数据
            sql.append("\'" + id_value + "\',");
            for (String key : fields_info.keySet()) {//填充其它数据
                String values = BeanUtils.getProperty(bean, key);
                sql.append("\'" + values + "\',");
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(");");
            DBConnector.executeSQL(sql.toString());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
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
    public void insert(T bean) {
        try {
            ArrayList<String> toInsertColumns = getInsertPstmt(bean);
            for (int i = 0; i < toInsertColumns.size(); i++) {
                preparedStatement.setObject(i + 1, BeanUtils.getProperty(bean, toInsertColumns.get(i)));
            }
            System.out.println(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /*根据每个bean的ID循环插入,当每个bean插入字段不同时，使用该方法*/
    public void insertListToTableByID(ArrayList<T> beans) {
        for (T bean : beans) {
            insert(bean);
        }
    }

    public void insertListToTable(ArrayList<T> beans) {
        ArrayList<String> toInsertColumns = getInsertPstmt(beans.get(0));
        try {
            for (T bean : beans) {
                for (int i = 0; i < toInsertColumns.size(); i++) {
                    preparedStatement.setObject(i + 1, BeanUtils.getProperty(bean, toInsertColumns.get(i)));
                }
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (SQLException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    private ArrayList<String> getInsertPstmt(Object bean) {
        ArrayList<String> toInsertColumns = null;
        try {
            StringBuffer sql = new StringBuffer("insert into " + tableName);
            StringBuffer insert_columns = new StringBuffer("(");
            StringBuffer insert_values = new StringBuffer(" values(");
            ArrayList<String> column_name = getColumnList();
            ArrayList<String> field_name = getFieldList();
            toInsertColumns = new ArrayList<>();
            String id_value = BeanUtils.getProperty(bean, idInfo.getField_name());//先填充主键数据
            if (id_value != null) {//拼接id字段和数据
                insert_columns.append(column_name.get(0) + ",");
                insert_values.append("?,");
                toInsertColumns.add(field_name.get(0));
            }
            for (int i = 1; i <= fields_info.size(); i++) {
                String value = BeanUtils.getProperty(bean, field_name.get(i));
                if (value != null) {//拼接其余字段
                    insert_columns.append(column_name.get(i) + ",");
                    insert_values.append("?,");
                    toInsertColumns.add(field_name.get(i));
                }
            }
            insert_columns.deleteCharAt(insert_columns.length() - 1).append(")");
            insert_values.deleteCharAt(insert_values.length() - 1).append(")");
            sql.append(insert_columns).append(insert_values);//拼接sql
            preparedStatement = DBConnector.getPreparedStatement(sql.toString());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return toInsertColumns;
    }


    //update
    public void updateAsID(T bean, String condition) {
        try {
            update(idInfo.getColumn_name() + "=" + BeanUtils.getProperty(bean, idInfo.getField_name()) + " and " + condition, bean);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void updateAsID(T bean) {
        try {
            update(idInfo.getColumn_name() + "=" + BeanUtils.getProperty(bean, idInfo.getField_name()), bean);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void update(String condition, T bean) {
        ArrayList<String> toInsertColumn = getUpdatePstmt(condition, bean);
        try {
            for (int i = 0; i < toInsertColumn.size(); i++) {
                preparedStatement.setObject(i + 1, BeanUtils.getProperty(bean, toInsertColumn.get(i)));
            }
            preparedStatement.executeUpdate();
        } catch (SQLException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void updateListToTable(ArrayList<T> beans) {
        StringBuilder condition = new StringBuilder(idInfo.getColumn_name() + "=?");
        ArrayList<String> toInsertColumns = getUpdatePstmt(condition.toString(), beans.get(0));
        toInsertColumns.add(idInfo.getField_name());
        try {
            for (T bean : beans) {
                for (int i = 0; i < toInsertColumns.size(); i++) {
                    preparedStatement.setObject(i + 1, BeanUtils.getProperty(bean, toInsertColumns.get(i)));
                }
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (SQLException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    public void updateListToTableByID(ArrayList<T> beans, ArrayList<HashMap<Integer, String>> conditions) {
        int condition_index = 0;
        String condition = null;
        boolean is_condition_end = false;
        for (int i = 0; i < beans.size(); i++) {
            if ((condition = conditions.get(condition_index).get(i)) != null && is_condition_end == false) {
                updateAsID(beans.get(i), condition);
                if (condition_index < conditions.size() - 1) {
                    condition_index++;
                } else {
                    is_condition_end = true;
                }
            } else {
                updateAsID(beans.get(i));
            }
        }
    }

    public void updateListToTableByID(ArrayList<T> beans) {
        for (T bean : beans) {
            updateAsID(bean);
        }
    }

    private ArrayList<String> getUpdatePstmt(String condition, T bean) {
        StringBuilder sql = new StringBuilder("update " + tableName + " set ");
        ArrayList<String> column_name = getColumnList();
        ArrayList<String> field_name = getFieldList();
        ArrayList<String> toInsertColumns = null;

        try {
            toInsertColumns = new ArrayList<>();
            for (int i = 1; i < column_name.size(); i++) {//主键不更改
                String value = BeanUtils.getProperty(bean, field_name.get(i));
                if (value != null) {
                    sql.append(column_name.get(i) + "=?,");
                    toInsertColumns.add(field_name.get(i));
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" where " + condition);
        preparedStatement = DBConnector.getPreparedStatement(sql.toString());
        return toInsertColumns;
    }


    //select
    /*
     * @para1:columns like:col1,col2...
     * @para2:like "=value" or ">value" or... You can append like "and column>value"
     * */
    public ArrayList<T> select(StringBuilder toSelect, StringBuilder condition, ArrayList<String> paras) {
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");//正则表达式去空格，换行符
        Matcher m = p.matcher(toSelect.toString());
        toSelect=new StringBuilder(m.replaceAll(""));
        StringBuffer sql = new StringBuffer("select " + toSelect.toString() + " from " + tableName);
        if (condition==null) {
            sql.append(";");
        } else {
            sql.append(" where " + condition.toString() + ";");
        }

        preparedStatement = DBConnector.getPreparedStatement(sql.toString());//防止sql注入攻击
        try {
            if (paras!=null&&paras.size()>0) {
                for (int i = 0; i < paras.size(); i++) {
                    preparedStatement.setObject(i + 1, paras.get(i));
                }
            }
            rs = preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        LoggerHelper.sqlOutput(sql.toString(), logger);
        ArrayList<String> list;
        if (toSelect.toString().equals("*")) {
            list = new ArrayList<>(fields_info.keySet());
            list.add(0, idInfo.getColumn_name());
        } else {
            String[] select_columns = m.replaceAll("").split(",");
            list = new ArrayList<>();
            for (String select_column : select_columns) {
                if (select_column.equals(idInfo.getColumn_name())) {
                    list.add(idInfo.getField_name());
                } else {
                    list.add(columns_info.get(select_column).getField_name());
                }
            }
        }
        ArrayList<T> beans = ResultSetToBean(rs, list);
        return beans;
    }

    public ArrayList<T> selectAll() {
        return select(new StringBuilder("*"), null,null);
    }

    public T selectAsID(Object id_value) {
        rs = DBConnector.executeQuery("select * from " + tableName + " where " + idInfo.getColumn_name() + "=" + id_value + ";");
        ArrayList<String> list = getFieldList();
        return ResultSetToBean(rs, list).get(0);
    }

    //get a column string list
    private ArrayList<String> getColumnList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add(idInfo.getColumn_name());
        for (String key : fields_info.keySet()) {
            list.add(fields_info.get(key).getColumn_name());
        }
        return list;
    }

    //get a filed string list
    private ArrayList<String> getFieldList() {
        ArrayList<String> list = new ArrayList<>(fields_info.keySet());
        list.add(0, idInfo.getField_name());
        return list;
    }


    private ArrayList<T> ResultSetToBean(ResultSet rs, ArrayList<String> data_name) {//convert resultset to list
        ArrayList<T> objects = new ArrayList<T>();
        try {
            ArrayList<ArrayList<String>> origin_data = new ArrayList<ArrayList<String>>();
            resultSetMetaData = DBConnector.getRsmd(rs);
            int row_count = 0;

            while (rs.next()) {//遍历resultset
                ArrayList<String> row_data = new ArrayList<String>();
                for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
                    row_data.add(rs.getString(i + 1));
                }
                origin_data.add(row_data);
                row_count++;
            }


            for (int i = 0; i < row_count; i++) {//将数据进行对应的强制类型转换
                T obj = (T) BeanClass.newInstance();
                ArrayList<String> strings = origin_data.get(i);
                for (int j = 0; j < strings.size(); j++) {
                    BeanUtils.setProperty(obj, data_name.get(j), strings.get(j));
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

    public void deleteAsID(T bean) {
        Object id_value = null;
        try {
            id_value = BeanUtils.getProperty(bean, idInfo.getField_name());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        StringBuilder sql = new StringBuilder("delete from " + tableName + " where " + idInfo.getColumn_name() + "=" + id_value + ";");
        DBConnector.executeSQL(sql.toString());
    }

    public void delete(String columns, String condition) {
        StringBuilder sql = new StringBuilder("delete from " + tableName);
        if (columns.equals("*")) {
            sql.append(" where " + condition + ";");
        } else {
            sql.append("(" + columns + "） where " + condition + ";");
        }
        DBConnector.executeSQL(sql.toString());
    }

    public void deleteListAsID(ArrayList<T> beans) {
        for (T bean : beans) {
            deleteAsID(bean);
        }
    }


    //cache
    public Cache<T> startCache(ArrayList<T> data, int mode) {
        Cache<T> cache = new Cache<T>(data, mode, this);
        return cache;
    }

    public Transaction startTransaction() {
        Transaction transaction = new Transaction(Configuration.getConnection(), Connection.TRANSACTION_REPEATABLE_READ);
        return transaction;
    }

    public Transaction startTransaction(int level) {
        Transaction transaction = new Transaction(Configuration.getConnection(), level);
        return transaction;
    }


}
