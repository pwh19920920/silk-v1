package com.spark.bitrade.service.impl;

import com.spark.bitrade.dto.PartnerBusinessDto;
import com.spark.bitrade.entity.PartnerBusinessMonthTemp;
import com.spark.bitrade.mapper.dao.PartnerBusinessMonthTempMapper;
import com.spark.bitrade.service.IPartnerBusinessMonthTempService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author shenzucai
 * @since 2018-05-31
 */
@Service
public class PartnerBusinessMonthTempServiceImpl extends ServiceImpl<PartnerBusinessMonthTempMapper, PartnerBusinessMonthTemp> implements IPartnerBusinessMonthTempService {


    @Autowired
    private PartnerBusinessMonthTempMapper partnerBusinessMonthTempMapper;
    /**
     * 清理合伙人业务统计明细表（当前统计周期和区域等级）
     *
     * @param partnerBusinessDto
     * @return true
     * @author shenzucai
     * @time 2018.06.01 14:13
     */
    @Override
    public Integer deleteDataByStatisticalCycleAndLevel(PartnerBusinessDto partnerBusinessDto) {
        return partnerBusinessMonthTempMapper.deleteDataByStatisticalCycleAndLevel(partnerBusinessDto);
    }

    /**
     * 根据条件将数据插入到partner_business
     *
     * @param partnerBusinessDto
     * @return true
     * @author shenzucai
     * @time 2018.06.01 18:34
     */
    @Override
    public Integer savePartnerBusinessMonth(PartnerBusinessDto partnerBusinessDto) {
        return partnerBusinessMonthTempMapper.savePartnerBusinessMonth(partnerBusinessDto);
    }
}
