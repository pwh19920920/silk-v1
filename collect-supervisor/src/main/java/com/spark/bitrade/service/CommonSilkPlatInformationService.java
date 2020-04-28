package com.spark.bitrade.service;

import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.InfoType;
import com.spark.bitrade.constant.MonitorTriggerEvent;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.constant.messager.NoticeType;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.messager.MemberMailEntity;
import com.spark.bitrade.entity.messager.NoticeEntity;
import com.spark.bitrade.entity.messager.SysNoticeEntity;
import com.spark.bitrade.entity.transform.EmailEnity;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import com.spark.bitrade.util.ValidateUtil;
import com.spark.bitrade.vendor.provider.SMSProviderProxy;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * kafka消费公共数据服务
 *
 * @author zhongxj
 * @time 2019.09.11
 */
@Service
@Slf4j
public class CommonSilkPlatInformationService {
    @Autowired
    private MemberService memberService;
    @Autowired
    private MessageDealService messageDealService;
    @Autowired
    private IEmailService iEmailService;
    @Autowired
    private MonitorRuleService monitorRuleService;
    @Autowired
    private MemberDepositService memberDepositService;
    @Autowired
    private OtcMsgService otcMsgService;
    @Autowired
    private IJpushService iJpushService;
    @Autowired
    private PlatInstationService platInstationService;
    @Autowired
    private SMSProviderProxy smsProvider;
    @Autowired
    private KafkaDelayService kafkaDelayService;

    /**
     * getService
     *
     * @return
     */
    public CommonSilkPlatInformationService getService() {
        return SpringContextUtil.getBean(CommonSilkPlatInformationService.class);
    }

    /**
     * C2C创建订单model
     *
     * @param order         订单编号
     * @param memberId      会员ID
     * @param advertiseType 交易类型
     * @return
     */
    public Map<String, Object> getCreateOrderModel(Order order, Long memberId, String advertiseType) {
        Map dataModel = new HashMap(3);
        Member member = getService().getMember(memberId);
        dataModel.put("order", order);
        dataModel.put("user", member);
        dataModel.put("advertiseType", advertiseType);
        return dataModel;
    }

    /**
     * C2C订单申诉处理结果model
     *
     * @param order
     * @return
     */
    public Map<String, Object> getAppealCompleteOrderModel(Order order) {
        Map dataModel = new HashMap(1);
        dataModel.put("order", order);
        return dataModel;
    }

    /**
     * C2C交易即将过期model
     *
     * @param order 订单编号
     * @return
     */
    public Map<String, Object> getExpireRemindOrderModel(Order order, Long memberId) {
        Map dataModel = new HashMap(3);
        Member member = getService().getMember(memberId);
        dataModel.put("order", order);
        dataModel.put("user", member);
        dataModel.put("extension", monitorRuleService.expireOtcOrder(memberId, MonitorTriggerEvent.OTC_CANCEL_ORDER.getOrdinal(), member.getMemberLevel().getOrdinal()));
        return dataModel;
    }

    /**
     * 充值到账model
     *
     * @param txid 交易哈希
     * @return
     */
    public Map<String, Object> getCoinInModel(String txid) {
        Map dataModel = new HashMap(4);
        MemberDeposit memberDeposit = memberDepositService.getMemberDepositByTxid(txid);
        String account = "";
        Long memberId = 0L;
        if (!StringUtils.isEmpty(memberDeposit)) {
            memberId = memberDeposit.getMemberId();
            Member member = getService().getMember(memberId);
            String mobilePhone = member.getMobilePhone();
            String email = member.getEmail();
            if (!StringUtils.isEmpty(mobilePhone) && !StringUtils.isEmpty(email)) {
                account = mobilePhone + "/" + email;
            } else if (!StringUtils.isEmpty(mobilePhone)) {
                account = mobilePhone;
            } else if (!StringUtils.isEmpty(email)) {
                account = email;
            }
        }
        dataModel.put("coin", memberDeposit);
        dataModel.put("account", account);
        dataModel.put("memberId", memberId);
        dataModel.put("times", DateUtil.dateToString(memberDeposit.getCreateTime()));
        return dataModel;
    }

