package org.easysql.annotation;

import org.easysql.info.ConstraintType;
import org.easysql.info.JoinInfo;

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
public @interface Column {
    String columnName() default "";
    String columnType() default "";
    ConstraintType[] constraintTypes() default {};
    ForeignKey[] foreignKey() default {@ForeignKey(point = "")};
    Index[] index() default {@Index};
    Join[] join() default {@Join(point = "")};
}
