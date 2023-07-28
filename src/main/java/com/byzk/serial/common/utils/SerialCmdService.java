package com.byzk.serial.common.utils;

import com.byzk.serial.common.enums.CmdType;
import com.byzk.serial.exception.NumberFormat;
import com.byzk.serial.exception.ReadConfigEx;
import com.byzk.serial.exception.SendDataToSerialPortFailure;
import com.byzk.serial.vo.ResultVo;
import com.byzk.serial.vo.RspObj;
import lombok.extern.slf4j.Slf4j;
import org.ini4j.Profile;
import org.ini4j.Wini;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystemNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author:wy
 * @Date: 2023/7/19  18:43
 * @Version 1.0
 */
@Slf4j
public class SerialCmdService {
    private static final String SVC = "SVC";
    private static final String IF_CONFIG = "ifconfig";
    private static final String DATA = "data";
    private static final String STATUS = "status";
    private static final String STATUS_IS_TRUE = "运行中";
    private static final String Z_LEFT = "[";
    private static final String Z_RIGHT = "]";

    private static final String STATUS_IS_FALSE = "停止";
    private static final String SIGN_SERV_NUM = "signServNum";
    private static final String SVC_NAME = "svcName";
    private static final String SVC_PORT = "svcPort";
    private static final String SERVER_NAME = "serverName";
    private static final String IPADDR = "IPADDR";
    private static final String ADDRESS_SMALL = "address";
    private static final String NETMASK = "NETMASK";
    private static final String NETMASK_EX = "netmask";
    private static final String GATEWAY = "GATEWAY";
    private static final String GATEWAY_EX = "gateway";
    private static final String ADDRESS = "地址:";
    private static final String ADDRESS_EX = "地址：";
    private static final String MASK = "掩码:";
    private static final String MASK_EX = "掩码：";

    private static final String BROADCAST = "广播:";
    private static final String BROADCAST_EX = "广播：";
    private static final String QUOTATION = "";
    private static final String DY = "=";
    private static final String MASTER = "MASTER";
    private static final String N = "\n";
    private static final String N_EX = "\n\n";

    private static final String MH = ":";
    private static final String SPACE = " ";

    private static final Integer ZERO = 0;
    private static final Integer ONE = 1;
    private static final Integer TWO = 2;

    public static ResultVo Execute(CmdType cmd, byte[] inputs, String configPath, String bondPath) throws SendDataToSerialPortFailure, ReadConfigEx, NumberFormat {
        switch (cmd) {
            case SvcStatus:
                return ResultUtil.success(SvcStatus(configPath));
            case IpInfo:
                try {
                    return ResultUtil.success(getNetInfoByConfig(bondPath));
                } catch (Exception e) {
                    log.error(e.getMessage());
                    return ResultUtil.success(IpInfo(bondPath));
                }
            case Hardware:
                return ResultUtil.success(Hardware());
            default:
                log.warn("往串口发送数据失败:解析头部编码错误");
                throw new SendDataToSerialPortFailure();
        }
    }

    /**
     * 服务状态信息
     */
    public static List<RspObj> SvcStatus(String configPath) throws ReadConfigEx, NumberFormat {
        log.info("获取服务状态信息");

        List<RspObj> rspObjs = new ArrayList<>();

        //读取配置文件
        ResultVo resultVo = readConfig(configPath, SVC);
        if (!resultVo.isCode()) {
            log.error("读取配置文件异常");
            throw new ReadConfigEx();
        }
        try {

            Profile.Section Section = (Profile.Section) resultVo.getData();
            //配置文件区域的值 解析
            readSvc(rspObjs, Section);
        } catch (Exception e) {
            log.error("数据转换异常");
            throw new NumberFormat();
        }
        return rspObjs;
    }


    /**
     * IP地址相关信息
     */
    private static List<RspObj> IpInfo(String bondPath) {
        log.info("获取IP信息");
        List<RspObj> rspObjs = new ArrayList<>();

        //IP地址
        RspObj rspObj = new RspObj();
        byte[] bytes = new byte[]{(byte) 0x02, (byte) 0x01};
        rspObj.setCmdCodes(bytes);
        rspObj.setData("192.168.100.26".getBytes());
        rspObjs.add(rspObj);

        //子网掩码
        RspObj rspObj1 = new RspObj();
        byte[] bytes1 = new byte[]{(byte) 0x02, (byte) 0x02};
        rspObj1.setCmdCodes(bytes1);
        rspObj1.setData("255.255.248.0".getBytes());
        rspObjs.add(rspObj1);


        //默认网关
        RspObj rspObj2 = new RspObj();
        byte[] bytes2 = new byte[]{(byte) 0x02, (byte) 0x03};
        rspObj2.setCmdCodes(bytes2);
        rspObj2.setData("192.168.100.254".getBytes());
        rspObjs.add(rspObj2);


        return rspObjs;
    }

