package com.spark.bitrade.service;

import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.PushType;
import com.spark.bitrade.entity.*;
import lombok.extern.slf4j.Slf4j;
import com.spark.bitrade.util.JsonUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


/**
  * 推送订单信息
  * @author tansitao
  * @time 2018/9/18 20:41 
  */

@Service
@Slf4j
public class PushMessageService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private PriceRangeService priceRangeService;

    /**
     * 异步推送奖池信息
     * @author tansitao
     * @time 2018/9/18 18:36 
     */
    @Async
    public void pushJackpotBalance(BigDecimal balance) throws Exception{
        JSONObject json = new JSONObject();
        json.put("type", PushType.JACKPOT.getOrdinal());
        //提币总数量
        json.put("balance", balance);
        kafkaTemplate.send("msg-promote-guess-handler", PushType.JACKPOT.getOrdinal()+"", json.toJSONString());
    }

    /**
      * 异步推送投票
      * @author tansitao
      * @time 2018/9/18 18:36 
      */
    @Async
    public void pushVoteMessage(BettingRecord bettingRecord) throws Exception{
        JSONObject json = new JSONObject();
        bettingRecord.setPromotionCode(StringUtils.isEmpty(bettingRecord.getPromotionCode()) ? "" : bettingRecord.getPromotionCode().replaceAll("(\\w{5})\\w{2}(\\w{0})","$1**$2"));
        json.put("type", PushType.VOTING_INF.getOrdinal());
        json.put("username", bettingRecord.getPromotionCode());
        //提币总数量
        json.put("data", JsonUtil.ObjectToJson(bettingRecord));
        kafkaTemplate.send("msg-promote-guess-handler", PushType.VOTING_INF.getOrdinal()+"", json.toJSONString());
    }

    /**
      * 异步推送头某一期的累积投票信息
      * @author tansitao
      * @time 2018/9/18 18:36 
      */
    @Async
    public void pushAllVoteMessage(BettingRecord bettingRecord) throws Exception{
        List<BettingPriceRange> priceRangeVoList = priceRangeService.findByPeriodId(bettingRecord.getPeriodId());
        if(priceRangeVoList != null){
            for (BettingPriceRange bettingPriceRange:priceRangeVoList) {
                if(bettingRecord.getRangeId().equals(bettingPriceRange.getId())){
                    bettingPriceRange.setNumber(bettingPriceRange.getNumber().add(bettingRecord.getBetNum()) );
                }
            }
        }

        JSONObject json = new JSONObject();
        json.put("type", PushType.VOTING_SUM_INF.getOrdinal());
        //提币总数量
        json.put("data", JsonUtil.ObjectToJson(priceRangeVoList));
        kafkaTemplate.send("msg-promote-guess-handler", PushType.VOTING_SUM_INF.getOrdinal()+"", json.toJSONString());
    }

    /**
      * 异步推送领奖
      * @author tansitao
      * @time 2018/9/18 18:36 
      */
    @Async
    public void pushAwardMessage(Reward reward) throws Exception{
        JSONObject json = new JSONObject();
        reward.setPromotionCode(StringUtils.isEmpty(reward.getPromotionCode()) ? "" : reward.getPromotionCode().replaceAll("(\\w{5})\\w{2}(\\w{0})","$1**$2"));
        json.put("type", PushType.REWARD.getOrdinal());
        json.put("username", reward.getPromotionCode());
        //提币总数量
        json.put("data", JsonUtil.ObjectToJson(reward));
        kafkaTemplate.send("msg-promote-guess-handler", PushType.REWARD.getOrdinal() + "", json.toJSONString());
    }


    /**
      * 异步推送开红包
      * @author tansitao
      * @time 2018/9/18 18:36 
      */
    @Async
    public void pushOpenRedMessage( RedPacket redPacket, String username)throws Exception{
        JSONObject json = new JSONObject();
        json.put("type", PushType.RED_PACKET.getOrdinal());
        json.put("username", username.replaceAll("(\\w{5})\\w{2}(\\w{0})","$1**$2"));
        //提币总数量
        json.put("data", JsonUtil.ObjectToJson(redPacket));
        kafkaTemplate.send("msg-promote-guess-handler", PushType.RED_PACKET.getOrdinal() + "", json.toJSONString());
    }
}
