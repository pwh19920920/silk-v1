package com.spark.bitrade.service;

import com.spark.bitrade.constant.BranchRecordBranchType;
import com.spark.bitrade.constant.BranchRecordBusinessType;
import com.spark.bitrade.dao.BettingRecordDao;
import com.spark.bitrade.dao.BranchRecordDao;
import com.spark.bitrade.entity.BettingRecord;
import com.spark.bitrade.entity.BranchRecord;
import com.spark.bitrade.mapper.dao.BettingRecordMapper;
import com.spark.bitrade.mapper.dao.BranchRecordMapper;
import com.spark.bitrade.service.Base.BaseService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 投注支入支出记录表service
 * @author tansitao
 * @time 2018/9/15 10:38 
 */
@Service
public class BranchRecordService extends BaseService{

    @Autowired
    private BranchRecordMapper branchRecordMapper;
    @Autowired
    private BranchRecordDao branchRecordDao;

    public BranchRecord save(BranchRecord branchRecord){
        return branchRecordDao.saveAndFlush(branchRecord);
    }


    /***
      * 获取指定周期、用户、类型的流水记录
      * @author yangch
      * @time 2018.09.18 11:29 
     * @param periodId 投注周期
     * @param incomeMemberId 用户id
     * @param businessType 业务类型
     */
    public List<BranchRecord> queryAllIncomeRecordByCondition(
            long periodId, long incomeMemberId, BranchRecordBusinessType businessType){
        return  branchRecordDao.queryAllByPeriodIdAndIncomeMemberIdAndBusinessType(
                periodId,  incomeMemberId,  businessType);
    }

    /**
     * 根据期数id和业务类型为红包扣除查询当期红包扣除总额,生成红包的总额
     * @param params
     *             查询参数
     * @return redparketNum
     *              红包总额
     */
    public BigDecimal findRedpacketDeductAmount(Map<String,Object> params){
        BigDecimal redpacketNum = branchRecordMapper.findRedpacketDeductAmount(params);
        return  redpacketNum;
    }

    /***
      * 根据周期id、指定的业务类型和支付类型统计总额
      * @author yangch
      * @time 2018.09.19 15:58 
     * @param periodId
     * @param businessType
     * @param branchType
     */
    public BigDecimal findBusinessAmount(
            long periodId, BranchRecordBusinessType businessType,
            BranchRecordBranchType branchType){
        BigDecimal amout = branchRecordMapper.findBusinessAmount(periodId, businessType, branchType);
        if(amout == null){
            amout = BigDecimal.ZERO;
        }

        return amout;
    }

}
