package org.easysql.helper;

import org.apache.log4j.Logger;

import java.util.ArrayList;

public class LoggerHelper {
    public static void sqlOutput(String sql, Logger logger){
        String[] lines=sql.split("\n");
        for (String line : lines) {
            logger.info(CommonValue.SQL_OUTPUT+"\t"+line);
        }
    }

    public static void ProcessOutput(String process,Logger logger){
        logger.info(CommonValue.PROCESS+"\t"+process);
    }

    public static <T>  void DataOutput(T data,Logger logger){
        logger.info(CommonValue.DATA_OUTPUT+"\t"+data.toString());
    }

    public static <T>  void DataOutput(ArrayList<T> dataList, Logger logger){
        for (T data : dataList) {
            DataOutput(data,logger);
        }
    }
}
