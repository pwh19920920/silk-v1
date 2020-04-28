package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.entity.MemberApiSecretDTO;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.exception.ApiException;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.services.MemberApiSecretService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Map;

/**
 * <p>ApiCommonController</p>
 *
 * @author tian.bo
 * @date 2018-12-5
 */
@Slf4j
public class ApiCommonController extends BaseController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberApiSecretService memberApiSecretService;

    /**
     * request body参数解析
     *
     * @param request
     * @return
     */
    protected Map<String, String> bodyParamterParse(HttpServletRequest request) {
        /**
         * 业务参数获取
         */
        InputStream inputStream = null;
        Reader input = null;
        Writer output = new StringWriter();
        try {
            inputStream = request.getInputStream();
            input = new InputStreamReader(inputStream);
            char[] buffer = new char[1024 * 4];
            int size = 0;
            while (-1 != (size = input.read(buffer))) {
                output.write(buffer, 0, size);
            }
            Map<String, String> params = JSON.parseObject(output.toString(), Map.class);
            inputStream.close();
            output.close();
            return params;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取AccessKeyId
     *
     * @param request
     * @return
     */
    protected String getAccessKeyId(HttpServletRequest request) {
        return request.getParameter("AccessKeyId");
    }

    /**
     * 获取用户信息
     *
     * @param request
     * @return
     */
    protected AuthMember getAuthMember(HttpServletRequest request) {
        //edit by yangch 时间： 2019.03.02 原因：添加入参校验，优化错误提示的可读性
        if (StringUtils.isEmpty(getAccessKeyId(request))) {
            throw new ApiException("500", "AccessKeyId is null");
        }

        MemberApiSecretDTO memberApiSecretDTO = memberApiSecretService.selectByAccessKey(getAccessKeyId(request));
        if (StringUtils.isEmpty(memberApiSecretDTO)) {
            log.error("invalid AccessKeyId  {}", this.getAccessKeyId(request));
            throw new ApiException("500", "invalid AccessKeyId");
        }

        return AuthMember.toAuthMember(memberService.findOne(memberApiSecretDTO.getMemberId()));

        //return AuthMember.toAuthMember(memberService.findOne(memberApiSecretService.selectByAccessKey(getAccessKeyId(request)).getMemberId()));
    }


}
