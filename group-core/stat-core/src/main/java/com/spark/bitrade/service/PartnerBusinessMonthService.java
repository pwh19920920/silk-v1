package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.dao.PartnerBusinessMonthDao;
import com.spark.bitrade.entity.PartnerBusinessMonth;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 合伙人业务月统计service
 * @author tansitao
 * @time 2018/5/28 15:44 
 */
@Service
public class PartnerBusinessMonthService extends BaseService<PartnerBusinessMonth> {
    @Autowired
    private PartnerBusinessMonthDao partnerBusinessMonthDao;

    public PartnerBusinessMonth save(PartnerBusinessMonth partnerBusinessMonth) {
        return partnerBusinessMonthDao.save(partnerBusinessMonth);
    }

    @Override
    public List<PartnerBusinessMonth> findAll() {
        return partnerBusinessMonthDao.findAll();
    }


    public PartnerBusinessMonth findById(Long id) {
        return partnerBusinessMonthDao.findOne(id);
    }

    public Page<PartnerBusinessMonth> findAll(Predicate predicate, Pageable pageable) {
        return partnerBusinessMonthDao.findAll(predicate, pageable);
    }

    public PartnerBusinessMonth findPartnerPartnerBusinessMonth(String areaId, String collectTime)
    {
        return partnerBusinessMonthDao.findPartnerBusinessMonthByAreaIdAndStatisticalCycle(areaId, collectTime);
    }
}
