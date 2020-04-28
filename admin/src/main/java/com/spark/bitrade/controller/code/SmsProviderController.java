package com.spark.bitrade.controller.code;

import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.Admin;
import com.spark.bitrade.entity.Country;
import com.spark.bitrade.service.CountryService;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.util.GeneratorUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.ValidateUtil;
import com.spark.bitrade.vendor.provider.SMSProvider;
import com.spark.bitrade.vendor.provider.SMSProviderProxy;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.spark.bitrade.util.MessageResult.error;
import static com.spark.bitrade.util.MessageResult.success;

@RestController
@Slf4j
@RequestMapping("/code/sms-provider")
public class SmsProviderController {

    private Logger logger = LoggerFactory.getLogger(SmsProviderController.class);

    @Autowired
    private SMSProviderProxy smsProvider ;

    @Autowired
    private RedisTemplate redisTemplate ;

    @Autowired
    CountryService countryService;

    @Resource
    private LocaleMessageSourceService localeMessageSourceService;


    /**
     * 币种管理 修改币种信息手机验证码
     * @param admin
     * @return
     */
    @RequestMapping(value="/system/coin-revise",method = RequestMethod.POST)
    public MessageResult sendReviseCode(@SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin){
        Assert.notNull(admin,"会话已过期，请重新登录");
        return sendCode(admin.getMobilePhone(),SysConstant.ADMIN_COIN_REVISE_PHONE_PREFIX);
    }

    /**
     * 币币管理 币币设置 手机验证码
     * @param admin
     * @return
     */
    @RequestMapping("/exchange-coin-set")
    public MessageResult sendExchangeCoinSet(@SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin){
        Assert.notNull(admin,"会话已过期，请重新登录");
        return sendCode(admin.getMobilePhone(),SysConstant.ADMIN_EXCHANGE_COIN_SET_PREFIX);
    }

    /**
     * 转入冷钱包 手机验证码
     * @param admin
     * @return
     */
    @RequestMapping("/transfer-cold-wallet")
    public MessageResult sendTransfer(@SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin){
        Assert.notNull(admin,"会话已过期，请重新登录");
        return sendCode(admin.getMobilePhone(),SysConstant.ADMIN_COIN_TRANSFER_COLD_PREFIX);
    }

    /**
     * 后台登录  手机验证码
     * @param phone
     * @return
     */
    @RequestMapping(value="/login",method = RequestMethod.POST)
    public MessageResult send(String phone){
       return sendCode(phone,SysConstant.ADMIN_LOGIN_PHONE_PREFIX);
    }


    private MessageResult sendCode(String phone,String prefix){
        Assert.notNull(/*admin.getMobilePhone()*/phone, "手机号不存在");
        MessageResult result;
        String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        try {
            ValueOperations valueOperations = redisTemplate.opsForValue();
            String key = prefix + phone;
            long expire = valueOperations.getOperations().getExpire(key,TimeUnit.SECONDS) ;
            if(expire<600&&expire>540)
                return error("1分钟内不得重复发送验证码");
            result = smsProvider.sendVerifyMessage(phone, randomCode);
            logger.info("短信验证码:{}",randomCode);
            if (result.getCode() == 0) {
                valueOperations.set(key, randomCode, 10, TimeUnit.MINUTES);
                return success("验证码已发送至手机"+phone);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return error("请求失败");
    }

}
