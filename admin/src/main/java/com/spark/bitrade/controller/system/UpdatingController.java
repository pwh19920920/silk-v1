package com.spark.bitrade.controller.system;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.model.screen.TransferAddressScreen;
import com.spark.bitrade.service.UpdatingService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import com.sparkframework.security.Encrypt;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static com.spark.bitrade.util.MessageResult.error;

/**
 * @author lingxing
 * @time 2018.07.21 09:15
 */
@RestController
@RequestMapping("/system/updating")
public class UpdatingController {
      @Value("${spark.system.md5.key}")
    private String md5Key;

    @Autowired
    UpdatingService updatingService;
//    public MessageResult addUpdating(@Valid TransferAddress transferAddress , @RequestParam("coinName") String coinName){
//        updatingService.
//        return MessageResult.success("保存成功");
//    }

    /**
     * 查询全部
     * @param pageModel
     * @return
     */
    @PostMapping("page-query")
    public MessageResult pageQuery(PageModel pageModel){
        List<Updating> updatingList=updatingService.getAll();
        return MessageResult.getSuccessInstance("获取成功",updatingList);
    }

    /**
     * 平台升级修改
     * @param id
     * @param updating
     * @param admin
     * @param password
     * @return
     */
    @PutMapping("{id}")
    public MessageResult update(@PathVariable("id")Integer id, Updating updating, @SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin,@RequestParam("password")String password){
        Assert.notNull(admin,"会话已过期，请重新登录");
        password = Encrypt.MD5(password + md5Key);
        if(!password.equals(admin.getPassword())) {
            return error("密码错误,请重新输入");
        }
        updating.setId(id);
        updatingService.update(updating);
        return MessageResult.success("修改成功");
    }

    /**
     * 查询单个升级详情
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public  MessageResult findByUpdating(@PathVariable("id")Integer id){
        return MessageResult.success("返回成功",updatingService.findByUpdatingId(id));
    }
}