    /**
     * 平台消息内容，通用邮件发送
     *
     * @param receivingObject 消息接收方(1-买方;2-卖方)
     * @param memberId        接收会员ID
     * @param infoType        触发事件
     * @param languageCode    语言zh_CN:中文;en_US:英文
     * @param dataModel       模板内容填充
     */
    @Async
    public void sendCommonEmail(Integer receivingObject, Long memberId, InfoType infoType, String languageCode, Map dataModel) {
        EmailEnity emailEnity = new EmailEnity();
        SilkPlatInformation silkPlatInformation = otcMsgService.getSilkPlatInformationByEvent(receivingObject, infoType.getOrdinal());
        log.info("事件：{}============模板内容：{}", infoType.getCnName(), silkPlatInformation);
        if (!StringUtils.isEmpty(silkPlatInformation) && silkPlatInformation.getUseEmail() == 1) {
            int type = infoType.getOrdinal() == 14 ? 1 : infoType.getOrdinal() == 15 ? 5 : infoType.getOrdinal();
            UserConfigurationCenter userConfigurationCenter = otcMsgService.getUserConfigurationCenterByMemberAndEvent(memberId, type);
            if (!StringUtils.isEmpty(userConfigurationCenter) && userConfigurationCenter.getIsEmail() == 1) {
                // 邮件通道开启（用户设置）
                Member member = getService().getMemberByEmail(memberId);
                if (!StringUtils.isEmpty(member)) {
                    emailEnity.setToEmail(member.getEmail());
                    String title = "";
                    String content = "";
                    if (SysConstant.EN_LANGUAGE.equals(languageCode)) {
                        title = silkPlatInformation.getEmailTitleEn() == null ? "" : silkPlatInformation.getEmailTitleEn();
                        content = silkPlatInformation.getEmailContentEn() == null ? "" : silkPlatInformation.getEmailContentEn();
                    } else {
                        title = silkPlatInformation.getEmailTitleCn() == null ? "" : silkPlatInformation.getEmailTitleCn();
                        content = silkPlatInformation.getEmailContentCn() == null ? "" : silkPlatInformation.getEmailContentCn();
                    }
                    if (title != null && !"".equals(title)) {
                        if (content != null && !"".equals(content)) {
                            title = messageDealService.parseNotificationConten("emailTitle",
                                    title, dataModel);
                            content = messageDealService.parseNotificationConten("emailContent",
                                    content, dataModel);
                            emailEnity.setSubject(title);
                            emailEnity.setHtmlConent(content);
                            log.info("事件：{}============推送邮件内容：{}", infoType.getCnName(), emailEnity);
                            iEmailService.sendEmailPro(emailEnity);
                        } else {
                            log.info("事件：{}============接收邮箱：{}，内容为空。", infoType.getCnName(), member.getEmail());
                        }
                    } else {
                        log.info("事件：{}============接收邮箱：{}，标题为空。", infoType.getCnName(), member.getEmail());
                    }
                } else {
                    log.info("事件：{}============接收邮箱为空", infoType.getCnName());
                }
            } else {
                log.info("事件：{}============邮件个人开关，已关闭", infoType.getCnName());
            }
        } else {
            log.info("事件：{}============模板为空或者邮件总开关已关闭", infoType.getCnName());
        }
    }

    /**
     * 获取实名国家
     *
     * @param memberId 会员ID
     * @return
     */
    public String getCountry(Long memberId) {
        if (memberId == null) {
            return null;
        }
        // 判断用户是否存在
        Member member = getService().getMember(memberId);
        if (StringUtils.isEmpty(member)) {
            return null;
        }
        // 推送邮件
        String country = member.getLocation().getCountry() == null ? SysConstant.CHINA : member.getLocation().getCountry();
        if (country.contains(SysConstant.CHINA)) {
            country = SysConstant.ZH_LANGUAGE;
        } else {
            country = SysConstant.EN_LANGUAGE;
        }
        return country;
    }

