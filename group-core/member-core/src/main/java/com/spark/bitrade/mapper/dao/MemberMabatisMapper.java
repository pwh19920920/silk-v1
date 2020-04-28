package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.dto.MemberInfoDTO;
import com.spark.bitrade.dto.MemberSecurityInfoDto;
import com.spark.bitrade.dto.PromotionMemberDTO;
import com.spark.bitrade.entity.ExchangeMemberDiscountRule;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.MemberPaymentAccount;
import com.spark.bitrade.service.SuperMapper;
import com.spark.bitrade.vo.MemberVO;
import com.spark.bitrade.vo.PromotionMemberVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

/**
  * 用户信息
  * @author tansitao
  * @time 2018/7/2 11:09 
  */
@Mapper
public interface MemberMabatisMapper extends SuperMapper<Member> {

	//通过手机查询用户信息
	Member findOneByPhone(@Param("phone") String phone);
	//查找多条件查找用户
	Member findByPhoneAndUserNameAndEmail(@Param("username")String username,@Param("phone")String phone,@Param("email")String email);
	@Select("SELECT * FROM member m WHERE m.promotion_code=#{promotionCode}")
	Member findByPromotionCode(@Param("promotionCode")String promotionCode);

	// @author lingxing
	//统计会员邀请的个数
	List<Map<String,Object>> countSingleTotal(@Param("list") List<PromotionMemberVO>promotionMemberVOList);
	//@author lingxing
	//查找会员信息
	@Select("SELECT * FROM Member m WHERE m.id=#{userId}")
	MemberInfoDTO findByMemberInfoId(@Param("userId")Long userId);

	//查询会员信息
	List<PromotionMemberVO> findBy(@Param("account") String account);
	//查询会员信息
	List<MemberVO> findAllBy(@Param("commonStatus")Integer commonStatus,@Param("account")String account);

	/**
	  * 通过用户id查询用户支付账户信息
	  * @author tansitao
	  * @time 2018/8/13 18:04 
	  */
	@Select("select * from member_payment_account where member_id=${memberId}")
	MemberPaymentAccount findPaymentAccountByMemberId(@Param("memberId") long memberId);

	/**
	 * 分页查询推荐会员的信息
	 * @author tansitao
	 * @time 2018/8/18 14:24 
	 */
	List<PromotionMemberDTO> findPromotionMemberList(@Param("memberId") long memberId);

	/**
	 * 分页查询会员优惠规则
	 * @param memberId
	 * @return
	 */
	List<ExchangeMemberDiscountRule> findMemberDiscountRule(@Param("memberId") Long memberId,@Param("symbol") String symbol);

	/**
	 * 添加会员优惠规则
	 * @author fumy
	 * @time 2018.08.30 14:41
	 * @param params
	 * @return true
	 */
	int insertDiscountRule(Map<String,Object> params);

	/**
	 * 修改会员优惠规则
	 * @author fumy
	 * @time 2018.08.30 14:41
	 * @param params
	 * @return true
	 */
	int updateDiscountRule(Map<String,Object> params);

	int countByMemberIdAndSymbol(@Param("memberId")Long memberId,@Param("symbol")String symbol);

	/**
	 * 根据id查询用户的安全设置信息
	 * @author fumy
	 * @time 2018.11.01 14:14
	 * @param memberId
	 * @return true
	 */
	MemberSecurityInfoDto findSecurityInfo(@Param("memberId") Long memberId);

	/**
	 * 计算某个用户的一级推荐人数量
	 * @author tansitao
	 * @time 2018/12/17 10:47 
	 */
	int countOneInviteeByMemberId(@Param("memberId")Long memberId);
}
