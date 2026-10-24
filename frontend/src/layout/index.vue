<template>
  <el-container class="layout-container">
    <!-- 左侧菜单栏 -->
    <el-aside :width="appStore.sidebarCollapsed ? '68px' : '230px'" class="layout-aside">
      <Sidebar />
    </el-aside>

    <!-- 右侧主体内容区域 -->
    <el-container class="main-container">
      <!-- 顶部浮动导航栏 -->
      <el-header height="64px" class="layout-header">
        <div class="header-left">
          <!-- 侧边栏折叠/展开切换按钮 -->
          <el-button
            link
            class="collapse-btn"
            @click="appStore.toggleSidebar"
            :title="appStore.sidebarCollapsed ? '展开菜单' : '折叠菜单'"
          >
            <el-icon :size="20">
              <component :is="appStore.sidebarCollapsed ? Expand : Fold" />
            </el-icon>
          </el-button>

          <!-- 面包屑与当前页标题 -->
          <div class="page-meta-box">
            <span class="page-category">控制台</span>
            <span class="page-divider">/</span>
            <span class="page-title">{{ route.meta?.title || '管理控制台' }}</span>
          </div>
        </div>

        <div class="header-right">
          <!-- 环境标识胶囊 -->
          <div class="env-pill">
            <span class="env-dot"></span>
            <span>API Proxy: 8080</span>
          </div>

          <!-- 用户名与退出下拉菜单 -->
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-profile">
              <div class="avatar-wrapper">
                <el-avatar :size="34" class="user-avatar">
                  {{ usernameInitial }}
                </el-avatar>
              </div>
              <div class="user-info-text">
                <span class="username">{{ currentUsername }}</span>
                <span class="user-role-label">当前管理员</span>
              </div>
              <el-icon class="dropdown-icon">
                <ArrowDown />
              </el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown-menu">
                <el-dropdown-item disabled>
                  <div class="dropdown-user-header">
                    <strong>{{ currentUsername }}</strong>
                    <span class="dropdown-role-tag">已认证登录</span>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout" class="logout-item">
                  <el-icon><SwitchButton /></el-icon>
                  <span>安全退出系统</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 核心页面展示区域 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessageBox } from 'element-plus';
import { Fold, Expand, ArrowDown, SwitchButton } from '@element-plus/icons-vue';
import Sidebar from './Sidebar.vue';
import { useAuthStore } from '@/stores/auth';
import { useAppStore } from '@/stores/app';

const route = useRoute();
const authStore = useAuthStore();
const appStore = useAppStore();

// 进入主页面时，确保只请求一次 /user/info，存入 Pinia 与 localStorage
onMounted(async () => {
  if (authStore.isLogin && !authStore.isInfoLoaded) {
    try {
      await authStore.fetchUserInfo();
    } catch (error) {
      // 错误由 request 拦截器统一处理
    }
  }
});

// 当前用户名展示
const currentUsername = computed(() => {
  return authStore.userInfo?.username || '管理员';
});

// 用户头像首字母
const usernameInitial = computed(() => {
  const name = currentUsername.value;
  return name ? name.slice(0, 1).toUpperCase() : 'U';
});

// 下拉菜单操作
const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出当前管理员账号吗？', '系统退出提示', {
      confirmButtonText: '确定退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        authStore.logout();
      })
      .catch(() => {});
  }
};
</script>

<style scoped>
.layout-container {
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background-color: var(--el-bg-color-page, #f1f5f9);
}

.layout-aside {
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  z-index: 20;
}

.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--el-bg-color-page, #f1f5f9);
}

/* 顶栏悬浮轻奢感设计 */
.layout-header {
  background-color: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 4px 20px -4px rgba(15, 23, 42, 0.06);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.collapse-btn {
  color: #475569;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s;
}

.collapse-btn:hover {
  color: #4f46e5;
  background-color: #eef2ff;
}

.page-meta-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-category {
  font-size: 13px;
  color: #94a3b8;
  font-weight: 500;
}

.page-divider {
  font-size: 13px;
  color: #cbd5e1;
}

.page-title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
  letter-spacing: -0.2px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.env-pill {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  color: #64748b;
  font-family: monospace;
}

.env-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #10b981;
}

/* 用户信息交互与头像微动效 */
.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 10px;
  transition: all 0.25s ease;
  border: 1px solid transparent;
}

.user-profile:hover {
  background-color: #f8fafc;
  border-color: #e2e8f0;
}

.avatar-wrapper {
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.user-profile:hover .avatar-wrapper {
  transform: scale(1.08) rotate(4deg);
}

.user-avatar {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  color: #ffffff;
  font-weight: 700;
  font-size: 14px;
  box-shadow: 0 4px 10px rgba(79, 70, 229, 0.3);
}

.user-info-text {
  display: flex;
  flex-direction: column;
  text-align: left;
}

.username {
  font-size: 13.5px;
  color: #1e293b;
  font-weight: 600;
  line-height: 1.2;
}

.user-role-label {
  font-size: 11px;
  color: #94a3b8;
}

.dropdown-icon {
  font-size: 12px;
  color: #94a3b8;
  transition: transform 0.2s;
}

.user-profile:hover .dropdown-icon {
  color: #4f46e5;
}

.dropdown-user-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 4px 0;
}

.dropdown-role-tag {
  font-size: 11px;
  color: #10b981;
}

.logout-item:hover {
  color: #ef4444 !important;
  background-color: #fef2f2 !important;
}

.layout-main {
  background-color: var(--el-bg-color-page, #f1f5f9);
  padding: 0;
  overflow-y: auto;
  flex: 1;
}

/* 优雅平滑页面切换动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(12px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-12px);
}
</style>
