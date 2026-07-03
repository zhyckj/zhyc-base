<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="role-data-scope-page">
    <a-card title="角色数据权限配置" :bordered="false">
      <div class="query-panel">
        <a-form layout="inline" class="query-form">
          <a-form-item label="当前租户">
            <a-input v-model:value="tenantId" disabled class="tenant-id" />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button :loading="optionsLoading" @click="loadOptions">
                <template #icon><ReloadOutlined /></template>
                查询角色
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>

        <div class="summary-strip">
          <div class="summary-item">
            <span>角色总数</span>
            <strong>{{ roles.length }}</strong>
          </div>
          <div class="summary-item">
            <span>组织候选</span>
            <strong>{{ orgOptions.length }}</strong>
          </div>
          <div class="summary-item">
            <span>已授权组织</span>
            <strong>{{ scopes.length }}</strong>
          </div>
          <div class="summary-item">
            <span>当前角色</span>
            <strong>{{ selectedRoleLabel }}</strong>
          </div>
        </div>
      </div>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <section class="role-list-section">
        <div class="section-header">
          <div>
            <h3>角色列表</h3>
            <p>先查询当前租户全部角色，再选择角色维护数据权限组织范围。</p>
          </div>
          <a-tag :color="roles.length ? 'blue' : 'default'">
            {{ roles.length ? `${roles.length} 个角色` : '无角色' }}
          </a-tag>
        </div>

        <a-table
          row-key="id"
          :columns="roleColumns"
          :data-source="roles"
          :loading="optionsLoading"
          :pagination="$tablePagination"
          size="middle"
        >
          <template #emptyText>
            <a-empty description="当前租户暂无角色" />
          </template>
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'roleInfo'">
              <div class="role-name-cell">
                <strong>{{ record.name }}</strong>
                <span>{{ record.roleCode }}</span>
              </div>
            </template>
            <template v-if="column.key === 'dataScope'">
              <a-tag>{{ dataScopeLabel(record.dataScope) }}</a-tag>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
                {{ $statusLabel(record.status) }}
              </a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a-button type="link" v-permission="'system:role:edit'" @click="openRoleDataScopeEditor(record)">
                配置数据权限
              </a-button>
            </template>
          </template>
        </a-table>
      </section>

      <a-empty
        v-if="!roleId"
        description="请先在角色列表中点击配置数据权限"
        class="page-empty"
      />

      <div v-else class="binding-layout">
        <section class="bound-section">
          <div class="section-header">
            <div>
              <h3>当前授权组织</h3>
              <p>{{ selectedRoleLabel }}</p>
            </div>
            <a-tag :color="scopes.length ? 'blue' : 'default'">
              {{ scopes.length ? `${scopes.length} 个组织` : '未授权' }}
            </a-tag>
          </div>

          <a-table
            row-key="orgId"
            :columns="scopeColumns"
            :data-source="scopes"
            :loading="status === 'loading'"
            :pagination="$tablePagination"
            size="middle"
          >
            <template #emptyText>
              <a-empty description="该角色暂未配置自定义数据权限组织" />
            </template>
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'orgName'">
                <div class="org-name-cell">
                  <strong>{{ record.orgName }}</strong>
                  <span>{{ resolveOrgCode(record.orgId) }}</span>
                </div>
              </template>
              <template v-if="column.key === 'scopeType'">
                <a-tag>{{ scopeTypeLabel(record.scopeType) }}</a-tag>
              </template>
            </template>
          </a-table>
        </section>

        <section class="editor-section">
          <div class="section-header">
            <div>
              <h3>调整授权组织</h3>
              <p>这里维护角色的数据权限组织范围，保存后覆盖当前配置。</p>
            </div>
          </div>

          <a-form layout="vertical" class="bind-form">
            <a-form-item label="授权组织">
              <a-select
                v-model:value="selectedOrgIds"
                mode="multiple"
                allow-clear
                show-search
                option-filter-prop="label"
                :loading="optionsLoading"
                :max-tag-count="4"
                :options="orgOptions"
                placeholder="请选择授权组织"
              />
            </a-form-item>
          </a-form>

          <div class="selection-preview">
            <span class="preview-label">保存后效果</span>
            <a-empty v-if="selectedOrgDetails.length === 0" description="未选择组织" />
            <div v-else class="org-tags">
              <a-tag v-for="org in selectedOrgDetails" :key="org.value" color="blue">
                {{ org.orgName }}（{{ org.orgCode }}）
              </a-tag>
            </div>
          </div>

          <div class="action-bar">
            <a-button :disabled="status === 'loading' || saving" @click="resetSelectionFromCurrent">恢复当前授权</a-button>
            <a-button
              type="primary"
              v-permission="'system:role:edit'"
              :disabled="!roleId"
              :loading="saving"
              @click="submitScopes"
            >
              <template #icon><SaveOutlined /></template>
              保存授权
            </a-button>
          </div>
        </section>
      </div>

      <span class="permission-code">system:role:edit</span>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { ReloadOutlined, SaveOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { computed, onMounted, ref } from 'vue';

import { listSystemOrgTree, type SystemOrgTreeNode } from '@/api/system/org';
import { listSystemRoles, type SystemRole } from '@/api/system/role';
import {
  bindSystemRoleDataScopes,
  listSystemRoleDataScopes,
  type SystemRoleDataScope,
} from '@/api/system/role-data-scope';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

interface OrgSelectOption {
  /** 下拉展示名称。 */
  label: string;
  /** 组织主键。 */
  value: number;
  /** 组织编码。 */
  orgCode: string;
  /** 组织名称。 */
  orgName: string;
}

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 当前角色主键。 */
const roleId = ref<number>();
/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 候选项加载状态。 */
const optionsLoading = ref(false);
/** 保存按钮加载状态。 */
const saving = ref(false);
/** 异常提示文案。 */
const errorMessage = ref('');
/** 已绑定数据权限范围。 */
const scopes = ref<SystemRoleDataScope[]>([]);
/** 角色候选项。 */
const roles = ref<SystemRole[]>([]);
/** 组织候选树。 */
const orgTree = ref<SystemOrgTreeNode[]>([]);
/** 已选择的组织主键列表。 */
const selectedOrgIds = ref<number[]>([]);

/** 角色列表表格列定义。 */
const roleColumns = [
  { title: '角色信息', dataIndex: 'name', key: 'roleInfo' },
  { title: '数据权限', dataIndex: 'dataScope', key: 'dataScope', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 150 },
];

/** 已授权组织表格列定义。 */
const scopeColumns = [
  { title: '组织信息', dataIndex: 'orgName', key: 'orgName' },
  { title: '范围类型', dataIndex: 'scopeType', key: 'scopeType', width: 120 },
];

/** 当前选中角色展示名称。 */
const selectedRoleLabel = computed(() => {
  const currentRole = roles.value.find((role) => role.id === roleId.value);
  return currentRole ? `${currentRole.name}（${currentRole.roleCode}）` : '未选择角色';
});

/** 组织选择项。 */
const orgOptions = computed(() => flattenOrgOptions(orgTree.value));

/** 已选择组织详情，用于保存前预览。 */
const selectedOrgDetails = computed(() =>
  selectedOrgIds.value
    .map((orgId) => orgOptions.value.find((org) => org.value === orgId))
    .filter((org): org is OrgSelectOption => Boolean(org)),
);

/**
 * 加载角色数据权限范围。
 */
async function loadScopes(): Promise<void> {
  if (!roleId.value) {
    message.warning('请先选择角色');
    return;
  }
  status.value = 'loading';
  errorMessage.value = '';
  try {
    const currentTenantId = syncTenantContext();
    scopes.value = await listSystemRoleDataScopes(currentTenantId, roleId.value);
    resetSelectionFromCurrent();
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '角色数据权限加载失败';
    status.value = 'error';
  }
}

/**
 * 打开角色数据权限配置区。
 *
 * @param role 当前待维护数据权限的系统角色
 */
async function openRoleDataScopeEditor(role: SystemRole): Promise<void> {
  if (roleId.value !== role.id) {
    handleRoleChange();
  }
  roleId.value = role.id;
  await loadScopes();
}

/**
 * 提交角色数据权限范围。
 */
async function submitScopes(): Promise<void> {
  if (!roleId.value) {
    message.warning('请先选择角色');
    return;
  }
  saving.value = true;
  try {
    const currentTenantId = syncTenantContext();
    await bindSystemRoleDataScopes(currentTenantId, roleId.value, {
      orgIds: selectedOrgIds.value,
    });
    await loadScopes();
    message.success('角色数据权限已保存');
  } catch (error) {
    message.error(error instanceof Error ? error.message : '角色数据权限保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 加载角色和组织候选项。
 */
async function loadOptions(): Promise<void> {
  optionsLoading.value = true;
  try {
    const currentTenantId = syncTenantContext();
    const [loadedRoles, loadedOrgTree] = await Promise.all([
      listSystemRoles(currentTenantId),
      listSystemOrgTree(currentTenantId),
    ]);
    roles.value = loadedRoles;
    orgTree.value = loadedOrgTree;
    if (roleId.value && !loadedRoles.some((role) => role.id === roleId.value)) {
      roleId.value = undefined;
      handleRoleChange();
    }
  } catch (error) {
    message.error(error instanceof Error ? error.message : '数据权限候选项加载失败');
  } finally {
    optionsLoading.value = false;
  }
}

/**
 * 切换角色时清理旧组织选择，避免误保存到新角色。
 */
function handleRoleChange(): void {
  scopes.value = [];
  selectedOrgIds.value = [];
  status.value = 'idle';
  errorMessage.value = '';
}

/**
 * 从当前已授权组织恢复编辑区。
 */
function resetSelectionFromCurrent(): void {
  selectedOrgIds.value = scopes.value.map((scope) => scope.orgId);
}

/**
 * 获取角色数据权限类型中文说明。
 *
 * @param dataScope 数据权限范围
 * @returns 中文说明
 */
function dataScopeLabel(dataScope?: string): string {
  const dataScopeMap: Record<string, string> = {
    ALL: '全部数据',
    DEPT: '本部门',
    DEPT_AND_CHILD: '本部门及下级',
    SELF: '仅本人',
    CUSTOM: '自定义',
  };
  return dataScope ? dataScopeMap[dataScope] ?? dataScope : '未设置';
}

/**
 * 获取角色数据权限范围类型中文说明。
 *
 * @param scopeType 范围类型
 * @returns 中文说明
 */
function scopeTypeLabel(scopeType: string): string {
  return scopeType === 'org' ? '组织' : scopeType;
}

/**
 * 解析组织编码。
 *
 * @param orgId 组织主键
 * @returns 组织编码
 */
function resolveOrgCode(orgId: number): string {
  return orgOptions.value.find((org) => org.value === orgId)?.orgCode ?? `ID:${orgId}`;
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
function flattenOrgOptions(nodes: SystemOrgTreeNode[], level = 0): OrgSelectOption[] {
  return nodes.flatMap((node) => [
    {
      label: `${'　'.repeat(level)}${node.orgName}（${node.orgCode}）`,
      value: node.id,
      orgCode: node.orgCode,
      orgName: node.orgName,
    },
    ...flattenOrgOptions(node.children ?? [], level + 1),
  ]);
}

onMounted(() => {
  void loadOptions();
});
</script>

<style scoped>
.role-data-scope-page {
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

.role-select {
  width: 300px;
}

.summary-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(96px, 1fr));
  min-width: 520px;
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

.role-list-section {
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

.org-name-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.role-name-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.role-name-cell span,
.org-name-cell span {
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

.org-tags {
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
