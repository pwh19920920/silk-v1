package com.spark.bitrade.controller.pay;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.AdvertiseType;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.dto.PayRecordDto;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.OtcCoin;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.OtcCoinService;
import com.spark.bitrade.service.OtcOrderService;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.vo.OtcOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 *  otc流水记录
 *
 * @author yangch
 * @time 2019.03.07 18:58
 */

@Api(description = "otc流水记录")
@RestController
@RequestMapping("/otc")
@Slf4j
public class OtcController {
    @Autowired
    private OtcOrderService otcOrderService;
    @Autowired
    private OtcCoinService otcCoinService;

    @ApiOperation(value = "otc流水记录")
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

        return flowRecord(unit, user.getId(), appId, pageNo, pageSize, startTime, endTime);
    }


    @ApiOperation(value = "otc流水记录API")
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
    public MessageRespResult<PageData<PayRecordDto>> flowRecord(String unit, Long memberId, Long appId, Integer pageNo, Integer pageSize,
                                        String startTime, String endTime){
        OtcCoin coin = otcCoinService.findByUnit(unit);
        PageInfo<OtcOrderVO> orderPageInfo =
                otcOrderService.findRecordByUidAndAppId(coin == null ? null : coin.getId(),memberId, appId, pageNo, pageSize, startTime, endTime);

        List<PayRecordDto> payRecordDtoList = new ArrayList<>();
        orderPageInfo.getList().forEach(order -> {
            OtcCoin otcCoin = otcCoinService.findOne(order.getCoinId());

            PayRecordDto payRecord = new PayRecordDto();
            payRecord.setId(order.getId().toString());
            payRecord.setMemberId(memberId);
            payRecord.setTotalAmount(order.getNumber());
            payRecord.setArriveAmount(order.getNumber());
            payRecord.setUnit(otcCoin.getUnit());
            payRecord.setFeeUnit(otcCoin.getUnit());

            payRecord.setCreateTime(order.getCreateTime());
            payRecord.setTradeUsername(order.getMemberRealName());
            //（交易对象）商家名称(注意：OTC流水记录包含商圈名称不合适)
            payRecord.setTradeBusinessName(order.getMemberRealName());
            payRecord.setTradeMemberId(order.getMemberId());
            payRecord.setUsername(order.getCustomerName());
            //商家名称(注意：OTC流水记录包含商圈名称不合适)
            payRecord.setBusinessname(order.getCustomerName());
            payRecord.setFee(BigDecimal.ZERO);

            //4法币
            payRecord.setTradeType(4);

            //0=已取消/1=未付款/2=已付款/3=已完成/4=申诉中
            payRecord.setStatus(order.getStatus().getOrdinal());
            payRecord.setToAddress("");
            payRecord.setFromAddress("");

            //0转入，1转出
            payRecord.setType(order.getAdvertiseType() == AdvertiseType.BUY ? 1: 0);

            payRecordDtoList.add(payRecord);
        });

        return MessageRespResult.success4Data(PageData.toPageData(orderPageInfo, payRecordDtoList));
    }
}
