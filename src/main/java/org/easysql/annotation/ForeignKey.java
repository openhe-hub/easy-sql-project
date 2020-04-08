package org.easysql.annotation;

import org.easysql.info.ConstraintType;

import java.lang.annotation.*;

/**
 * @program: EasySqlProject
 * @description annotation to configure foreign key constraint
 * @author: he
 * @create: 2020-04-07 14:16
 **/
@Target(ElementType.FIELD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignKey {
    //table.column
    String point();
    String name() default "";
    ConstraintType type() default ConstraintType.ONE_TO_ONE;
}
