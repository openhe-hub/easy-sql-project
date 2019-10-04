package org.easysql.test;

import org.easysql.bean.Student;
import org.easysql.helper.Configuration;
import org.easysql.session.Cache;
import org.easysql.session.Filter;
import org.easysql.session.Session;
import org.easysql.session.SessionHandler;

import java.util.ArrayList;

public class Test {
    public static void main(String[] args) {
        Configuration.configure(Test.class);//初始化中央管理器
        Session session=new Session("Student");//新建Student类的session
        session.init();//初始化session

        SessionHandler handler=session.getHandler();//创建对应的SessionHandler
        ArrayList<Student> students= (ArrayList<Student>) handler.selectAll();
        for (Student student : students) {
            student.setMark(100);
        }
        handler.updateListToTableAsID(students);
    }
}
