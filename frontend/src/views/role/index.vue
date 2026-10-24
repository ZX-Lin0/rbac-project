<template>
  <div class="app-container">
    <div class="table-card">
      <!-- 页面头部标题与介绍 -->
      <div class="card-header-bar">
        <div class="header-title-group">
          <div class="header-icon-box">
            <el-icon :size="18"><Avatar /></el-icon>
          </div>
          <div>
            <h3 class="card-main-title">角色管理</h3>
            <p class="card-sub-title">配置系统访问角色、管理角色权限绑定及授权策略</p>
          </div>
        </div>

        <div class="header-actions">
          <!-- 新增角色按钮（受按钮级权限 role:add 控制） -->
          <el-button
            v-permission="'role:add'"
            type="primary"
            :icon="Plus"
            class="glow-primary-btn"
            @click="openAddDialog"
          >
            新增角色
          </el-button>
        </div>
      </div>

      <!-- 搜索筛选栏 -->
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索角色名称或角色编码..."
            prefix-icon="Search"
            clearable
            class="filter-search-input"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
          <el-button type="primary" :icon="Search" class="btn-hover-lift" @click="handleSearch">
            搜索
          </el-button>
          <el-button :icon="Refresh" class="btn-hover-lift" @click="fetchRoleList">
            刷新
          </el-button>
        </div>

        <div class="toolbar-right">
          <span class="table-summary-tag">
            共配置 <strong>{{ filteredList.length }}</strong> 个角色
          </span>
        </div>
      </div>

      <!-- 角色数据表格 -->
      <el-table
        v-loading="loading"
        :data="pagedData"
        row-key="id"
        style="width: 100%"
        empty-text="暂无匹配的角色数据"
        class="tech-modern-table"
      >
        <el-table-column prop="id" label="ID" width="90" align="center">
          <template #default="{ row }">
            <span class="cell-id-badge">#{{ row.id }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="roleName" label="角色名称" min-width="150">
          <template #default="{ row }">
            <div class="role-cell">
              <span class="role-name-text">{{ row.roleName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="roleCode" label="角色编码" min-width="150">
          <template #default="{ row }">
            <el-tag effect="light" class="role-code-tag">
              {{ row.roleCode }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="职责描述" min-width="200">
          <template #default="{ row }">
            <span class="desc-text">{{ row.description || '暂无描述信息' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" min-width="190">
          <template #default="{ row }">
            <span class="time-text">{{ formatDateTime(row.createTime) }}</span>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="{ row }">
            <div class="table-operations">
              <!-- 编辑角色按钮 -->
              <el-button
                v-permission="'role:update'"
                type="primary"
                link
                :icon="Edit"
                class="op-btn op-edit"
                @click="openEditDialog(row)"
              >
                编辑
              </el-button>

              <!-- 分配权限按钮 -->
              <el-button
                v-permission="'role:assign'"
                type="warning"
                link
                :icon="Key"
                class="op-btn op-assign"
                @click="openAssignPermissionDialog(row)"
              >
                分配权限
              </el-button>

              <!-- 删除按钮 -->
              <el-button
                v-permission="'role:delete'"
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

    <!-- 1. 新增角色弹窗 -->
    <el-dialog
      v-model="addDialog.visible"
      title="新增系统角色"
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
        <el-form-item label="角色名称" prop="roleName">
          <el-input
            v-model="addDialog.form.roleName"
            placeholder="例如：系统运维管理员"
            clearable
          />
        </el-form-item>

        <el-form-item label="角色编码" prop="roleCode">
          <el-input
            v-model="addDialog.form.roleCode"
            placeholder="推荐大写格式，例如：OPS_ADMIN"
            clearable
          />
        </el-form-item>

        <el-form-item label="角色职责" prop="description">
          <el-input
            v-model="addDialog.form.description"
            type="textarea"
            :rows="3"
            placeholder="请详细描述该角色的权限职责与适用人员"
          />
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
          确定创建
        </el-button>
      </template>
    </el-dialog>

    <!-- 2. 编辑角色弹窗（改 roleName + description，注意 PUT query 传参） -->
    <el-dialog
      v-model="editDialog.visible"
      title="修改角色定义"
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
        <el-form-item label="角色编码">
          <el-input :value="editDialog.roleCode" disabled />
        </el-form-item>

        <el-form-item label="角色名称" prop="roleName">
          <el-input
            v-model="editDialog.form.roleName"
            placeholder="请输入修改后的角色名称"
            clearable
          />
        </el-form-item>

        <el-form-item label="角色职责" prop="description">
          <el-input
            v-model="editDialog.form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
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
          保存修改
        </el-button>
      </template>
    </el-dialog>

    <!-- 3. 分配权限弹窗（覆盖式勾选） -->
    <el-dialog
      v-model="assignPermDialog.visible"
      :title="`为角色「${assignPermDialog.roleName}」分配系统权限`"
      width="600px"
      :close-on-click-modal="false"
      destroy-on-close
      class="tech-dialog"
    >
      <el-alert
        title="当前后端接口未返回角色历史权限，提交后将覆盖原有关联"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 18px"
      />

      <div class="permission-selector-wrapper">
        <div class="permission-actions-bar">
          <el-checkbox
            v-model="selectAllPermissions"
            :indeterminate="isIndeterminate"
            @change="handleSelectAllChange"
          >
            <strong>全选所有可用权限</strong>
          </el-checkbox>
          <span class="selected-count-pill">
            已选 <strong>{{ assignPermDialog.permissionIds.length }}</strong> / {{ permissionOptions.length }}
          </span>
        </div>

        <el-scrollbar max-height="340px" class="permissions-scroll">
          <el-checkbox-group
            v-model="assignPermDialog.permissionIds"
            class="permission-checkbox-group"
            @change="handlePermissionChange"
          >
            <div
              v-for="perm in permissionOptions"
              :key="perm.id"
              class="permission-card-item"
              :class="{ 'is-checked': assignPermDialog.permissionIds.includes(perm.id) }"
            >
              <el-checkbox :value="perm.id" class="perm-checkbox">
                <div class="perm-info-box">
                  <span class="perm-title">{{ perm.permissionName }}</span>
                  <span class="perm-code-badge">{{ perm.permissionCode }}</span>
                </div>
              </el-checkbox>
            </div>
          </el-checkbox-group>
        </el-scrollbar>
      </div>

      <template #footer>
        <el-button @click="assignPermDialog.visible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="assignPermDialog.submitting"
          class="dialog-confirm-btn"
          @click="submitAssignPermission"
        >
          保存权限分配
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Refresh, Plus, Edit, Delete, Key, Avatar } from '@element-plus/icons-vue';
import roleApi from '@/api/role';
import permissionApi from '@/api/permission';
import { DEFAULT_PAGE_SIZE, PAGE_SIZES } from '@/utils/constants';

// 表格数据与状态
const roleList = ref([]);
const permissionOptions = ref([]);
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
    return roleList.value;
  }
  const keyword = searchKeyword.value.trim().toLowerCase();
  return roleList.value.filter((role) => {
    const matchName = role.roleName && role.roleName.toLowerCase().includes(keyword);
    const matchCode = role.roleCode && role.roleCode.toLowerCase().includes(keyword);
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

// 获取所有角色列表
const fetchRoleList = async () => {
  loading.value = true;
  try {
    const data = await roleApi.list();
    roleList.value = Array.isArray(data) ? data : [];
  } catch (error) {
    // 错误在 request 拦截器中提示
  } finally {
    loading.value = false;
  }
};

// 获取所有系统权限作为选择项
const fetchPermissions = async () => {
  try {
    const data = await permissionApi.list();
    permissionOptions.value = Array.isArray(data) ? data : [];
  } catch (error) {
    permissionOptions.value = [];
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

// ================= 1. 新增角色 =================
const addDialog = reactive({
  visible: false,
  submitting: false,
  form: {
    roleName: '',
    roleCode: '',
    description: ''
  }
});
const addFormRef = ref(null);

const addRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 30, message: '角色名称在 2 到 30 个字符', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z0-9_]+$/, message: '角色编码格式推荐为大写英文字母与下划线，例如 ADMIN', trigger: 'blur' }
  ]
};

const openAddDialog = () => {
  addDialog.form = {
    roleName: '',
    roleCode: '',
    description: ''
  };
  addDialog.visible = true;
};

const submitAdd = async () => {
  if (!addFormRef.value) return;

  await addFormRef.value.validate(async (valid) => {
    if (!valid) return;

    addDialog.submitting = true;
    try {
      await roleApi.add({
        roleName: addDialog.form.roleName.trim(),
        roleCode: addDialog.form.roleCode.trim(),
        description: addDialog.form.description ? addDialog.form.description.trim() : ''
      });

      ElMessage.success('新增角色成功');
      addDialog.visible = false;
      await fetchRoleList();
    } catch (error) {
      // 提交失败不关闭弹窗
    } finally {
      addDialog.submitting = false;
    }
  });
};

// ================= 2. 编辑角色 =================
const editDialog = reactive({
  visible: false,
  submitting: false,
  id: null,
  roleCode: '',
  form: {
    roleName: '',
    description: ''
  }
});
const editFormRef = ref(null);

const editRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 30, message: '角色名称在 2 到 30 个字符', trigger: 'blur' }
  ]
};

