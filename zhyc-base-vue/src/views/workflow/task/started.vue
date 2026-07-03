<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="started-page">
    <a-card title="我发起的" :bordered="false">
      <template #extra>
        <a-button :loading="status === 'loading'" @click="loadProcesses">刷新</a-button>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-empty v-if="status === 'success' && processes.length === 0" description="暂无我发起的流程" />

      <a-table
        v-else
        row-key="processInstanceId"
        :columns="columns"
        :data-source="processes"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
      />

      <div class="permission-code">workflow:task:started</div>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { listStartedProcesses, type WorkflowStartedProcessItem } from '@/api/workflow/task';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId, requireAdminUserId } from '@/utils/adminContext';

/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 异常提示文案。 */
const errorMessage = ref('');
/** 当前用户发起的流程列表。 */
const processes = ref<WorkflowStartedProcessItem[]>([]);

/** 我发起的流程表格列定义。 */
const columns = [
  { title: '流程定义', dataIndex: 'processKey', key: 'processKey' },
  { title: '业务单号', dataIndex: 'businessKey', key: 'businessKey' },
  { title: '流程实例', dataIndex: 'processInstanceId', key: 'processInstanceId' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '发起时间', dataIndex: 'startedAt', key: 'startedAt', width: 180 },
];

/**
 * 加载当前用户发起的流程实例。
 */
async function loadProcesses(): Promise<void> {
  status.value = 'loading';
  try {
    const adminContext = getAdminRuntimeContext();
    requireAdminTenantId(adminContext);
    requireAdminUserId(adminContext);
    processes.value = await listStartedProcesses();
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '我发起的流程加载失败';
    status.value = 'error';
  }
}

onMounted(() => {
  void loadProcesses();
});
</script>

<style scoped>
.started-page {
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
