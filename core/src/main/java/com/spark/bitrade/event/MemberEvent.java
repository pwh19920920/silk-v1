package com.spark.bitrade.event;

import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.dao.MemberDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.*;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年01月09日
 */
@Service
public class MemberEvent {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private MemberDao memberDao;

    @Autowired
    private MemberPromotionService memberPromotionService;
    @Autowired
    private RewardActivitySettingService rewardActivitySettingService;
    @Autowired
    private RewardPromotionSettingService rewardPromotionSettingService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private RewardRecordService rewardRecordService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private GyDmcodeService gyDmcodeService;
    @Autowired
    private  MemberService memberService;

    @Autowired
    private MemberLoginHistoryService memberLoginHistoryService;

    private String userNameFormat = "U%06d";

    /**
     * 注册成功事件
     *
     * @param member 持久化对象
     */
    @Async
    public void onRegisterSuccess(Member member, String promotionCode, LoginType type, HttpServletRequest request, String thirdMarkOrdinal) throws InterruptedException {
        //记录注册设备信息
        //add by tansitao 时间： 2018/12/27 原因：注册成功保存注册设备信息
        memberLoginHistoryService.save(member, request, type, thirdMarkOrdinal, BooleanEnum.IS_TRUE);

        //设置推荐码
        member.setPromotionCode(String.format(userNameFormat, member.getId()) + GeneratorUtil.getNonceString(2));

        //add by  shenzucai 时间： 2018.05.29  原因：增加用户注册时的IP进行归属地域的设置 start
        //edit by tansitao 时间： 2018/6/28 原因：修改定位规则，用户推广人有区域id时，先用用户推广人的区域ID，推广人没有区域Id才进行定位
        //edit by lingxing 时间： 2018/7/18 原因： 改用mybatis查询
        Member memberPromotion = null;
        if(StringUtils.hasText(promotionCode)){
            memberPromotion = memberService.findByPromotionCode(promotionCode);
        }
        if(memberPromotion != null) {
            member.setInviterId(memberPromotion.getId());//add by tansitao 时间： 2018/8/16 原因：设置注册用户的推荐人
            this.getService().PromotionCommission(member, memberPromotion);//add|edit|del by tansitao 时间： 2018/8/16 原因：注册是给推广人推广返佣奖励
            if(StringUtils.isEmpty(memberPromotion.getAreaId())) {
                DimArea dimArea = gyDmcodeService.getPostionInfo(member.getIdNumber(), member.getMobilePhone(), member.getIp());
                if(dimArea != null) {
                    member.setAreaId(dimArea.getAreaId());
                }
            } else {
                member.setAreaId(memberPromotion.getAreaId());
            }
        } else {
            DimArea dimArea = gyDmcodeService.getPostionInfo(member.getIdNumber(), member.getMobilePhone(), member.getIp());
            if(dimArea != null) {
                member.setAreaId(dimArea.getAreaId());
            }
        }
        memberService.save(member);
        //add by  shenzucai 时间： 2018.05.29  原因：增加用户注册时的IP进行归属地域的设置 end

        JSONObject json = new JSONObject();
        json.put("uid", member.getId());
        kafkaTemplate.send("member-register", json.toJSONString());
    }

