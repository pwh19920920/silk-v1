package com.spark.bitrade.messager.service.impl;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.messager.JPushDeviceType;
import com.spark.bitrade.entity.messager.NoticeEntity;
import com.spark.bitrade.messager.dto.SocketClient;
import com.spark.bitrade.messager.entity.BaseNoticeEntity;
import com.spark.bitrade.messager.entity.SysNoticeCountEntity;
import com.spark.bitrade.messager.service.INoticeClientService;
import com.spark.bitrade.messager.service.ISysNoticeCountService;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author ww
 * @time 2019.09.18 17:07
 */
@Component
public class NoticeClientServiceImpl  implements INoticeClientService {

    //userId = 0 发送给所有人


    @Autowired
    ISysNoticeCountService sysNoticeCountService;

    @Override
    public List<JPushDeviceType> sendToClient( NoticeEntity noticeEntity,long memberId) {


        List<JPushDeviceType> succDeviceTypes = new ArrayList<>();
        //找到通知的数量
        SysNoticeCountEntity allSysNoticeCountEntity = sysNoticeCountService.getSysNoticeCountEntityByMemberId(0);
        SysNoticeCountEntity sysNoticeCountEntity = sysNoticeCountService.getSysNoticeCountEntityByMemberId(memberId);
        //int unreadCount = getLastUnReadNoticeCountByMemberId(noticeEntity.getMemberId());
        noticeEntity.getExtras().put("unreadCount", sysNoticeCountEntity.getUnreadCount());
        noticeEntity.getExtras().put("totalCount", sysNoticeCountEntity.getTotalCount()+allSysNoticeCountEntity.getTotalCount());


        if(SocketClient.memberSocketClientMap.containsKey(memberId)){

            List<SocketClient> scs = SocketClient.memberSocketClientMap.get(memberId);
            BaseNoticeEntity baseNoticeEntity = new BaseNoticeEntity(noticeEntity);
            Iterator<SocketClient> it = scs.iterator();
            while (it.hasNext()) {
                SocketClient socketClient = it.next();
                synchronized (socketClient) {
                    socketClient.getChannel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(baseNoticeEntity)));
                }
                //保存返回的类型
                if(!succDeviceTypes.contains(socketClient.getDeviceType()))
                    succDeviceTypes.add(socketClient.getDeviceType());
            }

        }
        return succDeviceTypes;

    }

    @Override
    public void sendToAllClient(NoticeEntity noticeEntity) {

        Iterator<Map.Entry<Long, List<SocketClient>>> memeberSocketIterator =   SocketClient.memberSocketClientMap.entrySet().iterator();
        //发送给所有人
        while(memeberSocketIterator.hasNext()){
            Map.Entry<Long, List<SocketClient>> socketClientEntry =memeberSocketIterator.next();
            //找到通知的数量
            sendToClient(noticeEntity,socketClientEntry.getKey());
        }
    }
}
