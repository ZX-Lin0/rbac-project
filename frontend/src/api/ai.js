import request from '@/utils/request';

/**
 * AI 助手接口定义
 */
export const aiApi = {
  /**
   * 发送自然语言对话，请求生成 SQL 及查询数据
   * 接口契约：POST /api/ai/chat
   * 请求体：{ "message": "用户输入的话" }
   * 响应体：
   * {
   *   "code": 200,
   *   "data": {
   *     "sql": "生成的SQL语句",
   *     "data": [ { "role_name": "运营" } ],
   *     "message": "查询成功"
   *   }
   * }
   * @param {string} message 用户输入的文本
   * @returns {Promise<Object>}
   */
  chat(message) {
    return request({
      url: '/ai/chat',
      method: 'post',
      data: { message }
    });
  }
};

export default aiApi;
