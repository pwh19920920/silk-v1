package com.spark.bitrade.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.constant.PayTransferType;
import com.spark.bitrade.controller.SilkTraderPayResponse;
import com.spark.bitrade.dto.PaymentBarCodeGenerateDTO;
import com.spark.bitrade.dto.PaymentBarCodePayDTO;
import com.spark.bitrade.dto.WalletQRPayParameterDTO;
import com.spark.bitrade.emuns.PayType;
import com.spark.bitrade.emuns.PaymentCodeStatus;
import com.spark.bitrade.entity.AuthCode;
import com.spark.bitrade.entity.PayPaymentCodeManage;
import com.spark.bitrade.entity.PayPaymentCodeStorage;
import com.spark.bitrade.exception.ApiException;
import com.spark.bitrade.exception.BusinessProcessException;
import com.spark.bitrade.mapper.dao.PayPaymentCodeStorageMapper;
import com.spark.bitrade.service.IPayPaymentCodeManageService;
import com.spark.bitrade.service.IWalletQRPayService;
import com.spark.bitrade.util.IdGenerator;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.utils.BarCodeUtils;
import com.sun.tools.corba.se.idl.StringGen;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Calendar;
import java.util.Date;

/**
 * <p>扫码支付业务处理接口实现</p>
 * @author tian.bo
 * @date 2019/1/15.
 */
@Service
@Slf4j
public class WalletQRPayServiceImpl extends ServiceImpl<PayPaymentCodeStorageMapper,PayPaymentCodeStorage> implements IWalletQRPayService {

    @Autowired
    private IPayPaymentCodeManageService payPaymentCodeManageService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public boolean validationArgs(WalletQRPayParameterDTO dto) throws ApiException,IllegalStateException {

        /**
         * 业务参数校验
         */
        return this.bizValidationArgs(dto);
    }


    /**
     * 生成authcode
     * @param dto
     * @return
     */
    private AuthCode codeGenerate(PaymentBarCodeGenerateDTO dto){
        //String barcode = new StringBuilder(String.valueOf(new Date().getTime())).append(BarCodeUtils.barCodeGenerate(5)).toString();
        String authCode = BarCodeUtils.barCodeGenerate(BarCodeUtils.dataBit);
        AuthCode authCode1 = new AuthCode();
        authCode1.setAuthCode(authCode);
        PayPaymentCodeStorage payPaymentCodeStorageCondition = new PayPaymentCodeStorage();
        payPaymentCodeStorageCondition.setAuthCode(authCode);
        PayPaymentCodeStorage payPaymentCodeStorage = this.baseMapper.selectOne(payPaymentCodeStorageCondition);
        if(payPaymentCodeStorage != null){
            codeGenerate(dto);
        }else {
            return authCode1;
        }
        return authCode1;
    }


    /**
     * 条码生成
     * @param dto
     * @return
     */
    @Override
    public PayPaymentCodeStorage barCodeGenerate(PaymentBarCodeGenerateDTO dto) throws RuntimeException {

        dto.validation();
        PayPaymentCodeManage payPaymentCodeManage = payPaymentCodeManageService.findByMemberIdAndAppid(Long.valueOf(dto.getAccountId()),dto.getAppId());
        if(null == payPaymentCodeManage){
            throw  new BusinessProcessException("ISP.PAYMENT-CODE-DISABLED","未开启付款码功能");
        }

        AuthCode authCode = codeGenerate(dto);
        Date current = new Date();
        Calendar timeoutExpres = Calendar.getInstance();
        timeoutExpres.add(Calendar.SECOND, payPaymentCodeManage.getStrategyEffectiveTime());
        PayPaymentCodeStorage payPaymentCodeStorage = new PayPaymentCodeStorage();
        payPaymentCodeStorage.setAppId(dto.getAppId());
        payPaymentCodeStorage.setPid(dto.getPid());
        payPaymentCodeStorage.setAuthCode(authCode.getAuthCode());
        //payPaymentCodeStorage.setBarCode(authCode.getBarCode());
        payPaymentCodeStorage.setCreateTime(current);
        payPaymentCodeStorage.setMemberId(Long.valueOf(dto.getAccountId()));
        payPaymentCodeStorage.setScene(Integer.valueOf(dto.getScene()));
        payPaymentCodeStorage.setSymbol(dto.getSymbol());
        payPaymentCodeStorage.setTradeNo(String.valueOf(new IdGenerator(2).nextId()));
        payPaymentCodeStorage.setWalletType(dto.getWalletType());
        payPaymentCodeStorage.setTimeoutExpres(timeoutExpres.getTime());
        payPaymentCodeStorage.setStatus(PaymentCodeStatus.WAIT_BUYER_PAY.getCode());
        payPaymentCodeStorage.setWalletMarkId(dto.getWalletMarkId());
        this.baseMapper.insert(payPaymentCodeStorage);
        return payPaymentCodeStorage;
    }

