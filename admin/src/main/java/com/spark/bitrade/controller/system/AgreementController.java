package com.spark.bitrade.controller.system;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.controller.BaseController;
import com.spark.bitrade.entity.Agreement;
import com.spark.bitrade.entity.QAgreement;
import com.spark.bitrade.service.AgreementService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;


/**
 * 协议管理
 * @author tansitao
 * @time 2018/4/24 11:16 
 */
@RestController
@RequestMapping("system/agreement")
public class AgreementController extends BaseController {
    @Autowired
    private AgreementService agreementService;

    /**
     * 创建协议
     * @author tansitao
     * @time 2018/4/24 11:16 
     * @param title 标题
     * @param content 内容
     * @param isShow 是否显示
     * @param imgUrl 图片
     */
    @RequiresPermissions("cms:agreement:create")
    @PostMapping("create")
    public MessageResult create(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam("isShow") Boolean isShow,
            @RequestParam(value = "imgUrl", required = false) String imgUrl) {
        Agreement agreement = new Agreement();
        agreement.setTitle(title);
        agreement.setContent(content);
        agreement.setIsShow(isShow);
        agreement.setImgUrl(imgUrl);
        agreementService.save(agreement);
        return success("协议创建成功");
    }

    /**
     * 置顶协议
     * @author tansitao
     * @time 2018/4/24 11:17 
     * @param id
     */
    @RequiresPermissions("cms:agreement:top")
    @PostMapping("top")
    @AccessLog(module = AdminModule.CMS, operation = "协议置顶")
    public MessageResult toTop(@RequestParam("id")long id){
        Agreement agreement = agreementService.findById(id);
        int a = agreementService.getMaxSort();
        agreement.setSort(a+1);
        agreementService.save(agreement);
        return success("置顶成功");
    }

    /**
     * 分页查询协议
     * @author tansitao
     * @time 2018/4/24 11:18 
     * @param pageModel
     * @param isShow
     */
    @RequiresPermissions("cms:agreement:page-query")
    @GetMapping("page-query")
    public MessageResult page(
            PageModel pageModel,
            @RequestParam(required = false) Boolean isShow,@RequestParam(value = "title",required = false,defaultValue = "")String title) {
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (isShow != null) {
            booleanExpressions.add(QAgreement.agreement.isShow.eq(isShow));
        }
        if (!"".equals(title)){
            booleanExpressions.add(QAgreement.agreement.title.like("%"+title+"%"));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<Agreement> all = agreementService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }

    /* *
     * 删除协议
     * @author tansitao
     * @time 2018/4/24 11:19 
     * @param ids
     */
    @RequiresPermissions("cms:agreement:deletes")
    @PatchMapping("deletes")
    public MessageResult deleteOne(@RequestParam Long[] ids) {
        agreementService.deleteBatch(ids);
        return success();
    }

    /**
     * 查看协议详情
     * @author tansitao
     * @time 2018/4/24 11:19 
     * @param id
     */
    @RequiresPermissions("cms:agreement:detail")
    @GetMapping("{id}/detail")
    public MessageResult detail(
            @PathVariable Long id) {
        Agreement agreement = agreementService.findById(id);
        Assert.notNull(agreement, "validate id!");
        return success(agreement);
    }

    /**
     * 修改协议
     * @author tansitao
     * @time 2018/4/24 11:19 
     * @param id
     * @param title
     * @param content
     * @param isShow
     * @param imgUrl
     */
    @RequiresPermissions("cms:agreement:update")
    @PutMapping("{id}/update")
    public MessageResult update(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Boolean isShow,
            @RequestParam(value = "imgUrl", required = false) String imgUrl) {
        Agreement agreement = agreementService.findById(id);
        Assert.notNull(agreement, "validate id!");
        agreement.setTitle(title);
        agreement.setContent(content);
        agreement.setIsShow(isShow);
        agreement.setImgUrl(imgUrl);
        agreementService.save(agreement);
        return success();
    }

    /**
     * 隐藏协议
     * @author tansitao
     * @time 2018/4/24 11:20 
     * @param id
     */
    @RequiresPermissions("cms:agreement:turn-off")
    @PatchMapping("{id}/turn-off")
    public MessageResult turnOff(@PathVariable Long id) {
        Agreement agreement = agreementService.findById(id);
        Assert.notNull(agreement, "validate id!");
        agreement.setIsShow(false);
        agreementService.save(agreement);
        return success();
    }

    /**
     * 显示协议
     * @author tansitao
     * @time 2018/4/24 11:20 
     * @param id
     */
    @RequiresPermissions("cms:agreement:turn-on")
    @PatchMapping("{id}/turn-on")
    public MessageResult turnOn(@PathVariable("id") Long id) {
        Agreement agreement = agreementService.findById(id);
        Assert.notNull(agreement, "validate id!");
        agreement.setIsShow(true);
        agreementService.save(agreement);
        return success();
    }

}
