package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.annotation.WriteDataSource;
import com.spark.bitrade.entity.TransferSilubium;
import com.spark.bitrade.mapper.dao.TransferSilubiumMapper;
import com.spark.bitrade.service.TransferSilubiumService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * silubium-silktrader交易记录表 服务实现类
 * </p>
 *
 * @author shenzucai
 * @since 2019-01-21
 */
@Service
public class TransferSilubiumServiceImpl extends ServiceImpl<TransferSilubiumMapper, TransferSilubium> implements TransferSilubiumService {

    @WriteDataSource
    @Override
    public void insertOrupdate(TransferSilubium transferSilubium) {
        insertOrUpdate(transferSilubium);
    }

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
    @ReadDataSource
    @Override
    public PageInfo<TransferSilubium> findRecordByUidAndAppId(String unit,Long memberId, Long appId,
                                                        Integer pageNo, Integer pageSize,
                                                        String startTime, String endTime){
        com.github.pagehelper.Page<TransferSilubium> page = PageHelper.startPage(pageNo, pageSize);
        //PageHelper会自动拦截到下面这查询sql
        this.baseMapper.findRecordByUidAndAppId(unit,memberId, appId, startTime , endTime);
        return page.toPageInfo();
    }
}
