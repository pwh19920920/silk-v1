package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.BettingConfigStatus;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.dao.BettingConfigDao;
import com.spark.bitrade.dao.BettingPriceRangeDao;
import com.spark.bitrade.dto.RecordDTO;
import com.spark.bitrade.entity.BettingConfig;
import com.spark.bitrade.entity.BettingPriceRange;
import com.spark.bitrade.mapper.dao.BettingConfigMapper;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.SpringContextUtil;
import com.spark.bitrade.vo.BettingConfigVo;
import com.spark.bitrade.vo.GuessConfigVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author fumy
 * @time 2018.09.13 14:02
 */
@Service
public class BettingConfigService extends BaseService{

    @Autowired
    BettingConfigDao bettingConfigDao;
    @Autowired
    BettingConfigMapper bettingConfigMapper;

    @Autowired
    BettingPriceRangeDao rangeDao;

    /**
     * 保存新一期投注配置
     * @author fumy
     * @time 2018.09.13 14:26
     * @param config
     * @return true
     */
    @CacheEvict(cacheNames = {"bettingConfig","bettingPriceRange"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void save(BettingConfig config,List<BettingPriceRange> rangeList) {
        BettingConfig config1 = bettingConfigDao.save(config);
        for(BettingPriceRange range:rangeList){
            range.setPeriodId(config1.getId());
            rangeDao.save(range);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addRange(BettingPriceRange range) {
        rangeDao.save(range);
    }

    //add by tansitao 时间： 2018/9/14 原因：清空缓存
//    @CacheEvict(cacheNames = {"bettingConfig"}, allEntries = true)
//    public BettingConfig save(BettingConfig config){
//        return bettingConfigDao.save(config);
//    }

    /***
      * 通过竞猜币种查找最新一期的生效活动
      * @author yangch
      * @time 2018.09.14 14:23 
     * @param
     */
    //@Cacheable(cacheNames = "bettingConfig", key = "'entity:bettingConfig:' + #guessSymbol")
    public BettingConfig findOneLately(String guessSymbol){
        return bettingConfigMapper.findByGuessSymbolLately(guessSymbol);
    }

    /***
     * 查询最新一期所有有效的活动
     * @author yangch
     * @time 2018.09.14 14:23 
     * @param
     */
    @Cacheable(cacheNames = "bettingConfig", key = "'entity:bettingConfig:all'")
    public List<BettingConfig> findAllOfLately(){
        return bettingConfigMapper.findAllOfLately();
    }

    /**
     * 通过竞猜币种查找最新一期的生效活动
     * @author tansitao
     * @time 2018/9/14 10:15 
     */
    public GuessConfigVo findByGuessSymbolLately (String guessSymbol){
        //BettingConfig bettingConfig = bettingConfigMapper.findByGuessSymbolLately(guessSymbol);
        BettingConfig bettingConfig = getService().findOneLately(guessSymbol);
        GuessConfigVo guessConfig = null;
        if(bettingConfig != null){
            guessConfig = GuessConfigVo.builder().backRatio(bettingConfig.getBackRatio())
                    .beginTime(bettingConfig.getBeginTime())
                    .betSymbol(bettingConfig.getBetSymbol())
                    .endTime(bettingConfig.getEndTime())
                    .guessSymbol(bettingConfig.getGuessSymbol())
                    .id(bettingConfig.getId())
                    .lowerLimit(bettingConfig.getLowerLimit())
                    .name(bettingConfig.getName())
                    .nextPeriodRatio(bettingConfig.getNextPeriodRatio())
                    .period(bettingConfig.getPeriod())
                    .prizeBeginTime(bettingConfig.getPrizeBeginTime())
                    .prizeEndTime(bettingConfig.getPrizeEndTime())
                    .prizePrice(bettingConfig.getPrizePrice())
                    .prizeRatio(bettingConfig.getPrizeRatio())
                    .prizeSymbol(bettingConfig.getPrizeSymbol())
                    .rebateRatio(bettingConfig.getRebateRatio())
                    .redpacketBeginTime(bettingConfig.getRedpacketBeginTime())
                    .redpacketEndTime(bettingConfig.getRedpacketEndTime())
                    .redpacketGradeRatio(bettingConfig.getRedpacketGradeRatio())
                    .redpacketRatio(bettingConfig.getRedpacketRatio())
                    .redpacketSymbol(bettingConfig.getRedpacketSymbol())
                    .redpacketUseNum(bettingConfig.getRedpacketUseNum())
                    .remark(bettingConfig.getRemark())
                    .status(bettingConfig.getStatus())
                    .redpacketPrizeSymbol(bettingConfig.getRedpacketPrizeSymbol())
                    .openTime(bettingConfig.getOpenTime())
                    .redpacketState(bettingConfig.getRedpacketState()) //add by tansitao 时间： 2018/10/24 原因：增加红包状态
                    .build();
        }
        return guessConfig;
    }

    /**
     * 游戏管理分页
     * @author Zhang Yanjun
     * @time 2018.09.13 14:19
     * @param period 期数
     * @param status 活动状态
     */
    @ReadDataSource
    public PageInfo<BettingConfigVo> findAllByPeriodAndStatus(String period, Long status, int pageNo, int pageSize){
        Page<BettingConfigVo> page= PageHelper.startPage(pageNo,pageSize);
        bettingConfigMapper.findAllByPeriodAndStatus(period,status);
        return page.toPageInfo();
    }

    /**
     * 根据id查询配置详情
     * @author fumy
     * @time 2018.09.14 15:42
     * @param id
     * @return true
     */
    @ReadDataSource
//    @Cacheable(cacheNames = "bettingConfig", key = "'entity:bettingConfig:id-' + #id")
    public BettingConfig findConfigById(Long id){
        return bettingConfigMapper.queryConfigById(id);
    }

    @ReadDataSource
    public BettingConfig findConfigByIdRealtime(Long id){
        return bettingConfigMapper.queryConfigById(id);
    }

    /**
     * 根据id清除配置缓存
     * @author fumy
     * @time 2018.09.25 11:33
     * @param id
     * @return true
     */
    @CacheEvict(cacheNames = {"bettingConfig","bettingPriceRange"}, allEntries = true)
    public void flushByConfigId(Long id){

    }

    /**
     * 根据配置id查询对应的价格区间列表
     * @author fumy
     * @time 2018.09.14 16:58
     * @param id
     * @return true
     */
    @ReadDataSource
    @Cacheable(cacheNames = "bettingPriceRange", key = "'entity:bettingPriceRange:all:id-' + #id")
    public List<BettingPriceRange> queryPriceRangeByConfigId(Long id){
        return bettingConfigMapper.queryPriceRangeByConfigId(id);
    }

    /**
     * 根据id逻辑删除配置信息
     * @author fumy
     * @time 2018.09.14 15:10
     * @param id
     * @return true
     */
    @CacheEvict(cacheNames = {"bettingConfig"}, allEntries = true)
    @Transactional
    public boolean deleteById(Long id){
        int row = bettingConfigDao.deleteById(id);
        return row > 0 ? true : false;
    }

    /**
     * 根据传入的id删除价格区间
     * @author fumy
     * @time 2018.09.14 17:26
     * @param ids
     * @return true
     */
    @CacheEvict(cacheNames = {"bettingConfig"}, allEntries = true)
    public void deleteRangeById(Long[] ids){
        for(Long id:ids) {
            rangeDao.delete(id);
        }
    }

    /**
     *
     * 根据id更新配置状态
     * @author fumy
     * @time 2018.09.14 15:21
     * @param id
     * @param status
     * @return true
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"bettingConfig"}, allEntries = true)
    public boolean updateById(Long id, BettingConfigStatus status){
        int row = bettingConfigDao.updateConfigStatusById(id,status);
        return row > 0 ? true : false;
    }

    /**
     *
     * 根据id更新红包的状态
     * @author fumy
     * @time 2018.09.14 15:21
     * @param id
     * @param status
     * @return true
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"bettingConfig"}, allEntries = true)
    public boolean updateConfigRedpacketStateById(Long id, BooleanEnum status){
        int row = bettingConfigDao.updateConfigRedpacketStateById(id,status.getOrdinal());
        return row > 0 ? true : false;
    }

    /***
     * 异步更新配置状态
     * @author yangch
     * @time 2018.09.15 11:46 
     * @param id
     * @param newStatus
     */
    @Async
    public boolean asnycUpdateById(Long id, BettingConfigStatus newStatus){
        return getService().updateById(id, newStatus);
    }

    public BettingConfigService getService(){
        return SpringContextUtil.getBean(BettingConfigService.class);
    }

    /**
     * 是否存在正在进行中的投注活动
     * @author fumy
     * @time 2018.09.15 10:37
     * @param
     * @return true
     */
    public boolean isHaveRunningConfig(){
        int row = bettingConfigMapper.queryIsRunningConfig();
        return  row > 0 ? false : true;
    }

    /**
     * 分页获取每一期的中奖记录
     * @author tansitao
     * @time 2018/9/17 11:42 
     */
    @Cacheable(cacheNames = "bettingConfig", key = "'entity:bettingConfig:records:'")
    public PageInfo<RecordDTO> findAllRecord(int pageNum, int pageSize) {
        Page<RecordDTO> page = PageHelper.startPage(pageNum, pageSize);
        bettingConfigMapper.findAllRecord();
        return page.toPageInfo();
        }

    /**
      * 查询某一期的中奖数据
      * @author tansitao
      * @time 2018/9/17 11:42 
      */
    @Cacheable(cacheNames = "bettingConfig", key = "'entity:bettingConfig:records-' + #periodId")
    @ReadDataSource
    public RecordDTO findOneRecord(long periodId) {
        return bettingConfigMapper.findOneRecord(periodId);
    }

    /***
      * 更新中奖价格
      * @author yangch
      * @time 2018.09.17 23:13 
     * @param id
     * @param prizePrice
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"bettingConfig"}, allEntries = true)
    public int updatePrizePriceById(Long id, BigDecimal prizePrice){
        return bettingConfigDao.updatePrizePriceById(id, prizePrice);
    }

    /**
     * 根据当前id查询上期已完成的活动配置
     * @param params
     * @return
     */
    public BettingConfig findForwardBetConfig(Map<String,Object> params){
        return bettingConfigMapper.findForwardBetConfig(params);
    }
}
