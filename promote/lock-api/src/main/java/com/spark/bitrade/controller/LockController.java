package com.spark.bitrade.controller;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.config.LockConfig;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.*;
import com.spark.bitrade.service.cnyt.LockIncomeService;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.*;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.*;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static org.springframework.util.Assert.*;

/**
 * @author fumy
 * @time 2018.12.03 15:48
 */
@RestController
@RequestMapping("/lock")
@Slf4j
public class LockController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private MemberTransactionService memberTransactionService;

    @Autowired
    private LockCoinActivitieProjectService lockCoinActivitieProjectService;
    @Autowired
    private LockCoinActivitieSettingService lockCoinActivitieSettingService;

    @Autowired
    private LockCoinDetailMybatisService lockCoinDetailMybatisService;

    @Autowired
    private LockCoinDetailService lockCoinDetailService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private LockMarketRewardDetailService lockMarketRewardDetailService;

    @Autowired
    private LockMarketRewardIncomePlanService lockMarketRewardIncomePlanService;

    @Autowired
    private LockMarketPerformanceTotalService lockMarketPerformanceTotalService;

    @Autowired
    private LockIncomeService lockIncomeService;

    @Autowired
    private LockMemberIncomePlanService memberIncomePlanService;

    @Autowired
    private LockMarketLevelService lockMarketLevelService;

    @Autowired
    private LockConfig lockConfig;

    @Autowired
    private LockBttcActivitieService lockBttcActivitieService;
    @Autowired
    private ISilkDataDistService silkDataDistService;

    @Value("${lock.default.level:-}")
    private String defaultLevel;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 获取用户锁仓记录明细列表
     *
     * @param user
     * @return true
     * @author fumy
     * @time 2018.12.03 15:58
     */
    @ApiOperation(value = "获取用户锁仓记录明细列表", tags = "STO-v2.0锁仓")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页显示条数", required = true),
            @ApiImplicitParam(name = "activityId", value = "大活动id", required = true),
            @ApiImplicitParam(name = "lockType", value = "锁仓类型（0商家保证金、1员工锁仓、2锁仓活动、3理财锁仓、4SLB节点产品、5STO锁仓,6STO增值计划", required = true),
            @ApiImplicitParam(name = "memberId", value = "用户id,可空")
    })
    @PostMapping("/record/list")
    public MessageRespResult<PageData<StoLockDetailVo>> getLockRecordList(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, int pageNo, int pageSize,
                                                                          long activityId, LockType lockType, Long memberId) {

        PageInfo<LockCoinDetailBuilder> page = lockCoinDetailMybatisService.queryPageByMemberAndActId(activityId, user.getId(), lockType, pageNo, pageSize);
        PageInfo<StoLockDetailVo> pageInfo = new PageInfo<>();
        pageInfo.setPageSize(page.getPageSize());
        pageInfo.setPageNum(page.getPageNum());
        pageInfo.setPages(page.getPages());
        pageInfo.setTotal(page.getTotal());

        List<StoLockDetailVo> resList = new ArrayList<>();
        StoLockDetailVo stoLockDetailVo = null;
        for (int i = 0; i < page.getList().size(); i++) {
            stoLockDetailVo = new StoLockDetailVo();
            stoLockDetailVo.setLockCoinDetail(page.getList().get(i));

            if (DateUtil.stringToDate(lockConfig.getUpdateVersionDate())
                    .compareTo(stoLockDetailVo.getLockCoinDetail().getLockTime()) > 0) {
                //edit by yangch 时间： 2019.03.17 原因：兼容历史数据，按月返回的显示

                //计算分期数，30天为一期
                stoLockDetailVo.setDaysOfPeroid(30);
                Double cycle = page.getList().get(i).getCycle();

                //计算每期结算数量
                BigDecimal planIncome = page.getList().get(i).getPlanIncome();
                BigDecimal periodIncome = planIncome.divide(new BigDecimal(cycle.intValue()), 8, BigDecimal.ROUND_DOWN);
                stoLockDetailVo.setNextRewardTurnover(periodIncome);

                //根据锁仓记录获取待还收益期数
                int waitBack = memberIncomePlanService.countWaitBack(stoLockDetailVo.getLockCoinDetail().getId());

                //计算得到已经返回的期数
                int alreadyBack = cycle.intValue() - waitBack;
                stoLockDetailVo.setIncomePeroid(alreadyBack + 1);
                //计算下一期到账时间
                Date nextRewardTime = DateUtil.addDay(stoLockDetailVo.getLockCoinDetail().getLockTime(),
                        (alreadyBack + 1) * stoLockDetailVo.getDaysOfPeroid());
                stoLockDetailVo.setNextRewardTime(nextRewardTime);
            } else {
                //add by yangch 时间： 2019.03.17 原因：修改为按天返还的显示
                //计算分期数，30天为一期(更改为按天返回)
                stoLockDetailVo.setDaysOfPeroid(lockConfig.getCycle());
                Double cycle = page.getList().get(i).getCycle() * 30;
                stoLockDetailVo.getLockCoinDetail().setCycle(cycle);

                //计算每期结算数量
                BigDecimal planIncome = page.getList().get(i).getPlanIncome();
                BigDecimal periodIncome = planIncome.divide(new BigDecimal(cycle.intValue()), 8, BigDecimal.ROUND_DOWN);
                stoLockDetailVo.setNextRewardTurnover(periodIncome);

                //根据锁仓记录获取待还收益期数
                int waitBack = memberIncomePlanService.countWaitBack(stoLockDetailVo.getLockCoinDetail().getId());

                //计算得到已经返回的期数
                int alreadyBack = cycle.intValue() - waitBack;
                stoLockDetailVo.setIncomePeroid(alreadyBack + 1);
                //计算下一期到账时间
                Date nextRewardTime = DateUtil.addDay(stoLockDetailVo.getLockCoinDetail().getLockTime(), (alreadyBack + 1) * lockConfig.getCycle());
                stoLockDetailVo.setNextRewardTime(nextRewardTime);
            }

            resList.add(stoLockDetailVo);
        }
        pageInfo.setList(resList);

        return MessageRespResult.success("查询成功", PageData.toPageData(pageInfo));
    }

    /**
     * 获取用户的级别和佣金奖励信息
     *
     * @param user
     * @return true
     * @author fumy
     * @time 2018.12.03 15:59
     */
    @ApiOperation(value = "获取用户的级别和佣金奖励信息", tags = "STO-v2.0锁仓")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memberId", value = "用户id,可空"),
            @ApiImplicitParam(name = "symbol", value = "币种，默认为CNYT"),
            @ApiImplicitParam(name = "startTime", value = "开始时间"),
            @ApiImplicitParam(name = "endTime", value = "结束时间")
    })
    @PostMapping("/level-reward/info")
    public MessageRespResult<StoMemberInfoVo> getLevelAndRewardInfo(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, String symbol, Long memberId,
                                                                    String startTime, String endTime) {
        symbol = withCompatibilitySymbol(symbol);
        StoMemberInfoVo stoMemberInfoVo = new StoMemberInfoVo();
        //查询职务
        LockMarketLevel lockMarketLevel = lockMarketLevelService.findByMemberId(user.getId(), symbol);
        String position = lockMarketLevel.getLevel();
        if (position == null || position.equals("")) {
            position = defaultLevel;
        }
        //查询奖励信息
        //到账奖励
        StoMemberInfoVo backAmount = lockMarketRewardIncomePlanService.findAllByBacked(user.getId(), symbol, startTime, endTime);
        stoMemberInfoVo.setReferrerArrived(backAmount.getReferrerArrived());
        stoMemberInfoVo.setCrossArrived(backAmount.getCrossArrived());
        stoMemberInfoVo.setTrainingArrived(backAmount.getTrainingArrived());
        //总奖励
        StoMemberInfoVo amountTotal = lockMarketRewardIncomePlanService.findAllReward(user.getId(), symbol, startTime, endTime);
        stoMemberInfoVo.setReferrerAmount(amountTotal.getReferrerAmount());
        stoMemberInfoVo.setCrossAmount(amountTotal.getCrossAmount());
        stoMemberInfoVo.setTrainingAmount(amountTotal.getTrainingAmount());
        stoMemberInfoVo.setMemberId(user.getId());
        stoMemberInfoVo.setPosition(position);
        return MessageRespResult.success("查询成功", stoMemberInfoVo);
    }

    /**
     * 获取用户子部门（Subdivision，缩写：Sub）锁仓汇总信息
     *
     * @param user
     * @return true
     * @author fumy
     * @time 2018.12.03 16:06
     */
    @ApiOperation(value = "获取用户子部门锁仓汇总信息", notes = "子部门（Subdivision，缩写：Sub）", tags = "STO-v2.0锁仓")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页显示条数", required = true),
            @ApiImplicitParam(name = "memberId", value = "用户id,可空"),
            @ApiImplicitParam(name = "symbol", value = "币种单位，可空，默认查询CNYT", dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间"),
            @ApiImplicitParam(name = "endTime", value = "结束时间")
    })
    @PostMapping("/subdivision/record/collect")
    public MessageRespResult<PageData<StoLockDepVo>> getSubLockRecordCollect(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, int pageNo, int pageSize,
                                                                             Long memberId, String symbol, String startTime, String endTime) {
        symbol = withCompatibilitySymbol(symbol);
        PageInfo<StoLockDepVo> pageInfo = lockMarketRewardDetailService.findTotalByInivite(user.getId(), symbol, pageNo, pageSize, startTime, endTime);
        return MessageRespResult.success("查询成功", PageData.toPageData(pageInfo));
    }

    /**
     * 获取用户子部门（Subdivision，缩写：Sub）锁仓流水信息
     *
     * @param user
     * @return true
     * @author fumy
     * @time 2018.12.03 16:07
     */
    @ApiOperation(value = "获取用户子部门（Subdivision，缩写：Sub）锁仓流水信息", notes = "子部门（Subdivision，缩写：Sub）", tags = "STO-v2.0锁仓")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页显示条数", required = true),
            @ApiImplicitParam(name = "memberId", value = "会员id，可空"),
            @ApiImplicitParam(name = "symbol", value = "币种单位，可空，默认查询CNYT", dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", type = "datetime"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", type = "datetime")
    })
    @PostMapping("/subdivision/record/list")
    public MessageRespResult<PageData<StoLockDepDetailVo>> getSubLockRecordList(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, Long memberId,
                                                                                String symbol, String startTime, String endTime, int pageNo, int pageSize) {
        symbol = withCompatibilitySymbol(symbol);
        PageInfo<StoLockDepDetailVo> pageInfo = lockMarketRewardDetailService.findDepByInviter(user.getId(), symbol, startTime, endTime, pageNo, pageSize);
        return MessageRespResult.success("查询成功", PageData.toPageData(pageInfo));
    }

    /**
     * 获取用户奖励收益列表
     *
     * @param user
     * @param memberId
     * @param startTime
     * @param endTime
     * @return true
     * @author fumy
     * @time 2018.12.04 14:00
     */
    @ApiOperation(value = "获取用户奖励收益列表", notes = "查询用户各子部门的奖励收益记录", tags = "STO-v2.0锁仓")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页显示条数", required = true),
            @ApiImplicitParam(name = "memberId", value = "会员id，可空"),
            @ApiImplicitParam(name = "symbol", value = "币种单位，可空，默认查询CNYT", dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间"),
            @ApiImplicitParam(name = "endTime", value = "结束时间")
    })
    @PostMapping("/reward/income/list")
    public MessageRespResult<PageData<StoLockIncomeVo>> getIncomeRecordList(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, int pageNo, int pageSize,
                                                                            Long memberId, String symbol,
                                                                            String startTime, String endTime) {
        symbol = withCompatibilitySymbol(symbol);
        PageInfo<StoLockIncomeVo> pageInfo = lockMarketRewardDetailService.findMemberRewardIncome(user.getId(), symbol, startTime, endTime, pageNo, pageSize);

        //edit by yangch 时间： 2019.03.17 原因：兼容按天返回的显示
        if (pageInfo.getList() != null) {
            pageInfo.getList().forEach(item -> {
                if (DateUtil.stringToDate(lockConfig.getUpdateVersionDate())
                        .compareTo(item.getLockTime()) < 0) {
                    //兼容升级后的数据按天返还的显示
                    item.setIncomePeriod(0);
                    item.setCurrentPeriod(0);
                    item.setDaysOfPeroid(lockConfig.getCycle());
                } else {
                    item.setDaysOfPeroid(30);
                }
            });
        }

        return MessageRespResult.success("查询成功", PageData.toPageData(pageInfo));
    }

    /**
     * 用户参加STO增值计划锁仓
     *
     * @param user
     * @param id
     * @param cnyAmount
     * @param jyPassword
     * @return true
     * @author fumy
     * @time 2018.12.04 14:00
     */
    @ApiOperation(value = "参加STO增值计划锁仓", tags = "STO-v2.0锁仓")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "活动配置id", name = "id", dataType = "String", required = true),
            @ApiImplicitParam(value = "购买人民币总额", name = "cnyAmount", dataType = "String", required = true),
            @ApiImplicitParam(value = "资金密码", name = "jyPassword", dataType = "String", required = true)
    })
    @PostMapping("/joinStoLock")
    public MessageRespResult joinSTOLock(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user,
                                         long id, BigDecimal cnyAmount, String jyPassword) {
        log.info("【STO锁仓】------------------------------->");

        //验证资金密码
        hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));
        Member member = memberService.findOne(user.getId());
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        String mbPassword = member.getJyPassword();
        hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));
        //验证活动配置和锁仓配置是否存在
        LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOneByTime(id);
        isTrue(lockCoinActivitieSetting != null, msService.getMessage("NOT_HAVE_SET"));
        //add by young 时间： 2019.05.22 原因：判断活动是否有效
        isTrue(lockCoinActivitieSetting.getStatus() == LockSettingStatus.VALID, "无效的活动");

        //首先验证购买金额是否达到最低条件
        isTrue(cnyAmount.compareTo(lockCoinActivitieSetting.getMinBuyAmount()) >= 0, "低于最低的购买金额");
        //验证购买数量与已参与购买数量之和，是否大于最大计划总量
        BigDecimal maxPlanAmount = cnyAmount.add(lockCoinActivitieSetting.getBoughtAmount());
        isTrue(maxPlanAmount.compareTo(lockCoinActivitieSetting.getPlanAmount()) < 1, "已超出活动计划的购买金额");


        LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(lockCoinActivitieSetting.getActivitieId());
        isTrue(lockCoinActivitieProject != null, msService.getMessage("NOT_HAVE_ACTIVITY"));

        //购买CNYT总数 CNYT 与 人民币汇率就是 1(现为通用STO锁仓，币种价格都为1:1,传入的购买总额就是锁仓币数总额)
        BigDecimal totalAmount = cnyAmount, activityCoinPrice = new BigDecimal(1), cnyPrice = new BigDecimal(1);

        LockCoinDetail lockCoinDetail = getController().joinSTOLock1(member,
                lockCoinActivitieSetting,
                cnyAmount,
                totalAmount, totalAmount,
                activityCoinPrice, cnyPrice);

        //异步调用返佣奖励消息通知接口
        log.info("【STO锁仓】----------------->异步调用返佣奖励消息通知服务接口，id={}", lockCoinDetail.getId());
        lockIncomeService.dealIncomeMessage(lockCoinDetail);

        return MessageRespResult.success();
    }


    @Transactional(rollbackFor = Exception.class)
    public LockCoinDetail joinSTOLock1(Member member,
                                       LockCoinActivitieSetting lockCoinActivitieSetting,
                                       BigDecimal cnyAmount,
                                       BigDecimal totalAmount, BigDecimal coinNum,
                                       BigDecimal activityCoinPrice, BigDecimal cnyPrice) {
        MemberWallet activityMemberWallet = memberWalletService.findCacheByCoinUnitAndMemberId(lockCoinActivitieSetting.getCoinSymbol()
                , member.getId());
        if (activityMemberWallet == null) {
            memberWalletService.createMemberWallet(member.getId(), coinService.findByUnit(lockCoinActivitieSetting.getCoinSymbol()));
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }
        //增加用户STO锁仓CNYT数freezeBalanceToLockBalance
        log.info("【STO锁仓】-------------->增加用户 " + member.getId() + " 的 " + lockCoinActivitieSetting.getCoinSymbol() + "锁仓币数.............");
        MessageResult activityWalletResult = memberWalletService.freezeBalanceToLockBalance(activityMemberWallet, coinNum);
        if (activityWalletResult.getCode() != 0) {
            throw new IllegalArgumentException(msService.getMessage("INSUFFICIENT_BALANCE"));
        }

        //添加锁仓记录
        LockCoinDetail lockCoinDetail = new LockCoinDetail();
        lockCoinDetail.setLockPrice(activityCoinPrice);
        lockCoinDetail.setPlanUnlockTime(DateUtil.addDay(new Date(), lockCoinActivitieSetting.getLockDays()));
        lockCoinDetail.setMemberId(member.getId());
        lockCoinDetail.setType(LockType.STO_CNYT);
        lockCoinDetail.setCoinUnit(lockCoinActivitieSetting.getCoinSymbol());
        lockCoinDetail.setTotalAmount(totalAmount);
        lockCoinDetail.setRemainAmount(coinNum);
        lockCoinDetail.setUsdtPriceCNY(cnyPrice);
        lockCoinDetail.setTotalCNY(cnyAmount);
        lockCoinDetail.setLockRewardSatus(LockRewardSatus.NO_REWARD);
        lockCoinDetail.setRemark("自主参加");
        log.info("【STO锁仓】-------------->添加用户 " + member.getId() + " 的 " + lockCoinActivitieSetting.getCoinSymbol() + " 产品购买详情记录.............");

        //计算收益
        BigDecimal planIncome = lockCoinDetail.getTotalAmount().multiply(lockCoinActivitieSetting.getEarningRate());
        lockCoinDetail.setPlanIncome(planIncome);
        lockCoinDetail.setStatus(LockStatus.LOCKED);
        lockCoinDetail.setRefActivitieId(lockCoinActivitieSetting.getId());
        LockCoinDetail lockCoinDetailNew = lockCoinDetailService.save(lockCoinDetail);

        //更新活动参与数量
        BigDecimal newBoughtAmount = lockCoinActivitieSetting.getBoughtAmount().add(totalAmount);
        lockCoinActivitieSetting.setBoughtAmount(newBoughtAmount);
        lockCoinActivitieSettingService.save(lockCoinActivitieSetting);

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

    public LockController getController() {
        return SpringContextUtil.getBean(LockController.class);
    }

    /**
     * 获取所有的活动信息
     *
     * @param activitieId
     * @return true
     * @author fumy
     * @time 2018.12.06 16:52
     */
    @PostMapping("/lockActivitySetting/{activitieId}")
    public MessageResult lockActivitySetting(@PathVariable(value = "activitieId") long activitieId) throws Exception {
        List<LockActivitySettingBuilder> laSettingBuilderList = new ArrayList<LockActivitySettingBuilder>();
        List<LockCoinActivitieSetting> lcActivitieSettingList = lockCoinActivitieSettingService.findByActivitieId(activitieId);
        //判断是否有子活动配置
        if (lcActivitieSettingList == null || lcActivitieSettingList.size() == 0) {
            throw new IllegalArgumentException(msService.getMessage("NOT_HAVE_SET"));
        }
        //遍历配置，处理部分数据，过滤不需要的数据
        for (LockCoinActivitieSetting lockCoinActivitieSetting : lcActivitieSettingList) {
            //获取锁币周期（月）
            double month = (double) lockCoinActivitieSetting.getLockDays() / 30;
            //获取总活动
            LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(lockCoinActivitieSetting.getActivitieId());
            //获取预计收益
            BigDecimal planIncome = lockCoinActivitieProject.getUnitPerAmount().multiply(BigDecimal.valueOf(10)).multiply(lockCoinActivitieSetting.getEarningRate()).multiply(BigDecimal.valueOf(month / 12));
            LockActivitySettingBuilder lockActivitySettingBuilder = LockActivitySettingBuilder.builder()
                    .boughtAmount(lockCoinActivitieSetting.getBoughtAmount())
                    .cycle((double) lockCoinActivitieSetting.getLockDays() / 30)
                    .earningRate(lockCoinActivitieSetting.getEarningRate())
                    .endTime(lockCoinActivitieSetting.getEndTime())
                    .startTime(lockCoinActivitieSetting.getStartTime())
                    .id(lockCoinActivitieSetting.getId())
                    .maxBuyAmount(lockCoinActivitieSetting.getMaxBuyAmount())
                    .minBuyAmount(lockCoinActivitieSetting.getMinBuyAmount())
                    .name(lockCoinActivitieSetting.getName())
                    .planAmount(lockCoinActivitieSetting.getPlanAmount())
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
     * 获取直接部门列表
     *
     * @param user
     * @author Zhang Yanjun
     * @time 2018.12.25 14:55
     */
    @ApiOperation(value = "获取直接部门列表", tags = "STO-v2.0锁仓")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "币种单位，为空查全部", name = "symbol", dataType = "String"),
            @ApiImplicitParam(value = "页码，ex:1", name = "pageNo", dataType = "int"),
            @ApiImplicitParam(value = "显示条数，ex:10", name = "pageSize", dataType = "int")

    })
    @PostMapping("sub")
    public MessageRespResult<StoSubInfoVo> subMember(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, String symbol, int pageNo, int pageSize) {
        PageInfo<StoSubInfoVo> pageInfo = lockMarketLevelService.findSubInfoByMemberId(user.getId(),
                defaultLevel, symbol, pageNo, pageSize);
        int count = memberService.getOneInviteeNumd(user.getId());
        Map<String, Object> map = new HashMap<>();
        map.put("info", PageData.toPageData(pageInfo));
        map.put("count", count);
        return MessageRespResult.success("查询成功", map);
    }

    /**
     * 币种兼容性处理
     *
     * @param symbol
     * @return
     */
    private String withCompatibilitySymbol(String symbol) {
        if (StringUtils.isEmpty(symbol)) {
            symbol = LockConstant.DEFAULT_CNYT_SYMBOL;
        }
        return symbol;
    }


    /**
     * 统计所有收益和未解锁锁仓数
     *
     * @param
     * @author Zhang Yanjun
     * @time 2019.01.18 10:04
     */
    @ApiOperation(value = "统计所有收益和未解锁锁仓数", tags = "STO-v2.0锁仓")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "锁仓类型,6", name = "type", dataType = "int", required = true),
            @ApiImplicitParam(value = "币种单位，可空，默认查询CNYT", name = "symbol", dataType = "String")
    })
    @PostMapping("lockInfo")
    public MessageRespResult lockInfo(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, LockType type, String symbol) {
        //edit by fumy .兼容多币种，默认CNYT
        symbol = withCompatibilitySymbol(symbol);
        return MessageRespResult.success("查询成功", lockCoinDetailService.statTotalAndIncome(user.getId(), type, symbol));
    }

    /**
     * SLU锁仓活动数据重做调起接口
     *
     * @param param
     * @return
     */
    @GetMapping("reward/redo")
    public MessageRespResult rewardRedo(String param) {
        Long id = Long.valueOf(param);
        //查询该条锁仓记录的返佣记录
//        boolean isExist = lockCoinDetailMybatisService.isExistRewardRecord(id);
//        if(isExist){
//            log.info("该锁仓记录已存在返佣数据，请核对锁仓ID.....");
//            return MessageRespResult.error("查询失败,该锁仓记录已存在返佣数据，请核对锁仓ID");
//        }
        LockCoinDetail lockCoinDetail = lockCoinDetailMybatisService.getMissRewardLock(id);
        if (lockCoinDetail == null) {
            log.info("没有查到锁仓记录，请核对锁仓ID......");
            return MessageRespResult.error("查询失败，没有查到锁仓记录，请核对锁仓ID");
        }

        if (lockCoinDetail.getLockRewardSatus() != LockRewardSatus.NO_REWARD) {
            log.info("该锁仓记录已存在返佣数据，请核对锁仓ID.....");
            return MessageRespResult.error("该锁仓记录的状态不是‘未返佣’状态，请核对锁仓ID");
        }

        log.info("STO锁仓活动重新生成返佣预处理数据...........");
        //不存在返佣记录时，重新发送消息通知生成的返佣预处理数据
        lockIncomeService.dealIncomeMessage(lockCoinDetail);
        return MessageRespResult.success("查询成功");
    }

    /**
     * IEO活动配置
     */
    @ApiOperation(value = "IEO活动配置", tags = "IEO-v1.0锁仓")
    @PostMapping("/ieoActivitySetting")
    public MessageResult ieoActivitySetting() {
        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("IEO_ACTIVITY", "SETTING_ID");
        SilkDataDist symbolDist = silkDataDistService.findByIdAndKey("IEO_ACTIVITY", "BASE_SYMBOL");
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
                    .cycle(lockCoinActivitieSetting.getLockCycle())
                    .lockDays(lockCoinActivitieSetting.getLockDays())
                    .cycleDays(lockCoinActivitieSetting.getCycleDays())
                    .beginDays(lockCoinActivitieSetting.getBeginDays())
                    .cycleRatio(lockCoinActivitieSetting.getCycleRatio())
                    .build();
            MessageResult mr = MessageResult.success();
            mr.setData(Collections.singletonList(lockActivitySettingBuilder));
            return mr;
        } else {
            return MessageResult.error("获取配置失败");
        }
    }

    /**
     * IEO锁仓活动
     *
     * @author wsy
     * @time 2019年4月17日10:12:07
     */
    @ApiOperation(value = "IEO锁仓活动", tags = "IEO-v1.0锁仓")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "锁仓数量（BT 数量）", name = "amount", dataType = "String", required = true),
            @ApiImplicitParam(value = "交易密码", name = "password", dataType = "String", required = true)
    })
    @PostMapping("/joinIeoLock")
    public MessageRespResult lockBttc(@SessionAttribute(SESSION_MEMBER) AuthMember user, BigDecimal amount, String password) {
        //验证资金密码
        hasText(password, msService.getMessage("MISSING_JYPASSWORD"));
        Member member = memberService.findOne(user.getId());
        String mbPassword = member.getJyPassword();
        String jyPass = new SimpleHash("md5", password, member.getSalt(), 2).toHex().toLowerCase();
        hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));

        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("IEO_ACTIVITY", "SETTING_ID");
        isTrue(dataDist != null && dataDist.getStatus() == BooleanEnum.IS_TRUE, "活动配置无效");
        SilkDataDist symbolDist = silkDataDistService.findByIdAndKey("IEO_ACTIVITY", "BASE_SYMBOL");
        isTrue(symbolDist != null && symbolDist.getStatus() == BooleanEnum.IS_TRUE, "活动配置无效");
        //验证活动配置和锁仓配置是否存在
        long id = ConvertUtils.lookup(Long.class).convert(Long.class, dataDist.getDictVal());
        LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOneByTime(id);
        notNull(lockCoinActivitieSetting, "IEO活动不存在");
        LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(lockCoinActivitieSetting.getActivitieId());
        notNull(lockCoinActivitieProject, "活动方案不存在");
        isTrue(lockCoinActivitieProject.getType() == ActivitieType.IEO, "活动类型错误");
        // 闪兑：BT->BTTC
        MessageResult mr = lockBttcActivitieService.exchangeBTToBTTC(restTemplate, user, symbolDist.getDictVal(), lockCoinActivitieSetting.getCoinSymbol(), amount);
        isTrue(mr.isSuccess(), mr.getMessage());
        BigDecimal bttcAmount = (BigDecimal) mr.getData();
        log.info("闪兑：{} -> {}: {}", symbolDist.getDictVal(), lockCoinActivitieSetting.getCoinSymbol(), bttcAmount);
        isTrue(bttcAmount.compareTo(BigDecimal.ZERO) > 0, "BT兑换BTTC失败，请检测余额");
        boolean lock = false;
        try {
            // 首先验证购买金额是否达到最低限额
            isTrue(bttcAmount.compareTo(lockCoinActivitieSetting.getMinBuyAmount()) >= 0, "低于最低购买数量");
            // 验证购买金额是否达到最高购买限额
            isTrue(bttcAmount.compareTo(lockCoinActivitieSetting.getMaxBuyAmount()) <= 0, "高于最大购买数量");
            // 验证账户总限额
            double total = lockCoinDetailService.findByMemberIdAndId(member.getId(), id).stream().mapToDouble(i -> i.getTotalAmount().doubleValue()).sum();
            isTrue(lockCoinActivitieSetting.getMaxBuyAmount().compareTo(new BigDecimal(total).add(bttcAmount)) > 0, "已经超出账户购买限额");
            //验证购买数量与已参与购买数量之和，是否大于最大计划总量
            BigDecimal maxPlanAmount = bttcAmount.add(lockCoinActivitieSetting.getBoughtAmount());
            isTrue(maxPlanAmount.compareTo(lockCoinActivitieSetting.getPlanAmount()) < 0, "已超出活动计划的购买金额");

            // 开始锁仓
            PriceUtil priceUtil = new PriceUtil();
            //获取锁仓币种人民币价格
            BigDecimal coinCnyPrice = priceUtil.getCoinCnyPrice(restTemplate, lockCoinActivitieSetting.getCoinSymbol());
            //获取锁仓币种USDT价格
            BigDecimal coinUSDTPrice = priceUtil.getCoinCnyPrice(restTemplate, lockCoinActivitieSetting.getCoinSymbol());
            //获取USDT的人民币价格
            BigDecimal usdtPrice = priceUtil.getUSDTPrice(restTemplate);
            lockBttcActivitieService.lockBttc(lockCoinActivitieSetting, user, coinCnyPrice, coinUSDTPrice, usdtPrice, bttcAmount);
            lock = true;
        } catch (Exception e) {
            log.error("IEO锁仓失败", e);
            throw e;
        } finally {
            if (!lock) {
                // 锁仓失败，闪兑：BTTC->BT
                mr = lockBttcActivitieService.exchangeBTTCToBT(restTemplate, user, symbolDist.getDictVal(), lockCoinActivitieSetting.getCoinSymbol(), bttcAmount);
                BigDecimal btAmount = (BigDecimal) mr.getData();
                log.error("锁仓失败，兑换{} -> {}: {}", lockCoinActivitieSetting.getCoinSymbol(), symbolDist.getDictVal(), btAmount);
            }
        }
        return MessageRespResult.success("锁仓成功");
    }
}
