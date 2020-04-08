package org.easysql.bean;

import org.easysql.annotation.Column;
import org.easysql.annotation.EasySqlPojo;
import org.easysql.annotation.Id;
import org.easysql.info.ConstraintType;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-08 13:08
 **/
@EasySqlPojo(tableName = "user")
public class User {
    @Id
    private String uid;
    @Column(constraintTypes = ConstraintType.UNIQUE)
    private String name;
    @Column
    private String password;
}
