package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.dto.PartnerBusinessDto;
import com.spark.bitrade.entity.PartnerBusinessMonthTemp;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shenzucai
 * @since 2018-05-31
 */
@Mapper
public interface PartnerBusinessMonthTempMapper extends BaseMapper<PartnerBusinessMonthTemp> {

    /**
     * 清理合伙人业务统计明细表（当前统计周期和区域等级）
     * @author shenzucai
     * @time 2018.06.01 14:13
     * @param partnerBusinessDto
     * @return true
     */
    Integer deleteDataByStatisticalCycleAndLevel(PartnerBusinessDto partnerBusinessDto);

    /**
     * 根据条件将数据插入到partner_business
     * @author shenzucai
     * @time 2018.06.01 18:34
     * @param partnerBusinessDto
     * @return true
     */
    Integer savePartnerBusinessMonth(PartnerBusinessDto partnerBusinessDto);
}
