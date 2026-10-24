package com.example.rbacdemo.controller;


import com.example.rbacdemo.annotation.RequirePermission;
import com.example.rbacdemo.common.R;
import com.example.rbacdemo.dto.AssignPermissionDTO;
import com.example.rbacdemo.dto.RoleAddDTO;
import com.example.rbacdemo.dto.RoleUpdateDTO;
import com.example.rbacdemo.entity.SysRole;
import com.example.rbacdemo.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name ="角色管理",description = "角色添加，管理，查询等相关接口")
@RestController
@RequestMapping("/role")
public class RoleController {
    @Autowired
    private SysRoleService sysRoleService;

    @Operation(summary = "获取所有角色列表", description = "需要 role:list 的权限")
    @RequirePermission("role:list")
    @GetMapping("/list")
    public R<List<SysRole>> list(){
        List<SysRole>roles = sysRoleService.listAll();
        return R.ok(roles);
    }

    @Operation(summary = "增加角色", description = "需要 role:add 的权限")
    @RequirePermission("role:add")
    @PostMapping("/add")
    public R<String> addRole(@RequestBody RoleAddDTO dto){
        sysRoleService.addRole(dto);
        return R.ok("新增成功");
    }

    @Operation(summary = "管理角色", description = "需要role:assign 的权限")
    @RequirePermission("role:assign")
    @PostMapping("/assign")
    public R<String> assignRolePermission(@RequestBody AssignPermissionDTO dto){
        sysRoleService.assignPermission(dto);
        return R.ok("修改成功");
    }

    @Operation(summary = "删除角色",description = "需要 role:delete 权限")
    @RequirePermission("role:delete")
    @DeleteMapping("/delete/{id}")
    public R<String> deleteRole(@PathVariable Long id){
        sysRoleService.deleteRole(id);
        return R.ok("删除成功");
    }

    @Operation(summary = "修改角色", description = "需要 role:update 权限")
    @RequirePermission("role:update")
    @PutMapping("/update")
    public R<String> roleUpdate(RoleUpdateDTO dto){
        sysRoleService.roleUpdate(dto);
        return R.ok("修改成功");
    }
}
