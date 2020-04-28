package com.spark.bitrade.controller.hqb;

import com.spark.bitrade.entity.LockHqbCoinSettgingVo;
import com.spark.bitrade.service.impl.ILockHqbCoinSettgingService;
import com.spark.bitrade.util.MessageRespResult;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Zhang Yanjun
 * @time 2019.04.24 09:59
 */
@RestController
@Slf4j
public class HqbSettingController {
    @Autowired
    private ILockHqbCoinSettgingService iLockHqbCoinSettgingService;

    /**
     * 活期宝币种列表接口
     *
     * @param appId
     * @author Zhang Yanjun
     * @time 2019.04.24 10:44
     */
    @ApiOperation(value = "活期宝有效配置列表接口",tags = "活期宝-v1.0")
    @ApiImplicitParam(name = "appId", value = "应用或渠道ID", required = true)
    @PostMapping("hqb/validSetting")
    public MessageRespResult<LockHqbCoinSettgingVo> validSetting(String appId, HttpServletRequest request) {
        List<LockHqbCoinSettgingVo> settgings = iLockHqbCoinSettgingService.findValidSettingByAppId(appId);
        String accept = request.getHeader("language");
        settgings.stream().forEach(item -> {
            if(accept == null || "zh_CN".equals(accept)){//简体中文
                item.setAcitivityName(item.getActivityNameCn());
            }else if("zh_HK".equals(accept)){//繁体中文
                item.setAcitivityName(item.getActivityNameZhTw());
            }else if("en_US".equals(accept)){//英文
                item.setAcitivityName(item.getActivityNameEn());
            }else if("ko_KR".equals(accept)){//韩文
                item.setAcitivityName(item.getActivityNameKo());
            }
            item.setActivityNameCn(null);
            item.setActivityNameEn(null);
            item.setActivityNameKo(null);
            item.setActivityNameZhTw(null);
        });
        return MessageRespResult.success("查询成功", settgings);
    }
}
