package com.xf.manager;

import com.xf.INTENTION;
import com.xf.Server.ChannelManager;
import com.xf.Server.HeartHandler;
import com.xf.Server.ManagerHandler;
import com.xf.channel.ChannelType;
import com.xf.common.Decoder;
import com.xf.common.Encoder;
import com.xf.common.SocketMessage;
import com.xf.queue.LogQueue;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@Slf4j
@SpringBootApplication
public class ManagerApplication implements CommandLineRunner {

    @Value("${netty.port}")
    private Integer port;

    public static void main(String[] args) {
        SpringApplication.run(ManagerApplication.class, args);
    }

    /**
     * netty 启动入口
     * @param args 空参数
     * @throws Exception 异常信息
     */
    @Override
    public void run(String... args) throws Exception {
        start();
    }


    /**
     * 启动netty 绑定yml文件中配置的端口
     */
    private void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap boot = new ServerBootstrap();
            boot.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(port)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //添加解码器
                            socketChannel.pipeline().addLast(new Decoder());
                            //添加编码器
                            socketChannel.pipeline().addLast(new Encoder());
                            //心跳管理
                            socketChannel.pipeline().addLast(new HeartHandler());
                            //连接触发事件
                            socketChannel.pipeline().addLast(new ManagerHandler());
                        }
                    });
            // start
            ChannelFuture future = boot.bind().sync();

            log.info("netty start port :" + port);
            logConsumer();
            //挂起线程 等待连接
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("服务器发生致命错误,宕机!");
        } finally {
            // shutdown
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 日志消费者
     */
    private void logConsumer() {
        log.info("日志线程启动!");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    try {
                        Thread.sleep(100);
                        SocketMessage socketMessage = LogQueue.getInstance().getSocketMessage();
                        if (socketMessage == null) continue;    //日志为空 跳过此次
                        Map<String, ChannelType> channelAll = ChannelManager.getInstance().getChannelAll();
                        channelAll.forEach((key, values) -> {
                            //判断取出连接是否失效 与是否为PC客户端
                            if (!values.getChannelHandlerContext().isRemoved() && values.getType().equals(INTENTION.PC)) {
                                values.getChannelHandlerContext().writeAndFlush(socketMessage);
                            } else if (values.getChannelHandlerContext().isRemoved()) {
                                ChannelManager.getInstance().removeChannelContext(values.getChannelHandlerContext());
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("发送日志消息到管理端失败!");
                    }
                }

            }
        }).start();
    }
}
