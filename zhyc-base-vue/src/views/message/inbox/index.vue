<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="message-page">
    <a-card title="站内消息" :bordered="false">
      <template #extra>
        <a-space>
          <a-select v-model:value="readFilter" class="read-filter" @change="loadMessages">
            <a-select-option value="all">全部</a-select-option>
            <a-select-option value="unread">未读</a-select-option>
            <a-select-option value="read">已读</a-select-option>
          </a-select>
          <a-button :loading="loading" @click="loadMessages">刷新</a-button>
          <a-button type="primary" :loading="sending" @click="openSendForm">发送消息</a-button>
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
        row-key="messageCode"
        size="small"
        :columns="columns"
        :data-source="messages"
        :loading="loading"
        :locale="{ emptyText: '暂无站内消息。' }"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'readFlag'">
            <a-tag :color="record.readFlag ? 'default' : 'blue'">
              {{ record.readFlag ? '已读' : '未读' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button
              type="link"
              size="small"
              :disabled="record.readFlag"
              @click="markRead(record.messageCode)"
            >
              标记已读
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="sendFormOpen" title="发送站内消息" :confirm-loading="sending" @ok="submitMessage">
      <a-form layout="vertical">
        <a-form-item label="接收人 ID">
          <a-input-number v-model:value="sendForm.receiverId" :min="1" class="full-input" />
        </a-form-item>
        <a-form-item label="接收人名称">
          <a-input v-model:value="sendForm.receiverName" />
        </a-form-item>
        <a-form-item label="消息类型">
          <a-select v-model:value="sendForm.messageType">
            <a-select-option value="workflow">流程</a-select-option>
            <a-select-option value="system">系统</a-select-option>
            <a-select-option value="notice">通知</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="消息标题">
          <a-input v-model:value="sendForm.title" />
        </a-form-item>
        <a-form-item label="消息内容">
          <a-textarea v-model:value="sendForm.content" :rows="4" />
        </a-form-item>
      </a-form>
      <span class="permission-code">message:inbox:send</span>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { computed, onMounted, reactive, ref } from 'vue';

import {
  listInboxMessages,
  markInboxMessageRead,
  sendInboxMessage,
  type InboxMessage,
} from '@/api/message/inbox';
import { getAdminRuntimeContext, requireAdminTenantId, requireAdminUserId } from '@/utils/adminContext';

/**
 * 发送消息表单状态。
 */
interface SendMessageForm {
  /** 接收人用户 ID；未选择时为空，提交前必须校验为有效后台用户 ID。 */
  receiverId: number | undefined;
  /** 接收人名称。 */
  receiverName: string;
  /** 消息类型。 */
  messageType: string;
  /** 消息标题。 */
  title: string;
  /** 消息内容。 */
  content: string;
}

/** 站内消息列表。 */
const messages = ref<InboxMessage[]>([]);
/** 列表加载状态。 */
const loading = ref(false);
/** 发送状态。 */
const sending = ref(false);
/** 列表加载错误提示。 */
const errorMessage = ref('');
/** 已读筛选条件。 */
const readFilter = ref<'all' | 'unread' | 'read'>('all');
/** 发送消息弹窗打开状态。 */
const sendFormOpen = ref(false);
/** 发送消息表单状态。 */
const sendForm = reactive<SendMessageForm>({
  receiverId: undefined,
  receiverName: '',
  messageType: 'system',
  title: '',
  content: '',
});

/** 当前已读筛选布尔值。 */
const readFlag = computed<boolean | undefined>(() => {
  if (readFilter.value === 'read') {
    return true;
  }
  if (readFilter.value === 'unread') {
    return false;
  }
  return undefined;
});

/** 站内消息表格列。 */
const columns = [
  { title: '消息编码', dataIndex: 'messageCode', key: 'messageCode', width: 180 },
  { title: '类型', dataIndex: 'messageType', key: 'messageType', width: 110 },
  { title: '标题', dataIndex: 'title', key: 'title' },
  { title: '状态', dataIndex: 'readFlag', key: 'readFlag', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 190 },
  { title: '操作', key: 'action', width: 110 },
];

/**
 * 加载站内消息列表。
 */
async function loadMessages(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    requireAdminUserId();
    const page = await listInboxMessages(readFlag.value);
    messages.value = page.records;
  } catch (error) {
    messages.value = [];
    errorMessage.value = error instanceof Error ? error.message : '站内消息加载失败';
  } finally {
    loading.value = false;
  }
}

/**
 * 打开发送消息表单。
 */
function openSendForm(): void {
  const adminContext = getAdminRuntimeContext();
  Object.assign(sendForm, {
    receiverId: adminContext.userId,
    receiverName: adminContext.accountName === '未登录' ? '' : adminContext.accountName,
    messageType: 'system',
    title: '',
    content: '',
  });
  sendFormOpen.value = true;
}

/**
 * 提交站内消息。
 */
async function submitMessage(): Promise<void> {
  const validationMessage = validateSendForm();
  if (validationMessage) {
    message.warning(validationMessage);
    return;
  }
  sending.value = true;
  try {
    const adminContext = getAdminRuntimeContext();
    requireAdminUserId(adminContext);
    const receiverId = requireSendReceiverId();
    await sendInboxMessage({
      tenantId: requireAdminTenantId(adminContext),
      receiverId,
      receiverName: sendForm.receiverName.trim() || undefined,
      messageType: sendForm.messageType,
      title: sendForm.title.trim(),
      content: sendForm.content.trim(),
    });
    message.success('消息已发送');
    sendFormOpen.value = false;
    await loadMessages();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '消息发送失败');
  } finally {
    sending.value = false;
  }
}

/**
 * 校验发送消息表单。
 *
 * @returns 校验失败提示，空字符串表示通过
 */
function validateSendForm(): string {
  const receiverId = sendForm.receiverId;
  if (typeof receiverId !== 'number' || !Number.isInteger(receiverId) || receiverId <= 0) {
    return '请输入有效接收人 ID';
  }
  if (!sendForm.messageType.trim()) {
    return '请选择消息类型';
  }
  if (!sendForm.title.trim()) {
    return '请输入消息标题';
  }
  if (!sendForm.content.trim()) {
    return '请输入消息内容';
  }
  return '';
}

/**
 * 获取已校验的接收人用户 ID。
 *
 * @returns 有效接收人用户 ID
 */
function requireSendReceiverId(): number {
  const receiverId = sendForm.receiverId;
  if (typeof receiverId !== 'number' || !Number.isInteger(receiverId) || receiverId <= 0) {
    throw new Error('接收人 ID 校验状态异常');
  }
  return receiverId;
}

/**
 * 标记站内消息为已读。
 *
 * @param messageCode 消息编码
 */
async function markRead(messageCode: string): Promise<void> {
  try {
    requireAdminUserId();
    await markInboxMessageRead(messageCode);
    message.success('消息已标记为已读');
    await loadMessages();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '消息标记已读失败');
  }
}

onMounted(() => {
  void loadMessages();
});
</script>

<style scoped>
.message-page {
  min-width: 0;
}

.state-alert {
  margin-bottom: 12px;
}

.read-filter {
  width: 110px;
}

.full-input {
  width: 100%;
}

.permission-code {
  display: none;
}
</style>
