package com.spark.bitrade.controller.member;

import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.dto.MemberInfoDTO;
import com.spark.bitrade.entity.ExchangeMemberDiscountRule;
import com.spark.bitrade.entity.MemberSecuritySet;
import com.spark.bitrade.model.MemberDiscountRuleScreen;
import com.spark.bitrade.model.screen.MemberScreen;
import com.spark.bitrade.dto.MemberDTO;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.MemberWallet;
import com.spark.bitrade.service.MemberPermissionService;
import com.spark.bitrade.service.MemberSecuritySetService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.service.MemberWalletService;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.MemberVO;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static com.spark.bitrade.constant.CertifiedBusinessStatus.*;
import static com.spark.bitrade.constant.MemberLevelEnum.IDENTIFICATION;
import static com.spark.bitrade.entity.QMember.member;
import static com.spark.bitrade.util.MessageResult.error;
import static com.spark.bitrade.util.MessageResult.success;
import static org.springframework.util.Assert.*;

/**
 * @author rongyu
 * @description 后台管理会员
 * @date 2017/12/25 16:50
 */
@RestController
@RequestMapping("/member")
public class MemberController extends BaseAdminController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private IdWorkByTwitter idWorkByTwitter;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private MemberSecuritySetService memberSecuritySetService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MemberPermissionService memberPermissionService;

    @RequiresPermissions("member:all")
    @PostMapping("all")
    @AccessLog(module = AdminModule.MEMBER, operation = "所有会员Member")
    public MessageResult all() {
        List<Member> all = memberService.findAll();
        if (all != null && all.size() > 0) {
            return success(all);
        }
        return error("请求数据发生错误");
    }

    @RequiresPermissions("member:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.MEMBER, operation = "会员交易记录详情")
    public MessageResult detail(@RequestParam("id") Long id) {
        Member member = memberService.findOne(id);
        notNull(member, "validate id!");
        List<MemberWallet> list = memberWalletService.findAllByMemberId(member.getId());
        MemberSecuritySet memberSecuritySet = memberSecuritySetService.findOneBymemberId(id);
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMember(member);
        memberDTO.setList(list);
        memberDTO.setMemberSecuritySet(memberSecuritySet);
        return success(memberDTO);
    }

    @RequiresPermissions("member:delete")
    @PostMapping("delete")
    @AccessLog(module = AdminModule.MEMBER, operation = "删除会员Member")
    public MessageResult delete(@RequestParam(value = "id") Long id) {
        Member member = memberService.findOne(id);
        notNull(member, "validate id!");
        // 修改状态非法
        member.setStatus(CommonStatus.ILLEGAL);
        memberService.save(member);
        return success();
    }

    @RequiresPermissions("member:update")
    @PostMapping(value = "update")
    @AccessLog(module = AdminModule.MEMBER, operation = "更新会员Member")
    public MessageResult update(Member member) {
        if (member.getId() == null){
            return error("id必须传参");
        }
        Member one = memberService.findOne(member.getId());
        if (one == null){
            return error("用户不存在");
        }
        if (StringUtils.isNotBlank(member.getUsername())){
            one.setUsername(member.getUsername());
        }
        if (StringUtils.isNotBlank(member.getPassword()))
        {
            //edit by yangch 时间： 2018.04.20 原因： 重置会员密码
            //不可重复随机数
            String loginNo = String.valueOf(idWorkByTwitter.nextId());
            //盐
            String credentialsSalt = ByteSource.Util.bytes(loginNo).toHex().toLowerCase();
            try
            {
                String password = new SimpleHash("md5", member.getPassword(), credentialsSalt, 2).toHex().toLowerCase();
                one.setPassword(password);
                one.setSalt(credentialsSalt);
            }
            catch (Exception e)
            {
                return error(e.getMessage());
            }
        }
        if (StringUtils.isNotBlank(member.getRealName())){
            one.setRealName(member.getRealName());
        }
        Member save = memberService.save(one);
        return success(save);
    }

    @RequiresPermissions("member:audit-business")
    @PatchMapping("{id}/audit-business")
    @AccessLog(module = AdminModule.MEMBER, operation = "会员Member认证商家")
    public MessageResult auditBusiness(

            @PathVariable("id") Long id,
            @RequestParam("status") CertifiedBusinessStatus status) {
        Member member = memberService.findOne(id);
        notNull(member, "validate id!");
        //确认是审核中
        isTrue(member.getCertifiedBusinessStatus() == AUDITING, "validate member certifiedBusinessStatus!");
        //确认传入certifiedBusinessStatus值正确
        isTrue(status == NOT_CERTIFIED || status == VERIFIED, "validate certifiedBusinessStatus!");
        //member.setCertifiedBusinessApplyTime(new Date());//time
        if (status == VERIFIED) {
            //通过
            //已认证
            member.setCertifiedBusinessStatus(VERIFIED);
            //认证商家
            member.setMemberLevel(IDENTIFICATION);
        } else {
            //不通过
            //未认证
            member.setCertifiedBusinessStatus(NOT_CERTIFIED);
        }
        memberService.save(member);
        return success();
    }

    @RequiresPermissions("member:page")
    @PostMapping("page-query")
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
        if (screen.getStatus() != null){
            booleanExpressions.add(member.certifiedBusinessStatus.eq(screen.getStatus()));
        }
        if (screen.getStartTime() != null){
            booleanExpressions.add(member.registrationTime.goe(screen.getStartTime()));
        }
        if (screen.getEndTime() != null){
            Calendar calendar = Calendar.getInstance() ;
            calendar.setTime(screen.getEndTime());
            calendar.add(Calendar.DAY_OF_YEAR,1);
            booleanExpressions.add(member.registrationTime.lt(calendar.getTime()));
        }

        if (!StringUtils.isEmpty(screen.getAccount())){
            booleanExpressions.add(member.username.like("%" + screen.getAccount() + "%")
                    .or(member.mobilePhone.like(screen.getAccount() + "%"))
                    .or(member.email.like(screen.getAccount() + "%"))
                    .or(member.realName.like("%" + screen.getAccount() + "%"))
                    //add by shenzucai 时间： 2018.05.25 原因：添加会员id查询
                    .or(member.id.like("%" + screen.getAccount() + "%")));
        }
        if(screen.getCommonStatus()!=null){
            booleanExpressions.add(member.status.eq(screen.getCommonStatus()));
        }
        if (screen.getTransactionStatus()!=null){
            booleanExpressions.add(member.transactionStatus.eq(screen.getTransactionStatus()));
        }

        return PredicateUtils.getPredicate(booleanExpressions);
    }

    /**
     * 导出会员
     * @author Zhang Yanjun
     * @time 2018.08.20 17:26
     * @param commonStatus 会员状态
     * @param account
     * @param response
     * @return
    */
    @RequiresPermissions("member:out-excel")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.MEMBER, operation = "导出会员Member Excel")
    public void outExcel(Integer commonStatus,String account,HttpServletResponse response) throws Exception {
        List<MemberVO> list = memberService.findByMemberAllForOut(commonStatus,account);
        String fileName="member_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,MemberVO.class.getDeclaredFields(),response,fileName);
//        ExcelUtil.listToExcel(list,MemberVO.class.getDeclaredFields(),response.getOutputStream());
    }

    //add by yangch 时间： 2018.04.24 原因：合并新增
    //edit by yangch 时间： 2018.04.26 原因：合并修改
    @RequiresPermissions("member:alter-publish-advertisement-status")
    @PostMapping("alter-publish-advertisement-status")
    @AccessLog(module = AdminModule.SYSTEM,operation = "禁用/解禁发布广告")
    public MessageResult publishAdvertise(@RequestParam("memberId")Long memberId,
                                          @RequestParam("status")BooleanEnum status){
        Member member = memberService.findOne(memberId);
        if(member.getCertifiedBusinessStatus()!=CertifiedBusinessStatus.VERIFIED) {
            return error("请先认证商家");
        }
        Assert.notNull(member,"商家不存在");
        member.setPublishAdvertise(status);
        memberService.save(member);
        return success(status==BooleanEnum.IS_FALSE?"禁止发布广告成功":"解除禁止成功");
    }

    //add by yangch 时间： 2018.04.26 原因：合并新增
    @RequiresPermissions("member:alter-status")
    @PostMapping("alter-status")
    @AccessLog(module = AdminModule.SYSTEM,operation = "禁用/解禁会员账号")
    public MessageResult ban(@RequestParam("status")CommonStatus status,
                             @RequestParam("memberId")Long memberId){
        Member member = memberService.findOne(memberId) ;

        member.setStatus(status);
        memberService.save(member);
        return success(status==CommonStatus.ILLEGAL?"禁用成功":"解禁成功");
    }

    //add by yangch 时间： 2018.04.26 原因：合并新增
    @RequiresPermissions("member:alter-transaction-status")
    @PostMapping("alter-transaction-status")
    @AccessLog(module = AdminModule.SYSTEM,operation = "禁用/解禁会员交易")
    public MessageResult alterTransactionStatus(@RequestParam("status")BooleanEnum status,
                                                @RequestParam("memberId")Long memberId){
        Member member = memberService.findOne(memberId) ;
        //>>> edit by zyj 2018.11.30 禁止交易后下架广告 start
        //禁止交易
        if (status == BooleanEnum.IS_FALSE) {
            memberPermissionService.forbidTrade(member);
        }else {
            //允许交易
            memberPermissionService.allowTrade(member);
        }
        //<<< edit by zyj 2018.11.30 禁止交易后下架广告 end
//        member.setTransactionStatus(status);
//        memberService.save(member);
        return success(status==BooleanEnum.IS_FALSE?"禁止交易成功":"解除禁止成功");
    }


    /**
     * 解绑谷歌，手机 验证状态
     * @param id
     * @param openColumnType
     * @return
     */
    @RequiresPermissions("member:close-security")
    @PatchMapping("{id}/close-security")
    @AccessLog(module = AdminModule.MEMBER, operation = "会员Member谷歌与手机验证解绑")
    public MessageResult closeSecurity(
            @PathVariable("id") Long id,
            @RequestParam("openColumnType") int openColumnType) {
        Member member = memberService.findOne(id);
        notNull(member, "validate id!");
        memberSecuritySetService.updateSecurityStatus(member.getId(), id,openColumnType);//edit by tansitao 时间： 2018/8/16 原因：清空缓存
        return success();
    }

    /**重置用户交易资金密码
     * lingxing
     * @param jyPassword
     * @param userId
     * @return
     * @throws Exception
     */
    @RequiresPermissions("member:reset-jypwd")
    @RequestMapping("transaction/password")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult approveTransaction(String jyPassword, Long userId) throws Exception {
        Assert.notNull(jyPassword,"资金交易密码不能空");
        Assert.notNull(userId,"资金交易密码不能空");
        Member member = memberService.findOne(userId);
        Assert.notNull(member,"用户ID不存在");
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        member.setJyPassword(jyPass);
        memberService.save(member);
        return MessageResult.success("修改资金密码成功");
    }


    /**
     * 修改会员信息
     * @author fumy
     * @time 2018.09.06 10:04
     * @param memberInfo
     * @return true
     */
    @RequiresPermissions("member:update")
    @RequestMapping("memberInfo-changes")
    @Transactional(rollbackFor = Exception.class)
    @AccessLog(module = AdminModule.MEMBER, operation = "修改会员信息")
    public MessageResult memberInfo(MemberInfoDTO memberInfo) throws Exception {
        Member member= memberService.findOne(memberInfo.getId());
        member.setUsername(memberInfo.getUsername());
        member.setGoogleKey(memberInfo.getGoogleKey());
        member.setUsername(memberInfo.getUsername());
        member.setGoogleDate(memberInfo.getGoogleDate());
        member.setGoogleState(memberInfo.getGoogleState());
        member.setRealName(memberInfo.getRealName());
        member.setIdNumber(memberInfo.getIdNumber());
        member.setEmail(memberInfo.getEmail());
        member.setMobilePhone(memberInfo.getMobilePhone());
        member.setMemberLevel(memberInfo.getMemberLevel());
        member.setStatus(memberInfo.getStatus());
        member.setBankInfo(memberInfo.getBankInfo());
        member.setCertifiedBusinessStatus(memberInfo.getCertifiedBusinessStatus());
        member.setRealNameStatus(memberInfo.getRealNameStatus());
        memberService.save(member);
        return MessageResult.success("修改用户信息成功");
    }


    /**
     * 一键设置会员成为商家
     * @author fumy
     * @time 2018.09.06 9:47
     * @param id
     * @return true
     */
    @RequiresPermissions("member:set-business")
    @RequestMapping("business/set")
    @Transactional(rollbackFor = Exception.class)
    @AccessLog(module = AdminModule.MEMBER, operation = "一键设置会员成为商家")
    public MessageResult becomeBusiness(Long id){
        Member member =  memberService.findOne(id);
        if(member!=null){
            //已经是认证商家
            if(member.getCertifiedBusinessStatus() == CertifiedBusinessStatus.VERIFIED && member.getMemberLevel() == MemberLevelEnum.IDENTIFICATION){
                return error("该会员已经是商家");
            }
            member.setCertifiedBusinessStatus(CertifiedBusinessStatus.VERIFIED);
            member.setMemberLevel(MemberLevelEnum.IDENTIFICATION);
            memberService.save(member);
            return success();
        }
        return error("会员不存在");
    }

