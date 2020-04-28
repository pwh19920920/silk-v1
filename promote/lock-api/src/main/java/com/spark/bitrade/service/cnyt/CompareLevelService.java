package com.spark.bitrade.service.cnyt;

import com.spark.bitrade.constant.LockConstant;
import com.spark.bitrade.entity.LockMarketLevel;
import com.spark.bitrade.entity.LockMarketPerformanceTotal;
import com.spark.bitrade.entity.LockRewardLevelConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/***
  * 等级比较服务
  * @author yangch
  * @time 2018.12.04 9:43
  */
@Slf4j
@Service
public class CompareLevelService {
    /**
     * 判断被邀请者是否超越邀请者
     *
     * @param invitee 被邀请者
     * @param inviter 邀请者
     * @return -1=未超越/0=平级/1=超越
     */
    public int compare(LockMarketLevel invitee, LockMarketLevel inviter) {
        return invitee.getRewardRate().compareTo(inviter.getRewardRate());
    }

    /**
     * 是否超越邀请者
     * @param invitee 被邀请者
     * @param inviter 邀请者
     * @return true = 平级或超越 /false=未超过
     */
    public boolean surpassInviter(LockMarketLevel invitee, LockMarketLevel inviter) {
        if (compare(invitee, inviter) >= 0
                //邀请人不能是虚拟的等级
                && inviter.getMemberLevelId().intValue() != LockConstant.VIRTUAL_MARKET_LEVEL) {
            return true;
        }

        return false;
        //return compare(invitee, inviter) >=0 ? true : false;
    }


