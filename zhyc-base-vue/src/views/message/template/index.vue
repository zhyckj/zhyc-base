<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="message-page">
    <a-card title="消息模板" :bordered="false">
      <template #extra>
        <a-space>
          <a-button :loading="loading" @click="loadTemplates">刷新</a-button>
          <a-button type="primary" @click="openCreateForm">新增模板</a-button>
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
        row-key="templateCode"
        size="small"
        :columns="columns"
        :data-source="templates"
        :loading="loading"
        :locale="{ emptyText: '暂无消息模板。' }"
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
      title="消息模板"
      :confirm-loading="saving"
      @ok="submitForm"
    >
      <a-form layout="vertical">
        <a-form-item label="模板编码" required>
          <a-input
            v-model:value="formState.templateCode"
            class="full-input"
            placeholder="例如 workflow.todo"
          />
        </a-form-item>
        <a-form-item label="模板名称" required>
          <a-input
            v-model:value="formState.templateName"
            class="full-input"
            placeholder="请输入模板名称"
          />
        </a-form-item>
        <a-form-item label="消息通道" required>
          <a-select v-model:value="formState.channelType" class="full-input">
            <a-select-option value="in_app">站内信</a-select-option>
            <a-select-option value="sms">短信</a-select-option>
            <a-select-option value="email">邮件</a-select-option>
            <a-select-option value="webhook">Webhook</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="标题模板" required>
          <a-input
            v-model:value="formState.titleTemplate"
            class="full-input"
            placeholder="请输入标题模板"
          />
        </a-form-item>
        <a-form-item label="内容模板" required>
          <a-textarea
            v-model:value="formState.contentTemplate"
            :rows="4"
            placeholder="请输入内容模板"
          />
        </a-form-item>
        <a-form-item label="模板状态" required>
          <a-select v-model:value="formState.status" class="full-input">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <span class="permission-code">message:template:save</span>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { onMounted, reactive, ref } from 'vue';

import {
  listMessageTemplates,
  saveMessageTemplate,
  type MessageTemplate,
  type MessageTemplateSavePayload,
} from '@/api/message/template';
import { requireAdminTenantId } from '@/utils/adminContext';

/** 消息模板列表。 */
const templates = ref<MessageTemplate[]>([]);
/** 列表加载状态。 */
const loading = ref(false);
/** 保存状态。 */
const saving = ref(false);
/** 列表加载错误提示。 */
const errorMessage = ref('');
/** 模板表单弹窗状态。 */
const formOpen = ref(false);
/** 模板表单数据，租户在提交时按当前上下文写入。 */
const formState = reactive<MessageTemplateSavePayload>({
  tenantId: '',
  templateCode: '',
  templateName: '',
  channelType: 'in_app',
  titleTemplate: '',
  contentTemplate: '',
  status: 'enabled',
});

/** 消息模板表格列。 */
const columns = [
  { title: '模板编码', dataIndex: 'templateCode', key: 'templateCode', width: 180 },
  { title: '模板名称', dataIndex: 'templateName', key: 'templateName', width: 160 },
  { title: '通道', dataIndex: 'channelType', key: 'channelType', width: 110 },
  { title: '标题模板', dataIndex: 'titleTemplate', key: 'titleTemplate' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 100 },
];

/**
 * 加载消息模板列表。
 */
async function loadTemplates(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    templates.value = await listMessageTemplates();
  } catch (error) {
    templates.value = [];
    errorMessage.value = error instanceof Error ? error.message : '消息模板加载失败';
  } finally {
    loading.value = false;
  }
}

/**
 * 打开新增消息模板表单。
 */
function openCreateForm(): void {
  resetForm();
  formOpen.value = true;
}

/**
 * 打开编辑消息模板表单。
 *
 * @param template 当前选中的消息模板
 */
function openEditForm(template: MessageTemplate): void {
  formState.tenantId = requireAdminTenantId();
  formState.templateCode = template.templateCode;
  formState.templateName = template.templateName;
  formState.channelType = template.channelType;
  formState.titleTemplate = template.titleTemplate;
  formState.contentTemplate = template.contentTemplate;
  formState.status = template.status || 'enabled';
  formOpen.value = true;
}

/**
 * 提交消息模板表单。
 */
async function submitForm(): Promise<void> {
  if (!validateForm()) {
    return;
  }
  saving.value = true;
  try {
    await saveMessageTemplate({
      tenantId: requireAdminTenantId(),
      templateCode: formState.templateCode.trim(),
      templateName: formState.templateName.trim(),
      channelType: formState.channelType,
      titleTemplate: formState.titleTemplate.trim(),
      contentTemplate: formState.contentTemplate.trim(),
      status: formState.status,
    });
    message.success('消息模板已保存');
    formOpen.value = false;
    await loadTemplates();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '消息模板保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 重置消息模板表单。
 */
function resetForm(): void {
  formState.tenantId = requireAdminTenantId();
  formState.templateCode = '';
  formState.templateName = '';
  formState.channelType = 'in_app';
  formState.titleTemplate = '';
  formState.contentTemplate = '';
  formState.status = 'enabled';
}

/**
 * 校验消息模板表单。
 *
 * @returns 表单是否满足提交条件
 */
function validateForm(): boolean {
  if (!formState.templateCode.trim()) {
    message.warning('请输入模板编码');
    return false;
  }
  if (!formState.templateName.trim()) {
    message.warning('请输入模板名称');
    return false;
  }
  if (!formState.channelType.trim()) {
    message.warning('请选择消息通道');
    return false;
  }
  if (!formState.titleTemplate.trim()) {
    message.warning('请输入标题模板');
    return false;
  }
  if (!formState.contentTemplate.trim()) {
    message.warning('请输入内容模板');
    return false;
  }
  return true;
}

onMounted(() => {
  void loadTemplates();
});
</script>

<style scoped>
.message-page {
  min-width: 0;
}

.state-alert {
  margin-bottom: 12px;
}

.full-input {
  width: 100%;
}

.permission-code {
  display: none;
}
</style>
