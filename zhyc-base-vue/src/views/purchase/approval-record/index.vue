<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="approval-record-page">
    <a-card title="采购审批记录" :bordered="false">
      <a-form layout="inline" class="query-form" @submit.prevent>
        <a-form-item label="任务 ID" required>
          <a-input v-model:value="taskId" class="task-input" placeholder="请输入采购审批任务 ID" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" :loading="status === 'loading'" @click="loadApprovalRecords">查询</a-button>
        </a-form-item>
      </a-form>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-descriptions v-if="detail" bordered size="small" class="detail-panel">
        <a-descriptions-item label="流程定义">{{ detail.processKey }}</a-descriptions-item>
        <a-descriptions-item label="业务单号">{{ detail.businessKey }}</a-descriptions-item>
        <a-descriptions-item label="任务状态">{{ $statusLabel(detail.status) }}</a-descriptions-item>
      </a-descriptions>

      <a-table
        row-key="operatedAt"
        :columns="columns"
        :data-source="detail?.approvalRecords ?? []"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
        size="small"
      />

      <div class="permission-code">purchase:approval:query</div>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue';

import { getTaskDetail, type WorkflowApprovalRecordItem, type WorkflowTaskDetailResponse } from '@/api/workflow/task';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId, requireAdminUserId } from '@/utils/adminContext';

/** 当前查询的采购审批任务 ID。 */
const taskId = ref('');
/** 采购审批任务详情，包含审批记录列表。 */
const detail = ref<WorkflowTaskDetailResponse>();
/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 查询失败提示。 */
const errorMessage = ref('');

/** 采购审批记录表格列。 */
const columns = [
  { title: '任务 ID', dataIndex: 'taskId', key: 'taskId' },
  { title: '操作人', dataIndex: 'operatorUserId', key: 'operatorUserId', width: 120 },
  { title: '动作', dataIndex: 'action', key: 'action', width: 120 },
  { title: '审批意见', dataIndex: 'approvalComment', key: 'approvalComment' },
  { title: '操作时间', dataIndex: 'operatedAt', key: 'operatedAt', width: 180 },
] as const satisfies ReadonlyArray<{
  title: string;
  dataIndex: keyof WorkflowApprovalRecordItem;
  key: string;
  width?: number;
}>;

/**
 * 加载采购审批记录。
 */
async function loadApprovalRecords(): Promise<void> {
  const normalizedTaskId = taskId.value.trim();
  if (!normalizedTaskId) {
    errorMessage.value = '请输入采购审批任务 ID';
    status.value = 'error';
    return;
  }
  status.value = 'loading';
  errorMessage.value = '';
  try {
    const adminContext = getAdminRuntimeContext();
    requireAdminTenantId(adminContext);
    requireAdminUserId(adminContext);
    detail.value = await getTaskDetail(normalizedTaskId);
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '采购审批记录加载失败';
    status.value = 'error';
  }
}
</script>

<style scoped>
.approval-record-page {
  min-width: 0;
}

.query-form {
  margin-bottom: 16px;
}

.task-input {
  width: 260px;
}

.state-alert,
.detail-panel {
  margin-bottom: 16px;
}

.permission-code {
  margin-top: 12px;
  color: #6b7280;
  font-size: 12px;
}
</style>
