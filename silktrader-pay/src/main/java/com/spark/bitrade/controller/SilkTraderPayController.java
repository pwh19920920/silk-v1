package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.SilkPayMainBackStatus;
import com.spark.bitrade.constant.SilkPayOrderStatus;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.dto.SilkTraderContractDTO;
import com.spark.bitrade.entity.MemberTransaction;
import com.spark.bitrade.entity.SilkTraderTransaction;
import com.spark.bitrade.service.ISilkTraderPayService;
import com.spark.bitrade.service.MemberWalletService;
import com.spark.bitrade.util.*;
import com.spark.bitrade.utils.Base64Util;
import com.spark.bitrade.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fumy
 * @time 2018.07.05 13:49
 */
@Controller
@Slf4j
public class SilkTraderPayController {


    @Autowired
    ISilkTraderPayService iSilkTraderPayService;

    @Autowired
    MemberWalletService memberWalletService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 平台账号
     */
    @Value("${silktrader.account}")
    private String SILKTRADER_ACCOUNT;

    /**
     * 验证码发送url
     */
    @Value("${login.code.url}")
    private String LOGIN_CODE_URL;

    /***
     * 商家订单信息确认页面
     * @author fumy
     * @time 2018.07.05 18:47
     * @param paraJson
     * @return true
     */
    @RequestMapping(value = "/customer/auth",method = RequestMethod.POST)
    public String customerInfoAuth(Map<String,Object> params,@RequestBody(required=false) String paraJson) throws UnsupportedEncodingException {
        MessageResult result = new MessageResult();


        System.out.println(paraJson);
        String enJson = paraJson.replace("paraJson=","");

        enJson = URLDecoder.decode(enJson,"UTF-8");
        System.out.println(enJson);

        JSONObject josn= JSON.parseObject(enJson);

        long id = CommonUtils.toLong(josn.get("id")+"");
        //商户的返回跳转url
        String return_url =josn.get("return_url")+"";


        //查询出商家的key,以便解密参数paraJson
        SilkTraderContractDTO stc = iSilkTraderPayService.findContractById(id);
        if(stc == null ){
            result.setCode(1);
            result.setMessage("该商户不是签约认证商户!");
            log.info("【第三方支付】------------------->该商户不是签约认证商户,id:{}",id);
        }else {//如果查询结果不为空，则是签约认证商户

            result.setCode(0);
            result.setMessage("认证通过");
            result.setData(enJson);
            params.put("busi_url",stc.getBusiUrl()==""|| stc.getBusiUrl()==null?"http://www.silktrader.net":stc.getBusiUrl());
            params.put("return_url",return_url);
            params.put("code_url",LOGIN_CODE_URL);
            log.info("【第三方支付】商家订单请求认证通过，自定义返回地址-return_rul:--------------->" + return_url);
        }

        params.put("result",result);
        return "index";
    }

