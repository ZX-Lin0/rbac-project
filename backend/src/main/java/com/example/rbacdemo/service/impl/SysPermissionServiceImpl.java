package com.example.rbacdemo.service.impl;

import com.example.rbacdemo.entity.SysPermission;
import com.example.rbacdemo.mapper.SysPermissionMapper;
import com.example.rbacdemo.service.SysPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysPermissionServiceImpl implements SysPermissionService {

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Override
    public List<SysPermission> listAll() {return sysPermissionMapper.selectList(null);}
}
