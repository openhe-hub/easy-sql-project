package org.easysql.utils.generator;

import com.alibaba.druid.support.spring.stat.annotation.Stat;
import org.easysql.utils.values.CommonValue;
import org.easysql.utils.LoggerUtils;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-25 10:38
 **/
public class InternalInfoGenerator {
    /**
     * example:com.company.entity.User->user
     * @param pojoClassName pojo class name
     * @return table name
     */
    public static String generateTableName(String pojoClassName){
        String[] packages= pojoClassName.split("\\.");
        return packages[packages.length-1].toLowerCase();
    }

    public static boolean isIdDetected(String fieldName){
        return fieldName.toLowerCase().contains("id");
    }

    public static String generateColumnName(String fieldName) {
        return fieldName;
    }

    public static String generateColumnType(String fieldType) {
        switch (fieldType) {
            case "int": {
                return "integer";
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
            case "java.util.Date": {
                //default date type in EasySql is datetime.
                return "datetime";
            }
            default: {
                LoggerUtils.error(InternalInfoGenerator.class, " This field type isn't supported.");
                LoggerUtils.info(InternalInfoGenerator.class, CommonValue.SUGGESTION + " Please set it in your mapping.xml.");
                return null;
            }
        }
    }

    public static String generateIdType(String fieldType) {
        switch (fieldType) {
            case "int": {
                return "integer";
            }
            case "long": {
                return "bigint";
            }
            case "java.math.BigDecimal": {
                return "numeric";
            }
            default: {
                LoggerUtils.error(InternalInfoGenerator.class, " This field type isn't supported.");
                LoggerUtils.info(InternalInfoGenerator.class, CommonValue.SUGGESTION + " Please set it in your mapping.xml.");
                return null;
            }
        }
    }
}
