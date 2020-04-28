package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.H5GamePayRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

/**
 * H5GameRecordMapper
 *
 * @author archx
 * @time 2019/4/25 11:44
 */
@Mapper
public interface H5GameRecordMapper extends BaseMapper<H5GamePayRecord> {

    @Update("update h5game_pay_record set state = #{state}, update_time = #{date} where id = #{id}")
    int updateStateById(@Param("id") long id, @Param("state") int state, @Param("date") Date date);

    @Update("update h5game_pay_record set ref_id = #{refId}, update_time = #{date} where id = #{id}")
    int updateRefIdById(@Param("id") long id, @Param("refId") long refId, @Param("date") Date date);

    @Select("select member_id from pay_wallet_plat_member_bind  where role_id = #{roleId} and app_id = #{appId} order by create_time desc limit 1")
    Long findPrimaryMemberIdByRolIdAndAppId(@Param("roleId") Long roleId, @Param("appId") Long appId);
}
