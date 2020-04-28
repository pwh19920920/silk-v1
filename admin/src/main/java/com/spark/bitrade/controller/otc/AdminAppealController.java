package com.spark.bitrade.controller.otc;

import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.annotation.CollectActionEvent;
import com.spark.bitrade.config.AliyunConfig;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.dto.AppealDTO;
import com.spark.bitrade.model.AppealDealWithScreen;
import com.spark.bitrade.model.screen.AppealScreen;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.event.OrderEvent;
import com.spark.bitrade.exception.InformationExpiredException;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.AppealDetailVO;
import com.spark.bitrade.vo.AppealVO;
import io.swagger.annotations.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static com.spark.bitrade.util.BigDecimalUtils.add;
import static com.spark.bitrade.util.BigDecimalUtils.sub;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * @author rongyu
 * @description 后台申诉管理
 * @date 2018/1/23 9:26
 */
@Api(description = "申诉管理",tags={"申诉管理接口操作"})
@RestController
@RequestMapping("/otc/appeal")
public class AdminAppealController extends BaseAdminController {
    private static Logger log = LoggerFactory.getLogger(AdminAppealController.class);
    @Autowired
    private AppealService appealService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AdvertiseService advertiseService;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private OrderEvent orderEvent;

    @Autowired
    private MemberTransactionService memberTransactionService;

    @Autowired
    private AliyunConfig aliyunConfig;

    @Autowired
    @Qualifier(value = "chatMongoTemplate")
    private MongoTemplate mongoTemplate;

    @Autowired
    private AdminOrderAppealSuccessAccessoryService adminOrderAppealSuccessAccessoryService;

    @Autowired
    private OtcOrderService otcOrderService;


