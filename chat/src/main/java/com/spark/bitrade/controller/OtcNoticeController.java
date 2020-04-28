//package com.spark.bitrade.controller;
//
//import com.alibaba.fastjson.JSON;
//import com.spark.bitrade.constant.SysConstant;
//import com.spark.bitrade.entity.Order;
//import com.spark.bitrade.service.OtcOrderService;
//import com.spark.bitrade.service.optfor.RedisZSetService;
//import com.spark.bitrade.util.MessageResult;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.ZSetOperations;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Set;
//
//@RestController
//public class OtcNoticeController {
//
//    @Autowired
//    private RedisZSetService redisZSetService;
//
//    @Autowired
//    private OtcOrderService otcOrderService;
//
//    /**
//     * 获取Otc模块的总通知数量
//     * @param uid 用户ID
//     * @return
//     */
//    @RequestMapping("/getNoticeCnt4Otc")
//    public MessageResult getNoticeCnt4Otc(String uid){
//        return MessageResult.success("success", getNotice4OtcCount(uid));
//    }
//
//    /**
//     * 获取Otc模块的总通知
//     * @param uid 用户ID
//     * @return
//     */
//    @RequestMapping("/getNotice4Otc")
//    public MessageResult getNotice4Otc(String uid){
//        List<NoticeEntity> lst = getNotice4OtcList(uid);
//        Collections.sort(lst); //倒序排序
//        return MessageResult.success("success", lst);
//    }
//
//    /**
//     * 获取OTC订单中包含消息通知的列表
//     * @param uid 用户ID
//     * @return 订单中包含了消息通知
//     */
//    @RequestMapping("/getNotice4OtcOrderInfo")
//    public MessageResult getNotice4OtcInfo(String uid){
//        List<NoticeOrder> lstMergeNoticeOrder = new ArrayList<>();
//
//        //未完成的订单列表
//        List<Order> lstOrder = otcOrderService.getAllOrdering(Long.valueOf(uid));
//        //通知列表
//        List<NoticeEntity> lstNotice = getNotice4OtcList(uid);
//
//        //=================合并未完成的订单和通知列表==============
//        //合并未完成的订单到结果中
//        if(lstOrder != null){
//            lstOrder.forEach(order -> {
//                NoticeOrder noticeOrder = new NoticeOrder();
//                BeanUtils.copyProperties(order, noticeOrder);
//                noticeOrder.setTime(order.getCreateTime().getTime());
//                //判断是否有消息通知
//                if(lstNotice != null) {
//                    //按订单ID从消息列表中过滤消息的数量
//                    long count = lstNotice.stream().filter( e -> e.id.equals(order.getOrderSn()) ).count();
//                    if(count > 0) {
//                        noticeOrder.setNew(true);
//                    }
//                }
//
//                lstMergeNoticeOrder.add(noticeOrder);
//            });
//        }
//
//        //合并消息中的订单到结果中（备注：临界值的情况下订单为已完成状态）
//        if(lstNotice != null){
//            lstNotice.forEach( notice -> {
//                long count = 0L;
//                //根据消息ID（订单ID）判断是否已经在未完成的订单列表中
//                if(lstOrder != null) {
//                    count = lstOrder.stream().filter( order -> notice.id.equals(order.getOrderSn()) ).count();
//                }
//                //处理消息对应的订单信息
//                if(count == 0){
//                    Order order = otcOrderService.findOneByOrderId(notice.id);
//                    NoticeOrder noticeOrder = new NoticeOrder();
//                    BeanUtils.copyProperties(order, noticeOrder);
//                    noticeOrder.setTime(order.getCreateTime().getTime());
//                    noticeOrder.setNew(true);
//
//                    lstMergeNoticeOrder.add(noticeOrder);
//                }
//            });
//        }
//
//        Collections.sort(lstMergeNoticeOrder); //倒序排序
//        return MessageResult.success("success", lstMergeNoticeOrder);
//    }
//
//
//    /**
//     * 获取根据订单ID去重的通知数量（包含订单流转和聊天）
//     * @param uid 用户ID
//     * @return
//     */
//    private int getNotice4OtcCount(String uid){
//        List<String> lst = new ArrayList<>();
//        //合并聊天和otc事件流转通知
//        String keyChat = SysConstant.NOTICE_OTC_CHAT_PREFIX+uid;
//        String keyOtc = SysConstant.NOTICE_OTC_EVENT_PREFIX+uid;
//
//        //聊天通知
//        Set setChat= redisZSetService.zRange(keyChat,0, -1);
//        //otc事件流转通知
//        Set setOtc = redisZSetService.zRange(keyOtc,0, -1);
//
//        if(setChat != null) {
//            setChat.forEach(s->lst.add(String.valueOf(s)));
//
//            if(setOtc != null) {
//                setOtc.forEach(s->{
//                    if(!lst.contains(s)) {
//                        lst.add(String.valueOf(s));
//                    }
//                });
//            }
//        }
//
//        return lst.size();
//    }
//
//
//    /**
//     * 获取根据订单ID去重的通知列表（包含订单流转和聊天）
//     *
//     * @param uid 用户ID
//     * @return 已按时间倒序排序的通知列表
//     */
//    private List<NoticeEntity> getNotice4OtcList(String uid){
//        List<NoticeEntity> lst = new ArrayList<>();
//        //合并聊天和otc事件流转通知
//        String keyChat = SysConstant.NOTICE_OTC_CHAT_PREFIX+uid;
//        String keyOtc = SysConstant.NOTICE_OTC_EVENT_PREFIX+uid;
//
//        //聊天通知
//        Set<ZSetOperations.TypedTuple<String>> setChat= redisZSetService.zRangeWithScores(keyChat,0, -1);
//        //otc事件流转通知
//        Set<ZSetOperations.TypedTuple<String>> setOtc = redisZSetService.zRangeWithScores(keyOtc,0, -1);
//
//        if(setChat != null) {
//            setChat.forEach(t->{
//                lst.add(new NoticeEntity(String.valueOf(t.getValue()), t.getScore().longValue()));
//            });
//
//            //合并事件流转的通知
//            if(setOtc != null) {
//                setOtc.forEach(t->{
//                    NoticeEntity noticeEntity =
//                            new NoticeEntity(String.valueOf(t.getValue()), t.getScore().longValue());
//
//                    int idx = lst.indexOf(noticeEntity);
//                    if(idx != -1) {
//                        NoticeEntity entity =lst.get(idx);
//                        //更新通知的时间
//                        if(entity.time < t.getScore().longValue()){
//                            entity.setTime(t.getScore().longValue());
//                        }
//                    } else {
//                        lst.add(noticeEntity);
//                    }
//                });
//            }
//        }
//
//        //Collections.sort(lst); //倒序排序
//        return lst;
//    }
//
//    //通知实体
//    @Data
//    @AllArgsConstructor
//    public static class NoticeEntity implements Comparable{
//        private String id;  //通知ID
//        private long time;  //通知时间
//
//        @Override
//        public boolean equals(Object obj){
//            if (obj == null) {
//                return false;
//            } else{
//                if(this == obj){ //实例相同
//                    return true;
//                } else if(this.id == null){ //ID不存在
//                    return false;
//                } else if (obj instanceof NoticeEntity){
//                    NoticeEntity c = (NoticeEntity) obj;
//                    if(this.id.equals(c.id)){ //ID相同
//                        return true ;
//                    }
//                }
//            }
//            return false ;
//        }
//
//        @Override
//        public String toString() {
//            return JSON.toJSONString(this);
//        }
//
//        //倒序排序
//        @Override
//        public int compareTo(Object o) {
//            NoticeEntity c = (NoticeEntity) o;
//            if(this.time < c.time){
//                return 1;
//            } else if(this.time > c.time) {
//                return -1;
//            }
//            return 0;
//        }
//    }
//
//    //订单&通知
//    @Data
//    public static class NoticeOrder extends Order implements Comparable{
//        private boolean isNew = false;  //通知标记，true=有通知，false=没通知
//        private long time;  //更新时间
//
//        //倒序排序
//        @Override
//        public int compareTo(Object o) {
//            NoticeOrder c = (NoticeOrder) o;
//            if(this.time < c.time){
//                return 1;
//            } else if(this.time > c.time) {
//                return -1;
//            }
//            return 0;
//        }
//    }
//}
