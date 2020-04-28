package com.spark.bitrade.chain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.config.WyGeetestConfig;
import com.spark.bitrade.constant.ValidateCodeType;
import com.spark.bitrade.exception.GeeTestException;
import com.spark.bitrade.system.HttpConnectionUtils;
import com.spark.bitrade.system.HttpServletUtil;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.08.07 15:09  
 */
@Slf4j
public class WyGeetestValidator extends AbstractValidateCode {

    public WyGeetestValidator(ValidateCodeType type) {
        this.validateCodeType = type;
    }


    private static final String REQ_VALIDATE = "NECaptchaValidate";


    @Override
    protected void validate() throws GeeTestException {
        log.info("===========================网易极验证开始==============================");
        //并没用什么用的参数但是网易需要
        WyGeetestConfig config = SpringContextUtil.getBean(WyGeetestConfig.class);
        Map<String, String> params = buildMap(config);
        log.info("params:{}",params);
        try {
            String response = HttpConnectionUtils.readContentFromPost(config.getValidateUrl(), params);
            if (!StringUtils.isEmpty(response)) {
                JSONObject obj = JSON.parseObject(response);
                String result = obj.getString("result");
                if (!"true".equals(result)) {
                    throw new GeeTestException(localeMessageSourceService.getMessage("ALI_VALIDATE_FAIL"));
                }
            }
        } catch (IOException e) {
            log.error("=================IOE异常================", e);
        } catch (GeeTestException e) {
            throw e;
        }
        log.info("===========================网易极验证成功==============================");
    }


    private Map<String, String> buildMap(WyGeetestConfig config) {
        HttpServletRequest request = HttpServletUtil.getRequest();
        String validate = request.getParameter(REQ_VALIDATE);
        Assert.hasText(validate,localeMessageSourceService.getMessage("ALI_VALIDATE_FAIL"));
        Map<String, String> params = new HashMap<String, String>();
        params.put("captchaId", config.getCaptchaId());
        params.put("validate", validate);
        params.put("user", "{'id':'123456'}");
        // 公共参数
        params.put("secretId", config.getSecretId());
        params.put("version", config.getVersion());
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(ThreadLocalRandom.current().nextInt()));
        // 计算请求参数签名信息
        String signature = sign(config.getSecretKey(), params);
        params.put("signature", signature);

        return params;
    }


    /**
     * 生成签名信息
     *
     * @param secretKey 验证码私钥
     * @param params    接口请求参数名和参数值map，不包括signature参数名
     * @return
     */
    private String sign(String secretKey, Map<String, String> params) {
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        StringBuffer sb = new StringBuffer();
        for (String key : keys) {
            sb.append(key).append(params.get(key));
        }
        sb.append(secretKey);
        try {
            return DigestUtils.md5Hex(sb.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // 一般编码都支持的。。
            log.error("=============网易加密失败============");
        }
        return null;
    }

}

