    @ApiOperation(value = "申诉管理分页",notes = "申诉订单分页数据列表")
    @RequiresPermissions("otc:appeal-page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.OTC, operation = "分页查找后台申诉Appeal")
    public MessageRespResult<AppealVO> pageQuery(
            PageModel pageModel,
            AppealScreen screen) {
        //add by yangch 时间： 2018.04.29 原因：合并
        StringBuilder headSqlBuilder = new StringBuilder("select a.id appealId,")
                //edit by tansitao 时间： 2018/6/30 原因：增加价格查询
                .append("b.member_name advertiseCreaterUserName,b.member_real_name advertiseCreaterName,b.price,")
                .append("b.customer_name customerUserName,b.customer_real_name customerName,")
                .append("c.real_name initiatorName,")
//                    .append("c.username initiatorUsername,c.real_name initiatorName,")
//                    .append("d.username associateUsername,d.real_name associateName,")
                //edit by yangch 时间： 2018.05.04 原因：解决js对大整型进行四舍五入的bug
                .append("b.commission fee,b.number,b.money,CONCAT(b.order_sn,'') orderSn,")
                .append("a.create_time createTime,a.deal_with_time dealWithTime,b.pay_mode payMode, e.name coinName,")
                //add by zyj 2018.10.26 新增订单创建时间字段查询
                .append("b.create_time orderCreateTime,")
                .append("a.is_success isSuccess,b.advertise_type advertiseType,a.status,b.status orderStatus ");
//                    .append("b.status orderStatus,a.is_success isSuccess,b.advertise_type advertiseType,a.status,a.remark,")
//                    .append("c.mobile_phone customerPhone,c.email customerEmail,d.mobile_phone associatePhone,d.email associateEmail ");

        StringBuilder countHead = new StringBuilder("select count(*) ");

        //add|edit|del by tansitao 时间： 2018/6/10 原因：取消只查订单状态为0的数据
//        StringBuilder endSql = new StringBuilder("from appeal a,otc_order b,member c,member d,otc_coin e")
        StringBuilder endSql = new StringBuilder("from (select a.* from (SELECT * FROM appeal ORDER BY create_time DESC) a group by a.order_id) a," +
                                                 "otc_order b,member c,member d,otc_coin e")
                .append(" where a.order_id = b.id and a.initiator_id = c.id and a.associate_id = d.id ")
                .append(" and b.coin_id = e.id ");
            //被申诉人
            if (!StringUtils.isEmpty(screen.getNegotiant())){
                endSql.append(" and (d.username like '%" + screen.getNegotiant() + "%'")
                        .append(" or d.real_name like '%" + screen.getNegotiant() + "%')");
            }
            //申诉人
            if (!StringUtils.isEmpty(screen.getComplainant())){
                endSql.append(" and (c.username like '%" + screen.getComplainant() + "%'")
                        .append(" or c.real_name like '%" + screen.getComplainant() + "%')");
            }
            //买入、卖出
            if (screen.getAdvertiseType() != null) {
                endSql.append(" and b.advertise_type = " + screen.getAdvertiseType().getOrdinal() + " ");
            }
            //是否胜诉
            if(screen.getSuccess() != null) {
                endSql.append(" and (a.is_success = " + screen.getSuccess().getOrdinal() + " and a.deal_with_time is not null) ");
            }
            //add|edit|del by tansitao 时间： 2018/6/10 原因：添加订单状态条件
            if(screen.getStatus() != null){
                endSql.append(" and b.status = "+screen.getStatus().getOrdinal()+" ");
            }
            if(!StringUtils.isEmpty(screen.getUnit())) {
                endSql.append(" and lower(e.unit) = '" + screen.getUnit().toLowerCase() + "'");
            }
            //add by zyj :增加筛选条件
            //订单创建时间
            if (!StringUtils.isEmpty(screen.getOrderCreateStartTime())) {
                endSql.append("and b.create_time >= '" + screen.getOrderCreateStartTime() + "'");
            }
            if (!StringUtils.isEmpty(screen.getOrderCreateEndTime())) {
                endSql.append("and b.create_time <='" + screen.getOrderCreateEndTime() + "'");
            }
            //订单申诉时间
            if (!StringUtils.isEmpty(screen.getAppealCreateStartTime())) {
                endSql.append("and a.create_time >='" + screen.getAppealCreateStartTime() + "'");
            }
            if (!StringUtils.isEmpty(screen.getAppealCreateEndTime())) {
                endSql.append("and a.create_time <='" + screen.getAppealCreateEndTime() + "'");
            }

            Page<AppealVO> page = appealService.createNativePageQuery(countHead.append(endSql),headSqlBuilder.append(endSql),pageModel, Transformers.ALIAS_TO_ENTITY_MAP);

            return MessageRespResult.success("获取成功",page);

        //del by yangch 时间： 2018.04.29 原因：合并
        /*ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        booleanExpressions.add(QAppeal.appeal.order.status.ne(OrderStatus.CANCELLED));
        if (screen.getAdvertiseType() != null)
            booleanExpressions.add(QAppeal.appeal.order.advertiseType.eq(screen.getAdvertiseType()));

        if (screen.getNegotiant() != null)
            booleanExpressions.add(
                    QAppeal.appeal.initiatorId==QAppeal.appeal.order.memberId?
                    QAppeal.appeal.order.customerName.like("%"+screen.getNegotiant()+"%")
                            .or(QAppeal.appeal.order.customerRealName.like("%"+screen.getNegotiant()+"%")):
                    QAppeal.appeal.order.memberName.like("%"+screen.getNegotiant()+"%")
                            .or(QAppeal.appeal.order.memberRealName.like("%"+screen.getNegotiant()+"%")));

        if (screen.getComplainant() != null)
            booleanExpressions.add(
                    QAppeal.appeal.initiatorId==QAppeal.appeal.order.memberId?
                    QAppeal.appeal.order.memberName.like("%"+screen.getComplainant()+"%")
                            .or(QAppeal.appeal.order.memberRealName.like("%"+screen.getComplainant()+"%")):
                    QAppeal.appeal.order.customerName.like("%"+screen.getComplainant()+"%")
                            .or(QAppeal.appeal.order.customerRealName.like("%"+screen.getComplainant()+"%")));

        if(screen.getSuccess() != null)
            booleanExpressions.add(QAppeal.appeal.isSuccess.eq(screen.getSuccess()));

        if(screen.getUnit()!=null)
            booleanExpressions.add(QAppeal.appeal.order.coin.unit.equalsIgnoreCase(screen.getUnit()));

        PageResult<AppealVO> all = appealService.joinFind(booleanExpressions, pageModel);
        return success(all);*/
    }

    @ApiOperation(value = "申诉详情",notes = "申诉详情")
    @RequiresPermissions("otc:appeal-detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.OTC, operation = "后台申诉Appeal详情")
    @ApiImplicitParam(value = "申诉id",name = "id",dataType = "String",paramType = "query")
    public MessageRespResult<AppealDetailVO> detail(
            @RequestParam(value = "id") Long id) {
        try {
//            AppealVO one = appealService.findOneAppealVO(id);
            AppealDetailVO one=appealService.findOneDetail(id);
            //0商家买入 1商家卖出
            if (one.getAdvertiseType()==0){
                one.setAdvertiseCreaterRole("买币方");
                one.setCustomerRole("卖币方");
            }else {
                one.setAdvertiseCreaterRole("卖币方");
                one.setCustomerRole("买币方");
            }
            //申诉方
            if (one.getInitiatorId().equals(one.getAdvertiseCreaterId())){
                one.setAdvertiseCreaterRole(one.getAdvertiseCreaterRole()+"、申诉方");
                if (one.getIsSuccess()!=null) {
                    //胜诉
                    if (one.getIsSuccess() == BooleanEnum.IS_TRUE) {
                        one.setAdvertiseCreaterRole(one.getAdvertiseCreaterRole() + "、胜方");
                    } else {
                        one.setCustomerRole(one.getCustomerRole() + "、胜方");
                    }
                }
            }else if (one.getInitiatorId().equals(one.getCustomerId())){
                one.setCustomerRole(one.getCustomerRole()+"、申诉方");
                if (one.getIsSuccess()!=null) {
                    //胜诉
                    if (one.getIsSuccess() == BooleanEnum.IS_TRUE) {
                        one.setCustomerRole(one.getCustomerRole() + "、胜方");
                    } else {
                        one.setAdvertiseCreaterRole(one.getAdvertiseCreaterRole() + "、胜方");
                    }
                }
            }
            //申诉取消方为广告创建者
            if (one.getCancelId()!=null) {
                if (one.getCancelId().equals(one.getAdvertiseCreaterId())){
                    one.setAdvertiseCreaterRole(one.getAdvertiseCreaterRole() + "、申诉取消方");
                }else{//申诉取消方为交易者
                    one.setCustomerRole(one.getCustomerRole() + "、申诉取消方");
                }
            }
            List<OrderAppealAccessory> list = appealService.findAppealImgById(id);
            for(int i=0;i<list.size();i++){
                String uri = AliyunUtil.getPrivateUrl(aliyunConfig, list.get(i).getUrlPath());
                list.get(i).setUrlPath(uri);
            }

            one.setList(list);
            return MessageRespResult.success("获取成功",one);
        }catch (Exception e){
            return MessageRespResult.error(e.getMessage() +" error **********************");
        }
//        if (one == null)
//            return error("Data is empty!You should check parameter (id)!");
    }

    /**
     * 申诉历史
     * @author Zhang Yanjun
     * @time 2018.11.28 15:58
     * @param orderSn
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "申诉历史",notes = "申诉历史")
//    @RequiresPermissions("otc:appeal-detail")
    @PostMapping("history")
    @AccessLog(module = AdminModule.OTC, operation = "后台申诉Appeal历史")
    public MessageRespResult appealHistory(String orderSn, int pageNo, int pageSize) throws Exception {
        PageInfo<AppealDetailVO> pageInfo=appealService.findAppealHistory(orderSn, pageNo, pageSize);
        return MessageRespResult.success("success",PageData.toPageData(pageInfo));
    }

    //查询断言
    private List<BooleanExpression> getBooleanExpressionList(AppealStatus status, OrderStatus orderStatus) {
        List<BooleanExpression> booleanExpressionList = new ArrayList();
        QAppeal qAppeal = QAppeal.appeal;
        if (status != null){
            booleanExpressionList.add(qAppeal.status.eq(status));
        }
        if (orderStatus != null) {
            booleanExpressionList.add(qAppeal.order.status.eq(orderStatus));
        }
        return booleanExpressionList;
    }

    /**
     * 申诉已处理  取消订单
     *
     * @param orderSn
     * @return
     * @throws InformationExpiredException
     */
    @RequiresPermissions("otc:appeal:cancel-order")
    @RequestMapping(value = "cancel-order")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult cancelOrder(long appealId, String orderSn, @RequestParam(value = "banned", defaultValue = "false") boolean banned) throws InformationExpiredException {
        Appeal appeal = appealService.findOne(appealId);
        Assert.notNull(appeal, "申诉单不存在");
        Long initiatorId = appeal.getInitiatorId();
        Long associateId = appeal.getAssociateId();
        Order order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        int ret = getRet(order, initiatorId, associateId);
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));
        isTrue(order.getStatus().equals(OrderStatus.NONPAYMENT) || order.getStatus().equals(OrderStatus.PAID) || order.getStatus().equals(OrderStatus.APPEAL), msService.getMessage("ORDER_NOT_ALLOW_CANCEL"));
        //取消订单
        if (!(orderService.cancelOrder(order.getOrderSn()) > 0)) {
            throw new InformationExpiredException("Information Expired");
        }
        MessageResult result = success("");
        if (ret == 1) {
            //banned为true 禁用账户
            Member member1 = memberService.findOne(initiatorId); //edit by yangch 时间： 2018.04.26 原因：更改 TODO 待验证。。。
            if (member1.getStatus() == CommonStatus.NORMAL && banned) {
                member1.setStatus(CommonStatus.ILLEGAL);
                memberService.save(member1);
            }

            result = cancel(order, order.getNumber(), associateId);
        } else if (ret == 2) {
            Member member1 = memberService.findOne(initiatorId);//edit by yangch 时间： 2018.04.26 原因：更改
            if (member1.getStatus() == CommonStatus.NORMAL && banned) {
                member1.setStatus(CommonStatus.ILLEGAL);
                memberService.save(member1);
            }
            result = cancel(order, add(order.getNumber(), order.getCommission()), associateId);
        } else if (ret == 3) {
            Member member1 = memberService.findOne(associateId);//edit by yangch 时间： 2018.04.26 原因：更改
            if (member1.getStatus() == CommonStatus.NORMAL && banned) {
                member1.setStatus(CommonStatus.ILLEGAL);
                memberService.save(member1);
            }
            result = cancel(order, add(order.getNumber(), order.getCommission()), initiatorId);
        } else if (ret == 4) {
            Member member1 = memberService.findOne(associateId); //edit by yangch 时间： 2018.04.26 原因：更改
            if (member1.getStatus() == CommonStatus.NORMAL && banned) {
                member1.setStatus(CommonStatus.ILLEGAL);
                memberService.save(member1);
            }
            result = cancel(order, order.getNumber(), initiatorId);
        } else {
            throw new InformationExpiredException("Information Expired");
        }
        appeal.setDealWithTime(DateUtil.getCurrentDate());
        appeal.setIsSuccess(BooleanEnum.IS_FALSE);
        appeal.setStatus(AppealStatus.PROCESSED);
        appealService.save(appeal);
        return result;
    }


