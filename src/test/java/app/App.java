package app;

import bean.Student;
import org.easysql.starter.EasySqlApplication;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-18 10:56
 **/
public class App extends EasySqlApplication {
    public static void main(String[] args) {
        init(App.class);
        DataOutput(handler(Student.class).selectAll());
    }
}
