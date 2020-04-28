package com.spark.bitrade.controller.finance;

import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.exception.UnexpectedException;
import com.spark.bitrade.model.screen.OrderScreen;
import com.spark.bitrade.model.screen.WithdrawRecordScreen;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.CoinTokenVo;
import com.spark.bitrade.vo.OtcOrderVO;
import com.spark.bitrade.vo.WithdrawRecordVO;
import com.sparkframework.security.Encrypt;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

import static com.spark.bitrade.constant.BooleanEnum.IS_FALSE;
import static com.spark.bitrade.constant.WithdrawStatus.*;
import static com.spark.bitrade.entity.QWithdrawRecord.withdrawRecord;
import static com.spark.bitrade.util.BigDecimalUtils.sub;
import static org.springframework.util.Assert.isNull;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * @author rongyu
 * @description 提现
 * @date 2018/2/25 11:22
 */
@RestController
@RequestMapping("/finance/withdraw-record")
@Slf4j
public class WithdrawRecordController extends BaseAdminController {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private MemberWalletService memberWalletService ;
    //add by tansitao 时间： 2018/5/1 原因：添加coinserveice
    @Autowired
    private CoinService coinService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private InterfaceLogService interfaceLogService;
    @Autowired
    private MemberWalletService walletService;
    @Autowired
    private WithdrawRecordService withdrawRecordService;
    @Autowired
    private CoinTokenService coinTokenService;

    @Value("${spark.system.md5.key}")
    private String md5Key;

    @RequiresPermissions("finance:withdraw-record:all")
    @GetMapping("/all")
    @AccessLog(module = AdminModule.FINANCE, operation = "所有提现记录WithdrawRecord")
    public MessageResult all() {
        List<WithdrawRecord> withdrawRecordList = withdrawRecordService.findAll();
        if (withdrawRecordList == null || withdrawRecordList.size() < 1) {
            return error("没有数据");
        }
        return success(withdrawRecordList);
    }

    @RequiresPermissions("finance:withdraw-record:page-query")
    @RequestMapping("/page-query")
    @AccessLog(module = AdminModule.FINANCE, operation = "分页查询提现记录WithdrawRecord")
    public MessageResult pageQuery(
            PageModel pageModel,
            WithdrawRecordScreen screen) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(QWithdrawRecord.withdrawRecord.memberId.eq(QMember.member.id));

        if(screen.getMemberId() != null){
            predicates.add(QWithdrawRecord.withdrawRecord.memberId.eq(screen.getMemberId()));
        }
        if(screen.getStatus()!=null) {
            predicates.add(QWithdrawRecord.withdrawRecord.status.eq(screen.getStatus()));
        }
        //add|edit|del by  shenzucai 时间： 2018.06.12  原因：添加时间查询 start

        if (screen.getStartTime() != null) {
            predicates.add(QWithdrawRecord.withdrawRecord.createTime.goe(screen.getStartTime()));
        }
        if (screen.getEndTime() != null){
            predicates.add(QWithdrawRecord.withdrawRecord.createTime.lt(DateUtil.dateAddDay(screen.getEndTime(),1)));
        }
        //add|edit|del by  shenzucai 时间： 2018.06.12  原因：添加时间查询 end

        if (screen.getIsAuto() != null) {
            predicates.add(QWithdrawRecord.withdrawRecord.isAuto.eq(screen.getIsAuto()));
        }

        if (!StringUtils.isEmpty(screen.getAddress())) {
            predicates.add(QWithdrawRecord.withdrawRecord.address.eq(screen.getAddress()));
        }

        if (!StringUtils.isEmpty(screen.getUnit())) {
            predicates.add(QWithdrawRecord.withdrawRecord.coin.unit.equalsIgnoreCase(screen.getUnit()));
        }
        if (!StringUtils.isEmpty(screen.getOrderSn())) {
            predicates.add(QWithdrawRecord.withdrawRecord.transactionNumber.eq(screen.getOrderSn()));
        }

        if (!StringUtils.isEmpty(screen.getAccount())) {
            predicates.add(QMember.member.username.like("%" + screen.getAccount() + "%")
                    .or(QMember.member.realName.like("%" + screen.getAccount() + "%")));
        }

