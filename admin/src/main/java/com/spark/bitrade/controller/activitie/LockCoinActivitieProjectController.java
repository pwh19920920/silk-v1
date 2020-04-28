package com.spark.bitrade.controller.activitie;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.LockCoinActivitieProjectService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.Assert.notNull;

/**
  * 锁仓大活动（模板设置）
  * @author tansitao
  * @time 2018/6/19 17:15 
  */
@RestController
@RequestMapping("activitieProject")
public class LockCoinActivitieProjectController extends BaseAdminController {
    @Autowired
    private LockCoinActivitieProjectService service;

    /**
     * 分页查询
     * @author tansitao
     * @time 2018/6/13 10:54 
     */
    @RequiresPermissions("lock:activitieProject-page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.PROMOTION ,operation = "锁仓活动配置分页查询")
    public MessageResult page(PageModel pageModel,String name){
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (StringUtils.isNotBlank(name)){
            booleanExpressions.add(QLockCoinActivitieProject.lockCoinActivitieProject.name.like("%"+name+"%"));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<LockCoinActivitieProject> pageData= service.findAll(predicate ,pageModel.getPageable());
        return success(pageData);
    }

    /**
      * 添加锁仓活动配置
      * @author tansitao
      * @time 2018/6/13 9:42 
      */
    @PostMapping("create")
    @RequiresPermissions("lock:activitieProject-create")
    @AccessLog(module = AdminModule.PROMOTION ,operation = "添加锁仓活动配置")
    public MessageResult create(@Valid LockCoinActivitieProject lockCoinActivitieProject) {
        service.save(lockCoinActivitieProject);
        return success();
    }

    /**
      * 修改活动
      * @author tansitao
      * @time 2018/6/13 9:42 
      */
    @PostMapping("update")
    @RequiresPermissions("lock:activitieProject-update")
    @AccessLog(module = AdminModule.PROMOTION ,operation = "修改锁仓活动配置")
    public MessageResult update(@Valid LockCoinActivitieProject lockCoinActivitieProject, @SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin) {
        LockCoinActivitieProject lcActivitieProject = service.findOne(lockCoinActivitieProject.getId());
        Assert.notNull(lcActivitieProject, "该活动配置不存在");
        //edit by fumy. date:2018.12.04 reason:取消生效状态不能修改配置的判断
//        Assert.isTrue(lcActivitieProject.getStatus() != LockSettingStatus.VALID,"生效中的锁仓配置不允许修改");
        lockCoinActivitieProject.setAdminId(admin.getId());
        lockCoinActivitieProject.setCreateTime(lcActivitieProject.getCreateTime());
        service.save(lockCoinActivitieProject);
        return success();
    }




    @PostMapping("detail")
    @RequiresPermissions("lock:activitieProject-detail")
    @AccessLog(module = AdminModule.PROMOTION ,operation = "锁仓活动配置详情")
    public MessageResult detail(@RequestParam("id") Long id){
        notNull(id, "validate id!");
        return success(service.findOne(id));
    }

    /**
     * 获取所有未生效的活动
     * @author tansitao
     * @time 2018/6/14 16:50 
     */
    @PostMapping("list")
    @RequiresPermissions(value = {"lock:lockCoinActivitie-page-query","lock:activitieProject-list"},logical = Logical.OR)
    public MessageResult list(@RequestParam("type") ActivitieType type)
    {

        List<LockCoinActivitieProject> lockCoinActivitieProjectList = service.findUnenforcedLockActivity(type, LockSettingStatus.UNENFORCED);
        for (LockCoinActivitieProject lockCoinActivitieProject : lockCoinActivitieProjectList)
        {
            lockCoinActivitieProject.setDescription("");
        }
        return success(lockCoinActivitieProjectList);
    }


}