    /**
     * 条码支付
     *  下个版本考虑pid和appid合法性校验
     * @param dto
     * @return
     * @throws ApiException
     */
    @Override
    public MessageResult barCodePay(PaymentBarCodePayDTO dto, String token) throws RuntimeException {
        //请求参数必填校验
        dto.validation();
        //查询条码信息,并验证有效性
        if(PayType.BARCODEPAY.getCode() != Integer.valueOf(dto.getPayType())){
            throw new BusinessProcessException("ISP.INVALID-PAY-TYPE","无效的payType参数");
        }

        PayPaymentCodeStorage payPaymentCodeStorage = new PayPaymentCodeStorage();
        payPaymentCodeStorage.setAuthCode(dto.getAuthCode());
        payPaymentCodeStorage = this.baseMapper.selectOne(payPaymentCodeStorage);
        if(null == payPaymentCodeStorage){
            throw new BusinessProcessException("ISP.INVALID-AUTH-CODE","无效的authCode参数");
        }
        //条码有效性验证
        if(payPaymentCodeStorage.getStatus() == PaymentCodeStatus.PAY_FINISHED.getCode()){
            //已支付
            throw new BusinessProcessException("ISP.REPEAT-PAYMENT-CODE","付款码重复支付");
        }
        if(payPaymentCodeStorage.getStatus() == PaymentCodeStatus.PAY_INVALID.getCode()){
            //已失效
            throw new BusinessProcessException("ISP.PAYMENT-CODE-EXPRES","付款码已过期");
        }
        //系统当前时间
        Date currentDate = new Date();
        //付款码过期时间
        Date expres = payPaymentCodeStorage.getTimeoutExpres();

        if(expres.compareTo(currentDate)<0){
            //修改付款码状态
            payPaymentCodeStorage.setStatus(PaymentCodeStatus.PAY_INVALID.getCode());
            this.baseMapper.updateById(payPaymentCodeStorage);

            //已失效
            throw new BusinessProcessException("ISP.PAYMENT-CODE-EXPRES","付款码已过期");
        }

        //查询会员付款码功能是否开启
        PayPaymentCodeManage payPaymentCodeManage = payPaymentCodeManageService.findByMemberIdAndAppid(payPaymentCodeStorage.getMemberId(),payPaymentCodeStorage.getAppId());
        if (null == payPaymentCodeManage){
            throw new BusinessProcessException("ISP.PAYMENT-CODE-DISABLED","付款码功能未开启");
        }
        //验证扫码方是否商家,下个版本考虑
        /**
         * 2.远程调用账务服务，完成转账服务
         */
        MessageResult messageResult = platformTransfer(dto,payPaymentCodeStorage,token);
        if(messageResult.isSuccess()){
            //修改条码支付状态
            payPaymentCodeStorage.setStatus(PaymentCodeStatus.PAY_FINISHED.getCode());
            this.baseMapper.updateById(payPaymentCodeStorage);
        }
        return messageResult;
    }

