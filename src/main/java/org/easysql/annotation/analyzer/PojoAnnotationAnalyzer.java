package org.easysql.annotation.analyzer;

import lombok.Data;
import org.apache.log4j.Logger;
import org.easysql.annotation.pojo.*;
import org.easysql.configuration.Configuration;
import org.easysql.info.constraint.ConstraintType;
import org.easysql.info.orm.*;
import org.easysql.utils.generator.InternalInfoGenerator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Analyze pojo class annotation
 * @program: EasySqlProject
 * @author: he
 * @create: 2020-04-18 13:07
 **/
@Data
public class PojoAnnotationAnalyzer {
    private Class<?> pojoClass;
    private ClassInfo pojoInfo;
    private LinkedHashMap<String, String[]> classInfo;
    private String tableName;
    private IdInfo idInfo;
    private LinkedHashMap<String, FieldInfo> fieldInfo;
    private LinkedHashMap<String, FieldInfo> columnInfo;
    private ArrayList<IndexInfo> indexInfo;
    private ArrayList<ForeignKeyInfo> foreignKeyInfo;
    private LinkedHashMap<String, JoinInfo> joinInfo;
    private final Logger logger;

    public PojoAnnotationAnalyzer(Class<?> pojoClass) {
        this.pojoClass = pojoClass;
        logger = Configuration.createLogger(PojoAnnotationAnalyzer.class);
        classInfo = new LinkedHashMap<>();
        fieldInfo = new LinkedHashMap<>();
        columnInfo = new LinkedHashMap<>();
        indexInfo = new ArrayList<>();
        foreignKeyInfo = new ArrayList<>();
        joinInfo = new LinkedHashMap<>();
    }

    public ClassInfo analyzePojo() {
        if (pojoClass.isAnnotationPresent(EasySqlPojo.class)) {
            handlePojo(pojoClass.getAnnotation(EasySqlPojo.class), pojoClass.getName());
            Arrays.stream(pojoClass.getDeclaredFields()).forEach(field -> {
                if (field.isAnnotationPresent(Id.class)) {
                    handleId(field.getAnnotation(Id.class), field);
                } else if (field.isAnnotationPresent(Column.class)) {
                    handleField(field.getAnnotation(Column.class), field);
                }
            });
            buildPojoInfo();
            return pojoInfo;
        } else if (pojoClass.isAnnotationPresent(EasySqlSmartPojo.class)) {
            handleSmartPojo(pojoClass.getAnnotation(EasySqlSmartPojo.class), pojoClass.getName());
            Arrays.stream(pojoClass.getDeclaredFields()).forEach(this::handleSmartColumn);
            buildPojoInfo();
            return pojoInfo;
        } else {
            return null;
        }
    }

    private void buildPojoInfo() {
        pojoInfo = ClassInfo.builder()
                .classInfo(classInfo)
                .idInfo(idInfo)
                .fieldInfo(fieldInfo)
                .columnInfo(columnInfo)
                .foreignKeyInfo(foreignKeyInfo)
                .indexInfos(indexInfo)
                .joinInfo(joinInfo)
                .build();
    }

    private void handlePojo(EasySqlPojo pojo, String className) {
        tableName = pojo.tableName();
        if (tableName.equals("")) {
            logger.error("Bean " + className + " mapping table not found.");
        }
        String sqlFileName = pojo.sqlFile();
        classInfo.put(className, new String[]{className, tableName, sqlFileName});
    }

    private void handleField(Column column, Field field) {
        String fieldName = field.getName();
        String fieldType = field.getType().getName();
        String columnName = column.columnName().equals("") ? InternalInfoGenerator.generateColumnName(fieldName) : column.columnName();
        String columnType = column.columnType().equals("") ? InternalInfoGenerator.generateColumnType(fieldType) : column.columnName();
        ConstraintType[] constraintTypes = column.constraintTypes();
        if (field.isAnnotationPresent(ForeignKey.class)) {
            ForeignKey[] foreignKeys = column.foreignKey();
            handleForeignInfos(foreignKeys, columnName);
        }
        if (field.isAnnotationPresent(Index.class)) {
            Index[] indices = column.index();
            handleIndexInfos(indices, columnName);
        }
        if (field.isAnnotationPresent(Join.class)) {
            Join[] joinInfos = column.join();
            handleJoinInfos(joinInfos, columnName);
        }
        FieldInfo info = FieldInfo.builder()
                .fieldName(fieldType)
                .columnName(columnName)
                .fieldType(fieldType)
                .columnType(columnType)
                .constraints(constraintTypes)
                .build();
        fieldInfo.put(fieldName, info);
        columnInfo.put(columnName, info);
    }

    private void handleId(Id id, Field field) {
        String fieldName = field.getName();
        String fieldType = field.getType().getName();
        String columnName = id.columnName().equals("") ? InternalInfoGenerator.generateColumnName(fieldName) : id.columnName();
        String columnType = id.columnType().equals("") ? InternalInfoGenerator.generateIdType(fieldType) : id.columnName();
        ConstraintType generatePolicy = id.generatePolicy();
        ConstraintType[] constraintTypes = id.constraintTypes();
        idInfo = new IdInfo(new String[]{fieldName, fieldType, columnName, columnType}, constraintTypes, generatePolicy.getConstraintType());
    }

    private void handleForeignInfos(ForeignKey[] foreignKeys, String columnName) {
        for (ForeignKey foreignKey : foreignKeys) {
            String fromTable = tableName;
            String toTable = foreignKey.point().split("\\.")[0];
            String toColumn = foreignKey.point().split("\\.")[1];
            foreignKeyInfo.add(new ForeignKeyInfo(fromTable, toTable, columnName, toColumn, foreignKey.type(), foreignKey.name()));
        }

    }

    private void handleIndexInfos(Index[] indices, String columnName) {
        for (Index index : indices) {
            indexInfo.add(new IndexInfo(columnName, index.name(), index.type()));
        }
    }

    //FIXME:JOIN CLASS ANNOTATION PROCESS
    private void handleJoinInfos(Join[] joins, String columnName) {
        for (Join join : joins) {
            String toTable = join.point().split("\\.")[0];
            String toColumn = join.point().split("\\.")[1];
        }
    }

    public void handleSmartPojo(EasySqlSmartPojo smartPojo, String className) {
        tableName = smartPojo.tableName();
        if (tableName.equals("")) {
            tableName = InternalInfoGenerator.generateTableName(className);
        }
        String sqlFileName = smartPojo.sqlFile();
        classInfo.put(className, new String[]{className, tableName, sqlFileName});
    }

    public void handleSmartColumn(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            handleField(field.getAnnotation(Column.class), field);
        } else if (field.isAnnotationPresent(Id.class)) {
            handleId(field.getAnnotation(Id.class), field);
        } else {
            String fieldName = field.getName();
            String fieldType = field.getType().getName();
            String columnName = InternalInfoGenerator.generateColumnName(fieldName);
            if (InternalInfoGenerator.isIdDetected(field.getName())) {
                String columnType =InternalInfoGenerator.generateIdType(fieldType);
                idInfo = new IdInfo(new String[]{fieldName, fieldType, columnName, columnType}, null,
                        ConstraintType.AUTO_INCREMENT.getConstraintType());
            } else {
                String columnType=InternalInfoGenerator.generateColumnType(fieldType);
                FieldInfo info=new FieldInfo(fieldType, fieldType, columnName, columnType, null);
                fieldInfo.put(fieldName,info);
                columnInfo.put(columnName,info);
            }
        }
    }
}
