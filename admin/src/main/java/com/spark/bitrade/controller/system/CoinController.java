package com.spark.bitrade.controller.system;

import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.dto.CoinAbstractDto;
import com.spark.bitrade.dto.CoinDescriptionDto;
import com.spark.bitrade.dto.CoinDto;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.CoinService;
import com.spark.bitrade.service.HotTransferRecordService;
import com.spark.bitrade.service.MemberWalletService;
import com.spark.bitrade.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.springframework.util.Assert.notNull;

/**
 * @author rongyu
 * @description 后台货币web
 * @date 2017/12/29 15:01
 */
@RestController
@RequestMapping("/system/coin")
@Slf4j
public class CoinController extends BaseAdminController {

    private Logger logger = LoggerFactory.getLogger(BaseAdminController.class);

    @Autowired
    private HotTransferRecordService hotTransferRecordService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    //add by shenzucai 时间： 2018.05.25 原因：添加配置文件的参数，用于不同环境的判断
    @Autowired
    private Environment env;

    @RequiresPermissions("system:coin:create")
    @PostMapping("create")
    @AccessLog(module = AdminModule.SYSTEM, operation = "创建后台货币Coin")
    public MessageResult create(@Valid Coin coin, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        Coin one = coinService.findOne(coin.getName());
        if (one != null)
            return error("货币名称已经存在");
        coinService.save(coin);
        return success();
    }

    @RequiresPermissions("system:coin:all-name")
    @PostMapping("all-name")
    @AccessLog(module = AdminModule.SYSTEM, operation = "查找所有coin的name")
    public MessageResult getAllCoinName() {
        List<String> list = coinService.getAllCoinName();
        return MessageResult.getSuccessInstance("保存成功", list);
    }

    //add by yangch 时间： 2018.04.29 原因：合并
    @RequiresPermissions("system:coin:all-name-and-unit")
    @PostMapping("all-name-and-unit")
    @AccessLog(module = AdminModule.SYSTEM, operation = "查找所有coin的name和unit")
    public MessageResult getAllCoinNameAndUnit() {
        List<CoinDto> list = coinService.getAllCoinNameAndUnit();
        return MessageResult.getSuccessInstance("保存成功", list);
    }

    @PostMapping("all-name/legal")
    @AccessLog(module = AdminModule.SYSTEM, operation = "查找所有coin的name")
    public MessageResult getAllCoinNameLegal() {
        List<String> list = coinService.getAllCoinNameLegal();
        return success(list);
    }

    /**
     * 修改币种配置信息
     * @author fumy
     * @time 2018.11.01 15:55
     * @param coin
     * @param admin
     * @param code
     * @param bindingResult
     * @return true
     */
    @RequiresPermissions("system:coin:update")
    @PostMapping("update")
    @AccessLog(module = AdminModule.SYSTEM, operation = "更新后台货币Coin")
    public MessageResult update(@Valid Coin coin,
                                @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin,
                                String code,
                                BindingResult bindingResult) {

        Assert.notNull(admin, "会话已过期，请重新登录");
        //edit by shenzucai 时间： 2018.05.25 原因：只有生产环境才需要发送验证码 start
        String[] profiles = env.getActiveProfiles();
        logger.info("当前激活的配置文件为：*********** {}", profiles[0]);
        if (profiles != null && "prod".equalsIgnoreCase(profiles[0])) {
            MessageResult checkCode = checkCode(code, SysConstant.ADMIN_COIN_REVISE_PHONE_PREFIX + admin.getMobilePhone());
            if (checkCode.getCode() != 0) {
                return checkCode;
            }
        }
        //edit by shenzucai 时间： 2018.05.25 原因：只有生产环境才需要发送验证码 end

        notNull(coin.getName(), "validate coin.name!");
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        Coin one = coinService.findOne(coin.getName());
        notNull(one, "validate coin.name!");
        //把币种介绍信息保存到MongoDB
        Criteria criteria = Criteria.where("unit").is(coin.getUnit().trim());
        CoinDescriptionDto descriptionDto = mongoTemplate.findOne(new Query(criteria), CoinDescriptionDto.class,"coin_description");

        if(descriptionDto == null){
            descriptionDto = new CoinDescriptionDto();
            descriptionDto.setUnit(one.getUnit());
            descriptionDto.setContent(coin.getContent());
            mongoTemplate.insert(descriptionDto,"coin_description");
        }else {
            Update update = new Update();
            update.set("content",coin.getContent());
            mongoTemplate.upsert(new Query(criteria),update,"coin_description");
        }
        //保存币种信息
        coinService.save(coin);
        return success();
    }

