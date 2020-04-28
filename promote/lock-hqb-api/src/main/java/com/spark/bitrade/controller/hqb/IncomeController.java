package com.spark.bitrade.controller.hqb;

import com.spark.bitrade.entity.LockHqbIncomeRecord;
import com.spark.bitrade.entity.LockHqbMemberWallet;
import com.spark.bitrade.entity.LockHqbThousandsIncomeRecord;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.impl.ILockHqbIncomeRecordService;
import com.spark.bitrade.service.impl.ILockHqbMemberWalletService;
import com.spark.bitrade.service.impl.ILockHqbThousandsIncomeRecordService;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageRespResult;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * 收益
 *
 * @author Zhang Yanjun
 * @time 2019.04.23 17:11
 */
@RestController
@Slf4j
public class IncomeController {

    @Autowired
    private ILockHqbIncomeRecordService iLockHqbIncomeRecordService;

    @Autowired
    private ILockHqbThousandsIncomeRecordService iLockHqbThousandsIncomeRecordService;

    @Autowired
    private ILockHqbMemberWalletService iLockHqbMemberWalletService;

    /**
     * 用户累计收益
     *
     * @param unit
     * @author Zhang Yanjun
     * @time 2019.04.24 10:49
     */
    @ApiOperation(value = "用户累计收益", tags = "活期宝-v1.0")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "unit", value = "币种", required = true),
    })
    @PostMapping("hqb/accumulateIncome")
    public MessageRespResult<BigDecimal> accumulateIncome(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user, String unit) {
        LockHqbMemberWallet lockHqbMemberWallet = iLockHqbMemberWalletService.findByAppIdAndUnitAndMemberId(user.getPlatform(),
                unit, user.getId());
        return MessageRespResult.success("查询成功", lockHqbMemberWallet.getAccumulateIncome());
    }


    /**
     * 昨日收益接口
     *
     * @param unit
     * @author Zhang Yanjun
     * @time 2019.04.24 10:49
     */
    @ApiOperation(value = "昨日收益接口", tags = "活期宝-v1.0")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "unit", value = "币种", required = true)
    })
    @PostMapping("hqb/yesterdayIncome")
    public MessageRespResult<List<LockHqbIncomeRecord>> yesterdayIncome(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user,
                                                                        String unit) {
        List<LockHqbIncomeRecord> records = iLockHqbIncomeRecordService.findByMemberIdAndAppIdAndUnitLimitBy(user.getId(), user.getPlatform(), unit, 1);
//        //如果记录中的最新一条数据为当天，则不显示
//        if (records.size() > 0) {
//            //当前时间
//            String newDay = DateUtil.getDateYMD();
//            //记录中存入的最新时间
//            Long createTime = records.get(0).getCreateTime();
//
////            String create = DateUtil.stampToDate(createTime.toString());
////            String createDay = DateUtil.getDateYMD(DateUtil.strToDate(create));
//            Date date = new Date(createTime);
//            String createDay = DateUtil.getDateYMD(date);
//
//            if (createDay.equals(newDay)) {
//                records = iLockHqbIncomeRecordService.findByMemberIdAndAppIdAndUnitLimitBy(user.getId(), user.getPlatform(), unit, 2);
//                records.remove(0);
//            }
//        }
        return MessageRespResult.success("查询成功", records);
    }

    /**
     * 最近30天按天的历史收益
     *
     * @param unit
     * @author Zhang Yanjun
     * @time 2019.04.24 10:50
     */
    @ApiOperation(value = "最近30天按天的历史收益", tags = "活期宝-v1.0")
    @ApiImplicitParam(name = "unit", value = "币种", required = true)
    @PostMapping("hqb/lastMonthIncome")
    public MessageRespResult<List<LockHqbIncomeRecord>> lastMonthIncome(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user,
                                                                        String unit) {
        List<LockHqbIncomeRecord> records = iLockHqbIncomeRecordService.findByMemberIdAndAppIdAndUnitLimitBy(user.getId(),
                user.getPlatform(), unit, 30);
        //如果记录中的最新一条数据为当天，则不显示
//        if (records.size() > 0) {
//            //当前时间
//            String newDay = DateUtil.getDateYMD();
//            //记录中存入的最新时间
//            Long createTime = records.get(0).getCreateTime();
//            Date date = new Date(createTime);
//            String createDay = DateUtil.getDateYMD(date);
////            String create = DateUtil.stampToDate(createTime.toString());
////            String createDay = DateUtil.getDateYMD(DateUtil.strToDate(create));
//
//            if (createDay.equals(newDay)) {
//                records = iLockHqbIncomeRecordService.findByMemberIdAndAppIdAndUnitLimitBy(user.getId(), user.getPlatform(), unit, 31);
//                records.remove(0);
//            }
//        }
        return MessageRespResult.success("查询成功", records);
    }

    /**
     * 昨日万份收益接口
     *
     * @param unit
     * @param appId
     * @param time
     * @author Zhang Yanjun
     * @time 2019.04.24 11:36
     */
    @ApiOperation(value = "昨日万份收益", tags = "活期宝-v1.0")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "unit", value = "币种", required = true),
            @ApiImplicitParam(name = "appId", value = "应用或渠道ID", required = true),
            @ApiImplicitParam(name = "time", value = "日期（为空，则取最新的一条数据）")
    })
    @PostMapping("hqb/yesterdayThousandsIncome")
    public MessageRespResult<LockHqbThousandsIncomeRecord> lastMonthIncome(String unit, String appId, Integer time) {
        LockHqbThousandsIncomeRecord lockHqbThousandsIncomeRecord = iLockHqbThousandsIncomeRecordService.yesterdayThousandsIncome(appId, unit, time);
        //如果记录中的最新一条数据为当天，则不显示
        //当前日期
        String newDay = DateUtil.getDateYMD(new Date());
        if (lockHqbThousandsIncomeRecord != null && lockHqbThousandsIncomeRecord.getOpTime().toString().equals(newDay)) {
            time = Integer.parseInt(newDay) - 1;
            lockHqbThousandsIncomeRecord = iLockHqbThousandsIncomeRecordService.yesterdayThousandsIncome(appId, unit, time);
        }

        lockHqbThousandsIncomeRecord.setTenThousandIncome(lockHqbThousandsIncomeRecord.getTenThousandIncome().multiply(BigDecimal.valueOf(10000)));
        String opTime = lockHqbThousandsIncomeRecord.getOpTime().toString();
        Date opTime1 = DateUtil.parseDate(opTime,"yyyyMMdd");
        lockHqbThousandsIncomeRecord.setOpTime(opTime1.getTime());
        return MessageRespResult.success("查询成功", lockHqbThousandsIncomeRecord);
    }

    @ApiOperation(value = "最近一个月的万份收益(30天)", tags = "活期宝-v1.0")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "unit", value = "币种", required = true),
            @ApiImplicitParam(name = "appId", value = "应用或渠道ID", required = true)
    })
    @PostMapping("hqb/lastMonthThousandsIncome")
    public MessageRespResult<List<LockHqbThousandsIncomeRecord>> lastMonthThousandsIncome(String unit, String appId) throws ParseException {
        List<LockHqbThousandsIncomeRecord> list = iLockHqbThousandsIncomeRecordService.lastMonthThousandsIncome(appId, unit, 30);
        //如果记录中的最新一条数据为当天，则不显示
        //当前日期
        String newDay = DateUtil.getDateYMD(new Date());

        if (list.size() > 0 && list.get(0).getOpTime().toString().equals(newDay)) {
            list = iLockHqbThousandsIncomeRecordService.lastMonthThousandsIncome(appId, unit, 31);
            list.remove(0);
        }
        List<LockHqbThousandsIncomeRecord> list1 = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            LockHqbThousandsIncomeRecord record = list.get(i);
            BigDecimal income = record.getTenThousandIncome().multiply(BigDecimal.valueOf(10000));
            record.setTenThousandIncome(income);
            String opTime = record.getOpTime().toString();
            Date time = DateUtil.parseDate(opTime,"yyyyMMdd");
            record.setOpTime(time.getTime());

            list1.add(record);
        }
        return MessageRespResult.success("查询成功", list1);
    }
}
