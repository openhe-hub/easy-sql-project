package org.easysql.cache;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-03 16:17
 **/
@FunctionalInterface
public interface Action<T> {
    T doAction(T bean);
}
