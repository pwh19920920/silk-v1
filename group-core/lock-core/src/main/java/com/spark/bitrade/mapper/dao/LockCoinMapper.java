package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.dto.LockCoinActivitieProjectDto;
import com.spark.bitrade.entity.LockCoinActivitieProject;
import com.spark.bitrade.entity.LockCoinActivitieSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 查询活动配置Mapper
 * @author tansitao
 * @time 2018/7/2 9:05 
 */
@Mapper
public interface LockCoinMapper {

	//查询锁仓活动配置
	LockCoinActivitieSetting findByIdAndTime(@Param("id") long id);

	//查询所有可用的锁仓方案配置
	List<LockCoinActivitieProject> findAllEnableProject(@Param("type") String type);

	//add by tansitao 时间： 2018/11/20 原因：通过币种查询所有生效中的活动
	List<LockCoinActivitieProject> findAllEnableProjectByUnit(@Param("unit") String unit);

	//查询热门活动列表
	List<LockCoinActivitieProjectDto> findAllHotProject(@Param("type") String type);

    //查询精品活动列表
	List<LockCoinActivitieProjectDto> findAllTopProject(@Param("type") String type);
	@Select("select name,brief_description from lock_coin_activitie_project_international where project_id=#{id} and international_type=#{language} limit 1")
	Map<String,String> findNameByIdAndLanguage(@Param("id") Long id, @Param("language") Integer integer);
	@Select("select name from lock_coin_activitie_setting_international where setting_id=#{id} and international_type=#{language} limit 1")
	String findSettingNameByidAndLanguage(@Param("id") Long id, @Param("language") Integer integer);
}
