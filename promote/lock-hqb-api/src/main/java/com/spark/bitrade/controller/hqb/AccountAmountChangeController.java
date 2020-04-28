package com.spark.bitrade.controller.hqb;

import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.service.impl.ILockHqbInRecordService;
import com.spark.bitrade.service.impl.ILockHqbMemberWalletService;
import com.spark.bitrade.service.impl.ILockHqbOutRecordService;
import com.spark.bitrade.util.MessageResult;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

@RestController
@Slf4j
public class AccountAmountChangeController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private ILockHqbInRecordService iLockHqbInRecordService;

    @Autowired
    private ILockHqbMemberWalletService iLockHqbMemberWalletService;

    @Autowired
    private ILockHqbOutRecordService iLockHqbOutRecordService;
    @Autowired
    private LocaleMessageSourceService messageSourceService;
    @ApiOperation(value = "活期宝转入操作", tags = "活期宝-v1.0")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "币种", required = true),
            @ApiImplicitParam(name = "amount", value = "转入数量", required = true)
    })
    @PostMapping("hqb/hqbTransferIn")
    public MessageResult hqbTransferIn(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user,
                                       String symbol, BigDecimal amount) throws Exception {
        log.info("================会员" + user.getId() + "开始执行活期宝转入操作====================");
        // 转入活期宝数量需大于0
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) == 1, messageSourceService.getMessage("TRANS_IN_NEED_GT_0"));
        Member member = memberService.findOne(user.getId());//user.getId());

        iLockHqbInRecordService.hqbTransferInOperation(member.getId(), user.getPlatform(), symbol, amount);

        // 清理缓存
        iLockHqbMemberWalletService.clearWalletCache(member.getId(), user.getPlatform(), symbol);

        log.info("================会员" + user.getId() + "执行活期宝转入操作，成功====================");

        return MessageResult.success();
    }

    @ApiOperation(value = "活期宝转出操作", tags = "活期宝-v1.0")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "币种", required = true),
            @ApiImplicitParam(name = "amount", value = "转入数量", required = true)
    })
    @PostMapping("hqb/hqbTransferOut")
    public MessageResult hqbTransferOut(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember user, String symbol, BigDecimal amount) throws Exception {
        log.info("================会员" + user.getId() + "开始执行活期宝转出操作====================");
        // 转出活期宝数量需大于0
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) == 1, messageSourceService.getMessage("TRANS_OUT_NEED_GT_0"));
        Member member = memberService.findOne(user.getId());

        iLockHqbOutRecordService.hqbTransferOutOperation(member.getId(), user.getPlatform(), symbol, amount);

        // 清理缓存
        iLockHqbMemberWalletService.clearWalletCache(member.getId(), user.getPlatform(), symbol);

        log.info("================会员" + user.getId() + "执行活期宝转出操作，成功====================");

        return MessageResult.success();
    }
}
