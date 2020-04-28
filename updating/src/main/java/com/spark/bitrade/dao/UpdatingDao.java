package com.spark.bitrade.dao;

import com.spark.bitrade.entity.Updating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Desc:
 * Author: yangch
 * Version: 1.0
 * Create Date Time: 2018-05-29 22:24:00
 * Update Date Time:
 *
 * @see
 */

@Repository
public interface UpdatingDao extends JpaRepository<Updating, Integer> {

    Integer countByFlagEquals(int flag);

    Updating findFirstByFlagOrderByIdDesc(int flag);
}
