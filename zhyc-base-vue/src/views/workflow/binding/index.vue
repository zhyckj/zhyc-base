<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="binding-page">
    <a-card title="表单绑定" :bordered="false">
      <template #extra>
        <a-space>
          <a-button :loading="status === 'loading'" @click="loadBindings">刷新</a-button>
          <a-button type="primary" @click="openCreateForm">新增绑定</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-table
        row-key="processKey"
        :columns="columns"
        :data-source="bindings"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
              {{ record.status === 'enabled' ? '启用' : '停用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button size="small" @click="openEditForm(record)">编辑</a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="formOpen" title="表单绑定" :confirm-loading="saving" @ok="submitForm">
      <a-form layout="vertical">
        <a-form-item label="流程定义 key">
          <a-input v-model:value="formState.processKey" :disabled="Boolean(formState.id)" />
        </a-form-item>
        <a-form-item label="业务模块">
          <a-input v-model:value="formState.businessModule" />
        </a-form-item>
        <a-form-item label="业务表名">
          <a-input v-model:value="formState.businessTable" />
        </a-form-item>
        <a-form-item label="后台表单路由">
          <a-input v-model:value="formState.formRoute" />
        </a-form-item>
        <a-form-item label="移动端表单路由">
          <a-input v-model:value="formState.mobileRoute" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="formState.status">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="formState.remark" :rows="3" />
        </a-form-item>
      </a-form>
      <span class="permission-code">workflow:binding:update</span>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { onMounted, reactive, ref } from 'vue';

import {
  listWorkflowFormBindings,
  saveWorkflowFormBinding,
  type WorkflowFormBinding,
  type WorkflowFormBindingSavePayload,
} from '@/api/workflow/binding';
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
/** 工作流表单绑定列表。 */
const bindings = ref<WorkflowFormBinding[]>([]);
/** 表单绑定编辑状态。 */
const formState = reactive<WorkflowFormBindingSavePayload>({
  processKey: '',
  businessModule: '',
  businessTable: '',
  formRoute: '',
  mobileRoute: '',
  status: 'enabled',
  remark: '',
});

/** 表格列定义。 */
const columns = [
  { title: '流程 key', dataIndex: 'processKey', key: 'processKey' },
  { title: '业务模块', dataIndex: 'businessModule', key: 'businessModule' },
  { title: '业务表', dataIndex: 'businessTable', key: 'businessTable' },
  { title: '后台路由', dataIndex: 'formRoute', key: 'formRoute' },
  { title: '移动端路由', dataIndex: 'mobileRoute', key: 'mobileRoute' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 100 },
];

/**
 * 加载工作流表单绑定。
 */
async function loadBindings(): Promise<void> {
  status.value = 'loading';
  try {
    requireAdminTenantId();
    bindings.value = await listWorkflowFormBindings();
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '表单绑定加载失败';
    status.value = 'error';
  }
}

/**
 * 打开新增表单绑定弹窗。
 */
function openCreateForm(): void {
  Object.assign(formState, {
    id: undefined,
    processKey: '',
    businessModule: '',
    businessTable: '',
    formRoute: '',
    mobileRoute: '',
    status: 'enabled',
    remark: '',
  });
  formOpen.value = true;
}

/**
 * 打开编辑表单绑定弹窗。
 *
 * @param binding 工作流表单绑定
 */
function openEditForm(binding: WorkflowFormBinding): void {
  Object.assign(formState, {
    id: binding.id,
    processKey: binding.processKey,
    businessModule: binding.businessModule,
    businessTable: binding.businessTable,
    formRoute: binding.formRoute,
    mobileRoute: binding.mobileRoute ?? '',
    status: binding.status,
    remark: binding.remark ?? '',
  });
  formOpen.value = true;
}

/**
 * 提交工作流表单绑定。
 */
async function submitForm(): Promise<void> {
  saving.value = true;
  try {
    requireAdminTenantId();
    await saveWorkflowFormBinding({ ...formState });
    message.success('表单绑定已保存');
    formOpen.value = false;
    await loadBindings();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '表单绑定保存失败');
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void loadBindings();
});
</script>

<style scoped>
.binding-page {
  min-width: 0;
}

.state-alert {
  margin-bottom: 12px;
}

.permission-code {
  display: none;
}
</style>
