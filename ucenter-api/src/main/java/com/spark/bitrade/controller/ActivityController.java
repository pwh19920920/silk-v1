package com.spark.bitrade.controller;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.dto.LockCoinActivitieProjectDto;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.event.LockCoinActivitieEvent;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.RewardActivityResp;
import com.spark.bitrade.vo.RewardActivityVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;

/**
  * 锁仓活动控制器
  * @author tansitao
  * @time 2018/6/14 11:58 
  */
@Api(description = "活动类接口",tags={"活动类接口操作"})
@RestController
@RequestMapping("/activity")
@Slf4j
public class ActivityController {

    @Autowired
    private LockCoinActivitieProjectService lockCoinActivitieProjectService;
    @Autowired
    private LockCoinActivitieSettingService lockCoinActivitieSettingService;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private LockCoinDetailService lockCoinDetailService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private LockCoinDetailMybatisService lockCoinDetailMybatisService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private UnLockCoinDetailService unLockCoinDetailService;
    @Autowired
    private LockCoinActivitieEvent lockCoinActivitieEvent;
    @Autowired
    private RewardActivitySettingService activitySettingService;
    @Autowired
    private  RewardPromotionSettingService rewardPromotionSettingService;

    /**
      * 获取所有活动列表
      * @author tansitao
      * @time 2018/6/14 13:52 
      */
    @RequestMapping("/lockActivityProject")
    public MessageRespResult<List<LockCoinActivitieProjectDto>> lockActivityProject(ActivitieType activitieType,Long memberId) {
        List<LockCoinActivitieProject> lcActivitieList = lockCoinActivitieProjectService.findAllEnableProject(activitieType);
        LockCoinActivitieProjectDto lockCoinActivitieProjectDto = null;
        List<LockCoinActivitieProjectDto> listProjectDto = new ArrayList<>();
        for(LockCoinActivitieProject project:lcActivitieList){
            lockCoinActivitieProjectDto = new LockCoinActivitieProjectDto();
            BeanUtils.copyProperties(project,lockCoinActivitieProjectDto);
            listProjectDto.add(lockCoinActivitieProjectDto);
        }
        //获取收益率和锁仓周期
        for(LockCoinActivitieProjectDto projectDto:listProjectDto){
            List<LockCoinActivitieSetting>  lcActivitieSettingList= lockCoinActivitieSettingService.findByActivitieId(projectDto.getId());
            //遍历配置，处理部分数据，过滤不需要的数据
            List<BigDecimal> listRate = new ArrayList<>();
            List<Integer> listDay = new ArrayList<>();
            for (LockCoinActivitieSetting lockCoinActivitieSetting:lcActivitieSettingList)
            {
                listRate.add(lockCoinActivitieSetting.getEarningRate());
                listDay.add(lockCoinActivitieSetting.getLockDays()/30);
            }
            projectDto.setLockDaysList(listDay);
            projectDto.setMonthRateList(listRate);
        }
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

        }else if("zh_HK".equals(accept)){//繁体中文
            for(LockCoinActivitieProjectDto dto:listProjectDto){
                LockCoinActivitieProjectInternational international = lockCoinDetailService.selectInternational(dto.getId(),1);
                if(null != international){
                    dto.setName(international.getName());
                    dto.setImgUrl(international.getImgUrl());
                    dto.setIncomeImg(international.getIncomeImg());
                    dto.setTitleImg(international.getTitleImg());
                    dto.setBriefDescription(international.getBriefDescription());
                    dto.setDescription(international.getDescription());
                }
            }

        }else if("en_US".equals(accept)){//英文
            for(LockCoinActivitieProjectDto dto:listProjectDto){
                LockCoinActivitieProjectInternational international = lockCoinDetailService.selectInternational(dto.getId(),2);
                if(null != international){
                    dto.setName(international.getName());
                    dto.setImgUrl(international.getImgUrl());
                    dto.setIncomeImg(international.getIncomeImg());
                    dto.setTitleImg(international.getTitleImg());
                    dto.setBriefDescription(international.getBriefDescription());
                    dto.setDescription(international.getDescription());
                }
            }
        }else if("ko_KR".equals(accept)){//韩文
            for(LockCoinActivitieProjectDto dto:listProjectDto){
                LockCoinActivitieProjectInternational international = lockCoinDetailService.selectInternational(dto.getId(),3);
                if(null != international){
                    dto.setName(international.getName());
                    dto.setImgUrl(international.getImgUrl());
                    dto.setIncomeImg(international.getIncomeImg());
                    dto.setTitleImg(international.getTitleImg());
                    dto.setBriefDescription(international.getBriefDescription());
                    dto.setDescription(international.getDescription());
                }
            }
        }
        for(LockCoinActivitieProjectDto a:listProjectDto){
            //判断活动是否过期
            if(a.getStartTime().compareTo(new Date()) > 0){
                a.setIsOverdue(2);
            }else if(a.getEndTime().compareTo(new Date()) < 0){
                a.setIsOverdue(1);
                if(null == memberId){
                    a = null;
                    continue;
                }
                List<LockCoinActivitieSetting> settingList = lockCoinActivitieSettingService.findByActivitieId(a.getId());
                List<LockCoinDetail> detailList = lockCoinActivitieProjectService.findByMemberIds(memberId,settingList);
                if(detailList.size() == 0){
                    a = null;
                }
            }else{
                a.setIsOverdue(0);
            }
        }
        MessageRespResult mr = MessageRespResult.success();
        mr.setData(listProjectDto);
        return mr;
    }

    /**
      * 获取理财宝页面精品活动和热门活动的列表,封装成map,精品活动列表key为top,热门活动列表key为hot
      * @author denglei
      * @time 2019/4/26 13:52 
      */
    @ApiOperation("理财宝宝接口")
    @PostMapping("/financialLockActivityProject")
