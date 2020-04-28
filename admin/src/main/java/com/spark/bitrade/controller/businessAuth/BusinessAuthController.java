package com.spark.bitrade.controller.businessAuth;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

import static com.spark.bitrade.constant.CertifiedBusinessStatus.*;
import static com.spark.bitrade.constant.CertifiedBusinessStatus.FAILED;
import static com.spark.bitrade.constant.CertifiedBusinessStatus.VERIFIED;
import static com.spark.bitrade.constant.MemberLevelEnum.IDENTIFICATION;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * 商家认证可用保证金类型
 *
 * @author zhang yingxin
 * @date 2018/5/5
 */
@RestController
@RequestMapping("business-auth")
@Slf4j
public class BusinessAuthController extends BaseAdminController {
    private static Logger logger = LoggerFactory.getLogger(BusinessAuthController.class);
    @Autowired
    private BusinessAuthDepositService businessAuthDepositService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private BusinessAuthApplyService businessAuthApplyService;
    @Autowired
    private  MemberService memberService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private LockCoinDetailService lockCoinDetailService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UnlockCoinApplyService unlockCoinApplyService;

    /**
     * 保证金管理分页
     * @param pageModel
     * @param status
     */
    @RequiresPermissions("business:auth-deposit-page-query")
    @GetMapping("page")
    public MessageResult getAll(PageModel pageModel, CommonStatus status) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        QBusinessAuthDeposit businessAuthDeposit = QBusinessAuthDeposit.businessAuthDeposit;
        if (status != null) {
            booleanExpressions.add(businessAuthDeposit.status.eq(status));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<BusinessAuthDeposit> depositPage = businessAuthDepositService.findAll(predicate, pageModel);
        MessageResult result = MessageResult.success();
        result.setData(depositPage);
        return result;
    }

    @RequiresPermissions("business:auth-deposit-create")
    @PostMapping("create")
    public MessageResult create(@SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin,
                                @RequestParam("amount") Double amount,
                                @RequestParam("coinUnit") String coinUnit) {
        Coin coin = coinService.findByUnit(coinUnit);
        if (coin == null) {
            return error("validate coinUnit");
        }
        BusinessAuthDeposit businessAuthDeposit = new BusinessAuthDeposit();
        businessAuthDeposit.setAmount(new BigDecimal(amount));
        businessAuthDeposit.setCoin(coin);
        businessAuthDeposit.setCreateTime(new Date());
        businessAuthDeposit.setAdmin(admin);
        businessAuthDeposit.setStatus(CommonStatus.NORMAL);
        businessAuthDepositService.save(businessAuthDeposit);
        return success();
    }



