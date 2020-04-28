package com.spark.bitrade.controller.pay;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.controller.BaseController;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.dto.PayThirdPlatDto;
import com.spark.bitrade.entity.SilkTraderContract;
import com.spark.bitrade.entity.SilkTraderContractDetail;
import com.spark.bitrade.entity.ThirdPlatform;
import com.spark.bitrade.entity.ThirdPlatformApply;
import com.spark.bitrade.service.PayApplyService;
import com.spark.bitrade.service.PayThirdPlatService;
import com.spark.bitrade.service.SilkTraderPayService;
import com.spark.bitrade.util.MD5Util;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.UUIDUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author fumy
 * @time 2018.10.23 14:27
 */
@Api(description = "第三方支付申请签约功能管理接口",tags = "第三方支付申请签约功能操作接口")
@RestController
@RequestMapping("pay/manager")
@Slf4j
public class PayApplyController extends BaseController {

    @Autowired
    PayApplyService payApplyService;
    @Autowired
    PayThirdPlatService payThirdPlatService;
    @Autowired
    SilkTraderPayService silkTraderPayService;

    /**
     * 获取支付申请列表
     * @author fumy
     * @time 2018.10.23 16:14
     * @param busiAccount
     * @param status
     * @param pageNo
     * @param pageSize
     * @return true
     */
    @ApiOperation("签约申请分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "商户账号",name = "busiAccount",dataType = "String"),
            @ApiImplicitParam(value = "审核状态",name = "status",dataType = "int"),
            @ApiImplicitParam(value = "页码，从0开始",name = "pageNo",dataType = "int"),
            @ApiImplicitParam(value = "每页显示条数",name = "pageSize",dataType = "int")
    })
    @PostMapping("/apply_list")
    @AccessLog(module = AdminModule.PAY,operation = "签约申请分页查询")
    public MessageRespResult<ThirdPlatformApply> applyPage(String busiAccount,Integer status,int pageNo,int pageSize){
        PageInfo<ThirdPlatformApply> pageInfo = payApplyService.findByPgae(busiAccount, status, pageNo, pageSize);
        return MessageRespResult.success("查询成功",PageData.toPageData(pageInfo));
    }

    /**
     * 获取合作的第三方平台列表
     * @author fumy
     * @time 2018.10.23 16:39
     * @param platName
     * @param pageNo
     * @param pageSize
     * @return true
     */
    @ApiOperation("第三方平台列表分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "平台名称",name = "platName",dataType = "String"),
            @ApiImplicitParam(value = "页码，从0开始",name = "pageNo",dataType = "Integer"),
            @ApiImplicitParam(value = "每页显示条数",name = "pageSize",dataType = "Integer")
    })
    @PostMapping("/plat_list")
    @AccessLog(module = AdminModule.PAY,operation = "第三方平台列表分页查询")
    public MessageRespResult<ThirdPlatform> getThirdPlatList(String platName,int pageNo,int pageSize){
        PageInfo<ThirdPlatform> pageInfo = payThirdPlatService.getThirdPlatList(platName,pageNo,pageSize);
        return MessageRespResult.success("",PageData.toPageData(pageInfo));
    }

    /**
     * 审核通过
     * @author fumy
     * @time 2018.10.23 16:47
     * @param id
     * @return true
     */
    @ApiOperation("审核通过操作")
    @ApiImplicitParam(value = "申请记录编号",name = "id",dataType = "Long")
    @PostMapping("/apply/audit/pass")
    @Transactional(rollbackFor=Exception.class)
    @AccessLog(module = AdminModule.PAY,operation = "支付签约申请审核")
    public MessageRespResult audit_pass(Long id){
        try {
            //查询申请信息
            ThirdPlatformApply tpa = payApplyService.findById(id);
            //查询申请所属平台
            ThirdPlatform tpf = payThirdPlatService.findByKey(tpa.getApplyKey());
            //更新状态,审核通过
            tpa.setStatus(2);
            tpa.setLastTime(new Date());
            //保存申请记录
            ThirdPlatformApply tpaNew = payApplyService.save(tpa);
            //添加签约记录
            SilkTraderContract stc = new SilkTraderContract();
            stc.setAsyncNotifyUrl(tpaNew.getAsyncNotifyUrl());
            stc.setBusiAccount(tpaNew.getBusiAccount());
            stc.setBusiUrl(tpaNew.getAsyncNotifyUrl());
            stc.setContractNo(UUIDUtil.getUUID());//暂由uuid生成
            stc.setContractStart(new Date());
            stc.setDiscount(tpf.getDiscount());
            stc.setExpireTime(tpaNew.getExpireTime());
            stc.setPeriod((long)tpaNew.getPeriod());
            //生成通讯key
            String messageKey = buildMessageKey(tpaNew.getBusiAccount());
            stc.setMessageKey(messageKey);
            stc.setApplyId(tpaNew.getId());
            stc.setCreateTime(new Date());
            int row = silkTraderPayService.create(stc);
            if(row < 1)return MessageRespResult.error("审核失败,添加签约记录失败");
            //添加签约详情
            SilkTraderContractDetail stcd = new SilkTraderContractDetail();
            stcd.setBusiCoin(tpaNew.getBusiCoin());
            stcd.setBusiCoinFeeRate(tpaNew.getBusiCoinFeeRate());
            stcd.setContractCoin(tpaNew.getContractCoin());
            stcd.setContractNo(stc.getContractNo());
            stcd.setCurrency(tpaNew.getCurrency());
            stcd.setCreateTime(new Date());
            silkTraderPayService.createDetail(stcd);
            if(row < 1)return MessageRespResult.error("审核失败,添加签约详情录失败");
        }catch (Exception e){
            e.printStackTrace();
            log.error("审核异常-------------> e:",e);
            return  MessageRespResult.error("审核异常");
        }
        return MessageRespResult.success("审核操作成功");
    }

    /**
     * 审核不通过
     * @author fumy
     * @time 2018.10.24 9:23
     * @param id
     * @param reason
     * @return true
     */
    @ApiOperation("审核不通过操作")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "申请记录编号",name = "id",dataType = "Long",required = true),
            @ApiImplicitParam(value = "审核不通过原因",name = "reason",dataType = "String",required = true)
    })
    @PostMapping("/apply/audit/no_pass")
    @Transactional(rollbackFor=Exception.class)
    @AccessLog(module = AdminModule.PAY,operation = "支付签约申请审核")
    public MessageRespResult audit_no_pass(Long id,String reason){
        try{
            //查询申请信息
            ThirdPlatformApply tpa = payApplyService.findById(id);
            //更新状态,审核不通过,不通过原因
            tpa.setStatus(1);
            tpa.setComment(reason);
            tpa.setLastTime(new Date());
            //保存
            payApplyService.save(tpa);
        }catch (Exception e){
            log.error("审核异常-------------> e:",e.getStackTrace());
            return  MessageRespResult.error("审核异常");
        }
        return MessageRespResult.success("审核操作成功");
    }

    /**
     * 第三方平台保存
     * @author fumy
     * @time 2018.10.24 10:08
     * @param platDto
     * @return true
     */
    @ApiOperation("第三方平台添加操作")
    @PostMapping("/plat/save")
    @Transactional(rollbackFor=Exception.class)
    @AccessLog(module = AdminModule.PAY,operation = "第三方平台信息保存")
    public MessageRespResult plat_save(PayThirdPlatDto platDto){

        ThirdPlatform platform =new ThirdPlatform();
        try{
            if(platDto.getPlatformKey() == null || "".equals(platDto.getPlatformKey())){
                String platKey = buildMessageKey(platDto.getPlatformName());
                platform.setPlatformKey(platKey);
            }else {
                platform.setPlatformKey(platDto.getPlatformKey());
            }
            platform.setId(platDto.getId());
            platform.setPlatformName(platDto.getPlatformName());
            platform.setCoinCheck(platDto.getCoinCheck());
            platform.setStatus(platDto.getStatus());
            platform.setDiscount(platDto.getDiscount());
            payThirdPlatService.save(platform);
        }catch (Exception e){
            return MessageRespResult.error("保存失败");
        }
        return MessageRespResult.success("保存成功");
    }


    /**
     * 生成key
     * @author fumy
     * @time 2018.10.23 17:30
     * @param busiAccount
     * @return true
     */
    public static String buildMessageKey(String busiAccount) {
        String key = UUIDUtil.getUUID();
        String messageKey = busiAccount + key;
        messageKey = MD5Util.md5Encode(messageKey).toUpperCase();
        return messageKey;
    }
}
