package com.spark.bitrade.vendor.provider.support;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.spark.bitrade.util.HttpSend;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.vendor.provider.SMSProvider;
import com.yunpian.sdk.YunpianClient;
import com.yunpian.sdk.model.Result;
import com.yunpian.sdk.model.VoiceSend;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author tansitao
 * @time 2018-04-05
 * 云片短信验证码发送类
 */
@Slf4j
public class YunpianSMSProvider implements SMSProvider {

    private String gateway;
    private String apikey;
    public static int RESEND_TIMES = 2;
    private List<Integer> retryCode = Arrays.asList(-50, -51, -53);
    public YunpianSMSProvider(String gateway, String apikey) {
        this.gateway = gateway;
        this.apikey = apikey;
    }

    private static Pattern RESPONSE_PATTERN = Pattern.compile("<response><error>(-?\\d+)</error><message>(.*[\\\\u4e00-\\\\u9fa5]*)</message></response>");


    public static String getName() {
        return "yunpian";
    }

    @Override
    public MessageResult sendSingleMessage(String mobile, String content) throws UnirestException {
        int sendTimes = 0;
        //发送短信
        MessageResult result = sendOneMessage(mobile, content);
        //如果失败，继续发送直到成功或者重发次数超过3次
        while (retryCode.contains(result.getCode()) && sendTimes < YunpianSMSProvider.RESEND_TIMES) {
            log.info("第{}次重发=>{} : {}", sendTimes, mobile, content);
            sendTimes++;
            result = sendOneMessage(mobile, content);
            try {
                Thread.sleep(10000);
            } catch (Exception ignored) {
            }
        }
        return result;

    }

