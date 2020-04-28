package com.spark.bitrade.controller.member;

import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.dto.MemberWalletDTO;
import com.spark.bitrade.exception.UnexpectedException;
import com.spark.bitrade.model.screen.MemberWalletScreen;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.MemberWalletBalanceVO;
import com.sparkframework.security.Encrypt;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("member/member-wallet")
public class MemberWalletController extends BaseAdminController{

    @Autowired
    private MemberWalletService memberWalletService ;

    @Autowired
    private MemberService memberService ;
    @Autowired
    private CoinService coinService ;
    @Autowired
    private KafkaTemplate kafkaTemplate ;
    @Autowired
    private MemberTransactionService memberTransactionService ;

    @Autowired
    private LockCoinRechargeSettingService lockCoinRechargeSettingService;

    @Autowired
    private LockCoinDetailService lockCoinDetailService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LockCoinActivitieSettingService lockCoinActivitieSettingService;

    @Value("${spark.system.md5.key}")
    private String md5Key;


    @RequiresPermissions("member:member-wallet-balance")
    @PostMapping("balance")
    @AccessLog(module = AdminModule.MEMBER, operation = "余额管理")
    public MessageResult getBalance(
            PageModel pageModel,
            MemberWalletScreen screen){
        QMemberWallet qMemberWallet = QMemberWallet.memberWallet ;
        QMember qMember = QMember.member;
        List<Predicate> criteria = new ArrayList<>();
        if (StringUtils.hasText(screen.getAccount())){
            criteria.add(qMember.username.like("%"+screen.getAccount()+"%")
                    .or(qMember.mobilePhone.like(screen.getAccount()+"%"))
                    .or(qMember.email.like(screen.getAccount()+"%"))
                    .or(qMember.realName.like("%"+screen.getAccount()+"%")));
        }
        if(!StringUtils.isEmpty(screen.getWalletAddress())){
            criteria.add(qMemberWallet.address.eq(screen.getWalletAddress()));
        }

        if(!StringUtils.isEmpty(screen.getUnit())) {
            criteria.add(qMemberWallet.coin.unit.eq(screen.getUnit()));
        }
        if(screen.getMaxAllBalance()!=null){
            criteria.add(qMemberWallet.balance.add(qMemberWallet.frozenBalance).loe(screen.getMaxAllBalance()));
        }

        if(screen.getMinAllBalance()!=null){
            criteria.add(qMemberWallet.balance.add(qMemberWallet.frozenBalance).goe(screen.getMinAllBalance()));
        }

        if(screen.getMaxBalance()!=null) {
            criteria.add(qMemberWallet.balance.loe(screen.getMaxBalance()));
        }
        if(screen.getMinBalance()!=null) {
            criteria.add(qMemberWallet.balance.goe(screen.getMinBalance()));
        }
        if(screen.getMaxFrozenBalance()!=null) {
            criteria.add(qMemberWallet.frozenBalance.loe(screen.getMaxFrozenBalance()));
        }
        if(screen.getMinFrozenBalance()!=null) {
            criteria.add(qMemberWallet.frozenBalance.goe(screen.getMinFrozenBalance()));
        }
        Page<MemberWalletDTO> page = memberWalletService.joinFind(criteria,qMember,qMemberWallet,pageModel);
        return success("获取成功",page) ;
    }

