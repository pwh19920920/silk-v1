package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.LockConstant;
import com.spark.bitrade.dao.LockBttcOfflineWalletDao;
import com.spark.bitrade.entity.LockBttcOfflineWallet;
import com.spark.bitrade.entity.LockMarketRewardDetail;
import com.spark.bitrade.mq.CnytMarketRewardMessage;
import com.spark.bitrade.mq.CnytMessagePeerStatus;
import com.spark.bitrade.mq.CnytMessageType;
import com.spark.bitrade.service.LockMarketRewardDetailService;
import com.spark.bitrade.service.cnyt.SendMessageService;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/***
 * 
 * @author yangch
 * @time 2018.05.26 17:08
 */

@RestController
public class TestController {

    @Autowired
    private LockMarketRewardDetailService lockMarketRewardDetailService;

    @Autowired
    private LockBttcOfflineWalletDao lockBttcOfflineWalletDao;

    @Autowired
    private KafkaTemplate kafkaTemplate;
    @RequestMapping("test")
    public MessageResult test(Long id){
        LockMarketRewardDetail lockMarketRewardDetail = lockMarketRewardDetailService.findOneById(id);
        CnytMarketRewardMessage message = new CnytMarketRewardMessage();
        message.setExistMaxRewardRate(lockMarketRewardDetail.getSubMaxRewardRate());
        message.setInviteeMemberId(lockMarketRewardDetail.getRefLockMemberId());
        message.setInviterMemberId(lockMarketRewardDetail.getMemberId());
        message.setPeerStatus(CnytMessagePeerStatus.HINT_OVER_PEER);
        message.setRefInviteeMarketRewardDetailId(null);
        message.setRefInviterMarketRewardDetailId(lockMarketRewardDetail.getId());
        message.setRefLockDetatilId(lockMarketRewardDetail.getLockDetailId());
        message.setType(CnytMessageType.PUSH_REWARD);
                kafkaTemplate.send(LockConstant.KAFKA_TX_CNYT_MESSAGE_HANDLER, String.valueOf(message.getType()), JSON.toJSONString(message));
        return MessageResult.success();
    }

    @PostMapping("testTransaction")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult testTransaction() throws Exception{

        LockBttcOfflineWallet lockBttcOfflineWallet = new LockBttcOfflineWallet();
        lockBttcOfflineWallet.setBalance(BigDecimal.ZERO);

        lockBttcOfflineWalletDao.save(lockBttcOfflineWallet);

        int i = 1;

        if(i == 1) {
            throw new Exception();
        }

       return MessageResult.success();
    }

}
