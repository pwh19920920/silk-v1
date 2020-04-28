package com.spark.bitrade.controller.alarm;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.dto.MemberCancelDTO;
import com.spark.bitrade.service.MonitorRuleService;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.vo.MemberAppealCountVo;
import com.spark.bitrade.vo.MemberOrderCancelVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Zhang Yanjun
 * @time 2018.11.02 14:30
 */
@Api(tags = "告警、监控")
@RestController
@RequestMapping("order-cancel")
@Slf4j
public class MemberOrderCancelController {

    @Autowired
    private MonitorRuleService monitorRuleService;

    @ApiOperation(value = "用户取消记录",notes = "用户取消记录")
    @PostMapping("detail")
    @RequiresPermissions("monitor:order-cancel-detail-page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "分页查询用户取消记录")
    public MessageRespResult<MemberCancelDTO> detail(MemberOrderCancelVO memberOrderCancelVO, int pageNo, int pageSize){
        PageInfo<MemberCancelDTO> pageInfo=monitorRuleService.getMemberCancelDetails(memberOrderCancelVO,pageNo,pageSize);
        return MessageRespResult.success("查询成功", PageData.toPageData(pageInfo));
    }

    @ApiOperation(value = "用户取消次数统计",notes = "用户取消次数统计")
    @PostMapping("count")
    @RequiresPermissions("monitor:order-cancel-count-page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "用户取消次数统计")
    public MessageRespResult count(MemberOrderCancelVO memberOrderCancelVO , int pageNo , int pageSize){
        PageInfo<Map<String,Object>> pageInfo=monitorRuleService.getMemberCancelCount(memberOrderCancelVO, pageNo, pageSize);
        return MessageRespResult.success("查询成功",PageData.toPageData(pageInfo));
    }

    @ApiOperation(value = "用户申诉次数统计",notes = "用户申诉次数统计")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "条件字段",name = "whereName",dataType = "String"),
            @ApiImplicitParam(value = "排序字段",name = "orderName",dataType = "String"),
            @ApiImplicitParam(value = "排序方式，ASC/DESC",name = "sort",dataType = "String"),
            @ApiImplicitParam(value = "申诉时间-开始时间",name = "startTime",dataType = "String"),
            @ApiImplicitParam(value = "申诉时间-结束时间",name = "endTime",dataType = "String"),
            @ApiImplicitParam(value = "订单创建时间-开始时间",name = "orderStartTime",dataType = "String"),
            @ApiImplicitParam(value = "订单创建时间-结束时间",name = "orderEndTime",dataType = "String"),
            @ApiImplicitParam(value = "页码",name = "pageNo",dataType = "int"),
            @ApiImplicitParam(value = "每页条数",name = "pageSize",dataType = "int")
    })
    @PostMapping("appeal/count")
    @RequiresPermissions("monitor:appeal-statistics-page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "用户申诉次数统计")
    public MessageRespResult<MemberAppealCountVo> appealCount(String whereName,String orderName,String sort,String startTime,String endTime,
                                                              String orderStartTime,String orderEndTime,int pageNo , int pageSize){
        PageInfo<MemberAppealCountVo> pageInfo=monitorRuleService.findMemberAppealCount(whereName,orderName,sort,startTime,endTime,
                                                                                        orderStartTime,orderEndTime,pageNo,pageSize);
        return MessageRespResult.success("查询成功",PageData.toPageData(pageInfo));
    }
}
