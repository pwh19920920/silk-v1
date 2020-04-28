package com.spark.bitrade.service;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.annotation.WriteDataSource;
import com.spark.bitrade.config.AliyunConfig;
import com.spark.bitrade.constant.OrderStatus;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.dao.AppealDao;
import com.spark.bitrade.dao.MemberDao;
import com.spark.bitrade.dao.OtcApiAppealDao;
import com.spark.bitrade.dto.AppealDTO;
import com.spark.bitrade.entity.OtcApiAppeal;
import com.spark.bitrade.dto.OtcApiOrderDto;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.Order;
import com.spark.bitrade.exception.UnexpectedException;
import com.spark.bitrade.mapper.dao.AppealMapper;
import com.spark.bitrade.pagination.PageResult;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.AliyunUtil;
import com.spark.bitrade.util.SpringContextUtil;
import com.spark.bitrade.vo.AppealDetailVO;
import com.spark.bitrade.vo.AppealVO;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * @author Zhang Jinwei
 * @date 2018年01月23日
 */
@Service
public class AppealService extends BaseService {
    @Autowired
    private AppealDao appealDao;

    @Autowired
    private MemberDao memberDao;

    @Resource
    private AppealMapper appealMapper;

    @Autowired
    private AliyunConfig aliyunConfig;

    @Autowired
    private OtcOrderService otcOrderService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private OrderAppealAccessoryService orderAppealAccessoryService;

    /**
     * 通过订单id查询申诉信息
     * @author tansitao
     * @time 2018/9/4 17:26 
     */
    @Deprecated
    public Appeal findByOrderId(String orderId){
        return appealMapper.findByorderId(orderId);
    }
    /**
     * 通过订单id查询最新的申诉信息
     * @author Zhang Yanjun
     * @time 2018.12.18 9:16
     * @param orderId
     */
    public Appeal findNewByorderId(String orderId){
        return appealMapper.findNewByorderId(orderId);
    }

    @WriteDataSource
    public Appeal findOne(Long id) {
        Appeal appeal = appealDao.findOne(id);
        return appeal;
    }

    @WriteDataSource
    public OtcApiAppeal findOneOtcApiAppeal(Long id) {
        OtcApiAppeal appeal = otcApiAppealDao.findOne(id);
        return appeal;
    }

    public AppealVO findOneAppealVO(long id) {
        return generateAppealVO(findOne(id));
    }

    /**
     * 根据申诉id获取对应的申诉截图
     * @author fumy
     * @time 2018.10.10 11:48
     * @param id
     * @return true
     */
    @ReadDataSource
    public List<OrderAppealAccessory> findAppealImgById(Long id){
        return appealMapper.getAppealImg(id);
    }

    public Appeal save(Appeal appeal) {
        return appealDao.save(appeal);
    }

