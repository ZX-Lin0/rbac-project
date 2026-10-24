import request from '@/utils/request';

/**
 * 角色相关接口定义
 */
export const roleApi = {
  /**
   * 获取所有角色列表
   * 需权限：role:list
   * @returns {Promise<Array>} 返回全量 SysRole[] 列表
   */
  list() {
    return request({
      url: '/role/list',
      method: 'get'
    });
  },

  /**
   * 增加角色
   * 需权限：role:add
   * @param {Object} data { roleName, roleCode, description }
   * @returns {Promise<string>}
   */
  add(data) {
    return request({
      url: '/role/add',
      method: 'post',
      data
    });
  },

  /**
   * 修改角色
   * 需权限：role:update
   * ⚠️ 关键契约注意：/role/update 是 PUT + query 参数传递，不是 requestBody！
   * Spring Boot 后端控制器如果以普通对象或 @ModelAttribute 接收 DTO，
   * 请求参数需平铺在 URL query (params) 中，因此第二个参数为 null，第三个配置项传 params。
   * @param {Object} data { id, roleName, description }
   * @returns {Promise<string>}
   */
  update(data) {
    return request({
      url: '/role/update',
      method: 'put',
      data: null,
      params: {
        id: data.id,
        roleName: data.roleName,
        description: data.description
      }
    });
  },

  /**
   * 删除角色
   * 需权限：role:delete
   * @param {number|string} id 角色ID
   * @returns {Promise<string>}
   */
  delete(id) {
    return request({
      url: `/role/delete/${id}`,
      method: 'delete'
    });
  },

  /**
   * 为角色分配权限（覆盖式提交）
   * 需权限：role:assign
   * @param {Object} data { roleCode: string, roleId: number, permissionIds: number[] }
   * @returns {Promise<string>}
   */
  assignPermission(data) {
    return request({
      url: '/role/assign',
      method: 'post',
      data
    });
  }
};

export default roleApi;