    /**
     * 用户信息-手机验证,并返回生成的支付信息
     * @author fumy
     * @time 2018.07.06 11:57
     * @param params
     * @return true
     */
    @RequestMapping(value = "/customer/phoneAuth",method = RequestMethod.POST)
    @ResponseBody
    public MessageResult phoneAuth(@RequestBody String params){
        MessageResult result = new MessageResult();

        //解码参数
        String key="";
        try {
            params = URLDecoder.decode(params,"UTF-8");
            String[] paraArr = params.split("&");
            Map<String,Object> map = new HashMap<>();
            for(int i =0;i<paraArr.length;i++){
                String[] arr;
                if(i == paraArr.length-1){
                    String ss = paraArr[i].replaceFirst("=","&");
                    arr = ss.split("&");
                }else{
                    arr = paraArr[i].split("=");
                }
                map.put(arr[0],arr[1]);
            }

            String phone = map.get("phone")+"";
            String inCode = map.get("smsCode")+"";
            //商家传入的json参数
            String enJson = map.get("enJson")+"";


            //从redis获取发出的验证码
            ValueOperations valueOperations = redisTemplate.opsForValue();
            //按规则拼装redis存储的key
            key = SysConstant.PHONE_LOGIN_CODE + phone;
            String code = valueOperations.get(key).toString();
            log.info("【第三方支付】已得到短信验证码，进行验证码对比.................");
            //对比验证码输入
            if(!inCode.equals(code)){
                //未通过验证
                result.setCode(1);
                result.setMessage("验证码错误!!!");

            }else {//验证码输入正确，即验证通过，根据手机号查询用户余额信息

                //解码商家的订单信息
                JSONObject josn= JSON.parseObject(enJson);
                log.info("【第三方支付】[josn] ---> {}",josn);
                long id = CommonUtils.toLong(josn.get("id")+"");
                //加密的商家信息
                String orderJson = josn.get("data")+"";
                //查询出商家的key,以便解密参数paraJson
                SilkTraderContractDTO stc = iSilkTraderPayService.findContractById(id);
                //enJson
                //通讯key,解密
                String SKEY = stc.getMessageKey();
                String paramStr = Base64Util.decodeDES(orderJson,SKEY);
                //解析解密的paramStr字符串
                JSONObject sellerMap= JSON.parseObject(paramStr);

                Map<String,Object> userMap = iSilkTraderPayService.getBalanceByPhone(phone,stc.getContractCoin());
                //查询出的资金密码不要返回到前端，移除
                userMap.remove("jy_password");

                log.info("【第三方支付】获取商家签约币种、入账币种的市场价格.................");
                //解析并转换商品信息参数
                //计算应支付的金额 商家订单金额（CNY）/SLB实时价 * 折扣率 = 应付SLB数
                PriceUtil priceUtil = new PriceUtil();
                log.info("sellerMap.get {}",sellerMap.get("orderAmount"));
                BigDecimal orderAmount = new BigDecimal(sellerMap.get("orderAmount")+"");
                orderAmount.setScale(2,BigDecimal.ROUND_HALF_UP);
                BigDecimal discount = stc.getDiscount();
                discount.setScale(3,BigDecimal.ROUND_HALF_UP);

                //计算折扣后的实际金额
                BigDecimal actualAmount = BigDecimalUtils.mul(orderAmount,discount);

                //得到USDT的人民币价格
                BigDecimal usdtPrice = priceUtil.getUSDTPrice(restTemplate);
                //计算商家订单金额对应的USDT币数,人民币/USDT法币价格
                BigDecimal usdtCoins = BigDecimalUtils.div(orderAmount,usdtPrice,9);
                //得到签约币种的USDT价格
                BigDecimal coinPrice = priceUtil.getPriceByCoin(restTemplate,stc.getContractCoin());


                //计算实际用户支付订单金额对应的USDT币数,人民币/USDT法币价格
                BigDecimal actualUsdtCoins = BigDecimalUtils.div(actualAmount,usdtPrice,9);
                //计算得到应付总的SLB币数
                BigDecimal slbCoins = BigDecimalUtils.div(actualUsdtCoins,coinPrice,9);

                //比较得到余额是否充足
                BigDecimal balance = (BigDecimal)userMap.get("balance");
                if(balance.compareTo(slbCoins) == -1){
                    //余额不足
                    sellerMap.put("balanceInfo","可用"+stc.getContractCoin()+"不足");
                }else {
                    //余额充足
                    sellerMap.put("balanceInfo","可用"+stc.getContractCoin()+"充足");
                }

                Map<String,Object> orderInfo = iSilkTraderPayService.exsitsPayOrder(sellerMap.get("orderId").toString(),stc.getId().toString());
                //为null或者空时，生成支付订单
                if(orderInfo == null || orderInfo.isEmpty()){
                    //生成订单
                    SilkTraderTransaction stt = new SilkTraderTransaction();
                    stt.setFee(BigDecimalUtils.mul(usdtCoins,stc.getBusiCoinFeeRate()));
                    stt.setAmount(orderAmount);
                    stt.setActualAmount(actualAmount);
                    stt.setBusiAccount(stc.getBusiAccount());
                    stt.setBusiAmount(usdtCoins);
                    stt.setBusiCurrencyPrice(usdtPrice);
                    stt.setComment("支付订单生成测试");
                    //签约细节id,这里需要修改
                    stt.setContractDetailId(String.valueOf(stc.getId()));
                    //第三方支付平台账号id,平台总账户
                    stt.setMemberId(Long.valueOf(SILKTRADER_ACCOUNT));
                    //商户平台自身订单id
                    stt.setPayId(sellerMap.get("orderId").toString());
                    stt.setSilkOrderNo(DateUtil.getFormatDateTime(new Date(),"yyyyMMddHHmmssSSS-")+stc.getBusiAccount()+"-"+userMap.get("member_id").toString());
                    //顾客账号,就是手机号
                    stt.setUserAccount(phone);
                    //商家自身平台账户id
                    stt.setUserId(sellerMap.get("sellerAccount").toString());
                    //初始化，未支付
                    stt.setStatus(SilkPayOrderStatus.NONPAYMENT);
                    //初始化，退款状态为0
                    stt.setBackStatus(SilkPayMainBackStatus.UNAPPLYBACK);
                    stt.setContractBusiPrice(coinPrice);
                    stt.setContractAmount(slbCoins);
                    stt.setOrderTime(DateUtil.parseDate(sellerMap.get("orderTime").toString(),"yyyy-MM-dd HH:mm:ss"));
                    boolean flage = iSilkTraderPayService.insertNewPayOrder(stt);

                    if(!flage){
                        result.setCode(1);
                        result.setMessage("生成交易订单失败");
                        log.info("【第三方支付】生成支付订单失败.................");
                    }
                    log.info("【第三方支付】生成支付订单成功.................");
                    result.setCode(0);
                    result.setMessage("手机认证通过");
                    sellerMap.put("silkOrderNo",stt.getSilkOrderNo());
                    sellerMap.put("discount",stc.getDiscount());
                    sellerMap.put("coinUnit",stc.getContractCoin());
                    sellerMap.put("expireTime",stc.getExpireTime());
                    result.setData(sellerMap);
                }else {//订单存在时，进行状态判断

                    result.setCode(1);
                    result.setMessage("订单已存在，请勿重复下单");
                    result.setData(sellerMap.get("orderId").toString());
                    log.info("【第三方支付】订单已存在------->商家订单ordderId={},商家签约id={}",sellerMap.get("orderId"),stc.getId().toString());

                }
            }
        } catch (Exception e) {
            log.error("【第三方支付】用户账号信息验证失败：获取验证码失败key={},exception={}",key,e.getStackTrace());
            result.setCode(1);
            result.setMessage("SilkTrader账户信息验证异常!!!");
        }
        return result;
    }



