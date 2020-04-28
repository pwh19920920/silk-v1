package com.spark.bitrade.controller.org;

import com.spark.bitrade.constant.PartnerLevle;
import com.spark.bitrade.constant.PartnerStaus;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.controller.BaseController;
import com.spark.bitrade.entity.DimArea;
import com.spark.bitrade.service.GyDmcodeService;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 组织架构控制器
 * @author tansitao
 * @time 2018/5/30 18:21 
 */
@RestController
@RequestMapping("/org")
public class OrgController extends BaseController {
    @Autowired
    private GyDmcodeService gyDmcodeService;
    @Autowired
    private RedisTemplate redisTemplate ;


    /**
     * 获取区域组织架构信息
     * @author tansitao
     * @time 2018/5/28 17:17 
     */
//    @RequiresPermissions("area:allArea")
    @PostMapping("allArea")
    public MessageResult allArea(@RequestParam(value = "areaId") String areaId) {
        //从redis获取数据
        List<DimArea> areaList = gyDmcodeService.findAllByFatherId(areaId);
        return success(areaList);
    }


   @PostMapping("partnerStatus")
    public MessageResult partnerStatus() {
        return success(PartnerLevle.values());
    }

}
