package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.dao.PartnerBusinessDao;
import com.spark.bitrade.entity.PartnerBusiness;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 合伙人业务明细service
 * @author tansitao
 * @time 2018/5/28 15:44 
 */
@Service
public class PartnerBusinessService extends BaseService<PartnerBusiness> {
    @Autowired
    private PartnerBusinessDao partnerBusinessDao;

    public PartnerBusiness save(PartnerBusiness partnerBusiness) {
        return partnerBusinessDao.save(partnerBusiness);
    }

    @Override
    public List<PartnerBusiness> findAll() {
        return partnerBusinessDao.findAll();
    }


    public PartnerBusiness findById(Long id) {
        return partnerBusinessDao.findOne(id);
    }

    public Page<PartnerBusiness> findAll(Predicate predicate, Pageable pageable) {
        return partnerBusinessDao.findAll(predicate, pageable);
    }

    /**
     * 通过区域id查询合伙人业务累计信息
     * @author tansitao
     * @time 2018/5/29 18:51 
     */
    public PartnerBusiness findPartnerBusinessByAreaId(String areaId)
    {
        return partnerBusinessDao.findPartnerBusinessByAreaId(areaId);
    }

}
