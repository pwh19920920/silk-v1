package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.dao.LockMarketPerformanceTotalDao;
import com.spark.bitrade.entity.LockMarketLevel;
import com.spark.bitrade.entity.LockMarketPerformanceTotal;
import com.spark.bitrade.mapper.dao.LockMarketPerformanceTotalMapper;
import com.spark.bitrade.vo.StoLockDepVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 会员市场奖励业绩总累计service
 * @author Zhang Yanjun
 * @time 2018.12.03 19:49
 */
@Service
public class LockMarketPerformanceTotalService {
    @Autowired
    LockMarketPerformanceTotalDao lockMarketPerformanceTotalDao;
    @Autowired
    LockMarketPerformanceTotalMapper lockMarketPerformanceTotalMapper;

    /**
     * 保存
     * @author Zhang Yanjun
     * @time 2018.12.03 19:54
     * @param lockMarketPerformanceTotal
     */
    @CacheEvict(cacheNames = "lockMarketPerformanceTotal",
            key = "'entity:lockMarketPerformanceTotal:'" +
                    "+#lockMarketPerformanceTotal.getMemberId()+'-'+#lockMarketPerformanceTotal.getSymbol()")
    public void save(LockMarketPerformanceTotal lockMarketPerformanceTotal){
        if(lockMarketPerformanceTotal.getId() == null){
            lockMarketPerformanceTotal.setId(
                    lockMarketPerformanceTotal.getMemberId().toString()
                            .concat("-")
                            .concat(lockMarketPerformanceTotal.getSymbol()));
        }

        lockMarketPerformanceTotalDao.save(lockMarketPerformanceTotal);
    }

    /**
     * 更新业绩
     * @author tansitao
     * @time 2018/12/5 18:12 
     */
    public void updataPerformance(BigDecimal performanceTurnover, Long memberId){
        lockMarketPerformanceTotalDao.updataPerformance(performanceTurnover, memberId);
    }



    /**
     * 根据会员ID查询
     * @author Zhang Yanjun
     * @time 2018.12.03 20:06
     * @param memberId
     */
    @Cacheable(cacheNames = "lockMarketPerformanceTotal",
            key = "'entity:lockMarketPerformanceTotal:'+#memberId+'-'+#symbol")
    @ReadDataSource
    public LockMarketPerformanceTotal findByMemberId(Long memberId, String symbol ){
        return lockMarketPerformanceTotalMapper.findByMemberId(memberId, symbol);
    }

    /**
     * 查询子部门列表
     * @author Zhang Yanjun
     * @time 2018.12.03 20:46
     * @param memebrId
     */
    //@Cacheable(cacheNames = "lockMarketPerformanceTotal", key = "'entity:lockMarketPerformanceTotal:iniviteId:'+#memberId")
    @ReadDataSource
    public List<LockMarketPerformanceTotal> findAllByInivite(Long memebrId, String symbol){
        return lockMarketPerformanceTotalMapper.findAllByInivite(memebrId, symbol);
    }

    /**
     * 用户子部门锁仓汇总信息
     * @author Zhang Yanjun
     * @time 2018.12.05 10:51
     * @param memberId
     * @param pageNo
     * @param pageSize
     */
    @ReadDataSource
    public PageInfo<StoLockDepVo> findTotalByInivite(Long memberId, String symbol, int pageNo, int pageSize, String startTime, String endTime){
        Page<StoLockDepVo> page= PageHelper.startPage(pageNo, pageSize);
        lockMarketPerformanceTotalMapper.findTotalByInivite(memberId, symbol, startTime, endTime);
        return page.toPageInfo();
    }

}
