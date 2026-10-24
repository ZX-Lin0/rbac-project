package com.example.rbacdemo.exception;

public class NoPermissionException extends RuntimeException {
    public NoPermissionException(String message){
        super(message);
    }
}
