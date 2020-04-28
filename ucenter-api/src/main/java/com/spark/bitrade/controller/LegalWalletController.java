package com.spark.bitrade.controller;

import com.spark.bitrade.entity.LegalWallet;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.LegalWalletService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

//del by yangch 时间： 2018.04.21 原因：代码同步时发现该类已删除
//@RestController
//@RequestMapping("legal-wallet")
public class LegalWalletController extends BaseController {
    @Autowired
    private LegalWalletService legalWalletService;
    @Autowired
    private MemberService memberService;

    //查询用户 人民币 新加坡币金额
    @GetMapping()
    public MessageResult findWallet(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Member member = memberService.findOne(user.getId());
        Assert.notNull(member, "validate loginUserId!");
        LegalWallet legalWallet = legalWalletService.findLegalWalletByMember(member);
        return success(legalWallet);
    }
}
