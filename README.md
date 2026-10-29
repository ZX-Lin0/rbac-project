# RBAC 全栈权限管理系统

基于 Spring Boot 4 + Vue 3 + Docker 开发的现代化企业级 RBAC 权限管理系统。支持动态路由、按钮级权限控制、JWT 无状态认证及 Docker Compose 一键部署。

## 技术栈

### 后端 (Backend)
- **核心框架**：Spring Boot 4.0.8、Spring MVC
- **持久层**：MyBatis-Plus 3.5.13、MySQL 26.7
- **缓存**：Redis 
- **安全认证**：JWT (jjwt 0.12.6)、Spring Security Crypto (BCrypt)
- **权限控制**：Spring AOP + 自定义注解
- **接口文档**：Springdoc OpenAPI + Swagger UI

### 前端 (Frontend)
- **核心框架**：Vue 3 (Composition API) + Vite
- **UI 组件库**：Element Plus
- **网络请求**：Axios
- **状态管理/路由**：Vue Router、Pinia (或 LocalStorage)

### 部署运维 (DevOps)
- **容器化**：Docker、Docker Compose
- **Web 服务器**：Nginx (静态资源托管 + 反向代理)
- **系统环境**：Ubuntu 24.04 (阿里云 ECS)

## 核心功能

- **认证与授权**：JWT 无状态登录，结合 Redis 黑名单实现 Token 主动失效。
- **细粒度权限**：基于自定义注解 `@RequirePermission` 与 AOP 实现接口级校验，前端同步实现动态菜单与按钮隐藏。
- **并发与缓存**：Redis 缓存用户权限，减少高频四表联查；采用延迟双删策略保证缓存一致性。
- **数据安全**：BCrypt 密码加盐哈希、MyBatis-Plus 逻辑删除防止误删。
- **容器化部署**：前后端、MySQL、Redis 完整 Docker 编排，一键启动。

## 项目结构

```text
rbac-project/
├── backend/            # Spring Boot 后端源码
├── frontend/           # Vue 3 前端源码
├── deploy/             # 部署配置目录
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── docker-compose.yml
│   ├── init.sql
│   └── .env.example    # 环境变量模板
└── README.md

```

## 快速启动 (本地开发)
### 1. 后端启动
导入 backend 模块至 IDEA。

配置本地 MySQL 和 Redis。

配置环境变量 MYSQL_PWD (或修改 application.yaml)。

运行 RbacDemoApplication，访问 Swagger: http://localhost:8080/swagger-ui/index.html

### 2. 前端启动

进入 frontend 目录，执行 npm install。

执行 npm run dev，访问 http://localhost:5173。

## 生产环境一键部署 (Docker)
在服务器上安装 Docker 与 Docker Compose。

进入 deploy 目录，复制 .env.example 为 .env 并填入真实密码。

确保后端打包产物 xxx.jar 和前端打包产物 dist 已放置在对应位置。

执行一键启动：

```bash
docker compose up -d --build
```

## 后续优化
引入 Refresh Token 机制
权限缓存精确失效（按角色反向查用户）
基于 AOP 的全局操作日志记录
