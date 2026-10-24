import request from '@/utils/request';

/**
 * 权限相关接口定义
 */
export const permissionApi = {
  /**
   * 列出所有系统权限
   * 需权限：role:list（特别注意：后端接口注释与拦截切面要求的就是 role:list，而非 permission:list）
   * @returns {Promise<Array>} 返回全量 SysPermission[] 列表
   */
  list() {
    return request({
      url: '/permission/list',
      method: 'get'
    });
  }
};

export default permissionApi;
