package com.byzk.serial.exception;

/**
 * @Author:wy
 * @Date: 2023/7/20  10:09
 * @Version 1.0
 */
public class ReadConfigEx extends  Exception{
    private static final long serialVersionUID = 1L;

    public ReadConfigEx() {
    }

    @Override
    public String toString() {
        return "读取配置文件异常";
    }
}
