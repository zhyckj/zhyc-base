<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="visual-page">
    <a-card title="报表数据集" :bordered="false">
      <template #extra>
        <a-space>
          <a-select v-model:value="status" class="status-select" allow-clear placeholder="状态">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">停用</a-select-option>
          </a-select>
          <a-button :loading="loading" @click="loadDatasets">刷新</a-button>
          <a-button type="primary" @click="openCreateForm">新增数据集</a-button>
        </a-space>
      </template>

      <a-alert
        v-if="errorMessage"
        class="state-alert"
        :message="errorMessage"
        type="error"
        show-icon
      />

      <a-table
        row-key="datasetCode"
        size="small"
        :columns="columns"
        :data-source="datasets"
        :loading="loading"
        :pagination="$tablePagination"
        :locale="{ emptyText: '暂无数据集。' }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
              {{ $statusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="openEditForm(record)">编辑</a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="formOpen"
      title="报表数据集"
      :confirm-loading="saving"
      @ok="submitForm"
    >
      <a-form layout="vertical">
        <a-form-item label="数据集编码" required>
          <a-input
            v-model:value="formState.datasetCode"
            placeholder="例如 sales_month"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="数据集名称" required>
          <a-input
            v-model:value="formState.datasetName"
            placeholder="请输入数据集名称"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="数据源编码" required>
          <a-input
            v-model:value="formState.datasourceCode"
            placeholder="请输入数据源编码"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="查询 SQL" required>
          <a-textarea
            v-model:value="formState.sqlText"
            :rows="5"
            placeholder="请输入数据集查询 SQL"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="formState.status" class="full-input">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">停用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
      <span class="permission-code">visual:dataset:save</span>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { onMounted, reactive, ref } from 'vue';

import {
  listVisualDatasets,
  saveVisualDataset,
  type VisualDataset,
  type VisualDatasetSavePayload,
} from '@/api/visual/report';
import { requireAdminTenantId } from '@/utils/adminContext';

/** 数据集列表。 */
const datasets = ref<VisualDataset[]>([]);
/** 数据集状态筛选值。 */
const status = ref<string | undefined>();
/** 列表加载状态。 */
const loading = ref(false);
/** 保存状态。 */
const saving = ref(false);
/** 错误提示。 */
const errorMessage = ref('');
/** 数据集表单弹窗打开状态。 */
const formOpen = ref(false);
/** 数据集表单状态。 */
const formState = reactive<VisualDatasetSavePayload>({
  tenantId: '',
  datasetCode: '',
  datasetName: '',
  datasourceCode: '',
  sqlText: '',
  status: 'enabled',
});

/** 数据集表格列。 */
const columns = [
  { title: '数据集编码', dataIndex: 'datasetCode', key: 'datasetCode', width: 170 },
  { title: '数据集名称', dataIndex: 'datasetName', key: 'datasetName', width: 160 },
  { title: '数据源', dataIndex: 'datasourceCode', key: 'datasourceCode', width: 130 },
  { title: '查询 SQL', dataIndex: 'sqlText', key: 'sqlText' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 90 },
];

/**
 * 加载数据集列表。
 */
async function loadDatasets(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    datasets.value = await listVisualDatasets(status.value);
  } catch (error) {
    datasets.value = [];
    errorMessage.value = error instanceof Error ? error.message : '数据集加载失败';
  } finally {
    loading.value = false;
  }
}

/**
 * 打开新增数据集表单。
 */
function openCreateForm(): void {
  resetForm();
  formOpen.value = true;
}

/**
 * 打开编辑数据集表单。
 *
 * @param dataset 当前选中的数据集
 */
function openEditForm(dataset: VisualDataset): void {
  formState.tenantId = dataset.tenantId;
  formState.datasetCode = dataset.datasetCode;
  formState.datasetName = dataset.datasetName;
  formState.datasourceCode = dataset.datasourceCode;
  formState.sqlText = dataset.sqlText;
  formState.status = dataset.status || 'enabled';
  formOpen.value = true;
}

/**
 * 提交数据集表单。
 */
async function submitForm(): Promise<void> {
  if (!validateForm()) {
    return;
  }

  saving.value = true;
  try {
    await saveVisualDataset({
      tenantId: requireAdminTenantId(),
      datasetCode: formState.datasetCode.trim(),
      datasetName: formState.datasetName.trim(),
      datasourceCode: formState.datasourceCode.trim(),
      sqlText: formState.sqlText.trim(),
      status: formState.status || 'enabled',
    });
    message.success('数据集已保存');
    formOpen.value = false;
    await loadDatasets();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '数据集保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 重置数据集表单。
 */
function resetForm(): void {
  formState.tenantId = '';
  formState.datasetCode = '';
  formState.datasetName = '';
  formState.datasourceCode = '';
  formState.sqlText = '';
  formState.status = 'enabled';
}

/**
 * 校验数据集表单。
 *
 * @returns 表单是否可以提交
 */
function validateForm(): boolean {
  if (!formState.datasetCode.trim()) {
    message.warning('请输入数据集编码');
    return false;
  }
  if (!formState.datasetName.trim()) {
    message.warning('请输入数据集名称');
    return false;
  }
  if (!formState.datasourceCode.trim()) {
    message.warning('请输入数据源编码');
    return false;
  }
  if (!formState.sqlText.trim()) {
    message.warning('请输入查询 SQL');
    return false;
  }
  return true;
}

onMounted(() => {
  void loadDatasets();
});
</script>

<style scoped>
.visual-page {
  min-width: 0;
}

.state-alert {
  margin-bottom: 12px;
}

.status-select {
  width: 130px;
}

.full-input {
  width: 100%;
}

.permission-code {
  display: none;
}
</style>
