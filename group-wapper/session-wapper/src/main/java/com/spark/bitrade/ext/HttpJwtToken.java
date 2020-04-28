package com.spark.bitrade.ext;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class HttpJwtToken {

    private static final HttpJwtToken instance = new HttpJwtToken();
    private static final Algorithm algorithm = Algorithm.HMAC256("22Y5MGWFBVPpuEvN");
    private static final String issuer = "ucenter-api";
    private static final Map<String, Object> header = new HashMap<>();
    private static final int DEFAULT_EXPIRES = 15; // 默认过期时间：15天


    static {
        header.put("alg", "HS256");
        header.put("typ", "JWT");
    }

    public static HttpJwtToken getInstance() {
        return instance;
    }

    /**
     * 返回一定时间后的日期
     *
     * @param date   开始计时的时间
     * @param year   增加的年
     * @param month  增加的月
     * @param day    增加的日
     * @param hour   增加的小时
     * @param minute 增加的分钟
     * @param second 增加的秒
     */
    private Date getAfterDate(Date date, int year, int month, int day, int hour, int minute, int second) {
        if (date == null) {
            date = new Date();
        }
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        if (year != 0) {
            cal.add(Calendar.YEAR, year);
        }
        if (month != 0) {
            cal.add(Calendar.MONTH, month);
        }
        if (day != 0) {
            cal.add(Calendar.DATE, day);
        }
        if (hour != 0) {
            cal.add(Calendar.HOUR_OF_DAY, hour);
        }
        if (minute != 0) {
            cal.add(Calendar.MINUTE, minute);
        }
        if (second != 0) {
            cal.add(Calendar.SECOND, second);
        }
        return cal.getTime();
    }

    public String createTokenWithClaim(MemberClaim claim) {
        try {
            Date expiresAt;
            if (claim.expiresAt == null) {
                expiresAt = getAfterDate(new Date(), 0, 0, DEFAULT_EXPIRES, 0, 0, 0);
            } else {
                expiresAt = claim.expiresAt;
            }
            return JWT.create()
                    .withHeader(header)
                    .withClaim("userId", claim.userId)
                    .withClaim("username", claim.username)
                    .withIssuer(issuer)
                    .withSubject(claim.subject)
                    .withAudience(claim.audience == null ? "0" : claim.audience)
                    .withIssuedAt(claim.issuedAt == null ? new Date() : claim.issuedAt) // 生成签名的时间
                    .withExpiresAt(expiresAt) // 签名过期的时间
                    .sign(algorithm);
        } catch (Exception exception) {
            log.error("create jwt exception:{}", exception.getMessage());
        }
        return null;
    }

    public MemberClaim verifyToken(String token) {
        MemberClaim memberClaim = null;
        try {
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(issuer).build();
            DecodedJWT jwt = verifier.verify(token);
            Map<String, Claim> claims = jwt.getClaims();
            memberClaim = MemberClaim.builder()
                    .userId(claims.get("userId").asLong())
                    .username(claims.get("username").asString())
                    .audience(jwt.getAudience().get(0))
                    .subject(jwt.getSubject())
                    .issuedAt(jwt.getIssuedAt())
                    .expiresAt(jwt.getExpiresAt())
                    .build();
        } catch (Exception exception) {
            log.error("verify jwt exception: {}", exception.getMessage());
        }
        return memberClaim;
    }

    public static void main(String[] args) {
        HttpJwtToken demo = new HttpJwtToken();
        MemberClaim claim = MemberClaim.builder().subject("xxxx").userId(1L).username("1").audience("123").build();
        String createTokenWithClaim = demo.createTokenWithClaim(claim);
        System.out.println(createTokenWithClaim);
        claim = demo.verifyToken(createTokenWithClaim);
        System.out.println(claim);
    }
}