package org.easysql.app;

import org.easysql.bean.Student;
import org.easysql.helper.CommonValue;
import org.easysql.plugin.pageHelper.CachePageHelper;
import org.easysql.session.Cache;
import org.easysql.session.SessionHandler;
import org.easysql.session.SqlSession;

import java.util.ArrayList;


public class App extends EasySqlApplication {
    public static void main(String[] args) {
        init(App.class);
        SessionHandler<Student> handler=handler(Student.class);
        Cache<Student> cache=handler.buildCache(new ArrayList<>(),CommonValue.READ_WRITE_MODE);
        cache.insert(new Student("haha",1,314));
        cache.close();
    }
}
