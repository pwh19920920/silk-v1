
package com.spark.bitrade.controller.system;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.SysConstant;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.core.Menu;
import com.spark.bitrade.entity.Admin;
import com.spark.bitrade.entity.Department;
import com.spark.bitrade.entity.QAdmin;
import com.spark.bitrade.entity.SysRole;
import com.spark.bitrade.service.AdminService;
import com.spark.bitrade.service.DepartmentService;
import com.spark.bitrade.service.SysPermissionService;
import com.spark.bitrade.service.SysRoleService;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vendor.provider.SMSProvider;
import com.spark.bitrade.vendor.provider.SMSProviderProxy;
import com.sparkframework.lang.Convert;
import com.sparkframework.security.Encrypt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.spark.bitrade.constant.SysConstant.SESSION_ADMIN;


/**
 * @author Zhang Jinwei
 * @date 2017年12月19日
 */


@Slf4j
@Controller
@RequestMapping("/system/employee")
public class EmployeeController extends BaseAdminController {

    @Value("${spark.system.md5.key}")
    private String md5Key;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private DepartmentService departmentService;
    @Resource
    private SysPermissionService sysPermissionService;

    @Autowired
    private RedisTemplate redisTemplate ;

    @Autowired
    private SMSProviderProxy smsProvider ;

    //是否启用登录的短信
    @Value("${sms.admin.login.enable:true}")
    private Boolean smsLoginEnable;


    /**
     * 提交登录信息
     *
     * @param request
     * @return
     */


