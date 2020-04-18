package org.easysql.annotation.starter;

import java.lang.annotation.*;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-07 13:30
 **/
@Target(ElementType.TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface EasySqlDao {
    String pojoName();
}