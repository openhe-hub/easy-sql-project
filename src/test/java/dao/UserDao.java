package dao;

import bean.User;
import org.easysql.annotation.starter.EasySqlDao;
import org.easysql.starter.EasyDao;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-18 19:10
 **/
@EasySqlDao(pojoName = "bean.User")
public class UserDao extends EasyDao<User> {
    public void logAll(){
        DataOutput(handler.selectAll());
    }
}