//    /**
//     * 查询会员详细信息
//     * @author fumy
//     * @time 2018.09.06 10:00
//     * @param id
//     * @return true
//     */
//    @PostMapping("memberInfo-detail")
//    public MessageResult memberInfoDetail(Long id) throws Exception {
//        MemberInfoDTO memberInfoDTO= memberService.findByMemberInfoId(id);
//        return success(memberInfoDTO);
//    }


    /**
     *
     * 获取会员优惠规则
     * @author fumy
     * @time 2018.08.29 16:17
     * @param memberId
     * @return true
     */
    @RequiresPermissions("member:discount-rule-page")
    @GetMapping("discountRule/page-query")
    public MessageResult getMemberDiscountInfo(Long memberId,String symbol,int pageNo,int pageSize){
        PageInfo<ExchangeMemberDiscountRule> pageInfo = memberService.discountRulePageInfo(memberId, symbol,PageData.pageNo4PageHelper(pageNo),pageSize);
        return success(PageData.toPageData(pageInfo));
    }

    /**
     * 刷新会员优惠规则缓存
     * @author fumy
     * @time 2018.08.29 16:30
     * @param memberId
     * @return true
     */
    @RequiresPermissions("member:discount-rule-flush")
    @GetMapping("discountRule/flush")
    public MessageResult flushDiscountRuleCache(Long memberId){
        String serviceName = "bitrade-market";
        String url = "http://" + serviceName + "/market/flushMemberDiscountRuleCache?memberId=";
        ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class, memberId);
        MessageResult mr = result.getBody();
        if(mr.getCode() == 0){
            return success();
        }
        return error("刷新失败");
    }

    /**
     * 添加会员优惠规则
     * @author fumy
     * @time 2018.08.29 16:31
     * @param screen
     * @return true
     */
    @RequiresPermissions("member:discount-rule-add")
    @PostMapping("discountRule/add")
    @AccessLog(module = AdminModule.MEMBER, operation = "添加会员优惠规则")
    public MessageResult addDiscountRule(MemberDiscountRuleScreen screen){
        isTrue(memberService.countByMemberIdAndSymbol(screen.getMemberId(), screen.getSymbol()),"会员已存在该交易对的优惠");
        ExchangeMemberDiscountRule rule =new ExchangeMemberDiscountRule();
        rule.setMemberId(screen.getMemberId());
        rule.setEnable(screen.getEnable()==1?BooleanEnum.IS_TRUE:BooleanEnum.IS_FALSE);
        rule.setFeeBuyDiscount(screen.getBuyDiscount());
        rule.setFeeSellDiscount(screen.getSellDiscount());
        rule.setNote(screen.getNote());
        rule.setSymbol(screen.getSymbol());
        rule = memberService.addDiscountRule(rule);
        return success(rule);
    }

    /**
     * 修改会员优惠规则
     * @author fumy
     * @time 2018.08.29 16:35
     * @param screen
     * @return true
     */
    @RequiresPermissions("member:discount-rule-update")
    @PostMapping("discountRule/update")
    @AccessLog(module = AdminModule.MEMBER, operation = "修改会员优惠规则")
    public MessageResult updateDiscountRule(MemberDiscountRuleScreen screen){
        ExchangeMemberDiscountRule rule =new ExchangeMemberDiscountRule();
        rule.setId(screen.getId());
        rule.setMemberId(screen.getMemberId());
        rule.setEnable(screen.getEnable()==1?BooleanEnum.IS_TRUE:BooleanEnum.IS_FALSE);
        rule.setFeeBuyDiscount(screen.getBuyDiscount());
        rule.setFeeSellDiscount(screen.getSellDiscount());
        rule.setNote(screen.getNote());
        rule.setSymbol(screen.getSymbol());
        rule = memberService.updateDiscountRule(rule);
        return success(rule);
    }
}

