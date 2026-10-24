package com.example.rbacdemo.dto;


import lombok.Data;

import java.util.List;

@Data
public class AssignPermissionDTO {
    private String roleCode;
    private Long roleId;
    private List<Long> permissionIds;
}
