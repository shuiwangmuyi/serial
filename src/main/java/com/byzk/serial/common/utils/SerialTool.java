package com.byzk.serial.common.utils;

import com.byzk.serial.exception.NoSuchPort;
import com.byzk.serial.exception.NotASerialPort;
import com.byzk.serial.exception.PortInUse;
import com.byzk.serial.exception.SerialPortParameterFailure;
import gnu.io.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Enumeration;

/**
 * @Author:wy
 * @Date: 2023/7/19  10:45
 * @Version 1.0
 */
@Slf4j
public class SerialTool {
    private final static Integer OPEN_OUT_TIME = 2000;

    /**
     * 私有化SerialTool类的构造方法，不允许其他类生成SerialTool对象
     */
    private SerialTool() {
    }

    /**
     * 查找所有可用端口
     *
     * @return 可用端口名称列表
     */
    public static final ArrayList<String> findPort() {

        // 获得当前所有可用串口
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();

        ArrayList<String> portNameList = new ArrayList<>();

        // 将可用串口名添加到List并返回该List
        while (portList.hasMoreElements()) {
            String portName = portList.nextElement().getName();
            portNameList.add(portName);
        }
        return portNameList;

    }

    /**
     * 打开串口
     *
     * @param portName 端口名称
     * @param baudRate 波特率
     * @return 串口对象
     * @throws SerialPortParameterFailure 设置串口参数失败
     * @throws NotASerialPort             端口指向设备不是串口类型
     * @throws NoSuchPort                 没有该端口对应的串口设备
     * @throws PortInUse                  端口已被占用
     */
    public static final SerialPortUtil openPort(String name, String portName, int baudRate)
            throws SerialPortParameterFailure, NotASerialPort, NoSuchPort,
            PortInUse {

        try {

            // 通过端口名识别端口
            CommPortIdentifier portIdentifier = CommPortIdentifier
                    .getPortIdentifier(portName);
            log.info("打开的串口为：{}", portName);
            // 打开端口，并给端口名字和一个timeout（打开操作的超时时间）
            CommPort commPort = portIdentifier.open(portName, OPEN_OUT_TIME);
            log.info("打开串口：{}", commPort);
            // 判断是不是串口
            if (commPort instanceof SerialPort) {

                SerialPort serialPort = (SerialPort) commPort;
                SerialPortUtil serialPortVo = new SerialPortUtil(serialPort);
                serialPortVo.setName(name);
                try {
                    log.info("设置一下串口的波特率等参数");
                    // 设置一下串口的波特率等参数
                    serialPort.setSerialPortParams(baudRate,
                            SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                    log.info("设置一下串口的波特率等参数完成");
                } catch (UnsupportedCommOperationException e) {
                    log.error(e.getMessage());
                    throw new SerialPortParameterFailure();
                }
                return serialPortVo;

            } else {
                // 不是串口
                log.error("这不是一个串口");
                throw new NotASerialPort();
            }
        } catch (NoSuchPortException e1) {
            log.error("打开串口：{}", e1);
            throw new NoSuchPort();
        } catch (PortInUseException e2) {
            log.error("打开串口失败,端口已被占用：{}", e2);
            throw new PortInUse();
        }
    }

}
