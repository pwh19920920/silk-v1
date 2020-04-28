package com.spark.bitrade.service.impl;

import com.spark.bitrade.entity.ExchangeFeeStat;
import com.spark.bitrade.mapper.dao.ExchangeFeeStatMapper;
import com.spark.bitrade.mapper.dao.MemberMapper;
import com.spark.bitrade.service.IExchangeFeeStatService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fumy
 * @since 2018-06-16
 */
@Service
public class ExchangeFeeStatServiceImpl extends ServiceImpl<ExchangeFeeStatMapper, ExchangeFeeStat> implements IExchangeFeeStatService {

    @Autowired
    MemberMapper memberMapper;

    @Override
    public List<ExchangeFeeStat> exchangeFeeTotal(String startTime,String endTime) {
        return baseMapper.exchangeFeeTotal(startTime,endTime);
    }

    @Override
    public List<ExchangeFeeStat> dayOfFeeStat(String startTime,String endTime) {
        return baseMapper.dayOfFeeStat(startTime,endTime);
    }

    @Override
    public String getMaxOpDate() {
        return baseMapper.getMaxOpDate();
    }


    @Override
    public int insertAndUpdate(ExchangeFeeStat efs) {
        return baseMapper.insertAndUpdate(efs);
    }

    @Override
    public int updateTotal(ExchangeFeeStat efs) {
        return baseMapper.updateTotal(efs);
    }

    /////////////////////////////////////////////操盘账户币币交易手续费统计

    @Override
    public List<ExchangeFeeStat> traderExchangeFeeTotal(String startTime,String endTime) {
        //2--操盘手
        String types="2";
        List<String> memberList = memberMapper.getMemberIdsByTypes(types);
        String traderId = memberList.get(0);
        return baseMapper.traderExchangeFeeTotal(startTime,endTime,traderId);
    }

    @Override
    public List<ExchangeFeeStat> traderDayOfFeeStat(String startTime,String endTime) {
        //2--操盘手
        String types="2";
        List<String> memberList = memberMapper.getMemberIdsByTypes(types);
        String traderId = memberList.get(0);
        return baseMapper.traderDayOfFeeStat(startTime,endTime,traderId);
    }

    @Override
    public String getTraderMaxOpDate() {
        return baseMapper.getTraderMaxOpDate();
    }

    @Override
    public int selectTraderTotalCount() {
        return baseMapper.traderCount();
    }

    @Override
    public int traderUpdateTotal(ExchangeFeeStat efs) {
        return baseMapper.traderUpdateTotal(efs);
    }

    @Override
    public int traderInsertAndUpdate(ExchangeFeeStat efs) {
        return baseMapper.traderInsertAndUpdate(efs);
    }

    @Override
    public boolean traderInsert(ExchangeFeeStat efs) {
        int row = baseMapper.traderInsert(efs);
        return row > 0 ? true : false;
    }
}
