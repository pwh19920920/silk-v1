package com.spark.bitrade.dao;

import com.spark.bitrade.constant.AuditStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.MemberApplication;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

/**
 * @author rongyu
 * @description
 * @date 2017/12/26 15:12
 */
public interface MemberApplicationDao extends BaseDao<MemberApplication> {
//    MemberApplication findMemberApplicationByMemberAndAuditStatusOrderByIdDesc(Member var1, AuditStatus var2);
    MemberApplication findFirstByMemberAndAuditStatusOrderByIdDesc(Member var1, AuditStatus var2);

    //add by yangch 时间： 2018.05.11 原因：代码合并
//    List<MemberApplication> findMemberApplicationByMemberAndAuditStatusOrderByIdDesc(Member var1, AuditStatus var2);

    @Modifying
    @Query("update MemberApplication m set m.opType = 2 where m.id=?1")
    int updateOpType(Long id);

    @Query(value = "select count(1) num from member_application where id = (select max(id) from member_application where member_id=?1)", nativeQuery = true)
    int countMemberApplicationByAuditStatus(Long memberId);

    @Query(value = "select * from member_application where id = (select max(id) from member_application where member_id=?1)", nativeQuery = true)
    MemberApplication getMemberApplicationByAuditStatus(Long memberId);
}
