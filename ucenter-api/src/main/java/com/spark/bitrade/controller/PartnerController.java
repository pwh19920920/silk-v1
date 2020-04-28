package com.spark.bitrade.controller;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static org.springframework.util.Assert.isTrue;

/**
  * 合伙人信息接口
  * @author tansitao
  * @time 2018/5/30 15:35 
  */
@RestController
@RequestMapping("/partner")
@Slf4j
public class PartnerController {
    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private PartnerAreaService partnerAreaService;

    @Autowired
    private PartnerBusinessService partnerBusinessService;

    @Autowired
    private PartnerBusinessMonthService partnerBusinessMonthService;

    @Autowired
    private RewardRecordService rewardRecordService;

    @Autowired
    private GyDmcodeService gyDmcodeService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private RedisTemplate redisTemplate ;

    /**
      * 通过APP查看合伙人信息
      * @author tansitao
      * @time 2018/5/29 9:20 
     * @param user
     */
    @PostMapping("appUserInfo")
    public MessageResult appUserInfo(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Member member = new Member();
        member.setId(user.getId());
        PartnerArea partnerArea = partnerAreaService.findByMemberAndStatus(member);
        Assert.notNull(partnerArea, msService.getMessage("NOT_PARTNER"));
        PartnerInfo partnerInfo = new PartnerInfo();
        partnerInfo.setArea(partnerArea.getDimArea().getAreaName());
        partnerInfo.setLevel(partnerArea.getLevel());
        PartnerBusiness partnerBusiness = partnerBusinessService.findPartnerBusinessByAreaId(partnerArea.getDimArea().getAreaId());
        //判断合伙人是否有收益
        if(partnerBusiness != null)
        {
            partnerInfo.setAllAddUserNum(partnerBusiness.getAllAddUserNum());
            partnerInfo.setAllIncomeAmount(partnerBusiness.getAllIncomeAmount());
            partnerInfo.setAllTradeAmount(partnerBusiness.getAllTradeAmount());
            partnerInfo.setMonthAddUserNum(partnerBusiness.getMonthAddUserNum());
            partnerInfo.setMonthIncomeAmount(partnerBusiness.getMonthIncomeAmount());
            partnerInfo.setMonthTradeAmount(partnerBusiness.getMonthTradeAmount());
        }

        MessageResult mr = MessageResult.success();
        mr.setData(partnerInfo);
        return mr;
    }

    /**
      * 查看合伙人信息
      * @author tansitao
      * @time 2018/5/29 9:20 
     * @param user
     */
    @PostMapping("userInfo")
    public MessageResult detail(@SessionAttribute(SESSION_MEMBER) AuthMember user,long id) {
        Member member = new Member();
        member.setId(id);
        PartnerArea partnerArea = partnerAreaService.findByMemberAndStatus(member);
        Assert.notNull(partnerArea, msService.getMessage("NOT_PARTNER"));
        MessageResult mr = MessageResult.success();
        mr.setData(partnerArea);
        return mr;
    }

    /**
      * 查看合伙人业务累计收益数据
      * @author tansitao
      * @time 2018/5/29 9:20 
     * @param areaId
     */
    @PostMapping("businessTotal")
    public MessageResult businessTotal(
            @SessionAttribute(SESSION_MEMBER) AuthMember user,
            @RequestParam String areaId) {
        PartnerBusiness partnerBusiness = partnerBusinessService.findPartnerBusinessByAreaId(areaId);
//        Assert.notNull(partnerBusiness, msService.getMessage("DO_NOT_HAVE_DATA"));
        MessageResult mr = MessageResult.success();
        mr.setData(partnerBusiness);
        return mr;
    }


    /**
      * 查看合伙人业务月统计数据
      * @author tansitao
      * @time 2018/5/29 9:20 
      */
    @PostMapping("businessMonthTotal")
    public MessageResult businessMonthTotal(
            @SessionAttribute(SESSION_MEMBER) AuthMember user,
            @RequestParam String areaId,
            @RequestParam String collectTime) {
        PartnerBusinessMonth partnerBusinessMonth = partnerBusinessMonthService.findPartnerPartnerBusinessMonth(areaId, collectTime);
//        Assert.notNull(partnerBusinessMonth, msService.getMessage("DO_NOT_HAVE_DATA"));
        MessageResult mr = MessageResult.success();
        mr.setData(partnerBusinessMonth);
        return mr;
    }


