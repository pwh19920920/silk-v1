package com.spark.bitrade.service;

import com.spark.bitrade.entity.PlatInstation;
import com.spark.bitrade.mapper.dao.PlatInstationMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 运营手动编辑站内信
 *
 * @author zhongxj
 * @date 2019.09.29
 */
@Service
@Slf4j
public class PlatInstationService {

    @Resource
    private PlatInstationMapper platInstationMapper;

    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    public PlatInstation findById(@Param("id") Long id) {
        return platInstationMapper.findById(id);
    }

    /**
     * 修改状态为，发送失败
     *
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePlatInstation(@Param("id") Long id) {
        platInstationMapper.updatePlatInstation(id);
    }
}
