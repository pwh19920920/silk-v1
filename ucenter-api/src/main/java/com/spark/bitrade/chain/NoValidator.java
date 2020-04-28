package com.spark.bitrade.chain;

import com.spark.bitrade.constant.ValidateCodeType;
import com.spark.bitrade.exception.GeeTestException;
import lombok.extern.slf4j.Slf4j;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.08.08 13:59  
 */
@Slf4j
public class NoValidator extends AbstractValidateCode {

    public NoValidator(ValidateCodeType type){
        this.validateCodeType=type;
    }
    @Override
    protected void validate() throws GeeTestException {
      log.info("=======================无需验证直接放行=========================");
    }
}
