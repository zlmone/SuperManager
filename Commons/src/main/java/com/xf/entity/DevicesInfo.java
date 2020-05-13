package com.xf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DevicesInfo {
    /**
     * 设备状态
     */
    private String type;

    /**
     * 设备编码
     */
    private String id;

    /**
     * 用户给设备注册的编号
     */
    private String userID;

    /**
     * 安卓设备唯一id
     */
    private String devicesId;

    /**
     * 设备详情
     */
    private String devicesDescription;

    /**
     * 命令字(预留
     */
    private String cmdId;

    /**
     * 当前脚本名
     */
    private String workName;

}