    private MessageResult cancel(Order order , BigDecimal amount , Long memberId)  throws InformationExpiredException{
        MemberWallet memberWallet  ;
        //更改广告
        if (!advertiseService.updateAdvertiseAmountForCancel(order.getAdvertiseId(), amount)) {
            throw new InformationExpiredException("Information Expired");
        }
        memberWallet = memberWalletService.findByOtcCoinAndMemberId(order.getCoin(), memberId);
        MessageResult result = memberWalletService.thawBalance(memberWallet,amount);
        if (result.getCode() == 0) {
            return MessageResult.success("取消订单成功");
        } else {
            throw new InformationExpiredException("Information Expired");
        }
    }

    /**
     * 取消订单（对广告、订单、钱包的修改）
     * @author Zhang Yanjun
     * @time 2018.11.12 14:28
     * @param order
     * @param initiatorId
     * @param associateId
     * @param banned
     */
    private MessageResult cancelOrder(int ret,Order order,Long initiatorId,Long associateId) throws InformationExpiredException {
        MessageResult result = success("");
        if (ret == 1) {
            result = cancel(order, order.getNumber(), associateId);
        } else if (ret == 2) {
            result = cancel(order, add(order.getNumber(), order.getCommission()), associateId);
        } else if (ret == 3) {
            result = cancel(order, add(order.getNumber(), order.getCommission()), initiatorId);
        } else if (ret == 4) {
            result = cancel(order, order.getNumber(), initiatorId);
        } else {
            throw new InformationExpiredException("Information Expired");
        }
        return result;
    }

