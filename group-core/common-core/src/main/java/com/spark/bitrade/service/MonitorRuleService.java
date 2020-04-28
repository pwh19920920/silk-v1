package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.MonitorExecuteEnvent;
import com.spark.bitrade.constant.MonitorTriggerEvent;
import com.spark.bitrade.dao.MonitorRuleDao;
import com.spark.bitrade.dto.ExpireOtcOrderDto;
import com.spark.bitrade.dto.MemberCancelDTO;
import com.spark.bitrade.dto.MonitorRuleConfigDto;
import com.spark.bitrade.entity.MonitorRuleConfig;
import com.spark.bitrade.mapper.dao.MonitorRuleMapper;
import com.spark.bitrade.vo.MemberAppealCountVo;
import com.spark.bitrade.vo.MemberOrderCancelVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;


/**
 * @author fumy
 * @time 2018.11.02 10:45
 */
@Service
public class MonitorRuleService {

    @Autowired
    MonitorRuleMapper ruleMapper;

    @Autowired
    MonitorRuleDao ruleDao;

    /**
     * 分页获取权限冻结配置列表
     * @author fumy
     * @time 2018.11.02 10:47
     * @param
     * @return true
     */
    public PageInfo<MonitorRuleConfigDto> findPage(int pageNo, int pageSize){
        Page<MonitorRuleConfigDto> page = PageHelper.startPage(pageNo,pageSize);
        ruleMapper.findList();
        return page.toPageInfo();
    }

    /**
     * 保存权限冻结配置
     * @author fumy
     * @time 2018.11.02 11:02
     * @param ruleConfigDto
     * @return true
     */
    @Transactional(rollbackFor = Exception.class)
    public void save(MonitorRuleConfigDto ruleConfigDto){

        //循环保存
        String[] events = ruleConfigDto.getExecuteEvent().split(",");
        for (int i =0;i<events.length;i++){
            //相同配置公共部分组装
            MonitorRuleConfig config = new MonitorRuleConfig();
            config.setExecuteDuration(ruleConfigDto.getExecuteDuration());
            config.setTriggerEvent(ruleConfigDto.getTriggerEvent());
            config.setTriggerStageCycle(ruleConfigDto.getTriggerStageCycle());
            config.setTriggerTimes(ruleConfigDto.getTriggerTimes());
            config.setTriggerUserLevel(ruleConfigDto.getTriggerUserLevel());
            //约束权限枚举索引值
            int index = Integer.parseInt(events[i]);
            config.setExecuteEvent(MonitorExecuteEnvent.values()[index]);
            ruleDao.save(config);
        }
    }

    /**
     * 更新
     * @author fumy
     * @time 2018.11.02 17:28
     * @param ruleConfigDto
     * @return true
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(MonitorRuleConfigDto ruleConfigDto){
        //相同配置公共部分组装
        MonitorRuleConfig config = new MonitorRuleConfig();
        config.setExecuteDuration(ruleConfigDto.getExecuteDuration());
        config.setTriggerEvent(ruleConfigDto.getTriggerEvent());
        config.setTriggerStageCycle(ruleConfigDto.getTriggerStageCycle());
        config.setTriggerTimes(ruleConfigDto.getTriggerTimes());
        config.setTriggerUserLevel(ruleConfigDto.getTriggerUserLevel());

        //循环保存
        String[] ids = ruleConfigDto.getId().split(",");
        String[] events = ruleConfigDto.getExecuteEvent().split(",");
        for (int i =0;i<ids.length;i++){
            config.setId(Long.valueOf(ids[i]));
            //如果位移到 i 位置的 id 大于 约束权限枚举数组最大索引值，则删除该 id 对应的规则配置(这里判断只针对编辑功能编辑约束权限<数据条数时的逻辑)
            if(i > events.length-1){
                ruleDao.delete(Long.valueOf(ids[i]));
            }else {//约束权限枚举索引值
                int index = Integer.parseInt(events[i]);
                config.setExecuteEvent(MonitorExecuteEnvent.values()[index]);
                ruleDao.save(config);
            }
        }
    }

    /**
     * 根据id删除删除权限冻结配置
     * @author fumy
     * @time 2018.11.02 11:04
     * @param ids
     * @return true
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long[] ids){
        for(int i=0;i<ids.length;i++){
            ruleDao.delete(ids[i]);
        }

    }
    /**
     * 用户取消记录
     * @author Zhang Yanjun
     * @time 2018.11.02 16:43
     * @param memberOrderCancelVO
     */
    public PageInfo<MemberCancelDTO> getMemberCancelDetails(MemberOrderCancelVO memberOrderCancelVO,int pageNo,int pageSize){
        Page<MemberCancelDTO> page=PageHelper.startPage(pageNo,pageSize);
        ruleMapper.findMemberCancelDetail(memberOrderCancelVO);
        return page.toPageInfo();
    }

