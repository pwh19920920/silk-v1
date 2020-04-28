package com.spark.bitrade.controller.finance;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.dto.MemberTransactionDetailDTO;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.CoinBase;
import com.spark.bitrade.model.screen.MemberTransactionScreen;
import com.spark.bitrade.service.CoinService;
import com.spark.bitrade.service.MemberTransactionService;
import com.spark.bitrade.service.MemberWalletService;
import com.spark.bitrade.service.TotalBalanceStatService;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.ExcelUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.util.Assert.notNull;

/**
 * @author rongyu
 * @description 交易记录
 * @date 2018/1/17 17:07
 */
@Api(description = "总额查询",tags={"总额查询接口操作"})
@RestController
@RequestMapping("/finance/member-transaction")
@Slf4j
public class MemberTransactionController extends BaseAdminController {

    /**
     * 用于内存保存动态查询的结果
     */
    private static volatile Map<String, Coin> coinConcurrentHashMap = new ConcurrentHashMap<String, Coin>();

    @Autowired
    private EntityManager entityManager;

    //查询工厂实体
    private JPAQueryFactory queryFactory;

    @Autowired
    private MemberTransactionService memberTransactionService;

    @Autowired
    private MemberWalletService memberWalletService;//add by tansitao 时间： 2018/5/4 原因：添加memberWalletService

    @Autowired
    private CoinService coinService;  //add tansitao 时间： 2018/5/4 原因：添加coinservice

    @Autowired
    private RestTemplate restTemplate; //add tansitao 时间： 2018/5/4 原因：添加restTemplate

    @Autowired
    @Qualifier(value = "walletMongoTemplate")
    private MongoTemplate mongoTemplate; //add by fumy date:2018.09.27

    @Autowired
    private TotalBalanceStatService balanceStatService; //add by fumy date:2018.09.27

    private Logger logger = LoggerFactory.getLogger(BaseAdminController.class); //add tansitao 时间： 2018/5/4 原因：添加logger

    @RequiresPermissions("finance:member-transaction:all")
    @PostMapping("/all")
    @AccessLog(module = AdminModule.FINANCE, operation = "所有交易记录MemberTransaction")
    public MessageResult all() {
        List<MemberTransaction> memberTransactionList = memberTransactionService.findAll();
        if (memberTransactionList != null && memberTransactionList.size() > 0){
            return success(memberTransactionList);
        }
        return error("没有数据");
    }

