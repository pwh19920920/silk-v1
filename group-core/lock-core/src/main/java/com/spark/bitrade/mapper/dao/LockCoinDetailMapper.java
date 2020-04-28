package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.constant.LockStatus;
import com.spark.bitrade.constant.LockType;
import com.spark.bitrade.dto.LockCoinActivitieProjectDto;
import com.spark.bitrade.dto.LockCoinDetailVo;
import com.spark.bitrade.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
  * 锁仓详情mapper
  * @author tansitao
  * @time 2018/6/22 10:54 
  */
@Mapper
public interface LockCoinDetailMapper {

	/**
	 * 查询所有活动列表
	 */
	List<LockCoinDetailBuilder> pageQueryByMemberAndActivitId(@Param("activitieId") long activitieId, @Param("memberId") long memberId, @Param("type") int type);

	/**
	 * 分页查询锁仓记录
	 */
	List<LockCoinDetailBuilder> pageQueryByMember(@Param("memberId") long memberId, @Param("type") int type);

	/**
	 * 分页查询锁仓记录
	 */
	List<LockCoinDetailBuilder> pageQueryByMemberAndTypeAndSymbol(@Param("memberId") long memberId, @Param("type") int type, @Param("symbol") String symbol);

	/**
	 * 查询解锁列表
	 */
	List<UnLockCoinDetailBuilder> findHandleLockDetail(@Param("memberId") long memberId);

	/**
	 * 查询结束详情列表信息
	 */
	List<UnlockCoinDetail> findHandleUnLockDetail(@Param("lockCoinDetailId") long lockCoinDetailId);

	/**
	 * 查询锁仓详情
	 */
	LockCoinDetailBuilder findOneByid(@Param("lockCoinDetailId") long lockCoinDetailId);

	/**
	 * 通过id和类型查询锁仓记录
	 */
	LockCoinDetail findOneByIdAndType(@Param("id") long id, @Param("type") int type);


	/**
	 * 根据用户id查询SLB节点产品购买总金额（cny）
	 */
	BigDecimal queryUserQuantifyTotalCny(@Param("memberId") Long memberId);

	/**
	 * edit by tansitao 时间： 2018/11/27 原因：通过短信状态和锁仓类型查询锁仓记录
	 */
	List<LockCoinDetailVo> findByPlanUnlockTimeAndSmsSendStatus(@Param("smsSendStatus")int smsSendStatus, @Param("type") int type);

	/**
	 * 通过id和类型和用户id查询锁仓详情
	 */
	LockCoinDetail findOneByIdAndTypeAndMemberId(@Param("id") long id, @Param("type") int type, @Param("memberId") long memberId);

	/**
	 * 根据用户id和币种，查询总的锁仓收益
	 */
	BigDecimal queryTotalInCome(@Param("memberId") Long memberId, @Param("unit") String unit, @Param("type") int type);

	/**
	 * 根据会员id和类型查询已锁定和解锁中的记录
	 */
	List<LockCoinDetail> findByMemberIdAndType(@Param("memberId")Long memberId, @Param("type")LockType type);

	/**
 	 * 统计STO增值计划所有收益和未解锁锁仓数
	 */
	Map<String,Object> statTotalAndIncome(@Param("memberId") Long memberId,@Param("type")int type,@Param("symbol")String symbol);

	/**
	 * 根据用户查询满足资格条件的锁仓条数
	 * @param memberId
	 * @return
	 */
    BigDecimal queryQualificationDetailByMemberId(@Param("memberId") Long memberId,@Param("lockDays") int lockDays,@Param("symbol")String symbol);
	/**
	 * 查询用户的子部门锁仓数量
	 * @param memberId
	 * @return
	 */
	BigDecimal queryMemberSubAmount(@Param("memberId") Long memberId, @Param("symbol")String symbol);

	/**
	 * 根据锁仓id查询锁仓详情
	 * @param id
	 * @return
	 */
	LockCoinDetail queryLockDetailById(@Param("id")Long id);

	/**
	 * 根据锁仓id查询是否存在返佣记录
	 * @param lockDetailId
	 * @return
	 */
	int isExistRewardRecord(@Param("lockDetailId") Long lockDetailId);

	/**
	 * 根据活动id、会员ID 查询锁仓记录
	 *
	 * @param memberId    会员id
	 * @param activitieId 活动ID
	 */
	@Select("select * from lock_coin_detail WHERE ref_activitie_id = #{activitieId} AND member_id = #{memberId}")
	List<LockCoinDetail> findByMemberIdAndId(@Param("memberId") Long memberId, @Param("activitieId") Long activitieId);

	/**
	 * 查询金钥匙活动用户锁仓记录
	 * @param memberId
	 * @param lockType
	 * @return
	 */
	List<CustomerLockCoinDetail> findGoldenKeyLockRecords(@Param("memberId") Long memberId, @Param("lockType") LockType lockType);

	/**
	 * 查询活动中心配置的所有活动
	 * @return
	 */
	List<LockCoinActivitieProjectDto> listAll();

	/**
	 * 统计每个活动参加人数
	 * @param ids
	 * @return
	 */
	List<LockCoinActivitieProjectDto> memberCount(List<Long> ids);

	LockCoinActivitieProjectInternational selectInternational(@Param("projectId") Long projectId,@Param("type")Integer type);

	LockCoinActivitieSettingInternational selectInternationalSetting(@Param("settingId") Long settingId,@Param("type")Integer type);

	/**
	 * 查询单个活动详情
	 * @return
	 */
	LockCoinActivitieProjectDto findProjectById(Long activiteId);

	/**
	 * 查询当前用户参加的活动
	 * @param memberId
	 * @return
	 */
	List<LockCoinDetail> findByMemberIds(@Param("memberId") Long memberId,@Param("list")List<LockCoinActivitieSetting> list);

	/**
	 * 查询当前用户参加的活动
	 * @param memberId
	 * @return
	 */
	List<LockCoinDetail> findByMemberId(@Param("memberId") Long memberId,@Param("activitieId")Long activitieId);

	/**
	 * 查询所有活动的配置
	 * @param list
	 * @return
	 */
	List<LockCoinActivitieSetting> selectByActivitieIds(List<Long> list);
}
