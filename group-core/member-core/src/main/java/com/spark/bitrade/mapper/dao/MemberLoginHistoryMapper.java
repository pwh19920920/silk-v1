package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.MemberLoginHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
  *会员登录历史Mapper
  * @author tansitao
  * @time 2018/7/10 13:50 
  */
@Mapper
public interface MemberLoginHistoryMapper {


	//分页查询会员登录历史
	List<MemberLoginHistory> findloginHistory(@Param("memberId") long memberId);
	
}
