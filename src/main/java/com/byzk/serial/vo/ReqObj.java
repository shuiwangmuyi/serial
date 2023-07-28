package com.byzk.serial.vo;

import com.byzk.serial.common.enums.CmdType;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author:wy
 * @Date: 2023/7/19  16:33
 * @Version 1.0
 */
@Slf4j
public class ReqObj {

    /**
     * 请求命令码
     */
    public CmdType Cmd ;

    /// <summary>
    /// 输入信息流
    /// </summary>
    public byte[] InputStream ;
}
