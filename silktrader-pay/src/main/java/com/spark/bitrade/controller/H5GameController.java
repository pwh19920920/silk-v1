package com.spark.bitrade.controller;

import com.spark.bitrade.emuns.H5GameRecordState;
import com.spark.bitrade.entity.H5GamePayRecord;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.enums.MessageCode;
import com.spark.bitrade.h5game.H5Resp;
import com.spark.bitrade.h5game.IH5GameApiService;
import com.spark.bitrade.service.IH5GameService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.util.MessageRespResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * H5GameController
 *
 * @author archx
 * @time 2019/4/25 9:18
 */
@Api(description = "H5游戏转账相关接口")
@RestController
@RequestMapping("/h5game")
@Slf4j
public class H5GameController {

    private IH5GameService gameService;
    private IH5GameApiService gameApiService;
    private MemberService memberService;


    @GetMapping
    public MessageRespResult<?> index() {
        return MessageRespResult.success();
    }

    @ApiOperation(value = "参与游戏", notes = "返回H5游戏平台联合登录的参数，指定充值数额则须验证转账")
    @ApiImplicitParam(name = "amount", value = "充值数额", paramType = "query", defaultValue = "0")
    @PostMapping("/join")
    public MessageRespResult<?> join(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember auth,
                                     @RequestParam(value = "amount", required = false) BigDecimal amount) { // 加入游戏

        Member member = memberService.findOne(auth.getId());
        if (member == null) {
            return MessageRespResult.error(MessageCode.MISSING_USER);
        }

        String amt = "0";
        H5GamePayRecord record = null;
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) { // 有转入金额

            amount = new BigDecimal(amount.intValue()); // 只取整数部分
            // 转账
            IH5GameService.Resp resp = gameService.topUp(member, member.getPromotionCode(), amount);
            if (!resp.isSuccess()) {

                return MessageRespResult.error(resp.getCode(), resp.getMessage());
            }
            record = resp.getRecord();
            amt = String.valueOf(amount.intValue());
        }


        // 上级推荐码
        String inviter = "";
        Member supper;
        if (member.getInviterId() != null && (supper = memberService.findOne(member.getInviterId())) != null) {
            inviter = supper.getPromotionCode();
        }

        // 返回提交信息
        H5Resp h5resp = gameApiService.union(member.getPromotionCode(), amt, inviter);

        if (record != null) {

            // 设定默认值
            record.setRefId(0L);
            record.setErrCode(0);

            if (h5resp.isSuccess()) {
                record.setState(H5GameRecordState.SUCCESSFUL.getCode());
                gameService.update(record);
                return MessageRespResult.success4Data(record.getId());
            } else {
                record.setState(H5GameRecordState.FAILED.getCode());
                record.setErrCode(h5resp.getStatus());
                record.setErrMsg(h5resp.getMsg());

                gameService.update(record);

                // 执行退款
                IH5GameService.Resp resp = gameService.refund(member, record.getId());

                log.info("联合失败执行退款 [ refId = {}, err_code = {}, err_msg = {} ]", record.getId(), resp.getCode(), resp.getMessage());

                return MessageRespResult.error(MessageCode.H5_PLATFORM_ERR, h5resp.getMsg());
            }
        }
        if (h5resp.isSuccess()) {
            return MessageRespResult.success();
        }

