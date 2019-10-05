package org.easysql.test;

import org.easysql.bean.Student;
import org.easysql.helper.Configuration;
import org.easysql.session.Cache;
import org.easysql.session.Session;
import org.easysql.session.SessionHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class Test {
    public static void main(String[] args) {
        Configuration.configure(Test.class);//初始化中央管理器
        Session<Student> session=new Session("Student");//新建Student类的session
        session.init();//初始化session

        SessionHandler<Student> handler=session.getHandler();//创建对应的SessionHandler

        ArrayList<Student> students=handler.select("*","id<41000");
        ArrayList<HashMap<Integer,String>> conditions=new ArrayList<>();
        HashMap<Integer,String> condition1=new HashMap<>();
        condition1.put(0,"student_mark!=100.0");
        conditions.add(condition1);
        for (Student student : students) {
            student.setName("rooth");
        }
        Cache.start_timer();
        handler.updateListToTableByID(students,conditions);
        System.out.println(Cache.calc_time());
        /*Cache<Student> cache=handler.startCache(handler.selectAll(),Cache.READ_WRITE_MODE);

        cache.setFilter((data -> {return data.getId()==29444;}));
        cache.delete();
        cache.close();*/
        session.close();



    }
}
