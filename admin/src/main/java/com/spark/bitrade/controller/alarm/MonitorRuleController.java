package com.spark.bitrade.controller.alarm;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.dto.MonitorRuleConfigDto;
import com.spark.bitrade.service.MonitorRuleService;
import com.spark.bitrade.util.MessageRespResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fumy
 * @time 2018.11.02 11:15
 */
@Api(description = "权限冻结配置操作控制类",tags = "告警、监控")
@RestController
@RequestMapping("monitor")
public class MonitorRuleController extends BaseAdminController{

    @Autowired
    MonitorRuleService ruleService;


    @ApiOperation(value = "分页查询权限冻结管理配置",notes = "分页查询权限冻结管理配置")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码",name = "pageNo",dataType = "int"),
            @ApiImplicitParam(value = "每页条数",name = "pageSize",dataType = "int")
    })
    @GetMapping("/rule/config/page-query")
    @RequiresPermissions("monitor:setting-page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "分页查询权限冻结管理配置")
    public MessageRespResult<MonitorRuleConfigDto> page(int pageNo, int pageSize){
        PageInfo<MonitorRuleConfigDto> pageInfo = ruleService.findPage(pageNo,pageSize);
        return MessageRespResult.success("查询成功", PageData.toPageData(pageInfo));
    }

    @ApiOperation(value = "添加权限冻结管理配置",notes = "添加权限冻结管理配置")
    @PostMapping("/rule/config/add")
    @RequiresPermissions("monitor:setting-create")
    @AccessLog(module = AdminModule.SYSTEM, operation = "添加权限冻结管理配置")
    public MessageRespResult add(MonitorRuleConfigDto ruleConfigDto){
        String[] events = ruleConfigDto.getExecuteEvent().split(",");
        for(int i=0;i<events.length;i++){
            int executeEvent = Integer.parseInt(events[i]);
            //根据触发事件，触发次数、冻结权限查询、用户等级是否已经存在该条规则
            boolean isExist = ruleService.isExistRule(ruleConfigDto.getTriggerEvent().getOrdinal(),ruleConfigDto.getTriggerTimes(),executeEvent,
                    ruleConfigDto.getTriggerUserLevel().getOrdinal());
            if(isExist){
                //如果存在
                return MessageRespResult.error("不允许添加重复的规则配置");
            }
        }

        ruleService.save(ruleConfigDto);
        return MessageRespResult.success("操作成功");
    }

    @ApiOperation(value = "编辑权限冻结管理配置",notes = "编辑权限冻结管理配置")
    @PostMapping("/rule/config/edit")
    @RequiresPermissions("monitor:setting-edit")
    @AccessLog(module = AdminModule.SYSTEM, operation = "编辑权限冻结管理配置")
    public MessageRespResult edit(MonitorRuleConfigDto ruleConfigDto){
        String[] events = ruleConfigDto.getExecuteEvent().split(",");
        for(int i=0;i<events.length;i++){
            int executeEvent = Integer.parseInt(events[i]);
            //根据触发事件，触发次数、冻结权限查询、用户等级是否已经存在该条规则
            boolean isExist = ruleService.isExistRule(ruleConfigDto.getTriggerEvent().getOrdinal(),ruleConfigDto.getTriggerTimes(),executeEvent,
                    ruleConfigDto.getTriggerUserLevel().getOrdinal());
            if(isExist){
                //如果存在
                return MessageRespResult.error("不允许保存为已经存在的规则配置");
            }
        }
        ruleService.update(ruleConfigDto);
        return MessageRespResult.success("操作成功");
    }

    @ApiOperation(value = "删除权限冻结管理配置",notes = "删除权限冻结管理配置")
    @RequiresPermissions("monitor:setting-del")
    @ApiImplicitParam(value = "id数组，如：1,2,4",name = "ids",dataType = "Long[]")
    @GetMapping("/rule/config/del")
    @AccessLog(module = AdminModule.SYSTEM, operation = "删除权限冻结管理配置")
    public MessageRespResult delete(Long[] ids){
        ruleService.deleteById(ids);
        return MessageRespResult.success("操作成功");
    }
}
