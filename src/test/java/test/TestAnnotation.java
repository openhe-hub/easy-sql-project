package test;

import bean.User;
import dao.UserDao;
import org.easysql.annotation.starter.EasySqlApplication;
import org.easysql.starter.DaoManager;

import org.easysql.starter.EasyApplication;
import org.junit.jupiter.api.Test;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-25 09:44
 **/
@EasySqlApplication
public class TestAnnotation extends EasyApplication {
    @Test
    public void testAnnotation(){
        init(TestAnnotation.class);
        UserDao dao = (UserDao) dao(User.class);
        dao.logAll();
    }

    @Test
    public void testSmartAnnotation() {
        init(TestAnnotation.class);
        UserDao dao = (UserDao) dao(User.class);
        dao.logAll();
    }
}
