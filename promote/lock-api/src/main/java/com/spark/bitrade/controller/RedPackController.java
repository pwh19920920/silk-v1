package com.spark.bitrade.controller;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.RedPackManage;
import com.spark.bitrade.entity.RedPackReceiveRecord;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.enums.MessageCode;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.service.RedPackReceiveRecordService;
import com.spark.bitrade.service.cnyt.RedPackBizService;
import com.spark.bitrade.util.GeneratorUtil;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.vo.RedPackManageVo;
import com.spark.bitrade.vo.RedPackVo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 *  
 *  红包活动控制器
 *  @author liaoqinghui  
 *  @time 2019.11.25 16:00  
 */
@RestController
@RequestMapping("/redPack")
@Slf4j
public class RedPackController {

    @Autowired
    private RedPackBizService redPackBizService;
    @Autowired
    private LocaleMessageSourceService msgService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedPackReceiveRecordService redPackReceiveRecordService;

    @ApiOperation(value = "红包拆包", tags = "红包拆包")
    @PostMapping("unpack")
    public MessageRespResult<RedPackVo> unpack(@ApiParam("红包配置id") @RequestParam Long packManageId) {

        RedPackManage manage = redPackBizService.findManageById(packManageId);
//        if(authMember != null){
//            redPackBizService.findRecordByMemberId(authMember.getId(),packManageId);
//        }
        Assert.notNull(manage, msgService.getMessage("RED_PACK_NOT_FIND"));
        //判断有效时间
        Date startTime = manage.getStartTime();
        Date endTime = manage.getEndTime();
        Date now = new Date();
        boolean before = startTime.before(now);
        boolean after = endTime.after(now);
        Assert.isTrue(after && before, msgService.getMessage("RED_PACK_IS_END"));

        //判断红包数量
        BigDecimal balance = manage.getRedPacketBalance();
        Assert.isTrue(balance.compareTo(BigDecimal.ZERO) > 0,
                msgService.getMessage("RED_PACK_HAS_ALREADY_ZERO"));
        /**
         * 生成红包
         */
        RedPackReceiveRecord record = redPackBizService.generateRedPack(manage);
        RedPackVo vo = new RedPackVo();
        vo.setRedPackRecordId(record.getId());
        vo.setAmount(record.getReceiveAmount());
        vo.setCoinUnit(record.getReceiveUnit());
        vo.setRedPackActId(record.getRedpackId());
        vo.setValidHours(record.getWithin());
        return MessageRespResult.success4Data(vo);
    }

    @ApiOperation(value = "领红包", tags = "领红包")
    @PostMapping("receivePack")
    public MessageRespResult<RedPackVo> receivePack(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember authMember,
                                                    @ApiParam("红包id") @RequestParam Long packRecordId) {

        //判断红包是否过期
        RedPackReceiveRecord record = redPackBizService.findValidRecordById(packRecordId);
        Assert.notNull(record,"红包不存在");
        //判断红包是否已经领取过
        Assert.isTrue(record.getReceiveStatus()==0,"红包已被领取或已失效");
        //判断红包时限
        RedPackManage manage = redPackBizService.findManageById(record.getRedpackId());
        Assert.isTrue(manage.getEndTime().after(new Date()),msgService.getMessage("RED_PACK_IS_END"));
        //判断新老用户2老会员 1新用户
        Integer isNew = userType(authMember.getId(),record.getCreateTime());
        //判断是否已领取
        try {
            redPackBizService.findRecordByMemberId(authMember.getId(), record.getRedpackId());
        }catch (IllegalArgumentException e){
            Integer receiveStatus = record.getReceiveStatus();
            if(receiveStatus==0){
                //不满足条件 退回红包
                record.setMemberId(authMember.getId());
                record.setUserType(isNew);
                redPackReceiveRecordService.doReturn(record);
            }
            return MessageRespResult.error(e.getMessage());
        }

        Integer isOldUser = manage.getIsOldUser()==null?0:manage.getIsOldUser();
        try {
            if (isOldUser == 1) {
                Assert.isTrue(isNew == 1, msgService.getMessage("IS_ONLY_NEW_MEMBER_JOIN"));
            }
            if (isOldUser == 2) {
                Assert.isTrue(isNew == 3, msgService.getMessage("IS_ONLY_OLD_MEMBER_JOIN"));
            }
        }catch (IllegalArgumentException e){
            //不满足条件 退回红包
            if(record.getReceiveStatus()==0){
                record.setMemberId(authMember.getId());
                record.setUserType(isNew);
                redPackReceiveRecordService.doReturn(record);
            }
            return MessageRespResult.error(e.getMessage());
        }


        if(record.getReceiveStatus()==2){
            return MessageRespResult.error(msgService.getMessage("RED_PACK_HAS_ALREADY_RETURN"));
        }
        record.setUserType(isNew);
        record.setReceiveStatus(1);
        record.setReceiveTime(new Date());
        record.setMemberId(authMember.getId());
        //账户加减
        MessageResult result =  redPackBizService.pickRedPack(record);
        RedPackVo vo = new RedPackVo();
        vo.setRedPackRecordId(record.getId());
        vo.setAmount(record.getReceiveAmount());
        vo.setCoinUnit(record.getReceiveUnit());
        vo.setRedPackActId(record.getRedpackId());
        vo.setReceiveTime(record.getReceiveTime());
        vo.setValidHours(record.getWithin());


        //edit by lc 时间： 2019.12.25 原因:新币需求,ESP红包领取成功后交易锁仓释放
        if("ESP".equals(record.getReceiveUnit()) && result.isSuccess()){
             redPackBizService.exchangeReleaseLock(record);
        }
        return MessageRespResult.success4Data(vo);
    }