    /**
      * 查看合伙人业务月收益详细数据
      * @author tansitao
      * @time 2018/5/29 9:20 
      */
    @PostMapping("businessDetail")
    public MessageResult businessDetail(@SessionAttribute(SESSION_MEMBER) AuthMember user,
                                        PageModel pageModel, PartnerBusinessDetailScreen screen) throws Exception{
        //条件,包含用户id，佣金类型，时间
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        booleanExpressions.add(QRewardRecord.rewardRecord.member.id.eq(screen.getMemberId()));
        booleanExpressions.add(QRewardRecord.rewardRecord.type.eq(RewardRecordType.PARTNER));
        if (!org.springframework.util.StringUtils.isEmpty(screen.getStartTime()))
        {
            booleanExpressions.add(QRewardRecord.rewardRecord.createTime.gt(DateUtil.YYYY_MM_DD_MM_HH_SS.parse(screen.getStartTime() + " 00:00:00")));
        }
        if (!org.springframework.util.StringUtils.isEmpty(screen.getEndTime()))
        {
            booleanExpressions.add(QRewardRecord.rewardRecord.createTime.lt(DateUtil.YYYY_MM_DD_MM_HH_SS.parse(screen.getEndTime() + " 00:00:00")));
        }
        //根据条件查询合伙人详细分页数据，
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<PartnerBusinessDetailBuilder> rewardRecordPage =  rewardRecordService.findAll(predicate, pageModel.getPageable()).map(x ->
                PartnerBusinessDetailBuilder.builder()
                        .userName(memberService.findOne(x.getFromMemberId()).getUsername())
                        .symbol(x.getFromCoinUnit())
                        .time(x.getCreateTime())
                        .incomeAmount(x.getFromAmount())
                        .status(x.getStatus())
                        .build()
        );
        MessageResult mr = MessageResult.success("success");
        mr.setData(rewardRecordPage);
        return mr;
    }


    /**
      * 获取区域组织架构信息
      * @author tansitao
      * @time 2018/5/28 17:17 
      */
    @PostMapping("allArea")
    public MessageResult allArea(@RequestParam(value = "areaId") String areaId) {
        //从redis获取数据
        ListOperations listOperations = redisTemplate.opsForList();
        List<DimArea> resutAreaList = new ArrayList<DimArea>();
        List<DimArea> areaList = listOperations.range(SysConstant.AREA_PREFIX + "areaId",0,-1);
        //如果没有从redis获取到数据从数据库获取
        if(areaList == null ||areaList.size() == 0)
        {
            //如果从数据库获取到数据，将数据存入redis
            areaList = gyDmcodeService.findAllByFatherId(areaId);
            if(areaList != null && areaList.size() > 0)
            {
                listOperations.leftPushAll(SysConstant.AREA_PREFIX + areaId, areaList);
            }

        }
        //将无数据的区域取消掉
        if(areaList != null)
        {
            for (DimArea dimArea: areaList)
            {
                PartnerArea partnerArea = partnerAreaService.findByDimAreaEndingWith(dimArea);
                if(partnerArea != null)
                {
                    resutAreaList.add(dimArea);
                }
            }
        }

        MessageResult mr = MessageResult.success("success");
        mr.setData(resutAreaList);
        return mr;
    }

    @PostMapping("partnerStatus")
    public MessageResult partnerStatus() {
        MessageResult mr = MessageResult.success("success");
        mr.setData(PartnerLevle.values());
        return mr;
    }


    /**
      * 分页查询用户合伙人
      * @author tansitao
      * @time 2018/5/28 17:13 
      */
    @PostMapping("page-query")
    public MessageResult page(@SessionAttribute(SESSION_MEMBER) AuthMember user,
            PageModel pageModel, PartnerAreaScreen screen) {
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        Assert.notNull(screen.getAreaId(), msService.getMessage("NOT_HAVE_AREA"));
        booleanExpressions.add(QPartnerArea.partnerArea.partnerStaus.eq(PartnerStaus.normal));
        if(!org.apache.commons.lang.StringUtils.isEmpty(screen.getAreaId()))
        {
            booleanExpressions.add(QPartnerArea.partnerArea.dimArea.areaId.like(screen.getAreaId() + "%"));
        }

        if(! (screen.getLevel() == null))
        {
            booleanExpressions.add(QPartnerArea.partnerArea.level.eq(screen.getLevel()));
        }

        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<PartnerAreaBuilder> scanPartnerAreaBuilder = partnerAreaService.findAll(predicate, pageModel.getPageable()).map(x ->
                PartnerAreaBuilder.builder().memberId(x.getMember().getId())
                        .partnerId(x.getId())
                        .realName(x.getMember().getRealName())
                        .mobilePhone(x.getMember().getMobilePhone())
                        .userName(x.getMember().getUsername())
                        .level(x.getLevel())
                        .area(x.getDimArea().getAreaName())
                        .status(x.getPartnerStaus())
                        .build()
        );

        MessageResult mr = MessageResult.success("success");
        mr.setData(scanPartnerAreaBuilder);
        return mr;
    }
}
