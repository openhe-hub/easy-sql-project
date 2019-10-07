package org.easysql.test;

import org.easysql.bean.Student;
import org.easysql.bean.Teacher;
import org.easysql.helper.Configuration;
import org.easysql.helper.DBConnector;
import org.easysql.session.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;


public class Test {
    public static void main(String[] args) throws SQLException {
        Cache.start_timer();
        Configuration.configure(Test.class);//初始化中央管理器
        Session<Teacher> teacherSession=new Session<>("Teacher");
        Session<Student> studentSession=new Session<>("Student");
        SessionManager.initAll();

        SessionManager.createAll();

        SessionManager.closeAll();
        System.out.println(Cache.calc_time());
    }
}
