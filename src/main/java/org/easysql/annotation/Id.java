package org.easysql.annotation;

import org.easysql.info.ConstraintType;

import java.lang.annotation.*;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-07 13:29
 **/
@Target(ElementType.FIELD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
    String columnName() default "";
    String columnType() default "integer";
    ConstraintType generatePolicy() default ConstraintType.AUTO_INCREMENT;
    ConstraintType[] constraintTypes() default {ConstraintType.PRIMARY_KEY};
}
