SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_role(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    role_code VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_permission(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_name VARCHAR(50) NOT NULL,
    permission_code VARCHAR(50) UNIQUE NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user_role(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role(user_id, role_id)
);

CREATE TABLE IF NOT EXISTS sys_role_permission(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_permission(role_id, permission_id)
);

INSERT INTO sys_user (id, username, password, status) VALUES 
(1, 'admin', '$2a$10$3gTv36vLkIRzmTyUFffAOuIok2aO1L2.kAi4QBeRz4MuOeodWqdLa', 1),
(2, 'operator', '$2a$10$3gTv36vLkIRzmTyUFffAOuIok2aO1L2.kAi4QBeRz4MuOeodWqdLa', 1);

INSERT INTO sys_role (id, role_name, role_code, description) VALUES 
(1, '管理员', 'ADMIN', '拥有系统所有权限'),
(2, '运营', 'OPERATOR', '仅拥有查看权限');

INSERT INTO sys_permission (id, permission_name, permission_code) VALUES 
(1, '用户列表', 'user:list'),
(2, '用户新增', 'user:add'),
(3, '用户修改', 'user:update'),
(4, '用户删除', 'user:delete'),
(5, '分配角色', 'user:assign'),
(6, '角色列表', 'role:list'),
(7, '新增角色', 'role:add'),
(8, '修改角色', 'role:update'),
(9, '删除角色', 'role:delete'),
(10, '分配权限', 'role:assign');

INSERT INTO sys_user_role (user_id, role_id) VALUES 
(1, 1), 
(2, 2);

INSERT INTO sys_role_permission (role_id, permission_id) VALUES 
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
(2, 1);
