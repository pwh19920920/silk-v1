package com.spark.bitrade.dao;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.Vote;
import org.springframework.data.jpa.repository.Query;

/**
 * @author Zhang Jinwei
 * @date 2018年03月26日
 */
public interface VoteDao extends BaseDao<Vote> {

    Vote findVoteByStatus(BooleanEnum var);

    @Query("select a from Vote a order by a.id desc")
    Vote findVote();
}
