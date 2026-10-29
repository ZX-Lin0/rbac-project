<template>
  <div class="ai-chat-container">
    <!-- 顶部状态栏 -->
    <div class="chat-header">
      <div class="header-left">
        <div class="header-icon-box">
          <el-icon class="header-icon"><Cpu /></el-icon>
        </div>
        <div class="header-text">
          <div class="header-title-row">
            <h2 class="header-title">AI 智能 SQL 助手</h2>
            <el-tag size="small" type="success" effect="light" class="header-badge">
              <span class="live-dot"></span> 实时就绪
            </el-tag>
          </div>
          <p class="header-subtitle">输入自然语言，自动分析意图并生成 SQL，动态查询并呈现结果表格</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button
          v-if="messages.length > 0"
          type="danger"
          link
          :icon="Delete"
          @click="handleClearChat"
        >
          清空对话
        </el-button>
      </div>
    </div>

    <!-- 中间对话气泡展示区 -->
    <div ref="chatScrollContainer" class="chat-body">
      <!-- 空状态欢迎界面与快捷问答 -->
      <div v-if="messages.length === 0" class="welcome-box">
        <div class="welcome-avatar">
          <span class="avatar-sparkle">✨</span>
          <el-icon :size="38" color="#6366f1"><DataAnalysis /></el-icon>
        </div>
        <h3 class="welcome-title">智能语义转 SQL 查询</h3>
        <p class="welcome-desc">
          您可以直接用日常中文向 AI 提问。AI 将为您生成精准 SQL 语句，并在左侧以高亮代码块及动态表格直接展示返回结果。
        </p>

        <div class="quick-prompts-title">
          <span>💡 快速尝试以下提问：</span>
        </div>
        <div class="quick-prompts-grid">
          <div
            v-for="(item, idx) in quickPrompts"
            :key="idx"
            class="prompt-card"
            @click="useQuickPrompt(item)"
          >
            <div class="prompt-icon">{{ item.icon }}</div>
            <div class="prompt-info">
              <div class="prompt-text">{{ item.text }}</div>
              <div class="prompt-hint">{{ item.hint }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 消息列表 -->
      <div v-else class="message-list">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-row"
          :class="[msg.role === 'user' ? 'message-user' : 'message-ai']"
        >
          <!-- AI 头像 -->
          <div v-if="msg.role === 'assistant'" class="avatar-wrapper ai-avatar-wrapper">
            <div class="ai-avatar">
              <el-icon :size="18"><Cpu /></el-icon>
            </div>
          </div>

          <!-- 消息气泡主体 -->
          <div class="message-bubble" :class="{ 'user-bubble': msg.role === 'user', 'ai-bubble': msg.role === 'assistant' }">
            <!-- 用户消息展示 -->
            <div v-if="msg.role === 'user'" class="user-content">
              <div class="message-text">{{ msg.content }}</div>
              <div class="message-time">{{ msg.time }}</div>
            </div>

            <!-- AI 消息展示 -->
            <div v-else class="ai-content">
              <!-- AI 状态描述及文字反馈 -->
              <div class="ai-header-status">
                <div class="status-badge-row">
                  <el-tag
                    :type="msg.status === 'error' ? 'danger' : 'success'"
                    size="small"
                    effect="plain"
                    class="status-tag"
                  >
                    <el-icon style="margin-right: 4px">
                      <component :is="msg.status === 'error' ? 'CircleClose' : 'CircleCheck'" />
                    </el-icon>
                    {{ msg.message || (msg.status === 'error' ? '查询异常' : '查询成功') }}
                  </el-tag>
                  <span class="ai-timestamp">{{ msg.time }}</span>
                </div>
              </div>

              <!-- SQL 代码高亮块展示 -->
              <div v-if="msg.sql" class="sql-block-wrapper">
                <div class="sql-header">
                  <div class="sql-header-title">
                    <span class="sql-tag">SQL</span>
                    <span class="sql-label">生成的执行语句</span>
                  </div>
                  <button
                    type="button"
                    class="copy-btn"
                    :class="{ 'copied': msg.copied }"
                    @click="copySql(msg)"
                  >
                    <el-icon><component :is="msg.copied ? 'Check' : 'CopyDocument'" /></el-icon>
                    <span>{{ msg.copied ? '已复制' : '复制 SQL' }}</span>
                  </button>
                </div>
                <pre class="sql-code-pre"><code class="sql-code" v-html="highlightSql(msg.sql)"></code></pre>
              </div>

              <!-- 动态表格展示 data 数组 -->
              <div v-if="msg.tableData && msg.tableData.length > 0" class="table-block-wrapper">
                <div class="table-block-header">
                  <div class="table-info">
                    <el-icon color="#6366f1"><DataAnalysis /></el-icon>
                    <span class="table-title">查询结果集</span>
                  </div>
                  <el-tag size="small" type="info" effect="plain" round>
                    共 {{ msg.tableData.length }} 条记录
                  </el-tag>
                </div>

                <div class="table-scroller">
                  <el-table
                    :data="msg.tableData"
                    stripe
                    border
                    size="small"
                    style="width: 100%"
                    max-height="300"
                    empty-text="暂无数据"
                  >
                    <el-table-column
                      type="index"
                      label="#"
                      width="50"
                      align="center"
                    />
                    <el-table-column
                      v-for="col in msg.columns"
                      :key="col"
                      :prop="col"
                      :label="col"
                      min-width="130"
                      show-overflow-tooltip
                    >
                      <template #default="scope">
                        <!-- 空值/null 处理 -->
                        <span v-if="scope.row[col] === null || scope.row[col] === undefined" class="cell-null">
                          NULL
                        </span>
                        <!-- 布尔值徽标展示 -->
                        <el-tag
                          v-else-if="typeof scope.row[col] === 'boolean'"
                          :type="scope.row[col] ? 'success' : 'danger'"
                          size="small"
                          effect="light"
                        >
                          {{ scope.row[col] ? 'TRUE' : 'FALSE' }}
                        </el-tag>
                        <!-- 对象/数组转字符串展示 -->
                        <span v-else-if="typeof scope.row[col] === 'object'">
                          {{ JSON.stringify(scope.row[col]) }}
                        </span>
                        <!-- 普通文本/数值 -->
                        <span v-else class="cell-normal">
                          {{ scope.row[col] }}
                        </span>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>

              <!-- data 数组为空提示 -->
              <div v-else-if="msg.tableData && msg.tableData.length === 0" class="empty-table-box">
                <el-empty :image-size="50" description="SQL 执行成功，但返回结果集为空 (0 条记录)" />
              </div>

              <!-- 失败时的错误信息与模拟测试兜底 -->
              <div v-if="msg.status === 'error'" class="error-notice-box">
                <div class="error-text">{{ msg.errorDetail || '请求失败，请确认后端已启动服务 (http://localhost:8080)' }}</div>
                <el-button
                  size="small"
                  type="primary"
                  plain
                  style="margin-top: 8px;"
                  @click="retryWithMock(msg)"
                >
                  查看示例数据效果
                </el-button>
              </div>
            </div>
          </div>

          <!-- 用户头像 -->
          <div v-if="msg.role === 'user'" class="avatar-wrapper user-avatar-wrapper">
            <div class="user-avatar">
              <el-icon :size="18"><UserFilled /></el-icon>
            </div>
          </div>
        </div>

        <!-- AI 思考中 Loading 气泡 -->
        <div v-if="isLoading" class="message-row message-ai">
          <div class="avatar-wrapper ai-avatar-wrapper">
            <div class="ai-avatar pulsing">
              <el-icon :size="18"><Cpu /></el-icon>
            </div>
          </div>
          <div class="message-bubble ai-bubble loading-bubble">
            <div class="loading-state">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
              <span class="loading-text">AI 正在理解查询需求并生成 SQL...</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部输入框与发送区 -->
    <div class="chat-footer">
      <!-- 快捷标签推荐 -->
      <div class="footer-tags">
        <span class="tags-label">推荐提问:</span>
        <button
          v-for="(tag, idx) in tagSuggestions"
          :key="idx"
          type="button"
          class="tag-chip"
          :disabled="isLoading"
          @click="fillInput(tag)"
        >
          {{ tag }}
        </button>
      </div>

      <div class="input-panel">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 4 }"
          placeholder="输入您的查询需求，例如：'查询所有运营角色' 或 '统计每个角色的用户数' (Enter 发送，Shift+Enter 换行)"
          resize="none"
          class="chat-input"
          :disabled="isLoading"
          @keydown.enter.exact.prevent="handleSendMessage"
        />
        <div class="input-actions">
          <span class="input-hint">按 Enter 发送</span>
          <el-button
            type="primary"
            :icon="Promotion"
            :loading="isLoading"
            :disabled="!inputMessage.trim() || isLoading"
            class="send-btn"
            @click="handleSendMessage"
          >
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import {
  Cpu,
  Promotion,
  Delete,
  DataAnalysis,
  UserFilled,
  CopyDocument,
  Check,
  CircleCheck,
  CircleClose
} from '@element-plus/icons-vue';
import aiApi from '@/api/ai';

