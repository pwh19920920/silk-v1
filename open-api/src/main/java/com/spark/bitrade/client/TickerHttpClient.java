package com.spark.bitrade.client;

import com.google.common.collect.Maps;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 *  远程调用客户端
 *
 * @author lc
 * @since 2020-01-30
 */
@RefreshScope
@FeignClient( name = "${api.property.feixiaohao}", url = "${api.property.feixiaohao}", fallbackFactory = HystrixMarketHttpClientFallbackFactory.class)
public interface TickerHttpClient {

    /**
     * 获取ticker
     *
     * @param convert
     * @param limit
     * @return
     */
    @RequestMapping(value="/v1/ticker?convert=USDT&limit=100", headers = {HttpHeaders.USER_AGENT, "application/json"},method= RequestMethod.GET)
    List<LinkedHashMap> queryPrice();



}


@Component
@Slf4j
class HystrixMarketHttpClientFallbackFactory implements FallbackFactory<TickerHttpClient> {

    @Override
    public TickerHttpClient create(Throwable throwable) {
        return  new TickerHttpClient(){
            @Override
            public List<LinkedHashMap> queryPrice() {
                log.error(throwable.getMessage());
                RestTemplate restTemplate = new RestTemplate();
                //请求头中加上user-agent属性伪装欺骗服务器
                restTemplate.setInterceptors(Collections.singletonList(new AgentInterceptor()));
                Map map = Maps.newHashMap();
                map.put("convert","USDT");
                map.put("limit","20");
                ResponseEntity<List> response = restTemplate.getForEntity("https://fxhapi.feixiaohao.com/public/v1/ticker?convert=USDT&limit=100", List.class,map);
                return (List<LinkedHashMap>) response.getBody();
            }
		};
    }

}

