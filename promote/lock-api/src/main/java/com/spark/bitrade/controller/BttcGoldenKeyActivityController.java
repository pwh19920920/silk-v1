package com.spark.bitrade.controller;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.LockType;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.vo.*;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static org.springframework.util.Assert.isTrue;

/**
 * 金钥匙活动controller
 * @author dengdy
 * @time 2019/5/08
 */
@RestController
@Slf4j
public class BttcGoldenKeyActivityController {

    @Autowired
    private ILockBttcOfflineWalletService iLockBttcOfflineWalletService;

    @Autowired
    private LockCoinActivitieSettingService lockCoinActivitieSettingService;

    @Autowired
    private LockCoinActivitieProjectService lockCoinActivitieProjectService;

    @Autowired
    private LockCoinDetailService lockCoinDetailService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberTransactionService memberTransactionService;

    @Autowired
    private ISilkDataDistService silkDataDistService;
    @Autowired
    private LockUttMemberService lockUttMemberService;

    @ApiOperation(value = "查询用户账户数据", tags = "bttc金钥匙-v1.0")
    @PostMapping("/goldenKey/getUserAccountInfo")
    public MessageRespResult<LockBttcOfflineWalletVo> getUserAccountInfo(@ApiIgnore() @SessionAttribute(SESSION_MEMBER)
                                                                                     AuthMember user){
        Member member = memberService.findOne(user.getId());
        Assert.isTrue(member != null, "登录信息无效");
        LockBttcOfflineWalletVo lockBttcOfflineWalletVo = iLockBttcOfflineWalletService
                                    .findLockBttcOfflineWalletVoByMemberId(user.getId());
        BigDecimal ieoBalance = iLockBttcOfflineWalletService
                .findLockBttcIeoOfflineWalletBalanceByMemberId(user.getId());
        LockBttcOfflineWalletAndIeoWWalletVo lockBttcOfflineWalletAndIeoWWalletVo = new LockBttcOfflineWalletAndIeoWWalletVo();
        if(lockBttcOfflineWalletVo!=null){
            lockBttcOfflineWalletAndIeoWWalletVo.setBalance(lockBttcOfflineWalletVo.getBalance());
            lockBttcOfflineWalletAndIeoWWalletVo.setEnableUnlockAmount(lockBttcOfflineWalletVo.getEnableUnlockAmount());
            lockBttcOfflineWalletAndIeoWWalletVo.setUnlockedAmount(lockBttcOfflineWalletVo.getUnlockedAmount());
        }
        lockBttcOfflineWalletAndIeoWWalletVo.setIeoBalance(ieoBalance==null?BigDecimal.ZERO:ieoBalance);
        return MessageRespResult.success("查询成功", lockBttcOfflineWalletAndIeoWWalletVo);
    }

