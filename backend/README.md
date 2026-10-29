# RBAC 权限管理系统后端 (Backend)

基于 Spring Boot 4 + MyBatis-Plus + JWT + Redis + Spring AI 构建的 RBAC 权限控制系统后端。

## 技术栈

- **核心框架**：Spring Boot 4.0.8、Spring MVC
- **持久层**：MyBatis-Plus 3.5.13、MySQL 26.7
- **缓存**：Redis
- **安全认证**：JWT（jjwt 0.12.6）、BCrypt（Spring Security Crypto）
- **权限控制**：Spring AOP + 自定义注解
- **AI 能力**：Spring AI 2.0.2、DeepSeek 大模型
- **接口文档**：Springdoc OpenAPI + Swagger UI
- **工具**：Lombok、Maven

## 功能清单

### 认证与授权
- 用户注册、登录（BCrypt 密码加密）
- JWT Token 签发与校验
- 基于拦截器的登录校验（401）
- 基于自定义注解 `@RequirePermission` + AOP 的接口级权限校验（403）
- 基于 Redis 黑名单的 Token 主动失效（禁用账号即时踢下线）

### 用户、角色与权限管理
- 完整的 CRUD 接口
- 分配角色、分配权限（含批量插入优化）
- 逻辑删除防误删（解决用户名唯一约束冲突）

### AI 智能查询 (Text-to-SQL)
- 接入 DeepSeek 大模型，将用户自然语言转换为 SQL
- 结合 JdbcTemplate 动态执行 SQL 并返回结构化 JSON
- **安全机制**：正则校验拦截非 SELECT 语句；强制追加 `LIMIT 100` 防全表扫描

### 基础设施
- 统一响应体 `R`（code / msg / data）
- 全局异常处理（精准区分 401 / 403 / 500）
- Redis 权限缓存 + 延迟双删保一致性
- Swagger 在线接口文档

## 项目结构

```text
src/main/java/com/example/rbacdemo/
├── ai/                # AI 业务包
│   ├── controller/    # AI 接口控制层
│   ├── dto/           # AI 请求/响应对象
│   └── service/       # AI 核心业务（Prompt 组装、SQL 安全校验、JdbcTemplate 执行）
├── annotation/        # 自定义注解（@RequirePermission）
├── aspect/            # AOP 切面（权限校验）
├── common/            # 工具类（R、JwtUtil、SecurityUtil、RedisUtil、UserContext）
├── config/            # 配置类（WebMvcConfig、SwaggerConfig）
├── controller/        # 基础业务控制层
├── dto/               # 数据传输对象
├── entity/            # 数据库实体
├── exception/         # 自定义异常
├── interceptor/       # 拦截器（JWT 校验）
├── mapper/            # MyBatis-Plus Mapper
└── service/           # 业务层

```

## 快速启动
### 1. 准备环境
请确保本地已启动 MySQL 和 Redis，并创建数据库：

```sql
CREATE DATABASE rbac_demo DEFAULT CHARACTER SET utf8mb4;
```
### 2. 配置环境变量
在 IDEA 运行配置中配置以下环境变量：

```text
MYSQL_PASSWORD=your_database_password_here
AI_API_KEY=your_api_key_here
```
### 3. 启动项目
运行 RbacDemoApplication，访问 Swagger：
http://localhost:8080/swagger-ui/index.html

## 核心设计
### 认证与授权流程
请求进入 JwtInterceptor，校验 Token 合法性及 Redis 黑名单，解析 userId 存入 ThreadLocal。

PermissionAspect 切面拦截 @RequirePermission 注解，从 Redis/数据库查询用户权限码进行比对。

### AI Text-to-SQL 核心流程
Prompt 组装：将 5 张核心表结构（Schema）与系统预设规则（如“只返回纯 SQL”）拼接注入大模型。

安全拦截：清理可能存在的 Markdown 标记，校验 SQL 必须以 SELECT 开头。

防全表扫描：若未包含 LIMIT，代码自动追加 LIMIT 100。

动态执行：利用 JdbcTemplate.queryForList() 执行动态 SQL，将查询结果封装为 List<Map<String, Object>> 返回。

### 遇到的问题与解决
1. Spring Boot 4 兼容性：AOP Starter 更名为 spring-boot-starter-aspectj；MyBatis-Plus 需使用 mybatis-plus-spring-boot4-starter。

2. Spring AI 版本冲突：2.0 版本起，OpenAI Starter 更改为 spring-ai-starter-model-openai，且需配合 Spring Boot 4 使用。

3. JWT Bearer 前缀：拦截器主动剥离 Bearer 前缀后再解析，防止 Swagger 或前端传参引发异常。

4. 延迟双删：在用户角色/权限更新后，先删缓存，更新数据库后通过异步线程延迟 500ms 再次删除缓存，保证并发场景下的缓存一致性。

## 后续优化方向
引入 Refresh Token

引入 JSqlParser 进行 AST 级别的 SQL 安全校验

增加 AI 查询的流式输出 (SSE)