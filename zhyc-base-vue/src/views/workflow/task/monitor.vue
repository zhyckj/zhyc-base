<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="monitor-page">
    <a-card title="流程监控" :bordered="false">
      <template #extra>
        <a-button :loading="status === 'loading'" @click="loadProcesses">刷新</a-button>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-empty v-if="status === 'success' && processes.length === 0" description="暂无流程实例" />

      <a-table
        v-else
        row-key="processInstanceId"
        :columns="columns"
        :data-source="processes"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="resolveStatusColor(record.status)">
              {{ $statusLabel(record.status) }}
            </a-tag>
          </template>
        </template>
      </a-table>

      <div class="permission-code">workflow:task:monitor</div>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { listMonitoredProcesses, type WorkflowProcessMonitorItem } from '@/api/workflow/task';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 异常提示文案。 */
const errorMessage = ref('');
/** 租户下流程实例监控列表。 */
const processes = ref<WorkflowProcessMonitorItem[]>([]);

/** 流程监控表格列定义。 */
const columns = [
  { title: '流程定义', dataIndex: 'processKey', key: 'processKey' },
  { title: '业务单号', dataIndex: 'businessKey', key: 'businessKey' },
  { title: '流程实例', dataIndex: 'processInstanceId', key: 'processInstanceId' },
  { title: '发起人', dataIndex: 'starterUserId', key: 'starterUserId', width: 110 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '发起时间', dataIndex: 'startedAt', key: 'startedAt', width: 180 },
  { title: '结束时间', dataIndex: 'endedAt', key: 'endedAt', width: 180 },
];

/**
 * 加载当前租户下的流程实例监控列表。
 */
async function loadProcesses(): Promise<void> {
  status.value = 'loading';
  try {
    const adminContext = getAdminRuntimeContext();
    requireAdminTenantId(adminContext);
    processes.value = await listMonitoredProcesses();
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '流程监控加载失败';
    status.value = 'error';
  }
}

/**
 * 解析流程状态对应的标签颜色。
 *
 * @param runtimeStatus 流程实例状态编码
 * @returns Ant Design Vue 标签颜色
 */
function resolveStatusColor(runtimeStatus: string): string {
  if (runtimeStatus === 'RUNNING' || runtimeStatus === 'TODO') {
    return 'blue';
  }
  if (runtimeStatus === 'APPROVED') {
    return 'green';
  }
  if (runtimeStatus === 'REJECTED' || runtimeStatus === 'REVOKED') {
    return 'red';
  }
  return 'default';
}

onMounted(() => {
  void loadProcesses();
});
</script>

<style scoped>
.monitor-page {
  min-width: 0;
}

.state-alert {
  margin-bottom: 12px;
}

.permission-code {
  color: #6b7280;
  font-size: 12px;
  margin-top: 12px;
}
</style>
