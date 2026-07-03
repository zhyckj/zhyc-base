<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="openapi-call-audit-page">
    <a-card title="调用审计" :bordered="false">
      <template #extra>
        <a-space>
          <a-input v-model:value="tenantId" class="tenant-id" />
          <a-input v-model:value="appCode" class="app-code" placeholder="应用编码" />
          <a-button :loading="loading" @click="loadAudits">查询</a-button>
        </a-space>
      </template>

      <a-table
        row-key="requestId"
        size="small"
        :columns="columns"
        :data-source="audits"
        :loading="loading"
        :pagination="$tablePagination"
        :scroll="{ x: 1280 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'success'">
            <a-tag :color="record.success === 1 ? 'green' : 'red'">
              {{ record.success === 1 ? '成功' : '失败' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'durationMs'">
            {{ record.durationMs }} ms
          </template>
        </template>
      </a-table>

      <span class="permission-code">openapi:call-audit:query</span>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue';

import { listOpenApiCallAudits, type OpenApiCallAuditResponse } from '@/api/openapi/call-audit';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 当前开发者应用编码。 */
const appCode = ref('');
/** 调用审计列表。 */
const audits = ref<OpenApiCallAuditResponse[]>([]);
/** 列表加载状态。 */
const loading = ref(false);

/** 调用审计权限编码，供页面和后续权限指令统一引用。 */
const permissionCodes = {
  /** 查询调用审计权限。 */
  query: 'openapi:call-audit:query',
};

/** 调用审计表格列定义。 */
const columns = [
  { title: 'API 编码', dataIndex: 'apiCode', key: 'apiCode', width: 180 },
  { title: '方法', dataIndex: 'httpMethod', key: 'httpMethod', width: 90 },
  { title: '请求路径', dataIndex: 'requestPath', key: 'requestPath', width: 260 },
  { title: '状态码', dataIndex: 'responseStatus', key: 'responseStatus', width: 100 },
  { title: '结果', dataIndex: 'success', key: 'success', width: 90 },
  { title: '耗时', dataIndex: 'durationMs', key: 'durationMs', width: 100 },
  { title: '错误码', dataIndex: 'errorCode', key: 'errorCode', width: 140 },
  { title: '客户端 IP', dataIndex: 'clientIp', key: 'clientIp', width: 140 },
  { title: '追踪 ID', dataIndex: 'requestId', key: 'requestId', width: 180 },
  { title: '调用时间', dataIndex: 'calledAt', key: 'calledAt', width: 180 },
];

/**
 * 查询当前应用的开放 API 调用审计。
 */
async function loadAudits(): Promise<void> {
  if (!appCode.value) {
    return;
  }
  loading.value = true;
  try {
    audits.value = await listOpenApiCallAudits(syncTenantContext(), appCode.value);
  } finally {
    loading.value = false;
  }
}

/**
 * 同步后台租户到调用审计查询条件。
 *
 * @returns 当前租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId();
  tenantId.value = currentTenantId;
  return currentTenantId;
}
</script>

<style scoped>
.openapi-call-audit-page {
  min-width: 0;
}

.tenant-id {
  width: 160px;
}

.app-code {
  width: 180px;
}

.permission-code {
  display: none;
}
</style>