    @RequiresPermissions("member:member-wallet-balance-out-excel")
    @GetMapping("balance/out-excel")
    @AccessLog(module = AdminModule.MEMBER,operation = "余额导出")
    public void outExcel(MemberWalletScreen memberWalletScreen, HttpServletResponse response) throws IOException {
        Map<String,Object> map=new HashMap<>();
        map.put("account",memberWalletScreen.getAccount());
        map.put("unit",memberWalletScreen.getUnit());
        map.put("walletAddress",memberWalletScreen.getWalletAddress());
        map.put("minBalance",memberWalletScreen.getMinBalance());
        map.put("maxBalance",memberWalletScreen.getMaxBalance());
        map.put("minFrozenBalance",memberWalletScreen.getMinFrozenBalance());
        map.put("maxFrozenBalance",memberWalletScreen.getMaxFrozenBalance());
        map.put("minAllBalance",memberWalletScreen.getMinAllBalance());
        map.put("maxAllBalance",memberWalletScreen.getMaxAllBalance());
        List<MemberWalletBalanceVO> list=memberWalletService.findByMemberWalletAllForOut(map);
        String fileName="memberWalletBalance_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,MemberWalletBalanceVO.class.getDeclaredFields(),response,fileName);
//        ExcelUtil.listToExcel(list,MemberWalletBalanceVO.class.getDeclaredFields(),response.getOutputStream());
    }

