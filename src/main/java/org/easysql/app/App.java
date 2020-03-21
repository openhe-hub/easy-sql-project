package org.easysql.app;

import org.easysql.bean.Student;
import org.easysql.info.FillData;
import org.easysql.session.SessionHandler;
import org.easysql.session.SqlSession;

public class App extends EasySqlApplication {
    public static void main(String[] args) {
        System.out.println(App.class.getPackage().getName());
        /*init(App.class);
        SqlSession<Student> sql=sql(Student.class);
        SessionHandler<Student> handler=handler(Student.class);
        sql.insert("insert",new FillData("he33"));
        DataOutput(handler.selectAll());*/
    }
}
