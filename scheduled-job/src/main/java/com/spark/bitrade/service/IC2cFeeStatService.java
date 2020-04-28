package com.spark.bitrade.service;

import com.spark.bitrade.entity.C2cFeeStat;
import com.baomidou.mybatisplus.service.IService;

import java.util.List;

/**
 * <p>
 * 总c2c交易手续费统计表 服务类
 * </p>
 *
 * @author fumy
 * @since 2018-06-19
 */
public interface IC2cFeeStatService extends IService<C2cFeeStat> {

    List<C2cFeeStat> c2cFeeTotal(String startTime,String endTime);

    List<C2cFeeStat> dayOfFeeStat(String startTime,String endTime);

    String getMaxOpDate();

    int insertAndUpdate(C2cFeeStat cfs);

    int updateTotal(C2cFeeStat cfs);

    //////////////////////////////////“内部商家”C2C交易手续费统计

    List<C2cFeeStat> innerC2cFeeTotal(String startTime,String endTime);

    List<C2cFeeStat> innerC2cDayOfFeeStat(String startTime,String endTime);

    String getInnerMaxOpDate();

    int innerInsertAndUpdate(C2cFeeStat cfs);

    int innerUpdateTotal(C2cFeeStat cfs);

    int innerFeeCount();

    boolean innerFeeInsert(C2cFeeStat csf);

    //////////////////////////////////“外部商家”C2C交易手续费统计

    List<C2cFeeStat> outerC2cFeeTotal(String startTime,String endTime);

    List<C2cFeeStat> outerC2cDayOfFeeStat(String startTime,String endTime);

    String getOuterMaxOpDate();

    int outerInsertAndUpdate(C2cFeeStat cfs);

    int outerUpdateTotal(C2cFeeStat cfs);

    int outerFeeCount();

    boolean outerFeeInsert(C2cFeeStat csf);

}
