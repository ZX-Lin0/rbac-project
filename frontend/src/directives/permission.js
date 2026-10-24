import { useAuthStore } from '@/stores/auth';

/**
 * 按钮级权限指令 v-permission
 * 用法：
 *   v-permission="'user:add'"
 *   v-permission="['user:add', 'user:update']"
 */
export const permissionDirective = {
  mounted(el, binding) {
    checkPermission(el, binding);
  },
  updated(el, binding) {
    checkPermission(el, binding);
  }
};

function checkPermission(el, binding) {
  const { value } = binding;
  const authStore = useAuthStore();

  if (value) {
    const hasPerm = authStore.hasPermission(value);
    if (!hasPerm) {
      // 无权限时直接从 DOM 树移除当前节点，防止通过检查元素绕过
      if (el.parentNode) {
        el.parentNode.removeChild(el);
      } else {
        el.style.display = 'none';
      }
    }
  } else {
    throw new Error("使用 v-permission 指令必须传入权限标识！例如 v-permission=\"'user:add'\"");
  }
}

export default {
  install(app) {
    app.directive('permission', permissionDirective);
  }
};
