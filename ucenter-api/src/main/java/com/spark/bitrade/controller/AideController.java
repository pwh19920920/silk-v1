package com.spark.bitrade.controller;

import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.Platform;
import com.spark.bitrade.constant.SysAdvertiseLocation;
import com.spark.bitrade.constant.SysHelpClassification;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.AgreementService;
import com.spark.bitrade.service.AppRevisionService;
import com.spark.bitrade.service.SysAdvertiseService;
import com.spark.bitrade.service.SysHelpService;
import com.spark.bitrade.service.WebsiteInformationService;
import com.spark.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年02月05日
 */
@Api("系统广告信息控制类")
@RestController
@RequestMapping("/ancillary")
@Slf4j
public class AideController {
    @Autowired
    private WebsiteInformationService websiteInformationService;

    @Autowired
    private SysAdvertiseService sysAdvertiseService;

    @Autowired
    private SysHelpService sysHelpService;
    @Autowired
    private AppRevisionService appRevisionService;

    ////add by tansitao 时间： 2018/4/24 原因：添加协议service
    @Autowired
    private AgreementService agreementService;

    /**
     * 站点信息
     *
     * @return
     */
    @RequestMapping("/website/info")
    public MessageResult keyWords() {
        WebsiteInformation websiteInformation = websiteInformationService.fetchOne();
        MessageResult result = MessageResult.success();
        result.setData(websiteInformation);
        return result;
    }

    /**
     * 系统广告
     *
     * @return
     */
    @ApiOperation(value = "系统广告查询", tags = "系统广告与公告", notes = "PC、APP端获取系统广告信息")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "广告类型，0：Web网页，1：APP模块", name = "sysAdvertiseType", dataType = "int", required = true),
            @ApiImplicitParam(value = "广告位置，0：APP首页轮播，1：PC轮播，2：PC分类，3：APP首页活动", name = "sysAdvertiseLocation", dataType = "int", required = true)
    })
    @PostMapping("/system/advertise")
    public MessageResult sysAdvertise(Integer sysAdvertiseType, SysAdvertiseLocation sysAdvertiseLocation, HttpServletRequest request) {
        String language = request.getHeader("language");
        if (sysAdvertiseType == null) {
            return MessageResult.success("", new ArrayList<>());
        }
        List<SysAdvertise> list = sysAdvertiseService.findNormalAdByTypeAndLocation(sysAdvertiseType, sysAdvertiseLocation, language);
        //edit by tansitao 时间： 2018/8/22 原因：将广告内容取消，只要列表数据
        if (list != null) {
            for (SysAdvertise sysAdvertise : list) {
                sysAdvertise.setContent("");
            }
        }
        return MessageResult.success("", list);
//        MessageResult result = MessageResult.success();
//        result.setData(list);
//        return result;
    }

    /**
     *  * 系统广告详情
     *  * @author tansitao
     *  * @time 2018/8/21 18:00 
     *  
     */
    @RequestMapping("/system/advertise/{serialNumber}")
    public MessageResult sysAdvertise(@PathVariable(value = "serialNumber") String serialNumber) {
        SysAdvertise sysAdvertise = sysAdvertiseService.findOne(serialNumber);
        MessageResult result = MessageResult.success();
        result.setData(sysAdvertise);
        return result;
    }

    /**
     * 系统帮助
     *
     * @return
     */
    @RequestMapping("/system/help")
    public MessageResult sysHelp(@RequestParam(value = "sysHelpClassification", required = false) SysHelpClassification sysHelpClassification) {
        List<SysHelp> list = null;
        List<SystemHelp> systemHelpList = new ArrayList<SystemHelp>();
        if (sysHelpClassification == null) {
            list = sysHelpService.findAllByStatus(CommonStatus.NORMAL);//add by zyj 时间2018/8/28 修改：只查询状态为显示的帮助
        } else {
            list = sysHelpService.findAllByStatusAndSysHelpClassification(CommonStatus.NORMAL, sysHelpClassification);
        }
        //edit by tansitao 时间： 2018/6/4 原因：将帮助内容取消，只要列表数据
        for (SysHelp sysHelp : list) {
            SystemHelp systemHelp = SystemHelp.builder().author(sysHelp.getAuthor())
                    .createTime(sysHelp.getCreateTime())
                    .id(sysHelp.getId())
                    .imgUrl(sysHelp.getImgUrl())
                    .sort(sysHelp.getSort())
                    .status(sysHelp.getStatus())
                    .title(sysHelp.getTitle())
                    .sysHelpClassification(sysHelp.getSysHelpClassification())
                    .build();
            systemHelpList.add(systemHelp);
        }
        MessageResult result = MessageResult.success();
        result.setData(systemHelpList);
        return result;
    }

    /**
     * 系统帮助详情
     *
     * @param id
     * @return
     */
    @RequestMapping("/system/help/{id}")
    public MessageResult sysHelp(@PathVariable(value = "id") long id) {
        //List<SysHelp> list = sysHelpService.findBySysHelpClassification(sysHelpClassification);
        SysHelp sysHelp = sysHelpService.findOne(id);
        MessageResult result = MessageResult.success();
        result.setData(sysHelp);
        return result;
    }

    ////add by tansitao 时间： 2018/4/24 原因：新增系统协议获取接口
    @RequestMapping("/system/agreement")
    public MessageResult sysAgreement(@RequestParam(value = "sysHelpClassification", required = false) SysHelpClassification sysHelpClassification) {
        List<Agreement> list = agreementService.findAllByisShow(true);
        MessageResult result = MessageResult.success();
        result.setData(list);
        return result;
    }

    //add by tansitao 时间： 2018/5/4 原因：新增系统详情获取接口
    @RequestMapping("/system/agreement/{id}")
    public MessageResult sysAgreement(@PathVariable(value = "id") long id) {
        Agreement agreement = agreementService.findById(id);
        MessageResult result = MessageResult.success();
        result.setData(agreement);
        return result;
    }

    //add by yangch 时间： 2018.04.26 原因：代码合并

    /**
     * 移动版本号
     * //add|edit|del by  shenzucai 时间： 2018.08.20  原因：添加钱包app更新控制
     *
     * @param platform 0:安卓 1:苹果 2：安卓钱包 3：苹果钱包 4:安卓理财APP 5:苹果理财APP 6亿豚安卓钱包 7亿豚苹果钱包 8 DCCPay安卓钱包 9 DCCPay苹果钱包
     * @return
     */
    @RequestMapping("/system/app/version/{id}")
    public MessageResult sysHelp(@PathVariable(value = "id") Platform platform) {
        MessageResult result = MessageResult.success();
        result.setData(appRevisionService.findRecentVersion(platform));
        return result;
    }

    /**
     * 上币中心
     *
     * @param appId 终端类型，0-web;1-app
     * @return
     */
    @ApiOperation(value = "上币中心",tags = "上币中心",notes = "PC、APP端获取上币中心信息")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "终端类型，0-web;1-app",name = "appId",dataType = "int",required = true)
    })
    @PostMapping("/currency/center")
    public MessageResult currencyCenter(@RequestParam(value = "appId") Integer appId) {
        MessageResult result = MessageResult.success();
        result.setData(agreementService.currencyCenter(appId));
        return result;
    }

}
