package org.easysql.plugin.pageHelper;

import java.util.ArrayList;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-03-28 16:14
 **/
public interface PageHelper<T> {
    /**
     * init method
     * @param pageSize page size,row number in a page
     */
     void init(int pageSize);

    /**
     *
     * @param pageNum page number (index starts with 0)
     * @return list of data
     */
    ArrayList<T> getPage(int pageNum);

    /**
     *
     * @param startRowNumber start page number(index starts with 0,inclusive)
     * @param endRowNumber end page number(exclusive)
     * @return list of data
     */
    ArrayList<T> getPage(int startRowNumber,int endRowNumber);

    /**
     *
     * @return get next page (current index refers to property:currPage)
     */
    ArrayList<T> nextPage();

    /**
     * close and release resource method
     */
    void close();
}
