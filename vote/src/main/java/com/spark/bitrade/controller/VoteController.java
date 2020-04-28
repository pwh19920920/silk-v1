package com.spark.bitrade.controller;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.BigDecimalUtils;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.math.BigDecimal;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * @author Zhang Jinwei
 * @date 2018年03月26日
 */
@RestController
@Slf4j
@RequestMapping(method = RequestMethod.POST)
public class VoteController {

    @Autowired
    private VoteService voteService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private PreCoinService preCoinService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private VoteDetailService voteDetailService;

    /**
     * 投票
     *
     * @param preCoinId
     * @param amount
     * @param user
     * @return
     */
    @RequestMapping(value = "/vote", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public MessageResult vote(long preCoinId, int amount, @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        PreCoin preCoin = preCoinService.findById(preCoinId);
        if (preCoin.getVote().getStatus().equals(BooleanEnum.IS_FALSE)) {
            return MessageResult.error("This vote has been closed");
        }
        Coin coin = coinService.queryPlatformCoin();
        if (coin == null) {
            return MessageResult.error("system not set platform coin");
        }
        Assert.isTrue(amount > 0, "The number of votes must be greater than 0");
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, user.getId());
        Assert.isTrue(memberWallet != null, "wallet is null");
        BigDecimal consume = BigDecimalUtils.mul(preCoin.getVote().getAmount(), amount);
        Assert.isTrue(memberWallet.getBalance().compareTo(consume) >= 0, "Insufficient balance");
        int voted = voteDetailService.queryVoted(user.getId(), preCoin.getVote());
        Assert.isTrue(preCoin.getVote().getVoteLimit() - voted >= amount, "You can vote up to " + (preCoin.getVote().getVoteLimit() - voted) + " votes");
        if (memberWalletService.deductBalance(memberWallet, consume) > 0) {
            preCoin.setAmount(preCoin.getAmount() + amount);
            preCoinService.save(preCoin);
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setMemberId(user.getId());
            memberTransaction.setType(TransactionType.VOTE);
            memberTransaction.setSymbol(memberWallet.getCoin().getUnit());
            memberTransaction.setAmount(consume);
            memberTransactionService.save(memberTransaction);
            VoteDetail voteDetail = new VoteDetail();
            voteDetail.setVoteAmount(amount);
            voteDetail.setPreCoin(preCoin);
            voteDetail.setVote(preCoin.getVote());
            voteDetail.setUserId(user.getId());
            voteDetail.setAmount(consume);
            voteDetailService.save(voteDetail);
            return MessageResult.success();
        } else {
            return MessageResult.error("Insufficient balance");
        }

    }

    /**
     * 投票信息
     *
     * @return
     */
    @RequestMapping(value = "/vote/info", method = RequestMethod.POST)
    public MessageResult voteInfo() {
        MessageResult result = MessageResult.success();
        Vote vote = voteService.findVote();
        result.setData(vote);
        return result;
    }
}
