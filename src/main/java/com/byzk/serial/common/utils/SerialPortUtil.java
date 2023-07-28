package com.byzk.serial.common.utils;


import com.byzk.serial.exception.*;
import com.byzk.serial.listener.PortListener;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import gnu.io.SerialPort;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;


/**
 * @Author:wy
 * @Date: 2023/7/19  10:12
 * @Version 1.0
 */
@Slf4j
public class SerialPortUtil {
    private String name;

    private SerialPort serialPort;

    public SerialPortUtil(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 发送数据到串口
     *
     * @param data
     * @throws SendDataToSerialPortFailure
     * @throws SerialPortOutputStreamCloseFailure
     */
    public void sendData(byte[] data) throws SendDataToSerialPortFailure, SerialPortOutputStreamCloseFailure {
        OutputStream out = null;
        try {
            out = serialPort.getOutputStream();
            out.write(data);
            out.flush();
        } catch (IOException e) {
            log.error("发送数据,在写数据时发生异常:{}", e.getMessage());
            throw new SendDataToSerialPortFailure();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                log.error("发送数据,在关闭流时发送异常:{}", e.getMessage());
                throw new SerialPortOutputStreamCloseFailure();
            }
        }

    }

    /**
     * 从串口读取数据
     *
     * @return
     * @throws ReadDataFromSerialPortFailure
     * @throws SerialPortInputStreamCloseFailure
     */
    public byte[] readData() throws ReadDataFromSerialPortFailure, SerialPortInputStreamCloseFailure, DecoderException {
        InputStream in = null;
        byte[] bytes = null;
        try {

            in = serialPort.getInputStream();
            // 获取buffer里的数据长度
            int buffLength = in.available();
            while (buffLength != 0) {
                // 初始化byte数组为buffer中数据的长度
                bytes = new byte[buffLength];
                in.read(bytes, 0, bytes.length);
                log.info("初始化byte数组为buffer中数据的长度:{}", HexBin.encode(bytes));
                buffLength = in.available();
            }
        } catch (IOException e) {
            log.error("读取数据时异常:{}", e.getMessage());
            throw new ReadDataFromSerialPortFailure();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                log.error("读取数据,在关闭流时发生异常:{}", e.getMessage());
                throw new SerialPortInputStreamCloseFailure();
            }
        }
        return bytes;

    }

    /**
     * 绑定监听器
     *
     * @param listener
     * @param sleepTime
     * @throws TooManyListeners
     */
    public void bindListener(PortListener listener, int sleepTime) throws TooManyListeners {
        try {
            log.info("开始绑定监听器");
            //设置监听器
            listener.setSerialPort(this);
            log.info("设置休眠时间");
            listener.setSleepTime(sleepTime);

            // 给串口添加监听器
            log.info("给串口添加监听器");
            serialPort.addEventListener(listener);
            // 设置当有数据到达时唤醒监听接收线程
            log.info("设置当有数据到达时唤醒监听接收线程");
            serialPort.notifyOnDataAvailable(true);
            // 设置当通信中断时唤醒中断线程
            log.info("设置当通信中断时唤醒中断线程");
            serialPort.notifyOnBreakInterrupt(true);
        } catch (TooManyListenersException e) {
            log.error(e.getMessage());
            throw new TooManyListeners();
        }
    }

    /**
     * 关闭串口
     */
    public void closePort() {
        log.error("关闭串口");
        if (serialPort != null) {
            serialPort.close();
        }
    }
}
