package com.byzk.serial.common.enums;

import java.util.Objects;

/**
 * @Author:wy
 * @Date: 2023/7/19  16:25
 * @Version 1.0
 */
public enum CmdType {

    /**
     * 服务状态信息
     */
    SvcStatus(1, "服务状态信息"),
    /**
     * IP地址相关信息
     */
    IpInfo(2, "IP地址相关信息"),

    /**
     * 硬件信息
     */
    Hardware(3, "硬件信息"),

    OTHER(0, "其他，失败"),
    ;

    private Integer code;
    private String msg;

    CmdType(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static CmdType getCmdType(Integer code) {
        for (Integer i = 0; i < CmdType.values().length; i++) {
            if (Objects.equals(CmdType.values()[i].getCode(), code)) {
                return CmdType.values()[i];
            }
        }
        return CmdType.OTHER;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }
}
