package com.xf.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 消息实体
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SocketMessage {

    //数据长度
    private Integer length;

    //消息数据
    private String jsonData;
}
