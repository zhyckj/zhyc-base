<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="login-log-page">
    <a-card title="登录日志" :bordered="false">
      <template #extra>
        <a-space>
          <a-input v-model:value="tenantId" class="tenant-id" />
          <a-input-number v-model:value="limit" :min="1" :max="200" class="limit-input" />
          <a-button :loading="status === 'loading'" @click="loadLogs">查询</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="logs"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'result'">
            <a-tag :color="record.result === 'success' ? 'green' : 'red'">{{ record.result }}</a-tag>
          </template>
        </template>
      </a-table>

      <span class="permission-code">system:audit:query</span>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { listSystemLoginLogs, type SystemLoginLog } from '@/api/system/login-log';
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
/** 登录日志列表。 */
const logs = ref<SystemLoginLog[]>([]);

/** 表格列定义。 */
const columns = [
  { title: '时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
  { title: '账号', dataIndex: 'username', key: 'username', width: 130 },
  { title: '方式', dataIndex: 'loginType', key: 'loginType', width: 100 },
  { title: '结果', dataIndex: 'result', key: 'result', width: 100 },
  { title: 'IP', dataIndex: 'clientIp', key: 'clientIp', width: 130 },
  { title: 'User-Agent', dataIndex: 'userAgent', key: 'userAgent' },
];

/**
 * 加载登录日志列表。
 */
async function loadLogs(): Promise<void> {
  status.value = 'loading';
  try {
    logs.value = await listSystemLoginLogs(syncTenantContext(), limit.value);
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录日志加载失败';
    status.value = 'error';
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

onMounted(() => {
  void loadLogs();
});
</script>

<style scoped>
.login-log-page {
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
