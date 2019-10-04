package org.easysql.test;

import org.easysql.bean.Student;
import org.easysql.helper.Configuration;
import org.easysql.session.Cache;
import org.easysql.session.Filter;
import org.easysql.session.Session;
import org.easysql.session.SessionHandler;

import java.util.ArrayList;
import java.util.Random;

public class Test {
    public static void main(String[] args) {
        Configuration.configure(Test.class);//初始化中央管理器
        Session session=new Session("Student");//新建Student类的session
        session.init();//初始化session

        SessionHandler handler=session.getHandler();//创建对应的SessionHandler


        Cache<Student> cache=handler.startCache(handler.selectAll(),Cache.READ_WRITE_MODE);

        cache.setFilter((data -> {return data.getId()==29444;}));
        cache.delete();
        cache.close();
        session.close();



    }
}
