package com.spark.bitrade.controller;


import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.dto.LockAbstractDto;
import com.spark.bitrade.dto.LockTypeDto;
import com.spark.bitrade.dto.MemberDepositDTO;
import com.spark.bitrade.dto.MemberTransactionDTO;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.enums.MessageCode;
import com.spark.bitrade.feign.ICoinExchange;
import com.spark.bitrade.feign.IOtcServerV2Service;
import com.spark.bitrade.job.CheckExchangeRate;
import com.spark.bitrade.service.*;
import com.spark.bitrade.system.CoinExchangeFactory;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.ExcelUtil;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.vo.MemberActivityIncomeVo;
import com.sparkframework.lang.Convert;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.hibernate.validator.constraints.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static com.spark.bitrade.util.MessageRespResult.error;
import static com.spark.bitrade.util.MessageRespResult.success;
import static org.springframework.util.Assert.isTrue;

@RestController
@RequestMapping("/asset")
@Slf4j
public class AssetController {
    private Logger logger = LoggerFactory.getLogger(AssetController.class);
    @Autowired
    private MemberWalletService walletService;
    @Autowired
    private MemberTransactionService transactionService;
    @Autowired
    private CoinExchangeFactory coinExchangeFactory;
    @Autowired
    private CoinService coinService;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private ICoinExchange coinExchange;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${gcx.match.max-limit:1000}")
    private double gcxMatchMaxLimit;
    @Value("${gcx.match.each-limit:5}")
    private double gcxMatchEachLimit;
    @Autowired
    private KafkaTemplate kafkaTemplate ;
    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberSecuritySetService memberSecuritySetService;

    @Autowired
    private LockCoinActivitieProjectService lockCoinActivitieProjectService;

    @Autowired
    private LockCoinDetailService lockCoinDetailService;

    @Autowired
    private CheckExchangeRate checkExchangeRate;
    @Autowired
    private IOtcServerV2Service iOtcServerV2Service;

    /**
     * 用户钱包信息
     *
     * @param member
     * @return
     */
    @RequestMapping("wallet")
    public MessageResult findWallet(@SessionAttribute(SESSION_MEMBER) AuthMember member) {
    	
    	////editby tansitao 时间： 2018/4/22 原因：修改为左连接获取钱包数据
        List<MemberWallet> wallets =  walletService.findAllByMemberIdLeftJion(member.getId());
        List<MemberActivityIncomeVo> memberActivityIncomeVos = new ArrayList<>();
        //获取用户默认法币
        //String fbUnit = iOtcServerV2Service.getMemberCurrenc(member.getId()).replaceAll("\"", "");


        wallets.forEach(wallet -> {
            //edit by tansitao 时间： 2018/11/21 原因：使用MemberActivityIncomeVo用来做我的资产数据返回
            MemberActivityIncomeVo memberActivityIncomeVo = new MemberActivityIncomeVo();
            CoinExchangeFactory.ExchangeRate rate = coinExchangeFactory.get(wallet.getCoin().getUnit());
            if (rate != null
                    && rate.getUsdRate().compareTo(BigDecimal.ZERO) > 0
                    && rate.getCnyRate().compareTo(BigDecimal.ZERO) > 0) {
                wallet.getCoin().setUsdRate(rate.getUsdRate().doubleValue());
                wallet.getCoin().setCnyRate(rate.getCnyRate().doubleValue());
            } else {
                BigDecimal cnyRate = checkExchangeRate.getUsdCnyRate();
                BigDecimal usdRate = checkExchangeRate.getUsdRate(wallet.getCoin().getUnit());
                wallet.getCoin().setUsdRate(usdRate.doubleValue());
                wallet.getCoin().setCnyRate(cnyRate == null ? 0D : cnyRate.multiply(usdRate).setScale(8, BigDecimal.ROUND_DOWN).doubleValue());
            }
            /*if(!StringUtils.isEmpty(fbUnit)) {
            	MessageRespResult fcResult = iOtcServerV2Service.getCurrencyRate(fbUnit, wallet.getCoin().getUnit());
        		if(fcResult.isSuccess()) {
        			Double fbRate = (Double)fcResult.getData();
        			wallet.getCoin().setCnyRate(fbRate);
        		}
            }*/
            memberActivityIncomeVo.setMemberWallet(wallet);

            memberActivityIncomeVo.setLockIncome(BigDecimal.ZERO);
            memberActivityIncomeVo.setActivitieNumType(ActivitieNumType.none);
            memberActivityIncomeVo.setRefActivitieId(0L);
            memberActivityIncomeVo.setMinLimit(BigDecimal.ZERO);
            memberActivityIncomeVos.add(memberActivityIncomeVo);
        });
        MessageResult mr = MessageResult.success("success");
        mr.setData(memberActivityIncomeVos); //add by tansitao 时间： 2018/11/21 原因：使用MemberActivityIncomeVo用来做我的资产数据返回
        return mr;
    }

