package com.spark.bitrade.controller.system;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.entity.CoinToken;
import com.spark.bitrade.service.CoinTokenService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.vo.CoinTokenVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fumy
 * @time 2018.09.05 10:28
 */
@RestController
@RequestMapping("/system/coinToken")
@Slf4j
public class CoinTokenController extends BaseAdminController {

    private Logger logger = LoggerFactory.getLogger(BaseAdminController.class);

    @Autowired
    CoinTokenService coinTokenService;


    /**
     * 分页查询代币列表
     * @author fumy
     * @time 2018.09.05 10:46
     * @param pageNo
     * @param pageSize
     * @return true
     */
    @PostMapping("page-query")
    @RequiresPermissions("system:coin-publish-page-query")
    public MessageResult findByPage(String coinUnit,String contractAddress,String coinName,int pageNo,int pageSize){
        Map<String,String> params = new HashMap<>();
        params.put("coinUnit",coinUnit);
        params.put("contractAddress",contractAddress);
        params.put("coinName",coinName);
        PageInfo<CoinTokenVo> page = coinTokenService.findByPage(params, PageData.pageNo4PageHelper(pageNo),pageSize);
        return success(PageData.toPageData(page));
    }

    @PostMapping("update")
    @RequiresPermissions("system:coin-publish-edit")
    public MessageResult updateById(CoinToken coinToken){
        CoinToken res = coinTokenService.updateById(coinToken);
            return success(res);

    }

    @PostMapping("add")
    @RequiresPermissions("system:coin-publish-create")
    public MessageResult add(CoinToken coinToken){
        CoinToken res = coinTokenService.insertNew(coinToken);
        return success(res);
    }

    @GetMapping("del")
    @RequiresPermissions("system:coin-publish-del")
    public MessageResult del(Long id){
        coinTokenService.deleteById(id);
        return success();
    }
}
