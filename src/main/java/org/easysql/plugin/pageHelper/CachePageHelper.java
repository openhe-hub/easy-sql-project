package org.easysql.plugin.pageHelper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.easysql.session.Cache;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-03-28 16:20
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class CachePageHelper<T> extends GenericPageHelper<T> {
    public CachePageHelper(Cache<T> cache,int pageSize){
        super.init(pageSize);
        super.setRowNum((int)cache.countBy());
        super.calculate();
        loadOriginalData(cache);
    }

    public CachePageHelper(ArrayList<T> data,int pageSize){
        super.init(pageSize);
        super.setRowNum(data.size());
        super.calculate();
        loadOriginalData(data);
    }
    
    public void loadOriginalData(Cache<T> cache) {
        setData(loadOriginalData(cache.selectAll()));
    }

    public LinkedHashMap<Integer,ArrayList<T>> loadOriginalData(ArrayList<T> originalData) {
        LinkedHashMap<Integer,ArrayList<T>> data=new LinkedHashMap<>();
        for (int i = 0; i < getPageNum(); i++) {
            ArrayList<T> pageTemp=new ArrayList<>();
            for (int j = 0; j < getPageSize()&&(j+i*getPageSize())<getRowNum(); j++) {
                pageTemp.add(originalData.get((i*getPageSize()+j)));
            }
            data.put(i,pageTemp);
        }
        setData(data);
        return data;
    }

}
