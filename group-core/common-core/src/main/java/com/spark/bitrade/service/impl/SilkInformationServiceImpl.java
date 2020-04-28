package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.entity.SilkInformation;
import com.spark.bitrade.mapper.dao.SilkInformationMapper;
import com.spark.bitrade.service.ISilkInformationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author wsy
 */
@Service
public class SilkInformationServiceImpl extends ServiceImpl<SilkInformationMapper, SilkInformation> implements ISilkInformationService {

    @Autowired
    private SilkInformationMapper silkInformationMapper;

    @Override
    public PageInfo<SilkInformation> page(int pageNo, int pageSize, String classify, String tags, String languageCode) {
        EntityWrapper<SilkInformation> wrapper = new EntityWrapper<>();
        if (StringUtils.isNotBlank(classify)) {
            wrapper.where("classify", classify);
        }
        if (StringUtils.isNotBlank(tags)) {
            wrapper.like("CONCAT(',',tags,',')", tags);
        }
        if (StringUtils.isNotBlank(languageCode)) {
            wrapper.eq("language_code", languageCode);
        }
        wrapper.eq("status", 1);
        wrapper.le("release_time", new Date());
        wrapper.orderBy("release_time", false);

        // 分页查询
        com.github.pagehelper.Page<SilkInformation> page = PageHelper.startPage(pageNo, pageSize);
        silkInformationMapper.selectList(wrapper);
        return page.toPageInfo();
    }

    @Override
    public SilkInformation findById(Long id) {
        EntityWrapper<SilkInformation> wrapper = new EntityWrapper<>();
        wrapper.eq("status", 1);
        wrapper.eq("id", id);
        return selectOne(wrapper);
    }
}
