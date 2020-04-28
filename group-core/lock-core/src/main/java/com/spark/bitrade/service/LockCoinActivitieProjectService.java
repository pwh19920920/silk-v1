package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.ActivitieType;
import com.spark.bitrade.constant.LockSettingStatus;
import com.spark.bitrade.dao.LockCoinActivitieProjectDao;
import com.spark.bitrade.dto.LockCoinActivitieProjectDto;
import com.spark.bitrade.entity.LockCoinActivitieProject;
import com.spark.bitrade.entity.LockCoinActivitieSetting;
import com.spark.bitrade.entity.LockCoinDetail;
import com.spark.bitrade.mapper.dao.LockCoinDetailMapper;
import com.spark.bitrade.mapper.dao.LockCoinMapper;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.MessageResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  * 活动配置service
 *  * @author tansitao
 *  * @time 2018/6/14 10:34 
 *  
 */
@Service
public class LockCoinActivitieProjectService extends BaseService {

    @Autowired
    private LockCoinActivitieProjectDao dao;

    @Resource
    private LockCoinMapper mapper;

    @Resource
    private LockCoinDetailMapper lockCoinDetailMapper;

    private static Map<String, Integer> languageMap = new HashMap<>();

    static {
        languageMap.put("zh_HK", 1);
        languageMap.put("en_US", 2);
        languageMap.put("ko_KR", 3);
    }

    //    @CacheEvict(cacheNames = "lockCoinActivitieProject", key = "'entity:lockCoinActivitieProject:'+#entity.id")
    //edit by tansitao 时间： 2018/11/20 原因：清空所有活动缓存
    @CacheEvict(cacheNames = "lockCoinActivitieProject", allEntries = true)
    public LockCoinActivitieProject save(LockCoinActivitieProject entity) {
        return dao.save(entity);
    }

    @Cacheable(cacheNames = "lockCoinActivitieProject", key = "'entity:lockCoinActivitieProject:'+#id")
    public LockCoinActivitieProject findOne(Long id) {
        return dao.findOne(id);
    }

    /**
     *  * 分页查询
     *  * @author tansitao
     *  * @time 2018/6/13 10:33 
     *  
     */
    public Page<LockCoinActivitieProject> findAll(Predicate predicate, Pageable pageable) {
        return dao.findAll(predicate, pageable);
    }

    @ReadDataSource
    public List<LockCoinActivitieProject> findAllEnableProject(ActivitieType type) {
        if (type == null) {
            return mapper.findAllEnableProject(null);
        }
        return mapper.findAllEnableProject(String.valueOf(type.getOrdinal()));
    }

    @ReadDataSource
    public Map<String, List<LockCoinActivitieProjectDto>> findAllEnableFinancialProject(ActivitieType type, HttpServletRequest request) {
        String language = request.getHeader("language");
        if (StringUtils.isEmpty(language)) {
            language = "zh_CN";
        }
        Map<String, List<LockCoinActivitieProjectDto>> projects = new HashMap<>();

        String types=type==null?null:String.valueOf(type.getOrdinal());
        List<LockCoinActivitieProjectDto> allHotProject= mapper.findAllHotProject(types);
        List<LockCoinActivitieProjectDto> allTopProject= mapper.findAllTopProject(types);
        if (!"zh_CN".equalsIgnoreCase(language)){
            Integer integer = languageMap.get(language);
            allHotProject.forEach(f->{
                Map<String,String> map = mapper.findNameByIdAndLanguage(f.getId(), integer);
                if(!CollectionUtils.isEmpty(map)){
                    f.setName(map.get("name"));
                    f.setBriefDescription(map.get("brief_description"));
                }
            });
            allTopProject.forEach(f->{
                Map<String,String> map = mapper.findNameByIdAndLanguage(f.getId(), integer);
                if(!CollectionUtils.isEmpty(map)){
                    f.setName(map.get("name"));
                    f.setBriefDescription(map.get("brief_description"));
                }
            });
        }
        projects.put("hot", allHotProject);
        projects.put("top", allTopProject);
        return projects;
    }

    /**
     *  * 获取所有生效中的活动
     *  * @author tansitao
     *  * @time 2018/11/20 11:28 
     *  
     */
    @ReadDataSource
    @Cacheable(cacheNames = "lockCoinActivitieProject", key = "'entity:lockCoinActivitieProject:allProject:' + #unit")
    public List<LockCoinActivitieProject> findAllProjectByUnit(String unit) {
        return mapper.findAllEnableProjectByUnit(unit);
    }

    /**
     *  * 通过活动类型和活动状态查询所有活动
     *  * @author tansitao
     *  * @time 2018/6/21 9:19 
     *  
     */
    public List<LockCoinActivitieProject> findUnenforcedLockActivity(ActivitieType type, LockSettingStatus status) {
        return dao.findAllByTypeAndStatus(type, status);
    }

    /**
     *  * 查询所有活动
     *  * @author tansitao
     *  * @time 2018/6/14 14:53 
     *  
     */
    @Override
    public List<LockCoinActivitieProject> findAll() {
        return dao.findAll();
    }

    /**
     *  * 增加活动参与数量
     *  * @author tansitao
     *  * @time 2018/6/14 15:54 
     *  
     */
    public MessageResult increaseBoughtAmount(long id, BigDecimal boughtAmount) {
        int ret = dao.increaseBoughtAmount(id, boughtAmount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("活动剩余份额不足");
        }
    }

    /**
     *  * 减少活动活动参与数量
     *  * @author tansitao
     *  * @time 2018/6/14 15:57 
     *  
     */
    public MessageResult decreaseBoughtAmount(long id, BigDecimal boughtAmount) {
        int ret = dao.decreaseBoughtAmount(id, boughtAmount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("活动不存在");
        }
    }

    public List<LockCoinActivitieProjectDto> listAll() {
        return lockCoinDetailMapper.listAll();
    }

    public List<LockCoinActivitieProjectDto> memberCount(List<Long> ids) {
        return lockCoinDetailMapper.memberCount(ids);
    }

    public LockCoinActivitieProjectDto findProjectById(Long activiteId) {
        return lockCoinDetailMapper.findProjectById(activiteId);
    }

    public List<LockCoinDetail> findByMemberIds(Long memberId, List<LockCoinActivitieSetting> list) {
        return lockCoinDetailMapper.findByMemberIds(memberId, list);
    }

    public List<LockCoinDetail> findByMemberId(Long memberId, Long activitieId) {
        return lockCoinDetailMapper.findByMemberId(memberId, activitieId);
    }

    public List<LockCoinActivitieSetting> selectByActivitieIds(List<Long> list) {
        return lockCoinDetailMapper.selectByActivitieIds(list);
    }

    public String findSettingNameByidAndLanguage(Long id, Integer integer){
        return mapper.findSettingNameByidAndLanguage(id,integer);
    }
}
