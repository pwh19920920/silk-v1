package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.dto.BettingRecordDTO;
import com.spark.bitrade.entity.BettingRecord;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BettingRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(BettingRecord record);

    int insertSelective(BettingRecord record);

    BettingRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(BettingRecord record);

    int updateByPrimaryKey(BettingRecord record);

    List<BettingRecord> queryByPeriodId(@Param("periodId") Long periodId, @Param("memberId") Long memberId);

    @Select("select * from pg_betting_record br where br.member_id = #{memberId} and br.period_id = #{periodId} limit 1")
    BettingRecord fondOneByPeriodIdAndMemberId(@Param("periodId") Long periodId, @Param("memberId") Long memberId);

    /**
     * 通过用户id，分页查询投票信息
     * @author tansitao
     * @time 2018/9/15 15:43 
     */
    List<BettingRecordDTO> pageQueryByMemberId(@Param("memberId")long memberId);

    /**
     * 总投注数量
     * @param periodId 投注周期id
     * @return
     */
    BigDecimal queryBetTotal(@Param("periodId")long periodId);

    /**
     * 按价格区间计算投注总额
     * @param periodId 投注ID
     * @param rangeId 投注价格区间id
     * @return
     */
    BigDecimal queryBetTotalByPriceRange(@Param("periodId")long periodId, @Param("rangeId")long rangeId);

    /***
     * 获取指定周期的投注用户
     * @author yangch
     * @time 2018.09.18 9:38 
     * @param periodId
     */
    List<String> queryBettingUserByPeriodId(@Param("periodId")long periodId);

    /***
      * 获取指定周期中有推荐邀请关系投注记录
      * @author yangch
      * @time 2018.09.18 9:38 
     * @param periodId
     */
    List<BettingRecord> queryInviterBettingRecordByPeriodId(@Param("periodId")long periodId);

    /**
     * 根据期数统计投注人数(重复投注只计算一次)
     * @param params
     * @return
     */
    Integer findBetCount(Map<String,Object> params);

    /**
      * 查询开启短信的投注信息
      * @author tansitao
      * @time 2018/9/19 16:25 
      */
    @Select("SELECT br.*, m.mobile_phone phone, c.area_code FROM pg_betting_record br LEFT JOIN member m on br.member_id = m.id LEFT JOIN country c on m.country = c.zh_name where br.period_id = #{periodId} and br.use_sms = 1")
    List<BettingRecord> findOpenSmsRecord(@Param("periodId")long periodId);
}