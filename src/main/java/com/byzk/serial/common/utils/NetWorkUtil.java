package com.byzk.serial.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Linux 脚本执行公共方法
 * @Author: wy
 * @Date: 2023/7/20  10:28
 * @Version 1.0
 */
@Slf4j
public class NetWorkUtil {
    private static final float FLOAT = 0f;
    private static final float DISK_INFO_NUM = 100;
    private static float FLOAT_EQUALS=1e-6f;

    /**
     * 获取CPU信息
     *
     * @return
     */
    public static float getCpuInfo() {
        File file = new File("/proc/stat");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            StringTokenizer tokenizer = new StringTokenizer(reader.readLine());
            tokenizer.nextToken();
            Long user1 = Long.valueOf(tokenizer.nextToken());
            Long nice1 = Long.valueOf(tokenizer.nextToken());
            Long sys1 = Long.valueOf(tokenizer.nextToken());
            Long idle1 = Long.valueOf(tokenizer.nextToken());

            Thread.sleep(1000);

            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            tokenizer = new StringTokenizer(reader.readLine());
            tokenizer.nextToken();
            Long user2 = Long.valueOf(tokenizer.nextToken());
            Long nice2 = Long.valueOf(tokenizer.nextToken());
            Long sys2 = Long.valueOf(tokenizer.nextToken());
            Long idle2 = Long.valueOf(tokenizer.nextToken());

            Long v1 = (user2 + nice2 + sys2) - (user1 + nice1 + sys1);

            Long v2 = (user2 + nice2 + sys2 + idle2) - (user1 + nice1 + sys1 + idle1);
            float v3 = (v1 * 1.0f / v2) * 100;
            return v3;

        } catch (FileNotFoundException e) {
            log.error("获取CPU信息失败" + e.getMessage());
            return 0;
        } catch (IOException e) {
            log.error("获取CPU信息失败" + e.getMessage());
            return 0;
        } catch (InterruptedException e) {
            log.error("获取CPU信息失败" + e.getMessage());
            return 0;
        } catch (Exception e) {
            log.error("获取CPU信息失败" + e.getMessage());
            return 0;
        }
    }


    /**
     * 获取内存信息
     *
     * @return
     */
    public static int getMemInfo() {
        String totalCmd = "free -m |awk 'NR==2 {print $2}'";
        String usedCmd = "free -m |awk 'NR==2 {print $3}'";
        try {
            Map<String, Object> totalCmdResult = listInfo(totalCmd);
            Map<String, Object> usedCmdCmdResult = listInfo(usedCmd);
            List totalData = (List) totalCmdResult.get("data");
            List usedCmdData = (List) usedCmdCmdResult.get("data");
            String totalStr = (String) totalData.get(0);
            String usedStr = (String) usedCmdData.get(0);

            Integer memTotal = Integer.parseInt(totalStr);
            Integer meUsed = Integer.parseInt(usedStr);

            int intValue = new BigDecimal(usedStr)
                    .divide(BigDecimal.valueOf(memTotal), 2, BigDecimal.ROUND_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .intValue();
            return intValue;
        } catch (Exception e) {
            log.error("获取内存信息 失败" + e.getMessage());
            return 0;
        }
    }

    /**
     * 获取硬盘信息
     *
     * @return
     */
    public static float getDiskInfo() throws InterruptedException, IOException {
        String command = "df -h";
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(command);
        p.waitFor();
        BufferedReader in = null;
        in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String str = null;
        String[] s = null;

        int line = 0;
        float diskInfo = 0;
        float yDiskInfo = 0;

        while ((str = in.readLine()) != null) {
            s = str.split(" ");
            int count = 0;
            if (str.contains("dev")) {
                if (!(str.contains("tmpfs") || str.contains("udev"))) {
                    for (String para : s) {
                        boolean info = false;
                        if (count == 0) {
                            if (! "0" .equals(para)) {
                                Float aFloat = 0f;
                                if (para.endsWith("G")) {
                                    info = true;
                                    aFloat = Float.valueOf(para.substring(0, para.length() - 1));
                                }
                                if (para.endsWith("M")) {
                                    info = true;
                                    aFloat = Float.valueOf(para.substring(0, para.length() - 1)) / 1000;
                                }
                                if (para.endsWith("K")) {
                                    info = true;
                                    aFloat = Float.valueOf(para.substring(0, para.length() - 1)) / 1000 / 1000;
                                }
                                diskInfo += aFloat;
                            }
                        }
                        if (count == 1) {
                            if (!"0" .equals(para)) {
                                Float aFloat = 0f;
                                if (para.endsWith("G")) {
                                    info = false;
                                    aFloat = Float.valueOf(para.substring(0, para.length() - 1));
                                    count = 0;
                                    yDiskInfo += aFloat;
                                    break;
                                }
                                if (para.endsWith("M")) {
                                    info = false;
                                    aFloat = Float.valueOf(para.substring(0, para.length() - 1)) / 1000;
                                    count = 0;
                                    yDiskInfo += aFloat;
                                    break;
                                }
                                if (para.endsWith("K")) {
                                    info = false;
                                    aFloat = Float.valueOf(para.substring(0, para.length() - 1)) / 1000 / 1000;
                                    count = 0;
                                    yDiskInfo += aFloat;
                                    break;
                                }
                            } else if ("0" .equals(para)) {
                                count = 0;
                                yDiskInfo += 0;
                                break;
                            }
                        }
                        if (info) {
                            count++;
                        }
                    }
                }

            }
        }

        //diskInfo 为0
        if (Math.abs(diskInfo - FLOAT) < FLOAT_EQUALS) {
            return FLOAT;
        }
        float yRate = yDiskInfo / diskInfo / DISK_INFO_NUM;
        return yRate;
    }


    public static Map<String, Object> listInfo(String cmd) throws Exception {
        log.info("命令： {}", cmd);
        String[] comands = new String[]{"/bin/sh", "-c", cmd};

        Process process = null;
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;

        try {
            process = Runtime.getRuntime().exec(comands);
            //方法阻塞，等待名录库执行完成
            process.waitFor();

            // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（PS: 子进程的输出就是主进程的输入）
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            String line;
            Map<String, Object> map = new HashMap<>();
            while ((line = bufrError.readLine()) != null) {
                log.error("获取配置,{}", line);
                map.put("status", false);
                map.put("data", line);
                return map;
            }
            ArrayList<String> list = new ArrayList<>();
            while ((line = bufrIn.readLine()) != null) {
                list.add(line);
            }
            map.put("status", true);
            map.put("data", list);
            return map;
        } finally {
            if (bufrIn != null) {
                bufrIn.close();
            }
            if (bufrError != null) {
                bufrError.close();
            }
            if (process != null) {
                process.destroy();
            }
        }
    }
}
