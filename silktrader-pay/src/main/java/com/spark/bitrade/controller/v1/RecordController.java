package com.spark.bitrade.controller.v1;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.PayTransferType;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.dto.PayRecordDto;
import com.spark.bitrade.entity.PayFastRecord;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.enums.MessageCode;
import com.spark.bitrade.service.IPayFastRecordService;
import com.spark.bitrade.util.MessageRespResult;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static com.spark.bitrade.util.MessageRespResult.success;
import static com.spark.bitrade.util.MessageRespResult.success4Data;

/**
 * 交易记录控制器
 * @author Zhang Yanjun
 * @time 2019.03.14 18:03
 */
@RestController
@Slf4j
public class RecordController {

    @Autowired
    private IPayFastRecordService iPayFastRecordService;

    /**
      * 根据id查询快速转账流水
      * @author Zhang Yanjun
      * @time 2019.03.15 14:49
      * @param id 流水id
      */
    @PostMapping("api/fastRecord")
    public MessageRespResult fastRecord(Long id){
        if (id == null || "".equals(id)){
            return MessageRespResult.error(MessageCode.REQUIRED_PARAMETER);
        }
        PayFastRecord payFastRecord = iPayFastRecordService.selectById(id);
        return MessageRespResult.success4Data(payFastRecord);
    }

    @ApiOperation(value = "云端流水记录")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "交易类型(1云端支付-（云端钱包<->云端钱包），2兑换，3支付码支付) 为空查全部", name = "transferType"),
            @ApiImplicitParam(value = "开始时间", name = "startTime"),
            @ApiImplicitParam(value = "结束时间", name = "endTime"),
            @ApiImplicitParam(value = "页码", name = "pageNo", required = true),
            @ApiImplicitParam(value = "页大小", name = "pageSize", required = true),
            @ApiImplicitParam(value = "转账方用户id", name = "fromId"),
            @ApiImplicitParam(value = "转账方手机号（模糊查询）", name = "fromPhone"),
            @ApiImplicitParam(value = "转账方应用id", name = "fromAppid", required = true),
            @ApiImplicitParam(value = "收款方用户id", name = "toId"),
            @ApiImplicitParam(value = "收款方手机号（模糊查询）", name = "toPhone"),
            @ApiImplicitParam(value = "收款方应用id", name = "toAppid", required = true),
    })
    @PostMapping("account/cloudRecord")
    public MessageRespResult<PageData<PayFastRecord>> cloudRecord(int pageNo, int pageSize, PayTransferType transferType, String startTime, String endTime,
                                                                  Long fromId, Long toId, String fromPhone, String toPhone, String fromAppid, String toAppid) {
        PageInfo<PayFastRecord> pageInfo = iPayFastRecordService.findlist(pageNo, pageSize, transferType, startTime, endTime,
                fromId, toId, fromPhone, toPhone, fromAppid, toAppid);
        return success("查询成功", PageData.toPageData(pageInfo));
    }

    @ApiOperation(value = "云端流水记录")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "币种", name = "unit"),
            @ApiImplicitParam(value = "开始时间", name = "startTime"),
            @ApiImplicitParam(value = "结束时间", name = "endTime"),
            @ApiImplicitParam(value = "页码", name = "pageNo", required = true),
            @ApiImplicitParam(value = "页大小", name = "pageSize", required = true)
    })
    @PostMapping("account/getFastRecord")
    public MessageRespResult<PageData<PayRecordDto>> getFastRecord(@SessionAttribute(SESSION_MEMBER) AuthMember user, String unit, int pageNo, int pageSize,
                                                                   String startTime, String endTime) {
        PageInfo<PayFastRecord> pageInfo = iPayFastRecordService.findFastRecord(unit,pageNo, pageSize, user.getId(), user.getPlatform(),startTime, endTime);
        List<PayFastRecord> fastRecordList = pageInfo.getList();
        List<PayRecordDto> payRecordDtoList = iPayFastRecordService.getFastRecord(user.getId(), user.getPlatform(),fastRecordList);
        return success4Data(PageData.toPageData(pageInfo,payRecordDtoList));
    }

}
