package com.spark.bitrade.service;

import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.dao.MemberPromotionDao;
import com.spark.bitrade.entity.MemberPromotion;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.vo.RegisterPromotionVO;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
@Service
public class MemberPromotionService extends BaseService {

    @Autowired
    private MemberPromotionDao memberPromotionDao;


    public MemberPromotion save(MemberPromotion memberPromotion){
        return memberPromotionDao.save(memberPromotion);
    }

    //add by yangch 时间： 2018.04.29 原因：合并
    public Page<RegisterPromotionVO> getPromotionDetails(long memberId, PageModel pageModel){

        StringBuilder headSql = new StringBuilder("select a.id id ,a.username presentee,a.email presenteeEmail, ")
                .append("a.mobile_phone presenteePhone,a.real_name presenteeRealName,a.registration_time promotionTime");

        StringBuilder endSql = new StringBuilder(" from member a where a.inviter_id = "+memberId);

        StringBuilder countHead = new StringBuilder("select count(*) ") ;
        Page<RegisterPromotionVO> page = createNativePageQuery(countHead.append(endSql),headSql.append(endSql),pageModel,Transformers.aliasToBean(RegisterPromotionVO.class)) ;
        return page ;
    }
}
