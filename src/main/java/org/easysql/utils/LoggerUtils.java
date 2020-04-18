package org.easysql.utils;

import org.apache.log4j.Logger;

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
        logger.info(CommonValue.DATA_OUTPUT+"\t"+CommonValue.PRINT_SEPERATOR);
        dataList.forEach(data -> DataOutput(data,logger));
        logger.info(CommonValue.DATA_OUTPUT+"\t"+CommonValue.PRINT_SEPERATOR);
    }
}
