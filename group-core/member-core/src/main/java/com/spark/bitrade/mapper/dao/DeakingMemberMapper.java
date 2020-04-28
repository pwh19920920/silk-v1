package com.spark.bitrade.mapper.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
  * 迪肯内部员工信息
  * @author tansitao
  * @time 2018/7/2 11:09 
  */
@Mapper
public interface DeakingMemberMapper {

	//查询员工信息
	Long findOneBymemberId(@Param("memberId") long memberId);

}
