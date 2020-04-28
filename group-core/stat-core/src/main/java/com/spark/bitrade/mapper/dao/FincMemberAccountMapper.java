package com.spark.bitrade.mapper.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import com.spark.bitrade.entity.FincMemberAccount;

/**
 * @author Zhang Yanjun
 * @time 2018.12.18 11:47
 */
@Mapper
public interface FincMemberAccountMapper {

    int insert(@Param("pojo") FincMemberAccount pojo);

    int insertList(@Param("pojos") List< FincMemberAccount> pojo);

    List<FincMemberAccount> select(@Param("pojo") FincMemberAccount pojo);

    int update(@Param("pojo") FincMemberAccount pojo);

    /**
     * 根据类型查找
     * @author Zhang Yanjun
     * @time 2018.12.18 11:46
     * @param memberType
     */
    List<FincMemberAccount> findByType(@Param("memberType") int memberType);

}
