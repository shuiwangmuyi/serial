package com.byzk.serial.exception;

/**
 * @Author:wy
 * @Date: 2023/7/19  10:20
 * @Version 1.0
 */
public class SerialPortParameterFailure extends  Exception{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SerialPortParameterFailure() {
    }

    @Override
    public String toString() {
        return "从串口读取数据时出错！";
    }
}
