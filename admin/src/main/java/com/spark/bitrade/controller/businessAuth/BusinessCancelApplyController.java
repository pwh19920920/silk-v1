package com.spark.bitrade.controller.businessAuth;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.BaseController;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.exception.UnexpectedException;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

import static com.spark.bitrade.constant.CertifiedBusinessStatus.*;
import static org.springframework.util.Assert.isTrue;

@Api(description = "取消商家类")
@RestController
@RequestMapping("business/cancel-apply")
@Slf4j
public class BusinessCancelApplyController extends BaseController {
    private static Logger logger = LoggerFactory.getLogger(BusinessCancelApplyController.class);
    @Autowired
    private UnlockCoinApplyService unlockCoinApplyService;
    @Autowired
    private LockCoinDetailService lockCoinDetailService;
    @Autowired
    private BusinessAuthApplyService businessAuthApplyService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UnLockCoinDetailService unLockCoinDetailService;
    @Autowired
    private AdvertiseService advertiseService;

    @PostMapping("page-query")
    @RequiresPermissions("business:cancel-apply-page-query")
    public MessageResult pageQuery(
            PageModel pageModel,
            @RequestParam(value = "account", required = false) String account,
            @RequestParam(value = "status", required = false) BusinessApplyStatus status,
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date startDate,
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date endDate) {
        List<BooleanExpression> predicates = new ArrayList<>();
        if (!StringUtils.isEmpty(account))
            predicates.add(QUnlockCoinApply.unlockCoinApply.member.username.like("%" + account + "%")
                    .or(QUnlockCoinApply.unlockCoinApply.member.mobilePhone.like("%" + account + "%"))
                    .or(QUnlockCoinApply.unlockCoinApply.member.email.like("%" + account + "%"))
                    .or(QUnlockCoinApply.unlockCoinApply.member.realName.like("%" + account + "%")));
        predicates.add(QUnlockCoinApply.unlockCoinApply.status.in(BusinessApplyStatus.APPLYING, BusinessApplyStatus.NOPASS, BusinessApplyStatus.PASS));
        if (status != null)
            predicates.add(QUnlockCoinApply.unlockCoinApply.status.eq(status));
        if (startDate != null)
            predicates.add(QUnlockCoinApply.unlockCoinApply.applyTime.goe(startDate));
        if (endDate != null)
            predicates.add(QUnlockCoinApply.unlockCoinApply.applyTime.loe(endDate));

        Page<UnlockCoinApply> page = unlockCoinApplyService.findAll(PredicateUtils.getPredicate(predicates), pageModel);

        for (UnlockCoinApply unlockCoinApply : page.getContent()) {
            LockCoinDetail lockCoinDetail = lockCoinDetailService.findOne(unlockCoinApply.getLockCoinDetailId());
            unlockCoinApply.setLockCoinDetail(lockCoinDetail);
        }
        return success(page);
    }