    private Integer userType(Long memberId,Date createTime){
        //判断新老用户
        String redisKey = "NEWREGIEST:MEMBERID:" + memberId;
        //2游客
        Integer isNew = 3;
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object o = valueOperations.get(redisKey);
        if (o != null) {
            Member member = memberService.findOne(memberId);
            Date registrationTime = member.getRegistrationTime();
            boolean after = registrationTime.after(createTime);
            isNew = after ? 1 : 3;
        }
        return isNew;
    }

    @PostMapping("findValidRedPack")
    @ApiOperation(value = "获取有效的配置红包", tags = "获取有效的配置红包")
    public MessageRespResult<RedPackManageVo> findValidRedPack() {
        RedPackManage validRedPack = redPackBizService.findValidRedPack();
        RedPackManageVo vo = new RedPackManageVo();
        if (validRedPack != null) {
            BeanUtils.copyProperties(validRedPack, vo);
            return MessageRespResult.success4Data(vo);
        }
        return MessageRespResult.error(MessageCode.INVALID_PARAMETER_SIZE.getCode(), MessageCode.INVALID_PARAMETER_SIZE.getDesc());
    }

    @PostMapping("findRedpackRecord")
    @ApiOperation(value = "已领取红包记录", tags = "已领取红包记录")
    public MessageRespResult<PageInfo<RedPackVo>> findRedpackRecord(@ApiParam("红包配置id") @RequestParam Long packManageId,
                                                                    @RequestParam(defaultValue = "1") Integer pageNo,
                                                                    @RequestParam(defaultValue = "10") Integer size) {
        PageInfo<RedPackVo> pageInfo = redPackBizService.findRedpackRecordByManageId(packManageId, pageNo, size);
        List<RedPackVo> list = pageInfo.getList();
        for (RedPackVo vo : list) {
            String mobilePhone = vo.getMobilePhone();
            if (StringUtils.hasText(mobilePhone) && mobilePhone.contains("@")) {
                String[] split = mobilePhone.split("@");
                if (mobilePhone.length() < 3) {
                    vo.setMobilePhone(getXing(split[0].length()) + "@" + split[1]);
                } else {
                    char c = split[0].charAt(0);
                    char e = split[0].charAt(split[0].length() - 1);
                    int length = split[0].length();
                    String xing = getXing(length - 2);
                    vo.setMobilePhone(c + xing + e + "@" + split[1]);
                }
            } else {
                if (mobilePhone.length() > 3) {
                    String f = mobilePhone.substring(0, 3);
                    String e = mobilePhone.substring(mobilePhone.length() - 4);
                    vo.setMobilePhone(String.format("%s****%s", f, e));
                }
            }

        }
        RedPackManage manage = redPackBizService.findManageById(packManageId);
        List<RedPackVo> list1 = generateReds(manage.getMinAmount(), manage.getMaxAmount(), manage.getUnit(),manage.getStartTime());
        list1.addAll(list);
        pageInfo.setList(list1);
        return MessageRespResult.success4Data(pageInfo);
    }


    private List<RedPackVo> generateReds(BigDecimal min, BigDecimal max, String coin,Date date) {
        String[] phonePrifx = new String[]{"137", "138", "158", "139", "136", "133", "181", "177", "132", "180", "153"};
        double minD = min.doubleValue();
        double maxD = max.doubleValue();
        Integer sc = redPackBizService.getScale(coin);
        List<RedPackVo> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            BigDecimal db = new BigDecimal(Math.random() * (maxD - minD) + minD);
            BigDecimal scale = db.setScale(sc, BigDecimal.ROUND_DOWN);
            RedPackVo vo = new RedPackVo();
            vo.setValidHours(1);
            Calendar calendar=Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.SECOND,GeneratorUtil.getRandomNumber(1,60));
            vo.setReceiveTime(calendar.getTime());
            vo.setCoinUnit(coin);
            vo.setAmount(scale);
            int randomNumber = GeneratorUtil.getRandomNumber(0, phonePrifx.length - 1);
            vo.setMobilePhone(phonePrifx[randomNumber] + "****" + GeneratorUtil.getRandomNumber(1000, 9999));
            list.add(vo);
        }

        return list;
    }


    public static String getXing(int length) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            buffer.append("*");
        }
        return buffer.toString();
    }

}




























