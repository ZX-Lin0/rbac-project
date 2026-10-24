package com.example.rbacdemo.common;

import lombok.Data;
@Data
public class R<T> {
    private String msg;
    private Integer code;
    private T data;

    public static <T> R<T> ok(){
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("操作成功");
        return r;
    }
    public static <T> R<T> ok(T data){
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("操作成功");
        r.setData(data);
        return r;
    }
    public static <T> R<T> fail(String msg){
        R<T> r = new R<>();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }
}
