<template>
  <div class="app-container">
    <div class="table-card">
      <!-- 页面头部标题与介绍 -->
      <div class="card-header-bar">
        <div class="header-title-group">
          <div class="header-icon-box">
            <el-icon :size="18"><User /></el-icon>
          </div>
          <div>
            <h3 class="card-main-title">用户管理</h3>
            <p class="card-sub-title">管理系统登录用户账号、账号启用状态及关联角色分配</p>
          </div>
        </div>

        <div class="header-actions">
          <!-- 新增用户按钮（微浮动动效与渐变） -->
          <el-button
            v-permission="'user:add'"
            type="primary"
            :icon="Plus"
            class="glow-primary-btn"
            @click="openAddDialog"
          >
            新增用户
          </el-button>
        </div>
      </div>

      <!-- 搜索筛选栏 -->
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="按用户名快速搜索..."
            prefix-icon="Search"
            clearable
            class="filter-search-input"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
          <el-button type="primary" :icon="Search" class="btn-hover-lift" @click="handleSearch">
            搜索
          </el-button>
          <el-button :icon="Refresh" class="btn-hover-lift" @click="fetchUserList">
            刷新
          </el-button>
        </div>

        <div class="toolbar-right">
          <span class="table-summary-tag">
            共找到 <strong>{{ filteredList.length }}</strong> 位用户
          </span>
        </div>
      </div>

      <!-- 用户数据表格（无纵向生硬框线，纯横向分割与柔和悬停色） -->
      <el-table
        v-loading="loading"
        :data="pagedData"
        row-key="id"
        style="width: 100%"
        empty-text="暂无匹配的用户数据"
        class="tech-modern-table"
      >
        <el-table-column prop="id" label="ID" width="90" align="center">
          <template #default="{ row }">
            <span class="cell-id-badge">#{{ row.id }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="username" label="用户名" min-width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="30" class="table-avatar">
                {{ row.username ? row.username.slice(0, 1).toUpperCase() : 'U' }}
              </el-avatar>
              <span class="user-cell-name">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="账号状态" width="130" align="center">
          <template #default="{ row }">
            <!-- 状态圆角圆点胶囊标签 -->
            <span
              class="status-pill"
              :class="row.status === USER_STATUS.NORMAL ? 'status-pill-success' : 'status-pill-danger'"
            >
              <span class="status-dot"></span>
              <span>{{ row.status === USER_STATUS.NORMAL ? '正常运行' : '已禁用' }}</span>
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" min-width="190">
          <template #default="{ row }">
            <span class="time-text">{{ formatDateTime(row.createTime) }}</span>
          </template>
        </el-table-column>

        <!-- 操作列（精美胶囊 Link 按钮） -->
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="{ row }">
            <div class="table-operations">
              <!-- 编辑用户按钮 -->
              <el-button
                v-permission="'user:update'"
                type="primary"
                link
                :icon="Edit"
                class="op-btn op-edit"
                @click="openEditDialog(row)"
              >
                编辑
              </el-button>

              <!-- 分配角色按钮（仅当拥有 role:list 权限且拥有 user:assign 时展示；无 role:list 权限直接隐藏） -->
              <el-button
                v-if="hasPermission('role:list')"
                v-permission="'user:assign'"
                type="warning"
                link
                :icon="UserFilled"
                class="op-btn op-assign"
                @click="openAssignRoleDialog(row)"
              >
                分配角色
              </el-button>

              <!-- 删除按钮 -->
              <el-button
                v-permission="'user:delete'"
                type="danger"
                link
                :icon="Delete"
                class="op-btn op-delete"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </div>
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

    <!-- 1. 新增用户弹窗 -->
    <el-dialog
      v-model="addDialog.visible"
      title="新增系统用户"
      width="540px"
      :close-on-click-modal="false"
      destroy-on-close
      class="tech-dialog"
    >
      <el-form
        ref="addFormRef"
        :model="addDialog.form"
        :rules="addRules"
        label-width="95px"
        size="default"
        class="dialog-form"
      >
        <el-form-item label="登录账号" prop="username">
          <el-input
            v-model="addDialog.form.username"
            placeholder="请输入登录用户名 (2-30位)"
            clearable
          />
        </el-form-item>

        <el-form-item label="初始密码" prop="password">
          <el-input
            v-model="addDialog.form.password"
            type="password"
            placeholder="请输入初始登录密码（不少于 6 位）"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="初始状态" prop="status">
          <el-switch
            v-model="addDialog.form.status"
            :active-value="USER_STATUS.NORMAL"
            :inactive-value="USER_STATUS.DISABLED"
            active-text="正常可用"
            inactive-text="禁用"
          />
        </el-form-item>

        <el-form-item v-if="hasPermission('role:list')" label="分配角色" prop="roleIds">
          <el-select
            v-model="addDialog.form.roleIds"
            multiple
            :loading="roleLoading"
            placeholder="请选择赋予该用户的权限角色"
            style="width: 100%"
            collapse-tags
            collapse-tags-tooltip
          >
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="`${role.roleName} (${role.roleCode})`"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addDialog.visible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="addDialog.submitting"
          class="dialog-confirm-btn"
          @click="submitAdd"
        >
          确定新增
        </el-button>
      </template>
    </el-dialog>

    <!-- 2. 编辑用户弹窗（仅允许修改密码和状态） -->
    <el-dialog
      v-model="editDialog.visible"
      title="修改用户资料"
      width="540px"
      :close-on-click-modal="false"
      destroy-on-close
      class="tech-dialog"
    >
      <el-form
        ref="editFormRef"
        :model="editDialog.form"
        :rules="editRules"
        label-width="95px"
        size="default"
        class="dialog-form"
      >
        <el-form-item label="用户名称">
          <el-input :value="editDialog.username" disabled />
        </el-form-item>

        <el-form-item label="重置密码" prop="password">
          <el-input
            v-model="editDialog.form.password"
            type="password"
            placeholder="留空表示不修改密码"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="账号状态" prop="status">
          <el-switch
            v-model="editDialog.form.status"
            :active-value="USER_STATUS.NORMAL"
            :inactive-value="USER_STATUS.DISABLED"
            active-text="正常可用"
            inactive-text="禁用状态"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialog.visible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="editDialog.submitting"
          class="dialog-confirm-btn"
          @click="submitEdit"
        >
          确定保存修改
        </el-button>
      </template>
    </el-dialog>

    <!-- 3. 分配角色弹窗（覆盖式选择） -->
    <el-dialog
      v-model="assignRoleDialog.visible"
      :title="`为用户「${assignRoleDialog.username}」分配角色`"
      width="540px"
      :close-on-click-modal="false"
      destroy-on-close
      class="tech-dialog"
    >
      <el-alert
        title="当前后端接口未返回用户历史角色，提交后将覆盖原有关联"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <el-form label-width="95px" size="default" class="dialog-form" v-loading="roleLoading">
        <el-form-item label="选择角色">
          <el-select
            v-model="assignRoleDialog.roleIds"
            multiple
            :loading="roleLoading"
            placeholder="请勾选赋予该用户的角色"
            style="width: 100%"
          >
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="`${role.roleName} (${role.roleCode})`"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="assignRoleDialog.visible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="assignRoleDialog.submitting"
          class="dialog-confirm-btn"
          @click="submitAssignRole"
        >
          保存角色配置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Refresh, Plus, Edit, Delete, UserFilled, User } from '@element-plus/icons-vue';
