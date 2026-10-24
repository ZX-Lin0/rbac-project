import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';

import App from './App.vue';
import router from './router';
import permissionDirective from './directives/permission';
import './styles/index.css';

const app = createApp(App);

// 注册 Pinia 状态管理
const pinia = createPinia();
app.use(pinia);

// 注册 Vue Router
app.use(router);

// 全量引入 Element Plus
app.use(ElementPlus);

// 全局注册 Element Plus 所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

// 注册按钮权限自定义指令 v-permission
app.use(permissionDirective);

// 挂载应用
app.mount('#app');
