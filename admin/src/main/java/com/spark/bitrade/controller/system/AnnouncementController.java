package com.spark.bitrade.controller.system;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.SysAdvertiseLocation;
import com.spark.bitrade.controller.BaseController;
import com.spark.bitrade.entity.Announcement;
import com.spark.bitrade.entity.QAnnouncement;
import com.spark.bitrade.entity.SysHelp;
import com.spark.bitrade.service.AnnouncementService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;


/**
 * @author rongyu
 * @description 公告
 * @date 2018/3/5 15:25
 */
@RestController
@RequestMapping("system/announcement")
public class AnnouncementController extends BaseController {
    @Autowired
    private AnnouncementService announcementService;

    /**
     * 添加新公告
     * @author fumy
     * @time 2018.11.20 10:43
     * @param title
     * @param sort
     * @param content
     * @param isShow
     * @param imgUrl
     * @return true
     */
    @RequiresPermissions("system:announcement:create")
    @PostMapping("create")
    public MessageResult create(
            @RequestParam String title,
            @RequestParam(value = "announcementLocation",required = false,defaultValue = "8") int platform,
            @RequestParam int sort,
            @RequestParam String content,
            @RequestParam("isShow") Boolean isShow,
            @RequestParam String url,
            @RequestParam(value = "imgUrl", required = false) String imgUrl) {
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setSort(sort);
        announcement.setContent(content);
        announcement.setIsShow(isShow);
        announcement.setImgUrl(imgUrl);
        announcement.setUrl(url);
        announcement.setAnnouncementLocation(platform);
        announcementService.save(announcement);
        return success("公告创建成功");
    }

    /**
     * 全局置顶
     * @author fumy
     * @time 2018.11.19 19:50
     * @param id
     * @param isTop 0：取消全局置顶，1:全局置顶
     * @return true
     */
    @RequiresPermissions("system:announcement:top")
    @PostMapping("top")
    @AccessLog(module = AdminModule.CMS, operation = "公告全局置顶与取消")
    public MessageResult toTop(@RequestParam("id")long id,BooleanEnum isTop){
        Announcement announcement = announcementService.findById(id);
        if(isTop == BooleanEnum.IS_FALSE){
            //取消置顶
            announcement.setIsGlobalSort(BooleanEnum.IS_FALSE);
        }else {
            //如果存在已经全局置顶的公告，则置顶失败（全局置顶唯一）
            boolean isExist = announcementService.isExistGlobalTop(BooleanEnum.IS_TRUE);
            if(isExist){
                return error("全局置顶已被启用，请重新操作");
            }
            announcement.setIsGlobalSort(BooleanEnum.IS_TRUE);
        }
        announcementService.save(announcement);
        return success("置顶成功");
    }

    /**
     * 是否显示到首页
     * @author fumy
     * @time 2018.11.19 19:50
     * @param id
     * @param opType 0：否，1:是
     * @return true
     */
    @RequiresPermissions("system:announcement:front-show")
    @PostMapping("front-show")
    @AccessLog(module = AdminModule.CMS, operation = "公告是否显示到首页")
    public MessageResult toFrontShow(@RequestParam("id")long id,BooleanEnum opType){
        Announcement announcement = announcementService.findById(id);
        if(opType == BooleanEnum.IS_FALSE){
            //取消显示到首页
            announcement.setIsFrontShow(BooleanEnum.IS_FALSE);
        }else {
            //显示到首页
            announcement.setIsFrontShow(BooleanEnum.IS_TRUE);
        }
        announcementService.save(announcement);
        return success("设置成功");
    }

    @RequiresPermissions("system:announcement:page-query")
    @GetMapping("page-query")
    public MessageResult page(
            PageModel pageModel,
            @RequestParam(required = false) Boolean isShow,@RequestParam(value = "title",required = false,defaultValue = "")String title) {
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (isShow != null) {
            booleanExpressions.add(QAnnouncement.announcement.isShow.eq(isShow));
        }
        if (!"".equals(title)){
            booleanExpressions.add(QAnnouncement.announcement.title.like("%"+title+"%"));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<Announcement> all = announcementService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }

    @RequiresPermissions("system:announcement:deletes")
    @PatchMapping("deletes")
    public MessageResult deleteOne(@RequestParam Long[] ids) {
        announcementService.deleteBatch(ids);
        return success();
    }

    @RequiresPermissions("system:announcement:detail")
    @GetMapping("{id}/detail")
    public MessageResult detail(
            @PathVariable Long id) {
        Announcement announcement = announcementService.findById(id);
        Assert.notNull(announcement, "validate id!");
        return success(announcement);
    }


    /**
     * 修改公告信息
     * @author fumy
     * @time 2018.11.20 10:42
     * @param id
     * @param title
     * @param sort
     * @param content
     * @param isShow
     * @param imgUrl
     * @return true
     */
    @RequiresPermissions("system:announcement:update")
    @PutMapping("{id}/update")
    public MessageResult update(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam int sort,
            @RequestParam String content,
            @RequestParam Boolean isShow,
            @RequestParam(value = "announcementLocation",required = false,defaultValue = "8") int platform,
            @RequestParam String url,
            @RequestParam(value = "imgUrl", required = false) String imgUrl) {
        Announcement announcement = announcementService.findById(id);
        Assert.notNull(announcement, "validate id!");
        if(!isShow && announcement.getIsGlobalSort() == BooleanEnum.IS_TRUE){
            return error("全局置顶的公告不允许隐藏");
        }
        announcement.setTitle(title);
        announcement.setSort(sort);
        announcement.setContent(content);
        announcement.setIsShow(isShow);
        announcement.setImgUrl(imgUrl);
        announcement.setUrl(url);
        announcement.setAnnouncementLocation(platform);
        announcementService.save(announcement);
        return success();
    }

    @RequiresPermissions("system:announcement:turn-off")
    @PatchMapping("{id}/turn-off")
    public MessageResult turnOff(@PathVariable Long id) {
        Announcement announcement = announcementService.findById(id);
        Assert.notNull(announcement, "validate id!");
        announcement.setIsShow(false);
        announcementService.save(announcement);
        return success();
    }

    @RequiresPermissions("system:announcement:turn-on")
    @PatchMapping("{id}/turn-on")
    public MessageResult turnOn(@PathVariable("id") Long id) {
        Announcement announcement = announcementService.findById(id);
        Assert.notNull(announcement, "validate id!");
        announcement.setIsShow(true);
        announcementService.save(announcement);
        return success();
    }

}
