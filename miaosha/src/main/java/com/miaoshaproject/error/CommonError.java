package com.miaoshaproject.error;

/**
 * Created on 2021/1/3.
 *
 * @author 小逸
 * @description
 */
public interface CommonError {
    public int getErrCode();
    public String getErrMsg();
    public CommonError setErrMsg(String errMsg);
}
