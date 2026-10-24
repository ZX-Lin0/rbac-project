# RBAC 权限管理系统前端

基于 **Vue 3 + Vite + Element Plus + Pinia + Axios** 构建的企业级 RBAC 权限管理系统前端界面，严格依据后端 OpenAPI 契约设计。

---

## 一、技术栈与特性

- **核心框架**：Vue 3 (Composition API + `<script setup>`，纯 JavaScript 开发，极简无冗余)
- **工程构建**：Vite 8（极速热重载，开箱即用）
- **UI 组件库**：Element Plus 全量引入 + `@element-plus/icons-vue` 图标库 + 中文语言包配置
- **路由导航**：Vue Router 4（History 模式 + 全局鉴权守卫 + 路由懒加载）
- **状态管理**：Pinia（手写 `localStorage` 双向同步持久化）
- **网络请求**：Axios（全局请求/响应拦截器，无感兼容裸对象响应与统一 `R<T>` 包装响应）
- **权限控制**：
  - 路由级鉴权：未登录强制拦截跳转，无权限路由拦截至 403 页面
  - 菜单级鉴权：根据当前用户具备的权限编码动态过滤侧边栏菜单（无权限不渲染）
  - 按钮级鉴权：全局自定义指令 `v-permission`，无权限直接从 DOM 树物理移除元素

---

## 二、快速开始

### 1. 安装依赖

确保已安装 Node.js (推荐 v18+ 或 v20+)：

```bash
npm install
```

### 2. 启动本地开发服务

```bash
npm run dev
```

本地服务默认运行在 `http://localhost:3000`。
Vite 将自动把前端 `/api` 开头的请求反向代理到后端 `http://localhost:8080`，并自动去掉 `/api` 前缀。

### 3. 项目打包构建

```bash
npm run build
```

打包产物将输出在 `dist` 目录中。

---

## 三、系统默认账号与初始化指引

由于后端系统刚部署时数据库可能是空的：

1. **注册新账号**：
   - 可以在前端系统直接点击登录页下方的 **「立即注册新用户」** 链接；
   - 也可以通过后端的 Swagger UI（`http://localhost:8080/swagger-ui.html`）调用 `POST /user/register` 接口完成注册；
   - 默认注册用户的状态为 `status = 1`（正常）。
2. **初始角色与权限配置**：
   - 首次登录后，可以在「角色管理」中添加角色（如 `ADMIN` 超级管理员），并在「分配权限」中为该角色关联权限；
   - 然后在「用户管理」中将该角色赋予指定用户。

---

## 四、后端接口契约关键处理说明

1. **裸对象兼容**：
   - 后端 `GET /user/get/{username}` 接口直接返回 `SysUser` 对象（未包装 `{ code: 200, data: ... }` 统一响应体）。
   - 前端在 `src/utils/request.js` 响应拦截器中做了特殊判断：当返回数据为对象且不包含 `code` 属性时，直接原样返回数据，避免被当作异常阻断。
2. **角色修改接口（PUT + Query 参数）**：
   - 后端 `/role/update` 契约定义为 `PUT` 方法，但参数在 Query 中（`dto` / 平铺传参），而非 Request Body。
   - 前端在 `src/api/role.js` 中采用 `request({ url: '/role/update', method: 'put', data: null, params: { id, roleName, description } })` 方式提交。
3. **权限码别名兼容**：
   - 后端接口在部分注解中将 `user:update` 误写为 `user:upadate`，前端在 `src/stores/auth.js` 的 `hasPermission` 检查中已内置双向别名兼容，确保权限判定无缝运行。

---

## 五、目录结构

```
rbac-frontend/
├── index.html               # 入口 HTML
├── package.json             # 依赖与脚本
├── vite.config.js           # Vite 配置与 /api 代理
├── README.md                # 架构与契约说明
└── src/
    ├── main.js              # 应用入口（Element Plus、Pinia、Router、指令全局注册）
    ├── App.vue              # 根组件（Element Plus 中文语言包注入）
    ├── styles/
    │   └── index.css        # 全局样式（布局重置、表格卡片、工具栏）
    ├── utils/
    │   ├── request.js       # Axios 实例、Token 自动携带、401/403/500/裸响应统一拦截
    │   └── constants.js     # 用户状态枚举、权限码常量、默认分页参数
    ├── api/
    │   ├── user.js          # 用户登录、注册、CRUD、分配角色
    │   ├── role.js          # 角色 CRUD、分配权限 (PUT query 处理)
    │   └── permission.js    # 权限列表查询 (要求 role:list 权限)
    ├── stores/
    │   ├── auth.js          # Token、用户信息、权限列表持久化与鉴权逻辑
    │   └── app.js           # 侧边栏折叠状态
    ├── router/
    │   └── index.js         # 路由表、全局前置守卫（登录拦截、403权限拦截）
    ├── directives/
    │   └── permission.js    # v-permission 按钮级权限自定义指令
    ├── layout/
    │   ├── index.vue        # 后台主布局（顶栏、侧边栏折叠、退出登录）
    │   └── Sidebar.vue      # 动态权限过滤侧边导航
    └── views/
        ├── login/index.vue       # 登录页
        ├── register/index.vue    # 注册页
        ├── user/index.vue        # 用户管理（增删改、分配角色、状态切换）
        ├── role/index.vue        # 角色管理（增删改、分配权限）
        ├── permission/index.vue  # 权限管理（只读表格）
        └── error/
            ├── 403.vue           # 403 无权限错误页
            └── 404.vue           # 404 页面未找到错误页
```
