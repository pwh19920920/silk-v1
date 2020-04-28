package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.MemberDeposit;
import com.spark.bitrade.entity.MemberTransaction;
import com.spark.bitrade.service.SuperMapper;
import com.spark.bitrade.vo.MemberDepositVO;
import com.spark.bitrade.vo.ThirdAuthQueryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author fumy
 * @time 2018.09.19 17:53
 */
@Mapper
public interface MemberDepositMapper extends SuperMapper<MemberDeposit> {

    List<ThirdAuthQueryVo> queryDepositByUnit(@Param("symbol") String symbol);

    List<MemberDepositVO> queryFoPage(Map<String, Object> params);

    Map<String, Object> getFixMemberStat(@Param("date") String date, @Param("unit") String unit);

    //@Select("select id,user_name from sys_user where id=#{id} ")
    @Select("select * from member_deposit where address = #{address} and txid = #{txid} and unit = #{unit} and amount = #{amount}")
    MemberDeposit findByAddressAndTxidAndUnitAndAmount(@Param("address") String address
            , @Param("txid") String txid
            , @Param("unit") String unit
            , @Param("amount") BigDecimal amount);

    /**
     * 根据交易哈希，获取交易详情
     *
     * @param txid 交易哈希
     * @return 交易详情
     */
    @Select("select * from member_deposit where txid = #{txid}")
    MemberDeposit getMemberDepositByTxid(@Param("txid") String txid);

}