    /**
     * 审核认证商家
     * @author tansitao
     * @time 2018/6/9 10:51 
     */
    @RequiresPermissions("business:apply-audit-business")
    @PostMapping("audit-business")
    @AccessLog(module = AdminModule.MEMBER, operation = "会员Member认证商家")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult auditBusiness(
            @RequestParam("id") Long id,
            @RequestParam("status") CertifiedBusinessStatus status,
            @RequestParam("detail") String detail,
            @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) {
        Member member = memberService.findOne(id);
        notNull(member, "validate id!");
        //确认是审核中
        isTrue(member.getCertifiedBusinessStatus() == AUDITING, "用户商家状态异常!");
        //确认传入certifiedBusinessStatus值正确，审核通过或者不通过
        isTrue(status == VERIFIED || status == FAILED, "传入的商家状态异常!");
        //member.setCertifiedBusinessApplyTime(new Date());//time
        List<BusinessAuthApply> businessAuthApplyList = businessAuthApplyService.findByMemberAndCertifiedBusinessStatus(member, AUDITING);
        if (status == VERIFIED) {
            //通过
            member.setCertifiedBusinessStatus(VERIFIED);//已认证
            member.setMemberLevel(IDENTIFICATION);//认证商家
            if (businessAuthApplyList != null && businessAuthApplyList.size() > 0) {
                BusinessAuthApply businessAuthApply = businessAuthApplyList.get(0);
                businessAuthApply.setCertifiedBusinessStatus(VERIFIED);
                //>>> add by zyj 2018.12.24 : 添加审核人员信息 start
                businessAuthApply.setAdminRealName(admin.getRealName());
                businessAuthApply.setAdminMobilePhone(admin.getMobilePhone());
                businessAuthApply.setAuditingTime(new Date());
                //<<< add by zyj 2018.12.24 : 添加审核人员信息 end
                //如果申请的时候选择了保证金策略
                if (businessAuthApply.getBusinessAuthDeposit() != null) {
//                    //扣除保证金
//                    MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(businessAuthApply.getBusinessAuthDeposit().getCoin().getUnit(), member.getId());
//                    //冻结保证金需要的金额
//                    MessageResult result = memberWalletService.freezeBalanceToLockBalance(memberWallet, businessAuthApply.getAmount());
//                    if (result.getCode() != 0)
//                    {
//                        throw new IllegalArgumentException("余额不足");
//                    }
                    //添加锁仓记录，调用market的汇率信息获取
                    LockCoinDetail lockCoinDetail = new LockCoinDetail();
                    String unit = businessAuthApply.getBusinessAuthDeposit().getCoin().getUnit();
                    String serviceName = "bitrade-market";
                    String url = "http://" + serviceName + "/market/exchange-rate/usd/" + unit;
                    try
                    {
                        ResponseEntity<MessageResult> pricResult = restTemplate.getForEntity(url, MessageResult.class);
                        MessageResult mr = pricResult.getBody();
                        log.info("=========查询" + unit + "价格后返回的结果{}=========", mr.getCode()+ "===" + mr.getMessage());
                        if (mr.getCode() == 0)
                        {
                            lockCoinDetail.setLockPrice(BigDecimal.valueOf(Double.parseDouble(mr.getData().toString())));
                        }

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    lockCoinDetail.setMemberId(member.getId());
                    lockCoinDetail.setType(LockType.DEPOSIT);
                    lockCoinDetail.setCoinUnit(unit);
                    lockCoinDetail.setTotalAmount(businessAuthApply.getAmount());
                    lockCoinDetail.setRemainAmount(businessAuthApply.getAmount());
                    lockCoinDetail.setStatus(LockStatus.LOCKED);
                    lockCoinDetail = lockCoinDetailService.save(lockCoinDetail);
                    businessAuthApply.setLockCoinDetailId(lockCoinDetail.getId());
                    businessAuthApplyService.save(businessAuthApply);


                    MemberTransaction memberTransaction = new MemberTransaction();
                    memberTransaction.setAmount(BigDecimal.ZERO.subtract(businessAuthApply.getAmount()));
                    memberTransaction.setMemberId(member.getId());
                    memberTransaction.setCreateTime(new Date());
                    memberTransaction.setType(TransactionType.BUSINESS_DEPOSIT);
                    memberTransaction.setSymbol(businessAuthApply.getBusinessAuthDeposit().getCoin().getUnit());
                    memberTransactionService.save(memberTransaction);
                }
            }
        } else {
            //不通过
            member.setCertifiedBusinessStatus(FAILED);//认证失败
            if (businessAuthApplyList != null && businessAuthApplyList.size() > 0) {
                BusinessAuthApply businessAuthApply = businessAuthApplyList.get(0);
                businessAuthApply.setCertifiedBusinessStatus(FAILED);
                //>>> add by zyj 2018.12.24 : 添加审核人员信息 start
                businessAuthApply.setAdminRealName(admin.getRealName());
                businessAuthApply.setAdminMobilePhone(admin.getMobilePhone());
                businessAuthApply.setAuditingTime(new Date());
                //<<< add by zyj 2018.12.24 : 添加审核人员信息 end
                businessAuthApply.setDetail(detail);
                //申请商家认证时冻结的金额退回
                if (businessAuthApply.getBusinessAuthDeposit() != null) {
                    MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(businessAuthApply.getBusinessAuthDeposit().getCoin().getUnit(), member.getId());
                    MessageResult result = memberWalletService.thawBalanceFromLockBlance(memberWallet, businessAuthApply.getAmount());
                    if (result.getCode() != 0)
                    {
                        throw new IllegalArgumentException("冻结余额不足");
                    }
                }
                businessAuthApplyService.save(businessAuthApply);
            }
        }
        member.setCertifiedBusinessCheckTime(new Date());
        memberService.save(member);
        return success();
    }

//    @RequiresPermissions("business-auth:apply:detail")
    @PostMapping("business-auth-detail")
//    @AccessLog(module = AdminModule.MEMBER, operation = "查询会员Member申请资料")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult getBusinessAuthApply(@RequestParam("id") Long id, BooleanEnum isapply) {
//        Member member = memberService.findOne(id);
//        notNull(member, "会员不存在!");
        //查询申请记录
//        List<BusinessAuthApply> businessAuthApplyList = businessAuthApplyService.findByMemberAndCertifiedBusinessStatus(member, status);
//        MessageResult result = MessageResult.success();
//        if (businessAuthApplyList != null && businessAuthApplyList.size() > 0) {
//            result.setData(businessAuthApplyList.get(0));
//        }
        //edit by zyj 2018-12-21 : 商家管理改版
        notNull(id,"id不合法");
        BusinessAuthApply businessAuthApply;
        //是否为一键成为商家
        BooleanEnum isBusi = BooleanEnum.IS_FALSE;
        //是否为商家审核页面
        if (isapply.equals(BooleanEnum.IS_TRUE)){
            //是，商家审核页面（认证商家申请id）
            businessAuthApply = businessAuthApplyService.findOne(id);
        }else {
            //否，认证商家页面（会员id）
            businessAuthApply = businessAuthApplyService.findOneByMemberIdDesc(id);
            List<LockCoinDetail> list = lockCoinDetailService.findByMemberIdAndType(id, LockType.DEPOSIT);
            if (list.size() == 0){// 一键成为商家
                isBusi = BooleanEnum.IS_TRUE;
                businessAuthApply = new BusinessAuthApply();
                businessAuthApply.setMember(memberService.findOne(id));
            }
        }
        Map<String, Object> map1 = unlockCoinApplyService.getBusinessOrderStatistics(id);
//        logger.info("会员订单信息:{}", map1);
        Map<String, Object> map2 = unlockCoinApplyService.getBusinessAppealStatistics(id);
//        logger.info("会员申诉信息:{}", map2);
        Long advertiseNum = unlockCoinApplyService.getAdvertiserNum(id);
//        logger.info("会员广告信息:{}", advertiseNum);
        Map<String,Object> map = new HashMap<>();
        map.putAll(map1);
        map.putAll(map2);
        map.put("advertiseNum", advertiseNum);
        map.put("businessAuthApply", businessAuthApply);
        map.put("isBusi", isBusi);
        MessageResult result = MessageResult.success();
        result.setData(map);
        return result;
    }

//    @RequiresPermissions("business-auth:apply:detail")
    @PostMapping("apply/detail")
    public MessageResult detail(@RequestParam("id") Long id) {
        MessageResult result = businessAuthApplyService.detail(id);
        return result;
    }

