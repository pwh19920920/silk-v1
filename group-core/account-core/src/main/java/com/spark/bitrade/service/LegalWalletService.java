package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.dao.LegalWalletDao;
import com.spark.bitrade.entity.LegalWallet;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.QLegalWallet;
import com.spark.bitrade.service.Base.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

//del by yangch 时间： 2018.04.21 原因：代码同步是发现该类已删除
@Service
@Slf4j
public class LegalWalletService extends BaseService<LegalWallet> {
    @Autowired
    private LegalWalletDao legalWalletDao;

    @Autowired
    private void setDao(LegalWalletDao legalWalletDao) {
        super.setDao(legalWalletDao);
    }

    /**
     * 根据memberId查询用户合法钱包（为null 自动创建）
     *
     * @param
     * @return
     */
    public LegalWallet findLegalWalletByMember(Member member) {
        BooleanExpression eq = QLegalWallet.legalWallet.member.eq(member);
        LegalWallet legalWallet = legalWalletDao.findOne(eq);
        if (legalWallet == null) {
            legalWallet = legalWalletDao.save(new LegalWallet(member));
        }
        return legalWallet;
    }

    public LegalWallet findOne(Long id) {
        return legalWalletDao.findOne(id);
    }


}
