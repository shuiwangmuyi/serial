package com.byzk.serial.common.utils;

import com.byzk.serial.vo.ResultVo;

/**
 * 同意返回值处理
 *
 * @Author:wy
 * @Date: 2023/7/20  9:17
 * @Version 1.0
 */
public class ResultUtil {
    private static final boolean TRUE = true;
    private static final boolean FALSE = false;

    public static ResultVo success(Object o) {
        ResultVo vo = new ResultVo();
        vo.setCode(TRUE);
        vo.setData(o);
        return vo;
    }

    public static ResultVo error(Object o) {
        ResultVo vo = new ResultVo();
        vo.setCode(FALSE);
        vo.setData(o);
        return vo;
    }
}
