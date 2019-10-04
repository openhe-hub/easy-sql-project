package org.easysql.session;

import lombok.Setter;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;

public class Cache <T> {
    private static long time;
    private ArrayList<T> data;
    private SessionHandler handler;
    private int mode;
    @Setter
    private Filter<T> filter;

    public Cache(ArrayList<T> data,int mode) {
        this.data = data;
        this.mode=mode;
    }

    //效率计时器
    public static void start_timer(){
        time=System.currentTimeMillis();
    }

    public static long calc_time(){
        return System.currentTimeMillis()- time;
    }


    //查询
    public ArrayList<T> selectAll(){
        return data;
    }

    public ArrayList<T> selectAsID(){
        return selectAsID(Filter.ALL_VALUE);
    }

    public ArrayList<T> selectAsID(int type){
        ArrayList<T> ans=new ArrayList<T>();
        for (T t:data) {
            if (filter.filter(t)){
                ans.add(t);
                if (type==Filter.ONLY_VALUE){
                    return ans;
                }
            }
        }
        return ans;
    }


    //public ArrayList<T> selectAs(){}

    public void commit(){
        if (mode!=Filter.READ_MODE){
            //commit
        }
        close();
    }



    private void close() {
        data.clear();
        data=null;
    }

}

