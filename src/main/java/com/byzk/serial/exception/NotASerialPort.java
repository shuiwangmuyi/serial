package com.byzk.serial.exception;

/**
 * @Author:wy
 * @Date: 2023/7/19  10:16
 * @Version 1.0
 */
public class NotASerialPort extends     Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public NotASerialPort() {
    }

    @Override
    public String toString() {
        return "端口指向设备不是串口类型！打开串口操作失败！";
    }
}
