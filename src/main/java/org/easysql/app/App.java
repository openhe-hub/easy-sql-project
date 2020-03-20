package org.easysql.app;

import org.easysql.bean.Student;
import org.easysql.helper.Configuration;
import org.easysql.session.SessionHandler;
import org.easysql.session.SessionManager;

public class App {
    public static void main(String[] args) {
        Configuration.AutoConfigure(App.class);
        SessionHandler<Student> handler=SessionManager.getHandlerByClass(Student.class);
        handler.selectAll().forEach(System.out::println);
    }
}