    /**
     * 判断当前会员，是否存在邮箱
     *
     * @param memberId 会员ID
     * @return 如果存在邮箱，则返回会员数据
     */
    public Member getMemberByEmail(Long memberId) {
        Member member = memberService.findOne(memberId);
        if (!StringUtils.isEmpty(member)) {
            String email = member.getEmail();
            if (!StringUtils.isEmpty(email)) {
                return member;
            }
        }
        return null;
    }

    /**
     * 根据会员ID，获取会员基础信息
     *
     * @param memberId 会员ID
     * @return
     */
    public Member getMember(Long memberId) {
        return memberService.findOne(memberId);
    }

    /**
     * 平台消息内容，通用通知发送
     *
     * @param receivingObject 接收方，1表示买方，2表示卖方
     * @param memberId        会员ID
     * @param infoType        事件类型
     * @param languageCode    语言
     * @param dataModel       动态内容
     * @param id              跳转关联ID
     * @param noticeType      类型，用作判断跳转到哪个页面
     */
    @Async
    public void sendCommonNotice(Integer receivingObject, Long memberId, InfoType infoType, String languageCode, Map dataModel, String id, NoticeType noticeType) {
        NoticeEntity noticeEntity = new NoticeEntity();
        noticeEntity.setNoticeType(NoticeType.SYS_NOTICE);
        SysNoticeEntity sysNoticeEntity = new SysNoticeEntity();
        sysNoticeEntity.setMemberId(memberId);
        SilkPlatInformation silkPlatInformation = otcMsgService.getSilkPlatInformationByEvent(receivingObject, infoType.getOrdinal());
        if (!StringUtils.isEmpty(silkPlatInformation)) {
            Integer useOffline = silkPlatInformation.getUseOffline();
            Integer useInstation = silkPlatInformation.getUseInstation();
            if ((useOffline == 1 || useInstation == 1)) {
                int type = infoType.getOrdinal() == 14 ? 1 : infoType.getOrdinal() == 15 ? 5 : infoType.getOrdinal();
                UserConfigurationCenter userConfigurationCenter = otcMsgService.getUserConfigurationCenterByMemberAndEvent(memberId, type);
                if (userConfigurationCenter.getIsApns() == 1) {
                    String title = "";
                    String content = "";
                    if (SysConstant.EN_LANGUAGE.equals(languageCode)) {
                        title = silkPlatInformation.getOfflineTitleEn() == null ? "" : silkPlatInformation.getOfflineTitleEn();
                        content = silkPlatInformation.getOfflineContentEn() == null ? "" : silkPlatInformation.getOfflineContentEn();
                    } else {
                        title = silkPlatInformation.getOfflineTitleCn() == null ? "" : silkPlatInformation.getOfflineTitleCn();
                        content = silkPlatInformation.getOfflineContentCn() == null ? "" : silkPlatInformation.getOfflineContentCn();
                    }
                    title = messageDealService.parseNotificationConten("offLineTitle",
                            title, dataModel);
                    content = messageDealService.parseNotificationConten("offLineContent",
                            content, dataModel);
                    noticeEntity.setIsAlert(useOffline == 1 ? 1 : 0);
                    noticeEntity.setIsOffline(useInstation == 1 ? 1 : 0);
                    sysNoticeEntity.setSubNoticeType(noticeType);
                    sysNoticeEntity.setTitle(title);
                    sysNoticeEntity.setSubTitle(content);
                    sysNoticeEntity.getExtras().put("id", id);
                    noticeEntity.setData(sysNoticeEntity);
                    log.info("事件：{}============推送通知内容：{}", infoType.getCnName(), noticeEntity);
                    iJpushService.sendNoticeEntity(noticeEntity);
                } else {
                    log.info("事件：{}============APP通知，个人开关已关闭", infoType.getCnName());
                }
            } else {
                log.info("事件：{}============APP通知总开关已关闭", infoType.getCnName());
            }
        } else {
            log.info("事件：{}============模板为空", infoType.getCnName());
        }
    }

