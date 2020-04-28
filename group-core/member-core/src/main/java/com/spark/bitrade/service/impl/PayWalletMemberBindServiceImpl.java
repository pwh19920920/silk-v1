package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.annotation.WriteDataSource;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.MemberLevelEnum;
import com.spark.bitrade.dao.MemberDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.exception.AuthenticationException;
import com.spark.bitrade.service.IPayWalletMemberBindService;
import com.spark.bitrade.service.MemberSecuritySetService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.util.IpUtils;
import com.spark.bitrade.util.Md5;
import com.spark.bitrade.util.SpringContextUtil;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.spark.bitrade.mapper.dao.PayWalletMemberBindMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PayWalletMemberBindServiceImpl extends ServiceImpl<PayWalletMemberBindMapper, PayWalletMemberBind> implements IPayWalletMemberBindService {

    @Resource
    private PayWalletMemberBindMapper payWalletMemberBindMapper;
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberSecuritySetService securitySetService;

//    public int insert(PayWalletMemberBind pojo){
//        return payWalletMemberBindMapper.insert(pojo);
//    }

//    public int insertList(List< PayWalletMemberBind> pojos){
//        return payWalletMemberBindMapper.insertList(pojos);
//    }
//
//    public PayWalletMemberBind select(PayWalletMemberBind pojo){
//        return payWalletMemberBindMapper.select(pojo);
//    }
//
//    public int update(PayWalletMemberBind pojo){
//        return payWalletMemberBindMapper.update(pojo);
//    }

    /**
     * 验证用户名密码是否正确
     * @author tansitao
     * @time 2019/1/10 14:36 
     */
    public Member check(String username, String password) throws Exception {
        //通过手机号和邮箱查询用户
        Member member = memberDao.findMemberByMobilePhoneOrEmail(username, username);
        if(member == null) {
            throw new AuthenticationException("LOGIN_FALSE");
        }
        //验证加密规则
        String userPassWord = new SimpleHash("md5", password, member.getSalt(), 2).toHex().toLowerCase();
        if (!userPassWord.equals(member.getPassword())) {
            throw new AuthenticationException("LOGIN_FALSE");
        }
        return member;
    }

    /**
     * 通过邮件注册,并绑定
     * @author tansitao
     * @time 2019/1/10 16:16 
     */
    @Transactional(rollbackFor = Exception.class)
    public Member registerByEmail(String loginNo, String email, String passWord, String walletMarkId, HttpServletRequest request) throws Exception{
        //盐
        String credentialsSalt = ByteSource.Util.bytes(loginNo).toHex().toLowerCase();
        //生成加密后的密码
        String password = new SimpleHash("md5", passWord, credentialsSalt, 2).toHex().toLowerCase();

        Member member = new Member();
        member.setMemberLevel(MemberLevelEnum.GENERAL);
        member.setUsername(email);
        member.setPassword(password);
        member.setEmail(email);
        member.setSalt(credentialsSalt);
        member.setIp(IpUtils.getIp(request));
        Member member1 = memberService.save(member);

        //生成钱包的token
        String token = Md5.md5Digest(member1.getUsername() + System.currentTimeMillis()) + System.currentTimeMillis() + member1.getId();

        //处理渠道来源
        String thirdMark = request.getHeader("thirdMark");
        token = this.getToken(token, thirdMark);

        PayWalletMemberBind payWalletMemberBind = new PayWalletMemberBind();
        payWalletMemberBind.setWalletMarkId(walletMarkId);
        payWalletMemberBind.setMemberId(member1.getId());
        payWalletMemberBind.setToken(token);
        payWalletMemberBind.setAppId(thirdMark);
        getService().save(payWalletMemberBind);

        member1.setToken(token);
        return member1;
    }

    public PayWalletMemberBindServiceImpl getService(){
        return SpringContextUtil.getBean(PayWalletMemberBindServiceImpl.class);
    }

    /**
     * 通过手机注册并绑定钱包
     * @author tansitao
     * @time 2019/1/11 9:59 
     */
    @Transactional(rollbackFor = Exception.class)
    public Member registerByPhone(String loginNo, String phone, String passWord, String walletMarkId, String countryStr, HttpServletRequest request) throws Exception{
        //盐
        String credentialsSalt = ByteSource.Util.bytes(loginNo).toHex().toLowerCase();
        //新的生成密码规则
        String password = new SimpleHash("md5", passWord, credentialsSalt, 2).toHex().toLowerCase();
        Member member=new Member();
        member.setMemberLevel(MemberLevelEnum.GENERAL);
        Location location = new Location();
        location.setCountry(countryStr);
        Country country = new Country();
        country.setZhName(countryStr);
        member.setCountry(country);
        member.setLocation(location);
        member.setUsername(phone);
        member.setPassword(password);
        member.setMobilePhone(phone);
        member.setSalt(credentialsSalt);
        //add|edit|del by  shenzucai 时间： 2018.05.29  原因：获取请求ip
        member.setIp(IpUtils.getIp(request));

        Member member1 = memberService.save(member);
        //add by fumy. date:2018.10.29 reason:手机注册默认开启手机登录、提币验证
        MemberSecuritySet memberSecuritySet = new MemberSecuritySet();
        memberSecuritySet.setMemberId(member1.getId());
        memberSecuritySet.setIsOpenPhoneLogin(BooleanEnum.IS_TRUE);
        memberSecuritySet.setIsOpenPhoneUpCoin(BooleanEnum.IS_TRUE);
        securitySetService.save(memberSecuritySet);

        //生成钱包的token
        String token = Md5.md5Digest(member1.getUsername() + System.currentTimeMillis()) + System.currentTimeMillis() + member1.getId();
        //处理渠道来源
        String thirdMark = request.getHeader("thirdMark");
        token = this.getToken(token,thirdMark);

        PayWalletMemberBind payWalletMemberBind = new PayWalletMemberBind();
        payWalletMemberBind.setWalletMarkId(walletMarkId);
        payWalletMemberBind.setMemberId(member1.getId());
        payWalletMemberBind.setToken(token);
        payWalletMemberBind.setAppId(thirdMark);
        getService().save(payWalletMemberBind);

        member1.setToken(token);
        return member1;
    }

    //处理渠道来源
    public String getToken(String token, String thirdMark){
        if(!StringUtils.isEmpty(thirdMark)){
            token = token.concat("$$").concat(thirdMark);
        }
        return token;
    }

    @Override
    @WriteDataSource
    public int save(PayWalletMemberBind payWalletMemberBind) {
        return payWalletMemberBindMapper.insert(payWalletMemberBind);
    }

    @Override
    @WriteDataSource
    public int update(PayWalletMemberBind payWalletMemberBind){
        return payWalletMemberBindMapper.updateById(payWalletMemberBind);
    }

    @Override
    @ReadDataSource
    public List<PayWalletMemberBind> findMembers(String walletMarkId, BooleanEnum usable, String appId) {
        Map<String,Object> mapMark =new HashMap<>();
        mapMark.put("wallet_mark_id",walletMarkId);
        mapMark.put("app_id",appId);
        mapMark.put("usable",usable);
        List<PayWalletMemberBind> walletMarkIdList = payWalletMemberBindMapper.selectByMap(mapMark);
        return walletMarkIdList;
    }

    @Override
    @ReadDataSource
    public PayWalletMemberBind findByMemberIdAndAppId(long memberId, String appId) {
        Map<String,Object> map =new HashMap<>();
        map.put("member_id",memberId);
        map.put("app_id",appId);
        List<PayWalletMemberBind> bind=payWalletMemberBindMapper.selectByMap(map);
        return bind.size()==0 ? null: bind.get(0);
    }

    @Override
    @ReadDataSource
    public PayWalletMemberBind findByMemberIdAndAppIdAndWalletMarkId(long memberId, String appId, String walletMarkId) {
        Map<String,Object> map =new HashMap<>();
        map.put("member_id",memberId);
        map.put("wallet_mark_id",walletMarkId);
        map.put("app_id", appId);
        List<PayWalletMemberBind> list = payWalletMemberBindMapper.selectByMap(map);
        return list.size() == 0 ? null : list.get(0);
    }
}