//    @RequestMapping("/financialLockActivityProject")
    public MessageResult financialLockActivityProject(HttpServletRequest request) {
        ActivitieType activitieType = ActivitieType.STO;
        Map<String,List<LockCoinActivitieProjectDto>> map = lockCoinActivitieProjectService.findAllEnableFinancialProject(activitieType,request);
        //处理数据,查询每一个大活动的小活动列表,找到小活动的最高月利率设置到大活动中
        List<LockCoinActivitieProjectDto> hotProjects = map.get("hot");
        for (LockCoinActivitieProjectDto hotProject : hotProjects) {
                queryProjectMonthRate(hotProject,request);
        }
        List<LockCoinActivitieProjectDto> topProjects = map.get("top");
        for (LockCoinActivitieProjectDto topProject : topProjects) {
                queryProjectMonthRate(topProject,request);
        }
        MessageResult mr = MessageResult.success();
        mr.setData(map);
        return mr;
    }

    //查询大活动关联的小活动中的最高月利率设置给大活动
    private void queryProjectMonthRate(LockCoinActivitieProjectDto a,HttpServletRequest request){
        //判断活动是否过期
        if(a.getStartTime().compareTo(new Date()) > 0){
            a.setIsOverdue(2);
        }else if(a.getEndTime().compareTo(new Date()) < 0){
            a.setIsOverdue(1);
        }else{
            a.setIsOverdue(0);
        }
        //查询到大活动对应的小活动列表
        List<LockCoinActivitieSetting>  settings = lockCoinActivitieSettingService.findByActivitieId(a.getId());
        //遍历小活动列表,找到月利率最高的小活动
        BigDecimal monthRate = BigDecimal.ZERO;
        Integer minLockDays = 0;
        for (LockCoinActivitieSetting setting : settings) {
            if(setting.getLockDays() < minLockDays || minLockDays == 0){
                minLockDays = setting.getLockDays();
            }
            Integer month = setting.getLockDays()/30;
            BigDecimal tempMonthRate = setting.getEarningRate().divide(new BigDecimal(month),8,BigDecimal.ROUND_HALF_DOWN);
            if(tempMonthRate.compareTo(monthRate) > 0){
                monthRate = tempMonthRate;
            }
        }
        a.setMonthRate(monthRate);
        if(settings.size() == 1){
            a.setLockDays(minLockDays+ msService.getMessage("DAY_UNIT"));
        }else {
            String language = request.getHeader("language");
            if(StringUtils.pathEquals("en_US",language)){
                a.setLockDays(String.format("Start from %s days",minLockDays));
            }else {
                a.setLockDays(minLockDays + msService.getMessage("DAY_UNIT_START"));
            }
        }
    }

    /**
     * 获取单个活动详情
     * @param id
     * @return
     */
    @PostMapping("/lockActivityProject/{id}")
    public MessageResult lockActivityProject(@PathVariable(value = "id") long id)
    {
        LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(id);
        //判断活动是否过期
        if(lockCoinActivitieProject.getEndTime().compareTo(new Date()) >= 0){
            lockCoinActivitieProject.setIsOverdue(BooleanEnum.IS_FALSE);
        }else{
            lockCoinActivitieProject.setIsOverdue(BooleanEnum.IS_TRUE);
        }
        //add by tansitao 时间： 2018/11/27 原因：根据锁仓类型，查询佣金参数
        RewardPromotionSetting rewardPromotionSetting = null;
        if(lockCoinActivitieProject.getType() == ActivitieType.STO){
            Coin coin=  coinService.findByUnit(lockCoinActivitieProject.getCoinSymbol());
            rewardPromotionSetting = rewardPromotionSettingService.findByTypeAndCoin(PromotionRewardType.ACTIVE_STO, coin);
            lockCoinActivitieProject.setReward(rewardPromotionSetting != null ? rewardPromotionSetting.getInfo() : null);
        }
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

        }else if("zh_HK".equals(accept)){//繁体中文
            LockCoinActivitieProjectInternational international = lockCoinDetailService.selectInternational(lockCoinActivitieProject.getId(),1);
            if(null != international){
                lockCoinActivitieProject.setName(international.getName());
                lockCoinActivitieProject.setImgUrl(international.getImgUrl());
                lockCoinActivitieProject.setIncomeImg(international.getIncomeImg());
                lockCoinActivitieProject.setTitleImg(international.getTitleImg());
                //lockCoinActivitieProject.setBriefDescription(international.getBriefDescription());
                lockCoinActivitieProject.setDescription(international.getDescription());
            }
        }else if("en_US".equals(accept)){//英文
            LockCoinActivitieProjectInternational international = lockCoinDetailService.selectInternational(lockCoinActivitieProject.getId(),2);
            if(null != international){
                lockCoinActivitieProject.setName(international.getName());
                lockCoinActivitieProject.setImgUrl(international.getImgUrl());
                lockCoinActivitieProject.setIncomeImg(international.getIncomeImg());
                lockCoinActivitieProject.setTitleImg(international.getTitleImg());
                //lockCoinActivitieProject.setBriefDescription(international.getBriefDescription());
                lockCoinActivitieProject.setDescription(international.getDescription());
            }
        }else if("ko_KR".equals(accept)){//韩文
            LockCoinActivitieProjectInternational international = lockCoinDetailService.selectInternational(lockCoinActivitieProject.getId(),3);
            if(null != international){
                lockCoinActivitieProject.setName(international.getName());
                lockCoinActivitieProject.setImgUrl(international.getImgUrl());
                lockCoinActivitieProject.setIncomeImg(international.getIncomeImg());
                lockCoinActivitieProject.setTitleImg(international.getTitleImg());
                //lockCoinActivitieProject.setBriefDescription(international.getBriefDescription());
                lockCoinActivitieProject.setDescription(international.getDescription());
            }
        }
        MessageResult mr = MessageResult.success();
        mr.setData(lockCoinActivitieProject);
        return mr;
    }

    /**
     * 获取活动配置所有详情
     * @param activitieId
     * @return
     */
    @PostMapping("/lockActivitySetting/{activitieId}")
    public MessageResult lockActivitySetting(@PathVariable(value = "activitieId") long activitieId,HttpServletRequest request)throws Exception{
        String language = request.getHeader("language");
        if (StringUtils.isEmpty(language)) {
            language = "zh_CN";
        }
        InternationalType type = InternationalType.nameOf(language);
        List<LockActivitySettingBuilder> laSettingBuilderList = new ArrayList<LockActivitySettingBuilder>();
        List<LockCoinActivitieSetting>  lcActivitieSettingList= lockCoinActivitieSettingService.findByActivitieId(activitieId);
        if(!CollectionUtils.isEmpty(lcActivitieSettingList)){
            lcActivitieSettingList.forEach(l->{
                if(type!=null){
                    String nameByidAndLanguage = lockCoinActivitieProjectService.findSettingNameByidAndLanguage(l.getId(), type.getValue());
                    if(!StringUtils.isEmpty(nameByidAndLanguage)){
                        l.setName(nameByidAndLanguage);
                    }
                }

            });
        }
        //判断是否有子活动配置
        if(lcActivitieSettingList == null || lcActivitieSettingList.size() == 0 )
        {
            throw new IllegalArgumentException(msService.getMessage("NOT_HAVE_SET"));
        }
        //遍历配置，处理部分数据，过滤不需要的数据
        for (LockCoinActivitieSetting lockCoinActivitieSetting:lcActivitieSettingList)
        {
            //获取锁币周期（月）
            double month = (double)lockCoinActivitieSetting.getLockDays()/30;
            //获取总活动
            LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(lockCoinActivitieSetting.getActivitieId());
            //获取预计收益
            BigDecimal planIncome = lockCoinActivitieProject.getUnitPerAmount().multiply(BigDecimal.valueOf(10)).multiply(lockCoinActivitieSetting.getEarningRate()).multiply(BigDecimal.valueOf(month/12));
            LockActivitySettingBuilder lockActivitySettingBuilder = LockActivitySettingBuilder.builder().boughtAmount(lockCoinActivitieProject.getBoughtAmount())
                    .cycle((double) lockCoinActivitieSetting.getLockDays()/30)
                    .earningRate(lockCoinActivitieSetting.getEarningRate())
                    .endTime(lockCoinActivitieSetting.getEndTime())
                    .startTime(lockCoinActivitieSetting.getStartTime())
                    .id(lockCoinActivitieSetting.getId())
                    .maxBuyAmount(lockCoinActivitieSetting.getMaxBuyAmount())
                    .minBuyAmount(lockCoinActivitieSetting.getMinBuyAmount())
                    .name(lockCoinActivitieSetting.getName())
                    .planAmount(lockCoinActivitieProject.getPlanAmount())
                    .symbol(lockCoinActivitieSetting.getCoinSymbol())
                    .unitPerAmount(lockCoinActivitieProject.getUnitPerAmount())
                    .planIncome(planIncome)
                    .build();
            laSettingBuilderList.add(lockActivitySettingBuilder);
        }
        MessageResult mr = MessageResult.success();
        mr.setData(laSettingBuilderList);
        return mr;
    }



    /**
     * 获取某个活动配置
     * @param id
     * @return
     */
    @PostMapping("/oneLockActivitySetting/{id}")
    public MessageResult oneLockActivitySetting(@PathVariable(value = "id") long id)throws Exception{
        LockCoinActivitieSetting  lockCoinActivitieSetting= lockCoinActivitieSettingService.findOne(id);
        //遍历配置，处理部分数据，过滤不需要的数据
        //获取锁币周期（月）
        double month = (double)lockCoinActivitieSetting.getLockDays()/30;
        //获取总活动
        LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(lockCoinActivitieSetting.getActivitieId());
        //获取预计收益
        BigDecimal planIncome = lockCoinActivitieProject.getUnitPerAmount().multiply(BigDecimal.valueOf(10)).multiply(lockCoinActivitieSetting.getEarningRate()).multiply(BigDecimal.valueOf(month/12));
        LockActivitySettingBuilder lockActivitySettingBuilder = LockActivitySettingBuilder.builder().boughtAmount(lockCoinActivitieProject.getBoughtAmount())
                .cycle((double) lockCoinActivitieSetting.getLockDays()/30)
                .earningRate(lockCoinActivitieSetting.getEarningRate())
                .endTime(lockCoinActivitieSetting.getEndTime())
                .startTime(lockCoinActivitieSetting.getStartTime())
                .id(lockCoinActivitieSetting.getId())
                .maxBuyAmount(lockCoinActivitieSetting.getMaxBuyAmount())
                .minBuyAmount(lockCoinActivitieSetting.getMinBuyAmount())
                .name(lockCoinActivitieSetting.getName())
                .planAmount(lockCoinActivitieProject.getPlanAmount())
                .symbol(lockCoinActivitieSetting.getCoinSymbol())
                .unitPerAmount(lockCoinActivitieProject.getUnitPerAmount())
                .planIncome(planIncome)
                .build();
        MessageResult mr = MessageResult.success();
        mr.setData(lockActivitySettingBuilder);
        return mr;
    }


    /**
      * 参加锁仓活动
      * @author tansitao
      * @time 2018/6/20 14:56 
      */
    @PostMapping("/joinActivity")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult joinActivity(@SessionAttribute(SESSION_MEMBER) AuthMember user,
                                      @RequestParam long id, @RequestParam BigDecimal boughtAmount,@RequestParam String jyPassword)
    {
        //验证资金密码
        hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));
        Member member = memberService.findOne(user.getId());
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        String mbPassword = member.getJyPassword();
        Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));
        //验证活动配置和锁仓配置是否存在
        LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOneByTime(id);
        Assert.isTrue(lockCoinActivitieSetting != null, msService.getMessage("NOT_HAVE_SET"));
        LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(lockCoinActivitieSetting.getActivitieId());
        Assert.isTrue(lockCoinActivitieProject != null, msService.getMessage("NOT_HAVE_ACTIVITY"));
        //验证数量参与活动数量是否正确
        Assert.isTrue(lockCoinActivitieSetting.getMinBuyAmount().compareTo(boughtAmount) <= 0,  msService.getMessage("NOT_LT") + lockCoinActivitieSetting.getMinBuyAmount());
        Assert.isTrue(lockCoinActivitieSetting.getMaxBuyAmount().compareTo(boughtAmount) >= 0,  msService.getMessage("NOT_GT") + lockCoinActivitieSetting.getMaxBuyAmount());
        //增加参加活动份额数
        MessageResult result = lockCoinActivitieProjectService.increaseBoughtAmount(lockCoinActivitieProject.getId(), boughtAmount);
        if (result.getCode() != 0)
        {
            throw new IllegalArgumentException("REMAIN_INSUFFICIENT");
        }

        //获取钱包信息
        BigDecimal totalCoinNum = boughtAmount.multiply(lockCoinActivitieSetting.getUnitPerAmount());
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinActivitieSetting.getCoinSymbol(),member.getId());
        isTrue(memberWallet != null,msService.getMessage("WALLET_GET_FAIL"));
        //冻结锁仓币数
        MessageResult walletResult = memberWalletService.freezeBalanceToLockBalance(memberWallet, totalCoinNum);
        if (walletResult.getCode() != 0)
        {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }

        //保存资金记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setAmount(BigDecimal.ZERO.subtract(totalCoinNum));
        memberTransaction.setMemberId(member.getId());
        memberTransaction.setType(TransactionType.ADMIN_LOCK_ACTIVITY);
        memberTransaction.setSymbol(lockCoinActivitieSetting.getCoinSymbol());
        memberTransactionService.save(memberTransaction);


        //添加锁仓记录，调用market的汇率信息获取
        LockCoinDetail lockCoinDetail = new LockCoinDetail();
        String unit = lockCoinActivitieSetting.getCoinSymbol();
        String serviceName = "bitrade-market";
        String url = "http://" + serviceName + "/market/exchange-rate/usd/" + unit;
        try
        {
            ResponseEntity<MessageResult> pricResult = restTemplate.getForEntity(url, MessageResult.class);
            MessageResult mr = pricResult.getBody();
            log.info("=========查询" + unit + "价格后返回的结果{}=========", mr.getCode()+ "===" + mr.getMessage());
            if (mr.getCode() == 0)
            {
                lockCoinDetail.setLockPrice(BigDecimal.valueOf(Double.parseDouble(mr.getData().toString())));
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //保存锁仓详情
        int month = lockCoinActivitieSetting.getLockDays()/30;
        lockCoinDetail.setPlanUnlockTime(DateUtil.addMonth(new Date(), month));
        lockCoinDetail.setMemberId(user.getId());
        lockCoinDetail.setType(LockType.LOCK_ACTIVITY);
        lockCoinDetail.setCoinUnit(unit);
        lockCoinDetail.setTotalAmount(totalCoinNum);
        lockCoinDetail.setRemainAmount(totalCoinNum);
        if(LockCoinActivitieType.COIN_REWARD == lockCoinActivitieSetting.getType())
        {
            BigDecimal planIncome = lockCoinDetail.getTotalAmount().multiply(lockCoinActivitieSetting.getEarningRate()).multiply(BigDecimal.valueOf((double) month/12));
            lockCoinDetail.setPlanIncome(planIncome);
        }
        lockCoinDetail.setStatus(LockStatus.LOCKED);
        lockCoinDetail.setRefActivitieId(lockCoinActivitieSetting.getId());
        lockCoinDetailService.save(lockCoinDetail);

        return MessageResult.success();
    }

    /**
      * 用户手动申请解锁锁仓活动
      * @author tansitao
      * @time 2018/6/20 14:56 
      */
    @PostMapping("/unLockActivity")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult unLockActivity(@SessionAttribute(SESSION_MEMBER) AuthMember user, @RequestParam long id,@RequestParam String jyPassword)
    {
        //验证资金密码
        hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));
        Member member = memberService.findOne(user.getId());
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        String mbPassword = member.getJyPassword();
        Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));
        //edit by tansitao 时间： 2018/7/31 原因：修改条件查询方法，添加用户id查询
        LockCoinDetail lockCoinDetail = lockCoinDetailService.findOneByIdAndTypeAndMemberId(id, LockType.LOCK_ACTIVITY, member.getId());
        Assert.isTrue(lockCoinDetail != null, msService.getMessage("NOT_EXIST_RECORD"));
        Assert.isTrue(lockCoinDetail.getStatus() == LockStatus.LOCKED, msService.getMessage("NOT_REUNLOCK"));
        //获取、增加用户理财活动钱包锁仓余额
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinDetail.getCoinUnit(),member.getId());
        isTrue(memberWallet != null,msService.getMessage("WALLET_GET_FAIL"));
        //增加理财锁仓活动币数
        MessageResult activityWalletResult = memberWalletService.thawBalanceFromLockBlance(memberWallet, lockCoinDetail.getTotalAmount());
        if (activityWalletResult.getCode() != 0)
        {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }


        //保存增加理财币种锁仓资金记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setAmount(lockCoinDetail.getTotalAmount());
        memberTransaction.setMemberId(member.getId());
        memberTransaction.setType(TransactionType.ADMIN_LOCK_ACTIVITY);
        memberTransaction.setSymbol(lockCoinDetail.getCoinUnit());
        memberTransactionService.save(memberTransaction);

        //修改锁仓记录信息
        lockCoinDetail.setUnlockTime(new Date());
        lockCoinDetail.setRemainAmount(BigDecimal.ZERO);
        lockCoinDetail.setStatus(LockStatus.CANCLE);
        lockCoinDetail.setCancleTime(new Date());
        lockCoinDetailService.save(lockCoinDetail);

        //获取活动币种最新USDT价格
        PriceUtil priceUtil = new PriceUtil();
        BigDecimal coinPrice = priceUtil.getPriceByCoin(restTemplate, lockCoinDetail.getCoinUnit());
        //如果价格为0，则说明价格异常
        if(coinPrice.compareTo(BigDecimal.ZERO) == 0)
        {
            throw new IllegalArgumentException(msService.getMessage("PRICE_ERROR"));
        }

        //添加解锁记录
        UnlockCoinDetail unlockCoinDetail = new UnlockCoinDetail();
        unlockCoinDetail.setLockCoinDetailId(lockCoinDetail.getId());
        unlockCoinDetail.setAmount(lockCoinDetail.getTotalAmount());
        unlockCoinDetail.setRemainAmount(lockCoinDetail.getRemainAmount());
        unlockCoinDetail.setPrice(coinPrice);
        unLockCoinDetailService.save(unlockCoinDetail);

        //修改活动份额总数
        //增加参加活动份额数
        LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOne(lockCoinDetail.getRefActivitieId());
        MessageResult result = lockCoinActivitieProjectService.decreaseBoughtAmount(lockCoinActivitieSetting.getActivitieId(), lockCoinDetail.getTotalAmount().divide(lockCoinActivitieSetting.getUnitPerAmount(),8, BigDecimal.ROUND_DOWN));
        if (result.getCode() != 0)
        {
            throw new IllegalArgumentException("NOT_HAVE_ACTIVITY");
        }
        return MessageResult.success();

    }

    /**
      * 参加理财锁仓活动
      * @author tansitao
      * @time 2018/6/20 14:56 
      */
    @PostMapping("/joinFinancialLock")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult joinFinancialLock(@SessionAttribute(SESSION_MEMBER) AuthMember user,
                                           @RequestParam long id, @RequestParam BigDecimal usdtNum, @RequestParam String jyPassword)
    {
        //验证资金密码
        hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));
        Member member = memberService.findOne(user.getId());
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        String mbPassword = member.getJyPassword();
        Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));
        //验证活动配置和锁仓配置是否存在
        LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOneByTime(id);
        Assert.isTrue(lockCoinActivitieSetting != null, msService.getMessage("NOT_HAVE_SET"));
        LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(lockCoinActivitieSetting.getActivitieId());
        Assert.isTrue(lockCoinActivitieProject != null, msService.getMessage("NOT_HAVE_ACTIVITY"));

        //获取活动币种最新USDT价格
        PriceUtil priceUtil = new PriceUtil();
        BigDecimal activityCoinPrice = priceUtil.getPriceByCoin(restTemplate, lockCoinActivitieSetting.getCoinSymbol());
        //如果价格为0，则说明价格异常
        if(activityCoinPrice.compareTo(BigDecimal.ZERO) == 0)
        {
            throw new IllegalArgumentException(msService.getMessage("PRICE_ERROR"));
        }

        //获取usdt的人民币价格
        BigDecimal cnyPrice = priceUtil.getUSDTPrice(restTemplate);
        if(cnyPrice.compareTo(BigDecimal.ZERO) == 0)
        {
            throw new IllegalArgumentException(msService.getMessage("PRICE_ERROR"));
        }
        //理财活动的币总数
        BigDecimal totalAmount = usdtNum.divide(activityCoinPrice,8, BigDecimal.ROUND_DOWN);

        //获取、减少用户USDT钱包余额
        MemberWallet usdtMemberWallet = memberWalletService.findByCoinUnitAndMemberId("USDT",member.getId());
        if(usdtMemberWallet == null)
        {
            memberWalletService.createMemberWallet(user.getId(), coinService.findByUnit("USDT"));
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        MessageResult usdtWalletResult = memberWalletService.decreaseBalance(usdtMemberWallet.getId(), usdtNum);
        if (usdtWalletResult.getCode() != 0)
        {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        //保存USDT扣除资金记录
        MemberTransaction usdtMemberTransaction = new MemberTransaction();
        usdtMemberTransaction.setAmount(BigDecimal.ZERO.subtract(usdtNum));
        usdtMemberTransaction.setMemberId(member.getId());
        usdtMemberTransaction.setType(TransactionType.FINANCIAL_ACTIVITY);
        usdtMemberTransaction.setSymbol("USDT");
        memberTransactionService.save(usdtMemberTransaction);

        //获取、增加用户理财活动钱包锁仓余额
        MemberWallet activityMemberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinActivitieSetting.getCoinSymbol(),member.getId());
        if(activityMemberWallet == null)
        {
            activityMemberWallet = memberWalletService.createMemberWallet(user.getId(), coinService.findByUnit(lockCoinActivitieSetting.getCoinSymbol()));
        }
        //增加理财锁仓活动币数
        MessageResult activityWalletResult = memberWalletService.increaseLockBalance(activityMemberWallet.getId(), totalAmount);
        if (activityWalletResult.getCode() != 0)
        {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }

        //保存增加理财币种锁仓资金记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setAmount(totalAmount);
        memberTransaction.setMemberId(member.getId());
        memberTransaction.setType(TransactionType.FINANCIAL_ACTIVITY);
        memberTransaction.setSymbol(lockCoinActivitieSetting.getCoinSymbol());
        memberTransactionService.save(memberTransaction);


        //添加锁仓记录
        LockCoinDetail lockCoinDetail = new LockCoinDetail();
        //保存锁仓详情
        int month = lockCoinActivitieSetting.getLockDays()/30;
        lockCoinDetail.setLockPrice(activityCoinPrice);
        lockCoinDetail.setPlanUnlockTime(DateUtil.addMonth(new Date(), month));
        lockCoinDetail.setMemberId(user.getId());
        lockCoinDetail.setType(LockType.FINANCIAL_LOCK);
        lockCoinDetail.setCoinUnit(lockCoinActivitieSetting.getCoinSymbol());
        lockCoinDetail.setTotalAmount(totalAmount);
        lockCoinDetail.setRemainAmount(totalAmount);
        lockCoinDetail.setUsdtPriceCNY(cnyPrice);
        lockCoinDetail.setTotalCNY(usdtNum.multiply(cnyPrice));
        lockCoinDetail.setRemark("自主参加");

        if(LockCoinActivitieType.FIXED_DEPOSIT == lockCoinActivitieSetting.getType())
        {
            BigDecimal planIncome = lockCoinDetail.getTotalCNY().multiply(lockCoinActivitieSetting.getEarningRate()).multiply(BigDecimal.valueOf((double) month/12));
            lockCoinDetail.setPlanIncome(planIncome);
        }
        lockCoinDetail.setStatus(LockStatus.LOCKED);
        lockCoinDetail.setRefActivitieId(lockCoinActivitieSetting.getId());
        lockCoinDetailService.save(lockCoinDetail);
        return MessageResult.success();
    }

    /**
      * 满足计划解锁时间，用户解锁投资锁仓活动
      * @author tansitao
      * @time 2018/6/20 14:56 
      */
    @PostMapping("/autoUnLockFinancialLock")
    public MessageResult autoUnLockFinancialLock(@SessionAttribute(SESSION_MEMBER) AuthMember user, @RequestParam long id,@RequestParam SettlementType settlementType, @RequestParam String jyPassword) {
        //验证资金密码
        hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));
        Member member = memberService.findOne(user.getId());
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        String mbPassword = member.getJyPassword();
        Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));
        //edit by tansitao 时间： 2018/7/31 原因：修改条件查询方法，添加用户id查询
        LockCoinDetail lockCoinDetail = lockCoinDetailService.findOneByIdAndTypeAndMemberId(id, LockType.FINANCIAL_LOCK, member.getId());
        Assert.isTrue(lockCoinDetail != null, msService.getMessage("NOT_EXIST_RECORD"));
        Assert.isTrue(lockCoinDetail.getStatus() == LockStatus.LOCKED, msService.getMessage("NOT_REUNLOCK"));
        Assert.isTrue(lockCoinDetail.getPlanUnlockTime().compareTo(new Date()) <= 0, msService.getMessage("DO_NOT_REACHED"));
        try
        {
            unLockCoinDetailService.UnLockFinancialLock(settlementType, lockCoinDetail, restTemplate);
        }
        catch (Exception e){
            return MessageResult.error(e.getMessage());
        }

        return MessageResult.success();
    }

    /**
      * 用户手动申请提前解锁投资锁仓活动
      * @author tansitao
      * @time 2018/6/20 14:56 
      */
    @PostMapping("/unLockFinancialLock")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult unLockFinancialLock(@SessionAttribute(SESSION_MEMBER) AuthMember user, @RequestParam long id,@RequestParam SettlementType settlementType, @RequestParam String jyPassword)
    {
        //验证资金密码
        hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));
        Member member = memberService.findOne(user.getId());
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        String mbPassword = member.getJyPassword();
        Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));
        //edit by tansitao 时间： 2018/7/31 原因：修改条件查询方法，添加用户id查询
        LockCoinDetail lockCoinDetail = lockCoinDetailService.findOneByIdAndTypeAndMemberId(id, LockType.FINANCIAL_LOCK, member.getId());
        Assert.isTrue(lockCoinDetail != null, msService.getMessage("NOT_EXIST_RECORD"));
        Assert.isTrue(lockCoinDetail.getStatus() == LockStatus.LOCKED, msService.getMessage("NOT_REUNLOCK"));
        //修改活动份额总数
        //增加参加活动份额数
        LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOne(lockCoinDetail.getRefActivitieId());
        MessageResult result = lockCoinActivitieProjectService.decreaseBoughtAmount(lockCoinActivitieSetting.getActivitieId(), lockCoinDetail.getTotalAmount().divide(lockCoinActivitieSetting.getUnitPerAmount(),8, BigDecimal.ROUND_DOWN));
        if (result.getCode() != 0)
        {
            throw new IllegalArgumentException("NOT_HAVE_ACTIVITY");
        }
        //计算违约金
        BigDecimal damagesAmount = BigDecimal.ZERO;
        if(lockCoinActivitieSetting.getDamagesCalcType() == DamagesCalcType.PERCENT)
        {
            damagesAmount = lockCoinDetail.getTotalCNY().multiply(lockCoinActivitieSetting.getDamagesAmount());
        }
        else if(lockCoinActivitieSetting.getDamagesCalcType() == DamagesCalcType.FIXED_NUMBER)
        {
            damagesAmount = lockCoinActivitieSetting.getDamagesAmount();
        }


        //获取活动币种最新USDT价格
        PriceUtil priceUtil = new PriceUtil();
        BigDecimal coinPrice = priceUtil.getPriceByCoin(restTemplate, lockCoinDetail.getCoinUnit());
        //如果价格为0，则说明价格异常
        if(coinPrice.compareTo(BigDecimal.ZERO) == 0)
        {
            throw new IllegalArgumentException(msService.getMessage("PRICE_ERROR"));
        }
        BigDecimal usdtPrice = priceUtil.getUSDTPrice(restTemplate);
        //如果价格为0，则说明价格异常
        if(usdtPrice.compareTo(BigDecimal.ZERO) == 0)
        {
            throw new IllegalArgumentException(msService.getMessage("PRICE_ERROR"));
        }

        //计算实际活动币种收益
