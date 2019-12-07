package org.easysql.test;

import org.easysql.bean.Mark;
import org.easysql.bean.Student;
import org.easysql.bean.Teacher;
import org.easysql.helper.Configuration;
import org.easysql.helper.DBConnector;
import org.easysql.helper.XmlHelper;
import org.easysql.info.CRUD_VALUE;
import org.easysql.session.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;


public class Test {
    public static void main(String[] args)  {
        Configuration.configure(Test.class);
        Session<Student> studentSession=new Session<>("Student");
        Session<Teacher> teacherSession=new Session<>("Teacher");
        Session<Mark> markSession=new Session<>("Mark");

        SessionManager.initAll();

        SessionHandler<Student> studentHandler=studentSession.getHandler();

        XmlHelper xml=new XmlHelper();
        xml.init_sql_parser("sql",studentSession,studentHandler);
        ArrayList<Teacher> teachers=xml.parseSelect("test2",null,null);
        for (Teacher teacher : teachers) {
            System.out.println(teacher.toString());
        }
        SessionManager.closeAll();
    }
}
