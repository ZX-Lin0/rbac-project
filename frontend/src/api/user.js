import request from '@/utils/request';

/**
 * 用户相关接口定义
 */
export const userApi = {
  /**
   * 用户登录
   * @param {Object} data { username, password }
   * @returns {Promise<Object>} 返回包含 token 及可能的 user/permissions/roleCodes
   */
  login(data) {
    return request({
      url: '/user/login',
      method: 'post',
      data
    });
  },

  /**
   * 用户注册
   * @param {Object} data { username, password, status }
   * @returns {Promise<Object>} 返回新注册的用户信息 SysUser
   */
  register(data) {
    return request({
      url: '/user/register',
      method: 'post',
      data
    });
  },

  /**
   * 获取所有用户列表
   * 需权限：user:list
   * @returns {Promise<Array>} 返回全量 SysUser[] 列表
   */
  list() {
    return request({
      url: '/user/list',
      method: 'get'
    });
  },

  /**
   * 根据用户名获取用户信息
   * 注意：此接口后端直接返回 SysUser 裸对象，未包装统一响应体 R
   * @param {string} username
   * @returns {Promise<Object>} SysUser 对象
   */
  getByUsername(username) {
    return request({
      url: `/user/get/${encodeURIComponent(username)}`,
      method: 'get'
    });
  },

  /**
   * 新增用户
   * 需权限：user:add
   * @param {Object} data { username, password, status, roleIds: number[] }
   * @returns {Promise<string>}
   */
  add(data) {
    return request({
      url: '/user/add',
      method: 'post',
      data
    });
  },

  /**
   * 修改用户（密码和状态）
   * 需权限：user:update
   * @param {Object} data { id, password, status }
   * @returns {Promise<string>}
   */
  update(data) {
    return request({
      url: '/user/update',
      method: 'put',
      data
    });
  },

  /**
   * 删除用户
   * 需权限：user:delete
   * @param {number|string} id 用户ID
   * @returns {Promise<string>}
   */
  delete(id) {
    return request({
      url: `/user/delete/${id}`,
      method: 'delete'
    });
  },

  /**
   * 为用户分配角色（覆盖式提交）
   * 需权限：user:assign
   * @param {Object} data { userId, roleIds: number[] }
   * @returns {Promise<string>}
   */
  assignRole(data) {
    return request({
      url: '/user/assignRole',
      method: 'post',
      data
    });
  },

  /**
   * 获取当前登录用户的基础信息及权限列表
   * 接口契约：GET /user/info
   * 返回格式：{ code: 200, data: { username: string, permissions: string[] } }
   * @returns {Promise<Object>}
   */
  getUserInfo() {
    return request({
      url: '/user/info',
      method: 'get'
    });
  }
};

export default userApi;
