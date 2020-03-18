package org.easysql.app;

import org.easysql.bean.Student;
import org.easysql.helper.Configuration;
import org.easysql.helper.XmlHelper;
import org.easysql.session.Session;
import org.easysql.session.SessionHandler;
import org.easysql.session.SessionManager;

public class App {
    public static void main(String[] args) {
        Configuration.AutoConfigure(App.class);
        SessionHandler<Student> handler=SessionManager.getHandlerByClass(Student.class);
        Session<Student> studentSession= SessionManager.getSessionByClass(Student.class);
        XmlHelper parser=new XmlHelper();
        parser.initSqlParser("sql",studentSession,handler);
        parser.parseSelect("test2",null,null).forEach(System.out::println);
    }
}
