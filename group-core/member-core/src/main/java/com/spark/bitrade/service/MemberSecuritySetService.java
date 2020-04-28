package com.spark.bitrade.service;

import com.spark.bitrade.dao.MemberSecuritySetDao;
import com.spark.bitrade.entity.MemberSecuritySet;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户权限认证service
 * @author tansitao
 * @time 2018/7/5 9:25 
 */
@Service
public class MemberSecuritySetService extends BaseService {
    @Autowired
    private MemberSecuritySetDao memberSecuritySetDao;

    @Cacheable(cacheNames = "memberSecuritySet", key = "'entity:memberSecuritySet:'+#id")
    public MemberSecuritySet findOneBymemberId(long id){
        return memberSecuritySetDao.findByMemberId(id);
    }

    @CacheEvict(cacheNames = "memberSecuritySet", key = "'entity:memberSecuritySet:'+#memberSecuritySet.memberId")
    public MemberSecuritySet save(MemberSecuritySet memberSecuritySet){
        return memberSecuritySetDao.save(memberSecuritySet);
    }

    /**
     * 关闭谷歌、手机验证
     * @author fumy
     * @time 2018.11.06 11:53
     * @param memberId
     * @param id
     * @param openColumnType
     * @return true
     */
    @CacheEvict(cacheNames = "memberSecuritySet", key = "'entity:memberSecuritySet:'+#memberId")
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSecurityStatus(long memberId, Long id,int openColumnType){
        int row = 0;
        switch (openColumnType){
            case 1 :
            //登录谷歌验证
                row = memberSecuritySetDao.updateSecurityStatus1(id);
                break;
            //提币谷歌认证列
            case 2 : row = memberSecuritySetDao.updateSecurityStatus2(id);
                break;
            //登录手机认证列
            case 3 : row = memberSecuritySetDao.updateSecurityStatus3(id);
                break;
            //提币手机认证列
            case 4 : row = memberSecuritySetDao.updateSecurityStatus4(id);
                break;
            default: break;
        }
        return  row > 0 ? true : false;
    }

}
