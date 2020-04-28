package com.spark.bitrade.controller;

import com.spark.bitrade.config.AliyunConfig;
import com.spark.bitrade.constant.AppealStatus;
import com.spark.bitrade.constant.AppealType;
import com.spark.bitrade.constant.OrderStatus;
import com.spark.bitrade.dto.OtcApiOrderDto;
import com.spark.bitrade.entity.AppealApply;
import com.spark.bitrade.entity.OrderAppealAccessory;
import com.spark.bitrade.entity.OtcApiAppeal;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.AppealService;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.OrderAppealAccessoryService;
import com.spark.bitrade.service.OtcOrderService;
import com.spark.bitrade.util.AliyunUtil;
import com.spark.bitrade.util.BindingResultUtil;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.vo.OtcApiAppealVo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static com.spark.bitrade.util.MessageResult.error;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.09.30 09:27  
 */
@Api(description = "otcApiOrder申诉")
@RestController
@RequestMapping(value = "/otcApiOrder")
@Slf4j
public class OtcApiAppealController {

    @Autowired
    private OtcOrderService otcOrderService;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private AppealService appealService;
    @Autowired
    private OrderAppealAccessoryService orderAppealAccessoryService;

    /**
     * otcApi BTbank内部用户申诉
     *
     * @param appealApply
     * @param bindingResult
     * @param user
     * @return
     */
    @PostMapping("applyAppeal")
    public MessageResult applyAppeal(@Valid AppealApply appealApply, BindingResult bindingResult,
                                     @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        OtcApiOrderDto orderDto = otcOrderService.findOtcApiOrderByorderSn(appealApply.getOrderSn());
        notNull(orderDto, msService.getMessage("ORDER_NOT_EXISTS"));
        isTrue(orderDto.getStatus().equals(OrderStatus.PAID), msService.getMessage("NO_APPEAL"));
        isTrue(user.getId()==orderDto.getTradeId(),msService.getMessage("THE_ORDER_NOT_YOURS"));
        // 付款完成后30分钟才可以进行申诉
        if (DateUtil.compareDateMinute(new Date(), orderDto.getPayTime()) < 30) {
            return error(msService.getMessage("FIRST_APPEAL_ILLEGAL_TIME"));
        }
        //取消申诉30分钟后才能再次申诉
        OtcApiAppeal lastAppeal = otcOrderService.findAppealByOtcApiOrder(orderDto.getId());
        if (lastAppeal != null && lastAppeal.getStatus() == AppealStatus.CANCELED.getOrdinal() &&
                DateUtil.compareDateMinute(new Date(), lastAppeal.getCancelTime()) < 30) {
            return error(msService.getMessage("APPEAL_ILLEGAL_TIME"));
        }

        //申诉者必是 星客 交易方 octApiOrder 卖出
        OtcApiAppeal appeal = new OtcApiAppeal();
        appeal.setOtcApiOrderId(orderDto.getId());
        appeal.setInitiatorId(user.getId());
        appeal.setAssociateId(orderDto.getMemberId());
        appeal.setRemark(appealApply.getRemark());
        //  将申诉类型存入表
        appeal.setAppealType(appealApply.getAppealType().getOrdinal());
        appeal.setCreateTime(new Date());
        appeal.setStatus(0);
        //事务处理申诉
        appeal.setOrganizationId(orderDto.getOrganizationId());
        OtcApiAppeal o = appealService.doOtcApiAppeal(appeal, orderDto, appealApply.getMaterialUrls());


        return MessageResult.success(msService.getMessage("APPEAL_SUCCESS"), o);
    }


    /**
     * 取消申诉
     *
     * @param type
     * @param description
     * @param appealId
     * @param user
     * @return
     */
    @PostMapping("cancelAppeal")
    public MessageResult cancelAppeal(@RequestParam Integer type,
                                      @RequestParam String description,
                                      @RequestParam Long appealId,
                                      @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        notNull(appealId, msService.getMessage("PARAMETER_ERROR"));

        OtcApiAppeal appeal = appealService.findOneOtcApiAppeal(appealId);
        notNull(appeal, msService.getMessage("APPEAL_NOT_HAVE"));
        isTrue(appeal.getStatus().equals(AppealStatus.NOT_PROCESSED.getOrdinal()),
                msService.getMessage("APPEAL_CANCEL_FAILED"));
        //非申诉方不可取消申诉
        isTrue(appeal.getInitiatorId() == user.getId(), msService.getMessage("ILLEGAL_INITIATOR"));
        appeal.setCancelReason(type);
        appeal.setCancelDescription(description);
        appeal.setCancelTime(new Date());
        appeal.setStatus(AppealStatus.CANCELED.getOrdinal());
        appeal.setCancelId(user.getId());
        appeal.setUpdateTime(new Date());
        appealService.doOtcApiCancelAppeal(appeal);

        return MessageResult.success();
    }

    @Autowired
    private AliyunConfig aliyunConfig;

    @PostMapping("findOtcApiAppeal")
    public MessageResult findOtcApiAppeal(@RequestParam Long otcApiOrderId) {
        OtcApiAppeal apiAppeal = appealService.findOtcAppealByOtcApiOrderId(otcApiOrderId);
        if (apiAppeal == null) {
            return MessageResult.success();
        }
        OtcApiAppealVo vo = new OtcApiAppealVo();
        BeanUtils.copyProperties(apiAppeal, vo);
        vo.setAppealTypeName(AppealType.findOfOrdinal(vo.getAppealType()).getCnName());
        List<String> urls = new ArrayList<>();
        //查询图片
        List<OrderAppealAccessory> accessories = orderAppealAccessoryService.findByOtcApiAppealId(apiAppeal.getId());
        if (!CollectionUtils.isEmpty(accessories)) {
            try {
                accessories.forEach(k -> urls.add(AliyunUtil.getPrivateUrl(aliyunConfig, k.getUrlPath())));
            }catch (Exception e){
                log.error(ExceptionUtils.getStackTrace(e));
                log.error("图片地址解析失败");
            }
        }
        vo.setFiles(urls);
        return MessageResult.success("success", vo);
    }



}






