    /**
     * 支付页面
     * @author fumy
     * @time 2018.07.06 11:56
     * @param params
     * @param orderInfo,member  进入页面时所传参数
     * @return true
     */
    @RequestMapping(value = "/customer/payInfo",method = {RequestMethod.POST,RequestMethod.GET})
    public String payInfo(Map<String,Object> params,@RequestParam String orderInfo,@RequestParam String phone) throws UnsupportedEncodingException {
        MessageResult result = new MessageResult();

        orderInfo = URLDecoder.decode(orderInfo,"UTF-8");
        JSONObject simpleOrder= JSON.parseObject(orderInfo);

        SilkTraderTransaction stt = iSilkTraderPayService.findOrderByPayId(simpleOrder.get("silkOrderNo").toString());
        if(stt == null){
            result.setCode(1);
            result.setMessage("订单信息查询错误");
        }else {
            int orderExpireTime = Integer.parseInt(simpleOrder.get("expireTime")+"");
            int exp_time = DateUtil.compareDateSec(new Date(),stt.getCreateTime());
            if(exp_time > orderExpireTime){
                //超时
                result.setCode(6);
                result.setMessage("订单超时");
                result.setData(simpleOrder);
                log.info("【第三方支付】订单已存在------->平台订单id={}",stt.getSilkOrderNo());
            }

            simpleOrder.put("orderId",stt.getPayId());
            simpleOrder.put("silkOrderNo",stt.getSilkOrderNo());
            simpleOrder.put("contractBusiPrice",stt.getContractBusiPrice());
            simpleOrder.put("busiCurrencyPrice",stt.getBusiCurrencyPrice());
            simpleOrder.put("contractAmount",stt.getContractAmount());
            simpleOrder.put("amount",stt.getAmount());

            result.setCode(0);
            result.setMessage("认证通过");
            result.setData(simpleOrder);

            if(orderExpireTime==-1){
                //超时时间=-1时,不限时
                params.put("expireTime", orderExpireTime);
            }else {
                params.put("expireTime", (orderExpireTime-exp_time));
            }
            params.put("result", result);
            params.put("resultForString",simpleOrder.toJSONString());
            params.put("orderInfo", stt.getSilkOrderNo());
            params.put("phone", phone);
            params.put("busi_url",simpleOrder.get("busi_url"));
            params.put("return_url",simpleOrder.get("return_url"));
        }

        return "pay";
    }


