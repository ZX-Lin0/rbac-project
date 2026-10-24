import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

/**
 * 路由表定义
 */
export const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true, title: '用户登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { public: true, title: '用户注册' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    children: [
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/user/index.vue'),
        meta: {
          title: '用户管理',
          icon: 'User',
          permission: 'user:list'
        }
      },
      {
        path: 'role',
        name: 'RoleManagement',
        component: () => import('@/views/role/index.vue'),
        meta: {
          title: '角色管理',
          icon: 'Avatar',
          permission: 'role:list'
        }
      },
      {
        path: 'permission',
        name: 'PermissionManagement',
        component: () => import('@/views/permission/index.vue'),
        meta: {
          title: '权限管理',
          icon: 'Key',
          // 后端契约明确注明：权限管理接口需要 role:list 权限
          permission: 'role:list'
        }
      }
    ]
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { public: true, title: '403 无权限' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { public: true, title: '404 页面未找到' }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 全局路由守卫
router.beforeEach(async (to, from, next) => {
  // 1. 设置网页标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - RBAC 权限管理系统`;
  } else {
    document.title = 'RBAC 权限管理系统';
  }

  const authStore = useAuthStore();
  const isLogin = authStore.isLogin;

  // 2. 目标路由为公开页面（如登录、注册）
  if (to.meta.public) {
    if (isLogin && (to.path === '/login' || to.path === '/register')) {
      next('/');
    } else {
      next();
    }
    return;
  }

  // 3. 未登录且访问受保护页面，跳转登录页携带重定向参数
  if (!isLogin) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    });
    return;
  }

  // 4. 已登录：进入主页面时，只请求一次 /user/info，确保 permissions 存入全局状态与 localStorage
  if (!authStore.isInfoLoaded) {
    try {
      await authStore.fetchUserInfo();
    } catch (error) {
      console.error('路由守卫获取用户信息 /user/info 失败:', error);
    }
  }

  // 5. 访问根路径 '/' 时，根据 permissions 动态重定向到用户有权访问的第一个菜单
  if (to.path === '/') {
    if (authStore.hasPermission('user:list')) {
      next('/user');
    } else if (authStore.hasPermission('role:list')) {
      next('/role');
    } else {
      next('/403');
    }
    return;
  }

  // 6. 路由级权限校验
  if (to.meta.permission && !authStore.hasPermission(to.meta.permission)) {
    next('/403');
    return;
  }

  // 7. 校验通过放行
  next();
});

export default router;