//        BigDecimal realEarningsCoinNum = lockCoinDetail.getTotalAmount().subtract(damagesAmount.divide(coinPrice.multiply(usdtPrice),8, BigDecimal.ROUND_DOWN));
        BigDecimal realEarningsCoinNum = lockCoinDetail.getTotalCNY().subtract(damagesAmount);//edit by tansitao 时间： 2018/7/30 原因：修改强制解锁是使用锁仓的总额来计算
        //计算实际USDT收益
        BigDecimal usdtrealEarningsNum;
        BigDecimal settlementAmount = BigDecimal.ZERO;//结算实际数量,默认是活动币种
        if(SettlementType.ACTIVITY_COIN == settlementType)
        {
            settlementAmount = realEarningsCoinNum.divide(coinPrice.multiply(usdtPrice),8, BigDecimal.ROUND_DOWN);
            //获取、更新用户理财活动钱包锁仓余额
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinDetail.getCoinUnit(),member.getId());
            isTrue(memberWallet != null,msService.getMessage("WALLET_GET_FAIL"));
            //增加理财锁仓活动币数
            MessageResult activityWalletResult = memberWalletService.updateBlanceAndLockBlance(memberWallet, settlementAmount, lockCoinDetail.getTotalAmount());
            if (activityWalletResult.getCode() != 0)
            {
                throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
            }

            //保存增加理财币种锁仓资金记录
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setAmount(realEarningsCoinNum);
            memberTransaction.setMemberId(member.getId());
            memberTransaction.setType(TransactionType.FINANCIAL_ACTIVITY);
            memberTransaction.setSymbol(lockCoinDetail.getCoinUnit());
            memberTransactionService.save(memberTransaction);
        }
        else
        {
            usdtrealEarningsNum = realEarningsCoinNum.divide(usdtPrice,8, BigDecimal.ROUND_DOWN);
            settlementAmount = usdtrealEarningsNum;
            //获取、减少用户理财活动钱包锁仓余额
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinDetail.getCoinUnit(),member.getId());
            isTrue(memberWallet != null,msService.getMessage("WALLET_GET_FAIL"));
            MessageResult activityWalletResult = memberWalletService.decreaseLockBalance(memberWallet.getId(), lockCoinDetail.getTotalAmount());
            if (activityWalletResult.getCode() != 0)
            {
                throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
            }

            //获取、增加用户USDT钱包余额
            MemberWallet usdtMemberWallet = memberWalletService.findByCoinUnitAndMemberId("USDT",member.getId());
            if(usdtMemberWallet == null)
            {
                usdtMemberWallet = memberWalletService.createMemberWallet(user.getId(), coinService.findByUnit("USDT"));
            }
            MessageResult usdtWalletResult = memberWalletService.increaseBalance(usdtMemberWallet.getId(), settlementAmount);
            if(usdtWalletResult.getCode() != 0 )
            {
                throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
            }

            //保存增加理财币种锁仓资金记录
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setAmount(usdtrealEarningsNum);
            memberTransaction.setMemberId(member.getId());
            memberTransaction.setType(TransactionType.FINANCIAL_ACTIVITY);
            memberTransaction.setSymbol("USDT");
            memberTransactionService.save(memberTransaction);
        }

        //修改锁仓记录信息
        lockCoinDetail.setUnlockTime(new Date());
        lockCoinDetail.setRemainAmount(BigDecimal.ZERO);
        lockCoinDetail.setStatus(LockStatus.CANCLE);
        lockCoinDetail.setCancleTime(new Date());
        lockCoinDetailService.save(lockCoinDetail);



        //添加解锁记录
        UnlockCoinDetail unlockCoinDetail = new UnlockCoinDetail();
        unlockCoinDetail.setLockCoinDetailId(lockCoinDetail.getId());
        unlockCoinDetail.setAmount(lockCoinDetail.getTotalAmount());
        unlockCoinDetail.setRemainAmount(lockCoinDetail.getRemainAmount());
        unlockCoinDetail.setPrice(coinPrice);
        unlockCoinDetail.setSettlementType(settlementType);
        unlockCoinDetail.setUsdtPriceCNY(usdtPrice);
        unlockCoinDetail.setSettlementAmount(settlementAmount);
        unlockCoinDetail.setIncomeType(IncomeType.FINANCIAL_C1);
        unLockCoinDetailService.save(unlockCoinDetail);

        return MessageResult.success();

    }

    /**
      * 获取用户某个活动的锁仓记录
      * @author tansitao
      * @time 2018/6/20 15:12 
      */
    @PostMapping("/lockCoinDial")
    public MessageResult lockCoinDial(@SessionAttribute(SESSION_MEMBER) AuthMember user, PageModel pageModel,@RequestParam long activityId, @RequestParam LockType lockType) {
        PageInfo<LockCoinDetailBuilder> page = lockCoinDetailMybatisService.queryPageByMemberAndActId(activityId, user.getId(), lockType, pageModel.getPageNo(), pageModel.getPageSize());
        MessageResult mr = MessageResult.success("success");
        mr.setData(page);
        return mr;
    }


    /**
      * 获取用户的所有锁仓记录
      * @author tansitao
      * @time 2018/6/20 15:12 
      */
    @ApiOperation("获取用户的所有锁仓记录")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "锁仓类型（0商家保证金，1手动锁仓，2锁仓活动，3理财锁仓，4SLB节点产品，5STO锁仓，6STO增值计划）",name = "lockType")
    })
    @PostMapping("/allLockCoinDial")
    public MessageRespResult<PageInfo<LockCoinDetailBuilder>> allLockCoinDial(@SessionAttribute(SESSION_MEMBER) AuthMember user,PageModel pageModel, @RequestParam LockType lockType) {
        PageInfo<LockCoinDetailBuilder> page = lockCoinDetailMybatisService.queryPageByMember(user.getId(), lockType.getOrdinal(), pageModel.getPageNo(), pageModel.getPageSize());
        MessageRespResult mr = MessageRespResult.success("success");
        mr.setData(page);
        return mr;
    }

    /**
      * 获取用户的单条锁仓记录
      * @author tansitao
      * @time 2018/6/20 15:12 
      */
    @PostMapping("/oneLockCoinDial")
    public MessageResult oneLockCoinDial(@SessionAttribute(SESSION_MEMBER) AuthMember user, @RequestParam long id) {
        LockCoinDetailBuilder lockCoinDetailBuilder = lockCoinDetailMybatisService.findOneById(id);
        //通过结算类型，计算锁仓收益金额
        if(lockCoinDetailBuilder.getStatus() == LockStatus.UNLOCKED){
            if(lockCoinDetailBuilder.getSettlementType() == SettlementType.USDT_SETTL){
                lockCoinDetailBuilder.setLockIncomeCNY(lockCoinDetailBuilder.getUnLockUSDTPriceCNY().multiply(lockCoinDetailBuilder.getSettlementIncome()));
            }else{
                lockCoinDetailBuilder.setLockIncomeCNY(lockCoinDetailBuilder.getUnLockPriceCNY().multiply(lockCoinDetailBuilder.getSettlementIncome()));
            }
        }
        //判断锁仓活动是否达到解锁时间
        if(lockCoinDetailBuilder.getPlanUnlockTime().compareTo(new Date()) >= 0){
            lockCoinDetailBuilder.setIsOverdue(BooleanEnum.IS_FALSE);
        }else{
            lockCoinDetailBuilder.setIsOverdue(BooleanEnum.IS_TRUE);
        }

        MessageResult mr = MessageResult.success("success");
        mr.setData(lockCoinDetailBuilder);
        return mr;
    }

    /**
      * 获取手动充值锁仓记录
      * @author tansitao
      * @time 2018/6/22 17:00 
      */
    @PostMapping("/handleLockCoinDial")
    public MessageResult handleLockCoinDial(@SessionAttribute(SESSION_MEMBER) AuthMember user,PageModel pageModel) {
        PageInfo<UnLockCoinDetailBuilder> page = lockCoinDetailMybatisService.queryPageHandleLockByMember(user.getId(), pageModel.getPageNo(), pageModel.getPageSize());
        MessageResult mr = MessageResult.success("success");
        mr.setData(page);
        return mr;
    }

    /**
      * 获取手动充值解锁记录
      * @author tansitao
      * @time 2018/6/22 17:00 
      */
    @PostMapping("/handleUnLockCoinDial")
    public MessageResult handleUnLockCoinDial(@SessionAttribute(SESSION_MEMBER) AuthMember user,PageModel pageModel, @RequestParam long lockCoinDetailId )
    {
        List<UnlockCoinDetail> unlockCoinDetails = lockCoinDetailMybatisService.findHandleUnLockByMember(lockCoinDetailId, pageModel.getPageNo(), pageModel.getPageSize());
        MessageResult mr = MessageResult.success("success");
        mr.setData(unlockCoinDetails);
        return mr;
    }



    /**
     * 参加SLB节点产品
     * @author fumy
     * @time 2018.07.24 11:51
     * @param user
     * @param id
     * @param cnyAmount
     * @param jyPassword
     * @return true
     */
    @PostMapping("/joinQuantifyLock")
    public MessageResult joinQuantifyLock(@SessionAttribute(SESSION_MEMBER) AuthMember user,
                                          @RequestParam long id, @RequestParam BigDecimal cnyAmount, @RequestParam String jyPassword)
    {
        log.info("【SLB节点产品】------------------------------->");
        //首先验证购买金额是否达到最低条件
        isTrue(cnyAmount.compareTo(BigDecimal.valueOf(10000L))>=0,"低于最低的购买金额");
        //验证资金密码
        hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));
        Member member = memberService.findOne(user.getId());
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        String mbPassword = member.getJyPassword();
        Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));
        //验证活动配置和锁仓配置是否存在
        LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOneByTime(id);
        Assert.isTrue(lockCoinActivitieSetting != null, msService.getMessage("NOT_HAVE_SET"));
        LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(lockCoinActivitieSetting.getActivitieId());
        Assert.isTrue(lockCoinActivitieProject != null, msService.getMessage("NOT_HAVE_ACTIVITY"));

        //        //获取活动币种最新USDT价格
        PriceUtil priceUtil = new PriceUtil();
