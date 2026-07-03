<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="tenant-param-page">
    <a-card title="租户参数" :bordered="false">
      <template #extra>
        <a-space>
          <a-input v-model:value="tenantId" class="tenant-id" />
          <a-button :loading="status === 'loading'" @click="loadParams">查询</a-button>
          <a-button type="primary" v-permission="'system:tenant-param:save'" @click="openCreateForm">新增参数</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-table
        row-key="paramKey"
        :columns="columns"
        :data-source="params"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'visible'">
            <a-tag :color="record.visible ? 'green' : 'default'">
              {{ record.visible ? '显示' : '隐藏' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button size="small" v-permission="'system:tenant-param:save'" @click="openEditForm(record)">编辑</a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="formOpen" title="租户参数" :confirm-loading="saving" @ok="submitForm">
      <a-form layout="vertical">
        <a-form-item label="租户编码">
          <a-input v-model:value="formState.tenantId" />
        </a-form-item>
        <a-form-item label="参数键">
          <a-input v-model:value="formState.paramKey" />
        </a-form-item>
        <a-form-item label="参数值">
          <a-textarea v-model:value="formState.paramValue" :rows="3" />
        </a-form-item>
        <a-form-item label="值类型">
          <a-select v-model:value="formState.valueType">
            <a-select-option value="string">字符串</a-select-option>
            <a-select-option value="number">数字</a-select-option>
            <a-select-option value="boolean">布尔</a-select-option>
            <a-select-option value="json">JSON</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="租户可见">
          <a-switch v-model:checked="formState.visible" />
        </a-form-item>
      </a-form>
      <span class="permission-code">system:tenant-param:save</span>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { onMounted, reactive, ref } from 'vue';

import {
  listSystemTenantParams,
  saveSystemTenantParam,
  type SystemTenantParam,
  type SystemTenantParamSavePayload,
} from '@/api/system/tenant-param';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 保存按钮加载状态。 */
const saving = ref(false);
/** 表单弹窗打开状态。 */
const formOpen = ref(false);
/** 异常提示文案。 */
const errorMessage = ref('');
/** 租户参数列表。 */
const params = ref<SystemTenantParam[]>([]);
/** 租户参数表单状态。 */
const formState = reactive<SystemTenantParamSavePayload>({
  tenantId: getAdminRuntimeContext().tenantId,
  paramKey: '',
  paramValue: '',
  valueType: 'string',
  visible: true,
});

/** 表格列定义。 */
const columns = [
  { title: '参数键', dataIndex: 'paramKey', key: 'paramKey' },
  { title: '参数值', dataIndex: 'paramValue', key: 'paramValue' },
  { title: '值类型', dataIndex: 'valueType', key: 'valueType', width: 110 },
  { title: '租户可见', dataIndex: 'visible', key: 'visible', width: 110 },
  { title: '操作', key: 'action', width: 100 },
];

/**
 * 加载租户参数列表。
 */
async function loadParams(): Promise<void> {
  status.value = 'loading';
  try {
    params.value = await listSystemTenantParams(syncTenantContext());
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '租户参数加载失败';
    status.value = 'error';
  }
}

/**
 * 打开新增租户参数表单。
 */
function openCreateForm(): void {
  const currentTenantId = syncTenantContext();
  Object.assign(formState, {
    tenantId: currentTenantId,
    paramKey: '',
    paramValue: '',
    valueType: 'string',
    visible: true,
  });
  formOpen.value = true;
}

/**
 * 打开编辑租户参数表单。
 *
 * @param param 租户参数
 */
function openEditForm(param: SystemTenantParam): void {
  Object.assign(formState, {
    tenantId: param.tenantId,
    paramKey: param.paramKey,
    paramValue: param.paramValue ?? '',
    valueType: param.valueType,
    visible: param.visible,
  });
  formOpen.value = true;
}

/**
 * 提交租户参数表单。
 */
async function submitForm(): Promise<void> {
  saving.value = true;
  try {
    const currentTenantId = syncTenantContext();
    await saveSystemTenantParam({ ...formState, tenantId: currentTenantId });
    message.success('租户参数已保存');
    formOpen.value = false;
    formState.tenantId = currentTenantId;
    tenantId.value = currentTenantId;
    await loadParams();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '租户参数保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 同步后台运行时租户上下文。
 *
 * @returns 当前后台租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId();
  tenantId.value = currentTenantId;
  return currentTenantId;
}

onMounted(() => {
  void loadParams();
});
</script>

<style scoped>
.tenant-param-page {
  min-width: 0;
}

.tenant-id {
  width: 180px;
}

.state-alert {
  margin-bottom: 12px;
}

.permission-code {
  display: none;
}
</style>
