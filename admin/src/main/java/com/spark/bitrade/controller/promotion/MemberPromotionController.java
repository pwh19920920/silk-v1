package com.spark.bitrade.controller.promotion;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.controller.BaseController;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.service.MemberPromotionService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.PromotionMemberVO;
import com.spark.bitrade.vo.RegisterPromotionVO;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

//edit by yangch 时间： 2018.04.29 原因：合并
@RestController
@RequestMapping("promotion/member")
public class MemberPromotionController extends BaseController {

    @Autowired
    private MemberService memberService ;

    @Autowired
    private MemberPromotionService memberPromotionService ;


    /**
     * @author lingxing 修改 2018 08:06 13.12
     * @throws Exception
     */
    @PostMapping("page-query")
    @RequiresPermissions("promotion:member-page-query")
    @AccessLog(module = AdminModule.PROMOTION,operation = "推荐会员分页查询")
    public MessageResult page(String account, Integer pageNo, Integer pageSize){
        if(pageNo==null){
            pageNo =0;
        }
        if(pageSize==null){
            pageSize=10;
        }
        PageInfo pageInfo=memberService.findByPage(account, PageData.pageNo4PageHelper(pageNo),pageSize);
        return success(PageData.toPageData(pageInfo));
    }


    @RequiresPermissions("promotion:member-details")
    @PostMapping("details")
    @AccessLog(module = AdminModule.PROMOTION,operation = "推荐会员明细")
    public MessageResult promotionDetails(PageModel pageModel,
                                          @RequestParam("memberId")Long memberId){
        pageModel.setSort();
        Page<RegisterPromotionVO> page = memberPromotionService.getPromotionDetails(memberId,pageModel);
        return MessageResult.getSuccessInstance("",page);
    }


    /**
     * @author lingxing 修改 2018 08:06 13.12
     * @param response
     * @throws Exception
     */
    @RequiresPermissions("promotion:member-out-excel")
    @GetMapping("out-excel")
    public void outExcel(String account,HttpServletResponse response) throws Exception {
        List<PromotionMemberVO> list=memberService.findByPromotionMemberAllForOut(account);

        String fileName="promotionMember_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
//        ExcelUtil.listToExcel(list,PromotionMemberVO.class.getDeclaredFields(),response.getOutputStream());
        ExcelUtil.listToCSV(list,PromotionMemberVO.class.getDeclaredFields(),response,fileName);
    }

}
