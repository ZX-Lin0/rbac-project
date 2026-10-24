package com.example.rbacdemo.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserAddDTO {
    private String username;
    private String password;
    private Integer status;
    private List<Long> roleIds;
}
