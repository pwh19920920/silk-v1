package com.spark.bitrade.controller.partner;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.BaseController;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.model.response.PartnerAreaBuilder;
import com.spark.bitrade.model.response.PartnerBusinessDetailBuilder;
import com.spark.bitrade.model.screen.MemberScreen;
import com.spark.bitrade.model.screen.PartnerAreaScreen;
import com.spark.bitrade.model.screen.PartnerBusinessDetailScreen;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.ExcelUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import com.spark.bitrade.vo.PartnerAreaVo;
import com.spark.bitrade.vo.WithdrawRecordVO;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static com.spark.bitrade.entity.QMember.member;


/**
  * 合伙人管理
  * @author tansitao
  * @time 2018/5/28 14:10 
  */
@RestController
@RequestMapping("/partner")
public class PartnerController extends BaseController {
    @Autowired
    private PartnerAreaService partnerAreaService;

    @Autowired
    private PartnerBusinessService partnerBusinessService;

    @Autowired
    private PartnerBusinessMonthService partnerBusinessMonthService;

    @Autowired
    private RewardRecordService rewardRecordService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private GyDmcodeService gyDmcodeService;

  /**
   * 添加合伙人
   * @author tansitao
   * @time 2018/5/28 18:43 
   */
    @PostMapping("create")
    public MessageResult create(
            @RequestParam long memberId,
            @RequestParam String areaId,
            @RequestParam("level") PartnerLevle level) {
        DimArea dimArea = gyDmcodeService.findOneByDmCode(areaId);
        Member meber = memberService.findOne(memberId);
        Assert.notNull(dimArea,"区域不存在");
        Assert.notNull(meber,"会员不存在");

        MessageResult ms = MessageResult.success("添加合伙人成功");
        if(((level.getOrdinal() + 1) == dimArea.getLevel()) || (level.getOrdinal() >= 2  && dimArea.getLevel() ==3 ))
        {
            PartnerArea partnerArea = partnerAreaService.findPartnerAreaByMemberIdOrAreaId(memberId, dimArea.getAreaId(), PartnerStaus.normal.getOrdinal() + "");
            Assert.isNull(partnerArea,"该区域已有合伙人，或者该用户已是合伙人");
            meber.setAreaId(areaId);
            partnerArea = new PartnerArea();
            partnerArea.setDimArea(dimArea);
            partnerArea.setMember(meber);
            partnerArea.setLevel(level);
            partnerArea.setPartnerStaus(PartnerStaus.normal);
            partnerArea.setCreatTime(new Date());
            partnerArea = partnerAreaService.save(partnerArea);

            ms.setData(partnerArea.getId());
        }
        else
        {
            return  error("合伙人等级和区域设置异常");
        }
        return ms;
    }

    /**
     * 修改合伙人
     * @author tansitao
     * @time 2018/5/28 19:28 
     */
//    @RequiresPermissions("update")
    @PostMapping("update")
    public MessageResult update(@RequestParam long id,
                                @RequestParam("status") PartnerStaus status) {
        PartnerArea partnerArea = partnerAreaService.findById(id);
        Assert.notNull(partnerArea, "合伙人不存在");
        //如果是将合伙人改为正常，需要判断该区域是否已有正常的合伙人
        if(PartnerStaus.normal == status)
        {
            if(partnerAreaService.findByAreaAndStatus(partnerArea.getDimArea(),status) == null)
            {
                partnerArea.setPartnerStaus(status);
            }
            else
            {
                throw new IllegalArgumentException("该区域已有合伙人，请先禁用该区域合伙人");
            }
        }
        partnerArea.setPartnerStaus(status);
        partnerAreaService.save(partnerArea);
        return success();
    }

