package com.spark.bitrade.controller;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.RateLimitExceededException;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/***
 * 统一错误的处理
  *
 * @author yangch
 * @time 2018.07.09 11:40
 */
@RestController
public class ErrorHandlerController implements ErrorController {
    @Autowired
    private LocaleMessageSourceService msService;

    /**
     * 出异常后进入该方法，交由下面的方法处理
     */
    @Override
    public String getErrorPath() {
        return "/error";
    }


    @RequestMapping(value = "/error")
    public MessageResult error2(HttpServletRequest request) {
        int code = 500;
        String message=msService.getMessage("UNKNOWN_ERROR");

        if(request!=null) {

            Object objCode = request.getAttribute("javax.servlet.error.status_code");
            Object objMessage = request.getAttribute("javax.servlet.error.message");
            Object objException = request.getAttribute("javax.servlet.error.exception");
            if(!StringUtils.isEmpty(objCode)) {
                code = Integer.parseInt(objCode.toString());
            }

            //异常类型处理
            if(objException instanceof RateLimitExceededException || code==429){
                message = msService.getMessage("FREQUENT_REQUESTS"); //限流提示
            } else {
                if(!StringUtils.isEmpty(objMessage)) {
                    message = objMessage.toString();
                }
            }
        }

        return MessageResult.error(code, message);
    }

/*    @RequestMapping(value = "/error2")
    public MessageResult error(HttpServletRequest request) {
        int code =Integer.parseInt(request.getAttribute("javax.servlet.error.status_code").toString());
        String message = request.getAttribute("javax.servlet.error.message").toString();
        return MessageResult.error(code, message);
    }*/
}
