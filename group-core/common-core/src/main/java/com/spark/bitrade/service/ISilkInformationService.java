package com.spark.bitrade.service;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.entity.SilkInformation;

/**
 * @author wsy
 * @date 2019/7/1 17:01
 */
public interface ISilkInformationService {

    /**
     * 分页查询资讯
     *
     * @param pageNo
     * @param pageSize
     * @param classify
     * @param tags
     * @param languageCode 语言，zh_CN：简体中文，zh_HK：繁体中文，en_US：English，ko_KR：韩文
     * @return
     */
    PageInfo<SilkInformation> page(int pageNo, int pageSize, String classify, String tags, String languageCode);

    SilkInformation findById(Long id);
}
