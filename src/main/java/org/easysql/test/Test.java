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
        XmlHelper parser=new XmlHelper();
        parser.init_sql_parser("sql",studentSession,handler);

        ArrayList<Student> students=parser.parseSelect("sel_mark_rank10",new Student("he",144,1),new String[]{"140","1000","1100"});
        for (Student student : students) {
            System.out.println(student);
        }
        studentSession.close();

    }
}
