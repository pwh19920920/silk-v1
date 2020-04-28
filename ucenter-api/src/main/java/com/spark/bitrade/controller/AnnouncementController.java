package com.spark.bitrade.controller;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.entity.Announcement;
import com.spark.bitrade.entity.QAnnouncement;
import com.spark.bitrade.pagination.PageResult;
import com.spark.bitrade.service.AnnouncementService;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;


/**
 * 公告
 *
 * @author rongyu
 * @description
 * @date 2018/3/5 15:25
 */
@RestController
@RequestMapping("announcement")
public class AnnouncementController extends BaseController {
    @Autowired
    private AnnouncementService announcementService;


    //@PostMapping("page")
    @RequestMapping(value = "page", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    public MessageResult page(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) boolean isIndex,
            @RequestParam(name = "platform", defaultValue = "0") Integer platform, HttpServletRequest request) {
//        String thirdMark = request.getHeader("thirdMark");
//        int platform = NumberUtils.toInt(thirdMark, 0);

        //edit by yangch 时间： 2018.07.13 原因：修改为缓存，但缓存后反序列化报错
        String language = request.getHeader("language");
        return success(getController().cachePage(pageNo, pageSize, isIndex, platform, language));
    }

    //公告查询比较频繁，只缓存page的第一页
    public PageResult<Announcement> cachePage(Integer pageNo, Integer pageSize, boolean isIndex, int platform, String languageCode) {
        //条件
        ArrayList<Predicate> predicates = new ArrayList<>();
        predicates.add(QAnnouncement.announcement.isShow.eq(true));
        predicates.add(QAnnouncement.announcement.languageCode.eq(languageCode));
        predicates.add(QAnnouncement.announcement.announcementLocation.eq(platform));
        if (isIndex == true) {
            //如果判断为true 则查询条件需要显示到首页字段为true
            predicates.add(QAnnouncement.announcement.isFrontShow.eq(BooleanEnum.IS_TRUE));
        }
        //排序
        ArrayList<OrderSpecifier> orderSpecifiers = new ArrayList<>();
        orderSpecifiers.add(QAnnouncement.announcement.sort.desc());
        orderSpecifiers.add(QAnnouncement.announcement.createTime.desc());
        //查
        PageResult<Announcement> pageResult = announcementService.queryDsl(pageNo, pageSize, predicates, QAnnouncement.announcement, orderSpecifiers);
        pageResult.getContent().forEach(announcement -> announcement.setContent(""));
        return pageResult;
    }

    @GetMapping("{id}")
    public MessageResult detail(@PathVariable("id") Long id) {
        Announcement announcement = announcementService.findById(id);
        Assert.notNull(announcement, "validate id!");
//        announcement.setPvCount(announcement.getPvCount()+1);
        announcementService.save(announcement);
        return success(announcement);
    }

    public AnnouncementController getController() {
        return SpringContextUtil.getBean(AnnouncementController.class);
    }

    /**
     *  * 获取最近一次的公告
     *  * @author tansitao
     *  * @time 2018/11/6 14:04 
     *  
     */
    @RequestMapping("/getLatelyAnnounce")
    public MessageResult getLatelyAnnouncement() {
        MessageResult rusult = new MessageResult();
        Announcement announcement = announcementService.findLatelyAnnounce(Boolean.TRUE);
        rusult.setData(announcement);
        return rusult;
    }

    /**
     * 获取全局置顶公告 edit by fumy date:2018.11.20
     *
     * @param
     * @return true
     * @author fumy
     * @time 2018.11.20 11:27
     */
    @ApiOperation(value = "获取全局置顶的公告", tags = "系统广告与公告", notes = "查询全局置顶的公告")
    @RequestMapping(value = "/getTopAnnounce", method = {RequestMethod.GET, RequestMethod.POST})
    public MessageRespResult<Announcement> getTopAnnouncement(@RequestParam(name = "platform", defaultValue = "0") Integer platform, HttpServletRequest request) {
        String language = request.getHeader("language");
        Announcement announcement = announcementService.findTopAnnounce(platform, language);
        return MessageRespResult.success("查询成功", announcement);
    }
}