//        BigDecimal activityCoinPrice = new BigDecimal("2.1");
        BigDecimal activityCoinPrice = priceUtil.getPriceByCoin(restTemplate, lockCoinActivitieSetting.getCoinSymbol());
//        //如果价格为0，则说明价格异常
        if(activityCoinPrice.compareTo(BigDecimal.ZERO) == 0)
        {
            throw new IllegalArgumentException(msService.getMessage("PRICE_ERROR"));
        }

        //获取usdt的人民币价格
//        BigDecimal cnyPrice = new BigDecimal("6.72");
        BigDecimal cnyPrice = priceUtil.getUSDTPrice(restTemplate);
        if(cnyPrice.compareTo(BigDecimal.ZERO) == 0)
        {
            throw new IllegalArgumentException(msService.getMessage("PRICE_ERROR"));
        }
        //计算量化购买总CNY金额对应的usdt币总数,cnyAmount / cnyPrice (人民币金额 / usdt 人民币价格)
        BigDecimal totalAmount = cnyAmount.divide(cnyPrice,8, BigDecimal.ROUND_DOWN);

        //计算对应量化投资锁仓的币种（ex：SLB）数量, totalAmout / activityCoinPrice ( usdt总量 / 量化投资币种usdt价格)
        BigDecimal qutifyCoinNum = totalAmount.divide(activityCoinPrice,8,BigDecimal.ROUND_DOWN);

        //获取、减少用户USDT钱包余额
        MemberWallet usdtMemberWallet = memberWalletService.findCacheByCoinUnitAndMemberId("USDT",member.getId());

        LockCoinDetail lockCoinDetail  = getController().joinQuantifyLock1(  member,
                lockCoinActivitieSetting,  usdtMemberWallet,
                cnyAmount,
                totalAmount ,  qutifyCoinNum,
                activityCoinPrice,  cnyPrice);

        //返佣异步调用
        log.info("【SLB节点产品】----------------->异步调用返佣奖励接口，id={}",lockCoinDetail.getId());
        lockCoinActivitieEvent.ansyActivityPromotionReward1(lockCoinDetail, lockCoinActivitieSetting);

        return MessageResult.success();
    }
    public ActivityController getController(){
        return SpringContextUtil.getBean(ActivityController.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public LockCoinDetail  joinQuantifyLock1(Member member,
                                             LockCoinActivitieSetting lockCoinActivitieSetting, MemberWallet usdtMemberWallet,
                                             BigDecimal cnyAmount,
                                             BigDecimal totalAmount , BigDecimal qutifyCoinNum,
                                             BigDecimal activityCoinPrice, BigDecimal cnyPrice)
    {

        if(usdtMemberWallet == null)
        {
            memberWalletService.createMemberWallet(member.getId(), coinService.findByUnit("USDT"));
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        log.info("【SLB节点产品】-------------->扣除用户 "+member.getId()+" 的SLB节点产品所需的 USDT 币数.............");
        MessageResult usdtWalletResult = memberWalletService.decreaseBalance(usdtMemberWallet.getId(), totalAmount);
        if (usdtWalletResult.getCode() != 0)
        {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }

        //获取、增加用户量化投资钱包锁仓余额
        MemberWallet activityMemberWallet = memberWalletService.findCacheByCoinUnitAndMemberId(lockCoinActivitieSetting.getCoinSymbol(),member.getId());
        if(activityMemberWallet == null)
        {
            activityMemberWallet = memberWalletService.createMemberWallet(member.getId(), coinService.findByUnit(lockCoinActivitieSetting.getCoinSymbol()));
        }
        //增加量化投资活动币数
        log.info("【SLB节点产品】-------------->增加用户 "+member.getId()+" 的 "+lockCoinActivitieSetting.getCoinSymbol()+"活动购买币数.............");
        MessageResult activityWalletResult = memberWalletService.increaseLockBalance(activityMemberWallet.getId(), qutifyCoinNum);
        if (activityWalletResult.getCode() != 0)
        {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }

        //添加锁仓记录
        LockCoinDetail lockCoinDetail = new LockCoinDetail();
        //保存锁仓详情
        int month = lockCoinActivitieSetting.getLockDays()/30;
        lockCoinDetail.setLockPrice(activityCoinPrice);
        lockCoinDetail.setPlanUnlockTime(DateUtil.addMonth(new Date(), month));
        lockCoinDetail.setMemberId(member.getId());
        lockCoinDetail.setType(LockType.QUANTIFY);
        lockCoinDetail.setCoinUnit(lockCoinActivitieSetting.getCoinSymbol());
        lockCoinDetail.setTotalAmount(qutifyCoinNum);
        lockCoinDetail.setRemainAmount(qutifyCoinNum);
        lockCoinDetail.setUsdtPriceCNY(cnyPrice);
        lockCoinDetail.setTotalCNY(cnyAmount);
        lockCoinDetail.setLockRewardSatus(LockRewardSatus.NO_REWARD);
        lockCoinDetail.setRemark("自主参加");
        log.info("【SLB节点产品】-------------->添加用户 "+member.getId()+" 的 "+lockCoinActivitieSetting.getCoinSymbol()+" 产品购买详情记录.............");

        //计算收益
        BigDecimal planIncome = lockCoinDetail.getTotalCNY().multiply(lockCoinActivitieSetting.getEarningRate());
        lockCoinDetail.setPlanIncome(planIncome);
        lockCoinDetail.setStatus(LockStatus.LOCKED);
        lockCoinDetail.setRefActivitieId(lockCoinActivitieSetting.getId());
        LockCoinDetail lockCoinDetailNew = lockCoinDetailService.save(lockCoinDetail);
        //lockCoinDetailService.save(lockCoinDetail);

        //获取ref_Id单号，关联到member_transaction记录
        String ref_id = String.valueOf(lockCoinDetail.getId());
        //保存USDT扣除资金记录
        MemberTransaction usdtMemberTransaction = new MemberTransaction();
        usdtMemberTransaction.setAmount(BigDecimal.ZERO.subtract(totalAmount));
        usdtMemberTransaction.setMemberId(member.getId());
        usdtMemberTransaction.setType(TransactionType.QUANTIFY_ACTIVITY);
        usdtMemberTransaction.setSymbol("USDT");
        usdtMemberTransaction.setRefId(ref_id);
        memberTransactionService.save(usdtMemberTransaction);

        //保存增加量化投资币种锁仓资金记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setAmount(qutifyCoinNum);
        memberTransaction.setMemberId(member.getId());
        memberTransaction.setType(TransactionType.QUANTIFY_ACTIVITY);
        memberTransaction.setSymbol(lockCoinActivitieSetting.getCoinSymbol());
        memberTransaction.setRefId(ref_id);
        memberTransactionService.save(memberTransaction);


        //返佣异步调用
        //log.info("【SLB节点产品】----------------->异步调用返佣奖励接口，id={}",lockCoinDetailNew.getId());
        //lockCoinActivitieEvent.ansyActivityPromotionReward1(lockCoinDetailNew,lockCoinActivitieSetting);

        //return MessageResult.success();
        return lockCoinDetailNew;
    }

    /**
     * 获取SLB节点产品用户累计等级
     * @author fumy
     * @time 2018.07.25 10:38
     * @param memberId 查询参数（memberId）
     * @return true
     */
    @PostMapping("/quantify/user-cny/get")
    public MessageResult queryUserQuantifyTotalCny(@RequestParam Long memberId,@RequestParam(required = false) Long activityId){

        Member member = memberService.findOne(memberId);
        MessageResult result = MessageResult.success();
        BigDecimal totalCny = lockCoinDetailService.getUserQuantifyTotalCny(memberId);
        int startLevel = 0;
        if (totalCny == null){
            startLevel = 0;
            totalCny = BigDecimal.ZERO;
        }
        //初级节点
        if( totalCny.compareTo(BigDecimal.valueOf(10000L)) >=0 ) {
            startLevel = 1;
        }
//        else {//老用户
//            if(activityId==null){
//                startLevel = 0;
//            } else {
//                LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(activityId);
//                if (member.getRegistrationTime().getTime() < lockCoinActivitieProject.getStartTime().getTime()) {
//                    MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(lockCoinActivitieProject.getCoinSymbol(), member.getId());
//                    if (memberWallet == null) {
//                        log.warn("用户钱包账户为空，ID={}", member.getId());
//                        startLevel = 0;
//                    }
//
//                    BigDecimal totalBalance = memberWallet.getBalance().add(memberWallet.getFrozenBalance()).add(memberWallet.getLockBalance());
//                    if (totalBalance.compareTo(BigDecimal.valueOf(10000L)) >= 0) {
//                        startLevel = 1;
//                    }
//                }
//            }
//        }
        //高级节点
        if( totalCny.compareTo(BigDecimal.valueOf(100000L)) >=0 ) {
            startLevel = 2;
        }
        //超级节点
        if( totalCny.compareTo(BigDecimal.valueOf(500000L)) >=0 ) {
            startLevel = 3;
        }else if(member.getId()==100529L){
            //如果是福建合伙人，则为超级节点
            startLevel = 3;
        }
        result.setData(startLevel);
        return result;
    }


    /**
     * 返佣活动列表
     * @author fumy
     * @time 2018.10.16 15:09
     * @param
     * @return true
     */
    @ApiOperation(value = "返佣活动列表",notes = "查询已经启用推荐返佣中用户奖励与佣金参数的配置列表",tags = "返佣活动查询")
    @GetMapping("reward-list")
    public MessageRespResult<RewardActivityVo> getRewardActivityList(){
        List<RewardActivityVo> list = activitySettingService.getRewardList();
        return MessageRespResult.success("查询成功",list);
    }

    /**
     * 返佣活动富文本内容查询
     * @author fumy
     * @time 2018.10.16 15:10
     * @param id
     * @param tName
     * @return true
     */
    @ApiOperation(value = "返佣活动富文本内容查询",notes = "查询已经启用推荐返佣中用户奖励与佣金参数的配置富文本内容",tags = "返佣活动查询")
    @GetMapping("reward-content")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "配置id",name = "id",dataType = "long"),
            @ApiImplicitParam(value = "来源标识",name = "tName",dataType = "String")
    })
    public MessageRespResult<RewardActivityResp> getRewardActivityContent(Long id, String tName){
        if("act".equals(tName)){
            tName ="reward_activity_setting";
        }
        if("promot".equals(tName)){
            tName ="reward_promotion_setting";
        }
        return MessageRespResult.success("查询成功",activitySettingService.getRewardList(id,tName));
    }

    /**
     * 参加STO锁仓
     * @author fumy
     * @time 2018.11.05 15:14
     * @param user
     * @param id
     * @param cnyAmount
     * @param jyPassword
     * @return true
     */
    @ApiOperation(value = "参加STO锁仓",tags = "STO锁仓")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "活动配置id",name = "id",dataType = "String"),
            @ApiImplicitParam(value = "购买人民币总额",name = "cnyAmount",dataType = "String"),
            @ApiImplicitParam(value = "资金密码",name = "jyPassword",dataType = "String")
    })
    @PostMapping("/joinStoLock")
    public MessageRespResult joinSTOLock(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user,
                                          @RequestParam long id, @RequestParam BigDecimal cnyAmount, @RequestParam String jyPassword)
    {
        log.info("【STO锁仓】------------------------------->");

        //验证资金密码
        hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));
        Member member = memberService.findOne(user.getId());
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        String mbPassword = member.getJyPassword();
        Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));
        //验证活动配置和锁仓配置是否存在
        LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOneByTime(id);
        Assert.isTrue(lockCoinActivitieSetting != null, msService.getMessage("NOT_HAVE_SET"));
        //首先验证购买金额是否达到最低条件
        isTrue(cnyAmount.compareTo(lockCoinActivitieSetting.getMinBuyAmount())>=0,"低于最低的购买金额");
        LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(lockCoinActivitieSetting.getActivitieId());
        Assert.isTrue(lockCoinActivitieProject != null, msService.getMessage("NOT_HAVE_ACTIVITY"));

        //购买CNYT总数 CNYT 与 人民币汇率就是 1(现为通用STO锁仓，币种价格都为1:1,传入的购买总额就是锁仓币数总额)
        BigDecimal totalAmount = cnyAmount,activityCoinPrice=new BigDecimal(1),cnyPrice=new BigDecimal(1);

        LockCoinDetail lockCoinDetail  = getController().joinSTOLock1(  member,
                lockCoinActivitieSetting,
                cnyAmount,
                totalAmount ,  totalAmount,
                activityCoinPrice,  cnyPrice);

        //返佣异步调用
        log.info("【STO锁仓】----------------->异步调用返佣奖励接口，id={}",lockCoinDetail.getId());
        lockCoinActivitieEvent.ansyActivityPromotionReward4Sto(lockCoinDetail,lockCoinActivitieSetting);

        return MessageRespResult.success();
    }


    @Transactional(rollbackFor = Exception.class)
    public LockCoinDetail  joinSTOLock1(Member member,
                                             LockCoinActivitieSetting lockCoinActivitieSetting,
                                             BigDecimal cnyAmount,
                                             BigDecimal totalAmount , BigDecimal coinNum,
                                             BigDecimal activityCoinPrice, BigDecimal cnyPrice)
    {
        MemberWallet activityMemberWallet = memberWalletService.findCacheByCoinUnitAndMemberId(lockCoinActivitieSetting.getCoinSymbol()
                ,member.getId());
        if(activityMemberWallet == null)
        {
            memberWalletService.createMemberWallet(member.getId(), coinService.findByUnit(lockCoinActivitieSetting.getCoinSymbol()));
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        //增加用户STO锁仓CNYT数freezeBalanceToLockBalance
        log.info("【STO锁仓】-------------->增加用户 "+member.getId()+" 的 "+lockCoinActivitieSetting.getCoinSymbol()+"锁仓币数.............");
        MessageResult activityWalletResult = memberWalletService.freezeBalanceToLockBalance(activityMemberWallet, coinNum);
        if (activityWalletResult.getCode() != 0)
        {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }

        //添加锁仓记录
        LockCoinDetail lockCoinDetail = new LockCoinDetail();
        //保存锁仓详情
//        int month = lockCoinActivitieSetting.getLockDays()/30;
        lockCoinDetail.setLockPrice(activityCoinPrice);
//        lockCoinDetail.setPlanUnlockTime(DateUtil.addMonth(new Date(), month));
        //add by tansitao 时间： 2018/11/11 原因：解锁时间修改为按照天数增加
        lockCoinDetail.setPlanUnlockTime(DateUtil.addDay(new Date(), lockCoinActivitieSetting.getLockDays()));
        lockCoinDetail.setMemberId(member.getId());
        lockCoinDetail.setType(LockType.STO);
        lockCoinDetail.setCoinUnit(lockCoinActivitieSetting.getCoinSymbol());
        lockCoinDetail.setTotalAmount(totalAmount);
        lockCoinDetail.setRemainAmount(coinNum);
        lockCoinDetail.setUsdtPriceCNY(cnyPrice);
        lockCoinDetail.setTotalCNY(cnyAmount);
        lockCoinDetail.setLockRewardSatus(LockRewardSatus.NO_REWARD);
        lockCoinDetail.setRemark("自主参加");
        log.info("【STO锁仓】-------------->添加用户 "+member.getId()+" 的 "+lockCoinActivitieSetting.getCoinSymbol()+" 产品购买详情记录.............");

        //计算收益
        BigDecimal planIncome = lockCoinDetail.getTotalAmount().multiply(lockCoinActivitieSetting.getEarningRate());
        lockCoinDetail.setPlanIncome(planIncome);
        lockCoinDetail.setStatus(LockStatus.LOCKED);
        lockCoinDetail.setRefActivitieId(lockCoinActivitieSetting.getId());
        LockCoinDetail lockCoinDetailNew = lockCoinDetailService.save(lockCoinDetail);
        //lockCoinDetailService.save(lockCoinDetail);

        //获取ref_Id单号，关联到member_transaction记录
        String ref_id = String.valueOf(lockCoinDetail.getId());
        //保存CNYT扣除资金记录
        MemberTransaction usdtMemberTransaction = new MemberTransaction();
        usdtMemberTransaction.setAmount(BigDecimal.ZERO.subtract(totalAmount));
        usdtMemberTransaction.setMemberId(member.getId());
        //edit by tansitao 时间： 2018/11/20 原因：修改活动类型，之前的类型多了"_"
        usdtMemberTransaction.setType(TransactionType.STO_ACTIVITY);
        usdtMemberTransaction.setSymbol(lockCoinActivitieSetting.getCoinSymbol());
        usdtMemberTransaction.setRefId(ref_id);
        memberTransactionService.save(usdtMemberTransaction);
        return lockCoinDetailNew;
    }
}
