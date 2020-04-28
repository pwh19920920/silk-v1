package com.spark.bitrade.controller;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.PromotionLevel;
import com.spark.bitrade.dto.PromotionMemberDTO;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.service.RewardRecordService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.vo.PromotionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static com.spark.bitrade.util.MessageResult.error;
import static com.spark.bitrade.util.MessageResult.success;

/**
 * 推广
 *
 * @author Zhang Jinwei
 * @date 2018年03月19日
 */
@RestController
@RequestMapping(value = "/promotion")
public class PromotionController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private RewardRecordService rewardRecordService;
    @Autowired
    private LocaleMessageSourceService localeMessageSourceService;

    /**
     * 推广记录查询
     *
     * @param member
     * @return
     */
    @RequestMapping(value = "/record")
    public MessageResult promotionRecord(PageModel pageModel, @SessionAttribute(SESSION_MEMBER) AuthMember member) {
        //获取一级推荐人数
        int oneInviteeNum = memberService.getOneInviteeNumd(member.getId());
        //edit by tansitao 时间： 2018/8/18 原因：分页查询推荐会员信息
        PageInfo<PromotionMemberDTO> page = memberService.pageGetPromotionMember(member.getId(), pageModel.getPageNo(), pageModel.getPageSize());
        //add by tansitao 时间： 2018/12/17 原因：设置推荐信息
        PromotionVO promotionVO = new PromotionVO();
        promotionVO.setOneInviteeNum(oneInviteeNum);
        promotionVO.setPageInfo(page);
        MessageResult mr = MessageResult.success("success");
        mr.setData(promotionVO);
        return mr;
    }

    /**
     * 推广奖励记录
     *
     * @param member
     * @return
     */
    @RequestMapping(value = "/reward/record")
    public MessageResult rewardRecord(@SessionAttribute(SESSION_MEMBER) AuthMember member, int pageNo, int pageSize) throws ParseException {
        //edit by tansitao 时间： 2018/5/23 原因：修改为分页查询
        Page<RewardRecord> rewardRecordPage  = rewardRecordService.queryRewardPromotionList(member.getId(), pageNo, pageSize);
        Page<PromotionRewardRecord> scanRewardRecord = rewardRecordPage.map(x ->
                PromotionRewardRecord.builder().amount(x.getAmount())
                .createTime(x.getCreateTime())
                .remark(x.getRemark())
                .symbol(x.getCoin().getUnit())
                .build()
        );
        MessageResult result = MessageResult.success();
        result.setData(scanRewardRecord);
        return result;
    }

    /**
      * 核查推荐码是否正确
      * @author tansitao
      * @time 2019/1/18 13:39 
      */
    @RequestMapping(value = "/checkPromotion")
    public MessageResult checkPromotion(String promotionCode){
        Member memberPromotion = memberService.findByPromotionCode(promotionCode);
        if(memberPromotion == null){
            return error(localeMessageSourceService.getMessage("PROMOTION_CODE_ERRO"));
        }else {
            return success();
        }
    }
}
