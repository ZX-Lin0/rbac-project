package com.example.rbacdemo.ai.controller;


import com.example.rbacdemo.ai.dto.AiChatResponse;
import com.example.rbacdemo.ai.dto.ChatRequestDTO;
import com.example.rbacdemo.common.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.rbacdemo.ai.service.AiService;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/chat")
    public R<AiChatResponse> chat (@RequestBody ChatRequestDTO request){
        AiChatResponse response = aiService.chat(request);
        return R.ok(response);
    }
}
