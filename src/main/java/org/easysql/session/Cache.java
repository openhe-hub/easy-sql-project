package org.easysql.session;

import lombok.Setter;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class Cache <T> {
    private static long time;
    private SessionHandler handler;
    private int mode;
    @Setter
    private Filter<T> filter;

    private ArrayList<ArrayList<T>> datas;

    public static final int READ_MODE=67;
    public static final int READ_WRITE_MODE=13;
    public static final int ORIGIN_DATA_INDEX=0;
    public static final int INSERTED_DATA_INDEX=1;
    public static final int UPDATED_DATA_INDEX=2;
    public static final int DELETED_DATA_INDEX=3;
    private final int STACK_NUM=4;


    public Cache(ArrayList<T> source_data,int mode,SessionHandler handler) {
        this.mode=mode;
        this.handler=handler;
        datas=new ArrayList<ArrayList<T>>();
        ArrayList<T> origin_data = source_data;
        datas.add(origin_data);
        for (int i = 1; i < STACK_NUM; i++) {
            ArrayList<T> temp=new ArrayList<>();
            datas.add(temp);
        }
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
        ArrayList<T> result=new ArrayList<>();
        for (ArrayList<T> data : datas) {
            result.addAll(data);
        }
        return result;
    }

    public ArrayList<T> select(){
        return select(Filter.ALL_VALUE);
    }

    public ArrayList<T> select(int type){
        ArrayList<T> ans=new ArrayList<T>();
        for (ArrayList<T> data:datas) {
            for (T t:data) {
                if (filter.filter(t)){
                    ans.add(t);
                    if (type==Filter.ONLY_VALUE){
                        return ans;
                    }
                }
            }
        }
        return ans;
    }

    //插入
    public void insert(T bean){
        if (type_check()) {
            datas.get(INSERTED_DATA_INDEX).add(bean);
        }
    }

    public void insertListToCache(ArrayList<T> beans){
        for (T t : beans) {
            insert(t);
        }
    }

    public void update(T bean){
        if (type_check()) {
            try {
                for (int i=0;i<datas.size();i++) {
                    ArrayList<T> data=datas.get(i);
                    for (int j=0;j<data.size();j++) {
                        T t=data.get(j);
                        if (filter.filter(t)) {
                            for (String key:handler.getFields_info().keySet()){
                                String field_name=handler.getFields_info().get(key).getField_name();
                                BeanUtils.setProperty(t,field_name,BeanUtils.getProperty(bean,field_name));
                            }
                             if (i==Cache.INSERTED_DATA_INDEX||i==Cache.ORIGIN_DATA_INDEX){
                                 moveTo(i,Cache.UPDATED_DATA_INDEX,t,j);
                             }
                             return;
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateListToCache(ArrayList<T> beans){
        for (T t : beans) {
            update(t);
        }
    }


    public void delete(){
        if (type_check()) {
            for (int i=0;i<datas.size();i++) {
                ArrayList<T> data=datas.get(i);
                for (int j=0;j<data.size();j++) {
                    T t=data.get(j);
                    if (filter.filter(t)) {
                        moveTo(i,Cache.DELETED_DATA_INDEX,t,j);
                        data.remove(j);
                        return;
                    }
                }
            }
        }
    }

    private boolean type_check(){
        if (mode==Cache.READ_WRITE_MODE){
            return true;
        }
        else {
            System.out.println("error:This is a only_read cache,you can't modify it!");
            return false;
        }
    }

    private void moveTo(int from, int to, T bean, int fromIndex){
        datas.get(to).add(bean);
        datas.get(from).remove(fromIndex);
    }



    //public ArrayList<T> selectAs(){}

    public void close(){
        if (mode==Cache.READ_WRITE_MODE){
            commit();
        }
        datas.clear();
        datas=null;
    }



    private void commit() {
        handler.insertListToTable(datas.get(Cache.INSERTED_DATA_INDEX));
        handler.updateListToTableAsID(datas.get(Cache.UPDATED_DATA_INDEX));
        handler.deleteListAsID(datas.get(Cache.DELETED_DATA_INDEX));
    }

}

