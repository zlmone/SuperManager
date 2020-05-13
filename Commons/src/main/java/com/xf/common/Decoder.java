package com.xf.common;

import com.xf.INTENTION;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 对象转字节
 */
public class Decoder extends MessageToByteEncoder<SocketMessage> {

    protected void encode(ChannelHandlerContext channelHandlerContext, SocketMessage socketMessage, ByteBuf out) throws Exception {
        byte[] bytes = socketMessage.getJsonData().getBytes();
        out.writeInt(bytes.length + INTENTION.HEAD_SIZE);
        System.out.println("数据长度" + bytes.length);
        out.writeBytes(socketMessage.getJsonData().getBytes());
    }
}