    @RequiresPermissions("system:coin:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.SYSTEM, operation = "后台货币Coin详情")
    public MessageResult detail(@RequestParam("name") String name) {
        Coin coin = coinService.findOne(name);
        notNull(coin, "validate coin.name!");
        return success(coin);
    }

    @RequiresPermissions("system:coin:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "分页查找后台货币Coin")
    public MessageResult pageQuery(PageModel pageModel) {
        if (pageModel.getProperty() == null) {
            List<String> list = new ArrayList<>();
            //edit by yangch 2018-04-20 ，按sort字段排序
            //list.add("name");
            list.add("sort");

            List<Sort.Direction> directions = new ArrayList<>();
            //edit by yangch 2018-04-20 ，按sort字段排序
            //directions.add(Sort.Direction.DESC);
            directions.add(Sort.Direction.ASC);

            pageModel.setProperty(list);
            pageModel.setDirection(directions);
        }
        Page<Coin> pageResult = coinService.findAll(null, pageModel.getPageable());
//        for (Coin coin : pageResult.getContent()) {
//            coin.setAllBalance(memberWalletService.getAllBalance(coin.getName()));
//            String url = "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/balance";
//            coin.setHotAllBalance(getRPCWalletBalance(url, coin.getUnit()));
//            //del by yangch 时间： 2018.04.29 原因：合并时发一下逻辑已删除 ？？
//            String coinUrl = "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/coinBase";
//            CoinBase coinBase = getRPCWalletCoinBalance(coinUrl,coin);
//            if(coinBase != null) {
//                coin.setCoinBaseBalance(coinBase.getBalance());
//                coin.setCoinBaseAddress(coinBase.getCoinBase());
//            }
//        }
        return success(pageResult);
    }


//    @RequiresPermissions("system:coin:page-query")
    @PostMapping("all_coin_abstract")
    @AccessLog(module = AdminModule.SYSTEM, operation = "获取所有币种摘要")
    public MessageResult allCoinAbstract() {

        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();

        booleanExpressions.add(QCoin.coin.status.eq(CommonStatus.NORMAL));

        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        List<Coin> coins = coinService.findAll(predicate);
        List<CoinAbstractDto> coinAbstractDtos = new ArrayList<>();
        coins.forEach(coin->{
            CoinAbstractDto coinAbstractDto = new CoinAbstractDto();
            coinAbstractDto.setName(coin.getName());
            coinAbstractDto.setNameCn(coin.getNameCn());
            coinAbstractDto.setSort(coin.getSort());
            coinAbstractDto.setUnit(coin.getUnit());
            coinAbstractDtos.add(coinAbstractDto);
        });

        return success(coinAbstractDtos);
    }


    private BigDecimal getRPCWalletBalance(String url, String unit) {
        try {
            //String url = "http://" + serviceName + "/rpc/address/{account}";
            ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class);
            log.info("result={}", result);
            if (result.getStatusCode().value() == 200) {
                MessageResult mr = result.getBody();
                if (mr.getCode() == 0) {
                    String balance = mr.getData().toString();
                    BigDecimal bigDecimal = new BigDecimal(balance);
                    log.info(unit + "热钱包余额:", bigDecimal);
                    return bigDecimal;
                }
            }
        } catch (Exception e) {
            log.error("error={}", e);
            return new BigDecimal("0");
        }
        return new BigDecimal("0");
    }


    @RequiresPermissions("system:coin:out-excel")
    @GetMapping("outExcel")
    @AccessLog(module = AdminModule.SYSTEM, operation = "导出后台货币Coin Excel")
    public MessageResult outExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List all = coinService.findAll();
        return new FileUtil().exportExcel(request, response, all, "coin");
    }

    @RequiresPermissions("system:coin:delete-by-name")
    @PostMapping("delete/{name}")
    @AccessLog(module = AdminModule.SYSTEM, operation = "删除后台货币Coin")
    public MessageResult Delete(@PathVariable("name") String name) {
        Coin coin = coinService.findOne(name);
        notNull(coin, "validate coin.name!");
        coinService.deleteOne(name);
        return success();
    }

    @RequiresPermissions("system:coin:set-platform")
    @PostMapping("set/platform")
    @AccessLog(module = AdminModule.SYSTEM, operation = "设置平台币")
    public MessageResult setPlatformCoin(@RequestParam("name") String name) {
        Coin coin = coinService.findOne(name);
        notNull(coin, "validate coin.name!");
        coinService.setPlatformCoin(coin);
        return success();
    }


