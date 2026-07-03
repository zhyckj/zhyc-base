<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="permission-audit-page">
    <a-card title="权限审计" :bordered="false">
      <template #extra>
        <a-space>
          <a-input v-model:value="tenantId" class="tenant-id" />
          <a-input-number v-model:value="limit" :min="1" :max="200" class="limit-input" />
          <a-button :loading="status === 'loading'" @click="loadAudits">查询</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="audits"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'operator'">
            {{ formatOperatorName(record.operatorId) }}
          </template>
          <template v-else-if="column.key === 'target'">
            {{ record.targetType }} / {{ record.targetId }}
          </template>
          <template v-else-if="column.key === 'changeType'">
            <a-tag>{{ record.changeType }}</a-tag>
          </template>
        </template>
      </a-table>

      <span class="permission-code">system:audit:query</span>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { listSystemPermissionAudits, type SystemPermissionAudit } from '@/api/system/permission-audit';
import { listSystemUsers, type SystemUser } from '@/api/system/user';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 查询条数上限。 */
const limit = ref(50);
/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 异常提示文案。 */
const errorMessage = ref('');
/** 权限变更审计列表。 */
const audits = ref<SystemPermissionAudit[]>([]);
/** 操作人候选用户。 */
const users = ref<SystemUser[]>([]);

/** 表格列定义。 */
const columns = [
  { title: '时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
  { title: '操作者', key: 'operator', width: 180 },
  { title: '目标', key: 'target', width: 220 },
  { title: '变更类型', dataIndex: 'changeType', key: 'changeType', width: 120 },
  { title: '变更前', dataIndex: 'beforeValue', key: 'beforeValue' },
  { title: '变更后', dataIndex: 'afterValue', key: 'afterValue' },
];

/**
 * 加载权限变更审计列表。
 */
async function loadAudits(): Promise<void> {
  status.value = 'loading';
  try {
    const currentTenantId = syncTenantContext();
    audits.value = await listSystemPermissionAudits(currentTenantId, limit.value);
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '权限审计加载失败';
    status.value = 'error';
  }
}

/**
 * 加载操作人候选用户。
 */
async function loadUsers(): Promise<void> {
  try {
    users.value = await listSystemUsers(syncTenantContext());
  } catch {
    users.value = [];
  }
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
 * 格式化操作人展示名称。
 *
 * @param operatorId 操作人用户主键
 * @returns 操作人业务名称
 */
function formatOperatorName(operatorId?: number): string {
  if (!operatorId) {
    return '系统';
  }
  const user = users.value.find((item) => item.id === operatorId);
  return user ? `${user.nickname}（${user.username}）` : '未知用户';
}

onMounted(() => {
  void loadAudits();
  void loadUsers();
});
</script>

<style scoped>
.permission-audit-page {
  min-width: 0;
}

.tenant-id {
  width: 180px;
}

.limit-input {
  width: 96px;
}

.state-alert {
  margin-bottom: 12px;
}

.permission-code {
  display: none;
}
</style>
