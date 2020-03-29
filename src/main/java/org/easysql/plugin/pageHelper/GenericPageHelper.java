package org.easysql.plugin.pageHelper;

import lombok.*;
import org.apache.log4j.Logger;
import org.easysql.helper.CommonValue;
import org.easysql.helper.Configuration;
import org.easysql.helper.LoggerHelper;
import org.easysql.session.Cache;
import org.easysql.session.Filter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public void init(int pageSize) {
        this.pageSize = pageSize;
        data = new LinkedHashMap<>();
        logger= Configuration.createLogger(GenericPageHelper.class);
        currPage=0;
    }


    @Override
    public ArrayList<T> getPage(int pageNum) {
        currPage++;
        return data.get(pageNum);
    }

    @Override
    public ArrayList<T> search(Filter<T> filter, int[] bound) {
        ArrayList<T> result= new ArrayList<> ();
        for (int i = bound[0]; i <= bound[1]; i++) {
            result.addAll(data.get(i).stream().filter(filter::filter).collect(Collectors.toList()));
        }
        return result;
    }

    @Override
    public ArrayList<T> search(Filter<T> filter) {
        return search(filter,new int[]{0,pageNum-1});
    }

    @Override
    public int has(Filter<T> filter, int[] bound) {
        int result=0;
        for (int i = bound[0]; i <= bound[1]; i++) {
           result += (int) data.get(i).stream().filter(filter::filter).count();
        }
        return result;
    }

    @Override
    public int has(Filter<T> filter) {
        return has(filter, new int[] {0,pageNum-1});
    }

    @Override
    public ArrayList<T> getPage(int startPageNumber, int endPageNumber) {
        ArrayList<T> result= new ArrayList<> ();
        for (int i = startPageNumber; i < endPageNumber; i++) {
            result.addAll(data.get(i));
        }
        return result;
    }

    @Override
    public ArrayList<T> getRows(int startRowNumber, int endRowNumber) {
        ArrayList<T> result = new ArrayList<>();
        endRowNumber--;
        int start = 0, end = 0;
        int startPage = (startRowNumber % pageSize == 0) ? startRowNumber / pageSize - 1 : startRowNumber / pageSize;
        int startRow = (startRowNumber % pageSize == 0) ? pageSize - 1 : startRowNumber % pageSize - 1;
        int endPage = (endRowNumber % pageSize == 0) ? endRowNumber / pageSize - 1 : endRowNumber / pageSize;
        int endRow = (endRowNumber % pageSize == 0) ? pageSize - 1 : endRowNumber % pageSize - 1;
        for (int i = startPage; i <= endPage; i++) {
            start = (i == startPage) ? startRow : 0;
            end = (i == endPage) ? endRow : 0;
            result.addAll(data.get(i).subList(start, end + 1));
        }
        return result;
    }

    /**
     * close and release resource method
     */
    @Override
    public void close() {
    }

    @Override
    public ArrayList<T> nextPage() {
        if (!hasNext()) {
            logger.error("error:index out of range.Expect: " + (++currPage) + ",max: " + pageNum);
            return null;
        } else {
            ArrayList<T> result = data.get(currPage);
            currPage++;
            return result;
        }
    }

    @Override
    public ArrayList<T> prevPage() {
        if (!hasPrevious()) {
            logger.error("error:index out of range.Expect: " + (--currPage) + ",min: " + 0);
            return null;
        } else {
            ArrayList<T> result = data.get(currPage);
            currPage--;
            return result;
        }
    }

    public boolean hasNext() {
        return currPage < pageNum;
    }

    public boolean hasPrevious() {
        return currPage >= 0;
    }

    public void calculate() {
        pageNum = rowNum % pageSize == 0 ? rowNum / pageSize : rowNum / pageSize + 1;
    }

    public void logAll() {
        for (int i = 0; i < pageNum; i++) {
            logger.info("page" + (currPage + 1));
            logger.info(CommonValue.PRINT_SEPERATOR);
            ArrayList<T> temp = nextPage();
            LoggerHelper.DataOutput(temp,logger);
        }
    }
}
