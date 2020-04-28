package com.spark.bitrade.service;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.dao.CoinDao;
import com.spark.bitrade.dao.MemberAddressDao;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.MemberAddress;
import com.spark.bitrade.pagination.Criteria;
import com.spark.bitrade.pagination.Restrictions;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.MessageResult;
import com.sparkframework.sql.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Zhang Jinwei
 * @date 2018年01月26日
 */
@Service
public class MemberAddressService extends BaseService {
    @Autowired
    private MemberAddressDao memberAddressDao;
    @Autowired
    //private CoinDao coinDao;
    private CoinService coinService;

    //@CacheEvict(cacheNames = "memberAddress", key = "'entity:memberAddress:all-'+#memberId+'-'+#address")
    public MessageResult addMemberAddress(Long memberId, String address, String unit, String remark) {
        //Coin coin = coinDao.findByUnit(unit);
        Coin coin = coinService.findByUnit(unit); //edit by yangch 时间： 2018.07.11 原因：调用service层，缓存中有数据可以直接从缓存中获取数据
        if (coin == null || coin.getCanWithdraw().equals(BooleanEnum.IS_FALSE)) {
            return MessageResult.error(600, "The currency does not support withdrawals");
        }
        MemberAddress memberAddress = new MemberAddress();
        memberAddress.setAddress(address);
        memberAddress.setCoin(coin);
        memberAddress.setMemberId(memberId);
        memberAddress.setRemark(remark);
        MemberAddress memberAddress1=memberAddressDao.saveAndFlush(memberAddress);
        if (memberAddress1!=null){
            return MessageResult.success();
        }else {
            return MessageResult.error( "failed");
        }
    }

    //@CacheEvict(cacheNames = "memberAddress", allEntries = true)  todo 待完善。。
    public MessageResult deleteMemberAddress(Long memberId,Long addressId){
        int is=memberAddressDao.deleteMemberAddress(new Date(), addressId, memberId);
        if (is>0){
            return MessageResult.success();
        }else {
            return MessageResult.error("failed");
        }
    }

    public Page<MemberAddress> pageQuery(int pageNo, Integer pageSize, long id,String unit) {
//        Sort orders = Criteria.sortStatic("id.as");
        PageRequest pageRequest = new PageRequest(pageNo, pageSize);
        Criteria<MemberAddress> specification = new Criteria<>();
        specification.add(Restrictions.eq("memberId",id,false));
        specification.add(Restrictions.eq("status", CommonStatus.NORMAL, false));
        specification.add(Restrictions.eq("coin.unit",unit,false));
        return memberAddressDao.findAll(specification, pageRequest);
    }

    //@Cacheable(cacheNames = "memberAddress", key = "'entity:memberAddress:all-'+#userId+'-'+#coinId")
    public List<Map<String,String>> queryAddress(long userId,String coinId)  {
        try {
            return new Model("member_address")
                    .field(" remark,address")
                    .where("member_id=? and coin_id=? and status=?", userId, coinId, CommonStatus.NORMAL.ordinal())
                    .select();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    //@Cacheable(cacheNames = "memberAddress", key = "'entity:memberAddress:all-'+#userId+'-'+#address")
    public List<MemberAddress> findByMemberIdAndAddress(long userId,String address){
        return memberAddressDao.findAllByMemberIdAndAddressAndStatus(userId,address,CommonStatus.NORMAL);
    }

    /**
     * 提币地址查重
     * @author shenzucai
     * @time 2018.11.12 18:02
     * @param userId
     * @param coin
     * @param address
     * @return true
     */
    public MemberAddress findByMemberIdAndAddressAndCoinAndStatus(long userId,Coin coin,String address){
        return memberAddressDao.findByMemberIdAndAddressAndCoinAndStatus(userId,address,coin,CommonStatus.NORMAL);
    }
}
