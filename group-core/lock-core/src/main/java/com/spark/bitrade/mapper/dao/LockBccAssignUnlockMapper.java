package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.plugins.Page;
import com.spark.bitrade.entity.LockBccAssignUnlock;
import com.spark.bitrade.service.SuperMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface LockBccAssignUnlockMapper extends SuperMapper<LockBccAssignUnlock> {

    @Select("select * from lock_bcc_assign_unlock where member_id = #{memberId} order by release_time desc")
    List<LockBccAssignUnlock> findLockBccAssignUnlocksByMemberId(@Param("memberId") Long memberId);
}
