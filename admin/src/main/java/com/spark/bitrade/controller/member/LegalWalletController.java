package com.spark.bitrade.controller.member;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.LegalWallet;
import com.spark.bitrade.entity.QLegalWallet;
import com.spark.bitrade.service.LegalWalletService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("legal-wallet")
public class LegalWalletController extends BaseAdminController {

    @Autowired
    private LegalWalletService legalWalletService;

    @GetMapping("page")
    public MessageResult page(PageModel pageModel, @RequestParam(value = "username", required = false) String username) {
        Predicate predicate = getPredicate(username);
        Page<LegalWallet> page = legalWalletService.findAll(predicate, pageModel);
        return success(page);
    }

    //
    @GetMapping("{id}")
    public MessageResult id(@PathVariable("id") Long id) {
        LegalWallet one = legalWalletService.findOne(id);
        if (one == null) return error("validate id!");
        return success(one);
    }

    //条件
    private Predicate getPredicate(String username) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (StringUtils.isNotBlank(username)) {
            booleanExpressions.add(QLegalWallet.legalWallet.member.username.eq(username));
        }
        return PredicateUtils.getPredicate(booleanExpressions);
    }

}
