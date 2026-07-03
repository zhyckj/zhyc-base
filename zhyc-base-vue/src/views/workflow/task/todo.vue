<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="todo-page">
    <a-card title="流程待办" :bordered="false">
      <template #extra>
        <a-button :loading="status === 'loading'" @click="loadTasks">刷新</a-button>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-empty v-if="status === 'success' && tasks.length === 0" description="暂无待办任务" />

      <a-table
        v-else
        row-key="taskId"
        :columns="columns"
        :data-source="tasks"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="primary" size="small" @click="handleTask(record.taskId, 'approve')">
                通过
              </a-button>
              <a-button danger size="small" @click="handleTask(record.taskId, 'reject')">驳回</a-button>
              <a-button size="small" @click="handleRevoke(record.processInstanceId)">撤回</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { Modal, message } from 'ant-design-vue';
import { onMounted, ref } from 'vue';

import {
  approveTask,
  listTodoTasks,
  rejectTask,
  revokeProcessInstance,
  type WorkflowTaskTodoItem,
} from '@/api/workflow/task';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId, requireAdminUserId } from '@/utils/adminContext';

/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 异常提示文案。 */
const errorMessage = ref('');
/** 当前用户待办任务。 */
const tasks = ref<WorkflowTaskTodoItem[]>([]);

/** 表格列定义，权限动作与后端 Shiro 编码保持一致。 */
const columns = [
  { title: '任务名称', dataIndex: 'taskName', key: 'taskName' },
  { title: '业务单号', dataIndex: 'businessKey', key: 'businessKey' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt' },
  { title: '操作', key: 'action', width: 220 },
];

/**
 * 加载待办任务。
 */
async function loadTasks(): Promise<void> {
  status.value = 'loading';
  try {
    const adminContext = getAdminRuntimeContext();
    requireAdminTenantId(adminContext);
    requireAdminUserId(adminContext);
    tasks.value = await listTodoTasks();
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '待办任务加载失败';
    status.value = 'error';
  }
}

/**
 * 审批或驳回任务。
 *
 * @param taskId 工作流任务 ID
 * @param action 处理动作，对应 workflow:task:approve 或 workflow:task:reject
 */
async function handleTask(taskId: string, action: 'approve' | 'reject'): Promise<void> {
  const comment = action === 'approve' ? '同意' : '驳回';
  Modal.confirm({
    title: action === 'approve' ? '确认审批通过' : '确认驳回任务',
    content: `任务 ID：${taskId}`,
    async onOk() {
      try {
        requireAdminUserId();
        if (action === 'approve') {
          await approveTask(taskId, { comment });
        } else {
          await rejectTask(taskId, { comment });
        }
        message.success(action === 'approve' ? '任务已审批通过' : '任务已驳回');
        await loadTasks();
      } catch (error) {
        message.error(error instanceof Error ? error.message : '任务处理失败');
      }
    },
  });
}

/**
 * 撤回流程实例。
 *
 * @param processInstanceId 流程实例 ID，对应 workflow:task:revoke
 */
async function handleRevoke(processInstanceId: string): Promise<void> {
  Modal.confirm({
    title: '确认撤回流程',
    content: `流程实例 ID：${processInstanceId}`,
    async onOk() {
      try {
        requireAdminUserId();
        await revokeProcessInstance(processInstanceId, { reason: '后台管理端撤回' });
        message.success('流程已撤回');
        await loadTasks();
      } catch (error) {
        message.error(error instanceof Error ? error.message : '流程撤回失败');
      }
    },
  });
}

onMounted(() => {
  void loadTasks();
});
</script>

<style scoped>
.todo-page {
  min-width: 0;
}

.state-alert {
  margin-bottom: 12px;
}
</style>
