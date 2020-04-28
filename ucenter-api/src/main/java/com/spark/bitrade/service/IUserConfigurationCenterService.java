package com.spark.bitrade.service;

import com.spark.bitrade.entity.UserConfigurationCenter;
import com.spark.bitrade.feign.IUserCenterConfigurationApiService;
import com.spark.bitrade.mapper.dao.UserConfigurationCenterMapper;
import com.spark.bitrade.util.MessageRespResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 用户配置中心service层
 *
 * @author zhongxj
 * @date 2019.09.11
 */
@Service
@Slf4j
public class IUserConfigurationCenterService {

    @Resource
    private UserConfigurationCenterMapper userConfigurationCenterMapper;
    @Autowired
    private UserConfigurationCenterService userConfigurationCenterService;
    @Resource
    private IUserCenterConfigurationApiService iUserCenterConfigurationApiService;

    private static final String IS_SMS = "isSms";
    private static final String IS_EMAIL = "isEmail";
    private static final String IS_APNS = "isApns";


    /**
     * 保存
     *
     * @param memberId        会员ID
     * @param triggeringEvent 事件  触发事件{0:充值到账提醒,1:新订单创建提醒,2:交易即将过期,3:已付款提醒,4:已释放提醒,5:申诉处理结果提醒}
     * @param channel         渠道
     * @param status          状态 1开启，2关闭
     */
//    @CacheEvict(cacheNames = "userConfigurationCenter", key = "'entity:userConfigurationCenter:'+#memberId+'-'+#triggeringEvent", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Integer saveUserConfigurationCenter(Integer triggeringEvent, String channel, Long memberId, Integer status) {
        UserConfigurationCenter userConfigurationCenter = userConfigurationCenterMapper.getUserConfigurationCenterByMemberIdAndEvent(memberId, triggeringEvent);
        UserConfigurationCenter initUserConfigurationCenter = userConfigurationCenterService.getInitEvent(triggeringEvent,0);
        if (channel.equals(IS_SMS)) {
            initUserConfigurationCenter.setIsSms(status);
        }
        if (channel.equals(IS_EMAIL)) {
            initUserConfigurationCenter.setIsEmail(status);
        }
        if (channel.equals(IS_APNS)) {
            initUserConfigurationCenter.setIsApns(status);
        }
        if (userConfigurationCenter != null) {
            iUserCenterConfigurationApiService.updateUserConfigurationCenter(memberId, triggeringEvent, channel, status);
        } else {
            iUserCenterConfigurationApiService.addUserConfigurationCenter(memberId, triggeringEvent, initUserConfigurationCenter.getIsSms(), initUserConfigurationCenter.getIsEmail(), initUserConfigurationCenter.getIsApns());
        }
        return 1;
    }

}
