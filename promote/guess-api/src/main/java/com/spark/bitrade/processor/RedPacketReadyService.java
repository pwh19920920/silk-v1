package com.spark.bitrade.processor;

import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.BettingConfig;
import com.spark.bitrade.entity.BettingState;
import com.spark.bitrade.entity.Jackpot;
import com.spark.bitrade.service.*;
import com.spark.bitrade.task.RedPacketManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>红包准备服务</p>
 * @author octopus
 * @date 2018-9-20
 */
@Service
@Slf4j
public class RedPacketReadyService {

    @Autowired
    private BettingConfigService bettingConfigService;

    @Autowired
    private JackpotService jackpotService;

    @Autowired
    private BranchRecordService branchRecordService;

    @Autowired
    private BettingRecordService bettingRecordService;

    @Autowired
    private RedPacketManager redPacketManager;

    @Autowired
    private BettingStateService bettingStateService;

    @Autowired
    private RewardService rewardService;

    @Autowired
    private CoinExchangeRateService coinExchangeRateService;


    /**
     * 红包准备
     * @param bettingState
     *             配置分配消息
     */
   public void redPacketReady(BettingState bettingState){
       //判断属于抢红包业务类型并且标记状态为未处理
       if(BettingStateOperateType.OP_REDPACKET_READY == bettingState.getOperate() &&
               BettingStateOperateMark.UNTREATED == bettingState.getMark()){
           try {
               //先清空红包
               redPacketManager.clearRedPacket();
               //查询投注配置
               List<BettingConfig> bettingConfigs = bettingConfigService.findAllOfLately();
               if(null == bettingConfigs || bettingConfigs.isEmpty()){
                   log.info("**********当前没有生效的活动**********");
                   return;
               }
               //当前生效的活动(本期)
               BettingConfig bettingConfig = bettingConfigs.get(0);

               //判断活动是否被删除
               if(bettingConfig.getDeleted() == BooleanEnum.IS_TRUE){
                   log.info("**********当前活动已被删除**********");
                   return;
               }
               //红包功能未开启
               if(bettingConfig.getRedpacketState() == BooleanEnum.IS_FALSE){
                   log.info("**********当前活动未开启红包功能**********");
                   return;
               }

               //活动是否已经结束
               if(bettingConfig.getStatus() == BettingConfigStatus.STAGE_FINISHED){
                   log.info("**********当前活动已完成**********");
                   return;
               }



               //根据投注期数查询当期红包数量
               Map<String,Object> params = new HashMap<>();
               params.put("id",bettingConfig.getId());
               params.put("businessType", BranchRecordBusinessType.RED_PACKET_TOTAL.getCode());
               BigDecimal redpacketAmount = branchRecordService.findRedpacketDeductAmount(params);
               log.info("***********本期红包扣除: {} **********",redpacketAmount);
               //查询当期投注人数
               Integer betNum = bettingRecordService.findBetCount(params);
               log.info("***********投注人数为{}************",betNum);
               //判断扣除红包是否满足生成红包条件
               if (redpacketAmount == null || redpacketAmount.compareTo(BigDecimal.ZERO) <= 0){
                   log.info("***********红包扣除额为空**********");
                   return;
               }
               //判断投注人数
               if(betNum == 0){
                   log.info("***********投注人数为0************");
                   return;
               }


               //上期活动 ,在红包扣除时已经累加了上期沉淀
               /*Map<String,Object> forwarParams = new HashMap<>();
               forwarParams.put("id",bettingConfig.getId());
               forwarParams.put("status", BettingConfigStatus.STAGE_FINISHED.getCode());
               BettingConfig forwardBetConfig = bettingConfigService.findForwardBetConfig(forwarParams);
               if(null != forwardBetConfig){
                   //查询上期沉淀奖池,按照当期配置红包扣除比例，扣除上期奖池沉淀
                   Jackpot jackpot =jackpotService.findByPeriodId(forwardBetConfig.getId());
                   //上期总的沉淀
                   BigDecimal forwar = jackpot.getJackpotBalance().add(jackpot.getRedpacketBalance());
                   //上期扣除
                   BigDecimal forwarsum = forwar.multiply(bettingConfig.getRedpacketRatio());
                   //累加当期
                   redpacketAmount = redpacketAmount.add(forwarsum);
               }*/
               //上期活动 ,在红包扣除时已经累加了上期沉淀

               //异常情况导致当期红包未抢完而中断,查询当期抢过的部分红包，总额需要减去被抢过的红包
               //查询本期红包已开奖的总额
               Map<String,Object> paramsRedpacketed = new HashMap<>();
               paramsRedpacketed.put("id",bettingState.getPeriodId());
               paramsRedpacketed.put("businessType", RewardBusinessType.REDPACKET.getCode());
               paramsRedpacketed.put("status", RewardStatus.PRIZE.getCode());
               BigDecimal grantAmount = rewardService.findRewardAmount(paramsRedpacketed);
               if(null != grantAmount && grantAmount.compareTo(BigDecimal.ZERO) > 0){
                   //减去本期已开奖的红包
                   redpacketAmount = redpacketAmount.subtract(grantAmount);
               }

               //投注的币种
               String betSymbol = bettingConfig.getBetSymbol();
               //红包支付币种
               String redpacketSymbol = bettingConfig.getRedpacketSymbol();
               //红包奖励币种
               String redpacketPrizeSymbol = bettingConfig.getRedpacketPrizeSymbol();

               //红包币种支付数量
               BigDecimal redpacketUseNum = bettingConfig.getRedpacketUseNum();
               if(null == redpacketUseNum || redpacketUseNum.compareTo(BigDecimal.ZERO) <=0 ){
                   log.error("未配置红包支付数量.");
                   return;
               }
               /*String usdt = "USDT";
                StringBuilder sb = new StringBuilder();
                sb.append(betSymbol).append("/").append(usdt);
                coinExchangeRateService.findSymbolThumb(sb.toString());*/

               //转换投注币种和红包币种的汇率
               BigDecimal betSymbolToredpacketSymbolRate = coinExchangeRateService.toRate(betSymbol,redpacketSymbol);
               log.info("币种汇率：{}",betSymbolToredpacketSymbolRate);
               if(betSymbol.equals(redpacketPrizeSymbol)) {
                   //计算单个红包面额限制
                   BigDecimal unitLimit = betSymbolToredpacketSymbolRate.divide(redpacketUseNum, RedPacketConstant.DIGITS, BigDecimal.ROUND_DOWN);

                   log.info("单个红包面额：{}", unitLimit);
                   //BigDecimal unitLimit = new BigDecimal("0.149");
                   //先清空红包
                   redPacketManager.clearRedPacket();
                   //生成红包
                   redPacketManager.generateRedPacket(bettingConfig, redpacketAmount, betNum, unitLimit);
                   int redPacketSize = redPacketManager.getRedPacketSize();
                   if (redPacketSize > 0) {
                       log.info("****************************已生成红包个数:{} ********************************", redPacketSize);
                       //修改红包状态为已准备完成
                       updateBettingState(bettingState);
                   }
               }else{
                   log.info("****************************投注币种和红包奖励币种不一样需要做转换,下个版本迭代********************************");


               }
           } catch (Exception e){
               //log.error("****************************红包生成失败********************************");
               e.printStackTrace();
           }
       }
   }

    /**
     * 修改红包状态为已准备完成
     * @param bettingState
     *             分配状态
     */
   public void updateBettingState(BettingState bettingState){
       bettingState.setMark(BettingStateOperateMark.TREATED);
       bettingStateService.save(bettingState);
       log.info("***********修改红包状态为已准备完成*******");
   }



}
