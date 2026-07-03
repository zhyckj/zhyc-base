<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="i18n-page">
    <a-card title="国际化词条" :bordered="false">
      <template #extra>
        <a-space>
          <a-select v-model:value="locale" class="locale-select" allow-clear placeholder="语言">
            <a-select-option value="zh-CN">zh-CN</a-select-option>
            <a-select-option value="en-US">en-US</a-select-option>
          </a-select>
          <a-select v-model:value="status" class="status-select" allow-clear placeholder="状态">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">停用</a-select-option>
          </a-select>
          <a-button :loading="loading" @click="loadMessages">刷新</a-button>
          <a-button type="primary" @click="openCreateForm">新增词条</a-button>
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
        row-key="id"
        size="small"
        :columns="columns"
        :data-source="messages"
        :loading="loading"
        :locale="{ emptyText: '暂无国际化词条。' }"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
              {{ $statusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'locale'">
            <a-tag color="blue">{{ record.locale }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="openEditForm(record)">编辑</a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="formOpen"
      title="国际化词条"
      :confirm-loading="saving"
      @ok="submitForm"
    >
      <a-form layout="vertical">
        <a-form-item label="语言标识" required>
          <a-select v-model:value="formState.locale" class="full-input">
            <a-select-option value="zh-CN">zh-CN</a-select-option>
            <a-select-option value="en-US">en-US</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="词条键" required>
          <a-input v-model:value="formState.messageKey" class="full-input" placeholder="例如 menu.system" />
        </a-form-item>
        <a-form-item label="词条值" required>
          <a-textarea v-model:value="formState.messageValue" :rows="3" placeholder="请输入词条值" />
        </a-form-item>
        <a-form-item label="词条状态" required>
          <a-select v-model:value="formState.status" class="full-input">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">停用</a-select-option>
          </a-select>
        </a-form-item>
        <span class="permission-code">i18n:message:save</span>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { onMounted, reactive, ref } from 'vue';

import {
  listI18nMessages,
  saveI18nMessage,
  type I18nMessage,
  type I18nMessageSavePayload,
} from '@/api/i18n/message';
import { requireAdminTenantId } from '@/utils/adminContext';

/** 国际化词条列表。 */
const messages = ref<I18nMessage[]>([]);
/** 语言筛选值。 */
const locale = ref('zh-CN');
/** 状态筛选值。 */
const status = ref<string | undefined>();
/** 列表加载状态。 */
const loading = ref(false);
/** 保存状态。 */
const saving = ref(false);
/** 列表加载错误提示。 */
const errorMessage = ref('');
/** 词条表单弹窗状态。 */
const formOpen = ref(false);
/** 词条表单数据，提交时写入当前租户。 */
const formState = reactive<I18nMessageSavePayload>({
  tenantId: '',
  locale: 'zh-CN',
  messageKey: '',
  messageValue: '',
  status: 'enabled',
});

/** 国际化词条表格列。 */
const columns = [
  { title: '语言', dataIndex: 'locale', key: 'locale', width: 110 },
  { title: '词条键', dataIndex: 'messageKey', key: 'messageKey', width: 220 },
  { title: '词条值', dataIndex: 'messageValue', key: 'messageValue' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 100 },
];

/**
 * 加载国际化词条列表。
 */
async function loadMessages(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    messages.value = await listI18nMessages(locale.value || undefined, status.value);
  } catch (error) {
    messages.value = [];
    errorMessage.value = error instanceof Error ? error.message : '国际化词条加载失败';
  } finally {
    loading.value = false;
  }
}

/**
 * 打开新增词条表单。
 */
function openCreateForm(): void {
  resetForm();
  formOpen.value = true;
}

/**
 * 打开编辑词条表单。
 *
 * @param item 当前选中的国际化词条
 */
function openEditForm(item: I18nMessage): void {
  formState.tenantId = requireAdminTenantId();
  formState.locale = item.locale;
  formState.messageKey = item.messageKey;
  formState.messageValue = item.messageValue;
  formState.status = item.status || 'enabled';
  formOpen.value = true;
}

/**
 * 提交国际化词条表单。
 */
async function submitForm(): Promise<void> {
  if (!validateForm()) {
    return;
  }
  saving.value = true;
  try {
    await saveI18nMessage({
      tenantId: requireAdminTenantId(),
      locale: formState.locale,
      messageKey: formState.messageKey.trim(),
      messageValue: formState.messageValue.trim(),
      status: formState.status || 'enabled',
    });
    message.success('国际化词条已保存');
    formOpen.value = false;
    await loadMessages();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '国际化词条保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 重置国际化词条表单。
 */
function resetForm(): void {
  formState.tenantId = requireAdminTenantId();
  formState.locale = locale.value || 'zh-CN';
  formState.messageKey = '';
  formState.messageValue = '';
  formState.status = 'enabled';
}

/**
 * 校验国际化词条表单。
 *
 * @returns 表单是否满足提交条件
 */
function validateForm(): boolean {
  if (!formState.locale.trim()) {
    message.warning('请选择语言标识');
    return false;
  }
  if (!formState.messageKey.trim()) {
    message.warning('请输入词条键');
    return false;
  }
  if (!formState.messageValue.trim()) {
    message.warning('请输入词条值');
    return false;
  }
  return true;
}

onMounted(() => {
  void loadMessages();
});
</script>

<style scoped>
.i18n-page {
  min-width: 0;
}

.state-alert {
  margin-bottom: 12px;
}

.locale-select,
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
