package com.spark.bitrade.task;

import com.spark.bitrade.entity.BettingState;

import com.spark.bitrade.processor.RedPacketReadyService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


/***
 * 红包准备任务
 * @author yangch,octopus
 * @time 2018.09.14 14:31
 */
@Component
@Slf4j
public class RedPacketReadyTaskHandlerImpl implements IBettingTaskHandler {

   /* @Autowired
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
    private RewardService rewardService;*/

   /* @Autowired
    private CoinExchangeRateService coinExchangeRateService;*/
   @Autowired
   private RedPacketReadyService redPacketReadyService;

    @Override
//    @Async
    public void run(BettingState bettingState) {
        if(null == bettingState){
            log.warn("bettingState is null.");
            return;
        }
        redPacketReadyService.redPacketReady(bettingState);
        /*

        if(null == bettingState){
            return;
        }
        //判断属于抢红包业务类型并且标记状态为未处理
        if(BettingStateOperateType.OP_REDPACKET_READY == bettingState.getOperate() &&
                BettingStateOperateMark.UNTREATED == bettingState.getMark()){

            try {
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

                //根据投注期数查询当期红包数量
                Map<String,Object> params = new HashMap<>();
                params.put("id",bettingConfig.getId());
                params.put("businessType", BranchRecordBusinessType.RED_PACKET_TOTAL);
                BigDecimal redpacketAmount = branchRecordService.findRedpacketDeductAmount(params);

                //查询当期投注人数
                Integer betNum = bettingRecordService.findBetCount(params);

                //判断扣除红包是否满足生成红包条件
                if (redpacketAmount == null){
                    log.info("***********红包扣除额为空**********");
                    return;
                }
                //判断投注人数
                if(betNum == 0){
                    log.info("***********投注人数为0************");
                    return;
                }
                //上期活动
                Map<String,Object> forwarParams = new HashMap<>();
                forwarParams.put("id",bettingConfig.getId());
                forwarParams.put("status", BettingConfigStatus.STAGE_FINISHED);
                BettingConfig forwardBetConfig = bettingConfigService.findForwardBetConfig(forwarParams);
                if(null != forwardBetConfig){
                    //查询上期沉淀奖池,按照当期配置红包扣除比例，扣除上期奖池沉淀
                    Jackpot jackpot =jackpotService.findByPeriodId(forwardBetConfig.getId());
                    BigDecimal forwar = jackpot.getJackpotBalance().add(jackpot.getRedpacketBalance());
                    //上期扣除
                    BigDecimal forwarsum = forwar.multiply(bettingConfig.getRedpacketRatio());
                    //累加当期
                    redpacketAmount = redpacketAmount.add(forwarsum);
                }
                //异常情况导致当期红包未抢完而中断,查询当期抢过的部分红包，总额需要减去被抢过的红包
                //查询本期红包已开奖的总额
                Map<String,Object> paramsRedpacketed = new HashMap<>();
                paramsRedpacketed.put("id",bettingState.getPeriodId());
                paramsRedpacketed.put("businessType", RewardBusinessType.REDPACKET);
                paramsRedpacketed.put("status", RewardStatus.PRIZE);
                BigDecimal grantAmount = rewardService.findRewardAmount(paramsRedpacketed);
                if(null != grantAmount && grantAmount.compareTo(new BigDecimal(0)) > 0){
                    //减去本期已开奖的红包
                    redpacketAmount = redpacketAmount.subtract(grantAmount);
                }

                //投注的币种
                String betSymbol = bettingConfig.getBetSymbol();
                String redpacketSymbol = bettingConfig.getRedpacketSymbol();
                String usdt = "USDT";
                //红包币种支付数量
                BigDecimal redpacketUseNum = bettingConfig.getRedpacketUseNum();
                if(null == redpacketUseNum || redpacketUseNum.compareTo(BigDecimal.ZERO) <=0 ){
                    log.error("RedpacketUseNum is empty.");
                    return;
                }

               *//* StringBuilder sb = new StringBuilder();
                sb.append(betSymbol).append("/").append(usdt);
                coinExchangeRateService.findSymbolThumb(sb.toString());*//*

               //转换投注币种和红包币种的汇率
               // BigDecimal betSymbolToredpacketSymbolRate = coinExchangeRateService.toRate(betSymbol,redpacketSymbol);

                //计算单个红包面额限制
                //BigDecimal unitLimit = betSymbolToredpacketSymbolRate.divide(redpacketUseNum, 8,BigDecimal.ROUND_DOWN);
                BigDecimal unitLimit = new BigDecimal("0.149");
                //先清空红包
                redPacketManager.clearRedPacket();
                //生成红包
                redPacketManager.generateRedPacket(bettingConfig,redpacketAmount,betNum,unitLimit);
                if(redPacketManager.getRedPacketSize() > 0) {
                    //修改状态为已完成
                    BettingState bs = new BettingState();
                    bs.setId(bettingState.getId());
                    bs.setMark(BettingStateOperateMark.TREATED);
                    bettingStateService.save(bs);
                }
            } catch (Exception e){
                log.error("****************************Redpacket generate failed.********************************");
                e.printStackTrace();
            }
        }*/
    }
}
