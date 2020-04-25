package org.easysql.annotation.starter;

import org.w3c.dom.DocumentType;

import java.lang.annotation.*;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-07 13:31
 **/
@Target(ElementType.TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface EasySqlApplication {}
