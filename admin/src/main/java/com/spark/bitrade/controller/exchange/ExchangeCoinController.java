package com.spark.bitrade.controller.exchange;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.KafkaTopicConstant;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.entity.Admin;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.entity.QExchangeCoin;
import com.spark.bitrade.model.screen.ExchangeCoinScreen;
import com.spark.bitrade.service.ExchangeCoinService;
import com.spark.bitrade.util.FileUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import com.sparkframework.security.Encrypt;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.spark.bitrade.util.MessageResult.error;
import static com.spark.bitrade.util.MessageResult.success;
import static org.springframework.util.Assert.notNull;

/**
 * @author rongyu
 * @description 币币交易手续费
 * @date 2018/1/19 15:16
 */
@RestController
@RequestMapping("exchange/exchange-coin")
public class    ExchangeCoinController extends BaseAdminController {

    @Value("${spark.system.md5.key}")
    private String md5Key;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ExchangeCoinService exchangeCoinService;

    @Autowired
    private RestTemplate restTemplate;

    @RequiresPermissions("exchange:exchange-coin:merge")
    @PostMapping("merge")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "添加交易对exchangeCoin")
    public MessageResult ExchangeCoinList(
            @Valid ExchangeCoin exchangeCoin) { /*, 2018-04-27 和本地代码有冲突，注释以下代码
            //note by yangch 时间： 2018.04.26 原因： 合并时一下代码已删除 bengin
            @SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin,
            @RequestParam("password") String password) {
        *//*MessageResult checkCode = checkCode(code,SysConstant.ADMIN_EXCHANGE_COIN_SET_PREFIX+admin.getMobilePhone());
        if (checkCode.getCode()!=0)
            return checkCode ;*//*
        password = Encrypt.MD5(password + md5Key);
        Assert.isTrue(password.equals(Encrypt.MD5(admin.getPassword() + md5Key)),"密码错误");
        //note by yangch 时间： 2018.04.26 原因： 合并时一下代码已删除 end*/
        exchangeCoin = exchangeCoinService.save(exchangeCoin);
        return MessageResult.getSuccessInstance("保存成功", exchangeCoin);
    }

    @RequiresPermissions("exchange:exchange-coin:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "分页查找币币交易手续费exchangeCoin")
    public MessageResult ExchangeCoinList(PageModel pageModel,ExchangeCoinScreen screen) {
        if(pageModel.getProperty()==null){
            List<String> list = new ArrayList<>();
            list.add("symbol");
            List<Sort.Direction> directions = new ArrayList<>();
            directions.add(Sort.Direction.DESC);
            pageModel.setProperty(list);
            pageModel.setDirection(directions);
        }
        Predicate predicate = getPredicate(screen);
        Page<ExchangeCoin> all = exchangeCoinService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }

    private Predicate getPredicate(ExchangeCoinScreen screen) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (screen.getSymbol() != null)
            booleanExpressions.add(QExchangeCoin.exchangeCoin.symbol.eq(screen.getSymbol()));
        if (screen.getCoinSymbol() != null)
            booleanExpressions.add(QExchangeCoin.exchangeCoin.coinSymbol.eq(screen.getCoinSymbol()));
        if (screen.getBaseSymbol() != null)
            booleanExpressions.add(QExchangeCoin.exchangeCoin.baseSymbol.eq(screen.getBaseSymbol()));
        return PredicateUtils.getPredicate(booleanExpressions);
    }


    @ApiOperation(value = "撮合器管理分页",notes = "撮合器管理分页")
    @GetMapping("traderDiscount/page-query")
    @RequiresPermissions("exchange:exchange-coin-traderDiscount:page-query")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "页大小",required = true),
            @ApiImplicitParam(name = "symbol",value = "交易对"),
            @ApiImplicitParam(name = "coinSymbol",value = "交易币种"),
            @ApiImplicitParam(name = "baseSymbol",value = "结算币种")
    })
    @AccessLog(module =AdminModule.EXCHANGE,operation = "撮合器管理分页")
    public  MessageResult TraderDiscountPage(int pageNo,int pageSize,String symbol,String coinSymbol,String baseSymbol){
        Map<String,Object> tradeInfo=(Map<String,Object>)flushDiscountRule(symbol);
        com.github.pagehelper.Page<Map<String,Object>> page= PageHelper.startPage(pageNo, pageSize);
        List<Map<String,Object>> list=exchangeCoinService.getExchangeCoin(symbol,coinSymbol,baseSymbol);
        for (int i=0;i<list.size();i++){
            Object sym=tradeInfo.get(list.get(i).get("symbol"));
            list.get(i).put("status",sym);
        }
        PageInfo<Map<String,Object>> pageInfo=page.toPageInfo();
        return success(PageData.toPageData(pageInfo));
    }

    @RequiresPermissions("exchange:exchange-coin:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "币币交易手续费exchangeCoin 详情")
    public MessageResult detail(
            @RequestParam(value = "symbol") String symbol) {
        ExchangeCoin exchangeCoin = exchangeCoinService.findOne(symbol);
        notNull(exchangeCoin, "validate symbol!");
        return success(exchangeCoin);
    }

    @RequiresPermissions("exchange:exchange-coin:deletes")
    @PostMapping("deletes")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "币币交易手续费exchangeCoin 删除")
    public MessageResult deletes(
            @RequestParam(value = "ids") String[] ids) {
        exchangeCoinService.deletes(ids);
        return success("批量删除成功");
    }

    @RequiresPermissions("exchange:exchange-coin:alter-rate")
    @PostMapping("alter-rate")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "修改币币交易手续费exchangeCoin")
    public MessageResult alterExchangeCoinRate(@Valid ExchangeCoin exchangeCoin,
            @RequestParam(value = "password") String password,
            @SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin) {

        //edit by tansitao 时间： 2018/6/2 原因：修改密码判断
        Assert.notNull(admin, "会话已过期，请重新登录");
        password = Encrypt.MD5(password + md5Key);
        Assert.isTrue(password.equals(admin.getPassword()),"密码错误");
        exchangeCoinService.save(exchangeCoin);
        return success();
    }

    @RequiresPermissions("exchange:exchange-coin:out-excel")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "导出币币交易手续费exchangeCoin Excel")
    public MessageResult outExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List all = exchangeCoinService.findAll();
        return new FileUtil().exportExcel(request, response, all, "exchangeCoin");
    }

    /**
     * 更新交易对撮合器中对应的状态
     * @author fumy
     * @time 2018.08.27 15:52
     * @param symbol
     * @param type 1：启用（上币），2：禁止
     * @return true
     */
    @RequiresPermissions("exchange:exchange-coin:alter-coin-status")
    @GetMapping("alter-coin-status")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "交易对撮合器状态更改kafaka消息发送操作")
    public MessageResult CoinStatusPushToKafka(String symbol, int type){

        ExchangeCoin exchangeCoin= exchangeCoinService.findBySymbol(symbol.toUpperCase());
        exchangeCoin.setEnable(type);
        if(null != exchangeCoin) {
            kafkaTemplate.send(KafkaTopicConstant.exchangeTraderManager, exchangeCoin.getSymbol(), JSON.toJSONString(exchangeCoin));
        }
        return  success();
    }

    /**
     * 查询撮合器状态
     * @author fumy
     * @time 2018.09.04 16:37
     * @param symbol
     * @return true
     */
    @GetMapping("query-coin-status")
    public Object flushDiscountRule(String symbol){
        String serviceName = "service-exchange-trade";
        String url = "http://" + serviceName + "/extrade/monitor/traderStatus";
        ResponseEntity<Object> result;
        if(symbol != null){
            url += "?symbol="+symbol;
             result = restTemplate.getForEntity(url, Object.class, symbol);
        }else{
            result = restTemplate.getForEntity(url, Object.class);
        }
        Object mr = result.getBody();
        return mr;
    }

    /**
     * 刷新撮合器状态
     * @author Zhang Yanjun
     * @time 2018.09.11 15:51
     * @param
     */
    @GetMapping("query-coin-update")
    public MessageResult flush(){
        exchangeCoinService.flushAll();
        return success();
    }

}
