package org.easysql.session;

@FunctionalInterface
public interface Filter<T>{
    static int ONLY_VALUE=31;
    static int ALL_VALUE=37;

    boolean filter(T data);
}
