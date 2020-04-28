package com.spark.bitrade.controller.promotion;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.entity.Admin;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.RewardActivitySetting;
import com.spark.bitrade.service.CoinService;
import com.spark.bitrade.service.RewardActivitySettingService;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.vo.RewardActivitySettingVO;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;


/**
 * 用户奖励
 * @author Zhang Yanjun
 * @time 2018.10.11 10:59
 */
@Api(description = "用户奖励",tags={"用户奖励操作"})
@RestController
@RequestMapping("promotion/reward/activity")
@Slf4j
public class RewardActivityController {

    @Autowired
    private RewardActivitySettingService rewardActivitySettingService;

    @Autowired
    private CoinService coinService;

    /**
     * 用户奖励分页
     * @author Zhang Yanjun
     * @time 2018.10.11 14:08
     * @param pageNo
     * @param pageSize
     */
    @ApiOperation(value = "用户奖励分页",notes = "用户奖励分页查询")
    @PostMapping("page-query")
    @RequiresPermissions("promote:activity-page-query")
    @AccessLog(module = AdminModule.PROMOTION,operation = "用户奖励分页")
    public MessageRespResult<RewardActivitySettingVO> page(@ApiParam(name = "pageNo",value = "页码",type = "int") int pageNo,@ApiParam(name = "pageSize",value = "页大小",type = "int")int pageSize){
        PageInfo<RewardActivitySettingVO> pageInfo=rewardActivitySettingService.findAllPage(pageNo, pageSize);
        return MessageRespResult.success("查询成功", PageData.toPageData(pageInfo));
    }

    /**
     * 用户奖励编辑/添加
     * @author Zhang Yanjun
     * @time 2018.10.12 9:42
     * @param admin
     * @param r
     */
    @ApiOperation(value = "用户奖励编辑/添加",notes = "用户奖励编辑/添加操作（不传id为添加）")
    @PostMapping("update")
    @RequiresPermissions("promote:activity-merge")
    @AccessLog(module = AdminModule.PROMOTION,operation = "用户奖励编辑/添加")
    public MessageRespResult<RewardActivitySetting> update(@ApiIgnore @SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin,
                                                           RewardActivitySettingVO r){
        RewardActivitySetting setting=rewardActivitySettingService.findOneByType(r.getType());
        RewardActivitySetting rewardActivitySetting=new RewardActivitySetting();
        if (r.getId()==null&&setting!=null){
            return MessageRespResult.error("该类型的配置已存在");
        }
        rewardActivitySetting.setId(r.getId());
        rewardActivitySetting.setType(r.getType());
        rewardActivitySetting.setTitle(r.getType().getCnName());
        Coin coin =coinService.findByUnit(r.getUnit());
        rewardActivitySetting.setCoin(coin);
        rewardActivitySetting.setRemark(r.getRemark());
        rewardActivitySetting.setData(r.getData());
        rewardActivitySetting.setStatus(r.getStatus());
        rewardActivitySetting.setAdmin(admin);
        rewardActivitySetting.setInfo( "{\"amount\":"+r.getAmount()+"}");
        //add by fumy . date：2018.11.20 reason:添加是否显示到首页
        rewardActivitySetting.setIsFrontShow(r.getIsFrontShow());
        rewardActivitySettingService.save(rewardActivitySetting);
        return MessageRespResult.success("修改/添加成功");
    }



}
