package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.PayTransferType;
import com.spark.bitrade.dto.PayRecordDto;
import com.spark.bitrade.entity.PayFastRecord;

import java.util.List;

/**
 * @author Zhang Yanjun
 * @time 2019.01.18 14:02
 */
public interface IPayFastRecordService extends IService<PayFastRecord>{

    int save(PayFastRecord payFastRecord);

    PayFastRecord findOneByTradeSn(String tradeSn);

    PageInfo<PayFastRecord> findByMember(String unit, Long memberId, int pageNo, int pageSize,int tradeType, PayTransferType transferType,
                                          String platform, String platformTo,String startTime,String endTime);

    PageInfo<PayFastRecord> findlist(int pageNo, int pageSize, PayTransferType transferType,String startTime,String endTime,
                                     Long fromId,Long toId,String fromPhone,String toPhone,String fromAppid,String toAppid);


    /**
     * 查询silkpay云端流水
     * @author Zhang Yanjun
     * @time 2019.03.07 18:31
     * @param pageNo 页码 1开始
     * @param pageSize 页大小
     * @param fromAppid 支付方渠道
     * @param toAppid  收款方渠道
     */
    PageInfo<PayFastRecord> findFastRecord(String unit,int pageNo, int pageSize, Long memberId, String appId,
                                          String startTime, String endTime);

    /**
     * 处理silkpay云端流水记录
     * @author Zhang Yanjun
     * @time 2019.03.09 22:43
     * @param memberId
     * @param appId
     * @param list
     */
    List<PayRecordDto> getFastRecord(Long memberId, String appId,List<PayFastRecord> list);
}
