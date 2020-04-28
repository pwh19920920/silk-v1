package com.spark.bitrade.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.spark.bitrade.config.WalletConfig;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.exception.InformationExpiredException;
import com.spark.bitrade.exception.UnexpectedException;
import com.spark.bitrade.feign.ICoinExchange;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.CoinTokenVo;
import io.swagger.annotations.Api;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static com.spark.bitrade.util.BigDecimalUtils.add;
import static com.spark.bitrade.util.BigDecimalUtils.compare;
import static com.spark.bitrade.util.BigDecimalUtils.sub;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * <p>
 * silubium-silktrader交易记录表 前端控制器
 * </p>
 *
 * @author shenzucai
 * @since 2019-01-21
 */
@RestController
@RequestMapping("/transferSilubium")
@Api(value = "silubium-silktrader交易记录表")
public class TransferSilubiumController {

    @Autowired
    private TransferSilubiumService transferSilubiumService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private LocaleMessageSourceService sourceService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private WalletConfig walletConfig;

    @Value("${oneDay.auto.withDraw.enable:false}")
    private Boolean oneDayAutoWithDraw;

    @Autowired
    private WithdrawRecordService withdrawRecordService;

    @Autowired
    private ICoinExchange iCoinExchange;

    @Autowired
    private WithdrawRecordService withdrawApplyService;

    @Autowired
    private InterfaceLogService interfaceLogService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
//edit by  shenzucai 时间： 2019.04.04  原因：采用远程调用校验
    // @Value("${main.slu.address}")
    // private String mainSluAddress;

    @Autowired
    private Environment env;

    @Autowired
    private CoinTokenService coinTokenService;

    @Autowired
    private IPaySupportCoinConfigService iPaySupportCoinConfigService;
    @Autowired
    private ValidateOpenTranscationService validateOpenTranscationService;
    private Logger logger = LoggerFactory.getLogger(TransferSilubiumController.class);



