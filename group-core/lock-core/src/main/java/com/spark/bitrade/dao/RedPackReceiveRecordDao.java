package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.RedPackReceiveRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * <p>
 * 红包领取记录(red_pack_receive_record) Mapper 接口
 * </p>
 *
 * @author qiliao
 * @since 2019-11-25
 */
public interface RedPackReceiveRecordDao extends BaseDao<RedPackReceiveRecord> {


    @Query(value = "select * from red_pack_receive_record rc WHERE   rc.id=:recordId",nativeQuery = true)
    RedPackReceiveRecord findValidRecordById(@Param("recordId") Long id);

    @Query(value = "select a from RedPackReceiveRecord a where a.memberId=:memberId and a.redpackId=:redpackId and a.receiveStatus=1")
    List<RedPackReceiveRecord> findByMemberIdAndRedpackIdStatus(@Param("memberId") Long memberId, @Param("redpackId") Long redpackId);
    @Query(value = "select * from red_pack_receive_record WHERE DATE_ADD(create_time,INTERVAL within hour)<NOW() AND member_id is NULL and receive_status=0",nativeQuery = true)
    List<RedPackReceiveRecord> findByMemberIdIsNullAndLast();

    @Query(value = "select * from red_pack_receive_record where member_id is NULL and receive_status=0 and redpack_id=:manageId",nativeQuery = true)
    List<RedPackReceiveRecord> findRecordExpireByManageId(@Param("manageId")Long manageId);

}