    /**
     * 根据配置文件获取bond 的值
     *
     * @return
     * @throws Exception
     */
    public static List<RspObj> getNetInfoByConfig(String bondPath) throws Exception {
        log.info("根据配置文件获取IP 信息");
        List<RspObj> rspObjs = new ArrayList<>();
        if (!new File(bondPath).exists()) {
            log.warn("bond 文件不存在");
            throw new FileSystemNotFoundException("bond 文件不存在");
        }
        BufferedReader br = new BufferedReader(new FileReader(bondPath));
        String line = null;
        while ((line = br.readLine()) != null) {
            if (line.contains(IPADDR)) {
                RspObj rspObj = analysisIp(line, DY, SPACE, new byte[]{(byte) 0x02, (byte) 0x01});
                rspObjs.add(rspObj);
            } else if (line.contains(ADDRESS_SMALL)) {
                RspObj rspObj = analysisIp(line, DY, ADDRESS_SMALL, new byte[]{(byte) 0x02, (byte) 0x01});
                rspObjs.add(rspObj);
            } else if (line.contains(NETMASK)) {
                RspObj rspObj = analysisIp(line, DY, SPACE, new byte[]{(byte) 0x02, (byte) 0x02});
                rspObjs.add(rspObj);
            } else if (line.contains(NETMASK_EX)) {
                RspObj rspObj = analysisIp(line, DY, NETMASK_EX, new byte[]{(byte) 0x02, (byte) 0x02});
                rspObjs.add(rspObj);
            } else if (line.contains(GATEWAY)) {
                RspObj rspObj = analysisIp(line, DY, ADDRESS_SMALL, new byte[]{(byte) 0x02, (byte) 0x03});
                rspObjs.add(rspObj);
            } else if (line.contains(GATEWAY_EX)) {
                RspObj rspObj = analysisIp(line, DY, SPACE, new byte[]{(byte) 0x02, (byte) 0x03});
                rspObjs.add(rspObj);
            }
        }
        return rspObjs;
    }


