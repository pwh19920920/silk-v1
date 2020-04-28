package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.spark.bitrade.config.AliyunConfig;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.entity.SilkDataDist;
import com.spark.bitrade.entity.SilkInformation;
import com.spark.bitrade.pagination.SpecialPageData;
import com.spark.bitrade.service.ISilkDataDistService;
import com.spark.bitrade.service.ISilkInformationService;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 资讯
 *
 * @author wsy
 * @date 2019/7/1 16:56
 */
@Slf4j
@RestController
@RequestMapping("/information")
public class SilkInformationController {

    @Autowired
    private ISilkInformationService silkInformationService;
    @Autowired
    private ISilkDataDistService silkDataDistService;
    @Autowired
    private AliyunConfig aliyunConfig;

    @PostMapping("list")
    public MessageResult page(@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                              @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                              @RequestParam(value = "classify", defaultValue = "") String classify,
                              @RequestParam(value = "tags", defaultValue = "") String tags, HttpServletRequest request) {
        String language = request.getHeader("language");
        PageInfo<SilkInformation> page = silkInformationService.page(pageNo, pageSize, classify, tags, language);
        page.getList().forEach(item -> {
            String content = item.getContent().replaceAll("<.*?>", "");
            item.setContent(content.substring(0, content.length() >= 100 ? 100 : content.length()));
        });
        MessageResult messageResult = MessageResult.success();
        messageResult.setData(SpecialPageData.toPageData(page));
        return messageResult;
    }

    @PostMapping("detail")
    public MessageResult detail(@RequestParam(value = "id") Long id) {
        Assert.isTrue(id != null && id > 0, "id error");
        SilkInformation silkInformation = silkInformationService.findById(id);
        return silkInformation != null ? MessageResult.success("success", silkInformation) : MessageResult.error("fail");
    }

    @PostMapping("tags")
    public MessageResult tags(HttpServletRequest request) {
        SilkDataDist silkDataDist = silkDataDistService.findByIdAndKey("SILKINFORMATION", "TAGS");
        if (silkDataDist != null && silkDataDist.getStatus() == BooleanEnum.IS_TRUE) {
            //update by shushiping 多语言适配
            String accept = request.getHeader("language");
            Map<String,Object> allTagss = Maps.newLinkedHashMap();
            //update by shushiping 标签要做多语言
            String tags = silkDataDist.getDictVal();
            Map<String,Object> tagMap = (Map<String,Object>) JSONObject.parse(tags);
            String[] array = null;
            if(accept == null || "zh_CN".equals(accept)){//简体中文
                array = tagMap.get("cn").toString().split(",");
            }else if("zh_HK".equals(accept)){//繁体中文
                array = tagMap.get("tw").toString().split(",");
            }else if("en_US".equals(accept)){//英文
                array = tagMap.get("en").toString().split(",");
            }else if("ko_KR".equals(accept)){//韩文
                array = tagMap.get("ko").toString().split(",");
            }
            return MessageResult.success("success", array);
        } else {
            return MessageResult.success("success", new String[]{});
        }
    }
}
