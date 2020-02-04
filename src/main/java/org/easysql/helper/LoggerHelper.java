package org.easysql.helper;

import org.apache.log4j.Logger;

public class LoggerHelper {
    public static void sqlOutput(String sql, Logger logger){
        logger.info(CommonValue.SQL_OUTPUT+sql);
    }
}
