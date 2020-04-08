package org.easysql.annotation;

import org.easysql.info.ConstraintType;

import java.lang.annotation.*;

/**
 * @program: EasySqlProject
 * @description annotation to configure index
 * @author: he
 * @create: 2020-04-07 14:17
 **/
@Target(ElementType.FIELD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Index {
    String name() default "";
    ConstraintType type() default ConstraintType.INDEX;
}
