package org.easysql.app;

import org.easysql.bean.Student;
import org.easysql.session.SessionHandler;
import org.easysql.session.SqlSession;

public class App extends EasySqlApplication {
    public static void main(String[] args) {
        init(App.class);
        SqlSession<Student> sql=sql(Student.class);
        sql.insert("insert", Student.builder().student_id(2000).name("he").build());
        SessionHandler<Student> handler=handler(Student.class);
        DataOutput(handler.selectAll());
    }
}
