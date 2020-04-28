package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.vo.ThirdAuthQueryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author fumy
 * @time 2018.09.19 16:21
 */
@Mapper
public interface WithdrawRecordMapper {

    List<ThirdAuthQueryVo> queryWithdrawRecord(@Param("symbol") String symbol);

    @Select("SELECT count(1) FROM withdraw_record WHERE member_id=#{memberId} AND coin_id=#{coinId} AND is_auto=1")
    Integer countMemberWithdraw(@Param("memberId") Long memberId, @Param("coinId") String coinId);

}
