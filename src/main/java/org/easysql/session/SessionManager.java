package org.easysql.session;

import org.apache.log4j.Logger;
import org.easysql.helper.CommonValue;
import org.easysql.helper.Configuration;
import org.easysql.info.ConstraintType;
import org.easysql.info.ForeignKeyInfo;
import org.easysql.info.JoinInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class SessionManager {
    private static LinkedHashMap<String,Session<?>> sessions;//table_name to session
    private static LinkedHashMap<String,Session<?>> classNameToSession;//class_name to session
    private static LinkedHashMap<Class<?>,Session<?>> clazzToSession;
    private static LinkedHashMap<Class<?>,SessionHandler<?>> clazzToSessionHandler;
    private static LinkedHashMap<Class<?>,SqlSession<?>>  clazzToSqlSession;
    private static Logger logger;

    static {
        sessions = new LinkedHashMap<>();
        classNameToSession =new LinkedHashMap<>();
        clazzToSession=new LinkedHashMap<>();
        clazzToSessionHandler=new LinkedHashMap<>();
        clazzToSqlSession=new LinkedHashMap<> ();
        logger= Configuration.createLogger(SessionManager.class);
    }

    public static void registerSession(Session<?> session) {
        sessions.put(session.getTableName(), session);
        classNameToSession.put(session.getClassName(),session);
        clazzToSession.put(session.getBeanClass(),session);
        clazzToSessionHandler.put(session.getBeanClass(),session.getHandler());
        clazzToSqlSession.put(session.getBeanClass(),session.getSqlSession());
        logger.info(CommonValue.PROCESS+"Registering session("+session.getClassName()+") finished.");
    }

    public static void autoScanBeans(){
        ArrayList<String> classList=Configuration.scanAllClass();
        try {
            for (String className : classList) {
               Session<?> session=new Session<>(Class.forName(className));
               session.init();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void initAll(){
        for (Map.Entry<String,Session<?>> entry:sessions.entrySet()){
            entry.getValue().init();
        }
        logger.info(CommonValue.PROCESS+"All sessions have been successfully initiated.");
    }

    public static void createAll(){
        for (Map.Entry<String,Session<?>> entry:sessions.entrySet()){
            entry.getValue().create();
        }
    }

    public static void closeAll(){
        for (Map.Entry<String,Session<?>> entry:sessions.entrySet()){
            entry.getValue().close();
        }
    }

    public static Session<?> selectSessionByClassName(String className){
        return classNameToSession.get(className);
    }

    public static Session<?> selectSessionByTableName(String tableName){
        return sessions.get(tableName);
    }

    public static <T> Session<T> getSessionByClass(Class<T> clazz){
        return (Session<T>) clazzToSession.get(clazz);
    }

    public static <T> SessionHandler<T> getHandlerByClass(Class<T> clazz){
        return (SessionHandler<T>) clazzToSessionHandler.get(clazz);
    }

    public static <T> SqlSession<T> getSqlSessionByClass(Class<T> clazz){
        return (SqlSession<T>) clazzToSqlSession.get(clazz);
    }

    public static boolean checkForeignKeyConnect(ForeignKeyInfo foreignKeyInfo) {
        String from_table = foreignKeyInfo.getFromTable();
        String to_table = foreignKeyInfo.getToTable();
        String from_column = foreignKeyInfo.getFromColumn();
        String to_column = foreignKeyInfo.getToColumn();
        ConstraintType type = foreignKeyInfo.getType();
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

    public static JoinInfo getJoin(String main_class, String join_class){
        Session<?> main_session= classNameToSession.get(main_class);
        JoinInfo joinInfo = main_session.getClassInfo().getJoins().get(join_class);
        if(joinInfo.getToClass().equals(join_class)){
           return joinInfo;
        }else {
            return null;
        }
    }

    private static boolean ifForeignKeyExists(String from_table, String to_table, String from_column, String to_column, ConstraintType type) {
        Session<?> toSession = sessions.get(to_table);
        SessionHandler<?> sessionHandler = toSession.getHandler();
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