    /**
     * 用户订单取消次数统计
     * @author Zhang Yanjun
     * @time 2018.11.02 17:27
     */
    public PageInfo<Map<String,Object>> getMemberCancelCount(MemberOrderCancelVO memberOrderCancelVO , int pageNo , int pageSize){
        Page<Map<String,Object>> page=PageHelper.startPage(pageNo,pageSize);
        ruleMapper.getMemberCancelCount(memberOrderCancelVO);
        return page.toPageInfo();
    }

    /**
     * 用户申诉次数查询
     * @author fumy
     * @time 2018.11.02 19:15
     * @param pageNo
     * @param pageSize
     * @return true
     */
    public PageInfo<MemberAppealCountVo> findMemberAppealCount(String whereName,String orderName,String sort,
                                                               String startTime,String endTime,String orderStartTime,String orderEndTime,
                                                               int pageNo,int pageSize){
        Page<MemberAppealCountVo> pageInfo = PageHelper.startPage(pageNo,pageSize);
        ruleMapper.findMemberAppealCount(whereName,orderName,sort,startTime,endTime,orderStartTime,orderEndTime);
        return pageInfo.toPageInfo();
    }

    /**
     * 根据用户id查询取消次数
     * @author Zhang Yanjun
     * @time 2018.11.05 11:24
     * @param memberId
     */
    public Map<String,Object> findOneByMemberId(Long memberId,int time){
        return ruleMapper.findOneByMemberId(memberId,time);
    }

    /**
     * 根据触发事件查询冻结权限配置
     * @author Zhang Yanjun
     * @time 2018.11.05 11:51
     * @param monitorTriggerEvent
     * @return
     */
    public List<MonitorRuleConfigDto> findMonitorRuleByEvent(MonitorTriggerEvent monitorTriggerEvent) {
        return ruleMapper.findMonitorRuleByEvent(monitorTriggerEvent.getOrdinal());
    }

    /**
     * 查询所有告警规则信息规则
     * @author tansitao
     * @time 2018/11/5 10:00 
     */
    @ReadDataSource
    public List<MonitorRuleConfig> findAllByType(MonitorTriggerEvent triggerEvent){
        return  ruleMapper.findAllByType(triggerEvent.getOrdinal());
    }

    /**
     * 根据触发事件，触发次数、冻结权限查询、用户等级是否已经存在该条规则
     * @author fumy
     * @time 2018.11.09 14:38
     * @param triggerEvent
     * @param triggerTimes
     * @param executeEvent
     * @return true
     */
    public boolean isExistRule(int triggerEvent,int triggerTimes,int executeEvent,int userLevel){
        int row = ruleMapper.isExistRule(triggerEvent,triggerTimes,executeEvent,userLevel);
        return row > 0 ? true : false;
    }

    /**
     * 获取C2C订单即将超时，扩充内容
     *
     * @param memberId     会员ID
     * @param triggerEvent 触发事件
     * @param memberLevel  0-普通会员；1-已实名认证；2-商家认证
     * @return C2C订单即将超时，扩充内容
     */
    public ExpireOtcOrderDto expireOtcOrder(Long memberId, Integer triggerEvent, Integer memberLevel) {
        ExpireOtcOrderDto expireOtcOrderDto = new ExpireOtcOrderDto();
        Map<String, Object> map = ruleMapper.countCancleOrder(memberId);
        if (!StringUtils.isEmpty(map)) {
            expireOtcOrderDto.setOtcOrderCancle(Integer.valueOf(map.get("countCancel").toString()));
        } else {
            expireOtcOrderDto.setOtcOrderCancle(0);
        }
        List<Map<String, Object>> listMap = ruleMapper.listMonitorRuleByUserLevelAndTriggerEvent(memberLevel, triggerEvent);
        if (listMap != null && listMap.size() > 0) {
            StringBuffer stringBuffer = new StringBuffer();
            Integer triggerTimes = 0;
            Integer triggerStageCycle = 0;
            for (Map m : listMap) {
                stringBuffer.append(MonitorTriggerEvent.getTypeByOrdinal(Integer.valueOf(m.get("execute_event").toString())).getCnName() + ",");
                triggerTimes = Integer.valueOf(m.get("trigger_times").toString());
                triggerStageCycle = Integer.valueOf(m.get("trigger_stage_cycle").toString());
            }
            expireOtcOrderDto.setAccessControl(stringBuffer.toString().substring(0, stringBuffer.toString().length() - 1));
            expireOtcOrderDto.setTimeRange(triggerStageCycle);
            expireOtcOrderDto.setTimeRangeOtcOrderCancle(triggerTimes);
        } else {
            expireOtcOrderDto.setAccessControl("");
            expireOtcOrderDto.setTimeRange(0);
            expireOtcOrderDto.setTimeRangeOtcOrderCancle(0);
        }
        return expireOtcOrderDto;
    }
}