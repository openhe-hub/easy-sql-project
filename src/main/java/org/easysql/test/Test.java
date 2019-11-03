package org.easysql.test;

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
        studentSession.init();

        SessionHandler<Student> handler=studentSession.getHandler();
        XmlHelper.init_sql_parser("sql",studentSession);
        Student student=new Student(4567,"he",100,1);

        StringBuilder sql=XmlHelper.parseCondition("condition1");
        sql=XmlHelper.fill(student,new String[]{"140"},sql);

        ArrayList<Student> students=handler.select("*",sql);
        for (Student stu : students) {
            System.out.println(stu.toString());
        }

        studentSession.close();

    }
}
