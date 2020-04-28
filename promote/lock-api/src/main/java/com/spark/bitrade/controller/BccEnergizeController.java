package com.spark.bitrade.controller;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.ActivitieType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.LockType;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.PriceUtil;
import com.spark.bitrade.vo.LockActivitySettingBuilder;
import com.spark.bitrade.vo.LockBccLockedInfoVo;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static org.springframework.util.Assert.*;

@RestController
@RequestMapping("/bccEnergize")
@Slf4j
public class BccEnergizeController {

    @Autowired
    private ISilkDataDistService silkDataDistService;

    @Autowired
    private LockCoinActivitieSettingService lockCoinActivitieSettingService;

    @Autowired
    private LockCoinActivitieProjectService lockCoinActivitieProjectService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private LockBccAssignRecordService lockBccAssignRecordService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LockCoinDetailMybatisService lockCoinDetailMybatisService;

    @Autowired
    private LockBccAssignUnlockService lockBccAssignUnlockService;

    @Autowired
    private LockCoinDetailService lockCoinDetailService;

    /**
     * BCC赋能计划活动配置
     */
    @ApiOperation(value = "BCC赋能计划活动配置", tags = "BCC赋能-v1.0锁仓")
    @PostMapping("/bccEnergizeActivitySetting")
    public MessageRespResult ieoBccActivitySetting(Long memberId) {
        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("BCC_ENERGIZE", "SETTING_ID");
        SilkDataDist symbolDist = silkDataDistService.findByIdAndKey("BCC_ENERGIZE", "BASE_SYMBOL");
        if (dataDist != null && dataDist.getStatus() == BooleanEnum.IS_TRUE && symbolDist != null && symbolDist.getStatus() == BooleanEnum.IS_TRUE) {
            long settingId = ConvertUtils.lookup(Long.class).convert(Long.class, dataDist.getDictVal());
            LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOne(settingId);
            LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(lockCoinActivitieSetting.getActivitieId());
            LockActivitySettingBuilder lockActivitySettingBuilder = LockActivitySettingBuilder.builder()
                    .id(lockCoinActivitieSetting.getId())
                    .name(lockCoinActivitieSetting.getName())
                    .note(lockCoinActivitieSetting.getNote())
                    .boughtAmount(lockCoinActivitieSetting.getBoughtAmount())
                    .earningRate(lockCoinActivitieSetting.getEarningRate())
                    .startTime(lockCoinActivitieSetting.getStartTime())
                    .endTime(lockCoinActivitieSetting.getEndTime())
                    .maxBuyAmount(lockCoinActivitieSetting.getMaxBuyAmount())
                    .minBuyAmount(lockCoinActivitieSetting.getMinBuyAmount())
                    .planAmount(lockCoinActivitieSetting.getPlanAmount())
                    .baseSymbol(symbolDist.getDictVal())
                    .symbol(lockCoinActivitieSetting.getCoinSymbol())
                    .unitPerAmount(lockCoinActivitieProject.getUnitPerAmount())
                    .cycle(lockCoinActivitieSetting.getLockCycle())
                    .lockDays(lockCoinActivitieSetting.getLockDays())
                    .cycleDays(lockCoinActivitieSetting.getCycleDays())
                    .beginDays(lockCoinActivitieSetting.getBeginDays())
                    .cycleRatio(lockCoinActivitieSetting.getCycleRatio())
                    .maxPurchase(lockCoinActivitieSetting.getMaxBuyAmount()
                            .divide(lockCoinActivitieSetting.getUnitPerAmount(),0,BigDecimal.ROUND_UP).intValue())
                    .build();
            lockActivitySettingBuilder.setNowTime(new Date());
            //判断当前是何种语言环境
            String accept = null;
            try {
                Class holderClass = Class.forName("org.springframework.web.context.request.RequestContextHolder");
                Method currentRequestAttributes = ReflectionUtils.findMethod(holderClass, "currentRequestAttributes");
                Object requestAttributes = ReflectionUtils.invokeMethod(currentRequestAttributes, null);
                Method request = ReflectionUtils.findMethod(requestAttributes.getClass(), "getRequest");
                HttpServletRequest httpServletRequest = (HttpServletRequest) ReflectionUtils.invokeMethod(request, requestAttributes);
                accept = httpServletRequest.getHeader("language");
            } catch (Exception e) {
                log.error("获取请求头的异常", e);
            }
            if(accept == null || "zh_CN".equals(accept)){//简体中文
                lockActivitySettingBuilder.setImgUrl(lockCoinActivitieProject.getTitleImg());
            }else if("zh_HK".equals(accept)){//繁体中文
                LockCoinActivitieSettingInternational international = lockCoinDetailService.selectInternationalSetting(lockActivitySettingBuilder.getId(),1);
                LockCoinActivitieProjectInternational selectInternational = lockCoinDetailService.selectInternational(lockCoinActivitieProject.getId(),1);
                if(null != international){
                    lockActivitySettingBuilder.setName(international.getName());
                    lockActivitySettingBuilder.setNote(international.getNote());
                }
                if(null != selectInternational){
                    lockActivitySettingBuilder.setImgUrl(selectInternational.getTitleImg());
                }

            }else if("en_US".equals(accept)){//英文
                LockCoinActivitieSettingInternational international = lockCoinDetailService.selectInternationalSetting(lockActivitySettingBuilder.getId(),2);
                LockCoinActivitieProjectInternational selectInternational = lockCoinDetailService.selectInternational(lockCoinActivitieProject.getId(),2);
                if(null != international){
                    lockActivitySettingBuilder.setName(international.getName());
                    lockActivitySettingBuilder.setNote(international.getNote());
                }
                if(null != selectInternational){
                    lockActivitySettingBuilder.setImgUrl(selectInternational.getTitleImg());
                }
            }else if("ko_KR".equals(accept)){//韩文
                LockCoinActivitieSettingInternational international = lockCoinDetailService.selectInternationalSetting(lockActivitySettingBuilder.getId(),3);
                LockCoinActivitieProjectInternational selectInternational = lockCoinDetailService.selectInternational(lockCoinActivitieProject.getId(),3);
                if(null != international){
                    lockActivitySettingBuilder.setName(international.getName());
                    lockActivitySettingBuilder.setNote(international.getNote());
                }
                if(null != selectInternational){
                    lockActivitySettingBuilder.setImgUrl(selectInternational.getTitleImg());
                }
            }
            //判断活动是否过期
            if(lockCoinActivitieProject.getStatus().getOrdinal() == 2){
                lockActivitySettingBuilder.setIsOverdue(1);
            }else {
                if(lockCoinActivitieProject.getStartTime().compareTo(new Date()) > 0){
                    lockActivitySettingBuilder.setIsOverdue(2);
                }else if(lockCoinActivitieProject.getEndTime().compareTo(new Date()) < 0){
                    lockActivitySettingBuilder.setIsOverdue(1);
                    if(null != memberId){
                        List<LockCoinDetail> detailList = lockCoinActivitieProjectService.findByMemberId(memberId,lockCoinActivitieSetting.getId());
                        if(detailList.size() == 0){
                            MessageRespResult messageRespResult = MessageRespResult.success();
                            //messageRespResult.setData(Collections.singletonList(lockActivitySettingBuilder));
                            return messageRespResult;
                        }
                    }else{
                        MessageRespResult messageRespResult = MessageRespResult.success();
                        //messageRespResult.setData(Collections.singletonList(lockActivitySettingBuilder));
                        return messageRespResult;
                    }
                }else{
                    lockActivitySettingBuilder.setIsOverdue(0);
                }
            }
            MessageRespResult mr = MessageRespResult.success();
            mr.setData(Collections.singletonList(lockActivitySettingBuilder));
            return mr;
        } else {
            return MessageRespResult.error("获取配置失败");
        }
    }

