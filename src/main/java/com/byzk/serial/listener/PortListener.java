package com.byzk.serial.listener;

import com.byzk.serial.common.utils.SerialPortUtil;
import com.byzk.serial.exception.*;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import lombok.extern.slf4j.Slf4j;


/**
 * @Author:wy
 * @Date: 2023/7/19  10:06
 * @Version 1.0
 */
@Slf4j
public abstract  class PortListener implements SerialPortEventListener
{

    private final static Integer MILLIS=100;



    protected SerialPortUtil serialPortVo;
    protected int sleepTime;
    public void setSerialPort(SerialPortUtil serialPortVo) {
        this.serialPortVo = serialPortVo;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }
    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        log.info("监听到 类型为{}",serialPortEvent.getEventType());
        switch (serialPortEvent.getEventType()) {
            case SerialPortEvent.BI: // 10 通讯中断
                log.error("与串口设备通讯中断");
                break;
            case SerialPortEvent.OE: // 7 溢位（溢出）错误
            case SerialPortEvent.FE: // 9 帧错误
            case SerialPortEvent.PE: // 8 奇偶校验错误
            case SerialPortEvent.CD: // 6 载波检测
            case SerialPortEvent.CTS: // 3 清除待发送数据
            case SerialPortEvent.DSR: // 4 待发送数据准备好了
            case SerialPortEvent.RI: // 5 振铃指示
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 输出缓冲区已清空
                break;
            case SerialPortEvent.DATA_AVAILABLE: // 1 串口存在可用数据
                try {
                    // 延时收到字符串一段时间，足够接收所有字节，以免出现字符串隔断
                    log.info("延时收到字符串一段时间");
                    Thread.sleep(MILLIS);
                    log.info("延时收到字符串一段时间结束");
                    byte[] data = null;
                    log.info("判断串口");
                    if (serialPortVo.getSerialPort() == null) {
                        log.error("串口对象为空！监听失败！");
                    } else {
                        data = serialPortVo.readData(); // 读取数据，存入字节数组
                        onReceive(data,serialPortVo);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                    onReadException(e);
                }
                break;
        }
    }

    abstract public void onReadException(Exception e);
    abstract public void onReceive(byte[] data, SerialPortUtil serialPortVo) throws ReadDataFromSerialPortFailure, SendDataToSerialPortFailure, SerialPortOutputStreamCloseFailure, ReadConfigEx, NumberFormat;

}
