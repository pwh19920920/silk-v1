package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.BettingRecordStatus;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.BranchRecordBranchType;
import com.spark.bitrade.constant.BranchRecordBusinessType;
import com.spark.bitrade.dao.BettingRecordDao;
import com.spark.bitrade.dto.BettingRecordDTO;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.mapper.dao.BettingRecordMapper;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.MessageResult;
import org.apache.ibatis.annotations.Param;
import org.apache.xmlbeans.impl.piccolo.util.FactoryServiceFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 投票记录service
 * @author tansitao
 * @time 2018/9/15 10:38 
 */
@Service
public class BettingRecordService extends BaseService{

    @Autowired
    BettingRecordMapper bettingRecordMapper;
    @Autowired
    private BettingRecordDao bettingRecordDao;
    @Autowired
    private BranchRecordService branchRecordService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private BettingRecordService bettingRecordService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private LocaleMessageSourceService msService;

    public BettingRecord save(BettingRecord bettingRecord){
        return bettingRecordDao.saveAndFlush(bettingRecord);
    }

    public BettingRecord findOne(long id){
        return bettingRecordMapper.selectByPrimaryKey(id);
    }

    /**
      * 通过用户id和期数id查询中奖信息
      * @author tansitao
      * @time 2018/9/15 17:43 
      */
    public BettingRecord findByMemberIdAndPeriodId(long id){
        return bettingRecordMapper.selectByPrimaryKey(id);
    }

    /**
      * 分页查询用户投票历史信息
      * @author tansitao
      * @time 2018/9/14 10:45 
      */
    public PageInfo<BettingRecordDTO> queryPageByMemberId(long memberId, int pageNum, int pageSize){
        Page<BettingRecordDTO> page = PageHelper.startPage(pageNum, pageSize);
        bettingRecordMapper.pageQueryByMemberId(memberId);
        return page.toPageInfo();
    }

    /**
     * 投注记录查询
     * @author fumy
     * @time 2018.09.15 17:00
     * @param periodId
     * @param memberId
     * @param pageNum
     * @param pageSize
     * @return true
     */
    public PageInfo<BettingRecord> queryByPeriodId(Long periodId, Long memberId, int pageNum, int pageSize){
        Page<BettingRecord> page = PageHelper.startPage(pageNum, pageSize);
        bettingRecordMapper.queryByPeriodId(periodId,memberId);
        return page.toPageInfo();
    }

    /**
     * 判断用户是否参加了活动
     * @author tansitao
     * @time 2018/9/19 11:05 
     */
    public boolean findListByPeriodId(Long periodId, Long memberId){
        return bettingRecordMapper.fondOneByPeriodIdAndMemberId(periodId,memberId) == null ? false : true;
    }

    /**
     * 总投注数量
     * @param periodId 投注周期id
     * @return
     */
    public BigDecimal queryBetTotal(long periodId){
        BigDecimal betTotal = bettingRecordMapper.queryBetTotal(periodId);
        if(null == betTotal) {
            return BigDecimal.ZERO;
        }

        return betTotal;
    }

    /**
     * 按价格区间计算投注总额
     * @param rangeId 投注价格区间id
     * @return
     */
    public BigDecimal queryBetTotalByPriceRange(long periodId, long rangeId){
        BigDecimal betTotal = bettingRecordMapper.queryBetTotalByPriceRange(periodId, rangeId);
        if(null == betTotal) {
            return BigDecimal.ZERO;
        }

        return betTotal;
    }

    /***
      * 获取指定周期的投注用户
      * @author yangch
      * @time 2018.09.18 9:38 
     * @param periodId
     */
    public List<String> queryBettingUserByPeriodId(long periodId){
        return  bettingRecordMapper.queryBettingUserByPeriodId(periodId);
    }

    /***
      * 获取指定周期中有推荐邀请关系投注记录
      * @author yangch
      * @time 2018.09.18 9:38 
     * @param periodId
     */
    public List<BettingRecord> queryInviterBettingRecordByPeriodId(long periodId){
        return bettingRecordMapper.queryInviterBettingRecordByPeriodId(periodId);
    }

    /**
     * 统计当期投注人数(去重重复投注的)
     * @param params
     *
     * @return
     */
    public Integer findBetCount(Map<String,Object> params){
        Integer betNum = bettingRecordMapper.findBetCount(params);
        if (betNum == null){
            betNum = 0;
        }
        return betNum;
    }

    /**
      * 查询开启短信的投注信息
      * @author tansitao
      * @time 2018/9/19 16:25 
      */
    List<BettingRecord> findOpenSmsRecord(long periodId){
        return bettingRecordMapper.findOpenSmsRecord(periodId);
    }

    /**
     * 更改中奖状态
     * @author yangch
      * @time 2018.09.18 9:38 
     * @param rangeId 中奖价格区间ID
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int updatePraiseStatus( long rangeId){
       return bettingRecordDao.updatePraiseStatus(rangeId);
    }

    /**
     * 未中奖投注状态更改
     * @author yangch
      * @time 2018.09.18 9:38 
     * @param rangeId 中奖价格区间ID
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateLostStatus( long periodId, long rangeId){
        return bettingRecordDao.updateLostStatus(periodId, rangeId);
    }

    /**
     * 处理会员参加游戏投票
     * @author tansitao
     * @time 2018/10/23 17:14 
     */
    @Transactional(rollbackFor = Exception.class)
    public void dealBetting(BooleanEnum useSms, BettingConfig bettingConfig, Member member, BettingRecord bettingRecord,BranchRecord branchRecord,BranchRecord smsbranchRecord){
        //扣除短信费用
        if(useSms == BooleanEnum.IS_TRUE && smsbranchRecord != null){
            //保存支出收入记录
            branchRecordService.save(smsbranchRecord);

            //获取用户钱包信息
            MemberWallet sluMemberWallet = memberWalletService.findByCoinUnitAndMemberId(bettingConfig.getSmsSymbol(),member.getId());
            Assert.isTrue(sluMemberWallet != null && sluMemberWallet.getBalance().compareTo(bettingConfig.getSmsUseNum()) >= 0, bettingConfig.getSmsSymbol() + msService.getMessage("INSUFFICIENT_BALANCE"));
            MessageResult walletResult = memberWalletService.decreaseBalance(sluMemberWallet.getId(), bettingConfig.getSmsUseNum());
            if (walletResult.getCode() != 0)
            {
                throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
            }
        }

        //保存投票记录
        bettingRecordService.save(bettingRecord);

        //保存流水记录
        branchRecordService.save(branchRecord);

        //获取、减少用户钱包余额
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(bettingRecord.getBetSymbol(),member.getId());
        if(memberWallet == null)
        {
            memberWalletService.createMemberWallet(member.getId(), coinService.findByUnit(bettingRecord.getBetSymbol()));
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        MessageResult walletResult = memberWalletService.decreaseBalance(memberWallet.getId(), branchRecord.getAmount());
        if (walletResult.getCode() != 0)
        {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
    }

}