package com.spark.bitrade.netty;

import com.aqmd.netty.common.NettyCacheUtils;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * copy NettyCacheUtils 类，解决并发问题
 *
 * @author yangch
 * @time 2018.05.25 14:18  
 */
public class NettyCacheUtilsDK {
    private static final Logger logger = LoggerFactory.getLogger(NettyCacheUtils.class);
    private static Map<String, Set<Channel>> channelIdCache = new HashMap();
    //修改为线程安全
    public static Map<Channel, String> keyChannelCache = Collections.synchronizedMap(new HashMap());
    public static Map<String, Set<String>> userKey = new HashMap();

    public NettyCacheUtilsDK() {
    }

    public static void storeChannel(String key, Channel channel) {
        logger.debug("store channel with key:{}, channel id:{}", key, channel.id().asLongText());
        Set<Channel> set = channelIdCache.get(key);
        if (set == null) {
            //修改为线程安全
            Set<Channel> setNew = Collections.synchronizedSet(new HashSet());
            setNew.add(channel);
            channelIdCache.put(key, setNew);
        } else if (!set.contains(channel)) {
            set.add(channel);
        }

    }

    public static Set<Channel> getChannel(String key) {
        if (StringUtils.isEmpty(key)) {
            logger.debug("没有订阅[{}]的channel!", key);
        }

        return channelIdCache.get(key);
    }

    public static void removeChannel(String key) {
        if (StringUtils.isEmpty(key)) {
            logger.debug("没有订阅[{}]的channel!", key);
        }

        channelIdCache.remove(key);
    }

    public static Set<Channel> getAllChannels() {
        return new HashSet(channelIdCache.values());
    }
}
