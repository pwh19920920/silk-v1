package com.spark.bitrade.service.impl;

import com.spark.bitrade.constant.PayTransferType;
import com.spark.bitrade.emuns.H5GameDirection;
import com.spark.bitrade.emuns.H5GameRecordState;
import com.spark.bitrade.emuns.MessageCode;
import com.spark.bitrade.entity.H5GamePayRecord;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.MemberWallet;
import com.spark.bitrade.entity.PayFastRecord;
import com.spark.bitrade.h5game.H5Config;
import com.spark.bitrade.mapper.dao.H5GameRecordMapper;
import com.spark.bitrade.service.IH5GameService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.service.MemberWalletService;
import com.spark.bitrade.service.PayWalletService;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * H5GameService
 *
 * @author archx
 * @time 2019/4/25 11:42
 */
@Service
@Slf4j
public class H5GameService implements IH5GameService {

    private PayWalletService payWalletService;
    private MemberWalletService memberWalletService;
    private MemberService memberService;
    private H5GameRecordMapper recordMapper;

    private RestTemplate restTemplate;

    private final Long payAppId;
    private final Long payRoleId;

    public H5GameService(H5Config config) {
        payAppId = config.getPayAppId();
        payRoleId = config.getPayRoleId();
    }

    @Override
    @Transactional(transactionManager = "transactionManagerPrimary", rollbackFor = RuntimeException.class)
    public Resp topUp(Member member, String mobile, BigDecimal amount) {

        Member primary = null;
        try {
            primary = getPrimaryMember();
        } catch (RuntimeException e) {
            return Resp.Fail(MessageCode.MISSING_ACCOUNT.getCode(), e.getMessage());
        }

        MemberWallet mw = getBTMemberWallet(member.getId());

        MemberWallet receive = getBTMemberWallet(primary.getId());
        // 尝试转入
        try {
            MessageRespResult<PayFastRecord> result = payWalletService.platformTransfer(mw, receive,
                    amount, member, primary,
                    restTemplate, PayTransferType.PAY_FAST, payAppId + "", payAppId + "");

            if (!result.isSuccess()) {
                return Resp.Fail(result.getCode(), result.getMessage());
            }

            Date date = Calendar.getInstance().getTime();
            Long fpId = result.getData().getId();

            // 写入记录
            H5GamePayRecord top = H5GamePayRecord.builder().memberId(member.getId()).amount(amount)
                    .direction(H5GameDirection.TOP_UP.getCode()).state(H5GameRecordState.STARTED.getCode()).fpId(fpId)
                    .mobile(mobile).remark("充值记录")
                    .createTime(date).build();

            Integer insert = recordMapper.insert(top);

            if (insert == null || insert != 1 || top.getId() == null) {
                throw new RuntimeException("写入充值记录出错");
            }

            return Resp.Ok(top);
        } catch (Exception ex) {
            log.error("调用快速转账接口出错", ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    @Transactional(transactionManager = "transactionManagerPrimary", propagation= Propagation.REQUIRES_NEW, rollbackFor = RuntimeException.class)
    public Resp withdraw(Member member, String mobile, BigDecimal amount) {

        Member primary = null;
        try {
            primary = getPrimaryMember();
        } catch (RuntimeException e) {
            return Resp.Fail(MessageCode.MISSING_ACCOUNT.getCode(), e.getMessage());
        }

        // 写入记录
        H5GamePayRecord record = H5GamePayRecord.builder().memberId(member.getId()).amount(amount)
                .direction(H5GameDirection.WITHDRAW.getCode()).state(H5GameRecordState.STARTED.getCode())
                .mobile(mobile).remark("游戏余额提现")
                .createTime(Calendar.getInstance().getTime()).build();

        Integer insert = recordMapper.insert(record);

        if (insert == null || insert != 1 || record.getId() == null) {
            throw new RuntimeException("写入提现记录出错");
        }


        // 尝试转账
        record.setRefId(0L);
        record.setUpdateTime(Calendar.getInstance().getTime());

        MemberWallet receive = getBTMemberWallet(member.getId());
        MemberWallet from = getBTMemberWallet(primary.getId());

        try {

//            if (amount.compareTo(BigDecimal.ZERO)>0) {
//                throw new RuntimeException("test error");
//            }

            MessageRespResult<PayFastRecord> result = SpringContextUtil.getBean(this.getClass())
                    .getPayFastRecordMessageRespResult(member, amount, primary, receive, from);

            if (!result.isSuccess()) {

                // 更新记录
                record.setState(H5GameRecordState.FAILED.getCode());
                record.setErrCode(result.getCode());
                record.setErrMsg(result.getMessage());
                recordMapper.updateAllColumnById(record);

                return Resp.Fail(result.getCode(), result.getMessage());
            } else {

                Long fpId = result.getData().getId();
                // 更新记录
                record.setFpId(fpId);
                record.setState(H5GameRecordState.SUCCESSFUL.getCode());
                recordMapper.updateAllColumnById(record);

                return Resp.Ok(record);
            }

        } catch (Exception ex) {
            log.error("调用快速转账接口出错", ex);
//            log.error("提现失败记录: phone = {}, code = {}, amount = {}", member.getMobilePhone(), mobile, amount);
//            throw new RuntimeException(ex);
            // 更新记录
            record.setState(H5GameRecordState.FAILED.getCode());
            record.setErrCode(MessageCode.ERROR.getCode());
            record.setErrMsg("调用快速转账接口出错:" + ex.getMessage());
            recordMapper.updateAllColumnById(record);
            return Resp.Fail(MessageCode.ERROR.getCode(), "调用快速转账接口出错:" + ex.getMessage());
        }

    }

    @Transactional(transactionManager = "transactionManagerPrimary", propagation= Propagation.REQUIRES_NEW, rollbackFor = RuntimeException.class)
    public MessageRespResult<PayFastRecord> getPayFastRecordMessageRespResult(Member member, BigDecimal amount,
                                                                               Member primary,
                                                                               MemberWallet receive, MemberWallet from) throws Exception {
        return payWalletService.platformTransfer(from, receive,
                        amount, primary, member,
                        restTemplate, PayTransferType.PAY_FAST, payAppId + "", payAppId + "");
    }

    @Override
    @Transactional(transactionManager = "transactionManagerPrimary", rollbackFor = RuntimeException.class)
    public Resp refund(Member member, Long refId) {

        Member primary = null;
        try {
            primary = getPrimaryMember();
        } catch (RuntimeException e) {
            return Resp.Fail(MessageCode.MISSING_ACCOUNT.getCode(), e.getMessage());
        }

        H5GamePayRecord record = recordMapper.selectById(refId);

        // 订单不存在
        if (record == null || record.getMemberId() != member.getId().longValue()) {
            return Resp.Fail(MessageCode.RECORD_NOT_EXIST.getCode(), MessageCode.RECORD_NOT_EXIST.getDesc());
        }

        // 不是转入订单 或 订单状态不符合
        if (!H5GameDirection.TOP_UP.eq(record.getDirection()) || !H5GameRecordState.FAILED.eq(record.getState())
                || record.getRefId() != 0) {
            return Resp.Fail(MessageCode.INCORRECT_STATE.getCode(), MessageCode.INCORRECT_STATE.getDesc());
        }

        // 开始退款

        MemberWallet receive = getBTMemberWallet(member.getId());
        MemberWallet from = getBTMemberWallet(primary.getId());

        try {
            MessageRespResult<PayFastRecord> result = payWalletService.platformTransfer(from, receive,
                    record.getAmount(), primary, member,
                    restTemplate, PayTransferType.PAY_FAST, "", "");

            if (!result.isSuccess()) {
                return Resp.Fail(result.getCode(), result.getMessage());
            }

            Long fpId = result.getData().getId();
            Date date = Calendar.getInstance().getTime();

            // 写入记录
            H5GamePayRecord refund = H5GamePayRecord.builder().memberId(member.getId()).amount(record.getAmount()).refId(record.getId())
                    .direction(H5GameDirection.REFUND.getCode()).state(H5GameRecordState.SUCCESSFUL.getCode()).fpId(fpId)
                    .mobile(member.getPromotionCode()).remark("转账退款 [ refId = " + record.getId() + "]")
                    .createTime(date).build();

            Integer insert = recordMapper.insert(refund);

            if (insert == null || insert != 1 || refund.getId() == null) {
                throw new RuntimeException("写入退款记录出错");
            }

            // 更新原记录
            int update = recordMapper.updateRefIdById(record.getId(), refund.getId(), date);

            if (update < 1) {
                throw new RuntimeException("更新转入记录出错");
            }

            return Resp.Ok(refund);
        } catch (Exception ex) {
            log.error("调用快速转账接口出错", ex);
            throw new RuntimeException(ex);
        }

    }

    @Override
    public void update(H5GamePayRecord record) {
        record.setUpdateTime(Calendar.getInstance().getTime());
        recordMapper.updateAllColumnById(record);
    }

    private MemberWallet getBTMemberWallet(Long memberId) {
        return memberWalletService.findCacheByCoinUnitAndMemberId("BT", memberId);
    }

    private Member getPrimaryMember() {
        Long memberId = recordMapper.findPrimaryMemberIdByRolIdAndAppId(payRoleId, payAppId);
        Member primary = null;
        if (memberId == null || (primary = memberService.findOne(memberId)) == null) {
            throw new RuntimeException(String.format("未找到设置主账号 [ roleId = %d,  appId = %d ]", payRoleId, payAppId));
        }
        return primary;
    }

    @Autowired
    public void setPayWalletService(PayWalletService payWalletService) {
        this.payWalletService = payWalletService;
    }

    @Autowired
    public void setMemberWalletService(MemberWalletService memberWalletService) {
        this.memberWalletService = memberWalletService;
    }

    @Autowired
    public void setRecordMapper(H5GameRecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    public void setMemberService(MemberService memberService) {
        this.memberService = memberService;
    }
}
