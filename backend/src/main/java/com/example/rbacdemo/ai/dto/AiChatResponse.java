package com.example.rbacdemo.ai.dto;


import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AiChatResponse {

    private String sql;
    private List<Map<String,Object>>data;
    private String message;

}
