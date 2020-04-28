package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.MemberApiSecretDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>MemberApiSecretMapper</p>
 * @author tian.bo
 * @date 2018/12/6.
 */
@Mapper
public interface MemberApiSecretMapper {
   MemberApiSecretDTO selectByAccessKey(@Param("accessKey") String accessKey);
}
