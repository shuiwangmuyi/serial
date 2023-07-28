package com.byzk.serial.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @Author:wy
 * @Date: 2023/7/19  10:44
 * @Version 1.0
 */
public class ExceptionUtils {
    /**
     * 异常转成字符串
     * @param t
     * @return
     */
    public static String getTrace(Throwable t) {
        StringWriter stringWriter= new StringWriter();
        PrintWriter writer= new PrintWriter(stringWriter);
        t.printStackTrace(writer);
        StringBuffer buffer= stringWriter.getBuffer();
        return buffer.toString();
    }
}
