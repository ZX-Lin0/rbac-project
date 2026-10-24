package com.example.rbacdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import com.example.rbacdemo.entity.SysRolePermission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
    @Insert("<script>"+
            "INSERT INTO sys_role_permission (role_id, permission_id) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.roleId},#{item.permissionId})"+
            "</foreach>"+
            "</script>"
    )
    void insertBatch(@Param("list") List<SysRolePermission> list);
}