    /**
     * 条件查询对象 (pageNo pageSize 同时传时分页)
     *
     * @param booleanExpressionList
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<AppealVO> joinFind(List<BooleanExpression> booleanExpressionList, PageModel pageModel) {
        QAppeal qAppeal = QAppeal.appeal ;
        QBean qBean = Projections.fields(AppealVO.class
                ,qAppeal.id.as("appealId")
                ,qAppeal.order.memberName.as("advertiseCreaterUserName")
                ,qAppeal.order.memberRealName.as("advertiseCreaterName")
                ,qAppeal.order.customerName.as("customerUserName")
                ,qAppeal.order.customerRealName.as("customerName")
                ,qAppeal.initiatorId==qAppeal.order.memberId?qAppeal.order.memberName.as("initiatorUsername"):qAppeal.order.customerName.as("initiatorUsername")
                ,qAppeal.initiatorId==qAppeal.order.memberId?qAppeal.order.memberRealName.as("initiatorName"):qAppeal.order.customerRealName.as("initiatorName")
                ,qAppeal.initiatorId==qAppeal.order.memberId?qAppeal.order.customerName.as("associateUsername"):qAppeal.order.memberName.as("associateUsername")
                ,qAppeal.initiatorId==qAppeal.order.memberId?qAppeal.order.customerRealName.as("associateName"):qAppeal.order.memberRealName.as("associateName")
                ,qAppeal.order.commission.as("fee")
                ,qAppeal.order.number
                ,qAppeal.order.money
                ,qAppeal.order.orderSn.as("orderSn")
                ,qAppeal.order.createTime.as("transactionTime")
                ,qAppeal.createTime.as("createTime")
                ,qAppeal.dealWithTime.as("dealTime")
                ,qAppeal.order.payMode.as("payMode")
                ,qAppeal.order.coin.name.as("coinName")
                ,qAppeal.order.status.as("orderStatus")
                ,qAppeal.isSuccess.as("isSuccess")
                ,qAppeal.order.advertiseType.as("advertiseType")
                ,qAppeal.status
                ,qAppeal.remark
        );
        List<OrderSpecifier> orderSpecifiers = pageModel.getOrderSpecifiers();
        JPAQuery<AppealVO> jpaQuery = queryFactory.select(qBean);
        jpaQuery.from(qAppeal);
        if (booleanExpressionList != null) {
            jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));
        }
        jpaQuery.orderBy(orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]));

        List<AppealVO> list = jpaQuery.offset((pageModel.getPageNo() - 1) * pageModel.getPageSize())
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]))
                .limit(pageModel.getPageSize()).fetch();
        return new PageResult<>(list, jpaQuery.fetchCount());
    }

    /**
     * 申诉详情
     * @param appeal
     * @return
     */
    private AppealVO generateAppealVO(Appeal appeal){
        Member initialMember = memberDao.findOne(appeal.getInitiatorId());
        Member associateMember = memberDao.findOne(appeal.getAssociateId());
        AppealVO vo = new AppealVO();
        vo.setAppealId(BigInteger.valueOf(appeal.getId()));
        vo.setAssociateName(associateMember.getRealName());
        vo.setAssociateUsername(associateMember.getUsername());
        vo.setInitiatorName(initialMember.getRealName());
        vo.setInitiatorUsername(initialMember.getUsername());
        Order order = appeal.getOrder() ;
        vo.setCoinName(order.getCoin().getName());
        vo.setFee(order.getCommission());
        vo.setMoney(order.getMoney());
        vo.setOrderSn(order.getOrderSn());
        vo.setNumber(order.getNumber());
        vo.setOrderStatus(order.getStatus().getOrdinal());
        vo.setPayMode(order.getPayMode());
        vo.setTransactionTime(order.getCreateTime());
        vo.setIsSuccess(appeal.getIsSuccess().getOrdinal());
        vo.setAdvertiseType(order.getAdvertiseType().getOrdinal());
        vo.setAdvertiseCreaterName(order.getMemberRealName());
        vo.setAdvertiseCreaterUserName(order.getMemberName());
        vo.setCustomerUserName(order.getCustomerName());
        vo.setCustomerName(order.getCustomerRealName());
        vo.setStatus(appeal.getStatus().getOrdinal());
        vo.setCreateTime(appeal.getCreateTime());
        vo.setDealTime(appeal.getDealWithTime());
        vo.setRemark(appeal.getRemark());
        return vo ;
    }

    /**
     * 申诉详情
     * @author Zhang Yanjun
     * @time 2018.11.10 16:25
     * @param appealId
     */
    @ReadDataSource
    public AppealDetailVO findOneDetail(Long appealId){
        return appealMapper.findOne(appealId);
    }

    /**
     * 申诉历史分页
     * @author Zhang Yanjun
     * @time 2018.11.28 15:55
     * @param orderSn
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ReadDataSource
    public PageInfo<AppealDetailVO> findAppealHistory(String orderSn, int pageNo, int pageSize) throws Exception {
        Page<AppealDetailVO> page= PageHelper.startPage(pageNo,pageSize);
        List<AppealDetailVO> list=this.appealMapper.findAppealHistoty(orderSn);

        for (int i=0;i<list.size();i++){
            List<OrderAppealAccessory> list2 = this.findAppealImgById(list.get(i).getAppealId().longValue());
            for(int j=0;j<list2.size();j++){
                String uri = AliyunUtil.getPrivateUrl(aliyunConfig, list2.get(j).getUrlPath());
                list2.get(j).setUrlPath(uri);
            }
            list.get(i).setList(list2);
        }
        return page.toPageInfo();
    }

    public org.springframework.data.domain.Page<Appeal> findAll(Predicate predicate, Pageable pageable) {
        return appealDao.findAll(predicate, pageable);
    }

    /**
     * 后台申诉导出
     * @author Zhang Yanjun
     * @time 2018.09.06 17:23
     * @param map
    */
    @ReadDataSource
    public List<AppealDTO> findByAppealAllForOut(Map<String,Object> map){
        List<AppealDTO> list=appealMapper.findAllBy(map);
        for (int i=0;i<list.size();i++){
            //处理结果
            if (list.get(i).getIsSuccess()==null){
                list.get(i).setResult("未处理");
            }else if (list.get(i).getIsSuccess().getNameCn()=="是"){
                list.get(i).setResult("申诉成功");
            }else {
                list.get(i).setResult("申诉失败");
            }
            //订单状态
            list.get(i).setOs(list.get(i).getOrderStatus().getCnName());
            //广告类型
            list.get(i).setAt(list.get(i).getAdvertiseType().getCnName());
            //申诉时间
            String createTime=list.get(i).getCreateTime();
            list.get(i).setCreateTime(createTime.substring(0,createTime.length()-2));
        }
        return list;
    }

