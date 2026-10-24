import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';

// 免 Token 认证的白名单接口列表
const WHITE_LIST = ['/user/login', '/user/register'];

// 创建 Axios 实例
const service = axios.create({
  baseURL: '/api', // Vite proxy 会代理到 http://localhost:8080 并重写去掉 /api
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
});

// 处理登录态失效清空与跳转逻辑
const handleUnauthorized = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('userInfo');
  localStorage.removeItem('permissions');
  localStorage.removeItem('roleCodes');

  const currentRoute = router.currentRoute.value;
  const currentPath = currentRoute ? currentRoute.fullPath : '/';

  // 避免在登录页时重复重定向
  if (currentRoute && currentRoute.path === '/login') {
    return;
  }

  ElMessage.error('登录已失效，请重新登录');
  router.push(`/login?redirect=${encodeURIComponent(currentPath)}`);
};

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 Token
    const token = localStorage.getItem('token');

    // 判断当前请求是否在免认证白名单中
    const isWhitelisted = WHITE_LIST.some((url) => config.url && config.url.includes(url));

    // 非白名单请求，自动携带 Bearer Token
    if (token && !isWhitelisted) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data;

    // 重点兼容：如果后端响应没有 R 包装（例如 GET /user/get/{username} 直接返回 SysUser 裸对象）
    // 则 res 是对象但不含 code 属性，此时直接原样返回数据
    if (res !== null && typeof res === 'object' && !('code' in res)) {
      return res;
    }

    // 统一 R 包装响应体处理：{ code: number, msg: string, data: any }
    if (res && typeof res.code === 'number') {
      // 业务状态码 200 表示成功
      if (res.code === 200) {
        return res.data;
      }

      // 401: Token 无效/过期/被 Redis 拉黑
      if (res.code === 401) {
        handleUnauthorized();
        return Promise.reject(new Error(res.msg || '登录已失效，请重新登录'));
      }

      // 403: 无权限执行该操作（提示用户，但不进行页面重定向）
      if (res.code === 403) {
        ElMessage.error(res.msg || '无权限执行该操作');
        return Promise.reject(new Error(res.msg || '无权限执行该操作'));
      }

      // 其他业务错误
      ElMessage.error(res.msg || '请求失败');
      return Promise.reject(new Error(res.msg || '请求失败'));
    }

    // 非 JSON 或纯字符串等特殊响应兜底
    return res;
  },
  (error) => {
    // HTTP 层状态码错误拦截
    const { response, config } = error;
    const isWhitelisted = config && WHITE_LIST.some((url) => config.url && config.url.includes(url));

    if (response) {
      const status = response.status;
      const errorMsg = response.data?.msg || response.data?.message;

      switch (status) {
        case 401:
          // 如果是登录或注册接口返回 401，不要触发跳转循环，只提示错误
          if (isWhitelisted) {
            ElMessage.error(errorMsg || '用户名或密码错误');
          } else {
            handleUnauthorized();
          }
          break;
        case 403:
          ElMessage.error(errorMsg || '无权限执行该操作');
          break;
        case 404:
          ElMessage.error(errorMsg || '请求的接口资源不存在 (404)');
          break;
        case 500:
          ElMessage.error(errorMsg || '后端服务器内部发生异常 (500)');
          break;
        default:
          ElMessage.error(errorMsg || `网络请求错误 (HTTP ${status})`);
      }
    } else {
      // 无 response，如跨域、服务未启动、连接被拒绝 ECONNREFUSED
      ElMessage.error('网络连接异常，请检查后端服务(http://localhost:8080)是否已启动');
    }

    return Promise.reject(error);
  }
);

export default service;
