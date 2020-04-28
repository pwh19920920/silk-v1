package com.spark.bitrade.service.impl;

import com.spark.bitrade.entity.C2cFeeStat;
import com.spark.bitrade.mapper.dao.C2cFeeStatMapper;
import com.spark.bitrade.mapper.dao.MemberMapper;
import com.spark.bitrade.service.IC2cFeeStatService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 总c2c交易手续费统计表 服务实现类
 * </p>
 *
 * @author fumy
 * @since 2018-06-19
 */
@Service
public class C2cFeeStatServiceImpl extends ServiceImpl<C2cFeeStatMapper, C2cFeeStat> implements IC2cFeeStatService {

    @Autowired
    MemberMapper memberMapper;

    @Override
    public List<C2cFeeStat> c2cFeeTotal(String startTime,String endTime) {
        return baseMapper.c2cFeeTotal(startTime,endTime);
    }

    @Override
    public List<C2cFeeStat> dayOfFeeStat(String startTime,String endTime) {
        return baseMapper.dayOfFeeStat(startTime,endTime);
    }

    @Override
    public String getMaxOpDate() {
        return baseMapper.getMaxOpDate();
    }

    @Override
    public int insertAndUpdate(C2cFeeStat cfs) {
        return baseMapper.insertAndUpdate(cfs);
    }

    @Override
    public int updateTotal(C2cFeeStat cfs) {
        return baseMapper.updateTotal(cfs);
    }

    @Override
    public List<C2cFeeStat> innerC2cFeeTotal(String startTime,String endTime) {
        String innerMemberId = getMemberIds(1);
        return baseMapper.innerC2cFeeTotal(startTime,endTime,innerMemberId);
    }

    @Override
    public List<C2cFeeStat> innerC2cDayOfFeeStat(String startTime,String endTime) {
        String innerMemberId = getMemberIds(1);
        return baseMapper.innerC2cDayOfFeeStat(startTime,endTime,innerMemberId);
    }

    @Override
    public String getInnerMaxOpDate() {
        return baseMapper.getInnerMaxOpDate();
    }

    @Override
    public int innerInsertAndUpdate(C2cFeeStat cfs) {
        return baseMapper.innerInsertAndUpdate(cfs);
    }

    @Override
    public int innerUpdateTotal(C2cFeeStat cfs) {
        return baseMapper.innerUpdateTotal(cfs);
    }

    @Override
    public int innerFeeCount() {
        return baseMapper.innerFeeCount();
    }

    @Override
    public boolean innerFeeInsert(C2cFeeStat cfs) {
        int row = baseMapper.innerFeeInsert(cfs);
        return row > 0 ? true : false;
    }

    //外部商家

    @Override
    public List<C2cFeeStat> outerC2cFeeTotal(String startTime,String endTime) {
        String innerMemberId = getMemberIds(1);
        return baseMapper.outerC2cFeeTotal(startTime,endTime,innerMemberId);
    }

    @Override
    public List<C2cFeeStat> outerC2cDayOfFeeStat(String startTime,String endTime) {
        String innerMemberId = getMemberIds(1);
        return baseMapper.outerC2cDayOfFeeStat(startTime,endTime,innerMemberId);
    }

    @Override
    public String getOuterMaxOpDate() {
        return baseMapper.getOuterMaxOpDate();
    }

    @Override
    public int outerInsertAndUpdate(C2cFeeStat cfs) {
        return baseMapper.outerInsertAndUpdate(cfs);
    }

    @Override
    public int outerUpdateTotal(C2cFeeStat cfs) {
        return baseMapper.outerUpdateTotal(cfs);
    }

    @Override
    public int outerFeeCount() {
        return baseMapper.outerFeeCount();
    }

    @Override
    public boolean outerFeeInsert(C2cFeeStat cfs) {
        int row = baseMapper.outerFeeInsert(cfs);
        return row > 0 ? true : false;
    }

    private String getMemberIds(int type){
        List<String> memberIdArray  = memberMapper.getMemberIds(type);
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<memberIdArray.size();i++) {
            if(i == (memberIdArray.size()-1) ){
                sb.append(memberIdArray.get(i));
            }else {
                sb.append(memberIdArray.get(i)).append(",");
            }
        }
        return sb.toString();
    }
}
