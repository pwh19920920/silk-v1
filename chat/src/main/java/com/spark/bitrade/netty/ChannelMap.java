package com.spark.bitrade.netty;

import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ChannelMap {
    private Map<String,Set<Channel>> channelMap = new HashMap<>();

    public boolean addChannel(String orderId,Channel channel){
        System.out.println("new channel,orderId="+orderId+",channel="+channel.id().asLongText());
        if(channelMap.containsKey(orderId)){
            return channelMap.get(orderId).add(channel);
        }
        else{
            Set<Channel> channelSet = new LinkedHashSet<>();
            channelMap.put(orderId,channelSet);
            return channelSet.add(channel);
        }
    }

    public boolean removeChannel(String orderId,Channel channel){
        if(channelMap.containsKey(orderId)) {
            Iterator<Channel> iterator = channelMap.get(orderId).iterator();
            while (iterator.hasNext()) {
                Channel item = iterator.next();
                if (item.id().asLongText().equalsIgnoreCase(channel.id().asLongText())) {
                    iterator.remove();
                    System.out.println("remove channel,id=" + channel.id());
                    return true;
                }
            }
            return false;
        }
        else return false;
    }

    public Set<Channel> getChannels(String orderId){
        return channelMap.get(orderId);
    }
}
