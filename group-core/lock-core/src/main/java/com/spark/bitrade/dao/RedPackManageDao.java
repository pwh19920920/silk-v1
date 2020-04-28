package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.RedPackManage;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.criteria.CriteriaBuilder;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 红包信息活动管理表（red_pack_manage） Mapper 接口
 * </p>
 *
 * @author qiliao
 * @since 2019-11-25
 */
public interface RedPackManageDao extends BaseDao<RedPackManage> {
    @Query(value = "SELECT " +
            " * " +
            " FROM " +
            " red_pack_manage rpm " +
            " WHERE " +
            " rpm.start_time <= NOW() " +
            " AND rpm.end_time >= NOW() " +
            " AND rpm.red_packet_balance>0 and rpm.surplus_count>0 and rpm.priority >0   " +
            " ORDER BY " +
            " rpm.priority DESC " +
            " LIMIT 1",nativeQuery = true)
    RedPackManage findValidRedPack();

    @Query(value = "update RedPackManage r set r.redPacketBalance=r.redPacketBalance-:changeAmount," +
            " r.surplusCount=r.surplusCount-:counts " +
            " where r.redPacketBalance-:changeAmount>=0 and r.surplusCount-:counts>=0 and r.id=:id")
    @Modifying
    int updateBalance(@Param("changeAmount") BigDecimal changeAmount, @Param("id") Long id, @Param("counts")Integer count);

    @Query(value = "update red_pack_manage set red_packet_balance=red_packet_balance+:receiveAmount ,surplus_count=surplus_count+1 where  id=:redpackId",nativeQuery = true)
    @Modifying
    int returnBalance(@Param("receiveAmount") BigDecimal receiveAmount,@Param("redpackId") Long redpackId);

    @Query(value = "SELECT * FROM red_pack_manage r WHERE r.red_packet_balance>=0 AND r.end_time<NOW()",nativeQuery = true)
    List<RedPackManage> findExpireManages();

    @Query(value = "update red_pack_manage set red_packet_balance=red_packet_balance-:amount where  id=:manageId and red_packet_balance>=:amount",nativeQuery = true)
    @Modifying
    int updateManageRedBalance(@Param("manageId") Long manageId,@Param("amount")  BigDecimal amount);

    @Query(value = "select member_id from support_apply_red_pack where red_pack_manage_id=:manageId limit 1",nativeQuery = true)
    Long findSupportProjectMemberId(@Param("manageId")Long manageId);

}
