package com.spark.bitrade.service;

import com.spark.bitrade.dao.VoteDetailDao;
import com.spark.bitrade.entity.Vote;
import com.spark.bitrade.entity.VoteDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Zhang Jinwei
 * @date 2018年03月30日
 */
@Service
public class VoteDetailService {
    @Autowired
    private VoteDetailDao voteDetailDao;

    public VoteDetail save(VoteDetail voteDetail){
        return voteDetailDao.save(voteDetail);
    }

    public int queryVoted(long userId,Vote vote){
        return voteDetailDao.findAllByUserIdAndVote(userId, vote)
                .stream()
                .map(x -> x.getVoteAmount())
                .reduce(Integer::sum)
                .orElse(0);
    }
}
