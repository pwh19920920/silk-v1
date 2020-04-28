package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.Platform;
import com.spark.bitrade.dao.AppRevisionDao;
import com.spark.bitrade.dto.AppRevisionDto;
import com.spark.bitrade.entity.AppRevision;
import com.spark.bitrade.mapper.dao.AppRevisionMapper;
import com.spark.bitrade.service.Base.TopBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lingxing
 * @Description:对移动端发布版本管理
 * @date 2018/7/14 10:19
 */
@Service
@Slf4j
public class AppRevisionService extends TopBaseService<AppRevision, AppRevisionDao> {
    @Autowired
    AppRevisionMapper appRevisionMapper;
    @Autowired
    AppRevisionDao appRevisionDao;

    @CacheEvict(cacheNames = "appRevision", allEntries = true)
    @Autowired
    public void setDao(AppRevisionDao dao) {
        super.setDao(dao);
    }

    @Cacheable(cacheNames = "appRevision", key = "'entity:appRevision:'+#p.name()")
    public AppRevision findRecentVersion(Platform p) {
        //edit by tansitao 时间： 2018/5/5 原因：修改app版本更新
        return dao.findAppRevisionByPlatformOrderByIdDesc(p.getOrdinal());
    }

    /**
     * 版本添加
     *
     * @param appRevision
     * @return
     */
//    @WriteDataSource
    @CacheEvict(cacheNames = "appRevision", key = "'entity:appRevision:'+#appRevision.platform.name()")
    public AppRevision save(AppRevision appRevision) {
        String serviceName = "[app版本添加]";
        //查询移动端的版本是否存在
        List<AppRevision> appRevisionList = appRevisionMapper.findByPlatformAndVersion(appRevision.getPlatform().getOrdinal(), appRevision.getVersion());
        if (appRevisionList != null && appRevisionList.size() > 0) {
            throw new IllegalArgumentException(serviceName + "，版本号重复");
        }
        appRevisionDao.save(appRevision);
        return appRevision;
    }

    /**
     * 修改版本
     *
     * @param id          需要修改的Id
     * @param appRevision 改动的值
     * @return
     */
    @CacheEvict(cacheNames = "appRevision", key = "'entity:appRevision:'+#appRevision.platform.name()")
    public AppRevision update(Long id, AppRevision appRevision) {
        String serviceName = "[app版本修改]";
        AppRevision appRevisionList = appRevisionMapper.findById(id);
        if (appRevisionList == null) {
            throw new IllegalArgumentException(serviceName + "，修改版本不存在");
        }
        if (appRevision.getDownloadUrl() != null) {
            appRevisionList.setDownloadUrl(appRevision.getDownloadUrl());
        }
        if (appRevision.getRemark() != null) {
            appRevisionList.setRemark(appRevision.getRemark());
        }
        appRevisionDao.save(appRevisionList);
        return appRevision;
    }

    /**
     * 根据条件分页
     *
     * @param appRevisionDto 分页条件
     * @param pageModel      分页
     */
    @ReadDataSource
    public PageInfo<AppRevision> queryPage(AppRevisionDto appRevisionDto, PageModel pageModel) {
        Page<AppRevision> page = PageHelper.startPage(pageModel.getPageNo(), pageModel.getPageSize());
        this.appRevisionMapper.findByAppRevisionAll(appRevisionDto);
        return page.toPageInfo();
    }

    /**
     * 删除
     */
    @CacheEvict(cacheNames = "appRevision", key = "'entity:appRevision:'+#appRevision.platform.name()")
    public void removeById(Long id, AppRevision appRevision) {
        appRevisionDao.delete(id);
    }

    /**
     * 根据平台类型查询
     *
     * @param platfrom
     * @return
     */
    public AppRevision findRevisionByPlatFrom(String platfrom) {
        return appRevisionMapper.findRevisionByPlatFrom(platfrom);
    }
}
