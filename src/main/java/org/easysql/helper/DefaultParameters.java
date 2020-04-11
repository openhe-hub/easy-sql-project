package org.easysql.helper;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-10 20:01
 **/
public interface DefaultParameters {
    String DRIVER_CLASS ="com.mysql.cj.jdbc.Driver";
    String URL ="jdbc:mysql://localhost:3306/";
    String URL_PARAMETERS ="characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true";
    int MAX_ACTIVE_CONNECTION=10;
    int INITIAL_CONNECTION = 10;
    long MAX_WAIT_TIME=3000;
}
