package com.spark.bitrade.mapper.dao;


import com.spark.bitrade.entity.LockHqbCoinSettging;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.LockHqbCoinSettgingVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Mapper
public interface LockHqbCoinSettgingMapper extends BaseMapper<LockHqbCoinSettging> {

    List<LockHqbCoinSettgingVo> findValidSettingByAppId(@Param("appId") String appId);

    List<LockHqbCoinSettgingVo> findInvalidSettingByAppId(@Param("appId") String appId);

    List<LockHqbCoinSettgingVo> findByAppId(@Param("appId") String appId);

    /**
     * 根据币种与appId 查询有效配置
     * @param appId
     * @param symbol
     * @return
     */
    @Select("select * from lock_hqb_coin_settging where app_id = #{appId} and coin_symbol = #{symbol} and (status = 1 or status = 2)" +
            " and valid_time > unix_timestamp(now())  limit 1")
    LockHqbCoinSettging findValidSettingByAppIdAndSymbol(@Param("appId") String appId, @Param("symbol") String symbol);
}
