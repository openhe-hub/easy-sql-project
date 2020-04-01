package org.easysql.app;

import org.easysql.bean.Student;
import org.easysql.helper.CommonValue;
import org.easysql.plugin.pageHelper.CachePageHelper;
import org.easysql.session.Cache;
import org.easysql.session.SessionHandler;
import org.easysql.session.SqlSession;


public class App extends EasySqlApplication {
    public static void main(String[] args) {
        init(App.class);
        SessionHandler<Student> handler=handler(Student.class);
        Cache<Student> cache=handler.buildCache(handler.selectAll(),CommonValue.READ_WRITE_MODE);
        CachePageHelper<Student> pageHelper=new CachePageHelper<>(cache,3);
        pageHelper.logAll();
        cache.delete(d->d.getId()==29);
        pageHelper.reLoad(4);
        pageHelper.logAll();
    }
}
