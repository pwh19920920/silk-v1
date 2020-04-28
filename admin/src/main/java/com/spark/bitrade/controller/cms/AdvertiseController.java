package com.spark.bitrade.controller.cms;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.SysAdvertiseLocation;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.model.screen.SysAdvertiseScreen;
import com.spark.bitrade.entity.QSysAdvertise;
import com.spark.bitrade.entity.SysAdvertise;
import com.spark.bitrade.service.SysAdvertiseService;
import com.spark.bitrade.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.spark.bitrade.entity.QSysAdvertise.sysAdvertise;
import static org.springframework.util.Assert.notNull;

/**
 * @author rongyu
 * @description 系统广告
 * @date 2018/1/6 15:03
 */
@Slf4j
@RestController
@RequestMapping("/cms/system-advertise")
public class AdvertiseController extends BaseAdminController {

    @Autowired
    private SysAdvertiseService sysAdvertiseService;

    /**
     * 创建系统广告
     * @author fumy
     * @time 2018.11.19 14:27
     * @param sysAdvertise
     * @param bindingResult
     * @return true
     */
    @RequiresPermissions("cms:system-advertise:create")
    @PostMapping("/create")
    @AccessLog(module = AdminModule.CMS, operation = "创建系统广告")
    public MessageResult findOne(@Valid SysAdvertise sysAdvertise, BindingResult bindingResult) {
        Date end = DateUtil.strToDate(sysAdvertise.getEndTime());
        Date start = DateUtil.strToDate(sysAdvertise.getStartTime());
        Assert.isTrue(end.after(start), "开始时间不得晚于结束时间");
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        sysAdvertise.setSerialNumber(UUIDUtil.getUUID());
        sysAdvertise.setCreateTime(DateUtil.getCurrentDate());

        //add by fumy date:2018.11.19 reason:APP活动广告只能上线一个
        //当广告类型为APP模块(type = 1) 且 位置为APP活动、状态为上线(正常)时，查询是否存在已经上线的APP活动广告（同时只允许上线一个）
        if(sysAdvertise.getSysAdvertiseType() == 1
                && sysAdvertise.getSysAdvertiseLocation() == SysAdvertiseLocation.APP_ACTIVITY
                && sysAdvertise.getStatus() == CommonStatus.NORMAL){
            boolean isExist = sysAdvertiseService.isExistAppActNormalAd(sysAdvertise.getSysAdvertiseType(),sysAdvertise.getSysAdvertiseLocation().getOrdinal(),
                    sysAdvertise.getStatus().getOrdinal());
            //如果存在，则不能创建广告
            if(isExist){
                return error("APP首页活动位置广告已被其他广告启用，请重新操作");
            }
        }

        SysAdvertise one = sysAdvertiseService.save(sysAdvertise);
        return success(one);
    }

    @RequiresPermissions("cms:system-advertise:all")
    @PostMapping("/all")
    @AccessLog(module = AdminModule.CMS, operation = "所有系统广告")
    public MessageResult all() {
        List<SysAdvertise> all = sysAdvertiseService.findAll();
        if (all != null & all.size() > 0)
            return success(all);
        return error("没有数据");
    }

    @RequiresPermissions("cms:system-advertise:detail")
    @PostMapping("/detail")
    @AccessLog(module = AdminModule.CMS, operation = "系统广告详情")
    public MessageResult findOne(@RequestParam(value = "serialNumber") String serialNumber) {
        SysAdvertise sysAdvertise = sysAdvertiseService.findOne(serialNumber);
        notNull(sysAdvertise, "validate serialNumber!");
        return success(sysAdvertise);
    }

