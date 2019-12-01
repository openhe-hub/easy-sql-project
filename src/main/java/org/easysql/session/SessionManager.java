package org.easysql.session;

import org.easysql.info.ConstraintType;
import org.easysql.info.ForeignKeyInfo;
import org.easysql.info.Join;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class SessionManager {
    private static LinkedHashMap<String,Session> sessions;//table_name to session
    private static LinkedHashMap<String,Session> class_to_sessions;//class_name to session

    static {
        sessions = new LinkedHashMap<>();
        class_to_sessions=new LinkedHashMap<>();
    }

    public static void registerSession(Session session) {
        sessions.put(session.getTable_name(), session);
        class_to_sessions.put(session.getClass_name(),session);
    }

    public static void initAll(){
        for (Map.Entry<String,Session> entry:sessions.entrySet()){
            entry.getValue().init();
        }
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

    public static Session select_session_by_class_name(String class_name){
        return class_to_sessions.get(class_name);
    }

    public static Session select_session_by_table_name(String table_name){
        return sessions.get(table_name);
    }


    public static boolean check_fk_connect(ForeignKeyInfo fk_info) {
        String from_table = fk_info.getFromTable();
        String to_table = fk_info.getToTable();
        String from_column = fk_info.getFromColumn();
        String to_column = fk_info.getToColumn();
        ConstraintType type = fk_info.getType();
        switch (type) {
            case ONE_TO_MANY: {
                return false;
            }
            case MANY_TO_ONE: {
                return if_fk_correct(from_table, to_table, from_column, to_column,ConstraintType.ONE_TO_MANY);
            }
            case ONE_TO_ONE: {
                return if_fk_correct(from_table, to_table, from_column, to_column,ConstraintType.ONE_TO_ONE);
            }
            default:{
                return false;
            }
        }
    }

    public static Join getJoin(String main_class,String join_class){
        Session main_session=class_to_sessions.get(main_class);
        Join join = main_session.getClassInfo().getJoins().get(join_class);
        if(join.getTo_class().equals(join_class)){
           return join;
        }else {
            return null;
        }
    }

    private static boolean if_fk_correct(String from_table, String to_table, String from_column, String to_column,ConstraintType type) {
        Session toSession = sessions.get(to_table);
        SessionHandler sessionHandler = toSession.getHandler();
        if (!sessionHandler.if_table_exists()) {
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
