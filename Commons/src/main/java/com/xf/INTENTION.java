package com.xf;

public interface INTENTION {

    public static final int HEAD_SIZE = 4;    //定义协议头字符大小

    public static final int HEART = 100;   //心跳消息
    public static final int REGISTER = 110;   //注册消息
    public static final int LOG = 120;   //日志消息
    public static final int INVOKE = 130;   //调用消息
    public static final int GET_DEVICES = 140;   //拉取所有设备
    public static final int GET_WORK = 150;   //拉取任务数据

    //安卓设备标识
    public static final String ANDROID = "android";
    public static final String PC = "PC";

}