    /**
     * 用户资金密码验证,完成支付操作，并异步录入对应数据
     * @author fumy
     * @time 2018.07.06 14:21
     * @param params
     * @return true
     */
    @RequestMapping(value = "/customer/pwdAuth",method = RequestMethod.POST)
    @ResponseBody
    public MessageResult pwdAuth(@RequestBody(required = false) String params){
        MessageResult result = new MessageResult();
        try {
            //解析参数
            params = URLDecoder.decode(params,"UTF-8");
            String[] paraArr = params.split("&");
            Map<String,Object> map = new HashMap<>();
            for(int i =0;i<paraArr.length;i++){
                String[] arr = paraArr[i].split("=");
                map.put(arr[0],arr[1]);
            }

            String phone = map.get("phone")+"";
            String jypwd = map.get("jypwd")+"";
            String silkOrderNo = map.get("orderInfo")+"";

            SilkTraderTransaction stt = iSilkTraderPayService.findOrderByPayId(silkOrderNo);
            SilkTraderContractDTO stc = iSilkTraderPayService.findContractById(CommonUtils.toLong(stt.getContractDetailId()));

            //查询用户信息
            Map<String,Object> user = iSilkTraderPayService.getBalanceByPhone(phone,stc.getContractCoin());
            log.info("【第三方支付】查询用户余额.....................user={}",user.toString());
            //验证余额
            BigDecimal balance = (BigDecimal)user.get("balance");
            //比较余额与实际应付币数大小，大于则进行交易
            if(BigDecimalUtils.compare(balance,stt.getContractAmount())){
                //验证资金密码
                String jypassword = new SimpleHash("md5", jypwd, user.get("salt").toString(), 2).toHex().toLowerCase();
                if( jypassword.equals(user.get("jy_password")+"") ){

                    Map<String,Object> payStatus = iSilkTraderPayService.exsitsPayOrder(stt.getPayId(),stt.getContractDetailId());
                    if(payStatus == null || payStatus.isEmpty()){
                        result.setCode(1);
                        result.setMessage("订单不存在");
                        log.info("【第三方支付】订单不存在.....................");
                    }else {
                        if("0".equals(payStatus.get("status").toString())){

                            //如果系统当前时间与订单生成时间的差大于15分钟，则订单超时
                            int exp_time = DateUtil.compareDateSec(new Date(),stt.getCreateTime());
                            if( exp_time > stc.getExpireTime().intValue()){
                                //修改订单支付状态为： 取消
                                result.setCode(6);
                                stt.setStatus(SilkPayOrderStatus.CANCELLED);
                                boolean isSucc =iSilkTraderPayService.updatePayOrderStatus(stt);
                                if(isSucc){
                                    result.setMessage("订单超时，已自动取消");
                                }else {//订单超时未自动取消成功
                                    result.setMessage("订单超时");
                                }
                                log.info("【第三方支付】订单超时------->平台订单id={}",stt.getSilkOrderNo());
                                return result;
                            }

                            //如果订单在可支付时间内，修改订单支付状态为： 支付中
                            stt.setStatus(SilkPayOrderStatus.PAYING);
                            boolean isSucc =iSilkTraderPayService.updatePayOrderStatus(stt);

                            //修改成功，调用异步执行支付
                            if(isSucc){
                                //异步调用
                                getService().callBack(stt, user, stc);
                            }

                            result.setCode(0);
                            result.setMessage("密码验证通过");
                        }else {
                            if("3".equals(payStatus.get("status").toString())){
                                //订单状态为 3 = 支付中
                                result.setCode(4);
                                result.setMessage("订单正在支付中，请勿重复提交");
                                log.info("【第三方支付】订单正在支付中------->平台订单orderId={}",stt.getSilkOrderNo());
                            }else {
                                result.setCode(5);
                                result.setMessage("订单已支付或已取消");
                                log.info("【第三方支付】订单已支付或已取消------->平台订单orderId={}",stt.getSilkOrderNo());
                            }
                        }

                    }

                }else {
                    result.setCode(2);
                    result.setMessage("资金密码错误");
                    log.info("【第三方支付】资金密码错误.....................memberId={}",user.get("member_id").toString());
                }
            }else {
                result.setCode(3);
                result.setMessage("账户余额不足");
                log.info("【第三方支付】账户余额不足.....................memberId={}",user.get("member_id").toString());
            }


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 支付结果页面
     * @author fumy
     * @time 2018.07.06 13:48
     * @param params
     * @param simpleOrder 简单订单信息
     * @return true
     */
    @RequestMapping(value = "/customer/coinPay",method = {RequestMethod.POST,RequestMethod.GET})
    public String coinPay(Map<String,Object> params,@RequestParam String simpleOrder,@RequestParam String busiUrl,@RequestParam String returnUrl) throws UnsupportedEncodingException {
        MessageResult result = new MessageResult();

        simpleOrder = URLDecoder.decode(simpleOrder,"UTF-8");
        JSONObject data= JSON.parseObject(simpleOrder);

        result.setCode(0);
        result.setMessage("支付成功");
        result.setData(data);

        params.put("result",result);
        params.put("busi_url",busiUrl);
        params.put("return_url",returnUrl);
        return "payResult";
    }

    /**
     * 支付失败结果页面
     * @author fumy
     * @time 2018.07.06 13:48
     * @param params
     * @param simpleOrder 简单订单信息
     * @param code 返回错误码
     * @return true
     */
    @RequestMapping(value = "/customer/coinPayFail",method = {RequestMethod.POST,RequestMethod.GET})
    public String coinPayFail(Map<String,Object> params,@RequestParam String simpleOrder,@RequestParam String code,
                              @RequestParam String busiUrl,@RequestParam String returnUrl) throws UnsupportedEncodingException {
        MessageResult result = new MessageResult();

        simpleOrder = URLDecoder.decode(simpleOrder,"UTF-8");
        JSONObject data= JSON.parseObject(simpleOrder);
        //根据不同的错误码，设置不同的返回消息
        switch (code){
            case "1" :
                result.setCode(1);
                result.setMessage("支付失败");
                break;
            case "2" :
                result.setCode(1);
                result.setMessage("资金密码错误");
                break;
            case "3" :
                result.setCode(1);
                result.setMessage("余额不足");
                break;
            case "4" :
                result.setCode(1);
                result.setMessage("订单支付中，请勿重复提交");
                break;
            case "5" :
                result.setCode(1);
                result.setMessage("订单已支付或已取消");
                break;
            case "6" :
                result.setCode(1);
                result.setMessage("订单超时，已取消支付");
                break;
            default:break;
        }

        result.setData(data);
        params.put("result",result);
        params.put("busi_url",busiUrl);
        params.put("return_url",returnUrl);
        return "payResult_Fail";
    }




    /**
     * 测试入口
     * @param params
     * @return
     */
    @RequestMapping(value = "/customer/test",method = {RequestMethod.POST,RequestMethod.GET})
    public String customerInfoAuthtest(Map<String,Object> params){
        MessageResult result = new MessageResult();

        //简单模拟订单id
        String orderId = "test"+DateUtil.getTimeMillis();

        JSONObject map = new JSONObject();
        map.put("orderName","BCE");
        map.put("orderId",orderId);
        map.put("orderTime","2018-07-02 13:45:36");
        map.put("sellerNick","商家昵称");
        //订单金额
        map.put("orderAmount","100");
        //商家自身账号
        map.put("sellerAccount","2008");


        String enJson = Base64Util.encodeDES(map.toString(),"Silktrader#2018$");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",1);
        jsonObject.put("data",enJson);
        jsonObject.put("return_url","https://www.baidu.com");

        System.out.println(enJson);
        result.setCode(0);
        result.setMessage("认证通过");
        result.setData(jsonObject.toJSONString());

        params.put("result",result);
        params.put("busi_url","http://www.silktrader.net");
        params.put("return_url",jsonObject.get("return_url")+"");
        params.put("code_url",LOGIN_CODE_URL);
        return "index";
    }

    /**
     * 异步回调测试入口
     * @param params
     * @return
     */
    @RequestMapping(value = "/customer/callback",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public MessageResult customerCallback(@RequestBody String params){
        MessageResult result = new MessageResult();

        String enJson = Base64Util.encodeDES(params,"Silktrader#2018$");
        System.out.println("异步通知请求参数：----------->"+enJson);

        result.setCode(0);
        result.setMessage("异步调用成功，订单支付完成");

        return result;
    }


    /**
     * 把传入的map格式字符串转换为map对象（商品信息参数）
     * @author fumy
     * @time 2018.07.07 15:11
     * @param mapString
     * @return true
     */
    public static Map<String,Object> mapStringToMap(String mapString) {
        mapString = mapString.replace("{","");
        mapString = mapString.replace("}","");
        String[] paraArr = mapString.split(",");
        Map<String,Object> map = new HashMap<>();
        for(int i =0;i<paraArr.length;i++){
            String[] arr = paraArr[i].split("=");
            map.put(arr[0],arr[1]);
        }
        return  map;
    }



    @Async
    public void callBack(SilkTraderTransaction stt, Map<String,Object> user, SilkTraderContractDTO stc) {
        System.out.println("进入异步通知调用");
        boolean isSucc = getService().pay(stt,user,stc);
        int times =0;
        //订单状态修改成功，表示支付已完成，调用商家异步通知url
        if(isSucc){
            boolean flg = payOrderSellerCallBack(stc.getAsyncNotifyUrl(),stt,stc.getMessageKey());

            while (!flg) {
                //异步返回false,休眠五秒重调
                try {
                    log.info("===============商家异步回调通知返回失败，5秒后重新调用通知url=============");
                    Thread.sleep(5000);
                    times++;
                    if(times*5000<=30000){
                        log.info("===============商家异步回调通知,第"+times+"次调用=============");
                        flg = payOrderSellerCallBack(stc.getAsyncNotifyUrl(),stt,stc.getMessageKey());
                    }else{
                        log.info("===============商家异步回调通知重新调用超过30秒，停止通知=============");
                        flg =true;
                    }
                    if (flg) {break;}

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        log.info("退出异步通知调用");

    }

    /**
     * 支付扣款
     * @param stt
     * @param user
     * @param stc
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean pay(SilkTraderTransaction stt, Map<String,Object> user, SilkTraderContractDTO stc){
        log.info("========开始更新用户钱包余额，并记录手续费记录------------------------------>");
        MemberTransaction mt = new MemberTransaction();
        //扣除用户余额 实际支付的SLB
        long userWalletId = iSilkTraderPayService.getWalletIdByMemberId(user.get("member_id").toString(),stc.getContractCoin());
        //减少钱包余额
        memberWalletService.decreaseBalance(userWalletId,stt.getContractAmount());

        //顾客用户手续费记录
        //扣除手续费的支付总额为负数
        mt.setAmount(stt.getContractAmount().negate());
        //顾客用户手续费为0
        mt.setFee(new BigDecimal(0));
        mt.setFeeDiscount(new BigDecimal(0));
        //顾客用户id
        mt.setMemberId(Long.valueOf(user.get("member_id").toString()));
        mt.setSymbol("SLB");
        mt.setRefId(stt.getSilkOrderNo());
        mt.setType(TransactionType.THIRD_PAY);
        iSilkTraderPayService.insertFeeTransaction(mt);

        //silktrader 账户余额增加 实际支付的SLB
        long skWalletId = iSilkTraderPayService.getWalletIdByMemberId(SILKTRADER_ACCOUNT,stc.getContractCoin());
        //增加余额
        memberWalletService.increaseBalance(skWalletId,stt.getContractAmount());

        //silktrader平台账号用户手续费记录
        mt.setAmount(stt.getContractAmount());
        mt.setFee(new BigDecimal(0));
        mt.setFeeDiscount(new BigDecimal(0));
        //silktrader平台账号用户id
        mt.setMemberId(Long.valueOf(SILKTRADER_ACCOUNT));
        mt.setSymbol("SLB");
        mt.setRefId(stt.getSilkOrderNo());
        mt.setType(TransactionType.THIRD_PAY);
        iSilkTraderPayService.insertFeeTransaction(mt);

        //silktrader账户扣除 对等实际支付SLB数量的USDT
        // 扣除/增加的余额 = 平台实际扣除总额/商家实际增加的总额（顾客支付的总额 -/+ 手续费）
        BigDecimal busiAmount = BigDecimalUtils.sub(stt.getBusiAmount(),stt.getFee());
        long skWalletId2 = iSilkTraderPayService.getWalletIdByMemberId(SILKTRADER_ACCOUNT,stc.getBusiCoin());
        //减少钱包余额
        memberWalletService.decreaseBalance(skWalletId2,busiAmount);

        //支付交易手续费记录 ，平台silketrader与商家交易产生的手续费 在商家账户入账记录中 体现扣除
        //交易总额 = 未扣除手续的总额
        mt.setAmount(stt.getBusiAmount().negate());
        mt.setFee(new BigDecimal(0));
        mt.setFeeDiscount(new BigDecimal(0));
        //silktrader平台账号用户id
        mt.setMemberId(Long.valueOf(SILKTRADER_ACCOUNT));
        mt.setSymbol("USDT");
        mt.setRefId(stt.getSilkOrderNo());
        mt.setType(TransactionType.THIRD_PAY);
        iSilkTraderPayService.insertFeeTransaction(mt);

        //商家账户增加 对等实际支付SLB数量的USDT
        long busiWalletId = iSilkTraderPayService.getWalletIdByMemberId(stt.getBusiAccount(),stc.getBusiCoin());
        //增加的余额：等于silktrader平台账号扣除的余额
        memberWalletService.increaseBalance(busiWalletId,busiAmount);
        //支付交易手续费记录
        //商家实际入账的总额
        mt.setAmount(busiAmount);
        //扣除手续费
        mt.setFee(stt.getFee());
        mt.setFeeDiscount(new BigDecimal(0));
        //商家平台账号用户id
        mt.setMemberId(Long.valueOf(stt.getBusiAccount()));
        mt.setSymbol("USDT");
        mt.setRefId(stt.getSilkOrderNo());
        mt.setType(TransactionType.THIRD_PAY);
        iSilkTraderPayService.insertFeeTransaction(mt);

        //更新支付订单信息状态为已支付
        stt.setStatus(SilkPayOrderStatus.PAID);
        boolean isSucc = iSilkTraderPayService.updatePayOrderStatus(stt);

        log.info("========更新用户钱包余额，并记录手续费记录,支付订单修改完成------------------------------");

        return isSucc;
    }


    /**
     * 调用商家的异步通知url,通知订单支付结果
     * @author fumy
     * @time 2018.07.10 8:50
     * @param url
     * @param stt
     * @param key
     * @return true
     */
    public boolean payOrderSellerCallBack(String url,SilkTraderTransaction stt,String key){

        //不需要返回给商家的订单信息，设置为null
        stt.setId(null);
        stt.setMemberId(null);
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(stt);
        String params = Base64Util.encodeDES(jsonObject.toJSONString(),key);

        String result = HttpUtil.httpURLConnectionPOST(url,params);
        if(result == null || result.isEmpty()){
            log.info("=========商家异步通知url返回结果为{null}=========");
            return false;
        }
        JSONObject resJson = JSON.parseObject(result);
        log.info("=========商家异步通知url返回结果{}=========", resJson.get("code"));
        if(!"0".equals(resJson.get("code"))){
            log.info("===============商家异步回调通知成功，支付订单流程完成=============");
        }
        return true;
    }

    /**
     * 解决异步调用controller加载为空的情况
     * @return
     */
    private SilkTraderPayController getService(){
        return SpringContextUtil.getBean(this.getClass());
    }


}