    @RequiresPermissions("business:auth-deposit-update")
    @PatchMapping("update")
    public MessageResult update(
            @RequestParam("id") Long id,
            @RequestParam("amount") Double amount,
            @RequestParam("status") CommonStatus status) {
        BusinessAuthDeposit oldData = businessAuthDepositService.findById(id);
        if (amount != null) {
            /*if(businessAuthDeposit.getAmount().compareTo(oldData.getAmount())>0){
                //如果上调了保证金，所有使用当前类型保证金的已认证商家的认证状态都改为保证金不足
                ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
                booleanExpressions.add(QDepositRecord.depositRecord.coin.eq(oldData.getCoin()));
                booleanExpressions.add(QDepositRecord.depositRecord.status.eq(DepositStatusEnum.PAY));
                Predicate predicate=PredicateUtils.getPredicate(booleanExpressions);
                List<DepositRecord> depositRecordList=depositRecordService.findAll(predicate);
                if(depositRecordList!=null){
                    List<Long> idList=new ArrayList<>();
                    for(DepositRecord depositRecord:depositRecordList){
                        idList.add(depositRecord.getMember().getId());
                    }
                    memberService.updateCertifiedBusinessStatusByIdList(idList);
                }
            }*/
            oldData.setAmount(new BigDecimal(amount));
        }
        if (status != null) {
            oldData.setStatus(status);
        }
        businessAuthDepositService.save(oldData);
        return success();
    }

