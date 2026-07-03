<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="cms-page">
    <a-card title="内容文章" :bordered="false">
      <template #extra>
        <a-space>
          <a-input v-model:value="channelCode" class="channel-input" placeholder="栏目编码" allow-clear />
          <a-select
            v-model:value="status"
            class="status-select"
            allow-clear
            placeholder="状态"
          >
            <a-select-option value="draft">草稿</a-select-option>
          <a-select-option value="published">已发布</a-select-option>
          </a-select>
          <a-button :loading="loading" @click="loadContents">刷新</a-button>
          <a-button type="primary" @click="openCreateForm">新增文章</a-button>
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
        :data-source="contents"
        :loading="loading"
        :locale="{ emptyText: '暂无内容文章。' }"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'published' ? 'green' : 'default'">
              {{ $statusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="openEditForm(record)">编辑</a-button>
              <a-button
                size="small"
                type="primary"
                :disabled="record.status === 'published'"
                @click="publishContent(record)"
              >
                发布
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="formOpen"
      title="内容文章"
      :confirm-loading="saving"
      @ok="submitForm"
    >
      <a-form layout="vertical">
        <a-form-item label="栏目编码" required>
          <a-input v-model:value="formState.channelCode" class="full-input" placeholder="请输入栏目编码" />
        </a-form-item>
        <a-form-item label="文章标题" required>
          <a-input v-model:value="formState.title" class="full-input" placeholder="请输入文章标题" />
        </a-form-item>
        <a-form-item label="文章摘要">
          <a-textarea v-model:value="formState.summary" :rows="2" placeholder="请输入文章摘要" />
        </a-form-item>
        <a-form-item label="文章正文">
          <a-textarea v-model:value="formState.bodyContent" :rows="5" placeholder="请输入文章正文" />
        </a-form-item>
        <a-form-item label="文章状态" required>
          <a-select v-model:value="formState.status" class="full-input">
            <a-select-option value="draft">草稿</a-select-option>
            <a-select-option value="published">发布</a-select-option>
          </a-select>
        </a-form-item>
        <span class="permission-code">cms:content:save</span>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { onMounted, reactive, ref } from 'vue';

import {
  changeCmsContentStatus,
  listCmsContents,
  saveCmsContent,
  type CmsContent,
  type CmsContentSavePayload,
} from '@/api/cms/content';
import { requireAdminTenantId, requireAdminUserId } from '@/utils/adminContext';

/** 内容文章列表。 */
const contents = ref<CmsContent[]>([]);
/** 栏目编码筛选值。 */
const channelCode = ref('');
/** 文章状态筛选值。 */
const status = ref<string | undefined>();
/** 列表加载状态。 */
const loading = ref(false);
/** 保存状态。 */
const saving = ref(false);
/** 列表加载错误提示。 */
const errorMessage = ref('');
/** 文章表单弹窗状态。 */
const formOpen = ref(false);
/** 文章表单数据，提交时补齐租户和作者。 */
const formState = reactive<CmsContentSavePayload>({
  id: undefined,
  tenantId: '',
  channelCode: '',
  title: '',
  summary: '',
  bodyContent: '',
  status: 'draft',
  authorId: undefined,
});

/** 内容文章表格列。 */
const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 220 },
  { title: '栏目', dataIndex: 'channelCode', key: 'channelCode', width: 120 },
  { title: '摘要', dataIndex: 'summary', key: 'summary' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '作者', dataIndex: 'authorId', key: 'authorId', width: 90 },
  { title: '操作', key: 'action', width: 140 },
];

/**
 * 加载内容文章列表。
 */
async function loadContents(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    contents.value = await listCmsContents(channelCode.value || undefined, status.value);
  } catch (error) {
    contents.value = [];
    errorMessage.value = error instanceof Error ? error.message : '内容文章加载失败';
  } finally {
    loading.value = false;
  }
}

/**
 * 打开新增文章表单。
 */
function openCreateForm(): void {
  resetForm();
  formOpen.value = true;
}

/**
 * 打开编辑文章表单。
 *
 * @param content 当前选中的内容文章
 */
function openEditForm(content: CmsContent): void {
  formState.id = content.id;
  formState.tenantId = requireAdminTenantId();
  formState.channelCode = content.channelCode;
  formState.title = content.title;
  formState.summary = content.summary || '';
  formState.bodyContent = content.bodyContent || '';
  formState.status = content.status || 'draft';
  formState.authorId = content.authorId || requireAdminUserId();
  formOpen.value = true;
}

/**
 * 提交文章表单。
 */
async function submitForm(): Promise<void> {
  if (!validateForm()) {
    return;
  }
  saving.value = true;
  try {
    await saveCmsContent({
      id: formState.id,
      tenantId: requireAdminTenantId(),
      channelCode: formState.channelCode.trim(),
      title: formState.title.trim(),
      summary: formState.summary?.trim(),
      bodyContent: formState.bodyContent?.trim(),
      status: formState.status || 'draft',
      authorId: requireAdminUserId(),
    });
    message.success('内容文章已保存');
    formOpen.value = false;
    await loadContents();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '内容文章保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 重置文章表单。
 */
function resetForm(): void {
  formState.id = undefined;
  formState.tenantId = requireAdminTenantId();
  formState.channelCode = channelCode.value || '';
  formState.title = '';
  formState.summary = '';
  formState.bodyContent = '';
  formState.status = 'draft';
  formState.authorId = requireAdminUserId();
}

/**
 * 校验文章表单。
 *
 * @returns 表单是否满足提交条件
 */
function validateForm(): boolean {
  if (!formState.channelCode.trim()) {
    message.warning('请输入栏目编码');
    return false;
  }
  if (!formState.title.trim()) {
    message.warning('请输入文章标题');
    return false;
  }
  return true;
}

/**
 * 发布内容文章。
 *
 * @param content 内容文章
 */
async function publishContent(content: CmsContent): Promise<void> {
  try {
    await changeCmsContentStatus(content.id, 'published');
    message.success('内容文章已发布');
    await loadContents();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '内容文章发布失败');
  }
}

onMounted(() => {
  void loadContents();
});
</script>

<style scoped>
.cms-page {
  min-width: 0;
}

.state-alert {
  margin-bottom: 12px;
}

.channel-input {
  width: 140px;
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
