package com.spark.bitrade.controller;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.ActivitieType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.LockType;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.dto.LockCoinActivitieProjectDto;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PriceUtil;
import com.spark.bitrade.vo.LockActivitySettingBuilder;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * @author Zhang Yanjun
 * @time 2019.04.17 14:32
 */
@RestController
@Slf4j
public class IeoController {
    @Autowired
    private LockBttcRestitutionIncomePlanService lockBttcRestitutionIncomePlanService;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private ISilkDataDistService silkDataDistService;
    @Autowired
    private LockCoinActivitieSettingService lockCoinActivitieSettingService;
    @Autowired
    private LockCoinActivitieProjectService lockCoinActivitieProjectService;
    @Autowired
    private LockIeoActivitieService lockIeoActivitieService;
    @Autowired
    private LockCoinDetailService lockCoinDetailService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LockCoinDetailMybatisService lockCoinDetailMybatisService;

    /**
     * 返还计划分页记录
     * @author Zhang Yanjun
     * @time 2019.04.17 17:14
      * @param lockDetailId
     * @param pageNo
     * @param pageSize
     */
    @ApiOperation("返还计划分页记录")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "锁仓记录详情id",name = "lockDetailId",required = true),
            @ApiImplicitParam(value = "页码（1开始）",name = "pageNo",required = true),
            @ApiImplicitParam(value = "页大小",name = "pageSize",required = true)
    })
    @PostMapping("lock/ieoIncomePlanList")
    public MessageRespResult<PageInfo<LockBttcRestitutionIncomePlan>> incomePlan(Long lockDetailId,Integer pageNo, Integer pageSize){
        Assert.notNull(lockDetailId,msService.getMessage("DATA_ILLEGAL"));
        PageInfo<LockBttcRestitutionIncomePlan> pageInfo = lockBttcRestitutionIncomePlanService.findById(lockDetailId,pageNo,pageSize);
        return MessageRespResult.success("查询成功",PageData.toPageData(pageInfo));
    }

    /**
      * 获取用户的所有锁仓记录
      * @author fatKarin
      * @time 2019/6/3 15:12 
      */
    @ApiOperation(value = "获取用户IEO bcc币种的锁仓记录", tags = "IEO-v1.0锁仓")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码（1开始）",name = "pageNo",required = true),
            @ApiImplicitParam(value = "页大小",name = "pageSize",required = true),
            @ApiImplicitParam(value = "锁仓类型（0商家保证金，1手动锁仓，2锁仓活动，3理财锁仓，4SLB节点产品，5STO锁仓，6STO增值计划,7 ieo）",name = "lockType"),
    })
    @PostMapping("/ieoRestitutionRecords")
    public MessageRespResult<PageInfo<LockCoinDetailBuilder>> ieoRestitutionRecords(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user, Integer pageNo, Integer pageSize, @RequestParam LockType lockType) {
        Assert.notNull(lockType, msService.getMessage("DATA_ILLEGAL"));
        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("IEO_ACTIVITY_BCC", "SETTING_ID");
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
     * IEO活动配置
     */
    @ApiOperation(value = "IEO-BCC活动配置", tags = "IEO-v1.0锁仓")
    @PostMapping("/lock/ieoBccActivitySetting")
    public MessageRespResult ieoBccActivitySetting(Long memberId) {
        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("IEO_ACTIVITY_BCC", "SETTING_ID");
        SilkDataDist symbolDist = silkDataDistService.findByIdAndKey("IEO_ACTIVITY_BCC", "BASE_SYMBOL");
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
                    .endTime(lockCoinActivitieSetting.getEndTime())
                    .startTime(lockCoinActivitieSetting.getStartTime())
                    //由于闪对完后，出现小数点情况，在最高限额后减一，最小限额加一
                    .maxBuyAmount(lockCoinActivitieSetting.getMaxBuyAmount().subtract(BigDecimal.ONE))
                    .minBuyAmount(lockCoinActivitieSetting.getMinBuyAmount().add(BigDecimal.ONE))
                    .planAmount(lockCoinActivitieSetting.getPlanAmount())
                    .baseSymbol(symbolDist.getDictVal())
                    .symbol(lockCoinActivitieSetting.getCoinSymbol())
                    .unitPerAmount(lockCoinActivitieProject.getUnitPerAmount())
                    .cycle(lockCoinActivitieSetting.getLockCycle())
                    .lockDays(lockCoinActivitieSetting.getLockDays())
                    .cycleDays(lockCoinActivitieSetting.getCycleDays())
                    .beginDays(lockCoinActivitieSetting.getBeginDays())
                    .cycleRatio(lockCoinActivitieSetting.getCycleRatio())
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
            if(lockCoinActivitieProject.getStartTime().compareTo(new Date()) > 0){
                lockActivitySettingBuilder.setIsOverdue(2);
            }else if(lockCoinActivitieProject.getEndTime().compareTo(new Date()) < 0){
                lockActivitySettingBuilder.setIsOverdue(1);
                if(null == memberId){
                    MessageRespResult messageRespResult = MessageRespResult.success();
                    //messageRespResult.setData(Collections.singletonList(lockActivitySettingBuilder));
                    return messageRespResult;
                }
                List<LockCoinDetail> detailList = lockCoinActivitieProjectService.findByMemberId(memberId,lockCoinActivitieSetting.getId());
                if(detailList.size() == 0){
                    MessageRespResult messageRespResult = MessageRespResult.success();
                    //messageRespResult.setData(Collections.singletonList(lockActivitySettingBuilder));
                    return messageRespResult;
                }
            }else{
                lockActivitySettingBuilder.setIsOverdue(0);
            }
            MessageRespResult mr = MessageRespResult.success();
            mr.setData(Collections.singletonList(lockActivitySettingBuilder));
            return mr;
        } else {
            return MessageRespResult.error("获取配置失败");
        }
    }

    /**
     * IEO锁仓活动
     *
     * @author fatKarin
     * @time 2019/5/29 15:18
     */
    @ApiOperation(value = "IEO锁仓活动", tags = "IEO-v1.0锁仓")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "锁仓数量（BT 数量）", name = "amount", dataType = "String", required = true),
            @ApiImplicitParam(value = "交易密码", name = "password", dataType = "String", required = true),
            @ApiImplicitParam(value = "用来兑换的币种", name = "baseSymbol", dataType = "String", required = true)
    })
    @PostMapping("/lock/ieoBccLock")
    public MessageRespResult lockBcc(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user, BigDecimal amount, String password, String baseSymbol) {
        //验证资金密码
        hasText(password, msService.getMessage("MISSING_JYPASSWORD"));
        Member member = memberService.findOne(user.getId());

        String mbPassword = member.getJyPassword();
        String jyPass = new SimpleHash("md5", password, member.getSalt(), 2).toHex().toLowerCase();
        hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));


        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("IEO_ACTIVITY_BCC", "SETTING_ID");
        isTrue(dataDist != null && dataDist.getStatus() == BooleanEnum.IS_TRUE, "活动配置无效");
        SilkDataDist symbolDist = silkDataDistService.findByIdAndKey("IEO_ACTIVITY_BCC", "BASE_SYMBOL");
        isTrue(symbolDist != null && symbolDist.getStatus() == BooleanEnum.IS_TRUE, "活动配置无效");

        String[] baseSymbols = symbolDist.getDictVal().split(",");
        List<String> symbolList = Arrays.asList(baseSymbols);
        isTrue(symbolList.contains(baseSymbol), "该币种不是活动币种");

        //验证活动配置和锁仓配置是否存在
        long activityId = ConvertUtils.lookup(Long.class).convert(Long.class, dataDist.getDictVal());
        LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOneByTimeWithoutCache(activityId);
        notNull(lockCoinActivitieSetting, "有效IEO活动不存在");
        LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(lockCoinActivitieSetting.getActivitieId());
        notNull(lockCoinActivitieProject, "有效活动方案不存在");
        isTrue(lockCoinActivitieProject.getType() == ActivitieType.IEO, "活动类型错误");



        // 闪兑：baseSymobl->活动币种
        MessageResult mr = lockIeoActivitieService.fastExchange(restTemplate, user, baseSymbol, lockCoinActivitieSetting.getCoinSymbol(), amount);
        isTrue(mr.isSuccess(), mr.getMessage());
        BigDecimal exchangeAmount = (BigDecimal) mr.getData();
        log.info("闪兑：{} -> {}: {}", baseSymbol, lockCoinActivitieSetting.getCoinSymbol(), exchangeAmount);
        isTrue(exchangeAmount.compareTo(BigDecimal.ZERO) > 0, baseSymbol +"兑换"+   lockCoinActivitieSetting.getCoinSymbol() + "失败，请检测余额");
        boolean lock = false;
        try {
            // 首先验证购买金额是否达到最低限额
            isTrue(exchangeAmount.compareTo(lockCoinActivitieSetting.getMinBuyAmount()) >= 0, "低于最低购买数量");
            // 验证购买金额是否达到最高购买限额
            isTrue(exchangeAmount.compareTo(lockCoinActivitieSetting.getMaxBuyAmount()) <= 0, "高于最大购买数量");

            // 验证账户总限额
            double total = lockCoinDetailService.findByMemberIdAndId(member.getId(), activityId).stream().mapToDouble(i -> i.getTotalAmount().doubleValue()).sum();
            isTrue(lockCoinActivitieSetting.getMaxBuyAmount().compareTo(new BigDecimal(total).add(exchangeAmount)) > 0, "已经超出账户购买限额");

            //验证购买数量与已参与购买数量之和，是否大于最大计划总量
            BigDecimal maxPlanAmount = exchangeAmount.add(lockCoinActivitieSetting.getBoughtAmount());
            isTrue(maxPlanAmount.compareTo(lockCoinActivitieSetting.getPlanAmount()) < 0, "已超出活动计划的购买金额");

            // 开始锁仓
            PriceUtil priceUtil = new PriceUtil();
            //获取锁仓币种人民币价格
            BigDecimal coinCnyPrice = priceUtil.getCoinCnyPrice(restTemplate, lockCoinActivitieSetting.getCoinSymbol());
            //获取锁仓币种USDT价格
            BigDecimal coinUSDTPrice = priceUtil.getCoinCnyPrice(restTemplate, lockCoinActivitieSetting.getCoinSymbol());
            //获取USDT的人民币价格
            BigDecimal usdtPrice = priceUtil.getUSDTPrice(restTemplate);
            lockIeoActivitieService.ieoBccLock(lockCoinActivitieSetting, user, coinCnyPrice, coinUSDTPrice, usdtPrice, exchangeAmount);
            lock = true;
        } catch (Exception e) {
            log.error("IEO锁仓失败", e);
            throw e;
        } finally {
            if (!lock) {
                // 锁仓失败，闪兑：活动币种->baseSymobl
                mr = lockIeoActivitieService.fastExchangeBack(restTemplate, user, baseSymbol, lockCoinActivitieSetting.getCoinSymbol(), exchangeAmount);
                BigDecimal btAmount = (BigDecimal) mr.getData();
                log.error("锁仓失败，兑换{} -> {}: {}", lockCoinActivitieSetting.getCoinSymbol(), baseSymbol, btAmount);
            }
        }
        return MessageRespResult.success("锁仓成功");
    }

}
