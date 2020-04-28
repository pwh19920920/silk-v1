package com.spark.bitrade.service;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.chat.RealTimeChatMessage;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.service.impl.SysNotificationTemplateService;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息处理service（包含发送短信、邮件，推送soket消息等）
 *
 * @author tansitao
 * @time 2018/12/19 14:17 
 */
@Service
@Slf4j
public class MessageDealService {
    @Autowired
    private MemberService memberService;
    @Autowired
    private IChatService iChatService;
    @Autowired
    private SysNotificationTemplateService sysNotificationTemplateService;

    /**
     * 获取系统消息内容模板
     *
     * @param carrier
     * @param monitorTriggerEvent
     * @param notificationType
     * @return
     */
    public SysNotificationTemplate getSystemMessage(CollectCarrier carrier, MonitorTriggerEvent monitorTriggerEvent, NotificationType notificationType) {
        String language = SysConstant.ZH_LANGUAGE;
        SysNotificationTemplate sysNotificationTemplate = sysNotificationTemplateService.findByType(
                monitorTriggerEvent.getOrdinal(), notificationType.getOrdinal(), language);
        language = SysConstant.EN_LANGUAGE;
        SysNotificationTemplate sysNotificationTemplateNews = sysNotificationTemplateService.findByType(
                monitorTriggerEvent.getOrdinal(), notificationType.getOrdinal(), language);
        StringBuffer stringBuffer = new StringBuffer();
        if (sysNotificationTemplate != null) {
            stringBuffer.append(sysNotificationTemplate.getTemplate());
            if (sysNotificationTemplateNews != null) {
                stringBuffer.append("~~~~").append(sysNotificationTemplateNews.getTemplate());
            }
        } else {
            stringBuffer.append(sysNotificationTemplateNews.getTemplate());
        }
        sysNotificationTemplate.setTemplate(stringBuffer.toString());
        if (sysNotificationTemplate == null) {
            log.info("============订单id{}，事件{}，渠道{}，内容模板不存在,============",
                    carrier.getRefId(), monitorTriggerEvent.getCnName(),
                    notificationType.getCnName());
            return null;
        } else {
            log.info("============订单id{}，事件{}，渠道{}，内容{},============",
                    carrier.getRefId(), monitorTriggerEvent.getCnName(),
                    notificationType.getCnName(), sysNotificationTemplate);
            return sysNotificationTemplate;
        }
    }

    /**
     * 推送（创建C2C订单、C2C订单标记已付款）消息
     *
     * @param order                   订单信息
     * @param sysNotificationTemplate 模板信息
     */
    @Async
    public void pushCreateOrPayOrderMessage(Order order, SysNotificationTemplate sysNotificationTemplate) {
        Member businessMember = memberService.findOne(order.getMemberId());
        Member customerMember = memberService.findOne(order.getCustomerId());
        Map dataModel = new HashMap();
        dataModel.put("order", order);
        boolean customerIsSend=true;
        boolean businessIsSend=true;
        // 判断广告类型
        if (order.getAdvertiseType() == AdvertiseType.SELL) {
            // 卖币广告则等待用户付款
            dataModel.put("user", customerMember);
            dataModel.put("sellName", businessMember.getUsername());
            //卖币广告 买家已付款 不推送买家
            customerIsSend=false;
        } else {
            // 买币广告则等待商家付款
            dataModel.put("user", businessMember);
            dataModel.put("sellName", customerMember.getUsername());
            //买币广告 商家付款 不推送商家
            businessIsSend=false;
        }
        MonitorTriggerEvent type = sysNotificationTemplate.getType();
        String msg = parseNotificationConten(type.name() + sysNotificationTemplate.getId(),
                sysNotificationTemplate.getTemplate(), dataModel);
        RealTimeChatMessage realTimeChatMessage = new RealTimeChatMessage();
        realTimeChatMessage.setOrderId(order.getOrderSn());
        realTimeChatMessage = getChatMessage(order, businessMember, customerMember);
        try {
            // 推送1
            realTimeChatMessage = this.pushContent(realTimeChatMessage, msg);
            log.info("==========进行聊天窗口系统消息推送，{}事件，订单ID{},推送内容1{}==============",
                    sysNotificationTemplate.getType().getCnName(),
                    order.getOrderSn(), JSON.toJSONString(realTimeChatMessage));
            if(type==MonitorTriggerEvent.OTC_PAY_CASH){
                realTimeChatMessage.setSendFromMember(businessIsSend);
            }
            iChatService.chatPush(JSON.toJSONString(realTimeChatMessage));
            // 推送2
            realTimeChatMessage = getChatMessage(order, customerMember, businessMember);
            realTimeChatMessage = this.pushContent(realTimeChatMessage, msg);
            if(type==MonitorTriggerEvent.OTC_PAY_CASH){
                realTimeChatMessage.setSendFromMember(customerIsSend);
            }
            if(type==MonitorTriggerEvent.OTC_ADD_ORDER){
                realTimeChatMessage.setSendFromMember(false);
            }
            log.info("==========进行聊天窗口系统消息推送，{}事件，订单ID{},推送内容2{}==============",
                    sysNotificationTemplate.getType().getCnName(),
                    order.getOrderSn(), JSON.toJSONString(realTimeChatMessage));
            iChatService.chatPush(JSON.toJSONString(realTimeChatMessage));
        } catch (Exception e) {
            log.error("============{}事件，推送系统消息失败=============={}", sysNotificationTemplate.getType().getCnName(), e);
        }
    }