import userApi from '@/api/user';
import roleApi from '@/api/role';
import { useAuthStore } from '@/stores/auth';
import { USER_STATUS, DEFAULT_PAGE_SIZE, PAGE_SIZES } from '@/utils/constants';

const authStore = useAuthStore();
const hasPermission = (code) => authStore.hasPermission(code);

// 表格数据与搜索
const userList = ref([]);
const roleOptions = ref([]);
const loading = ref(false);
const roleLoading = ref(false);
const searchKeyword = ref('');

// 客户端分页状态
const pagination = reactive({
  currentPage: 1,
  pageSize: DEFAULT_PAGE_SIZE
});

// 前端关键字过滤
const filteredList = computed(() => {
  if (!searchKeyword.value.trim()) {
    return userList.value;
  }
  const keyword = searchKeyword.value.trim().toLowerCase();
  return userList.value.filter((user) => {
    return user.username && user.username.toLowerCase().includes(keyword);
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

// 获取全量用户列表
const fetchUserList = async () => {
  loading.value = true;
  try {
    const data = await userApi.list();
    userList.value = Array.isArray(data) ? data : [];
  } catch (error) {
    // 错误在 request 拦截器中提示
  } finally {
    loading.value = false;
  }
};

// 获取全量角色列表作为选择项
const fetchRoles = async () => {
  try {
    const data = await roleApi.list();
    roleOptions.value = Array.isArray(data) ? data : [];
  } catch (error) {
    roleOptions.value = [];
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

// ================= 1. 新增用户 =================
const addDialog = reactive({
  visible: false,
  submitting: false,
  form: {
    username: '',
    password: '',
    status: USER_STATUS.NORMAL,
    roleIds: []
  }
});
const addFormRef = ref(null);

const addRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于 6 位', trigger: 'blur' }
  ]
};

const openAddDialog = async () => {
  addDialog.form = {
    username: '',
    password: '',
    status: USER_STATUS.NORMAL,
    roleIds: []
  };
  addDialog.visible = true;

  // 仅在具备 role:list 权限且尚未获取过角色时，在打开新增弹窗时按需请求
  if (hasPermission('role:list') && roleOptions.value.length === 0) {
    roleLoading.value = true;
    try {
      await fetchRoles();
    } finally {
      roleLoading.value = false;
    }
  }
};

const submitAdd = async () => {
  if (!addFormRef.value) return;

  await addFormRef.value.validate(async (valid) => {
    if (!valid) return;

    addDialog.submitting = true;
    try {
      await userApi.add({
        username: addDialog.form.username.trim(),
        password: addDialog.form.password,
        status: addDialog.form.status,
        roleIds: addDialog.form.roleIds
      });

      ElMessage.success('新增用户成功');
      addDialog.visible = false;
      await fetchUserList();
    } catch (error) {
      // 提交失败不关闭弹窗
    } finally {
      addDialog.submitting = false;
    }
  });
};

// ================= 2. 编辑用户 =================
const editDialog = reactive({
  visible: false,
  submitting: false,
  id: null,
  username: '',
  form: {
    password: '',
    status: USER_STATUS.NORMAL
  }
});
const editFormRef = ref(null);

const editRules = {
  password: [
    {
      validator: (rule, value, callback) => {
        if (value && value.length < 6) {
          callback(new Error('密码不少于 6 位'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

const openEditDialog = (row) => {
  editDialog.id = row.id;
  editDialog.username = row.username;
  editDialog.form = {
    password: '',
    status: typeof row.status === 'number' ? row.status : USER_STATUS.NORMAL
  };
  editDialog.visible = true;
};

const submitEdit = async () => {
  if (!editFormRef.value) return;

  await editFormRef.value.validate(async (valid) => {
    if (!valid) return;

    editDialog.submitting = true;
    try {
      const payload = {
        id: editDialog.id,
        status: editDialog.form.status
      };
      if (editDialog.form.password) {
        payload.password = editDialog.form.password;
      }

      await userApi.update(payload);

      ElMessage.success('修改用户成功');
      editDialog.visible = false;
      await fetchUserList();
    } catch (error) {
      // 提交失败不关闭弹窗
    } finally {
      editDialog.submitting = false;
    }
  });
};

// ================= 3. 分配角色 =================
const assignRoleDialog = reactive({
  visible: false,
  submitting: false,
  userId: null,
  username: '',
  roleIds: []
});

const openAssignRoleDialog = async (row) => {
  assignRoleDialog.userId = row.id;
  assignRoleDialog.username = row.username;
  assignRoleDialog.roleIds = [];
  assignRoleDialog.visible = true;

  // 严格遵循：只有在点击“分配角色”打开弹窗的一刻，再去请求 /role/list 加载数据
  if (hasPermission('role:list')) {
    roleLoading.value = true;
    try {
      await fetchRoles();
    } finally {
      roleLoading.value = false;
    }
  }
};

const submitAssignRole = async () => {
  assignRoleDialog.submitting = true;
  try {
    await userApi.assignRole({
      userId: assignRoleDialog.userId,
      roleIds: assignRoleDialog.roleIds
    });

    ElMessage.success('分配角色成功');
    assignRoleDialog.visible = false;
    await fetchUserList();
  } catch (error) {
    // 提交失败不关闭弹窗
  } finally {
    assignRoleDialog.submitting = false;
  }
};

// ================= 4. 删除用户 =================
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要彻底删除用户「${row.username}」(ID: ${row.id}) 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        await userApi.delete(row.id);
        ElMessage.success('删除成功');
        await fetchUserList();
      } catch (error) {
        // request.js 处理
      }
    })
    .catch(() => {});
};

onMounted(() => {
  // 只请求用户列表，绝对不在此处请求 /role/list
  fetchUserList();
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

.glow-primary-btn {
  box-shadow: 0 4px 14px rgba(79, 70, 229, 0.35) !important;
}

.glow-primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(79, 70, 229, 0.45) !important;
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

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-avatar {
  background: linear-gradient(135deg, #818cf8 0%, #4f46e5 100%);
  color: #ffffff;
  font-weight: 600;
  font-size: 13px;
}

.user-cell-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}

.time-text {
  font-size: 13.5px;
  color: #64748b;
}

/* 按钮微动效与圆润胶囊效果 */
.op-btn {
  padding: 6px 10px !important;
  border-radius: 6px !important;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.op-btn:hover {
  transform: translateY(-1px);
}

.dialog-confirm-btn {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%) !important;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3) !important;
}

.dialog-confirm-btn:hover {
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.45) !important;
}
</style>
