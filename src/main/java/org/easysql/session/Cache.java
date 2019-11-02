package org.easysql.session;

import lombok.*;
import org.apache.commons.beanutils.BeanUtils;
import org.easysql.info.CRUD_VALUE;
import org.easysql.info.FieldInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Cache<T> {
    private static long time;
    private SessionHandler<T> handler;
    private LinkedHashMap<String, FieldInfo> fields_info;
    private int mode;
    @Setter
    private Filter<T> filter;
    @Getter
    private ArrayList<CacheData<T>> datas;


    public Cache(ArrayList<T> source_data, int mode, SessionHandler handler) {
        this.mode = mode;
        this.fields_info = handler.getFields_info();
        datas=new ArrayList<>();
        for (T t : source_data) {
            datas.add(new CacheData<>(t, CRUD_VALUE.ORIGIN_DATA_INDEX));
        }
    }

    //效率计时器
    public static void start_timer() {
        time = System.currentTimeMillis();
    }

    public static long calc_time() {
        return System.currentTimeMillis() - time;
    }


    //查询
    public ArrayList<T> selectAll() {
        ArrayList<T> result = new ArrayList<>();
        for (CacheData<T> cacheData : datas) {
            result.add(cacheData.getData());
        }
        return result;
    }

    public ArrayList<T> select() {
        return select(CRUD_VALUE.ALL_VALUE);
    }

    public ArrayList<T> select(int select_type) {
        ArrayList<T> ans = new ArrayList<T>();
        for (CacheData<T> cacheData : datas) {
            T data = cacheData.getData();
            if (filter.filter(data)) {
                ans.add(data);
                if (select_type == CRUD_VALUE.ONLY_VALUE) {
                    return ans;
                }
            }
        }
        return ans;
    }

    public void logAll(){
        for (CacheData<T> cacheData : datas) {
            T data = cacheData.getData();
            System.out.println(data.toString());
        }
    }

    //插入
    public void insert(T bean) {
        if (type_check()) {
            datas.add(new CacheData<>(bean,CRUD_VALUE.INSERTED_DATA_INDEX));
        }
    }

    public void insertListToCache(ArrayList<T> beans) {
        for (T t : beans) {
            insert(t);
        }
    }

    public void update(T bean) {
        if (type_check()) {
            try {
                for (int i=0;i<datas.size();i++) {
                    T data=datas.get(i).getData();
                    int type=datas.get(i).getType();
                    if (filter.filter(data)) {
                        for (String key : fields_info.keySet()) {
                            String field_name = fields_info.get(key).getField_name();
                            BeanUtils.setProperty(data, field_name, BeanUtils.getProperty(bean, field_name));
                        }
                        if (type == CRUD_VALUE.INSERTED_DATA_INDEX || type == CRUD_VALUE.ORIGIN_DATA_INDEX) {
                            datas.set(i,new CacheData<>(data,CRUD_VALUE.UPDATED_DATA_INDEX));
                        }
                        return;
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

    public void updateListToCache(ArrayList<T> beans) {
        for (T t : beans) {
            update(t);
        }
    }


    public void delete() {
        if (type_check()) {
            for (int i = 0; i < datas.size(); i++) {
               datas.set(i,new CacheData<>(datas.get(i).getData(),CRUD_VALUE.DELETED_DATA_INDEX));
            }
        }
    }

    public void order_by(String field_name, int ORDER_TYPE,int SORT_TYPE) {
        datas.sort((data1, data2) -> {
            T t1=data1.getData();
            T t2=data2.getData();
            try {
                switch (SORT_TYPE){
                    case CRUD_VALUE.NUMBER_SORT:{
                        double num1=Double.parseDouble(BeanUtils.getProperty(t1,field_name));
                        double num2=Double.parseDouble(BeanUtils.getProperty(t2,field_name));
                        double d_value = (num1 - num2) / (Math.abs(num1 - num2));
                        if(ORDER_TYPE==CRUD_VALUE.ASC){
                            return (int) d_value;
                        }
                        else if(ORDER_TYPE==CRUD_VALUE.DESC){
                            return -(int) d_value;
                        }
                    } break;
                    case CRUD_VALUE.LONG_NUMBER_SORT:{
                        long num1=Long.parseLong(BeanUtils.getProperty(t1,field_name));
                        long num2=Long.parseLong(BeanUtils.getProperty(t2,field_name));
                        long d_value = (num1 - num2) / (Math.abs(num1 - num2));
                        if(ORDER_TYPE==CRUD_VALUE.ASC){
                            return (int)d_value;
                        }
                        else if(ORDER_TYPE==CRUD_VALUE.DESC){
                            return -(int)d_value;
                        }
                    } break;
                    case CRUD_VALUE.STRING_SORT:{
                        String str1=BeanUtils.getProperty(t1,field_name);
                        String str2=BeanUtils.getProperty(t2,field_name);
                        if(ORDER_TYPE==CRUD_VALUE.ASC){
                            return str1.compareTo(str2);
                        }
                        else if(ORDER_TYPE==CRUD_VALUE.DESC){
                            return -(str1.compareTo(str2));
                        }
                    }break;
                    default:{
                        return 0;
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    public  LinkedHashMap<Object,ArrayList<T>> group_by(String group_field){
        LinkedHashMap<Object,ArrayList<T>> ans=new LinkedHashMap<>();
        try {
            for (CacheData<T> cacheData : datas) {
                T data = cacheData.getData();
                String key = BeanUtils.getProperty(data, group_field);
                if (!ans.containsKey(key)){
                    ArrayList<T> list=new ArrayList<>();
                    list.add(data);
                    ans.put(key,list);
                }
                else {
                    ArrayList<T> list=ans.get(key);
                    list.add(data);
                    ans.put(key,list);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return ans;
    }





    private boolean type_check() {
        if (mode == CRUD_VALUE.READ_WRITE_MODE) {
            return true;
        } else {
            System.out.println("error:This is a only_read cache,you can't modify it!");
            return false;
        }
    }


    public void close() {
        if (mode == CRUD_VALUE.READ_WRITE_MODE) {
            commit();
        }
        datas.clear();
        datas = null;
        System.gc();
    }


    private void commit() {
        ArrayList<T> insert_list=new ArrayList<>();
        ArrayList<T> update_list=new ArrayList<>();
        ArrayList<T> delete_list=new ArrayList<>();
        for (CacheData<T> cacheData : datas) {
            T data=cacheData.getData();
            int type=cacheData.getType();
            switch (type){
                case CRUD_VALUE.INSERTED_DATA_INDEX:{
                    insert_list.add(data);
                }break;
                case CRUD_VALUE.UPDATED_DATA_INDEX:{
                    update_list.add(data);
                }break;
                case CRUD_VALUE.DELETED_DATA_INDEX:{
                    delete_list.add(data);
                }break;
                default: {
                }break;
            }
        }
        handler.insertListToTable(insert_list);
        handler.updateListToTableByID(update_list);
        handler.deleteListAsID(delete_list);
    }

}



