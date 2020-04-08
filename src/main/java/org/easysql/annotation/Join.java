package org.easysql.annotation;

import org.easysql.info.ConstraintType;

import java.lang.annotation.*;

/**
 * @program: EasySqlProject
 * @description annotation to configure join
 * @author: he
 * @create: 2020-04-07 14:17
 **/
@Target(ElementType.FIELD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Join {
    //table.column
    String point();
    ConstraintType type() default ConstraintType.ONE_TO_ONE;
    ConstraintType form() default ConstraintType.INNER_JOIN;
}
