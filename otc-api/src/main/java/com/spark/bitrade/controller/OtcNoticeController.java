package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.AdvertiseType;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.dto.OtcMsgDTO;
import com.spark.bitrade.entity.CurrencyManage;
import com.spark.bitrade.entity.Order;
import com.spark.bitrade.entity.ScanOrder;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.feign.IOtcServerV2Service;
import com.spark.bitrade.service.CountryService;
import com.spark.bitrade.service.OtcMsgService;
import com.spark.bitrade.service.OtcOrderService;
import com.spark.bitrade.service.optfor.RedisZSetService;
import com.spark.bitrade.util.MessageResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.*;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * 订单与通知关联的接口
 */
@Slf4j
@RestController
public class OtcNoticeController {

    @Autowired
    private RedisZSetService redisZSetService;
    @Autowired
    private OtcOrderService otcOrderService;
    @Autowired
    private CountryService countryService;
    @Autowired
    private OtcMsgService otcMsgService;
    @Autowired
    private IOtcServerV2Service iOtcServerV2Service;

    /**
     * 获取Otc模块的总通知数量，PC目前正在调用的
     *
     * @return
     */
    @RequestMapping("/getNoticeCnt4Otc")
    public MessageResult getNoticeCnt4Otc(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        return MessageResult.success("success", getNotice4OtcCount(String.valueOf(user.getId())));
    }

    /**
     * 获取Otc模块的总通知
     *
     * @return
     */
    @RequestMapping("/getNotice4Otc")
    public MessageResult getNotice4Otc(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        List<NoticeEntity> lst = getNotice4OtcList(String.valueOf(user.getId()));
        Collections.sort(lst); //倒序排序
        return MessageResult.success("success", lst);
    }

    /**
     * 获取OTC订单中包含消息通知的列表
     *
     * @return 订单中包含了消息通知
     */
    @RequestMapping("/getNotice4OtcOrderInfo")
    public MessageResult getNotice4OtcInfo(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        List<ScanOrder> lstMergeNoticeOrder = new ArrayList<>();

        //进行中的订单列表
        List<Order> lstOrder = otcOrderService.getAllGoingOrder(user.getId());
        List<CurrencyManage> currencyManageList = iOtcServerV2Service.getAllCurrency().getData();

        //通知列表
        List<NoticeEntity> lstNotice = getNotice4OtcList(String.valueOf(user.getId()));

        //=================合并未完成的订单和通知列表==============
        //进行中的订单到结果中
        if (lstOrder != null) {
            lstOrder.forEach(order -> {
                ScanOrder scanOrder = ScanOrder.toScanOrder(order, user.getId());
                if("USDC".equals(order.getCoin().getUnit()) && scanOrder.getType() == AdvertiseType.BUY && scanOrder.getAdvertiseType() == AdvertiseType.BUY){
                    scanOrder.setCommission(order.getCommission());
                }
                for(CurrencyManage c : currencyManageList){
                    if(c.getId().equals(order.getCurrencyId())){
                        scanOrder.setCurrencyUnit(c.getUnit());
                        scanOrder.setCurrencySymbol(c.getSymbol());
                        scanOrder.setCurrencyName(c.getName());
                        scanOrder.setCurrencyId(c.getId());
                        break;
                    }
                }
//                scanOrder.setCountry(countryService.findOne(scanOrder.getCountryName()));
                //判断是否有消息通知
                if (lstNotice != null) {
                    //按订单ID从消息列表中过滤消息的数量
                    /*long count = lstNotice.stream().filter( e -> e.id.equals(order.getOrderSn()) ).count();
                    if(count > 0) {
                        scanOrder.setNoticeFlag(true);
                    }*/
                    Optional<NoticeEntity> noticeEntity =
                            lstNotice.stream().filter(e -> e.id.equals(order.getOrderSn())).findFirst();
                    if (noticeEntity.isPresent()) {
                        scanOrder.setNoticeFlag(true);  //设置有通知
                        scanOrder.setNoticeType(noticeEntity.get().type); //更新通知类型
                        scanOrder.setNoticeTime(noticeEntity.get().time); //更新通知时间
                    }
                }

                lstMergeNoticeOrder.add(scanOrder);
            });
        }

        //合并消息中的订单到结果中（备注：临界值的情况下订单为已完成状态）
        if (lstNotice != null) {
            lstNotice.forEach(notice -> {
                long count = 0L;
                //根据消息ID（订单ID）判断是否已经在未完成的订单列表中
                if (lstOrder != null) {
                    count = lstOrder.stream().filter(order -> notice.id.equals(order.getOrderSn())).count();
                }
                //处理消息对应的订单信息
                if (count == 0) {
                    Order order = otcOrderService.findOneByOrderId(notice.id);
                    //输出的订单信息
                    ScanOrder scanOrder = ScanOrder.toScanOrder(order, user.getId());
                    if("USDC".equals(order.getCoin().getUnit()) && scanOrder.getType() == AdvertiseType.BUY && scanOrder.getAdvertiseType() == AdvertiseType.BUY){
                        scanOrder.setCommission(order.getCommission());
                    }
                    for(CurrencyManage c : currencyManageList){
                        if(c.getId().equals(order.getCurrencyId())){
                            scanOrder.setCurrencyUnit(c.getUnit());
                            scanOrder.setCurrencySymbol(c.getSymbol());
                            scanOrder.setCurrencyName(c.getName());
                            scanOrder.setCurrencyId(c.getId());
                            break;
                        }
                    }
//                    scanOrder.setCountry(countryService.findOne(scanOrder.getCountryName()));
                    //scanOrder.setNoticeTime(order.getCreateTime().getTime());
                    scanOrder.setNoticeFlag(true); //设置有通知
                    scanOrder.setNoticeType(notice.type); //更新通知类型
                    scanOrder.setNoticeTime(notice.time); //更新通知时间

                    lstMergeNoticeOrder.add(scanOrder);
                }
            });
        }

        Collections.sort(lstMergeNoticeOrder); //倒序排序
        return MessageResult.success("success", lstMergeNoticeOrder);
    }