    /**
      * 发送一条短信
      * @author tansitao
      * @time 2018/7/20 14:27 
      */
    public MessageResult sendOneMessage(String mobile, String content) throws UnirestException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("apikey", apikey);
        params.put("text", content);
        params.put("mobile", mobile);
        log.info("yunpianParameters==== {}", params.toString());
        String resultXml= HttpSend.yunpianPost(gateway, params);
        log.info("result = {}", resultXml);
        return parseXml(resultXml);
    }

    @Override
    public MessageResult sendInternationalMessage(String content, String mobile) throws IOException, DocumentException {
        content = String.format("【SilkTrader】Your verification code is %s, Please fill in according the instructions. Do not reveal it to others.", content);
        Map<String, String> params = new HashMap<String, String>();
        params.put("apikey", apikey);
        params.put("text", content);
        params.put("mobile", mobile);
        log.info("yunpianParameters==== {}", params.toString());
        String resultXml= HttpSend.yunpianPost(gateway, params);
        log.info("result = {}", resultXml);
        return parseXml(resultXml);
    }

    /**
      * 发送繁体短信
      * @author tansitao
      * @time 2018/9/12 15:46 
      */
    @Override
    public MessageResult sendComplexMessage(String content, String mobile) throws IOException, DocumentException {
        content = String.format("【SilkTrader】您的驗證碼為：%s，請按頁面提示填寫，切勿泄露於他人。", content);
        Map<String, String> params = new HashMap<String, String>();
        params.put("apikey", apikey);
        params.put("text", content);
        params.put("mobile", mobile);
        log.info("yunpianParameters==== {}", params.toString());
        String resultXml= HttpSend.yunpianPost(gateway, params);
        log.info("result = {}", resultXml);
        return parseXml(resultXml);
    }

    /**
     * 批量发送短信,相同内容多个号码,智能匹配短信模板
     * * @author lingxing
      * @time 2018/08/02 09:27 
     * @param text   需要使用已审核通过的模板或者默认模板
     * @param mobile 接收的手机号,多个手机号用英文逗号隔开
     * @return json格式字符串
     */
    @Override
    public  MessageResult batchSend( String  mobile,String text) {
        Map<String, String> params = new HashMap();//请求参数集合
        params.put("apikey", apikey);
        params.put("text", text);
        params.put("mobile", mobile);
        String resultXml=HttpSend.yunpianPost(gateway,params);
        int sendTimes = 0;
        //如果失败，继续发送直到成功或者重发次数超过3次
        MessageResult result= parseXml(resultXml);
        while(result.getCode() != 0 && sendTimes < YunpianSMSProvider.RESEND_TIMES){
            sendTimes++;
            batchSend(mobile,text);
        }
        return result;//请自行使用post方式请求,可使用Apache HttpClient
    }
    /**
     * 获取验证码信息格式
     *
     * @param code
     * @return
     */
    @Override
    public String formatVerifyCode(String code) {
        return String.format("【SilkTrader】您的验证码为：%s，请按页面提示填写，切勿泄露于他人。", code);
    }

    @Override
    public MessageResult sendVerifyMessage(String mobile, String verifyCode) throws Exception {
        String content = formatVerifyCode(verifyCode);
        return sendSingleMessage(mobile, content);
    }

    @Override
    public MessageResult sendMessage(String mobile, String verifyCode) throws Exception {
        return sendSingleMessage(mobile, verifyCode);
    }

    private MessageResult parseXml(String xml) {
        JSONObject myJsonObject = JSONObject.fromObject(xml);
        int code = myJsonObject.getInt("code");
        MessageResult result = new MessageResult(500, "系统错误");
        if(code == 0){
            result.setCode(code);
            result.setMessage(myJsonObject.getString("msg"));
        }else{
            result.setMessage(myJsonObject.getString("msg"));
            log.info("======短信发送失败{}=====", xml);
        }
        return result;
    }

    /**
     * 语音验证码
     * @author fumy
     * @time 2018.08.09 9:56
     * @param mobile
     * @param voiceCode
     * @return true
     */
    @Override
    public MessageResult sendVoiceCode(String mobile,String voiceCode){
        //初始化clnt,使用单例方式
        YunpianClient clnt = new YunpianClient(apikey).init();

        //发送语音验证码API
        Map<String, String> param = clnt.newParam(2);
        param.put(YunpianClient.MOBILE, mobile);
        param.put(YunpianClient.CODE, voiceCode);   //验证码，4-6位阿拉伯数字
        Result<VoiceSend> r = clnt.voice().send(param);
        //获取返回结果，返回码:r.getCode(),返回码描述:r.getMsg(),API结果:r.getData(),其他说明:r.getDetail(),调用异常:r.getThrowable()

        //账户:clnt.user().* 签名:clnt.sign().* 模版:clnt.tpl().* 短信:clnt.sms().* 语音:clnt.voice().* 流量:clnt.flow().* 隐私通话:clnt.call().*

        //释放clnt
        clnt.close();
        log.info("【发送语音验证码】-->发送号码={}---------->返回结果={}",mobile,r.toString());
        if(r.getCode()==0){//发送成功
            return MessageResult.success(r.toString());
        }else{
            return MessageResult.error("语音验证码发送失败");
        }
    }

    /**
     * 语音通知（暂未开通，未配置模板ID）
     * @author fumy
     * @time 2018.08.09 10:09
     * @param mobile
     * @param tpl_value
     * @return true
     */
    public MessageResult voiceNotifiy(String mobile,String tpl_value){
        String tpl_id = "";
        //初始化clnt,使用单例方式
        YunpianClient clnt = new YunpianClient(apikey).init();

        //发送语音验证码API
        Map<String, String> param = clnt.newParam(3);
        param.put(YunpianClient.MOBILE, mobile);
        param.put(YunpianClient.TPL_ID,tpl_id);//审核通过的模板ID,如：1136
        param.put(YunpianClient.TPL_VALUE, tpl_value);//语音通知的变量值
        Result<VoiceSend> r = clnt.voice().tpl_notify(param);
        //获取返回结果，返回码:r.getCode(),返回码描述:r.getMsg(),API结果:r.getData(),其他说明:r.getDetail(),调用异常:r.getThrowable()

        //账户:clnt.user().* 签名:clnt.sign().* 模版:clnt.tpl().* 短信:clnt.sms().* 语音:clnt.voice().* 流量:clnt.flow().* 隐私通话:clnt.call().*

        //释放clnt
        clnt.close();

        return MessageResult.success(r.toString());
    }


    public static void main(String[] args) {
        //初始化clnt,使用单例方式
        YunpianClient clnt = new YunpianClient("20b2c951cca70b0e4e7ff72ccf868f04").init();

        //发送语音验证码API
        Map<String, String> param = clnt.newParam(2);
        param.put(YunpianClient.MOBILE, "9293241099");
        param.put(YunpianClient.CODE, "123456");   //验证码，4-6位阿拉伯数字
        Result<VoiceSend> r = clnt.voice().send(param);
        //获取返回结果，返回码:r.getCode(),返回码描述:r.getMsg(),API结果:r.getData(),其他说明:r.getDetail(),调用异常:r.getThrowable()

        //账户:clnt.user().* 签名:clnt.sign().* 模版:clnt.tpl().* 短信:clnt.sms().* 语音:clnt.voice().* 流量:clnt.flow().* 隐私通话:clnt.call().*

        //释放clnt
        clnt.close();
        System.out.println(r.toString());
    }
}