    /**
     * 推送C2C交易释放币消息
     *
     * @param order                   订单信息
     * @param sysNotificationTemplate 模板信息
     */
    @Async
    public void pushReleaseOrderMessage(Order order, SysNotificationTemplate sysNotificationTemplate) {
        Member businessMember = memberService.findOne(order.getMemberId());
        Member customerMember = memberService.findOne(order.getCustomerId());
        Map dataModel = new HashMap();
        dataModel.put("order", order);
        //处理订单放行的系统消息
        RealTimeChatMessage realTimeChatMessage = new RealTimeChatMessage();
        realTimeChatMessage.setOrderId(order.getOrderSn());
        boolean customerIsSend=true;
        boolean businessIsSend=true;
        //判断广告类型
        if (order.getAdvertiseType().equals(AdvertiseType.SELL)) {
            //如果是买币广告，将消息推送给商家
            dataModel.put("user", businessMember);
            businessIsSend=false;
        } else {
            //如果是卖币广告，将消息推送给客户
            dataModel.put("user", customerMember);
            customerIsSend=false;
        }
        String msg = parseNotificationConten(sysNotificationTemplate.getType().name() + sysNotificationTemplate.getId(),
                sysNotificationTemplate.getTemplate(), dataModel);
        try {
            // 推送1
            realTimeChatMessage = getChatMessage(order, customerMember, businessMember);
            realTimeChatMessage = this.pushContent(realTimeChatMessage, msg);
            log.info("==========进行聊天窗口系统消息推送，{}，订单ID{},推送内容1{}==============",
                    sysNotificationTemplate.getType().getCnName(),
                    order.getOrderSn(), JSON.toJSONString(realTimeChatMessage));
            realTimeChatMessage.setSendFromMember(customerIsSend);
            iChatService.chatPush(JSON.toJSONString(realTimeChatMessage));
            // 推送2
            realTimeChatMessage = getChatMessage(order, businessMember, customerMember);
            realTimeChatMessage = this.pushContent(realTimeChatMessage, msg);
            log.info("==========进行聊天窗口系统消息推送，{}，订单ID{},推送内容2{}==============",
                    sysNotificationTemplate.getType().getCnName(),
                    order.getOrderSn(), JSON.toJSONString(realTimeChatMessage));
            realTimeChatMessage.setSendFromMember(businessIsSend);
            iChatService.chatPush(JSON.toJSONString(realTimeChatMessage));
        } catch (Exception e) {
            log.error("============{}事件，推送系统消息失败=============={}", sysNotificationTemplate.getType().getCnName(), e);
        }

    }

