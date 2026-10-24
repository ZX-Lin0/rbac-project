import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import userApi from '@/api/user';
import router from '@/router';

export const useAuthStore = defineStore('auth', () => {
  // 从 localStorage 恢复持久化状态
  const token = ref(localStorage.getItem('token') || '');

  let parsedUserInfo = null;
  try {
    const raw = localStorage.getItem('userInfo');
    parsedUserInfo = raw ? JSON.parse(raw) : null;
  } catch (e) {
    parsedUserInfo = null;
  }
  const userInfo = ref(parsedUserInfo);

  let parsedPermissions = [];
  try {
    const raw = localStorage.getItem('permissions');
    parsedPermissions = raw ? JSON.parse(raw) : [];
  } catch (e) {
    parsedPermissions = [];
  }
  const permissions = ref(parsedPermissions);

  let parsedRoleCodes = [];
  try {
    const raw = localStorage.getItem('roleCodes');
    parsedRoleCodes = raw ? JSON.parse(raw) : [];
  } catch (e) {
    parsedRoleCodes = [];
  }
  const roleCodes = ref(parsedRoleCodes);

  // 记录当前会话是否已经请求过 /user/info，确保只请求一次
  const isInfoLoaded = ref(false);
  let fetchInfoPromise = null;

  // Getter: 是否已登录
  const isLogin = computed(() => !!token.value);

  /**
   * 检查是否具备某个或某组权限
   * 严格根据后端 /user/info 返回的 permissions 判定
   * 只有拥有相应权限才返回 true
   * @param {string|string[]} code 权限标识（如 'user:list' 或 ['user:list', 'role:list']）
   * @returns {boolean}
   */
  const hasPermission = (code) => {
    if (!permissions.value || permissions.value.length === 0) {
      return false;
    }

    const matchSingle = (p) => {
      if (permissions.value.includes(p)) return true;
      // 兼容后端历史可能存在的 typo user:upadate
      if (p === 'user:update' && permissions.value.includes('user:upadate')) return true;
      if (p === 'user:upadate' && permissions.value.includes('user:update')) return true;
      return false;
    };

    if (Array.isArray(code)) {
      return code.some((item) => matchSingle(item));
    }
    return matchSingle(code);
  };

  /**
   * 获取当前登录用户信息及权限
   * 契约：GET /user/info
   * 返回格式：{ code: 200, data: { username: string, permissions: string[] } }
   * 控制并发与只请求一次，结果存入全局状态与 localStorage
   */
  const fetchUserInfo = async () => {
    if (isInfoLoaded.value) {
      return {
        userInfo: userInfo.value,
        permissions: permissions.value
      };
    }

    if (fetchInfoPromise) {
      return fetchInfoPromise;
    }

    fetchInfoPromise = (async () => {
      try {
        const data = await userApi.getUserInfo();
        if (data) {
          // 1. 同步用户名与用户信息
          const currentUsername = data.username || userInfo.value?.username || '';
          userInfo.value = {
            ...(userInfo.value || {}),
            username: currentUsername
          };
          localStorage.setItem('userInfo', JSON.stringify(userInfo.value));

          // 2. 存入权限数组
          const perms = Array.isArray(data.permissions) ? data.permissions : [];
          permissions.value = perms;
          localStorage.setItem('permissions', JSON.stringify(perms));
        }
        isInfoLoaded.value = true;
        return data;
      } catch (error) {
        console.error('调用 /user/info 获取权限信息失败:', error);
        // 请求失败（如无网络连接）时也标记已加载，防止死循环请求风暴
        isInfoLoaded.value = true;
        throw error;
      } finally {
        fetchInfoPromise = null;
      }
    })();

    return fetchInfoPromise;
  };

  /**
   * 登录 Action
   * @param {Object} form { username, password }
   */
  const login = async (form) => {
    const resData = await userApi.login(form);

    // 1. 保存 Token
    const receivedToken = resData?.token || (typeof resData === 'string' ? resData : '');
    token.value = receivedToken;
    localStorage.setItem('token', receivedToken);

    // 2. 初始化基本用户信息
    userInfo.value = { username: form.username };
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value));

    // 3. 重置加载状态，并立即触发 /user/info
    isInfoLoaded.value = false;
    try {
      await fetchUserInfo();
    } catch (e) {
      // 允许进入系统后由 Layout 或路由守卫继续获取
    }

    return resData;
  };

  /**
   * 退出登录 Action
   */
  const logout = () => {
    token.value = '';
    userInfo.value = null;
    permissions.value = [];
    roleCodes.value = [];
    isInfoLoaded.value = false;
    fetchInfoPromise = null;

    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    localStorage.removeItem('permissions');
    localStorage.removeItem('roleCodes');

    router.push('/login');
  };

  return {
    token,
    userInfo,
    permissions,
    roleCodes,
    isLogin,
    isInfoLoaded,
    hasPermission,
    fetchUserInfo,
    login,
    logout
  };
});

export default useAuthStore;