    @RequiresPermissions("finance:member-transaction:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.FINANCE, operation = "交易记录MemberTransaction 详情")
    public MessageResult detail(@RequestParam(value = "id") Long id) {
        MemberTransaction memberTransaction = memberTransactionService.findOne(id);
        notNull(memberTransaction, "validate id!");
        return success(memberTransaction);
    }

    @RequiresPermissions(value = {"finance:member-transaction:page-query", "finance:member-transaction:page-query:recharge",
            "finance:member-transaction:page-query:check", "finance:member-transaction:page-query:fee"}, logical = Logical.OR)
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.FINANCE, operation = "分页查找交易记录MemberTransaction")
    public MessageResult pageQuery(
            PageModel pageModel,
            MemberTransactionScreen screen) {
        //add by zyj:优化分页查询慢的问题
        Map<String,Object> map=new HashMap<>();
            map.put("memberId",screen.getMemberId());
            map.put("userName",screen.getAccount());
            map.put("type",screen.getType()==null?null:screen.getType().getOrdinal());
            map.put("symbol",screen.getSymbol());
            map.put("minMoney",screen.getMinMoney());
            map.put("maxMoney",screen.getMaxMoney());
            map.put("minFee",screen.getMinFee());
            map.put("maxFee",screen.getMaxFee());
            map.put("startTime",screen.getStartTime()==null?"":DateUtil.dateToString(screen.getStartTime()));
            map.put("endTime",screen.getEndTime()==null?"":DateUtil.dateToString(screen.getEndTime()).replace("00:00:00","23:59:59"));
        PageInfo<MemberTransactionDetailDTO> pageInfo=memberTransactionService.findBy(pageModel.getPageNo(),pageModel.getPageSize(),map);
        return success(PageData.toPageData(pageInfo));

//        List<Predicate> predicates = new ArrayList<>();
//
//        if(screen.getMemberId()!=null)
//            predicates.add((QMember.member.id.eq(screen.getMemberId())));
//        if (!StringUtils.isEmpty(screen.getAccount()))
//            predicates.add(QMember.member.username.like("%"+screen.getAccount()+"%")
//                        .or(QMember.member.realName.like("%"+screen.getAccount()+"%")));
//        if (screen.getStartTime() != null)
//            predicates.add(QMemberTransaction.memberTransaction.createTime.goe(screen.getStartTime()));
//        if (screen.getEndTime() != null){
//            predicates.add(QMemberTransaction.memberTransaction.createTime.lt(DateUtil.dateAddDay(screen.getEndTime(),1)));
//        }
//        //add by tansitao 时间： 2018/5/7 原因：增加通过币种查询
//        if (!StringUtils.isEmpty(screen.getSymbol())){
//            predicates.add(QMemberTransaction.memberTransaction.symbol.eq(screen.getSymbol()));
//        }
//        if (screen.getType() != null)
//            predicates.add(QMemberTransaction.memberTransaction.type.eq(screen.getType()));
//
//        if(screen.getMinMoney()!=null)
//            predicates.add(QMemberTransaction.memberTransaction.amount.goe(screen.getMinMoney()));
//
//        if(screen.getMaxMoney()!=null)
//            predicates.add(QMemberTransaction.memberTransaction.amount.loe(screen.getMaxMoney()));
//
//        if(screen.getMinFee()!=null)
//            predicates.add(QMemberTransaction.memberTransaction.fee.goe(screen.getMinMoney()));
//
//        if(screen.getMaxFee()!=null)
//            predicates.add(QMemberTransaction.memberTransaction.fee.loe(screen.getMaxMoney()));
//
//        Page<MemberTransactionVO> results = memberTransactionService.joinFind(predicates, pageModel);
//
//        return success(results);
    }



