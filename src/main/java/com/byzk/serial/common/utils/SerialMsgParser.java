package com.byzk.serial.common.utils;

import com.byzk.serial.common.enums.CmdType;
import com.byzk.serial.vo.ParseRsp;
import com.byzk.serial.vo.ReqObj;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author:wy
 * @Date: 2023/7/19  16:00
 * @Version 1.0
 */
@Slf4j
public class SerialMsgParser {
    /// <summary>
    /// 帧头
    /// </summary>

    private final byte[] _headers = new byte[]{(byte) 0xff, (byte) 0x55};
    /// <summary>
    /// 帧头长度
    /// </summary>
    private int _headerLen;

    public Integer getHeaderLen() {
        return _headers.length;
    }

    /// <summary>
    /// 命令码长度
    /// </summary>
    private final int _cmdCodeLen = 2;
    /// <summary>
    /// 数据长度位长度
    /// </summary>
    private final int _dataLenLen = 1;

    /// <summary>
    /// 最小帧长度
    /// </summary>
    private Integer minFrameLen;

    public Integer getMinFrameLen() {
        //头长(2) + 命令码长(2) + 数据位长(1) + 无数据位(0) + 无校验位(0) = 5
        return getHeaderLen() + _cmdCodeLen + _dataLenLen;
    }

    /// <summary>
    /// 命令码集合
    /// </summary>
    private Map<Byte, Set<Byte>> _cmds;
//    private Dictionary<byte, HashSet<byte>> _cmds;

    /**
     * 命令码集合
     */
    public SerialMsgParser() {
        _cmds = new HashMap<>();
        HashSet<Byte> setByte_01 = new HashSet<>();
        setByte_01.add((byte) 0x00);
        _cmds.put((byte) 0x01, setByte_01);

        HashSet<Byte> setByte_02 = new HashSet<>();
        setByte_02.add((byte) 0x00);
        _cmds.put((byte) 0x02, setByte_02);


        HashSet<Byte> setByte_03 = new HashSet<>();
        setByte_03.add((byte) 0x00);
        _cmds.put((byte) 0x03, setByte_03);

    }

    /**
     * 校验cmd命令码是否合法
     * 如果合法将返回本次命令类型
     */
    private Integer CmdCodeIsLegal(byte[] cmdCodes) {
        if (cmdCodes == null || cmdCodes.length != _cmdCodeLen) {
            return CmdType.OTHER.getCode();
        }

        if (!_cmds.containsKey(cmdCodes[0]) || !_cmds.get(cmdCodes[0]).contains(cmdCodes[1])) {
            return CmdType.OTHER.getCode();
        }
        Integer returnCmd = CmdType.OTHER.getCode();
        switch (cmdCodes[0]) {
            case 0x01:
                returnCmd = CmdType.SvcStatus.getCode();
                break;
            case 0x02:
                returnCmd = CmdType.IpInfo.getCode();
                break;
            case 0x03:
                returnCmd = CmdType.Hardware.getCode();
                break;
            default:
                returnCmd = CmdType.OTHER.getCode();
        }
        return returnCmd;
    }

