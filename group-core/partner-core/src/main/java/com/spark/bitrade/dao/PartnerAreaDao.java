package com.spark.bitrade.dao;

import com.spark.bitrade.constant.PartnerStaus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.DimArea;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.PartnerArea;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
  * 区域合伙人dao
  * @author tansitao
  * @time 2018/5/28 15:32 
  */
public interface PartnerAreaDao extends BaseDao<PartnerArea> {

    /**
     * 通过用户id、区域id、合伙人状态，判断用户是否存在
     * @author tansitao
     * @time 2018/5/30 16:32 
     */
    @Query(value="select * from partner_area p where (p.member_id = :memberId or p.area_id = :areaId) AND p.partner_staus = :partnerStaus limit 1",nativeQuery = true)
    PartnerArea findPartnerAreaByMemberOrDimArea(@Param("memberId")long memberId, @Param("areaId")String areaId, @Param("partnerStaus")String partnerStaus);

    /**
     * 通过id和状态查找合伙人信息
     * @author tansitao
     * @time 2018/5/30 15:43 
     */
    PartnerArea findPartnerAreaByMemberAndPartnerStaus(Member member, PartnerStaus partnerStaus);


    //add by yangch 时间： 2018.05.30 原因：根据区域和状态查询合伙人
    PartnerArea findPartnerAreaByDimAreaAndPartnerStaus(DimArea dimArea, PartnerStaus partnerStaus);

    //add by tansitao 时间： 2018/6/4 原因：添加通过区域模糊查询区域用户
    @Query(value="select * from partner_area p where p.area_id LIKE :areaId limit 1",nativeQuery = true)
    PartnerArea findLikeAreaId(@Param("areaId")String areaId);
}
