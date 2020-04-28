package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.TestReadWrite;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/***
 * 读写分类测试mapper接口
  *
 * @author yangch
 * @time 2018.06.20 11:58
 */
@Mapper
public interface TestReadWriteMapper {

	@Insert("insert test_read_write(id,user_name) values(#{id},#{userName})")
	void insert(TestReadWrite u);
	
	//@Select("select id,user_name from sys_user where id=#{id} ")
	@Select("select * from test_read_write where id=#{id} ")
	TestReadWrite findById(@Param("id") String id);
	
	//注：方法名和要UserMapper.xml中的id一致
	List<TestReadWrite> query(@Param("userName") String userName);
	
}
