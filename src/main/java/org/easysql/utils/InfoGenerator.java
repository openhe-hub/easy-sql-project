package org.easysql.utils;

import org.easysql.configuration.Configuration;

import org.apache.log4j.Logger;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-25 10:38
 **/
public class InfoGenerator {
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
                LoggerUtils.error(InfoGenerator.class, " This field type isn't supported.");
                LoggerUtils.info(InfoGenerator.class, CommonValue.SUGGESTION + " Please set it in your mapping.xml.");
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
                LoggerUtils.error(InfoGenerator.class, " This field type isn't supported.");
                LoggerUtils.info(InfoGenerator.class, CommonValue.SUGGESTION + " Please set it in your mapping.xml.");
                return null;
            }
        }
    }
}