    /**
     * 修改系统广告
     * @author fumy
     * @time 2018.11.19 14:29
     * @param sysAdvertise
     * @param bindingResult
     * @return true
     */
    @RequiresPermissions("cms:system-advertise:update")
    @PostMapping("/update")
    @AccessLog(module = AdminModule.CMS, operation = "更新系统广告")
    public MessageResult update(@Valid SysAdvertise sysAdvertise, BindingResult bindingResult) {
        notNull(sysAdvertise.getSerialNumber(), "validate serialNumber(null)!");
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null){
            return result;
        }
        //add by fumy date:2018.11.19 reason:APP活动广告只能上线一个
        //当广告类型为APP模块(type = 1) 且 位置为APP活动、状态为上线(正常)时，查询是否存在已经上线的APP活动广告（同时只允许上线一个）
        if(sysAdvertise.getSysAdvertiseType() == 1
                && sysAdvertise.getSysAdvertiseLocation() == SysAdvertiseLocation.APP_ACTIVITY
                && sysAdvertise.getStatus() == CommonStatus.NORMAL){
            boolean isExist = sysAdvertiseService.isExistAppActNormalAd(sysAdvertise.getSysAdvertiseType(),sysAdvertise.getSysAdvertiseLocation().getOrdinal(),
                    sysAdvertise.getStatus().getOrdinal());
            //如果存在，则不能创建广告
            if(isExist){
                return error("APP首页活动位置广告已被其他广告启用，请重新操作");
            }
        }
        SysAdvertise one = sysAdvertiseService.findOne(sysAdvertise.getSerialNumber());
        notNull(one, "validate serialNumber!");
        sysAdvertiseService.save(sysAdvertise);
        return success();
    }


    @RequiresPermissions("cms:system-advertise:deletes")
    @PostMapping("/deletes")
    @AccessLog(module = AdminModule.CMS, operation = "批量删除系统广告")
    public MessageResult delete(@RequestParam(value = "ids", required = true) String[] ids) {
        sysAdvertiseService.deleteBatch(ids);
        return success();
    }


    @RequiresPermissions("cms:system-advertise:page-query")
    @PostMapping("/page-query")
    @AccessLog(module = AdminModule.CMS, operation = "分页查询系统广告")
    public MessageResult pageQuery(PageModel pageModel, SysAdvertiseScreen screen) {
        Predicate predicate = getPredicate(screen);
        if(pageModel.getProperty()==null){
            List<String> property = new ArrayList<>();
            property.add(0,"createTime");
            List<Sort.Direction> directions = new ArrayList<>();
            directions.add(0,Sort.Direction.DESC);
            if (screen.getStatus()==null){
                directions.add(1,Sort.Direction.ASC);
                property.add(1,"status");
            }
            pageModel.setProperty(property);
            pageModel.setDirection(directions);
        }
        Page<SysAdvertise> all = sysAdvertiseService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }

    private Predicate getPredicate(SysAdvertiseScreen screen) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (screen.getStatus() != null) {
            booleanExpressions.add(QSysAdvertise.sysAdvertise.status.eq(screen.getStatus()));
        }
        if (screen.getSysAdvertiseLocation() != null) {
            booleanExpressions.add(QSysAdvertise.sysAdvertise.sysAdvertiseLocation.eq(screen.getSysAdvertiseLocation()));
        }
        if (StringUtils.isNotBlank(screen.getSerialNumber())) {
            booleanExpressions.add(QSysAdvertise.sysAdvertise.serialNumber.like("%" + screen.getSerialNumber() + "%"));
        }
        return PredicateUtils.getPredicate(booleanExpressions);
    }

    @RequiresPermissions("cms:system-advertise:top")
    @PostMapping("top")
    @AccessLog(module = AdminModule.CMS, operation = "广告置顶")
    public MessageResult toTop(@RequestParam("serialNum")String serialNum){
        SysAdvertise advertise = sysAdvertiseService.findOne(serialNum);
        int a = sysAdvertiseService.getMaxSort();
        advertise.setSort(a+1);
        sysAdvertiseService.save(advertise);
        return success("置顶成功");
    }


    @RequiresPermissions("cms:system-advertise:out-excel")
    @GetMapping("/out-excel")
    @AccessLog(module = AdminModule.CMS, operation = "导出系统广告Excel")
    public MessageResult outExcel(
            @RequestParam(value = "serialNumber", required = false) String serialNumber,
            @RequestParam(value = "sysAdvertiseLocation", required = false) SysAdvertiseLocation sysAdvertiseLocation,
            @RequestParam(value = "status", required = false) CommonStatus status,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<Predicate> predicateList = getPredicateList(serialNumber, sysAdvertiseLocation, status);
        List list = sysAdvertiseService.query(predicateList, null, null).getContent();
        return new FileUtil().exportExcel(request, response, list, "sysAdvertise");
    }

    private List<Predicate> getPredicateList(String serialNumber, SysAdvertiseLocation sysAdvertiseLocation, CommonStatus status) {
        ArrayList<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(serialNumber))
            predicates.add(sysAdvertise.serialNumber.eq(serialNumber));
        if (sysAdvertiseLocation != null)
            predicates.add(sysAdvertise.sysAdvertiseLocation.eq(sysAdvertiseLocation));
        if (status != null)
            predicates.add(sysAdvertise.status.eq(status));
        return predicates;
    }
}