    /**
     * 处理订单申诉
     * @author tansitao
     * @time 2018/12/21 11:49 
     */
    @Transactional(rollbackFor = Exception.class)
    public Appeal dealOrderApplea(Appeal appeal, Order order, String materialUrlStrs) throws Exception{
        if (!(otcOrderService.updateOrderAppeal(order.getOrderSn()) > 0)) {
            throw new UnexpectedException(String.format("OTCSS001：%s", msService.getMessage("APPEAL_FAILED")));
        }
        Appeal appeal1 = getService().save(appeal);
        if(appeal1!=null){
            saveMaterialUrl(appeal1.getId(),materialUrlStrs,0);
        }
        return  appeal1;
    }

    private void saveMaterialUrl(Long appealId,String materialUrlStrs,int isOtcApi){
        if (appealId != null) {
            //add by tansitao 时间： 2018/9/6 原因：保存申诉材料
            if (!StringUtils.isEmpty(materialUrlStrs)) {
                String[] materialUrls = materialUrlStrs.split(",");
                for (String materialUrl : materialUrls) {
                    OrderAppealAccessory orderAppealAccessory = new OrderAppealAccessory();
                    if(isOtcApi==0){
                        orderAppealAccessory.setAppealId(appealId);
                    }
                    if(isOtcApi==1){
                        orderAppealAccessory.setOtcApiAppealId(appealId);
                    }
                    //截取url，不需要保存详细参数
                    orderAppealAccessory.setUrlPath(materialUrl.split("[?]")[0].split("[|/]", 4)[3]);
                    orderAppealAccessoryService.save(orderAppealAccessory);
                }
            }
        }
    }


    public AppealService getService(){
        return SpringContextUtil.getBean(AppealService.class);
    }

    @Autowired
    private OtcApiAppealDao otcApiAppealDao;

    /**
     * otcapiorder申诉
     * @param appeal
     * @param orderDto
     * @param materialUrls
     */
    @Transactional(rollbackFor = Exception.class)
    public OtcApiAppeal doOtcApiAppeal(OtcApiAppeal appeal, OtcApiOrderDto orderDto, String materialUrls){
        //更新两种订单的状态
        //更新otcapiorder
        int i=otcOrderService.updateOtcApiOrder(OrderStatus.APPEAL.getOrdinal(),orderDto.getId(),OrderStatus.PAID.getOrdinal());
        Assert.isTrue(i>0,String.format("OTCSS001：%s", msService.getMessage("UPDATE_OTC_API_ORDER_FAIL")));
        //更新老的otcorder
        if(orderDto.getRefOtcOrderId()!=null){
            int i1 = otcOrderService.updateOldOtcOrder(OrderStatus.APPEAL.getOrdinal(), orderDto.getRefOtcOrderId(), OrderStatus.PAID.getOrdinal());
            Assert.isTrue(i1>0,String.format("OTCSS001：%s", msService.getMessage("UPDATE_OLD_ORDER_FAIL")));
        }
        //保存申诉
        OtcApiAppeal i2 = otcApiAppealDao.save(appeal);

        Assert.notNull(i2,msService.getMessage("APPEAL_FAILED"));
        saveMaterialUrl(i2.getId(),materialUrls,1);
        return i2;
    }
    @Transactional(rollbackFor = Exception.class)
    public void doOtcApiCancelAppeal(OtcApiAppeal appeal){
        //更新申诉
        otcApiAppealDao.save(appeal);
        OtcApiOrderDto order = otcOrderService.findOtcApiOrderByOrderId(appeal.getOtcApiOrderId());
        if(order.getRefOtcOrderId()!=null){
            int i1 = otcOrderService.updateOldOtcOrder(OrderStatus.PAID.getOrdinal(), order.getRefOtcOrderId(), OrderStatus.APPEAL.getOrdinal());
            Assert.isTrue(i1>0,msService.getMessage("UPDATE_OLD_ORDER_FAIL"));
        }
        //更新原有订单 为已付款
        //更新otcApiOrder 为已付款
        int i=otcOrderService.updateOtcApiOrder(OrderStatus.PAID.getOrdinal(),order.getId(),OrderStatus.APPEAL.getOrdinal());
        Assert.isTrue(i>0,msService.getMessage("UPDATE_OTC_API_ORDER_FAIL"));
    }

    public OtcApiAppeal findOtcAppealByOtcApiOrderId(Long otcApiOrderId) {
        List<OtcApiAppeal> appeals = otcApiAppealDao.findByOtcApiOrderIdOrderByCreateTimeDesc(otcApiOrderId);
        if(!CollectionUtils.isEmpty(appeals)){
            return appeals.get(0);
        }
        return null;
    }
}
