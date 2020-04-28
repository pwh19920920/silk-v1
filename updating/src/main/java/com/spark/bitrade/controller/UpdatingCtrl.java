package com.spark.bitrade.controller;

import com.spark.bitrade.constant.Platform;
import com.spark.bitrade.dao.AppRevisionDao;
import com.spark.bitrade.entity.Updating;
import com.spark.bitrade.service.IAnnouncementService;
import com.spark.bitrade.service.UpdatingService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.entity.AppRevision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
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
public class UpdatingCtrl {
    @Autowired
    private UpdatingService updatingService;
    @Autowired
    private IAnnouncementService iAnnouncementService;

    @RequestMapping("/stat")
    @ResponseBody
    public String isUpdating() {
        if (updatingService.isUpdating()) {
            return "{\"flag\":1}";
        } else {
            return "{\"flag\":0}";
        }
    }

    @RequestMapping("/nowStat")
    @ResponseBody
    public String flushUpdatingStatus() {
        if (updatingService.flushUpdatingStatus()) {
            return "{\"flag\":1}";
        } else {
            return "{\"flag\":0}";
        }
    }

    @RequestMapping(value = "/api", produces = "application/json")
    @ResponseBody
    public String isUpdatingApi() {
        if (updatingService.isUpdating()) {
            return "{\"data\":\"\",\"code\":999,\"message\":\"系统正在升级...\"}";
        } else {
            return "{\"data\":\"\",\"code\":999,\"message\":\"系统升级接口...\"}";
        }
    }

    @RequestMapping(value = "/**")
    @ResponseBody
    public Object content(HttpServletRequest req, HttpServletResponse response) {
        //System.out.println("url:"+req.getRequestURI());
        String url = req.getRequestURI();
        if (url.equalsIgnoreCase("/")) {
            if (updatingService.isUpdating()) {
                Updating entity = updatingService.queryByIdDesc();
                if (entity != null) {
                    String format = "yyyy-MM-dd HH:mm:ss";
                    SimpleDateFormat sdf = new SimpleDateFormat(format);

                    //更换开始时间(${starttime})和结束时间${endtime}
                    return entity.getPagetemp().replace("${starttime}", sdf.format(entity.getStarttime())).replace("${endtime}", sdf.format(entity.getEndtime()));
                } else {
                    return "Welcome updating page.";
                }
            } else {
                return "Welcome updating page.";
            }
        } else {
            Map map = new HashMap();
            map.put("data", "");
            map.put("code", 999);

            if (updatingService.isUpdating()) {
                map.put("message", "系统正在升级...");
                return map;
                //return  "{\"data\":\"\",\"code\":999,\"message\":\"系统正在升级...\"}";
            } else {
                //return "{\"data\":\"\",\"code\":999,\"message\":\"系统升级接口...\"}";
                map.put("message", "系统升级接口...");
                return map;
            }
        }
    }

    /**
     *  * 
     *  * @author tansitao
     *  * @time 2018/11/6 14:04 
     *  
     */
    @RequestMapping("/getLatelyAnnounce")
    public MessageResult getAnnouncement() {
        MessageResult rusult = iAnnouncementService.getAnnouncement();
        return rusult;
    }


    /**
     * 版本更新
     *
     * @param platfrom
     * @return MessageResult
     * @author zhangYanjun
     * @time 2019.09.02 10:04
     */
    @RequestMapping(value = "/appVersion/{platfrom}")
    @ResponseBody
    public Map<String,Object> getAppVersion(@PathVariable("platfrom") Integer platfrom) {
        Assert.notNull(platfrom, "获取版本失败,无平台类型");

        Assert.isTrue(platfrom>=0,"无效的平台类型");
        Platform test = Platform.fromOrdinal(platfrom);
        Assert.notNull(test, "获取版本失败,无平台类型");
        AppRevision appRevision = updatingService.findAppRevisionByPlatformOrderByIdDesc(test);
        Assert.notNull(appRevision, "获取版本失败,未找到该版本");

        Map map = new HashMap();
        map.put("version", appRevision.getVersion());
        map.put("url", appRevision.getDownloadUrl());
        map.put("information", appRevision.getRemark());
        map.put("publishTime", appRevision.getPublishTime());
        if (appRevision.getLowestVersion() == null) {
            appRevision.setLowestVersion("0");
        }
        if (Integer.valueOf(appRevision.getLowestVersion().replace(".", "")) > Integer.valueOf(appRevision.getVersion().replace(".", ""))) {
            appRevision.setLowestVersion(appRevision.getVersion());
        }
        map.put("lowestVersion", appRevision.getLowestVersion());

        Map<String,Object> stringObjectMap = new HashMap<>();

        stringObjectMap.put("success",Boolean.TRUE);
        stringObjectMap.put("code",0);
        stringObjectMap.put("message","");
        stringObjectMap.put("data",map);
        return stringObjectMap;
    }
}
