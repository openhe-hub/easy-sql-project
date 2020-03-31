package org.easysql.session;

import lombok.*;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.easysql.helper.CommonValue;
import org.easysql.helper.LoggerHelper;
import org.easysql.info.FieldInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;

public class Cache<T> {
    private static long time;
    private SessionHandler<T> handler;
    private LinkedHashMap<String, FieldInfo> fieldsInfo;
    private int mode;
    @Setter
    private Filter<T> filter;
    @Getter
    private ArrayList<CacheData<T>> dataList;
    private Logger logger;

    public Cache(ArrayList<T> sourceData, int mode, SessionHandler<T> handler) {
        this.mode = mode;
        this.fieldsInfo = handler.getFieldsInfo();
        dataList = new ArrayList<>();
        for (T t : sourceData) {
            dataList.add(new CacheData<>(t, CommonValue.ORIGIN_DATA_INDEX));
        }
        logger = Logger.getLogger(Cache.class);
        filter = (data)->true;
        LoggerHelper.ProcessOutput("Cache(" + handler.getClassName() + ")" + " has benn created successfully.", logger);
    }

    //效率计时器
    public static void startTimer() {
        time = System.currentTimeMillis();
    }

    public static long calcTime() {
        return System.currentTimeMillis() - time;
    }


    //查询
    public ArrayList<T> selectAll() {
        ArrayList<T> result = new ArrayList<>();
        for (CacheData<T> cacheData : dataList) {
            result.add(cacheData.getData());
        }
        return result;
    }

    public ArrayList<T> select() {
        return select(CommonValue.ALL_VALUE);
    }

    public ArrayList<T> select(int selectType) {
        ArrayList<T> ans = new ArrayList<T>();
        for (CacheData<T> cacheData : dataList) {
            T data = cacheData.getData();
            if (filter.filter(data)) {
                ans.add(data);
                if (selectType == CommonValue.ONLY_VALUE) {
                    return ans;
                }
            }
        }
        return ans;
    }

    public void logAll(){
        setFilter((T data)-> true);
        log();
    }

    public void log(){
        for (CacheData<T> cacheData : dataList) {
            T data = cacheData.getData();
            if (filter.filter(data)){
                LoggerHelper.DataOutput(data,logger);
            }
        }
    }

    //插入
    public void insert(T bean) {
        if (modeCheck()) {
            dataList.add(new CacheData<>(bean,CommonValue.INSERTED_DATA_INDEX));
        }
    }

    public void insertListToCache(ArrayList<T> beans) {
        for (T t : beans) {
            insert(t);
        }
    }

    public void update(T bean) {
        if (modeCheck()) {
            try {
                for (int i = 0; i< dataList.size(); i++) {
                    T data= dataList.get(i).getData();
                    int type= dataList.get(i).getType();
                    if (filter.filter(data)) {
                        for (String key : fieldsInfo.keySet()) {
                            String field_name = fieldsInfo.get(key).getFieldName();
                            BeanUtils.setProperty(data, field_name, BeanUtils.getProperty(bean, field_name));
                        }
                        if (type == CommonValue.INSERTED_DATA_INDEX || type == CommonValue.ORIGIN_DATA_INDEX) {
                            dataList.set(i,new CacheData<>(data,CommonValue.UPDATED_DATA_INDEX));
                        }
                        return;
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
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
        if (modeCheck()) {
            for (int i = 0; i < dataList.size(); i++) {
               dataList.set(i,new CacheData<>(dataList.get(i).getData(),CommonValue.DELETED_DATA_INDEX));
            }
        }
    }

    public ArrayList<T> limit(int pageSize,int pageIndex){
        ArrayList<T> result= new ArrayList<> ();
        for (int i = pageSize*pageIndex; i < pageSize*(pageIndex+1); i++) {
            result.add(dataList.get(i).getData());
        }
        return result;
    }

    public void orderBy(Comparator<T> comparator){
        dataList.sort((a,b)->{
            T data1=a.getData();
            T data2=b.getData();
            return comparator.compare(data1, data2);
        });
    }

    public void orderBy(String field_name, int ORDER_TYPE, int SORT_TYPE) {
        dataList.sort((data1, data2) -> {
            T t1=data1.getData();
            T t2=data2.getData();
            try {
                switch (SORT_TYPE){
                    case CommonValue.NUMBER_SORT:{
                        double num1=Double.parseDouble(BeanUtils.getProperty(t1,field_name));
                        double num2=Double.parseDouble(BeanUtils.getProperty(t2,field_name));
                        double d_value = (num1 - num2) / (Math.abs(num1 - num2));
                        if(ORDER_TYPE==CommonValue.ASC){
                            return (int) d_value;
                        }
                        else if(ORDER_TYPE==CommonValue.DESC){
                            return -(int) d_value;
                        }
                    } break;
                    case CommonValue.LONG_NUMBER_SORT:{
                        long num1=Long.parseLong(BeanUtils.getProperty(t1,field_name));
                        long num2=Long.parseLong(BeanUtils.getProperty(t2,field_name));
                        long d_value = (num1 - num2) / (Math.abs(num1 - num2));
                        if(ORDER_TYPE==CommonValue.ASC){
                            return (int)d_value;
                        }
                        else if(ORDER_TYPE==CommonValue.DESC){
                            return -(int)d_value;
                        }
                    } break;
                    case CommonValue.STRING_SORT:{
                        String str1=BeanUtils.getProperty(t1,field_name);
                        String str2=BeanUtils.getProperty(t2,field_name);
                        if(ORDER_TYPE==CommonValue.ASC){
                            return str1.compareTo(str2);
                        }
                        else if(ORDER_TYPE==CommonValue.DESC){
                            return -(str1.compareTo(str2));
                        }
                    }break;
                    default:{
                        return 0;
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    public  LinkedHashMap<Object,ArrayList<T>> groupBy(String group_field){
        LinkedHashMap<Object,ArrayList<T>> ans=new LinkedHashMap<>();
        try {
            for (CacheData<T> cacheData : dataList) {
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
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return ans;
    }

    public long countBy(){
        long count=0;
        for (CacheData<T> cacheData : dataList) {
            if (filter.filter(cacheData.getData())){
                count++;
            }
        }
        return count;
    }

    private boolean modeCheck() {
        if (mode == CommonValue.READ_WRITE_MODE) {
            return true;
        } else {
            System.out.println("error:This is a only_read cache,you can't modify it!");
            return false;
        }
    }

    public void close() {
        if (mode == CommonValue.READ_WRITE_MODE) {
            commit();
        }
        dataList.clear();
        dataList = null;
        System.gc();
    }

    private void commit() {
        ArrayList<T> insertList = new ArrayList<>();
        ArrayList<T> updateList = new ArrayList<>();
        ArrayList<T> deleteList = new ArrayList<>();
        dataList.forEach(cacheData -> {
            T data = cacheData.getData();
            int type = cacheData.getType();
            switch (type) {
                case CommonValue.INSERTED_DATA_INDEX:{
                    insertList.add(data);
                }break;
                case CommonValue.UPDATED_DATA_INDEX:{
                    updateList.add(data);
                }break;
                case CommonValue.DELETED_DATA_INDEX: {
                    deleteList.add(data);
                }break;
                default: {}break;
                    }});
        handler.insertListToTable(insertList);
        handler.updateListToTableByID(updateList);
        handler.deleteListAsID(deleteList);
    }
}



