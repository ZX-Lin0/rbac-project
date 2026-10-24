<template>
  <div class="app-container">
    <div class="table-card">
      <!-- 页面头部标题与介绍 -->
      <div class="card-header-bar">
        <div class="header-title-group">
          <div class="header-icon-box">
            <el-icon :size="18"><Key /></el-icon>
          </div>
          <div>
            <h3 class="card-main-title">权限管理</h3>
            <p class="card-sub-title">系统底层受保护的功能接口与操作权限定义列表（系统内置只读）</p>
          </div>
        </div>

        <div class="header-actions">
          <div class="system-badge-pill">
            <span class="badge-dot"></span>
            <span>系统底层权限资产（只读）</span>
          </div>
        </div>
      </div>

      <!-- 搜索筛选栏 -->
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索权限名称或权限编码..."
            prefix-icon="Search"
            clearable
            class="filter-search-input"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
          <el-button type="primary" :icon="Search" class="btn-hover-lift" @click="handleSearch">
            搜索
          </el-button>
          <el-button :icon="Refresh" class="btn-hover-lift" @click="fetchPermissionList">
            刷新
          </el-button>
        </div>

        <div class="toolbar-right">
          <span class="table-summary-tag">
            共计 <strong>{{ filteredList.length }}</strong> 项系统权限
          </span>
        </div>
      </div>

      <!-- 权限数据表格（无增删改按钮，纯只读） -->
      <el-table
        v-loading="loading"
        :data="pagedData"
        row-key="id"
        style="width: 100%"
        empty-text="暂无匹配的权限数据"
        class="tech-modern-table"
      >
        <el-table-column prop="id" label="ID" width="90" align="center">
          <template #default="{ row }">
            <span class="cell-id-badge">#{{ row.id }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="permissionName" label="权限名称" min-width="170">
          <template #default="{ row }">
            <div class="perm-cell">
              <span class="perm-name-text">{{ row.permissionName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="permissionCode" label="权限编码标识" min-width="200">
          <template #default="{ row }">
            <el-tag effect="light" class="perm-code-tag">
              {{ row.permissionCode }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="注册时间" min-width="190">
          <template #default="{ row }">
            <span class="time-text">{{ formatDateTime(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="保护状态" width="140" align="center">
          <template #default>
            <span class="protected-pill">
              <span class="lock-icon">🔒</span>
              <span>内置受护</span>
            </span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 客户端分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="PAGE_SIZES"
          :total="filteredList.length"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { Search, Refresh, Key } from '@element-plus/icons-vue';
import permissionApi from '@/api/permission';
import { DEFAULT_PAGE_SIZE, PAGE_SIZES } from '@/utils/constants';

// 表格数据与状态
const permissionList = ref([]);
const loading = ref(false);
const searchKeyword = ref('');

// 客户端分页状态
const pagination = reactive({
  currentPage: 1,
  pageSize: DEFAULT_PAGE_SIZE
});

// 前端关键字过滤
const filteredList = computed(() => {
  if (!searchKeyword.value.trim()) {
    return permissionList.value;
  }
  const keyword = searchKeyword.value.trim().toLowerCase();
  return permissionList.value.filter((perm) => {
    const matchName = perm.permissionName && perm.permissionName.toLowerCase().includes(keyword);
    const matchCode = perm.permissionCode && perm.permissionCode.toLowerCase().includes(keyword);
    return matchName || matchCode;
  });
});

// 当前页切片展示数据
const pagedData = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize;
  const end = start + pagination.pageSize;
  return filteredList.value.slice(start, end);
});

// 格式化时间字符串
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-';
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return dateStr;
  const pad = (n) => String(n).padStart(2, '0');
  const YYYY = d.getFullYear();
  const MM = pad(d.getMonth() + 1);
  const DD = pad(d.getDate());
  const hh = pad(d.getHours());
  const mm = pad(d.getMinutes());
  const ss = pad(d.getSeconds());
  return `${YYYY}-${MM}-${DD} ${hh}:${mm}:${ss}`;
};

// 获取权限列表
// ⚠️ 注意后端契约：GET /permission/list 要求具备 role:list 权限
const fetchPermissionList = async () => {
  loading.value = true;
  try {
    const data = await permissionApi.list();
    permissionList.value = Array.isArray(data) ? data : [];
  } catch (error) {
    // 错误在 request 拦截器中提示
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.currentPage = 1;
};

const handleSizeChange = (val) => {
  pagination.pageSize = val;
  pagination.currentPage = 1;
};

const handleCurrentChange = (val) => {
  pagination.currentPage = val;
};

onMounted(() => {
  fetchPermissionList();
});
</script>

<style scoped>
/* 页面标题栏 */
.card-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  margin-bottom: 18px;
  border-bottom: 1px solid #f1f5f9;
}

.header-title-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  color: #4f46e5;
  box-shadow: 0 4px 10px rgba(79, 70, 229, 0.12);
}

.card-main-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.2px;
}

.card-sub-title {
  margin: 4px 0 0;
  font-size: 13px;
  color: #64748b;
}

.system-badge-pill {
  display: flex;
  align-items: center;
  gap: 6px;
  background-color: #f1f5f9;
  border: 1px solid #e2e8f0;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12.5px;
  color: #475569;
  font-weight: 500;
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #6366f1;
}

.filter-search-input {
  width: 260px;
}

:deep(.filter-search-input .el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  background-color: #fafbfc;
  transition: all 0.2s;
}

:deep(.filter-search-input .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #cbd5e1 inset;
}

:deep(.filter-search-input .el-input__wrapper.is-focus) {
  background-color: #ffffff;
  box-shadow: 0 0 0 2px #6366f1 inset !important;
}

.btn-hover-lift {
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-hover-lift:hover {
  transform: translateY(-2px);
}

.table-summary-tag {
  font-size: 13px;
  color: #64748b;
  background-color: #f8fafc;
  padding: 6px 12px;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
}

.cell-id-badge {
  font-family: monospace;
  font-weight: 600;
  color: #64748b;
  background-color: #f1f5f9;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 12px;
}

.perm-name-text {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}

.perm-code-tag {
  font-family: monospace;
  font-weight: 600;
  background-color: #fef3c7;
  color: #d97706;
  border-color: #fde68a;
  letter-spacing: 0.3px;
}

.time-text {
  font-size: 13.5px;
  color: #64748b;
}

.protected-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  color: #64748b;
}

.lock-icon {
  font-size: 11px;
}
</style>
