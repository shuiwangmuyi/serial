package com.byzk.serial.exception;

/**
 * @Author:wy
 * @Date: 2023/7/19  10:18
 * @Version 1.0
 */
public class SerialPortInputStreamCloseFailure extends  Exception{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SerialPortInputStreamCloseFailure() {
    }

    @Override
    public String toString() {
        return "关闭串口对象输入流（InputStream）时出错！";
    }

}
