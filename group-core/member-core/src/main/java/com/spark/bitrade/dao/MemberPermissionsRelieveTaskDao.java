package com.spark.bitrade.dao;

import com.spark.bitrade.constant.RelievePermissionsStaus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.MemberPermissionsRelieveTask;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
  * 用户权限自动解禁任务表dao
  * @author tansitao
  * @time 2018/11/27 15:27 
  */
public interface MemberPermissionsRelieveTaskDao extends BaseDao<MemberPermissionsRelieveTask> {

    MemberPermissionsRelieveTask findById(long id);

    @Modifying
    @Query("update MemberPermissionsRelieveTask m set m.status = :status where m.id = :id and m.usable = 1")
    int updateRelievePermissionsStatus(@Param("status") RelievePermissionsStaus status, @Param("id") Long id);
}
