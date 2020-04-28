package com.spark.bitrade.service;

import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.spark.bitrade.entity.StartupImgConfig;
import com.spark.bitrade.mapper.dao.StartupImgConfigMapper;

@Service
public class StartupImgConfigService {

    @Resource
    private StartupImgConfigMapper startupImgConfigMapper;

    public int insert(StartupImgConfig pojo){
        return startupImgConfigMapper.insert(pojo);
    }

    public int insertList(List< StartupImgConfig> pojos){
        return startupImgConfigMapper.insertList(pojos);
    }

    public List<StartupImgConfig> select(StartupImgConfig pojo){
        return startupImgConfigMapper.select(pojo);
    }

    public int update(StartupImgConfig pojo){
        return startupImgConfigMapper.update(pojo);
    }

    /**
     * 查找所有数据
     * @author Zhang Yanjun
     * @time 2018.12.12 14:02
     * @param
     */
    @Cacheable(cacheNames = "startupImgConfig", key = "'entity:startupImgConfig:all'")
    public List<StartupImgConfig> findAll(){
        return startupImgConfigMapper.findAll();
    }

}
