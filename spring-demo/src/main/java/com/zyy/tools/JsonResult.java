package com.zyy.tools;

/**
 * Created by zyy on 2017/2/26.
 */
public class JsonResult {

    private int code = 200;
    private String message = "请求成功";
    private Object obj;

    public JsonResult() {
    }

    public JsonResult(int code, String message, Object obj) {
        this.code = code;
        this.message = message;
        this.obj = obj;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
