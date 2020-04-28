package com.spark.bitrade.notice.impl;

import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.chat.RealTimeChatMessage;
import com.spark.bitrade.notice.INoticeService;
import com.spark.bitrade.service.optfor.RedisKeyService;
import com.spark.bitrade.service.optfor.RedisZSetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/***
  * c2c事件通知提示服务
  * @author yangch
  * @time 2018.12.24 17:52
  */
@Service("otcEventNoticeServiceImpl")
@Slf4j
public class OtcEventNoticeServiceImpl implements INoticeService {
    @Autowired
    private RedisKeyService redisKeyService;
    @Autowired
    private RedisZSetService redisZSetService;
    private String prefix = SysConstant.NOTICE_OTC_EVENT_PREFIX;
    private long expireTime = 3L;

    @Override
    public void save(RealTimeChatMessage message) {
        log.info("OtcEventNoticeServiceImpl===========message:{}", message);
        // toMemberId
        String toMemberId = prefix + message.getUidTo();
        boolean sendFromMember = message.getSendFromMember();
        if (sendFromMember) {
            redisZSetService.zAdd(toMemberId, message.getOrderId(), System.currentTimeMillis());
            redisKeyService.expire(toMemberId, expireTime, TimeUnit.DAYS);
        }
    }

    @Override
    public long count(String uid) {
        String key = prefix + uid;
        return redisZSetService.zSize(key);
    }

    @Override
    public void ack(String uid, String nid) {
        String key = prefix + uid;
        redisZSetService.zRemove(key, nid);
    }

    @Override
    public List<String> list(String uid) {
        String key = prefix + uid;
        Set set = redisZSetService.zReverseRange(key, 0, -1);
        if (set != null) {
            List<String> lst = new ArrayList<>(set.size());
            set.forEach(s -> lst.add(String.valueOf(s)));
            return lst;
        }

        return null;
    }
}
