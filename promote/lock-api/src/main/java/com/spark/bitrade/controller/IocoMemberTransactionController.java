package com.spark.bitrade.controller;



import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.transform.AuthMember;

import com.spark.bitrade.feign.IiocoService;
import com.spark.bitrade.util.AssertUtil;
import com.spark.bitrade.util.MessageRespResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * ioco钱包交易记录(IocoMemberTransaction)表控制层
 *
 * @author daring5920
 * @since 2019-07-03 14:38:58
 */
@RestController
@RequestMapping("iocoMemberTransaction")
@Api(description = "ioco钱包交易记录表控制层")
public class IocoMemberTransactionController {


    @Autowired
    private IiocoService iiocoService;
    /**
     * 获取ioco申购页面所需数据
     * @author shenzucai
     * @time 2019.07.04 8:21
     * @param user
     * @return true
     */
    @ApiOperation(value = "获取ioco申购页面所需数据", notes = "获取ioco申购页面所需数据")
    @PostMapping(value = "/purchaseIndex")
    public MessageRespResult<Object> getPurchaseData(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        // 调用service获取具体数据
        return iiocoService.getPurchaseData(user.getId());
    }


    /**
     * 获取ioco转赠页面所需数据
     * @author shenzucai
     * @time 2019.07.04 8:21
     * @param user
     * @return true
     */
    @ApiOperation(value = "获取ioco转赠页面所需数据", notes = "获取ioco转赠页面所需数据")
    @PostMapping(value = "/giftIndex")
    public MessageRespResult<Object> giftIndex(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        // 调用service获取具体数据
        return iiocoService.giftIndex(user.getId());
    }

    /**
     * ioco申购slp
     * @author shenzucai
     * @time 2019.07.04 8:21
     * @param user
     * @return true
     */
    @ApiOperation(value = "ioco申购slp", notes = "ioco申购slp")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "支付币种", name = "purchasetUnit", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(value = "支付数量", name = "purchaseAmount", dataTypeClass = BigDecimal.class, required = true),
            @ApiImplicitParam(value = "申购数量", name = "slpAmount", dataTypeClass = BigDecimal.class, required = true),
            @ApiImplicitParam(value = "申购份数", name = "share", dataTypeClass = Integer.class, required = true),
            @ApiImplicitParam(value = "当前活动id", name = "activityId", dataTypeClass = Long.class, required = true)
    })
    @PostMapping(value = "/purchaseSLP")
    public MessageRespResult<Boolean> purchaseSLP(@SessionAttribute(SESSION_MEMBER) AuthMember user
            , @RequestParam("purchasetUnit") String purchasetUnit
            , @RequestParam("purchaseAmount") BigDecimal purchaseAmount
            , @RequestParam("slpAmount") BigDecimal slpAmount
            , @RequestParam("share") Integer share
            , @RequestParam("activityId") Long activityId) {
        // 调用service获取具体数据
        return iiocoService.purchaseSLP(user.getId() ,purchasetUnit,purchaseAmount,slpAmount,share,activityId);
    }


    /**
     * ioco转赠slp
     * @author shenzucai
     * @time 2019.07.04 8:21
     * @param user
     * @return true
     */
    @ApiOperation(value = "ioco转赠slp", notes = "ioco转赠slp")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "转赠币种", name = "giftUnit", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(value = "转赠数量", name = "giftAmount", dataTypeClass = BigDecimal.class, required = true),
            @ApiImplicitParam(value = "转赠对象（手机号或邮箱）", name = "giftTo", dataTypeClass = String.class, required = true)
    })
    @PostMapping(value = "/gitSLP")
    public MessageRespResult<Boolean> giftSLP(@SessionAttribute(SESSION_MEMBER) AuthMember user
            , @RequestParam("giftUnit") String giftUnit
            , @RequestParam("giftAmount") BigDecimal giftAmount
            , @RequestParam("giftTo") String giftTo) {
        // 调用service获取具体数据
        return iiocoService.giftSLP(user.getId() ,giftUnit,giftAmount,giftTo);
    }

    /**
     * 分页查询所有转账数据
     *
     * @param size 分页.每页数量
     * @param current 分页.当前页码
     * @param type 转账类型0 申购，1是转赠
     * @return 所有数据
     */
    @ApiOperation(value = "分页查询ioco所有数据接口", notes = "分页查询ioco所有数据接口")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "分页.每页数量。eg：10", defaultValue = "10", name = "size", dataTypeClass = Integer.class, required = true),
            @ApiImplicitParam(value = "分页.当前页码.eg：从1开始", name = "current", defaultValue = "1", dataTypeClass = Integer.class, required = true),
            @ApiImplicitParam(value = "转账类型0 申购，1是转赠", name = "type", dataTypeClass =Integer.class)
    })
    @PostMapping(value = "/listByType")
    public MessageRespResult<Object> listByType(@SessionAttribute(SESSION_MEMBER) AuthMember user, @RequestParam("size") Integer size, @RequestParam("current") Integer current, @RequestParam("type") Integer type) {
        return iiocoService.listByType(user.getId() ,size,current,type);
    }
}