package com.wj.aischedule.util;

public class ResponseResult<T> {

    private int code;
    private String message;
    private T data;

    public ResponseResult() {}

    public ResponseResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功返回
    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(200, "success", data);
    }

    public static <T> ResponseResult<T> success() {
        return new ResponseResult<>(200, "success", null);
    }

    // 失败返回
    public static <T> ResponseResult<T> error(String msg) {
        return new ResponseResult<>(500, msg, null);
    }

    // getter & setter
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
