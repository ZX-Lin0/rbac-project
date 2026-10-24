package com.example.rbacdemo.dto;


import lombok.Data;

@Data
public class UserUpdateDTO {
    private Long id;
    private String password;
    private Integer status;
}
