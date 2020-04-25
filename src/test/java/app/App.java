package app;

import bean.Student;
import bean.User;
import dao.UserDao;
import org.easysql.annotation.starter.EasySqlApplication;
import org.easysql.starter.EasyApplication;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-18 10:56
 **/
public class App extends EasyApplication {
    public static void main(String[] args) {
        new App().run();
    }

    public void run(){
        init(App.class);
        UserDao dao = (UserDao) dao(User.class);
        dao.logAll();
    }
}