const openEditDialog = (row) => {
  editDialog.id = row.id;
  editDialog.roleCode = row.roleCode;
  editDialog.form = {
    roleName: row.roleName,
    description: row.description || ''
  };
  editDialog.visible = true;
};

const submitEdit = async () => {
  if (!editFormRef.value) return;

  await editFormRef.value.validate(async (valid) => {
    if (!valid) return;

    editDialog.submitting = true;
    try {
      await roleApi.update({
        id: editDialog.id,
        roleName: editDialog.form.roleName.trim(),
        description: editDialog.form.description ? editDialog.form.description.trim() : ''
      });

      ElMessage.success('修改角色成功');
      editDialog.visible = false;
      await fetchRoleList();
    } catch (error) {
      // 提交失败不关闭弹窗
    } finally {
      editDialog.submitting = false;
    }
  });
};

// ================= 3. 分配权限 =================
const assignPermDialog = reactive({
  visible: false,
  submitting: false,
  roleId: null,
  roleName: '',
  roleCode: '',
  permissionIds: []
});

const selectAllPermissions = ref(false);
const isIndeterminate = ref(false);

const openAssignPermissionDialog = (row) => {
  assignPermDialog.roleId = row.id;
  assignPermDialog.roleName = row.roleName;
  assignPermDialog.roleCode = row.roleCode;
  assignPermDialog.permissionIds = [];
  selectAllPermissions.value = false;
  isIndeterminate.value = false;
  assignPermDialog.visible = true;
};

