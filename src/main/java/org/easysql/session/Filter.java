package org.easysql.session;

@FunctionalInterface
public interface Filter<T>{
    /**
     * @param data data to operate
     * @return isFiltered
     */
    boolean filter(T data);
}