    @ApiOperation(value = "查询金钥匙活动详情", tags = "bttc金钥匙-v1.0")
    @PostMapping("/goldenKey/goldenKeySetting")
    public MessageRespResult goldenKeySetting(Long memberId) {
        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("GOLDEN_KEY", "SETTING_ID");
        SilkDataDist symbolDist = silkDataDistService.findByIdAndKey("GOLDEN_KEY", "BASE_SYMBOL");
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
                    .maxBuyAmount(lockCoinActivitieSetting.getMaxBuyAmount())
                    .minBuyAmount(lockCoinActivitieSetting.getMinBuyAmount())
                    .planAmount(lockCoinActivitieSetting.getPlanAmount())
                    .baseSymbol(symbolDist.getDictVal())
                    .symbol(lockCoinActivitieSetting.getCoinSymbol())
                    .unitPerAmount(lockCoinActivitieProject.getUnitPerAmount())
                    .lockDays(lockCoinActivitieSetting.getLockDays())
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
            MessageRespResult messageRespResult = MessageRespResult.success();
            messageRespResult.setData(Collections.singletonList(lockActivitySettingBuilder));
            return messageRespResult;
        } else {
            return MessageRespResult.error("获取配置失败");
        }
    }

    @ApiOperation(value = "查询用户锁仓记录", tags = "bttc金钥匙-v1.0")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "页面大小", required = true)
    })
    @PostMapping("/goldenKey/getUserLockRecords")
    public MessageRespResult<List<CustomerLockCoinDetail>> getUserLockRecords(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user
            , int pageNo, int pageSize){
        Member member = memberService.findOne(user.getId());
        Assert.isTrue(member != null, "登录信息无效");
        PageInfo<CustomerLockCoinDetail> lockCoinDetails =
                lockCoinDetailService.findGoldenKeyLockRecords(member.getId().longValue(), LockType.GOLD_KEY, pageNo, pageSize);
        return MessageRespResult.success("查询成功", lockCoinDetails);
    }

    @ApiOperation(value = "查询已解金钥匙记录", tags = "bttc金钥匙-v1.0")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "页面大小", required = true)
    })
    @PostMapping("/goldenKey/getUserUnlockedRecords")
    public MessageRespResult<List<UnlockedGoldKeyAmountVo>> getUserUnlockedRecords(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user
            , int pageNo, int pageSize){
        Member member = memberService.findOne(user.getId());
        Assert.isTrue(member != null, "登录信息无效");
        PageInfo<UnlockedGoldKeyAmountVo> unlockedGoldKeyAmountVos =
                memberTransactionService.findReleaseGoldenKeyRecords(member.getId(), pageNo, pageSize);
        return MessageRespResult.success("查询成功", unlockedGoldKeyAmountVos);
    }

    @ApiOperation(value = "用户锁仓操作", tags = "bttc金钥匙-v1.0")
    @ApiImplicitParam(name = "amount", value = "锁仓金额", required = true)
    @PostMapping("/goldenKey/lockOpreation")
    public MessageRespResult lockOpreation(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user
            , BigDecimal amount) throws Exception{
        Member member = memberService.findOne(user.getId());
        Assert.isTrue(member != null, "登录信息无效");

        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("GOLDEN_KEY", "SETTING_ID");
        isTrue(dataDist != null && dataDist.getStatus() == BooleanEnum.IS_TRUE, "活动配置无效");
        SilkDataDist symbolDist = silkDataDistService.findByIdAndKey("GOLDEN_KEY", "BASE_SYMBOL");
        isTrue(symbolDist != null && symbolDist.getStatus() == BooleanEnum.IS_TRUE, "活动配置无效");
        //验证活动配置和锁仓配置是否存在
        long id = ConvertUtils.lookup(Long.class).convert(Long.class, dataDist.getDictVal());

        // 用户锁仓操作
        iLockBttcOfflineWalletService.lockReleaseGoldenKey(member.getId(), amount, id);
        return MessageRespResult.success();
    }

    @ApiOperation(value = "用户BTTC导入记录", tags = "用户BTTC导入记录")
    @PostMapping("/goldenKey/bttcImportRecord")
    public MessageRespResult bttcImportRecord(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user,
                                              @RequestParam(defaultValue = "1") Integer pageNo,
                                              @RequestParam(defaultValue = "10") Integer pageSize){
        //筛选出所有表
        List<String> allImportTable = lockUttMemberService.findAllImportTable();
        Iterator<String> iterator = allImportTable.iterator();
        while (iterator.hasNext()){
            String tableName = iterator.next();
            String tableExist = lockUttMemberService.tableExist("%" + tableName + "%");
            if(StringUtils.isEmpty(tableExist)){
                iterator.remove();
            }
        }

        List<LockBttcImportVo> result=new ArrayList<>();
        for (String table:allImportTable){
            List<LockBttcImportVo> bttcImportList = lockUttMemberService.findBttcImportList(user.getId(), table);
            if(!CollectionUtils.isEmpty(bttcImportList)){
                result.addAll(bttcImportList);
            }
        }

        return MessageRespResult.success4Data(result);
    }

}





















