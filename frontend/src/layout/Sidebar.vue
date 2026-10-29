<template>
  <div class="sidebar-wrapper" :class="{ 'is-collapsed': appStore.sidebarCollapsed }">
    <!-- 侧边栏顶部品牌 Logo 栏 -->
    <div class="sidebar-logo">
      <div class="logo-badge">
        <span class="logo-icon">🛡️</span>
      </div>
      <div v-if="!appStore.sidebarCollapsed" class="logo-meta">
        <span class="logo-title">RBAC 管理系统</span>
        <span class="logo-tag">Console v1.0</span>
      </div>
    </div>

    <!-- 侧边导航菜单 -->
    <el-scrollbar class="sidebar-scrollbar">
      <el-menu
        :default-active="activeMenu"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        background-color="transparent"
        text-color="#94a3b8"
        active-text-color="#ffffff"
        router
        class="sidebar-menu"
      >
        <template v-for="item in visibleMenuItems" :key="item.path">
          <el-menu-item :index="item.path" class="tech-menu-item">
            <el-icon class="menu-icon">
              <component :is="item.icon" />
            </el-icon>
            <template #title>
              <span class="menu-label">{{ item.title }}</span>
            </template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-scrollbar>

    <!-- 侧边栏底部安全状态微章 -->
    <div v-if="!appStore.sidebarCollapsed" class="sidebar-footer">
      <div class="status-indicator">
        <span class="indicator-dot"></span>
        <span class="indicator-text">系统鉴权运行中</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useAppStore } from '@/stores/app';
import { User, Avatar, Key, ChatDotRound } from '@element-plus/icons-vue';

const route = useRoute();
const authStore = useAuthStore();
const appStore = useAppStore();

// 菜单配置清单
const menuConfigs = [
  {
    path: '/user',
    title: '用户管理',
    icon: User,
    permission: 'user:list'
  },
  {
    path: '/role',
    title: '角色管理',
    icon: Avatar,
    permission: 'role:list'
  },
  {
    path: '/permission',
    title: '权限管理',
    icon: Key,
    permission: 'role:list' // 后端契约注明权限为 role:list
  },
  {
    path: '/ai-chat',
    title: 'AI 助手',
    icon: ChatDotRound
  }
];

// 根据用户权限过滤可见菜单（无权限直接不渲染，不显示禁用状态）
const visibleMenuItems = computed(() => {
  return menuConfigs.filter((item) => {
    if (!item.permission) return true;
    return authStore.hasPermission(item.permission);
  });
});

// 当前激活的菜单项
const activeMenu = computed(() => {
  return route.path;
});
</script>

<style scoped>
.sidebar-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  width: 230px;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.25);
  position: relative;
  z-index: 20;
}

.sidebar-wrapper.is-collapsed {
  width: 68px;
}

.sidebar-logo {
  height: 68px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  background: rgba(15, 23, 42, 0.6);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  overflow: hidden;
  white-space: nowrap;
}

.logo-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2) 0%, rgba(79, 70, 229, 0.4) 100%);
  border: 1px solid rgba(129, 140, 248, 0.3);
  border-radius: 10px;
  box-shadow: 0 0 15px rgba(99, 102, 241, 0.3);
  flex-shrink: 0;
}

.logo-icon {
  font-size: 20px;
}

.logo-meta {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.logo-title {
  font-size: 15px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 0.3px;
  line-height: 1.2;
}

.logo-tag {
  font-size: 10.5px;
  color: #818cf8;
  font-family: monospace;
  font-weight: 600;
}

.sidebar-scrollbar {
  flex: 1;
  padding: 12px 0;
}

.sidebar-menu {
  border-right: none;
  padding: 0 8px;
}

.tech-menu-item {
  height: 48px;
  line-height: 48px;
  border-radius: 10px;
  margin-bottom: 6px;
  transition: all 0.22s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.tech-menu-item .menu-icon {
  font-size: 18px;
  margin-right: 12px;
  transition: transform 0.2s ease, color 0.2s ease;
}

.tech-menu-item .menu-label {
  font-size: 14px;
  font-weight: 500;
}

.tech-menu-item:hover {
  background: rgba(255, 255, 255, 0.06) !important;
  color: #f1f5f9 !important;
}

.tech-menu-item:hover .menu-icon {
  transform: translateX(2px);
  color: #818cf8;
}

/* 激活菜单项科技感高亮边框与背景 */
.tech-menu-item.is-active {
  background: linear-gradient(90deg, rgba(79, 70, 229, 0.25) 0%, rgba(99, 102, 241, 0.08) 100%) !important;
  color: #ffffff !important;
  font-weight: 600;
  box-shadow: inset 3px 0 0 #6366f1, 0 4px 12px rgba(79, 70, 229, 0.2);
}

.tech-menu-item.is-active .menu-icon {
  color: #818cf8 !important;
  filter: drop-shadow(0 0 6px rgba(129, 140, 248, 0.6));
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(15, 23, 42, 0.4);
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #94a3b8;
}

.indicator-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background-color: #10b981;
  box-shadow: 0 0 8px #10b981;
  animation: pulseDot 2s infinite;
}

@keyframes pulseDot {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.85);
  }
}
</style>
