package com.spark.bitrade.controller;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.dto.LockCoinActivitieProjectDto;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.vo.LockActivitySettingBuilder;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

/**
 * 活动中心
 * @author zhou haifeng
 * @time 2019.10.23 15:32
 */
@RestController
@RequestMapping("/LockActivt")
@Slf4j
public class LockActivtController {

    @Autowired
    private LockCoinActivitieProjectService lockCoinActivitieProjectService;

    @Autowired
    private LockCoinDetailService lockCoinDetailService;

    @Autowired
    private LockCoinActivitieSettingService lockCoinActivitieSettingService;

    @Autowired
    private ISilkDataDistService silkDataDistService;

    @Autowired
    private LocaleMessageSourceService msService;

    @ApiOperation(value = "活动中心详情接口")
    @RequestMapping("/lockActivtList")
    public MessageRespResult<LinkedHashSet<LockCoinActivitieProjectDto>> lockActivtList(Long memberId) {
        Long start = System.currentTimeMillis();
        List<LockCoinActivitieProjectDto> list =  lockCoinActivitieProjectService.listAll();
        List<Long> ids = new ArrayList<>();
        for(LockCoinActivitieProjectDto a:list){
            //拼接活动id
            ids.add(a.getId());
            //判断活动是否过期
            if(a.getStatus().getOrdinal() == 2){//活动状态已失效，优先判断状态
                a.setIsOverdue(1);
            }else {
                if(a.getStartTime().compareTo(new Date()) > 0){
                    a.setIsOverdue(2);
                }else if(a.getEndTime().compareTo(new Date()) < 0){
                    a.setIsOverdue(1);
                }else{
                    a.setIsOverdue(0);
                }
            }
        }
        List<Long> activieList = new ArrayList<>();
        List<LockCoinActivitieSetting> settingList = lockCoinActivitieProjectService.selectByActivitieIds(ids);
        for(LockCoinActivitieSetting setting:settingList){
            activieList.add(setting.getId());
        }
        //统计活动参与人数
        List<LockCoinActivitieProjectDto> listCount = lockCoinActivitieProjectService.memberCount(activieList);
        for(LockCoinActivitieProjectDto dtoCount:listCount){
            for(LockCoinActivitieProjectDto dto:list){
                if(dto.getId() == dtoCount.getId()){
                    dto.setMemberTotal(dtoCount.getMemberTotal());
                }
            }
        }
        LinkedHashSet<LockCoinActivitieProjectDto> setResult = new LinkedHashSet<>();
        //进行中活动(isOverdue==0)
        Set<LockCoinActivitieProjectDto> setResult0 = new HashSet<>();
        //预热中(未开始)的活动(isOverdue==2)
        Set<LockCoinActivitieProjectDto> setResult2 = new HashSet<>();
        //已结束的活动(isOverdue==1)
        Set<LockCoinActivitieProjectDto> setResult1 = new HashSet<>();
        if(memberId == null){//未登录的用户剔除已结束活动
            for(LockCoinActivitieProjectDto project:list){
                if(project.getIsOverdue() == 1){
                    continue;
                }else if(project.getIsOverdue() == 0){
                    setResult0.add(project);
                }else if(project.getIsOverdue() == 2){
                    setResult2.add(project);
                }
                //setResult.add(project);
            }
        }else {//已登录用户剔除自身未参加的已结束的活动
            List<LockCoinDetail> detailList = lockCoinDetailService.selectByMemberId(memberId);
            if(detailList.size() == 0){
                for(LockCoinActivitieProjectDto project:list){
                    if(project.getIsOverdue() == 2){
                        setResult2.add(project);
                    }else if (project.getIsOverdue() == 0){
                        setResult0.add(project);
                    }
                }
            }else {
                for(LockCoinDetail lockCoinDetail:detailList){
                    for(LockCoinActivitieProjectDto project:list){
                        if(project.getIsOverdue() == 2){
                            setResult2.add(project);
                        }else if (project.getIsOverdue() == 0){
                            setResult0.add(project);
                        }else if (project.getIsOverdue() == 1){
                            LockCoinActivitieSetting setting = null;
                            if(lockCoinDetail.getRefActivitieId() != null){
                                setting = lockCoinActivitieSettingService.findOne(lockCoinDetail.getRefActivitieId());
                            }
                            if(setting == null){
                                continue;
                            }
                            if(setting.getActivitieId() == project.getId()){
                                setResult1.add(project);
                            }
                        }
                    }
                }
            }
        }
        //判断当前是何种语言环境
        String accept = null;
        try {
            Class holderClass = Class.forName("org.springframework.web.context.request.RequestContextHolder");
            Method currentRequestAttributes = ReflectionUtils.findMethod(holderClass, "currentRequestAttributes");
            Object requestAttributes = ReflectionUtils.invokeMethod(currentRequestAttributes, null);
            Method request = ReflectionUtils.findMethod(requestAttributes.getClass(), "getRequest");
            HttpServletRequest httpServletRequest = (HttpServletRequest) ReflectionUtils.invokeMethod(request, requestAttributes);
            accept = httpServletRequest.getHeader("language");
        } catch (Exception e) {
            log.error("获取请求头的异常", e);
        }

        //按照进行中——预热中——已结束活动状态排序
        setResult.addAll(setResult0);
        setResult.addAll(setResult2);
        if(setResult1.size() != 0){
            setResult.addAll(setResult1);
        }
        if(accept == null || "zh_CN".equals(accept)){//简体中文

        }else if("zh_HK".equals(accept)){//繁体中文
            for(LockCoinActivitieProjectDto dto:setResult){
                LockCoinActivitieProjectInternational international = lockCoinDetailService.selectInternational(dto.getId(),1);
                if(null != international){
                    dto.setName(international.getName());
                    dto.setImgUrl(international.getImgUrl());
                    dto.setIncomeImg(international.getIncomeImg());
                    dto.setTitleImg(international.getTitleImg());
                    dto.setBriefDescription(international.getBriefDescription());
                    dto.setDescription(international.getDescription());
                }
            }

        }else if("en_US".equals(accept)){//英文
            for(LockCoinActivitieProjectDto dto:setResult){
                LockCoinActivitieProjectInternational international = lockCoinDetailService.selectInternational(dto.getId(),2);
                if(null != international){
                    dto.setName(international.getName());
                    dto.setImgUrl(international.getImgUrl());
                    dto.setIncomeImg(international.getIncomeImg());
                    dto.setTitleImg(international.getTitleImg());
                    dto.setBriefDescription(international.getBriefDescription());
                    dto.setDescription(international.getDescription());
                }
            }
        }else if("ko_KR".equals(accept)){//韩文
            for(LockCoinActivitieProjectDto dto:setResult){
                LockCoinActivitieProjectInternational international = lockCoinDetailService.selectInternational(dto.getId(),3);
                if(null != international){
                    dto.setName(international.getName());
                    dto.setImgUrl(international.getImgUrl());
                    dto.setIncomeImg(international.getIncomeImg());
                    dto.setTitleImg(international.getTitleImg());
                    dto.setBriefDescription(international.getBriefDescription());
                    dto.setDescription(international.getDescription());
                }
            }
        }
        Long end = System.currentTimeMillis();
        System.out.println(end-start);
        MessageRespResult mr = MessageRespResult.success();
        mr.setData(setResult);
        return mr;
    }

