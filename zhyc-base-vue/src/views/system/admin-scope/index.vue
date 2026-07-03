<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="admin-scope-page">
    <a-card title="管理员范围配置" :bordered="false">
      <div class="query-panel">
        <a-form layout="inline" class="query-form">
          <a-form-item label="当前租户">
            <a-input v-model:value="tenantId" disabled class="tenant-id" />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button :loading="optionsLoading" @click="loadOptions">
                <template #icon><ReloadOutlined /></template>
                刷新用户
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>

        <div class="summary-strip">
          <div class="summary-item">
            <span>已配置范围</span>
            <strong>{{ scopes.length }}</strong>
          </div>
          <div class="summary-item">
            <span>租户用户</span>
            <strong>{{ users.length }}</strong>
          </div>
          <div class="summary-item">
            <span>当前配置用户</span>
            <strong>{{ selectedUserLabel }}</strong>
          </div>
        </div>
      </div>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <section class="user-list-panel">
        <div class="section-header">
          <div>
            <h3>租户用户列表</h3>
            <p>先选择当前租户下的用户，再维护该用户的管理范围。</p>
          </div>
          <a-tag :color="users.length ? 'blue' : 'default'">{{ users.length ? `${users.length} 个用户` : '暂无用户' }}</a-tag>
        </div>

        <a-table
          row-key="id"
          :columns="userColumns"
          :data-source="users"
          :loading="optionsLoading"
          :pagination="$tablePagination"
          :row-class-name="userRowClassName"
          class="user-table"
          size="middle"
        >
          <template #emptyText>
            <a-empty description="当前租户暂无用户" />
          </template>
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'userName'">
              <div class="user-name-cell">
                <strong>{{ record.nickname }}</strong>
                <span>{{ record.username }}</span>
              </div>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">{{ $statusLabel(record.status) }}</a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a-button
                size="small"
                type="primary"
                ghost
                v-permission="'system:admin:edit'"
                :loading="status === 'loading' && userId === record.id"
                @click="configureUserScope(record)"
              >
                配置范围
              </a-button>
            </template>
          </template>
        </a-table>
      </section>

      <a-empty
        v-if="!userId"
        description="请先在租户用户列表中点击配置范围"
        class="page-empty"
      />

      <div v-else class="scope-list-panel">
        <section class="scope-list-section">
          <div class="section-header">
            <div>
              <h3>管理员范围列表</h3>
              <p>当前管理员：{{ selectedUserLabel }}，直接在列表中维护租户、组织或模块范围。</p>
            </div>
            <a-space>
              <a-tag :color="scopes.length ? 'blue' : 'default'">
                当前 {{ scopes.length ? `${scopes.length} 条` : '未配置' }}
              </a-tag>
              <a-tag :color="normalizedEditableScopes.length ? 'green' : 'default'">
                待保存 {{ normalizedEditableScopes.length }} 条
              </a-tag>
              <a-button size="small" type="primary" ghost v-permission="'system:admin:edit'" @click="appendScope">
                <template #icon><PlusOutlined /></template>
                添加范围
              </a-button>
            </a-space>
          </div>

          <a-table
            row-key="id"
            :columns="editableColumns"
            :data-source="editableScopes"
            :loading="status === 'loading'"
            :pagination="$tablePagination"
            class="scope-edit-table"
            size="middle"
          >
            <template #emptyText>
              <a-empty description="暂无管理范围，点击添加范围后维护" />
            </template>
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'scopeType'">
                <a-select
                  v-model:value="record.scopeType"
                  :disabled="saving"
                  :options="scopeTypeOptions"
                  class="scope-type-select"
                />
              </template>
              <template v-if="column.key === 'scopeRefCode'">
                <a-select
                  v-if="record.scopeType === 'org'"
                  v-model:value="record.scopeRefCode"
                  allow-clear
                  show-search
                  option-filter-prop="label"
                  :disabled="saving"
                  :options="orgCodeOptions"
                  class="scope-ref-control"
                  placeholder="请选择组织"
                />
                <a-input
                  v-else
                  v-model:value="record.scopeRefCode"
                  :disabled="saving"
                  class="scope-ref-control"
                  :placeholder="record.scopeType === 'tenant' ? '例如 zhyc-platform' : '例如 system'"
                />
              </template>
              <template v-if="column.key === 'scopeName'">
                <div class="scope-name-cell">
                  <strong>{{ record.scopeRefCode ? resolveScopeName(record.scopeType, record.scopeRefCode) : '未填写范围编码' }}</strong>
                  <span>{{ scopeTypeLabel(record.scopeType) }} · {{ record.scopeRefCode || '待补充' }}</span>
                </div>
              </template>
              <template v-if="column.key === 'action'">
                <a-button danger v-permission="'system:admin:edit'" :disabled="saving" @click="removeScope(record.id)">
                  <template #icon><DeleteOutlined /></template>
                  删除
                </a-button>
              </template>
            </template>
          </a-table>

          <div class="scope-list-footer">
            <div class="scope-save-preview">
              <span class="preview-label">保存后范围</span>
              <span v-if="normalizedEditableScopes.length === 0" class="preview-empty">未配置范围</span>
              <span v-else class="preview-text">
                {{ normalizedEditableScopes.map((item) => `${scopeTypeLabel(item.scopeType)}：${resolveScopeName(item.scopeType, item.scopeRefCode)}`).join('、') }}
              </span>
            </div>
            <div class="action-bar">
              <a-button :disabled="status === 'loading' || saving" @click="resetSelectionFromCurrent">恢复当前范围</a-button>
              <a-button
                type="primary"
                v-permission="'system:admin:edit'"
                :disabled="!userId"
                :loading="saving"
                @click="submitScopes"
              >
                <template #icon><SaveOutlined /></template>
                保存范围
              </a-button>
            </div>
          </div>
        </section>
      </div>

      <span class="permission-code">system:admin:edit</span>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { DeleteOutlined, PlusOutlined, ReloadOutlined, SaveOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { computed, onMounted, ref } from 'vue';

import { bindSystemAdminScopes, listSystemAdminScopes, type SystemAdminScope } from '@/api/system/admin-scope';
import { listSystemOrgTree, type SystemOrgTreeNode } from '@/api/system/org';
import { listSystemUsers, type SystemUser } from '@/api/system/user';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

interface AdminScopeEditItem {
  /** 前端编辑行唯一标识。 */
  id: string;
  /** 管理范围类型。 */
  scopeType: string;
  /** 管理范围引用编码。 */
  scopeRefCode: string;
}

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 当前配置范围的用户主键。 */
const userId = ref<number>();
/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 候选项加载状态。 */
const optionsLoading = ref(false);
/** 保存按钮加载状态。 */
const saving = ref(false);
/** 异常提示文案。 */
const errorMessage = ref('');
/** 管理员范围列表。 */
const scopes = ref<SystemAdminScope[]>([]);
/** 管理员范围编辑列表。 */
const editableScopes = ref<AdminScopeEditItem[]>([]);
/** 当前租户用户列表。 */
const users = ref<SystemUser[]>([]);
/** 组织树候选项。 */
const orgTree = ref<SystemOrgTreeNode[]>([]);

/** 管理范围类型选项。 */
const scopeTypeOptions = [
  { label: '租户', value: 'tenant' },
  { label: '组织', value: 'org' },
  { label: '模块', value: 'module' },
];

/** 租户用户列表列定义。 */
const userColumns = [
  { title: '用户', dataIndex: 'nickname', key: 'userName' },
  { title: '登录账号', dataIndex: 'username', key: 'username', width: 180 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '操作', key: 'action', width: 120 },
];

/** 列表式编辑表格列定义。 */
const editableColumns = [
  { title: '范围类型', dataIndex: 'scopeType', key: 'scopeType', width: 150 },
  { title: '范围编码', dataIndex: 'scopeRefCode', key: 'scopeRefCode', width: 360 },
  { title: '展示名称', dataIndex: 'scopeName', key: 'scopeName' },
  { title: '操作', key: 'action', width: 110 },
];

/** 当前选中用户展示名称。 */
const selectedUserLabel = computed(() => {
  const selectedUser = users.value.find((item) => item.id === userId.value);
  return selectedUser ? `${selectedUser.nickname}（${selectedUser.username}）` : '未选择用户';
});

/** 展平后的组织候选项。 */
const orgOptions = computed(() => flattenOrgOptions(orgTree.value));

/** 组织编码选择项，提交管理员范围时使用组织编码。 */
const orgCodeOptions = computed(() =>
  orgOptions.value.map((org) => ({
    label: org.label,
    value: org.orgCode,
  })),
);

/** 已规范化的编辑范围，过滤空行并去重。 */
const normalizedEditableScopes = computed(() => normalizeScopes(editableScopes.value));

/**
 * 加载管理员范围。
 */
async function loadScopes(): Promise<void> {
  if (!userId.value) {
    message.warning('请先在租户用户列表中选择用户');
    return;
  }
  status.value = 'loading';
  errorMessage.value = '';
  try {
    const currentTenantId = syncTenantContext();
    scopes.value = await listSystemAdminScopes(currentTenantId, userId.value);
    resetSelectionFromCurrent();
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '管理员范围加载失败';
    status.value = 'error';
  }
}

/**
 * 提交管理员范围。
 */
async function submitScopes(): Promise<void> {
  if (!userId.value) {
    message.warning('请先在租户用户列表中选择用户');
    return;
  }
  if (editableScopes.value.some((item) => !item.scopeType || !item.scopeRefCode.trim())) {
    message.warning('请补全范围类型和范围编码，或删除空行');
    return;
  }
  saving.value = true;
  try {
    const currentTenantId = syncTenantContext();
    await bindSystemAdminScopes(currentTenantId, userId.value, {
      scopes: normalizedEditableScopes.value,
    });
    await loadScopes();
    message.success('管理员范围已保存');
  } catch (error) {
    message.error(error instanceof Error ? error.message : '管理员范围保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 加载租户用户、组织候选项。
 */
async function loadOptions(): Promise<void> {
  optionsLoading.value = true;
  try {
    const currentTenantId = syncTenantContext();
    const selectedUserId = userId.value;
    const [loadedUsers, loadedOrgTree] = await Promise.all([
      listSystemUsers(currentTenantId),
      listSystemOrgTree(currentTenantId),
    ]);
    users.value = loadedUsers;
    orgTree.value = loadedOrgTree;
    if (selectedUserId && !loadedUsers.some((user) => user.id === selectedUserId)) {
      clearUserSelection();
      return;
    }
    if (selectedUserId) {
      await loadScopes();
    }
  } catch (error) {
    message.error(error instanceof Error ? error.message : '管理员范围候选项加载失败');
  } finally {
    optionsLoading.value = false;
  }
}

/**
 * 从租户用户列表进入范围配置。
 *
 * @param user 待配置范围的租户用户
 */
async function configureUserScope(user: SystemUser): Promise<void> {
  if (saving.value) {
    message.warning('范围保存中，请稍后再切换用户');
    return;
  }
  userId.value = user.id;
  await loadScopes();
}

/**
 * 清理当前用户范围配置状态。
 */
function clearUserSelection(): void {
  userId.value = undefined;
  scopes.value = [];
  editableScopes.value = [];
  status.value = 'idle';
  errorMessage.value = '';
}

/**
 * 新增一条管理范围编辑行。
 */
function appendScope(): void {
  editableScopes.value.push({
    id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
    scopeType: 'org',
    scopeRefCode: '',
  });
}

/**
 * 删除一条管理范围编辑行。
 *
 * @param itemId 编辑行唯一标识
 */
function removeScope(itemId: string): void {
  editableScopes.value = editableScopes.value.filter((item) => item.id !== itemId);
}

/**
 * 标记当前正在配置范围的用户行。
 *
 * @param user 租户用户
 * @returns 表格行样式类名
 */
function userRowClassName(user: SystemUser): string {
  return user.id === userId.value ? 'user-row-selected' : '';
}

/**
 * 从当前已配置范围恢复编辑区。
 */
function resetSelectionFromCurrent(): void {
  editableScopes.value = scopes.value.map((scope, index) => ({
    id: `${scope.scopeType}-${scope.scopeRefCode}-${index}`,
    scopeType: scope.scopeType,
    scopeRefCode: scope.scopeRefCode,
  }));
}

/**
 * 获取范围类型中文说明。
 *
 * @param scopeType 范围类型
 * @returns 中文说明
 */
function scopeTypeLabel(scopeType: string): string {
  return scopeTypeOptions.find((option) => option.value === scopeType)?.label ?? scopeType;
}

/**
 * 解析范围展示名称。
 *
 * @param scopeType 范围类型
 * @param scopeRefCode 范围引用编码
 * @returns 展示名称
 */
function resolveScopeName(scopeType: string, scopeRefCode: string): string {
  if (scopeType === 'tenant' && scopeRefCode === tenantId.value) {
    return `${tenantId.value}（当前租户）`;
  }
  if (scopeType === 'org') {
    return orgOptions.value.find((org) => org.orgCode === scopeRefCode)?.label.trim() ?? scopeRefCode;
  }
  return scopeRefCode;
}

/**
 * 规范化管理员范围编辑项。
 *
 * @param items 编辑项列表
 * @returns 去重后的提交范围
 */
function normalizeScopes(items: AdminScopeEditItem[]): Array<{ scopeType: string; scopeRefCode: string }> {
  const scopeMap = new Map<string, { scopeType: string; scopeRefCode: string }>();
  items.forEach((item) => {
    const scopeType = item.scopeType.trim();
    const scopeRefCode = item.scopeRefCode.trim();
    if (!scopeType || !scopeRefCode) {
      return;
    }
    scopeMap.set(`${scopeType}:${scopeRefCode}`, {
      scopeType,
      scopeRefCode,
    });
  });
  return [...scopeMap.values()];
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

/**
 * 展平组织树为下拉项。
 *
 * @param nodes 组织树节点
 * @param level 当前层级
 * @returns 组织选择项
 */
function flattenOrgOptions(
  nodes: SystemOrgTreeNode[],
  level = 0,
): Array<{ label: string; value: number; orgCode: string }> {
  return nodes.flatMap((node) => [
    {
      label: `${'　'.repeat(level)}${node.orgName}（${node.orgCode}）`,
      value: node.id,
      orgCode: node.orgCode,
    },
    ...flattenOrgOptions(node.children ?? [], level + 1),
  ]);
}

onMounted(() => {
  void loadOptions();
});
</script>

<style scoped>
.admin-scope-page {
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
  grid-template-columns: repeat(3, minmax(96px, 1fr));
  min-width: 420px;
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

.page-empty {
  padding: 40px 0;
}

.user-list-panel {
  padding-top: 16px;
}

.user-table,
.scope-list-panel {
  margin-top: 16px;
}

.user-name-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name-cell span {
  color: #64748b;
  font-size: 12px;
}

:deep(.user-row-selected > td) {
  background: #eff6ff;
}

.scope-list-panel {
  padding-top: 16px;
  border-top: 1px solid #eef2f7;
}

.scope-list-section {
  min-width: 0;
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

.scope-name-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.scope-name-cell span {
  color: #64748b;
  font-size: 12px;
}

.scope-edit-table {
  overflow-x: auto;
}

.scope-type-select,
.scope-ref-control {
  width: 100%;
}

.scope-list-footer {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-top: 14px;
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f8fafc;
}

.scope-save-preview {
  min-width: 0;
}

.preview-label {
  display: block;
  margin-bottom: 4px;
  color: #475569;
  font-size: 13px;
  font-weight: 600;
}

.preview-empty,
.preview-text {
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
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
  .query-panel {
    display: grid;
    grid-template-columns: 1fr;
  }

  .summary-strip {
    min-width: 0;
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

  .scope-list-footer,
  .action-bar {
    flex-direction: column;
  }

  .scope-list-footer,
  .action-bar,
  .action-bar .ant-btn {
    width: 100%;
  }
}
</style>
