package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.controller.SilkTraderPayResponse;
import com.spark.bitrade.dto.PaymentBarCodeGenerateDTO;
import com.spark.bitrade.dto.PaymentBarCodePayDTO;
import com.spark.bitrade.dto.WalletQRPayParameterDTO;
import com.spark.bitrade.entity.PayPaymentCodeStorage;
import com.spark.bitrade.exception.ApiException;
import com.spark.bitrade.util.MessageResult;

/**
 * <p>扫码支付业务处理接口</p>
 * @author tian.bo
 * @date 2019/1/15.
 */
public interface IWalletQRPayService extends IService<PayPaymentCodeStorage> {


    /**
     * 参数校验
     * @param dto
     * @return
     */
     boolean validationArgs(WalletQRPayParameterDTO dto) throws ApiException,IllegalStateException;


    /**
     * 条码生成
     * @param dto
     * @return
     * @throws ApiException
     */
    PayPaymentCodeStorage barCodeGenerate(PaymentBarCodeGenerateDTO dto) throws RuntimeException;

    /**
     * 条码支付
     * @param dto
     * @return
     * @throws ApiException
     */
    MessageResult barCodePay(PaymentBarCodePayDTO dto, String token) throws RuntimeException;


    /**
     * 扫码枪支付
     * @param dto
     * @return
     * @throws RuntimeException
     */
    MessageResult barCodeScannerPay(PaymentBarCodePayDTO dto) throws RuntimeException;


    /**
     * 根据授权码查询条码信息
     * @param auth
     * @return
     * @throws RuntimeException
     */
    PayPaymentCodeStorage selectPaymentCodeByAuthCode(String auth) throws RuntimeException;

}