    /**
     * BCC赋能计划锁仓接口
     */
    @ApiOperation(value = "BCC赋能计划锁仓接口", tags = "BCC赋能-v1.0锁仓")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "锁仓数量", name = "amount", dataType = "BigDecimal", required = true),
            @ApiImplicitParam(value = "交易密码", name = "password", dataType = "String", required = true),
            @ApiImplicitParam(value = "锁仓份数", name = "portion", dataType = "Integer", required = true)
    })
    @PostMapping("/lock")
    public MessageRespResult lock(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user,
                                   BigDecimal amount, String password, Integer portion) {
        //验证资金密码
        hasText(password, msService.getMessage("MISSING_JYPASSWORD"));
        Member member = memberService.findOne(user.getId());

        String mbPassword = member.getJyPassword();
        String jyPass = new SimpleHash("md5", password, member.getSalt(), 2).toHex().toLowerCase();
        hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));

        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("BCC_ENERGIZE", "SETTING_ID");
        isTrue(dataDist != null && dataDist.getStatus() == BooleanEnum.IS_TRUE, "活动配置无效");


        long activityId = ConvertUtils.lookup(Long.class).convert(Long.class, dataDist.getDictVal());

        LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOneByTimeWithoutCache(activityId);
        notNull(lockCoinActivitieSetting, "有效BCC赋能计划活动不存在");
        LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(lockCoinActivitieSetting.getActivitieId());
        notNull(lockCoinActivitieProject, "有效活动方案不存在");
        isTrue(lockCoinActivitieProject.getType() == ActivitieType.ENERGIZE, "活动类型错误");

        // 开始锁仓
        PriceUtil priceUtil = new PriceUtil();
        //获取锁仓币种人民币价格
        BigDecimal coinCnyPrice = priceUtil.getCoinCnyPrice(restTemplate, lockCoinActivitieSetting.getCoinSymbol());
        //获取锁仓币种USDT价格
        BigDecimal coinUSDTPrice = priceUtil.getCoinCnyPrice(restTemplate, lockCoinActivitieSetting.getCoinSymbol());
        //获取USDT的人民币价格
        BigDecimal usdtPrice = priceUtil.getUSDTPrice(restTemplate);

        // 开始锁仓
        lockBccAssignRecordService.bccLock(lockCoinActivitieSetting, member, coinCnyPrice, coinUSDTPrice, usdtPrice, amount, portion);

        return MessageRespResult.success("锁仓成功");
    }

    /**
      * 获取用户的所有锁仓记录
      * @author fatKarin
      * @time 2019/6/2415:12 
      */
    @ApiOperation(value = "获取用户BCC赋能计划 bcc币种的锁仓记录", tags = "BCC赋能-v1.0锁仓")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码（1开始）",name = "pageNo",required = true),
            @ApiImplicitParam(value = "页大小",name = "pageSize",required = true),
            @ApiImplicitParam(value = "锁仓类型（0商家保证金，1手动锁仓，2锁仓活动，3理财锁仓，4SLB节点产品，5STO锁仓，6STO增值计划,7 ieo,8 金钥匙活动，9 赋能计划）",name = "lockType")
    })
    @PostMapping("/restitutionRecords")
    public MessageRespResult<PageInfo<LockCoinDetailBuilder>> restitutionRecords(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user, Integer pageNo, Integer pageSize, @RequestParam LockType lockType) {
        Assert.notNull(lockType, msService.getMessage("DATA_ILLEGAL"));
        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("BCC_ENERGIZE", "SETTING_ID");
        Assert.notNull(dataDist, "找不到活动配置");
        long settingId = ConvertUtils.lookup(Long.class).convert(Long.class, dataDist.getDictVal());
        LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOne(settingId);
        Assert.notNull(lockCoinActivitieSetting, "找不到活动配置");
        PageInfo<LockCoinDetailBuilder> page = lockCoinDetailMybatisService.queryPageByMemberAndTypeAndSymbol(user.getId(), lockType.getOrdinal(), pageNo, pageSize,lockCoinActivitieSetting.getCoinSymbol());
        MessageRespResult mr = MessageRespResult.success("success");
        mr.setData(page);
        return mr;
    }
    /**
      * 分页用户的解仓仓记录
      * @author fatKarin
      * @time 2019/6/24 15:12 
      */
    @ApiOperation(value = "获取用户BCC赋能计划 bcc币种的解仓记录", tags = "BCC赋能-v1.0锁仓")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码（1开始）",name = "pageNo",required = true),
            @ApiImplicitParam(value = "页大小",name = "pageSize",required = true)
    })
    @PostMapping("/getUnlockRecords")
    public MessageRespResult<PageInfo<LockBccAssignUnlock>> getUnlockRecords(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user, Integer pageNo, Integer pageSize) {
        PageInfo page = lockBccAssignUnlockService.findLockBccAssignUnlocksForPage(user.getId(), pageNo, pageSize);
        MessageRespResult mr = MessageRespResult.success("success");
        mr.setData(page);
        return mr;
    }

    /**
      * 获取用户锁仓信息
      * @author fatKarin
      * @time 2019/6/24 15:12 
      */
    @ApiOperation(value = "用户BCC赋能计划锁仓信息", tags = "BCC赋能-v1.0锁仓")
    @PostMapping("/getLockBccLockedInfo")
    public MessageRespResult<LockBccLockedInfoVo> getLockBccLockedInfo(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("BCC_ENERGIZE", "SETTING_ID");
        Assert.notNull(dataDist, "找不到活动配置");
        long settingId = ConvertUtils.lookup(Long.class).convert(Long.class, dataDist.getDictVal());
        LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOne(settingId);
        Assert.notNull(lockCoinActivitieSetting, "找不到活动配置");
        // 获取用户BCC赋能计划锁仓信息
        LockBccLockedInfoVo lockBccLockedInfoVo = lockBccAssignRecordService.findLockBccLockedInfo(user.getId(), lockCoinActivitieSetting);
        MessageRespResult mr = MessageRespResult.success("success");
        mr.setData(lockBccLockedInfoVo);
        return mr;
    }
}
