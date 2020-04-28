package com.spark.bitrade.service.cnyt;

import com.spark.bitrade.entity.LockMarketLevel;
import com.spark.bitrade.entity.LockMarketPerformanceTotal;
import com.spark.bitrade.entity.LockRewardLevelConfig;
import com.spark.bitrade.service.LockCoinDetailService;
import com.spark.bitrade.service.LockMarketLevelService;
import com.spark.bitrade.service.LockMarketPerformanceTotalService;
import com.spark.bitrade.service.LockRewardLevelConfigService;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 *  更新锁仓用户的等级
 *
 * @author yangch
 * @time 2019.03.17 11:59
 */

@Slf4j
@Service
public class UpdateLockUserLevelService {

    @Autowired
    private LockCoinDetailService lockCoinDetailService;


    @Autowired
    private LockMarketPerformanceTotalService lockMarketPerformanceTotalService;


    @Autowired
    private LockMarketLevelService lockMarketLevelService;

    @Autowired
    private LockRewardLevelConfigService lockRewardLevelConfigService;


    @Autowired
    private CompareLevelService compareLevelService;


    /**
     * 初始节点（使用的是lock_reward_level_config.id的值）
     * （最小的有效等级，用于判定达成奖励的最低等级条件：本人锁仓5万360天 或者业绩10万以上 ）
     */
    @Value("${lock.valid.min.levelId:1}")
    private Integer lockValidMinLevelId;


    /**
     *  更新锁仓用户的等级
     *
     * @author yangch
     * @time 2019.03.17 13:55  
     */
    @Async
    public void updateLockUserLevel(Long memberId, String symbol) {
        if(StringUtils.isEmpty(memberId)){
            log.warn("更新锁仓用户的等级，无效的memberId参数值");
            return;
        }

        log.info("锁仓用户ID={}，开始处理锁仓用户的等级----------------------", memberId);

        //查询会员市场等级
        LockMarketLevel lockMarketLevel = lockMarketLevelService.findByMemberId(memberId, symbol);
        log.info("锁仓用户ID={}，查询会员市场等级={}", memberId, lockMarketLevel);

        if (lockMarketLevel.getMemberLevelId().compareTo(lockValidMinLevelId.longValue()) >= 0) {
            log.info("锁仓用户ID={}，会员市场等级={} 比 初始节点高，不需要忽略“初始节点”的升级", memberId, lockMarketLevel);
        } else {
            //查询极差配置信息
            List<LockRewardLevelConfig> lstLevel = lockRewardLevelConfigService.getLevelConfigList(symbol);
            log.info("锁仓用户ID={}，查询极差配置信息={}", memberId, lstLevel);

            //查询当前等级配置信息
            LockRewardLevelConfig currLevel = lockRewardLevelConfigService.getLevelConfigById(lockMarketLevel.getMemberLevelId().intValue(), symbol);
            log.info("锁仓用户ID={}，查询当前等级配置信息={}", memberId, currLevel);

            //查询会员所有子部门数据
            List<LockMarketPerformanceTotal> lstPerformance = lockMarketPerformanceTotalService.findAllByInivite(memberId, symbol);
            log.info("锁仓用户ID={}，查询会员所有子部门数据={}", memberId, lstPerformance);

            //比较生成目标等级（根据业绩生成的目标等级）
            LockRewardLevelConfig targetLevel = compareLevelService.matchLevel(lstLevel, currLevel, lstPerformance);
            log.info("锁仓用户ID={}，比较生成目标等级={}", memberId, targetLevel);

            //判断是否需要更新当前等级到目标等级
            if (compareLevelService.canUpgradeLevel(currLevel, targetLevel)) {
                log.info("锁仓用户ID={}，业绩已满足升级条件，从“{}”升级到“{}” ", memberId, currLevel, targetLevel);
                getService().updateLockMarketLevel(lockMarketLevel, currLevel, targetLevel);
            } else {
                //获取是否满足“本人锁仓5万360天”的条件
                LockRewardLevelConfig validFirstLevelConfig = lockCoinDetailService.getValidFirstLevelConfig(symbol);
                if (lockCoinDetailService.matchMemberLockCondition(memberId, symbol)) {
                    log.info("锁仓用户ID={}，个人锁仓已满足升级条件，从“{}”升级到“{}” ", memberId, currLevel, validFirstLevelConfig);
                    getService().updateLockMarketLevel(lockMarketLevel, currLevel, validFirstLevelConfig);
                } else {
                    log.info("锁仓用户ID={}，没有达到升级条件", memberId);
                }
            }
        }
        log.info("锁仓用户ID={}，完成锁仓用户等级的处理----------------------", memberId);

    }

    /**
     * 更新等级
     *  
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateLockMarketLevel(LockMarketLevel lockMarketLevel,
                                      LockRewardLevelConfig currLevel, LockRewardLevelConfig targetLevel) {

        //判断是否需要更新当前等级到目标等级
        if (compareLevelService.canUpgradeLevel(currLevel, targetLevel)) {
            log.info("锁仓用户ID={}，更新会员等级,currLevel={},targetLevel={}",
                    lockMarketLevel.getMemberId(), currLevel, targetLevel);
            //更新会员等级
            lockMarketLevel.setLevel(targetLevel.getLevel());
            lockMarketLevel.setMemberLevelId(Long.valueOf(targetLevel.getLevelId()));
            lockMarketLevel.setRewardRate(targetLevel.getRewardRate());
            lockMarketLevel.setUpdateTime(new Date());
            lockMarketLevelService.save(lockMarketLevel);
        }
    }

    public UpdateLockUserLevelService getService() {
        return SpringContextUtil.getBean(UpdateLockUserLevelService.class);
    }
}
