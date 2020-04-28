package com.spark.bitrade.service;

import com.spark.bitrade.annotation.MybatisTransactional;
import com.spark.bitrade.annotation.WriteDataSource;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.enums.MessageCode;
import com.spark.bitrade.mapper.dao.MemberTransactionCoreMapper;
import com.spark.bitrade.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.spark.bitrade.util.MessageRespResult.error;

/**
 * 钱包支付
 * @author Zhang Yanjun
 * @time 2019.01.15 15:05
 */
@Service
@Slf4j
public class PayWalletNewService {
    @Autowired
    private IPayWalletPlatMemberBindService iPayWalletPlatMemberBindService;
    @Autowired
    private IPayRoleFeeRateConfigService iPayRoleFeeRateConfigService;
    @Autowired
    private PayRoleConfigService payRoleConfigService;
    @Autowired
    private ICoinService coinService;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private IMemberWalletService memberWalletService;
    @Autowired
    private IdWorkByTwitter idWorkByTwitter;
    @Autowired
    private IPayFastRecordService iPayFastRecordService;
    @Autowired
    private MemberTransactionCoreMapper memberTransactionCoreMapper;
    @Autowired
    private IPaySupportCoinConfigService iPaySupportCoinConfigService;

    /**
     * 角色配置
     * @param memberId
     * @param appId
     * @return
     */
    public PayRoleConfig getPayRoleConfig(Long memberId, String appId ){

        PayWalletPlatMemberBind platMemberBind =iPayWalletPlatMemberBindService.findByMemberIdAndAppId(memberId,appId);
        if (platMemberBind == null){
            return payRoleConfigService.findDefaultRole();
        }else {

        }return payRoleConfigService.findOneById(platMemberBind.getRoleId());
    }

