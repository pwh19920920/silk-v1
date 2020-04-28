package com.spark.bitrade.mapper.dao;

import org.apache.ibatis.annotations.Param;
import java.util.List;
import com.spark.bitrade.entity.PayRoleConfig;

public interface PayRoleConfigMapper {

    int insert(@Param("pojo") PayRoleConfig pojo);

    int insertList(@Param("pojos") List<PayRoleConfig> pojo);

    List<PayRoleConfig> select(@Param("pojo") PayRoleConfig pojo);

    int update(@Param("pojo") PayRoleConfig pojo);

    PayRoleConfig findOneById(@Param("id") Long id);

}