    /**
     * 获取提币地址和提币地址余额
     * @author shenzucai
     * @time 2018.04.25 14:53
     * @param url
     * @param coin
     * @return true
     */
    private CoinBase getRPCWalletCoinBalance(String url, Coin coin){
        CoinBase coinBase = null;
        try{
            ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class);
            log.info("--------test------------result={}",  result);
            if (result.getStatusCode().value() == 200) {
                logger.info(result.getBody().toString());
                MessageResult mr = result.getBody();
                if (mr.getCode() == 0) {
                    try {
                        logger.info("数据{}",mr.getData());
                        String str = JSONObject.toJSONString(mr.getData());
                        log.info("这是个什么鬼 {}",str);
                        coinBase = JSONObject.parseObject(str, CoinBase.class);
                    }catch (Exception e){
                        log.info("{}",e);
                        log.info("该币种{}没有coinbase",coin.getName());

                    }
                }

            }
        }
        catch (Exception e){

        }
        return coinBase;
    }


    //add by tansitao 时间： 2018/4/27 原因：添加总额查询
    @RequiresPermissions("finance:member-transaction:totalBalance-query")
    @PostMapping("totalBalance-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "分页查找后台货币Coin总额")
    public MessageResult totalBalancePageQuery(PageModel pageModel) {
        if(pageModel.getProperty()==null){
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

        //edit by  shenzucai 时间： 2018.09.07  原因：将结果放在内存中，查询是直接返回内存中的数据，并做异步更新
        Page<Coin> pageResult = coinService.findAll(null, pageModel.getPageable());
        if (coinConcurrentHashMap.isEmpty()) {
            for (Coin coin : pageResult.getContent()) {
                String url = null;
                String coinUrl = null;
                coin.setAllBalance(memberWalletService.getAllBalance(coin.getName()));
                if ("ETH".equalsIgnoreCase(coin.getBaseCoinUnit()) && !Objects.equals(coin.getUnit(), "ETC")) {
                    url = "http://SERVICE-RPC-ETH/rpc/balance?coinUnit=" + coin.getUnit();
                }else if("SLU".equalsIgnoreCase(coin.getBaseCoinUnit())){
                    url = "http://SERVICE-RPC-SLU/rpc/balance?coinUnit=" + coin.getUnit();
                } else {
                    url = "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/balance";
                }

                coin.setHotAllBalance(getRPCWalletBalance(url, coin.getUnit()));

                if ("ETH".equalsIgnoreCase(coin.getBaseCoinUnit()) && !Objects.equals(coin.getUnit(), "ETC")) {
                    coinUrl = "http://SERVICE-RPC-ETH/rpc/coinBase?coinUnit=" + coin.getUnit();
                }else if("SLU".equalsIgnoreCase(coin.getBaseCoinUnit())){
                    coinUrl = "http://SERVICE-RPC-SLU/rpc/coinBase?coinUnit=" + coin.getUnit();
                } else {
                    coinUrl = "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/coinBase";
                }


                CoinBase coinBase = getRPCWalletCoinBalance(coinUrl, coin);
                if (coinBase != null) {
                    coin.setCoinBaseBalance(coinBase.getBalance());
                    coin.setCoinBaseAddress(coinBase.getCoinBase());
                }
                coinConcurrentHashMap.put(coin.getUnit(),coin);
            }
        } else {
            for (String str : coinConcurrentHashMap.keySet()) {
                for (Coin coin : pageResult.getContent()) {
                    if (str.equalsIgnoreCase(coin.getUnit())) {
                        coin.setAllBalance(coinConcurrentHashMap.get(str).getAllBalance());
                        coin.setHotAllBalance(coinConcurrentHashMap.get(str).getHotAllBalance());
                        coin.setCoinBaseBalance(coinConcurrentHashMap.get(str).getCoinBaseBalance());
                        coin.setCoinBaseAddress(coinConcurrentHashMap.get(str).getCoinBaseAddress());
                    }

                }
            }
            getService().getTotalBalance(pageResult.getContent());
            List<Coin> coins = new ArrayList();
            for(Coin coin:pageResult.getContent()){
                if(coinConcurrentHashMap.containsKey(coin.getUnit())){
                    coins.add(coinConcurrentHashMap.get(coin.getUnit()));
                }
            }
            pageResult = new PageImpl<Coin>(coins,pageModel.getPageable(),pageResult.getTotalElements());

        }
        return success(pageResult);
    }


    /**
     * 总额查询异步处理
     * @author shenzucai
     * @time 2018.09.07 9:17
     * @param coins
     * @return true
     */
    @Async
    public void getTotalBalance(List<Coin> coins){
        logger.info("开始更新数据：{}",coins);
        for (Coin coin : coins) {
            String url = null;
            String coinUrl = null;
            coin.setAllBalance(memberWalletService.getAllBalance(coin.getName()));
            if ("ETH".equalsIgnoreCase(coin.getBaseCoinUnit()) && !Objects.equals(coin.getUnit(), "ETC")) {
                url = "http://SERVICE-RPC-ETH/rpc/balance?coinUnit=" + coin.getUnit();
            }else if("SLU".equalsIgnoreCase(coin.getBaseCoinUnit())){
                url = "http://SERVICE-RPC-SLU/rpc/balance?coinUnit=" + coin.getUnit();
            } else {
                url = "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/balance";
            }

            coin.setHotAllBalance(getRPCWalletBalance(url, coin.getUnit()));

            if ("ETH".equalsIgnoreCase(coin.getBaseCoinUnit()) && !Objects.equals(coin.getUnit(), "ETC")) {
                coinUrl = "http://SERVICE-RPC-ETH/rpc/coinBase?coinUnit=" + coin.getUnit();
            }else if("SLU".equalsIgnoreCase(coin.getBaseCoinUnit())){
                coinUrl = "http://SERVICE-RPC-SLU/rpc/coinBase?coinUnit=" + coin.getUnit();
            } else {
                coinUrl = "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/coinBase";
            }


            CoinBase coinBase = getRPCWalletCoinBalance(coinUrl, coin);
            if (coinBase != null) {
                coin.setCoinBaseBalance(coinBase.getBalance());
                coin.setCoinBaseAddress(coinBase.getCoinBase());
            }

            if(coinConcurrentHashMap.containsKey(coin.getUnit())){
                coinConcurrentHashMap.replace(coin.getUnit(),coin);
            }else{
                coinConcurrentHashMap.put(coin.getUnit(),coin);
            }

        }
        logger.info("更新数据：{}",coins);
    }


    @RequiresPermissions("finance:member-transaction:totalBalance-out-excel")
    @GetMapping("totalBalance/out-excel")
    @AccessLog(module = AdminModule.SYSTEM,operation = "货币总额导出")
    public void outExcel(HttpServletResponse response) throws IOException {
        /*List<Coin> list=coinService.findAllOrderBySort();
        for (Coin coin:list){
            String url = null;
            String coinUrl = null;
            coin.setAllBalance(memberWalletService.getAllBalance(coin.getName()));
            if("ETH".equalsIgnoreCase(coin.getBaseCoinUnit())){
                url = "http://SERVICE-RPC-ETH/rpc/balance?coinUnit="+coin.getUnit();
            }else{
                url = "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/balance";
            }
            coin.setHotAllBalance(getRPCWalletBalance(url,coin.getUnit()));

            if("ETH".equalsIgnoreCase(coin.getBaseCoinUnit())){
                coinUrl = "http://SERVICE-RPC-ETH/rpc/coinBase?coinUnit="+coin.getUnit();
            }else{
                coinUrl = "http://SERVICE-RPC-" + coin.getUnit() + "/rpc/coinBase";
            }
            CoinBase coinBase = getRPCWalletCoinBalance(coinUrl,coin);
            if(coinBase != null) {
                coin.setCoinBaseBalance(coinBase.getBalance());
                coin.setCoinBaseAddress(coinBase.getCoinBase());
            }
        }*/
        //edit by  shenzucai 时间： 2018.09.07  原因：直接导出内存中的数据
//        ExcelUtil.listToExcel(new ArrayList<>(coinConcurrentHashMap.values()),Coin.class.getDeclaredFields(),response.getOutputStream());
        String fileName="totalBalance_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(new ArrayList<>(coinConcurrentHashMap.values()),Coin.class.getDeclaredFields(),response,fileName);
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
                    BigDecimal bigDecimal = new BigDecimal(balance) ;
                    log.info(unit + "热钱包余额:", bigDecimal);
                    return bigDecimal;
                }
            }
        }  catch (Exception e) {
            log.error("error={}", e);
            return new BigDecimal("0");
        }
        return new BigDecimal("0");
    }

    //edit by zyj:重写导出
    @RequiresPermissions("finance:member-transaction:out-excel")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.FINANCE, operation = "导出交易记录MemberTransaction Excel")
    public void outExcel(MemberTransactionScreen screen,HttpServletResponse response) throws IOException {
        Map<String,Object> map=new HashMap<>();
        map.put("memberId",screen.getMemberId());
        map.put("userName",screen.getAccount());
        map.put("type",screen.getType()==null?null:screen.getType().getOrdinal());
        map.put("symbol",screen.getSymbol());
        map.put("minMoney",screen.getMinMoney());
        map.put("maxMoney",screen.getMaxMoney());
        map.put("minFee",screen.getMinFee());
        map.put("maxFee",screen.getMaxFee());
        map.put("startTime",screen.getStartTime()==null?"":DateUtil.dateToString(screen.getStartTime()));
        map.put("endTime",screen.getEndTime()==null?"":DateUtil.dateToString(screen.getEndTime()).replace("00:00:00","23:59:59"));
        List<MemberTransactionDetailDTO> list=memberTransactionService.findAllByMemberTransactionForOut(map);
        String fileName="memberTransactionDetail_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,MemberTransactionDetailDTO.class.getDeclaredFields(),response,fileName);
//        ExcelUtil.listToExcel(list,MemberTransactionDetailDTO.class.getDeclaredFields(),response.getOutputStream());
    }

