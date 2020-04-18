package org.easysql.annotation.pojo;
import java.lang.annotation.*;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-07 13:23
 **/
@Target(ElementType.TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface EasySqlPojo {
    String tableName();
    String sqlFile() default "";
}