    /**
     * 平台内互转
     * @author Zhang Yanjun
     * @time 2019.01.14 17:10
     * @param fromMember 转出账户(支付方)
     * @param toMember  接收账户
     * @param amount 转账数量
     * @param transferType 交易类型
     * @param platform 转账方应用ID
     * @param platformTo 收款方应用ID
     */
    // @Transactional(transactionManager="transactionManager",rollbackFor = Exception.class)
    @MybatisTransactional(rollbackFor = Exception.class)
    @WriteDataSource
    public MessageRespResult<PayFastRecord> platformTransfer(MemberWallet fromMemberWallet , MemberWallet toMemberWallet,
                                                     BigDecimal amount, Member fromMember, Member toMember,
                                                     RestTemplate restTemplate, PayTransferType transferType, String platform, String platformTo) throws Exception {
        MessageResult result;
        //收款数额
        BigDecimal receiveAmount;
        //换算价格后的收款手续费
        BigDecimal afterFee;


        //支付方角色
        PayRoleConfig forwardRole = getPayRoleConfig(fromMemberWallet.getMemberId(), platform);
        //收款方角色
        PayRoleConfig receiveRole = getPayRoleConfig(toMemberWallet.getMemberId(), platformTo);

        PayFastRecord payFastRecord = new PayFastRecord();
        payFastRecord.setId(idWorkByTwitter.nextId());

        //根据收款角色 查询收支角色手续费率配置表，计算手续费  如果手续费币种与交易币种不统一，要将交易币种换算成手续费币种
        //操作钱包手续费为收款方扣除

        PayRoleFeeRateConfig receiveFee = iPayRoleFeeRateConfigService.findByIdAndTradeUnit(receiveRole.getId(), toMemberWallet.getCoin().getUnit() );

        if(receiveFee != null){

            //手续费类型：固定值
            if (receiveFee.getFeeType().equals(CoinFeeType.FIXED)){
                receiveAmount = amount.subtract(receiveFee.getIncomeFee());
                //到账金额是否大于0
                if (receiveAmount.compareTo(BigDecimal.ZERO) < 1){
                    return error(MessageCode.ACCOUNT_AMOUNT_INSUFFICIENT);
                }
                //手续费币种和交易币种是否相同
                if (receiveFee.getFeeUnit().equals(receiveFee.getTradeUnit())){
                    //相同，修改钱包，收款方+（转款数额-手续费），支付方-转账数额

                    result = memberWalletService.subtractBalance(fromMemberWallet, amount);
                    if (result.getCode() != 0){
//                        return MessageRespResult.error(MessageCode.FAILED_SUBTRACT_BALANCE);
                        throw new Exception(MessageCode.FAILED_SUBTRACT_BALANCE.getEnCode());
                    }

                    result = memberWalletService.increaseBalance(toMemberWallet.getId(), receiveAmount);
                    if (result.getCode() != 0){
//                        return MessageRespResult.error(MessageCode.FAILED_ADD_BALANCE);
                        throw new Exception(MessageCode.FAILED_ADD_BALANCE.getEnCode());
                    }
                    //存入快速支付记录表
                    payFastRecord.setTradeSn(String.valueOf(idWorkByTwitter.nextId()));
                    payFastRecord.setReceiptId(toMemberWallet.getMemberId());
                    payFastRecord.setReceiptWalletId(toMemberWallet.getId());
                    payFastRecord.setReceiptRoleId(receiveRole.getId());
                    payFastRecord.setReceiptRole(receiveFee.getRoleName());
                    payFastRecord.setReceiptAddress(toMemberWallet.getAddress());
                    payFastRecord.setPayId(fromMemberWallet.getMemberId());
                    payFastRecord.setPayWalletId(fromMemberWallet.getId());
                    payFastRecord.setPayRoleId(forwardRole.getId());
                    payFastRecord.setPayRole(forwardRole==null ? "" : forwardRole.getRoleName());
                    payFastRecord.setPayAddress(fromMemberWallet.getAddress());
                    payFastRecord.setAmount(amount);
                    payFastRecord.setArrivedAmount(receiveAmount);
                    payFastRecord.setUnit(fromMemberWallet.getCoin().getUnit());
                    payFastRecord.setFeeUnit(receiveFee.getFeeUnit());
                    payFastRecord.setFeeType(CoinFeeType.FIXED);
                    payFastRecord.setFee(receiveFee.getIncomeFee());
                    payFastRecord.setTradeType(transferType);
                    payFastRecord.setCreateTime(new Date());
                    payFastRecord.setStatus(PayTransferStatus.SUCCESS);
                    payFastRecord.setReceiptPhone(toMember.getMobilePhone());
                    payFastRecord.setPayPhone(fromMember.getMobilePhone());
                    payFastRecord.setPlatform(platform);
                    payFastRecord.setPlatformTo(platformTo);
//                    payFastRecord.setComment();
                    iPayFastRecordService.save(payFastRecord);

                    PayFastRecord payRecord = iPayFastRecordService.findOneByTradeSn(payFastRecord.getTradeSn());
                    log.info("========payFastRecordId-{}============",payRecord.getId());
                    //交易记录
                    //支付钱包，收款钱包，手续费钱包，支付数额，收款数额，手续费，快速支付记录id
                    getService().saveTransaction(fromMemberWallet,toMemberWallet,null,null,amount,receiveAmount,receiveFee.getIncomeFee(),payRecord.getId());

                }else{
                    //不同，获取价格
                    result = getService().convertFee(receiveFee,receiveFee.getIncomeFee(),restTemplate);
                    if (result.getCode() != 0){
//                        return MessageRespResult.error(MessageCode.FAILED_COIN_PRICE);
                        return MessageRespResult.error(result.getCode(),result.getMessage());
                    }
                    afterFee = CommonUtils.toBigDecimal(result.getData());

                    //支付方-转款数额,接收方：交易币种钱包+转账数额，手续费币种钱包-换算后的手续费
                    result = memberWalletService.subtractBalance(fromMemberWallet, amount);
                    if (result.getCode() != 0){
//                        return MessageRespResult.error(MessageCode.FAILED_SUBTRACT_BALANCE);
                        throw new Exception(MessageCode.FAILED_SUBTRACT_BALANCE.getEnCode());
                    }
                    //接收方
                    MemberWallet feeWallet = memberWalletService.findByCoinUnitAndMemberId(receiveFee.getFeeUnit(), toMemberWallet.getMemberId());
                    if (feeWallet == null){
//                        return MessageRespResult.error(MessageCode.MISSING_ACCOUNT);
                        throw new Exception(MessageCode.MISSING_ACCOUNT.getEnCode());
                    }
                    result = memberWalletService.subtractBalance(feeWallet, afterFee);
                    if (result.getCode() != 0){
//                        return MessageRespResult.error(MessageCode.FAILED_SUBTRACT_BALANCE);
                        throw new Exception(MessageCode.FAILED_SUBTRACT_BALANCE.getEnCode());
                    }

                    result = memberWalletService.increaseBalance(toMemberWallet.getId(),amount);
                    if (result.getCode() != 0){
//                        return MessageRespResult.error(MessageCode.FAILED_ADD_BALANCE);
                        throw new Exception(MessageCode.FAILED_ADD_BALANCE.getEnCode());
                    }
                    //存入快速支付记录表
                    payFastRecord.setTradeSn(String.valueOf(idWorkByTwitter.nextId()));
                    payFastRecord.setReceiptId(toMemberWallet.getMemberId());
                    payFastRecord.setReceiptWalletId(toMemberWallet.getId());
                    payFastRecord.setReceiptRoleId(receiveRole.getId());
                    payFastRecord.setReceiptRole(receiveFee.getRoleName());
                    payFastRecord.setReceiptAddress(toMemberWallet.getAddress());
                    payFastRecord.setPayId(fromMemberWallet.getMemberId());
                    payFastRecord.setPayWalletId(fromMemberWallet.getId());
                    payFastRecord.setPayRoleId(forwardRole.getId());
                    payFastRecord.setPayRole(forwardRole==null ? "" : forwardRole.getRoleName());
                    payFastRecord.setPayAddress(fromMemberWallet.getAddress());
                    payFastRecord.setAmount(amount);
                    payFastRecord.setArrivedAmount(amount);
                    payFastRecord.setUnit(fromMemberWallet.getCoin().getUnit());
                    payFastRecord.setFeeUnit(receiveFee.getFeeUnit());
                    payFastRecord.setFeeType(CoinFeeType.SCALE);
                    payFastRecord.setFee(afterFee);
                    payFastRecord.setTradeType(transferType);
                    payFastRecord.setCreateTime(new Date());
                    payFastRecord.setStatus(PayTransferStatus.SUCCESS);
                    payFastRecord.setReceiptPhone(toMember.getMobilePhone());
                    payFastRecord.setPayPhone(fromMember.getMobilePhone());
                    payFastRecord.setPlatform(platform);
                    payFastRecord.setPlatformTo(platformTo);
//                    payFastRecord.setComment();
                    iPayFastRecordService.save(payFastRecord);


                    PayFastRecord payRecord = iPayFastRecordService.findOneByTradeSn(payFastRecord.getTradeSn());
                    //交易记录
                    //支付钱包，收款钱包，手续费钱包，支付数额，收款数额，手续费，快速支付记录id
                    getService().saveTransaction(fromMemberWallet,toMemberWallet,feeWallet,receiveFee.getFeeUnit(),amount,amount,afterFee,payRecord.getId());
                }
            }else{
                //手续费类型：百分比
                BigDecimal fee = receiveFee.getIncomeFee().multiply(amount);

                //手续费币种和交易币种是否相同
                if (receiveFee.getFeeUnit().equals(receiveFee.getTradeUnit())){
                    //相同，修改钱包，收款方+（转款数额-手续费），支付方-转账数额
                    receiveAmount = amount.subtract(fee);
                    //到账金额是否大于0
                    if (receiveAmount.compareTo(BigDecimal.ZERO) < 1){
                        return error(MessageCode.ACCOUNT_AMOUNT_INSUFFICIENT);
                    }

                    result = memberWalletService.subtractBalance(fromMemberWallet, amount);
                    if (result.getCode() != 0){
//                        return MessageRespResult.error(MessageCode.FAILED_SUBTRACT_BALANCE);
                        throw new Exception(MessageCode.FAILED_SUBTRACT_BALANCE.getEnCode());
                    }

                    result = memberWalletService.increaseBalance(toMemberWallet.getId(), receiveAmount);
                    if (result.getCode() != 0){
//                        return MessageRespResult.error(MessageCode.FAILED_ADD_BALANCE);
                        throw new Exception(MessageCode.FAILED_ADD_BALANCE.getEnCode());
                    }

                    //存入快速支付记录表
                    payFastRecord.setTradeSn(String.valueOf(idWorkByTwitter.nextId()));
                    payFastRecord.setReceiptId(toMemberWallet.getMemberId());
                    payFastRecord.setReceiptWalletId(toMemberWallet.getId());
                    payFastRecord.setReceiptRoleId(receiveRole.getId());
                    payFastRecord.setReceiptRole(receiveFee.getRoleName());
                    payFastRecord.setReceiptAddress(toMemberWallet.getAddress());
                    payFastRecord.setPayId(fromMemberWallet.getMemberId());
                    payFastRecord.setPayWalletId(fromMemberWallet.getId());
                    payFastRecord.setPayRoleId(forwardRole.getId());
                    payFastRecord.setPayRole(forwardRole==null ? "" : forwardRole.getRoleName());
                    payFastRecord.setPayAddress(fromMemberWallet.getAddress());
                    payFastRecord.setAmount(amount);
                    payFastRecord.setArrivedAmount(receiveAmount);
                    payFastRecord.setUnit(fromMemberWallet.getCoin().getUnit());
                    payFastRecord.setFeeUnit(receiveFee.getFeeUnit());
                    payFastRecord.setFeeType(CoinFeeType.SCALE);
                    payFastRecord.setFee(fee);
                    payFastRecord.setTradeType(transferType);
                    payFastRecord.setCreateTime(new Date());
                    payFastRecord.setStatus(PayTransferStatus.SUCCESS);
                    payFastRecord.setReceiptPhone(toMember.getMobilePhone());
                    payFastRecord.setPayPhone(fromMember.getMobilePhone());
                    payFastRecord.setPlatform(platform);
                    payFastRecord.setPlatformTo(platformTo);
//                    payFastRecord.setComment();
                    iPayFastRecordService.save(payFastRecord);

                    PayFastRecord payRecord = iPayFastRecordService.findOneByTradeSn(payFastRecord.getTradeSn());
                    //交易记录
                    //支付钱包，收款钱包，手续费钱包，支付数额，收款数额，手续费，快速支付记录id
                    getService().saveTransaction(fromMemberWallet,toMemberWallet,null,null,amount,receiveAmount,fee,payRecord.getId());
                }else{
                    //不同，获取价格
                    result = getService().convertFee(receiveFee,fee,restTemplate);
                    if (result.getCode() != 0){
//                        return MessageRespResult.error(MessageCode.FAILED_COIN_PRICE);
                        return MessageRespResult.error(result.getCode(),result.getMessage());
                    }
                    afterFee = CommonUtils.toBigDecimal(result.getData());

                    //支付方-转款数额,接收方：交易币种钱包+转账数额，手续费币种钱包-换算后的手续费
                    result = memberWalletService.subtractBalance(fromMemberWallet, amount);
                    if (result.getCode() != 0){
//                        return MessageRespResult.error(MessageCode.FAILED_SUBTRACT_BALANCE);
                        throw new Exception(MessageCode.FAILED_SUBTRACT_BALANCE.getEnCode());
                    }

                    MemberWallet feeWallet = memberWalletService.findByCoinUnitAndMemberId(receiveFee.getFeeUnit(), toMemberWallet.getMemberId());
                    if (feeWallet == null){
//                        return MessageRespResult.error(MessageCode.MISSING_ACCOUNT);
                        throw new Exception(MessageCode.MISSING_ACCOUNT.getEnCode());
                    }
                    result = memberWalletService.subtractBalance(feeWallet, afterFee);
                    if (result.getCode() != 0){
//                        return MessageRespResult.error(MessageCode.FAILED_SUBTRACT_BALANCE);
                        throw new Exception(MessageCode.FAILED_SUBTRACT_BALANCE.getEnCode());
                    }

                    //接收方
                    result = memberWalletService.increaseBalance(toMemberWallet.getId(),amount);
                    if (result.getCode() != 0){
//                        return MessageRespResult.error(MessageCode.FAILED_ADD_BALANCE);
                        throw new Exception(MessageCode.FAILED_ADD_BALANCE.getEnCode());
                    }


                    //存入快速支付记录表
                    payFastRecord.setTradeSn(String.valueOf(idWorkByTwitter.nextId()));
                    payFastRecord.setReceiptId(toMemberWallet.getMemberId());
                    payFastRecord.setReceiptWalletId(toMemberWallet.getId());
                    payFastRecord.setReceiptRoleId(receiveRole.getId());
                    payFastRecord.setReceiptRole(receiveFee.getRoleName());
                    payFastRecord.setReceiptAddress(toMemberWallet.getAddress());
                    payFastRecord.setPayId(fromMemberWallet.getMemberId());
                    payFastRecord.setPayWalletId(fromMemberWallet.getId());
                    payFastRecord.setPayRoleId(forwardRole.getId());
                    payFastRecord.setPayRole(forwardRole==null ? "" : forwardRole.getRoleName());
                    payFastRecord.setPayAddress(fromMemberWallet.getAddress());
                    payFastRecord.setAmount(amount);
                    payFastRecord.setArrivedAmount(amount);
                    payFastRecord.setUnit(fromMemberWallet.getCoin().getUnit());
                    payFastRecord.setFeeUnit(receiveFee.getFeeUnit());
                    payFastRecord.setFeeType(CoinFeeType.SCALE);
                    payFastRecord.setFee(afterFee);
                    payFastRecord.setTradeType(transferType);
                    payFastRecord.setCreateTime(new Date());
                    payFastRecord.setStatus(PayTransferStatus.SUCCESS);
                    payFastRecord.setReceiptPhone(toMember.getMobilePhone());
                    payFastRecord.setPayPhone(fromMember.getMobilePhone());
                    payFastRecord.setPlatform(platform);
                    payFastRecord.setPlatformTo(platformTo);
//                    payFastRecord.setComment();
                    iPayFastRecordService.save(payFastRecord);

                    PayFastRecord payRecord = iPayFastRecordService.findOneByTradeSn(payFastRecord.getTradeSn());
                    //交易记录
                    //支付钱包，收款钱包，手续费钱包，支付数额，收款数额，手续费，快速支付记录id
                    getService().saveTransaction(fromMemberWallet,toMemberWallet,feeWallet,receiveFee.getFeeUnit(),amount,amount,afterFee,payRecord.getId());
                }
            }
        }else {
            //支付币种配置表
            Map<String,Object> coinMap = new HashMap<>();
            coinMap.put("app_id", platform);
            coinMap.put("unit",fromMemberWallet.getCoin().getUnit());
            coinMap.put("status", BooleanEnum.IS_TRUE);
            coinMap.put("is_rapid_transfer",BooleanEnum.IS_TRUE);
            List<PaySupportCoinConfig> coinConfigs= iPaySupportCoinConfigService.selectByMap(coinMap);
            if (coinConfigs.size()!=0){
                PaySupportCoinConfig paySupportCoinConfig = coinConfigs.get(0);
                BigDecimal fee = paySupportCoinConfig.getAssetTransferRapidFee();
                receiveAmount = amount.subtract(fee);
                //到账金额是否大于0
                if (receiveAmount.compareTo(BigDecimal.ZERO) < 1){
                    return error(MessageCode.ACCOUNT_AMOUNT_INSUFFICIENT);
                }

                result = memberWalletService.subtractBalance(fromMemberWallet, amount);
                if (result.getCode() != 0){
//                    return MessageRespResult.error(MessageCode.FAILED_SUBTRACT_BALANCE);
                    throw new Exception(MessageCode.FAILED_SUBTRACT_BALANCE.getEnCode());
                }

                //修改钱包，收款方+（转款数额-手续费），支付方-转账数额
                result = memberWalletService.increaseBalance(toMemberWallet.getId(), receiveAmount);
                if (result.getCode() != 0){
//                    return MessageRespResult.error(MessageCode.FAILED_ADD_BALANCE);
                    throw new Exception(MessageCode.FAILED_ADD_BALANCE.getEnCode());
                }
                //存入快速支付记录表
                payFastRecord.setTradeSn(String.valueOf(idWorkByTwitter.nextId()));
                payFastRecord.setReceiptId(toMemberWallet.getMemberId());
                payFastRecord.setReceiptWalletId(toMemberWallet.getId());
                payFastRecord.setReceiptRoleId(receiveRole.getId());
                payFastRecord.setReceiptRole(receiveRole.getRoleName());
                payFastRecord.setReceiptAddress(toMemberWallet.getAddress());
                payFastRecord.setPayId(fromMemberWallet.getMemberId());
                payFastRecord.setPayWalletId(fromMemberWallet.getId());
                payFastRecord.setPayRoleId(forwardRole.getId());
                payFastRecord.setPayRole(forwardRole==null ? "" : forwardRole.getRoleName());
                payFastRecord.setPayAddress(fromMemberWallet.getAddress());
                payFastRecord.setAmount(amount);
                payFastRecord.setArrivedAmount(receiveAmount);
                payFastRecord.setUnit(fromMemberWallet.getCoin().getUnit());
                payFastRecord.setFeeUnit(paySupportCoinConfig.getUnit());
                payFastRecord.setFeeType(CoinFeeType.FIXED);
                payFastRecord.setFee(fee);
                payFastRecord.setTradeType(transferType);
                payFastRecord.setCreateTime(new Date());
                payFastRecord.setStatus(PayTransferStatus.SUCCESS);
                payFastRecord.setReceiptPhone(toMember.getMobilePhone());
                payFastRecord.setPayPhone(fromMember.getMobilePhone());
                payFastRecord.setPlatform(platform);
                payFastRecord.setPlatformTo(platformTo);
//                    payFastRecord.setComment();
                iPayFastRecordService.save(payFastRecord);

                PayFastRecord payRecord = iPayFastRecordService.findOneByTradeSn(payFastRecord.getTradeSn());
                log.info("========payFastRecordId-{}============",payRecord.getId());
                //交易记录
                //支付钱包，收款钱包，手续费钱包，支付数额，收款数额，手续费，快速支付记录id
                getService().saveTransaction(fromMemberWallet,toMemberWallet,null,null,amount,receiveAmount,fee,payRecord.getId());

            }else {
                //没有手续费配置，手续费为0
                result = memberWalletService.subtractBalance(fromMemberWallet, amount);
                if (result.getCode() != 0){
//                    return MessageRespResult.error(MessageCode.FAILED_SUBTRACT_BALANCE);
                    throw new Exception(MessageCode.FAILED_SUBTRACT_BALANCE.getEnCode());
                }

                //修改钱包，收款方+（转款数额-手续费），支付方-转账数额
                result = memberWalletService.increaseBalance(toMemberWallet.getId(), amount);
                if (result.getCode() != 0){
//                    return MessageRespResult.error(MessageCode.FAILED_ADD_BALANCE);
                    throw new Exception(MessageCode.FAILED_ADD_BALANCE.getEnCode());
                }

                //存入快速支付记录表
                payFastRecord.setTradeSn(String.valueOf(idWorkByTwitter.nextId()));
                payFastRecord.setReceiptId(toMemberWallet.getMemberId());
                payFastRecord.setReceiptWalletId(toMemberWallet.getId());
                payFastRecord.setReceiptRoleId(receiveRole.getId());
                payFastRecord.setReceiptRole(receiveRole.getRoleName());
                payFastRecord.setReceiptAddress(toMemberWallet.getAddress());
                payFastRecord.setPayId(fromMemberWallet.getMemberId());
                payFastRecord.setPayWalletId(fromMemberWallet.getId());
                payFastRecord.setPayRoleId(forwardRole.getId());
                payFastRecord.setPayRole(forwardRole==null ? "" : forwardRole.getRoleName());
                payFastRecord.setPayAddress(fromMemberWallet.getAddress());
                payFastRecord.setAmount(amount);
                payFastRecord.setArrivedAmount(amount);
                payFastRecord.setUnit(fromMemberWallet.getCoin().getUnit());
                payFastRecord.setTradeType(transferType);
                payFastRecord.setFeeUnit(fromMemberWallet.getCoin().getUnit());
                payFastRecord.setFeeType(CoinFeeType.FIXED);
                payFastRecord.setFee(BigDecimal.ZERO);
                payFastRecord.setCreateTime(new Date());
                payFastRecord.setStatus(PayTransferStatus.SUCCESS);
                payFastRecord.setReceiptPhone(toMember.getMobilePhone());
                payFastRecord.setPayPhone(fromMember.getMobilePhone());
                payFastRecord.setPlatform(platform);
                payFastRecord.setPlatformTo(platformTo);
//                    payFastRecord.setComment();
                iPayFastRecordService.save(payFastRecord);

                PayFastRecord payRecord = iPayFastRecordService.findOneByTradeSn(payFastRecord.getTradeSn());
                log.info("========payFastRecordId-{}============",payRecord.getId());
                //交易记录
                //支付钱包，收款钱包，手续费钱包，支付数额，收款数额，手续费，快速支付记录id
                getService().saveTransaction(fromMemberWallet,toMemberWallet,null,null,amount,amount,BigDecimal.ZERO,payRecord.getId());

            }
        }
        return MessageRespResult.success("转账成功", payFastRecord);
    }