// 聊天滚动容器引用
const chatScrollContainer = ref(null);

// 用户输入框内容
const inputMessage = ref('');

// 请求加载中状态
const isLoading = ref(false);

// 消息列表
const messages = ref([
  // 初始预置一条引导示例
  {
    id: 1,
    role: 'assistant',
    time: formatTime(new Date()),
    status: 'success',
    message: '助手就绪',
    sql: 'SELECT id, role_name, role_code, create_time FROM sys_role WHERE status = 1;',
    tableData: [
      { id: 1, role_name: '超级管理员', role_code: 'ROLE_ADMIN', status: 1 },
      { id: 2, role_name: '运营主管', role_code: 'ROLE_OPERATOR', status: 1 },
      { id: 3, role_name: '审计员', role_code: 'ROLE_AUDITOR', status: 1 }
    ],
    columns: ['id', 'role_name', 'role_code', 'status'],
    copied: false
  }
]);

// 快捷推荐问题列表
const quickPrompts = [
  {
    icon: '🎯',
    text: '查询所有运营角色',
    hint: '自动查找名称包含“运营”的角色信息'
  },
  {
    icon: '👥',
    text: '查询所有正常启用的用户列表',
    hint: '筛选 status = 1 的所有系统用户'
  },
  {
    icon: '🔑',
    text: '查询分配了角色权限的统计报表',
    hint: '联表聚合统计角色与其包含权限'
  },
  {
    icon: '⚡',
    text: '查询最近创建的 5 个角色',
    hint: '按创建时间倒序排列并限制行数'
  }
];

