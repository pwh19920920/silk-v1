package com.spark.bitrade.controller.promotion;

import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.Admin;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.QRewardPromotionSetting;
import com.spark.bitrade.entity.RewardPromotionSetting;
import com.spark.bitrade.model.RewardPromotionScreen;
import com.spark.bitrade.service.CoinService;
import com.spark.bitrade.service.RewardPromotionSettingService;
import com.spark.bitrade.util.CommonUtils;
import com.spark.bitrade.util.MessageResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("promotion/reward")
public class RewardPromotionController extends BaseAdminController {

    @Autowired
    private RewardPromotionSettingService rewardPromotionSettingService;

    @Autowired
    private CoinService coinService;

    /**
     * edit by yangch 时间： 2018.04.29 原因：新增 SessionAttribute注解
     * @author
     * @time 2018.11.27 10:58
     * @param setting
     * @param admin
     * @return true
     */
    @RequiresPermissions("promotion:reward-merge")
    @PostMapping("merge")
    @AccessLog(module = AdminModule.PROMOTION, operation = "创建修改邀请奖励设置")
    public MessageResult merge(RewardPromotionScreen setting, @SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin) {
        //>>> edit by zyj 2018.11.26 规则修改 start
        Coin coin=coinService.findByUnit(setting.getCoin()) == null ? null : coinService.findByUnit(setting.getCoin());
        Long promotionId=rewardPromotionSettingService.isExistPromotionSetting(setting.getType(),coin == null ? "" : coin.getName());
        //创建
        if(setting.getId() == null && promotionId != null ) {
            return error("该配置的类型与币种已存在");
        }
        //修改
        if( setting.getId() != null && promotionId != null && !CommonUtils.equals( setting.getId() , promotionId ) ){
            return error("修改的类型与币种已存在");
        }
        //<<< edit by zyj 2018.11.26 规则修改 end
        RewardPromotionSetting rewardPromotionSetting=new RewardPromotionSetting();
        rewardPromotionSetting.setId(setting.getId());
        rewardPromotionSetting.setType(setting.getType());
        rewardPromotionSetting.setTitle(setting.getType().getCnName());
        rewardPromotionSetting.setCoin(coin);
        if (setting.getType() == PromotionRewardType.EXCHANGE_TRANSACTION) {
            rewardPromotionSetting.setCoin(null);
        }
        rewardPromotionSetting.setInfo("{\"one\":"+setting.getOne()+",\"two\":"+setting.getTwo()+",\"three\":"+setting.getThree()+"}");
        rewardPromotionSetting.setRewardCoin(setting.getRewardCoin());
        rewardPromotionSetting.setRewardCycle(setting.getRewardCycle());
        rewardPromotionSetting.setEffectiveTime(setting.getEffectiveTime());
        rewardPromotionSetting.setData(setting.getData());
        rewardPromotionSetting.setNote(setting.getNote());
        rewardPromotionSetting.setStatus(setting.getStatus());
        rewardPromotionSetting.setAdmin(admin);
        //add by fumy . date 2018.11.20 reason:是否显示到前端
        rewardPromotionSetting.setIsFrontShow(setting.getIsFrontShow());
        rewardPromotionSettingService.save(rewardPromotionSetting);
        return MessageResult.success("保存成功");
    }

    /**
     * 查询所有未被禁用的（判断type条件）
     * 默认按照updatetime降序
     *
     * @param type
     * @return
     */
    @RequiresPermissions("promotion:reward-page-query")
    @GetMapping("page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "分页查询邀请奖励设置")
    public MessageResult pageQuery(
            PageModel pageModel,
            @RequestParam(value = "type", required = false) PromotionRewardType type) {
        //edit by tansitao 时间： 2018/6/2 原因：将状态为禁用的也做显示
        BooleanExpression predicate = null;
        if (type != null){
            predicate=QRewardPromotionSetting.rewardPromotionSetting.type.eq(type);
        }
        Page<RewardPromotionSetting> all = rewardPromotionSettingService.findAll(predicate, pageModel);
        for(RewardPromotionSetting setting : all){
            if(StringUtils.isEmpty(setting.getInfo())){
                continue ;
            }
            JSONObject jsonObject = JSONObject.parseObject(setting.getInfo());
            setting.setOne(jsonObject.getString("one"));
            setting.setTwo(jsonObject.getString("two"));
            setting.setThree(jsonObject.getString("three"));
        }
        return success(all);
    }

    @RequiresPermissions("promotion:reward-del")
    @DeleteMapping("deletes")
    @AccessLog(module = AdminModule.SYSTEM, operation = "批量删除邀请奖励设置")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult deletes(long[] ids) {
        Assert.notNull(ids, "ids不能为null");
        rewardPromotionSettingService.deletes(ids);
        return MessageResult.success("删除成功");
    }
}
