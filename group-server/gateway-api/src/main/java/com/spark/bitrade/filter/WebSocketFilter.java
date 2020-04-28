package com.spark.bitrade.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/***
 * 
 * @author yangch
 * @time 2018.07.07 16:58
 */
//无效果？？websocket任然走nginx
//@Component
//public class WebSocketFilter extends ZuulFilter {
//    @Override
//    public String filterType() {
//        return "pre";
//    }
//    @Override
//    public int filterOrder() {
//        return 0;
//    }
//    @Override
//    public boolean shouldFilter() {
//        return true;
//    }
//    @Override
//    public Object run() {
//        RequestContext context = RequestContext.getCurrentContext();
//        HttpServletRequest request = context.getRequest();
//        String upgradeHeader = request.getHeader("Upgrade");
//        if (null == upgradeHeader) {
//            upgradeHeader = request.getHeader("upgrade");
//        }
//        if (null != upgradeHeader && "websocket".equalsIgnoreCase(upgradeHeader)) {
//            context.addZuulRequestHeader("connection", "Upgrade");
//        }
//        return null;
//    }
//}