    private Integer getAddress(String unit,MemberWallet wallet,Coin coin){

        //远程RPC服务URL,后缀为币种单位
        String serviceName = "SERVICE-RPC-" + unit;
        String account = "U" + wallet.getMemberId();
        //add|edit|del by  shenzucai 时间： 2019.02.19  原因：由于后面返回
        MemberWallet memberWallet = new MemberWallet();
        Integer exist = -1;
        Integer getFail = 1;
        Integer success = 0;
        //如果该币已有钱包地址返回结果
        if (wallet != null && wallet.getAddress() != null && !"".equals(wallet.getAddress())) {
            return exist;
        }
        //如果该币无钱包信息或地址，先获取该币钱包地址
        else {
            String address = "";
            try {
                //edit by shenzucai 时间： 2018.04.22 原因：判断是否为代币，如果不是，则走rpc获取地址 start--------------
                if (StringUtils.isEmpty(coin.getBaseCoinUnit())) {
                    String url = "http://" + serviceName + "/rpc/address/{account}";
                    ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class, account);
                    logger.info("remote call:service={},result={}", serviceName, result);
                    if (result.getStatusCode().value() == 200) {
                        MessageResult mr = result.getBody();
                        logger.info("mr={}", mr);
                        if (mr.getCode() == 0) {
                            //返回地址成功，调用持久化
                            address = (String) mr.getData();
                            if(StringUtils.isEmpty(address)) {
                                return getFail;
                            }
                        }else{
                            return getFail;
                        }
                    }else{
                        return getFail;
                    }
                } else {
                    //add|edit|del by  shenzucai 时间： 2018.06.21  原因：如果获取的是代币的地址，且主币地址也不存在的时候，就会生成主币和代币的地址
                    Coin baseCoin = coinService.findByUnit(coin.getBaseCoinUnit());
                    MemberWallet baseWallet = memberWalletService.findByCoinAndMemberId(baseCoin, wallet.getMemberId());
                    //edit by tansitao 时间： 2018/5/21 原因：修改对钱包为null的判断
                    if(baseWallet == null || StringUtils.isEmpty(baseWallet.getAddress())){

                        //远程RPC服务URL,后缀为币种单位
                        String tempName = "SERVICE-RPC-" + coin.getBaseCoinUnit();
                        String url = "http://" + tempName + "/rpc/address/{account}";
                        ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class, account);
                        String baseAddress = null;
                        logger.info("remote call:service={},result={}", serviceName, result);
                        if (result.getStatusCode().value() == 200) {

                            MessageResult mr = result.getBody();
                            logger.info("mr={}", mr);
                            if (mr.getCode() == 0) {
                                //返回地址成功，调用持久化
                                baseAddress = (String) mr.getData();
                            }
                        }

                        if(!StringUtils.isEmpty(baseAddress)) {
                            if(baseWallet == null) {
                                MemberWallet baseWallet1 = new MemberWallet();
                                baseWallet1.setAddress(baseAddress);
                                baseWallet1.setBalance(BigDecimal.valueOf(0));
                                baseWallet1.setCoin(baseCoin);
                                baseWallet1.setFrozenBalance(BigDecimal.valueOf(0));
                                baseWallet1.setLockBalance(BigDecimal.valueOf(0));
                                baseWallet1.setMemberId(wallet.getMemberId());
                                baseWallet1.setIsLock(BooleanEnum.IS_FALSE);
                                memberWalletService.save(baseWallet1);
                            }else{

                                memberWalletService.updateMemberWalletAddress(baseWallet.getId(),baseAddress);
                            }
                            address = baseAddress;
                        }else{
                            return getFail;
                        }

                    }else {
                        address = baseWallet.getAddress();
                    }
                }
                //edit by shenzucai 时间： 2018.04.22 原因：判断是否为代币，如果不是，则走rpc获取地址 end -----------------------
            } catch (Exception e) {
                logger.error("call {} failed,error={}", serviceName, e.getMessage());
                return getFail;
            }
            //如果该用户无钱包信息，创建新的钱包信息，并添加其他基本信息
            if (null == wallet) {
                wallet = new MemberWallet();
                wallet.setAddress(address);
                wallet.setBalance(BigDecimal.valueOf(0));
                wallet.setCoin(coin);
                wallet.setFrozenBalance(BigDecimal.valueOf(0));
                wallet.setLockBalance(BigDecimal.valueOf(0));
                wallet.setMemberId(wallet.getMemberId());
                wallet.setIsLock(BooleanEnum.IS_FALSE);
                memberWalletService.save(wallet);
            }
            //如果有钱包信息，则设置钱包地址
            else {
                //add by  shenzucai 时间： 2018.11.20  原因：由于原先操作可能影响账，遂改成sql形式
                memberWalletService.updateMemberWalletAddress(wallet.getId(),address);
            }
            BeanUtils.copyProperties(wallet,memberWallet);
            memberWallet.setAddress(address);

        }
        return success;
    }

    /**
     * 获取币种的提币地址
     * @author shenzucai
     * @time 2019.04.04 14:30
     * @param unit
     * @return true
     */
    private String getCoinBase(String unit){
        //远程RPC服务URL,后缀为币种单位

        String url;
        Coin coin = coinService.findByUnit(unit);
        if(coin == null){
            return null;
        }
        if("ETH".equalsIgnoreCase(coin.getBaseCoinUnit())  && !Objects.equals(coin.getUnit(),"ETC")){
            url = "http://SERVICE-RPC-ETH/rpc/" + RPCUtil.coinBase+"?coinUnit="+coin.getUnit();
        }else if("SLU".equalsIgnoreCase(coin.getBaseCoinUnit())){
            url = "http://SERVICE-RPC-SLU/rpc/" + RPCUtil.coinBase+"?coinUnit="+coin.getUnit();
        }else{
            url = "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/" + RPCUtil.coinBase;
        }

        ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class);
        logger.info("remote call:service={},result={}", url, result);
        if (result.getStatusCode().value() == 200)
        {
            MessageResult mr = result.getBody();
            logger.info("==========调用RPC后返回的结果{}=========", mr.getCode()+ "===" + mr.getMessage());
            if (mr.getCode() == 0)
            {
                //返回用户余额成功，调用持久化
                String res = JSONObject.toJSONString(mr.getData());
                CoinBase coinBase = JSONObject.parseObject(res,CoinBase.class);
                if(coinBase != null){
                    return coinBase.getCoinBase();
                }
            }
        }
        return null;
    }
    /**
     * 申请充币（钱包划转到交易平台）
     *
     * @param user
     * @param unit 币种单位
     * @param fromAddress 发送地址
     * @param amount 金额
     * @param txhash 交易hash
     * @param toAddress 接收地址
     * @param jyPassword 交易密码
     * @return
     * @throws Exception
     */
    @PostMapping("deposit")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult deposit(@SessionAttribute(SESSION_MEMBER) AuthMember user, String unit, String fromAddress,
                                  String amount, String txhash,String toAddress,String jyPassword) throws Exception {
        String remark = "silkPay";


        if(StringUtils.isEmpty(txhash) ||
                StringUtils.isEmpty(unit) ||
                StringUtils.isEmpty(fromAddress) ||
                StringUtils.isEmpty(amount) ||
                StringUtils.isEmpty(toAddress) ||
                StringUtils.isEmpty(jyPassword) ||
                user == null){
            return MessageResult.error(sourceService.getMessage("PARAMETER_ERROR"));
        }
        String appId = AppIdUtil.getRealAppIdBySessionPaltform(user.getPlatform());
        BigDecimal amountNum = BigDecimal.ZERO;
        try {
            amountNum = new BigDecimal(amount);
        }catch (Exception e){
            return MessageResult.error(sourceService.getMessage("NUMBER_ERROR"));
        }finally {
            if(BigDecimal.ZERO.compareTo(amountNum) != -1){
                return MessageResult.error(sourceService.getMessage("NUMBER_ERROR"));
            }
        }


        //add by  shenzucai 时间： 2019.03.22  原因： 暂时只针对dcc app取后台资金划转的种类，其余默认开启
        String[] profiles = env.getActiveProfiles();
        logger.info("当前激活的配置文件为：*********** {}", profiles[0]);
        if (profiles != null && "prod".equalsIgnoreCase(profiles[0])) {
            if("19573380".equalsIgnoreCase(appId)) {
                PaySupportCoinConfig paySupportCoinConfig = iPaySupportCoinConfigService.findByStatusAndUnit(unit, 1);

                if (paySupportCoinConfig != null) {
                    if (paySupportCoinConfig.getIsAssetTransfer().getOrdinal() == 0) {
                        return MessageResult.error(sourceService.getMessage("UNSUPPORT_TRANSFER"));
                    }
                } else {
                    return MessageResult.error(sourceService.getMessage("UNSUPPORT_TRANSFER"));
                }
            }
        }else{
            if("55434535".equalsIgnoreCase(appId)) {
                PaySupportCoinConfig paySupportCoinConfig = iPaySupportCoinConfigService.findByStatusAndUnit(unit, 1);

                if (paySupportCoinConfig != null) {
                    if (paySupportCoinConfig.getIsAssetTransfer().getOrdinal() == 0) {
                        return MessageResult.error(sourceService.getMessage("UNSUPPORT_TRANSFER"));
                    }
                } else {
                    return MessageResult.error(sourceService.getMessage("UNSUPPORT_TRANSFER"));
                }
            }
        }



        hasText(jyPassword, sourceService.getMessage("MISSING_JYPASSWORD"));
        Coin coin = coinService.findByUnit(unit);
        notNull(coin, sourceService.getMessage("COIN_ILLEGAL"));

        if(!org.apache.commons.lang3.StringUtils.equalsIgnoreCase(toAddress,getCoinBase(unit))){
            return MessageResult.error(sourceService.getMessage("WRONG_ADDRESS"));
        }

        //获取用户当前币种钱包信息
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, user.getId());
        if(memberWallet == null){
            return MessageResult.error(sourceService.getMessage("WALLET_NOT_EXIST"));
        }
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
                MessageResult.class, org.apache.commons.lang3.StringUtils.trim(fromAddress));
        if (resultTemp.getCode() != 0) {
            return MessageResult.error(sourceService.getMessage("WRONG_ADDRESS"));
        }
        TransferSilubium transferSilubiumCheck = transferSilubiumService.selectOne(new EntityWrapper<TransferSilubium>()
                .where("transfer_hash={0}", txhash));
        if(transferSilubiumCheck != null){
            return MessageResult.error(sourceService.getMessage("DUPLICATE_SUBMIT"));
        }

        //以下代码主要是判断资金密码是否正确
        Member member = memberService.findOne(user.getId());
        Assert.isTrue(member.getTransactionStatus() == null || member.getTransactionStatus() == BooleanEnum.IS_TRUE, sourceService.getMessage("LIMIT_APPLY"));//edit by tansitao 时间： 2018/5/16 原因：修改国际化
        validateOpenTranscationService.validateOpenUpCoinTransaction(member.getId(), sourceService.getMessage("LIMIT_APPLY"));
        String mbPassword = member.getJyPassword();
        Assert.hasText(mbPassword, sourceService.getMessage("NO_SET_JYPASSWORD"));
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        Assert.isTrue(jyPass.equals(mbPassword), sourceService.getMessage("ERROR_JYPASSWORD"));

        //add by  shenzucai 时间： 2019.01.21  原因：进行 交易记录表 转出记录初始化 start
        TransferSilubium transferSilubium = new TransferSilubium();
        transferSilubium.setArriveAmount(amountNum);
        transferSilubium.setCoinUnit(coin.getUnit());
        transferSilubium.setCreateTime(DateUtil.getCurrentDate());
        transferSilubium.setFromAddress(fromAddress);
        transferSilubium.setToAddress(toAddress);
        transferSilubium.setTotalAmount(amountNum);
        transferSilubium.setTransferHash(txhash);
        transferSilubium.setTransferStatus(0);
        transferSilubium.setUserId(user.getId());
        transferSilubium.setWalletId(memberWallet.getId());
        transferSilubium.setTransferType(0);
        transferSilubium.setComment(remark);
        transferSilubium.setAppId(appId);
        // DataSourceContextHolder.setWrite();
        transferSilubiumService.insertOrupdate(transferSilubium);
        // DataSourceContextHolder.setRead();
        //add by  shenzucai 时间： 2019.01.21  原因：进行 交易记录表 转出记录初始化 end
        return MessageResult.success(sourceService.getMessage("DEPOSIT_SUCCESS"));


    }

    /**
     * 申请提币
     *
     * @param user
     * @param unit 币种
     * @param address 提币地址
     * @param amount 提币金额
     * @param jyPassword 交易密码
     * @return
     * @throws Exception
     */
    @PostMapping("apply")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult withdraw(@SessionAttribute(SESSION_MEMBER) AuthMember user, String unit, String address,
                                  String amount, String jyPassword, String feeDiscountCoinUnit) throws Exception {

        String remark = "silkPay";
        Member one = memberService.findOne(user.getId());
        memberService.checkRealName(one);
        if(StringUtils.isEmpty(address) ||
                StringUtils.isEmpty(unit) ||
                StringUtils.isEmpty(amount) ||
                StringUtils.isEmpty(jyPassword) ||
                user == null){
            return MessageResult.error(sourceService.getMessage("PARAMETER_ERROR"));
        }

        BigDecimal amountNum = BigDecimal.ZERO;
        try {
            amountNum = new BigDecimal(amount);
        }catch (Exception e){
            return MessageResult.error(sourceService.getMessage("NUMBER_ERROR"));
        }finally {
            if(BigDecimal.ZERO.compareTo(amountNum) != -1){
                return MessageResult.error(sourceService.getMessage("NUMBER_ERROR"));
            }
        }

        String appId = AppIdUtil.getRealAppIdBySessionPaltform(user.getPlatform());

        //add by  shenzucai 时间： 2019.03.22  原因： 暂时只针对dcc app取后台资金划转的种类，其余默认开启
        String[] profiles = env.getActiveProfiles();
        logger.info("当前激活的配置文件为：*********** {}", profiles[0]);
        if (profiles != null && "prod".equalsIgnoreCase(profiles[0])) {
            if("19573380".equalsIgnoreCase(appId)) {
                PaySupportCoinConfig paySupportCoinConfig = iPaySupportCoinConfigService.findByStatusAndUnit(unit, 1);

                if (paySupportCoinConfig != null) {
                    if (paySupportCoinConfig.getIsAssetTransfer().getOrdinal() == 0) {
                        return MessageResult.error(sourceService.getMessage("UNSUPPORT_TRANSFER"));
                    }
                } else {
                    return MessageResult.error(sourceService.getMessage("UNSUPPORT_TRANSFER"));
                }
            }
        }else{
            if("55434535".equalsIgnoreCase(appId)) {
                PaySupportCoinConfig paySupportCoinConfig = iPaySupportCoinConfigService.findByStatusAndUnit(unit, 1);

                if (paySupportCoinConfig != null) {
                    if (paySupportCoinConfig.getIsAssetTransfer().getOrdinal() == 0) {
                        return MessageResult.error(sourceService.getMessage("UNSUPPORT_TRANSFER"));
                    }
                } else {
                    return MessageResult.error(sourceService.getMessage("UNSUPPORT_TRANSFER"));
                }
            }
        }

        BigDecimal fee = BigDecimal.ZERO;
        Coin coin = coinService.findByUnit(unit);
        notNull(coin, sourceService.getMessage("COIN_ILLEGAL"));
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
        //add by shenzucai 时间： 2018.05.25 原因：添加地址校验 end

        // 如果没有启用提币优惠方案
        if (StringUtils.isEmpty(feeDiscountCoinUnit)) {
            hasText(jyPassword, sourceService.getMessage("MISSING_JYPASSWORD"));
            hasText(unit, sourceService.getMessage("MISSING_COIN_TYPE"));
            //add by tansitao 时间： 2018/5/1 原因：声明变量主币，主币费率默认值为0，主币钱包,
            Coin baseCoin = null;
            BigDecimal baseCoinFree = BigDecimal.valueOf(0);
            MemberWallet baseMemberWallet = null;
            amountNum = amountNum.setScale(coin.getWithdrawScale(), BigDecimal.ROUND_DOWN); //add by yangch 时间： 2018.04.24 原因：合并新增
            // fee = fee.setScale(coin.getWithdrawScale(), BigDecimal.ROUND_UP);
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
                BigDecimal bigDecimal = amountNum.multiply(coin.getFeeRate()).setScale(coin.getWithdrawScale(), BigDecimal.ROUND_DOWN);
                BigDecimal bigDecimal1 = amountNum.multiply(coin.getFeeRate()).setScale(coin.getWithdrawScale(), BigDecimal.ROUND_UP);
                fee = bigDecimal1;
                // 舍弃后的值小于或等于前端传过来的手续费
                isTrue(compare(fee, bigDecimal), sourceService.getMessage("FEE_ERROR") + "实际值 " + fee + "需要大于目标值 " + bigDecimal);
                // 进位后的值大于或等于前端传过来的手续费
                isTrue(compare(bigDecimal1, fee), sourceService.getMessage("FEE_ERROR") + "目标值 " + bigDecimal1 + "需要大于实际值 " + fee);
            } else {
                fee = new BigDecimal(Double.toString(coin.getMinTxFee()));
                //比较费率和币种最小费率
                isTrue(compare(fee, new BigDecimal(Double.toString(coin.getMinTxFee()))), sourceService.getMessage("CHARGE_MIN") + coin.getMinTxFee());
                //比较最大费率和最小费率
                isTrue(compare(new BigDecimal(Double.toString(coin.getMaxTxFee())), fee), sourceService.getMessage("CHARGE_MAX") + coin.getMaxTxFee());
            }
            //add|edit|del by  shenzucai 时间： 2018.07.06  原因：添加手续费类型判断 end
            //比较最大提币数和当前用户提币数
            isTrue(compare(coin.getMaxWithdrawAmount(), amountNum), sourceService.getMessage("WITHDRAW_MAX") + coin.getMaxWithdrawAmount());
            //比较最小提币数和当前用户提币数
            isTrue(compare(amountNum, coin.getMinWithdrawAmount()), sourceService.getMessage("WITHDRAW_MIN") + coin.getMinWithdrawAmount());
            //获取用户当前币种钱包信息
            MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, user.getId());

            //add|edit|del by  shenzucai 时间： 2019.03.12  原因：解决用户使用资金划转功能，且没有对应地址时，生成地址
            Assert.isTrue(!Objects.equals(getAddress(unit,memberWallet,coin),1), sourceService.getMessage("WALLET_GET_FAIL"));

            //对比用户余额与当前提币数量
            isTrue(compare(memberWallet.getBalance(), amountNum), sourceService.getMessage("INSUFFICIENT_BALANCE"));

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
            String mbPassword = member.getJyPassword();
            Assert.hasText(mbPassword, sourceService.getMessage("NO_SET_JYPASSWORD"));
            String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
            Assert.isTrue(jyPass.equals(mbPassword), sourceService.getMessage("ERROR_JYPASSWORD"));

            //add by tansitao 时间： 2018/5/1 原因：判断是否为带币，如果是带币，用户主币数量余额必须大于配置的主币费率数量,
            if (!StringUtils.isEmpty(coin.getBaseCoinUnit())) {
                baseCoinFree = walletConfig.getEthNum();
                if (baseCoinFree != null) {
                    baseCoin = coinService.findByUnit(coin.getBaseCoinUnit());
                    baseMemberWallet = memberWalletService.findByCoinAndMemberId(baseCoin, user.getId());
                    //add by tansitao 时间： 2018/7/28 原因：增加主币钱包不存在的判断
                    if (baseMemberWallet == null) {
                        baseMemberWallet = memberWalletService.createMemberWallet(member.getId(), baseCoin);
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
            MessageResult result = memberWalletService.freezeBalance(memberWallet, amountNum);
            if (result.getCode() != 0) {
                throw new InformationExpiredException("Information Expired");
            }

            //提币申请记录
            WithdrawRecord withdrawApply = new WithdrawRecord();
            withdrawApply.setCoin(coin);

            withdrawApply.setFee(fee);
            //add by tansitao 时间： 2018/5/1 原因：设置主币手续费
            withdrawApply.setBaseCoinFree(baseCoinFree);
            withdrawApply.setArrivedAmount(sub(amountNum, fee));
            withdrawApply.setMemberId(user.getId());
            withdrawApply.setTotalAmount(amountNum);
            withdrawApply.setAddress(address);
            withdrawApply.setRemark(remark);
            withdrawApply.setCanAutoWithdraw(coin.getCanAutoWithdraw());
            Assert.isTrue(withdrawApply.getArrivedAmount().compareTo(new BigDecimal("0")) == 1, sourceService.getMessage("INSUFFICIENT_BALANCE"));
            getService().asyncWithdrawCoin(withdrawApply, address, member, fee,memberWallet,appId);
        } else {
            // 启用了提币优惠方案
            hasText(jyPassword, sourceService.getMessage("MISSING_JYPASSWORD"));
            hasText(unit, sourceService.getMessage("MISSING_COIN_TYPE"));
            //add by tansitao 时间： 2018/5/1 原因：声明变量主币，主币费率默认值为0，主币钱包,
            Coin baseCoin = null;
            BigDecimal baseCoinFree = BigDecimal.valueOf(0);
            MemberWallet baseMemberWallet = null;
            amountNum = amountNum.setScale(coin.getWithdrawScale(), BigDecimal.ROUND_DOWN); //add by yangch 时间： 2018.04.24 原因：合并新增
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
                BigDecimal bigDecimal = amountNum.multiply(coin.getFeeRate()).setScale(coin.getWithdrawScale(), BigDecimal.ROUND_DOWN);
                BigDecimal bigDecimal1 = amountNum.multiply(coin.getFeeRate()).setScale(coin.getWithdrawScale(), BigDecimal.ROUND_UP);
                fee = bigDecimal1;
                // 舍弃后的值小于或等于前端传过来的手续费
                isTrue(compare(fee, bigDecimal), sourceService.getMessage("FEE_ERROR") + "实际值 " + fee + "需要大于目标值 " + bigDecimal);
                // 进位后的值大于或等于前端传过来的手续费
                isTrue(compare(bigDecimal1, fee), sourceService.getMessage("FEE_ERROR") + "目标值 " + bigDecimal1 + "需要大于实际值 " + fee);
            } else {
                fee = new BigDecimal(Double.toString(coin.getMinTxFee()));
                //比较费率和币种最小费率
                isTrue(compare(fee, new BigDecimal(Double.toString(coin.getMinTxFee()))), sourceService.getMessage("CHARGE_MIN") + coin.getMinTxFee());
                //比较最大费率和最小费率
                isTrue(compare(new BigDecimal(Double.toString(coin.getMaxTxFee())), fee), sourceService.getMessage("CHARGE_MAX") + coin.getMaxTxFee());
            }
            //add|edit|del by  shenzucai 时间： 2018.07.06  原因：添加手续费类型判断 end
            //比较最大提币数和当前用户提币数
            isTrue(compare(coin.getMaxWithdrawAmount(), amountNum), sourceService.getMessage("WITHDRAW_MAX") + coin.getMaxWithdrawAmount());
            //比较最小提币数和当前用户提币数
            isTrue(compare(amountNum, coin.getMinWithdrawAmount()), sourceService.getMessage("WITHDRAW_MIN") + coin.getMinWithdrawAmount());
            //获取用户当前币种钱包信息
            MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, user.getId());

            //add|edit|del by  shenzucai 时间： 2019.03.12  原因：解决用户使用资金划转功能，且没有对应地址时，生成地址
            Assert.isTrue(!Objects.equals(getAddress(unit,memberWallet,coin),1), sourceService.getMessage("WALLET_GET_FAIL"));

            //对比用户余额与当前提币数量
            isTrue(compare(memberWallet.getBalance(), amountNum), sourceService.getMessage("INSUFFICIENT_BALANCE"));

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
            String mbPassword = member.getJyPassword();
            Assert.hasText(mbPassword, sourceService.getMessage("NO_SET_JYPASSWORD"));
            String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
            Assert.isTrue(jyPass.equals(mbPassword), sourceService.getMessage("ERROR_JYPASSWORD"));

            //add by tansitao 时间： 2018/5/1 原因：判断是否为带币，如果是带币，用户主币数量余额必须大于配置的主币费率数量,
            if (!StringUtils.isEmpty(coin.getBaseCoinUnit())) {
                baseCoinFree = walletConfig.getEthNum();
                if (baseCoinFree != null) {
                    baseCoin = coinService.findByUnit(coin.getBaseCoinUnit());
                    baseMemberWallet = memberWalletService.findByCoinAndMemberId(baseCoin, user.getId());
                    //add by tansitao 时间： 2018/7/28 原因：增加主币钱包不存在的判断
                    if (baseMemberWallet == null) {
                        baseMemberWallet = memberWalletService.createMemberWallet(member.getId(), baseCoin);
                    }
                    isTrue(baseMemberWallet != null, sourceService.getMessage("WALLET_NOT_EXIST"));

                    //对比用户主币余额与配置的主币汇率数
                    isTrue(compare(baseMemberWallet.getBalance(), baseCoinFree),baseMemberWallet.getCoin().getUnit() + sourceService.getMessage("BASE_COIN_INSUFFICIENT_BALANCE"));

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
                //add by  shenzucai 时间： 2018.12.20  原因：当折扣币种账户不存在的时候，返回提示信息 start
                isTrue(feeDiscountWallet != null, feeDiscountCoinUnit+sourceService.getMessage("WALLET_NOT_EXIST"));
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
                    MessageResult resultUnit = memberWalletService.freezeBalance(memberWallet, amountNum);
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
                    withdrawApply.setArrivedAmount(sub(amountNum, fee));
                    withdrawApply.setMemberId(user.getId());
                    withdrawApply.setTotalAmount(amountNum);
                    withdrawApply.setAddress(address);
                    withdrawApply.setRemark(remark);
                    withdrawApply.setCanAutoWithdraw(coin.getCanAutoWithdraw());
                    withdrawApply.setFeeDiscountCoinUnit(coin.getFeeDiscountCoinUnit());
                    withdrawApply.setFeeDiscountAmount(coin.getFeeDiscountAmount());
                    Assert.isTrue(withdrawApply.getArrivedAmount().compareTo(new BigDecimal("0")) == 1, sourceService.getMessage("INSUFFICIENT_BALANCE"));
                    getService().asyncWithdrawCoin(withdrawApply, address, member, fee,memberWallet,appId);
                    // 抵扣模式为优惠比例时
                } else if (coin.getFeeDiscountType() == CoinFeeType.SCALE) {
                    //提币币种USD价格
                    BigDecimal unitPrice = BigDecimal.ZERO;

                    try {
                        MessageResult mr = iCoinExchange.getUsdExchangeRate(unit);
                        logger.info("=========查询" + unit + "价格后返回的结果{}=========", mr.getCode() + "===" + mr.getMessage());
                        if (mr != null && mr.getCode() == 0) {
                            unitPrice = BigDecimal.valueOf(Double.parseDouble(mr.getData().toString()));
                        }
                    } catch (Exception e) {
                        logger.info("获取{}价格失败 {}", unit, e.getMessage());
                    }


                    //抵扣币币种USD价格
                    BigDecimal feeDiscountUnitPrice = BigDecimal.ZERO;


                    try {
                        MessageResult mr1 = iCoinExchange.getUsdExchangeRate(coin.getFeeDiscountCoinUnit());
                        logger.info("=========查询" + coin.getFeeDiscountCoinUnit() + "价格后返回的结果{}=========", mr1.getCode() + "===" + mr1.getMessage());
                        if (mr1 != null && mr1.getCode() == 0) {
                            feeDiscountUnitPrice = BigDecimal.valueOf(Double.parseDouble(mr1.getData().toString()));
                        }
                    } catch (Exception e) {
                        logger.info("获取{}价格失败 {}", coin.getFeeDiscountCoinUnit(), e.getMessage());
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
                            MessageResult resultUnit = memberWalletService.freezeBalance(memberWallet, amountNum);
                            if (resultUnit.getCode() != 0) {
                                throw new InformationExpiredException("Information Expired");
                            }

                            //提币申请记录
                            WithdrawRecord withdrawApply = new WithdrawRecord();
                            withdrawApply.setCoin(coin);
                            withdrawApply.setFee(BigDecimal.ZERO);
                            withdrawApply.setBaseCoinFree(baseCoinFree);
                            withdrawApply.setArrivedAmount(sub(amountNum, fee));
                            withdrawApply.setMemberId(user.getId());
                            withdrawApply.setTotalAmount(amountNum);
                            withdrawApply.setAddress(address);
                            withdrawApply.setRemark(remark);
                            withdrawApply.setCanAutoWithdraw(coin.getCanAutoWithdraw());
                            withdrawApply.setFeeDiscountCoinUnit(coin.getFeeDiscountCoinUnit());
                            withdrawApply.setFeeDiscountAmount(feeDiscountAmount);
                            withdrawApply.setComment(coin.getUnit() + ":" + unitPrice + "-" + coin.getFeeDiscountCoinUnit() + ":" + feeDiscountUnitPrice);
                            Assert.isTrue(withdrawApply.getArrivedAmount().compareTo(new BigDecimal("0")) == 1, sourceService.getMessage("INSUFFICIENT_BALANCE"));
                            getService().asyncWithdrawCoin(withdrawApply, address, member, fee,memberWallet,appId);

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
                            MessageResult resultUnit = memberWalletService.freezeBalance(memberWallet, amountNum);
                            if (resultUnit.getCode() != 0) {
                                throw new InformationExpiredException("Information Expired");
                            }

                            //提币申请记录
                            WithdrawRecord withdrawApply = new WithdrawRecord();
                            withdrawApply.setCoin(coin);
                            withdrawApply.setFee(fee);
                            withdrawApply.setBaseCoinFree(baseCoinFree);
                            withdrawApply.setArrivedAmount(sub(amountNum, fee));
                            withdrawApply.setMemberId(user.getId());
                            withdrawApply.setTotalAmount(amountNum);
                            withdrawApply.setAddress(address);
                            withdrawApply.setRemark(remark);
                            withdrawApply.setCanAutoWithdraw(coin.getCanAutoWithdraw());
                            withdrawApply.setFeeDiscountCoinUnit(coin.getFeeDiscountCoinUnit());
                            withdrawApply.setFeeDiscountAmount(feeDiscounPartAmount);
                            withdrawApply.setComment(coin.getUnit() + ":" + unitPrice + "-" + coin.getFeeDiscountCoinUnit() + ":" + feeDiscountUnitPrice);
                            //add|edit|del by  shenzucai 时间： 2018.11.12  原因：条件不通过的时候，报异常
                            Assert.isTrue(withdrawApply.getArrivedAmount().compareTo(new BigDecimal("0")) == 1, sourceService.getMessage("INSUFFICIENT_BALANCE"));
                            getService().asyncWithdrawCoin(withdrawApply, address, member, fee,memberWallet,appId);
                        }
                    } else {
                        throw new InformationExpiredException("Information Expired");
                    }
                } else {
                    Assert.isTrue(false,sourceService.getMessage("PARAMETER_ERROR"));
                }
            } else {
                Assert.isTrue(false,(sourceService.getMessage("COIN_ILLEGAL")));
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
    public void asyncWithdrawCoin(WithdrawRecord withdrawApply, String address, Member member, BigDecimal fee,MemberWallet memberWallet,String appId) throws Exception {
        //add by tansitao 时间： 2018/9/7 原因：从数据库查询带币并设置到coins
        String coins = "ETH,USDT";
        List<CoinTokenVo> coinTokenVoList = coinTokenService.findAll();
        if (coinTokenVoList != null) {
            for (CoinTokenVo coinTokenVo : coinTokenVoList) {
                coins += "," + coinTokenVo.getCoinUnit();
            }
        }
        RPCUtil.coins = coins;
        getService().dealWithdrawCoin(withdrawApply, address, member, fee,memberWallet,appId);
    }

    /**
     *  * 进行提币处理
     *  * @author tansitao
     *  * @time 2018/7/31 17:26 
     *  
     */
    public void dealWithdrawCoin(WithdrawRecord withdrawApply, String address, Member member, BigDecimal fee,MemberWallet memberWallet,String appId) throws Exception {

        BigDecimal amount = withdrawApply.getArrivedAmount();
        BigDecimal baseCoinFree = withdrawApply.getBaseCoinFree();
        Coin coin = withdrawApply.getCoin();
        //edit by tansitao 时间： 2018/4/27 原因：修改用户提币处理逻辑
        RPCUtil rpcUtil = new RPCUtil();

        String coinBase = getCoinBase(coin.getUnit());
        //add by  shenzucai 时间： 2019.05.06  原因：内部转账无限制条件
        if (memberWalletService.hasExistByAddr(address)) {
            withdrawApply.setStatus(WithdrawStatus.PUTING);
            withdrawApply.setIsAuto(BooleanEnum.IS_TRUE);
            WithdrawRecord withdrawRecord = withdrawApplyService.save(withdrawApply);

            logger.info("==========进入平台互转 withdrawRecord={}==========", withdrawRecord);
            String txid = UUIDUtil.getUUID();
            MessageResult result = memberWalletService.recharge(coin, address, amount, txid);
            if (result.getCode() != 0) {
                throw new UnexpectedException("not the desired result");
            }
            //处理成功,data为txid，更新业务订单
            try {
                withdrawRecordService.withdrawSuccess(withdrawRecord.getId(), txid);
            } catch (Exception e) {
                logger.error("===============内部互转失败==================withdrawRecordId" + withdrawRecord.getId(), e);
                throw new UnexpectedException("not the desired result");
            }
        }else if (amount.compareTo(coin.getWithdrawThreshold()) <= 0 && coin.getCanAutoWithdraw().equals(BooleanEnum.IS_TRUE)) {


                //add by  shenzucai 时间： 2018.12.21  原因：增加提现的时候自动提现的限制，规则为一天之内只能自动提现一次，两次自动审核提币时间间隔不能小于24小时(适用外部提币) start
                Boolean allowAutoWithDraw = true;
                if(oneDayAutoWithDraw) {
                    allowAutoWithDraw = withdrawRecordService.allowAutoWithDraw(member.getId(),coin.getName());
                    if(!allowAutoWithDraw) {
                        logger.info("==============一天之内只能自动提现一次，两次自动审核提币时间间隔不能小于24小时(适用外部提币)=========memberId{}======amount{}", withdrawApply.getMemberId(), withdrawApply.getTotalAmount());
                    }
                }



                //判断平台的该币余额是否足够,如果不足则转人工处理，如果allowAutoWithDraw为false也转人工处理
                if (rpcUtil.balanceIsEnough(interfaceLogService, restTemplate, coin, amount) && allowAutoWithDraw) {
                    //add by  shenzucai 时间： 2018.11.22  原因：增加提现的时候自动提现的限制，规则为一天之内只能自动提现一次，两次自动审核提币时间间隔不能小于24小时(适用外部提币) end
                    //判断是否为带币
                    if (StringUtils.isEmpty(coin.getBaseCoinUnit())) {
                        withdrawApply.setStatus(WithdrawStatus.PUTING);
                        withdrawApply.setIsAuto(BooleanEnum.IS_TRUE);
                        WithdrawRecord withdrawRecord = withdrawApplyService.saveAndFlush(withdrawApply);
                        createTransferSilubiumForApply(withdrawApply,member.getId(),memberWallet.getId(),appId,coinBase);
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
                            createTransferSilubiumForApply(withdrawApply,member.getId(),memberWallet.getId(),appId,coinBase);
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
                            WithdrawRecord withdrawRecord = withdrawApplyService.save(withdrawApply);
                            createTransferSilubiumForApply(withdrawApply,member.getId(),memberWallet.getId(),appId,coinBase);
                            if (withdrawRecord != null) {
                                //add by tansitao 时间： 2018/5/2 原因：添加归集钱包功能
                                rpcUtil.collectCoin(interfaceLogService, restTemplate, baseCoin);
                            } else {
                                logger.info("==============提币转人工处理失败========TBEX001=========memberId{}======amount{}", withdrawApply.getMemberId(), withdrawApply.getTotalAmount());
                            }
                        }
                    }
                }
                //如果平台余额不足则转人工处理,
                else {
                    withdrawApply.setStatus(WithdrawStatus.PROCESSING);
                    withdrawApply.setIsAuto(BooleanEnum.IS_FALSE);
                    WithdrawRecord withdrawRecord = withdrawApplyService.save(withdrawApply);
                    createTransferSilubiumForApply(withdrawApply,member.getId(),memberWallet.getId(),appId,coinBase);
                    if (withdrawRecord != null) {
                        //add by tansitao 时间： 2018/5/2 原因：添加归集钱包功能
                        rpcUtil.collectCoin(interfaceLogService, restTemplate, coin);
                    } else {
                        logger.info("==============提币转人工处理失败=========TBEX002========memberId{}======amount{}", withdrawApply.getMemberId(), withdrawApply.getTotalAmount());
                    }

                }

        } else {
            withdrawApply.setStatus(WithdrawStatus.PROCESSING);
            withdrawApply.setIsAuto(BooleanEnum.IS_FALSE);
            WithdrawRecord withdrawRecord = withdrawApplyService.save(withdrawApply);
            createTransferSilubiumForApply(withdrawApply,member.getId(),memberWallet.getId(),appId,coinBase);
            if (withdrawRecord == null) {
                logger.info("==============提币转人工处理失败=========TBEX003========memberId{}======amount{}", withdrawApply.getMemberId(), withdrawApply.getTotalAmount());
            }
        }
    }

    private  void createTransferSilubiumForApply(WithdrawRecord withdrawApply,Long memberId,Long memberWalletId,String appId,String coinBase){

        //add by  shenzucai 时间： 2019.01.21  原因：进行 交易记录表 转出记录初始化 start
        TransferSilubium transferSilubium = new TransferSilubium();
        transferSilubium.setArriveAmount(withdrawApply.getArrivedAmount());
        transferSilubium.setBaseCoinFree(withdrawApply.getBaseCoinFree());
        transferSilubium.setCoinUnit(withdrawApply.getCoin().getUnit());
        transferSilubium.setComment(withdrawApply.getComment());
        transferSilubium.setCreateTime(DateUtil.getCurrentDate());
        transferSilubium.setFeeDiscountAmount(withdrawApply.getFeeDiscountAmount());
        transferSilubium.setFeeDiscountCoinUnit(withdrawApply.getFeeDiscountCoinUnit());
        transferSilubium.setFee(withdrawApply.getFee());
        transferSilubium.setFromAddress(coinBase);
        transferSilubium.setToAddress(withdrawApply.getAddress());
        transferSilubium.setTotalAmount(withdrawApply.getTotalAmount());
        transferSilubium.setTransferHash(withdrawApply.getId().toString());
        transferSilubium.setTransferStatus(0);
        transferSilubium.setTransferType(1);
        transferSilubium.setUserId(memberId);
        transferSilubium.setWalletId(memberWalletId);
        transferSilubium.setAppId(appId);
        // DataSourceContextHolder.setWrite();
        transferSilubiumService.insertOrupdate(transferSilubium);
        // DataSourceContextHolder.setRead();
        //add by  shenzucai 时间： 2019.01.21  原因：进行 交易记录表 转出记录初始化 end

    }

    private TransferSilubiumController getService() {
        return SpringContextUtil.getBean(this.getClass());
    }
}