//    @RequiresPermissions("finance:member-transaction:out-excel")
//    @GetMapping("out-excel")
//    @AccessLog(module = AdminModule.FINANCE, operation = "导出交易记录MemberTransaction Excel")
//    public MessageResult outExcel(
//            @RequestParam(value = "startTime", required = false) Date startTime,
//            @RequestParam(value = "endTime", required = false) Date endTime,
//            @RequestParam(value = "type", required = false) TransactionType type,
//            @RequestParam(value = "memberId", required = false) Long memberId,
//            HttpServletRequest request, HttpServletResponse response) throws Exception {
//        List<BooleanExpression> booleanExpressionList = getBooleanExpressionList(startTime, endTime, type, memberId);
//        List list = memberTransactionService.queryWhereOrPage(booleanExpressionList, null, null).getContent();
//        return new FileUtil().exportExcel(request, response, list, "交易记录");
//    }

    // 获得条件
    private List<BooleanExpression> getBooleanExpressionList(
            Date startTime, Date endTime, TransactionType type, Long memberId) {
        QMemberTransaction qEntity = QMemberTransaction.memberTransaction;
        List<BooleanExpression> booleanExpressionList = new ArrayList();
        if (startTime != null){
            booleanExpressionList.add(qEntity.createTime.gt(startTime));
        }
        if (endTime != null){
            booleanExpressionList.add(qEntity.createTime.lt(endTime));
        }
        if (type != null){
            booleanExpressionList.add(qEntity.type.eq(type));
        }
        if (memberId != null){
            booleanExpressionList.add(qEntity.memberId.eq(memberId));
        }
        return booleanExpressionList;
    }

    /**
     * 添加异步处理
     *
     * @param
     * @return true
     * @author shenzucai
     * @time 2018.08.27 19:06
     */
    private MemberTransactionController getService() {
        return SpringContextUtil.getBean(this.getClass());
    }


    /**
     * 根据总额地址查询到账明细
     * @author fumy
     * @time 2018.09.27 11:51
     * @param coinUnit
     * @return true
     */
    @ApiOperation(value = "总额提币地址明细查询",notes = "得到总额提币地址明细列表")
    @PostMapping("/withdraw-address/detail")
    @RequiresPermissions("finance:member-transaction:withdraw-address-detail")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo",value = "页码",dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize",value = "每页条数",dataType = "Integer"),
            @ApiImplicitParam(name = "coinUnit",value = "币种单位",dataType = "String"),
            @ApiImplicitParam(name = "address",value = "提币地址",dataType = "String")
    })
    public MessageResult getAddressDetail(Integer pageNo,Integer pageSize,String coinUnit,String address){
        Sort.Order order = new Sort.Order(Sort.Direction.DESC,"time");
        Sort sort = new Sort(order);
        Query query = new Query();

        Criteria  criteria = Criteria.where("address").is(address.trim());
        query.addCriteria(criteria);
        //查询总数
        long count = mongoTemplate.count(query,Deposit.class,coinUnit + "_deposit");
        //分页查询mongo从0开始
        PageRequest page = new PageRequest(pageNo-1, pageSize,sort);
        query.with(page);
        List<Deposit> result = mongoTemplate.find(query, Deposit.class, coinUnit + "_deposit");

        PageInfo<Deposit> pageInfo = new PageInfo<>();

        //计算分页数据
        pageInfo.setTotal(count);
        pageInfo.setList(result);
        pageInfo.setPageSize(pageSize);
        pageInfo.setPageNum(PageData.pageNo4PageHelper(pageNo));
        pageInfo.setPages( ((int)count + pageSize -1 )/pageSize) ;
        return success(PageData.toPageData(pageInfo));
    }

    @ApiOperation(value = "总额日统计数据查询",notes = "得到总额日统计数据列表")
    @PostMapping("/total/day-query")
    @RequiresPermissions("finance:member-transaction-totalBalance-day:page-query")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "pageNo",value = "页码",dataType = "Integer"),
//            @ApiImplicitParam(name = "pageSize",value = "每页条数",dataType = "Integer")
//    })
//    @AccessLog(module = AdminModule.MEMBER, operation = "游戏管理分页查询")
    public MessageResult dayPage(String unit,Integer pageNo,Integer pageSize){
        PageInfo<TotalBalanceStat> pageInfo=balanceStatService.getDayOfList(unit,pageNo,pageSize);
        return success(PageData.toPageData(pageInfo));
    }

}
