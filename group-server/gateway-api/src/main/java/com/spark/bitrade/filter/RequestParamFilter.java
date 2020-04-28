//package com.spark.bitrade.filter;
//
//import com.alibaba.druid.support.json.JSONParser;
//import com.alibaba.fastjson.JSON;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.util.JSONPObject;
//import com.netflix.zuul.ZuulFilter;
//import com.netflix.zuul.context.RequestContext;
//import com.spark.bitrade.entity.Member;
//import com.spark.bitrade.entity.transform.AuthMember;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpSession;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///***
// * 
// * @author yangch
// * @time 2018.07.07 10:15
// */
//@Slf4j
//@Component
//public class RequestParamFilter extends ZuulFilter {
//    @Override
//    public Object run() {
//        //更改请求参数的测试
//        RequestContext ctx = RequestContext.getCurrentContext();
//        HttpServletRequest request = ctx.getRequest();
//        // 一定要get一下,下面这行代码才能取到值... [注1]
//        request.getParameterMap();
//        Map<String, List<String>> requestQueryParams = ctx.getRequestQueryParams();
//
//        if (requestQueryParams==null) {
//            requestQueryParams=new HashMap<>();
//        }
//
//        //将要新增的参数添加进去,被调用的微服务可以直接 去取,就想普通的一样,框架会直接注入进去
//        ArrayList<String> arrayList = new ArrayList<>();
//        //arrayList.add("1");
//        Member member = new Member();
//        member.setId(71639L);
//        member.setUsername("yang");
//        member.setRealName("yangch");
//
//        AuthMember authMember = AuthMember.toAuthMember(member);
//        /*ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            String jsonString = objectMapper.writeValueAsString(authMember);
//            arrayList.add(jsonString);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }*/
//        arrayList.add(JSON.toJSONString(authMember));
//
//
//        requestQueryParams.put("authMember", arrayList);
//
//        ctx.setRequestQueryParams(requestQueryParams);
//
//        return null;
//    }
//
//    @Override
//    public boolean shouldFilter() {
//        return true;// 是否执行该过滤器，此处为true，说明需要过滤
//    }
//
//    @Override
//    public int filterOrder() {
//        return -3;// 优先级为0，数字越大，优先级越低
//    }
//
//    @Override
//    public String filterType() {
//        return "pre";// 前置过滤器
//    }
//}