    /**
     * 币种转换
     * @author Zhang Yanjun
     * @time 2019.01.16 10:49
     * @param receiveFee 接收账户手续费配置
     * @param fee 转换前手续费
     * @param restTemplate
     */
    private MessageResult convertFee (PayRoleFeeRateConfig receiveFee,BigDecimal fee, RestTemplate restTemplate){
        PriceUtil priceUtil = new PriceUtil();
        //交易币种价格
        BigDecimal tradeCoinPrice = priceUtil.getPriceByCoin(restTemplate, receiveFee.getTradeUnit());
        log.info("支付币种价格-{}======",tradeCoinPrice);
//        BigDecimal tradeCoinPrice = new BigDecimal(0.2);
        //手续费币种价格
        BigDecimal feeCoinPrice = priceUtil.getPriceByCoin(restTemplate, receiveFee.getFeeUnit());
        log.info("手续费币种价格-{}======",feeCoinPrice);
//        BigDecimal feeCoinPrice = new BigDecimal(0.1);
        //如果价格为0，则说明价格异常
        if(tradeCoinPrice.compareTo(BigDecimal.ZERO) == 0 || feeCoinPrice.compareTo(BigDecimal.ZERO) == 0){
            return new MessageResult(500,SpringContextUtil.getBean(LocaleMessageSourceService.class).getMessage("PRICE_ERROR"));
        }
        //换算后的数额
        Coin coin = SpringContextUtil.getBean(ICoinService.class).findByUnit(receiveFee.getFeeUnit());
        BigDecimal afterFee = PriceUtil.toRate(fee,coin.getWithdrawScale(),tradeCoinPrice,feeCoinPrice);
        log.info("换算前(支付币种)的手续费-{}，========换算成手续费币种的手续费-{}======",fee,afterFee);
        return MessageResult.success("查询成功", afterFee);
    }

