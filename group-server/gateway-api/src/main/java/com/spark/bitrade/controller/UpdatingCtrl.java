package com.spark.bitrade.controller;

import com.spark.bitrade.entity.Updating;
import com.spark.bitrade.service.UpdatingService;
import com.spark.bitrade.filter.UpdatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Desc:
 * Author: yangch
 * Version: 1.0
 * Create Date Time: 2018-05-29 23:02:00
 * Update Date Time:
 *
 * @see
 */

@RestController
@RequestMapping("/gateway")
public class UpdatingCtrl {
    @Autowired
    private UpdatingFilter updatingFilter;
    @Autowired
    private UpdatingService updatingService;

    @RequestMapping("/stat")
    @ResponseBody
    public String isUpdating(){
        if(updatingService.isUpdating()){
            updatingFilter.setIsUpdating(true);
            return  "{\"flag\":1}";
        }else {
            updatingFilter.setIsUpdating(false);
            return "{\"flag\":0}";
        }
    }

    // /gateway/nowStat
    @RequestMapping("/nowStat")
    @ResponseBody
    public String flushUpdatingStatus(){
        if(updatingService.flushUpdatingStatus()){
            updatingFilter.setIsUpdating(true);
            return  "{\"flag\":1}";
        }else {
            updatingFilter.setIsUpdating(false);
            return "{\"flag\":0}";
        }
    }

    @RequestMapping(value = "/api",produces="application/json")
    @ResponseBody
    public String isUpdatingApi(){
        if(updatingService.isUpdating()){
            return  "{\"data\":\"\",\"code\":999,\"message\":\"系统正在升级...\"}";
        }else {
            return "{\"data\":\"\",\"code\":999,\"message\":\"系统升级接口...\"}";
        }
    }

//    @RequestMapping(value = "/**")
//    @ResponseBody
//    public Object content(HttpServletRequest req,HttpServletResponse response){
//        //System.out.println("url:"+req.getRequestURI());
//        String url = req.getRequestURI();
//        if(url.equalsIgnoreCase("/")) {
//            if (updatingService.isUpdating()) {
//                Updating entity = updatingService.queryByIdDesc();
//                if (entity != null) {
//                    String format = "yyyy-MM-dd HH:mm:ss";
//                    SimpleDateFormat sdf = new SimpleDateFormat(format);
//
//                    //更换开始时间(${starttime})和结束时间${endtime}
//                    return entity.getPagetemp().replace("${starttime}", sdf.format(entity.getStarttime())).replace("${endtime}", sdf.format(entity.getEndtime()));
//                } else {
//                    return "Welcome updating page.";
//                }
//            } else {
//                return "Welcome updating page.";
//            }
//        } else {
//            Map map = new HashMap();
//            map.put("data","");
//            map.put("code",999);
//
//            if(updatingService.isUpdating()){
//                map.put("message","系统正在升级...");
//                return  map;
//                //return  "{\"data\":\"\",\"code\":999,\"message\":\"系统正在升级...\"}";
//            }else {
//                //return "{\"data\":\"\",\"code\":999,\"message\":\"系统升级接口...\"}";
//                map.put("message","系统升级接口...");
//                return  map;
//            }
//        }
//    }
}
