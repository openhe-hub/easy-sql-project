package org.easysql.helper;

import org.apache.log4j.Logger;

public class LoggerHelper {
    public static void sqlOutput(String sql, Logger logger){
        String[] lines=sql.split("\n");
        for (String line : lines) {
            logger.info(CommonValue.SQL_OUTPUT+"\t"+line);
        }
    }
}
