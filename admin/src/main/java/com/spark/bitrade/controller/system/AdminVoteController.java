package com.spark.bitrade.controller.system;

import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.PreCoin;
import com.spark.bitrade.entity.Vote;
import com.spark.bitrade.service.CoinService;
import com.spark.bitrade.service.PreCoinService;
import com.spark.bitrade.service.VoteService;
import com.spark.bitrade.util.MessageResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("system/vote")
public class AdminVoteController extends BaseAdminController {

    @Autowired
    private VoteService voteService;

    @Autowired
    private CoinService coinService ;

    @Autowired
    private PreCoinService preCoinService ;

    @RequiresPermissions("system:vote:merge")
    @PostMapping("merge")
    public MessageResult merge(@RequestBody Vote vote) {
        if(vote.getId()!=null){
            for(PreCoin preCoin : vote.getPreCoins()){
                preCoin.setVote(vote);
            }
        }
        for(PreCoin preCoin:vote.getPreCoins()){
            Coin coin = coinService.findByUnit(preCoin.getUnit());
            if(coin!=null){
                return error("预选币中有已存在的币种");
            }
        }
        Assert.notNull(vote, "vote null");
        vote = voteService.save(vote);
        return MessageResult.getSuccessInstance("保存成功", vote);
    }

    @RequiresPermissions("system:vote:detail")
    @PostMapping("detail")
    public MessageResult detail(Long id) {
        Vote vote = voteService.findById(id);
        return MessageResult.getSuccessInstance("保存成功", vote);
    }

    @RequiresPermissions("system:vote:page-query")
    @PostMapping("page-query")
    public MessageResult pageQuery(PageModel pageModel) {
        Page<Vote> all = voteService.findAll(null, pageModel.getPageable());
        return success(all);
    }
}
