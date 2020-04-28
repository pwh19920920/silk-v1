package com.spark.bitrade.chain;

import com.spark.bitrade.constant.ValidateCodeType;
import com.spark.bitrade.system.HttpServletUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;

/**
 *  
 *   图形验证码处理器
 *  @author liaoqinghui  
 *  @time 2019.08.06 09:59  
 */
@Slf4j
public class ImageCodeValdator extends AbstractValidateCode {

    public static final String VALIDATE_KEY="validateImage";

    public static final String TIME_KEY="timeKey";
    /**
     * 构造方法
     *
     * @param type
     */
    public ImageCodeValdator(ValidateCodeType type) {
        this.validateCodeType = type;
    }

    @Override
    protected void validate() {
        log.info("=================================图形验证码开始=================================");
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        //验证图形验证码
        HttpServletRequest request = HttpServletUtil.getRequest();
        //时间戳key
        String timeKey = request.getParameter(TIME_KEY);
        Assert.hasText(timeKey, localeMessageSourceService.getMessage("IMAGE_CODE_FAIL"));
        //前端传入的验证code
        String validateKey = request.getParameter(VALIDATE_KEY);
        Assert.hasText(validateKey, localeMessageSourceService.getMessage("IMAGE_CODE_FAIL"));
        log.info("timeKey:{},validateKey:{}",timeKey,validateKey);
        //之前存入redis的验证code
        String validate = valueOperations.get(VALIDATE_KEY+timeKey);
        //验证
        Assert.hasText(validate, localeMessageSourceService.getMessage("IMAGE_CODE_FAIL"));

        Assert.isTrue(validateKey.trim().equals(validate.trim()), localeMessageSourceService.getMessage("IMAGE_CODE_IS_CORRECT"));

        valueOperations.getOperations().delete(VALIDATE_KEY+timeKey);
        log.info("=================================图形验证码验证成功=================================");
    }


}