    @RequestMapping(value = "sign/in")
    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "提交登录信息Admin")
    public MessageResult doLogin(@SessionAttribute("username")String username,
                                 @SessionAttribute("password")String password,
                                 @SessionAttribute("phone")String phone,String code,
                                 @RequestParam(value="rememberMe",defaultValue = "true")boolean rememberMe, //add by yangch 时间： 2018.04.26 原因：合并新增
                                 HttpServletRequest request) {
        Assert.isTrue(StringUtils.isNotEmpty(username)&&StringUtils.isNotEmpty(password)&&StringUtils.isNotEmpty(phone),"会话已过期");
        ValueOperations valueOperations = redisTemplate.opsForValue() ;
        if(smsLoginEnable) {
            //Assert.isNull(code,"请输入验证码");
            Assert.isTrue(org.springframework.util.StringUtils.hasText(code),"请输入验证码");
            String cacheCode = valueOperations.get(SysConstant.ADMIN_LOGIN_PHONE_PREFIX + phone).toString();
            Assert.notNull(cacheCode,"验证码已经被清除，请重新发送");
            if (!code.equals(cacheCode.toString())) {
                return error("手机验证码错误，请重新输入");
            }
        }
        try {
            log.info("md5Key {}", md5Key);

            //password = Encrypt.MD5(password + md5Key);
            //edit by yangch 时间： 2018.04.26 原因：合并
            //UsernamePasswordToken token = new UsernamePasswordToken(username, password);
            UsernamePasswordToken token = new UsernamePasswordToken(username, password,true);
            token.setHost(getRemoteIp(request));
            //token.setRememberMe(true); //del by yangch 时间： 2018.04.26 原因：代码合并
            SecurityUtils.getSubject().login(token);

            valueOperations.getOperations().delete(SysConstant.ADMIN_LOGIN_PHONE_PREFIX+phone);
            Admin admin = (Admin) request.getSession().getAttribute(SysConstant.SESSION_ADMIN);
            request.getSession().getAttribute(SysConstant.SESSION_ADMIN);
            //token.setRememberMe(true);

            //获取当前用户的菜单权限
            List<Menu> list;
            //edit by yangch 时间： 2018.04.29 原因：合并
            if (admin.getRoleId()==1) {
                //if (admin.getUsername().equals("root")) {
                list = sysRoleService.toMenus(sysPermissionService.findAll(), 0L);
            } else {
                list = sysRoleService.toMenus(sysRoleService.getPermissions(admin.getRoleId()), 0L);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("permissions", list);
            map.put("admin", admin);
            return success("登录成功", map);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return error(e.getMessage());
        }
    }


    @RequestMapping(value = "/check")
    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "判断后台登录输入手机验证码")
    public MessageResult valiatePhoneCode(HttpServletRequest request){
        String username = Convert.strToStr(request(request, "username"), "");
        String password = Convert.strToStr(request(request, "password"), "");
        String captcha = Convert.strToStr(request(request, "captcha"), "");
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return error("用户名或密码不能为空");
        }
        HttpSession session = request.getSession();
        if (StringUtils.isBlank(captcha)) {
            return error("验证码不能为空");
        }
        String ADMIN_LOGIN = "ADMIN_LOGIN";
        if (!CaptchaUtil.validate(session, ADMIN_LOGIN, captcha)) {
            return error("验证码不正确");
        }
        password = Encrypt.MD5(password + md5Key);
        Admin admin = adminService.login(username,password);
        if(admin==null){
            return error("用户名或密码不存在");
        }else{
            //del by yangch 时间： 2018.04.26 原因：合并
            //String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
            try {
                request.getSession().setAttribute("username",username);
                request.getSession().setAttribute("password",password);
                request.getSession().setAttribute("phone",admin.getMobilePhone());
                Map<String,Object>map=new HashMap<>();
                map.put("phone",admin.getMobilePhone());
                map.put("VerificationType",admin.getVerificationType());
                return success(map);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return error("手机验证码发送或保存失败");
        }
    }






    /**
     * 退出登录
     *
     * @return
     */


    @RequestMapping(value = "logout")
    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "退出登录")
    public MessageResult logout() {
        SecurityUtils.getSubject().logout();
        return success();
    }



    /**
     * 创建或更改后台用户
     *
     * @param admin
     * @param bindingResult
     * @return
     */


    @RequiresPermissions("system:employee:merge")
    @RequestMapping(value = "/merge")
    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "创建或更改后台用户")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult addAdmin(Admin admin,
                                  @RequestParam("departmentId") Long departmentId,
                                  BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        Assert.notNull(departmentId, "请选择部门");
        Department department = departmentService.findOne(departmentId);
        admin.setDepartment(department);
        String password;
        if (admin.getId() != null) {//修改
            Admin admin1 = adminService.findOne(admin.getId());
            admin.setLastLoginIp(admin1.getLastLoginIp());
            admin.setLastLoginTime(admin1.getLastLoginTime());
            //如果密码不为null更改密码
            if (StringUtils.isNotBlank(admin.getPassword())) {
                password = Encrypt.MD5(admin.getPassword() + md5Key);
            } else {
                password = admin1.getPassword();
            }
        } else {//创建
            //这里是新增
            Admin a = adminService.findByUsername(admin.getUsername());
            if (a != null)
                return error("用户名已存在！");
            if (StringUtils.isBlank(admin.getPassword())) {
                return error("密码不能为空");
            }
            password = Encrypt.MD5(admin.getPassword() + md5Key);

        }
        admin.setPassword(password);
        admin.setGoogleState(0);
        admin.setVerificationType(0);
        admin.setGoogleKey("");
        adminService.saveAdmin(admin);
        return success("操作成功");
    }

    @ResponseBody
    @RequiresPermissions("system:employee:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "分页查找后台用户admin")
    public MessageResult findAllAdminUser(
            PageModel pageModel,
            @RequestParam(value = "searchKey", defaultValue = "") String searchKey) {
        BooleanExpression predicate = QAdmin.admin.username.notEqualsIgnoreCase("root");
        if (StringUtils.isNotBlank(searchKey)) {
            predicate.and(QAdmin.admin.email.like(searchKey)
                    .or(QAdmin.admin.realName.like(searchKey))
                    .or(QAdmin.admin.mobilePhone.like(searchKey))
                    .or(QAdmin.admin.username.like(searchKey)));
        }
        Page<Admin> all = adminService.findAll(predicate, pageModel.getPageable());
        for (Admin admin : all.getContent()) {
            SysRole role = sysRoleService.findOne(admin.getRoleId());
            admin.setRoleName(role.getRole());
        }
        return success(all);
    }

    @RequiresPermissions("system:employee:update-password")
    @PostMapping("update-password")
    @ResponseBody
    public MessageResult updatePassword(Long id, String lastPassword, String newPassword) {
        Assert.notNull(id, "admin id 不能为null");
        Assert.notNull(lastPassword, "请输入原密码");
        Assert.notNull(newPassword, "请输入新密码");
        Admin admin = adminService.findOne(id);
        lastPassword = Encrypt.MD5(lastPassword + md5Key);
        Assert.isTrue(lastPassword.equalsIgnoreCase(admin.getPassword()), "密码错误");
        admin.setPassword(Encrypt.MD5(newPassword + md5Key));
        adminService.save(admin);
        return MessageResult.success("修改密码成功");
    }


    @PostMapping("reset-password")
    @ResponseBody
    public MessageResult resetPassword(Long id) {
        Assert.notNull(id, "admin id 不能为null");
        Admin admin = adminService.findOne(id);
        admin.setPassword(Encrypt.MD5("123456" + md5Key));
        adminService.save(admin);
        return MessageResult.success("重置密码成功，默认密码123456");
    }



    /**
     * admin信息
     *
     * @param id
     * @return
     */


    @RequiresPermissions("system:employee:detail")
    @RequestMapping(value = "/detail")
    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "后台用户Admin详情")
    public MessageResult adminDetail(Long id) {
        Map map = adminService.findAdminDetail(id);
        MessageResult result = success();
        result.setData(map);
        return result;
    }

    /**
     * 后台个人信息
     */

    @RequestMapping(value = "/adminInfo")
    @ResponseBody
    public MessageResult adminInfo( @SessionAttribute(SESSION_ADMIN) Admin adminInfo) {
        return  success(adminService.findById(adminInfo.getId()));
    }

    /**
     * admin信息
     *
     * @return
     */


    @RequiresPermissions("system:employee:deletes")
    @RequestMapping(value = "/deletes")
    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "删除后台用户")
    public MessageResult deletes(Long[] ids) {
        adminService.deletes(ids);
        return MessageResult.success("批量删除成功");
    }



    /**
     * 测试登录
     *
     * @return
     */

    @RequestMapping(value = "login")
    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "测试登陆")
    public MessageResult login() {
        return success("测试登录");
    }


    /**
     * 测试首页
     *
     * @return
     */

    @RequestMapping(value = "index")
    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "测试首页")
    public MessageResult index() {
        return success("成功登录");
    }


    /**
     * 测试无权限
     *
     * @return
     */

    @RequestMapping(value = "403")
    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "测试无权限")
    public MessageResult x() {
        return error(5000, "没有权限");
    }

    /**lingxing
     * 修改登录验证状态
     * @param adminInfo
     * @param verificationType
     * @return
     */
    @PostMapping(value = "/verificationType")
    @ResponseBody
    public MessageResult verificationType(@SessionAttribute(SESSION_ADMIN) Admin adminInfo,int verificationType) {
        Admin admin= adminService.findOne(adminInfo.getId());
        if(admin.getVerificationType()!=verificationType && admin.getGoogleState()==1){
            admin.setVerificationType(verificationType);
            adminService.save(admin);
        }
        return  success();
    }
}

