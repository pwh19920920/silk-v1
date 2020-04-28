package com.spark.bitrade.mapper.dao;

import org.apache.ibatis.annotations.Param;
import java.util.List;
import com.spark.bitrade.entity.PayMannerConfig;

public interface PayMannerConfigMapper {

    int insert(@Param("pojo") PayMannerConfig pojo);

    int insertList(@Param("pojos") List< PayMannerConfig> pojo);

    List<PayMannerConfig> select(@Param("pojo") PayMannerConfig pojo);

    int update(@Param("pojo") PayMannerConfig pojo);

}
