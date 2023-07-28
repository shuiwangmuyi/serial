package com.byzk.serial.listener;

import com.byzk.serial.common.utils.SerialCmdService;
import com.byzk.serial.common.utils.SerialMsgParser;
import com.byzk.serial.common.utils.SerialPortUtil;
import com.byzk.serial.exception.*;
import com.byzk.serial.vo.ParseRsp;
import com.byzk.serial.vo.ReqObj;
import com.byzk.serial.vo.ResultVo;
import com.byzk.serial.vo.RspObj;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author:wy
 * @Date: 2023/7/19  14:48
 * @Version 1.0
 */
@Slf4j
public class MyListener extends PortListener {

    /**
     * 上次消息读取剩余缓存
     */

    private byte[] byteCache;

    private String configFilePath;
    private String bondPath;

    public MyListener(String configFilePath, String bondPath) {
        log.info("configFilePath:{},bondPath:{}", configFilePath, bondPath);
        this.configFilePath = configFilePath;
        this.bondPath = bondPath;
    }

    @Override
    public void onReadException(Exception e) {
        log.error("接收数据异常：{}", e.getMessage());
//        System.out.println("发生异常: " + e.getMessage());
    }


    /**
     * 读取数据
     *
     * @param data
     * @param serialPortVo
     * @throws ReadDataFromSerialPortFailure      读数据异常
     * @throws SendDataToSerialPortFailure        发送数据异常
     * @throws SerialPortOutputStreamCloseFailure 端口输出流关闭异常
     * @throws ReadConfigEx                       读取配置文件异常
     * @throws NumberFormat                       数值转换异常
     */
    @Override
    public void onReceive(byte[] data, SerialPortUtil serialPortVo)
            throws ReadDataFromSerialPortFailure, SendDataToSerialPortFailure,
            SerialPortOutputStreamCloseFailure, ReadConfigEx, NumberFormat {
        String encode = HexBin.encode(data);
        log.info("串口接收到数据: " + encode);

        if (data.length > 0) {
            byte[] waitParseData = new byte[0];
            //是否需要结合上次剩余数据
            if (!ObjectUtils.isEmpty(byteCache)) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {
                    outputStream.write(byteCache);
                    outputStream.write(data);
                } catch (IOException e) {
                    throw new ReadDataFromSerialPortFailure();
                }
            } else {
                waitParseData = data;
            }


            SerialMsgParser _msgParser = new SerialMsgParser();

            //数据解析
            ParseRsp parseRsp = _msgParser.ParseData(waitParseData);

            //赋值剩余数据内容
            byteCache = parseRsp.ResidueBytes;


            if (parseRsp.Reqs != null && parseRsp.Reqs.size() > 0) {
                List<RspObj> rspCollection = new ArrayList<>();

                //处理请求事件
                for (ReqObj req : parseRsp.Reqs) {
                    log.info("读取要发送数据开始 .......");
                    ResultVo resultVo = SerialCmdService.Execute(req.Cmd, req.InputStream, configFilePath, bondPath);
                    log.info("返回值为：{}", resultVo);
                    if (!resultVo.isCode()) {
                        log.error("获取要发送 的数据错误");
                        throw new ReadConfigEx();
                    }
                    List<RspObj> rsp = (List<RspObj>) resultVo.getData();
                    if (!ObjectUtils.isEmpty(req)) {
                        rspCollection.addAll(rsp);
                    }
                }
                log.info("读取要发送数据结束 .......");
                log.info("要发送的数据为：{}", rspCollection);
                //发送数据
                sendData(serialPortVo, rspCollection, _msgParser);

            }
        }
    }

    /**
     * 发送数据
     *
     * @param serialPortVo
     * @param rspCollection 发送数据
     * @param _msgParser
     * @throws SendDataToSerialPortFailure        发送数据异常
     * @throws SerialPortOutputStreamCloseFailure 端口输出流关闭异常
     */
    public void sendData(SerialPortUtil serialPortVo, List<RspObj> rspCollection, SerialMsgParser _msgParser)
            throws SendDataToSerialPortFailure, SerialPortOutputStreamCloseFailure {
        log.info("发送数据开始 .......");
        //响应内容
        if (rspCollection.size() > 0) {
            for (RspObj rsp : rspCollection) {
                try {
                    byte[] sendMsg = _msgParser.AssemblyData(rsp.CmdCodes, rsp.Data);
                    serialPortVo.sendData(sendMsg);
                } catch (Exception ex) {
                    log.info("发送数据异常{}", ex);
                    throw new SendDataToSerialPortFailure();
                }
            }
        }
        log.info("发送数据结束 .......");
    }
}