    @WriteDataSource
    @MybatisTransactional(rollbackFor = Exception.class)
    // @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void saveTransaction(MemberWallet fromMember,MemberWallet toMember,MemberWallet feeWallet,String feeWalletunit,BigDecimal amount,BigDecimal receiveAmount,
                                 BigDecimal fee,Long payRecordId){
        MemberTransaction fromMemberTransaction = new MemberTransaction();
        MemberTransaction toMemberTransaction = new MemberTransaction();
        MemberTransaction toMemberTransactionFee = new MemberTransaction();

        fromMemberTransaction.setMemberId(fromMember.getMemberId());
        fromMemberTransaction.setAmount(amount.negate());
        fromMemberTransaction.setCreateTime(new Date());
        fromMemberTransaction.setType(TransactionType.THIRD_PAY);
        fromMemberTransaction.setSymbol(fromMember.getCoin().getUnit());
        fromMemberTransaction.setAddress(fromMember.getAddress());
        fromMemberTransaction.setFee(BigDecimal.ZERO);
        fromMemberTransaction.setRefId(payRecordId.toString());
        fromMemberTransaction.setComment("转账-[支付方]");

        toMemberTransaction.setMemberId(toMember.getMemberId());
        toMemberTransaction.setAmount(receiveAmount);
        toMemberTransaction.setCreateTime(new Date());
        toMemberTransaction.setType(TransactionType.THIRD_PAY);
        toMemberTransaction.setSymbol(toMember.getCoin().getUnit());
        toMemberTransaction.setAddress(toMember.getAddress());
        toMemberTransaction.setRefId(payRecordId.toString());
        toMemberTransaction.setComment("转账-[收款方]");

        if (feeWallet == null) {
            toMemberTransaction.setFee(fee);
        }else {
            if (fee.compareTo(BigDecimal.ZERO)>0) {
                toMemberTransactionFee.setMemberId(feeWallet.getMemberId());
                toMemberTransactionFee.setAmount(fee.negate());
                toMemberTransactionFee.setCreateTime(new Date());
                toMemberTransactionFee.setType(TransactionType.THIRD_PAY);
                toMemberTransactionFee.setSymbol(feeWalletunit);
                toMemberTransactionFee.setAddress(feeWallet.getAddress());
                toMemberTransactionFee.setRefId(payRecordId.toString());
                toMemberTransactionFee.setComment("转账手续费-[收款方]");

                SpringContextUtil.getBean(MemberTransactionCoreMapper.class).insert(toMemberTransactionFee);
            }
        }
        try {
            SpringContextUtil.getBean(MemberTransactionCoreMapper.class).insert(fromMemberTransaction);
            SpringContextUtil.getBean(MemberTransactionCoreMapper.class).insert(toMemberTransaction);
        }catch (Exception e){
            e.printStackTrace();
            log.info(e.getMessage());
            throw e;
        }

    }

    public PayWalletNewService getService(){
        return SpringContextUtil.getBean(PayWalletNewService.class);
    }


    /**
     * 积分兑换
     *
     * @param fromMember   转出账户(支付方)
     * @param toMember     接收账户
     * @param amount       转账数量
     * @param transferType 交易类型
     * @param platform     转账方应用ID
     * @param platformTo   收款方应用ID
     */
    @WriteDataSource
    @MybatisTransactional(rollbackFor = Exception.class)
    // @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public MessageRespResult<PayFastRecord> platformExchange(MemberWallet fromMemberWallet, MemberWallet toMemberWallet,
                                              BigDecimal amount, Member fromMember, Member toMember,
                                              RestTemplate restTemplate, PayTransferType transferType,
                                              String platform, String platformTo) throws Exception {
        //支付方角色
        PayRoleConfig forwardRole = getPayRoleConfig(fromMemberWallet.getMemberId(), platform);
        //收款方角色
        PayRoleConfig receiveRole = getPayRoleConfig(toMemberWallet.getMemberId(), platformTo);

        // 积分兑换查询支付方式手续费
        BigDecimal fee = BigDecimal.ZERO;
        String coin = fromMemberWallet.getCoin().getUnit();
        CoinFeeType feeType = CoinFeeType.FIXED;
        PayRoleFeeRateConfig payRoleFeeRateConfig = iPayRoleFeeRateConfigService.findByIdAndTradeUnit(forwardRole.getId(), fromMemberWallet.getCoin().getUnit());
        // 角色手续费配置不存在时，取币种配置手续费
        if (payRoleFeeRateConfig == null) {
            Map<String, Object> coinMap = new HashMap<>();
            coinMap.put("app_id", platform);
            coinMap.put("status", BooleanEnum.IS_TRUE);
            coinMap.put("is_rapid_transfer", BooleanEnum.IS_TRUE);
            coinMap.put("unit", fromMemberWallet.getCoin().getUnit());
            List<PaySupportCoinConfig> coinConfigs = iPaySupportCoinConfigService.selectByMap(coinMap);
            if (coinConfigs != null && coinConfigs.size() == 1) {
                PaySupportCoinConfig config = coinConfigs.get(0);
                // 使用币种配置的默认手续费（固定值）
                fee = config.getAssetTransferRapidFee();
            }
        } else {
            coin = payRoleFeeRateConfig.getFeeUnit();
            feeType = payRoleFeeRateConfig.getFeeType();
            if (payRoleFeeRateConfig.getFeeType() == CoinFeeType.SCALE) {
                fee = amount.multiply(payRoleFeeRateConfig.getIncomeFee());
            } else {
                fee = payRoleFeeRateConfig.getIncomeFee();
            }
            // 交易币种与手续费币种不相同，转换为手续费币种
            if (!payRoleFeeRateConfig.getTradeUnit().equals(payRoleFeeRateConfig.getFeeUnit())) {
                MessageResult result = getService().convertFee(payRoleFeeRateConfig, fee, restTemplate);
                if (result.isSuccess()) {
                    fee = (BigDecimal) result.getData();
                }
            }
        }

        // 减少付款方余额
        MessageResult result = memberWalletService.subtractBalance(fromMemberWallet, amount);
        if (result.getCode() != 0) {
            throw new Exception(MessageCode.FAILED_SUBTRACT_BALANCE.getEnCode());
        }
        // 减少手续费余额
        MemberWallet forward = memberWalletService.findByCoinUnitAndMemberId(coin, fromMember.getId());
        result = forward == null ? MessageResult.error(MessageCode.MISSING_ACCOUNT) : memberWalletService.subtractBalance(forward, fee);
        if (result.getCode() != 0) {
            throw new Exception(MessageCode.FAILED_SUBTRACT_BALANCE.getEnCode());
        }
        // 增加收款方余额
        result = memberWalletService.increaseBalance(toMemberWallet.getId(), amount);
        if (result.getCode() != 0) {
            throw new Exception(MessageCode.FAILED_ADD_BALANCE.getEnCode());
        }

        // 记录快速转账
        log.info("存入快速记录表开始==========");
        PayFastRecord payFastRecord = new PayFastRecord();
        payFastRecord.setId(idWorkByTwitter.nextId());
        payFastRecord.setTradeSn(String.valueOf(idWorkByTwitter.nextId()));//交易编号
        payFastRecord.setReceiptId(toMemberWallet.getMemberId());//收款用户ID
        payFastRecord.setReceiptWalletId(toMemberWallet.getId());//收款账户id（memberWallet表）
        payFastRecord.setReceiptPhone(toMember.getMobilePhone());//收款方手机号
        payFastRecord.setReceiptRole(receiveRole.getRoleName());//收款用户角色(冗余)
        payFastRecord.setReceiptRoleId(receiveRole.getId());//收款用户角色id
        payFastRecord.setReceiptAddress(toMemberWallet.getAddress());//收款地址
        payFastRecord.setPayWalletId(fromMemberWallet.getId());//支付账户id（memberWallet表）
        payFastRecord.setPayId(fromMemberWallet.getMemberId());//支付用户ID
        payFastRecord.setPayPhone(fromMember.getMobilePhone());//支付方手机号
        payFastRecord.setPayRoleId(forwardRole.getId());//支付用户角色id
        payFastRecord.setPayRole(forwardRole.getRoleName());//支付用户角色(冗余)
        payFastRecord.setPayAddress(toMemberWallet.getAddress());//支付地址
//        payFastRecord.setPayMoney();//支付金额
        payFastRecord.setArrivedAmount(amount); //实际到账数量
        payFastRecord.setAmount(amount);//支付币数量
        payFastRecord.setFeeUnit(coin);//手续费币种
        payFastRecord.setUnit(fromMemberWallet.getCoin().getUnit());//币种
        payFastRecord.setFeeType(feeType);//手续费方式(固定，比例)
        payFastRecord.setFee(fee);//手续费
//        payFastRecord.setDiscountsFee();//优惠手续费
        payFastRecord.setTradeType(transferType);//交易类型
        payFastRecord.setPlatform(platform);//转账方应用ID
        payFastRecord.setPlatformTo(platformTo);//收款方应用ID
        payFastRecord.setCreateTime(new Date());//转账时间
//        payFastRecord.setUpdateTime();//更新时间
        payFastRecord.setStatus(PayTransferStatus.SUCCESS);//状态（发起、成功、失败）
//        payFastRecord.setComment();//备注
        ///iPayFastRecordService.save(payFastRecord);
        getService().savePayFastRecord(payFastRecord);

        log.info("存入快速记录表结束，trade_sn-{}=====", payFastRecord.getTradeSn());

        // 记录资金记录
        getService().saveTransaction(fromMemberWallet, toMemberWallet, forward,coin, amount, amount, fee, payFastRecord.getId());

        // 返回结果
        return MessageRespResult.success("转账成功", payFastRecord);
    }

    @WriteDataSource
    @MybatisTransactional(rollbackFor = Exception.class)
    // @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void savePayFastRecord(PayFastRecord payFastRecord){
        iPayFastRecordService.save(payFastRecord);
    }
}
