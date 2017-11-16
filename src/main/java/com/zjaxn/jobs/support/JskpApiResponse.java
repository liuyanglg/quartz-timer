package com.zjaxn.jobs.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public class JskpApiResponse implements Serializable {
    private String code;
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private Object data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public  <T> T getJavaObject(Class<T> clazz) throws Exception{
        JSON json = (JSON) JSONObject.toJSON(data);
        T value = JSONObject.toJavaObject(json, clazz);
        return value;
    }
}
