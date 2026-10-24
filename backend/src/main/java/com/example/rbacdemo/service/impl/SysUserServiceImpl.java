package com.example.rbacdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.rbacdemo.common.RedisUtil;
import com.example.rbacdemo.common.SecurityUtil;
import com.example.rbacdemo.dto.AssignRoleDTO;
import com.example.rbacdemo.dto.UserAddDTO;
import com.example.rbacdemo.dto.UserUpdateDTO;
import com.example.rbacdemo.entity.SysRole;
import com.example.rbacdemo.entity.SysUser;
import com.example.rbacdemo.entity.SysUserRole;
import com.example.rbacdemo.mapper.SysPermissionMapper;
import com.example.rbacdemo.mapper.SysRoleMapper;
import com.example.rbacdemo.mapper.SysUserRoleMapper;
import com.example.rbacdemo.service.SysUserService;
import com.example.rbacdemo.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.rbacdemo.common.JwtUtil;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysUserServiceImpl implements SysUserService {
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Override
    public List<SysUser> listAll(){
        return sysUserMapper.selectList(null);
    }

    @Override
    public SysUser getByUsername(String username){
        QueryWrapper<SysUser>wrapper = new QueryWrapper<>();
        wrapper.eq("username",username);
        return sysUserMapper.selectOne(wrapper);
    }
    @Override
    public Map<String,Object> login(String username,String password){
        SysUser user = getByUsername(username);
        if(user==null){
            throw new RuntimeException("用户不存在");
        }
        if(!SecurityUtil.matches(password,user.getPassword())){
            throw new RuntimeException("账号或密码错误");
        }
        if(user.getStatus()==0){
            throw new RuntimeException("用户已被禁用");
        }
        String token=JwtUtil.generateToken(user.getId(),user.getUsername());
        Map<String,Object> result =new HashMap<>();
        result.put("user",user);
        result.put("token",token);
        return result;
    }

    @Override
    public Map<String,Object> getUserInfo(Long userId){
        SysUser user =sysUserMapper.selectById(userId);
        if(user == null){
            throw new RuntimeException("用户不存在");
        }
        List<String> permissions = sysPermissionMapper.findPermissionCodesByUserId(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("username",user.getUsername());
        data.put("permissions",permissions);
        return data;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser register(String username,String password){
        SysUser user=getByUsername(username);
        if(user!=null){
            throw new RuntimeException("该用户名已被注册");
        }
        SysUser newUser =new SysUser();
        newUser.setUsername(username);
        newUser.setPassword(SecurityUtil.encrypt(password));
        sysUserMapper.insert(newUser);

        QueryWrapper<SysRole> roleWrapper = new QueryWrapper<>();
        roleWrapper.eq("role_code","OPERATOR");
        SysRole defaultRole = sysRoleMapper.selectOne(roleWrapper);
        if(defaultRole!=null){
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(newUser.getId());
            userRole.setRoleId(defaultRole.getId());
            sysUserRoleMapper.insert(userRole);
        }
        else{
            throw new RuntimeException("系统配置错误：默认角色不存在");
        }

        return newUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserAddDTO dto){
        SysUser existingUser = getByUsername(dto.getUsername());
        if(existingUser!=null){
            throw new RuntimeException("用户名已存在");
        }

        SysUser newUser = new SysUser();
        newUser.setUsername(dto.getUsername());
        newUser.setPassword(SecurityUtil.encrypt(dto.getPassword()));
        newUser.setStatus(dto.getStatus() == null ? 1:dto.getStatus());
        sysUserMapper.insert(newUser);

        if(dto.getRoleIds()!=null && !dto.getRoleIds().isEmpty()){
            QueryWrapper<SysRole> roleWrapper = new QueryWrapper<>();
            roleWrapper.in("id",dto.getRoleIds());
            Long count = sysRoleMapper.selectCount(roleWrapper);//看能在数据库查到几个
            if(count!=dto.getRoleIds().size()){//如果查到的个数和传入的个数不一样就说明有不存在的角色ID
                throw new RuntimeException("包含不存在的角色ID，请检查");
            }
            List<SysUserRole> userRoles=new ArrayList<>();
            for(Long roleId : dto.getRoleIds()){
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(newUser.getId());
                userRole.setRoleId(roleId);
                userRoles.add(userRole);
            }
            sysUserRoleMapper.insertBatch(userRoles);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRole(AssignRoleDTO dto){
        SysUser existingUser = sysUserMapper.selectById(dto.getUserId());
        if(existingUser == null){
            throw new RuntimeException("用户不存在");
        }

        redisUtil.delete("permission:user:"+dto.getUserId());

        QueryWrapper<SysUserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", dto.getUserId());
        sysUserRoleMapper.delete(wrapper);

        if(dto.getRoleIds()!=null && !dto.getRoleIds().isEmpty()){
            QueryWrapper<SysRole> roleWrapper = new QueryWrapper<>();
            roleWrapper.in("id",dto.getRoleIds());
            Long count = sysRoleMapper.selectCount(roleWrapper);
            if(count!=dto.getRoleIds().size()){
                throw new RuntimeException("包含不存在的角色ID，请检查");
            }
            List<SysUserRole> sysUserRoles = new ArrayList<>();
            for (Long roleId : dto.getRoleIds()){
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(dto.getUserId());
                userRole.setRoleId(roleId);
                sysUserRoles.add(userRole);
            }
            sysUserRoleMapper.insertBatch(sysUserRoles);
        }
        //redisUtil.delete("permission:user:"+dto.getUserId());

        new Thread(()->{
            try{
                Thread.sleep(500);
                redisUtil.delete("permission:user:"+dto.getUserId());
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }).start();

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser (Long id) {
        SysUser existingUser = sysUserMapper.selectById(id);
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        redisUtil.delete("permission:user:"+id);
        QueryWrapper<SysUserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", id);
        sysUserRoleMapper.delete(wrapper);
        //sysUserMapper.deleteById(id);

        String newUsername = existingUser.getUsername()+"_del_"+System.currentTimeMillis();
        UpdateWrapper<SysUser>updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id)
                        .set("username",newUsername)
                                .set("is_deleted",1);
        sysUserMapper.update(null,updateWrapper);

        //redisUtil.delete("permission:user:"+id);
        redisUtil.set("user:disabled:"+id,String.valueOf(System.currentTimeMillis()),24*60);

        new Thread(()->{
            try{
                Thread.sleep(500);
                redisUtil.delete("permission:user:"+id);
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void userUpdate(UserUpdateDTO dto){
        SysUser existingUser = sysUserMapper.selectById(dto.getId());
        if(existingUser ==null){
            throw new RuntimeException("用户不存在");
        }
        redisUtil.delete("permission:user:"+dto.getId());
        if(dto.getPassword()!=null && !dto.getPassword().isEmpty()){
            existingUser.setPassword(SecurityUtil.encrypt(dto.getPassword()));
        }
        if(dto.getStatus()!=null){
            existingUser.setStatus(dto.getStatus());
        }
        sysUserMapper.updateById(existingUser);
        //redisUtil.delete("permission:user:"+dto.getId());
        if(dto.getStatus()!=null){
            if(dto.getStatus()==0){
                redisUtil.set("user:disabled:"+dto.getId(),String.valueOf(System.currentTimeMillis()),24*60);
                new Thread(()->{
                    try{
                        Thread.sleep(500);
                        redisUtil.delete("permission:user:"+dto.getId());
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }).start();
            }
            else{
                redisUtil.delete("user:disabled:"+dto.getId());
            }
        }
    }
}
