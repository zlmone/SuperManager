package com.xf.common;

import com.xf.Constants;
import com.xf.INTENTION;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * 解码器 netty实现拆包
 */
public class Encoder extends ReplayingDecoder<Void> {


    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        int i = in.readInt();   //读取包头数据 该数据为字节流数据大小
        byte[] bytes = new byte[ i - INTENTION.HEAD_SIZE ];
        SocketMessage socketMessage = new SocketMessage();
        socketMessage.setLength(i-INTENTION.HEAD_SIZE);
        in.readBytes(bytes);
        socketMessage.setJsonData(new String(bytes));
        list.add(socketMessage);
    }
}
