package com.byzk.serial.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author:wy
 * @Date: 2023/7/19  16:32
 * @Version 1.0
 */
@Data
public class ParseRsp {


    /**
     * 解析完成的消息内容
     */
    public List<ReqObj> Reqs;

    /**
     * 读取后多余(冗余，剩余)的字节流
     */
    public byte[] ResidueBytes;


}
