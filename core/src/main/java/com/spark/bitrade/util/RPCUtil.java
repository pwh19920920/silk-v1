package com.spark.bitrade.util;

import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.CoinBase;
import com.spark.bitrade.entity.InterfaceLog;
import com.spark.bitrade.service.InterfaceLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 访问rpc的工具类
 * @author tansitao
 * @time 2018.05.01 14:04
 */
public class RPCUtil {
    private Logger logger = LoggerFactory.getLogger(RPCUtil.class);
    public static String coins = "ETH,TEBK,USDT,TCZ,BTMC";
    public static final String  balance = "balance";
    public static final String coinBase = "coinBase";

   /**
    * 调用rpc coinBase接口
    * @author tansitao
    * @time 2018/5/2 14:17 
    * @param restTemplate
    * @param url rpc地址
    * @param account 用户账号
    */
    public boolean callRpcCoinBase(InterfaceLogService interfaceLogService, RestTemplate restTemplate, String url, BigDecimal needCoinaNum)
    {
        boolean isEnough = false;
        CoinBase coinBase = null;
        ResponseEntity<MessageResult> result = null;
        try
        {
            result = restTemplate.getForEntity(url, MessageResult.class);
            logger.info("===============remote call:url={},result={}==================", url, result);

            if (result.getStatusCode().value() == 200)
            {
                MessageResult mr = result.getBody();
                logger.info("==========调用RPC后返回的结果{}=========", mr.getCode()+ "===" + mr.getMessage());
                if (mr.getCode() == 0)
                {
                    //返回用户余额成功，调用持久化
                    String res = JSONObject.toJSONString(mr.getData());
                    coinBase = JSONObject.parseObject(res,CoinBase.class);
                    if(coinBase.getBalance().compareTo(needCoinaNum) > 0)
                    {
                        isEnough = true;
                    }
                }
            }
        }
        catch (Exception e)
        {

            logger.error("========调用RPC地址失败======" + url,e);
        }
        finally
        {
            InterfaceLog interfaceLog = new InterfaceLog();
            interfaceLog.setUrl(url);
            interfaceLog.setRemark("RPC-GetBanlance-coinBase");
            if(result == null)
            {
                interfaceLog.setResponseParam("404");
            }
           else
            {
                interfaceLog.setResponseParam(result.getBody().toString());
            }
            interfaceLog.setReqestTime(new Date());
            interfaceLogService.saveLog(interfaceLog);
            return isEnough;
        }
    }


    /**
      * 调用rpc balance接口
      * @author tansitao
      * @time 2018/5/2 14:17 
     * @param restTemplate
     * @param url rpc地址
     * @param account 用户账号
     */
    public boolean callRpcBalance(InterfaceLogService interfaceLogService, RestTemplate restTemplate, String url, BigDecimal needCoinaNum)
    {
        boolean isEnough = false;
        ResponseEntity<MessageResult> result = null;
        try
        {
            result = restTemplate.getForEntity(url, MessageResult.class);
            logger.info("===============remote call:url={},result={}==================", url, result);

            if (result.getStatusCode().value() == 200)
            {
                MessageResult mr = result.getBody();
                logger.info("==========调用RPC后返回的结果{}=========", mr.getCode()+ "===" + mr.getMessage());
                if (mr.getCode() == 0)
                {
                    if(BigDecimal.valueOf(Double.parseDouble(mr.getData() + "")).compareTo(needCoinaNum) > 0)
                    {
                        isEnough = true;
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("========调用RPC地址失败======" + url,e);
        }
        finally
        {
            InterfaceLog interfaceLog = new InterfaceLog();
            interfaceLog.setUrl(url);
            interfaceLog.setRemark("RPC-GetBanlance-balance");
            if(result == null)
            {
                interfaceLog.setResponseParam("404");
            }
            else
            {
                interfaceLog.setResponseParam(result.getBody().toString());
            }
            interfaceLog.setReqestTime(new Date());
            interfaceLogService.saveLog(interfaceLog);
            return isEnough;
        }
    }

    /**
     * 判断平台该币余额是否足够
     * @author tansitao
     * @time 2018/5/2 14:19 
     * @param
     */
    public boolean balanceIsEnough(InterfaceLogService interfaceLogService, RestTemplate restTemplate, Coin coin, BigDecimal needCoinaNum)
    {
        boolean isEnough;
        //判断该币是否在为coins里面存放的币种
        if(RPCUtil.coins.indexOf(coin.getUnit()) != -1)
        {
            logger.info("==========调用RPC的接口：=========" +RPCUtil.coinBase);
            if("ETH".equalsIgnoreCase(coin.getBaseCoinUnit())  && !Objects.equals(coin.getUnit(),"ETC")){
                isEnough = callRpcCoinBase(interfaceLogService, restTemplate, "http://SERVICE-RPC-ETH/rpc/" + RPCUtil.coinBase+"?coinUnit="+coin.getUnit(), needCoinaNum);
            }else if("SLU".equalsIgnoreCase(coin.getBaseCoinUnit())){
                isEnough = callRpcCoinBase(interfaceLogService, restTemplate, "http://SERVICE-RPC-SLU/rpc/" + RPCUtil.coinBase+"?coinUnit="+coin.getUnit(), needCoinaNum);
            }else{
                isEnough = callRpcCoinBase(interfaceLogService, restTemplate, "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/" + RPCUtil.coinBase, needCoinaNum);
            }

        }
        else
        {

            isEnough = callRpcBalance(interfaceLogService, restTemplate, "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/" + RPCUtil.balance, needCoinaNum);

        }
        return isEnough;
    }

    /**
     * 归集钱包某币
     * @author tansitao
     * @time 2018/5/2 14:23 
     * @param
     */
    public void collectCoin(InterfaceLogService interfaceLogService, RestTemplate restTemplate, Coin coin)
    {
        if(RPCUtil.coins.indexOf(coin.getUnit()) == -1)
        {
            logger.info("===============" + coin.getUnit() + "不需要归集==================");
            return;
        }
        ResponseEntity<MessageResult> result = null;
        String url = null;
        if("ETH".equalsIgnoreCase(coin.getBaseCoinUnit())  && !Objects.equals(coin.getUnit(),"ETC")){
            url = "http://SERVICE-RPC-ETH/rpc/collection?coinUnit="+coin.getUnit();
        }else if("SLU".equalsIgnoreCase(coin.getBaseCoinUnit())){
            url = "http://SERVICE-RPC-SLU/rpc/collection?coinUnit="+coin.getUnit();
        }else{
            url = "http://SERVICE-RPC-"+coin.getUnit()+"/rpc/collection";
        }

        try
        {
            result = restTemplate.getForEntity(url, MessageResult.class);
            logger.info("===============remote call:url={},result={}==================", url, result);
            if(result.getStatusCode().value() == 200)
            {
                MessageResult mr = result.getBody();
                logger.info("==========归集钱包" + coin.getUnit() + "成功=========");
            }
        }
        catch (Exception e)
        {
            logger.error("========调用RPC地址失败======" + url,e);
        }
        finally
        {
            InterfaceLog interfaceLog = new InterfaceLog();
            interfaceLog.setUrl(url);
            interfaceLog.setRemark("RPC-collect");
            if(StringUtils.isEmpty(result))
            {
                interfaceLog.setResponseParam("404");
            }
            else
            {
                interfaceLog.setResponseParam(result.getBody().toString());
            }
            interfaceLog.setReqestTime(new Date());
            interfaceLogService.saveLog(interfaceLog);
        }
    }
}
