package com.spark.bitrade.controller.common;

import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.controller.BaseController;
import com.spark.bitrade.entity.Admin;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Zhang Jinwei
 * @date 2017年12月19日
 */
@Component
public class BaseAdminController extends BaseController {

    @Autowired
    private RedisTemplate redisTemplate ;

    protected Admin getAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (Admin) session.getAttribute(SysConstant.SESSION_ADMIN);
    }

    /**
     * 判断手机验证码正确否
     * @param code 验证码
     * @param key redis中的key 前缀+手机号
     * @return
     */
    protected MessageResult checkCode(String code, String key){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object value = valueOperations.get(key);
        if(value==null)
            return error("缓存中不存在该验证码，请重新发送");
        if(!value.toString().equals(code))
            return  error("验证码错误，请重新输入");
        valueOperations.getOperations().delete(key);
        return success("验证码正确");
    }
}
