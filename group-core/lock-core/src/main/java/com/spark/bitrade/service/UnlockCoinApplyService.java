package com.spark.bitrade.service;

import com.spark.bitrade.constant.CertifiedBusinessStatus;
import com.spark.bitrade.dao.*;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.UnlockCoinApply;
import com.spark.bitrade.service.Base.TopBaseService;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UnlockCoinApplyService extends TopBaseService<UnlockCoinApply,UnLockCoinApplyDao> {

    @Autowired
    public void setDao(UnLockCoinApplyDao dao) {
        super.setDao(dao);
    }

    @Autowired
    private OrderDao orderDao ;

    @Autowired
    private AppealDao appealDao ;

    @Autowired
    private AdvertiseDao advertiseDao ;

    @Autowired
    private MemberDao memberDao ;


    public List<UnlockCoinApply> findByMemberAndStaus(Member member, CertifiedBusinessStatus status){
        return dao.findByMemberAndStatusOrderByIdDesc(member,status);
    }

    public List<UnlockCoinApply> findByMember(Member member){
        return dao.findByMemberOrderByIdDesc(member);
    }

    public Map<String,Object> getBusinessOrderStatistics(Long memberId) {
       return orderDao.getBusinessStatistics(memberId);
    }

    public  Map<String,Object> getBusinessAppealStatistics(Long memberId){
        Map<String,Object> map = new HashedMap();
        Long complainantNum = appealDao.getBusinessAppealInitiatorIdStatistics(memberId);
        Long defendantNum = appealDao.getBusinessAppealAssociateIdStatistics(memberId);
        map.put("defendantNum",defendantNum);
        map.put("complainantNum",complainantNum);
        return map ;
    }

    public Long getAdvertiserNum(Long memberId) {
        Member member = memberDao.findOne(memberId);
        return advertiseDao.getAdvertiseNum(member);
    }

    public long countAuditing(){
        return dao.countAllByStatus(CertifiedBusinessStatus.CANCEL_AUTH);
    }


}
