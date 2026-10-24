package com.example.rbacdemo.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
@Component
public class SecurityUtil {
    private static final BCryptPasswordEncoder encoder =new BCryptPasswordEncoder();
    public static String encrypt(String rawPassword){
        return encoder.encode(rawPassword);
    }
    public static boolean matches(String rawPassword,String encodedPassword){
        return encoder.matches(rawPassword,encodedPassword);
    }
}
