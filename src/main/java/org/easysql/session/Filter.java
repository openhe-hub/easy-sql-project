package org.easysql.session;

@FunctionalInterface
public interface Filter<T>{
    boolean filter(T data);
}
