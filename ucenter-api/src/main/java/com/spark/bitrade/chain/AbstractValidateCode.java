package com.spark.bitrade.chain;

import com.spark.bitrade.constant.ValidateCodeType;
import com.spark.bitrade.exception.GeeTestException;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.Setter;
import org.springframework.data.redis.core.RedisTemplate;

/**
 *  
 *    责任链模式
 *  @author liaoqinghui  
 *  @time 2019.08.06 09:43  
 */
@Setter
public abstract class AbstractValidateCode {

    protected ValidateCodeType validateCodeType;

    protected AbstractValidateCode nextAbstractValidateCode;

    protected RedisTemplate redisTemplate;

    protected LocaleMessageSourceService localeMessageSourceService;

    /**
     * 选择并执行验证的方法
     *
     * @param type
     */
    public void chooseValdate(ValidateCodeType type) throws GeeTestException {
        this.redisTemplate = SpringContextUtil.getBean(RedisTemplate.class);
        this.localeMessageSourceService = SpringContextUtil.getBean(LocaleMessageSourceService.class);
        if (type.equals(validateCodeType)) {
            validate();
        }
        if (nextAbstractValidateCode != null) {
            nextAbstractValidateCode.chooseValdate(type);
        }

    }


    public static AbstractValidateCode getValidator() {
        AbstractValidateCode imageValidator = new ImageCodeValdator(ValidateCodeType.IMAGE_CODE);
        AbstractValidateCode oldValidator = new OldGeetestValidator(ValidateCodeType.OLD_GEETEST);
        AbstractValidateCode wyValidator = new WyGeetestValidator(ValidateCodeType.WANGYI_GEETEST);
        AbstractValidateCode noValidator = new NoValidator(ValidateCodeType.NO_VALIDATE);
        imageValidator.setNextAbstractValidateCode(oldValidator);
        oldValidator.setNextAbstractValidateCode(wyValidator);
        wyValidator.setNextAbstractValidateCode(noValidator);
        return imageValidator;
    }


    /**
     * 验证方法
     *
     * @throws GeeTestException
     */
    abstract protected void validate() throws GeeTestException;

}
