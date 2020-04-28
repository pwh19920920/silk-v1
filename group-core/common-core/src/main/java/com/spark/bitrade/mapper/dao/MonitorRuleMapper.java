package com.spark.bitrade.mapper.dao;


import com.spark.bitrade.constant.MonitorTriggerEvent;
import com.spark.bitrade.dto.MemberCancelDTO;
import com.spark.bitrade.dto.MonitorRuleConfigDto;
import com.spark.bitrade.entity.MonitorRuleConfig;
import com.spark.bitrade.vo.MemberAppealCountVo;
import com.spark.bitrade.vo.MemberOrderCancelVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author fumy
 * @time 2018.11.02 10:33
 */
@Mapper
public interface MonitorRuleMapper {

    List<MonitorRuleConfigDto> findList();

    /**
     * 用户订单取消记录
     * @author fumy
     * @time 2018.11.05 9:44
     * @param memberOrderCancelVO
     * @return true
     */
    List<MemberCancelDTO> findMemberCancelDetail(MemberOrderCancelVO memberOrderCancelVO);

    //用户订单取消次数
    List<Map<String,Object>> getMemberCancelCount(MemberOrderCancelVO memberOrderCancelVO);


    /**
     * 用户订单取消次数
     * @author fumy
     * @time 2018.11.05 9:44
     * @param memberOrderCancelVO
     * @return true
     */
    List<Map<String,Object>> MemberCancelCount(MemberOrderCancelVO memberOrderCancelVO);


    /**
     * 用户申诉次数查询
     * @author fumy
     * @time 2018.11.05 9:42
     * @param whereName
     * @param orderName
     * @param sort
     * @param startTime
     * @param endTime
     * @return true
     */
    List<MemberAppealCountVo> findMemberAppealCount(@Param("whereName") String whereName,@Param("orderName") String orderName,@Param("sort") String sort,
                                                    @Param("startTime") String startTime,@Param("endTime") String endTime,
                                                    @Param("orderStartTime") String orderStartTime,@Param("orderEndTime") String orderEndTime);

    /**
     * 根据用户id统计订单取消次数
     * @author Zhang Yanjun
     * @param memberId
     * @return
     */
    Map<String,Object> findOneByMemberId(@Param("memberId") Long memberId,@Param("time")int time);

    /**
     * 根据触发事件查询冻结权限配置
     * @author Zhang Yanjun
     * @time 2018.11.05 11:51
     * @param event
     * @return
     */
    List<MonitorRuleConfigDto> findMonitorRuleByEvent(@Param("event") int event);

    //add by tansitao 时间： 2018/11/5 原因：通过事件类型查询C2C告警监控规则
    List<MonitorRuleConfig> findAllByType(@Param("triggerEvent") int triggerEvent);

    /**
     * 根据触发事件，触发次数、冻结权限查询、用户等级是否已经存在该条规则
     * @author fumy
     * @time 2018.11.09 14:35
     * @param triggerEvent
     * @param triggerTimes
     * @param executeEvent
     * @return true
     */
    int isExistRule(@Param("triggerEvent")int triggerEvent,@Param("triggerTimes")int triggerTimes,@Param("executeEvent")int executeEvent,
                    @Param("userLevel") int userLevel);

    /**
            * 根据会员ID，获取C2C订单已取消次数
     *
             * @param memberId 会员ID
     * @return 当前会员C2C订单已取消次数
     */
    Map<String, Object> countCancleOrder(@Param("memberId") Long memberId);

    /**
     * 根据会员等级和事件，拦截权限
     *
     * @param triggerUserLevel 用户等级，0-普通会员；1-实名认证；2-商家认证
     * @param triggerEvent     触发事件
     * @return 会员等级和事件拦截权限
     */
    List<Map<String, Object>> listMonitorRuleByUserLevelAndTriggerEvent(@Param("triggerUserLevel") Integer triggerUserLevel, @Param("triggerEvent") Integer triggerEvent);

}