    /**
     * 匹配等级
     *
     * @param lstLevel       必填，级差配置列表
     * @param currLevel      选填，当前等级
     * @param lstPerformance 选填，子部门业绩
     * @return
     */
    public LockRewardLevelConfig matchLevel(List<LockRewardLevelConfig> lstLevel,
                                            LockRewardLevelConfig currLevel, List<LockMarketPerformanceTotal> lstPerformance) {
        if (StringUtils.isEmpty(lstLevel)) {
            return null;
        }
        if (StringUtils.isEmpty(lstPerformance)) {
            //从可用的等级中选择最小的等级配置
            Optional<LockRewardLevelConfig> opLevelConfig = lstLevel.stream()
                    .min(Comparator.comparing(LockRewardLevelConfig::getLevelId));
            if (opLevelConfig.isPresent()) {
                return opLevelConfig.get();
            }
            return null;
        }

        //当前等级
        final int currLevelId;
        if (StringUtils.isEmpty(currLevel) == false) {
            currLevelId = currLevel.getLevelId();
        } else {
            currLevelId = 0;
        }

        //所有下属部门的业绩总和
        BigDecimal performanceTotal =
                lstPerformance.stream().map(m ->
                        m.getOwnLockAmountTotal().add(m.getSubDepartmentAmountTotal()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        //筛选出 满足考核条件的最高等级（考核条件 ：总业绩完成量、考核部门数+小部门业绩）
        Optional<LockRewardLevelConfig> opLevelConfig = lstLevel.stream()
                //匹配 不低于当前的等级 的等级选项
                .filter(c -> c.getLevelId() >= currLevelId)
                //考核总业绩完成量，匹配满足 不大于 总业绩完成量 的等级选项
                .filter(c -> c.getPerformanceTotal().compareTo(performanceTotal) <= 0)
                .filter(c -> {
                    //考核 小部门业绩（不包含小部门自己的锁仓业绩） 和 部门数量，满足小部门业绩 的等级选项 及 部门数量
                    long subdivisionCount = lstPerformance.stream().
                            filter(f -> f.getSubDepartmentAmountTotal().compareTo(c.getSubdivisionPerformance()) >= 0).count();
                    //匹配 满足 小部门业绩和部门数量 等级选项
                    return subdivisionCount >= c.getSubdivisionCount();
                })
                //从可用的等级中选择最大的等级选项
                .max(Comparator.comparing(LockRewardLevelConfig::getLevelId));

        if (opLevelConfig.isPresent()) {
            return opLevelConfig.get();
        }

        return null;
    }

    /**
     * 匹配等级
     *
     * @param lstLevel       必填，级差配置列表
     * @param lstPerformance 选填，子部门业绩
     * @return
     */
    public LockRewardLevelConfig matchLevel(List<LockRewardLevelConfig> lstLevel, List<LockMarketPerformanceTotal> lstPerformance) {
        return matchLevel(lstLevel, null, lstPerformance);
    }

    /**
     * 是否可以升级，当前等级与目标等级比较
     *
     * @param currLevel   当前等级
     * @param targetLevel 目标等级
     * @return true=可以升级等级/false=不能升级等级
     */
    public boolean canUpgradeLevel(LockRewardLevelConfig currLevel, LockRewardLevelConfig targetLevel) {
        if (StringUtils.isEmpty(currLevel) || StringUtils.isEmpty(targetLevel)) {
            return false;
        }
        if (targetLevel.getLevelId() > currLevel.getLevelId()) {
            return true;
        }

        return false;
    }


    public static void main(String[] args) {
        //等级
        List<LockRewardLevelConfig> lstLevel = new ArrayList<>();
        LockRewardLevelConfig level1 = new LockRewardLevelConfig();
        level1.setLevelId(1);   //1级别
        level1.setSubdivisionCount(0);
        level1.setSubdivisionPerformance(BigDecimal.valueOf(0));
        level1.setPerformanceTotal(BigDecimal.valueOf(0)); //0
        lstLevel.add(level1);

        LockRewardLevelConfig level2 = new LockRewardLevelConfig();
        level2.setLevelId(2);   //2级别
        level2.setSubdivisionCount(1);
        level2.setSubdivisionPerformance(BigDecimal.valueOf(1000));
        level2.setPerformanceTotal(BigDecimal.valueOf(2000)); //2000
        lstLevel.add(level2);

        LockRewardLevelConfig level3 = new LockRewardLevelConfig();
        level3.setLevelId(3);   //3级别
        level3.setSubdivisionCount(2);
        level3.setSubdivisionPerformance(BigDecimal.valueOf(2000));
        level3.setPerformanceTotal(BigDecimal.valueOf(4000)); //4000
        lstLevel.add(level3);

        LockRewardLevelConfig level4 = new LockRewardLevelConfig();
        level4.setLevelId(4);   //4级别
        level4.setSubdivisionCount(3);
        level4.setSubdivisionPerformance(BigDecimal.valueOf(2000));
        level4.setPerformanceTotal(BigDecimal.valueOf(6000)); //6000
        lstLevel.add(level4);


        //子部门业绩
        List<LockMarketPerformanceTotal> lstPerformance = new ArrayList<>();
        LockMarketPerformanceTotal total1 = new LockMarketPerformanceTotal();
        total1.setOwnLockAmountTotal(BigDecimal.valueOf(100));
        total1.setSubDepartmentAmountTotal(BigDecimal.valueOf(1000));
        lstPerformance.add(total1);

        LockMarketPerformanceTotal total2 = new LockMarketPerformanceTotal();
        total2.setOwnLockAmountTotal(BigDecimal.valueOf(200));
        total2.setSubDepartmentAmountTotal(BigDecimal.valueOf(3000));
        lstPerformance.add(total2);

        LockMarketPerformanceTotal total3 = new LockMarketPerformanceTotal();
        total3.setOwnLockAmountTotal(BigDecimal.valueOf(500));
        total3.setSubDepartmentAmountTotal(BigDecimal.valueOf(5000));
        lstPerformance.add(total3);

        LockMarketPerformanceTotal total4 = new LockMarketPerformanceTotal();
        total4.setOwnLockAmountTotal(BigDecimal.valueOf(600));
        total4.setSubDepartmentAmountTotal(BigDecimal.valueOf(2000));
        lstPerformance.add(total4);

        BigDecimal total = lstPerformance.stream().filter(f -> f.getOwnLockAmountTotal().compareTo(BigDecimal.valueOf(3000)) < 0)
                .map(m ->
                        m.getOwnLockAmountTotal().add(m.getSubDepartmentAmountTotal())
                ).reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println("totoal=" + total);


        //测试
        CompareLevelService service = new CompareLevelService();
        //LockRewardLevelConfig levelConfig = service.matchLevel(lstLevel, null , lstPerformance);      //不指定当前等级
        //LockRewardLevelConfig levelConfig = service.matchLevel(lstLevel, level4 , lstPerformance);    //有业绩
        LockRewardLevelConfig levelConfig = service.matchLevel(lstLevel, level4, null); //没有业绩的情况

        System.out.println("-------------------");
        System.out.println(levelConfig);

    }

}
