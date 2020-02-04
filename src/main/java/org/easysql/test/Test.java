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
        SessionHandler<Student> studentSessionHandler=studentSession.getHandler();
        ArrayList<Student> students=studentSessionHandler.selectAll();
        for (Student student : students) {
            System.out.println(student.toString());
        }
    }
}
