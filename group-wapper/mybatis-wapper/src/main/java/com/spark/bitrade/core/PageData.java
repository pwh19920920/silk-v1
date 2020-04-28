package com.spark.bitrade.core;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/***
  * 统一的分页数据格式
 *
  * @author yangch
  * @time 2018.06.22 18:52
  */
@Data
public class PageData<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<T> content;
    /**
     * 总记录数
     */
    private long totalElements;
    /**
     * 总分页数
     */
    private int totalPages;
    /**
     * 分页开始页面
     */
    private long number;
    /**
     * 每页数量
     */
    private int size;

    public PageData() {
    }

    public PageData(PageInfo<T> pageInfo, List<T> content) {
        this.totalElements = pageInfo.getTotal();
        this.totalPages = pageInfo.getPages();
        //this.number = pageInfo.getPageNum()-1;
        //兼容性问题，为getPageNum的前一页
        this.number = pageInfo.getPrePage();
        this.size = pageInfo.getPageSize();
        this.content = content;
    }

    public PageData(PageInfo<T> pageInfo) {
        this.totalElements = pageInfo.getTotal();
        this.totalPages = pageInfo.getPages();
        //this.number = pageInfo.getPageNum()-1;
        //兼容性问题，为getPageNum的前一页
        this.number = pageInfo.getPrePage();
        this.size = pageInfo.getPageSize();
        this.content = pageInfo.getList();
    }

    public static PageData toPageData(PageInfo pageInfo) {
        return new PageData<>(pageInfo);
    }

    public static PageData toPageData(PageInfo pageInfo, List content) {
        return new PageData<>(pageInfo, content);
    }

    /**
     * mybatis 兼容性的分页
     *
     * @param pageNo   页码，注意需要从0（代表第一页）开始
     * @param pageSize 每页数量
     * @param <E>
     * @return
     */
    public static <E> Page<E> startPageOfcompatibility(int pageNo, int pageSize) {
        return PageHelper.startPage(pageNo, pageSize);
    }

    /**
     * 解决兼容性问题，PageHelper的pageNo应该从1开始，以前都是从0开始
     *
     * @param pageNo
     * @return
     */
    public static int pageNo4PageHelper(int pageNo) {
        //兼容性问题，pageNo应该从1开始，以前都是从0开始
        return pageNo + 1;
    }

}
