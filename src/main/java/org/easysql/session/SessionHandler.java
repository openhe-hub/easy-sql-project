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
    private LinkedHashMap<String, String[]> classInfo;
    @Getter
    private LinkedHashMap<String, FieldInfo> fieldsInfo;
    @Getter
    private LinkedHashMap<String, FieldInfo> columnsInfo;
    @Getter
    private ArrayList<ForeignKeyInfo> foreignKeyInfos;
    @Getter
    private ArrayList<IndexInfo> indexInfos;
    @Getter
    private IdInfo idInfo;
    @Getter
    private String tableName;
    @Getter
    private String className;
    private Class BeanClass;
    private ResultSet rs;
    private ResultSetMetaData resultSetMetaData;
    private PreparedStatement preparedStatement;
    private Logger logger=Logger.getLogger(SessionHandler.class);

    public SessionHandler(Session<T> session) {//将session里的数据解包
        ClassInfo classInfos = session.getClassInfo();
        classInfo = classInfos.getClassInfo();
        fieldsInfo = classInfos.getFieldInfos();
        columnsInfo = classInfos.getColumnInfos();
        foreignKeyInfos = classInfos.getForeignKeyInfos();
        idInfo = classInfos.getIdInfo();
        indexInfos = classInfos.getIndexInfos();
        tableName = session.getTableName();
        className=session.getClassName();
        BeanClass = session.getBeanClass();
    }

    //DDL
    public void createTable() {
        this.createTable(this.tableName);
    }

    public void createTable(String table_name) {
        StringBuilder sql = new StringBuilder("create table ");
        //拼接表名
        sql.append(table_name).append("(\n");
        //拼接主键信息
        sql.append(idInfo.getColumnName()).append(" ")
                .append(idInfo.getColumnType()).append(" primary key ")
                .append(idInfo.getPrimaryKeyType()).append(" ")
                .append(appendConstraints(idInfo))
                .append(",\n");
        //拼接字段信息
        for (String key : fieldsInfo.keySet()) {
            FieldInfo fieldInfo = fieldsInfo.get(key);
            String column_name = fieldInfo.getColumnName();
            String column_type = fieldInfo.getColumnType();
            String constraint_type = appendConstraints(fieldInfo);
            sql.append(column_name).append(" ").append(column_type).append(" ").append(constraint_type).append(",\n");
        }

        for (ForeignKeyInfo foreignKeyInfo : foreignKeyInfos) {
            if (!foreignKeyInfo.getType().equals(ConstraintType.ONE_TO_MANY)) {
                sql.append(appendForeignKeyConstraints(foreignKeyInfo));
            }
        }

        if (indexInfos != null) {
            for (IndexInfo indexInfo : indexInfos) {
                sql.append(appendIndex(indexInfo));
            }
        }
        sql.deleteCharAt(sql.length() - 2);
        sql.append(");");
        LoggerHelper.sqlOutput(sql.toString(),logger);
        DBConnector.executeSQL(sql.toString());
    }

    private String appendForeignKeyConstraints(ForeignKeyInfo foreignKeyInfo) {
        String fromColumn = foreignKeyInfo.getFromColumn();
        String toTable = foreignKeyInfo.getToTable();
        String toColumn = foreignKeyInfo.getToColumn();
        String sql="constraint " + foreignKeyInfo.getName() + " foreign key(" + fromColumn + ") references " +
                toTable + "(" + toColumn + "),\n";
        if (!SessionManager.checkForeignKeyConnect(foreignKeyInfo)) {
            logger.warn("your foreign key("+tableName+"."+ fromColumn+"->"+toTable+"."+toColumn+") maybe invalid.");
        }
        return sql;
    }

    private String appendIndex(IndexInfo index_info) {
        return index_info.getType().getConstraint_type() + " " + index_info.getName() + "(" +
                fieldsInfo.get(index_info.getFieldName()).getColumnName() + "),\n";
    }

    //拼接sql约束
    private String appendConstraints(FieldInfo fieldInfo) {
        StringBuilder constraintType = new StringBuilder();
        ConstraintType[] constraints = fieldInfo.getConstraints();
        if (constraints != null) {
            for (ConstraintType type : constraints) {
                constraintType.append(type.getConstraint_type()).append(" ");
            }
        }
        return constraintType.toString();
    }

    //更新表结构
    private void updateTable(String flag, String[] columns) {
        if (ifTableExists(null)||flag.equals(CommonValue.ALTER_TABLE_NAME)) {
            for (String str : columns) {
                String sql = null;
                String columnType = null;
                switch (flag) {
                    case CommonValue.ADD_COLUMN: {
                        columnType= columnsInfo.get(str).getColumnType();
                        sql = "alter table " + tableName + " add " + str + " " + columnType + ";";
                    }
                    break;
                    case CommonValue.DELETE_COLUMN: {
                        sql = "alter table " + tableName + " drop column " + str + ";";
                    }
                    break;
                    case CommonValue.ALTER_COLUMN_NAME: {
                        String[] changes=str.split("->");
                        String oldColumnName=changes[0];
                        String newColumnName=changes[1];
                        sql = "alter table " + tableName + " rename column " + oldColumnName + " to " + newColumnName + ";";
                    }
                    break;
                    case CommonValue.ALTER_COLUMN_TYPE: {
                        columnType= columnsInfo.get(str).getColumnType();
                        sql = "alter table " + tableName + " modify " + str + " " + columnType + ";";
                    }
                    break;
                    case CommonValue.ALTER_TABLE_NAME: {
                        sql = "rename table " + str + " to " + tableName + ";";//here column is the old table name
                    }
                    break;
                    default: {
                        logger.error(CommonValue.ERROR + "Can't find your update type.");
                    }
                    break;
                }
                if (sql!=null) {
                    LoggerHelper.sqlOutput(sql, logger);
                    DBConnector.executeSQL(sql);
                }
            }
            logger.info(CommonValue.PROCESS + "Table(" + tableName + ") updated successfully.");
        } else {
            logger.error(CommonValue.ERROR + "Table(" + tableName + ") not exists.Update failed.");
        }
    }

    public void addColumn(String updateData) {
        updateTable(CommonValue.ADD_COLUMN, updateData.split(","));
    }

    public void deleteColumn(String updateData) {
        updateTable(CommonValue.DELETE_COLUMN, updateData.split(","));
    }

    public void alterColumnName(String updateData) {
        updateTable(CommonValue.ALTER_COLUMN_NAME, updateData.split(","));
    }

    public void alterColumnType(String updateData) {
        updateTable(CommonValue.ALTER_COLUMN_TYPE, updateData.split(","));
    }

    public void alterTableName(String updateData) {
        updateTable(CommonValue.ALTER_TABLE_NAME, updateData.split(","));
    }

    //表是否存在
    public boolean ifTableExists(String tableName) {
        if (tableName==null){
            tableName=this.tableName;
        }
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
    public void deleteTable() {
        String sql = "drop table " + tableName + ";";
        LoggerHelper.sqlOutput(sql,logger);
        DBConnector.executeSQL(sql);
    }

    //DML
    //insert
    public void insertAll(T bean) {
        StringBuilder sql = new StringBuilder("insert into " + tableName + " values(");
        try {
            String id_value = BeanUtils.getProperty(bean, idInfo.getFieldName());//先填充主键数据
            sql.append("'").append(id_value).append("',");
            for (String key : fieldsInfo.keySet()) {//填充其它数据
                String values = BeanUtils.getProperty(bean, key);
                sql.append("'").append(values).append("',");
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(");");
            LoggerHelper.sqlOutput(sql.toString(),logger);
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
            ArrayList<String> toInsertColumns = getInsertPreparedStatement(bean);
            for (int i = 0; i < toInsertColumns.size(); i++) {
                preparedStatement.setObject(i + 1, BeanUtils.getProperty(bean, toInsertColumns.get(i)));
            }
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
        ArrayList<String> toInsertColumns = getInsertPreparedStatement(beans.get(0));
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


    private ArrayList<String> getInsertPreparedStatement(Object bean) {
        ArrayList<String> toInsertColumns = null;
        try {
            StringBuilder sql = new StringBuilder("insert into " + tableName);
            StringBuilder insert_columns = new StringBuilder("(");
            StringBuilder insert_values = new StringBuilder(" values(");
            ArrayList<String> column_name = getColumnList();
            ArrayList<String> field_name = getFieldList();
            toInsertColumns = new ArrayList<>();
            String id_value = BeanUtils.getProperty(bean, idInfo.getFieldName());//先填充主键数据
            if (id_value != null) {//拼接id字段和数据
                insert_columns.append(column_name.get(0)).append(",");
                insert_values.append("?,");
                toInsertColumns.add(field_name.get(0));
            }
            for (int i = 1; i <= fieldsInfo.size(); i++) {
                String value = BeanUtils.getProperty(bean, field_name.get(i));
                if (value != null) {//拼接其余字段
                    insert_columns.append(column_name.get(i)).append(",");
                    insert_values.append("?,");
                    toInsertColumns.add(field_name.get(i));
                }
            }
            insert_columns.deleteCharAt(insert_columns.length() - 1).append(")");
            insert_values.deleteCharAt(insert_values.length() - 1).append(")");
            sql.append(insert_columns).append(insert_values);//拼接sql
            LoggerHelper.sqlOutput(sql.toString(),logger);
            preparedStatement = DBConnector.getPreparedStatement(sql.toString());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return toInsertColumns;
    }


    //update
    public void updateAsID(T bean, String condition) {
        try {
            update(idInfo.getColumnName() + "=" + BeanUtils.getProperty(bean, idInfo.getFieldName()) + " and " + condition, bean);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void updateAsID(T bean) {
        try {
            update(idInfo.getColumnName() + "=" + BeanUtils.getProperty(bean, idInfo.getFieldName()), bean);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void update(String condition, T bean) {
        ArrayList<String> toInsertColumn = getUpdatePreparedStatement(condition, bean);
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
        ArrayList<String> toInsertColumns = getUpdatePreparedStatement(idInfo.getColumnName() + "=?", beans.get(0));
        toInsertColumns.add(idInfo.getFieldName());
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

    private ArrayList<String> getUpdatePreparedStatement(String condition, T bean) {
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
        sql.append(" where ").append(condition);
        LoggerHelper.sqlOutput(sql.toString(),logger);
        preparedStatement = DBConnector.getPreparedStatement(sql.toString());
        return toInsertColumns;
    }


    /*
     * @param1:columns to be selected (* is allowed)
     * @param2:selection condition
     * @param3:data to be filled in Sql
     * @return:selected data(in a list of Bean object)
     * */
    public ArrayList<T> select(StringBuilder toSelect, StringBuilder condition, ArrayList<Object> params) {
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");//正则表达式去空格，换行符
        Matcher m = p.matcher(toSelect.toString());
        toSelect=new StringBuilder(m.replaceAll(""));
        StringBuilder sql = new StringBuilder("select " + toSelect.toString() + " from " + tableName);
        if (condition==null) {
            sql.append(";");
        } else {
            sql.append(" where ").append(condition.toString()).append(";");
        }

        preparedStatement = DBConnector.getPreparedStatement(sql.toString());//防止sql注入攻击
        try {
            if (params!=null&&params.size()>0) {
                for (int i = 0; i < params.size(); i++) {
                    preparedStatement.setObject(i + 1, params.get(i));
                }
            }
            rs = preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        LoggerHelper.sqlOutput(sql.toString(), logger);
        ArrayList<String> list;
        if (toSelect.toString().equals("*")) {
            list = new ArrayList<>(fieldsInfo.keySet());
            list.add(0, idInfo.getColumnName());
        } else {
            String[] selectColumns = m.replaceAll("").split(",");
            list = new ArrayList<>();
            for (String selectColumn : selectColumns) {
                if (selectColumn.equals(idInfo.getColumnName())) {
                    list.add(idInfo.getFieldName());
                } else {
                    list.add(columnsInfo.get(selectColumn).getFieldName());
                }
            }
        }
        return ResultSetToBean(rs, list);
    }

    public ArrayList<T> selectAll() {
        return select(new StringBuilder("*"), null,null);
    }

    public T selectAsID(Object id_value) {
        rs = DBConnector.executeQuery("select * from " + tableName + " where " + idInfo.getColumnName() + "=" + id_value + ";");
        ArrayList<String> list = getFieldList();
        return ResultSetToBean(rs, list).get(0);
    }

    //get a column string list
    private ArrayList<String> getColumnList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add(idInfo.getColumnName());
        for (String key : fieldsInfo.keySet()) {
            list.add(fieldsInfo.get(key).getColumnName());
        }
        return list;
    }

    //get a filed string list
    private ArrayList<String> getFieldList() {
        ArrayList<String> list = new ArrayList<>(fieldsInfo.keySet());
        list.add(0, idInfo.getFieldName());
        return list;
    }


    private ArrayList<T> ResultSetToBean(ResultSet rs, ArrayList<String> data_name) {//convert resultSet to list
        ArrayList<T> objects = new ArrayList<>();
        try {
            ArrayList<ArrayList<String>> origin_data = new ArrayList<ArrayList<String>>();
            resultSetMetaData = DBConnector.getRsmd(rs);
            int row_count = 0;

            //遍历resultSet
            while (rs.next()) {
                ArrayList<String> row_data = new ArrayList<String>();
                for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
                    row_data.add(rs.getString(i + 1));
                }
                origin_data.add(row_data);
                row_count++;
            }

            //将数据进行对应的强制类型转换
            for (int i = 0; i < row_count; i++) {
                T obj = (T) BeanClass.newInstance();
                ArrayList<String> strings = origin_data.get(i);
                for (int j = 0; j < strings.size(); j++) {
                    BeanUtils.setProperty(obj, data_name.get(j), strings.get(j));
                }
                objects.add(obj);
            }
            return objects;
        } catch (InstantiationException | InvocationTargetException | SQLException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    //delete

    public void deleteAsID(T bean) {
        Object id_value = null;
        try {
            id_value = BeanUtils.getProperty(bean, idInfo.getFieldName());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        StringBuilder sql = new StringBuilder("delete from " + tableName + " where " + idInfo.getColumnName() + "=" + id_value + ";");
        LoggerHelper.sqlOutput(sql.toString(),logger);
        DBConnector.executeSQL(sql.toString());
    }

    public void delete(String columns, String condition) {
        StringBuilder sql = new StringBuilder("delete from " + tableName);
        if (columns.equals("*")) {
            sql.append(" where ").append(condition).append(";");
        } else {
            sql.append("(").append(columns).append("） where ").append(condition).append(";");
        }
        LoggerHelper.sqlOutput(sql.toString(),logger);
        DBConnector.executeSQL(sql.toString());
    }

    public void deleteListAsID(ArrayList<T> beans) {
        for (T bean : beans) {
            deleteAsID(bean);
        }
    }


    //cache
    public Cache<T> buildCache(ArrayList<T> data, int mode) {
        return new Cache<>(data, mode, this);
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
