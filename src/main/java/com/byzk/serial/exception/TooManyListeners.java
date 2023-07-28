package com.byzk.serial.exception;

/**
 * @Author:wy
 * @Date: 2023/7/19  10:20
 * @Version 1.0
 */
public class TooManyListeners extends Exception{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public TooManyListeners() {
    }

    @Override
    public String toString() {
        return "串口监听类数量过多！添加操作失败！";
    }
}
