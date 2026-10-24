package com.example.rbacdemo.common;

import com.example.rbacdemo.exception.NoPermissionException;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.slf4j.Logger;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoPermissionException.class)
    public R<String> handleNoPermission(NoPermissionException e){
        R<String> r=new R<>();
        r.setCode(403);
        r.setMsg(e.getMessage());
        return r;
    }
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(Exception.class)
    public R<String> handleException(Exception e){
        log.error("系统异常",e);
        return R.fail("服务器内部错误：" + e.getMessage());

    }
}
