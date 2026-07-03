<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="definition-page">
    <a-card title="流程定义" :bordered="false">
      <template #extra>
        <a-space>
          <a-button :loading="status === 'loading'" @click="loadDefinitions">刷新</a-button>
          <a-button type="primary" @click="openCreateForm">登记部署</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="definitions"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'active' ? 'green' : 'default'">
              {{ record.status === 'active' ? '启用' : '停用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button size="small" @click="openEditForm(record)">编辑</a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="formOpen" title="流程定义" :confirm-loading="saving" @ok="submitForm">
      <a-form layout="vertical">
        <a-form-item label="流程 key">
          <a-input v-model:value="formState.processKey" :disabled="Boolean(formState.id)" />
        </a-form-item>
        <a-form-item label="流程名称">
          <a-input v-model:value="formState.processName" />
        </a-form-item>
        <a-form-item label="版本号">
          <a-input-number v-model:value="formState.version" :min="1" class="full-field" />
        </a-form-item>
        <a-form-item label="部署 ID">
          <a-input v-model:value="formState.deploymentId" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="formState.status">
            <a-select-option value="active">启用</a-select-option>
            <a-select-option value="suspended">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="formState.remark" :rows="3" />
        </a-form-item>
      </a-form>
      <span class="permission-code">workflow:model:deploy</span>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { onMounted, reactive, ref } from 'vue';

import {
  listWorkflowDefinitions,
  saveWorkflowDefinition,
  type WorkflowDefinition,
  type WorkflowDefinitionSavePayload,
} from '@/api/workflow/definition';
import type { LoadStatus } from '@/types/platform';
import { requireAdminTenantId } from '@/utils/adminContext';

/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 保存按钮加载状态。 */
const saving = ref(false);
/** 表单弹窗打开状态。 */
const formOpen = ref(false);
/** 异常提示文案。 */
const errorMessage = ref('');
/** 流程定义版本列表。 */
const definitions = ref<WorkflowDefinition[]>([]);
/** 流程定义编辑状态。 */
const formState = reactive<WorkflowDefinitionSavePayload>({
  processKey: '',
  processName: '',
  version: 1,
  deploymentId: '',
  status: 'active',
  remark: '',
});

/** 表格列定义。 */
const columns = [
  { title: '流程 key', dataIndex: 'processKey', key: 'processKey' },
  { title: '流程名称', dataIndex: 'processName', key: 'processName' },
  { title: '版本', dataIndex: 'version', key: 'version', width: 90 },
  { title: '部署 ID', dataIndex: 'deploymentId', key: 'deploymentId' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 100 },
];

/**
 * 加载流程定义版本。
 */
async function loadDefinitions(): Promise<void> {
  status.value = 'loading';
  try {
    requireAdminTenantId();
    definitions.value = await listWorkflowDefinitions();
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '流程定义加载失败';
    status.value = 'error';
  }
}

/**
 * 打开新增流程定义弹窗。
 */
function openCreateForm(): void {
  Object.assign(formState, {
    id: undefined,
    processKey: '',
    processName: '',
    version: 1,
    deploymentId: '',
    status: 'active',
    remark: '',
  });
  formOpen.value = true;
}

/**
 * 打开编辑流程定义弹窗。
 *
 * @param definition 流程定义版本
 */
function openEditForm(definition: WorkflowDefinition): void {
  Object.assign(formState, {
    id: definition.id,
    processKey: definition.processKey,
    processName: definition.processName,
    version: definition.version,
    deploymentId: definition.deploymentId,
    status: definition.status,
    remark: definition.remark ?? '',
  });
  formOpen.value = true;
}

/**
 * 提交流程定义版本。
 */
async function submitForm(): Promise<void> {
  saving.value = true;
  try {
    requireAdminTenantId();
    await saveWorkflowDefinition({ ...formState });
    message.success('流程定义已保存');
    formOpen.value = false;
    await loadDefinitions();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '流程定义保存失败');
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void loadDefinitions();
});
</script>

<style scoped>
.definition-page {
  min-width: 0;
}

.state-alert {
  margin-bottom: 12px;
}

.full-field {
  width: 100%;
}

.permission-code {
  display: none;
}
</style>
