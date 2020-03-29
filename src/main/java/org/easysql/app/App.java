package org.easysql.app;

import org.easysql.bean.Student;
import org.easysql.helper.CommonValue;
import org.easysql.plugin.pageHelper.CachePageHelper;
import org.easysql.session.Cache;
import org.easysql.session.SessionHandler;
import org.easysql.session.SqlSession;

import java.util.Comparator;

public class App extends EasySqlApplication {
    public static void main(String[] args) {
        init(App.class);
        SessionHandler<Student> handler=handler(Student.class);
        CachePageHelper<Student> pageHelper=new CachePageHelper<>(handler.selectAll(),3);
        CachePageHelper<Student> pageResult=new CachePageHelper<>(pageHelper.search(d->d.getId()>=30),2);
        pageResult.logAll();
    }
}