    @RequiresPermissions("member:member-wallet-recharge")
    @PostMapping("recharge")
    @AccessLog(module = AdminModule.MEMBER, operation = "充币管理")
    @Transactional(rollbackFor=Exception.class)
    public MessageResult recharge (
            @RequestParam("unit")String unit,
            @RequestParam("uid")Long uid,
            @RequestParam("amount")BigDecimal amount,
            @RequestParam("type")Integer type,
            @RequestParam("reason") String reason,
            HttpServletRequest request) throws UnexpectedException {

        Coin coin = coinService.findByUnit(unit);
        if(coin == null ){
            return error("币种不存在");
        }
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin,uid);
        Assert.notNull(memberWallet,"wallet null");
        //edit by tansitao 时间： 2018/5/18 原因：修改操作钱包为sql方式
        //memberWallet.setBalance(memberWallet.getBalance().add(amount));
        MessageResult result = memberWalletService.increaseBalance(memberWallet.getId(), amount);
        Assert.isTrue(result.getCode() == 0,"充值失败");

        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(amount);
        memberTransaction.setMemberId(memberWallet.getMemberId());
        memberTransaction.setSymbol(unit);
        //add|edit|del by  shenzucai 时间： 2018.06.25  原因：添加活动充值操作
        switch (type){
            case 6:
                memberTransaction.setType(TransactionType.ACTIVITY_AWARD);
                break;
            default:
                memberTransaction.setType(TransactionType.ADMIN_RECHARGE);
        }
        memberTransaction.setCreateTime(DateUtil.getCurrentDate());
        //add by shenzucai 时间： 2018.05.25 原因：获取操作员id start
        String operationName = ((Admin)request.getSession().getAttribute(SysConstant.SESSION_ADMIN)).getRealName();
        memberTransaction.setComment(String.valueOf(operationName)+":"+reason);
        //add by shenzucai 时间： 2018.05.25 原因：获取操作员id end
        memberTransactionService.save(memberTransaction);
        return success("充值成功") ;
    }

    /**
     * 手动调账接口
     * @author fumy
     * @time 2018.11.05 11:25
     * @param admin
     * @param password //密码
     * @param unit  //币种如SLB
     * @param uid   //会员ID
     * @param amount     //金额
     * @param type  //0=余额->冻结余额/1=冻结余额 -> 余额/2=余额->锁仓余额/3=锁仓余额 -> 余额 /add by zyj: 4=从余额减去/5=从冻结余额减去/6=从锁仓余额减去
     * @param reason //调账原因
     * @param request
     * @return true
     */
    @RequiresPermissions("member:member-wallet-adjustBalance")
    @PostMapping("adjustBalance")
    @AccessLog(module = AdminModule.MEMBER, operation = "手动调账")
    @Transactional(rollbackFor=Exception.class)
    public MessageResult adjustBalance (
            @SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin,
            @RequestParam("password")String password,
            @RequestParam("unit")String unit,
            @RequestParam("uid")Long uid,
            @RequestParam("amount")BigDecimal amount,
            @RequestParam("type")Integer type,
            @RequestParam("reason") String reason,
            HttpServletRequest request) throws UnexpectedException {

        Assert.notNull(admin,"会话已过期，请重新登录");
        password = Encrypt.MD5(password + md5Key);
        if(!password.equals(admin.getPassword())) {
            return error("密码错误,请重新输入");
        }

        Coin coin = coinService.findByUnit(unit);
        if(coin == null ){
            return error("币种不存在");
        }
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin,uid);
        Assert.notNull(memberWallet,"wallet null");

        //调账记录
        MemberTransaction memberTransaction = new MemberTransaction();
        MessageResult result;
        if(type==0){
            //冻结操作：余额 -> 冻结余额
            result = memberWalletService.freezeBalance(memberWallet, amount);
            memberTransaction.setAmount(amount.negate());
            memberTransaction.setComment("冻结："+reason);
        }else if(type==1){
            //解冻操作：冻结余额 -> 余额
            result = memberWalletService.thawBalance(memberWallet, amount);
            memberTransaction.setAmount(amount);
            memberTransaction.setComment("解冻："+reason);
        }else if(type==2){
            //锁币：余额 -> 锁仓余额
            result = memberWalletService.freezeBalanceToLockBalance(memberWallet, amount);
            memberTransaction.setAmount(amount.negate());
            memberTransaction.setComment("锁币："+reason);
        }else if(type==3){
            //解锁：锁仓余额 -> 余额
            result = memberWalletService.thawBalanceFromLockBlance(memberWallet, amount);
            memberTransaction.setAmount(amount);
            memberTransaction.setComment("解锁："+reason);
        }else if (type==4){
            //从余额减去
            result=memberWalletService.subtractBalance(memberWallet,amount);
            memberTransaction.setAmount(amount.negate());
            memberTransaction.setComment("从可用余额中减去指定金额："+reason);
        }else if (type==5){
            //从冻结余额减去
            result=memberWalletService.subtractFreezeBalance(memberWallet,amount);
            memberTransaction.setAmount(amount.negate());
            memberTransaction.setComment("从冻结余额中减去指定金额："+reason);
        }else if (type==6){
            //从锁仓余额减去
            result=memberWalletService.subtractLockBalance(memberWallet,amount);
            memberTransaction.setAmount(amount.negate());
            memberTransaction.setComment("从锁仓余额中减去指定金额："+reason);
        }else if(type==7){
            //从余额加上当前佣金
            result=memberWalletService.commissionBalanceFromBlance(memberWallet,amount);
            memberTransaction.setAmount(amount);
            memberTransaction.setComment("从余额里面增加佣金："+reason);
        }
        else{
            return MessageResult.error("请提供操作类型");
        }

        if(result.getCode()==0) {
            memberTransaction.setFee(BigDecimal.ZERO);
            memberTransaction.setMemberId(uid);
            memberTransaction.setSymbol(unit);
            memberTransaction.setType(TransactionType.ADMIN_ADJUST_BALANCE);
            memberTransaction.setCreateTime(DateUtil.getCurrentDate());
            memberTransactionService.save(memberTransaction);
        }
        return result;
    }


    /**
     * add by yangch 时间： 2018.06.12 原因：添加锁仓充币的接口
     * @author yangch
     * @time 2018.06.12
     * @param admin
     * @param password
     * @param activitieId
     * @param uid
     * @param amount
     * @param request
     * @return true
     */
    @RequiresPermissions("member:member-wallet-lockCoinRecharge")
    @PostMapping("lockCoinRecharge")
    @AccessLog(module = AdminModule.MEMBER, operation = "锁仓充币")
    @Transactional(rollbackFor=Exception.class)
    public MessageResult lockCoinRecharge (
            @SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin,
            @RequestParam("password")String password, //密码
            @RequestParam("activitieId")Long activitieId, //活动ID
            @RequestParam("uid")Long uid, //会员ID
            @RequestParam("amount")BigDecimal amount, //充值金额
             HttpServletRequest request) throws UnexpectedException {

        Assert.notNull(admin,"会话已过期，请重新登录");
        password = Encrypt.MD5(password + md5Key);
        if(!password.equals(admin.getPassword())) {
            return error("密码错误,请重新输入");
        }

        LockCoinRechargeSetting lockCoinRechargeSetting = lockCoinRechargeSettingService.findOne(activitieId);
        Assert.notNull(lockCoinRechargeSetting,"锁仓充值活动不存在");

        Coin coin = coinService.findByUnit(lockCoinRechargeSetting.getCoinSymbol());
        Assert.notNull(coin,"锁仓充值的币种不存在");

        //添加锁仓金额
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin,uid);
        Assert.notNull(memberWallet,"该用户的钱包账户不存在");
        MessageResult result = memberWalletService.increaseLockBalance(memberWallet.getId(), amount);
        Assert.isTrue(result.getCode() == 0,"充值失败");

        //充值记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(amount);
        memberTransaction.setMemberId(uid);
        memberTransaction.setSymbol(lockCoinRechargeSetting.getCoinSymbol());
        memberTransaction.setType(TransactionType.ADMIN_LOCK_RECHARGE);
        memberTransaction.setCreateTime(DateUtil.getCurrentDate());
        String operationName = ((Admin)request.getSession().getAttribute(SysConstant.SESSION_ADMIN)).getRealName();
        memberTransaction.setComment(String.valueOf(operationName));
        memberTransactionService.save(memberTransaction);

        //锁仓记录
        LockCoinDetail lockCoinDetail = new LockCoinDetail();
        lockCoinDetail.setMemberId(uid);
        //手动锁仓
        lockCoinDetail.setType(LockType.HANDLE_LOCK);
        lockCoinDetail.setCoinUnit(lockCoinRechargeSetting.getCoinSymbol());
        lockCoinDetail.setRefActivitieId(lockCoinRechargeSetting.getId());
        lockCoinDetail.setTotalAmount(amount);
        lockCoinDetail.setRemainAmount(amount);
        lockCoinDetail.setLockTime(DateUtil.getCurrentDate());
        lockCoinDetail.setStatus(LockStatus.LOCKED);
        //获取最新价格
        String serviceName = "BITRADE-MARKET";
        String url = "http://" + serviceName + "/market/exchange-rate/usd/"+lockCoinRechargeSetting.getCoinSymbol();
        ResponseEntity<MessageResult> resultPrice = restTemplate.getForEntity(url, MessageResult.class);
        Assert.notNull(resultPrice,"未能获取到最新的价格");
        Assert.isTrue(resultPrice.getBody().getCode() == 0,"未能获取到最新的价格");
        lockCoinDetail.setLockPrice( BigDecimal.valueOf(Double.parseDouble(resultPrice.getBody().getData().toString() )) );
        lockCoinDetailService.save(lockCoinDetail);

        return success("充值成功") ;
    }


    /**
     * 理财锁仓添加
     * @author fumy
     * @time 2018.06.28 15:19
     * @param admin
     * @param password
     * @param activitieId
     * @param uid
     * @param amount
     * @param request
     * @return true
     */
    @PostMapping("lockCoinActivitieRecharge")
    @AccessLog(module = AdminModule.MEMBER, operation = "理财锁仓")
    @Transactional(rollbackFor=Exception.class)
    public MessageResult lockCoinActivitieRecharge (
            @SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin,
            @RequestParam("password")String password, //密码
            @RequestParam("activitieId")Long activitieId, //活动ID
            @RequestParam("uid")Long uid, //会员ID
            @RequestParam("amount")BigDecimal amount, //充值金额CNY
            HttpServletRequest request) throws UnexpectedException {

        Assert.notNull(admin,"会话已过期，请重新登录");
        password = Encrypt.MD5(password + md5Key);
        if(!password.equals(admin.getPassword())) {
            return error("密码错误,请重新输入");
        }

        LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOne(activitieId);
        Assert.notNull(lockCoinActivitieSetting,"理财锁仓活动不存在");

        Coin coin = coinService.findByUnit(lockCoinActivitieSetting.getCoinSymbol());
        Assert.notNull(coin,"锁仓充值的币种不存在");

        //获取最新价格
        PriceUtil priceUtil = new PriceUtil();
        //获取锁仓币种人民币价格
        BigDecimal coinCnyPrice = priceUtil.getCoinCnyPrice(restTemplate,coin.getUnit());
        //获取锁仓币种USDT价格
        BigDecimal coinUSDTPrice = priceUtil.getCoinCnyPrice(restTemplate,coin.getUnit());
        //计算理财币种总币数
        BigDecimal coinNumber = BigDecimalUtils.div(amount,coinCnyPrice,8);
        //获取usdt人民币价格
        BigDecimal usdtCnyPrice = priceUtil.getUSDTPrice(restTemplate);

        //添加锁仓金额
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin,uid);
        Assert.notNull(memberWallet,"该用户的钱包账户不存在");
        MessageResult result = memberWalletService.increaseLockBalance(memberWallet.getId(), coinNumber);
        Assert.isTrue(result.getCode() == 0,"充值失败");

        //充值记录
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(coinNumber);
        memberTransaction.setMemberId(uid);
        memberTransaction.setSymbol(lockCoinActivitieSetting.getCoinSymbol());
        //理财锁仓
        memberTransaction.setType(TransactionType.FINANCIAL_ACTIVITY);
        memberTransaction.setCreateTime(DateUtil.getCurrentDate());
        String operationName = ((Admin)request.getSession().getAttribute(SysConstant.SESSION_ADMIN)).getRealName();