// 输入区下方的简易标签
const tagSuggestions = [
  '查询所有运营角色',
  '查询系统管理员信息',
  '统计用户与角色数量',
  '查询状态为禁用的用户'
];

/**
 * 格式化当前时间
 */
function formatTime(date) {
  const d = date || new Date();
  const pad = (n) => String(n).padStart(2, '0');
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

/**
 * 平滑滚动到聊天记录底部
 */
const scrollToBottom = () => {
  nextTick(() => {
    if (chatScrollContainer.value) {
      chatScrollContainer.value.scrollTop = chatScrollContainer.value.scrollHeight;
    }
  });
};

/**
 * 使用快捷卡片问题
 */
const useQuickPrompt = (item) => {
  inputMessage.value = item.text;
  handleSendMessage();
};

/**
 * 填充快捷问题至输入框
 */
const fillInput = (text) => {
  inputMessage.value = text;
};

/**
 * 清空对话
 */
const handleClearChat = () => {
  messages.value = [];
  ElMessage.success('对话已清空');
};

/**
 * 提取对象数组的所有字段键名
 */
const extractColumns = (data) => {
  if (!Array.isArray(data) || data.length === 0) {
    return [];
  }
  const keySet = new Set();
  data.forEach((row) => {
    if (row && typeof row === 'object') {
      Object.keys(row).forEach((k) => keySet.add(k));
    }
  });
  return Array.from(keySet);
};

/**
 * 复制 SQL 语句
 */
const copySql = async (msg) => {
  if (!msg.sql) return;
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(msg.sql);
    } else {
      const textArea = document.createElement('textarea');
      textArea.value = msg.sql;
      textArea.style.position = 'fixed';
      textArea.style.opacity = '0';
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
    }
    msg.copied = true;
    ElMessage.success('SQL 语句已复制到剪贴板');
    setTimeout(() => {
      msg.copied = false;
    }, 2000);
  } catch (err) {
    ElMessage.warning('复制失败，请手动选中文本');
  }
};

