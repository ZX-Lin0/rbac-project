import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useAppStore = defineStore('app', () => {
  // 侧边栏折叠状态
  const sidebarCollapsed = ref(false);

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value;
  };

  const setSidebar = (collapsed) => {
    sidebarCollapsed.value = collapsed;
  };

  return {
    sidebarCollapsed,
    toggleSidebar,
    setSidebar
  };
});

export default useAppStore;
