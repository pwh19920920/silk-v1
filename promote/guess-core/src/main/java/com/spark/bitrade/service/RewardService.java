package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.dao.RewardDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.exception.InconsistencyException;
import com.spark.bitrade.mapper.dao.BettingPriceRangeMapper;
import com.spark.bitrade.mapper.dao.RewardMapper;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import sun.security.jca.GetInstance;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 中奖记录service
 * @author tansitao
 * @time 2018/9/14 11:39 
 */
@Service
public class RewardService extends BaseService{

    @Autowired
    private RewardMapper rewardMapper;
    @Autowired
    private RewardDao rewardDao;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private BranchRecordService branchRecordService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private PushMessageService pushMessageService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private RewardService rewardService;


   /**
    * 分页所有用户查询中奖信息
    * @author tansitao
    * @time 2018/9/14 10:45 
    */
   @ReadDataSource
    public PageInfo<Reward> queryPageByPeriodId(long periodId, int type, int pageNum, int pageSize){
        Page<Reward> page = PageHelper.startPage(pageNum, pageSize);
        rewardMapper.pageQueryByType(periodId,type);
        return page.toPageInfo();
    }

    /**
      * 倒叙查询领奖和红包信息
      * @author tansitao
      * @time 2018/9/26 14:14 
      */
    @ReadDataSource
    public PageInfo<Reward> pageQueryAll(long periodId, int pageNum, int pageSize){
        Page<Reward> page = PageHelper.startPage(pageNum, pageSize);
        rewardMapper.pageQueryAll(periodId);
        return page.toPageInfo();
    }

    public Reward save(Reward reward){
        return rewardDao.saveAndFlush(reward);
    }