        Page<WithdrawRecordVO> pageListMapResult = withdrawRecordService.joinFind(predicates,pageModel);
        return success(pageListMapResult);
    }


    /**
     * 参数 fileName 为导出excel 文件的文件名 格式为 .xls  定义在OutExcelInterceptor 拦截器中 ，非必须参数
     * @param pageModel
     * @param screen
     * @param response
     * @throws Exception
     */
    @RequiresPermissions("finance:withdraw-record:out-excel")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.FINANCE, operation = "导出提现记录WithdrawRecord Excel")
    //edit by yangch 时间： 2018.04.29 原因：合并
    public void outExcel(
            PageModel pageModel,
            WithdrawRecordScreen screen,
            HttpServletResponse response
    ) throws Exception {

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(QWithdrawRecord.withdrawRecord.memberId.eq(QMember.member.id));

        if(screen.getMemberId() != null){
            predicates.add(QWithdrawRecord.withdrawRecord.memberId.eq(screen.getMemberId()));
        }

        if(screen.getStatus()!=null) {
            predicates.add(QWithdrawRecord.withdrawRecord.status.eq(screen.getStatus()));
        }

        //add|edit|del by  shenzucai 时间： 2018.06.12  原因：添加时间查询 start

        if (screen.getStartTime() != null){
            predicates.add(QWithdrawRecord.withdrawRecord.createTime.goe(screen.getStartTime()));
        }
        if (screen.getEndTime() != null){
            predicates.add(QWithdrawRecord.withdrawRecord.createTime.lt(DateUtil.dateAddDay(screen.getEndTime(),1)));
        }
        //add|edit|del by  shenzucai 时间： 2018.06.12  原因：添加时间查询 end

        if (screen.getIsAuto() != null) {
            predicates.add(QWithdrawRecord.withdrawRecord.isAuto.eq(screen.getIsAuto()));
        }
        if (!StringUtils.isEmpty(screen.getAddress())) {
            predicates.add(QWithdrawRecord.withdrawRecord.address.eq(screen.getAddress()));
        }
        if (!StringUtils.isEmpty(screen.getUnit())) {
            predicates.add(QWithdrawRecord.withdrawRecord.coin.unit.equalsIgnoreCase(screen.getUnit()));
        }
        if (!StringUtils.isEmpty(screen.getAccount())) {
            predicates.add(QMember.member.username.like("%" + screen.getAccount() + "%")
                    .or(QMember.member.realName.like("%" + screen.getAccount() + "%")));
        }
        List<WithdrawRecordVO> list = withdrawRecordService.outExcel(predicates,pageModel);
        String fileName="withdrawRecord_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,WithdrawRecordVO.class.getDeclaredFields(),response,fileName);
//        ExcelUtil.listToExcel(list,WithdrawRecordVO.class.getDeclaredFields(),response.getOutputStream(),WithdrawRecordVO.class.getName());
    }

    @GetMapping("/{id}")
    @RequiresPermissions("finance:withdraw-record:detail")
    @AccessLog(module = AdminModule.FINANCE, operation = "提现记录WithdrawRecord 详情")
    public MessageResult detail(@PathVariable("id") Long id) {
        WithdrawRecord withdrawRecord = withdrawRecordService.findOne(id);
        notNull(withdrawRecord, "没有数据");
        return success(withdrawRecord);
    }



    /**
     * 一键审核通过
     * @param ids
     * @return
     */
    @RequiresPermissions("finance:withdraw-record:audit-pass")
    @PatchMapping("/audit-pass")
    @AccessLog(module = AdminModule.FINANCE, operation = "提现记录WithdrawRecord一键审核通过")
    public MessageResult auditPass(@RequestParam("ids") Long[] ids) {
       try{
           //add by tansitao 时间： 2018/9/7 原因：从数据库查询带币并设置到coins
//           String coins = "ETH,USDT";
           StringBuilder coins = new StringBuilder("ETH,USDT");
           List<CoinTokenVo> coinTokenVoList = coinTokenService.findAll();
           if(coinTokenVoList != null){
//               for (CoinTokenVo coinTokenVo:coinTokenVoList) {
//                   coins += "," + coinTokenVo.getCoinUnit();
//               }
               for(int i=0;i<coinTokenVoList.size();i++){
                   coins.append(",").append(coinTokenVoList.get(i));
               }
           }
           RPCUtil.coins = coins.toString();
           withdrawRecordService.audit(ids, WAITING, restTemplate);
       }catch (Exception e){
           return error(e.getMessage());
       }
       return success("审核通过");
    }



    /**
     * 一键审核不通过
     * @param ids
     * @return
     */
    @RequiresPermissions("finance:withdraw-record:audit-no-pass")
    @PatchMapping("/audit-no-pass")
    @AccessLog(module = AdminModule.FINANCE, operation = "提现记录WithdrawRecord一键审核不通过")
    public MessageResult auditNoPass(@RequestParam("ids") Long[] ids) {
        try{
            withdrawRecordService.audit(ids, FAIL, restTemplate);
        }catch (Exception e){
            return error(e.getMessage());
        }
        return success("审核不通过");
    }

    /**
     * 单个打款 转账成功添加流水号
     * @param id
     * @param transactionNumber
     * @return
     */
    @RequiresPermissions("finance:withdraw-record:add-transaction-number")
    @PatchMapping("/add-transaction-number")
    @AccessLog(module = AdminModule.FINANCE, operation = "添加交易流水号")
    public MessageResult addNumber(@RequestParam("id") Long id,
                                   @RequestParam("transactionNumber") String transactionNumber){
           WithdrawRecord record = withdrawRecordService.findOne(id);
           Assert.notNull(record,"该记录不存在");
           Assert.isTrue(record.getIsAuto()== BooleanEnum.IS_FALSE,"该提现单为自动审核");
           record.setTransactionNumber(transactionNumber);
        //edit by tansitao 时间： 2018/5/14 原因：修改状态为放币中
           record.setStatus(WithdrawStatus.PUTING);
           Coin coin = record.getCoin();
           //add by tansitao 时间： 2018/5/1 原因：声明变量主币，主币钱包,
           Coin baseCoin = null;
           isTrue(coin != null, "COIN_ILLEGAL");


        //add by tansitao 时间： 2018/7/31 原因：判断是否为内部转账
        if(memberWalletService.hasExistByAddr(record.getAddress())){
            log.info("==========进入平台互转 withdrawRecord={}==========",record);
            String txid = UUIDUtil.getUUID();
            MessageResult result = walletService.recharge(coin, record.getAddress(), record.getArrivedAmount(), txid);
            if(result.getCode() != 0){
                throw new IllegalArgumentException("放币失败FBEX001");
            }
            //处理成功,data为txid，更新业务订单
            try {
                withdrawRecordService.withdrawSuccess(record.getId(), txid);
            }
            catch (Exception e){
                log.error("===============内部互转失败==================withdrawRecordId" + record.getId(), e);
                throw new IllegalArgumentException("放币失败FBEX002");
            }
        }
        //add by tansitao 时间： 2018/7/31 原因：不是内部互转，走区块链
        else{
            //add by tansitao 时间： 2018/9/7 原因：从数据库查询带币并设置到coins
            StringBuilder coins = new StringBuilder("ETH,USDT");
            List<CoinTokenVo> coinTokenVoList = coinTokenService.findAll();
            if(coinTokenVoList != null){
//               for (CoinTokenVo coinTokenVo:coinTokenVoList) {
//                   coins += "," + coinTokenVo.getCoinUnit();
//               }
                for(int i=0;i<coinTokenVoList.size();i++){
                    coins.append(",").append(coinTokenVoList.get(i));
                }
            }
            RPCUtil.coins = coins.toString();
            RPCUtil rpcUtil = new RPCUtil();
            //add by tansitao 时间： 2018/5/1 原因：添加对主币、带币是否足够的判断
            //判断平台的该币余额是否足够
            if(!rpcUtil.balanceIsEnough(interfaceLogService, restTemplate, coin, record.getArrivedAmount()))
            {
                //余额不足归集钱包
                rpcUtil.collectCoin(interfaceLogService, restTemplate, coin);
                throw new IllegalArgumentException(coin.getUnit() + "余额不足请充值");
            }
            //判断是否为带币
            if (!StringUtils.isEmpty(coin.getBaseCoinUnit()))
            {
                //判断主币余额是否足够
                baseCoin = coinService.findByUnit(coin.getBaseCoinUnit());
                if(!rpcUtil.balanceIsEnough(interfaceLogService, restTemplate, baseCoin, record.getBaseCoinFree()))
                {
                    //余额不足归集钱包
                    rpcUtil.collectCoin(interfaceLogService, restTemplate,  baseCoin);
                    throw new IllegalArgumentException(baseCoin.getUnit() + "余额不足请充值");
                }
            }

            // 提交提现请求给wallet模块################################# shengzc 新增
            JSONObject json = new JSONObject();
            json.put("uid", record.getMemberId());
            //提币总数量
            json.put("totalAmount", record.getTotalAmount());
            //手续费
            json.put("fee", record.getFee());
            //预计到账数量
            json.put("arriveAmount", sub(record.getTotalAmount(), record.getFee()));
            //币种
            json.put("coin", record.getCoin());
            //提币地址
            json.put("address", record.getAddress());
            //提币记录id
            json.put("withdrawId", record.getId());
            kafkaTemplate.send("withdraw", record.getCoin().getUnit(), json.toJSONString());
            // ######################################################### shengzc 新增
            record = withdrawRecordService.save(record);
        }
        return MessageResult.success("添加流水号成功",record);
    }

    /**
     *  批量打款
     * @param admin
     * @param ids
     * @param transactionNumber
     * @param password
     * @return
     */
    @RequiresPermissions("finance:withdraw-record:remittance")
    @PatchMapping("/remittance")
    @AccessLog(module = AdminModule.FINANCE, operation = "提现记录/批量打款")
    //edit by tansitao 时间： 2018/5/14 原因：取消事务回滚
    @Transactional(rollbackFor = Exception.class)
    public MessageResult remittance(
            @SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin,
            @RequestParam("ids") Long[] ids,
            @RequestParam("transactionNumber") String transactionNumber,
            @RequestParam("password")String password) {
        Assert.notNull(admin,"会话已过期，请重新登录");
        password = Encrypt.MD5(password + md5Key);
        if(!password.equals(admin.getPassword())) {
            return error("密码错误,请重新输入");
        }
        WithdrawRecord withdrawRecord;
        //add by tansitao 时间： 2018/5/3 原因：修改批量提币逻辑为，同币种的币提币数量求和后判断余额是否足够
        WithdrawRecord withdrawRecordTemp;
        HashMap<String, BigDecimal> allTakeCoinInfo = new HashMap<String, BigDecimal>();
        RPCUtil rpcUtil = new RPCUtil();
        //循环遍历所有提币订单，将相同的币种提币订单数量加在一起
        for (Long id : ids)
        {
            withdrawRecordTemp = withdrawRecordService.findOne(id);
            notNull(withdrawRecordTemp, "id :" + id + "数据为空!");
            isTrue(withdrawRecordTemp.getStatus() == WAITING, "提现状态不是等待放币,不能打款!");
            isTrue(withdrawRecordTemp.getIsAuto() == IS_FALSE, "不是人工审核提现!");
            Coin coin = withdrawRecordTemp.getCoin();
            //对所有提币单，进行提币数量求和
            if(allTakeCoinInfo.containsKey(coin.getUnit()))
            {
                //求和同币种提币数量
                BigDecimal sumTemp = allTakeCoinInfo.get(coin.getUnit()).add(withdrawRecordTemp.getArrivedAmount());
                allTakeCoinInfo.put(coin.getUnit(), sumTemp);
            }
            else
            {
                allTakeCoinInfo.put(coin.getUnit(), withdrawRecordTemp.getArrivedAmount());
            }

            //判断是否有主币
            if (!StringUtils.isEmpty(coin.getBaseCoinUnit()))
            {
                //对所有提币单，进行主币数量求和
                if(allTakeCoinInfo.containsKey(coin.getBaseCoinUnit()))
                {
                    //求和同币种提币数量
                    BigDecimal sumTemp =  allTakeCoinInfo.get(coin.getBaseCoinUnit()).add(withdrawRecordTemp.getBaseCoinFree());
                    allTakeCoinInfo.put(coin.getBaseCoinUnit(), sumTemp);
                }
                else
                {
                    allTakeCoinInfo.put(coin.getBaseCoinUnit(), withdrawRecordTemp.getBaseCoinFree());
                }
            }
        }
        Iterator iter = allTakeCoinInfo.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry<String, BigDecimal> entry = (Map.Entry) iter.next();
            System.out.println(entry.getKey() + entry.getValue());
            Coin coin = coinService.findByUnit(entry.getKey());
            if(coin == null){
                continue;
            }
            //判断平台的该币余额是否足够
            if(!rpcUtil.balanceIsEnough(interfaceLogService, restTemplate,coin , entry.getValue()))
            {
                //余额不足归集钱包
                rpcUtil.collectCoin(interfaceLogService, restTemplate, coin);
                throw new IllegalArgumentException(entry.getKey() + "余额不足请充值");
            }
        }

        for (Long id : ids) {
            withdrawRecord = withdrawRecordService.findOne(id);

            //标记提现完成
            //edit by tansitao 时间： 2018/5/14 原因：修改状态为放币中
            withdrawRecord.setStatus(PUTING);
            //交易编码
            withdrawRecord.setTransactionNumber(transactionNumber);
            // 提交提现请求给wallet模块################################# shengzc 新增
            JSONObject json = new JSONObject();
            json.put("uid", withdrawRecord.getMemberId());
            //提币总数量
            json.put("totalAmount", withdrawRecord.getTotalAmount());
            //手续费
            json.put("fee", withdrawRecord.getFee());
            //预计到账数量
            json.put("arriveAmount", sub(withdrawRecord.getTotalAmount(), withdrawRecord.getFee()));
            //币种
            json.put("coin", withdrawRecord.getCoin());
            //提币地址
            json.put("address", withdrawRecord.getAddress());
            //提币记录id
            json.put("withdrawId", withdrawRecord.getId());
            kafkaTemplate.send("withdraw", withdrawRecord.getCoin().getUnit(), json.toJSONString());
            // ######################################################### shengzc 新增
            withdrawRecordService.save(withdrawRecord);
        }
        return success();
    }



}
