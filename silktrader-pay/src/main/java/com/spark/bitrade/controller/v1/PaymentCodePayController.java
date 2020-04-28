package com.spark.bitrade.controller.v1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.annotation.definition.ApiRequestLimit;
import com.spark.bitrade.controller.SilkTraderPayResponse;
import com.spark.bitrade.dto.PaymentBarCodeGenerateDTO;
import com.spark.bitrade.dto.PaymentBarCodePayDTO;
import com.spark.bitrade.emuns.SilkpayMessageCode;
import com.spark.bitrade.entity.PayPaymentCodeStorage;
import com.spark.bitrade.exception.ApiException;
import com.spark.bitrade.exception.BusinessException;
import com.spark.bitrade.exception.ExceptionCode;
import com.spark.bitrade.exception.RequiredArgException;
import com.spark.bitrade.service.IWalletQRPayService;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <p>付款码支付控制器</p>
 * @author tian.bo
 * @date 2019/3/8.
 */
@Api(description = "付款码支付控制器")
@RestController
@RequestMapping("/paymentCode/api")
@Slf4j
public class PaymentCodePayController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IWalletQRPayService walletQRPayService;

    /**
     * 扫码支付
     * @param dto
     *             WalletQRPayParameterDTO
     * @param request
     * @return
     */
    @ApiRequestLimit(count = 10000)
    //@Decrypt(decryptType = EncryptType.AES)
    @PostMapping("/barCodePay")
    public SilkTraderPayResponse barCodePay(@RequestBody PaymentBarCodePayDTO dto, HttpServletRequest request){
        log.info("{}",dto);
        SilkTraderPayResponse response = new SilkTraderPayResponse();
        response.setCode("10000");
        response.setMsg("SUCCESS");
        dto.setPid(request.getHeader("pid"));
        dto.setAppId(request.getHeader("appId"));
        dto.setToken(request.getHeader("wallet-auth-token"));
        MessageResult result = walletQRPayService.barCodePay(dto,request.getHeader("wallet-auth-token"));
        if(!result.isSuccess()){
            rpcErrorCodeMapper(response,result);
        } else {
            response.setBody(JSON.toJSONString(result.getData()));
        }
        return response;
    }

    /**
     * 远程调用失败错误代码映射
     * @param response
     * @param messageResult
     */
    private void rpcErrorCodeMapper(SilkTraderPayResponse response,MessageResult messageResult){
        response.setCode(ExceptionCode.BUSINESS_EXCEPTION_CODE);
        response.setMsg(ExceptionCode.BUSINESS_EXCEPTION_CODE_MESSAGE);
        String subCode = messageResult.getMessage();
        String msg = subCode;
        if(messageResult.getCode() == 500 || messageResult.getCode() >= 9000 || messageResult.getCode() == 429
                || messageResult.getCode() == 999 || messageResult.getCode() == 4000 || messageResult.getCode() == 6000
                || messageResult.getCode() == 3000){
            subCode = ExceptionCode.ISP_UNKNOWN_ERROR;
            //msg = ResponseException.SPQ_UNKNOWN_ERROR.getName();
            response.setSubMsg(msg);
        } else {
            if(isContainChinese(msg)){
                subCode = ExceptionCode.ISP_UNKNOWN_ERROR;
                //msg = ResponseException.SPQ_UNKNOWN_ERROR.getName();
                response.setSubMsg(msg);
            }else {
                Optional<SilkpayMessageCode> silkpayMessageCode = SilkpayMessageCode.convertToOptionalMessageCode(messageResult.getMessage());
                response.setSubMsg(silkpayMessageCode.get().getDesc());
            }
        }
        response.setSubCode("ISP." + subCode);
    }

    public static boolean isContainChinese(String str) {
        Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }


    /**
     * 条形码生成
     * @param dto
     * @param request
     * @return
     */
    @ApiOperation(value = "付款码申请")
    @ApiRequestLimit(count = 10000)
    //@Decrypt(decryptType = EncryptType.AES)
    @PostMapping("/barCodeGenerate")
    public SilkTraderPayResponse barCodeGenerate(@RequestBody PaymentBarCodeGenerateDTO dto, HttpServletRequest request){
        SilkTraderPayResponse response = new SilkTraderPayResponse();
        response.setCode("10000");
        response.setMsg("SUCCESS");
        dto.setPid(request.getHeader("pid"));
        dto.setAppId(request.getHeader("appId"));
        dto.setToken(request.getHeader("wallet-auth-token"));
        //生成条形码
        PayPaymentCodeStorage payPaymentCodeStorage =  payPaymentCodeStorage = walletQRPayService.barCodeGenerate(dto);
        response.setBody(JSON.toJSONString(payPaymentCodeStorage));
        return response;
    }

    @GetMapping("/test")
    public String test(){

        return "hello";
    }

    /**
     * 根据授权码查询条码信息
     * @param authCode
     * @return
     */
    @RequestMapping(value = "/payQuery", method={RequestMethod.GET, RequestMethod.POST})
    public SilkTraderPayResponse payQuery(String accountId, String authCode){
        if(StringUtils.isEmpty(authCode)){
            throw new RequiredArgException("ISP.MISSING-AUTH-CODE","缺少authCode参数");
        }
        if(StringUtils.isEmpty(accountId)){
            throw new RequiredArgException("ISP.MISSING-ACCOUNT-ID","缺少accountId参数");
        }
        SilkTraderPayResponse response = new SilkTraderPayResponse();
        response.setCode("10000");
        response.setMsg("SUCCESS");
        PayPaymentCodeStorage payPaymentCodeStorage = walletQRPayService.selectPaymentCodeByAuthCode(authCode);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("payStatus",payPaymentCodeStorage.getStatus().toString());
        jsonObject.put("accountId",accountId);
        jsonObject.put("authCode",authCode);
        response.setBody(jsonObject.toJSONString());
        return response;
    }


    /**
     * 扫码抢支付
     * @param request
     * @return
     */
    @ApiRequestLimit(count = 10000)
    //@Decrypt(decryptType = EncryptType.AES)
    @PostMapping("/barCodeScannerPay")
    public SilkTraderPayResponse barCodeScannerPay(String accountId,
                                                   String authCode,
                                                   String payType,
                                                   String amount,
                                                   String symbol,
                                                   String scanScene,
                                                   HttpServletRequest request){

        SilkTraderPayResponse response = new SilkTraderPayResponse();
        response.setCode("10000");
        response.setMsg("SUCCESS");
        PaymentBarCodePayDTO dto = new PaymentBarCodePayDTO();
        dto.setAccountId(accountId);
        dto.setAmount(new BigDecimal(amount));
        dto.setAuthCode(authCode);
        dto.setScanScene(scanScene);
        dto.setSymbol(symbol);
        dto.setPayType(payType);
        dto.setPid(request.getHeader("pid"));
        dto.setAppId(request.getHeader("appId"));
        MessageResult result = walletQRPayService.barCodeScannerPay(dto);
        if(result.isSuccess()){
            response.setBody(JSON.toJSONString(result.getData()));
        } else {
            rpcErrorCodeMapper(response,result);
        }
        return response;
    }



}
