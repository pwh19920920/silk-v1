package com.spark.bitrade.controller;

import com.spark.bitrade.entity.ASellConfigDetailVo;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.feign.IBTOpenApiService;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.util.MessageRespResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.math.BigDecimal;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

@RestController
@RequestMapping("api")
@Api(description = "BT-c2c API 控制层")
@Slf4j
public class OtcApiController {
  @Autowired private IBTOpenApiService ibtOpenApiService;
  @Autowired private MemberService memberService;
  @Autowired private LocaleMessageSourceService msService;

  @ApiOperation(value = "app-资产查询")
  @PostMapping(value = "/appAssert")
  public MessageRespResult<Object> appAssert(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
    // 调用service获取具体数据
    return ibtOpenApiService.appAssert(user.getId());
  }

  @ApiOperation(value = "app-查询交易记录")
  @PostMapping(value = "/appOrderPages")
  public MessageRespResult<Object> appOrderPages(
      @SessionAttribute(SESSION_MEMBER) AuthMember user,
      Integer pageNo,
      Integer pageSize,
      Integer status,
      Integer type) {
    // 调用service获取具体数据
    return ibtOpenApiService.appOrderPages(pageNo, pageSize, user.getId(), status, type);
  }

  @ApiOperation(value = "app-查询正在进行的订单")
  @PostMapping(value = "/appGoingOrderPages")
  public MessageRespResult<Object> appGoingOrderPages(
      @SessionAttribute(SESSION_MEMBER) AuthMember user, Integer pageNo, Integer pageSize) {
    // 调用service获取具体数据
    return ibtOpenApiService.appGoingOrderPages(pageNo, pageSize, user.getId());
  }

  @ApiOperation(value = "app-资产明细查询")
  @PostMapping(value = "/assertPages")
  public MessageRespResult<Object> assertPages(
      @SessionAttribute(SESSION_MEMBER) AuthMember user, Integer pageNo, Integer pageSize) {
    // 调用service获取具体数据
    return ibtOpenApiService.assertPages(pageNo, pageSize, user.getId());
  }

  @ApiOperation(value = "app-查询订单记录")
  @PostMapping(value = "/apiOrderDetail")
  public MessageRespResult<Object> apiOrderDetail(
      @SessionAttribute(SESSION_MEMBER) AuthMember user, String orderSn) {
    // 调用service获取具体数据
    return ibtOpenApiService.apiOrderDetail(orderSn, user.getId());
  }

  @ApiOperation(value = "app-api卖币查询")
  @PostMapping(value = "/sellConfigDetail")
  public MessageRespResult<Object> sellConfigDetail(
      @SessionAttribute(SESSION_MEMBER) AuthMember user) {
    // 调用service获取具体数据
    return ibtOpenApiService.sellConfigDetail(user.getId());
  }

  @ApiOperation(value = "app-api开启/关闭卖币")
  @PostMapping(value = "/sellConfigUpdate")
  public MessageRespResult<Object> sellConfigUpdate(
      @SessionAttribute(SESSION_MEMBER) AuthMember user, ASellConfigDetailVo vo) {
    vo.setMemberId(user.getId());
    // 调用service获取具体数据
    return ibtOpenApiService.sellConfigUpdate(vo);
  }

  @ApiOperation(value = "api划转（APP -> API）")
  @PostMapping(value = "/transfor")
  public MessageRespResult<Object> transfor(
      @SessionAttribute(SESSION_MEMBER) AuthMember user, BigDecimal amount, String jyPassword) {
    Assert.notNull(jyPassword, "资金交易密码不能空");
    Member member = memberService.findOne(user.getId());
    String mbPassword = member.getJyPassword();
    Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
    String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
    Assert.isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));
    // 调用service获取具体数据
    return ibtOpenApiService.sellConfigUpdate(amount, user.getId());
  }
}
