package org.easysql.session;

@FunctionalInterface
public interface Filter<T>{
    static int ONLY_VALUE=31;
    static int ALL_VALUE=37;
    static int READ_MODE=67;
    boolean filter(T data);
}
