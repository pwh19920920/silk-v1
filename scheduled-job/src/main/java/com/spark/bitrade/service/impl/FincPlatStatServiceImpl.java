package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.entity.FincPlatStat;
import com.spark.bitrade.mapper.dao.FincPlatStatMapper;
import com.spark.bitrade.mapper.dao.MemberMapper;
import com.spark.bitrade.service.IFincPlatStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 平台货币统计表 服务实现类
 * </p>
 *
 * @author shenzucai
 * @since 2018-05-13
 */
@Service
public class FincPlatStatServiceImpl extends ServiceImpl<FincPlatStatMapper, FincPlatStat> implements IFincPlatStatService {

    @Autowired
    MemberMapper memberMapper;

    /**
     * 获取时间段内前的数据
     *
     * @return true
     * @author shenzucai
     * @time 2018.05.13 15:50
     */
    @Override
    public List<FincPlatStat> listPlatStat(String startTime,String endTime) {
        //1--内部商户，2--操盘手，3--员工商户
        String types="1,2,3";
        List<String> memberList = memberMapper.getMemberIdsByTypes(types);
        String innerId = memberList.get(0);
        String traderId = memberList.get(1);
        String employeeId = memberList.get(2);
        return baseMapper.listPlatStat(startTime,endTime,traderId,innerId,employeeId);
    }
}
