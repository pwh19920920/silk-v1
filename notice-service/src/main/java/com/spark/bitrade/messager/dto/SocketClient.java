package com.spark.bitrade.messager.dto;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.messager.JPushDeviceType;
import com.spark.bitrade.entity.messager.NoticeEntity;
import com.spark.bitrade.messager.entity.BaseNoticeEntity;
import com.spark.bitrade.messager.entity.SysNoticeCountEntity;
import com.spark.bitrade.messager.service.ISysNoticeCountService;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author ww
 * @time 2019.09.18 15:56
 */
@Data
public class SocketClient {

    @Autowired
    static  ISysNoticeCountService sysNoticeCountService;

    //每个用户不同的端建立为同一分组，只要在线就推送通知，如无一端在线，进行JPUSH推送
    public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static Map<Long, List<SocketClient>> memberSocketClientMap = new ConcurrentReferenceHashMap<>();
    public static long  socketClientIndex= 0;
    //存所有的通信websocket /socket

    public SocketClient(){
        clientId = socketClientIndex ++;
    }

    //客户端设备类型
    JPushDeviceType deviceType;
    long clientId = 0L;
    //当前用户所有的连接
    Channel channel;
    long memberId;

    // 加了会自动增加

    synchronized  public  static void add(SocketClient sc){
        if(!memberSocketClientMap.containsKey(sc.memberId)){
            List<SocketClient> scs= new ArrayList<>();
            scs.add(sc);
            memberSocketClientMap.put(sc.memberId,scs);
        }else{
            memberSocketClientMap.get(sc.memberId).add(sc);
        }
        group.add(sc.getChannel());
    }

//    synchronized public static  void add(Channel channel , Long memberId, JPushDeviceType deviceType){
//        SocketClient.group.add(channel);
//
//        SocketClient sc = new SocketClient();
//        sc.setMemberId(memberId);
//        sc.setDeviceType(deviceType);
//        sc.setChannel(channel);
//        add(sc);
//    }
//    public static  List<JPushDeviceType> sendToMemeber(NoticeEntity noticeEntity){
//
//        List<JPushDeviceType> succDeviceTypes = new ArrayList<>();
//
//        //现在没有区分设备
//
//
//        SysNoticeCountEntity allSysNoticeCountEntity = sysNoticeCountService.getSysNoticeCountEntityByMemberId(0);
//
//        //为0的时候发送给所有人
//        if(noticeEntity.getMemberId()==0){
//            Iterator<Map.Entry<Long, List<SocketClient>>> memeberSocketIterator =   memberSocketClientMap.entrySet().iterator();
//            //发送给所有人
//            while(memeberSocketIterator.hasNext()){
//                Map.Entry<Long, List<SocketClient>> socketClientEntry =memeberSocketIterator.next();
//                //找到通知的数量
//                SysNoticeCountEntity sysNoticeCountEntity = sysNoticeCountService.getSysNoticeCountEntityByMemberId(socketClientEntry.getKey());
//                //int unreadCount = getLastUnReadNoticeCountByMemberId(noticeEntity.getMemberId());
//                noticeEntity.getExtras().put("unreadCount", sysNoticeCountEntity.getUnreadCount());
//                noticeEntity.getExtras().put("totalCount", sysNoticeCountEntity.getTotalCount()+allSysNoticeCountEntity.getTotalCount());
//                BaseNoticeEntity baseNoticeEntity = new BaseNoticeEntity(noticeEntity) ;
//                Iterator<SocketClient> it = socketClientEntry.getValue().iterator();
//                while (it.hasNext()) {
//                    SocketClient socketClient = it.next();
//
//                    synchronized (socketClient) {
//                        socketClient.getChannel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(baseNoticeEntity)));
//
//                    }
//                    //保存返回的类型
//                    //if(!succDeviceTypes.contains(socketClient.getDeviceType()))
//                    //    succDeviceTypes.add(socketClient.getDeviceType());
//                }
//
//            }
//
//            //BaseNoticeEntity baseNoticeEntity = new BaseNoticeEntity(noticeEntity) ;
//            //group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(baseNoticeEntity)));
//        }else {
//            //找到通知的数量
//            SysNoticeCountEntity sysNoticeCountEntity = sysNoticeCountService.getSysNoticeCountEntityByMemberId(noticeEntity.getMemberId());
//            //int unreadCount = getLastUnReadNoticeCountByMemberId(noticeEntity.getMemberId());
//            noticeEntity.getExtras().put("unreadCount", sysNoticeCountEntity.getUnreadCount());
//            noticeEntity.getExtras().put("totalCount", sysNoticeCountEntity.getTotalCount()+allSysNoticeCountEntity.getTotalCount());
//
//            List<SocketClient> scs = memberSocketClientMap.get(noticeEntity.getMemberId());
//            if (scs != null) {
//                BaseNoticeEntity baseNoticeEntity = new BaseNoticeEntity(noticeEntity);
//                Iterator<SocketClient> it = scs.iterator();
//                while (it.hasNext()) {
//                    SocketClient socketClient = it.next();
//
//                    synchronized (scs) {
//                        socketClient.getChannel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(baseNoticeEntity)));
//                    }
//                    //保存返回的类型
//                    if(!succDeviceTypes.contains(socketClient.getDeviceType()))
//                        succDeviceTypes.add(socketClient.getDeviceType());
//
//                }
//            }
//        }
//        return succDeviceTypes;
//    }
    synchronized public static  void remove(SocketClient sc){

        SocketClient.group.remove(sc.getChannel());

        List<SocketClient> scs  = memberSocketClientMap.get(sc.getMemberId());
        if(scs!=null){
            Iterator<SocketClient> it =  scs.iterator();
            while (it.hasNext()){
                SocketClient socketClient = it.next();
                if(socketClient.getChannel().equals(sc.getChannel())){
                    it.remove();
                }
            }
        }
    }

}
