package com.spark.bitrade.mapper.dao;


import com.spark.bitrade.entity.SilkPlatInformation;
import com.spark.bitrade.service.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 平台消息内容
 *
 * @author zhongxj
 * @date 2019.09.11
 */
@Mapper
public interface SilkPlatInformationMapper extends SuperMapper<SilkPlatInformation> {
    /**
     * 获取指定时间，平台消息内容
     *
     * @param receivingObject 消息接收方（0-被交易方；1-交易方）
     * @param infoType        事件类型{0:充值到账提醒,1:新订单创建提醒,2:交易即将过期,3:已付款提醒,4:已释放提醒,5:申诉处理结果(成功)提醒,6:申诉处理结果(失败用户权限被冻结)提醒,7:申诉处理结果(失败被冻结前警告)提醒,8:收到了C2C用户聊天消息,9:商家认证审核通过}
     * @return 当前会员，需要发送的消息通道
     */
    SilkPlatInformation getSilkPlatInformationByEventAndReceiving(@Param("receivingObject") Integer receivingObject, @Param("infoType") Integer infoType);

    /**
     * 根据事件获取渠道开关
     *
     * @param infoType
     * @return
     */
    SilkPlatInformation getSilkPlatInformation(@Param("infoType") Integer infoType);
}
