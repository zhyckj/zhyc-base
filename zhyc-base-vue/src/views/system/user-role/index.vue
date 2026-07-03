<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="user-role-page">
    <a-card title="用户角色绑定" :bordered="false">
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
            <span>已绑定角色</span>
            <strong>{{ roles.length }}</strong>
          </div>
          <div class="summary-item">
            <span>可选角色</span>
            <strong>{{ allRoles.length }}</strong>
          </div>
          <div class="summary-item">
            <span>禁用角色</span>
            <strong>{{ disabledRoleCount }}</strong>
          </div>
        </div>
      </div>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <section class="user-list-section">
        <div class="section-header">
          <div>
            <h3>用户列表</h3>
            <p>先查询当前租户全部用户，再选择用户维护角色绑定关系。</p>
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
              <a-button type="link" v-permission="'system:user:edit'" @click="openUserRoleEditor(record)">
                编辑角色
              </a-button>
            </template>
          </template>
        </a-table>
      </section>

      <a-empty
        v-if="!userId"
        description="请先在用户列表中点击编辑角色"
        class="page-empty"
      />

      <div v-else class="binding-layout">
        <section class="bound-section">
          <div class="section-header">
            <div>
              <h3>当前已绑定角色</h3>
              <p>{{ selectedUserLabel }}</p>
            </div>
            <a-tag :color="roles.length ? 'blue' : 'default'">
              {{ roles.length ? `${roles.length} 个角色` : '未绑定' }}
            </a-tag>
          </div>

          <a-table
            row-key="roleId"
            :columns="roleColumns"
            :data-source="roles"
            :loading="status === 'loading'"
            :pagination="$tablePagination"
            size="middle"
          >
            <template #emptyText>
              <a-empty description="该用户暂未绑定角色" />
            </template>
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'roleName'">
                <div class="role-name-cell">
                  <strong>{{ record.roleName }}</strong>
                  <span>{{ record.roleCode }}</span>
                </div>
              </template>
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
                  {{ $statusLabel(record.status) }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </section>

        <section class="editor-section">
          <div class="section-header">
            <div>
              <h3>调整角色绑定</h3>
              <p>角色决定菜单按钮权限，保存前请确认没有包含已停用角色。</p>
            </div>
          </div>

          <a-form layout="vertical" class="bind-form">
            <a-form-item label="绑定角色">
              <a-select
                v-model:value="selectedRoleIds"
                mode="multiple"
                allow-clear
                show-search
                option-filter-prop="label"
                :loading="optionsLoading"
                :max-tag-count="4"
                :options="roleOptions"
                placeholder="请选择要绑定的角色"
              />
            </a-form-item>
          </a-form>

          <div class="selection-preview">
            <span class="preview-label">保存后效果</span>
            <a-empty v-if="selectedRoleDetails.length === 0" description="未选择角色" />
            <div v-else class="role-tags">
              <a-tag
                v-for="role in selectedRoleDetails"
                :key="role.id"
                :color="role.status === 'enabled' ? 'blue' : 'default'"
              >
                {{ role.name }}（{{ role.roleCode }}）{{ role.status === 'enabled' ? '' : ' · 已停用' }}
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
              @click="submitUserRoles"
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

import { listSystemRoles, type SystemRole } from '@/api/system/role';
import { listSystemUsers, type SystemUser } from '@/api/system/user';
import { bindSystemUserRoles, listSystemUserRoles, type SystemUserRole } from '@/api/system/user-role';
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
/** 已绑定角色列表。 */
const roles = ref<SystemUserRole[]>([]);
/** 用户下拉数据。 */
const users = ref<SystemUser[]>([]);
/** 角色下拉数据。 */
const allRoles = ref<SystemRole[]>([]);
/** 已选择的角色主键列表。 */
const selectedRoleIds = ref<number[]>([]);

/** 用户列表表格列定义。 */
const userColumns = [
  { title: '用户信息', dataIndex: 'nickname', key: 'userInfo' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 120 },
];

/** 已绑定角色表格列定义。 */
const roleColumns = [
  { title: '角色信息', dataIndex: 'roleName', key: 'roleName' },
  { title: '数据权限', dataIndex: 'dataScope', key: 'dataScope', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
];

/** 当前选中用户展示名称。 */
const selectedUserLabel = computed(() => {
  const currentUser = users.value.find((user) => user.id === userId.value);
  return currentUser ? `${currentUser.nickname}（${currentUser.username}）` : '未选择用户';
});

/** 角色选择项。 */
const roleOptions = computed(() =>
  allRoles.value.map((role) => ({
    label: `${role.name}（${role.roleCode}）${role.status === 'enabled' ? '' : ' · 已停用'}`,
    value: role.id,
  })),
);

/** 已选择角色详情，用于保存前预览。 */
const selectedRoleDetails = computed(() =>
  selectedRoleIds.value
    .map((roleId) => allRoles.value.find((role) => role.id === roleId))
    .filter((role): role is SystemRole => Boolean(role)),
);

/** 可选角色中已停用的数量。 */
const disabledRoleCount = computed(() => allRoles.value.filter((role) => role.status !== 'enabled').length);

/**
 * 加载用户角色列表。
 */
async function loadUserRoles(): Promise<void> {
  if (!userId.value) {
    message.warning('请先选择用户');
    return;
  }
  status.value = 'loading';
  errorMessage.value = '';
  try {
    const currentTenantId = syncTenantContext();
    roles.value = await listSystemUserRoles(currentTenantId, userId.value);
    resetSelectionFromCurrent();
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '用户角色加载失败';
    status.value = 'error';
  }
}

/**
 * 打开用户角色编辑区。
 *
 * @param user 当前待维护角色的系统用户
 */
async function openUserRoleEditor(user: SystemUser): Promise<void> {
  if (userId.value !== user.id) {
    handleUserChange();
  }
  userId.value = user.id;
  await loadUserRoles();
}

/**
 * 提交用户角色绑定。
 */
async function submitUserRoles(): Promise<void> {
  if (!userId.value) {
    message.warning('请先选择用户');
    return;
  }
  saving.value = true;
  try {
    const currentTenantId = syncTenantContext();
    await bindSystemUserRoles(currentTenantId, userId.value, {
      roleIds: selectedRoleIds.value,
    });
    await loadUserRoles();
    message.success('用户角色已保存');
  } catch (error) {
    message.error(error instanceof Error ? error.message : '用户角色保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 加载用户和角色候选项。
 */
async function loadOptions(): Promise<void> {
  optionsLoading.value = true;
  try {
    const currentTenantId = syncTenantContext();
    const [loadedUsers, loadedRoles] = await Promise.all([
      listSystemUsers(currentTenantId),
      listSystemRoles(currentTenantId),
    ]);
    users.value = loadedUsers;
    allRoles.value = loadedRoles;
    if (userId.value && !loadedUsers.some((user) => user.id === userId.value)) {
      userId.value = undefined;
      handleUserChange();
    }
  } catch (error) {
    message.error(error instanceof Error ? error.message : '用户角色候选项加载失败');
  } finally {
    optionsLoading.value = false;
  }
}

/**
 * 切换用户时清理旧的角色选择，避免误保存到新用户。
 */
function handleUserChange(): void {
  roles.value = [];
  selectedRoleIds.value = [];
  status.value = 'idle';
  errorMessage.value = '';
}

/**
 * 从当前已绑定角色恢复编辑区选择。
 */
function resetSelectionFromCurrent(): void {
  selectedRoleIds.value = roles.value.map((role) => role.roleId);
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
.user-role-page {
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

.role-name-cell {
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
.role-name-cell span {
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

.role-tags {
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
