<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="cc-page">
    <a-card title="抄送我的" :bordered="false">
      <template #extra>
        <a-button :loading="status === 'loading'" @click="loadCcTasks">刷新</a-button>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-empty v-if="status === 'success' && ccTasks.length === 0" description="暂无抄送任务" />

      <a-table
        v-else
        row-key="ccRecordId"
        :columns="columns"
        :data-source="ccTasks"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'readFlag'">
            <a-tag :color="record.readFlag === 1 ? 'green' : 'blue'">
              {{ record.readFlag === 1 ? '已读' : '未读' }}
            </a-tag>
          </template>
        </template>
      </a-table>

      <div class="permission-code">workflow:task:cc</div>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { listCcTasks, type WorkflowCcTaskItem } from '@/api/workflow/task';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId, requireAdminUserId } from '@/utils/adminContext';

/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 异常提示文案。 */
const errorMessage = ref('');
/** 当前用户收到的抄送任务列表。 */
const ccTasks = ref<WorkflowCcTaskItem[]>([]);

/** 抄送任务表格列定义。 */
const columns = [
  { title: '流程定义', dataIndex: 'processKey', key: 'processKey' },
  { title: '业务单号', dataIndex: 'businessKey', key: 'businessKey' },
  { title: '流程实例', dataIndex: 'processInstanceId', key: 'processInstanceId' },
  { title: '阅读状态', dataIndex: 'readFlag', key: 'readFlag', width: 120 },
  { title: '抄送时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
];

/**
 * 加载当前用户收到的抄送任务。
 */
async function loadCcTasks(): Promise<void> {
  status.value = 'loading';
  try {
    const adminContext = getAdminRuntimeContext();
    requireAdminTenantId(adminContext);
    requireAdminUserId(adminContext);
    ccTasks.value = await listCcTasks();
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '抄送任务加载失败';
    status.value = 'error';
  }
}

onMounted(() => {
  void loadCcTasks();
});
</script>

<style scoped>
.cc-page {
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
