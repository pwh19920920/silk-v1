package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.config.WalletConfig;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.ScanMemberAddress;
import com.spark.bitrade.entity.WithdrawWalletInfo;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.exception.InformationExpiredException;
import com.spark.bitrade.exception.UnexpectedException;
import com.spark.bitrade.feign.ICoinExchange;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.CoinTokenVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static com.spark.bitrade.util.BigDecimalUtils.*;
import static com.spark.bitrade.util.MessageResult.error;
import static org.springframework.util.Assert.*;

/**
 * @author Zhang Jinwei
 * @date 2018年01月26日
 */
@RestController
@Slf4j
@RequestMapping(value = "/withdraw", method = RequestMethod.POST)
public class WithdrawController {
    @Autowired
    private MemberAddressService memberAddressService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MemberService memberService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private WithdrawRecordService withdrawApplyService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private LocaleMessageSourceService sourceService;
    @Autowired
    private WalletConfig walletConfig;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private InterfaceLogService interfaceLogService;
    @Autowired
    private MemberWalletService walletService;
    @Autowired
    private WithdrawRecordService withdrawRecordService;
    @Autowired
    private CoinTokenService coinTokenService;
    //add|edit|del by  shenzucai 时间： 2018.10.31  原因：使用feign获取market
    @Autowired
    private ICoinExchange iCoinExchange;

    @Value("${oneDay.auto.withDraw.enable:false}")
    private Boolean oneDayAutoWithDraw;

    @Autowired
    private IDrawCoinEvent iDrawCoinEvent;
    @Autowired
    private ValidateOpenTranscationService validateOpenTranscationService;
    @Autowired
    private LockBTLFService lockBTLFService;

    /**
     * 增加提现地址
     *
     * @param address
     * @param unit
     * @param remark
     * @param user
     * @return
     */
    @RequestMapping("address/add")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult addAddress(String address, String unit, String remark, /*String code,String aims,*/ @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        hasText(address, sourceService.getMessage("MISSING_COIN_ADDRESS"));
        hasText(unit, sourceService.getMessage("MISSING_COIN_TYPE"));
        //add|edit|del by  shenzucai 时间： 2018.10.31  原因：去掉短信验证
        /*hasText(code, sourceService.getMessage("MISSING_VERIFICATION_CODE"));*/
        //hasText(aims, sourceService.getMessage("MISSING_PHONE_OR_EMAIL"));
        // ValueOperations valueOperations = redisTemplate.opsForValue();


        //add|edit|del by  shenzucai 时间： 2018.11.06  原因：取消实名检测
        // Member member = memberService.findOne(user.getId());
        // isTrue(member.getMemberLevel() != MemberLevelEnum.GENERAL, sourceService.getMessage("NO_REAL_NAME"));//add by tansitao 时间： 2018/9/12 原因：判断用户是否实名认证
       /* if (member.getMobilePhone() != null && aims.equals(member.getMobilePhone())) {
            Object info = valueOperations.get(SysConstant.PHONE_ADD_ADDRESS_PREFIX + member.getMobilePhone());
            //add tansitao 时间： 2018/7/11 原因：增加信息是否为null的判断
            isTrue(info != null, sourceService.getMessage("NO_GET_VERIFICATION_CODE"));
            if (!info.toString().equals(code)) {
                return MessageResult.error(sourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
            } else {
                valueOperations.getOperations().delete(SysConstant.PHONE_ADD_ADDRESS_PREFIX + member.getMobilePhone());
            }
        } else if (member.getEmail() != null && aims.equals(member.getEmail())) {
            Object info = valueOperations.get(SysConstant.ADD_ADDRESS_CODE_PREFIX + member.getEmail());
            if (!info.toString().equals(code)) {
                return MessageResult.error(sourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
            } else {
                valueOperations.getOperations().delete(SysConstant.ADD_ADDRESS_CODE_PREFIX + member.getEmail());
            }
        } else {
            return MessageResult.error(sourceService.getMessage("ADD_ADDRESS_FAILED"));
        }*/


        //add by shenzucai 时间： 2018.05.25 原因：添加地址校验 start
        Coin coin = coinService.findByUnit(unit);


        //add|edit|del by  shenzucai 时间： 2018.11.12  原因：添加提币地址去重判断

        MemberAddress memberAddress = memberAddressService.findByMemberIdAndAddressAndCoinAndStatus(user.getId(), coin, org.apache.commons.lang3.StringUtils.trim(address));

        if (memberAddress != null) {
            MessageResult result = new MessageResult();
            result.setCode(1);
            result.setMessage(sourceService.getMessage("ADDR_HAS_EXIST"));
            return result;
        }

        //add|edit|del by  shenzucai 时间： 2018.11.12  原因：添加提币地址去重判断


        String serviceName = null;
        //add|edit|del by  shenzucai 时间： 2018.08.30  原因：添加以太代币统一处理
        if ("ETH".equalsIgnoreCase(coin.getBaseCoinUnit()) && !Objects.equals(coin.getUnit(), "ETC")) {
            serviceName = "SERVICE-RPC-ETH";
        } else if ("SLU".equalsIgnoreCase(coin.getBaseCoinUnit())) {
            serviceName = "SERVICE-RPC-SLU";
        } else {
            serviceName = "SERVICE-RPC-" + unit.toUpperCase();
        }

        String url = "http://" + serviceName + "/rpc/validate?address={1}";

        MessageResult resultTemp = restTemplate.getForObject(url,
                MessageResult.class, address);
        if (resultTemp.getCode() != 0) {
            resultTemp.setMessage(sourceService.getMessage("WRONG_ADDRESS"));
            return resultTemp;
        }
        //add by shenzucai 时间： 2018.05.25 原因：添加地址校验 end
        MessageResult result = memberAddressService.addMemberAddress(user.getId(), address, unit, remark);
        if (result.getCode() == 0) {
            result.setMessage(sourceService.getMessage("ADD_ADDRESS_SUCCESS"));
        } else if (result.getCode() == 500) {
            result.setMessage(sourceService.getMessage("ADD_ADDRESS_FAILED"));
        } else if (result.getCode() == 600) {
            result.setMessage(sourceService.getMessage("COIN_NOT_SUPPORT"));
        }
        return result;
    }

    /**
     * 删除提现地址
     *
     * @param id
     * @param user
     * @return
     */
    @RequestMapping("address/delete")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult deleteAddress(long id, @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        MessageResult result = memberAddressService.deleteMemberAddress(user.getId(), id);
        if (result.getCode() == 0) {
            result.setMessage(sourceService.getMessage("DELETE_ADDRESS_SUCCESS"));
        } else {
            result.setMessage(sourceService.getMessage("DELETE_ADDRESS_FAILED"));
        }
        return result;
    }

