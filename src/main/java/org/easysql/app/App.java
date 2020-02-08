package org.easysql.app;

import org.easysql.bean.Student;
import org.easysql.bean.Teacher;
import org.easysql.helper.CommonValue;
import org.easysql.helper.Configuration;
import org.easysql.session.Cache;
import org.easysql.session.Session;
import org.easysql.session.SessionHandler;

import java.util.ArrayList;

public class App {
    public static void main(String[] args) {
        Configuration.configure(App.class);
        Session<Student> studentSession=new Session<>(Student.class);
        SessionHandler<Student> studentSessionHandler=studentSession.getHandler();
        Session<Teacher> teacherSession=new Session<>(Teacher.class);
        SessionHandler<Teacher> teacherSessionHandler=teacherSession.getHandler();

        ArrayList<Student> students=studentSessionHandler.selectAll();
        Cache<Student> studentCache=studentSessionHandler.buildCache(students, CommonValue.READ_WRITE_MODE);
        studentCache.logAll();
    }
}
