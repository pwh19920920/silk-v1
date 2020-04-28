package com.spark.bitrade.controller.hqb;

import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.vo.AnnualRateOfWeekVO;
import com.spark.bitrade.service.impl.ILockHqbThousandsIncomeRecordService;
import com.spark.bitrade.util.MessageRespResult;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Date;
import java.util.List;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * 七日年化率接口
 *
 * @author dengdy
 * @time 2019/4/24
 */
@RestController
@Slf4j
public class AnnualRateOfWeekController {

    @Autowired
    private ILockHqbThousandsIncomeRecordService iLockHqbThousandsIncomeRecordService;

    @ApiOperation(value = "查询过去七天7日年化率", tags = "活期宝-v1.0")
    @ApiImplicitParam(name = "symbol", value = "币种", required = true)
    @PostMapping("hqb/getAnnualRateOfWeek")
    public MessageRespResult<AnnualRateOfWeekVO> getAnnualRateOfWeek(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user, String symbol) throws Exception{
        Assert.isTrue(symbol != null && !"".equals(symbol), "币种不能为空");
        AnnualRateOfWeekVO annualRateOfWeekVO = iLockHqbThousandsIncomeRecordService.
                getAnnualRateOfWeekVO(user.getId(), symbol, user.getPlatform(), new Date());
        return MessageRespResult.success("查询成功", annualRateOfWeekVO);
    }

    @ApiOperation(value = "查询最近一个月7日年化率Vo 列表", tags = "活期宝-v1.0")
    @ApiImplicitParam(name = "unit", value = "币种", required = true)
    @PostMapping("hqb/recentMonthAnnualRateOfWeek")
    public MessageRespResult<List<AnnualRateOfWeekVO>> recentMonthAnnualRateOfWeek(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user, String unit) throws Exception{
        Assert.isTrue(unit != null && !"".equals(unit) || "".equals(unit), "币种不能为空");
        List<AnnualRateOfWeekVO> annualRateOfWeekVOList = iLockHqbThousandsIncomeRecordService.
                getRecentMonthAnnualRateOfWeekVO(user.getId(), unit, user.getPlatform(), new Date());
        return MessageRespResult.success("查询成功", annualRateOfWeekVOList);
    }
}
