package com.spark.bitrade.dao;

import com.spark.bitrade.entity.AppRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author rongyu
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/2416:18
 */
public interface AppRevisionDao extends JpaRepository<AppRevision, Integer> {
    //edit by tansitao 时间： 2018/5/5 原因：修改app版本更新
    @Query(value="SELECT * FROM app_revision  WHERE platform = :platform ORDER BY version DESC LIMIT 1",nativeQuery = true)
    AppRevision findAppRevisionByPlatformOrderByIdDesc(@Param("platform") Integer platform);

}
