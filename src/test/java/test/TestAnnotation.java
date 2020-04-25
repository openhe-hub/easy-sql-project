package test;

import bean.User;
import dao.UserDao;
import org.easysql.annotation.starter.EasySqlApplication;
import org.easysql.starter.DaoManager;

import org.junit.jupiter.api.Test;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-25 09:44
 **/
@EasySqlApplication
public class TestAnnotation {
    @Test
    public void testAnnotation(){
        DaoManager.init(TestAnnotation.class);
        UserDao dao = (UserDao) DaoManager.dao(User.class);
        dao.logAll();
    }
}
