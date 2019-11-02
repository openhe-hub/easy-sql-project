package org.easysql.test;

import org.easysql.bean.Student;
import org.easysql.bean.Teacher;
import org.easysql.helper.Configuration;
import org.easysql.helper.DBConnector;
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

        Cache<Student> cache=handler.startCache(handler.selectAll(),CRUD_VALUE.READ_MODE);
        Cache.start_timer();
        LinkedHashMap<Object,ArrayList<Student>> students=cache.group_by("mark");
        System.out.println(Cache.calc_time());
        studentSession.close();


    }
}
