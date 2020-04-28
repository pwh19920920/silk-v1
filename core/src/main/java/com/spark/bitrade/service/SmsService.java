package com.spark.bitrade.service;

import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.util.GoogleAuthenticatorUtil;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

/**
 * @author fumy
 * @time 2018.10.25 15:53
 */
@Service
@Slf4j
public class SmsService {

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    MemberService memberService;

    /**
     * 手机验证码检查
     * @author fumy
     * @time 2018.10.25 15:52
     * @param phone
     * @param code
     * @return true
     */
    public boolean checkCode(String phone,String code){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object redisCode = valueOperations.get(SysConstant.PHONE_LOGIN_CODE + phone);
        if(redisCode == null) return false;
        if (!code.equals(redisCode.toString())) {
            return false;
        }
        valueOperations.getOperations().delete(SysConstant.PHONE_LOGIN_CODE + phone);
        return true;
    }


    /**
     * 谷歌验证码核查
     * @author fumy
     * @time 2018.10.25 16:33
     * @param MemberId
     * @param codes
     * @return true
     */
    public boolean checkGoogleCode(Long MemberId,String codes) {
        long code = Long.parseLong(codes);
        Member member = memberService.findOne(MemberId);
        long t = System.currentTimeMillis();
        GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
        //  ga.setWindowSize(0); // should give 5 * 30 seconds of grace...
        boolean r = ga.check_code(member.getGoogleKey(), code, t);
        System.out.println("rrrr=" + r);
        if (!r) return false;
        return true;
    }
}
