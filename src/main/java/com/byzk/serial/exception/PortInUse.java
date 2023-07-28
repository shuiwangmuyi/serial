package com.byzk.serial.exception;

/**
 * @Author:wy
 * @Date: 2023/7/19  10:17
 * @Version 1.0
 */
public class PortInUse extends Exception{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public PortInUse() {
    }

    @Override
    public String toString() {
        return "端口已被占用！打开串口操作失败！";
    }
}