    /**
     * 扫码枪支付
     * @param dto
     * @return
     * @throws RuntimeException
     */
    @Override
    public MessageResult barCodeScannerPay(PaymentBarCodePayDTO dto) throws RuntimeException {
        //请求参数必填校验
        /*if(null == dto){
            throw new ApiException("isp.invalid-parameter","参数无效");
        }*/
        dto.validation();

        //查询条码信息,并验证有效性
        if(PayType.BARCODEPAY.getCode() != Integer.valueOf(dto.getPayType())){
            throw new BusinessProcessException("ISP.INVALID-PAY-TYPE","无效的payType参数");
        }

        PayPaymentCodeStorage payPaymentCodeStorage = new PayPaymentCodeStorage();
        payPaymentCodeStorage.setAuthCode(dto.getAuthCode());
        payPaymentCodeStorage = this.baseMapper.selectOne(payPaymentCodeStorage);
        if(null == payPaymentCodeStorage){
            throw new BusinessProcessException("ISP.INVALID-AUTH-CODE","无效的authCode参数");
        }
        //条码有效性验证
        if(payPaymentCodeStorage.getStatus() == PaymentCodeStatus.PAY_FINISHED.getCode()){
            //已支付
            throw new BusinessProcessException("ISP.REPEAT-PAYMENT-CODE","付款码重复支付");
        }
        if(payPaymentCodeStorage.getStatus() == PaymentCodeStatus.PAY_INVALID.getCode()){
            //已失效
            throw new BusinessProcessException("ISP.PAYMENT-CODE-EXPRES","付款码已过期");
        }
        //系统当前时间
        Date currentDate = new Date();
        //付款码过期时间
        Date expres = payPaymentCodeStorage.getTimeoutExpres();

        if(expres.compareTo(currentDate)<0){
            //修改付款码状态
            payPaymentCodeStorage.setStatus(PaymentCodeStatus.PAY_INVALID.getCode());
            this.baseMapper.updateById(payPaymentCodeStorage);

            //已失效
            throw new BusinessProcessException("ISP.PAYMENT-CODE-EXPRES","付款码已过期");
        }

        //查询会员付款码功能是否开启
        PayPaymentCodeManage payPaymentCodeManage = payPaymentCodeManageService.findByMemberIdAndAppid(payPaymentCodeStorage.getMemberId(),payPaymentCodeStorage.getAppId());
        if (null == payPaymentCodeManage){
            throw new BusinessProcessException("ISP.PAYMENT-CODE-DISABLED","付款码功能未开启");
        }
        //验证扫码方是否商家,下个版本考虑
        /**
         * 2.远程调用账务服务，完成转账服务
         */
        MessageResult messageResult = platformTransferScannerPay(dto,payPaymentCodeStorage);
        if(messageResult.isSuccess()){
            //messageResult.setData(payPaymentCodeStorage);
            //修改条码支付状态
            payPaymentCodeStorage.setStatus(PaymentCodeStatus.PAY_FINISHED.getCode());
            this.baseMapper.updateById(payPaymentCodeStorage);
        }
        return messageResult;

    }

