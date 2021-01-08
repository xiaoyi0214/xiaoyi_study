package com.miaoshaproject.response;

/**
 * Created on 2021/1/3.
 *
 * @author 小逸
 * @description
 */
public class CommonReturnType {
    // 对应请求的返回处理结果success/fail
    private String status;
    // 若status = success data内返回json数据
    // 若status = fail  data内使用通用的错误码格式
    private Object data;

    public static CommonReturnType create(Object result){
        return CommonReturnType.create(result,"success");
    }
    public static CommonReturnType create(Object result,String status){
        CommonReturnType type = new CommonReturnType();
        type.setStatus(status);
        type.setData(result);
        return type;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }


    
}