    /**
     * 提现地址分页信息
     *
     * @param user
     * @param pageNo
     * @param pageSize
     * @param unit
     * @return
     */
    @RequestMapping("address/page")
    public MessageResult addressPage(@SessionAttribute(SESSION_MEMBER) AuthMember user, int pageNo, int pageSize, String unit) {
        Page<MemberAddress> page = memberAddressService.pageQuery(pageNo, pageSize, user.getId(), unit);
        Page<ScanMemberAddress> scanMemberAddresses = page.map(x -> ScanMemberAddress.toScanMemberAddress(x));
        MessageResult result = MessageResult.success();
        result.setData(scanMemberAddresses);
        return result;
    }

    /**
     * 支持提现的地址
     *
     * @return
     */
    @RequestMapping("support/coin")
    public MessageResult queryWithdraw() {
        List<Coin> list = coinService.findAllCanWithDraw();
        List<Map<String, Object>> list1 = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (Coin coin : list) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("unit", coin.getUnit());
                map.put("hasLabel", coin.getHasLabel().getOrdinal());
                list1.add(map);
            }
        }
        MessageResult result = MessageResult.success();
        result.setData(list1);
        return result;
    }

    /**
     * 提现币种详细信息
     *
     * @param user
     * @return
     */
    @RequestMapping("support/coin/info")
    public MessageResult queryWithdrawCoin(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        List<Coin> list = coinService.findAllCanWithDraw();
        List<MemberWallet> list1 = memberWalletService.findAllByMemberId(user.getId());
        long id = user.getId();
        List<WithdrawWalletInfo> list2 = list1.stream().filter(
                x -> list.contains(x.getCoin())).map(x ->
                WithdrawWalletInfo.builder()
                        .balance(x.getBalance())
                        .withdrawScale(x.getCoin().getWithdrawScale())  //add by yangch 时间： 2018.04.24 原因：合并新增
                        .maxTxFee(x.getCoin().getMaxTxFee())
                        .minTxFee(x.getCoin().getMinTxFee())
                        .minAmount(x.getCoin().getMinWithdrawAmount())
                        .maxAmount(x.getCoin().getMaxWithdrawAmount())
                        .name(x.getCoin().getName())
                        .nameCn(x.getCoin().getNameCn())
                        .threshold(x.getCoin().getWithdrawThreshold())
                        .unit(x.getCoin().getUnit())
                        .canAutoWithdraw(x.getCoin().getCanAutoWithdraw())
                        //add|edit|del by  shenzucai 时间： 2018.07.06  原因：添加费率和手续费类型
                        .feeType(x.getCoin().getFeeType())
                        .feeRate(x.getCoin().getFeeRate())
                        .addresses(memberAddressService.queryAddress(id, x.getCoin().getName()))
                        //add|edit|del by  shenzucai 时间： 2018.10.30  原因：提币优惠
                        .feeDiscountType(x.getCoin().getFeeDiscountType())
                        .feeDiscountCoinUnit(x.getCoin().getFeeDiscountCoinUnit())
                        .feeDiscountAmount(x.getCoin().getFeeDiscountAmount())
                        //add|edit|del by  shenzucai 时间： 2018.11.02  原因：添加是否具有地址标签的返回参数 0有 1无
                        .hasLabel(x.getCoin().getHasLabel().getOrdinal())
                        .build()
        ).collect(Collectors.toList());
        MessageResult result = MessageResult.success();
        result.setData(list2);
        return result;
    }


    /**
     * 单一提现币种详细信息
     *
     * @param user
     * @return
     */
    @RequestMapping("support/coin/one")
    public MessageResult getWithdrawCoin(@SessionAttribute(SESSION_MEMBER) AuthMember user, @RequestParam String unit) {
        Coin list = coinService.findCanWithDraw(unit);
        if (list == null || !list.getUnit().equalsIgnoreCase(unit)) {
            MessageResult result = MessageResult.error(unit + " is can't withdraw");
            return result;
        }
        //add|edit|del by  shenzucai 时间： 2018.11.02  原因：不使用缓存，改成实时获取余额
        MemberWallet list1 = memberWalletService.findByCoinUnitAndMemberId(unit, user.getId());
        if (list1 == null || list1.getIsLock() == BooleanEnum.IS_TRUE) {
            MessageResult result = MessageResult.error(unit + " not exist");
            return result;
        }
        long id = user.getId();
        WithdrawWalletInfo withdrawWalletInfo =
                WithdrawWalletInfo.builder()
                        .balance(list1.getBalance())
                        .withdrawScale(list1.getCoin().getWithdrawScale())  //add by yangch 时间： 2018.04.24 原因：合并新增
                        .maxTxFee(list1.getCoin().getMaxTxFee())
                        .minTxFee(list1.getCoin().getMinTxFee())
                        .minAmount(list1.getCoin().getMinWithdrawAmount())
                        .maxAmount(list1.getCoin().getMaxWithdrawAmount())
                        .name(list1.getCoin().getName())
                        .nameCn(list1.getCoin().getNameCn())
                        .threshold(list1.getCoin().getWithdrawThreshold())
                        .unit(list1.getCoin().getUnit())
                        .canAutoWithdraw(list1.getCoin().getCanAutoWithdraw())
                        //add|edit|del by  shenzucai 时间： 2018.07.06  原因：添加费率和手续费类型
                        .feeType(list1.getCoin().getFeeType())
                        .feeRate(list1.getCoin().getFeeRate())
                        .addresses(memberAddressService.queryAddress(id, list1.getCoin().getName()))
                        //add|edit|del by  shenzucai 时间： 2018.10.30  原因：提币优惠
                        .feeDiscountType(list1.getCoin().getFeeDiscountType())
                        .feeDiscountCoinUnit(list1.getCoin().getFeeDiscountCoinUnit())
                        .feeDiscountAmount(list1.getCoin().getFeeDiscountAmount())
                        //add|edit|del by  shenzucai 时间： 2018.11.02  原因：添加是否具有地址标签的返回参数 0有 1无
                        .hasLabel(list1.getCoin().getHasLabel().getOrdinal())
                        .build();

        MessageResult result = MessageResult.success();
        List<WithdrawWalletInfo> withdrawWalletInfos = new ArrayList<>();
        withdrawWalletInfos.add(withdrawWalletInfo);
        result.setData(withdrawWalletInfos);
        return result;
    }

    /**
     * 申请提币
     *
     * @param user
     * @param unit
     * @param address
     * @param amount
     * @param fee
     * @param remark
     * @param jyPassword
     * @return
     * @throws Exception
     */
    @RequestMapping("apply")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult withdraw(@SessionAttribute(SESSION_MEMBER) AuthMember user, String unit, String address,
                                  BigDecimal amount, BigDecimal fee, String remark, String jyPassword, String feeDiscountCoinUnit, HttpServletRequest request) throws Exception {


        Coin coin = coinService.findByUnit(unit);
        notNull(coin, sourceService.getMessage("COIN_ILLEGAL"));

        //add by zyj 2019.01.07:接入风控
        DrawCoinInfo drawCoinInfo = new DrawCoinInfo();
        drawCoinInfo.setCoin(unit);//币种
        drawCoinInfo.setAmount(amount);//数量
        drawCoinInfo.setTarget(address);//目标地址

        //add by shenzucai 时间： 2018.05.25 原因：添加地址校验 start
        String serviceName = null;
        //add|edit|del by  shenzucai 时间： 2018.08.30  原因：添加以太代币统一处理
        if ("ETH".equalsIgnoreCase(coin.getBaseCoinUnit()) && !Objects.equals(coin.getUnit(), "ETC")) {
            serviceName = "SERVICE-RPC-ETH";
        } else if ("SLU".equalsIgnoreCase(coin.getBaseCoinUnit())) {
            serviceName = "SERVICE-RPC-SLU";
        } else {
            serviceName = "SERVICE-RPC-" + unit.toUpperCase();
        }

        String url = "http://" + serviceName + "/rpc/validate?address={1}";

        MessageResult resultTemp = restTemplate.getForObject(url,
                MessageResult.class, address);
        if (resultTemp.getCode() != 0) {
            return MessageResult.error(sourceService.getMessage("WRONG_ADDRESS"));
        }
        //add by shenzucai 原因：处理空格
        address = address.trim();
        //add by shenzucai 时间： 2018.05.25 原因：添加地址校验 end
        // 如果没有启用提币优惠方案
        if (StringUtils.isEmpty(feeDiscountCoinUnit)) {
            hasText(jyPassword, sourceService.getMessage("MISSING_JYPASSWORD"));
            hasText(unit, sourceService.getMessage("MISSING_COIN_TYPE"));
            //add by tansitao 时间： 2018/5/1 原因：声明变量主币，主币费率默认值为0，主币钱包,
            Coin baseCoin = null;
            BigDecimal baseCoinFree = BigDecimal.valueOf(0);
            MemberWallet baseMemberWallet = null;
            amount = amount.setScale(coin.getWithdrawScale(), BigDecimal.ROUND_DOWN); //add by yangch 时间： 2018.04.24 原因：合并新增
            fee = fee.setScale(coin.getWithdrawScale(), BigDecimal.ROUND_UP);
            notNull(coin, sourceService.getMessage("COIN_ILLEGAL"));
            //判断币种状态是否正常，是否支持提币
            isTrue(coin.getStatus().equals(CommonStatus.NORMAL) && coin.getCanWithdraw().equals(BooleanEnum.IS_TRUE), sourceService.getMessage("COIN_NOT_SUPPORT"));
            //edit by tansitao 时间： 2018/8/9 原因：如果是区块链提币，判断是否允许区块链提币
            if (!memberWalletService.hasExistByAddr(address)) {
                isTrue(coin.getCanTransfer() == BooleanEnum.IS_TRUE, sourceService.getMessage("COIN_NOT_SUPPORT_OUT"));
            }

            //add|edit|del by  shenzucai 时间： 2018.07.06  原因：添加手续费类型判断 start
            if (coin.getFeeType() == CoinFeeType.SCALE) {
                //按照比例，手续费的位数超过的时候，前端进行向上截取，后台则进行向下截取，判断条件为后端计算的值需要小于或等于前端计算的值
                BigDecimal bigDecimal = amount.multiply(coin.getFeeRate()).setScale(coin.getWithdrawScale(), BigDecimal.ROUND_DOWN);
                BigDecimal bigDecimal1 = amount.multiply(coin.getFeeRate()).setScale(coin.getWithdrawScale(), BigDecimal.ROUND_UP);
                // 舍弃后的值小于或等于前端传过来的手续费
                isTrue(compare(fee, bigDecimal), sourceService.getMessage("FEE_ERROR") + "实际值 " + fee + "需要大于目标值 " + bigDecimal);
                // 进位后的值大于或等于前端传过来的手续费
                isTrue(compare(bigDecimal1, fee), sourceService.getMessage("FEE_ERROR") + "目标值 " + bigDecimal1 + "需要大于实际值 " + fee);
            } else {
                //比较费率和币种最小费率
                isTrue(compare(fee, new BigDecimal(String.valueOf(coin.getMinTxFee()))), sourceService.getMessage("CHARGE_MIN") + coin.getMinTxFee());
                //比较最大费率和最小费率
                isTrue(compare(new BigDecimal(String.valueOf(coin.getMaxTxFee())), fee), sourceService.getMessage("CHARGE_MAX") + coin.getMaxTxFee());
            }
            //add|edit|del by  shenzucai 时间： 2018.07.06  原因：添加手续费类型判断 end
            //比较最大提币数和当前用户提币数
            isTrue(compare(coin.getMaxWithdrawAmount(), amount), sourceService.getMessage("WITHDRAW_MAX") + coin.getMaxWithdrawAmount());
            //比较最小提币数和当前用户提币数
            isTrue(compare(amount, coin.getMinWithdrawAmount()), sourceService.getMessage("WITHDRAW_MIN") + coin.getMinWithdrawAmount());
            //获取用户当前币种钱包信息
            MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, user.getId());
            //对比用户余额与当前提币数量
            isTrue(compare(memberWallet.getBalance(), amount), sourceService.getMessage("INSUFFICIENT_BALANCE"));

            //add|edit|del by  shenzucai 时间： 2018.11.01  原因：不对地址进行存在判断，只使用合法性校验
            //判断提币地址是否存在
            //isTrue(memberAddressService.findByMemberIdAndAddress(user.getId(), address).size() > 0, sourceService.getMessage("WRONG_ADDRESS"));
            //判断用户钱包该币是否被锁定
            isTrue(memberWallet.getIsLock() == BooleanEnum.IS_FALSE, sourceService.getMessage("WALLET_LOCKED"));//edit by tansitao 时间： 2018/5/16 原因：增加国际化
            //判断用户钱包该币是否允许提币
            isTrue(memberWallet.getEnabledOut() == BooleanEnum.IS_TRUE, sourceService.getMessage("WALLET_LOCKED"));//edit by tansitao 时间： 2018/8/6 原因：增加对钱包的是否可提币判断

            //以下代码主要是判断资金密码是否正确
            Member member = memberService.findOne(user.getId());
            memberService.checkRealName(member);
            Assert.isTrue(member.getTransactionStatus() == null || member.getTransactionStatus() == BooleanEnum.IS_TRUE, sourceService.getMessage("LIMIT_APPLY"));//edit by tansitao 时间： 2018/5/16 原因：修改国际化
            validateOpenTranscationService.validateOpenUpCoinTransaction(member.getId(), sourceService.getMessage("LIMIT_APPLY"));
            //验证是否是内部提币 若存在则为内部平台转账
            MemberWallet coinAndAddress = walletService.findByCoinAndAddress(coin, address);
            if (coinAndAddress != null) {
                //验证当前用户是否开通 内部平台转账
                validateOpenTranscationService.validateOpenPlatformTransaction(member.getId(), sourceService.getMessage("CURRENT_INTERNAL_TRANSFER_DISABLED"));
                if (!member.getId().equals(coinAndAddress.getMemberId())) {
                    //验证对方
                    validateOpenTranscationService.validateOpenPlatformTransaction(coinAndAddress.getMemberId(), sourceService.getMessage("ADVERSE_ACCOUNT_DISABLE"));
                }
            }


            String mbPassword = member.getJyPassword();
            Assert.hasText(mbPassword, sourceService.getMessage("NO_SET_JYPASSWORD"));
            String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
            Assert.isTrue(jyPass.equals(mbPassword), sourceService.getMessage("ERROR_JYPASSWORD"));


            //add by zyj 2019.01.07:接入风控
            drawCoinInfo.setSource(memberWallet.getAddress());//来源地址

            MessageResult res = iDrawCoinEvent.drawCoin(request, null, member, drawCoinInfo);
            if (res.getCode() != 0) {
                return error(res.getMessage());
            }


            //add by tansitao 时间： 2018/5/1 原因：判断是否为带币，如果是带币，用户主币数量余额必须大于配置的主币费率数量,
            if (!StringUtils.isEmpty(coin.getBaseCoinUnit())) {
                baseCoinFree = walletConfig.getEthNum();
                if (baseCoinFree != null) {
                    baseCoin = coinService.findByUnit(coin.getBaseCoinUnit());
                    baseMemberWallet = memberWalletService.findByCoinAndMemberId(baseCoin, user.getId());
                    //add by tansitao 时间： 2018/7/28 原因：增加主币钱包不存在的判断
                    if (baseMemberWallet == null) {
                        baseMemberWallet = walletService.createMemberWallet(member.getId(), baseCoin);
                    }
                    isTrue(baseMemberWallet != null, sourceService.getMessage("WALLET_NOT_EXIST"));
                    //对比用户主币余额与配置的主币汇率数
                    //edit by tansitao 时间： 2018/12/10 原因：修改提示
                    isTrue(compare(baseMemberWallet.getBalance(), baseCoinFree), baseMemberWallet.getCoin().getUnit() + sourceService.getMessage("BASE_COIN_INSUFFICIENT_BALANCE"));

                    //冻结用户主币钱包的币账户数量
                    MessageResult result = memberWalletService.freezeBalance(baseMemberWallet, baseCoinFree);
                    if (result.getCode() != 0) {
                        throw new InformationExpiredException(baseCoin.getUnit() + sourceService.getMessage("INSUFFICIENT_BALANCE"));
                    }
                }
            }

            //冻结用户钱包的币账户数量
            MessageResult result = memberWalletService.freezeBalance(memberWallet, amount);
            if (result.getCode() != 0) {
                throw new InformationExpiredException("Information Expired");
            }

            //提币申请记录
            WithdrawRecord withdrawApply = new WithdrawRecord();
            withdrawApply.setCoin(coin);

            withdrawApply.setFee(fee);
            //add by tansitao 时间： 2018/5/1 原因：设置主币手续费
            withdrawApply.setBaseCoinFree(baseCoinFree);
            withdrawApply.setArrivedAmount(sub(amount, fee));
            withdrawApply.setMemberId(user.getId());
            withdrawApply.setTotalAmount(amount);
            withdrawApply.setAddress(address);
            withdrawApply.setRemark(remark);
            withdrawApply.setCanAutoWithdraw(coin.getCanAutoWithdraw());
            Assert.isTrue(withdrawApply.getArrivedAmount().compareTo(new BigDecimal("0")) == 1, sourceService.getMessage("INSUFFICIENT_BALANCE"));
            getService().asyncWithdrawCoin(withdrawApply, address, member, fee);
        } else {
            // 启用了提币优惠方案
            hasText(jyPassword, sourceService.getMessage("MISSING_JYPASSWORD"));
            hasText(unit, sourceService.getMessage("MISSING_COIN_TYPE"));
            //add by tansitao 时间： 2018/5/1 原因：声明变量主币，主币费率默认值为0，主币钱包,
            Coin baseCoin = null;
            BigDecimal baseCoinFree = BigDecimal.valueOf(0);
            MemberWallet baseMemberWallet = null;
            amount = amount.setScale(coin.getWithdrawScale(), BigDecimal.ROUND_DOWN); //add by yangch 时间： 2018.04.24 原因：合并新增
            fee = fee.setScale(coin.getWithdrawScale(), BigDecimal.ROUND_UP);
            notNull(coin, sourceService.getMessage("COIN_ILLEGAL"));
            //判断币种状态是否正常，是否支持提币
            isTrue(coin.getStatus().equals(CommonStatus.NORMAL) && coin.getCanWithdraw().equals(BooleanEnum.IS_TRUE), sourceService.getMessage("COIN_NOT_SUPPORT"));
            //edit by tansitao 时间： 2018/8/9 原因：如果是区块链提币，判断是否允许区块链提币
            if (!memberWalletService.hasExistByAddr(address)) {
                isTrue(coin.getCanTransfer() == BooleanEnum.IS_TRUE, sourceService.getMessage("COIN_NOT_SUPPORT_OUT"));
            }

            //add|edit|del by  shenzucai 时间： 2018.07.06  原因：添加手续费类型判断 start
            if (coin.getFeeType() == CoinFeeType.SCALE) {
                //按照比例，手续费的位数超过的时候，前端进行向上截取，后台则进行向下截取，判断条件为后端计算的值需要小于或等于前端计算的值
                BigDecimal bigDecimal = amount.multiply(coin.getFeeRate()).setScale(coin.getWithdrawScale(), BigDecimal.ROUND_DOWN);
                BigDecimal bigDecimal1 = amount.multiply(coin.getFeeRate()).setScale(coin.getWithdrawScale(), BigDecimal.ROUND_UP);
                // 舍弃后的值小于或等于前端传过来的手续费
                isTrue(compare(fee, bigDecimal), sourceService.getMessage("FEE_ERROR") + "实际值 " + fee + "需要大于目标值 " + bigDecimal);
                // 进位后的值大于或等于前端传过来的手续费
                isTrue(compare(bigDecimal1, fee), sourceService.getMessage("FEE_ERROR") + "目标值 " + bigDecimal1 + "需要大于实际值 " + fee);
            } else {
                //比较费率和币种最小费率
                isTrue(compare(fee, new BigDecimal(String.valueOf(coin.getMinTxFee()))), sourceService.getMessage("CHARGE_MIN") + coin.getMinTxFee());
                //比较最大费率和最小费率
                isTrue(compare(new BigDecimal(String.valueOf(coin.getMaxTxFee())), fee), sourceService.getMessage("CHARGE_MAX") + coin.getMaxTxFee());
            }
            //add|edit|del by  shenzucai 时间： 2018.07.06  原因：添加手续费类型判断 end
            //比较最大提币数和当前用户提币数
            isTrue(compare(coin.getMaxWithdrawAmount(), amount), sourceService.getMessage("WITHDRAW_MAX") + coin.getMaxWithdrawAmount());
            //比较最小提币数和当前用户提币数
            isTrue(compare(amount, coin.getMinWithdrawAmount()), sourceService.getMessage("WITHDRAW_MIN") + coin.getMinWithdrawAmount());
            //获取用户当前币种钱包信息
            MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, user.getId());
            //对比用户余额与当前提币数量
            isTrue(compare(memberWallet.getBalance(), amount), sourceService.getMessage("INSUFFICIENT_BALANCE"));

            //add|edit|del by  shenzucai 时间： 2018.11.01  原因：不对地址进行存在判断，只使用合法性校验
            //判断提币地址是否存在
            //isTrue(memberAddressService.findByMemberIdAndAddress(user.getId(), address).size() > 0, sourceService.getMessage("WRONG_ADDRESS"));
            //判断用户钱包该币是否被锁定
            isTrue(memberWallet.getIsLock() == BooleanEnum.IS_FALSE, sourceService.getMessage("WALLET_LOCKED"));//edit by tansitao 时间： 2018/5/16 原因：增加国际化
            //判断用户钱包该币是否允许提币
            isTrue(memberWallet.getEnabledOut() == BooleanEnum.IS_TRUE, sourceService.getMessage("WALLET_LOCKED"));//edit by tansitao 时间： 2018/8/6 原因：增加对钱包的是否可提币判断

            //以下代码主要是判断资金密码是否正确
            Member member = memberService.findOne(user.getId());
            Assert.isTrue(member.getTransactionStatus() == null || member.getTransactionStatus() == BooleanEnum.IS_TRUE, sourceService.getMessage("LIMIT_APPLY"));//edit by tansitao 时间： 2018/5/16 原因：修改国际化
            validateOpenTranscationService.validateOpenUpCoinTransaction(member.getId(), sourceService.getMessage("LIMIT_APPLY"));
            //验证是否是内部提币 若存在则为内部平台转账
            MemberWallet coinAndAddress = walletService.findByCoinAndAddress(coin, address);
            if (coinAndAddress != null) {
                //验证当前用户是否开通 内部平台转账
                validateOpenTranscationService.validateOpenPlatformTransaction(member.getId(), sourceService.getMessage("CURRENT_INTERNAL_TRANSFER_DISABLED"));
                if (!member.getId().equals(coinAndAddress.getMemberId())) {
                    //验证对方
                    validateOpenTranscationService.validateOpenPlatformTransaction(coinAndAddress.getMemberId(), sourceService.getMessage("ADVERSE_ACCOUNT_DISABLE"));
                }
            }
            String mbPassword = member.getJyPassword();
            Assert.hasText(mbPassword, sourceService.getMessage("NO_SET_JYPASSWORD"));
            String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
            Assert.isTrue(jyPass.equals(mbPassword), sourceService.getMessage("ERROR_JYPASSWORD"));


            //add by zyj 2019.01.07:接入风控
            drawCoinInfo.setSource(memberWallet.getAddress());//来源地址

            MessageResult res = iDrawCoinEvent.drawCoin(request, null, member, drawCoinInfo);
            if (res.getCode() != 0) {
                return error(res.getMessage());
            }


            //add by tansitao 时间： 2018/5/1 原因：判断是否为带币，如果是带币，用户主币数量余额必须大于配置的主币费率数量,
            if (!StringUtils.isEmpty(coin.getBaseCoinUnit())) {
                baseCoinFree = walletConfig.getEthNum();
                if (baseCoinFree != null) {
                    baseCoin = coinService.findByUnit(coin.getBaseCoinUnit());
                    baseMemberWallet = memberWalletService.findByCoinAndMemberId(baseCoin, user.getId());
                    //add by tansitao 时间： 2018/7/28 原因：增加主币钱包不存在的判断
                    if (baseMemberWallet == null) {
                        baseMemberWallet = walletService.createMemberWallet(member.getId(), baseCoin);
                    }
                    isTrue(baseMemberWallet != null, sourceService.getMessage("WALLET_NOT_EXIST"));

                    //对比用户主币余额与配置的主币汇率数
                    isTrue(compare(baseMemberWallet.getBalance(), baseCoinFree), baseMemberWallet.getCoin().getUnit() + sourceService.getMessage("BASE_COIN_INSUFFICIENT_BALANCE"));

                    //冻结用户主币钱包的币账户数量
                    MessageResult result = memberWalletService.freezeBalance(baseMemberWallet, baseCoinFree);


                    Assert.isTrue(result.getCode() == 0, sourceService.getMessage("INSUFFICIENT_BALANCE"));

                }
            }

            //add|edit|del by  shenzucai 时间： 2018.10.31  原因：进行提币优惠判断
            if (coin != null && org.apache.commons.lang3.StringUtils.equalsIgnoreCase(coin.getFeeDiscountCoinUnit(), feeDiscountCoinUnit)) {

                // 获取手续费抵扣币种
                Coin feeDiscountCoin = coinService.findByUnit(coin.getFeeDiscountCoinUnit());
                //获取用户当前抵扣币种钱包信息
                MemberWallet feeDiscountWallet = memberWalletService.findByCoinAndMemberId(feeDiscountCoin, user.getId());
                //add by  shenzucai 时间： 2018.12.20  原因：当折扣币种账户不存在的时候，返回提示信息 start,修改提示
                isTrue(feeDiscountWallet != null, feeDiscountCoinUnit + sourceService.getMessage("WALLET_NOT_EXIST"));
                //add by  shenzucai 时间： 2018.12.20  原因：当折扣币种账户不存在的时候，返回提示信息 end
                // 抵扣模式为固定时
                if (coin.getFeeDiscountType() == CoinFeeType.FIXED) {
                    //对比用户抵扣币种余额与当前提币数量
                    isTrue(compare(feeDiscountWallet.getBalance(), coin.getFeeDiscountAmount()), sourceService.getMessage("INSUFFICIENT_BALANCE"));
                    //冻结用户钱包的抵扣币账户数量
                    MessageResult result = memberWalletService.freezeBalance(feeDiscountWallet, coin.getFeeDiscountAmount());
                    if (result.getCode() != 0) {
                        throw new InformationExpiredException("Information Expired");
                    }

                    //冻结用户钱包的币账户数量
                    MessageResult resultUnit = memberWalletService.freezeBalance(memberWallet, amount);
                    if (resultUnit.getCode() != 0) {
                        throw new InformationExpiredException("Information Expired");
                    }
                    fee = BigDecimal.ZERO;
                    //提币申请记录
                    WithdrawRecord withdrawApply = new WithdrawRecord();
                    withdrawApply.setCoin(coin);

                    withdrawApply.setFee(BigDecimal.ZERO);
                    //add by tansitao 时间： 2018/5/1 原因：设置主币手续费
                    withdrawApply.setBaseCoinFree(baseCoinFree);
                    withdrawApply.setArrivedAmount(sub(amount, fee));
                    withdrawApply.setMemberId(user.getId());
                    withdrawApply.setTotalAmount(amount);
                    withdrawApply.setAddress(address);
                    withdrawApply.setRemark(remark);
                    withdrawApply.setCanAutoWithdraw(coin.getCanAutoWithdraw());
                    withdrawApply.setFeeDiscountCoinUnit(coin.getFeeDiscountCoinUnit());
                    withdrawApply.setFeeDiscountAmount(coin.getFeeDiscountAmount());
                    Assert.isTrue(withdrawApply.getArrivedAmount().compareTo(new BigDecimal("0")) == 1, sourceService.getMessage("INSUFFICIENT_BALANCE"));
                    getService().asyncWithdrawCoin(withdrawApply, address, member, fee);
                    // 抵扣模式为优惠比例时
                } else if (coin.getFeeDiscountType() == CoinFeeType.SCALE) {
                    //提币币种USD价格
                    BigDecimal unitPrice = BigDecimal.ZERO;

                    try {
                        MessageResult mr = iCoinExchange.getUsdExchangeRate(unit);
                        log.info("=========查询" + unit + "价格后返回的结果{}=========", mr.getCode() + "===" + mr.getMessage());
                        if (mr != null && mr.getCode() == 0) {
                            unitPrice = BigDecimal.valueOf(Double.parseDouble(mr.getData().toString()));
                        }
                    } catch (Exception e) {
                        log.info("获取{}价格失败 {}", unit, e.getMessage());
                    }


                    //抵扣币币种USD价格
                    BigDecimal feeDiscountUnitPrice = BigDecimal.ZERO;


                    try {
                        MessageResult mr1 = iCoinExchange.getUsdExchangeRate(coin.getFeeDiscountCoinUnit());
                        log.info("=========查询" + coin.getFeeDiscountCoinUnit() + "价格后返回的结果{}=========", mr1.getCode() + "===" + mr1.getMessage());
                        if (mr1 != null && mr1.getCode() == 0) {
                            feeDiscountUnitPrice = BigDecimal.valueOf(Double.parseDouble(mr1.getData().toString()));
                        }
                    } catch (Exception e) {
                        log.info("获取{}价格失败 {}", coin.getFeeDiscountCoinUnit(), e.getMessage());
                    }


                    if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) > 0 && feeDiscountUnitPrice != null && feeDiscountUnitPrice.compareTo(BigDecimal.ZERO) > 0) {
                        //根据交易所价格获取抵扣币种的数量
                        BigDecimal feeDiscountAmount = unitPrice.multiply(fee).divide(feeDiscountUnitPrice, coin.getWithdrawScale(), BigDecimal.ROUND_UP).multiply(coin.getFeeDiscountAmount()).setScale(coin.getWithdrawScale(), BigDecimal.ROUND_UP);
                        if (feeDiscountAmount.compareTo(feeDiscountWallet.getBalance()) <= 0) {
                            //冻结用户钱包的抵扣币账户数量
                            MessageResult result = memberWalletService.freezeBalance(feeDiscountWallet, feeDiscountAmount);
                            if (result.getCode() != 0) {
                                throw new InformationExpiredException("Information Expired");
                            }
                            // 此时提币币种手续费为0
                            fee = BigDecimal.ZERO;
                            //冻结用户钱包的币账户数量
                            MessageResult resultUnit = memberWalletService.freezeBalance(memberWallet, amount);
                            if (resultUnit.getCode() != 0) {
                                throw new InformationExpiredException("Information Expired");
                            }

                            //提币申请记录
                            WithdrawRecord withdrawApply = new WithdrawRecord();
                            withdrawApply.setCoin(coin);
                            withdrawApply.setFee(BigDecimal.ZERO);
                            withdrawApply.setBaseCoinFree(baseCoinFree);
                            withdrawApply.setArrivedAmount(sub(amount, fee));
                            withdrawApply.setMemberId(user.getId());
                            withdrawApply.setTotalAmount(amount);
                            withdrawApply.setAddress(address);
                            withdrawApply.setRemark(remark);
                            withdrawApply.setCanAutoWithdraw(coin.getCanAutoWithdraw());
                            withdrawApply.setFeeDiscountCoinUnit(coin.getFeeDiscountCoinUnit());
                            withdrawApply.setFeeDiscountAmount(feeDiscountAmount);
                            withdrawApply.setComment(coin.getUnit() + ":" + unitPrice + "-" + coin.getFeeDiscountCoinUnit() + ":" + feeDiscountUnitPrice);
                            Assert.isTrue(withdrawApply.getArrivedAmount().compareTo(new BigDecimal("0")) == 1, sourceService.getMessage("INSUFFICIENT_BALANCE"));
                            getService().asyncWithdrawCoin(withdrawApply, address, member, fee);

                        } else {
                            BigDecimal feeDiscounPartAmount = feeDiscountWallet.getBalance().setScale(coin.getWithdrawScale(), BigDecimal.ROUND_DOWN);
                            //冻结用户钱包的抵扣币账户全部数量
                            MessageResult result = memberWalletService.freezeBalance(feeDiscountWallet, feeDiscounPartAmount);
                            if (result.getCode() != 0) {
                                throw new InformationExpiredException("Information Expired");
                            }
                            //对使用抵扣币剩余的收费使用提现币种结算
                            fee = fee.subtract(feeDiscounPartAmount.divide(coin.getFeeDiscountAmount(), coin.getWithdrawScale(), BigDecimal.ROUND_DOWN).multiply(feeDiscountUnitPrice).divide(unitPrice, coin.getWithdrawScale(), BigDecimal.ROUND_DOWN).setScale(coin.getWithdrawScale(), BigDecimal.ROUND_UP));

                            //冻结用户钱包的币账户数量
                            MessageResult resultUnit = memberWalletService.freezeBalance(memberWallet, amount);
                            if (resultUnit.getCode() != 0) {
                                throw new InformationExpiredException("Information Expired");
                            }

                            //提币申请记录
                            WithdrawRecord withdrawApply = new WithdrawRecord();
                            withdrawApply.setCoin(coin);
                            withdrawApply.setFee(fee);
                            withdrawApply.setBaseCoinFree(baseCoinFree);
                            withdrawApply.setArrivedAmount(sub(amount, fee));
                            withdrawApply.setMemberId(user.getId());
                            withdrawApply.setTotalAmount(amount);
                            withdrawApply.setAddress(address);
                            withdrawApply.setRemark(remark);
                            withdrawApply.setCanAutoWithdraw(coin.getCanAutoWithdraw());
                            withdrawApply.setFeeDiscountCoinUnit(coin.getFeeDiscountCoinUnit());
                            withdrawApply.setFeeDiscountAmount(feeDiscounPartAmount);
                            withdrawApply.setComment(coin.getUnit() + ":" + unitPrice + "-" + coin.getFeeDiscountCoinUnit() + ":" + feeDiscountUnitPrice);
                            //add|edit|del by  shenzucai 时间： 2018.11.12  原因：条件不通过的时候，报异常
                            Assert.isTrue(withdrawApply.getArrivedAmount().compareTo(new BigDecimal("0")) == 1, sourceService.getMessage("INSUFFICIENT_BALANCE"));
                            getService().asyncWithdrawCoin(withdrawApply, address, member, fee);
                        }
                    } else {
                        throw new InformationExpiredException("Information Expired");
                    }
                } else {
                    Assert.isTrue(false, sourceService.getMessage("PARAMETER_ERROR"));
                }
            } else {
                Assert.isTrue(false, (sourceService.getMessage("COIN_ILLEGAL")));
            }

        }
        return MessageResult.success(sourceService.getMessage("APPLY_SUCCESS"));
    }

    /**
     *  * 开始异步提币
     *  * @author tansitao
     *  * @time 2018/7/31 16:57 
     *  
     */
    @Async
    public void asyncWithdrawCoin(WithdrawRecord withdrawApply, String address, Member member, BigDecimal fee) throws Exception {
        //add by tansitao 时间： 2018/9/7 原因：从数据库查询带币并设置到coins
        String coins = "ETH,USDT";
        List<CoinTokenVo> coinTokenVoList = coinTokenService.findAll();
        if (coinTokenVoList != null) {
            for (CoinTokenVo coinTokenVo : coinTokenVoList) {
                coins += "," + coinTokenVo.getCoinUnit();
            }
        }
        RPCUtil.coins = coins;
        getService().dealWithdrawCoin(withdrawApply, address, member, fee);
    }

    /**
     *  * 进行提币处理
     *  * @author tansitao
     *  * @time 2018/7/31 17:26 
     *  
     */
    public void dealWithdrawCoin(WithdrawRecord withdrawApply, String address, Member member, BigDecimal fee) throws Exception {

        BigDecimal amount = withdrawApply.getArrivedAmount();
        BigDecimal baseCoinFree = withdrawApply.getBaseCoinFree();
        Coin coin = withdrawApply.getCoin();
        //edit by tansitao 时间： 2018/4/27 原因：修改用户提币处理逻辑
        RPCUtil rpcUtil = new RPCUtil();
        //提币数量低于或等于阈值并且该币种支持自动提币
        //add by  shenzucai 时间： 2019.05.06  原因：内部转账无需审核
        //edit by  shenzucai 时间： 2019.10.21  原因：内部转账也要查看是否开启自动提币，否则需要审核
        //add by qhliao 时间: 2019.12.18 原因:增加自动提现次数 超过提现次数走人工审核1
        boolean canAuto=true;
        Integer countWith=withdrawApplyService.countMemberWithdraw(member.getId(),coin.getName());
        Integer autoCount = coin.getAutoCount();
        if (autoCount==null){
            Coin byUnitNew = coinService.findByUnitNew(coin.getUnit());
            autoCount = Optional.ofNullable(byUnitNew.getAutoCount()).orElse(1);
        }
        if(countWith>=autoCount){
            canAuto=false;
        }
        if (coin.getCanAutoWithdraw().equals(BooleanEnum.IS_TRUE) && memberWalletService.hasExistByAddr(address)&&canAuto) {
            withdrawApply.setStatus(WithdrawStatus.PUTING);
            withdrawApply.setIsAuto(BooleanEnum.IS_TRUE);
            WithdrawRecord withdrawRecord = withdrawApplyService.save(withdrawApply);

            log.info("==========进入平台互转 withdrawRecord={}==========", withdrawRecord);
            String txid = UUIDUtil.getUUID();
            MessageResult result = walletService.recharge(coin, address, amount, txid);
            if (result.getCode() != 0) {
                throw new UnexpectedException("not the desired result");
            }
            try {
                if ("BTLF".equals(coin.getUnit())) {
                    Long memberId = withdrawRecord.getMemberId();
                    MemberWallet memberWallet = walletService.findByCoinAndAddress(coin,address);
                    if(Objects.isNull(memberWallet)){
                        throw new Exception("memberWallet is null");
                    }
                    if (memberId.longValue() == 408124L || memberId.longValue() == 408313L || memberId.longValue() == 408308L || memberId.longValue() == 397987L) {
                        lockBTLFService.lockBTLF(memberWallet.getMemberId(), amount, coin.getUnit());
                    }
                }
            }catch(Exception e){
                log.info("BTLF 充值锁仓失败 {}",e);
            }
            //处理成功,data为txid，更新业务订单
            try {
                withdrawRecordService.withdrawSuccess(withdrawRecord.getId(), txid);
            } catch (Exception e) {
                log.error("===============内部互转失败==================withdrawRecordId" + withdrawRecord.getId(), e);
                throw new UnexpectedException("not the desired result");
            }
        } else if (amount.compareTo(coin.getWithdrawThreshold()) <= 0 && coin.getCanAutoWithdraw().equals(BooleanEnum.IS_TRUE)&&canAuto) {
            /*//add by tansitao 时间： 2018/7/31 原因：判断是否为内部转账
            if (memberWalletService.hasExistByAddr(address)) {
                withdrawApply.setStatus(WithdrawStatus.PUTING);
                withdrawApply.setIsAuto(BooleanEnum.IS_TRUE);
                WithdrawRecord withdrawRecord = withdrawApplyService.save(withdrawApply);

                log.info("==========进入平台互转 withdrawRecord={}==========", withdrawRecord);
                String txid = UUIDUtil.getUUID();
                MessageResult result = walletService.recharge(coin, address, amount, txid);
                if (result.getCode() != 0) {
                    throw new UnexpectedException("not the desired result");
                }
                //处理成功,data为txid，更新业务订单
                try {
                    withdrawRecordService.withdrawSuccess(withdrawRecord.getId(), txid);
                } catch (Exception e) {
                    log.error("===============内部互转失败==================withdrawRecordId" + withdrawRecord.getId(), e);
                    throw new UnexpectedException("not the desired result");
                }
            }*/
            //不是内部互转走区块链
            // else {

            //add by  shenzucai 时间： 2018.12.21  原因：增加提现的时候自动提现的限制，规则为一天之内只能自动提现一次，两次自动审核提币时间间隔不能小于24小时(适用外部提币) start
            Boolean allowAutoWithDraw = true;
            if (oneDayAutoWithDraw) {
                allowAutoWithDraw = withdrawRecordService.allowAutoWithDraw(member.getId(), coin.getName());
                if (!allowAutoWithDraw) {
                    log.info("==============一天之内只能自动提现一次，两次自动审核提币时间间隔不能小于24小时(适用外部提币)=========memberId{}======amount{}", withdrawApply.getMemberId(), withdrawApply.getTotalAmount());
                }
            }


            //判断平台的该币余额是否足够,如果不足则转人工处理，如果allowAutoWithDraw为false也转人工处理
            if (rpcUtil.balanceIsEnough(interfaceLogService, restTemplate, coin, amount) && allowAutoWithDraw&&canAuto) {
                //add by  shenzucai 时间： 2018.11.22  原因：增加提现的时候自动提现的限制，规则为一天之内只能自动提现一次，两次自动审核提币时间间隔不能小于24小时(适用外部提币) end
                //判断是否为带币
                if (StringUtils.isEmpty(coin.getBaseCoinUnit())) {
                    withdrawApply.setStatus(WithdrawStatus.PUTING);
                    withdrawApply.setIsAuto(BooleanEnum.IS_TRUE);
                    WithdrawRecord withdrawRecord = withdrawApplyService.saveAndFlush(withdrawApply);
//                        TransactionAspectSupport.currentTransactionStatus().flush();//add by tansitao 时间： 2018/8/1 原因：手动提交事务

                    JSONObject json = new JSONObject();
                    json.put("uid", member.getId());
                    //提币总数量
                    json.put("totalAmount", add(amount, fee));
                    //手续费
                    json.put("fee", fee);
                    //预计到账数量
                    json.put("arriveAmount", amount);
                    //币种
                    json.put("coin", coin);
                    //提币地址
                    json.put("address", address);
                    //提币记录id
                    json.put("withdrawId", withdrawRecord.getId());
                    kafkaTemplate.send("withdraw", coin.getUnit(), json.toJSONString());

                }
                //如果是带币，需要判断主币信息
                else {
                    Coin baseCoin = coinService.findByUnit(coin.getBaseCoinUnit());
                    //判断平台主币是否余额充足
                    if (rpcUtil.balanceIsEnough(interfaceLogService, restTemplate, baseCoin, baseCoinFree)) {
                        withdrawApply.setStatus(WithdrawStatus.PUTING);
                        withdrawApply.setIsAuto(BooleanEnum.IS_TRUE);
                        WithdrawRecord withdrawRecord = withdrawApplyService.saveAndFlush(withdrawApply);
//                            TransactionAspectSupport.currentTransactionStatus().flush();//add by tansitao 时间： 2018/8/1 原因：手动提交事务
                        JSONObject json = new JSONObject();
                        json.put("uid", member.getId());
                        //提币总数量
                        json.put("totalAmount", add(amount, fee));
                        //手续费
                        json.put("fee", fee);
                        //预计到账数量
                        json.put("arriveAmount", amount);
                        //币种
                        json.put("coin", coin);
                        //提币地址
                        json.put("address", address);
                        //提币记录id
                        json.put("withdrawId", withdrawRecord.getId());
                        kafkaTemplate.send("withdraw", coin.getUnit(), json.toJSONString());
                    }
                    //主币余额不足转人工处理
                    else {
                        withdrawApply.setStatus(WithdrawStatus.PROCESSING);
                        withdrawApply.setIsAuto(BooleanEnum.IS_FALSE);
                        if (withdrawApplyService.save(withdrawApply) != null) {
                            //add by tansitao 时间： 2018/5/2 原因：添加归集钱包功能
                            rpcUtil.collectCoin(interfaceLogService, restTemplate, baseCoin);
                        } else {
                            log.info("==============提币转人工处理失败========TBEX001=========memberId{}======amount{}", withdrawApply.getMemberId(), withdrawApply.getTotalAmount());
                        }
                    }
                }
            }
            //如果平台余额不足则转人工处理,
            else {
                withdrawApply.setStatus(WithdrawStatus.PROCESSING);
                withdrawApply.setIsAuto(BooleanEnum.IS_FALSE);
                if (withdrawApplyService.save(withdrawApply) != null) {
                    //add by tansitao 时间： 2018/5/2 原因：添加归集钱包功能
                    rpcUtil.collectCoin(interfaceLogService, restTemplate, coin);
                } else {
                    log.info("==============提币转人工处理失败=========TBEX002========memberId{}======amount{}", withdrawApply.getMemberId(), withdrawApply.getTotalAmount());
                }

            }
            // }
        } else {
            withdrawApply.setStatus(WithdrawStatus.PROCESSING);
            withdrawApply.setIsAuto(BooleanEnum.IS_FALSE);
            if (withdrawApplyService.save(withdrawApply) == null) {
                log.info("==============提币转人工处理失败=========TBEX003========memberId{}======amount{}", withdrawApply.getMemberId(), withdrawApply.getTotalAmount());
            }
        }
    }

    /**
     * 提币记录
     *
     * @param user
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("record")
    public MessageResult pageWithdraw(@SessionAttribute(SESSION_MEMBER) AuthMember user, int page, int pageSize, String unit) {
        MessageResult mr = new MessageResult(0, "success");
        Page<WithdrawRecord> records = withdrawApplyService.findAllByMemberId(user.getId(), page, pageSize, unit);

        // 启用区块链浏览器
        Page<ScanWithdrawRecord> scanWithdrawRecords = records.map(x -> ScanWithdrawRecord.toScanWithdrawRecord(x));

        mr.setData(scanWithdrawRecords);
        return mr;
    }

    /**
     * 查询当前登录用户的钱包币种
     * @param  user 当前登录用户
     * @return
     */
    @GetMapping("wallets")
    public MessageResult findMemberWallet(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        MessageResult mr = new MessageResult(0, "success");
        List<MemberWallet> wallets = memberWalletService.findMemberWalletByMemberId(user.getId());
        mr.setData(wallets);
        return mr;
    }

    private WithdrawController getService() {
        return SpringContextUtil.getBean(this.getClass());
    }
}