    /***
      * 获取用户新币的地址,
      * @author tansitao
      * @time 2018/4/18 16:03 
     * @param member
     * @param coinName
     */
    @RequestMapping("wallet/getCoinAddr")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult findWalletAddrByCoin(@SessionAttribute(SESSION_MEMBER) AuthMember member, String coinName) throws InvocationTargetException, IllegalAccessException {
        //add by  shenzucai 时间： 2019.04.23  原因：防止币种为空的情况
        if(StringUtils.isEmpty(coinName)){
            return new MessageResult(100, coinName + msService.getMessage("MISSING_COIN_TYPE"));
        }
        Coin coin = coinService.findOne(coinName);
        isTrue(coin != null, coinName + msService.getMessage("COIN_NOT_EXIST"));//edit by tansitao 时间： 2018/5/21 原因：修改为国际化
        Member userMember = new Member();
        userMember.setId(member.getId());
        //远程RPC服务URL,后缀为币种单位
        String serviceName = "SERVICE-RPC-" + coin.getUnit();
        String account = "U" + member.getId();
        coin.setName(coinName);
        MemberWallet wallet = walletService.findByCoinAndMember(coin, userMember);
        //add|edit|del by  shenzucai 时间： 2019.02.19  原因：由于后面返回
        MemberWallet memberWallet = new MemberWallet();
        //如果该币已有钱包地址返回结果
        if (wallet != null && wallet.getAddress() != null && !"".equals(wallet.getAddress())) {
            return new MessageResult(100, coinName + msService.getMessage("ADDR_HAS_EXIST"));//edit by tansitao 时间： 2018/5/21 原因：修改为国际化
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
                                return new MessageResult(500, msService.getMessage("WALLET_GET_FAIL"));//edit by tansitao 时间： 2018/5/21 原因：修改为国际化
                            }
                        }else{
                            return new MessageResult(500, coinName + msService.getMessage("WALLET_GET_FAIL"));//add by tansitao 时间： 2018/7/25 原因：钱包接口返回异常获取钱包失败
                        }
                    }else{
                        return new MessageResult(500, coinName + msService.getMessage("WALLET_GET_FAIL"));//edit by tansitao 时间： 2018/5/21 原因：修改为国际化
                    }
                } else {
                    //add|edit|del by  shenzucai 时间： 2018.06.21  原因：如果获取的是代币的地址，且主币地址也不存在的时候，就会生成主币和代币的地址
                    Coin baseCoin = coinService.findByUnit(coin.getBaseCoinUnit());
                    MemberWallet baseWallet = walletService.findByCoinAndMember(baseCoin, userMember);
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
                                    baseWallet1.setMemberId(member.getId());
                                    baseWallet1.setIsLock(BooleanEnum.IS_FALSE);
                                    walletService.save(baseWallet1);
                                }else{
                                    //add by  shenzucai 时间： 2018.11.20  原因：由于原先操作可能影响账，遂改成sql形式
                                    // baseWallet.setAddress(baseAddress);
                                    // walletService.save(baseWallet);
                                    walletService.updateMemberWalletAddress(baseWallet.getId(),baseAddress);
                                }
                                address = baseAddress;
                            }else{
                                return new MessageResult(500, msService.getMessage("WALLET_GET_FAIL"));//edit by tansitao 时间： 2018/5/21 原因：修改为国际化
                            }


                            // logger.error("请先获取{}地址，然后再获取{}地址", baseCoin.getName(), coinName);

                    }else {
                        address = baseWallet.getAddress();
                    }
                }
                //edit by shenzucai 时间： 2018.04.22 原因：判断是否为代币，如果不是，则走rpc获取地址 end -----------------------
            } catch (Exception e) {
                logger.error("call {} failed,error={}", serviceName, e.getMessage());
                return new MessageResult(500, coinName + msService.getMessage("WALLET_GET_FAIL"));//edit by tansitao 时间： 2018/5/21 原因：修改为国际化
            }
            //如果该用户无钱包信息，创建新的钱包信息，并添加其他基本信息
            if (null == wallet) {
                wallet = new MemberWallet();
                wallet.setAddress(address);
                wallet.setBalance(BigDecimal.valueOf(0));
                wallet.setCoin(coin);
                wallet.setFrozenBalance(BigDecimal.valueOf(0));
                wallet.setLockBalance(BigDecimal.valueOf(0));
                wallet.setMemberId(member.getId());
                wallet.setIsLock(BooleanEnum.IS_FALSE);
                walletService.save(wallet);
            }
            //如果有钱包信息，则设置钱包地址
            else {
                //add by  shenzucai 时间： 2018.11.20  原因：由于原先操作可能影响账，遂改成sql形式
                // wallet.setAddress(address);
                walletService.updateMemberWalletAddress(wallet.getId(),address);
            }
            BeanUtils.copyProperties(wallet,memberWallet);
            memberWallet.setAddress(address);

        }
        MessageResult mr = MessageResult.success("success");
        mr.setData(memberWallet);
        return mr;
    }

    /**
     * 查询特定类型的记录
     * @param member
     * @param pageNo
     * @param pageSize
     * @param type
     * @return
     */
    @RequestMapping("transaction")
    public Page<MemberTransaction> findTransaction(@SessionAttribute(SESSION_MEMBER) AuthMember member, int pageNo, int pageSize, TransactionType type, String unit){
        return transactionService.queryByMember(member.getId(),pageNo,pageSize,type,unit);
    }

    /**
     * 查询充币记录
     * @param member
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("rechargeRecord")
    public PageInfo<MemberDepositDTO> findMemberRechargeRecord(@SessionAttribute(SESSION_MEMBER) AuthMember member,
                                                               int pageNo, int pageSize, String unit){
        return transactionService.findMemberRechargeRecord(member.getId(),pageNo,pageSize,unit);
    }

    /**
     * 查询所有记录
     * @param member
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("transaction/all")
    public Page<MemberTransaction> findTransaction(@SessionAttribute(SESSION_MEMBER) AuthMember member, HttpServletRequest request, int pageNo, int pageSize,String unit) throws ParseException {
        TransactionType type = null;
        if(request.getParameter("type")!=null){
            type = TransactionType.valueOfOrdinal(Convert.strToInt(request.getParameter("type"),0));
        }

        TransferDirection direction = TransferDirection.NONE;
        String direct = request.getParameter("direction");
        if (StringUtils.hasText(direct)) {
            direction = TransferDirection.of(Convert.strToInt(direct, 0));
        }

        String startDate = "";
        String endDate = "";
        if(request.getParameter("dateRange") != null){
            String[] parts = request.getParameter("dateRange").split("~");
            startDate = parts[0].trim();
            endDate = parts[1].trim();
        }
        return transactionService.queryByMember(member.getId(),pageNo,pageSize,type, direction, startDate,endDate,unit);
    }

    /**
     * 导出交易记录
     * @author tansitao
     * @time 2018/8/23 11:58 
     */
