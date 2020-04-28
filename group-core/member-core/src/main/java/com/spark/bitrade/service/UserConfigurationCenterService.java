package com.spark.bitrade.service;

import com.spark.bitrade.entity.SilkPlatInformation;
import com.spark.bitrade.entity.UserConfigurationCenter;
import com.spark.bitrade.mapper.dao.UserConfigurationCenterMapper;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.spark.bitrade.util.MessageResult.success;

/**
 * 用户配置中心service层
 *
 * @author zhongxj
 * @date 2019.09.11
 */
@Service
@Slf4j
public class UserConfigurationCenterService {

    @Resource
    private UserConfigurationCenterMapper userConfigurationCenterMapper;
    @Autowired
    private SilkPlatInformationService silkPlatInformationService;

    /**
     * 公共部分
     *
     * @param u                       总开关
     * @param userConfigurationCenter 个人设置开关
     * @return
     */
    public UserConfigurationCenter getCommonUserConfigurationCenter(UserConfigurationCenter u, UserConfigurationCenter userConfigurationCenter) {
        userConfigurationCenter.setIsSms(u.getIsSms() == 0 ? 0 : u.getIsSms() == 2 ? 0 : userConfigurationCenter.getIsSms());
        userConfigurationCenter.setIsEmail(u.getIsEmail() == 0 ? 0 : u.getIsEmail() == 2 ? 0 : userConfigurationCenter.getIsEmail());
        userConfigurationCenter.setIsApns(u.getIsApns() == 0 ? 0 : u.getIsApns() == 2 ? 0 : userConfigurationCenter.getIsApns());
        return userConfigurationCenter;
    }

    /**
     * 获取当前会员，需要发送的消息通道
     *
     * @param memberId        会员ID
     * @param triggeringEvent 事件
     * @return 当前会员，需要发送的消息通道
     */
//    @Cacheable(cacheNames = "userConfigurationCenter", key = "'entity:userConfigurationCenter:'+#memberId+'-'+#triggeringEvent")
    public UserConfigurationCenter getUserConfigurationCenterByMemberAndEvent(Long memberId, Integer triggeringEvent) {
        UserConfigurationCenter userConfigurationCenter = userConfigurationCenterMapper.getUserConfigurationCenterByMemberIdAndEvent(memberId, triggeringEvent);
        if (StringUtils.isEmpty(userConfigurationCenter)) {
            return getService().getInitEvent(triggeringEvent, 0);
        }
        // 总开关，开启则开放开关按钮
        UserConfigurationCenter u = getService().getInitEvent(userConfigurationCenter.getTriggeringEvent(), 1);
        if (!StringUtils.isEmpty(u)) {
            getService().getCommonUserConfigurationCenter(u, userConfigurationCenter);
        }
        return userConfigurationCenter;
    }

    /**
     * 初始化事件  触发事件{0:充值到账提醒,1:新订单创建提醒,2:交易即将过期,3:已付款提醒,4:已释放提醒,5:申诉处理结果提醒}
     *
     * @return 事件集合
     */
    public List<UserConfigurationCenter> listInitEvent() {
        List<UserConfigurationCenter> centerDTOList = new ArrayList<>();
        int six = 6;
        for (int i = 0; i < six; i++) {
            centerDTOList.add(getService().getInitEvent(i, 0));
        }
        return centerDTOList;
    }

    /**
     * 获取会员当前配置列表
     *
     * @param memberId 会员ID
     * @return 会员当前配置列表
     */
    public MessageResult listUserConfigurationCenter(Long memberId) {
        MessageResult messageResult = success();
        List<UserConfigurationCenter> centerList = new ArrayList<>();
        List<UserConfigurationCenter> initCenterList = getService().listInitEvent();
        for (int i = 0; i < initCenterList.size(); i++) {
            UserConfigurationCenter userConfigurationCenter = userConfigurationCenterMapper.getUserConfigurationCenterByMemberIdAndEvent(memberId, i);
            if (userConfigurationCenter != null) {
                UserConfigurationCenter u = getService().getInitEvent(userConfigurationCenter.getTriggeringEvent(), 1);
                if (!StringUtils.isEmpty(u)) {
                    centerList.add(getService().getCommonUserConfigurationCenter(u, userConfigurationCenter));
                } else {
                    centerList.add(userConfigurationCenter);
                }
            } else {
                centerList.add(this.getInitEvent(i, 0));
            }
        }
        messageResult.setData(getService().filterList(centerList));
        return messageResult;
    }

    public List<UserConfigurationCenter> filterList(List<UserConfigurationCenter> list) {
        List<UserConfigurationCenter> userConfigurationCenterList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            UserConfigurationCenter userConfigurationCenter = list.get(i);
            if (userConfigurationCenter.getIsApns() == 0 && userConfigurationCenter.getIsEmail() == 0 && userConfigurationCenter.getIsSms() == 0) {
                log.info("获取个人中心消息提醒列表，事件：{}，渠道都关闭，不下发数据到前端", userConfigurationCenter.getTriggeringEvent());
            } else {
                userConfigurationCenterList.add(userConfigurationCenter);
            }
        }
        return userConfigurationCenterList;
    }

    /**
     * 获取事件初始化开关
     *
     * @param triggeringEvent
     * @param methodType      0未保存；1：已保存
     * @return
     */
    public UserConfigurationCenter getInitEvent(Integer triggeringEvent, Integer methodType) {
        UserConfigurationCenter userConfigurationCenter = new UserConfigurationCenter();
        triggeringEvent = triggeringEvent == 1 ? 14 : triggeringEvent;
        triggeringEvent = triggeringEvent == 5 ? 15 : triggeringEvent;
        SilkPlatInformation silkPlatInformation = silkPlatInformationService.getSilkPlatInformation(triggeringEvent);
        if (!StringUtils.isEmpty(silkPlatInformation)) {
            triggeringEvent = triggeringEvent == 14 ? 1 : triggeringEvent;
            triggeringEvent = triggeringEvent == 15 ? 5 : triggeringEvent;
            userConfigurationCenter.setTriggeringEvent(triggeringEvent);
            Integer isSms = silkPlatInformation.getUseSms() == 2 ? 0 : silkPlatInformation.getUseSms();
            if (triggeringEvent == 1 && methodType != 1) {
                isSms = isSms == 1 ? 2 : isSms;
            }
            userConfigurationCenter.setIsSms(isSms);
            userConfigurationCenter.setIsEmail(silkPlatInformation.getUseEmail() == 2 ? 0 : silkPlatInformation.getUseEmail());
            userConfigurationCenter.setIsApns(silkPlatInformation.getUseOffline() == 2 ? 0 : silkPlatInformation.getUseOffline());
        }
        return userConfigurationCenter;
    }

    public UserConfigurationCenterService getService() {
        return SpringContextUtil.getBean(UserConfigurationCenterService.class);
    }

}
