package com.spark.bitrade.service;

import com.spark.bitrade.mapper.dao.ThirdAuthInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author fumy
 * @time 2018.09.19 17:59
 */
@Service
public class ThridAuthInfoService {

    @Autowired
    ThirdAuthInfoMapper thirdAuthInfoMapper;

    /**
     * 查询该账户某个币种是否具有查询资格
     * @author fumy
     * @time 2018.09.19 18:02
     * @param memberId
     * @param symbol
     * @return true
     */
    public boolean getAuthByMerberIdAndSymbol(Long memberId,String symbol){
        int row = thirdAuthInfoMapper.getAuthByMerberIdAndSymbol(memberId, symbol);
        return  row > 0 ? true : false;
    }

    public List<Map<String,String>> getAuthCoin(Long memberId){
        return thirdAuthInfoMapper.getAuthCoin(memberId);
    }
}
