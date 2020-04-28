package com.spark.bitrade.interceptor;

import com.spark.bitrade.config.HttpServerConfig;
import com.spark.bitrade.entity.MemberApiSecretDTO;
import com.spark.bitrade.exception.ApiException;
import com.spark.bitrade.services.MemberApiSecretService;
import com.spark.bitrade.utils.ApiSignature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>接口请求链接处理器</p>
 *  @author tian.bo
 *  @date 2018-12-5
 */
@Component
@Slf4j
public class ApiHttpRequestHandlerInterceptor implements HandlerInterceptor {

    static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
    static final ZoneId ZONE_GMT = ZoneId.of("Z");

    /**
     * Return epoch seconds
     */
    long epochNow() {
        return Instant.now().getEpochSecond();
    }

    String gmtNow() {
        return Instant.ofEpochSecond(epochNow()).atZone(ZONE_GMT).format(DT_FORMAT);
    }

    @Autowired
    private HttpServerConfig httpServerConfig;


    @Autowired
    private MemberApiSecretService memberApiSecretService;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        return this.preProcessor(httpServletRequest,httpServletResponse,o);
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }

    /**
     * 拦截处理
     * @param request
     * @param response
     * @param o
     * @return
     */
    protected boolean preProcessor(HttpServletRequest request, HttpServletResponse response, Object o){

        /**
         * 1.参数获取
         */
        Map<String,String[]> parameterMap = request.getParameterMap();
        String accessKeyId = parameterMap.get("AccessKeyId")[0];
        String signatureMethod = parameterMap.get("SignatureMethod")[0];
        String signatureVersion = parameterMap.get("SignatureVersion")[0];
        String timestamp = parameterMap.get("Timestamp")[0];
        String signature = parameterMap.get("Signature")[0];
        //域名
        String apiUrl = httpServerConfig.getApiUrl();
        //请求路径
        String uri = request.getRequestURI();
        //请求方法
        String method = request.getMethod();

        /**
         * 2.请求头校验
         */
        String userAgent = request.getHeader("User-Agent");
        String contentType = request.getHeader("Content-Type");
        if("POST".equals(StringUtils.upperCase(method))){
            if(!StringUtils.equals("application/json;charset=utf-8",contentType)){
                throw new ApiException("bad-request","bad-request");
            }
        }else{
            if(!StringUtils.equals("application/x-www-form-urlencoded",contentType)){
                throw new ApiException("bad-request","bad-request");
            }
        }
        if(!StringUtils.equals("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36",userAgent)){
            throw new ApiException("bad-request","bad-request");
        }

        /**
         * 3.参数校验
         */
        if (StringUtils.isEmpty(accessKeyId)){
            throw new ApiException("invalid-parameter","AccessKeyId is not null.");
        }

        if (StringUtils.isEmpty(signatureMethod)){
            throw new ApiException("invalid-parameter","SignatureMethod is not null.");
        }

        if (!StringUtils.equals("HmacSHA256",signatureMethod)){
            throw new ApiException("invalid-parameter","SignatureMethod invalid.");
        }

        if (StringUtils.isEmpty(signatureVersion)){
            throw new ApiException("invalid-parameter","SignatureVersion is not null.");
        }

        if (!StringUtils.equals(httpServerConfig.getApiVersion(),signatureVersion)){
            throw new ApiException("invalid-parameter","SignatureVersion invalid.");
        }

        if (StringUtils.isEmpty(timestamp)){
            throw new ApiException("invalid-parameter","Timestamp is not null.");
        }

        if (StringUtils.isEmpty(signature)){
            throw new ApiException("invalid-parameter","Signature is not null.");
        }

      /*  Instant requestInstant = Instant.parse(timestamp);
        Instant currentInstant = Instant.now();
        int diff = currentInstant.compareTo(requestInstant);*/


        /**
         * 4.访问秘钥(accesskey)过期校验
         */
        //根据accesskey查询用户访问api信息
        MemberApiSecretDTO dto = memberApiSecretService.selectByAccessKey(accessKeyId);
        if(null == dto){
            throw new ApiException("invalid-parameter","accessKeyId invalid");
        }
        //验证访问秘钥(accesskey)过期时间
        Date expires =  dto.getExpires();
        //当前时间
        Date currentDate = new Date();
        if(currentDate.compareTo(expires) > 0){
            //过期
            throw new ApiException("invalid-parameter","accessKeyId expired");
        }

        /**
         * 5.参数签名校验
         */
        ApiSignature apiSignature = new ApiSignature();
        //签名秘钥
        String appSecretKey = dto.getSecretKey();
        //签名参数
        Map<String, String> params = new HashMap<>();
        for(Map.Entry<String,String[]> entry : parameterMap.entrySet()){
           params.put(entry.getKey(),entry.getValue()[0]);
        }
        params.remove("Signature");
        //签名验证
        if(!apiSignature.verifySignature(appSecretKey,method,apiUrl,uri,params,signature)){
            throw new ApiException("api-signature-not-valid","api-signature-not-valid");
        }
        return true;
    }

}