    /**
     * 登录成功事件
     *
     * @param member 持久化对象
     */
    //edit by tansitao 时间： 2018/10/15 原因：修改为同步
    public void onLoginSuccess(Member member, HttpServletRequest request, LoginType type, String token, String thirdMark) {
        //异步记录登录信息
        memberLoginHistoryService.save(member, request, type, thirdMark, BooleanEnum.IS_FALSE);

        //更新token
        if(type != null && type != LoginType.WEB ) {
            //member 类被jpa 的session管理了，直接setter操作会同步修改数据库
            Member member1 = new Member();
            member1.setId(member.getId());
            member1.setToken(token);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, 24 * 7);
            member1.setTokenExpireTime(calendar.getTime());
            memberService.updateToken(member1);
        }
    }

    /**
     * 登出成功事件
     *
     * @param request
     * @param user
     */
    @Async
    public void onLoginOutSuccess(HttpServletRequest request, AuthMember user){
        if(user.getLoginType()!=null && user.getLoginType()!=LoginType.WEB) {
            Member member = memberService.findOne(user.getId());
            member.setToken(null);
            member.setTokenExpireTime(null);
            memberService.updateToken(member);
        }
    }

    /**
     * 给推广用户返佣，目前只给一级推广人返佣金
     * @author tansitao
     * @time 2018/8/16 14:12 
     */
    @Transactional
    public void PromotionCommission(Member member, Member memberPromotion){
        //推广活动
        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService.findByType(PromotionRewardType.REGISTER);
        //判断是否存在活动，如果存在，并且在活动日期内则返佣
        if (rewardPromotionSetting != null && !(DateUtil.diffDays(rewardPromotionSetting.getUpdateTime(), new Date()) > rewardPromotionSetting.getEffectiveTime())) {
            MemberWallet memberWallet1 = memberWalletService.findByCoinAndMember(rewardPromotionSetting.getCoin(), memberPromotion);
            //add by tansitao 时间： 2018/8/16 原因：如果推荐人钱包不存在，则创建钱包
            if(memberWallet1 == null){
                memberWallet1 = memberWalletService.createMemberWallet(memberPromotion.getId(), rewardPromotionSetting.getCoin());
            }
            BigDecimal amount1 = JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("one");
            //edit by tansitao 时间： 2018/8/15 原因：当配置的返佣数量为不大于0 则不进行返佣
            if(amount1.compareTo(BigDecimal.ZERO) > 0){
//                        memberWallet1.setBalance(BigDecimalUtils.add(memberWallet1.getBalance(), amount1));
                MessageResult messageResult = memberWalletService.increaseBalance(memberWallet1.getId(), amount1);
                if(messageResult.getCode() != 0 ){
                    throw new IllegalArgumentException("INSUFFICIENT_BALANCE");
                }
                //del by tansitao 时间： 2018/11/21 原因：注释掉改行代码，无需保存
//                memberWalletService.save(memberWallet1);
                RewardRecord rewardRecord1 = new RewardRecord();
                rewardRecord1.setAmount(amount1);
                rewardRecord1.setCoin(rewardPromotionSetting.getCoin());
                rewardRecord1.setMember(memberPromotion);
                rewardRecord1.setRemark(rewardPromotionSetting.getType().getCnName());
                rewardRecord1.setType(RewardRecordType.PROMOTION);
                rewardRecordService.save(rewardRecord1);

                MemberTransaction memberTransaction = new MemberTransaction();
                memberTransaction.setFee(BigDecimal.ZERO);
                memberTransaction.setAmount(amount1);
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

            //add|edit|del by tansitao 时间： 2018/8/16 原因：取消对二级推广人的返佣
//                if (memberPromotion.getInviterId() != null) {
//                    Member member2 = memberDao.findOne(memberPromotion.getInviterId());
//                    if (rewardPromotionSetting != null) {
//                        MemberWallet memberWallet2 = memberWalletService.findByCoinAndMember(rewardPromotionSetting.getCoin(), member2);
//                        BigDecimal amount2 = JSONObject.parseObject(rewardPromotionSetting.getInfo()).getBigDecimal("two");
//                        //edit by tansitao 时间： 2018/8/15 原因：当配置的返佣数量为不大于0 则不进行返佣
//                        if(amount2.compareTo(BigDecimal.ZERO) > 0){
//                            memberWallet2.setBalance(BigDecimalUtils.add(memberWallet2.getBalance(), amount2));
//                            memberWalletService.save(memberWallet2);
//
//                            RewardRecord rewardRecord2 = new RewardRecord();
//                            rewardRecord2.setAmount(amount2);
//                            rewardRecord2.setCoin(rewardPromotionSetting.getCoin());
//                            rewardRecord2.setMember(member2);
//                            rewardRecord2.setRemark(rewardPromotionSetting.getType().getCnName());
//                            rewardRecord2.setType(RewardRecordType.PROMOTION);
//                            rewardRecordService.save(rewardRecord2);
//
//                            MemberTransaction memberTransaction = new MemberTransaction();
//                            memberTransaction.setFee(BigDecimal.ZERO);
//                            memberTransaction.setAmount(amount2);
//                            memberTransaction.setSymbol(rewardPromotionSetting.getCoin().getUnit());
//                            memberTransaction.setType(TransactionType.PROMOTION_AWARD);
//                            memberTransaction.setMemberId(member2.getId());
//                            memberTransactionService.save(memberTransaction);
//                        }
//                    }
//                    member2.setSecondLevel(member2.getSecondLevel() + 1);
//
//                    MemberPromotion two = new MemberPromotion();
//                    two.setInviterId(member2.getId());
//                    two.setInviteesId(member.getId());
//                    two.setLevel(PromotionLevel.TWO);
//                    memberPromotionService.save(two);
//
//                    if (member2.getInviterId() != null) {
//                        Member member3 = memberDao.findOne(member2.getInviterId());
//                        member3.setThirdLevel(member3.getThirdLevel() + 1);
//                    }
//                }
        //del by yangch 时间： 2018.04.26 原因：代码已删除
        /*//注册活动
        RewardActivitySetting rewardActivitySetting = rewardActivitySettingService.findByType(ActivityRewardType.REGISTER);
        if (rewardActivitySetting!=null){
            MemberWallet memberWallet=memberWalletService.findByCoinAndMember(rewardActivitySetting.getCoin(),member);
            while (memberWallet==null){
                Thread.sleep(1000);
                memberWallet=memberWalletService.findByCoinAndMember(rewardActivitySetting.getCoin(),member);
            }
            BigDecimal amount3=JSONObject.parseObject(rewardActivitySetting.getInfo()).getBigDecimal("amount");
            memberWallet.setBalance(BigDecimalUtils.add(memberWallet.getBalance(),amount3));
            memberWalletService.save(memberWallet);
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
        }*/

    }

    public  MemberEvent getService(){return SpringContextUtil.getBean(this.getClass());}
}
