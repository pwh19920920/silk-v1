package com.spark.bitrade.service;

import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.dao.MemberApplicationDao;
import com.spark.bitrade.dao.MemberDao;
import com.spark.bitrade.dao.MemberWalletDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.pagination.PageResult;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.BigDecimalUtils;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.spark.bitrade.constant.AuditStatus.AUDIT_DEFEATED;
import static com.spark.bitrade.constant.AuditStatus.AUDIT_SUCCESS;
import static com.spark.bitrade.constant.RealNameStatus.NOT_CERTIFIED;
import static com.spark.bitrade.constant.RealNameStatus.VERIFIED;
import static com.spark.bitrade.entity.QMemberApplication.memberApplication;

/**
 * @author rongyu
 * @description 会员审核单Service
 * @date 2017/12/26 15:10
 */
@Service
public class MemberApplicationService extends BaseService {
    private Logger logger = LoggerFactory.getLogger(MemberApplicationService.class);

    @Autowired
    private MemberApplicationDao memberApplicationDao;

    @Value("${commission.need.real-name:0}")
    private int needRealName;

    @Autowired
    private MemberDao memberDao;

    @Autowired
    private MemberService memberService;

    @Autowired
    private RewardActivitySettingService rewardActivitySettingService;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private MemberWalletDao memberWalletDao;

    @Autowired
    private RewardRecordService rewardRecordService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private MemberPromotionService memberPromotionService;
    @Autowired
    private RewardPromotionSettingService rewardPromotionSettingService;

    @Override
    public List<MemberApplication> findAll() {
        return memberApplicationDao.findAll();
    }

    public Page<MemberApplication> findAll(Predicate predicate, Pageable pageable) {
        return memberApplicationDao.findAll(predicate, pageable);
    }

    public MemberApplication findOne(Long id) {
        return memberApplicationDao.findOne(id);
    }

    public MemberApplication save(MemberApplication memberApplication) {
        return memberApplicationDao.save(memberApplication);
    }

    public MemberApplication findLatelyReject(Member member) {
        return memberApplicationDao.findFirstByMemberAndAuditStatusOrderByIdDesc(member, AuditStatus.AUDIT_DEFEATED);
    }

