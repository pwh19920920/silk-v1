package com.spark.bitrade.controller.activitie;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.LockSettingStatus;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.Admin;
import com.spark.bitrade.entity.LockCoinActivitieSetting;
import com.spark.bitrade.entity.QLockCoinActivitieSetting;
import com.spark.bitrade.service.LockCoinActivitieSettingService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import com.spark.bitrade.util.PriceUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;

import static org.springframework.util.Assert.notNull;

/***
 * 锁仓活动方案配置
 * @author yangch
 * @time 2018.06.12 16:49
 */

@RestController
@RequestMapping("lockCoinActivitie")
public class LockCoinActivitieSettingController extends BaseAdminController {
    @Autowired
    private LockCoinActivitieSettingService service;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 分页查询
     * @author tansitao
     * @time 2018/6/13 10:54 
     */
    @RequiresPermissions("lock:lockCoinActivitie-page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.PROMOTION ,operation = "活动方案配置分页查询")
    public MessageResult page(PageModel pageModel,String name){
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (StringUtils.isNotBlank(name)){
            booleanExpressions.add(QLockCoinActivitieSetting.lockCoinActivitieSetting.name.like("%"+name+"%"));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<LockCoinActivitieSetting> pageData= service.findAll(predicate ,pageModel.getPageable());
        return success(pageData);
    }

    /**
      * 添加锁仓活动配置
      * @author tansitao
      * @time 2018/6/13 9:42 
      */
    @PostMapping("create")
    @RequiresPermissions("lock:lockCoinActivitie-create")
    @AccessLog(module = AdminModule.PROMOTION ,operation = "添加活动方案配置")
    public MessageResult create(@Valid LockCoinActivitieSetting lockCoinActivitieSetting) {
        service.save(lockCoinActivitieSetting);
        return success();
    }

    /**
      * 修改锁仓充值
      * @author tansitao
      * @time 2018/6/13 9:42 
      */
    @RequiresPermissions("lock:lockCoinActivitie-update")
    @PostMapping("update")
    @AccessLog(module = AdminModule.PROMOTION ,operation = "修改活动方案配置")
    public MessageResult update(@Valid LockCoinActivitieSetting lockCoinActivitieSetting, @SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin) {
        LockCoinActivitieSetting ccActivitieSetting = service.findOne(lockCoinActivitieSetting.getId());
        Assert.notNull(ccActivitieSetting, "该锁仓配置不存在");
        //edit by fumy. date:2018.12.04 reason:取消生效状态不能修改配置的判断
//        Assert.isTrue(ccActivitieSetting.getStatus() != LockSettingStatus.VALID,"生效中的锁仓配置不允许修改");
        lockCoinActivitieSetting.setAdminId(admin.getId());
        lockCoinActivitieSetting.setCreateTime(ccActivitieSetting.getCreateTime());
        lockCoinActivitieSetting.setActivitieId(ccActivitieSetting.getActivitieId());
        service.save(lockCoinActivitieSetting);
        return success();
    }


    @PostMapping("detail")
    @RequiresPermissions("lock:lockCoinActivitie-detail")
    @AccessLog(module = AdminModule.PROMOTION ,operation = "活动方案配置详情")
    public MessageResult detail(@RequestParam("id") Long id){
        notNull(id, "validate id!");
        return success(service.findOne(id));
    }

    @PostMapping("coinPrice")
    public MessageResult getCoinPrice(@RequestParam("unit")String unit){
        //获取最新价格
        PriceUtil priceUtil = new PriceUtil();
        //获取锁仓币种人民币价格
        BigDecimal coinCnyPrice = priceUtil.getCoinCnyPrice(restTemplate,unit);
        return  success(coinCnyPrice);
    }
}
