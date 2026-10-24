package com.example.rbacdemo.aspect;

import com.example.rbacdemo.annotation.RequirePermission;
import com.example.rbacdemo.common.RedisUtil;
import com.example.rbacdemo.common.UserContext;
import com.example.rbacdemo.mapper.SysPermissionMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.rbacdemo.exception.NoPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Aspect
@Component
public class PermissionAspect {
    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Autowired
    private RedisUtil redisUtil;

    private static final Logger log = LoggerFactory.getLogger(PermissionAspect.class);

    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint,RequirePermission requirePermission){
        Long userId = UserContext.getUserId();
        if(userId == null){
            throw new RuntimeException("未登录");
        }

        String cacheKey = "permission:user:" + userId;

        List<String> permissionCodes = redisUtil.getList(cacheKey);
        if(permissionCodes==null){
            permissionCodes = sysPermissionMapper.findPermissionCodesByUserId(userId);
            redisUtil.setList(cacheKey,permissionCodes,30);
            log.debug("缓存未命中，查数据库,userId={}",userId);
        }
        else{
            log.debug("缓存命中,userId={}",userId);
        }
        //List<String> permissionCodes = sysPermissionMapper.findPermissionCodesByUserId(userId);
        String requiredPermission = requirePermission.value();
        if(!permissionCodes.contains(requiredPermission)){
            throw new NoPermissionException("无权访问: "+requiredPermission);
        }
    }
}
