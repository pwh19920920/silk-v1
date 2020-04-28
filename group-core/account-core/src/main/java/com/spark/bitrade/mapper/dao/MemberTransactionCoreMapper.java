package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.plugins.Page;
import com.spark.bitrade.dto.FeeOtcExchangeDto;
import com.spark.bitrade.dto.MemberDepositDTO;
import com.spark.bitrade.dto.MemberTransactionDTO;
import com.spark.bitrade.dto.MemberTransactionDetailDTO;
import com.spark.bitrade.entity.MemberTransaction;
import com.spark.bitrade.service.SuperMapper;
import com.spark.bitrade.vo.UnlockedGoldKeyAmountVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
  * 用户交易记录mapper
  * @author tansitao
  * @time 2018/7/27 13:59 
  */
@Mapper
public interface MemberTransactionCoreMapper  extends SuperMapper<MemberTransaction> {


	/**
	 * 通过类型和时间查询用户交易记录
	 * @author tansitao
	 * @time 2018/7/27 13:59 
	 */
	List<MemberTransactionDTO> queryByTypeAndTime(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("type") String type, @Param("memberId") Long memberId);

	/*
	* 交易明细记录
	* @author Zhang Yanjun
	* @time 2018.08.30 11:45
	* @param map
	*/
	List<MemberTransactionDetailDTO> findBy(Map<String,Object> map);

	/**
	 * 查询金钥匙活动交易记录
	 * @param memberId
	 * @return
	 */
	@Select("select amount as unlockedAmount, create_time as lockTime,type as lockType from member_transaction" +
			" where member_id=#{memberId} and symbol='BTTC' and (type=28 or type=29) and amount > 0 order by create_time desc")
	List<UnlockedGoldKeyAmountVo> findGoldenKeyTransactions(@Param("memberId") Long memberId);


	List<FeeOtcExchangeDto> findFeeDto(@Param("startTime") String startTime, @Param("endTime") String endTime);


    List<MemberDepositDTO> findMemberRechargeRecord(@Param("memberId") long memberId, @Param("unit") String unit);
}
