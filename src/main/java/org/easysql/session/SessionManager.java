package org.easysql.session;

import org.apache.log4j.Logger;
import org.easysql.helper.CommonValue;
import org.easysql.helper.Configuration;
import org.easysql.info.ConstraintType;
import org.easysql.info.ForeignKeyInfo;
import org.easysql.info.Join;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class SessionManager {
    private static LinkedHashMap<String,Session> sessions;//table_name to session
    private static LinkedHashMap<String,Session> classToSessions;//class_name to session
    private static Logger logger;

    static {
        sessions = new LinkedHashMap<>();
        classToSessions =new LinkedHashMap<>();
        logger= Configuration.createLogger(SessionManager.class);
    }

    public static void registerSession(Session session) {
        sessions.put(session.getTableName(), session);
        classToSessions.put(session.getClassName(),session);
        logger.info(CommonValue.PROCESS+"Registering session("+session.getClassName()+") finished.");
    }

    public static void initAll(){
        for (Map.Entry<String,Session> entry:sessions.entrySet()){
            entry.getValue().init();
        }
        logger.info(CommonValue.PROCESS+"All sessions have been successfully initiated.");
    }

    public static void createAll(){
        for (Map.Entry<String,Session> entry:sessions.entrySet()){
            entry.getValue().create();
        }
    }

    public static void closeAll(){
        for (Map.Entry<String,Session> entry:sessions.entrySet()){
            entry.getValue().close();
        }
    }

    public static Session selectSessionByClassName(String class_name){
        return classToSessions.get(class_name);
    }

    public static Session selectSessionByTableName(String table_name){
        return sessions.get(table_name);
    }


    public static boolean checkForeignKeyConnect(ForeignKeyInfo fk_info) {
        String from_table = fk_info.getFromTable();
        String to_table = fk_info.getToTable();
        String from_column = fk_info.getFromColumn();
        String to_column = fk_info.getToColumn();
        ConstraintType type = fk_info.getType();
        switch (type) {
            case ONE_TO_MANY: {
                return true;
            }
            case MANY_TO_ONE: {
                return ifForeignKeyExists(from_table, to_table, from_column, to_column,ConstraintType.ONE_TO_MANY);
            }
            case ONE_TO_ONE: {
                return ifForeignKeyExists(from_table, to_table, from_column, to_column,ConstraintType.ONE_TO_ONE);
            }
            default:{
                return false;
            }
        }
    }

    public static Join getJoin(String main_class,String join_class){
        Session main_session= classToSessions.get(main_class);
        Join join = main_session.getClassInfo().getJoins().get(join_class);
        if(join.getToClass().equals(join_class)){
           return join;
        }else {
            return null;
        }
    }

    private static boolean ifForeignKeyExists(String from_table, String to_table, String from_column, String to_column, ConstraintType type) {
        Session toSession = sessions.get(to_table);
        SessionHandler sessionHandler = toSession.getHandler();
        if (!sessionHandler.ifTableExists(null)) {
            return false;
        } else {
            ArrayList<ForeignKeyInfo> toSearch_fk_infos = toSession.getClassInfo().getForeignKeyInfos();
            for (ForeignKeyInfo toSearch_fk_info : toSearch_fk_infos) {
                if (toSearch_fk_info.equals(new ForeignKeyInfo(to_table, from_table, to_column, from_column,
                        type, toSearch_fk_info.getName()))) {
                    return true;
                }
            }
            return false;
        }
    }


}
