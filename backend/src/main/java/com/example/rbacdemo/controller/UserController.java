package com.example.rbacdemo.controller;

import com.example.rbacdemo.common.UserContext;
import com.example.rbacdemo.dto.AssignRoleDTO;
import com.example.rbacdemo.dto.UserAddDTO;
import com.example.rbacdemo.dto.UserUpdateDTO;
import com.example.rbacdemo.entity.SysUser;
import com.example.rbacdemo.mapper.SysPermissionMapper;
import com.example.rbacdemo.service.SysUserService;
import com.example.rbacdemo.common.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import com.example.rbacdemo.annotation.RequirePermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="用户管理",description = "用户注册，登陆，查询相关接口")
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private SysUserService sysUserService;

    @Operation(summary = "获取所有用户列表", description = "需要 user:list 权限")
    @RequirePermission("user:list")
    @GetMapping("/list")
    public R<List<SysUser>> list(){
        List<SysUser>users= sysUserService.listAll();
        return R.ok(users);
    }
    @Operation(summary = "获取用户名字", description = "返回用户名")
    @GetMapping("/get/{username}")
    public SysUser getByUsername(@PathVariable String username){
        return sysUserService.getByUsername(username);
    }

    @Operation(summary = "用户登陆", description = "登陆成功返回token和用户信息")
    @PostMapping("/login")
    public R<Map<String,Object>>login(@RequestBody SysUser loginUser){
        Map<String,Object> result = sysUserService.login(loginUser.getUsername(),loginUser.getPassword());
        return R.ok(result);
    }

    @Operation(summary = "获取当前登陆用户信息及权限")
    @GetMapping("/info")
    public R<Map<String,Object>>getUserInfo(){
        Long userId = UserContext.getUserId();

        Map<String, Object> data = sysUserService.getUserInfo(userId);
        return R.ok(data);
    }

    @Operation(summary = "用户注册", description = "新注册一个用户")
    @PostMapping("/register")
    public R<SysUser> register(@RequestBody SysUser registerUser){
        SysUser user = sysUserService.register(registerUser.getUsername(),registerUser.getPassword());
        return R.ok(user);
    }

    @Operation(summary = "新增用户",description = "需要 user:add 权限")
    @RequirePermission("user:add")
    @PostMapping("/add")
    public  R<String> addUser(@RequestBody UserAddDTO dto){
        sysUserService.addUser(dto);
        return R.ok("新增成功");
    }

    @Operation(summary = "分配角色", description = "需要 user:assign权限")
    @RequirePermission("user:assign")
    @PostMapping("/assignRole")
    public R<String> assignRole(@RequestBody AssignRoleDTO dto){
        sysUserService.assignRole(dto);
        return R.ok("修改成功");
    }

    @Operation(summary = "删除用户", description = "需要 user:delete 权限")
    @RequirePermission("user:delete")
    @DeleteMapping("/delete/{id}")
    public R<String> deleteUser (@PathVariable Long id){
        sysUserService.deleteUser(id);
        return R.ok("删除成功");
    }

    @Operation(summary = "修改用户", description = "需要 user:update 的权限")
    @RequirePermission("user:update")
    @PutMapping("/update")
    public R<String> updateUser (@RequestBody UserUpdateDTO dto){
        sysUserService.userUpdate(dto);
        return R.ok("修改成功");
    }
}