/**
 * 纯前端高亮 SQL 关键字
 */
const highlightSql = (sql) => {
  if (!sql) return '';
  // 1. 转义 HTML 字符
  let escaped = sql
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');

  // 2. 匹配单引号字符串
  escaped = escaped.replace(/'([^']*)'/g, '<span class="sql-str">\'$1\'</span>');

  // 3. 匹配数字
  escaped = escaped.replace(/\b(\d+)\b/g, '<span class="sql-num">$1</span>');

  // 4. 匹配 SQL 常见关键字
  const keywords = [
    'SELECT', 'FROM', 'WHERE', 'AND', 'OR', 'NOT', 'IN', 'IS', 'NULL',
    'JOIN', 'LEFT', 'RIGHT', 'INNER', 'OUTER', 'ON', 'AS', 'GROUP', 'BY',
    'ORDER', 'ASC', 'DESC', 'LIMIT', 'OFFSET', 'HAVING', 'COUNT', 'SUM',
    'AVG', 'MAX', 'MIN', 'DISTINCT', 'LIKE', 'BETWEEN', 'UNION', 'ALL',
    'INSERT', 'INTO', 'VALUES', 'UPDATE', 'SET', 'DELETE', 'CREATE', 'TABLE'
  ];

  const regex = new RegExp(`\\b(${keywords.join('|')})\\b`, 'gi');
  escaped = escaped.replace(regex, (match) => {
    return `<span class="sql-kw">${match.toUpperCase()}</span>`;
  });

  return escaped;
};

/**
 * 发送消息核心逻辑
 */
const handleSendMessage = async () => {
  const query = inputMessage.value.trim();
  if (!query || isLoading.value) return;

  // 1. 压入用户消息气泡（右侧）
  const userMsgId = Date.now();
  messages.value.push({
    id: userMsgId,
    role: 'user',
    content: query,
    time: formatTime(new Date())
  });

  inputMessage.value = '';
  isLoading.value = true;
  scrollToBottom();

  try {
    // 2. 调用 POST /api/ai/chat
    const response = await aiApi.chat(query);

    // 兼容后端返回格式可能存在两层结构或直接解包结构
    // 契约：{ code: 200, data: { sql: "...", data: [ ... ], message: "查询成功" } }
    let aiResult = response;
    if (response && response.data && typeof response.data === 'object' && ('sql' in response.data || 'data' in response.data)) {
      aiResult = response.data;
    }

    const sql = aiResult?.sql || '';
    const tableData = Array.isArray(aiResult?.data) ? aiResult.data : [];
    const messageText = aiResult?.message || '查询成功';
    const columns = extractColumns(tableData);

    // 3. 压入 AI 消息气泡（左侧）
    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      time: formatTime(new Date()),
      status: 'success',
      message: messageText,
      sql: sql,
      tableData: tableData,
      columns: columns,
      copied: false
    });
  } catch (error) {
    console.error('AI 聊天接口调用失败:', error);
    // 生成一条带友好提示与重试机制的 AI 气泡
    messages.value.push({
      id: Date.now() + 2,
      role: 'assistant',
      time: formatTime(new Date()),
      status: 'error',
      userQuery: query,
      message: '查询未成功响应',
      errorDetail: error.message || '网络连接超时或后端未就绪',
      sql: '',
      tableData: null,
      columns: []
    });
  } finally {
    isLoading.value = false;
    scrollToBottom();
  }
};

/**
 * 当后端未就绪时，为用户提供一键模拟演示效果
 */