    /**
     * 平台消息内容，运营手动编辑站内信
     *
     * @param id         手动编辑站内信ID
     * @param noticeType 类型，用作判断跳转到哪个页面
     */
    @Async
    public void sendCustomizingNotice(String id, NoticeType noticeType) {
        NoticeEntity noticeEntity = new NoticeEntity();
        noticeEntity.setNoticeType(NoticeType.SYS_NOTICE);
        SysNoticeEntity sysNoticeEntity = new SysNoticeEntity();
        PlatInstation platInstation = platInstationService.findById(Long.valueOf(id));
        if (platInstation != null) {
            Integer type = platInstation.getType();
            String title = platInstation.getTitle();
            noticeEntity.setIsAlert(1);
            noticeEntity.setIsOffline(1);
            sysNoticeEntity.setSubNoticeType(noticeType);
            sysNoticeEntity.setTitle(title);
            String content = platInstation.getContent();
            Document document = Jsoup.parse(content);
            content = document.text();
            Integer contentLength = content.length();
            content = content.substring(0, contentLength >= 100 ? 100 : contentLength);
            log.info("运营手动编辑站内============id：{}，去标签，截取内容：{}", id, content);
            sysNoticeEntity.setSubTitle(content);
            noticeEntity.setData(sysNoticeEntity);
            log.info("运营手动编辑站内============id：{}", id);
            if (type == 1) {
                Long saveCustomizingNotice = getService().saveCustomizingNotice(title, platInstation.getContent(), Long.valueOf(0L), id, InfoType.MANUAL_INSTATION);
                if (saveCustomizingNotice == null) {
                    log.info("运营手动编辑站内============id：{}，发送给所有用户，推送通知内容：{}，通知内容保存失败，不推送通知", id, noticeEntity);
                } else {
                    sysNoticeEntity.getExtras().put("id", saveCustomizingNotice);
                    sysNoticeEntity.setMemberId(Long.valueOf(0L));
                    log.info("运营手动编辑站内============id：{}，发送给所有用户，推送通知内容：{}", saveCustomizingNotice, noticeEntity);
                    iJpushService.sendNoticeEntity(noticeEntity);
                }
            } else {
                String users = platInstation.getMemberIds();
                if (users != null) {
                    String user[] = users.split(",");
                    for (int i = 0; i < user.length; i++) {
                        String memberId = user[i];
                        if (memberId != null && !"".equals(memberId)) {
                            Long saveCustomizingNotice = getService().saveCustomizingNotice(title, platInstation.getContent(), Long.valueOf(memberId), id, InfoType.MANUAL_INSTATION);
                            if (saveCustomizingNotice == null) {
                                log.info("运营手动编辑站内============id：{}，发送给用户：{}，推送通知内容：{}，通知内容保存失败，不推送通知", id, memberId, noticeEntity);
                            } else {
                                sysNoticeEntity.getExtras().put("id", saveCustomizingNotice);
                                sysNoticeEntity.setMemberId(Long.valueOf(memberId));
                                log.info("运营手动编辑站内============id：{}，发送给用户：{}，推送通知内容：{}", saveCustomizingNotice, memberId, noticeEntity);
                                iJpushService.sendNoticeEntity(noticeEntity);
                            }
                        }
                    }
                }
            }
        } else {
            log.info("运营手动编辑站内，未找到相关内容============id：{}", id);
        }
    }

