package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.dao.VoteDao;
import com.spark.bitrade.entity.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @author Zhang Jinwei
 * @date 2018年03月26日
 */
@Service
public class VoteService {

    @Autowired
    private VoteDao voteDao;

    public Vote findById(Long id){
        return voteDao.findOne(id);
    }

    public Vote findVote(){
       return voteDao.findVote();
    }

    public Vote save(Vote vote){
        return voteDao.save(vote);
    }

    public Page<Vote> findAll(Predicate predicate, Pageable pageable) {
        return voteDao.findAll(predicate, pageable);
    }

}
