package com.example.rbacdemo.interceptor;

import com.example.rbacdemo.common.JwtUtil;
import com.example.rbacdemo.common.RedisUtil;
import com.example.rbacdemo.common.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler) throws Exception {
        if("OPTIONS".equalsIgnoreCase(request.getMethod())){
            return true;
        }

        String token = request.getHeader("Authorization");
        if(token==null||token.isEmpty()){
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录，请先登陆\",\"data\":null}");
            return false;
        }
        if (token.startsWith("Bearer ")){
            token=token.substring(7);
        }
        try{
            Claims claims = JwtUtil.parseToken(token);
            request.setAttribute("userId",claims.get("userId"));
            UserContext.setUserId(claims.get("userId",Long.class));
            Long userId = claims.get("userId", Long.class);
            if(redisUtil.exists("user:disabled:"+userId)){
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"账号已禁用\",\"data\":null}");
                return false;
            }
            return true;
        } catch (Exception e){
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"登陆状态无效或过期，请重新登陆\",\"data\":null}");
            return false;
        }
    }
    @Override
    public void afterCompletion(HttpServletRequest request,HttpServletResponse response,Object handler,Exception ex) throws Exception{
        UserContext.clear();
    }
}
