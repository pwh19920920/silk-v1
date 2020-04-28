package com.spark.bitrade.handler;

import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.chat.RealTimeChatMessage;
import com.spark.bitrade.notice.INoticeService;
import com.spark.bitrade.notice.IWaitAckTaskService;
import com.spark.bitrade.notice.task.ChatWaitAckTask;
import com.spark.bitrade.notice.task.OtcEnventWaitAckTask;
import com.spark.bitrade.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@Slf4j
public class ChatMessageHandler implements MessageHandler {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IWaitAckTaskService waitAckTaskService;
    @Autowired
    @Qualifier("chatNoticeServiceImpl")
    private INoticeService chatNoticeServiceImpl;
    @Autowired
    @Qualifier("otcEventNoticeServiceImpl")
    private INoticeService otcEventNoticeServiceImpl;
    @Autowired
    @Qualifier("sysNoticeServiceImpl")
    private INoticeService sysNoticeServiceImpl;

    @Override
    public void handleMessage(ChatMessageRecord message) {
        if (null != message) {
            mongoTemplate.insert(message, "chat_message");  /* "chat_message"+message.getOrderId() */

            //已完成实时推送，处理未读消息
            if (message.getMessageType() == MessageTypeEnum.OTC_EVENT) {
                //OTC事件流转消息，添加到延迟队列中
                waitAckTaskService.addTask(new OtcEnventWaitAckTask(message));
            } else if (message.getMessageType() == MessageTypeEnum.NORMAL_CHAT) {
                //聊天消息，添加到延迟队列中
                waitAckTaskService.addTask(new ChatWaitAckTask(message));
            }
        } else {
            log.info("ChatMessageRecord 为null");
        }
    }

    /**
     * 获取历史聊天记录
     *
     * @param message
     * @return
     */
    @Override
    public HistoryMessagePage getHistoryMessage(HistoryChatMessage message) {
        Criteria criteria = new Criteria();
        if (!StringUtils.isEmpty(message.getOrderId()))
            criteria = Criteria.where("orderId").is(message.getOrderId());
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, message.getSortFiled()));
        Query query = new Query(criteria).with(sort);
        long total = mongoTemplate.count(query, ChatMessageRecord.class, "chat_message");
        query.limit(message.getLimit()).skip((message.getPage() - 1) * message.getLimit());
        List<ChatMessageRecord> list = mongoTemplate.find(query, ChatMessageRecord.class, "chat_message");
        for (ChatMessageRecord record : list) {
            record.setSendTimeStr(DateUtils.getDateStr(record.getSendTime()));
        }
        long consult = total / message.getLimit();
        long residue = total % message.getLimit();
        long totalPage = residue == 0 ? consult : (consult + 1);
        return HistoryMessagePage.getInstance(message.getPage(), totalPage, list.size(), total, list);
    }

    @Override
    public void handleSysNoticeMessage(RealTimeChatMessage message) {
        //已完成实时推送，处理系统消息为未读，包含：系统消息、聊天和OTC事件消息
        //del by yangch 时间： 2018.12.25 原因：聊天和OTC事件消息的消息需要先保存，再获取通知数量
        if (message.getMessageType() == MessageTypeEnum.NORMAL_CHAT) {
            //聊天消息，保存到缓存
            ///chatNoticeServiceImpl.save(message);
        } else if (message.getMessageType() == MessageTypeEnum.OTC_EVENT) {
            //OTC事件消息，保存到缓存
            ///otcEventNoticeServiceImpl.save(message);
        } else if (message.getMessageType() == MessageTypeEnum.SYSTEM_MES) {
            //系统消息，缓存及入库保存
            sysNoticeServiceImpl.save(message);
        } else {
            log.warn("未知的消息类型，消息：{}", message);
        }
    }

    //发送聊天消息已读反馈
    @Override
    public void handleReadAckMessage(RealTimeChatMessage message) {
        waitAckTaskService.executeReceiveAck(message);
    }

    /**
     * 获取历史聊天记录
     *
     * @param message
     * @param uidTo 会员ID
     * @return
     */
    @Override
    public HistoryMessagePage getHistoryMessageNews(HistoryChatMessage message, String uidTo) {
        Criteria criteria = new Criteria();
        Criteria c1 = Criteria.where("orderId").is(message.getOrderId() == null ? 0 : message.getOrderId())
                .and("messageType").is(MessageTypeEnum.OTC_EVENT)
                .and("uidTo").is(uidTo == null ? 0 : uidTo);
        Criteria c2 = Criteria.where("orderId").is(message.getOrderId() == null ? 0 : message.getOrderId())
                .and("messageType").ne(MessageTypeEnum.OTC_EVENT);
        criteria.orOperator(c1, c2);
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, message.getSortFiled()));
        Query query = new Query(criteria).with(sort);
        long total = mongoTemplate.count(query, ChatMessageRecord.class, "chat_message");
        query.limit(message.getLimit()).skip((message.getPage() - 1) * message.getLimit());
        List<ChatMessageRecord> list = mongoTemplate.find(query, ChatMessageRecord.class, "chat_message");
        for (ChatMessageRecord record : list) {
            record.setSendTimeStr(DateUtils.getDateStr(record.getSendTime()));
        }
        long consult = total / message.getLimit();
        long residue = total % message.getLimit();
        long totalPage = residue == 0 ? consult : (consult + 1);
        return HistoryMessagePage.getInstance(message.getPage(), totalPage, list.size(), total, list);

    }

}