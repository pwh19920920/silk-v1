package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.dto.PayAccountVo;
import com.spark.bitrade.service.SuperMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import com.spark.bitrade.entity.PaySupportCoinConfig;

public interface PaySupportCoinConfigMapper extends SuperMapper<PaySupportCoinConfig>{

//    int insert(@Param("pojo") PaySupportCoinConfig pojo);
//
//    int insertList(@Param("pojos") List< PaySupportCoinConfig> pojo);

//    List<PaySupportCoinConfig> select(@Param("pojo") PaySupportCoinConfig pojo);

//    int update(@Param("pojo") PaySupportCoinConfig pojo);

    /**
     * 账户列表查询（有效币种）
     */
    List<PayAccountVo> findAccountByValidCoinAndAppIdOrderByRankDesc(@Param("memberId")Long memberId, @Param("appId")String appId);

    /**
     * 币种列表根据状态查询（排序倒序）
     */
    List<PaySupportCoinConfig> findAllByStatusAndAppIdOrderByRankDesc(@Param("status")int status, @Param("appId")String appId);

}
