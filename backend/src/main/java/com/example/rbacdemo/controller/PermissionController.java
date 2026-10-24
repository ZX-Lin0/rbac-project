package com.example.rbacdemo.controller;


import com.example.rbacdemo.annotation.RequirePermission;
import com.example.rbacdemo.common.R;
import com.example.rbacdemo.entity.SysPermission;
import com.example.rbacdemo.service.SysPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "权限管理", description = "权限相关接口")
@RestController
@RequestMapping("/permission")
public class PermissionController {
    @Autowired
    private SysPermissionService sysPermissionService;

    @Operation(summary = "列出所有权限", description = "需要 role:list 权限")
    @RequirePermission("role:list")
    @GetMapping("/list")
    public R<List<SysPermission>> list(){
        List<SysPermission>permissions=sysPermissionService.listAll();
        return R.ok(permissions);
    }
}
