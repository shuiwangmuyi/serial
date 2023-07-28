package com.byzk.serial.exception;

/**
 * @Author:wy
 * @Date: 2023/7/20  10:03
 * @Version 1.0
 */
public class NumberFormat extends Exception{
    private static final long serialVersionUID = 1L;

    public NumberFormat() {
    }

    @Override
    public String toString() {
        return "数值转换异常";
    }
}
