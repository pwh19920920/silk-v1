package com.spark.bitrade.controller.hqb;

import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.impl.ILockHqbCoinSettgingService;
import com.spark.bitrade.service.impl.ILockHqbIncomeRecordService;
import com.spark.bitrade.service.impl.ILockHqbMemberWalletService;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.impl.ILockHqbThousandsIncomeRecordService;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.vo.AnnualRateOfWeekVO;
import com.spark.bitrade.vo.MemberGeneralInfoVO;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * 账户
 *
 * @author Zhang Yanjun
 * @time 2019.04.23 17:18
 */
@RestController
@Slf4j
public class AccountController {

    @Autowired
    private ILockHqbMemberWalletService iLockHqbMemberWalletService;
    @Autowired
    private ILockHqbIncomeRecordService iLockHqbIncomeRecordService;
    @Autowired
    private ILockHqbThousandsIncomeRecordService iLockHqbThousandsIncomeRecordService;
    @Autowired
    private ILockHqbCoinSettgingService iLockHqbCoinSettgingService;

    /**
     * 账户查询接口
     *
     * @param unit
     * @author Zhang Yanjun
     * @time 2019.04.24 10:46
     */
    @ApiOperation(value = "账户查询接口", tags = "活期宝-v1.0")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "unit", value = "币种", required = true),
    })
    @PostMapping("hqb/findHqbWallet")
    public MessageRespResult<LockHqbMemberWallet> findHqbWallet(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user,
                                                                String unit) {
        LockHqbMemberWallet lockHqbMemberWallet = iLockHqbMemberWalletService.findByAppIdAndUnitAndMemberId(user.getPlatform(), unit, user.getId());
        //判断账户不存在时，创建账户
//        if (lockHqbMemberWallet == null) {
//            lockHqbMemberWallet = new LockHqbMemberWallet();
//            lockHqbMemberWallet.setMemberId(user.getId());
////            lockHqbMemberWallet.setMemberId(74737l);
//            lockHqbMemberWallet.setAppId(user.getPlatform());
//            lockHqbMemberWallet.setCoinSymbol(unit);
//            lockHqbMemberWallet.setPlanInAmount(BigDecimal.ZERO);
//            lockHqbMemberWallet.setLockAmount(BigDecimal.ZERO);
//            lockHqbMemberWallet.setAccumulateIncome(BigDecimal.ZERO);
//            lockHqbMemberWallet.setAccumulateInAmount(BigDecimal.ZERO);
//            lockHqbMemberWallet.setAccumulateOutAmount(BigDecimal.ZERO);
//            iLockHqbMemberWalletService.clearWalletCache(user.getId(),user.getPlatform(),unit);
//            iLockHqbMemberWalletService.save(lockHqbMemberWallet);
//        }
        return MessageRespResult.success("查询成功", lockHqbMemberWallet);
    }

    @ApiOperation(value = "首页活动列表", tags = "活期宝-v1.0")
    @PostMapping("hqb/memberGeneralInfo")
    public MessageRespResult<List<MemberGeneralInfoVO>> memberGeneralInfo(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user, HttpServletRequest request) {

        //有效活动列表
        List<LockHqbCoinSettgingVo> settgings = iLockHqbCoinSettgingService.findValidSettingByAppId(user.getPlatform());
        //无效活动列表
        List<LockHqbCoinSettgingVo> invalidSettings = iLockHqbCoinSettgingService.findInvalidSettingByAppId(user.getPlatform());
        //用户参与的币种列表
        List<LockHqbCoinSettgingVo> joinSettings = iLockHqbCoinSettgingService.findJoinSetting(user.getPlatform(), user.getId());

        List<MemberGeneralInfoVO> list = new ArrayList();

        //有效活动列表
        String accept = request.getHeader("language");
        for (LockHqbCoinSettgingVo settging : settgings) {
            MemberGeneralInfoVO memberGeneralInfoVO = new MemberGeneralInfoVO();
            memberGeneralInfoVO.setStatus(1);
            if(accept == null || "zh_CN".equals(accept)){//简体中文
                settging.setAcitivityName(settging.getActivityNameCn());
            }else if("zh_HK".equals(accept)){//繁体中文
                settging.setAcitivityName(settging.getActivityNameZhTw());
            }else if("en_US".equals(accept)){//英文
                settging.setAcitivityName(settging.getActivityNameEn());
            }else if("ko_KR".equals(accept)){//韩文
                settging.setAcitivityName(settging.getActivityNameKo());
            }
            memberGeneralInfoVO = this.getMemberGeneralInfo(memberGeneralInfoVO, settging.getCoinSymbol(), settging.getAcitivityName(), user);
            list.add(memberGeneralInfoVO);
        }

        //失效的活动列表，如果用户参与，就add
        for (LockHqbCoinSettgingVo invalidSetting : invalidSettings) {
            String unit = invalidSetting.getCoinSymbol();
            for (LockHqbCoinSettgingVo joinSettging : joinSettings) {
                if (joinSettging.getCoinSymbol().equals(unit)) {
                    MemberGeneralInfoVO memberGeneralInfoVO = new MemberGeneralInfoVO();
                    memberGeneralInfoVO.setStatus(2);
                    if(accept == null || "zh_CN".equals(accept)){//简体中文
                        joinSettging.setAcitivityName(joinSettging.getActivityNameCn());
                    }else if("zh_HK".equals(accept)){//繁体中文
                        joinSettging.setAcitivityName(joinSettging.getActivityNameZhTw());
                    }else if("en_US".equals(accept)){//英文
                        joinSettging.setAcitivityName(joinSettging.getActivityNameEn());
                    }else if("ko_KR".equals(accept)){//韩文
                        joinSettging.setAcitivityName(joinSettging.getActivityNameKo());
                    }
                    memberGeneralInfoVO = this.getMemberGeneralInfo(memberGeneralInfoVO, unit, joinSettging.getAcitivityName(), user);
                    list.add(memberGeneralInfoVO);
                }
            }
        }

        return MessageRespResult.success("查询成功", list);
    }


    /**
     * 首页返回数据处理
     *
     * @param memberGeneralInfoVO
     * @param unit
     * @param activityName
     * @param user
     * @return
     */
    private MemberGeneralInfoVO getMemberGeneralInfo(MemberGeneralInfoVO memberGeneralInfoVO, String unit, String activityName, @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        memberGeneralInfoVO.setAcitivityName(activityName);
        memberGeneralInfoVO.setUnit(unit);
        //昨日收益
        List<LockHqbIncomeRecord> records = iLockHqbIncomeRecordService.findByMemberIdAndAppIdAndUnitLimitBy(user.getId(),
                user.getPlatform(), unit, 1);
        BigDecimal yeterdayIncome = records.size() == 0 ? BigDecimal.ZERO : yesterIncome(records.get(0));

        memberGeneralInfoVO.setYesterdayIncome(yeterdayIncome);
        //活期宝账户
        MessageRespResult result = this.findHqbWallet(user, unit);
        LockHqbMemberWallet lockHqbMemberWallet = (LockHqbMemberWallet) result.getData();
        //账户总额
        BigDecimal amount = lockHqbMemberWallet.getPlanInAmount().add(lockHqbMemberWallet.getLockAmount());
        memberGeneralInfoVO.setAmount(amount);
        //累计收益
        memberGeneralInfoVO.setAccumulateIncome(lockHqbMemberWallet.getAccumulateIncome());
        //万份收益
        LockHqbThousandsIncomeRecord thousandsIncome = iLockHqbThousandsIncomeRecordService.yesterdayThousandsIncome(user.getPlatform(),
                unit, null);
        memberGeneralInfoVO.setThousandsIncome(thousandsIncome == null ? BigDecimal.ZERO : thousandsIncome.getTenThousandIncome().multiply(BigDecimal.valueOf(10000)));
        //7日年化率
        AnnualRateOfWeekVO annualRateOfWeekVO = iLockHqbThousandsIncomeRecordService.getAnnualRateOfWeekVO(user.getId(), unit,
                user.getPlatform(), new Date());
        memberGeneralInfoVO.setAnnualRateOfWeek(annualRateOfWeekVO == null ? null : annualRateOfWeekVO.getAnnualRateOfWeek());
        return memberGeneralInfoVO;
    }

    /**
     * 昨日收益
     * @param record
     * @return
     */
    private BigDecimal yesterIncome(LockHqbIncomeRecord record){
        if(record==null){
            return BigDecimal.ZERO;
        }
        Long createTime = record.getCreateTime();
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)-1,0,0,0);
        long tt = calendar.getTime().getTime();
        if(createTime>=tt){
            return record.getIncomeAmount();
        }
        return BigDecimal.ZERO;
    }

}
