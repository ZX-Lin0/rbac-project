package com.example.rbacdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.rbacdemo.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import java.util.List;
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    @Select("SELECT DISTINCT p.permission_code "+
            "FROM sys_user u "+
            "JOIN sys_user_role ur ON u.id = ur.user_id "+
            "JOIN sys_role_permission rp ON ur.role_id = rp.role_id "+
            "JOIN sys_permission p ON rp.permission_id = p.id "+
            "WHERE u.id =#{userId}"
    )
    List<String> findPermissionCodesByUserId(@Param("userId") Long userId);

}