const retryWithMock = (errorMsg) => {
  const mockQueries = {
    '运营': {
      sql: "SELECT id, role_name, role_code, description FROM sys_role WHERE role_name LIKE '%运营%';",
      data: [
        { id: 4, role_name: '运营主管', role_code: 'ROLE_OPERATOR_LEAD', description: '负责日常用户运营管理' },
        { id: 5, role_name: '运营专员', role_code: 'ROLE_OPERATOR', description: '负责活动与文案配置' }
      ],
      message: '查询成功（演示数据）'
    },
    'default': {
      sql: "SELECT r.id, r.role_name, COUNT(u.id) as user_count FROM sys_role r LEFT JOIN sys_user_role ur ON r.id = ur.role_id LEFT JOIN sys_user u ON ur.user_id = u.id GROUP BY r.id, r.role_name;",
      data: [
        { id: 1, role_name: '超级管理员', user_count: 2 },
        { id: 2, role_name: '运营', user_count: 5 },
        { id: 3, role_name: '普通用户', user_count: 18 }
      ],
      message: '查询成功（演示数据）'
    }
  };

  const choice = (errorMsg.userQuery && errorMsg.userQuery.includes('运营'))
    ? mockQueries['运营']
    : mockQueries['default'];

  errorMsg.status = 'success';
  errorMsg.message = choice.message;
  errorMsg.sql = choice.sql;
  errorMsg.tableData = choice.data;
  errorMsg.columns = extractColumns(choice.data);
  errorMsg.errorDetail = null;
  scrollToBottom();
};

onMounted(() => {
  scrollToBottom();
});
</script>

<style scoped>
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 110px);
  min-height: 580px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px -2px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(226, 232, 240, 0.8);
  overflow: hidden;
}

/* 顶部信息栏 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border-bottom: 1px solid #e2e8f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-icon-box {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.header-icon {
  font-size: 24px;
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.header-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
}

.header-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  border-radius: 9999px;
  padding: 0 8px;
}

.live-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 6px #10b981;
  display: inline-block;
}

.header-subtitle {
  margin: 0;
  font-size: 13px;
  color: #64748b;
}

/* 中间对话区域 */
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #f8fafc;
  scroll-behavior: smooth;
}

/* 欢迎引导卡片 */
.welcome-box {
  max-width: 680px;
  margin: 20px auto;
  text-align: center;
  padding: 32px 24px;
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.04);
}

.welcome-avatar {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: #eef2ff;
  border: 1px solid #c7d2fe;
  margin-bottom: 16px;
}

.avatar-sparkle {
  position: absolute;
  top: -6px;
  right: -6px;
  font-size: 16px;
}

.welcome-title {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.welcome-desc {
  margin: 0 auto 24px auto;
  font-size: 14px;
  color: #64748b;
  max-width: 520px;
  line-height: 1.6;
}

.quick-prompts-title {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  text-align: left;
  margin-bottom: 12px;
}

.quick-prompts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.prompt-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 14px;
  text-align: left;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s ease;
}

.prompt-card:hover {
  background: #ffffff;
  border-color: #6366f1;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.12);
  transform: translateY(-2px);
}

.prompt-icon {
  font-size: 20px;
  line-height: 1;
}

.prompt-text {
  font-size: 13.5px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 2px;
}

.prompt-hint {
  font-size: 12px;
  color: #94a3b8;
}

/* 消息列表与气泡 */
.message-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  animation: fadeIn 0.25s ease forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-user {
  flex-direction: row-reverse;
}

.message-ai {
  flex-direction: row;
}

/* 头像微章 */
.avatar-wrapper {
  flex-shrink: 0;
}

.ai-avatar {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #0f172a 0%, #334155 100%);
  color: #818cf8;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.2);
  border: 1px solid rgba(129, 140, 248, 0.3);
}

.ai-avatar.pulsing {
  animation: avatarPulse 1.5s infinite;
}

@keyframes avatarPulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(99, 102, 241, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 0 0 6px rgba(99, 102, 241, 0);
  }
}