    /**
     * 平台消息内容，充值到账(保存并发送通知)
     *
     * @param receivingObject
     * @param memberId
     * @param infoType
     * @param languageCode
     * @param dataModel
     * @param id
     * @param noticeType
     */
    @Async
    public void sendSaveAndCoinNotice(Integer receivingObject, Long memberId, InfoType infoType, String languageCode, Map dataModel, String id, NoticeType noticeType) {
        NoticeEntity noticeEntity = new NoticeEntity();
        noticeEntity.setNoticeType(NoticeType.SYS_NOTICE);
        SysNoticeEntity sysNoticeEntity = new SysNoticeEntity();
        sysNoticeEntity.setMemberId(memberId);
        SilkPlatInformation silkPlatInformation = otcMsgService.getSilkPlatInformationByEvent(receivingObject, infoType.getOrdinal());
        if (!StringUtils.isEmpty(silkPlatInformation)) {
            Integer useOffline = silkPlatInformation.getUseOffline();
            if ((useOffline == 1)) {
                UserConfigurationCenter userConfigurationCenter = otcMsgService.getUserConfigurationCenterByMemberAndEvent(memberId, infoType.getOrdinal());
                if (userConfigurationCenter.getIsApns() == 1) {
                    String title = "";
                    String content = "";
                    if (SysConstant.EN_LANGUAGE.equals(languageCode)) {
                        title = silkPlatInformation.getOfflineTitleEn() == null ? "" : silkPlatInformation.getOfflineTitleEn();
                        content = silkPlatInformation.getOfflineContentEn() == null ? "" : silkPlatInformation.getOfflineContentEn();
                    } else {
                        title = silkPlatInformation.getOfflineTitleCn() == null ? "" : silkPlatInformation.getOfflineTitleCn();
                        content = silkPlatInformation.getOfflineContentCn() == null ? "" : silkPlatInformation.getOfflineContentCn();
                    }
                    title = messageDealService.parseNotificationConten("offLineTitle",
                            title, dataModel);
                    content = messageDealService.parseNotificationConten("offLineContent",
                            content, dataModel);
                    noticeEntity.setIsAlert(1);
                    noticeEntity.setIsOffline(1);
                    sysNoticeEntity.setSubNoticeType(noticeType);
                    sysNoticeEntity.setTitle(title);
                    noticeEntity.setData(sysNoticeEntity);
                    log.info("事件：{}============推送通知内容：{}", infoType.getCnName(), noticeEntity);
                    if (memberId != null && !"".equals(memberId)) {
                        Long saveCustomizingNotice = getService().saveCustomizingNotice(title, content, Long.valueOf(memberId), id, infoType);
                        if (saveCustomizingNotice == null) {
                            log.info("充值到账============id：{}，发送给用户：{}，推送通知内容：{}，通知内容保存失败，不推送通知", id, memberId, noticeEntity);
                        } else {
                            sysNoticeEntity.setSubTitle(content);
                            sysNoticeEntity.getExtras().put("id", saveCustomizingNotice);
                            sysNoticeEntity.setMemberId(Long.valueOf(memberId));
                            log.info("充值到账============id：{}，发送给用户：{}，推送通知内容：{}", saveCustomizingNotice, memberId, noticeEntity);
                            iJpushService.sendNoticeEntity(noticeEntity);
                        }
                    }
                } else {
                    log.info("事件：{}============APP通知，个人开关已关闭", infoType.getCnName());
                }
            } else {
                log.info("事件：{}============APP通知总开关已关闭", infoType.getCnName());
            }
        } else {
            log.info("事件：{}============模板为空", infoType.getCnName());
        }
    }

    /**
     * 运营手动发送站内信/充值到账，先保存
     *
     * @param title    标题
     * @param content  内容
     * @param memberId 接收人
     * @return
     */
    public Long saveCustomizingNotice(String title, String content, Long memberId, String id, InfoType infoType) {
        try {
            MemberMailEntity memberMailEntity = new MemberMailEntity();
            memberMailEntity.setSubject(title);
            memberMailEntity.setContent(content);
            memberMailEntity.setToMemberId(memberId);
            MessageResult messageResult = iJpushService.sendMemberMailEntity(memberMailEntity);
            log.info("运营手动编辑站内/充值到账，调用通知保存，返回结果：{}", messageResult);
            if (messageResult.isSuccess()) {
                Object obj = messageResult.getData();
                if (obj != null) {
                    JSONObject json = (JSONObject) JSONObject.toJSON(obj);
                    if (json.containsKey("id")) {
                        return Long.valueOf(json.get("id").toString());
                    }
                }
            } else {
                kafkaDelayService.sendSaveNotice(id, memberId, infoType);
                log.info("调用通知保存，失败");
            }
            return null;
        } catch (Exception e) {
            kafkaDelayService.sendSaveNotice(id, memberId, infoType);
            log.info("调用通知保存，发生异常");
        }
        return null;
    }

