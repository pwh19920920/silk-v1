package com.spark.bitrade.dao;

import com.spark.bitrade.entity.BusinessDiscountRule;
import org.springframework.data.jpa.repository.JpaRepository;

/**
  * 
  * @author tansitao
  * @time 2018/9/6 17:15 
  */
public interface BusinessDiscountRuleDao
        extends JpaRepository<BusinessDiscountRule, Long> {

}
