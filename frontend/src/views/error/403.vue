<template>
  <div class="error-page-container">
    <div class="error-card">
      <div class="error-badge">
        <span class="error-code">403</span>
      </div>
      <div class="error-icon-box">
        <span class="error-icon">🚫</span>
      </div>
      <h2 class="error-title">抱歉，你没有访问该页面的权限</h2>
      <p class="error-desc">
        当前登录账号未分配该模块的访问权限。如需访问，请联系系统超级管理员为你分配相应系统角色。
      </p>
      <div class="error-actions">
        <el-button type="primary" size="large" class="glow-primary-btn" @click="goHome">
          返回控制台首页
        </el-button>
        <el-button size="large" class="btn-hover-lift" @click="handleLogout">
          重新登录账号
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const goHome = () => {
  router.push('/');
};

const handleLogout = () => {
  authStore.logout();
};
</script>

<style scoped>
.error-page-container {
  width: 100%;
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.error-card {
  text-align: center;
  max-width: 520px;
  background-color: #ffffff;
  border-radius: 18px;
  padding: 48px 40px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 10px 30px -5px rgba(15, 23, 42, 0.08);
}

.error-badge {
  display: inline-block;
  background: #fef2f2;
  border: 1px solid #fecaca;
  padding: 4px 18px;
  border-radius: 20px;
  margin-bottom: 16px;
}

.error-code {
  font-size: 28px;
  font-weight: 800;
  color: #ef4444;
  letter-spacing: 2px;
  font-family: monospace;
}

.error-icon-box {
  margin: 10px 0 16px;
}

.error-icon {
  font-size: 56px;
}

.error-title {
  margin: 0 0 12px;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.error-desc {
  margin: 0 0 28px;
  font-size: 13.5px;
  color: #64748b;
  line-height: 1.6;
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.glow-primary-btn {
  box-shadow: 0 4px 14px rgba(79, 70, 229, 0.35) !important;
}

.glow-primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(79, 70, 229, 0.45) !important;
}

.btn-hover-lift:hover {
  transform: translateY(-2px);
}
</style>