    /**
     * 推送撤销订单消息
     *
     * @param order                   订单信息
     * @param sysNotificationTemplate 模板信息
     */
    @Async
    public void pushCancelOrderMessage(Order order, SysNotificationTemplate sysNotificationTemplate) {
        // 处理订单撤销的系统消息
        RealTimeChatMessage realTimeChatMessage = new RealTimeChatMessage();
        realTimeChatMessage.setOrderId(order.getOrderSn());
        Member fromMember = null;
        Member toMember = null;
        Map dataModel = new HashMap();
        dataModel.put("order", order);
        // 判断是否为手动取消
        if (order.getIsManualCancel() == BooleanEnum.IS_TRUE) {
            // 手动取消，获取取消人信息
            fromMember = memberService.findOne(order.getCancelMemberId());
            // 通过取消人id判断，需要推送个谁
            if (order.getCancelMemberId().equals(order.getCustomerId())) {
                toMember = memberService.findOne(order.getMemberId());
            } else {
                toMember = memberService.findOne(order.getCustomerId());
            }
            realTimeChatMessage = getChatMessage(order, toMember, fromMember);
            dataModel.put("user", fromMember);
            String msg = parseNotificationConten(sysNotificationTemplate.getType().name() + sysNotificationTemplate.getId(),
                    sysNotificationTemplate.getTemplate(), dataModel);
            try {
                // 推送1
                realTimeChatMessage = this.pushContent(realTimeChatMessage, msg);
                log.info("==========进行系统消息推送，{}，订单ID{},推送内容1{}==============",
                        sysNotificationTemplate.getType().getCnName(),
                        order.getOrderSn(), JSON.toJSONString(realTimeChatMessage));
                iChatService.chatPush(JSON.toJSONString(realTimeChatMessage));
                // 推送2
                realTimeChatMessage = getChatMessage(order, fromMember, toMember);
                realTimeChatMessage.setSendFromMember(false);
                realTimeChatMessage = this.pushContent(realTimeChatMessage, msg);
                log.info("==========进行系统消息推送，{}，订单ID{},推送内容2{}==============",
                        sysNotificationTemplate.getType().getCnName(),
                        order.getOrderSn(), JSON.toJSONString(realTimeChatMessage));
                iChatService.chatPush(JSON.toJSONString(realTimeChatMessage));
            } catch (Exception e) {
                log.error("============{}事件，推送系统消息失败=============={}", sysNotificationTemplate.getType().getCnName(), e);
            }
        } else {
            Member businessMember = memberService.findOne(order.getMemberId());
            Member customerMember = memberService.findOne(order.getCustomerId());
            //判断该谁付款
            if (order.getAdvertiseType() == AdvertiseType.SELL) {
                dataModel.put("user", customerMember);
            } else {
                dataModel.put("user", businessMember);
            }
            String msg = parseNotificationConten(sysNotificationTemplate.getType().name() + sysNotificationTemplate.getId(),
                    sysNotificationTemplate.getTemplate(), dataModel);
            try {
                // 推送1
                realTimeChatMessage = getChatMessage(order, businessMember, customerMember);
                realTimeChatMessage = this.pushContent(realTimeChatMessage, msg);
                log.info("==========进行系统消息推送，{}，订单ID{},推送内容1{}==============",
                        sysNotificationTemplate.getType().getCnName(),
                        order.getOrderSn(), JSON.toJSONString(realTimeChatMessage));
                iChatService.chatPush(JSON.toJSONString(realTimeChatMessage));
                // 推送2
                realTimeChatMessage = getChatMessage(order, customerMember, businessMember);
                realTimeChatMessage = this.pushContent(realTimeChatMessage, msg);
                log.info("==========进行系统消息推送，{}，订单ID{},推送内容2{}==============",
                        sysNotificationTemplate.getType().getCnName(),
                        order.getOrderSn(), JSON.toJSONString(realTimeChatMessage));
                iChatService.chatPush(JSON.toJSONString(realTimeChatMessage));
            } catch (Exception e) {
                log.error("============{}事件，推送系统消息失败=============={}", sysNotificationTemplate.getType().getCnName(), e);
            }
        }
    }

    /**
     * 推送订单申诉和取消消息
     *
     * @param appeal                  申诉信息
     * @param sysNotificationTemplate 模板信息
     * @param order                   订单信息
     */
    @Async
    public void pushOrderAppealCreateOrCancelMessage(Appeal appeal, SysNotificationTemplate sysNotificationTemplate, Order order) {
        Member toMember = memberService.findOne(appeal.getAssociateId());
        Member fromMember = memberService.findOne(appeal.getInitiatorId());
        //处理订单申诉的系统消息
        RealTimeChatMessage realTimeChatMessage = getChatMessage(order, toMember, fromMember);
        Map dataModel = new HashMap();
        dataModel.put("order", order);
        dataModel.put("user", fromMember);
        String msg = parseNotificationConten(sysNotificationTemplate.getType().name() + sysNotificationTemplate.getId(),
                sysNotificationTemplate.getTemplate(), dataModel);
        try {
            // 推送1
            realTimeChatMessage = this.pushContent(realTimeChatMessage, msg);
            log.info("==========进行聊天窗口系统消息推送，{}，订单ID{},申诉id{},推送内容1{}==============",
                    sysNotificationTemplate.getType().getCnName(), order.getOrderSn(),
                    appeal.getId(), JSON.toJSONString(realTimeChatMessage));
            iChatService.chatPush(JSON.toJSONString(realTimeChatMessage));
            // 推送2
            realTimeChatMessage = getChatMessage(order, fromMember, toMember);
            realTimeChatMessage = this.pushContent(realTimeChatMessage, msg);
            realTimeChatMessage.setSendFromMember(false);
            log.info("==========进行聊天窗口系统消息推送，{}，订单ID{},申诉id{},推送内容2{}==============",
                    sysNotificationTemplate.getType().getCnName(), order.getOrderSn(),
                    appeal.getId(), JSON.toJSONString(realTimeChatMessage));
            iChatService.chatPush(JSON.toJSONString(realTimeChatMessage));
        } catch (Exception e) {
            log.error("============{}事件，推送系统消息失败=============={}", sysNotificationTemplate.getType().getCnName(), e);
        }
    }

