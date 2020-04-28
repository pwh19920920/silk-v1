package com.spark.bitrade.system;

import com.netflix.zuul.context.RequestContext;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.route.ZuulFallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


/***
  * 服务回退的处理（进行统一提示），如负载均衡失败（服务实例不存在）、服务请求（等待）超时等
 * @author yangch
 * @time 2018.07.09 16:16
 */

@Component
public class FallBackHandler implements ZuulFallbackProvider {
    @Autowired
    private LocaleMessageSourceService msService;

    @Override
    public String getRoute() {
        //代表所有的路由都适配该设置
        return "*";
    }

    @Override
    public ClientHttpResponse fallbackResponse() {
        return new ClientHttpResponse() {
            @Override
            public HttpStatus getStatusCode() throws IOException {
                return HttpStatus.OK;
            }

            @Override
            public int getRawStatusCode() throws IOException {
                return 200;
            }

            @Override
            public String getStatusText() throws IOException {
                return "OK";
            }

            @Override
            public void close() {

            }

            @Override
            public InputStream getBody() throws IOException {
                //服务请求失败的处理，如负载均衡失败（服务实例不存在）、服务请求（等待）超时等
                //RequestContext ctx = RequestContext.getCurrentContext();
                //int code = ctx.getResponseStatusCode();
                //String rateLimitExceeded = ctx.getOrDefault("rateLimitExceeded","").toString(); 备注：获取不到其他信息

                MessageResult result = MessageResult.error(500,msService.getMessage("SERVICE_IS_BUSY")); //可以拦截 负载失败、超时的错误（超过限流次数 将报错，由统一错误来处理）
                return new ByteArrayInputStream(result.toString().getBytes());
            }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
            }
        };
    }
}
