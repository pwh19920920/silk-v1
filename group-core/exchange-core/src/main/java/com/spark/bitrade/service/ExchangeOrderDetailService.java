package com.spark.bitrade.service;

import com.spark.bitrade.dao.ExchangeOrderDetailRepository;
import com.spark.bitrade.entity.ExchangeOrderDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ExchangeOrderDetailService {
    @Autowired
    private ExchangeOrderDetailRepository orderDetailRepository;

//    @Autowired
//    private ExchangeOrderRepository exchangeOrderRepository ;
//
//    @Autowired
//    private MemberService memberService ;
//
//    @Autowired
//    private OrderDetailAggregationRepository orderDetailAggregationRepository;


    /**
     * 查询某订单的成交详情
     * @param orderId
     * @return
     */
    //@Cacheable(cacheNames = "exchangeOrderDetail", key = "'entity:exchangeOrderDtlLst:'+#orderId") //会导致成交金额不对
    public List<ExchangeOrderDetail> findAllByOrderId(String orderId){
        return orderDetailRepository.findAllByOrderId(orderId);
    }

    /***
     * 历史订单明细查询
     * @author yangch
     * @time 2018.06.24 1:42 
     * @param orderId
     */
    @Cacheable(cacheNames = "exchangeOrderDetail", key = "'entity:exchangeOrderDtlLst:'+#orderId")
    public List<ExchangeOrderDetail> findHistoryAllByOrderId(String orderId){
        return orderDetailRepository.findAllByOrderId(orderId);
    }

    /***
     * 判断指定的成交详情是否存在
      *
     * @author yangch
     * @time 2018.06.05 10:37 
     * @param orderId 订单号
     * @param refOrderId 关联订单号
     */
    //@Cacheable(cacheNames = "exchangeOrderDetail", key = "'entity:exchangeOrderDtl:'+#orderId+'-'+#refOrderId")
    public boolean existsByOrderIdAndRefOrderId(String orderId, String refOrderId){
        //if(orderDetailRepository.countExchangeOrderDetailByOrderIdAndRefOrderId(orderId, refOrderId)>0){
        if(orderDetailRepository.findFirstByOrderIdAndRefOrderId(orderId, refOrderId)!=null){
            return true;
        } else {
            return  false;
        }
    }

    /***
      * 删除指定的成交详情
     *
      * @author yangch
      * @time 2018.06.09 10:37 
     * @param orderId 订单号
     * @param refOrderId 关联订单号
     */
    //@CacheEvict(cacheNames = "exchangeOrderDetail", key = "'entity:exchangeOrderDtl:'+#orderId+'-'+#refOrderId")
    public boolean deleteByOrderIdAndRefOrderId(String orderId, String refOrderId){
        if(orderDetailRepository.deleteExchangeOrderDetailByOrderIdAndRefOrderId(orderId, refOrderId)>0){
            return true;
        } else {
            return  false;
        }
    }

    /***
     * 保存
     * @author yangch
     * @time 2018.06.09 10:06 
     * @param entity
     */
    //@Transactional(rollbackFor = Exception.class) 不支持事物
    @CacheEvict(cacheNames = "exchangeOrderDetail", key = "'entity:exchangeOrderDtlLst:'+#entity.orderId") //删除某个订单的成交明细（目前配合findAllByOrderId接口会导致成交数量不对？）
    public ExchangeOrderDetail save(ExchangeOrderDetail entity){
       return orderDetailRepository.save(entity);
    }

    /*public void add(){
        //Pageable pageable = new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, "time"));
        List<ExchangeOrderDetail> list = orderDetailRepository.findAll() ;
        for(ExchangeOrderDetail exchangeOrderDetail : list){
            ExchangeOrder order =  exchangeOrderRepository.findOne(exchangeOrderDetail.getOrderId());
            ExchangeOrderDetailAggregation dto = new ExchangeOrderDetailAggregation();
            if(order!=null){
                BeanUtils.copyProperties(exchangeOrderDetail,dto);
                BeanUtils.copyProperties(order,dto);
                dto.setRestOrderTime(order.getTime());
                dto.setTime(Calendar.getInstance().getTimeInMillis());
                Member member = memberService.findOne(dto.getMemberId());
                if(member!=null)
                    BeanUtils.copyProperties(member,dto);
                System.out.println(exchangeOrderDetailAggregationRepository.save(dto));
            }

        }
    }*/
}
