package com.spark.bitrade.controller.member;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.config.AliyunConfig;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.model.screen.MemberApplicationScreen;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.AliyunUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import com.spark.bitrade.util.ValidateUtil;
import com.spark.bitrade.vendor.provider.SMSProvider;
import com.spark.bitrade.vendor.provider.SMSProviderProxy;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;

import static com.spark.bitrade.entity.QMemberApplication.memberApplication;
import static org.springframework.util.Assert.notNull;

/**
 * @author rongyu
 * @description 实名审核单
 * @date 2017/12/26 15:05
 */
@RestController
@RequestMapping("member/member-application")
public class MemberApplicationController extends BaseAdminController {

    private Logger logger = LoggerFactory.getLogger(MemberApplicationController.class);

    @Autowired
    private MemberApplicationService memberApplicationService;
    @Autowired
    private AliyunConfig aliyunConfig;

    @Autowired
    private MemberService memberService;
    /**
     * 添加restful请求
     */
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GyDmcodeService gyDmcodeService;

    @Autowired
    private SMSProviderProxy smsProvider ;


    @RequiresPermissions("member:member-application:all")
    @PostMapping("all")
    @AccessLog(module = AdminModule.MEMBER, operation = "所有会员MemberApplication认证信息")
    public MessageResult all() {
        List<MemberApplication> all = memberApplicationService.findAll();
        if (all != null && all.size() > 0){
            return success(all);
        }
        return error("数据不存在,没有数据!");
    }

