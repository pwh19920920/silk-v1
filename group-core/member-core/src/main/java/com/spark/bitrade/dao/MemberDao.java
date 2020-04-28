package com.spark.bitrade.dao;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.Member;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface MemberDao extends BaseDao<Member> {

    List<Member> getAllByEmailEquals(String email);

    List<Member> getAllByUsernameEquals(String username);

    List<Member> getAllByMobilePhoneEquals(String phone);

    Member findByUsername(String username);

    Member findMemberByTokenAndTokenExpireTimeAfter(String token, Date date);

    Member findMemberByToken(String token);

    Member findMemberByIdAndUsernameAndStatus(Long id, String username, CommonStatus status);

    Member findMemberByMobilePhoneOrEmail(String phone, String email);

    int countByRegistrationTimeBetween(Date startTime, Date endTime);

    Member findMemberByPromotionCode(String code);

    Member findMemberByEmail(String email);

    Member findMemberByMobilePhone(String mobilePhone);

    List<Member> findAllByInviterId(Long id);

    /*@Query("select new com.spark.bitrade.dto.MemberDTO(member,memberWallet) from")*/

    @Query(value = "select m.username from member m where m.id = :id", nativeQuery = true)
    String findUserNameById(@Param("id") Long id);

    /**
     * 添加身份证查询接口
     *
     * @param idNumber
     * @return true
     * @author shenzucai
     * @time 2018.04.24 19:01
     */
    List<Member> findAllByIdNumber(String idNumber);

    /**
     * 根据身份证号码、审核状态（0/1-待审核、2-审核通过），获取会员总条数
     *
     * @param idNumber 身份证号码
     * @return 会员总条数
     * @author zhongxj
     * @date 2019.08.02
     * @desc 一个身份证，最多实名认证2个账号
     */
    @Query(value = "select count(DISTINCT id_card) AS totalMember from (select CONCAT_WS(',',id,id_number) AS id_card from member m where m.id_number = ?1 and m.real_name_status in (1,2) union all select CONCAT_WS(',',member_id,id_card) from member_application  WHERE id_card=?1 AND audit_status in (0,2)) card", nativeQuery = true)
    Integer countMemberByIdNumberAndRealNameStatus(String idNumber);

    /**
     * @param areaId
     * @return true
     * @author shenzucai
     * @time 2018.05.30 11:53
     */
    List<Member> findAllByAreaId(String areaId);


    @Modifying
    @Query("update Member m set m.token=:token,m.tokenExpireTime=:tokenExpireTime where m.id = :id")
    int updateToken(@Param("id") long id, @Param("token") String token, @Param("tokenExpireTime") Date tokenExpireTime);

    //add by yangch 时间： 2018.07.31 原因：更新交易次数
    @Modifying
    @Query("update Member m set m.transactions=m.transactions+1  where m.id = :id")
    int updateTransactionsTime(@Param("id") long id);

    @Modifying
    @Query("update  Member m set m.password=:password where m.id=:memberId")
    int updateMemberPassword(@Param("memberId")Long memberId,@Param("password")String password);

    @Modifying
    @Query("update  Member m set m.status=:status where m.id=:memberId")
    int updateMemberStatus(@Param("memberId")Long memberId,@Param("status")CommonStatus status);

    @Modifying
    @Query("update  Member m set m.transactionStatus=:status where m.id=:memberId")
    int updateMemberTransactionStatus(@Param("memberId")Long memberId,@Param("status")BooleanEnum status);

    @Modifying
    @Query("update  Member m set m.publishAdvertise=:status where m.id=:memberId")
    int updateMemberPublishStatus(@Param("memberId")Long memberId,@Param("status")BooleanEnum status);

    @Modifying
    @Query(value = "update  member set ali_no=null, qr_code_url=null where id=:memberId",nativeQuery = true)
    int deleteMemberAli(@Param("memberId")Long memberId);

    @Modifying
    @Query(value = "update  member set wechat=null,wechat_nick=null ,qr_we_code_url=null where id=:memberId",nativeQuery = true)
    int deleteMemberWeChat(@Param("memberId")Long memberId);

}
