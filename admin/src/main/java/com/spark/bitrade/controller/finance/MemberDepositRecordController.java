package com.spark.bitrade.controller.finance;


import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.model.screen.CollectScreen;
import com.spark.bitrade.model.screen.FincPlatStatScreen;
import com.spark.bitrade.model.screen.MemberDepositScreen;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.ExcelUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import com.spark.bitrade.vo.MemberDepositVO;
import com.spark.bitrade.vo.TotalBalanceVo;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("finance/member-deposit")
public class MemberDepositRecordController extends BaseAdminController{

    @Autowired
    private MemberService memberService ;

    @Autowired
    private CollectLogService collectLogService ;//add by tansitao 时间： 2018/5/14 原因：增加归集日志

    @Autowired
    private FincPlatStatService fincPlatStatService ; //add by tansitao 时间： 2018/5/14 原因：增加平台货币统计

    @Autowired
    private MemberDepositService memberDepositService ;

    @Autowired
    private TotalBalanceStatService totalBalanceStatService;

    /**
     * 充币记录
     * @param
     * @param screen
     * @return
     */
    @RequiresPermissions("finance:member-deposit:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.FINANCE,operation = "充币记录") //add by yangch 时间： 2018.04.29 原因：合并
//    public MessageResult page(PageModel pageModel, MemberDepositScreen screen){
//        List<BooleanExpression> predicates = new ArrayList<>();
//        predicates.add(QMember.member.id.eq(QMemberDeposit.memberDeposit.memberId)); //add by yangch 时间： 2018.04.29 原因：合并
//        if(!StringUtils.isEmpty(screen.getUnit()))
//            predicates.add((QMemberDeposit.memberDeposit.unit.equalsIgnoreCase(screen.getUnit())));
//        if(!StringUtils.isEmpty(screen.getAddress()))
//            predicates.add((QMemberDeposit.memberDeposit.address.eq(screen.getAddress())));
//        if (!StringUtils.isEmpty(screen.getAccount()))
//            predicates.add(QMember.member.username.like("%"+screen.getAccount()+"%")
//                    .or(QMember.member.realName.like("%"+screen.getAccount()+"%")));
//        //edit by yangch 时间： 2018.04.29 原因：合并
//        Page<MemberDepositVO> page = memberDepositService.page(predicates,pageModel);
//
//        /*Page<MemberDeposit> page = memberDepositService.pageQuery(predicates,pageModel);
//
//        for(MemberDeposit memberDeposit:page.getContent()){
//            memberDeposit.setUsername(memberService.findUserNameById(memberDeposit.getMemberId()));
//        }*/
//        return success("分页查询成功",page);
//    }
    public MessageResult page(MemberDepositScreen screen, @RequestParam(required = false) Integer pageNo, @RequestParam(required = false) Integer pageSize){
        if(pageNo == null && pageSize == null){
            pageNo =0;
            pageSize = 10;
        }
        Map<String,Object> params= new HashMap<>();
        params.put("address",screen.getAddress());
        params.put("unit",screen.getUnit());
        params.put("account",screen.getAccount());
        PageInfo<MemberDepositVO> pageInfo=memberDepositService.page(params,pageNo,pageSize);
        return success(PageData.toPageData(pageInfo));
    }




    //add|edit|del by tansitao 时间： 2018/5/12 原因：增加归集明细查询
    /**
     * 归集明细查询
     * @param pageModel
     * @param screen
     * @return
     */
    @RequiresPermissions("finance:member-deposit:collectLog-query")
    @PostMapping("collectLog-query")
    @AccessLog(module = AdminModule.FINANCE,operation = "归集明细")
    public MessageResult collectLogPage(PageModel pageModel, CollectScreen screen){
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (!StringUtils.isEmpty(screen.getSymbol()))
        {
            booleanExpressions.add(QCollectionLog.collectionLog.coin.eq(screen.getSymbol()));
        }
        if (!StringUtils.isEmpty(screen.getStartTime()))
        {
            booleanExpressions.add(QCollectionLog.collectionLog.createTime.gt(screen.getStartTime()));
        }
        if (!StringUtils.isEmpty(screen.getEndTime()))
        {
            booleanExpressions.add(QCollectionLog.collectionLog.createTime.lt(screen.getEndTime()));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<CollectionLog> all = collectLogService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }


    /**
     * 添加归集明细导出
     * @author shenzucai
     * @time 2018.06.13 15:07
     * @param pageModel
     * @param screen
     * @param response
     * @return true
     */
    @RequiresPermissions("finance:member-deposit:collectLog-query-out-excel")
    @GetMapping("collectLog-query/out-excel")
    @AccessLog(module = AdminModule.FINANCE,operation = "归集明细导出")
    public void collectLogPageOutExcel(PageModel pageModel, CollectScreen screen,HttpServletResponse response) throws IOException {
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (!StringUtils.isEmpty(screen.getSymbol()))
        {
            booleanExpressions.add(QCollectionLog.collectionLog.coin.eq(screen.getSymbol()));
        }
        if (!StringUtils.isEmpty(screen.getStartTime()))
        {
            booleanExpressions.add(QCollectionLog.collectionLog.createTime.gt(screen.getStartTime()));
        }
        if (!StringUtils.isEmpty(screen.getEndTime()))
        {
            booleanExpressions.add(QCollectionLog.collectionLog.createTime.lt(screen.getEndTime()));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        List<CollectionLog> list = collectLogService.findAll(predicate);
        for (CollectionLog collectionLog : list){
            collectionLog.setTime(DateUtil.dateToString(collectionLog.getCreateTime()));
        }
        String fileName="collectionLog_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,CollectionLog.class.getDeclaredFields(),response,fileName);
//        ExcelUtil.listToExcel(list,CollectionLog.class.getDeclaredFields(),response.getOutputStream(),CollectionLog.class.getName());
    }

    //add|edit|del by tansitao 时间： 2018/5/12 原因：增加平台货币统计查询
    /**
     * 平台货币统计查询
     * @param pageModel
     * @param screen
     * @return
     */
    @RequiresPermissions("finance:member-deposit:statCoinInfo-query")
    @PostMapping("statCoinInfo-query")
    @AccessLog(module = AdminModule.FINANCE,operation = "货币统计")
    public MessageResult statCoinInfo(PageModel pageModel, FincPlatStatScreen screen){
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (!StringUtils.isEmpty(screen.getSymbol()))
        {
            booleanExpressions.add(QFincPlatStat.fincPlatStat.unit.eq(screen.getSymbol()));
        }
        if (!StringUtils.isEmpty(screen.getStartTime()))
        {
            booleanExpressions.add(QFincPlatStat.fincPlatStat.time.goe(screen.getStartTime()));
        }
        if (!StringUtils.isEmpty(screen.getEndTime()))
        {
            booleanExpressions.add(QFincPlatStat.fincPlatStat.time.loe(screen.getEndTime()));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<FincPlatStat> all = fincPlatStatService.findAll(predicate, pageModel.getPageable());

        Date nowDay = new Date();
        Date yesterday = nowDay;
        String endTime = DateUtil.getDate(new Date()) +" 16:00:00";
        Date endDate = DateUtil.stringToDate(endTime);


        //计算客户币数，钱包币数，公司笔数
        for(int i=0;i< all.getContent().size();i++){
            //
            //客户币数 = 平台币数 - 平台操盘币数 - 平台内部商户币数
            BigDecimal customerTotal = all.getContent().get(i).getPlatAllTotal().
                    subtract(all.getContent().get(i).getPlatTraderTotal()).
                    subtract(all.getContent().get(i).getPlatInnerTotal());
            all.getContent().get(i).setCustomerTotal(customerTotal);

            //钱包币数 = 总额日统计钱包余额
            //查询总额日统计钱包余额
            TotalBalanceVo totalBalanceVo;
            if( "total".equals(all.getContent().get(i).getDate()) ){
                if(nowDay.getTime() < endDate.getTime()){//如果当天时间小于结束统计时间，则执行为前一天时间
                    yesterday = DateUtil.addDay(nowDay,-1);
                }
                totalBalanceVo = totalBalanceStatService.getDayOfWalletBalance(all.getContent().get(i).getUnit(), DateUtil.getDate(yesterday));
            }else {
                totalBalanceVo = totalBalanceStatService.getDayOfWalletBalance(all.getContent().get(i).getUnit(), all.getContent().get(i).getDate());
            }
            if(totalBalanceVo==null) {
                totalBalanceVo = new TotalBalanceVo();
                totalBalanceVo.setAllBalance(BigDecimal.ZERO);
                totalBalanceVo.setHotAllBalance(BigDecimal.ZERO);
            }
            all.getContent().get(i).setPlatAllTotal(totalBalanceVo.getAllBalance());//设置会员总币数（取用总额查询：会员总余额）
            all.getContent().get(i).setWalletTotal(totalBalanceVo.getHotAllBalance());


            BigDecimal walletBalance = totalBalanceVo.getHotAllBalance();
            //公司币数 = 钱包币数 - 客户币数
            BigDecimal company = walletBalance.subtract(customerTotal);
            all.getContent().get(i).setCompanyTotal(company);

            //获取外购币数，平台提出数
            Map<String,Object> map = memberDepositService.getFixMemberStat(all.getContent().get(i).getDate(),all.getContent().get(i).getUnit());
            all.getContent().get(i).setOuterInPlatTotal(new BigDecimal(map.get("outerInPlat").toString()));
            all.getContent().get(i).setPlatWithdrawTotal(new BigDecimal(map.get("platWithdraw").toString()));
        }

        return success(all);
    }


    /**
     * 添加平台货币统计导出
     * @author shenzucai
     * @time 2018.06.13 15:07
     * @param pageModel
     * @param screen
     * @param response
     * @return true
     */
    @RequiresPermissions("finance:member-deposit:statCoinInfo-out-excel")
    @GetMapping("statCoinInfo-query/out-excel")
    @AccessLog(module = AdminModule.FINANCE,operation = "平台货币统计导出")
    public void statCoinInfoOutExcel(PageModel pageModel, CollectScreen screen,HttpServletResponse response) throws IOException {
//        //条件
//        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
//        if (!StringUtils.isEmpty(screen.getSymbol()))
//        {
//            booleanExpressions.add(QFincPlatStat.fincPlatStat.unit.eq(screen.getSymbol()));
//        }
//        if (!StringUtils.isEmpty(screen.getStartTime()))
//        {
//            booleanExpressions.add(QFincPlatStat.fincPlatStat.time.goe(screen.getStartTime()));
//        }
//        if (!StringUtils.isEmpty(screen.getEndTime()))
//        {
//            booleanExpressions.add(QFincPlatStat.fincPlatStat.time.loe(screen.getEndTime()));
//        }
//        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
//        List<FincPlatStat> list = fincPlatStatService.findAll(predicate);
        //edit by zyj 2018-12-17: 重写导出
        List<FincPlatStat> list = fincPlatStatService.findAllBy(screen.getStartTime(),screen.getEndTime());

                Date nowDay = new Date();
        Date yesterday = nowDay;
        String endTime = DateUtil.getDate(new Date()) +" 16:00:00";
        Date endDate = DateUtil.stringToDate(endTime);

        //计算客户币数，钱包币数，公司笔数
        for(int i=0;i< list.size();i++){
            //客户币数 = 平台币数 - 平台操盘币数 - 平台内部商户币数
            BigDecimal customerTotal = list.get(i).getPlatAllTotal().
                    subtract(list.get(i).getPlatTraderTotal()).
                    subtract(list.get(i).getPlatInnerTotal());
            list.get(i).setCustomerTotal(customerTotal);

            //钱包币数 = 总额日统计钱包余额
            //查询总额日统计钱包余额
            TotalBalanceVo totalBalanceVo;
            if( "total".equals(list.get(i).getDate()) ){
                if(nowDay.getTime() < endDate.getTime()){//如果当天时间小于结束统计时间，则执行为前一天时间
                    yesterday = DateUtil.addDay(nowDay,-1);
                }
                totalBalanceVo = totalBalanceStatService.getDayOfWalletBalance(list.get(i).getUnit(), DateUtil.getDate(yesterday));
            }else {
                totalBalanceVo = totalBalanceStatService.getDayOfWalletBalance(list.get(i).getUnit(), list.get(i).getDate());
            }
            if(totalBalanceVo==null) {
                totalBalanceVo = new TotalBalanceVo();
                totalBalanceVo.setAllBalance(BigDecimal.ZERO);
                totalBalanceVo.setHotAllBalance(BigDecimal.ZERO);
            }
            list.get(i).setPlatAllTotal(totalBalanceVo.getAllBalance());//设置会员总币数（取用总额查询：会员总余额）
            list.get(i).setWalletTotal(totalBalanceVo.getHotAllBalance());

            BigDecimal walletBalance = totalBalanceVo.getHotAllBalance();

            //公司币数 = 钱包币数 - 客户币数
            BigDecimal company = walletBalance.subtract(customerTotal);
            list.get(i).setCompanyTotal(company);


            //获取外购币数，平台提出数
            Map<String,Object> map = memberDepositService.getFixMemberStat(list.get(i).getDate(),list.get(i).getUnit());
            list.get(i).setOuterInPlatTotal(new BigDecimal(map.get("outerInPlat").toString()));
            list.get(i).setPlatWithdrawTotal(new BigDecimal(map.get("platWithdraw").toString()));
        }
        String fileName="fincPlatStat_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,FincPlatStat.class.getDeclaredFields(),response,fileName);
//        ExcelUtil.listToExcel(list,FincPlatStat.class.getDeclaredFields(),response.getOutputStream(),FincPlatStat.class.getName());
    }
}
