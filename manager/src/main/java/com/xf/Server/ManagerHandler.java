package com.xf.Server;

import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.xf.INTENTION;
import com.xf.channel.ChannelType;
import com.xf.common.SocketMessage;
import com.xf.entity.DevicesInfo;
import com.xf.queue.LogQueue;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ManagerHandler extends SimpleChannelInboundHandler<SocketMessage> {

    /**
     * 收到消息触发
     *
     * @param ctx 连接对象
     * @param msg 消息
     * @throws Exception 异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocketMessage msg) throws Exception {
        log.debug("读取到客户端消息" + msg);
        JSONObject jsonObject = JSONObject.parseObject(msg.getJsonData());
        log.info("type指令:" + jsonObject.getIntValue("type"));
        switch (jsonObject.getIntValue("type")) {
            case INTENTION.HEART:
                heart(ctx, msg);
                break;
            case INTENTION.REGISTER:
                register(ctx, msg);
                break;
            case INTENTION.GET_DEVICES:
                //拉取所有连接设备
                getDevices(ctx, msg);
                break;
            case INTENTION.GET_WORK:
                getWork(ctx, msg);
                break;
            default:
                break;
        }

    }

    /**
     * 连接触发事件
     *
     * @param ctx 连接对象
     * @throws Exception 异常
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.debug("连接建立!");
    }

    /**
     * 连接异常断开事件
     *
     * @param ctx   连接对象
     * @param cause 断开异常
     * @throws Exception 抛出异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //连接异常 移除对象
        ChannelManager.getInstance().removeChannelContext(ctx);
        ctx.close();
        log.warn("连接对象异常");
        super.exceptionCaught(ctx, cause);
    }


    /**
     * 只有操作服务端的数据才能在此handler中进行操作,操作设备需直接下发指令
     */


    /**
     * 设备注册
     *
     * @param channelHandlerContext 连接对象
     * @param socketMessage         消息
     */
    public void register(ChannelHandlerContext channelHandlerContext, SocketMessage socketMessage) {
        InetSocketAddress address = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
        String clientIP = address.getAddress().getHostAddress();
        String clientPort = String.valueOf(address.getPort());
        ChannelType channelType = new ChannelType();
        JSONObject jsonObject = JSONObject.parseObject(socketMessage.getJsonData());
        JSONObject object = JSONObject.parseObject(jsonObject.getString("channelType"));

        //获取设备类型 进行注册
        channelType.setType(object.getString("type"));
        if (object.getString("type").equals("PC")) {
            log.debug("PC管理端设备注册!");
        }
        channelType.setChannelHandlerContext(channelHandlerContext);
        ChannelManager.getInstance().putChannel(clientIP + clientPort, channelType);
        log.debug("设备注册完成!");

    }

    /**
     * 心跳
     *
     * @param channelHandlerContext 连接对象
     * @param socketMessage         消息
     */
    public void heart(ChannelHandlerContext channelHandlerContext, SocketMessage socketMessage) {
        log.debug("收到心跳消息!");
        //将心跳消息回传客户端
        DevicesInfo devicesInfo = new DevicesInfo();
        devicesInfo.setId("aaaaa");
        devicesInfo.setType("running");
        devicesInfo.setDevicesId("ssssss");
        devicesInfo.setUserID("admin");
        devicesInfo.setWorkName("快手_version_1.0");
        devicesInfo.setDevicesDescription(System.currentTimeMillis() + "");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("devicesInfo", devicesInfo);
        jsonObject.put("type", INTENTION.LOG);
        //生成一条 Android log
        SocketMessage message = new SocketMessage();
        message.setJsonData(jsonObject.toJSONString());
        LogQueue.getInstance().addSocketMessage(message);

        channelHandlerContext.writeAndFlush(socketMessage);
    }

    /**
     * 调用设备
     * 同时移除不可用设备
     *
     * @param channelHandlerContext 连接对象
     * @param socketMessage         消息
     */
    public void invoke(ChannelHandlerContext channelHandlerContext, SocketMessage socketMessage) {
        Map<String, ChannelType> channelAll = ChannelManager.getInstance().getChannelAll();
        channelAll.forEach((key, values) -> {
            //判断取出连接是否失效 与是否为安卓客户端
            if (!values.getChannelHandlerContext().isRemoved() && values.getType().equals(INTENTION.ANDROID)) {
                values.getChannelHandlerContext().writeAndFlush(socketMessage);
            } else if (values.getChannelHandlerContext().isRemoved()) {
                ChannelManager.getInstance().removeChannelContext(channelHandlerContext);
            }
        });
    }

    /**
     * 受控端日志消息  *** 只有安卓端能将日志传到此处 ***
     * 将日志丢进队列中 ***  使用日志消费线程去消费队列中的日志  ***
     *
     * @param channelHandlerContext 连接对象
     * @param socketMessage         消息
     */
    public void androidLog(ChannelHandlerContext channelHandlerContext, SocketMessage socketMessage) {
        LogQueue.getInstance().addSocketMessage(socketMessage);
    }

    /**
     * 拉取所有设备,返回所有挂载在服务器上的设备  **** PC管理端自行处理 ****
     *
     * @param ctx 连接对象
     * @param msg 消息
     */
    public void getDevices(ChannelHandlerContext ctx, SocketMessage msg) {
        Map<String, ChannelType> channelAll = ChannelManager.getInstance().getChannelAll();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("devicesList", channelAll);
        jsonObject.put("type", INTENTION.GET_DEVICES);
        msg.setJsonData(jsonObject.toJSONString());
        ctx.writeAndFlush(msg);
    }

    /**
     * 拉取所有脚本 但手机上启动的脚本可能与用户拥有的脚本不一样
     *
     * @param ctx 连接对象
     * @param msg 消息
     */
    public void getWork(ChannelHandlerContext ctx, SocketMessage msg) {
        JSONObject jsonObject = new JSONObject();
        //读取每一个设备上的脚本 去重复
        List<String> list = new ArrayList<>();
//        Map<String, ChannelType> channelAll = ChannelManager.getInstance().getChannelAll();
//        channelAll.forEach((key,valus)->{
        //如果脚本名不存在 添加到list中
//            if (list.contains(valus.getDevicesInfo().getWorkName())) {
//                list.add(valus.getDevicesInfo().getWorkName());
//            }
//        });
        list.add("dy");
        list.add("ks");
        list.add("cb");
        list.add("ms");
        list.add("lx");
        jsonObject.put("workList", list);
        jsonObject.put("type", INTENTION.GET_WORK);
        msg.setJsonData(jsonObject.toJSONString());
        ctx.writeAndFlush(msg);
    }
}
