package com.spark.bitrade.interceptor;

import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.PayWalletMemberBind;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.ext.HttpJwtToken;
import com.spark.bitrade.ext.MemberClaim;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.service.impl.PayWalletMemberBindServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <p>
 * jwt方式token 处理，兼容老方式的token
 * </p>
 *
 * @author wsy
 * @since 2019-4-8 17:37:58
 */
public class MemberJwtCheck {

    static boolean handleJwt(HttpServletRequest request) {
        String accessToken = request.getHeader("access-auth-token");
        String walletToken = request.getHeader("wallet-auth-token");
        BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
        MemberService memberService = (MemberService) factory.getBean("memberService");
        if (StringUtils.isNotBlank(accessToken) && Pattern.matches(".+\\..+\\..+", accessToken)) { // token 为jwt时
            MemberClaim claim = HttpJwtToken.getInstance().verifyToken(accessToken);
            if (claim != null) {
                Member member = memberService.loginWithUserId(claim.userId, claim.username, request.getRemoteAddr());
                if (member != null && member.getStatus() == CommonStatus.NORMAL) {
                    AuthMember authMember = AuthMember.toAuthMember(member);
                    authMember.setPlatform(claim.audience);
                    request.getSession().setAttribute(SysConstant.SESSION_MEMBER, authMember);
                    return true;
                }
            }
        } else if (StringUtils.isNotBlank(accessToken)) { // token
            Member member = memberService.loginWithToken(accessToken, request.getRemoteAddr(), "");
            if (member != null) {
                AuthMember authMember = AuthMember.toAuthMember(member);
                if (accessToken.indexOf("$$") > 0) {
                    authMember.setPlatform(accessToken.substring(accessToken.indexOf("$$") + 2));
                }
                request.getSession().setAttribute(SysConstant.SESSION_MEMBER, authMember);
                return true;
            }
        } else if (StringUtils.isNotBlank(walletToken)) {
            PayWalletMemberBindServiceImpl payWalletMemberBindServiceImpl = (PayWalletMemberBindServiceImpl) factory.getBean("payWalletMemberBindServiceImpl");
            Map<String, Object> map = new HashMap<>();
            map.put("token", walletToken);
            List<PayWalletMemberBind> payWalletMemberBindList = payWalletMemberBindServiceImpl.selectByMap(map);
            if (payWalletMemberBindList != null && payWalletMemberBindList.size() > 0) {
                PayWalletMemberBind payWalletMemberBind = payWalletMemberBindList.get(0);
                Member member = memberService.findOne(payWalletMemberBind.getMemberId());
                if (member != null) {
                    AuthMember authMember = AuthMember.toAuthMember(member);
                    if (walletToken.indexOf("$$") > 0) {
                        authMember.setPlatform(walletToken.substring(walletToken.indexOf("$$") + 2));
                    } else {
                        authMember.setPlatform(request.getHeader("thirdMark"));
                    }
                    request.getSession().setAttribute(SysConstant.SESSION_MEMBER, authMember);
                    return true;
                }
            }
        }
        return false;
    }
}