.user-avatar {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

/* 消息气泡容器 */
.message-bubble {
  max-width: 82%;
  border-radius: 14px;
}

/* 用户气泡 */
.user-bubble {
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%);
  color: #ffffff;
  padding: 12px 18px;
  border-bottom-right-radius: 4px;
  box-shadow: 0 4px 14px rgba(79, 70, 229, 0.2);
}

.user-content .message-text {
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.user-content .message-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
  text-align: right;
  margin-top: 4px;
}

/* AI 气泡 */
.ai-bubble {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  padding: 16px 20px;
  border-bottom-left-radius: 4px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  width: 100%;
}

.ai-header-status {
  margin-bottom: 12px;
}

.status-badge-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.status-tag {
  font-weight: 600;
}

.ai-timestamp {
  font-size: 11px;
  color: #94a3b8;
}

/* SQL 代码块容器 */
.sql-block-wrapper {
  background: #0f172a;
  border-radius: 10px;
  border: 1px solid #1e293b;
  overflow: hidden;
  margin-bottom: 14px;
}

.sql-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 14px;
  background: #1e293b;
  border-bottom: 1px solid #334155;
}

.sql-header-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sql-tag {
  font-size: 10px;
  font-weight: 700;
  color: #38bdf8;
  background: rgba(56, 189, 248, 0.15);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

.sql-label {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
}

.copy-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: transparent;
  border: 1px solid #475569;
  color: #cbd5e1;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.copy-btn:hover {
  background: #334155;
  color: #ffffff;
  border-color: #64748b;
}

.copy-btn.copied {
  color: #34d399;
  border-color: #34d399;
  background: rgba(52, 211, 153, 0.1);
}

.sql-code-pre {
  margin: 0;
  padding: 14px;
  overflow-x: auto;
  font-family: 'Fira Code', Consolas, Monaco, monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #e2e8f0;
}

/* SQL 代码着色样式 */
:deep(.sql-kw) {
  color: #38bdf8;
  font-weight: 700;
}

:deep(.sql-str) {
  color: #4ade80;
}

:deep(.sql-num) {
  color: #fbbf24;
}

/* 数据结果表格区域 */
.table-block-wrapper {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px;
  margin-top: 10px;
}

.table-block-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.table-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.table-title {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
}

.table-scroller {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.cell-null {
  color: #94a3b8;
  font-style: italic;
  font-family: monospace;
}

.cell-normal {
  color: #1e293b;
  font-size: 13px;
}

.empty-table-box {
  padding: 16px 0;
}

.error-notice-box {
  background: #fef2f2;
  border: 1px solid #fecaca;
  padding: 12px;
  border-radius: 8px;
  margin-top: 8px;
}

.error-text {
  font-size: 13px;
  color: #b91c1c;
  line-height: 1.5;
}

/* Loading 状态 */
.loading-bubble {
  max-width: 320px;
  padding: 14px 18px;
}

.loading-state {
  display: flex;
  align-items: center;
  gap: 12px;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #6366f1;
  display: inline-block;
  animation: typingBounce 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typingBounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1.1);
    opacity: 1;
  }
}

.loading-text {
  font-size: 13px;
  color: #64748b;
}

/* 底部操作区 */
.chat-footer {
  padding: 16px 24px;
  background: #ffffff;
  border-top: 1px solid #e2e8f0;
}

.footer-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.tags-label {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.tag-chip {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 9999px;
  padding: 3px 10px;
  font-size: 12px;
  color: #475569;
  cursor: pointer;
  transition: all 0.15s ease;
}

.tag-chip:hover:not(:disabled) {
  background: #eef2ff;
  border-color: #c7d2fe;
  color: #4f46e5;
}

.tag-chip:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.input-panel {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-input {
  flex: 1;
}

.chat-input :deep(.el-textarea__inner) {
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  padding: 10px 14px;
  font-size: 14px;
  transition: all 0.2s ease;
  line-height: 1.5;
}

.chat-input :deep(.el-textarea__inner:focus) {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

.input-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  flex-shrink: 0;
}

.input-hint {
  font-size: 11px;
  color: #94a3b8;
}

.send-btn {
  height: 40px;
  padding: 0 20px;
  border-radius: 10px;
  font-weight: 600;
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.4);
}
</style>