    //add by yangch 时间： 2018.05.11 原因：代码合并
    /*public List<MemberApplication> findLatelyReject(Member member) {
        return memberApplicationDao.findMemberApplicationByMemberAndAuditStatusOrderByIdDesc(member, AuditStatus.AUDIT_DEFEATED);
    }*/

    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param predicateList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<MemberApplication> query(List<Predicate> predicateList, Integer pageNo, Integer pageSize) {
        List<MemberApplication> list;
        JPAQuery<MemberApplication> jpaQuery = queryFactory.selectFrom(memberApplication);
        if (predicateList != null) {
            jpaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
        }
        jpaQuery.orderBy(memberApplication.createTime.desc());
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());
    }

    /**
     * 审核通过（人工）
     *
     * @param application
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditPass(MemberApplication application, int opType) {
        //edit by fumy  date: 2018.09.06  reason: 加入审核类型 1为系统审核 2为人工审核
        application.setOpType(opType);
        Member member = application.getMember();
        //实名会员
        member.setMemberLevel(MemberLevelEnum.REALNAME);
        //添加会员真实姓名
        member.setRealName(application.getRealName());
        //会员身份证号码
        member.setIdNumber(application.getIdCard());
        //会员状态修改已认证
        member.setRealNameStatus(VERIFIED);
        member.setApplicationTime(new Date());
        memberService.save(member);
        //审核成功
        application.setAuditStatus(AUDIT_SUCCESS);
        //edit by yangch 时间： 2018.05.11 原因：代码合并
        //edit by yangch 时间： 2018.05.17 原因：实名认证不返佣
        /*if(needRealName==1){
            if(member.getInviterId()!=null) {
                Member member1 = memberDao.findOne(member.getInviterId());
                promotion(member1, member);
            }
        }*/

        memberApplicationDao.save(application);
    }

    //add by yangch 时间： 2018.05.11 原因：代码合并
    private void promotion(Member member1, Member member) {
        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService.findByType(PromotionRewardType.REGISTER);
        if (rewardPromotionSetting != null) {
            MemberWallet memberWallet1 = memberWalletService.findByCoinAndMember(rewardPromotionSetting.getCoin(), member1);
            BigDecimal amount1 = JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("one");
            memberWallet1.setBalance(BigDecimalUtils.add(memberWallet1.getBalance(), amount1));
            memberWalletService.save(memberWallet1);
            RewardRecord rewardRecord1 = new RewardRecord();
            rewardRecord1.setAmount(amount1);
            rewardRecord1.setCoin(rewardPromotionSetting.getCoin());
            rewardRecord1.setMember(member1);
            rewardRecord1.setRemark(rewardPromotionSetting.getType().getCnName());
            rewardRecord1.setType(RewardRecordType.PROMOTION);
            rewardRecordService.save(rewardRecord1);
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setFee(BigDecimal.ZERO);
            memberTransaction.setAmount(amount1);
            memberTransaction.setSymbol(rewardPromotionSetting.getCoin().getUnit());
            memberTransaction.setType(TransactionType.PROMOTION_AWARD);
            memberTransaction.setMemberId(member1.getId());
            memberTransactionService.save(memberTransaction);
        }
        member1.setFirstLevel(member1.getFirstLevel() + 1);
        //member.setInviterId(member1.getId());
        MemberPromotion one = new MemberPromotion();
        one.setInviterId(member1.getId());
        one.setInviteesId(member.getId());
        one.setLevel(PromotionLevel.ONE);
        memberPromotionService.save(one);
        if (member1.getInviterId() != null) {
            Member member2 = memberDao.findOne(member1.getInviterId());
            if (needRealName == 1) {
                promotionLevelTwo(rewardPromotionSetting, member2, member);
            }
        }
    }

    //add by yangch 时间： 2018.05.11 原因：代码合并
    private void promotionLevelTwo(RewardPromotionSetting rewardPromotionSetting, Member member2, Member member) {
        if (rewardPromotionSetting != null) {
            MemberWallet memberWallet2 = memberWalletService.findByCoinAndMember(rewardPromotionSetting.getCoin(), member2);
            BigDecimal amount2 = JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("two");
            memberWallet2.setBalance(BigDecimalUtils.add(memberWallet2.getBalance(), amount2));
            memberWalletService.save(memberWallet2);
            RewardRecord rewardRecord2 = new RewardRecord();
            rewardRecord2.setAmount(amount2);
            rewardRecord2.setCoin(rewardPromotionSetting.getCoin());
            rewardRecord2.setMember(member2);
            rewardRecord2.setRemark(rewardPromotionSetting.getType().getCnName());
            rewardRecord2.setType(RewardRecordType.PROMOTION);
            rewardRecordService.save(rewardRecord2);
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setFee(BigDecimal.ZERO);
            memberTransaction.setAmount(amount2);
            memberTransaction.setSymbol(rewardPromotionSetting.getCoin().getUnit());
            memberTransaction.setType(TransactionType.PROMOTION_AWARD);
            memberTransaction.setMemberId(member2.getId());
            memberTransactionService.save(memberTransaction);
        }
        member2.setSecondLevel(member2.getSecondLevel() + 1);
        MemberPromotion two = new MemberPromotion();
        two.setInviterId(member2.getId());
        two.setInviteesId(member.getId());
        two.setLevel(PromotionLevel.TWO);
        memberPromotionService.save(two);
        if (member2.getInviterId() != null) {
            Member member3 = memberDao.findOne(member2.getInviterId());
            member3.setThirdLevel(member3.getThirdLevel() + 1);
        }
    }


    /**
     * 审核不通过
     *
     * @param application
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditNotPass(MemberApplication application) {
        //edit by fumy  date: 2018.09.06  reason: 设置为人工审核
        application.setOpType(2);
        Member member = application.getMember();
        //会员实名状态未认证
        member.setRealNameStatus(NOT_CERTIFIED);
        memberService.save(member);
        //审核失败
        application.setAuditStatus(AUDIT_DEFEATED);
        memberApplicationDao.save(application);
    }


    /**
     * 人工确认系统审核
     *
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean personConfirm(Long id) {
        int row = memberApplicationDao.updateOpType(id);
        return row > 0 ? true : false;
    }


    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleActivityForRealName(Member member) {

        logger.info("处理实名奖励活动---------->>");

        List<RewardActivitySetting> rewardActivitySettingList = rewardActivitySettingService.findListByType(ActivityRewardType.AUTHENTICATION);
        if (rewardActivitySettingList != null) {
            for (RewardActivitySetting rewardActivitySetting : rewardActivitySettingList) {
                if (rewardActivitySetting != null) {
                    MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(rewardActivitySetting.getCoin(), member.getId());
                    if (memberWallet == null) {
                        memberWallet = memberWalletService.createMemberWallet(member.getId(), rewardActivitySetting.getCoin());
                    }
                    BigDecimal amount3 = JSONObject.parseObject(rewardActivitySetting.getInfo()).getBigDecimal("amount");
                    memberWalletDao.increaseBalance(memberWallet.getId(), amount3);
                    RewardRecord rewardRecord3 = new RewardRecord();
                    rewardRecord3.setAmount(amount3);
                    rewardRecord3.setCoin(rewardActivitySetting.getCoin());
                    rewardRecord3.setMember(member);
                    rewardRecord3.setRemark(rewardActivitySetting.getType().getCnName());
                    rewardRecord3.setType(RewardRecordType.ACTIVITY);
                    rewardRecordService.save(rewardRecord3);
                    MemberTransaction memberTransaction = new MemberTransaction();
                    memberTransaction.setFee(BigDecimal.ZERO);
                    memberTransaction.setAmount(amount3);
                    memberTransaction.setSymbol(rewardActivitySetting.getCoin().getUnit());
                    memberTransaction.setType(TransactionType.ACTIVITY_AWARD);
                    memberTransaction.setMemberId(member.getId());
                    memberTransactionService.save(memberTransaction);
                }
            }
        }
    }

    @Async
    @Transactional(rollbackFor = Exception.class)
    public void PromotionForRealName(Member member, Member memberPromotion) {
        logger.info("处理协助实名认证返佣--------------->>");
        //推广活动
        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService.findByType(PromotionRewardType.AUTHENTICATION);
        //判断是否存在活动，如果存在，并且在活动日期内则返佣

        if (rewardPromotionSetting != null && (DateUtil.diffDays(rewardPromotionSetting.getUpdateTime(), new Date()) < rewardPromotionSetting.getEffectiveTime())) {
            MemberWallet rewardMemberWallet = memberWalletService.findByCoinAndMember(rewardPromotionSetting.getCoin(), memberPromotion);
            //add by tansitao 时间： 2018/8/16 原因：如果推荐人钱包不存在，则创建钱包
            if (rewardMemberWallet == null) {
                rewardMemberWallet = memberWalletService.createMemberWallet(memberPromotion.getId(), rewardPromotionSetting.getCoin());
            }
            BigDecimal rewardAmount = JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("one");
            //edit by tansitao 时间： 2018/8/15 原因：当配置的返佣数量为不大于0 则不进行返佣
            if (rewardAmount.compareTo(BigDecimal.ZERO) > 0) {
                MessageResult messageResult = memberWalletService.increaseBalance(rewardMemberWallet.getId(), rewardAmount);
                if (messageResult.getCode() != 0) {
                    throw new IllegalArgumentException("INSUFFICIENT_BALANCE");
                }
                logger.info("----->保存协助实名认证返佣记录");
                RewardRecord rewardRecord1 = new RewardRecord();
                rewardRecord1.setAmount(rewardAmount);
                rewardRecord1.setCoin(rewardPromotionSetting.getCoin());
                rewardRecord1.setMember(memberPromotion);
                rewardRecord1.setRemark(rewardPromotionSetting.getType().getCnName());
                rewardRecord1.setType(RewardRecordType.PROMOTION);
                rewardRecordService.save(rewardRecord1);

                logger.info("----->保存协助实名认证返佣产生的交易记录");
                MemberTransaction memberTransaction = new MemberTransaction();
                memberTransaction.setFee(BigDecimal.ZERO);
                memberTransaction.setAmount(rewardAmount);
                memberTransaction.setSymbol(rewardPromotionSetting.getCoin().getUnit());
                memberTransaction.setType(TransactionType.PROMOTION_AWARD);
                memberTransaction.setMemberId(memberPromotion.getId());
                memberTransactionService.save(memberTransaction);
            }
        }

        memberPromotion.setFirstLevel(memberPromotion.getFirstLevel() + 1);
        MemberPromotion one = new MemberPromotion();
        one.setInviterId(memberPromotion.getId());
        one.setInviteesId(member.getId());
        one.setLevel(PromotionLevel.ONE);
        memberPromotionService.save(one);
    }

    /**
     * 获取人工审核表状态
     * update by shushiping 2019-12-20 需要获取驳回理由
     * @param memberId 会员ID
     * @return
     */
