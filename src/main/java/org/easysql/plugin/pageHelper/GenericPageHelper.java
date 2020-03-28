package org.easysql.plugin.pageHelper;

import lombok.*;
import org.apache.log4j.Logger;
import org.easysql.helper.CommonValue;
import org.easysql.helper.Configuration;
import org.easysql.helper.LoggerHelper;
import org.easysql.session.Cache;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-03-28 16:28
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class GenericPageHelper<T> implements PageHelper<T> {
    private int pageSize;
    private int rowNum;
    private int pageNum;
    private int currPage;
    private LinkedHashMap<Integer, ArrayList<T>> data;
    private Logger logger;

    /**
     * init method
     *
     * @param pageSize page size,row number in a page
     */
    @Override
    public void init(int pageSize) {
        this.pageSize = pageSize;
        data = new LinkedHashMap<>();
        logger= Configuration.createLogger(GenericPageHelper.class);
        currPage=0;
    }

    /**
     * @param pageNum page number (index starts with 0)
     * @return list of data
     */
    @Override
    public ArrayList<T> getPage(int pageNum) {
        currPage++;
        return data.get(pageNum);
    }

    /**
     * @param startRowNumber start page number(index starts with 0,inclusive)
     * @param endRowNumber   end page number(exclusive)
     * @return list of data
     */
    @Override
    public ArrayList<T> getPage(int startRowNumber, int endRowNumber) {
        return null;
    }

    /**
     * close and release resource method
     */
    @Override
    public void close() {}

    @Override
    public ArrayList<T> nextPage() {
        if (!hasNext()){
            logger.error("error:index out of range.Expect: "+(++currPage)+",max: "+pageNum);
            return null;
        }else {
            ArrayList<T> result=data.get(currPage);
            currPage++;
            return result;
        }
    }

    private boolean hasNext() {
        return currPage<pageNum;
    }

    public void calculate() {
        pageNum = rowNum % pageSize == 0 ? rowNum / pageSize : rowNum / pageSize + 1;
    }

    public void logAll(){
        for (int i = 0; i < pageNum; i++) {
            logger.info("page"+(currPage+1));
            logger.info(CommonValue.PRINT_SEPERATOR);
            ArrayList<T> temp=nextPage();
            LoggerHelper.DataOutput(temp,logger);
        }
    }
}