    /**
     * 会员实名认证详情查询
     * @param id
     * @return
     */
    @RequiresPermissions("member:member-application-detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.MEMBER, operation = "会员MemberApplication认证信息详情")
    public MessageResult detail(@RequestParam("id") Long id) {
        MemberApplication memberApplication = memberApplicationService.findOne(id);
        //edit by yangch 时间： 2018.04.20 原因：OSS配置
        // 设置URL过期时间
//        Date expiration = new Date(new Date().getTime() + aliyunConfig.getOverTime() * 60 * 1000);
        // 生成认证后的身份证图片url，正面、手持、反面
        try
        {
            //edit by tansitao 时间： 2018/5/20 原因：修改为从亚马逊获取地址
//            AwsS3Util awsS3Util = new AwsS3Util(awsConfig.getAccessKeyId(), awsConfig.getAccessKeySecret(), awsConfig.getEndpoint(), awsConfig.getBucketName(), awsConfig.getRegion());
//            String idcadImgFrontUri = AwsS3Util.getPrivateUrl(memberApplication.getIdentityCardImgFront(), awsConfig.getOverTime());//edit by tansitao 时间： 2018/5/20 原因：修改为从亚马逊获取地址
//            String idcadImgInHandUri = AwsS3Util.getPrivateUrl(memberApplication.getIdentityCardImgInHand(), awsConfig.getOverTime());//edit by tansitao 时间： 2018/5/20 原因：修改为从亚马逊获取地址
//            String idcadImgReverseUri = AwsS3Util.getPrivateUrl(memberApplication.getIdentityCardImgReverse(), awsConfig.getOverTime());//edit by tansitao 时间： 2018/5/20 原因：修改为从亚马逊获取地址

            //add by tansitao 时间： 2018/7/21 原因：修改为从阿里云oss获取地址
            String idcadImgFrontUri = AliyunUtil.getPrivateUrl(aliyunConfig, memberApplication.getIdentityCardImgFront());
            //add by tansitao 时间： 2018/7/21 原因：修改为从阿里云oss获取地址
            String idcadImgInHandUri = AliyunUtil.getPrivateUrl(aliyunConfig, memberApplication.getIdentityCardImgInHand());
            //add by tansitao 时间： 2018/7/21 原因：修改为从阿里云oss获取地址
            String idcadImgReverseUri = AliyunUtil.getPrivateUrl(aliyunConfig, memberApplication.getIdentityCardImgReverse());

            memberApplication.setIdentityCardImgFront(idcadImgFrontUri);
            memberApplication.setIdentityCardImgInHand(idcadImgInHandUri);
            memberApplication.setIdentityCardImgReverse(idcadImgReverseUri);
        }
        catch (Exception e)
        {
            return error("OSS获取认证信息失败!");
        }

        notNull(memberApplication, "validate id!");
        return success(memberApplication);
    }

    @RequiresPermissions("member:member-application-page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.MEMBER, operation = "分页查找会员MemberApplication认证信息")
    public MessageResult queryPage(PageModel pageModel, MemberApplicationScreen screen) {
        List<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (screen.getAuditStatus() != null){
            booleanExpressions.add(memberApplication.auditStatus.eq(screen.getAuditStatus()));
        }
        if (!StringUtils.isEmpty(screen.getAccount())){
            booleanExpressions.add(memberApplication.member.username.like("%" + screen.getAccount() + "%")
                    //.or(memberApplication.member.mobilePhone.like(screen.getAccount() + "%"))
                   // .or(memberApplication.member.email.like(screen.getAccount() + "%"))
                    .or(memberApplication.member.realName.like("%" + screen.getAccount() + "%")));
        }
        if(!StringUtils.isEmpty(screen.getCardNo())){
            booleanExpressions.add(memberApplication.member.idNumber.like("%" + screen.getCardNo() + "%"));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<MemberApplication> all = memberApplicationService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }

    @RequiresPermissions("member:member-application-pass")
    @PatchMapping("{id}/pass")
    @AccessLog(module = AdminModule.MEMBER, operation = "会员MemberApplication认证通过审核")
    public MessageResult pass(@PathVariable("id") Long id) {
        //校验
        MemberApplication application = memberApplicationService.findOne(id);
        notNull(application, "validate id!");
        //业务
        //add by shenzucai 时间： 2018.04.24 原因：实名认证唯一校验 start
        Member member = application.getMember();
        if(member == null){
            return error("用户不存在");
        }
        //edit by yangch 时间： 2018.04.26 原因：目前不做身份证重复认证
        //会员身份证号码
        // edit by wushangyun 事件：2019-1-23 原因：实名身份证认证可重复认证
        List<Member> members = memberService.findAllByIdNumber(application.getIdCard());
        // if(members != null && members.size() > 0){
        //     return  error("身份证号已存在");
        // }
        //add by shenzucai 时间： 2018.04.24 原因：实名认证唯一校验 end
        //2为人工审核标识
        // add by wsy 事件：2019-1-23 原因：实名认证重复认证标记
        application.setRepeatAudit(members != null && members.size() > 0 ? 1 : 0);
        memberApplicationService.auditPass(application,2);

        //add by  shenzucai 时间： 2018.05.29  原因：增加用户注册时的IP进行归属地域的设置 start
        //edit by tansitao 时间： 2018/6/28 原因：如果该用户没有定位则获取定位信息
        if(StringUtils.isEmpty(member.getAreaId()))
        {
            DimArea dimArea = gyDmcodeService.getPostionInfo(member.getIdNumber(), member.getMobilePhone(), member.getIp());
            if(dimArea != null)
            {
                member.setAreaId(dimArea.getAreaId());
            }
        }
        //add by  shenzucai 时间： 2018.05.29  原因：增加用户注册时的IP进行归属地域的设置 end

        memberService.save(member);

        // edit by wsy date: 2019-1-23 10:17:32 reason:重复实名认证不发放奖励
        if (members == null || members.size() <= 0) {
            //add by fumy date:2018.10.12 reason:发放实名认证用户奖励
            memberApplicationService.handleActivityForRealName(member);
            //add by fumy date:2018.10.12 reason:发放实名认证推荐用户返佣
            Member memberPromotion = null;
            if (member.getInviterId() != null) {
                memberPromotion = memberService.findOne(member.getInviterId());
            }
            memberApplicationService.PromotionForRealName(member, memberPromotion);
        }
        //add by  shenzucai 时间： 2018.05.29  原因：增加用户注册时的IP进行归属地域的设置 end

        //如果实名的用户手机号不为空，给审核成功的会员发送信息
        if(member.getMobilePhone() !=null){
            sendMessage(member.getMobilePhone(),member.getCountry());
        }
        //返回
        return success();
    }

    @RequiresPermissions("member:member-application-no-pass")
    @PatchMapping("{id}/no-pass")
    @AccessLog(module = AdminModule.MEMBER, operation = "会员MemberApplication认证不通过审核")
    public MessageResult noPass(
            @PathVariable("id") Long id,
            @RequestParam(value = "rejectReason", required = false) String rejectReason) {
        //校验
        MemberApplication application = memberApplicationService.findOne(id);
        notNull(application, "validate id!");
        //业务
        //拒绝原因
        application.setRejectReason(rejectReason);
        memberApplicationService.auditNotPass(application);
        //返回
        return success();
    }

    /**
     * 人工确认系统审核通过的实名认证
     * @author fumy
     * @time 2018.09.06 17:57
     * @param id
     * @return true
     */
    @GetMapping("{id}/confirm")
    public MessageResult personConfirm(@PathVariable("id") Long id){
        boolean res = memberApplicationService.personConfirm(id);
        if (res){ return success();
        }
        return error("人工确认失败");
    }

    /**
     * 实名审核结果，发送短信给指定用户
     * @author fumy
     * @time 2018.07.10 16:46
     * @param phone
     * @param country
     * @return true
     */
    // @Async
    public void sendMessage(String phone,Country country){
        try {
            String content = "【SilkTrader】平台已审核完成您提交的身份认证信息！请登录到[个人中心]进行查看。祝您生活愉快！";
            if (country.getAreaCode().equals("86")) {
                smsProvider.sendMessage(phone, content);
            } else {
                smsProvider.sendInternationalMessage(content, country.getAreaCode() + phone);
            }
        }catch (Exception e){
            logger.debug("【实名认证】用户{}发送实名认证短信失败",phone);
        }

    }

}