    /**
     * C2C消息组件内容
     *
     * @param user 会员信息
     * @return
     */
    @RequestMapping("/getNotice4OtcOrderInfo/news")
    public MessageResult getNotice4OtcInfoNews(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Long memberId = user.getId();
        log.info("C2C消息组件，memberId={}", memberId);
//        Long memberId = 280474L;
        List<OtcMsgDTO> otcMsgDTOList = new ArrayList<>();
        List<Order> lstOrder = otcOrderService.getAllGoingOrder(memberId);
        List<NoticeEntity> lstNotice = getNotice4OtcList(String.valueOf(memberId));
        log.info("C2C消息组件，memberId={},lstOrder={},lstNotice={}", memberId, lstOrder, lstNotice);
        if (lstOrder != null) {
            lstOrder.forEach(order -> {
                OtcMsgDTO otcMsgDTO = otcMsgService.getListItemContent(order, memberId);
                // 判断是否有消息通知
                if (lstNotice != null) {
                    // 按订单ID从消息列表中过滤消息的数量
                    Optional<NoticeEntity> noticeEntity =
                            lstNotice.stream().filter(e -> e.id.equals(order.getOrderSn())).findFirst();
                    if (noticeEntity.isPresent()) {
                        otcMsgDTO.setNoticeFlag(true);  // 设置有通知
                        otcMsgDTO.setNoticeType(noticeEntity.get().type); // 更新通知类型
                    }
                }
                if (otcMsgDTO.getSendContent() != null && !"".equals(otcMsgDTO.getSendContent())) {
                    otcMsgDTOList.add(otcMsgDTO);
                } else {
                    log.info("C2C消息组件，memberId={},otcMsgDTO={},内容为空", memberId, otcMsgDTO);
                }
            });
        }
        if (lstNotice != null) {
            lstNotice.forEach(notice -> {
                long count = 0L;
                if (lstOrder != null) {
                    count = lstOrder.stream().filter(order -> notice.id.equals(order.getOrderSn())).count();
                }
                if (count == 0) {
                    Order order = otcOrderService.findOneByOrderId(notice.id);
                    OtcMsgDTO otcMsgDTO = otcMsgService.getListItemContent(order, memberId);
                    otcMsgDTO.setNoticeFlag(true); // 设置有通知
                    otcMsgDTO.setNoticeType(notice.type); // 更新通知类型
                    if (otcMsgDTO.getSendContent() != null && !"".equals(otcMsgDTO.getSendContent())) {
                        otcMsgDTOList.add(otcMsgDTO);
                    } else {
                        log.info("C2C消息组件，事件循环，memberId={},otcMsgDTO={},内容为空", memberId, otcMsgDTO);
                    }
                }
            });
        }
        Collections.sort(otcMsgDTOList);
        log.info("C2C消息组件，memberId={},otcMsgDTOList={}", memberId, otcMsgDTOList);
        return MessageResult.success("success", otcMsgDTOList);
    }

