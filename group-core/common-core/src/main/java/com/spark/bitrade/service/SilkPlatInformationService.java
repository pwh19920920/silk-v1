package com.spark.bitrade.service;

import com.spark.bitrade.entity.SilkPlatInformation;
import com.spark.bitrade.mapper.dao.SilkPlatInformationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 平台消息内容
 *
 * @author zhongxj
 * @date 2019.09.11
 */
@Service
@Slf4j
public class SilkPlatInformationService {

    @Resource
    private SilkPlatInformationMapper silkPlatInformationMapper;

    /**
     * 获取指定事件，平台消息内容
     *
     * @param receivingObject 消息接收方（0-被交易方；1-交易方）
     * @param infoType        事件类型{0:充值到账提醒,1:新订单创建提醒,2:交易即将过期,3:已付款提醒,4:已释放提醒,5:申诉处理结果(成功)提醒,6:申诉处理结果(失败用户权限被冻结)提醒,7:申诉处理结果(失败被冻结前警告)提醒,8:收到了C2C用户聊天消息,9:商家认证审核通过}
     * @return 当前会员，需要发送的消息通道
     */
//    @Cacheable(cacheNames = "silkPlatInformation", key = "'entity:silkPlatInformation:'+#receivingObject+'-'+#infoType")
    public SilkPlatInformation getSilkPlatInformationByEvent(Integer receivingObject, Integer infoType) {
        return silkPlatInformationMapper.getSilkPlatInformationByEventAndReceiving(receivingObject, infoType);
    }

    /**
     * 根据事件获取渠道开关
     *
     * @param infoType
     * @return
     */
    public SilkPlatInformation getSilkPlatInformation(Integer infoType) {
        return silkPlatInformationMapper.getSilkPlatInformation(infoType);
    }

}
