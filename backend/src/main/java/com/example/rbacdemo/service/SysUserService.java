package com.example.rbacdemo.service;

import com.example.rbacdemo.dto.AssignRoleDTO;
import com.example.rbacdemo.dto.UserAddDTO;
import com.example.rbacdemo.dto.UserUpdateDTO;
import com.example.rbacdemo.entity.SysUser;
import java.util.List;
import java.util.Map;

public interface SysUserService {
    List<SysUser> listAll();
    SysUser getByUsername(String username);
    Map<String,Object> login(String username,String password);
    SysUser register(String username,String password);
    void addUser(UserAddDTO dto);
    void assignRole(AssignRoleDTO dto);
    void deleteUser(Long id);
    void userUpdate(UserUpdateDTO dto);
    Map<String,Object> getUserInfo(Long userId);
}
