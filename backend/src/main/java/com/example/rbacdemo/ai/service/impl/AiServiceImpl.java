package com.example.rbacdemo.ai.service.impl;


import com.example.rbacdemo.ai.dto.AiChatResponse;
import com.example.rbacdemo.ai.service.AiService;
import com.example.rbacdemo.ai.dto.ChatRequestDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService{

    private final ChatClient chatClient;

    public AiServiceImpl(ChatClient.Builder chatClientBuilder){
        this.chatClient = chatClientBuilder.build();
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public AiChatResponse chat(ChatRequestDTO request){
        AiChatResponse response = new AiChatResponse();

        String schema = """
                已知数据库表结构如下：
                1. 用户表 sys_user (id, username, password, status, create_time, is_deleted)
                2. 角色表 sys_role (id, role_name, role_code, description, create_time)
                3. 用户角色关联表 sys_user_role (id, user_id, role_id)
                4. 权限表 sys_permission (id, permission_name, permission_code, create_time)
                5. 角色权限关联表 sys_role_permission (id, role_id, permission_id)
                """;
        String systemPrompt = """
                你是一个资深的 MySQL 数据库专家。
                请根据用户的需求，结合我提供的表结构，生成对应的 SQL 查询语句。
                
                严格遵守以下规则：
                1. 只能生成 SELECT 查询语句，绝对不能生成 INSERT、UPDATE、DELETE 或 DROP 等修改数据的语句。
                2. 如果用户的问题无法转换为 SQL，请直接回答“无法理解您的查询需求”。
                3. 只返回纯 SQL 代码，不要包含 Markdown 格式（不要用 ```sql 包裹），不要包含任何多余的解释文字。
                """;
        String sql= chatClient.prompt()
                .system(systemPrompt+"\n"+schema)
                .user(request.getMessage())
                .call()
                .content();
        String cleanSql = sql.replaceAll("(?i)```sql","").replace("```","").trim();
         if(!cleanSql.toUpperCase().startsWith("SELECT")){
             response.setMessage("系统安全拦截：只能执行SELECT查询");
             return response;
         }

         if(!cleanSql.toUpperCase().contains("LIMIT")){
             cleanSql =cleanSql.replace(";","")+" LIMIT 100";
         }
        response.setSql(cleanSql);
         try{
             List<Map<String,Object>> result = jdbcTemplate.queryForList(cleanSql);

             if(result.isEmpty()){
                 response.setMessage("查询为空");
                 response.setData(null);
                 return response;
             }

             response.setData(result);
             response.setMessage("查询成功");
             return response;
         }catch (Exception e){
             response.setMessage("sql执行失败："+e.getMessage());
             return response;
         }
    }
}
