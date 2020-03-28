package org.easysql.app;

import org.easysql.bean.Student;
import org.easysql.helper.CommonValue;
import org.easysql.session.Cache;
import org.easysql.session.SessionHandler;
import org.easysql.session.SqlSession;

public class App extends EasySqlApplication {
    public static void main(String[] args) {
        init(App.class);
        SessionHandler<Student> handler=handler(Student.class);
        Cache<Student> studentCache=handler.buildCache(handler.selectAll(), CommonValue.READ_WRITE_MODE);
        studentCache.setFilter(student->student.getName().equals("he"));
        DataOutput(studentCache.select(CommonValue.ONLY_VALUE));
    }
}