//        String operationName = "理财锁仓：测试";
        memberTransaction.setComment(String.valueOf(operationName));
        memberTransactionService.save(memberTransaction);

        //锁仓记录
        int month = lockCoinActivitieSetting.getLockDays()/30;
        LockCoinDetail lockCoinDetail = new LockCoinDetail();
        lockCoinDetail.setMemberId(uid);
        //理财锁仓
        lockCoinDetail.setType(LockType.FINANCIAL_LOCK);
        lockCoinDetail.setCoinUnit(lockCoinActivitieSetting.getCoinSymbol());
        lockCoinDetail.setRefActivitieId(lockCoinActivitieSetting.getId());
        lockCoinDetail.setTotalAmount(coinNumber);
        lockCoinDetail.setRemainAmount(coinNumber);
        lockCoinDetail.setTotalCNY(amount);
        lockCoinDetail.setUsdtPriceCNY(usdtCnyPrice);
        lockCoinDetail.setLockTime(DateUtil.getCurrentDate());
        lockCoinDetail.setPlanUnlockTime(DateUtil.addMonth(new Date(), month));
        lockCoinDetail.setStatus(LockStatus.LOCKED);
        lockCoinDetail.setRemark("人工处理--操作员ID->"+admin.getId());

        if(LockCoinActivitieType.FIXED_DEPOSIT == lockCoinActivitieSetting.getType())
        {
            BigDecimal planIncome = lockCoinDetail.getTotalCNY().multiply(lockCoinActivitieSetting.getEarningRate()).multiply(BigDecimal.valueOf((double) month/12));
            lockCoinDetail.setPlanIncome(planIncome);
        }
        //edit by tansitao 时间： 2018/7/2 原因：修改为获取币种usdt价格
        lockCoinDetail.setLockPrice( coinUSDTPrice );
        lockCoinDetailService.save(lockCoinDetail);

        return success("充值成功") ;
    }




    @RequiresPermissions("member:member-wallet-reset-address")
    @PostMapping("reset-address")
    @AccessLog(module = AdminModule.MEMBER, operation = "重置钱包地址")
    public MessageResult resetAddress(String unit,long uid){
        Member member = memberService.findOne(uid) ;
        Assert.notNull(member,"member null");
        try {
            JSONObject json = new JSONObject();
            json.put("uid", member.getId());
            //edit by yangch 时间： 2018.04.27 原因：不需要重置钱包功能
            //edit by yangch 时间： 2018.04.26 原因：代码冲突，临时屏蔽
            //kafkaTemplate.send("member-register",unit,json.toJSONString());
            return MessageResult.success("获取地址kafka消息发送成功");
            //add by yangch 时间： 2018.04.26 原因：新增以下方式
            //kafkaTemplate.send("reset-member-address",unit,json.toJSONString());
            //return MessageResult.success("提交成功");
        } catch (Exception e) {
            return MessageResult.error("未知异常");
        }
    }

    @RequiresPermissions("member:member-wallet-lock-wallet")
    @PostMapping("lock-wallet")
    @AccessLog(module = AdminModule.MEMBER, operation = "锁定钱包")
    public MessageResult lockWallet(Long uid,String unit){
        if(memberWalletService.lockWallet(uid,unit)){
            return success("锁定成功");
        }
        else{
            return error(500,"锁定失败");
        }
    }

    @RequiresPermissions("member:member-wallet-unlock-wallet")
    @PostMapping("unlock-wallet")
    @AccessLog(module = AdminModule.MEMBER, operation = "解锁钱包")
    public MessageResult unlockWallet(Long uid,String unit){
        if(memberWalletService.unlockWallet(uid,unit)){
            return success("解锁成功");
        }
        else{
            return error(500,"解锁失败");
        }
    }

    /**
     * @author lingxing
     * @param uid
     * @param unit
     * @return
     */
    @RequiresPermissions("member:member-wallet:pay-wallet-close")
    @PostMapping("pay-wallet-close")
    @AccessLog(module = AdminModule.MEMBER, operation = "关闭钱包充值")
    public MessageResult payWalletClose(Long uid,String unit){
        if(memberWalletService.payWalletClose(uid,unit)){
            return success("关闭钱包充值成功");
        }
        else{
            return error(500,"关闭钱包充值失败");
        }
    }
    /**
     * @author lingxing
     * @param uid
     * @param unit
     * @return
     */
    @RequiresPermissions("member:member-wallet-pay-wallet-enabled")
    @PostMapping("pay-wallet-enabled")
    @AccessLog(module = AdminModule.MEMBER, operation = "开启钱包充值")
    public MessageResult payWalletEnabled(Long uid,String unit){
        if(memberWalletService.payWalletEnabled(uid,unit)){
            return success("开启钱包充值成功");
        }
        else{
            return error(500,"开启钱包充值失败");
        }
    }
    /**
     * @author lingxing
     * @param uid
     * @param unit
     * @return
     */
    @RequiresPermissions("member:member-wallet-coin-wallet-close")
    @PostMapping("coin-wallet-close")
    @AccessLog(module = AdminModule.MEMBER, operation = "关闭提币")
    public MessageResult coinWalletClose(Long uid,String unit){
        if(memberWalletService.coinWalletClose(uid,unit)){
            return success("关闭提币成功");
        }
        else{
            return error(500,"关闭提币失败");
        }
    }

    /**
     * @author lingxing
     * @param uid
     * @param unit
     * @return
     */
    @RequiresPermissions("member:member-wallet-coin-wallet-enabled")
    @PostMapping("coin-wallet-enabled")
    @AccessLog(module = AdminModule.MEMBER, operation = "开启提币")
    public MessageResult coinWalletEnabled(Long uid,String unit){
        if(memberWalletService.coinWalletEnabled(uid,unit)){
            return success("开启提币成功");
        }
        else{
            return error(500,"开启提币失败");
        }
    }
}
