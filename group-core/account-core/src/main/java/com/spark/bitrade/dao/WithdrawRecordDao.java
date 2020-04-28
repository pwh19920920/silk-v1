package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.WithdrawRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

/**
 * @author Zhang Jinwei
 * @date 2018年01月29日
 */
public interface WithdrawRecordDao extends BaseDao<WithdrawRecord> {

    /**
     * 获取上一次提币的时间间隔
     * @author shenzucai
     * @time 2018.11.22 10:47
     * @param memberId
     * @param coinName
     * @return true
     */
    @Query(value="select DATEDIFF(CURDATE(),DATE(create_time)) days from withdraw_record where status = 3 AND 32 < LENGTH(transaction_number) and member_id = :memberId and coin_id = :coinName order by create_time desc limit 1",nativeQuery = true)
    Integer getLastWithDrawDayDiff(@Param("memberId")long memberId,@Param("coinName")String coinName);
}
