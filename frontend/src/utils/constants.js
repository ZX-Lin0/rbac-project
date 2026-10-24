/**
 * 常量定义模块
 * 包括用户状态枚举、权限编码常量映射
 */

// 用户状态枚举约定：1 = 正常，0 = 禁用
export const USER_STATUS = {
  NORMAL: 1,
  DISABLED: 0
};

// 用户状态显示配置
export const USER_STATUS_OPTIONS = [
  { label: '正常', value: USER_STATUS.NORMAL, tagType: 'success' },
  { label: '禁用', value: USER_STATUS.DISABLED, tagType: 'danger' }
];

export const USER_STATUS_MAP = {
  [USER_STATUS.NORMAL]: { label: '正常', tagType: 'success' },
  [USER_STATUS.DISABLED]: { label: '禁用', tagType: 'danger' }
};

// 权限码常量清单（与后端接口权限对应）
export const PERMISSIONS = {
  // 用户管理
  USER_LIST: 'user:list',
  USER_ADD: 'user:add',
  USER_UPDATE: 'user:update', // 注意：后端 Swagger 描述中部分可能写作 user:upadate，我们在鉴权时做别名兼容
  USER_UPDATE_ALIAS: 'user:upadate',
  USER_DELETE: 'user:delete',
  USER_ASSIGN: 'user:assign',

  // 角色管理
  ROLE_LIST: 'role:list',
  ROLE_ADD: 'role:add',
  ROLE_UPDATE: 'role:update',
  ROLE_DELETE: 'role:delete',
  ROLE_ASSIGN: 'role:assign',

  // 权限管理列表（后端接口注明此处权限仍为 role:list）
  PERMISSION_LIST: 'role:list'
};

// 默认分页配置
export const DEFAULT_PAGE_SIZE = 10;
export const PAGE_SIZES = [10, 20, 50, 100];
