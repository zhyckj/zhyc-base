<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="category-page">
    <a-card title="流程分类" :bordered="false">
      <template #extra>
        <a-space>
          <a-button :loading="status === 'loading'" @click="loadCategories">刷新</a-button>
          <a-button type="primary" @click="openCreateForm">新增分类</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-table
        row-key="categoryCode"
        :columns="columns"
        :data-source="categories"
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

    <a-modal v-model:open="formOpen" title="流程分类" :confirm-loading="saving" @ok="submitForm">
      <a-form layout="vertical">
        <a-form-item label="分类编码">
          <a-input v-model:value="formState.categoryCode" :disabled="Boolean(formState.id)" />
        </a-form-item>
        <a-form-item label="分类名称">
          <a-input v-model:value="formState.categoryName" />
        </a-form-item>
        <a-form-item label="排序号">
          <a-input-number v-model:value="formState.sortOrder" :min="0" class="full-field" />
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
      <span class="permission-code">workflow:model:update</span>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { onMounted, reactive, ref } from 'vue';

import {
  listWorkflowCategories,
  saveWorkflowCategory,
  type WorkflowCategory,
  type WorkflowCategorySavePayload,
} from '@/api/workflow/category';
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
/** 工作流分类列表。 */
const categories = ref<WorkflowCategory[]>([]);
/** 分类表单状态。 */
const formState = reactive<WorkflowCategorySavePayload>({
  categoryCode: '',
  categoryName: '',
  sortOrder: 0,
  status: 'enabled',
  remark: '',
});

/** 表格列定义。 */
const columns = [
  { title: '分类编码', dataIndex: 'categoryCode', key: 'categoryCode' },
  { title: '分类名称', dataIndex: 'categoryName', key: 'categoryName' },
  { title: '排序号', dataIndex: 'sortOrder', key: 'sortOrder', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 100 },
];

/**
 * 加载工作流分类。
 */
async function loadCategories(): Promise<void> {
  status.value = 'loading';
  try {
    requireAdminTenantId();
    categories.value = await listWorkflowCategories();
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '流程分类加载失败';
    status.value = 'error';
  }
}

/**
 * 打开新增分类表单。
 */
function openCreateForm(): void {
  Object.assign(formState, {
    id: undefined,
    categoryCode: '',
    categoryName: '',
    sortOrder: categories.value.length + 1,
    status: 'enabled',
    remark: '',
  });
  formOpen.value = true;
}

/**
 * 打开编辑分类表单。
 *
 * @param category 工作流分类
 */
function openEditForm(category: WorkflowCategory): void {
  Object.assign(formState, {
    id: category.id,
    categoryCode: category.categoryCode,
    categoryName: category.categoryName,
    sortOrder: category.sortOrder,
    status: category.status,
    remark: category.remark ?? '',
  });
  formOpen.value = true;
}

/**
 * 提交流程分类表单。
 */
async function submitForm(): Promise<void> {
  saving.value = true;
  try {
    requireAdminTenantId();
    await saveWorkflowCategory({ ...formState });
    message.success('流程分类已保存');
    formOpen.value = false;
    await loadCategories();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '流程分类保存失败');
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void loadCategories();
});
</script>

<style scoped>
.category-page {
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