    /**
     * 扫码枪支付
     * @param dto
     * @param payPaymentCodeStorage
     * @return
     */
    private MessageResult platformTransferScannerPay(PaymentBarCodePayDTO dto,PayPaymentCodeStorage payPaymentCodeStorage){
        //参数初始化
        String url = "http://ucenter-api/uc/account/platformTransferByMemberId";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type","application/x-www-form-urlencoded");

        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<String, Object>();
        multiValueMap.add("toMemberId",dto.getAccountId().toString());
        multiValueMap.add("fromMemberId",payPaymentCodeStorage.getMemberId().toString());
        multiValueMap.add("tradeUnit",dto.getSymbol());
        multiValueMap.add("amount",dto.getAmount().toString());
        multiValueMap.add("transferType", String.valueOf(PayTransferType.PAYMENT_CODE.getId()));
        multiValueMap.add("platform", payPaymentCodeStorage.getAppId());
        multiValueMap.add("platformTo", dto.getAppId());

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(multiValueMap,httpHeaders);
        ResponseEntity<String> response = null;
        MessageResult respnoseMsg = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST,httpEntity,String.class);
            respnoseMsg = JSON.parseObject(response.getBody(),MessageResult.class);
            log.info("rpc-transfer,params={}",dto.toString());
            log.info("rpc-transfer,response={}",respnoseMsg);
        }catch (Exception e){
            e.printStackTrace();
            log.error("rpc-transfer error={}",e.getMessage());
            if(null != respnoseMsg){
                return respnoseMsg;
            }
            return MessageResult.error(e.getMessage());
        }
        return respnoseMsg;
    }


    /**
     * 查询条码信息
     * @param authCode
     * @return
     * @throws RuntimeException
     */
    @Override
    public PayPaymentCodeStorage selectPaymentCodeByAuthCode(String authCode) throws RuntimeException {
        PayPaymentCodeStorage payPaymentCodeStorageCondition = new PayPaymentCodeStorage();
        payPaymentCodeStorageCondition.setAuthCode(authCode);
        PayPaymentCodeStorage payPaymentCodeStorage = this.baseMapper.selectOne(payPaymentCodeStorageCondition);
        return payPaymentCodeStorage;
    }


    /**
     * 付款码支付-交易平台转账接口
     * @param dto
     * @param payPaymentCodeStorage
     * @param token
     * @return
     */
    private MessageResult platformTransfer(PaymentBarCodePayDTO dto,PayPaymentCodeStorage payPaymentCodeStorage,String token){
        //参数初始化
        String url = "http://ucenter-api/uc/account/platformTransfer";
        //String url = "http://172.16.0.152:6001/uc/account/platformTransfer";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type","application/x-www-form-urlencoded");
        httpHeaders.add("wallet-auth-token",token);
        httpHeaders.add("cmd",/*request.getHeader("cmd")*/"");

        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<String, Object>();
        multiValueMap.add("toMemberId",dto.getAccountId().toString());
        multiValueMap.add("fromMemberId",payPaymentCodeStorage.getMemberId().toString());
        multiValueMap.add("tradeUnit",dto.getSymbol());
        multiValueMap.add("amount",dto.getAmount().toString());
        multiValueMap.add("transferType", String.valueOf(PayTransferType.PAYMENT_CODE.getId()));
        multiValueMap.add("platform", payPaymentCodeStorage.getAppId());
        multiValueMap.add("platformTo", dto.getAppId());
        /*if(null != dto.getFee()) {
            multiValueMap.add("fee", dto.getFee().toString());
        }
        if(StringUtils.isNotEmpty(dto.getChannel())) {
            multiValueMap.add("channel", dto.getChannel().toString());
        }
        if(StringUtils.isNotEmpty(dto.getWalletMarkId())) {
            multiValueMap.add("walletMarkId", dto.getWalletMarkId().toString());
        }
        if(StringUtils.isNotEmpty(dto.getPayType())) {
            multiValueMap.add("payType", dto.getPayType().toString());
        }*/

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(multiValueMap,httpHeaders);
        ResponseEntity<String> response = null;
        MessageResult respnoseMsg = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST,httpEntity,String.class);
            respnoseMsg = JSON.parseObject(response.getBody(),MessageResult.class);
            log.info("rpc-transfer,params={}",dto.toString());
            log.info("rpc-transfer,response={}",respnoseMsg);
        }catch (Exception e){
            e.printStackTrace();
            log.error("rpc-transfer error={}",e.getMessage());
            if(null != respnoseMsg){
                return respnoseMsg;
            }
            return MessageResult.error(e.getMessage());
        }
        return respnoseMsg;
    }




    /**
     * 业务参数校验
     * @param parameter
     * @return
     * @throws ApiException
     */
    private boolean bizValidationArgs(WalletQRPayParameterDTO parameter) throws ApiException {
        if(null == parameter){
            throw new ApiException("invalid-parameter","无效参数");
        }
        return parameter.validation();
    }
}
