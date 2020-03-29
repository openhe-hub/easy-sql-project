package org.easysql.plugin.pageHelper;

import org.easysql.session.Filter;

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
     * @param startPageNumber start page number(index starts with 0,inclusive)
     * @param endPageNumber end page number(exclusive)
     * @return list of data
     */
    ArrayList<T> getPage(int startPageNumber,int endPageNumber);

    /**
     *
     * @return get next page (current index refers to property:currPage)
     */
    ArrayList<T> nextPage();

    /**
     *
     * @return get the previous page (current index refers to property:currPage)
     */
    ArrayList<T> prevPage();

    /**
     *
     * @param startRowNumber start page index (index starts with 0,inclusive)
     * @param endRowNumber end page index(index ends with pageSize-1,exclusive)
     * @return list of data
     */
    ArrayList<T> getRows(int startRowNumber,int endRowNumber);

    /**
     *
     * @param filter filter condition
     * @param bound search bound,array length expect to be 2<br>bound[0] is startPage(inclusive)<br>bound[1] is endPage(exclusive)
     * @return search result
     */
    ArrayList<T> search(Filter<T> filter,int[] bound);

    /**
     *
     * @param filter filter condition
     * @return search result
     * @description no bound restriction
     */
    ArrayList<T> search(Filter<T> filter);

    /**
     *
     * @param filter filter condition
     * @param bound search bound,array length expect to be 2<br>bound[0] is startPage(inclusive)<br>bound[1] is endPage(exclusive)
     * @return if data not exists,return 0;otherwise return number of result
     */
    int has(Filter<T> filter,int[] bound);

    /**
     *
     * @param filter filter condition
     * @return if data not exists,return 0;otherwise return number of result
     */
    int has(Filter<T> filter);

    /**
     * close and release resource method
     */
    void close();
}
