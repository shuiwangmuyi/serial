package com.byzk.serial.exception;

/**
 * @Author:wy
 * @Date: 2023/7/19  10:17
 * @Version 1.0
 */
public class ReadDataFromSerialPortFailure extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ReadDataFromSerialPortFailure() {
    }

    @Override
    public String toString() {
        return "从串口读取数据时出错！";
    }
}
