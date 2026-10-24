# RBAC 权限管理系统

基于 Spring Boot 4 + MyBatis-Plus + JWT + Redis 的 RBAC 权限管理系统，支持用户、角色、权限的动态管理，接口级权限校验，以及 Token 主动失效。

## 技术栈

- **后端**：Spring Boot 4.0.8、Spring MVC、MyBatis-Plus 3.5.13
- **数据库**：MySQL 8.x
- **缓存**：Redis
- **认证**：JWT（jjwt 0.12.6）
- **密码加密**：BCrypt（Spring Security Crypto）
- **接口文档**：Springdoc OpenAPI + Swagger UI
- **工具**：Lombok、Maven

## 功能清单

### 认证与授权
- 用户注册、登录（BCrypt 密码加密）
- JWT Token 签发与校验
- 基于拦截器的登录校验（401）
- 基于自定义注解 + AOP 的接口级权限校验（403）
- 基于 Redis 黑名单的 Token 主动失效

### 用户管理
- 用户列表查询
- 用户新增（支持同时分配角色）
- 用户修改（密码、状态）
- 用户删除（级联删除角色关联）
- 给用户分配角色

### 角色管理
- 角色列表查询
- 角色新增、修改、删除
- 给角色分配权限（覆盖式）

### 权限管理
- 权限列表查询

### 基础设施
- 统一响应体 `R`（code / msg / data）
- 全局异常处理（区分 401 / 403 / 500）
- Redis 权限缓存（减少四表联查）
- Swagger 在线接口文档

## 项目结构

```

src/main/java/com/example/rbacdemo/
├── annotation/        # 自定义注解（@RequirePermission）
├── aspect/            # AOP 切面（权限校验）
├── common/            # 工具类（R、JwtUtil、SecurityUtil、RedisUtil、UserContext）
├── config/            # 配置类（WebMvcConfig、SwaggerConfig）
├── controller/        # 控制器层
├── dto/               # 数据传输对象
├── entity/            # 数据库实体
├── exception/         # 自定义异常
├── interceptor/       # 拦截器（JWT 校验）
├── mapper/            # MyBatis-Plus Mapper
└── service/           # 业务层
└── impl/              # 业务实现

```

## 快速启动

### 1. 准备数据库
```sql
CREATE DATABASE rbac_demo DEFAULT CHARACTER SET utf8mb4;
-- 执行建表 SQL（见 docs/schema.sql）
```

### 2. 启动 Redis
```bash
redis-server.exe
```

### 3. 修改配置
编辑 `src/main/resources/application.yaml`，替换数据库和 Redis 连接信息。

### 4. 启动项目
运行 `RbacDemoApplication`，访问 Swagger：
```
http://localhost:8080/swagger-ui/index.html
```

## 核心设计

### RBAC 模型
采用标准五表设计：
- `sys_user`：用户
- `sys_role`：角色
- `sys_permission`：权限
- `sys_user_role`：用户-角色关联（多对多）
- `sys_role_permission`：角色-权限关联（多对多）

用户通过角色间接获得权限，权限以 `模块:操作` 格式定义（如 `user:list`）。

### 认证与授权流程
```

请求进入
  ↓
JwtInterceptor 拦截
  ├─ 无 Token → 401
  ├─ Token 无效/过期 → 401
  ├─ Token 在 Redis 黑名单中 → 401（账号被禁用）
  └─ 通过 → 解析 userId 存入 ThreadLocal
  ↓
PermissionAspect 切面
  ├─ 从 ThreadLocal 取 userId
  ├─ 查 Redis 权限缓存（未命中则查数据库并回写）
  ├─ 比对方法上 @RequirePermission 要求的权限码
  └─ 无权限 → 403
  ↓
Controller 执行

```

### 缓存一致性
- 权限缓存 key：`permission:user:{userId}`
- 用户角色变更 → 删除该用户权限缓存
- 角色权限变更 → 清空所有 `permission:user:*`（粗粒度，保证一致性优先）
- 用户禁用/删除 → 写入 `user:disabled:{userId}` 黑名单

## 遇到的问题与解决

### 1. Spring Boot 4 中 AOP Starter 改名
Spring Boot 4 将 `spring-boot-starter-aop` 改名为 `spring-boot-starter-aspectj`，旧依赖会报 `not found`。

### 2. MyBatis-Plus 与 Spring Boot 4 不兼容
旧坐标 `mybatis-plus-boot-starter` 在 Spring Boot 4 下自动配置不生效，需替换为 `mybatis-plus-spring-boot4-starter`。

### 3. JWT Bearer 前缀导致校验失败
Swagger 会自动在 Token 前加 `Bearer `，拦截器需主动剥离前缀后再解析。

### 4. 全局异常处理器区分 401 / 403 / 500
通过自定义 `NoPermissionException` 和 `@ExceptionHandler` 精确匹配，避免所有异常都被当作 500 返回。

## 后续优化方向

- 引入 Refresh Token，避免 Access Token 过期后强制重新登录
- 权限缓存改为精确失效（按角色查用户反向删除）
- 引入操作日志（AOP 记录谁在什么时候做了什么）
- 补充单元测试和集成测试





