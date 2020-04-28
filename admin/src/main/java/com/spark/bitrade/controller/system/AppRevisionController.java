package com.spark.bitrade.controller.system;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.Platform;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.dto.AppRevisionDto;
import com.spark.bitrade.entity.AppRevision;
import com.spark.bitrade.model.update.AppRevisionUpdate;
import com.spark.bitrade.service.AppRevisionService;
import com.spark.bitrade.util.BindingResultUtil;
import com.spark.bitrade.util.MessageResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @author rongyu
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/2416:31
 */
@RestController
@RequestMapping("system/app-revision")
public class AppRevisionController extends BaseAdminController {
    @Autowired
    private AppRevisionService service;

    @RequiresPermissions("system:app-revision:add")
    @PostMapping("create")
    public MessageResult create(@Valid AppRevision appRevision,BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null){
            return result;
        }
        service.save(appRevision);
        return success();
    }

    /**
     * 修改
     * @param id
     * @param model
     * @return
     */
    @RequiresPermissions("system:app-revision:edit")
    @PutMapping (value = "/{id}")
    public MessageResult update(@PathVariable("id") Long id, AppRevisionUpdate model){
        AppRevision appRevision=new AppRevision();
        appRevision=transformation(model,appRevision);
        service.update(id,appRevision);
        return success();
    }

    //转化
    private AppRevision transformation(AppRevisionUpdate model,AppRevision appRevision) {
        switch (model.getPlatform()){
            case 0:
                appRevision.setPlatform(Platform.ANDROID);
                break;
            case 1:
                appRevision.setPlatform(Platform.IOS);
                break;
            default:break;
        }
        if (StringUtils.isNotBlank(model.getRemark())) {
            appRevision.setRemark(model.getRemark());
        }
        if (StringUtils.isNotBlank(model.getDownloadUrl())) {
            appRevision.setDownloadUrl(model.getDownloadUrl());
        }

        return appRevision;
    }

    /**
     * 版本信息详情
     * @param id
     * @return
     */
    @RequiresPermissions("system:app-revision:detail")
    @GetMapping("{id}")
    public MessageResult get(@PathVariable("id") Long id) {
        Assert.notNull(id, "查询id is null");
        AppRevision appRevision = service.findById(id);
        Assert.notNull(appRevision, "validate appRevision id!");
        return success(appRevision);
    }

    /**
     * 版本信息查询
     * @param pageModel
     * @param appRevisionDto
     * @return
     */
    @RequiresPermissions("system:app-revision:page")
    @PostMapping("page-query")
    public MessageResult get(PageModel pageModel, AppRevisionDto  appRevisionDto) {
        PageInfo<AppRevision> pageInfo=service.queryPage(appRevisionDto, pageModel);
        return success(PageData.toPageData(pageInfo));
    }

    /**
     * 删除版本信息
     * @author fumy
     * @time 2018.08.28 9:34
     * @param id
     * @return true
     */
    @RequiresPermissions("system:app-revision:delete")
    @PostMapping("delete")
    public MessageResult delete(Long [] id){
        Assert.notNull(id, "未选择删除APP版本");
        AppRevision appRevision;
        for(Long i: id){
            appRevision = service.findById(i);
            service.removeById(i,appRevision);
        }
        //service.delete(id);
        return success();
    }

    @RequestMapping(value="/testuploadimg", method = RequestMethod.POST)
    public @ResponseBody String uploadImg(@RequestParam("file") MultipartFile file,
                                          HttpServletRequest request) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        /*System.out.println("fileName-->" + fileName);
        System.out.println("getContentType-->" + contentType);*/
        String filePath = request.getSession().getServletContext().getRealPath("imgupload/");
        try {
//            FileUtil.uploadFile(file.getBytes(), filePath, fileName);
        } catch (Exception e) {
            // TODO: handle exception
        }
        //返回json
        return "uploadimg success";
    }
}
