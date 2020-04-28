package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.MemberPermissionsRelieveTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 权限自动解锁任务mapper
 * @author tansitao
 * @time 2018/11/28 11:50 
 */
@Mapper
public interface MemberPermissionRelieveTaskMapper {
    //根据用户id和类型查询权限自动解锁任务
    MemberPermissionsRelieveTask queryByMemberAndType(@Param("memberId") Long memberId,@Param("type") int type);

    //查询所有可解冻的权限自动解锁任务
    List<MemberPermissionsRelieveTask> queryAllTask();
    //根据用户id和类型查询权限自动解锁任务集合
    List<MemberPermissionsRelieveTask> queryListByMemberAndType(@Param("memberId") Long memberId,@Param("typeOne") int typeOne,@Param("typeTwo") int typeTwo);
}