    private LockCoinActivitieProjectDto getGoldenKey(){
        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("GOLDEN_KEY", "SETTING_ID");
        SilkDataDist symbolDist = silkDataDistService.findByIdAndKey("GOLDEN_KEY", "BASE_SYMBOL");
        LockCoinActivitieProjectDto dto = new LockCoinActivitieProjectDto();
        if (dataDist != null && dataDist.getStatus() == BooleanEnum.IS_TRUE && symbolDist != null && symbolDist.getStatus() == BooleanEnum.IS_TRUE) {
            long settingId = ConvertUtils.lookup(Long.class).convert(Long.class, dataDist.getDictVal());
            LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOne(settingId);
            LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(lockCoinActivitieSetting.getActivitieId());
            BeanUtils.copyProperties(lockCoinActivitieProject, dto);
            dto.setType(lockCoinActivitieProject.getType().getOrdinal());
            dto.setLockDays(lockCoinActivitieSetting.getLockDays().toString());
            dto.setMonthRate(lockCoinActivitieSetting.getEarningRate());
        }
        return dto;
    }

    private LockCoinActivitieProjectDto getIeo(){
        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("IEO_ACTIVITY_BCC", "SETTING_ID");
        SilkDataDist symbolDist = silkDataDistService.findByIdAndKey("IEO_ACTIVITY_BCC", "BASE_SYMBOL");
        LockCoinActivitieProjectDto dto = new LockCoinActivitieProjectDto();
        if (dataDist != null && dataDist.getStatus() == BooleanEnum.IS_TRUE && symbolDist != null && symbolDist.getStatus() == BooleanEnum.IS_TRUE) {
            long settingId = ConvertUtils.lookup(Long.class).convert(Long.class, dataDist.getDictVal());
            LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOne(settingId);
            LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(lockCoinActivitieSetting.getActivitieId());
            BeanUtils.copyProperties(lockCoinActivitieProject, dto);
            dto.setType(lockCoinActivitieProject.getType().getOrdinal());
            dto.setLockDays(lockCoinActivitieSetting.getLockDays().toString());
            dto.setMonthRate(lockCoinActivitieSetting.getEarningRate());
        }
        return dto;
    }

