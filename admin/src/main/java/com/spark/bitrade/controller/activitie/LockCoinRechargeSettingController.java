package com.spark.bitrade.controller.activitie;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.LockCoinActivitieSettingService;
import com.spark.bitrade.service.LockCoinRechargeSettingService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.Assert.notNull;

/***
 * 锁仓充值配置

 * @author yangch
 * @time 2018.06.12 16:09
 */

@RestController
@RequestMapping("lockCoinRecharge")
public class LockCoinRechargeSettingController extends BaseAdminController {
    @Autowired
    private LockCoinRechargeSettingService lockCoinRechargeSettingService;

    @Autowired
    private LockCoinActivitieSettingService lockCoinActivitieSettingService;


    @RequiresPermissions("lock:lockCoinRecharge-page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.PROMOTION ,operation = "锁仓充值配置分页查询")
    public MessageResult page(
            PageModel pageModel)
    {
        //条件,
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<LockCoinRechargeSetting>  pageData= lockCoinRechargeSettingService.findAll(predicate ,pageModel.getPageable());
        return success(pageData);
    }

    @PostMapping("list")
    public MessageResult list(){

        //add by fumy date:2018-06-27 reason : 手动锁仓充币新增初始化类型数据
        //员工锁仓充币配置信息列表
        List<LockCoinRechargeSetting> empRechargeList = lockCoinRechargeSettingService.findAllValid();
        //理财锁仓配置信息列表
        List<LockCoinActivitieSetting> actLockCoinList =  lockCoinActivitieSettingService.findByTypeAndStatus(LockCoinActivitieType.FIXED_DEPOSIT,LockSettingStatus.VALID);
        //add by fumy date:2018-08-06 reason : SLB节点产品配置信息列表
        //SLB节点产品配置信息列表
        List<LockCoinActivitieSetting> quanLockCoinList =  lockCoinActivitieSettingService.findQuantifyLock();

        //包装数据到list,返回给前端
        List<Object> list = new ArrayList();
        list.add(empRechargeList);
        list.add(actLockCoinList);
        list.add(quanLockCoinList);

        return success(list);

        //edit by fumy date:2018-06-27 reason:这是原有后台锁仓初始化查询的员工锁仓充币的配置
//        return success(lockCoinRechargeSettingService.findAllValid());
    }

    /**
     * 添加锁仓充值
     * @author tansitao
     * @time 2018/6/13 9:42 
     */
    @PostMapping("create")
    @RequiresPermissions("lock:lockCoinRecharge-create")
    @AccessLog(module = AdminModule.PROMOTION ,operation = "添加锁仓充值配置")
    public MessageResult create(@Valid LockCoinRechargeSetting lockCoinRechargeSetting, @SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin) {
        lockCoinRechargeSetting.setAdminId(admin.getId());
        lockCoinRechargeSettingService.save(lockCoinRechargeSetting);
        MessageResult ms = MessageResult.success("添加锁仓充值成功");
        return ms;
    }



    /**
     *
     * 修改锁仓充值
     * @author tansitao
     * @time 2018/6/13 9:42 
     */
    @PostMapping("update")
    @RequiresPermissions("lock:lockCoinRecharge-update")
    @AccessLog(module = AdminModule.PROMOTION ,operation = "修改锁仓充值配置")
    public MessageResult update(@Valid LockCoinRechargeSetting lockCoinRechargeSetting, @SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin) {
        LockCoinRechargeSetting ccRechargeSetting = lockCoinRechargeSettingService.findOne(lockCoinRechargeSetting.getId());
        Assert.notNull(ccRechargeSetting, "该锁仓配置不存在");
        Assert.isTrue(ccRechargeSetting.getStatus() != LockSettingStatus.VALID,"生效中的锁仓配置不允许修改");
        lockCoinRechargeSetting.setAdminId(admin.getId());
        lockCoinRechargeSetting.setCreateTime(ccRechargeSetting.getCreateTime());
        lockCoinRechargeSettingService.save(lockCoinRechargeSetting);
        return success();
    }

    @PostMapping("detail")
    @RequiresPermissions("lock:lockCoinRecharge-detail")
    @AccessLog(module = AdminModule.PROMOTION ,operation = "锁仓充值配置详情")
    public MessageResult detail(@RequestParam("id") Long id){
        notNull(id, "validate id!");
        return success(lockCoinRechargeSettingService.findOne(id));
    }

}
