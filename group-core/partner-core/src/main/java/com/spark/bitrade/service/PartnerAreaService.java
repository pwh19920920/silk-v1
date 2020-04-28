package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.PartnerStaus;
import com.spark.bitrade.dao.PartnerAreaDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.mapper.dao.PartnerAreaMapper;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.vo.PartnerAreaVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 区域合伙人service
 * @author tansitao
 * @time 2018/5/28 15:44 
 */
@Service
public class PartnerAreaService extends BaseService<PartnerArea> {
    @Autowired
    private PartnerAreaDao partnerAreaDao;

    @Autowired
    private PartnerAreaMapper mapper;

    //@CacheEvict(cacheNames = "partnerArea", key = "'entity:partnerArea:'+#partnerArea.dimArea.areaId")
    @CacheEvict(cacheNames = "partnerArea", allEntries = true)
    public PartnerArea save(PartnerArea partnerArea) {
        return partnerAreaDao.save(partnerArea);
    }

    @Override
    @Cacheable(cacheNames = "partnerArea", key = "'entity:partnerArea:all'")
    public List<PartnerArea> findAll() {
        return partnerAreaDao.findAll();
    }


    @Cacheable(cacheNames = "partnerArea", key = "'entity:partnerArea:id-'+#id")
    public PartnerArea findById(Long id) {
        return partnerAreaDao.findOne(id);
    }

    /**
     * 通过合伙人id和状态查找用户
     * @author tansitao
     * @time 2018/6/1 15:02 
     */
    @Cacheable(cacheNames = "partnerArea", key = "'entity:partnerArea:'+#dimArea.areaId+'-'+#partnerStaus.getOrdinal()")
    public PartnerArea findByAreaAndStatus(DimArea dimArea, PartnerStaus partnerStaus) {
        return partnerAreaDao.findPartnerAreaByDimAreaAndPartnerStaus(dimArea, partnerStaus);
    }


    @Cacheable(cacheNames = "partnerArea", key = "'entity:partnerArea:'+#member.areaId+'-'+#member.id+'-0'")
    public PartnerArea findByMemberAndStatus(Member member) {
        return partnerAreaDao.findPartnerAreaByMemberAndPartnerStaus(member, PartnerStaus.normal);
    }

    @CacheEvict(cacheNames = "partnerArea", allEntries = true)
    public void deleteById(Long id) {
        partnerAreaDao.delete(id);
    }

    @CacheEvict(cacheNames = "partnerArea", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long[] ids) {
        for (Long id : ids) {
            PartnerArea partnerArea = findById(id);
            Assert.notNull(partnerArea, "validate id!");
            deleteById(id);
        }
    }


    public Page<PartnerArea> findAll(Predicate predicate, Pageable pageable) {
        return partnerAreaDao.findAll(predicate, pageable);
    }

    /**
     * 导出列表查询
     * @param account
     * @param areaId
     * @return
     */
    public List<PartnerAreaVo>  findPartnerForout(String account, String areaId){
        List<PartnerAreaVo> list=mapper.getPartnerList(account, areaId);
        for (int i=0;i<list.size();i++){
            list.get(i).setPartnerStausOut(list.get(i).getPartnerStaus().getCnName());//合伙人状态
        }
        return list;
    }


    /**
     * 通过用户id、区域id、合伙人状态，判断用户是否存在
     * @author tansitao
     * @time 2018/5/30 16:30 
     */
    public PartnerArea findPartnerAreaByMemberIdOrAreaId(long memberId, String areaId, String partnerStaus)
    {
        return partnerAreaDao.findPartnerAreaByMemberOrDimArea(memberId, areaId, partnerStaus);
    }

    //add by yangch 时间： 2018.05.30 原因：根据区域查询合伙人
    @Cacheable(cacheNames = "partnerArea", key = "'entity:partnerArea:'+#dimArea.areaId")
    public PartnerArea findPartnerAreaByAreaId(DimArea dimArea)
    {
        return partnerAreaDao.findPartnerAreaByDimAreaAndPartnerStaus(dimArea, PartnerStaus.normal);
    }

    //add by tansitao 时间： 2018/6/4 原因：通过区域id模糊查询合伙人
    public PartnerArea findByDimAreaEndingWith(DimArea dimArea)
    {
        return partnerAreaDao.findLikeAreaId(dimArea.getAreaId() + '%');
    }

}
