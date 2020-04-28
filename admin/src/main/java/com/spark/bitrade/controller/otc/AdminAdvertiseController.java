package com.spark.bitrade.controller.otc;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.Advertise;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.QAdvertise;
import com.spark.bitrade.model.screen.AdvertiseScreen;
import com.spark.bitrade.service.AdvertiseService;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.MemberWalletService;
import com.spark.bitrade.service.ValidateOpenTranscationService;
import com.spark.bitrade.util.*;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;


/**
 * @author rongyu
 * @description 后台广告web层
 * @date 2018/1/3 9:42
 */
@RestController
@RequestMapping("/otc/advertise")
public class AdminAdvertiseController extends BaseAdminController {

    @Autowired
    private AdvertiseService advertiseService;
    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ValidateOpenTranscationService validateOpenTranscationService;

    @RequiresPermissions("otc:advertise-detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.OTC, operation = "后台广告Advertise详情")
    public MessageResult detail(Long id) {
        if (id == null){
            return error("id必传");
        }
        Advertise one = advertiseService.findOne(id);
        if (one == null) {
            return error("没有此id的广告");
        }
        return success("查询成功", one);
    }

//    @RequiresPermissions("otc:advertise-turnoff")
    @PostMapping("turnoff")
    @AccessLog(module = AdminModule.OTC, operation = "关闭后台广告Advertise")
    public MessageResult turnoff(Long id) {
        if (id == null){
            return error("id必传");
        }
        Advertise one = advertiseService.findOne(id);
        if (one == null){
            return error("没有此id的广告");
        }
        one.setStatus(AdvertiseControlStatus.TURNOFF);
        one.setUpdateTime(new Date());
        Advertise save = advertiseService.saveAdvertise(one);
        return success("关闭成功", save);
    }

    @RequiresPermissions("otc:advertise-alter-status")
    @PostMapping("alter-status")
    @AccessLog(module = AdminModule.OTC, operation = "修改后台广告Advertise状态")
    public MessageResult statue(
            @RequestParam(value = "ids") Long[] ids,
            @RequestParam(value = "status") AdvertiseControlStatus status) {
        if (ids.length==0){
            return error("id必传");
        }
        try {
            //add by zyj:下架或关闭广告时，返回相应的冻结金额
            //如果广告状态为上架，则解冻钱包
            for (Long id:ids){
                Advertise one = advertiseService.findOne(id);
                Assert.isTrue(one != null, msService.getMessage("NO_ADVERTISE"));
                //出售、上架
                if (one.getAdvertiseType().equals(AdvertiseType.SELL)&&one.getStatus().equals(AdvertiseControlStatus.PUT_ON_SHELVES)) {
                        //下架广告  清空该广告剩余量,解冻钱包
                    int row = advertiseService.putOffShelves(one);
                    if (row == 0){
                        return error("下架广告失败");
                    }
                    if (CommonUtils.equals(status,AdvertiseControlStatus.TURNOFF)){
                        // 上架 -> 关闭广告
                        advertiseService.updateStatus(status, one.getId());
                    }
                }else if (one.getStatus().equals(AdvertiseControlStatus.PUT_OFF_SHELVES) && CommonUtils.equals(status, AdvertiseControlStatus.TURNOFF)){
                    //下架 -> 关闭
                    advertiseService.updateStatus(status,one.getId());
                }else if (CommonUtils.equals(status, AdvertiseControlStatus.PUT_ON_SHELVES ) ){
                    //上架广告
                    Assert.isTrue(one.getStatus().equals(AdvertiseControlStatus.PUT_OFF_SHELVES), msService.getMessage("PUT_ON_SHELVES_FAILED"));
                    Member member = one.getMember();
                    //广告发布限制
                    isTrue(org.springframework.util.StringUtils.isEmpty(member.getTransactionStatus()) || member.getTransactionStatus() == BooleanEnum.IS_TRUE, msService.getMessage("NO_ALLOW_TRANSACT"));
                    validateOpenTranscationService.validateOpenExPitTransaction(member.getId(),msService.getMessage("NO_ALLOW_TRANSACT"),one.getAdvertiseType());
                    isTrue(org.springframework.util.StringUtils.isEmpty(member.getPublishAdvertise()) || member.getPublishAdvertise() == BooleanEnum.IS_TRUE , msService.getMessage("NOT_ADVERTISING"));
                    advertiseService.putOnShelves(one, restTemplate);
                }else{
                    advertiseService.updateStatus(status, one.getId());
                }

            }
            //修改广告状态,更新修改时间
//            advertiseService.turnOffBatch(status,ids);
            return success("修改成功");
        }catch (Exception e){
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }

    }

    @RequiresPermissions("otc:advertise-page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.OTC, operation = "分页查找后台广告Advertise")
    public MessageResult page(PageModel pageModel, AdvertiseScreen screen) {
        ArrayList<Sort.Direction> directions=new ArrayList<>();
        List<String> property=new ArrayList<>();
        //sort大于0的在前面按降序排列
        directions.add(0,Sort.Direction.DESC);
        property.add(0,"sort");
        directions.add(1,Sort.Direction.DESC);
        property.add(1,"createTime");
        pageModel.setDirection(directions);
        pageModel.setProperty(property);
        Predicate predicate = getPredicate(screen);
        Page<Advertise> all = advertiseService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }

    @RequiresPermissions("otc:advertise:out-excel")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.OTC, operation = "导出后台广告Advertise Excel")
    public MessageResult outExcel(
            @RequestParam(value = "startTime", required = false) Date startTime,
            @RequestParam(value = "endTime", required = false) Date endTime,
            @RequestParam(value = "advertiseType", required = false) AdvertiseType advertiseType,
            @RequestParam(value = "realName", required = false) String realName,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<BooleanExpression> booleanExpressionList = getBooleanExpressionList(startTime, endTime, advertiseType, realName);
        List list = advertiseService.queryWhereOrPage(booleanExpressionList, null, null).getContent();
        return new FileUtil().exportExcel(request, response, list, "order");
    }

    // 获得条件
    private List<BooleanExpression> getBooleanExpressionList(
            Date startTime, Date endTime, AdvertiseType advertiseType, String realName) {
        QAdvertise qEntity = QAdvertise.advertise;
        List<BooleanExpression> booleanExpressionList = new ArrayList();
        booleanExpressionList.add(qEntity.status.in(AdvertiseControlStatus.PUT_ON_SHELVES, AdvertiseControlStatus.PUT_OFF_SHELVES));
        if (startTime != null){
            booleanExpressionList.add(qEntity.createTime.gt(startTime));
        }
        if (endTime != null) {
            booleanExpressionList.add(qEntity.createTime.lt(endTime));
        }
        if (advertiseType != null){
            booleanExpressionList.add(qEntity.advertiseType.eq(advertiseType));
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(realName)){
            booleanExpressionList.add(qEntity.member.realName.like("%" + realName + "%"));
        }
        return booleanExpressionList;

    }


    private Predicate getPredicate(AdvertiseScreen screen) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if(screen.getStatus()!=AdvertiseControlStatus.TURNOFF&&screen.getStatus()!=null){
            booleanExpressions.add(QAdvertise.advertise.status.eq(screen.getStatus()));
        }
        if (screen.getAdvertiseType() != null){
            booleanExpressions.add(QAdvertise.advertise.advertiseType.eq(screen.getAdvertiseType()));
        }
        if (StringUtils.isNotBlank(screen.getAccount())){
            booleanExpressions.add(QAdvertise.advertise.member.realName.like("%" + screen.getAccount() + "%")
                    .or(QAdvertise.advertise.member.username.like("%" + screen.getAccount() + "%"))
                    .or(QAdvertise.advertise.member.mobilePhone.like((screen.getAccount()+"%")))
                    .or(QAdvertise.advertise.member.email.like((screen.getAccount()+"%"))));
        }
        //已置顶
        if (CommonUtils.equals(screen.getIsTop(), BooleanEnum.IS_TRUE)){
            booleanExpressions.add(QAdvertise.advertise.sort.gt(0));
        }
        //未置顶
        if (CommonUtils.equals(screen.getIsTop(), BooleanEnum.IS_FALSE)){
            booleanExpressions.add(QAdvertise.advertise.sort.eq(0));
        }
        if(screen.getPayModel()!=null){
            booleanExpressions.add(QAdvertise.advertise.payMode.contains(screen.getPayModel()));
        }
        return PredicateUtils.getPredicate(booleanExpressions);
    }

