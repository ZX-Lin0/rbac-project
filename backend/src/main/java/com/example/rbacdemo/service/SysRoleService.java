package com.example.rbacdemo.service;

import com.example.rbacdemo.dto.AssignPermissionDTO;

import com.example.rbacdemo.dto.RoleAddDTO;
import com.example.rbacdemo.dto.RoleUpdateDTO;
import com.example.rbacdemo.entity.SysRole;


import java.util.List;

public interface SysRoleService {
    void addRole (RoleAddDTO dto);
    List<SysRole> listAll();
    void assignPermission(AssignPermissionDTO dto);
    SysRole getByRoleCode (String roleCode);
    void deleteRole (Long id);
    void roleUpdate (RoleUpdateDTO dto);
}
