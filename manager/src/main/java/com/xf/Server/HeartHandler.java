package com.xf.Server;

import com.xf.Constants;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class HeartHandler extends IdleStateHandler {

    public HeartHandler() {
        super(Constants.READ_IDLE_TIME, Constants.WRITE_IDLE_TIME, 0);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                // 在规定时间内没有收到客户端的上行数据, 主动断开连接
                ctx.disconnect();
                log.warn("客户端无数据响应!该连接被服务端主动关闭!");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
