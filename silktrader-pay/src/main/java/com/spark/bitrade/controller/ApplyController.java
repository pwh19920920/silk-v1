package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.dto.PayApplyDTO;
import com.spark.bitrade.entity.ThirdPlatformApply;
import com.spark.bitrade.service.IPayApplyService;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.utils.Base64Util;
import com.spark.bitrade.vo.PayApplyVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author fumy
 * @time 2018.10.22 13:52
 */
@Api(description = "第三方支付申请操作接口")
@RestController
@RequestMapping("apply")
@Slf4j
public class ApplyController {

    @Autowired
    IPayApplyService iPayApplayService;


    @ApiOperation(value = "申请签约",notes = "第三方商户发起SilkTrader-Pay的支付签约申请")
    @PostMapping("/signed")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "加密json串",name = "para",dataType = "String"),
            @ApiImplicitParam(value = "申请key",name = "applyKey",dataType = "String")
    })
    public MessageRespResult applyPaySigned(String para,String applyKey){

        //根据applyKey 查询是否是第三方合作平台
        boolean isExist = iPayApplayService.isExistPlatByApplyKey(applyKey);
        //如果不存在，则返回提示结果
        if (!isExist){
           return MessageRespResult.error("发起申请所属的平台不存在或已关闭支付通道！");
        }
        //如果存在，则根据applyKey对加密para参数进行解密
        String decodeStr = Base64Util.decodeDES(para,applyKey);
        JSONObject strJson= JSON.parseObject(decodeStr);
        PayApplyDTO pao = JSON.toJavaObject(strJson,PayApplyDTO.class);
        log.info("提交支付签约申请，商户账号={},applyKey={}",pao.getBusiAccount(),applyKey);
        //查询用户是否已经提交申请
        isExist = iPayApplayService.isExistApply(pao.getBusiAccount(),applyKey);
        if(!isExist){
            return MessageRespResult.error("请勿重复提交申请！");
        }
        //将解密的参数内容填充到申请实体类
        ThirdPlatformApply tpa = new ThirdPlatformApply();
        tpa.setApplyKey(applyKey);
        tpa.setBusiAccount(pao.getBusiAccount());
        tpa.setAsyncNotifyUrl(pao.getAsyncNotifyUrl());
        tpa.setBusiCoin(pao.getBusiCoin());
        tpa.setBusiCoinFeeRate(pao.getBusiCoinFeeRate());
        tpa.setContractCoin(pao.getContractCoin());
        tpa.setCurrency(pao.getCurrency());
        tpa.setPeriod(pao.getPeriod());
        tpa.setExpireTime(pao.getExpireTime());
        tpa.setApplyTime(new Date());
        iPayApplayService.save(tpa);
        //添加申请记录
        return MessageRespResult.success("签约申请提交成功");
    }

    @ApiOperation(value = "根据商户SilkTrader账号id申请结果查询",notes = "商户支付签约申请结果查询")
    @GetMapping("/result")
    @ApiImplicitParam(value = "商户账号",name = "account",dataType = "String")
    public MessageRespResult<PayApplyVo> getApplyResultByBusiAccount(String account){
        PayApplyVo applyVo = iPayApplayService.getApplyByAccount(account);
        return MessageRespResult.success("查询成功",applyVo);
    }

    public static void main(String[] args) {
        String applyKey = "B5CE1478610F139E2CD00E83B60026EA";
        PayApplyDTO pao = new PayApplyDTO();
        pao.setBusiAccount("74737");
        pao.setCurrency("CNY");
        pao.setContractCoin("SLU");
        pao.setBusiCoin("SLU");
        pao.setBusiCoinFeeRate(new BigDecimal("0.1"));
        pao.setAsyncNotifyUrl("http://st.bi");
        pao.setPeriod(-1);
        pao.setExpireTime(-1L);

        String para = JSONObject.toJSONString(pao);

        //加密输入参数
        para = Base64Util.encodeDES(para,applyKey);
        //neLPST0SKy2mpEhwy6hYH0Y+bB/yeMIr0y8z8aBrDitjf4njTPo+TJbcK6B2P95s1BKIwOs6BiFm/vxWbJXGy5obUQ+SxvfZRiqyiUSSjNZ4iYzPRGQ4HcLF9ex4dJfKJhgdgMltapxjh6qs7thEi8bpn0i3q+/DfiRtk/xODLPviW9Evj3HCqqEaa7fCkRxGg8EbfNMQ2wntwCAuskJQdUQTuWq/+Nl
        System.out.println(para);
    }
}
