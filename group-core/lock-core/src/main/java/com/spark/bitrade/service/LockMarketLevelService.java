package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.dao.LockMarketLevelDao;
import com.spark.bitrade.entity.LockMarketLevel;
import com.spark.bitrade.entity.LockRewardLevelConfig;
import com.spark.bitrade.mapper.dao.LockMarketLevelMapper;
import com.spark.bitrade.util.SpringContextUtil;
import com.spark.bitrade.vo.StoSubInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会员CNYT市场等级service
 * @author Zhang Yanjun
 * @time 2018.12.03 19:49
 */
@Service
@Slf4j
public class LockMarketLevelService {
    @Autowired
    LockMarketLevelDao lockMarketLevelDao;
    @Autowired
    LockMarketLevelMapper lockMarketLevelMapper;
    @Autowired
    LockRewardLevelConfigService lockRewardLevelConfigService;

    /**
     * 保存
     * @author Zhang Yanjun
     * @time 2018.12.03 19:54
     * @param lockMarketLevel
     */
    ///@CacheEvict(cacheNames = "lockMarketLevel", allEntries = true)
    @CacheEvict(cacheNames = "lockMarketLevel", key = "'entity:lockMarketLevel:'" +
            "+#lockMarketLevel.getMemberId()+'-'+#lockMarketLevel.getSymbol()")
    public LockMarketLevel save(LockMarketLevel lockMarketLevel){
        //根据用户ID和币种组合为主键ID
        lockMarketLevel.setId(lockMarketLevel.getMemberId().toString().concat("-").concat(lockMarketLevel.getSymbol()));
        return lockMarketLevelDao.saveAndFlush(lockMarketLevel);
    }

    /**
     * 根据会员ID查询
     * @author Zhang Yanjun
     * @time 2018.12.03 20:06
     * @param memberId
     */
    @Cacheable(cacheNames = "lockMarketLevel", key = "'entity:lockMarketLevel:'+#memberId+'-'+#symbol")
    @ReadDataSource
    public LockMarketLevel findByMemberId(Long memberId, String symbol){
        // 查询为空的时候 创建一条默认的记录
        LockMarketLevel lockMarketLevel = lockMarketLevelMapper.findByMemberId(memberId, symbol);
        if(lockMarketLevel == null){
            //降最低配置设置为初始用户
            LockRewardLevelConfig lockRewardLevelConfig = lockRewardLevelConfigService.findMinimum(symbol);
            if(lockRewardLevelConfig != null){
                lockMarketLevel = new LockMarketLevel();
                lockMarketLevel.setLevel(lockRewardLevelConfig.getLevel());
                lockMarketLevel.setMemberId(memberId);
                lockMarketLevel.setMemberLevelId(Long.valueOf(lockRewardLevelConfig.getLevelId()));
                lockMarketLevel.setRewardRate(lockRewardLevelConfig.getRewardRate());
                lockMarketLevel.setStatus(BooleanEnum.IS_TRUE);
                lockMarketLevel.setSymbol(lockRewardLevelConfig.getSymbol());
                try {
                    lockMarketLevel = getService().save(lockMarketLevel);
                } catch (Exception e) {
                    log.warn("初始化用户的市场等级报错,{}", lockMarketLevel);
                    e.printStackTrace();
                }
            } else {
                log.warn("===========没有配置级差等级配置，不处理数据===========");
            }
        }
        return lockMarketLevel;
    }

    /**
     * 查询该会员的直接部门信息
     * @author Zhang Yanjun
     * @time 2018.12.25 15:23
     * @param memberId
     */
    @ReadDataSource
    public PageInfo<StoSubInfoVo> findSubInfoByMemberId(Long memberId,String defaultLevel, String symbol,int pageNo,int pageSize){
        Page<StoSubInfoVo> page = PageHelper.startPage(pageNo,pageSize);
        lockMarketLevelMapper.findSubInfoByMemberId(memberId,defaultLevel, symbol);
        return page.toPageInfo();
    }

    public LockMarketLevelService getService() {
        return SpringContextUtil.getBean(LockMarketLevelService.class);
    }
}