//    public Integer getMemberApplicationByAuditStatus(Long memberId) {
//        Integer initStatus = 0;
//        Integer auditStatus = memberApplicationDao.countMemberApplicationByAuditStatus(memberId);
//        if (auditStatus > 0) {
//            auditStatus = memberApplicationDao.getMemberApplicationByAuditStatus(memberId);
//            if (auditStatus == 1) {
//                initStatus = 3;
//            }
//        }
//        return initStatus;
//    }
    public Map<String,Object> getMemberApplicationByAuditStatus(Long memberId) {

//        Integer initStatus = 0;
        Map<String,Object> map = new HashMap<>();
        Integer auditStatus = memberApplicationDao.countMemberApplicationByAuditStatus(memberId);
        map.put("auditStatus",0);
        MemberApplication memberApplication = memberApplicationDao.getMemberApplicationByAuditStatus(memberId);
        if (auditStatus > 0) {
            map.put("auditStatus",memberApplication.getAuditStatus());
            if(AuditStatus.AUDIT_DEFEATED.equals(memberApplication.getAuditStatus())){
                map.put("auditStatus","3");
                map.put("rejectReason",memberApplication.getRejectReason());
            }
//            auditStatus = memberApplicationDao.getMemberApplicationByAuditStatus(memberId);
//            if (auditStatus == 1) {
//                initStatus = 3;
//            }
        }else{
            map.put("auditStatus",0);
        }
        return map;
    }
}