    /**
     * 修改中奖状态
     * @author tansitao
     * @time 2018/9/17 22:36 
     */
    public MessageResult updateRewardStatus(RewardStatus status,Reward reward){
        int ret = rewardDao.updateRewardStatus(reward.getId(),status.getOrdinal(),reward.getVersion());
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("修改中奖状态失败");
        }
    }



    /**
      * 通过用户id和期数Id查询所有抽奖信息
      * @author tansitao
      * @time 2018/9/15 17:16 
      */
    @ReadDataSource
    public List<Reward> findOneByMemberIdAndPeriodId(long memberId, long periodId, RewardBusinessType type, int status){
        return rewardMapper.findOneByMemberIdAndPeriodId(memberId, periodId, type.getOrdinal(), status);
    }

    /**
      * 查找某一期红包信息
      * @author tansitao
      * @time 2018/9/15 17:16 
      */
    @ReadDataSource
    public Reward findRedPacket(long memberId, long periodId, RewardBusinessType type, int status){
        List<Reward> rewardList = rewardMapper.findOneByMemberIdAndPeriodId(memberId, periodId, type.getOrdinal(), status);
        return rewardList == null || rewardList.size()==0 ? null : rewardList.get(0);
    }

    /**
      * 通过投注id和类型查询中奖信息
      * @author tansitao
      * @time 2018/9/15 17:16 
      */
    @ReadDataSource
    public Reward findByBettingId(long memberId,long bettingId, RewardBusinessType type, int status){
        return rewardMapper.findByBettingId(memberId, bettingId, type.getOrdinal(), status);
    }

    /**
     * 分页查询某期活动中奖或者抢红包记录
     * @author fumy
     * @time 2018.09.15 17:19
     * @param periodId
     * @param type
     * @param memberId
     * @param pageNum
     * @param pageSize
     * @return true
     */
    @ReadDataSource
    public PageInfo<Reward> queryByPeriodIdAndType(Long periodId,Integer type,Long memberId,int pageNum,int pageSize){
        Page<Reward> page = PageHelper.startPage(pageNum, pageSize);
        rewardMapper.queryByPeriodIdAndType(periodId,type,memberId);
        return page.toPageInfo();
    }


    /**
      * 分页查询单个用户中奖信息
      * @author tansitao
      * @time 2018/9/14 10:45 
      */
    @ReadDataSource
    public PageInfo<Reward> pageQueryByMemberId(long memberId,int type, int pageNum, int pageSize){
        Page<Reward> page = PageHelper.startPage(pageNum, pageSize);
        rewardMapper.pageQueryByMemberId(memberId, type);
        return page.toPageInfo();
    }


    /***
      * 批量生成用户的中奖记录
      * @author yangch
      * @time 2018.09.17 22:34 
     * @param periodId  投注ID
     * @param rangeId 中奖价格区间
     * @param cashRatio 兑换汇率
     * @param cashSymbol 兑换币种
     */
    public int batchSavePrizeRecord (long periodId, long rangeId,
                               BigDecimal cashRatio, String cashSymbol){
        return rewardDao.batchSavePrizeRecord(periodId, rangeId, cashRatio, cashSymbol);
    }

    /**
     * 根据期数统计本期红包中奖总额
     * @param params
     * @return
     */
    public BigDecimal findRewardAmount(Map<String,Object> params){
        return rewardMapper.findRewardAmount(params);
    }

    /***
     * 根据期数统计本期中奖总额
     * @author yangch
     * @time 2018.09.19 14:44 
       * @param periodId
     * @param businessType
     * @param status
     */
    public BigDecimal findRewardAmount(long periodId, RewardBusinessType businessType, RewardStatus status){
        Map<String, Object> param = new HashMap<>();
        param.put("id", periodId);
        param.put("businessType", businessType.getCode());
        param.put("status", status.getCode());

        BigDecimal amount = findRewardAmount(param);
        if(null == amount){
            amount = BigDecimal.ZERO;
        }
        return amount;
    }

    /**
      * 弃奖记录处理
      * @author yangch
      * @time 2018.09.18 14:17 
     * @param periodId
     */
    @Transactional(rollbackFor = Exception.class)
    public int autoAbandonPrize(long periodId){
        return rewardDao.autoAbandonPrize(periodId);
    }


    /**
     * 一键领奖
     * @author tansitao
     * @time 2018/9/23 10:06 
     */
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal dealOnePeriodReward(List<Reward> rewardList) throws Exception{
        BigDecimal allReward = BigDecimal.ZERO;
        for (Reward reward:rewardList) {
            MessageResult result = this.getService().updateRewardStatus(RewardStatus.RECEIVED,reward);
            if (result.getCode() != 0) {
                throw new InconsistencyException(msService.getMessage("ONE_CLICK_FAIL"));
            }
            //保存中奖收入记录
            BranchRecord branchRecordIn = new BranchRecord();
            branchRecordIn.setAmount(reward.getRewardNum());
            branchRecordIn.setBranchType(BranchRecordBranchType.INCOME);
            branchRecordIn.setBusinessType(BranchRecordBusinessType.GUESS_AWARD);
            branchRecordIn.setExpendMemberId(reward.getMemberId());
            branchRecordIn.setPeriodId(reward.getPeriodId());
            branchRecordIn.setSpecial(BooleanEnum.IS_FALSE);
            branchRecordIn.setSymbol(reward.getSymbol());
            branchRecordService.save(branchRecordIn);
            //将当前奖励叠加到总和
            allReward = allReward.add(reward.getRewardNum());
            //推送领奖信息
            pushMessageService.pushAwardMessage(reward);
        }

        //获取、增加用户钱包余额
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(rewardList.get(0).getSymbol(),rewardList.get(0).getMemberId());
        if(memberWallet == null)
        {
            memberWallet = memberWalletService.createMemberWallet(rewardList.get(0).getMemberId(), coinService.findByUnit(rewardList.get(0).getSymbol()));
        }
        MessageResult walletResult = memberWalletService.increaseBalance(memberWallet.getId(),allReward);
        if (walletResult.getCode() != 0)
        {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        return allReward;
    }

    /**
      * 领某一次投注奖励
      * @author tansitao
      * @time 2018/9/23 10:06 
      */
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal dealOneBettingReward(Reward reward) throws Exception{
        MessageResult result = this.getService().updateRewardStatus(RewardStatus.RECEIVED,reward);
        if (result.getCode() != 0) {
            throw new InconsistencyException(msService.getMessage("NOT_REPEAT"));
        }
        //保存中奖收入记录
        BranchRecord branchRecordIn = new BranchRecord();
        branchRecordIn.setAmount(reward.getRewardNum());
        branchRecordIn.setBranchType(BranchRecordBranchType.INCOME);
        branchRecordIn.setBusinessType(BranchRecordBusinessType.GUESS_AWARD);
        branchRecordIn.setExpendMemberId(reward.getMemberId());
        branchRecordIn.setPeriodId(reward.getPeriodId());
        branchRecordIn.setSpecial(BooleanEnum.IS_FALSE);
        branchRecordIn.setSymbol(reward.getSymbol());
        branchRecordService.save(branchRecordIn);

        //获取、增加用户钱包余额
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(reward.getSymbol(),reward.getMemberId());
        if(memberWallet == null)
        {
            memberWallet = memberWalletService.createMemberWallet(reward.getMemberId(), coinService.findByUnit(reward.getSymbol()));
        }
        MessageResult walletResult = memberWalletService.increaseBalance(memberWallet.getId(), reward.getRewardNum());
        if (walletResult.getCode() != 0)
        {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        //推送领奖信息
        pushMessageService.pushAwardMessage(reward);
        return reward.getRewardNum();
    }

    /**
     * 处理开启游戏红包
     * @author tansitao
     * @time 2018/10/23 17:49 
     */
    @Transactional(rollbackFor = Exception.class)
    public void dealOpenRedPacket(Member member, BettingConfig bettingConfig, RedPacket redPacket) throws Exception{
        Reward reward = rewardService.findRedPacket(member.getId(), bettingConfig.getId(), RewardBusinessType.REDPACKET, -1);
        Assert.isTrue(reward == null, msService.getMessage("NOT_REPEAT_DRAW"));

        //保存开启红包支出记录
        BranchRecord branchRecordOut = new BranchRecord();
        branchRecordOut.setAmount(bettingConfig.getRedpacketUseNum());
        branchRecordOut.setBranchType(BranchRecordBranchType.DISBURSE);
        branchRecordOut.setBusinessType(BranchRecordBusinessType.REDPACKET_BET);
        branchRecordOut.setExpendMemberId(member.getId());
//        branchRecord.setHappenTime();
//        branchRecord.setIncomeMemberId();
        branchRecordOut.setPeriodId(bettingConfig.getId());
        branchRecordOut.setSpecial(BooleanEnum.IS_FALSE);
        branchRecordOut.setSymbol(bettingConfig.getRedpacketSymbol());
        branchRecordService.save(branchRecordOut);

        //获取、减少用户钱包余额
        MemberWallet redMemberWallet = memberWalletService.findByCoinUnitAndMemberId(bettingConfig.getRedpacketSymbol(), member.getId());
        if (redMemberWallet == null) {
            memberWalletService.createMemberWallet(member.getId(), coinService.findByUnit(bettingConfig.getRedpacketSymbol()));
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        MessageResult resWalletResult = memberWalletService.decreaseBalance(redMemberWallet.getId(), bettingConfig.getRedpacketUseNum());
        if (resWalletResult.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }

        //红包中奖
        if (redPacket != null
                && redPacket.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            //保存红包抽奖记录
            reward = new Reward();
            reward.setBusinessType(RewardBusinessType.REDPACKET);
            reward.setGetTime(new Date());
            reward.setIsBestLuck(redPacket.getIsMax());
            reward.setMemberId(member.getId());
            reward.setPeriodId(bettingConfig.getId());
            reward.setPromotionCode(member.getPromotionCode());
            reward.setRewardNum(redPacket.getAmount());
            reward.setStatus(RewardStatus.PRIZE);
            reward.setSymbol(redPacket.getSymbol());
            reward.setVersion(0);
            rewardService.save(reward);

            //保存红包收入记录
            BranchRecord branchRecordIn = new BranchRecord();
            branchRecordIn.setAmount(reward.getRewardNum());
            branchRecordIn.setBranchType(BranchRecordBranchType.INCOME);
            branchRecordIn.setBusinessType(BranchRecordBusinessType.REDPACKET_AWARD);
            branchRecordIn.setExpendMemberId(member.getId());
//        branchRecord.setHappenTime();
//        branchRecord.setIncomeMemberId();
            branchRecordIn.setPeriodId(bettingConfig.getId());
            branchRecordIn.setSpecial(BooleanEnum.IS_FALSE);
            branchRecordIn.setSymbol(bettingConfig.getGuessSymbol());
            branchRecordService.save(branchRecordIn);

            //获取、增加用户钱包余额
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(reward.getSymbol(), member.getId());
            if (memberWallet == null) {
                memberWallet = memberWalletService.createMemberWallet(member.getId(), coinService.findByUnit(reward.getSymbol()));
            }
            MessageResult walletResult = memberWalletService.increaseBalance(memberWallet.getId(), reward.getRewardNum());
            if (walletResult.getCode() != 0) {
                throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
            }
            //推送中奖红包信息
            pushMessageService.pushOpenRedMessage(redPacket, member.getPromotionCode());
        } else {
            //红包未中奖，保存红包抽奖记录
            reward = new Reward();
            reward.setBusinessType(RewardBusinessType.REDPACKET);
            reward.setGetTime(new Date());
            reward.setIsBestLuck(BooleanEnum.IS_FALSE);
            reward.setMemberId(member.getId());
            reward.setPeriodId(bettingConfig.getId());
            reward.setPromotionCode(member.getPromotionCode());
            reward.setRewardNum(BigDecimal.ZERO);
            reward.setStatus(RewardStatus.UNPRIZE);
            reward.setSymbol(bettingConfig.getRedpacketPrizeSymbol());
            reward.setVersion(0);
            rewardService.save(reward);
        }
    }
    private RewardService getService(){
        return SpringContextUtil.getBean(this.getClass());
    }
}
