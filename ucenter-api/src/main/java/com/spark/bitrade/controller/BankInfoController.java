package com.spark.bitrade.controller;

import com.baomidou.mybatisplus.mapper.Condition;
import com.spark.bitrade.entity.BankInformation;
import com.spark.bitrade.service.BankInformationService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.vo.BankInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.09.23 14:38  
 */
@RestController
@RequestMapping("bankInfo")
@Api(description = "银行卡列表", value = "银行卡列表")
public class BankInfoController extends BaseController {

    @Autowired
    private BankInformationService bankInformationService;

    @GetMapping("list")
    @ApiOperation(value = "银行卡列表", tags = "银行卡列表")
    public MessageResult list(HttpServletRequest request) {
        String language = request.getHeader("language");
        if(StringUtils.isEmpty(language)){
            language="zh_CN";
        }
        List<BankInformation> bankInformationList = bankInformationService.selectList(Condition.create().eq("status", 1));
        List<BankInfoVo> bankInfoVos=new ArrayList<>();
        for (BankInformation information:bankInformationList){
            BankInfoVo vo=new BankInfoVo();
            vo.setId(information.getId());
            switch (language){
                case "zh_CN":
                    vo.setBankName(information.getBankNameZh());
                    break;
                case "ko_KR":
                    vo.setBankName(information.getBankNameKo());
                    break;
                case "zh_HK":
                    vo.setBankName(information.getBankNameHk());
                    break;
                case "en_US":
                    vo.setBankName(information.getBankNameEn());
                    break;
                    default:break;
            }
            bankInfoVos.add(vo);
        }

        return success(bankInfoVos);
    }


}
