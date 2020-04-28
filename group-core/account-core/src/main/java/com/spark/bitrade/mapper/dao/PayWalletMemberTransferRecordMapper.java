package com.spark.bitrade.mapper.dao;

import org.apache.ibatis.annotations.Param;
import java.util.List;
import com.spark.bitrade.entity.PayWalletMemberTransferRecord;

public interface PayWalletMemberTransferRecordMapper {

    int insert(@Param("pojo") PayWalletMemberTransferRecord pojo);

    int insertList(@Param("pojos") List< PayWalletMemberTransferRecord> pojo);

    List<PayWalletMemberTransferRecord> select(@Param("pojo") PayWalletMemberTransferRecord pojo);

    int update(@Param("pojo") PayWalletMemberTransferRecord pojo);

}
