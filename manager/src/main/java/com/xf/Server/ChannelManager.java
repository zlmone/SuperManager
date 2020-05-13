package com.xf.Server;

import com.xf.channel.ChannelType;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接对象管理器  superManager核心组件
 */
public class ChannelManager {
    private static ChannelManager channelManager = null;

    private Map<String, ChannelType> channelMap = new ConcurrentHashMap<>();

    private ChannelManager() {

    }

    /**
     * 单例控制
     *
     * @return
     */
    public synchronized static ChannelManager getInstance() {
        if (channelManager == null) {
            ChannelManager channelManager = new ChannelManager();
            ChannelManager.channelManager = channelManager;
            return channelManager;
        }
        return ChannelManager.channelManager;
    }

    /**
     * 根据value移除连接对象
     *
     * @param ctx 连接对象
     */
    public void removeChannelContext(ChannelHandlerContext ctx) {
        channelMap.entrySet().removeIf(next -> next.getValue().getChannelHandlerContext() == ctx);
    }

    /**
     * 根据String移除连接对象
     *
     * @param ip ip+ port
     */
    public void removeChannelContext(String ip) {
        channelMap.entrySet().removeIf(next -> next.getKey().equals(ip));
    }

    /**
     * 添加连接对象到map中
     *
     * @param ip  ip+port
     * @param ctxTye 连接对象
     * @return 连接对象
     */
    public ChannelType putChannel(String ip, ChannelType ctxTye) {
        return channelMap.put(ip, ctxTye);
    }


    public ChannelType getChannel(String ip) {
        return channelMap.get(ip);
    }

    public Map<String, ChannelType> getChannelAll() {
        return channelMap;
    }
}
