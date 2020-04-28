package com.spark.bitrade.pagination;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.util.List;

/**
 *
 * @author Zhang Jinwei
 * @date 2018年01月15日
 */
@Data
public class SpecialPageData<E> {
    private static final long serialVersionUID = 1L;
    private List<E> context;
    private int currentPage;
    private int totalPage;//总分页数
    private int pageNumber;//每页数量
    private long totalElement;//总记录数




    public SpecialPageData(PageInfo<E> pageInfo){
        this.totalElement = pageInfo.getTotal();
        this.totalPage = pageInfo.getPages();
        //this.number = pageInfo.getPageNum()-1;
        this.currentPage = pageInfo.getPrePage(); //兼容性问题，为getPageNum的前一页
        this.pageNumber = pageInfo.getPageSize();
        this.context = pageInfo.getList();
    }

    public static SpecialPageData toPageData(PageInfo pageInfo){
        return new SpecialPageData<>(pageInfo);
    }

    /**
     * 解决兼容性问题，PageHelper的pageNo应该从1开始，以前都是从0开始
     * @param pageNo
     * @return
     */
    public static int pageNo4PageHelper(int pageNo){
        return pageNo+1;  //兼容性问题，pageNo应该从1开始，以前都是从0开始
    }

}
