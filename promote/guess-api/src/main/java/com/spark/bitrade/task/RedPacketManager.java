package com.spark.bitrade.task;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.RedPacketConstant;
import com.spark.bitrade.entity.BettingConfig;
import com.spark.bitrade.entity.RedPacket;
import com.spark.bitrade.task.redpacket.RandomRedPacketGenerate;
import com.spark.bitrade.task.redpacket.RedPacketGenerate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * <p>红包管理</p>
 * @author octopus
 */
@Component
@Slf4j
public class RedPacketManager {

    //红包缓存
    private static final List<RedPacket> redPacketCache = new LinkedList<>();

    /**
     * 红包生成器
     * @param bettingConfig
     *             活动配置
     * @param redparketAmount
     *             红包总额
     * @param betNum
     *             投注人数
     * @param unitLimit
     *             红包最小面额限制
     */
    public void generateRedPacket(BettingConfig bettingConfig, BigDecimal redparketAmount, Integer betNum,BigDecimal unitLimit){

        log.info("*****************红包准备开始******************");
        log.info("*****************红包总额：{} ",redparketAmount);
        //计算生成红包个数 1.先计算大红包 2.再计算随机红包
        //获取大红包比例
        BigDecimal redpacketGradeRatio = bettingConfig.getRedpacketGradeRatio();
        //获取随机红包生成系数
        BigDecimal redpacketRandomRatio = bettingConfig.getRedpacketCoefficientRatio();
        //计算大红包面额,直接取整数. 大红包面额=红包总额*大红包比例
        BigDecimal bigRedpacket = redparketAmount.multiply(redpacketGradeRatio).setScale(RedPacketConstant.DIGITS,BigDecimal.ROUND_DOWN);
        log.info("大红包面额:{}",bigRedpacket);
        //计算随机红包总额=红包总额-大红包总额
        BigDecimal randomRedpacketAmount = redparketAmount.subtract(bigRedpacket).setScale(RedPacketConstant.DIGITS,BigDecimal.ROUND_DOWN);
        log.info("随机红包总额:{}",randomRedpacketAmount);
        //红包个数=投注人数*获取随机红包生成系数
        int sumRedpacketNum = new  BigDecimal(betNum).multiply(redpacketRandomRatio).setScale(0,BigDecimal.ROUND_DOWN).intValue();
        if(sumRedpacketNum <= 0){
            log.info("红包个数为0");
            return;
        }
        //计算单个红包面额=随机红包总额/红包个数
        BigDecimal singletonRedpacket = randomRedpacketAmount.divide(new BigDecimal(sumRedpacketNum),RedPacketConstant.DIGITS, BigDecimal.ROUND_DOWN);

        //生成红包,切割算法
        // LinkedList<BigDecimal> redpacketList = RedPacketUtils.generateRedPacket(randomRedpacketAmount.intValue(), sumRedpacketNum);

        //随机算法
        RedPacketGenerate redPacketGenerate = new RandomRedPacketGenerate();
        //生成红包
        List<BigDecimal> redpacketList = redPacketGenerate.generateRedPacket(randomRedpacketAmount,sumRedpacketNum,unitLimit);
        //包装红包
        redPacketWrap(redpacketList,bigRedpacket,bettingConfig.getRedpacketPrizeSymbol());

        log.info("*****************红包准备结束******************");

    }

    /**
     * 红包包装
     * @param redpacketList
     *             随机红包面额集合
     * @param bigRedpacketAmount
     *             大红包面额
     * @param symbol
     *             红包币种
     */
    public void redPacketWrap(List<BigDecimal> redpacketList, BigDecimal bigRedpacketAmount, String symbol){
        //大红包准备
        RedPacket bigRedPacket = new RedPacket();
        bigRedPacket.setSymbol(symbol);
        bigRedPacket.setAmount(bigRedpacketAmount);
        bigRedPacket.setIsMax(BooleanEnum.IS_TRUE);
        //随机红包准备
        for (BigDecimal bigDecimal : redpacketList){
            RedPacket redPacket = new RedPacket();
            redPacket.setSymbol(symbol);
            redPacket.setAmount(bigDecimal);
            redPacket.setIsMax(BooleanEnum.IS_FALSE);
            redPacketCache.add(redPacket);
        }
        //大红包存放位置,目前简单实现: 随机list下标位置
        Random rand = new Random();
        int offset = rand.nextInt(redPacketCache.size());
        RedPacket lastRedPacket = redPacketCache.get(offset);
        redPacketCache.set(offset,bigRedPacket);
        redPacketCache.add(lastRedPacket);
    }

    /**
     * 抢红包
     * @return
     */
    public synchronized RedPacket robRedPacket(){
        RedPacket redPacket = null;
       int size = redPacketCache.size();
       //红包已抢完
        if(size == 0){
            log.info("***************红包已抢完*************");
            return  null;
        }
        //随机数
        Random rand = new Random();
        int offset = rand.nextInt(size);
        //随机红包
        redPacket = redPacketCache.remove(offset);
        log.info("***************剩余红包个数：{}*************",redPacketCache.size());
       return redPacket;
    }

    /**
     * 清除红包
     */
    public void clearRedPacket(){
        redPacketCache.clear();
    }

    /**
     * 获取红包个数
     * @return
     */
    public int getRedPacketSize(){
        return redPacketCache.size();
    }


}