    //关闭订单
    private MessageRespResult close(Order order , BigDecimal amount , Long memberId)  throws InformationExpiredException{
        MemberWallet memberWallet  ;
        //更改广告
        if (!advertiseService.updateAdvertiseAmountForClose(order.getAdvertiseId(), amount)) {
            throw new InformationExpiredException("Information Expired");
        }
        memberWallet = memberWalletService.findByOtcCoinAndMemberId(order.getCoin(), memberId);
        MessageResult result = memberWalletService.thawBalance(memberWallet,amount);
        if (result.getCode() == 0) {
            return MessageRespResult.success("关闭订单成功",result.getData());
        } else {
            throw new InformationExpiredException("Information Expired");
        }
    }

    private int getRet(Order order, Long initiatorId, Long associateId) {
        int ret = 0;
        if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getMemberId().equals(initiatorId)) {
            //代表该申诉者是广告发布者，并且是付款者   卖家associateId
            ret = 1;
        } else if (order.getAdvertiseType().equals(AdvertiseType.SELL) && order.getCustomerId().equals(initiatorId)) {
            //代表该申诉者不是广告发布者，但是是付款者   卖家associateId
            ret = 2;
        } else if (order.getAdvertiseType().equals(AdvertiseType.SELL) && order.getCustomerId().equals(associateId)) {
            //代表该申诉者是广告发布者，但不是付款者   卖家initiatorId
            ret = 3;
        } else if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getMemberId().equals(associateId)) {
            //代表该申诉者不是广告发布者，但不是付款者  卖家initiatorId
            ret = 4;
        }
        return ret;
    }


    /**
     * 申诉处理 订单放行（放币）
     *
     * @param orderSn
     * @return
     */
    @RequiresPermissions("otc:appeal:release-coin")
    @RequestMapping(value = "release-coin")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult confirmRelease(long appealId, String orderSn, @RequestParam(value = "banned", defaultValue = "false") boolean banned) throws Exception {
        Appeal appeal = appealService.findOne(appealId);
        Assert.notNull(appeal, "申诉单不存在");
        Long initiatorId = appeal.getInitiatorId();
        Long associateId = appeal.getAssociateId();
        // Assert.hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));
        Member member = memberService.findOne(initiatorId);
       /* String mbPassword = member.getJyPassword();
        Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(Md5.md5Digest(jyPassword + member.getSalt()).toLowerCase().equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));*/
        Order order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        int ret = getRet(order, initiatorId, associateId);
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));
        isTrue(order.getStatus().equals(OrderStatus.PAID) || order.getStatus().equals(OrderStatus.APPEAL), msService.getMessage("ORDER_STATUS_EXPIRED"));
        if (ret == 1 || ret == 4) {
            //更改广告
            if (!advertiseService.updateAdvertiseAmountForRelease(order.getAdvertiseId(), order.getNumber())) {
                throw new InformationExpiredException("Information Expired");
            }
        } else if ((ret == 2 || ret == 3)) {
            //更改广告
            if (!advertiseService.updateAdvertiseAmountForRelease(order.getAdvertiseId(), add(order.getNumber(), order.getCommission()))) {
                throw new InformationExpiredException("Information Expired");
            }
        } else {
            throw new InformationExpiredException("Information Expired");
        }
        //放行订单
        if (!(orderService.releaseOrder(order.getOrderSn()) > 0)) {
            throw new InformationExpiredException("Information Expired");
        }
        //后台处理申诉结果为放行---更改买卖双方钱包
        memberWalletService.transferAdmin(order, ret);

        if (ret == 1) {

            generateMemberTransaction(order, TransactionType.OTC_SELL, associateId, BigDecimal.ZERO);

            generateMemberTransaction(order, TransactionType.OTC_BUY, initiatorId, order.getCommission());

        } else if (ret == 2) {

            generateMemberTransaction(order, TransactionType.OTC_SELL, associateId, order.getCommission());

            generateMemberTransaction(order, TransactionType.OTC_BUY, initiatorId, BigDecimal.ZERO);

        } else if (ret == 3) {

            generateMemberTransaction(order, TransactionType.OTC_BUY, associateId, BigDecimal.ZERO);

            generateMemberTransaction(order, TransactionType.OTC_SELL, initiatorId, order.getCommission());
        } else {

            generateMemberTransaction(order, TransactionType.OTC_BUY, associateId, order.getCommission());

            generateMemberTransaction(order, TransactionType.OTC_SELL, initiatorId, BigDecimal.ZERO);
        }
