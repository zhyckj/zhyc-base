<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="done-page">
    <a-card title="流程已办" :bordered="false">
      <template #extra>
        <a-button :loading="status === 'loading'" @click="loadTasks">刷新</a-button>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-empty v-if="status === 'success' && tasks.length === 0" description="暂无已办任务" />

      <a-table
        v-else
        row-key="taskId"
        :columns="columns"
        :data-source="tasks"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
      />

      <div class="permission-code">workflow:task:done</div>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { listDoneTasks, type WorkflowTaskTodoItem } from '@/api/workflow/task';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId, requireAdminUserId } from '@/utils/adminContext';

/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 异常提示文案。 */
const errorMessage = ref('');
/** 当前用户已办任务。 */
const tasks = ref<WorkflowTaskTodoItem[]>([]);

/** 表格列定义。 */
const columns = [
  { title: '任务名称', dataIndex: 'taskName', key: 'taskName' },
  { title: '业务单号', dataIndex: 'businessKey', key: 'businessKey' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt' },
];

/**
 * 加载当前用户已办任务。
 */
async function loadTasks(): Promise<void> {
  status.value = 'loading';
  try {
    const adminContext = getAdminRuntimeContext();
    requireAdminTenantId(adminContext);
    requireAdminUserId(adminContext);
    tasks.value = await listDoneTasks();
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '已办任务加载失败';
    status.value = 'error';
  }
}

onMounted(() => {
  void loadTasks();
});
</script>

<style scoped>
.done-page {
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