    /**
     *  推送处理订单申诉完成的消息
     *  @author tansitao
     *  @time 2018/12/19 15:12 
     *  
     */
    @Async
    public void pushOrderAppealSuccessMessage(Appeal appeal, SysNotificationTemplate sysNotificationTemplate, Order order) {
        Member associateMember = memberService.findOne(appeal.getAssociateId());
        Member initiatorMember = memberService.findOne(appeal.getInitiatorId());
        Map dataModel = new HashMap();
        dataModel.put("order", order);
        RealTimeChatMessage realTimeChatMessage = new RealTimeChatMessage();
        if (appeal.getIsSuccess() == BooleanEnum.IS_TRUE) {
            //订单申诉处理完成，申诉者成功的系统消息
            dataModel.put("user", initiatorMember);
        } else {
            //订单申诉处理完成，申诉者失败的系统消息
            dataModel.put("user", associateMember);
        }
        realTimeChatMessage = getChatMessage(order, associateMember, initiatorMember);
        String msg = parseNotificationConten(sysNotificationTemplate.getType().name() + sysNotificationTemplate.getId(),
                sysNotificationTemplate.getTemplate(), dataModel);

        try {
            // 推送1
            realTimeChatMessage = this.pushContent(realTimeChatMessage, msg);
            log.info("==========进行系统消息推送，{}，订单ID{},申诉id{},推送内容1{}==============",
                    sysNotificationTemplate.getType().getCnName(), appeal.getOrder().getOrderSn(),
                    appeal.getId(), JSON.toJSONString(realTimeChatMessage));
            iChatService.chatPush(JSON.toJSONString(realTimeChatMessage));
            // 推送2
            realTimeChatMessage = getChatMessage(order, initiatorMember, associateMember);
            realTimeChatMessage = this.pushContent(realTimeChatMessage, msg);
            log.info("==========进行系统消息推送，{}，订单ID{},申诉id{},推送内容2{}==============",
                    sysNotificationTemplate.getType().getCnName(), appeal.getOrder().getOrderSn(),
                    appeal.getId(), JSON.toJSONString(realTimeChatMessage));
            iChatService.chatPush(JSON.toJSONString(realTimeChatMessage));
        } catch (Exception e) {
            log.error("============推送系统消息失败==============", e);
        }
    }

    /**
     * 中英文内容推送
     *
     * @param msg
     * @return
     */
    public RealTimeChatMessage pushContent(RealTimeChatMessage realTimeChatMessage, String msg) {
        String[] message = msg.split("~~~~");
        for (int i = 0; i < message.length; i++) {
            if (i == 0) {
                realTimeChatMessage.setContent(message[0]);
            }
            if (i == 1) {
                realTimeChatMessage.setEnContent(message[1]);
            }
        }
        return realTimeChatMessage;
    }

    /**
     *  获取推送消息双方基本信息
     *  @author tansitao
     *  @time 2018/12/24 12:00 
     *  
     */
    public RealTimeChatMessage getChatMessage(Order order, Member toMember, Member fromMember) {
        RealTimeChatMessage realTimeChatMessage = new RealTimeChatMessage();
        realTimeChatMessage.setOrderId(order.getOrderSn());
        realTimeChatMessage.setUidTo(String.valueOf(toMember.getId()));
        realTimeChatMessage.setUidFrom(String.valueOf(fromMember.getId()));
        realTimeChatMessage.setNameTo(toMember.getUsername());
        realTimeChatMessage.setNameFrom(fromMember.getUsername());
        realTimeChatMessage.setMessageType(MessageTypeEnum.OTC_EVENT);
        return realTimeChatMessage;
    }

    /**
     * 使用freemarker解析通知内容
     *
     * @param templateName    模版名称
     * @param templateContent 通知模版
     * @param dataModel       数据模型
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public String parseNotificationConten(String templateName,
                                          String templateContent, Object dataModel) {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
            cfg.setTemplateUpdateDelayMilliseconds(0);
            StringTemplateLoader stringLoader = new StringTemplateLoader();
            stringLoader.putTemplate(templateName, templateContent);
            cfg.setTemplateLoader(stringLoader);
            Template template = cfg.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (Exception ex) {
            log.error("解析通知内容出错", ex);
        }
        return templateContent;
    }

}