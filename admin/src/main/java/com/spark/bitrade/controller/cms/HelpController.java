package com.spark.bitrade.controller.cms;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.dto.PageParam;
import com.spark.bitrade.entity.QSysHelp;
import com.spark.bitrade.entity.SysAdvertise;
import com.spark.bitrade.entity.SysHelp;
import com.spark.bitrade.pagination.PageResult;
import com.spark.bitrade.service.SysHelpService;
import com.spark.bitrade.util.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.Assert.notNull;

/**
 * @author rongyu
 * @description 后台帮助web
 * @date 2018/1/9 10:11
 */
@RestController
@RequestMapping("/cms/system-help")
public class HelpController extends BaseAdminController {

    @Autowired
    private SysHelpService sysHelpService;

    @RequiresPermissions("cms:system-help:create")
    @PostMapping("/create")
    @AccessLog(module = AdminModule.CMS, operation = "创建系统帮助")
    public MessageResult create(@Valid SysHelp sysHelp, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        sysHelp.setCreateTime(DateUtil.getCurrentDate());
        sysHelp = sysHelpService.save(sysHelp);
        return success(sysHelp);
    }

    @RequiresPermissions("cms:system-help:all")
    @PostMapping("/all")
    @AccessLog(module = AdminModule.CMS, operation = "查找所有系统帮助")
    public MessageResult all() {
        List<SysHelp> sysHelps = sysHelpService.findAll();
        if (sysHelps != null && sysHelps.size() > 0)
            return success(sysHelps);
        return error("没有数据");
    }

    @RequiresPermissions("cms:system-help:top")
    @PostMapping("top")
    @AccessLog(module = AdminModule.CMS, operation = "系统帮助置顶")
    public MessageResult toTop(@RequestParam("id")long id){
        SysHelp help = sysHelpService.findOne(id);
        int a = sysHelpService.getMaxSort();
        help.setSort(a+1);
        sysHelpService.save(help);
        return success("置顶成功");
    }

    @RequiresPermissions("cms:system-help:detail")
    @PostMapping("/detail")
    @AccessLog(module = AdminModule.CMS, operation = "系统帮助详情")
    public MessageResult detail(@RequestParam(value = "id") Long id) {
        SysHelp sysHelp = sysHelpService.findOne(id);
        notNull(sysHelp, "validate id!");
        return success(sysHelp);
    }

    @RequiresPermissions("cms:system-help:update")
    @PostMapping("/update")
    @AccessLog(module = AdminModule.CMS, operation = "更新系统帮助")
    public MessageResult update(@Valid SysHelp sysHelp, BindingResult bindingResult) {
        notNull(sysHelp.getId(), "validate id!");
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        SysHelp one = sysHelpService.findOne(sysHelp.getId());
        notNull(one, "validate id!");
        sysHelpService.save(sysHelp);
        return success();
    }

    @RequiresPermissions("cms:system-help:deletes")
    @PostMapping("/deletes")
    @AccessLog(module = AdminModule.CMS, operation = "删除系统帮助")
    public MessageResult deleteOne(@RequestParam("ids") Long[] ids) {
        sysHelpService.deleteBatch(ids);
        return success();
    }

    @RequiresPermissions("cms:system-help:page-query")
    @PostMapping("/page-query")
    @AccessLog(module = AdminModule.CMS, operation = "分页查询系统帮助")
    public MessageResult pageQuery(PageModel pageModel,@RequestParam(value = "title",required = false,defaultValue = "")String title) {
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (!"".equals(title)){
            booleanExpressions.add(QSysHelp.sysHelp.title.like("%"+title+"%"));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<SysHelp> all = sysHelpService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }

    @RequiresPermissions("cms:system-help:out-excel")
    @GetMapping("/out-excel")
    @AccessLog(module = AdminModule.CMS, operation = "导出系统帮助Excel")
    public MessageResult outExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List all = sysHelpService.findAll();
        return new FileUtil().exportExcel(request, response, all, "sysHelp");
    }
}
