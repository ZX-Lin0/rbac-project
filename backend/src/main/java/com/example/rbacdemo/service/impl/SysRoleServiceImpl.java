package com.example.rbacdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.rbacdemo.common.RedisUtil;
import com.example.rbacdemo.dto.AssignPermissionDTO;
import com.example.rbacdemo.dto.RoleAddDTO;
import com.example.rbacdemo.dto.RoleUpdateDTO;
import com.example.rbacdemo.entity.SysPermission;
import com.example.rbacdemo.entity.SysRole;
import com.example.rbacdemo.entity.SysRolePermission;
import com.example.rbacdemo.entity.SysUserRole;
import com.example.rbacdemo.mapper.SysPermissionMapper;
import com.example.rbacdemo.mapper.SysRoleMapper;
import com.example.rbacdemo.mapper.SysRolePermissionMapper;
import com.example.rbacdemo.mapper.SysUserRoleMapper;
import com.example.rbacdemo.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SysRoleServiceImpl implements SysRoleService {
    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Override
    public SysRole getByRoleCode(String roleCode){
        QueryWrapper<SysRole> wrapper = new QueryWrapper<>();
        wrapper.eq("role_code",roleCode);
        return sysRoleMapper.selectOne(wrapper);
    }

    @Override
    public List<SysRole> listAll(){return sysRoleMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole (RoleAddDTO dto){
        SysRole existingRole = getByRoleCode(dto.getRoleCode());
        if(existingRole != null){
            throw new RuntimeException("角色已存在");
        }

        SysRole newRole =new SysRole();
        newRole.setRoleCode(dto.getRoleCode());
        newRole.setRoleName(dto.getRoleName());
        if(dto.getDescription()!=null){
            newRole.setDescription(dto.getDescription());
        }
        sysRoleMapper.insert(newRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermission (AssignPermissionDTO dto){
        /*SysRole existingRole = getByRoleCode(dto.getRoleCode());
        if(existingRole == null){
            throw new RuntimeException("角色不存在");
        }*/
        SysRole existingRole = sysRoleMapper.selectById(dto.getRoleId());
        if(existingRole == null){
            throw new RuntimeException("角色不存在");
        }
        redisUtil.deleteByPattern("permission:user:*");
        QueryWrapper<SysRolePermission> wrapper =new QueryWrapper<>();
        wrapper.eq("role_id",dto.getRoleId());
        sysRolePermissionMapper.delete(wrapper);
        if(dto.getPermissionIds()!=null && !dto.getPermissionIds().isEmpty()){
            QueryWrapper<SysPermission> permissionWrapper = new QueryWrapper<>();
            permissionWrapper.in("id",dto.getPermissionIds());
            Long count = sysPermissionMapper.selectCount(permissionWrapper);
            if(count!= dto.getPermissionIds().size()){
                throw new RuntimeException("包含不存在的权限ID，请检查");
            }
            List<SysRolePermission> rolePermissions =new ArrayList<>();
            for(Long permissionId : dto.getPermissionIds()){
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(dto.getRoleId());
                rolePermission.setPermissionId(permissionId);
                rolePermissions.add(rolePermission);
            }
            sysRolePermissionMapper.insertBatch(rolePermissions);
        }
        //redisUtil.deleteByPattern("permission:user:*");
        new Thread(()->{
            try{
                Thread.sleep(500);
                redisUtil.deleteByPattern("permission:user:*");
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole (Long id){
        SysRole existingRole = sysRoleMapper.selectById(id);
        if(existingRole ==null){
            throw new RuntimeException("角色不存在");
        }
        redisUtil.deleteByPattern("permission:user:*");
        QueryWrapper<SysUserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id",id);
        sysUserRoleMapper.delete(wrapper);

        QueryWrapper<SysRolePermission> wrapper1= new QueryWrapper<>();
        wrapper1.eq("role_id",id);
        sysRolePermissionMapper.delete(wrapper1);

        sysRoleMapper.deleteById(id);
        //redisUtil.deleteByPattern("permission:user:*");
        new Thread(()->{
            try{
                Thread.sleep(500);
                redisUtil.deleteByPattern("permission:user:*");
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }).start();

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void roleUpdate (RoleUpdateDTO dto){
        SysRole existingRole = sysRoleMapper.selectById(dto.getId());
        if(existingRole == null ){
            throw new RuntimeException("角色不存在");
        }
        redisUtil.deleteByPattern("permission:user:*");
        if(dto.getRoleName()!=null && !dto.getRoleName().isEmpty()){
            existingRole.setRoleName(dto.getRoleName());
        }
        if(dto.getDescription()!=null && !dto.getDescription().isEmpty()){
            existingRole.setDescription(dto.getDescription());
        }
        sysRoleMapper.updateById(existingRole);
        //redisUtil.deleteByPattern("permission:user:*");
        new Thread(()->{
            try{
                Thread.sleep(500);
                redisUtil.deleteByPattern("permission:user:*");
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }).start();
    }
}