    /**
     * 获取根据订单ID去重的通知数量（包含订单流转和聊天）
     *
     * @param uid 用户ID
     * @return
     */
    private int getNotice4OtcCount(String uid) {
        List<String> lst = new ArrayList<>();
        //合并聊天和otc事件流转通知
        String keyChat = SysConstant.NOTICE_OTC_CHAT_PREFIX + uid;
        String keyOtc = SysConstant.NOTICE_OTC_EVENT_PREFIX + uid;

        //聊天通知
        Set setChat = redisZSetService.zRange(keyChat, 0, -1);
        //otc事件流转通知
        Set setOtc = redisZSetService.zRange(keyOtc, 0, -1);

        if (setChat != null) {
            setChat.forEach(s -> lst.add(String.valueOf(s)));

            if (setOtc != null) {
                setOtc.forEach(s -> {
                    if (!lst.contains(s)) {
                        lst.add(String.valueOf(s));
                    }
                });
            }
        }

        return lst.size();
    }


    /**
     * 获取根据订单ID去重的通知列表（包含订单流转和聊天）
     *
     * @param uid 用户ID
     * @return 已按时间倒序排序的通知列表
     */
    private List<NoticeEntity> getNotice4OtcList(String uid) {
        List<NoticeEntity> lst = new ArrayList<>();
        //合并聊天和otc事件流转通知
        String keyChat = SysConstant.NOTICE_OTC_CHAT_PREFIX + uid;
        String keyOtc = SysConstant.NOTICE_OTC_EVENT_PREFIX + uid;

        //聊天通知
        Set<ZSetOperations.TypedTuple<String>> setChat = redisZSetService.zRangeWithScores(keyChat, 0, -1);
        //otc事件流转通知
        Set<ZSetOperations.TypedTuple<String>> setOtc = redisZSetService.zRangeWithScores(keyOtc, 0, -1);

        if (setChat != null) {
            setChat.forEach(t -> {
                lst.add(new NoticeEntity(String.valueOf(t.getValue()), t.getScore().longValue(), 1));
            });

            //合并事件流转的通知
            if (setOtc != null) {
                setOtc.forEach(t -> {
                    NoticeEntity noticeEntity =
                            new NoticeEntity(String.valueOf(t.getValue()), t.getScore().longValue(), 2);

                    int idx = lst.indexOf(noticeEntity);
                    if (idx != -1) {
                        NoticeEntity entity = lst.get(idx);
                        entity.setType(3); //事件+聊天

                        //更新通知的时间
                        if (entity.time < t.getScore().longValue()) {
                            entity.setTime(t.getScore().longValue());
                        }
                    } else {
                        lst.add(noticeEntity);
                    }
                });
            }
        }

        //Collections.sort(lst); //倒序排序
        return lst;
    }

    //通知实体
    @Data
    @AllArgsConstructor
    public static class NoticeEntity implements Comparable {
        private String id;  //通知ID
        private long time;  //通知时间
        private int type;   //通知类型，0=未知/1=聊天/2=事件/3=事件+聊天

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else {
                if (this == obj) { //实例相同
                    return true;
                } else if (this.id == null) { //ID不存在
                    return false;
                } else if (obj instanceof NoticeEntity) {
                    NoticeEntity c = (NoticeEntity) obj;
                    if (this.id.equals(c.id)) { //ID相同
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }

        //倒序排序
        @Override
        public int compareTo(Object o) {
            NoticeEntity c = (NoticeEntity) o;
            if (this.time < c.time) {
                return 1;
            } else if (this.time > c.time) {
                return -1;
            }
            return 0;
        }
    }

    //订单&通知
    /*@Builder
    @Data
    public static class NoticeOrder extends ScanOrder implements Comparable{
        private boolean noticeFlag = false;  //通知标记，true=有通知，false=没通知
        private long noticeTime;  //更新时间

        public static NoticeOrder toNoticeOrder(Order order, Long id) {
            return NoticeOrder.builder().orderSn(order.getOrderSn())
                    .createTime(order.getCreateTime())
                    .unit(order.getCoin().getUnit())
                    .price(order.getPrice())
                    .amount(order.getNumber())
                    .money(order.getMoney())
                    .status(order.getStatus())
                    .commission(id.equals(order.getMemberId())?order.getCommission(): BigDecimal.ZERO)
                    .name(order.getCustomerId().equals(id) ? order.getMemberName() : order.getCustomerName())
                    .memberId(order.getCustomerId().equals(id) ? order.getMemberId():order.getCustomerId())
                    .type(judgeType(order.getAdvertiseType(), order, id))
                    .payCode(order.getPayCode())
                    .countryName(order.getCountry())
                    .build();
        }


        //倒序排序
        @Override
        public int compareTo(Object o) {
            NoticeOrder c = (NoticeOrder) o;
            if(this.noticeTime < c.noticeTime){
                return 1;
            } else if(this.noticeTime > c.noticeTime) {
                return -1;
            }
            return 0;
        }
    }*/
}
