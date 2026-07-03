<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="openapi-error-log-page">
    <a-card title="错误日志" :bordered="false">
      <template #extra>
        <a-space>
          <a-input v-model:value="tenantId" class="tenant-id" />
          <a-input v-model:value="appCode" class="app-code" placeholder="应用编码" />
          <a-button :loading="loading" @click="loadErrorLogs">查询</a-button>
        </a-space>
      </template>

      <a-table
        row-key="requestId"
        size="small"
        :columns="columns"
        :data-source="errorLogs"
        :loading="loading"
        :pagination="$tablePagination"
        :scroll="{ x: 1180 }"
      />

      <span class="permission-code">openapi:error-log:query</span>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue';

import { listOpenApiErrorLogs, type OpenApiCallAuditResponse } from '@/api/openapi/call-audit';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 当前开发者应用编码。 */
const appCode = ref('');
/** 开放 API 错误日志列表。 */
const errorLogs = ref<OpenApiCallAuditResponse[]>([]);
/** 列表加载状态。 */
const loading = ref(false);

/** 错误日志权限编码，供页面和后续权限指令统一引用。 */
const permissionCodes = {
  /** 查询错误日志权限。 */
  query: 'openapi:error-log:query',
};

/** 错误日志表格列定义。 */
const columns = [
  { title: 'API 编码', dataIndex: 'apiCode', key: 'apiCode', width: 180 },
  { title: '方法', dataIndex: 'httpMethod', key: 'httpMethod', width: 90 },
  { title: '请求路径', dataIndex: 'requestPath', key: 'requestPath', width: 260 },
  { title: '状态码', dataIndex: 'responseStatus', key: 'responseStatus', width: 100 },
  { title: '错误码', dataIndex: 'errorCode', key: 'errorCode', width: 160 },
  { title: '客户端 IP', dataIndex: 'clientIp', key: 'clientIp', width: 140 },
  { title: '追踪 ID', dataIndex: 'requestId', key: 'requestId', width: 180 },
  { title: '调用时间', dataIndex: 'calledAt', key: 'calledAt', width: 180 },
];

/**
 * 查询当前应用的开放 API 错误日志。
 */
async function loadErrorLogs(): Promise<void> {
  if (!appCode.value) {
    return;
  }
  loading.value = true;
  try {
    errorLogs.value = await listOpenApiErrorLogs(syncTenantContext(), appCode.value);
  } finally {
    loading.value = false;
  }
}

/**
 * 同步后台租户到错误日志查询条件。
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
.openapi-error-log-page {
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