    @RequiresPermissions("otc:advertise-toTop")
    @ApiOperation(value = "C2C广告置顶")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "advertiseId",value = "广告id",required = true),
            @ApiImplicitParam(name = "isTop",value = "是否置顶 1：是，0/null：否")
    })
    @PostMapping("toTop")
    @AccessLog(module = AdminModule.MEMBER, operation = "C2C广告置顶")
    public MessageRespResult toTop(Long advertiseId, BooleanEnum isTop){
        notNull(advertiseId,msService.getMessage("ID_ILLEGAL"));
        Advertise advertise = advertiseService.findOne(advertiseId);
        notNull(advertise,msService.getMessage("NO_ADVERTISE"));
         //isTop == 1 置顶
        if (CommonUtils.equals(isTop, BooleanEnum.IS_TRUE)){
            //查询sort最大的广告
            Map<String,Object> map = advertiseService.findOneBySortMax();
            //已是最大：已置顶
            isTrue(!CommonUtils.equals(map.get("id"), advertiseId), msService.getMessage("IS_TOP"));
            //没有，加1
            int sort = CommonUtils.toInt(map.get("sort")) + 1;
            advertise.setSort(sort);
            advertiseService.save(advertise);
        }else {
            //isTop == 0/NULL 取消置顶
            advertise.setSort(0);
            advertiseService.save(advertise);
        }
        return MessageRespResult.success("操作成功");
    }


}
