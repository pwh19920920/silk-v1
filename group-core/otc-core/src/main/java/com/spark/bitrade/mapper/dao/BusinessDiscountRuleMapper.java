package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.BusinessDiscountRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
  * 商家折扣规则dao
  * @author tansitao
  * @time 2018/9/3 14:25 
  */

@Mapper
public interface BusinessDiscountRuleMapper {

    @Select("SELECT b.member_id FROM business_discount_rule b WHERE  b.enable=1 group by b.member_id")
    List<Long> findMemberIds();

    @Select("SELECT * FROM business_discount_rule b WHERE  b.enable=1")
    List<BusinessDiscountRule> findAll();

    @Select("SELECT * FROM business_discount_rule b WHERE  b.enable=1 and b.member_id=#{memberId}")
    List<BusinessDiscountRule> findAllByMemberId(@Param("memberId") long memberId);
}
