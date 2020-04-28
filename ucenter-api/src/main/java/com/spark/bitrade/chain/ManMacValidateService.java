package com.spark.bitrade.chain;

import com.spark.bitrade.constant.ValidateCodeType;
import com.spark.bitrade.entity.SilkDataDist;
import com.spark.bitrade.exception.GeeTestException;
import com.spark.bitrade.service.ISilkDataDistService;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.system.HttpServletUtil;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.08.12 15:13  
 */
@Service
public class ManMacValidateService {

    @Autowired
    private ISilkDataDistService silkDataDistService;

    @Resource
    private LocaleMessageSourceService localeMessageSourceService;

    /**
     * 验证
     * @throws GeeTestException
     */
    public void validate() throws GeeTestException {
        HttpServletRequest request = HttpServletUtil.getRequest();
        String thirdMark = request.getHeader("thirdMark");
        Assert.isTrue(StringUtils.hasText(thirdMark), localeMessageSourceService.getMessage("THIRD_MARK_CANT_NULL"));
        SilkDataDist silkDataDist = silkDataDistService.findByIdAndKey("SYSTEM_VALIDATE_CONFIG", "VALIDATE_TYPE");
        Assert.notNull(silkDataDist, localeMessageSourceService.getMessage("VALIDATE_TYPE_IS_NULL"));
        //验证类型 0: 无验证 1:图形验证码   2:现在用的极验证 3:网易极验证
        int dictVal = Integer.valueOf(silkDataDist.getDictVal());

        ValidateCodeType codeType = ValidateCodeType.getTypeByOrdinal(dictVal);
        Assert.notNull(codeType,localeMessageSourceService.getMessage("REMOTE_ALIYUN_FAIL"));
        AbstractValidateCode validateCode = AbstractValidateCode.getValidator();
        validateCode.chooseValdate(codeType);
    }


}