    /**
     * 转入冷钱包,扣除矿工费Coin.minerFee
     *
     * @param admin  手工操作者
     * @param amount 转账数量
     * @param unit   转账币种单位
     * @param code   验证码
     * @return
     */
    @RequiresPermissions("system:coin:transfer")
    @PostMapping("transfer")
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    @AccessLog(module = AdminModule.SYSTEM, operation = "热钱包转账至冷钱包")
    public MessageResult transfer(@SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin,
                                  @RequestParam("amount") BigDecimal amount,
                                  @RequestParam("unit") String unit,
                                  @RequestParam("code") String code) {
        Assert.notNull(admin, "会话已过期，请重新登录");

        String[] profiles = env.getActiveProfiles();
        logger.info("当前激活的配置文件为：*********** {}", profiles[0]);
        if (profiles != null && "prod".equalsIgnoreCase(profiles[0])) {
            MessageResult checkCode = checkCode(code, SysConstant.ADMIN_COIN_TRANSFER_COLD_PREFIX + admin.getMobilePhone());
            if (checkCode.getCode() != 0) {
                return checkCode;
            }
        }


        Coin coin = coinService.findByUnit(unit);
        //edit by  shenzucai 时间： 2018.09.04  原因：适配优化后的eth代币
        String urlBalance;
        if("ETH".equalsIgnoreCase(coin.getBaseCoinUnit())  && !Objects.equals(coin.getUnit(),"ETC")) {
            urlBalance = "http://SERVICE-RPC-ETH/rpc/balance?coinUnit="+coin.getUnit();
        }else if("SLU".equalsIgnoreCase(coin.getBaseCoinUnit())) {
            urlBalance = "http://SERVICE-RPC-SLU/rpc/balance?coinUnit="+coin.getUnit();
        }else{
            urlBalance = "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/balance";
        }
        BigDecimal balance = getRPCWalletBalance(urlBalance, coin.getUnit());
        logger.info("balance:-------{}", balance);
        if (amount.compareTo(balance) > 0) {
            return error("热钱包余额不足");
        }

        String url;

        MessageResult result = null;

        if("ETH".equalsIgnoreCase(coin.getBaseCoinUnit())  && !Objects.equals(coin.getUnit(),"ETC")) {
            url = "http://SERVICE-RPC-ETH/rpc/transfer?address={1}&amount={2}&fee={3}&withdrawId={4}&coinUnit={5}";
            result = restTemplate.getForObject(url,
                    MessageResult.class, coin.getColdWalletAddress().toString(), amount, BigDecimal.ZERO,-1L,coin.getUnit());
        }else if("SLU".equalsIgnoreCase(coin.getBaseCoinUnit())) {
            url = "http://SERVICE-RPC-SLU/rpc/transfer?address={1}&amount={2}&fee={3}&withdrawId={4}&coinUnit={5}";
            result = restTemplate.getForObject(url,
                    MessageResult.class, coin.getColdWalletAddress().toString(), amount, BigDecimal.ZERO,-1L,coin.getUnit());
        }else{
            url = "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/transfer?address={1}&amount={2}&fee={3}&withdrawId={4}";
            result = restTemplate.getForObject(url,
                    MessageResult.class, coin.getColdWalletAddress().toString(), amount, BigDecimal.ZERO,-1L);
        }



        logger.info("result = {}", result);
        if (result.getCode() == 0 && result.getData() != null) {
            HotTransferRecord hotTransferRecord = new HotTransferRecord();
            hotTransferRecord.setAdminId(admin.getId());
            hotTransferRecord.setAdminName(admin.getUsername());
            hotTransferRecord.setAmount(amount);
            hotTransferRecord.setBalance(balance.subtract(amount));
            //edit by yangch 时间： 2018.04.29 原因：代码合并
            //hotTransferRecord.setMinerFee(coin.getMinerFee());
            hotTransferRecord.setMinerFee(coin.getMinerFee() == null ? BigDecimal.ZERO : coin.getMinerFee());
            hotTransferRecord.setUnit(unit.toUpperCase());
            hotTransferRecord.setColdAddress(coin.getColdWalletAddress());
            hotTransferRecord.setTransactionNumber(result.getData().toString());
            hotTransferRecordService.save(hotTransferRecord);
            return success("转入冷钱包成功", hotTransferRecord);
        }
        return error("转入冷钱包失败");
    }

    @RequiresPermissions("system:coin:hot-transfer-record:page-query")
    @PostMapping("/hot-transfer-record/page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "热钱包转账至冷钱包记录分页查询")
    public MessageResult page(PageModel pageModel, String unit,String adminName, String coldAddress) {
//        List<BooleanExpression> booleanExpressions = new ArrayList<>();
//        if (!StringUtils.isEmpty(unit))
//            booleanExpressions.add(QHotTransferRecord.hotTransferRecord.unit.eq(unit));
//        Page<HotTransferRecord> page = hotTransferRecordService.findAll(PredicateUtils.getPredicate(booleanExpressions), pageModel);
//        return success("", page);
        //edit by zyj
        PageInfo<HotTransferRecord>pageInfo=hotTransferRecordService.findAllBy(adminName,coldAddress,unit,pageModel.getPageNo(),pageModel.getPageSize());
        return success(PageData.toPageData(pageInfo));
    }

    @RequiresPermissions("system:coin:hot-transfer-record:out-excel")
    @GetMapping("/hot-transfer-record/out-excel")
    @AccessLog(module = AdminModule.SYSTEM,operation = "热钱包转账至冷钱包记录导出")
    public void outExcel(String unit,String adminName, String coldAddress,HttpServletResponse response) throws IOException {
        List<HotTransferRecord> list=hotTransferRecordService.findAllForOut(adminName,coldAddress,unit);
        ExcelUtil.listToExcel(list,HotTransferRecord.class.getDeclaredFields(),response.getOutputStream());
    }
}
