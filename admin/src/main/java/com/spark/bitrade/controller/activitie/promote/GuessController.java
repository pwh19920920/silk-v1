package com.spark.bitrade.controller.activitie.promote;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.BettingConfigStatus;
import com.spark.bitrade.constant.RedPacketConstant;
import com.spark.bitrade.constant.SysConstant;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.ExcelUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.vo.BettingConfigVo;
import com.spark.bitrade.vo.JackpotStatisticsVo;
import com.spark.bitrade.vo.RewardStatisticsVo;
import com.spark.bitrade.vo.VoteStatisticsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author fumy
 * @time 2018.09.13 13:50
 */
@Api(description = "疯狂比特游戏控制类")
@RestController
@RequestMapping("/activity/promote")
@Slf4j
public class GuessController extends BaseAdminController{
    @Autowired
    BettingConfigService bettingConfigService;

    @Autowired
    BettingConfigService configService;

    @Autowired
    RewardService rewardService;

    @Autowired
    BettingRecordService bettingRecordService;

    @Autowired
    StatService statService;

    @Autowired
    private RedisService redisService;

    /**
     * 游戏管理分页
     * @author Zhang Yanjun
     * @time 2018.09.13 14:25
     * @param period  期数
     * @param status  活动状态
     * @param pageNo
     * @param pageSize
     */
    @ApiOperation("游戏配置分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "期数",name = "period",dataType = "String"),
            @ApiImplicitParam(value = "状态",name = "status",dataType = "long"),
            @ApiImplicitParam(value = "页码",name = "pageNo",dataType = "int"),
            @ApiImplicitParam(value = "每页条数",name = "pageSize",dataType = "int")
    })
    @RequiresPermissions("promote-activity:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.MEMBER, operation = "游戏管理分页查询")
    public MessageResult page(String period,Long status,int pageNo,int pageSize){
        PageInfo<BettingConfigVo> pageInfo=bettingConfigService.findAllByPeriodAndStatus(period,status,pageNo,pageSize);
        return success(PageData.toPageData(pageInfo));
    }

    /**
     * 保存活动投注配置
     * @author fumy
     * @time 2018.09.13 13:57
     * @param screen
     * @return true
     */
    @ApiOperation("保存活动投注配置")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "价格区间信息(字符串类型)",name ="bettingPriceRange",dataType = "String"),
            @ApiImplicitParam(value = "操作类型，copy:克隆，update:编辑，save：添加",name ="opType",dataType = "String")
    })
    @RequiresPermissions("promote-activity:merge")
    @PostMapping("/save")
    @AccessLog(module = AdminModule.MEMBER, operation = "保存竞猜投注配置信息")
    public MessageResult saveForConfig(BettingConfig screen,String bettingPriceRange,String opType,@ApiIgnore @SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin){

        //判断传入的活动状态是否正确,新增和修改只能是0：未生效，1：未开始的状态值
        if(screen.getStatus().getOrdinal() != BettingConfigStatus.STAGE_INVALID.getOrdinal()
                && screen.getStatus().getOrdinal() !=BettingConfigStatus.STAGE_PREPARE.getOrdinal()){
            return error("未知的活动状态");
        }

        BettingConfig config = screen;
        List<BettingPriceRange> rangelist = new ArrayList<>();
        JSONArray ranges = JSON.parseArray(bettingPriceRange);
        for (int i=0;i<ranges.size();i++){
            BettingPriceRange range = new BettingPriceRange();
            JSONObject jsonObject = ranges.getJSONObject(i);
            if(i>0){
                JSONObject jsonObject1 = ranges.getJSONObject(i-1);
                //下一个价格区间开始数值必须大于上个结束数值,否则返回错误提示
                if(jsonObject.getBigDecimal("beginRange").compareTo(jsonObject1.getBigDecimal("endRange")) !=1){
                    return error("价格区间存在重合，请检查");
                }
            }
            range.setId(jsonObject.getLong("id"));
            range.setPeriodId(jsonObject.getLong("periodId"));
            range.setGroupName(jsonObject.getString("groupName"));
            range.setOrderId(jsonObject.getInteger("orderId"));
            range.setBeginRange(jsonObject.getBigDecimal("beginRange"));
            range.setEndRange(jsonObject.getBigDecimal("endRange"));
            rangelist.add(range);
        }
        if("copy".equals(opType)){//复制上一期，需重新设置期数为当前期数,时间
            /*
                edit by zyj:
                游戏期数、游戏状态，时间设置：投票时间、开奖时间、领奖时间、开红包时间，价格区间设置：全部清空，
                界面未显示属性：中奖价格字段、创建人、创建时间、更新人、更新时间、是否删除
             */
            //初始化原始字段
            config.setId(null); //清空基础配置id
            config.setPrizePrice(null);//清空中奖价格

//            config.setStatus(screen.getStatus());//设置为未生效状态
            config.setCreateBy(admin.getUsername());//创建人
            config.setUpdateBy(null);//清空更新人
            config.setUpdateTime(null);//清空更新时间

//            config.setPeriod(DateUtil.dateToString(new Date(),"yyyyMMdd"));//设置期数为当前期数
//            config.setBeginTime(DateUtil.addDay(config.getBeginTime(),1));//上期时间+1天，得到当前期数时间
//            config.setEndTime(DateUtil.addDay(config.getEndTime(),1));
//            config.setOpenTime(DateUtil.addDay(config.getOpenTime(),1));
//            config.setPrizeBeginTime(DateUtil.addDay(config.getPrizeBeginTime(),1));
//            config.setPrizeEndTime(DateUtil.addDay(config.getPrizeEndTime(),1));
//            config.setRedpacketBeginTime(DateUtil.addDay(config.getRedpacketBeginTime(),1));
//            config.setRedpacketEndTime(DateUtil.addDay(config.getRedpacketEndTime(),1));
        }else if("update".equals(opType)){
            config.setUpdateBy(admin.getUsername());//更新人
            config.setUpdateTime(new Date());//更新时间
        }else {
            config.setCreateBy(admin.getUsername());//创建人
        }
        //开奖时间openTime>投票结束时间endTime，领奖prizeBeginTime、开红包开始时间redpacketBeginTime>=开奖时间
        if (DateUtil.compareDateSec(screen.getEndTime(),screen.getOpenTime())>=0){
            return error("开奖时间必须大于投票结束时间");
        }
        if (DateUtil.compareDateSec(screen.getOpenTime(),screen.getPrizeBeginTime())>0){
            return error("领奖开始时间必须大于等于开奖时间");
        }
        if (DateUtil.compareDateSec(screen.getOpenTime(),screen.getRedpacketBeginTime())>0){
            return error("开红包开始时间必须大于等于开奖时间");
        }
        configService.save(config,rangelist);//保存
        return success();
    }


    /**
     * 查询中奖，抢红包记录
     * @author fumy
     * @time 2018.09.15 16:56
     * @param periodId
     * @param type 0:竞猜，1：抢红包
     * @param memberId
     * @param pageNo
     * @param pageSize
     * @return true
     */
    @ApiOperation("查询中奖，抢红包记录")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "期数id",name = "periodId",dataType = "long"),
            @ApiImplicitParam(value = "类型",name = "type",dataType = "int"),
            @ApiImplicitParam(value = "会员id",name = "memberId",dataType = "long"),
            @ApiImplicitParam(value = "页码",name = "pageNo",dataType = "int"),
            @ApiImplicitParam(value = "每页条数",name = "pageSize",dataType = "int")
    })
    @PostMapping("/reward/page-query")
    @AccessLog(module = AdminModule.MEMBER, operation = "查询中奖，抢红包记录")
    public MessageResult reward(Long periodId,Integer type,Long memberId,int pageNo,int pageSize){
        PageInfo<Reward> pageInfo=rewardService.queryByPeriodIdAndType(periodId,type,memberId,pageNo,pageSize);
        return success(PageData.toPageData(pageInfo));
    }

    /**
     * 查询投注记录
     * @author fumy
     * @time 2018.09.15 17:03
     * @param periodId
     * @param memberId
     * @param pageNo
     * @param pageSize
     * @return true
     */
    @PostMapping("/bettingRecord/page-query")
    @AccessLog(module = AdminModule.MEMBER, operation = "查询投注记录")
    public MessageResult bettingRecord(Long periodId ,Long memberId,int pageNo,int pageSize){
        PageInfo<BettingRecord> pageInfo=bettingRecordService.queryByPeriodId(periodId,memberId,pageNo,pageSize);
        return success(PageData.toPageData(pageInfo));
    }

    /**
     * 保存价格区间
     * @author fumy
     * @time 2018.09.14 17:12
     * @param range
     * @return true
     */
    @PostMapping("/price_range/save")
    @AccessLog(module = AdminModule.MEMBER, operation = "保存价格区间")
    public MessageResult addPriceRange(BettingPriceRange range){
        bettingConfigService.addRange(range);
        return success();
    }

    /**
     * 根据id删除价格区间
     * @author fumy
     * @time 2018.09.14 17:28
     * @param id
     * @return true
     */
    @PostMapping("/price_range/del")
    @AccessLog(module = AdminModule.MEMBER, operation = "根据id删除价格区间")
    public MessageResult delRangeById(Long[] id){
        bettingConfigService.deleteRangeById(id);
        return success();
    }

    /**
     *
     * 根据id查询投注配置信息
     * @author fumy
     * @time 2018.09.14 13:47
     * @param id
     * @return true
     */
    @PostMapping("/detail")
    @RequiresPermissions("promote-activity:detail")
    @AccessLog(module = AdminModule.MEMBER, operation = "根据id查询投注配置信息")
    public MessageResult getConfigById(Long id){
        BettingConfig bettingConfig = bettingConfigService.findConfigById(id);
        List<BettingPriceRange> range = bettingConfigService.queryPriceRangeByConfigId(id);
        Map<String,Object> map = new HashMap<>();
        map.put("bettingConfig",bettingConfig);
        map.put("priceRangeList",range);
        return success(map);
    }

    /**
     * 根据id删除投注配置信息
     * @author fumy
     * @time 2018.09.14 15:12
     * @param id
     * @return true
     */
    @PostMapping("/del")
    @RequiresPermissions("promote-activity:del")
    @AccessLog(module = AdminModule.MEMBER, operation = "根据id删除投注配置信息")
    public MessageResult delConfigById(Long id){
        boolean bool = bettingConfigService.deleteById(id);
        if(!bool){
            return error("删除失败");
        }
        return success();
    }

    /**
     * 根据id修改投注配置的状态(发布活动)
     * @author fumy
     * @time 2018.09.14 15:16
     * @param id
     * @return true
     */
    @PostMapping("/update_status")
    @RequiresPermissions("promote-activity:update_status")
    @AccessLog(module = AdminModule.MEMBER, operation = "根据id修改投注配置的状态(发布活动)")
    public MessageResult editConfigState(Long id,BettingConfigStatus status){
        //查询是否存在正在进行中的投注活动
        Assert.isTrue( bettingConfigService.isHaveRunningConfig(),"上一期活动还未完成，不能发布");
        boolean bool =  bettingConfigService.updateById(id,status);
        if(!bool){
            return error("发布失败");
        }
        return success();
    }

    /**
     * 清除游戏配置缓存
     * @author fumy
     * @time 2018.09.25 11:35
     * @param id
     * @return true
     */
    @GetMapping("/cache/flush")
    @AccessLog(module = AdminModule.MEMBER, operation = "清除游戏配置缓存")
    public MessageResult flushByConfigId(Long id){
        bettingConfigService.flushByConfigId(id);
        //add|edit|del by tansitao 时间： 2018/11/21 原因：清空缓存
        BettingConfig bettingConfig = bettingConfigService.findConfigById(id);
        redisService.remove(RedPacketConstant.JACKPOT_BALANCE + "_" + bettingConfig.getGuessSymbol().toUpperCase());

        return success();
    }

    /**
     * 疯狂比特报表统计
     * @author Zhang Yanjun
     * @time 2018.09.17 11:09
     * @param type 统计类型  0投票统计，1中奖统计
     * @param startTime 开奖时间范围（开始）
     * @param endTime 开奖时间范围（结束）
     * @param pageNo
     * @param pageSize
     */
    @PostMapping("vote_statistics")
    public MessageResult voteStatistics(Long type,String startTime,String endTime,int pageNo,int pageSize){
        if (type==null){
            return error("请输入统计类型");
        }
        PageInfo<Object> pageInfo=statService.statistics(type,startTime,endTime,pageNo,pageSize);
        return success(PageData.toPageData(pageInfo));
    }

    /**
     * 疯狂比特报表统计导出
     * @author Zhang Yanjun
     * @time 2018.09.18 17:33
     * @param type 统计类型  0投票统计，1中奖统计,2奖池统计
     * @param startTime 开奖时间范围（开始）
     * @param endTime 开奖时间范围（结束）
     * @param response
     */
    @GetMapping("out-excel")
    public MessageResult outStatistics(Long type, String startTime, String endTime, HttpServletResponse response){
        if (type==null){
            return error("请输入统计类型");
        }
        //投票统计
        if (type==0){
            List<VoteStatisticsVo> list=statService.outVoteStatistics(startTime, endTime);
            String fileName="VoteStatistics_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
            ExcelUtil.listToCSV(list,VoteStatisticsVo.class.getDeclaredFields(),response,fileName);
        }//中奖统计
        else if (type==1){
            List<RewardStatisticsVo> list=statService.outRewardStatistics(startTime, endTime);
            String fileName="RewardStatistics_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
            ExcelUtil.listToCSV(list,RewardStatisticsVo.class.getDeclaredFields(),response,fileName);
        }//奖池统计
        else{
            List<JackpotStatisticsVo> list=statService.outJackpotStatistics(startTime, endTime);
            String fileName="JackpotStatistics_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
            ExcelUtil.listToCSV(list,JackpotStatisticsVo.class.getDeclaredFields(),response,fileName);
        }
        return success();
    }

}