    /**
     * 平台消息内容，发送短信
     *
     * @param receivingObject 消息接收方(1-买方;2-卖方)
     * @param memberId        接收会员ID
     * @param infoType        触发事件
     * @param dataModel       模板内容填充
     */
    @Async
    public void sendCommonSms(Integer receivingObject, Long memberId, InfoType infoType, Map dataModel) {
        SilkPlatInformation silkPlatInformation = otcMsgService.getSilkPlatInformationByEvent(receivingObject, infoType.getOrdinal());
        log.info("事件：{}============模板内容：{}", infoType.getCnName(), silkPlatInformation);
        if (!StringUtils.isEmpty(silkPlatInformation) && silkPlatInformation.getUseSms() == 1) {
            // 短信通道开启（总开关）
            int type = infoType.getOrdinal() == 14 ? 1 : infoType.getOrdinal() == 15 ? 5 : infoType.getOrdinal();
            UserConfigurationCenter userConfigurationCenter = otcMsgService.getUserConfigurationCenterByMemberAndEvent(memberId, type);
            if (!StringUtils.isEmpty(userConfigurationCenter) && userConfigurationCenter.getIsSms() == 1) {
                // 短信通道开启（用户设置）
                Member member = getService().getMember(memberId);
                if (!StringUtils.isEmpty(member)) {
                    String content = "";
                    try {
                        String mobilePhone = member.getMobilePhone();
                        String areaCode = member.getCountry().getAreaCode();
                        if ("86".equals(areaCode)) {
                            if (!ValidateUtil.isMobilePhone(mobilePhone.trim())) {
                                log.info("{}用户（{}）的手机号为空或格式错误", member.getRealName(), member.getId());
                                return;
                            }
                            content = silkPlatInformation.getSmsContentCn() == null ? "" : silkPlatInformation.getSmsContentCn();
                            content = messageDealService.parseNotificationConten("smsContent",
                                    content, dataModel);
                            log.info("事件：{}============接收手机号：{}，短信内容：{}", infoType.getCnName(), mobilePhone, content);
                            smsProvider.sendSingleMessage(mobilePhone, content);
                        } else if ("+886".equals(areaCode) || "+853".equals(areaCode) || "+852".equals(areaCode)) {
                            content = silkPlatInformation.getSmsContentTraditional() == null ? "" : silkPlatInformation.getSmsContentTraditional();
                            content = messageDealService.parseNotificationConten("smsContent",
                                    content, dataModel);
                            log.info("事件：{}============接收手机号：{}，短信内容：{}", infoType.getCnName(), areaCode + mobilePhone, content);
                            smsProvider.sendSingleMessage(areaCode + mobilePhone, content);
                        } else {
                            content = silkPlatInformation.getSmsContentEn() == null ? "" : silkPlatInformation.getSmsContentEn();
                            content = messageDealService.parseNotificationConten("smsContent",
                                    content, dataModel);
                            log.info("事件：{}============接收手机号：{}，短信内容：{}", infoType.getCnName(), areaCode + mobilePhone, content);
                            smsProvider.sendSingleMessage(areaCode + mobilePhone, content);
                        }
                    } catch (Exception e) {
                        log.error("sms 发送失败");
                        e.printStackTrace();
                    }
                } else {
                    log.info("事件：{}============接收对象为空", infoType.getCnName());
                }
            } else {
                log.info("事件：{}============短信个人开关，已关闭", infoType.getCnName());
            }
        } else {
            log.info("事件：{}============模板为空或者短信总开关已关闭", infoType.getCnName());
        }
    }

}