    /**
     * 退保审核接口
     *
     * @param id
     * @param success 通过 : IS_TRUE
     * @param reason  审核不通过的理由
     * @return
     */
    @RequiresPermissions("business:cancel-apply-check")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id"),
            @ApiImplicitParam(name = "success", value = "审核是否通过 0否 1是"),
            @ApiImplicitParam(name = "refusalReason", value = "拒绝原因", defaultValue = ""),
            @ApiImplicitParam(name = "reason", value = "审核通过提示", defaultValue = "")
    })
    @PostMapping("check")
    @AccessLog(module = AdminModule.MEMBER, operation = "会员Member认证商家退保审核")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult pass(Long id, BooleanEnum success,String refusalReason,String reason,@SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) {
        UnlockCoinApply unlockCoinApply = unlockCoinApplyService.findById(id);
        Member member = unlockCoinApply.getMember();
        List<BusinessAuthApply> businessAuthApplyList = businessAuthApplyService.findByMemberAndCertifiedBusinessStatus(member, VERIFIED);
        if (businessAuthApplyList == null || businessAuthApplyList.size() < 1) {
            return error("data exception,businessAuthApply not exist。。。。");
        }
        BusinessAuthApply businessAuthApply = businessAuthApplyList.get(0);
        /**
         * 处理 取消申请 日志
         */
        unlockCoinApply.setCompleteTime(DateUtil.getCurrentDate());
        unlockCoinApply.setLockCoinDetailId(businessAuthApply.getLockCoinDetailId());

        unlockCoinApply.setRefusalReason(refusalReason);
        //>>> add by zyj 2018.12.24 : 添加审核人员信息 start
        unlockCoinApply.setAdminRealName(admin.getRealName());
        unlockCoinApply.setAdminMobilePhone(admin.getMobilePhone());
        //>>> add by zyj 2018.12.24 : 添加审核人员信息 end
        if (success == BooleanEnum.IS_TRUE) {
            unlockCoinApply.setReason(reason);
            unlockCoinApply.setStatus(BusinessApplyStatus.PASS);
            unlockCoinApplyService.save(unlockCoinApply);

            //取消商家认证 审核通过，
            //member.setCertifiedBusinessStatus(RETURN_SUCCESS);//未认证
            member.setCertifiedBusinessStatus(NOT_CERTIFIED);
            member.setMemberLevel(MemberLevelEnum.REALNAME);
            memberService.save(member);
            LockCoinDetail lockCoinDetail = lockCoinDetailService.findOne(unlockCoinApply.getLockCoinDetailId());
            if(lockCoinDetail != null)
            {
                BigDecimal deposit = BigDecimal.ZERO;
                lockCoinDetail.setRemainAmount(BigDecimal.ZERO);
                lockCoinDetail.setStatus(LockStatus.UNLOCKED);
                lockCoinDetail.setCancleTime(new Date());
                lockCoinDetail.setUnlockTime(new Date());
            }

            /**
             * 退回保证金
             */
            if (businessAuthApplyList != null && businessAuthApplyList.size() > 0) {
                MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(businessAuthApply.getBusinessAuthDeposit().getCoin().getUnit(), member.getId());
                //将保证金退回给商家,
                MessageResult result = memberWalletService.thawBalanceFromLockBlance(memberWallet, businessAuthApply.getAmount());
                if (result.getCode() != 0)
                {
                    throw new IllegalArgumentException("退回保证金失败");
                }

                UnlockCoinDetail unlockCoinDetail = new UnlockCoinDetail();
                unlockCoinDetail.setAmount(businessAuthApply.getAmount());
                unlockCoinDetail.setLockCoinDetailId(lockCoinDetail.getId());
                String serviceName = "bitrade-market";
                String url = "http://" + serviceName + "/market/exchange-rate/usd/" + lockCoinDetail.getCoinUnit();
                try
                {
                    ResponseEntity<MessageResult> pricResult = restTemplate.getForEntity(url, MessageResult.class);
                    MessageResult mr = pricResult.getBody();
                    log.info("=========查询" + lockCoinDetail.getCoinUnit() + "价格后返回的结果{}=========", mr.getCode()+ "===" + mr.getMessage());
                    if (mr.getCode() == 0)
                    {
                        unlockCoinDetail.setPrice(BigDecimal.valueOf(Double.parseDouble(mr.getData().toString())));
                    }

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                MemberTransaction memberTransaction = new MemberTransaction();
                memberTransaction.setAmount(businessAuthApply.getAmount());
                memberTransaction.setMemberId(member.getId());
                memberTransaction.setCreateTime(new Date());
                memberTransaction.setType(TransactionType.BUSINESS_DEPOSIT);
                memberTransaction.setSymbol(businessAuthApply.getBusinessAuthDeposit().getCoin().getUnit());
                memberTransactionService.save(memberTransaction);
            }
            /**
             * 更改认证申请状态
             */
            return MessageResult.success(msService.getMessage("PASS_THE_AUDIT"), refusalReason);
        } else {
            //审核不通过，商家 维持已认证状态
            //member.setCertifiedBusinessStatus(RETURN_FAILED);
            member.setCertifiedBusinessStatus(VERIFIED);
            member.setMemberLevel(MemberLevelEnum.IDENTIFICATION);
            memberService.save(member);

            unlockCoinApply.setStatus(BusinessApplyStatus.NOPASS);
            unlockCoinApplyService.save(unlockCoinApply);

            return MessageResult.success(msService.getMessage("AUDIT_DOES_NOT_PASS"), refusalReason);
        }
    }

    /**
     * 后台人员强制取消会员商家
     * @author Zhang Yanjun
     * @time 2018.12.24 14:04
     * @param memberId
     * @param isBack
     * @param admin
     */
    @ApiOperation(value = "后台人员强制取消会员商家")
    @RequiresPermissions("business:auth-cancelForce")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memberId", value = "会员id"),
            @ApiImplicitParam(name = "isBack", value = "是否退回保证金 0否 1是"),
            @ApiImplicitParam(name = "isBusi", value = "是否为一键成为商家 0否 1是"),
            @ApiImplicitParam(name = "reason", value = "提示", defaultValue = "")
    })
    @PostMapping("cancelForce")
    @AccessLog(module = AdminModule.MEMBER, operation = "后台人员强制取消会员商家")
    @Transactional(rollbackFor = Exception.class)
    public MessageRespResult cancelForce(Long memberId,BooleanEnum isBack,String reason,BooleanEnum isBusi,@SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) {
        Assert.notNull(memberId, msService.getMessage("ID_ILLEGAL"));
        Member member = memberService.findOne(memberId);
        Assert.notNull(member,msService.getMessage("MEMBER_NOT_EXIST"));
        Assert.isTrue(member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.VERIFIED),msService.getMessage("BUSINESS_STATE_ERROR"));

        //下架广告  清空该广告剩余量,解冻钱包
        List<Advertise> list = advertiseService.getAllOnAdvertiseByMemberId(memberId);
        if (list.size() > 0){
            for (Advertise one : list){
                if (one.getStatus().equals(AdvertiseControlStatus.PUT_ON_SHELVES)){
                    try {
                        int row = advertiseService.putOffShelves(one);
                        if (row == 0){
                            return MessageRespResult.error("下架广告失败");
                        }
                    } catch (UnexpectedException e) {
                        e.printStackTrace();
                        throw new IllegalArgumentException(e.getMessage());
                    }
                }
            }
        }

        member.setCertifiedBusinessStatus(CANCEL_FORCE);
        member.setMemberLevel(MemberLevelEnum.REALNAME);
        memberService.save(member);

        //一键成为商家
        if (isBusi.equals(BooleanEnum.IS_TRUE)){
            return MessageRespResult.success("取消成功");
        }else {

            UnlockCoinApply unlockCoinApply = new UnlockCoinApply();
            unlockCoinApply.setMember(member);
            unlockCoinApply.setStatus(BusinessApplyStatus.PASS);
            unlockCoinApply.setApplyReason("后台强制取消");
            unlockCoinApply.setApplyTime(new Date());
            unlockCoinApply.setReason(reason);
            unlockCoinApply.setCompleteTime(new Date());
            unlockCoinApply.setAdminRealName(admin.getRealName());
            unlockCoinApply.setAdminMobilePhone(admin.getMobilePhone());

            BusinessAuthApply businessAuthApply = businessAuthApplyService.findOneByMemberIdDesc(memberId);
            Assert.notNull(businessAuthApply, "data exception,businessAuthApply not exist。。。。");
            Assert.isTrue(businessAuthApply.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.VERIFIED), msService.getMessage("BUSINESS_STATE_ERROR"));

            LockCoinDetail lockCoinDetail = lockCoinDetailService.findOne(businessAuthApply.getLockCoinDetailId());
            if (lockCoinDetail != null) {
                lockCoinDetail.setStatus(LockStatus.UNLOCKED);
                lockCoinDetail.setRemainAmount(businessAuthApply.getAmount());
                lockCoinDetail.setCancleTime(new Date());
                lockCoinDetail.setUnlockTime(new Date());
            }
            unlockCoinApply.setLockCoinDetailId(lockCoinDetail.getId());

            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setMemberId(member.getId());
            memberTransaction.setAmount(BigDecimal.ZERO);
            memberTransaction.setCreateTime(new Date());
            memberTransaction.setType(TransactionType.BUSINESS_DEPOSIT);
            memberTransaction.setSymbol(businessAuthApply.getBusinessAuthDeposit().getCoin().getUnit());

            //解锁记录
            UnlockCoinDetail unlockCoinDetail = new UnlockCoinDetail();
            unlockCoinDetail.setAmount(BigDecimal.ZERO);
            unlockCoinDetail.setLockCoinDetailId(lockCoinDetail.getId());

            //是否退回保证金 (在选择了保证金策略的情况下)
            if (isBack == BooleanEnum.IS_TRUE && businessAuthApply.getBusinessAuthDeposit() != null) {
                //是
                lockCoinDetail.setRemainAmount(BigDecimal.ZERO);
                //退回保证金
                MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(businessAuthApply.getBusinessAuthDeposit().getCoin().getUnit(), member.getId());
                //将保证金退回给商家
                MessageResult result = memberWalletService.thawBalanceFromLockBlance(memberWallet, businessAuthApply.getAmount());
                if (result.getCode() != 0) {
                    throw new IllegalArgumentException("退回保证金失败");
                }
                unlockCoinDetail.setAmount(businessAuthApply.getAmount());
                memberTransaction.setAmount(businessAuthApply.getAmount());
            }
            String serviceName = "bitrade-market";
            String url = "http://" + serviceName + "/market/exchange-rate/usd/" + lockCoinDetail.getCoinUnit();
            try {
                ResponseEntity<MessageResult> pricResult = restTemplate.getForEntity(url, MessageResult.class);
                MessageResult mr = pricResult.getBody();
                log.info("=========查询" + lockCoinDetail.getCoinUnit() + "价格后返回的结果{}=========", mr.getCode() + "===" + mr.getMessage());
                if (mr.getCode() == 0) {
                    unlockCoinDetail.setPrice(BigDecimal.valueOf(Double.parseDouble(mr.getData().toString())));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            unLockCoinDetailService.save(unlockCoinDetail);

            memberTransactionService.save(memberTransaction);
            lockCoinDetailService.save(lockCoinDetail);

            unlockCoinApply.setLockCoinDetail(lockCoinDetail);
            unlockCoinApplyService.save(unlockCoinApply);
            return MessageRespResult.success("取消成功");
        }
    }

    /**
     * @param id:businessCancelApply id
     * @return
     */
    @PostMapping("detail")
    @RequiresPermissions("business:cancel-apply-detail")
    public MessageResult detail(@RequestParam(value = "id") Long id) {
        UnlockCoinApply unlockCoinApply = unlockCoinApplyService.findById(id);
        LockCoinDetail lockCoinDetail = lockCoinDetailService.findOne(unlockCoinApply.getLockCoinDetailId());
        Map<String, Object> map1 = unlockCoinApplyService.getBusinessOrderStatistics(unlockCoinApply.getMember().getId());
        logger.info("会员订单信息:{}", map1);
        Map<String, Object> map2 = unlockCoinApplyService.getBusinessAppealStatistics(unlockCoinApply.getMember().getId());
        logger.info("会员申诉信息:{}", map2);
        Long advertiseNum = unlockCoinApplyService.getAdvertiserNum(unlockCoinApply.getMember().getId());
        logger.info("会员广告信息:{}", advertiseNum);
        Map<String, Object> map = new HashMap<>();
        map.putAll(map1);
        map.putAll(map2);
        map.put("advertiseNum", advertiseNum);
        map.put("businessCancelApply", unlockCoinApply);
        map.put("depositRecord", lockCoinDetail);
        logger.info("会员退保相关信息:{}", map);
        return success(map);
    }

    @PostMapping("get-search-status")
    public MessageResult getSearchStatus() {
        CertifiedBusinessStatus[] statuses = CertifiedBusinessStatus.values();
        List<Map> list = new ArrayList<>();
        for (CertifiedBusinessStatus status : statuses) {
            if (status.getOrdinal() < CertifiedBusinessStatus.CANCEL_AUTH.getOrdinal()) {
                continue;
            }
            Map map = new HashMap();
            map.put("name", status.getCnName());
            map.put("value", status.getOrdinal());
            list.add(map);
        }
        return success(list);
    }
}
