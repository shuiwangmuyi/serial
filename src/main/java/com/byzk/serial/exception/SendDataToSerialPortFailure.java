package com.byzk.serial.exception;

/**
 * @Author:wy
 * @Date: 2023/7/19  10:18
 * @Version 1.0
 */
public class SendDataToSerialPortFailure extends Exception{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SendDataToSerialPortFailure() {
    }

    @Override
    public String toString() {
        return "往串口发送数据失败！";
    }
}
