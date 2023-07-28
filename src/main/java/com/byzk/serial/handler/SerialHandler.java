package com.byzk.serial.handler;

import com.byzk.serial.common.utils.SerialPortUtil;
import com.byzk.serial.common.utils.SerialTool;
import com.byzk.serial.exception.*;
import com.byzk.serial.listener.MyListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;

/**
 * @Author:wy
 * @Date: 2023/7/20  9:30
 * @Version 1.0
 */
@Order(1)
@Slf4j
@Component
public class SerialHandler implements ApplicationRunner {
    @Value("${configFilePath}")
    private String configFilePath;
    @Value("${isRun}")
    private boolean isRun;
    @Value("${bondPath}")
    private String bondPath;
    /**
     * 波特率
     */
    private final Integer BAUD_RATE = 115200;
    private final Integer MILLIS = 1000;
    private final Integer SLEEP_TIME = 500;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        //查找所有可用 串口
        ArrayList<String> ports = SerialTool.findPort();
        while (isRun) {
            log.info("获取端口：{}", ports);
            if (ObjectUtils.isEmpty(ports)) {
                isRun = true;
                //未获取串口，暂停 4000 毫秒
                Thread.sleep(MILLIS);
                //查找所有可用 串口
                ports = SerialTool.findPort();
                continue;
            }
            isRun = false;
            for (String str : ports) {
                try {
                    //打开 串口
//                    SerialPortUtil serialPortVo = SerialTool.openPort("/dev/ttyS4", "/dev/ttyS4", BAUD_RATE);
                    SerialPortUtil serialPortVo = SerialTool.openPort(str, str, BAUD_RATE);
                    log.info("绑定监听器");
                    serialPortVo.bindListener(new MyListener(configFilePath, bondPath), SLEEP_TIME);
                } catch (SerialPortParameterFailure e) {
                    log.error(e.getMessage());
                } catch (NotASerialPort e) {
                    log.error(e.getMessage());
                } catch (NoSuchPort e) {
                    log.error(e.getMessage());
                } catch (PortInUse e) {
                    log.error(e.getMessage());
                } catch (TooManyListeners e) {
                    log.error(e.getMessage());
                }
            }
        }
    }
}
