package org.easysql.utils;

import org.apache.log4j.Logger;
import org.easysql.configuration.Configuration;
import org.easysql.utils.values.CommonValue;

import java.util.ArrayList;

public class LoggerUtils {
    public static void sqlOutput(String sql, Logger logger){
        String[] lines=sql.split("\n");
        for (String line : lines) {
            logger.info(CommonValue.SQL_OUTPUT+"\t"+line);
        }
    }

    public static void sqlOutput(StringBuilder sql, Logger logger){
        sqlOutput(sql.toString(), logger);
    }

    public static void ProcessOutput(String process,Logger logger){
        logger.info(CommonValue.PROCESS+"\t"+process);
    }

    public static <T>  void DataOutput(T data,Logger logger){
        logger.info(CommonValue.DATA_OUTPUT+"\t"+data.toString());
    }

    public static <T>  void DataOutput(ArrayList<T> dataList, Logger logger){
        logger.info(CommonValue.DATA_OUTPUT+"\tdata list output.");
        logger.info(CommonValue.DATA_OUTPUT+"\t"+CommonValue.PRINT_SEPARATOR);
        dataList.forEach(data -> DataOutput(data,logger));
        logger.info(CommonValue.DATA_OUTPUT+"\t"+CommonValue.PRINT_SEPARATOR);
    }

    /**
     *
     * @param clazz proxy logger class
     * @param errorMessage error message to output
     */
    public static void error(Class<?> clazz,String errorMessage){
        Configuration.createLogger(clazz).error(errorMessage);
    }

    /**
     *
     * @param clazz proxy logger class
     * @param infoMessage info message to output
     */
    public static void info(Class<?> clazz,String infoMessage){
        Configuration.createLogger(clazz).error(infoMessage);
    }
}
