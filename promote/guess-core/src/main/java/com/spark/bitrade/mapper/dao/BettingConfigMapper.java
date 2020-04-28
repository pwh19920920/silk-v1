package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.dto.RecordDTO;
import com.spark.bitrade.entity.BettingConfig;
import com.spark.bitrade.entity.BettingPriceRange;
import com.spark.bitrade.vo.BettingConfigVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface BettingConfigMapper {
    int deleteByPrimaryKey(Long id);

    int insert(BettingConfig record);

    int insertSelective(BettingConfig record);

    BettingConfig selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(BettingConfig record);

    int updateByPrimaryKey(BettingConfig record);

    /**
     * 游戏管理分页查询
     * @author Zhang Yanjun
     * @time 2018.09.13 14:15
     * @param period 期数
     * @param status 活动状态
     */
    List<BettingConfigVo> findAllByPeriodAndStatus(@Param("period") String period, @Param("status") Long status);

    BettingConfig queryConfigById(@Param("id") Long id);

    List<BettingPriceRange> queryPriceRangeByConfigId(@Param("id") Long id);

    /**
      * 通过竞猜币种查找最新一期的生效活动
      * @author tansitao
      * @time 2018/9/13 15:24 
      */
    BettingConfig findByGuessSymbolLately(@Param("guessSymbol") String guessSymbol);

    List<BettingConfig> findAllOfLately();

    int queryIsRunningConfig();

    /**
     * 查询每期活动历史记录（只查开奖中、已完成的数据）
     * @author tansitao
     * @time 2018/9/17 9:39 
     */
    List<RecordDTO> findAllRecord();

    /**
     * 查询某一期的活动数据
     * @author tansitao
     * @time 2018/9/17 21:07 
     */
    RecordDTO findOneRecord(@Param("periodId") long periodId);

    /**
     * 查询上期活动配置
     * @param params
     * @return
     */
    BettingConfig findForwardBetConfig(Map<String,Object> params);


}