package com.spark.bitrade.service;

import com.spark.bitrade.constant.*;
import com.spark.bitrade.dto.OtcMsgDTO;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.util.SpringContextUtil;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * C2C消息组件
 *
 * @author zhongxj
 * @time 2019.09.11
 */
@Service
@Slf4j
public class OtcMsgService {
    @Autowired
    private SilkPlatInformationService silkPlatInformationService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private UserConfigurationCenterService userConfigurationCenterService;
    @Autowired
    private AppealService appealService;

    /**
     * getService
     *
     * @return
     */
    public OtcMsgService getService() {
        return SpringContextUtil.getBean(OtcMsgService.class);
    }

    /**
     * 获取指定事件，平台消息内容
     *
     * @param receivingObject 消息接收方（0-被交易方；1-交易方）
     * @param infoType        事件类型{0:充值到账提醒,1:新订单创建提醒,2:交易即将过期,3:已付款提醒,4:已释放提醒,5:申诉处理结果(成功)提醒,6:申诉处理结果(失败用户权限被冻结)提醒,7:申诉处理结果(失败被冻结前警告)提醒,8:收到了C2C用户聊天消息,9:商家认证审核通过}
     * @return 当前会员，需要发送的消息通道
     */
    public SilkPlatInformation getSilkPlatInformationByEvent(Integer receivingObject, Integer infoType) {
        return silkPlatInformationService.getSilkPlatInformationByEvent(receivingObject, infoType);
    }

    /**
     * 获取当前会员，需要发送的消息通道
     *
     * @param memberId        会员ID
     * @param triggeringEvent 事件
     * @return 当前会员，需要发送的消息通道
     */
    public UserConfigurationCenter getUserConfigurationCenterByMemberAndEvent(Long memberId, Integer triggeringEvent) {
        return userConfigurationCenterService.getUserConfigurationCenterByMemberAndEvent(memberId, triggeringEvent);
    }

    /**
     * 判断当前会员，是否存在邮箱
     *
     * @param memberId 会员ID
     * @return 如果存在邮箱，则返回会员数据
     */
    public Member getMember(Long memberId) {
        return memberService.findOne(memberId);
    }

    /**
     * C2C消息组件，推送消息内容
     *
     * @param order    订单数据
     * @param memberId 接收会员ID
     */
    public OtcMsgDTO getListItemContent(Order order, Long memberId) {
        OtcMsgDTO otcMsgDTO = new OtcMsgDTO();
        OrderStatus orderStatus = order.getStatus();
        Long buyMemberId = order.getMemberId();
        Long sellMemberId = order.getCustomerId();
        Member businessMember = memberService.findOne(buyMemberId);
        Member customerMember = memberService.findOne(sellMemberId);
        Map dataModel = new HashMap();
        Integer receivingObject = 0;
        dataModel.put("order", order);
        // 判断广告类型
        if (order.getAdvertiseType() == AdvertiseType.SELL) {
            if (sellMemberId.equals(memberId)) {
                // 出售广告，买方
                receivingObject = 1;
                dataModel.put("user", businessMember);
            } else if (buyMemberId.equals(memberId)) {
                // 出售广告，卖方
                receivingObject = 2;
                dataModel.put("user", customerMember);
            }
        } else {
            if (buyMemberId.equals(memberId)) {
                // 购买广告，买方
                receivingObject = 1;
                dataModel.put("user", customerMember);
            } else if (sellMemberId.equals(memberId)) {
                // 购买广告，卖方
                receivingObject = 2;
                dataModel.put("user", businessMember);
            }
        }
        Long dateTime = 0L;
        String content = "";
        String enContent = "";
        InfoType infoType = InfoType.OTC_ADD_ORDER;
        // 已取消
        if (OrderStatus.CANCELLED.equals(orderStatus)) {
            dateTime = order.getCancelTime().getTime();
            if (order.getIsManualCancel() == BooleanEnum.IS_TRUE) {
                // 主动取消C2C订单
                infoType = InfoType.OTC_ORDER_MANUAL_CANCLE;
            } else {
                infoType = InfoType.OTC_ORDER_AUTO_CANCLE;
            }
        }
        // 创建订单
        if (OrderStatus.NONPAYMENT.equals(orderStatus)) {
            dateTime = order.getCreateTime().getTime();
        }
        // 标记已付款
        if (OrderStatus.PAID.equals(orderStatus)) {
            dateTime = order.getPayTime().getTime();
            infoType = InfoType.OTC_PAY_CASH;
        }
        // 已完成
        if (OrderStatus.COMPLETED.equals(orderStatus)) {
            dateTime = order.getReleaseTime().getTime();
            infoType = InfoType.OTC_PAY_COIN;
        }
        // 申诉中
        if (OrderStatus.APPEAL.equals(orderStatus)) {
            Appeal appeal = appealService.findByOrderId(order.getId().toString());
            if (appeal != null) {
                dateTime = appeal.getCreateTime().getTime();
            }
            infoType = InfoType.OTC_ORDER_APPEAL;
        }
        // 申诉结果得出，关闭订单
        if (OrderStatus.CLOSE.equals(orderStatus)) {
            dateTime = order.getCloseTime().getTime();
            infoType = InfoType.OTC_APPEAL_SUCCESS;
        }
        content = getService().getCommoinListItemContent(receivingObject, memberId, infoType, SysConstant.ZH_LANGUAGE, dataModel);
        enContent = getService().getCommoinListItemContent(receivingObject, memberId, infoType, SysConstant.EN_LANGUAGE, dataModel);
        otcMsgDTO.setSendTime(dateTime);
        otcMsgDTO.setOrderSn(order.getOrderSn());
        otcMsgDTO.setSendContent(content);
        otcMsgDTO.setSendEnContent(enContent);
        otcMsgDTO.setToMemberId(memberId);
        return otcMsgDTO;
    }

    /**
     * C2C消息组件，公共模板内容填充
     *
     * @param receivingObject 消息接收方(1-买方;2-卖方)
     * @param memberId        接收会员ID
     * @param infoType        触发事件
     * @param languageCode    语言zh_CN:中文;en_US:英文
     * @param dataModel       模板内容填充
     */
    public String getCommoinListItemContent(Integer receivingObject, Long memberId, InfoType infoType, String languageCode, Map dataModel) {
        String content = "";
        SilkPlatInformation silkPlatInformation = getService().getSilkPlatInformationByEvent(receivingObject, infoType.getOrdinal());
        if (!StringUtils.isEmpty(silkPlatInformation)) {
            Integer useChat = silkPlatInformation.getUseChat();
            if (useChat == 1) {
                UserConfigurationCenter userConfigurationCenter = getService().getUserConfigurationCenterByMemberAndEvent(memberId, infoType.getOrdinal());
                if (!StringUtils.isEmpty(userConfigurationCenter)) {
                    Member member = getService().getMember(memberId);
                    if (!StringUtils.isEmpty(member)) {
                        if (SysConstant.EN_LANGUAGE.equals(languageCode)) {
                            content = silkPlatInformation.getChatContentEn();
                            content = getService().parseNotificationConten("chatEnContent",
                                    content, dataModel);
                        } else {
                            content = silkPlatInformation.getChatContentCn();
                            content = getService().parseNotificationConten("chatCnContent",
                                    content, dataModel);
                        }
                    }
                }
            } else {
                log.error("C2C消息组件，推送渠道已关闭");
            }
        }
        return content;
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
