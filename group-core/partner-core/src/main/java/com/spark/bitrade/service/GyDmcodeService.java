package com.spark.bitrade.service;

import com.spark.bitrade.config.LocationConfig;
import com.spark.bitrade.constant.PartnerStaus;
import com.spark.bitrade.dao.DimAreaDao;
import com.spark.bitrade.entity.DimArea;
import com.spark.bitrade.entity.PartnerArea;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.NetworkTool;
import com.spark.bitrade.util.ResolveJson;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.springframework.util.Assert.isTrue;

/**
  * 定位信息查询service
  * @author tansitao
  * @time 2018/5/15 14:37 
  */
@Service
@Slf4j
public class GyDmcodeService extends BaseService<DimArea> {
    @Autowired
    private DimAreaDao dimAreaDao;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private LocationConfig locationConfig;
    @Autowired
    private PartnerAreaService partnerAreaService;



    @Override
    public List<DimArea> findAll() {
        return dimAreaDao.findAll();
    }

    @Cacheable(cacheNames = "dimArea", key = "'entity:dimArea:all-'+#fatherId")
    public List<DimArea> findAllByFatherId(String fatherId)
    {
        return dimAreaDao.findAllByFatherId(fatherId);
    }

    /**
     * 通过身份证、手机号、ip对用户进行定位
     * @author tansitao
     * @time 2018/5/30 15:47 
     */
    public DimArea getPostionInfo(String idCard, String phone, String ip)
    {
        DimArea dimArea = null;
        MessageResult mr = MessageResult.success();
        if(StringUtils.isEmpty(idCard) && StringUtils.isEmpty(phone) && StringUtils.isEmpty(ip))
        {
            throw new IllegalArgumentException(msService.getMessage("PARAMETER_ERROR"));
        }
        else
        {
            //判断是否通过身份证查询到定位信息，如果没有查到则通过手机定位,如果手机号定位失败，则通过ip定位
//            dimArea = memberLocationByidCard(idCard);
//            if(dimArea == null)
//            {
//                dimArea = memberLocationByPhone(phone);
//                if(dimArea == null)
//                {
//                    dimArea = memberLocationByIp(ip);
//                }
//            }
            //判断是否通过手机定位到信息,如果手机号定位失败，则通过ip定位,如果没有查到则通过身份证查询定位
            dimArea = memberLocationByPhone(phone);
            if(dimArea == null)
            {
                dimArea = memberLocationByIp(ip);
                if(dimArea == null)
                {
                    dimArea = memberLocationByidCard(idCard);
                }
            }
        }
        //如果该区域无合伙人，不记录区域信息
//        if(dimArea != null)
//        {
//            PartnerArea partnerArea = partnerAreaService.findByAreaAndStatus(dimArea, PartnerStaus.normal);
//            if(partnerArea == null)
//            {
//                dimArea = null;
//            }
//        }

        return dimArea;
    }

    /**
      * 通过身份证定位
      * @author tansitao
      * @time 2018/5/15 15:58 
     * @param idCard
     */
    public DimArea memberLocationByidCard(String idCard)
    {
        if(StringUtils.isEmpty(idCard))
        {
            return null;
        }
        String dmCode = idCard.substring(0,6);
        DimArea gyDmcode  = findOneByDmCode(dmCode);
        if (gyDmcode != null)
        {
            log.info(idCard + "身份证查询成功，该用户所属区域==========" + gyDmcode.getAreaId());
        }
        else
        {
            dmCode = idCard.substring(0,4);
            gyDmcode  = findOneByDmCode(dmCode);
            if(gyDmcode != null)
            {
                log.info(idCard + "身份证查询成功，该用户所属区域==========" + gyDmcode.getAreaId());
            }
            else
            {
                dmCode = idCard.substring(0,2);
                gyDmcode  = findOneByDmCode(dmCode);
                if(gyDmcode != null)
                {
                    log.info(idCard + "身份证查询成功，该用户所属区域==========" + gyDmcode.getAreaId());
                }
                else
                {
                    log.info(idCard + "====身份证查询失败=====");
                }
            }
        }
        return gyDmcode;
    }

    /**
      * 通过手机定位
      * @author tansitao
      * @time 2018/5/15 15:57 
     * @param Phone
     */
    public DimArea memberLocationByPhone(String Phone)
    {
        if(StringUtils.isEmpty(Phone))
        {
            return null;
        }
        NetworkTool networkTool = new NetworkTool();
        ResolveJson resolveJson = new ResolveJson();
        DimArea dimArea = null;
        String url = locationConfig.toPhoneUrl(Phone);
        String res = networkTool.getContent(url);
        if("ok".equals(resolveJson.getVal(res, "ret")))
        {
            JSONArray jsonArray = JSONArray.fromObject(resolveJson.getVal(res, "data")); //吧list的值转为json数组对象
            Object[] strs = jsonArray.toArray(); //json转为数组
            if(!StringUtils.isEmpty(strs[0]) && !StringUtils.isEmpty(strs[1]))
            {
                dimArea = findOneByArea(strs[1] + "",strs[0] + "");
                if(dimArea != null)
                {
                    log.info(Phone + "手机号定位成功，该用户所属区域==========" + dimArea.getAreaId());
                }
                else
                {
                    log.info(Phone + "====手机号定位失败=====");
                }

            }
            else
            {
                log.info(Phone + "====手机号定位失败=====");
            }
        }
        else
        {
            log.info(Phone + "====手机号定位失败=====");
        }
        return dimArea;
    }

    /**
      * 通过ip定位
      * @author tansitao
      * @time 2018/5/15 15:57 
     * @param ip
     */
    public DimArea memberLocationByIp(String ip)
    {
        if(StringUtils.isEmpty(ip))
        {
            return null;
        }
        NetworkTool networkTool = new NetworkTool();
        ResolveJson resolveJson = new ResolveJson();
        DimArea dimArea = null;
        String url = locationConfig.toIpUrl(ip);
        String res = networkTool.getContent(url);
        if("ok".equals(resolveJson.getVal(res, "ret")))
        {
            JSONArray jsonArray = JSONArray.fromObject(resolveJson.getVal(res, "data")); //吧list的值转为json数组对象
            Object[] strs = jsonArray.toArray(); //json转为数组
            if(!StringUtils.isEmpty(strs[1]) && !StringUtils.isEmpty(strs[2]))
            {
                dimArea = findOneByArea(strs[2] + "",strs[1] + "");
                if(dimArea != null)
                {
                    log.info(ip + "ip定位成功，该用户所属区域==========" + dimArea.getAreaId());
                }
                else
                {
                    log.info(ip + "====ip定位失败=====");
                }

            }
            else
            {
                log.info(ip + "====ip定位失败=====");
            }
        }
        else
        {
            log.info(ip + "====ip定位失败=====");
        }
        return dimArea;
    }

    @Cacheable(cacheNames = "dimArea", key = "'entity:dimArea:'+#areaId")
    public DimArea findOneByDmCode(String areaId)
    {
        return dimAreaDao.findDimAreaByAreaId(areaId);
    }

    /**
     * 根据区域等级查询相关区域
     * @author shenzucai
     * @time 2018.05.30 11:40
     * @param level
     * @return true
     */
    public List<DimArea> findAllByLevel(String level){
        return dimAreaDao.findAllByLevel(level);
    }

    public DimArea findOneByArea(String areaAbbrName,String areaName)
    {
        return dimAreaDao.findOneByArea(areaAbbrName + '%', areaName + "%");
    }

}
