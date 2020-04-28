package com.spark.bitrade.controller.activitie;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.event.LockCoinActivitieEvent;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PriceUtil;
import com.spark.bitrade.util.SpringContextUtil;
import com.sparkframework.security.Encrypt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;
import static org.springframework.util.Assert.isTrue;

/**
 * SLB节点产品控制类
 * @author fumy
 * @time 2018.08.06 10:51
 */
@RestController
@RequestMapping("/activity/quantifyLock")
@Slf4j
public class QuantifyLockController extends BaseAdminController {

    @Autowired
    private LockCoinActivitieProjectService lockCoinActivitieProjectService;

    @Autowired
    private LockCoinActivitieSettingService lockCoinActivitieSettingService;

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
    private CoinService coinService;

    @Autowired
    private LockCoinActivitieEvent lockCoinActivitieEvent;

    @Value("${spark.system.md5.key}")
    private String md5Key;

    /**
     * 参加SLB节点产品
     * @author fumy
     * @time 2018.11.05 11:40
     * @param admin
     * @param uid
     * @param activitieId
     * @param amount
     * @param password
     * @param coinCnyPrice
     * @param usdtCnyPrice
     * @param reWardType
     * @return true
     */
    @PostMapping("/join")
    @AccessLog(module = AdminModule.MEMBER, operation = "SLB节点产品")
    public MessageResult joinQuantifyLock(@SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin,@RequestParam long uid,
                                          @RequestParam long activitieId, @RequestParam BigDecimal amount,@RequestParam String password,
                                          @RequestParam BigDecimal coinCnyPrice,@RequestParam BigDecimal usdtCnyPrice,@RequestParam int reWardType)
    {
        Assert.notNull(admin,"会话已过期，请重新登录");
        password = Encrypt.MD5(password + md5Key);
        if(!password.equals(admin.getPassword())) {
            return error("密码错误,请重新输入");
        }
        log.info("【SLB节点产品】------------------------------->");
        //首先验证购买金额是否达到最低条件
        isTrue(amount.compareTo(BigDecimal.valueOf(10000L))>=0,"低于最低的购买金额");
        Member member = memberService.findOne(uid);
        //验证活动配置和锁仓配置是否存在
        LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOneByTime(activitieId);
        Assert.isTrue(lockCoinActivitieSetting != null, "该活动无配置，或已过期");
        LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(lockCoinActivitieSetting.getActivitieId());
        Assert.isTrue(lockCoinActivitieProject != null, "该活动不存在");

        //获取活动币种最新USDT价格
        PriceUtil priceUtil = new PriceUtil();
        BigDecimal activityCoinPrice;
        BigDecimal cnyPrice;
        //输入的币种、usdt人民币价格  (币种人民币价格/USDT人民币价格)
        if(coinCnyPrice!=null && usdtCnyPrice !=null){
            activityCoinPrice = coinCnyPrice.divide(usdtCnyPrice,8,BigDecimal.ROUND_DOWN);
            cnyPrice = usdtCnyPrice;
        }else {//没有输入价格时，实时获取
            activityCoinPrice = priceUtil.getPriceByCoin(restTemplate, lockCoinActivitieSetting.getCoinSymbol());
            //        //如果价格为0，则说明价格异常
            if (activityCoinPrice.compareTo(BigDecimal.ZERO) == 0) {
                throw new IllegalArgumentException("活动币种价格获取失败");
            }
            //获取usdt的人民币价格
            cnyPrice = priceUtil.getUSDTPrice(restTemplate);
            if(cnyPrice.compareTo(BigDecimal.ZERO) == 0)
            {
                throw new IllegalArgumentException("活动币种价格获取失败");
            }
        }
        //计算量化购买总CNY金额对应的usdt币总数,cnyAmount / cnyPrice (人民币金额 / usdt 人民币价格)
        BigDecimal totalAmount = amount.divide(cnyPrice,8, BigDecimal.ROUND_DOWN);

        //计算对应量化投资锁仓的币种（ex：SLB）数量, totalAmout / activityCoinPrice ( usdt总量 / 量化投资币种usdt价格)
        BigDecimal qutifyCoinNum = totalAmount.divide(activityCoinPrice,8,BigDecimal.ROUND_DOWN);

        //获取、减少用户USDT钱包余额
        MemberWallet usdtMemberWallet = memberWalletService.findCacheByCoinUnitAndMemberId("USDT",member.getId());

        LockCoinDetail lockCoinDetail  = getController().joinQuantifyLock1( admin, member,
                lockCoinActivitieSetting,  usdtMemberWallet,
                amount,
                totalAmount ,  qutifyCoinNum,
                activityCoinPrice,  cnyPrice);

        //是否选择返佣，0：返佣，1：不返佣
        if(reWardType != 1){
            //返佣异步调用
            log.info("【SLB节点产品】----------------->异步调用返佣奖励接口，id={}",lockCoinDetail.getId());
            lockCoinActivitieEvent.ansyActivityPromotionReward1(lockCoinDetail, lockCoinActivitieSetting);
        }

        return MessageResult.success();
    }
    public QuantifyLockController getController(){
        return SpringContextUtil.getBean(QuantifyLockController.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public LockCoinDetail  joinQuantifyLock1(Admin admin,Member member,
                                             LockCoinActivitieSetting lockCoinActivitieSetting, MemberWallet usdtMemberWallet,
                                             BigDecimal cnyAmount,
                                             BigDecimal totalAmount , BigDecimal qutifyCoinNum,
                                             BigDecimal activityCoinPrice, BigDecimal cnyPrice)
    {

        if(usdtMemberWallet == null)
        {
            memberWalletService.createMemberWallet(member.getId(), coinService.findByUnit("USDT"));
            throw new IllegalArgumentException("可用余额不足");
        }
        log.info("【SLB节点产品】-------------->扣除用户 "+member.getId()+" 的SLB节点产品所需的 USDT 币数.............");
        MessageResult usdtWalletResult = memberWalletService.decreaseBalance(usdtMemberWallet.getId(), totalAmount);
        if (usdtWalletResult.getCode() != 0)
        {
            throw new IllegalArgumentException("可用余额不足");
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
            throw new IllegalArgumentException("可用余额不足");
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
        lockCoinDetail.setRemark("人工处理--金额:"+cnyAmount+" CNY--操作员ID->"+admin.getId());
        log.info("【SLB节点产品】-------------->添加用户 "+member.getId()+" 的 "+lockCoinActivitieSetting.getCoinSymbol()+" 产品购买详情记录.............");

        //计算收益
        BigDecimal planIncome = lockCoinDetail.getTotalCNY().multiply(lockCoinActivitieSetting.getEarningRate());
        lockCoinDetail.setPlanIncome(planIncome);
        lockCoinDetail.setStatus(LockStatus.LOCKED);
        lockCoinDetail.setRefActivitieId(lockCoinActivitieSetting.getId());
        LockCoinDetail lockCoinDetailNew = lockCoinDetailService.save(lockCoinDetail);

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

        return lockCoinDetailNew;
    }

    /**
     * 查询SLB节点产品VIP
     * @author fumy
     * @time 2018.08.28 14:47
     * @param memberId
     * @param pageNo
     * @param pageSize
     * @return true
     */
    @GetMapping("/vip/page-query")
    public MessageResult quantifyVipInfoPage(Long memberId,int pageNo, int pageSize){
        PageInfo<QuantifyLockReWard> page = lockCoinDetailService.findByPage(memberId, PageData.pageNo4PageHelper(pageNo),pageSize);
        return success(PageData.toPageData(page));
    }

    /**
     * 添加vip
     * @author fumy
     * @time 2018.08.29 14:58
     * @param memberId
     * @return true
     */
    @GetMapping("vip/add")
    @AccessLog(module = AdminModule.PROMOTION ,operation = "添加SLB节点产品VIP")
    public MessageResult addVip(Long memberId){
        QuantifyLockReWard quantifyLockReWard  = lockCoinDetailService.findOneByMemberId(memberId);
        if(quantifyLockReWard!=null){
            return error("该用户已经是VIP");
        }
        QuantifyLockReWard res = lockCoinDetailService.addVip(memberId);
        return success(res);
    }
}
