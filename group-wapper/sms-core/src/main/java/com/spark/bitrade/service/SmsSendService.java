package com.spark.bitrade.service;

//import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.vendor.provider.SMSProviderProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
  * 短信发送service
  * @author tansitao
  * @time 2018/7/13 11:24 
  */
@Service
@Slf4j
//public class SmsSendService extends BaseService {
public class SmsSendService {
    @Autowired
    private SMSProviderProxy smsProvider;

    /**
      * 发送国内短信
      * @author tansitao
      * @time 2018/7/13 11:39 
      */
    @Async
    public void sendVerifyMessage(String phone, String randomCode)
    {
        try
        {
            smsProvider.sendVerifyMessage(phone, randomCode);
        }
        catch (Exception e)
        {
            log.error("====发送短信异常====", e);
        }

    }

    /**
      * 发送国际短信
      * @author tansitao
      * @time 2018/7/13 11:39 
      */
    @Async
    public void sendInternationalMessage(String randomCode, String phone)
    {
        try
        {
            smsProvider.sendInternationalMessage(randomCode,  phone);
        }
        catch (Exception e)
        {
            log.error("====发送短信异常====", e);
        }

    }

    /**
     * 发送繁体短信
     * @author tansitao
     * @time 2018/9/12 17:06 
     */
    @Async
    public void sendComplexMessage(String randomCode, String phone)
    {
        try
        {
            smsProvider.sendComplexMessage(randomCode,  phone);
        }
        catch (Exception e)
        {
            log.error("====发送短信异常====", e);
        }

    }

    /**
     * 批量发短信
     * @param randomCode
     * @param phone
     */
    @Async
    public int batchSend(String randomCode, String phone)
    {
        int i=0;
        try
        {
            smsProvider.batchSend(phone,randomCode);
        }
        catch (Exception e) {
            log.error("====发送短信异常====", e);
            i=-1;
        }
        return i;
    }
}
