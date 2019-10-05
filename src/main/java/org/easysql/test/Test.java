package org.easysql.test;

import org.easysql.bean.Student;
import org.easysql.helper.Configuration;
import org.easysql.helper.DBConnector;
import org.easysql.session.Cache;
import org.easysql.session.Session;
import org.easysql.session.SessionHandler;
import org.easysql.session.Transaction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Test {
    public static void main(String[] args) throws SQLException {
        Configuration.configure(Test.class);//初始化中央管理器
        Session<Student> session=new Session<>("Student");//新建Student类的session
        session.init();//初始化session

        SessionHandler<Student> handler=session.getHandler();//创建对应的SessionHandler

        Transaction tr=handler.startTransaction();
        tr.start();


        tr.commit();
        session.close();
    }


}
