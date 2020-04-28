package com.spark.bitrade.service;

import com.spark.bitrade.entity.FincMemberAccount;
import com.spark.bitrade.mapper.dao.FincMemberAccountMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

/**
 * @author Zhang Yanjun
 * @time 2018.12.18 11:48
 */
@Service
public class FincMemberAccountService {

    @Resource
    private FincMemberAccountMapper fincMemberAccountMapper;

    public int insert(FincMemberAccount pojo){
        return fincMemberAccountMapper.insert(pojo);
    }

    public int insertList(List< FincMemberAccount> pojos){
        return fincMemberAccountMapper.insertList(pojos);
    }

    public List<FincMemberAccount> select(FincMemberAccount pojo){
        return fincMemberAccountMapper.select(pojo);
    }

    public int update(FincMemberAccount pojo){
        return fincMemberAccountMapper.update(pojo);
    }

    /**
     * 根据类型查找
     * @author Zhang Yanjun
     * @time 2018.12.18 11:46
     * @param memberType
     */
    public List<FincMemberAccount> findByType(int memberType){
        return fincMemberAccountMapper.findByType(memberType);
    }

}
