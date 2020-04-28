package com.spark.bitrade.service;

import com.spark.bitrade.annotation.MybatisTransactional;
import com.spark.bitrade.annotation.WriteDataSource;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.dto.PayFastRecordDTO;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.enums.MessageCode;
import com.spark.bitrade.mapper.dao.MemberTransactionCoreMapper;
import com.spark.bitrade.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

import static com.spark.bitrade.util.MessageRespResult.error;

/**
 * 优化三方转账
 *
 * @author shenzucai
 * @time 2019.07.30 10:00
 */
@Service
@Slf4j
public class OptimizationPayWalletNewService {
    @Autowired
    private IPayWalletPlatMemberBindService iPayWalletPlatMemberBindService;
    @Autowired
    private IPayRoleFeeRateConfigService iPayRoleFeeRateConfigService;
    @Autowired
    private PayRoleConfigService payRoleConfigService;
    @Autowired
    private IMemberWalletService memberWalletService;
    @Autowired
    private IdWorkByTwitter idWorkByTwitter;
    @Autowired
    private IPayFastRecordService iPayFastRecordService;
    @Autowired
    private IPaySupportCoinConfigService iPaySupportCoinConfigService;

    /**
     * 角色配置
     *
     * @param memberId
     * @param appId
     * @return
     */
    public PayRoleConfig getPayRoleConfig(Long memberId, String appId) {

        PayWalletPlatMemberBind platMemberBind = iPayWalletPlatMemberBindService.findByMemberIdAndAppId(memberId, appId);
        if (platMemberBind == null) {
            return payRoleConfigService.findDefaultRole();
        } else {

        }
        return payRoleConfigService.findOneById(platMemberBind.getRoleId());
    }

    /**
     * 平台内互转
     *
     * @param fromMember   转出账户(支付方)
     * @param toMember     接收账户
     * @param amount       转账数量
     * @param transferType 交易类型
     * @param platform     转账方应用ID
     * @param platformTo   收款方应用ID
     * @author Zhang Yanjun
     * @time 2019.01.14 17:10
     */
    @MybatisTransactional(rollbackFor = Exception.class)
    @WriteDataSource
    public MessageRespResult<PayFastRecordDTO> platformTransfer(MemberWallet fromMemberWallet, MemberWallet toMemberWallet,
                                                                BigDecimal amount, Member fromMember, Member toMember,
                                                                RestTemplate restTemplate, PayTransferType transferType, String platform, String platformTo, String tag) throws Exception {
        MessageResult result;
        //收款数额
        BigDecimal receiveAmount = BigDecimal.ZERO;
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

        PayRoleFeeRateConfig receiveFee = iPayRoleFeeRateConfigService.findByIdAndTradeUnit(receiveRole.getId(), toMemberWallet.getCoin().getUnit());

        //初始化快速支付记录表
        payFastRecord.setTradeSn(String.valueOf(idWorkByTwitter.nextId()));
        payFastRecord.setReceiptId(toMemberWallet.getMemberId());
        payFastRecord.setReceiptWalletId(toMemberWallet.getId());
        payFastRecord.setReceiptRoleId(receiveRole.getId());
        payFastRecord.setReceiptRole(Objects.isNull(receiveFee)?null:receiveFee.getRoleName());
        payFastRecord.setReceiptAddress(toMemberWallet.getAddress());
        payFastRecord.setPayId(fromMemberWallet.getMemberId());
        payFastRecord.setPayWalletId(fromMemberWallet.getId());
        payFastRecord.setPayRoleId(forwardRole.getId());
        payFastRecord.setPayRole(forwardRole == null ? "" : forwardRole.getRoleName());
        payFastRecord.setPayAddress(fromMemberWallet.getAddress());
        payFastRecord.setAmount(amount);
        payFastRecord.setPayMoney(amount);
        payFastRecord.setUnit(fromMemberWallet.getCoin().getUnit());
        payFastRecord.setFeeUnit(fromMemberWallet.getCoin().getUnit());
        payFastRecord.setTradeType(transferType);
        payFastRecord.setCreateTime(new Date());
        payFastRecord.setStatus(PayTransferStatus.SUCCESS);
        payFastRecord.setReceiptPhone(toMember.getMobilePhone());
        payFastRecord.setPayPhone(fromMember.getMobilePhone());
        payFastRecord.setPlatform(platform);
        payFastRecord.setPlatformTo(platformTo);
        payFastRecord.setComment(tag);

        //没有手续费配置，手续费为0
        result = memberWalletService.subtractBalance(fromMemberWallet, amount);
        if (result.getCode() != 0) {
//                    return MessageRespResult.error(MessageCode.FAILED_SUBTRACT_BALANCE);
            throw new Exception("钱包可用余额不足");
        }

        //修改钱包，收款方+（转款数额-手续费），支付方-转账数额
        result = memberWalletService.increaseBalance(toMemberWallet.getId(), amount);
        if (result.getCode() != 0) {
//                    return MessageRespResult.error(MessageCode.FAILED_ADD_BALANCE);
            throw new Exception("钱包不存在");
        }

        payFastRecord.setArrivedAmount(amount);
        payFastRecord.setFeeType(CoinFeeType.FIXED);
        payFastRecord.setFee(BigDecimal.ZERO);
        iPayFastRecordService.save(payFastRecord);
        //交易记录
        //支付钱包，收款钱包，手续费钱包，支付数额，收款数额，手续费，快速支付记录id
        getService().saveTransaction(fromMemberWallet, toMemberWallet, null, null, amount, amount, BigDecimal.ZERO, payFastRecord.getId());

        return MessageRespResult.success("转账成功", payFastRecord);
    }

