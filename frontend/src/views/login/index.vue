<template>
  <div class="login-container">
    <!-- 动态科技光晕球体装饰 -->
    <div class="ambient-orb orb-1"></div>
    <div class="ambient-orb orb-2"></div>
    <div class="ambient-orb orb-3"></div>

    <div class="login-card-wrapper">
      <div class="login-box">
        <!-- 头部系统品牌标题 -->
        <div class="login-header">
          <div class="login-logo-badge">
            <span class="logo-shield">🛡️</span>
            <span class="logo-pulse"></span>
          </div>
          <h2 class="login-title">RBAC 权限管理系统</h2>
          <p class="login-subtitle">企业级角色访问控制与权限调度中心</p>
        </div>

        <!-- 登录表单 -->
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入登录用户名"
              :prefix-icon="User"
              clearable
              class="tech-input"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码（不少于 6 位）"
              :prefix-icon="Lock"
              show-password
              clearable
              class="tech-input"
            />
          </el-form-item>

          <el-button
            type="primary"
            :loading="loading"
            class="submit-btn tech-btn-glow"
            @click="handleLogin"
          >
            {{ loading ? '验证中...' : '立即登录' }}
          </el-button>
        </el-form>

        <!-- 底部跳转链接 -->
        <div class="login-footer">
          <span class="tip-text">还没有系统账号？</span>
          <router-link to="/register" class="register-link">立即注册新用户</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const formRef = ref(null);
const loading = ref(false);

const form = reactive({
  username: '',
  password: ''
});

const rules = {
  username: [
    { required: true, message: '请输入登录用户名', trigger: 'blur' },
    { min: 2, max: 30, message: '用户名长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ]
};

const handleLogin = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid) => {
    if (!valid) return;

    loading.value = true;
    try {
      await authStore.login({
        username: form.username.trim(),
        password: form.password
      });

      ElMessage.success('登录成功');
      let redirect = route.query.redirect;
      if (!redirect) {
        if (authStore.hasPermission('user:list')) {
          redirect = '/user';
        } else if (authStore.hasPermission('role:list')) {
          redirect = '/role';
        } else {
          redirect = '/';
        }
      }
      router.push(redirect);
    } catch (error) {
      // 错误提示已在 request.js 响应拦截器中统一处理
    } finally {
      loading.value = false;
    }
  });
};
</script>

<style scoped>
/* 动态流动渐变背景 */
.login-container {
  position: relative;
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  background: linear-gradient(-45deg, #090d16, #111827, #1e1b4b, #0f172a);
  background-size: 400% 400%;
  animation: gradientFlow 15s ease infinite;
  padding: 24px;
}

@keyframes gradientFlow {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* 科技氛围光晕球 */
.ambient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.45;
  pointer-events: none;
  animation: floatOrb 20s infinite alternate ease-in-out;
}

.orb-1 {
  width: 480px;
  height: 480px;
  background: radial-gradient(circle, #6366f1 0%, rgba(99, 102, 241, 0) 70%);
  top: -100px;
  left: -80px;
}

.orb-2 {
  width: 420px;
  height: 420px;
  background: radial-gradient(circle, #3b82f6 0%, rgba(59, 130, 246, 0) 70%);
  bottom: -60px;
  right: -60px;
  animation-delay: -7s;
}

.orb-3 {
  width: 320px;
  height: 320px;
  background: radial-gradient(circle, #a855f7 0%, rgba(168, 85, 247, 0) 70%);
  top: 40%;
  left: 65%;
  animation-delay: -12s;
}

@keyframes floatOrb {
  0% {
    transform: translate(0, 0) scale(1);
  }
  100% {
    transform: translate(50px, 40px) scale(1.15);
  }
}

/* 毛玻璃高级质感卡片 */
.login-card-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 450px;
}

.login-box {
  width: 100%;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.35), 0 0 0 1px rgba(255, 255, 255, 0.2);
  padding: 44px 38px 36px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.login-box:hover {
  box-shadow: 0 30px 60px -12px rgba(0, 0, 0, 0.42), 0 0 0 1px rgba(255, 255, 255, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.login-logo-badge {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  border-radius: 18px;
  box-shadow: 0 8px 16px rgba(79, 70, 229, 0.15);
  margin-bottom: 14px;
}

.logo-shield {
  font-size: 32px;
  line-height: 1;
}

.logo-pulse {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 18px;
  border: 2px solid #818cf8;
  animation: logoPulse 2.5s infinite ease-out;
}

@keyframes logoPulse {
  0% {
    transform: scale(0.95);
    opacity: 0.8;
  }
  100% {
    transform: scale(1.3);
    opacity: 0;
  }
}

.login-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.3px;
  color: #0f172a;
}

.login-subtitle {
  margin: 8px 0 0;
  font-size: 13.5px;
  color: #64748b;
  letter-spacing: 0.2px;
}

.login-form {
  margin-top: 14px;
}

/* 输入框微发光焦点设计 */
:deep(.tech-input .el-input__wrapper) {
  border-radius: 10px;
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  transition: all 0.25s ease;
  padding: 4px 14px;
}

:deep(.tech-input .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #a5b4fc inset;
}

:deep(.tech-input .el-input__wrapper.is-focus) {
  background-color: #ffffff;
  box-shadow: 0 0 0 2px #6366f1 inset, 0 0 16px rgba(99, 102, 241, 0.22) !important;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #334155;
  font-size: 13.5px;
  margin-bottom: 6px;
}

/* 呼吸发光主按钮 */
.submit-btn {
  width: 100%;
  height: 46px;
  font-size: 15.5px;
  font-weight: 600;
  letter-spacing: 1px;
  margin-top: 10px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%) !important;
  color: #ffffff;
  box-shadow: 0 8px 20px -2px rgba(79, 70, 229, 0.42);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.submit-btn:hover {
  transform: translateY(-2px) scale(1.015);
  box-shadow: 0 12px 26px -2px rgba(79, 70, 229, 0.55);
}

.submit-btn:active {
  transform: translateY(0) scale(1);
}

.login-footer {
  margin-top: 26px;
  text-align: center;
  font-size: 13.5px;
  color: #64748b;
}

.tip-text {
  margin-right: 6px;
}

.register-link {
  color: #4f46e5;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}

.register-link:hover {
  color: #4338ca;
  text-decoration: underline;
}
</style>
