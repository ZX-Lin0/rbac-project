package com.example.rbacdemo.ai.service;

import com.example.rbacdemo.ai.dto.AiChatResponse;
import com.example.rbacdemo.ai.dto.ChatRequestDTO;
public interface AiService {
    AiChatResponse chat(ChatRequestDTO dto);
}
