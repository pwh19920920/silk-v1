package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.LockUttMember;
import com.spark.bitrade.vo.LockBttcImportVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 导入用户表 Mapper 接口
 * </p>
 *
 * @author qiliao
 * @since 2019-08-15
 */
public interface LockUttMemberMapper extends BaseMapper<LockUttMember> {

    List<LockBttcImportVo> findBttcImportList(@Param("memberId") Long memberId,@Param("tableName") String tableName);

    /**s
     * 查询所有导入表
     * @return
     */
    @Select("SELECT s.temp_table FROM lock_bttc_import_statistics s WHERE s.valid_amount!=0 AND s.`status`=1 ")
    List<String> findAllImportTable();

    /**
     * 表是否存在
     * @param tableName
     * @return
     */
    @Select("SHOW TABLES LIKE #{tableName} ")
    String tableExist(@Param("tableName")String tableName);
}
