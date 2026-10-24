package com.example.rbacdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.rbacdemo.entity.SysUserRole;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    @Insert("<script>"+
            "INSERT INTO sys_user_role (user_id, role_id) VALUES "+
            "<foreach collection='list' item = 'item' separator=','>"+
            "(#{item.userId},#{item.roleId})"+
            "</foreach>"+
            "</script>")
    void insertBatch(@Param("list") List<SysUserRole> list);
}
