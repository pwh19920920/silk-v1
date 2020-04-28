package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import com.spark.bitrade.chain.ImageCodeValdator;
import com.spark.bitrade.config.WyGeetestConfig;
import com.spark.bitrade.entity.SilkDataDist;
import com.spark.bitrade.service.ISilkDataDistService;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.UUIDUtil;
import com.spark.bitrade.vo.ImageCodeVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.08.02 09:38  
 */
@RestController
@Slf4j
@RequestMapping("random/image")
@Api(description = "图形验证码相关接口")
public class ValidateCodeController {


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DefaultKaptcha defaultKaptcha;

    @Autowired
    private WyGeetestConfig wyGeetestConfig;

    @Autowired
    private ISilkDataDistService silkDataDistService;

    @Resource
    private LocaleMessageSourceService localeMessageSourceService;

    @ApiOperation(value = "生成图形验证码并保存到redis，凡是调用所有邮箱或短信接口均要添加参数  timeKey(后台返回) , imageCode(用户输入) ")
    @RequestMapping(value = "create", method = {RequestMethod.POST, RequestMethod.GET})
    public MessageRespResult<ImageCodeVo> create() throws IOException {
        //生成图形验证码 和 timeKey 并返回
        String code = defaultKaptcha.createText();
        BufferedImage image = defaultKaptcha.createImage(code);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        Base64.Encoder encoder = Base64.getEncoder();

        String baseUrl = "data:image/png;base64," + encoder.encodeToString(outputStream.toByteArray());
        String timeKey = "key" + System.currentTimeMillis();

        ImageCodeVo vo = ImageCodeVo.builder().timeKey(timeKey).imageUrl(baseUrl).build();

        //保存到redis方便后续验证 有效时间3分钟
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set(timeKey, code, 3L, TimeUnit.MINUTES);
        log.info("图形验证码:{}", code);

        return MessageRespResult.success4Data(vo);
    }

    @ApiOperation(value = "验证图形验证码，成功时返回一个UUID，调用接口的时候，需要传此UUID。")
    @RequestMapping(value = "doValidateImageCode", method = {RequestMethod.POST, RequestMethod.GET})
    public MessageRespResult doValidateImageCode(@RequestParam String timeKey, @RequestParam String imageCode) {
        log.info("=================================图形验证码开始=================================");
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        //验证图形验证码

        //之前存入redis的code
        String imageCodeRedis = valueOperations.get(timeKey);
        Assert.hasText(imageCodeRedis, localeMessageSourceService.getMessage("IMAGE_CODE_IS_INVALID"));
        //验证
        Assert.hasText(imageCode, localeMessageSourceService.getMessage("IMAGE_CODE_IS_INVALID"));
        Assert.isTrue(imageCode.equalsIgnoreCase(imageCodeRedis), localeMessageSourceService.getMessage("IMAGE_CODE_IS_CORRECT"));

        valueOperations.getOperations().delete(timeKey);
        log.info("=================================图形验证码验证成功=================================");
        //验证成功之后返回给前端一个标识 并把标识存入redis
        String uuid = UUIDUtil.getUUID();
        valueOperations.set(ImageCodeValdator.VALIDATE_KEY + timeKey, uuid, 3L, TimeUnit.MINUTES);
        return MessageRespResult.success4Data(uuid);
    }


    @ApiOperation(value = "获取阿里极验证Appkey前端用于生成token,返回appKey,validateType: 0: 无验证 1:图形验证码  2:现在用的极验证 3:网易极验证")
    @RequestMapping(value = "getValidateConfig", method = {RequestMethod.POST, RequestMethod.GET})
    public MessageRespResult getValidateConfig() {
        SilkDataDist sk = silkDataDistService.findByIdAndKey("SYSTEM_VALIDATE_CONFIG", "VALIDATE_TYPE");
        Assert.notNull(sk, localeMessageSourceService.getMessage("VALIDATE_TYPE_IS_NULL"));
        JSONObject obj = new JSONObject();
        obj.put("validateType", sk.getDictVal());
        obj.put("wyCaptchaId", wyGeetestConfig.getCaptchaId());
        return MessageRespResult.success4Data(obj);
    }


    @ApiOperation(value = "获取账户密码错误次数")
    @RequestMapping(value = "getAccountErrorCount", method = {RequestMethod.POST, RequestMethod.GET})
    public MessageRespResult getAccountErrorCount(@RequestParam String account) {
        ValueOperations<String, Integer> op = redisTemplate.opsForValue();
        String key = "errorAccount:" + account + ":count";
        Integer integer = op.get(key);
        integer = integer == null ? 0 : integer;
        Integer forbidden = op.get(key + ":forbidden");
        if(forbidden!=null){
            return MessageRespResult.error(4009,localeMessageSourceService.getMessage("ACCOUNT_IS_FORBIDDEN_TWO"));
        }
        return MessageRespResult.success4Data(integer);
    }


    @Bean(name = "captchaProducer")
    public DefaultKaptcha getKaptchaBean() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        properties.setProperty("kaptcha.border", "yes");
        properties.setProperty("kaptcha.border.color", "105,179,90");
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        properties.setProperty("kaptcha.image.width", "125");
        properties.setProperty("kaptcha.image.height", "45");
        properties.setProperty("kaptcha.session.key", "code");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        properties.setProperty("kaptcha.textproducer.font.names", "宋体,楷体,微软雅黑");
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;

    }
}
