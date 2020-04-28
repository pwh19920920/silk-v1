package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.entity.TransferSilubium;

/**
 * <p>
 * silubium-silktrader交易记录表 服务类
 * </p>
 *
 * @author shenzucai
 * @since 2019-01-21
 */
public interface TransferSilubiumService extends IService<TransferSilubium> {

    void insertOrupdate(TransferSilubium transferSilubium);

    /**
     * 查询流水记录
     * @param memberId 用户ID
     * @param appId 应用ID
     * @param pageNo
     * @param pageSize
     * @param startTime 开始时间，可选
     * @param endTime  截至时间，可选
     * @return
     */
    public PageInfo<TransferSilubium> findRecordByUidAndAppId(String unit,Long memberId, Long appId,
                                                              Integer pageNo, Integer pageSize,
                                                              String startTime, String endTime);
}
