package com.example.rbacdemo.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET = "rbac-demo-secret-key-must-be-at-least-256-bits-long-for-hs256";
    private static final long EXPIRATION =24*60*60*1000;
    private static final SecretKey KEY=Keys.hmacShaKeyFor(SECRET.getBytes());
    public static String generateToken(Long userId,String username){
        return Jwts.builder()
                .claim("userId",userId)
                .claim("username",username)//上面两个都加入token负载
                .issuedAt(new Date()) //签发时间：现在
                .expiration(new Date(System.currentTimeMillis()+EXPIRATION))
                .signWith(KEY)
                .compact();
    }
    public static Claims parseToken(String token){
        return Jwts.parser()
                .verifyWith(KEY)//检验密钥是否为该系统签发
                .build()
                .parseSignedClaims(token)//解析token
                .getPayload();//取出负载
    }
    public static Long getUserId(String token){
        return parseToken(token).get("userId",Long.class);
    }
    public static String getUsername(String token){
        return parseToken(token).get("username",String.class);
    }
}