//    @SessionAttribute(SESSION_MEMBER) AuthMember member,
    @RequestMapping("transaction/out-excel")
    public MessageResult outExcel(@SessionAttribute(SESSION_MEMBER) AuthMember member, HttpServletResponse response, HttpServletRequest request) throws Exception {
//        Member member = memberService.findOne(memberId);
        isTrue(member != null, msService.getMessage("MEMBER_NOT_EXISTS"));
        String type = request.getParameter("type");
        String startDate = "";
        String endDate = "";
        if(request.getParameter("dateRange") != null){
            String[] parts = request.getParameter("dateRange").split("~");
            startDate = parts[0].trim();
            endDate = parts[1].trim();
        }
        List<MemberTransactionDTO> list = transactionService.findMemberTransaction(startDate, endDate, type, member.getId());
        if(list != null && list.size() > 0 ){
            for (MemberTransactionDTO memberTransactionDTO :list) {
                memberTransactionDTO.setType(TransactionType.valueOfOrdinal(Convert.strToInt(memberTransactionDTO.getType(),0)).getCnName());
            }
            String fileName = member.getMobilePhone() + "_TransactionRecords_" + DateUtil.dateToYYYYMMDDHHMMSS(new Date());
            //edit by tansitao 时间： 2018/9/5 原因：修改为导出csv文件
            ExcelUtil.listToCSV(list, MemberTransactionDTO.class.getDeclaredFields(), response, fileName);
            return MessageResult.success();
        }
        return MessageResult.error( msService.getMessage("NOT_HAVE_TRANSACTION"));
    }



    @RequestMapping("wallet/{symbol}")
    public MessageRespResult findWalletBySymbol(@SessionAttribute(SESSION_MEMBER) AuthMember member, @PathVariable String symbol ){
        Member m = memberService.findOne(member.getId());
        return findWalletBySymbol(m,symbol);
    }

    //todo pay模块移走时需一起移走
    @PostMapping("walletByUsername")
    public MessageRespResult findWalletBySymbol(String username,String unit ){
        Member m = memberService.findMemberByMobilePhoneOrEmail(username,username);
        if (m == null){
            return error(MessageCode.MISSING_USER);
        }
        return findWalletBySymbol(m, unit);
    }

    @RequestMapping("walletByMember/{symbol}")
    public MessageRespResult findWalletBySymbol(Member member,String symbol ){
        MessageRespResult mr = success("success");

        //edit by yangch 时间： 2018.05.21 原因：获取用户账户余额，没有对应币种的账户时则创建对应币种的账户（解决新上线币币交易的币种时，老用户在没有对应币种账户的情况下仍然可以买入的bug）
        Coin coin = coinService.findByUnit(symbol);
        if(coin == null){ //判断接口接收的币种是否是平台币种
            mr.setData("coin is not official");
            return mr;
        }
        //edit by tansitao 时间： 2018/6/25 原因：修改为只读模式
        MemberWallet memberWallet = walletService.findByCoinNameAndMemberIdReadOnly(coin.getName(),member.getId());
        if(null == memberWallet) {
            //没有对应的账户，新建账户
            memberWallet = walletService.createMemberWallet(member.getId(), coin);
        }
        else
        {
            memberWallet.setCoin(coin);
        }
        mr.setData(memberWallet);
        //mr.setData(walletService.findByCoinUnitAndMemberId(symbol,member.getId()));
        return mr;
    }

    /**
     * 币种转化(GCC配对GCX,特殊用途，其他项目可以不管)
     * @return
     */
    @RequestMapping("wallet/match-check")
    public MessageResult transformCheck(@SessionAttribute(SESSION_MEMBER) AuthMember member) throws Exception {
        //edit by yangch 时间： 2018.07.13 原因：屏蔽无用的代码
        //BigDecimal amount = BigDecimal.ZERO;
        /* String symbol = "GCX";
        List<MemberTransaction> transactions = transactionService.findMatchTransaction(member.getId(),symbol);
        for(MemberTransaction transaction:transactions){
            amount = amount.add(transaction.getAmount());
        }
       MemberWallet gccWallet = walletService.findByCoinUnitAndMemberId("GCC",member.getId());
        if(amount.compareTo(gccWallet.getBalance()) > 0){
            amount = gccWallet.getBalance();
        }
        MemberWallet gcxWallet = walletService.findByCoinUnitAndMemberId("GCX",member.getId());
        if(amount.compareTo(gcxWallet.getBalance())>0){
            amount = gcxWallet.getBalance();
        }
        MessageResult mr = new MessageResult(0,"success");
        mr.setData(amount.setScale(4,BigDecimal.ROUND_DOWN));
        */
        MessageResult mr = new MessageResult(0,"success");
        mr.setData(0);
        return mr;
    }

    @RequestMapping("wallet/match")
    public MessageResult transform(@SessionAttribute(SESSION_MEMBER) AuthMember member,BigDecimal amount) throws Exception {
        String symbol = "GCX";
        List<MemberTransaction> transactions = transactionService.findMatchTransaction(member.getId(),symbol);
        BigDecimal maxAmount = BigDecimal.ZERO;
        for(MemberTransaction transaction:transactions){
            maxAmount = maxAmount.add(transaction.getAmount());
        }
        if(amount.compareTo(maxAmount) > 0){
            return new MessageResult(500,"insufficient GCX amount");
        }
//        MemberWallet gccWallet = walletService.findByCoinUnitAndMemberId("GCC",member.getId());
       /* if(gccWallet.getBalance().compareTo(amount) < 0){
            return new MessageResult(500,"insufficient GCC amount");
        }
        MemberWallet gcxWallet = walletService.findByCoinUnitAndMemberId("GCX",member.getId());
        if(gcxWallet.getBalance().compareTo(amount) < 0){
            return new MessageResult(500,"insufficient GCX amount");
        }*/

        if(transactionService.isOverMatchLimit(DateUtil.YYYY_MM_DD.format(new Date()), gcxMatchMaxLimit)){
            return new MessageResult(500, msService.getMessage("SOLD_OUT"));//edit by tansitao 时间： 2018/5/21 原因：修改为国际化
        }
        if(amount.compareTo(new BigDecimal(gcxMatchEachLimit))>0){
            return new MessageResult(500,msService.getMessage("CANNOT_EXCEED") + 5);//edit by tansitao 时间： 2018/5/21 原因：修改为国际化
        }
        transactionService.matchWallet(member.getId(),"GCX",amount);
        return new MessageResult(0,"success");
    }

    //add by yangch 时间： 2018.05.02 原因：合并代码
    @RequestMapping("wallet/reset-address")
    public MessageResult resetWalletAddress(@SessionAttribute(SESSION_MEMBER) AuthMember member,String unit){
        try {
            JSONObject json = new JSONObject();
            json.put("uid", member.getId());
            kafkaTemplate.send("reset-member-address",unit,json.toJSONString());
            return MessageResult.success(msService.getMessage("SUBMIT_SUCCESS"));//edit by tansitao 时间： 2018/5/21 原因：修改为国际化
        } catch (Exception e) {
            return MessageResult.error(msService.getMessage("SUBMIT_FAIL"));//edit by tansitao 时间： 2018/5/21 原因：修改为国际化
        }
    }


    @PostMapping("isShow")
    @ApiOperation(value = "查询/修改是否展示总资产",notes = "查询/修改是否展示总资产")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "isShow",value = "是否展示总资产（0否，1是），为空时为查询，有值时为修改")
    })
    public MessageResult isShow(@SessionAttribute(SESSION_MEMBER) AuthMember user, Integer isShow){
        MemberSecuritySet memberSecuritySet=memberSecuritySetService.findOneBymemberId(user.getId());
        if(memberSecuritySet == null)
        {
            memberSecuritySet = new MemberSecuritySet();
            memberSecuritySet.setMemberId(user.getId());
        }
        if (isShow!=null){
            memberSecuritySet.setIsOpenPropertyShow(isShow==0?BooleanEnum.IS_FALSE:BooleanEnum.IS_TRUE);
            memberSecuritySetService.save(memberSecuritySet);
        }
        return MessageResult.success("success",memberSecuritySet.getIsOpenPropertyShow());
    }

}
