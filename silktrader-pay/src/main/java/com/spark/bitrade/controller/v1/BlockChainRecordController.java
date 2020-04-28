package com.spark.bitrade.controller.v1;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.dto.PayRecordDto;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.TransferSilubium;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.service.TransferSilubiumService;
import com.spark.bitrade.util.MessageRespResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 *  区块链流水记录
 *
 * @author yangch
 * @time 2019.03.07 18:58
 */

@Api(description = "区块链流水记录")
@RestController
@RequestMapping("/blockchain")
@Slf4j
public class BlockChainRecordController {
    @Autowired
    private TransferSilubiumService transferSilubiumService;
    @Autowired
    private MemberService memberService;

    @ApiOperation(value = "区块链流水记录")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "币种", name = "unit"),
            @ApiImplicitParam(value = "开始时间，可选", name = "startTime"),
            @ApiImplicitParam(value = "结束时间，可选", name = "endTime"),
            @ApiImplicitParam(value = "页码", name = "pageNo", required = true),
            @ApiImplicitParam(value = "页大小", name = "pageSize", required = true)
    })
    @PostMapping("flowRecord")
    public MessageRespResult<PageData<PayRecordDto>> flowRecord(@SessionAttribute(SESSION_MEMBER) AuthMember user,String unit, Integer pageNo, Integer pageSize,
                                                                   String startTime, String endTime){
        Long appId = null;
        try {
            appId = Long.parseLong(user.getPlatform());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return flowRecord(unit,user.getId(), appId, pageNo, pageSize, startTime, endTime);
    }


    @ApiOperation(value = "区块链流水记录API")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "币种", name = "unit"),
            @ApiImplicitParam(value = "会员ID",name = "memberId",dataType = "Long"),
            @ApiImplicitParam(value = "应用ID",name = "appId",dataType = "Long"),
            @ApiImplicitParam(value = "页码",name = "pageNo",required = true),
            @ApiImplicitParam(value = "页大小",name = "pageSize",required = true),
            @ApiImplicitParam(value = "开始时间，可选",name = "startTime"),
            @ApiImplicitParam(value = "截至时间，可选",name = "endTime"),
    })
    @RequestMapping(value = "/api/flowRecord", method={RequestMethod.GET, RequestMethod.POST})
    public MessageRespResult<PageData<PayRecordDto>> flowRecord(String unit,Long memberId, Long appId, Integer pageNo, Integer pageSize,
                                        String startTime, String endTime){
        PageInfo<TransferSilubium> pageInfo =
                transferSilubiumService.findRecordByUidAndAppId(unit,memberId, appId, pageNo, pageSize, startTime, endTime);

        Member member = memberService.findOne(memberId);

        // todo 暂时仅提供 资金划转的记录（资金划转的时候交易双方都为用户自己）
        List<PayRecordDto> payRecordDtoList = new ArrayList<>();
        pageInfo.getList().forEach(order -> {
            PayRecordDto payRecord = new PayRecordDto();
            payRecord.setId(order.getId().toString());
            payRecord.setMemberId(memberId);
            payRecord.setTotalAmount(order.getTotalAmount());
            payRecord.setArriveAmount(order.getArriveAmount());
            payRecord.setUnit(order.getCoinUnit());
            payRecord.setFeeUnit(order.getCoinUnit());

            payRecord.setCreateTime(order.getCreateTime());
            payRecord.setTradeUsername(member.getRealName());
            //（交易对象）商家名称
            payRecord.setTradeBusinessName(member.getUsername());
            payRecord.setTradeMemberId(memberId);
            payRecord.setUsername(member.getRealName());
            //商家名称
            payRecord.setBusinessname(member.getUsername());
            payRecord.setFee(order.getFee());

            //5资产划转
            payRecord.setTradeType(5);

            //0 发起转账，1 转账成功，2 转账失败
            payRecord.setStatus(order.getTransferStatus());
            payRecord.setToAddress(order.getToAddress());
            payRecord.setFromAddress(order.getFromAddress());

            //0转入，1转出
            payRecord.setType(order.getTransferType());

            payRecordDtoList.add(payRecord);
        });

        return MessageRespResult.success4Data(PageData.toPageData(pageInfo, payRecordDtoList));
    }
}