//        orderEvent.onOrderCompleted(order);

        //add by tansitao 时间： 2018/5/22 原因：增加用户C2C交易次数
        Member merchantMember = memberService.findOne(order.getMemberId());
        merchantMember.setTransactions(merchantMember.getTransactions() + 1);
        Member userMember = memberService.findOne(order.getCustomerId());
        userMember.setTransactions(userMember.getTransactions() + 1);
        memberService.save(merchantMember);
        memberService.save(userMember);

        //banned为true 禁用账户
        if (ret == 1 || ret == 2) {
            Member member1 = memberService.findOne(associateId);
            if (member1.getStatus() == CommonStatus.NORMAL && banned) {
                member1.setStatus(CommonStatus.ILLEGAL);
                memberService.save(member1);
            }

        } else {
            Member member1 = memberService.findOne(initiatorId);
            if (member1.getStatus() == CommonStatus.NORMAL && banned) {
                member1.setStatus(CommonStatus.ILLEGAL);
                memberService.save(member1);
            }
        }
        appeal.setDealWithTime(DateUtil.getCurrentDate());
        appeal.setIsSuccess(BooleanEnum.IS_TRUE);
        appeal.setStatus(AppealStatus.PROCESSED);
        appealService.save(appeal);
        return MessageResult.success("放币成功");
    }

    /**
     * 订单放行（处理广告、订单、用户钱包、交易记录、c2c交易次数）
     * @author Zhang Yanjun
     * @time 2018.11.12 14:34
     * @param order
     * @param initiatorId
     * @param associateId
     * @param banned
     */
    private MessageResult releaseOrder(int ret,Order order,Long initiatorId,Long associateId) throws InformationExpiredException {
        if (ret == 1 || ret == 4) {
            //更改广告
            if (!advertiseService.updateAdvertiseAmountForRelease(order.getAdvertiseId(), order.getNumber())) {
                throw new InformationExpiredException("Information Expired");
            }
        } else if ((ret == 2 || ret == 3)) {
            //更改广告
            if (!advertiseService.updateAdvertiseAmountForRelease(order.getAdvertiseId(), add(order.getNumber(), order.getCommission()))) {
                throw new InformationExpiredException("Information Expired");
            }
        } else {
            throw new InformationExpiredException("Information Expired");
        }

        //修改订单放行时间
        if(!otcOrderService.updateReleaseTime(order.getOrderSn())){
            throw new InformationExpiredException("Information Expired");
        }

        //后台处理申诉结果为放行---更改买卖双方钱包
        memberWalletService.transferAdmin(order, ret);

        if (ret == 1) {

            generateMemberTransaction(order, TransactionType.OTC_SELL, associateId, BigDecimal.ZERO);

            generateMemberTransaction(order, TransactionType.OTC_BUY, initiatorId, order.getCommission());

        } else if (ret == 2) {

            generateMemberTransaction(order, TransactionType.OTC_SELL, associateId, order.getCommission());

            generateMemberTransaction(order, TransactionType.OTC_BUY, initiatorId, BigDecimal.ZERO);

        } else if (ret == 3) {

            generateMemberTransaction(order, TransactionType.OTC_BUY, associateId, BigDecimal.ZERO);

            generateMemberTransaction(order, TransactionType.OTC_SELL, initiatorId, order.getCommission());
        } else {

            generateMemberTransaction(order, TransactionType.OTC_BUY, associateId, order.getCommission());

            generateMemberTransaction(order, TransactionType.OTC_SELL, initiatorId, BigDecimal.ZERO);
        }

        //add by tansitao 时间： 2018/5/22 原因：增加用户C2C交易次数
        Member merchantMember = memberService.findOne(order.getMemberId());
        merchantMember.setTransactions(merchantMember.getTransactions() + 1);
        Member userMember = memberService.findOne(order.getCustomerId());
        userMember.setTransactions(userMember.getTransactions() + 1);
        memberService.save(merchantMember);
        memberService.save(userMember);

        return success();
    }


    private void generateMemberTransaction(Order order, TransactionType type, long memberId, BigDecimal fee) {

        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setSymbol(order.getCoin().getUnit());
        memberTransaction.setType(type);
        memberTransaction.setFee(fee);
        memberTransaction.setMemberId(memberId);
        memberTransaction.setAmount(order.getNumber());
        memberTransactionService.save(memberTransaction);

    }

    /**
     * 后台申诉导出
     * @author Zhang Yanjun
     * @time 2018.08.31 15:57
     * @param appealScreen
     * @param response
    */
    @RequiresPermissions("otc:appeal-out-excel")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.OTC, operation = "后台申诉导出")
    public void outExcel(AppealScreen appealScreen, HttpServletResponse response) throws IOException {
        Map<String,Object> map=new HashMap<>();
        map.put("advertiseType",appealScreen.getAdvertiseType()==null?null:appealScreen.getAdvertiseType().getOrdinal());
        map.put("complainant",appealScreen.getComplainant());
        map.put("negotiant",appealScreen.getNegotiant());
        map.put("success",appealScreen.getSuccess()==null?null:appealScreen.getSuccess().getOrdinal());
        map.put("unit",appealScreen.getUnit());
        map.put("status",appealScreen.getStatus()==null?null:appealScreen.getStatus().getOrdinal());
        //订单创建时间
        map.put("orderCreateStartTime",appealScreen.getOrderCreateStartTime());
        map.put("orderCreateEndTime",appealScreen.getOrderCreateEndTime());
        //订单申诉时间
        map.put("appealCreateStartTime",appealScreen.getAppealCreateStartTime());
        map.put("appealCreateEndTime",appealScreen.getAppealCreateEndTime());
        List<AppealDTO> list = appealService.findByAppealAllForOut(map);
        String fileName="appeal_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,AppealDTO.class.getDeclaredFields(),response,fileName);
//        ExcelUtil.listToExcel(list,AppealDTO.class.getDeclaredFields(),response.getOutputStream());
    }

    /**
     * 查询某个订单历史聊天记录
     * @author Zhang Yanjun
     * @time 2018.10.31 9:19
     * @param
     */
    @ApiOperation(value = "申诉管理历史聊天记录",notes = "查询某个订单历史聊天记录")
    @RequiresPermissions("otc:appeal-historyChatMessage")
    @PostMapping("historyChatMessage")
    @AccessLog(module = AdminModule.OTC, operation = "申诉管理历史聊天记录")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码 首页为1",name = "pageNo"),
            @ApiImplicitParam(value = "页大小",name = "pageSize"),
            @ApiImplicitParam(value = "订单id 如：113316545798737920",name = "orderId",type = "String")
    })
    public MessageRespResult<HistoryChatMessage> historyChatMessage(Integer pageNo,Integer pageSize,String orderId){
        Sort.Order order = new Sort.Order(Sort.Direction.ASC,"sendTime");
        Sort sort = new Sort(order);
        Query query = new Query();

        Criteria criteria = Criteria.where("orderId").is(orderId.trim());
        query.addCriteria(criteria);
        //查询总数
        long count = mongoTemplate.count(query,HistoryChatMessage.class,"chat_message");
        //分页查询mongo从0开始
        PageRequest page = new PageRequest(pageNo-1, pageSize,sort);

        query.with(page);
        List<HistoryChatMessage> result = mongoTemplate.find(query, HistoryChatMessage.class, "chat_message");

        PageInfo<HistoryChatMessage> pageInfo = new PageInfo<>();

        //计算分页数据
        pageInfo.setTotal(count);
        pageInfo.setList(result);
        pageInfo.setPageSize(pageSize);
        pageInfo.setPageNum(PageData.pageNo4PageHelper(pageNo));
        pageInfo.setPages( ((int)count + pageSize -1 )/pageSize) ;
        return MessageRespResult.success("查询成功",PageData.toPageData(pageInfo));
    }


    /**
     * 申诉处理
     * @author Zhang Yanjun
     * @time 2018.11.08 15:24
     * @param appealDealWithScreen
     * @param admin
     */
    @ApiOperation(value = "申诉处理",notes = "申诉处理操作")
    @RequiresPermissions("otc:appeal-dealWith")
    @PostMapping("dealWith")
    @AccessLog(module = AdminModule.OTC, operation = "申诉处理")
    @Transactional(rollbackFor = Exception.class)
    @CollectActionEvent(collectType = CollectActionEventType.OTC_APPEAL_ORDER, memberId = "#appealDealWithScreen.getFailId()" ,refId = "#appealDealWithScreen.getAppealId()") //add by tansitao 时间： 2018/11/6 原因：增加订单申诉告警监控注解
    public MessageRespResult dealWith(AppealDealWithScreen appealDealWithScreen,@SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) throws Exception {
        List<AdminOrderAppealSuccessAccessory> list=adminOrderAppealSuccessAccessoryService.findByAppealId(appealDealWithScreen.getAppealId());
        Appeal appeal=appealService.findOne(appealDealWithScreen.getAppealId());
        notNull(appeal,"没有该申诉单");
        if (appeal.getStatus()==AppealStatus.PROCESSED || appeal.getStatus()==AppealStatus.CANCELED){
            String remark;
            Long successId;
            if (appeal.getStatus()==AppealStatus.CANCELED){
                //申诉取消者
                successId=appeal.getCancelId();
                remark="已取消申诉";
            }else {
                //胜诉者
                successId = appeal.getIsSuccess() == BooleanEnum.IS_TRUE ? appeal.getInitiatorId() : appeal.getAssociateId();
                //胜诉缘由
                remark = appeal.getSuccessRemark();
                for (int i = 0; i < list.size(); i++) {
//                String uri = AliyunUtil.getPublicUrl(aliyunConfig,list.get(i).getUrlPath());
                    //改为私有库
                    String uri = AliyunUtil.getPrivateUrl(aliyunConfig, list.get(i).getUrlPath());
                    list.get(i).setUrlPath(uri);
                }
            }
            Map<String,Object> map=new HashMap<>();
            map.put("list",list);
            map.put("successId",successId);
            map.put("successRemark",remark);
            return MessageRespResult.success("查询成功",map);
        }
        //逻辑类同取消订单、放行订单
        Long initiatorId = appeal.getInitiatorId();//申诉者
        Long associateId = appeal.getAssociateId();//非申诉者
        Long successId = appealDealWithScreen.getSuccessId();//胜方id
        Long failId = appealDealWithScreen.getFailId();//败方id
        Order order = orderService.findOneByOrderSn(appealDealWithScreen.getOrderSn());
        notNull(order, "订单不存在");
        log.info("======订单关闭============");
        //订单关闭
        isTrue(order.getStatus().equals(OrderStatus.NONPAYMENT) || order.getStatus().equals(OrderStatus.PAID) || order.getStatus().equals(OrderStatus.APPEAL), "订单无法关闭");
        int row=orderService.closeOrder(order.getOrderSn());
        if (row ==0) {
            return  MessageRespResult.error("订单关闭失败");
        }
        int ret = getRet(order, initiatorId, associateId);
        isTrue(ret != 0, "请求不合法");
        //胜方为消费者
        if (successId.equals(order.getCustomerId())){
            //广告类型为卖出，订单放行逻辑
            if (AdvertiseType.SELL == order.getAdvertiseType()){
                releaseOrder(ret,order,initiatorId,associateId);
            }else {//商家买入，取消订单逻辑
                cancelOrder(ret,order,initiatorId,associateId);
            }
        }else {//胜方为广告主
            //广告类型为卖出，取消订单逻辑
            if (AdvertiseType.SELL == order.getAdvertiseType()){
                cancelOrder(ret,order,initiatorId,associateId);
            }else {//订单放行逻辑
                releaseOrder(ret,order,initiatorId,associateId);
            }
        }
//        //更改广告交易中数量
//        if (ret == 1 || ret == 4) {
//            //更改广告
//            log.info("======更改广告交易中数量====ret{}===",ret);
//            boolean isSuccess=advertiseService.updateAdvertiseAmountForClose(order.getAdvertiseId(), order.getNumber());
//            if (!isSuccess) {
//                return MessageRespResult.error("更改广告交易中数量失败");
//            }
//            //更改钱包
//            //卖币者
//            MemberWallet seller=memberWalletService.findByOtcCoinAndMemberId(order.getCoin(),order.getCustomerId());
//            //买币者
//            MemberWallet buyer=memberWalletService.findByOtcCoinAndMemberId(order.getCoin(),order.getMemberId());
//            log.info("======更改钱包===卖币者{}====买币者{}====",order.getCustomerId(),order.getMemberId());
//            log.info("======广告主id："+order.getMemberId()+"普通用户id："+order.getCustomerId()+"胜诉者id："+successId+"申诉者id："+initiatorId+"===========");
//            //返回去
//            if (failId.equals(order.getCustomerId()) || successId.equals(order.getMemberId())){
//                log.info("======解冻钱包============");
//                //解冻钱包
//                memberWalletService.thawBalance(seller,order.getNumber());
//            }else {//订单放行
//                log.info("======卖币者减少冻结余额============");
//                MessageResult result1=memberWalletService.decreaseFrozen(seller.getId(),order.getNumber());//卖币者减少冻结余额
//                String message=result1.getMessage();
//                if (result1.getCode()==500) {
//                    return MessageRespResult.error(message);
//                }
//                log.info("======买币者增加钱包余额============");
//                MessageResult result2=memberWalletService.increaseBalance(buyer.getId(),sub(order.getNumber() , order.getCommission()));//买币者增加钱包余额
//                String message2=result2.getMessage();
//                if (result1.getCode()==500) {
//                    return MessageRespResult.error(message2);
//                }
//                //会员交易记录
//                log.info("======更新会员交易记录============");
//                generateMemberTransaction(order, TransactionType.OTC_SELL, order.getCustomerId(), BigDecimal.ZERO);//用户
//                generateMemberTransaction(order, TransactionType.OTC_BUY, order.getMemberId(), order.getCommission());//广告主
//            }
//
//        } else if ((ret == 2 || ret == 3)) {
//            //更改广告
//            log.info("======更改广告交易中数量============ret:"+ret+"===========");
//            boolean isSuccess=advertiseService.updateAdvertiseAmountForClose(order.getAdvertiseId(), add(order.getNumber(), order.getCommission()));
//            if (!isSuccess) {
//                return MessageRespResult.error("更改广告交易中数量失败");
//            }
//            //更改钱包
//            MemberWallet buyer=memberWalletService.findByOtcCoinAndMemberId(order.getCoin(),order.getCustomerId());//买币者
//            MemberWallet seller=memberWalletService.findByOtcCoinAndMemberId(order.getCoin(),order.getMemberId());//卖币者
//            log.info("======更改钱包============卖币者："+order.getMemberId()+"买币者："+order.getCustomerId()+"==========");
//            BigDecimal number=order.getNumber().add(order.getCommission());
//            if (failId.equals(order.getCustomerId()) || successId.equals(order.getMemberId())) {//返回去
//                log.info("======解冻钱包============");
//                memberWalletService.thawBalance(seller,number);//解冻钱包
//            }else {
//                log.info("======卖币者减少冻结余额============");
//                MessageResult result1= memberWalletService.decreaseFrozen(seller.getId(),number);//卖币者减少冻结余额
//                String message=result1.getMessage();
//                if (result1.getCode()==500) {
//                    return MessageRespResult.error(message);
//                }
//                log.info("======买币者增加钱包余额============");
//                MessageResult result2=memberWalletService.increaseBalance(buyer.getId(),order.getNumber());//买币者增加钱包余额
//                String message2=result2.getMessage();
//                if (result1.getCode()==500) {
//                    return MessageRespResult.error(message2);
//                }
//                //交易记录
//                log.info("======更新会员交易记录============");
//                generateMemberTransaction(order, TransactionType.OTC_BUY, order.getCustomerId(), BigDecimal.ZERO);//用户
//                generateMemberTransaction(order, TransactionType.OTC_SELL, order.getMemberId(), order.getCommission());//广告主
//            }
//        } else {
//            throw new InformationExpiredException("Information Expired");
//        }
//        log.info("======增加用户C2C交易次数============");
//        //增加用户C2C交易次数
//        Member merchantMember = memberService.findOne(order.getMemberId());
//        merchantMember.setTransactions(merchantMember.getTransactions() + 1);
//        Member userMember = memberService.findOne(order.getCustomerId());
//        userMember.setTransactions(userMember.getTransactions() + 1);
//        memberService.save(merchantMember);
//        memberService.save(userMember);

        //0不冻结  1冻结
        BooleanEnum banned=appealDealWithScreen.getIsFrozen();
        if (banned==BooleanEnum.IS_TRUE){//冻结败方用户
            log.info("======冻结败方用户============"+failId);
            Member member=memberService.findOne(failId);
            if (member.getStatus() == CommonStatus.NORMAL){
                member.setStatus(CommonStatus.ILLEGAL);//禁用
                memberService.save(member);
            }
        }
        //修改申诉状态
        log.info("======修改申诉状态============");
        appeal.setDealWithTime(DateUtil.getCurrentDate());
        //申诉方id==胜方id？胜诉：败诉
        appeal.setIsSuccess(appeal.getInitiatorId().equals(appealDealWithScreen.getSuccessId())?BooleanEnum.IS_TRUE:BooleanEnum.IS_FALSE);
        appeal.setStatus(AppealStatus.PROCESSED);
        appeal.setSuccessRemark(appealDealWithScreen.getSuccessRemark());
        appeal.setAdmin(admin);
        appealService.save(appeal);

        //保存场外订单申诉处理附件
        log.info("======保存场外订单申诉处理附件============");

        if(!StringUtils.isEmpty(appealDealWithScreen.getUrlPath())) {
            String[] materialUrls = appealDealWithScreen.getUrlPath().split(",");
            for (String materialUrl : materialUrls) {
                AdminOrderAppealSuccessAccessory adminOrderAppealSuccessAccessory=new AdminOrderAppealSuccessAccessory();
                adminOrderAppealSuccessAccessory.setAppealId(appealDealWithScreen.getAppealId());
                adminOrderAppealSuccessAccessory.setUrlPath(materialUrl.split("[?]")[0].split("[|/]",4)[3]);
                adminOrderAppealSuccessAccessoryService.save(adminOrderAppealSuccessAccessory);
            }
        }
        return MessageRespResult.success("处理成功");
    }

}
