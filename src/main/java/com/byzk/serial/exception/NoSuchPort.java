package com.byzk.serial.exception;

/**
 * @Author:wy
 * @Date: 2023/7/19  10:15
 * @Version 1.0
 */
public class NoSuchPort extends Exception{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public NoSuchPort() {
    }

    @Override
    public String toString() {
        return "没有找到与该端口名匹配的串口设备！打开串口操作失败！";
    }
}