    /**
     * 数据解析
     */
    public ParseRsp ParseData(byte[] dataPacket) {
        ParseRsp rsp = new ParseRsp();
        rsp.Reqs = new ArrayList<>();
        //空检查
        if (ObjectUtils.isEmpty(dataPacket)) {
            return rsp;
        }


        //当前数据帧解析起始下标
        Integer readBegin = 0;
        //是否直接退出(考虑的是帧内数据长度不够的情况)
        boolean nextBreak = false;
        //本批数据包总长度
        Integer dataPacketLen = dataPacket.length;


        while (true) {
            //检查剩余遍历长度
            if (readBegin >= dataPacketLen) {
                break;
            }
            Integer residueLen = dataPacketLen - readBegin;
            if (residueLen < getHeaderLen() || nextBreak) {
                //赋值剩余数据待下次读取
                rsp.ResidueBytes = new byte[residueLen];
                for (int i = readBegin; i < dataPacketLen; i++) {
                    rsp.ResidueBytes[i - readBegin] = dataPacket[i];
                }
                break;
            }


            //头部是否命中
            boolean hitHead = true;
            for (int i = 0; i < getHeaderLen(); i++) {
                if (dataPacket[readBegin + i] != _headers[i]) {
                    hitHead = false;
                    break;
                }
            }
            if (!hitHead) {
                readBegin++;
                continue;
            }

            //命令码是否合法
            byte[] cmdCode = new byte[_cmdCodeLen];
            for (int i = 0; i < cmdCode.length; i++) {
                cmdCode[i] = dataPacket[readBegin + getHeaderLen() + i];
            }

            Integer cmdRsp = CmdCodeIsLegal(cmdCode);

//            if (!Objects.equals(CmdType.OTHER.getCode(), cmdRsp)) {
            if (false) {
                //命令未命中，跳过当前帧头
                readBegin += _headerLen;
                continue;
            }

            //数据长度
            Integer dataLenPos = readBegin + getHeaderLen() + _cmdCodeLen;
            Integer dataLength = (int) dataPacket[dataLenPos];


            //没有携带数据的情况
            if (dataLength <= 0) {
                ReqObj reqObj = new ReqObj();
                reqObj.Cmd = CmdType.getCmdType(cmdRsp);
                rsp.Reqs.add(reqObj);
                readBegin = dataLenPos + 1;
                continue;
            }

            //携带了数据的情况
            Integer dataStartPos = dataLenPos + 1;

            //剩余长度不够数据长度的情况
            if (dataStartPos + dataLength > dataPacket.length) {
                nextBreak = true;
                continue;
            }


            //剩余长度充足的情况
            ReqObj reqObj = new ReqObj();
            reqObj.Cmd = CmdType.getCmdType(cmdRsp);
            reqObj.InputStream = new byte[dataLength];

            Integer nextReadBegin = dataStartPos + dataLength;
            for (int i = dataStartPos; i < nextReadBegin; i++) {
                reqObj.InputStream[i - dataStartPos] = dataPacket[i];
            }
            rsp.Reqs.add(reqObj);
            readBegin = nextReadBegin;

        }
        //合并重复请求
        if (rsp.Reqs.size() > 0) {
            rsp.Reqs = rsp.Reqs.stream().distinct().collect(Collectors.toList());
        }
        log.info("合并数据之后,值为:{}", rsp.Reqs);
        return rsp;

    }

    /**
     * 拼装要发送的数据
     *
     * @param cmdCode
     * @param data
     * @return
     */
    public byte[] AssemblyData(byte[] cmdCode, byte[] data) throws Exception {
        if (cmdCode == null || cmdCode.length != _cmdCodeLen) {
            throw new Exception("命名码格式错误");
        }
//        if (data != null && data.length > Math.pow(2,16)) {
//            throw new Exception("单帧数据不可超过 2 的 16 次方 ");
//        }
        log.info("未解析之前，发送的编码为:{},数据为:{}", cmdCode, data);

        Integer dataLength = data == null ? 0 : data.length;
        log.info("数据长度为：{}", data);
        //帧长度  为了 适配 服务信息返回值，需要将帧长度 +1
        Integer frameLength = getMinFrameLen() + dataLength + 1;
        log.info("frameLength 为{}", frameLength);


        byte[] rsp = new byte[frameLength];

        Integer writeIndex = 0;

        //帧头
        for (byte item : _headers) {
            rsp[writeIndex++] = item;
        }
        //命令码
        for (byte item : cmdCode) {
            rsp[writeIndex++] = item;
        }


        //数据长度
        if (dataLength == 0) {
            //数据长度
            rsp[writeIndex++] = (byte) 0;
            rsp[writeIndex++] = (byte) 0;
        } else {
            Integer firstLen = dataLength >> 8;
            Integer secondLen = dataLength & 0xff;

            rsp[writeIndex++] = firstLen.byteValue();
            rsp[writeIndex++] = secondLen.byteValue();
        }

//        rsp[writeIndex++] = dataLength.byteValue();


        //数据
        if (dataLength > 0) {
            for (byte item : data) {
                rsp[writeIndex++] = item;
            }
        }

        log.info("发送的数据为:{}", rsp);
        return rsp;
    }
}