const handleSelectAllChange = (val) => {
  assignPermDialog.permissionIds = val ? permissionOptions.value.map((p) => p.id) : [];
  isIndeterminate.value = false;
};

const handlePermissionChange = (value) => {
  const checkedCount = value.length;
  selectAllPermissions.value = checkedCount === permissionOptions.value.length;
  isIndeterminate.value = checkedCount > 0 && checkedCount < permissionOptions.value.length;
};

const submitAssignPermission = async () => {
  assignPermDialog.submitting = true;
  try {
    await roleApi.assignPermission({
      roleCode: assignPermDialog.roleCode,
      roleId: assignPermDialog.roleId,
      permissionIds: assignPermDialog.permissionIds
    });

    ElMessage.success('分配权限成功');
    assignPermDialog.visible = false;
    await fetchRoleList();
  } catch (error) {
    // 提交失败不关闭弹窗
  } finally {
    assignPermDialog.submitting = false;
  }
};

// ================= 4. 删除角色 =================
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要彻底删除角色「${row.roleName}」(${row.roleCode}) 吗？删除后关联用户的权限可能失效！`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        await roleApi.delete(row.id);
        ElMessage.success('删除成功');
        await fetchRoleList();
      } catch (error) {
        // request.js 处理
      }
    })
    .catch(() => {});
};

onMounted(() => {
  fetchRoleList();
  fetchPermissions();
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

.role-name-text {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}

.role-code-tag {
  font-family: monospace;
  font-weight: 600;
  background-color: #eef2ff;
  color: #4f46e5;
  border-color: #c7d2fe;
}

.desc-text {
  font-size: 13.5px;
  color: #64748b;
}

.time-text {
  font-size: 13.5px;
  color: #64748b;
}

.op-btn {
  padding: 6px 10px !important;
  border-radius: 6px !important;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.op-btn:hover {
  transform: translateY(-1px);
}

/* 权限分配卡片网格样式 */
.permission-selector-wrapper {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  background-color: #fafbfc;
}

.permission-actions-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  margin-bottom: 12px;
  border-bottom: 1px solid #e2e8f0;
}

.selected-count-pill {
  font-size: 12.5px;
  color: #4f46e5;
  background: #eef2ff;
  border: 1px solid #c7d2fe;
  padding: 2px 10px;
  border-radius: 12px;
}

.permission-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.permission-card-item {
  padding: 8px 14px;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.permission-card-item:hover {
  border-color: #a5b4fc;
  background-color: #f8faff;
  transform: translateY(-1px);
}

.permission-card-item.is-checked {
  border-color: #818cf8;
  background: #f5f7ff;
}

.perm-checkbox {
  width: 100%;
}

.perm-info-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.perm-title {
  font-weight: 600;
  color: #1e293b;
  font-size: 13.5px;
}

.perm-code-badge {
  font-family: monospace;
  font-size: 12px;
  color: #64748b;
  background-color: #f1f5f9;
  padding: 2px 8px;
  border-radius: 4px;
}

.dialog-confirm-btn {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%) !important;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3) !important;
}

.dialog-confirm-btn:hover {
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.45) !important;
}
</style>
