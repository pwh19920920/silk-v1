package com.spark.bitrade.service.impl;

import com.spark.bitrade.entity.WithdrawFeeStat;
import com.spark.bitrade.mapper.dao.MemberMapper;
import com.spark.bitrade.mapper.dao.WithdrawFeeStatMapper;
import com.spark.bitrade.service.IWithdrawFeeStatService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 提币手续费统计表 服务实现类
 * </p>
 *
 * @author fumy
 * @since 2018-06-13
 */
@Service
public class WithdrawFeeStatServiceImpl extends ServiceImpl<WithdrawFeeStatMapper, WithdrawFeeStat> implements IWithdrawFeeStatService {

    @Autowired
    MemberMapper memberMapper;

    /**
    * 获取前一天的数据
    * @author fumy
    * @time 2018.06.13 14:48
     * @param
    * @return true
    */
    @Override
    public List<WithdrawFeeStat> listFeeStat(String startTime,String endTime) {
        return baseMapper.listFeeTotal(startTime,endTime);
    }

   /**
   * 获取每日总提币手续费统计数据
   * @author fumy
   * @time 2018.06.13 17:38
    * @param
   * @return true
   */
    @Override
    public List<WithdrawFeeStat> dayOfFeeStat(String startTime,String endTime) {
        return baseMapper.dayOfFeeStat(startTime,endTime);
    }

    @Override
    public String getMaxOpDate() {
        return baseMapper.getMaxOpDate();
    }

    @Override
    public int insertAndUpdate(WithdrawFeeStat wfs){return baseMapper.insertOrUpdate(wfs);}

    @Override
    public int updateTotal(WithdrawFeeStat wfs){return baseMapper.updateTotal(wfs);}

   ///////////////////////////操盘手提币手续费统计

    @Override
    public List<WithdrawFeeStat> traderListFeeStat(String startTime,String endTime) {
        //2--操盘手
        String types="2";
        List<String> memberList = memberMapper.getMemberIdsByTypes(types);
        String traderId = memberList.get(0);
        return baseMapper.traderListFeeStat(startTime,endTime,traderId);
    }

    @Override
    public List<WithdrawFeeStat> traderDayOfFeeStat(String startTime,String endTime) {
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
    public int traderInsertAndUpdate(WithdrawFeeStat wfs) {
        return baseMapper.traderInsertAndUpdate(wfs);
    }

    @Override
    public int traderUpdateTotal(WithdrawFeeStat wfs) {
        return baseMapper.traderUpdateTotal(wfs);
    }

    @Override
    public boolean traderInsert(WithdrawFeeStat wfs) {
        int row = baseMapper.traderInsert(wfs);
        return row > 0 ? true : false;
    }

    @Override
    public int selectTraderTotalCount() {
        return baseMapper.traderCount();
    }
}
