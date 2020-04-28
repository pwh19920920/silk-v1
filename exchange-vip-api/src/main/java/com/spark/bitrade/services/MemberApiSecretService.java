package com.spark.bitrade.services;

import com.spark.bitrade.entity.MemberApiSecretDTO;
import com.spark.bitrade.mapper.dao.MemberApiSecretMapper;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * <p>用户接口认证信息查询服务</p>
 * @author tian.bo
 * @date 2018-12-5
 */
@Service
public class MemberApiSecretService extends BaseService {

    @Autowired
    private MemberApiSecretMapper memberApiSecretMapper;

    /**
     * 根据访问秘钥查询会员api信息
     * @param accessKey
     *             访问秘钥
     * @return
     */
    @Cacheable(cacheNames = "apikey", key = "'entity:memberApiSecretDTO:'+#accessKey")
    public MemberApiSecretDTO selectByAccessKey(String accessKey){
        return memberApiSecretMapper.selectByAccessKey(accessKey);
    }

}