    /**
     * 获取IP信息
     *
     * @return
     */
    public static List<RspObj> getNetInfo(String bondPath) throws Exception {
        List<RspObj> rspObjs = new ArrayList<>();
        //执行Linux 脚本
        Map<String, Object> ifConfigMap = NetWorkUtil.listInfo(IF_CONFIG);
        String ifConfig = ifConfigMap.get(DATA).toString();
        log.info("执行脚本之后，获取的信息为：{}", ifConfig);

        if (ifConfig.contains(ADDRESS) || ifConfig.contains(ADDRESS_EX)) {
            ifConfig = ifConfig.replace(ADDRESS, QUOTATION)
                    .replace(ADDRESS_EX, QUOTATION);
        }

        if (ifConfig.contains(MASK) || ifConfig.contains(MASK_EX)) {
            ifConfig = ifConfig.replace(MASK, QUOTATION)
                    .replace(MASK_EX, QUOTATION);
        }

        if (ifConfig.contains(BROADCAST) || ifConfig.contains(BROADCAST_EX)) {
            ifConfig = ifConfig.replace(BROADCAST, QUOTATION)
                    .replace(BROADCAST_EX, QUOTATION);
        }
        log.info("修改之后,网络配置信息为：{}", ifConfig);


        String[] ips = ifConfig.split(N_EX);
        log.info("分割之后，ip信息为：{}", ips);

        ArrayList<HashMap<String, String>> maps = new ArrayList<>();


        String name = null;

        for (String ip : ips) {
            if (ip.contains(MASTER)) {
                Map<String, String> ipv4Map = new HashMap<>();
                String[] info = ip.split(N);
                log.info("info:{}", info);
//                String[] names=info[ZERO].split(MH+SPACE);
//                if (!info[ZERO].contains(MH+SPACE)){
//                    names=info[ZERO].split(SPACE+SPACE+SPACE+SPACE+SPACE);
//                }
//                name=names[ZERO];


                String[] ipNetBro = info[ONE].split(SPACE);
                log.info("ipNetBro:{}", ipNetBro);
                List<String> filterList = Arrays.stream(ipNetBro).filter(i -> !QUOTATION.equals(i)).collect(Collectors.toList());
                log.info("数据过滤之后的值为:{}", filterList);

                for (int i = ZERO; i < filterList.size(); i++) {
                    ipv4Map.put(filterList.get(i), filterList.get(++i));
                }


                BufferedReader br = new BufferedReader(new FileReader(bondPath));
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (line.contains(IPADDR)) {
                        RspObj rspObj = analysisIp(line, DY, SPACE, new byte[]{(byte) 0x02, (byte) 0x01});
                        rspObjs.add(rspObj);
                    } else if (line.contains(ADDRESS_SMALL)) {
                        RspObj rspObj = analysisIp(line, DY, ADDRESS_SMALL, new byte[]{(byte) 0x02, (byte) 0x01});
                        rspObjs.add(rspObj);
                    } else if (line.contains(NETMASK)) {
                        RspObj rspObj = analysisIp(line, DY, SPACE, new byte[]{(byte) 0x02, (byte) 0x02});
                        rspObjs.add(rspObj);
                    } else if (line.contains(NETMASK_EX)) {
                        RspObj rspObj = analysisIp(line, DY, NETMASK_EX, new byte[]{(byte) 0x02, (byte) 0x02});
                        rspObjs.add(rspObj);
                    } else if (line.contains(GATEWAY)) {
                        RspObj rspObj = analysisIp(line, DY, ADDRESS_SMALL, new byte[]{(byte) 0x02, (byte) 0x03});
                        rspObjs.add(rspObj);
                    } else if (line.contains(GATEWAY_EX)) {
                        RspObj rspObj = analysisIp(line, DY, SPACE, new byte[]{(byte) 0x02, (byte) 0x03});
                        rspObjs.add(rspObj);
                    }
                }
            }
        }
        return rspObjs;
    }

    /**
     * 解析地址信息
     *
     * @param line
     * @param sp1
     * @param sp2
     * @param bytes
     * @return
     */
    private static RspObj analysisIp(String line, String sp1, String sp2, byte[] bytes) {
        String[] split;
        if (line.contains(sp1)) {
            split = line.split(sp1);
        } else {
            split = line.split(sp2);
        }
        String ipAddress = split[ONE].trim().toString();
        log.info("获取地址信息为:{}", ipAddress);
        RspObj rspObj = setRspObj(bytes, ipAddress);
        return rspObj;
    }

    /**
     * 设置ip 信息
     *
     * @param bytes
     * @param address
     * @return
     */
    private static RspObj setRspObj(byte[] bytes, String address) {
        RspObj rspObj = new RspObj();
        rspObj.setCmdCodes(bytes);
        rspObj.setData(address.getBytes());
        return rspObj;
    }

    /**
     * 硬件信息
     */
    private static List<RspObj> Hardware() throws ReadConfigEx {
        log.info("硬件信息");
        List<RspObj> rspObjs = new ArrayList<>();
        java.text.NumberFormat numberFormat = java.text.NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);


        //CPU使用百分比
        RspObj rspObj = new RspObj();
        byte[] bytes = new byte[]{(byte) 0x03, (byte) 0x01};
        rspObj.setCmdCodes(bytes);
        float cpuInfo = NetWorkUtil.getCpuInfo();
        log.info("获取cpu信息:" + cpuInfo);
        String cpuStr = numberFormat.format(cpuInfo);
        log.info("获取cpu信息值转换之后的值为: {}", cpuStr);
        rspObj.setData((cpuStr + "%").getBytes(Charset.forName("UTF-8")));
        rspObjs.add(rspObj);

        //内存使用百分比
        RspObj rspObjMemInfo = new RspObj();
        byte[] bytesMaMe = new byte[]{(byte) 0x03, (byte) 0x02};
        rspObjMemInfo.setCmdCodes(bytesMaMe);
        Integer memInfo = NetWorkUtil.getMemInfo();
        log.info("获取内存信息:" + memInfo);
        String memStr = numberFormat.format(cpuInfo);
        log.info("获取内存信息转换之后的值为: {}", cpuStr);
        rspObjMemInfo.setData((memStr + ".0%").getBytes(Charset.forName("UTF-8")));
        rspObjs.add(rspObjMemInfo);


        //外存使用百分比
        RspObj rspObjDisk = new RspObj();
        byte[] bytesDisk = new byte[]{(byte) 0x03, (byte) 0x03};
        rspObjDisk.setCmdCodes(bytesDisk);
        float diskInfos = 0;
        try {
            diskInfos = NetWorkUtil.getDiskInfo();
        } catch (InterruptedException | IOException e) {
            log.error("在获取硬盘信息时,发生异常:{}", e.getMessage());
            diskInfos = 0;
        }
        log.info("获取硬盘信息:" + diskInfos);
        String diskStr = numberFormat.format(cpuInfo);
        log.info("获取硬盘信息转换之后的值为: {}", diskStr);
        rspObjDisk.setData((diskStr + "%").getBytes(Charset.forName("UTF-8")));
        rspObjs.add(rspObjDisk);

        return rspObjs;
    }


    /**
     * 解析 服务状态信息
     *
     * @param rspObjs
     * @param section
     * @return
     * @throws NumberFormat
     */
    private static ResultVo readSvc(List<RspObj> rspObjs, Profile.Section section) throws NumberFormat {
        //获取 服务 数量
        String signServNum = section.get(SIGN_SERV_NUM);
        log.info("配置服务数量:{}", signServNum);
        Integer sevNum = 1;
        try {
            sevNum = Integer.valueOf(signServNum);
        } catch (NumberFormatException numE) {
            log.error("获取服务数量,在转换数值时,发送异常:{}", numE.getMessage());
            throw new NumberFormat();
        }

        RspObj rspObj = new RspObj();
        byte[] bytes = new byte[]{(byte) 0x01, (byte) 0x01};
        rspObjs.add(rspObj);


        List<String> svcList = new ArrayList<>();
        for (int i = 1; i <= sevNum; i++) {


            //解析服务器名称
            rspObj.setCmdCodes(bytes);
            String sevName = section.get(SVC_NAME + i);
            svcList.add(sevName);


            //解析端口号
            String svcPort = section.get(SVC_PORT + i);
            svcList.add(svcPort);

            String serverName = section.get(SERVER_NAME + i);
            //解析服务运作状态 getSvcStatus(svcPort,serverName)
            String status = STATUS_IS_TRUE;
            try {
                status = getSvcStatus(svcPort, serverName);
            } catch (Exception e) {
                log.error("获取服务状态时,发生异常:{}", e.getMessage());
                status = STATUS_IS_FALSE;
            }
            svcList.add(status);
        }
        log.info("获取服务状态信息为：{}", svcList);
        String svcStr = svcList.toString().replace(Z_RIGHT, QUOTATION)
                .replace(Z_LEFT, QUOTATION);
        log.info("去括号之后的值为:{}", svcStr);
        byte[] svcListByte = svcStr.getBytes(Charset.forName("UTF-8"));
        rspObj.setData(svcListByte);

        log.info("解析配置文件信息为：{}", rspObjs);
        return ResultUtil.success(rspObjs);
    }

    private static String getSvcStatus(String port, String svcName) throws Exception {
        String cmd = "netstat -tnlp |grep " + svcName + "|grep " + svcName;
        Map<String, Object> svcStatusMap = NetWorkUtil.listInfo(cmd);
        log.info("服务状态信息为:{}", svcStatusMap);
        boolean status = (boolean) svcStatusMap.get(STATUS);
        if (!status) {
            return STATUS_IS_FALSE;
        }
        List statusList = (List) svcStatusMap.get(DATA);
        return (statusList.size() >= ONE ? STATUS_IS_TRUE : STATUS_IS_FALSE);
    }

    /**
     * 读取配置文件
     *
     * @param configPath 配置文件地址
     * @return
     */
    public static ResultVo readConfig(String configPath, String winName) throws ReadConfigEx {

        try {
            Wini wini = new Wini(new File(configPath));
            //读取配置文件中 SVC 模块 数据
            Profile.Section section = wini.get(winName);
            return ResultUtil.success(section);
        } catch (IOException e) {
            log.error("读取配置文件发生异常:{}", e.getMessage());
            throw new ReadConfigEx();
        }

    }
}