    @PostMapping("apply/page-query")
    @RequiresPermissions("business:apply-page-query")
    public MessageResult page(
            PageModel pageModel,
            @RequestParam(value = "status", required = false) CertifiedBusinessStatus status,
            @RequestParam(value = "account", defaultValue = "") String account) {
        List<BooleanExpression> lists = new ArrayList<>();
        lists.add(QBusinessAuthApply.businessAuthApply.member.certifiedBusinessStatus.ne(CertifiedBusinessStatus.NOT_CERTIFIED)
                .and(QBusinessAuthApply.businessAuthApply.member.certifiedBusinessStatus.ne(CertifiedBusinessStatus.RETURN_SUCCESS)));
        if (!"".equals(account)) {
            lists.add(QBusinessAuthApply.businessAuthApply.member.username.like("%" + account + "%")
                    .or(QBusinessAuthApply.businessAuthApply.member.mobilePhone.like(account + "%"))
                    .or(QBusinessAuthApply.businessAuthApply.member.email.like(account + "%"))
                    .or(QBusinessAuthApply.businessAuthApply.member.realName.like("%" + account + "%")));
        }
        if (status != null) {
            lists.add(QBusinessAuthApply.businessAuthApply.certifiedBusinessStatus.eq(status));
        }
        Page<BusinessAuthApply> page = businessAuthApplyService.page(PredicateUtils.getPredicate(lists), pageModel.getPageable());
        return success(page);
    }

    /**
     * 认证成功的商家
     */
    //add by tansitao 时间： 2018/6/28 原因：商家管理
    @PostMapping("allBusiness/page-query")
    @RequiresPermissions("business:auth-page-query")
    public MessageResult allBusinessPage(
            PageModel pageModel,
            @RequestParam(value = "account", defaultValue = "") String account) {
//        //edit by zyj 2018-12-21 : 商家管理改版
//        List<BooleanExpression> lists = new ArrayList<>();
//        lists.add(QBusinessAuthApply.businessAuthApply.member.certifiedBusinessStatus.eq(CertifiedBusinessStatus.VERIFIED));
////                .and(QBusinessAuthApply.businessAuthApply.member.memberLevel.eq(MemberLevelEnum.IDENTIFICATION)));
//        if (!"".equals(account)) {
//            lists.add(QBusinessAuthApply.businessAuthApply.member.username.like("%" + account + "%")
//                    .or(QBusinessAuthApply.businessAuthApply.member.mobilePhone.like(account + "%"))
//                    .or(QBusinessAuthApply.businessAuthApply.member.email.like(account + "%"))
//                    .or(QBusinessAuthApply.businessAuthApply.member.realName.like("%" + account + "%")));
//        }
//        Page<BusinessAuthApply> page = businessAuthApplyService.page(PredicateUtils.getPredicate(lists), pageModel.getPageable());
        List<BooleanExpression> lists = new ArrayList<>();
        lists.add(QMember.member.certifiedBusinessStatus.eq(CertifiedBusinessStatus.VERIFIED)
                .and(QMember.member.memberLevel.eq(MemberLevelEnum.IDENTIFICATION)));
        if (!"".equals(account)) {
            lists.add(QMember.member.username.like("%" + account + "%")
                    .or(QMember.member.mobilePhone.like(account + "%"))
                    .or(QMember.member.email.like(account + "%"))
                    .or(QMember.member.realName.like("%" + account + "%")));
        }

        Page<Member> page = memberService.findAll(PredicateUtils.getPredicate(lists), pageModel.getPageable());
        return success(page);
    }

    @PostMapping("get-search-status")
    public MessageResult getSearchStatus() {
        CertifiedBusinessStatus[] statuses = CertifiedBusinessStatus.values();
        List<Map> list = new ArrayList<>();
        for (CertifiedBusinessStatus status : statuses) {
            if (status == CertifiedBusinessStatus.NOT_CERTIFIED
                    || status.getOrdinal() >= CertifiedBusinessStatus.DEPOSIT_LESS.getOrdinal()) {
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
