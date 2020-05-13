package com.xf.channel;

import com.xf.entity.DevicesInfo;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelType {

    private String type = "android";      //默认设备类型为安卓设备

    private DevicesInfo devicesInfo;        //安卓设备特有设备信息  PC管理设备无需上传,上传也没什么鸟用

    private ChannelHandlerContext channelHandlerContext;    //连接对象

}