    /**
     * 分页查询用户合伙人
     * @author tansitao
     * @time 2018/5/28 17:13 
     */
    @RequiresPermissions("partner:page-query")
    @PostMapping("page-query")
    public MessageResult page(
            PageModel pageModel, PartnerAreaScreen partnerAreaScreen) {
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if(!StringUtils.isEmpty(partnerAreaScreen.getAreaId()))
        {
            booleanExpressions.add(QPartnerArea.partnerArea.dimArea.areaId.like(partnerAreaScreen.getAreaId() + "%"));
        }
        if (!StringUtils.isEmpty(partnerAreaScreen.getAccount()))
        {
            booleanExpressions.add(QPartnerArea.partnerArea.member.username.like("%" + partnerAreaScreen.getAccount() + "%")
                    .or(QPartnerArea.partnerArea.member.mobilePhone.like(partnerAreaScreen.getAccount() + "%"))
                    .or(QPartnerArea.partnerArea.member.email.like(partnerAreaScreen.getAccount() + "%"))
                    .or(QPartnerArea.partnerArea.member.realName.like("%" + partnerAreaScreen.getAccount() + "%"))
                    .or(QPartnerArea.partnerArea.member.id.like("%" + partnerAreaScreen.getAccount() + "%")));
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

        return success(scanPartnerAreaBuilder);
    }


    @RequiresPermissions("partner:out-excel")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.FINANCE, operation = "导出合伙人 Excel")
    public void outExcel(@RequestParam(required = false) String account,@RequestParam(required = false) String areaId,
                         HttpServletResponse response) throws IOException {

        List<PartnerAreaVo> list=partnerAreaService.findPartnerForout(account,areaId);
        String fileName="partnerArea_"+ DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,PartnerAreaVo.class.getDeclaredFields(),response,fileName);
//        ExcelUtil.listToExcel(list,PartnerAreaVo.class.getDeclaredFields(),response.getOutputStream(),PartnerAreaVo.class.getName());
    }

    /**
      * 分页查询用户
      * @author tansitao
      * @time 2018/5/28 17:13 
      */
    @RequiresPermissions("partner:member-page-query")
    @PostMapping("member-page-query")
    @ResponseBody
    @AccessLog(module = AdminModule.MEMBER, operation = "分页查找会员Member")
    public MessageResult page(
            PageModel pageModel,
            MemberScreen screen) {
        Predicate predicate = getPredicate(screen);
        Page<Member> all = memberService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }


    private Predicate getPredicate(MemberScreen screen) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (screen.getStatus() != null)
            booleanExpressions.add(member.certifiedBusinessStatus.eq(screen.getStatus()));
        if (screen.getStartTime() != null)
            booleanExpressions.add(member.registrationTime.goe(screen.getStartTime()));
        if (screen.getEndTime() != null){
            Calendar calendar = Calendar.getInstance() ;
            calendar.setTime(screen.getEndTime());
            calendar.add(Calendar.DAY_OF_YEAR,1);
            booleanExpressions.add(member.registrationTime.lt(calendar.getTime()));
        }

        if (!StringUtils.isEmpty(screen.getAccount()))
            booleanExpressions.add(member.username.like("%" + screen.getAccount() + "%")
                    .or(member.mobilePhone.like(screen.getAccount() + "%"))
                    .or(member.email.like(screen.getAccount() + "%"))
                    .or(member.realName.like("%" + screen.getAccount() + "%"))
                    //add by shenzucai 时间： 2018.05.25 原因：添加会员id查询
                    .or(member.id.like("%" + screen.getAccount() + "%")));
        if(screen.getCommonStatus()!=null)
            booleanExpressions.add(member.status.eq(screen.getCommonStatus()));
        return PredicateUtils.getPredicate(booleanExpressions);
    }


    /**
     * 查看合伙人详情
     * @author tansitao
     * @time 2018/5/29 9:20 
     * @param id
     */
//    @RequiresPermissions("detail")
    @PostMapping("detail")
    public MessageResult detail(
            @RequestParam Long id) {
        PartnerArea partnerArea = partnerAreaService.findById(id);
        Assert.notNull(partnerArea, "合伙人不存在");
        return success(partnerArea);
    }

    /**
      * 查看合伙人业务累计收益数据
      * @author tansitao
      * @time 2018/5/29 9:20 
     * @param areaId
     */
//    @RequiresPermissions("businessCollect")
    @PostMapping("businessTotal")
    public MessageResult businessTotal(
            @RequestParam String areaId) {
        PartnerBusiness partnerBusiness = partnerBusinessService.findPartnerBusinessByAreaId(areaId);
//        Assert.notNull(partnerBusiness, "该合伙人暂无收益数据");
        return success(partnerBusiness);
    }

    /**
      * 查看合伙人业务月统计数据
      * @author tansitao
      * @time 2018/5/29 9:20 
     */
    @PostMapping("businessMonthTotal")
    public MessageResult businessMonthTotal(
            @RequestParam String areaId,
            @RequestParam String collectTime) {
        PartnerBusinessMonth partnerBusinessMonth = partnerBusinessMonthService.findPartnerPartnerBusinessMonth(areaId, collectTime);
//        Assert.notNull(partnerBusinessMonth, "该合伙人暂无收益数据");
        return success(partnerBusinessMonth);
    }

    /**
      * 查看合伙人业务月收益详细数据
      * @author tansitao
      * @time 2018/5/29 9:20 
      */
    @PostMapping("businessDetail")
    public MessageResult businessDetail(
            PageModel pageModel, PartnerBusinessDetailScreen screen) {
        Assert.isTrue(screen.getMemberId() != 0, "合伙人id不能为null");
        //申明返回数据
        List<PartnerBusinessDetailBuilder> pbDtailBuilderList = new ArrayList<PartnerBusinessDetailBuilder>();
        Member froemMember = null;
        //条件,包含用户id，佣金类型，时间
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        booleanExpressions.add(QRewardRecord.rewardRecord.member.id.eq(screen.getMemberId()));
        booleanExpressions.add(QRewardRecord.rewardRecord.type.eq(RewardRecordType.PARTNER));
        if (!org.springframework.util.StringUtils.isEmpty(screen.getStartTime()))
        {
            booleanExpressions.add(QRewardRecord.rewardRecord.createTime.gt(screen.getStartTime()));
        }
        if (!org.springframework.util.StringUtils.isEmpty(screen.getEndTime()))
        {
            booleanExpressions.add(QRewardRecord.rewardRecord.createTime.lt(screen.getEndTime()));
        }
        //根据条件查询合伙人详细分页数据
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
      * 查看全国合伙人业务累计统计数据
      * @author tansitao
      * @time 2018/5/29 9:20 
     */
    @RequiresPermissions("partner:nationalBusinessTotal")
    @PostMapping("nationalBusinessTotal")
    public MessageResult nationalBusinessTotal() {
        String areaId = "86";
        PartnerBusiness partnerBusiness = partnerBusinessService.findPartnerBusinessByAreaId(areaId);
//        Assert.notNull(partnerBusiness, "全国合伙人业务累计数据暂无");
        return success(partnerBusiness);
    }


}
