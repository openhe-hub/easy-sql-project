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
public class UserDao extends EasyDao<User> {}