    /**
     * 币种转换
     *
     * @param receiveFee   接收账户手续费配置
     * @param fee          转换前手续费
     * @param restTemplate
     * @author Zhang Yanjun
     * @time 2019.01.16 10:49
     */
    private MessageResult convertFee(PayRoleFeeRateConfig receiveFee, BigDecimal fee, RestTemplate restTemplate) {
        PriceUtil priceUtil = new PriceUtil();
        //交易币种价格
        BigDecimal tradeCoinPrice = priceUtil.getPriceByCoin(restTemplate, receiveFee.getTradeUnit());
        log.info("支付币种价格-{}======", tradeCoinPrice);
//        BigDecimal tradeCoinPrice = new BigDecimal(0.2);
        //手续费币种价格
        BigDecimal feeCoinPrice = priceUtil.getPriceByCoin(restTemplate, receiveFee.getFeeUnit());
        log.info("手续费币种价格-{}======", feeCoinPrice);
//        BigDecimal feeCoinPrice = new BigDecimal(0.1);
        //如果价格为0，则说明价格异常
        if (tradeCoinPrice.compareTo(BigDecimal.ZERO) == 0 || feeCoinPrice.compareTo(BigDecimal.ZERO) == 0) {
            return new MessageResult(500, SpringContextUtil.getBean(LocaleMessageSourceService.class).getMessage("PRICE_ERROR"));
        }
        //换算后的数额
        Coin coin = SpringContextUtil.getBean(ICoinService.class).findByUnit(receiveFee.getFeeUnit());
        BigDecimal afterFee = PriceUtil.toRate(fee, coin.getWithdrawScale(), tradeCoinPrice, feeCoinPrice);
        log.info("换算前(支付币种)的手续费-{}，========换算成手续费币种的手续费-{}======", fee, afterFee);
        return MessageResult.success("查询成功", afterFee);
    }

    @WriteDataSource
    @MybatisTransactional(rollbackFor = Exception.class)
    public void saveTransaction(MemberWallet fromMember, MemberWallet toMember, MemberWallet feeWallet, String feeWalletunit, BigDecimal amount, BigDecimal receiveAmount,
                                BigDecimal fee, Long payRecordId) {
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
        } else {
            if (fee.compareTo(BigDecimal.ZERO) > 0) {
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
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage());
            throw e;
        }

    }

    public OptimizationPayWalletNewService getService() {
        return SpringContextUtil.getBean(OptimizationPayWalletNewService.class);
    }
}
