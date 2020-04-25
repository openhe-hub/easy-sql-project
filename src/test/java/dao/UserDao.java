package dao;

import bean.User;
import org.easysql.annotation.starter.EasySqlDao;
import org.easysql.starter.EasyDao;

import java.util.ArrayList;
import java.util.Random;

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

    public void insertRandomUser(int number) {
        ArrayList<User> users=new ArrayList<>();
        for (int i = 0; i < number; i++) {
            User user= User.builder()
                    .uid(i)
                    .name("h"+i)
                    .password("aaa")
                    .build();
            users.add(user);
        }
        handler.insertListToTable(users);
    }
}
