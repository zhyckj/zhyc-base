<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="user-post-page">
    <a-card title="用户岗位绑定" :bordered="false">
      <div class="query-panel">
        <a-form layout="inline" class="query-form">
          <a-form-item label="当前租户">
            <a-input v-model:value="tenantId" disabled class="tenant-id" />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button :loading="optionsLoading" @click="loadOptions">
                <template #icon><ReloadOutlined /></template>
                查询用户
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>

        <div class="summary-strip">
          <div class="summary-item">
            <span>用户总数</span>
            <strong>{{ users.length }}</strong>
          </div>
          <div class="summary-item">
            <span>已绑定</span>
            <strong>{{ posts.length }}</strong>
          </div>
          <div class="summary-item">
            <span>主岗位</span>
            <strong>{{ primaryPostName }}</strong>
          </div>
          <div class="summary-item">
            <span>可选岗位</span>
            <strong>{{ allPosts.length }}</strong>
          </div>
        </div>
      </div>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <section class="user-list-section">
        <div class="section-header">
          <div>
            <h3>用户列表</h3>
            <p>先查询当前租户全部用户，再选择用户维护岗位绑定关系。</p>
          </div>
          <a-tag :color="users.length ? 'blue' : 'default'">
            {{ users.length ? `${users.length} 个用户` : '无用户' }}
          </a-tag>
        </div>

        <a-table
          row-key="id"
          :columns="userColumns"
          :data-source="users"
          :loading="optionsLoading"
          :pagination="$tablePagination"
          size="middle"
        >
          <template #emptyText>
            <a-empty description="当前租户暂无用户" />
          </template>
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'userInfo'">
              <div class="user-name-cell">
                <strong>{{ record.nickname }}</strong>
                <span>{{ record.username }}</span>
              </div>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
                {{ $statusLabel(record.status) }}
              </a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a-button type="link" v-permission="'system:user:edit'" @click="openUserPostEditor(record)">
                编辑岗位
              </a-button>
            </template>
          </template>
        </a-table>
      </section>

      <a-empty
        v-if="!userId"
        description="请先在用户列表中点击编辑岗位"
        class="page-empty"
      />

      <div v-else class="binding-layout">
        <section class="bound-section">
          <div class="section-header">
            <div>
              <h3>当前已绑定岗位</h3>
              <p>{{ selectedUserLabel }}</p>
            </div>
            <a-tag :color="posts.length ? 'blue' : 'default'">
              {{ posts.length ? `${posts.length} 个岗位` : '未绑定' }}
            </a-tag>
          </div>

          <a-table
            row-key="postId"
            :columns="postColumns"
            :data-source="posts"
            :loading="status === 'loading'"
            :pagination="$tablePagination"
            size="middle"
          >
            <template #emptyText>
              <a-empty description="该用户暂未绑定岗位" />
            </template>
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'postName'">
                <div class="post-name-cell">
                  <strong>{{ record.postName }}</strong>
                  <span>{{ record.postCode }}</span>
                </div>
              </template>
              <template v-if="column.key === 'primaryFlag'">
                <a-tag :color="record.primaryFlag ? 'green' : 'default'">
                  {{ record.primaryFlag ? '主岗位' : '兼任' }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </section>

        <section class="editor-section">
          <div class="section-header">
            <div>
              <h3>调整岗位绑定</h3>
              <p>先选择要绑定的岗位，再从已选岗位中指定一个主岗位。</p>
            </div>
          </div>

          <a-form layout="vertical" class="bind-form">
            <a-form-item label="绑定岗位" required>
              <a-select
                v-model:value="selectedPostIds"
                mode="multiple"
                allow-clear
                show-search
                option-filter-prop="label"
                :loading="optionsLoading"
                :max-tag-count="4"
                :options="postOptions"
                placeholder="请选择要绑定的岗位"
                @change="handleSelectedPostsChange"
              />
            </a-form-item>

            <a-form-item label="主岗位" required>
              <a-select
                v-model:value="primaryPostId"
                allow-clear
                show-search
                option-filter-prop="label"
                :disabled="selectedPostIds.length === 0"
                :options="selectedPostOptions"
                placeholder="请从已选岗位中选择主岗位"
              />
            </a-form-item>
          </a-form>

          <div class="selection-preview">
            <span class="preview-label">保存后效果</span>
            <a-empty v-if="selectedPostDetails.length === 0" description="未选择岗位" />
            <div v-else class="post-tags">
              <a-tag
                v-for="post in selectedPostDetails"
                :key="post.id"
                :color="primaryPostId === post.id ? 'green' : 'blue'"
              >
                {{ post.postName }}（{{ post.postCode }}）{{ primaryPostId === post.id ? ' · 主岗位' : '' }}
              </a-tag>
            </div>
          </div>

          <div class="action-bar">
            <a-button :disabled="status === 'loading' || saving" @click="resetSelectionFromCurrent">恢复当前绑定</a-button>
            <a-button
              type="primary"
              v-permission="'system:user:edit'"
              :disabled="!userId"
              :loading="saving"
              @click="submitUserPosts"
            >
              <template #icon><SaveOutlined /></template>
              保存绑定
            </a-button>
          </div>
        </section>
      </div>

      <span class="permission-code">system:user:edit</span>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { ReloadOutlined, SaveOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { computed, onMounted, ref } from 'vue';

import { listSystemPosts, type SystemPost } from '@/api/system/post';
import { listSystemUsers, type SystemUser } from '@/api/system/user';
import { bindSystemUserPosts, listSystemUserPosts, type SystemUserPost } from '@/api/system/user-post';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 当前用户主键。 */
const userId = ref<number>();
/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 候选项加载状态。 */
const optionsLoading = ref(false);
/** 保存按钮加载状态。 */
const saving = ref(false);
/** 异常提示文案。 */
const errorMessage = ref('');
/** 已绑定岗位列表。 */
const posts = ref<SystemUserPost[]>([]);
/** 用户候选项。 */
const users = ref<SystemUser[]>([]);
/** 岗位候选项。 */
const allPosts = ref<SystemPost[]>([]);
/** 已选择的岗位主键列表。 */
const selectedPostIds = ref<number[]>([]);
/** 主岗位主键。 */
const primaryPostId = ref<number>();

/** 用户列表表格列定义。 */
const userColumns = [
  { title: '用户信息', dataIndex: 'nickname', key: 'userInfo' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 120 },
];

/** 已绑定岗位表格列定义。 */
const postColumns = [
  { title: '岗位信息', dataIndex: 'postName', key: 'postName' },
  { title: '主岗位', dataIndex: 'primaryFlag', key: 'primaryFlag', width: 120 },
];

/** 当前选中用户展示名称。 */
const selectedUserLabel = computed(() => {
  const currentUser = users.value.find((user) => user.id === userId.value);
  return currentUser ? `${currentUser.nickname}（${currentUser.username}）` : '未选择用户';
});

/** 岗位选择项。 */
const postOptions = computed(() =>
  allPosts.value.map((post) => ({
    label: `${post.postName}（${post.postCode}）`,
    value: post.id,
  })),
);

/** 已选择岗位详情，用于保存前预览。 */
const selectedPostDetails = computed(() =>
  selectedPostIds.value
    .map((postId) => allPosts.value.find((post) => post.id === postId))
    .filter((post): post is SystemPost => Boolean(post)),
);

/** 主岗位候选项，限制在已选择岗位内。 */
const selectedPostOptions = computed(() =>
  postOptions.value.filter((option) => selectedPostIds.value.includes(option.value)),
);

/** 当前主岗位展示名称。 */
const primaryPostName = computed(() => {
  const currentPrimaryPost = selectedPostDetails.value.find((post) => post.id === primaryPostId.value);
  return currentPrimaryPost?.postName ?? '未设置';
});

/**
 * 加载用户岗位列表。
 */
async function loadUserPosts(): Promise<void> {
  if (!userId.value) {
    message.warning('请先选择用户');
    return;
  }
  status.value = 'loading';
  errorMessage.value = '';
  try {
    const currentTenantId = syncTenantContext();
    posts.value = await listSystemUserPosts(currentTenantId, userId.value);
    resetSelectionFromCurrent();
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '用户岗位加载失败';
    status.value = 'error';
  }
}

/**
 * 打开用户岗位编辑区。
 *
 * @param user 当前待维护岗位的系统用户
 */
async function openUserPostEditor(user: SystemUser): Promise<void> {
  if (userId.value !== user.id) {
    handleUserChange();
  }
  userId.value = user.id;
  await loadUserPosts();
}

/**
 * 提交用户岗位绑定。
 */
async function submitUserPosts(): Promise<void> {
  if (!userId.value) {
    message.warning('请先选择用户');
    return;
  }
  if (selectedPostIds.value.length > 0 && !primaryPostId.value) {
    message.warning('请选择一个主岗位');
    return;
  }
  saving.value = true;
  try {
    const currentTenantId = syncTenantContext();
    await bindSystemUserPosts(currentTenantId, userId.value, {
      posts: selectedPostIds.value.map((postId) => ({
        postId,
        primaryFlag: primaryPostId.value === postId,
      })),
    });
    await loadUserPosts();
    message.success('用户岗位已保存');
  } catch (error) {
    message.error(error instanceof Error ? error.message : '用户岗位保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 加载用户和岗位候选项。
 */
async function loadOptions(): Promise<void> {
  optionsLoading.value = true;
  try {
    const currentTenantId = syncTenantContext();
    const [loadedUsers, loadedPosts] = await Promise.all([
      listSystemUsers(currentTenantId),
      listSystemPosts(currentTenantId),
    ]);
    users.value = loadedUsers;
    allPosts.value = loadedPosts;
    if (userId.value && !loadedUsers.some((user) => user.id === userId.value)) {
      userId.value = undefined;
      handleUserChange();
    }
  } catch (error) {
    message.error(error instanceof Error ? error.message : '用户岗位候选项加载失败');
  } finally {
    optionsLoading.value = false;
  }
}

/**
 * 切换用户时清理旧的岗位选择，避免误保存到新用户。
 */
function handleUserChange(): void {
  posts.value = [];
  selectedPostIds.value = [];
  primaryPostId.value = undefined;
  status.value = 'idle';
  errorMessage.value = '';
}

/**
 * 同步主岗位选择，避免主岗位脱离已绑定岗位。
 */
function handleSelectedPostsChange(): void {
  if (primaryPostId.value && !selectedPostIds.value.includes(primaryPostId.value)) {
    primaryPostId.value = undefined;
  }
  if (!primaryPostId.value && selectedPostIds.value.length === 1) {
    primaryPostId.value = selectedPostIds.value[0];
  }
}

/**
 * 从当前已绑定岗位恢复编辑区选择。
 */
function resetSelectionFromCurrent(): void {
  selectedPostIds.value = posts.value.map((post) => post.postId);
  primaryPostId.value = posts.value.find((post) => post.primaryFlag)?.postId;
}

/**
 * 同步后台运行时租户上下文。
 *
 * @returns 当前后台租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId();
  tenantId.value = currentTenantId;
  return currentTenantId;
}

onMounted(() => {
  void loadOptions();
});
</script>

<style scoped>
.user-post-page {
  min-width: 0;
}

.query-panel {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eef2f7;
}

.query-form {
  row-gap: 12px;
}

.tenant-id {
  width: 180px;
}

.summary-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(96px, 1fr));
  min-width: 480px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f8fafc;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 14px;
  border-right: 1px solid #e5e7eb;
}

.summary-item:last-child {
  border-right: 0;
}

.summary-item span {
  color: #64748b;
  font-size: 12px;
}

.summary-item strong {
  overflow: hidden;
  color: #111827;
  font-size: 15px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.state-alert {
  margin-top: 16px;
}

.user-list-section {
  padding-top: 16px;
}

.page-empty {
  padding: 72px 0;
}

.binding-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 18px;
  padding-top: 16px;
}

.bound-section,
.editor-section {
  min-width: 0;
}

.editor-section {
  padding-left: 18px;
  border-left: 1px solid #eef2f7;
}

.section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.section-header h3 {
  margin: 0;
  color: #111827;
  font-size: 15px;
  font-weight: 600;
}

.section-header p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}

.post-name-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name-cell span,
.post-name-cell span {
  color: #64748b;
  font-size: 12px;
}

.bind-form :deep(.ant-select) {
  width: 100%;
}

.selection-preview {
  min-height: 96px;
  margin-top: 8px;
  padding: 12px;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  background: #f8fafc;
}

.preview-label {
  display: block;
  margin-bottom: 10px;
  color: #475569;
  font-size: 13px;
  font-weight: 600;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}

.permission-code {
  display: none;
}

@media (max-width: 1180px) {
  .query-panel,
  .binding-layout {
    grid-template-columns: 1fr;
  }

  .query-panel {
    display: grid;
  }

  .summary-strip {
    min-width: 0;
  }

  .editor-section {
    padding-left: 0;
    padding-top: 16px;
    border-top: 1px solid #eef2f7;
    border-left: 0;
  }
}

@media (max-width: 640px) {
  .tenant-id {
    width: 100%;
  }

  .summary-strip {
    grid-template-columns: 1fr;
  }

  .summary-item {
    border-right: 0;
    border-bottom: 1px solid #e5e7eb;
  }

  .summary-item:last-child {
    border-bottom: 0;
  }

  .action-bar {
    flex-direction: column;
  }
}
</style>
