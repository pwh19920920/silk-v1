package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.constant.UttMemberStatus;
import com.spark.bitrade.constant.UttReleaseStatus;
import com.spark.bitrade.dao.LockUttMemberDao;
import com.spark.bitrade.dao.LockUttReleasePlanDao;
import com.spark.bitrade.dto.LockUttDto;
import com.spark.bitrade.entity.LockUttMember;
import com.spark.bitrade.entity.LockUttReleasePlan;
import com.spark.bitrade.entity.SilkDataDist;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.PriceUtil;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.08.15 17:17  
 */
@Service
@Slf4j
public class LockUttBizServiceImpl implements ILockUttBizService {

    @Autowired
    private LockUttMemberService lockUttMemberService;
    @Autowired
    private LockUttMemberDao lockUttMemberDao;
    @Autowired
    private LockUttReleasePlanDao lockUttReleasePlanDao;
    @Autowired
    private IMemberService memberService;
    @Autowired
    private ISilkDataDistService silkDataDistService;
    @Autowired
    private LockUttReleasePlanService lockUttReleasePlanService;
    @Autowired
    private RestTemplate restTemplate;
    @Override
    public void lockUtt(String batchNum) {
        if (StringUtils.isEmpty(batchNum)) {
            XxlJobLogger.log("===================批次号为空直接返回=================");
            return;
        }
        XxlJobLogger.log("===================UTT开始锁仓=================");
        // 开始锁仓
        PriceUtil priceUtil = new PriceUtil();
        //获取锁仓币种人民币价格
        BigDecimal coinCnyPrice = priceUtil.getCoinCnyPrice(restTemplate, "UTT");
        //获取锁仓币种USDT价格
        BigDecimal coinUSDTPrice = priceUtil.getCoinCnyPrice(restTemplate, "UTT");
        //获取USDT的人民币价格
        BigDecimal usdtPrice = priceUtil.getUSDTPrice(restTemplate);
        //查询配置
        SilkDataDist first = silkDataDistService.findByIdAndKey("LOCK_UTT_ACTIVITY", "RATE_FIRST");
        SilkDataDist avg = silkDataDistService.findByIdAndKey("LOCK_UTT_ACTIVITY", "RATE_AVG");
        SilkDataDist totalMember = silkDataDistService.findByIdAndKey("LOCK_UTT_ACTIVITY", "TOTAL_ACCOUNT");
        if (first == null || avg == null || totalMember == null) {
            XxlJobLogger.log("===================UTT活动全局配置不存在=================");
            return;
        }
        BigDecimal firstRate = new BigDecimal(first.getDictVal());
        BigDecimal avgRate = new BigDecimal(avg.getDictVal());
        //总帐户id
        Long totalAccountId = Long.valueOf(totalMember.getDictVal());
        LockUttDto dto = LockUttDto.builder()
                .firstRate(firstRate)
                .avgRate(avgRate)
                .totalMemberId(totalAccountId)
                .coinCnyPrice(coinCnyPrice)
                .coinUSDTPrice(coinUSDTPrice)
                .usdtPrice(usdtPrice).build();

        //根据批次查询出待处理和处理失败的记录 进行锁仓
        EntityWrapper<LockUttMember> condition = new EntityWrapper<>();
        condition.eq(LockUttMember.STATUS, UttMemberStatus.PENDING.getOrdinal())
                .eq(LockUttMember.BATCH_NUM, batchNum);
        List<LockUttMember> members = lockUttMemberService.selectList(condition);
        if (!CollectionUtils.isEmpty(members)) {
            for (int i = 0; i < members.size(); i++) {
                LockUttMember lockUttMember = members.get(i);
                Long memberId = lockUttMember.getMemberId();
                if (memberService.isExist(memberId)) {
                    try {
                        lockUttMemberService.lockUtt(lockUttMember, dto);
                        continue;
                    } catch (Exception e) {
                        log.info("锁仓失败memberId:{}", memberId);
                        XxlJobLogger.log("===================用户ID" + memberId + "锁仓失败，处理失败=================");
                        lockUttMember.setRemark("处理失败,锁仓失败!");
                        log.info("异常信息:{}",ExceptionUtils.getFullStackTrace(e));
                    }
                } else {
                    lockUttMember.setRemark("处理失败,用户不存在!");
                    //mybatis 更新方法自带事务
                    XxlJobLogger.log("===================用户ID" + memberId + "系统中不存在，处理失败=================");
                }
                lockUttMember.setStatus(UttMemberStatus.PROCESSED_FAILED);
                lockUttMember.setUpdateTime(new Date());
                lockUttMemberDao.save(lockUttMember);
            }
        }
        XxlJobLogger.log("===================UTT锁仓成功=================");

    }

    @Override
    public void releaseUtt(TransactionType type) {
        log.info("==============================BTLF解锁开始====================================");
        //查询出需要释放的记录
        EntityWrapper<LockUttReleasePlan> e = new EntityWrapper<>();
        e.le(LockUttReleasePlan.PLAN_UNLOCK_TIME, new Date())
                .eq(LockUttReleasePlan.STATUS, UttReleaseStatus.BE_RELEASING.getOrdinal());
        List<LockUttReleasePlan> plans = lockUttReleasePlanService.selectList(e);
        //循环释放
        if (!CollectionUtils.isEmpty(plans)) {
            XxlJobLogger.log("======================开始解锁BTLF总条数:"+plans.size()+"=============================");
            for (LockUttReleasePlan plan : plans) {
                try {
                    lockUttReleasePlanService.doRelease(plan,type);
                } catch (Exception ex) {
                    XxlJobLogger.log("======================planId" + plan.getId() + "释放失败=============================");
                    plan.setStatus(UttReleaseStatus.RELEASE_FAILED);
                    plan.setRemark(plan.getRemark() + "释放失败..");
                    plan.setUpdateTime(new Date());
                    lockUttReleasePlanDao.save(plan);
                    log.info("异常信息:{}",ExceptionUtils.getFullStackTrace(ex));
                }
            }
        }
        XxlJobLogger.log("======================BTLF解锁结束=============================");
        log.info("==============================BTLF解锁结束====================================");
    }


}
