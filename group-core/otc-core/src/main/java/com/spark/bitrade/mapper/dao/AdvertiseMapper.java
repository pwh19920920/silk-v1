package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.Advertise;
import com.spark.bitrade.entity.transform.OtcAdvertise;
import com.spark.bitrade.entity.transform.ScanAdvertise;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
  * 法币交易订单mapper
  * @author tansitao
  * @time 2018/6/22 10:54 
  */
@Mapper
public interface AdvertiseMapper {


	//add by tansitao 时间： 2018/7/17 原因：通过id查询广告信息
	@Select("SELECT a.member_id bMemberId, a.country countryName,a.* from advertise a where a.id = #{advertiseId}")
	Advertise findById(@Param("advertiseId") long advertiseId);

	//add by tansitao 时间： 2018/7/17 原因：分页查询广告
	List<OtcAdvertise> pageQueryByOtcCoin(@Param("coinId") long coinId, @Param("advertiseType") long advertiseType, @Param("status") long status, @Param("marketPrice")double marketPrice, @Param("coinScale")int coinScale);

	//add by tansitao 时间： 2018/8/27 原因：增加排序分页查看广告
	List<OtcAdvertise> pageQueryByOtcCoinByRank(@Param("coinId") long coinId, @Param("advertiseType") long advertiseType, @Param("status") long status, @Param("marketPrice")double marketPrice, @Param("advertiseRankType")int advertiseRankType, @Param("isPositive")int isPositive, @Param("coinScale")int coinScale);

	@Select("SELECT a.id, a.member_id memberId, a.sort from advertise a order by sort desc limit 1")
	Map<String,Object> findOneBySortMax();

	//add by tansitao 时间： 2018/7/17 原因：获取某些用户的广告
	List<Advertise> findByMemberIds(@Param("memberIds") List<Long> memberIds, @Param("number")BigDecimal number,@Param("marketPrice")BigDecimal marketPrice, @Param("advertiseType") int advertiseType, @Param("coinId")Long coinId,@Param("currencyId")Long currencyId);


	List<Advertise> findByMemberIdAndMoney(@Param("memberIds") List<Long> memberIds, @Param("money")BigDecimal money, @Param("advertiseType") int advertiseType, @Param("coinId")Long coinId,@Param("currencyId")Long currencyId);
}
