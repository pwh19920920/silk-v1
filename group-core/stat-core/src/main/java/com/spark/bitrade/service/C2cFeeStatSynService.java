package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.mapper.dao.C2cFeeStatMapper;
import com.spark.bitrade.vo.C2cFeeStatSynVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * C2C交易手续费综合查询
 * @author Zhang Yanjun
 * @time 2018.10.10 17:39
 */
@Service
public class C2cFeeStatSynService {
    @Autowired
    private C2cFeeStatMapper c2cFeeStatMapper;

    //分页
    public PageInfo<C2cFeeStatSynVO> findC2cFeeStatSyn(int pageNo, int pageSize,Integer type,String unit,String startTime,String endTime){
        Page<C2cFeeStatSynVO> page= PageHelper.startPage(pageNo, pageSize);
        this.c2cFeeStatMapper.findC2cFeeStatAllAndInnerAndOuter(type, unit, startTime, endTime);
        return page.toPageInfo();
    }

    //导出
    public List<C2cFeeStatSynVO> findC2cFeeStatSynOut(Integer type,String unit,String startTime,String endTime){
        List<C2cFeeStatSynVO> list=c2cFeeStatMapper.findC2cFeeStatAllAndInnerAndOuter(type, unit, startTime, endTime);
        for (int i=0;i<list.size();i++){
            list.get(i).setTypeOut(list.get(i).getType()==0?"买":list.get(i).getType()==1?"卖":null);
        }
        return list;
    }
}