        return MessageRespResult.error(MessageCode.H5_PLATFORM_ERR, h5resp.getMsg());
    }

    @ApiOperation(value = "游戏充值", notes = "充值BT到H5游戏平台 \n注意：当前游戏平台只能接受整数数额充值，7以下充值结果为0，7倍数免手续费")
    @ApiImplicitParam(name = "amount", value = "充值数额", paramType = "query")
    @PostMapping("/topup")
    public MessageRespResult<?> topUp(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember auth,
                                      @RequestParam("amount") BigDecimal amount) { // 充值

        Member member = memberService.findOne(auth.getId());
        if (member == null) {
            return MessageRespResult.error(MessageCode.MISSING_USER, MessageCode.MISSING_USER.getDesc());
        }

        if (amount.compareTo(BigDecimal.ZERO) < 1) { // 必须是正整数
            return MessageRespResult.error(MessageCode.INVALID_AMOUNT, MessageCode.INVALID_AMOUNT.getDesc());
        }

        // 内部充值
        IH5GameService.Resp resp = gameService.topUp(member, member.getPromotionCode(), new BigDecimal(amount.intValue()));

        if (resp.isSuccess()) {
            // H5充值
            H5Resp h5resp = gameApiService.transferIn(member.getPromotionCode(), String.valueOf(amount.intValue()));

            H5GamePayRecord record = resp.getRecord();
            // 设定默认值
            record.setRefId(0L);
            record.setErrCode(0);

            if (h5resp.isSuccess()) { // 充值成功
                record.setState(H5GameRecordState.SUCCESSFUL.getCode());
                gameService.update(record);
                return MessageRespResult.success4Data(record.getId());
            } else {
                record.setState(H5GameRecordState.FAILED.getCode());
                record.setErrCode(h5resp.getStatus());
                record.setErrMsg(h5resp.getMsg());

                gameService.update(record);

                // 执行退款
                resp = gameService.refund(member, record.getId());

                log.info("转账失败执行退款 [ refId = {}, err_code = {}, err_msg = {} ]", record.getId(), resp.getCode(), resp.getMessage());

                return MessageRespResult.error(MessageCode.H5_PLATFORM_ERR, h5resp.getMsg());
            }
        }

        return MessageRespResult.error(resp.getCode(), resp.getMessage());
    }


    @ApiOperation(value = "游戏提现", notes = "提取游戏余额到BT钱包")
    @ApiImplicitParam(name = "amount", value = "提现数额", paramType = "query")
    @PostMapping("/withdraw")
    public MessageRespResult<?> withdraw(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember auth,
                                         @RequestParam("amount") BigDecimal amount) { // 提现

        Member member = memberService.findOne(auth.getId());
        if (member == null) {
            return MessageRespResult.error(MessageCode.MISSING_USER, MessageCode.MISSING_USER.getDesc());
        }

        amount = new BigDecimal(amount.intValue()); // 只取整数部分
        if (amount.compareTo(BigDecimal.ZERO) < 1) { // 必须是正整数
            return MessageRespResult.error(MessageCode.INVALID_AMOUNT, MessageCode.INVALID_AMOUNT.getDesc());
        }

        // 获取H5平台余额
        H5Resp h5resp = gameApiService.balance(member.getPromotionCode());
        if (!h5resp.isSuccess()) {
            return MessageRespResult.error(MessageCode.H5_PLATFORM_ERR, h5resp.getMsg());
        }

        // 比较余额
        Optional<BigDecimal> usableAmt = h5resp.unwrap("usableAmt", BigDecimal.class);
        if (!usableAmt.isPresent() || usableAmt.get().setScale(0, BigDecimal.ROUND_FLOOR).compareTo(amount) < 0) {
            return MessageRespResult.error(MessageCode.ACCOUNT_BALANCE_INSUFFICIENT, MessageCode.ACCOUNT_BALANCE_INSUFFICIENT.getDesc());
        }

        // 尝试从游戏平台提现
        h5resp = gameApiService.transferOut(member.getPromotionCode(), amount.toPlainString());
        if (!h5resp.isSuccess()) {
            return MessageRespResult.error(MessageCode.H5_PLATFORM_ERR, h5resp.getMsg());
        }


        // 提现
        IH5GameService.Resp resp = gameService.withdraw(member, member.getPromotionCode(), amount);

        if (resp.isSuccess()) {
            return MessageRespResult.success4Data(resp.getRecord().getId());
        } else {
            log.error("提现失败: phone = {}, code = {}, amount = {}", member.getMobilePhone(), member.getPromotionCode(), amount);
        }

        return MessageRespResult.error(resp.getCode(), resp.getMessage());
    }

    @ApiOperation(value = "查询余额", notes = "从H5平台获取用户可用余额")
    @GetMapping("/balance")
    public MessageRespResult<?> balance(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember auth) { // 余额

        Member member = memberService.findOne(auth.getId());
        if (member == null) {
            return MessageRespResult.error(MessageCode.MISSING_USER, MessageCode.MISSING_USER.getDesc());
        }

        H5Resp resp = gameApiService.balance(member.getPromotionCode());

        if (resp.isSuccess()) {
            Optional<BigDecimal> amt = resp.unwrap("usableAmt", BigDecimal.class);
            Optional<BigDecimal> balance = resp.unwrap("balance", BigDecimal.class);
            Optional<Integer> rate = resp.unwrap("rate", Integer.class);

            Map<String, Object> ret = new HashMap<>();
            // 燃料
            ret.put("fuel", balance.orElse(BigDecimal.ZERO).setScale(4, BigDecimal.ROUND_DOWN)); // balance.orElse(BigDecimal.ZERO).setScale(0, BigDecimal.ROUND_FLOOR));
            // BT
            ret.put("bt", amt.orElse(BigDecimal.ZERO).setScale(0, BigDecimal.ROUND_FLOOR));
            // 比例
            ret.put("rate", rate.orElse(0));

            return MessageRespResult.success4Data(ret);
        }

        return MessageRespResult.error(MessageCode.H5_PLATFORM_ERR, resp.getMsg());
    }

    /*
    // 暂时不暴露该接口
    @ApiOperation(value = "充值失败退款", notes = "充值到H5游戏平台失败后可调用退款，必须是充值失败的订单")
    @ApiImplicitParam(name = "refId", value = "充值记录ID", paramType = "path")
    @PostMapping("/refund/{refId}")
    public MessageRespResult<?> refund(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember auth,
                                       @PathVariable("refId") Long refId) { // 退款
        Member member = memberService.findOne(auth.getId());
        if (member == null) {
            return MessageRespResult.error(MessageCode.MISSING_USER);
        }

        IH5GameService.Resp resp = gameService.refund(member, refId);

        if (resp.isSuccess()) {
            return MessageRespResult.success4Data(resp.getRecord().getId());
        }

        return MessageRespResult.error(resp.getCode(), resp.getMessage());
    }
    */

    @Autowired
    public void setGameService(IH5GameService gameService) {
        this.gameService = gameService;
    }

    @Autowired
    public void setGameApiService(IH5GameApiService gameApiService) {
        this.gameApiService = gameApiService;
    }

    @Autowired
    public void setMemberService(MemberService memberService) {
        this.memberService = memberService;
    }

}
