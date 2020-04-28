package com.spark.bitrade.dao;

import com.spark.bitrade.entity.Vote;
import com.spark.bitrade.entity.VoteDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年03月30日
 */
public interface VoteDetailDao extends JpaRepository<VoteDetail,Long>,JpaSpecificationExecutor<VoteDetail> {
    List<VoteDetail> findAllByUserIdAndVote(long var1, Vote vote);
}