    private LockCoinActivitieProjectDto getBcc(){
        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("BCC_ENERGIZE", "SETTING_ID");
        SilkDataDist symbolDist = silkDataDistService.findByIdAndKey("BCC_ENERGIZE", "BASE_SYMBOL");
        LockCoinActivitieProjectDto dto = new LockCoinActivitieProjectDto();
        if (dataDist != null && dataDist.getStatus() == BooleanEnum.IS_TRUE && symbolDist != null && symbolDist.getStatus() == BooleanEnum.IS_TRUE) {
            long settingId = ConvertUtils.lookup(Long.class).convert(Long.class, dataDist.getDictVal());
            LockCoinActivitieSetting lockCoinActivitieSetting = lockCoinActivitieSettingService.findOne(settingId);
            LockCoinActivitieProject lockCoinActivitieProject = lockCoinActivitieProjectService.findOne(lockCoinActivitieSetting.getActivitieId());
            BeanUtils.copyProperties(lockCoinActivitieProject, dto);
            dto.setType(lockCoinActivitieProject.getType().getOrdinal());
            dto.setLockDays(lockCoinActivitieSetting.getLockDays().toString());
            dto.setMonthRate(lockCoinActivitieSetting.getEarningRate());
        }
        return dto;
    }

    @ApiOperation(value = "活动中心单个详情接口")
    @GetMapping("/findLockActivtProject")
    public MessageRespResult<LockCoinActivitieProjectDto> findLockActivtProject(Long activitieId) {
        LockCoinActivitieProjectDto dto = lockCoinActivitieProjectService.findProjectById(activitieId);
        List<LockCoinActivitieSetting> listSetting = lockCoinActivitieSettingService.findByActivitieId(activitieId);
        //判断是否有子活动配置
        if (listSetting != null ) {
            dto.setMonthRate(listSetting.get(0).getEarningRate());
            dto.setLockDays(listSetting.get(0).getLockDays().toString());
        }
        MessageRespResult mr = MessageRespResult.success();
        mr.setData(dto);
        return mr;
    }
}
