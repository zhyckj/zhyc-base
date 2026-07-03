<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="cms-page">
    <a-card title="内容栏目" :bordered="false">
      <template #extra>
        <a-space>
          <a-select
            v-model:value="status"
            class="status-select"
            allow-clear
            placeholder="状态"
            @change="loadChannels"
          >
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">停用</a-select-option>
          </a-select>
          <a-button :loading="loading" @click="loadChannels">刷新</a-button>
          <a-button type="primary" @click="openCreateForm">新增栏目</a-button>
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
        row-key="channelCode"
        size="small"
        :columns="columns"
        :data-source="channels"
        :loading="loading"
        :locale="{ emptyText: '暂无内容栏目。' }"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
              {{ $statusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="openEditForm(record)">编辑</a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="formOpen"
      title="内容栏目"
      :confirm-loading="saving"
      @ok="submitForm"
    >
      <a-form layout="vertical">
        <a-form-item label="栏目编码" required>
          <a-input v-model:value="formState.channelCode" class="full-input" placeholder="例如 news" />
        </a-form-item>
        <a-form-item label="栏目名称" required>
          <a-input v-model:value="formState.channelName" class="full-input" placeholder="请输入栏目名称" />
        </a-form-item>
        <a-form-item label="父栏目 ID">
          <a-input-number v-model:value="formState.parentId" class="full-input" :min="1" :precision="0" />
        </a-form-item>
        <a-form-item label="排序号" required>
          <a-input-number v-model:value="formState.sortOrder" class="full-input" :min="0" :precision="0" />
        </a-form-item>
        <a-form-item label="栏目状态" required>
          <a-select v-model:value="formState.status" class="full-input">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">停用</a-select-option>
          </a-select>
        </a-form-item>
        <span class="permission-code">cms:channel:save</span>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { onMounted, reactive, ref } from 'vue';

import {
  listCmsChannels,
  saveCmsChannel,
  type CmsChannel,
  type CmsChannelSavePayload,
} from '@/api/cms/content';
import { requireAdminTenantId } from '@/utils/adminContext';

/** 内容栏目列表。 */
const channels = ref<CmsChannel[]>([]);
/** 栏目状态筛选值。 */
const status = ref<string | undefined>();
/** 列表加载状态。 */
const loading = ref(false);
/** 保存状态。 */
const saving = ref(false);
/** 列表加载错误提示。 */
const errorMessage = ref('');
/** 栏目表单弹窗状态。 */
const formOpen = ref(false);
/** 栏目表单数据，提交时写入当前租户。 */
const formState = reactive<CmsChannelSavePayload>({
  tenantId: '',
  channelCode: '',
  channelName: '',
  parentId: undefined,
  sortOrder: 0,
  status: 'enabled',
});

/** 内容栏目表格列。 */
const columns = [
  { title: '栏目编码', dataIndex: 'channelCode', key: 'channelCode', width: 180 },
  { title: '栏目名称', dataIndex: 'channelName', key: 'channelName' },
  { title: '父栏目', dataIndex: 'parentId', key: 'parentId', width: 120 },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 90 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 100 },
];

/**
 * 加载内容栏目列表。
 */
async function loadChannels(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    channels.value = await listCmsChannels(status.value);
  } catch (error) {
    channels.value = [];
    errorMessage.value = error instanceof Error ? error.message : '内容栏目加载失败';
  } finally {
    loading.value = false;
  }
}

/**
 * 打开新增栏目表单。
 */
function openCreateForm(): void {
  resetForm();
  formOpen.value = true;
}

/**
 * 打开编辑栏目表单。
 *
 * @param channel 当前选中的内容栏目
 */
function openEditForm(channel: CmsChannel): void {
  formState.tenantId = requireAdminTenantId();
  formState.channelCode = channel.channelCode;
  formState.channelName = channel.channelName;
  formState.parentId = channel.parentId;
  formState.sortOrder = channel.sortOrder;
  formState.status = channel.status || 'enabled';
  formOpen.value = true;
}

/**
 * 提交栏目表单。
 */
async function submitForm(): Promise<void> {
  if (!validateForm()) {
    return;
  }
  saving.value = true;
  try {
    await saveCmsChannel({
      tenantId: requireAdminTenantId(),
      channelCode: formState.channelCode.trim(),
      channelName: formState.channelName.trim(),
      parentId: formState.parentId,
      sortOrder: formState.sortOrder,
      status: formState.status,
    });
    message.success('内容栏目已保存');
    formOpen.value = false;
    await loadChannels();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '内容栏目保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 重置栏目表单。
 */
function resetForm(): void {
  formState.tenantId = requireAdminTenantId();
  formState.channelCode = '';
  formState.channelName = '';
  formState.parentId = undefined;
  formState.sortOrder = 0;
  formState.status = 'enabled';
}

/**
 * 校验栏目表单。
 *
 * @returns 表单是否满足提交条件
 */
function validateForm(): boolean {
  if (!formState.channelCode.trim()) {
    message.warning('请输入栏目编码');
    return false;
  }
  if (!formState.channelName.trim()) {
    message.warning('请输入栏目名称');
    return false;
  }
  if (!Number.isFinite(formState.sortOrder) || formState.sortOrder < 0) {
    message.warning('请输入有效的排序号');
    return false;
  }
  return true;
}

onMounted(() => {
  void loadChannels();
});
</script>

<style scoped>
.cms-page {
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
