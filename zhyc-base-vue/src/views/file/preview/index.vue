<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="file-preview-page">
    <a-card title="预览记录" :bordered="false">
      <template #extra>
        <a-space>
          <a-input-search
            v-model:value="fileCode"
            class="file-code-input"
            placeholder="文件编码"
            allow-clear
            @search="loadPreviewLogs"
          />
          <a-button :loading="loading" @click="loadPreviewLogs">刷新</a-button>
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
        :data-source="previewLogs"
        :loading="loading"
        :locale="{ emptyText: '暂无文件预览记录。' }"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'result'">
            <a-tag :color="record.result === 'success' ? 'green' : 'red'">
              {{ record.result }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'previewUrl'">
            <a-typography-text copyable>{{ record.previewUrl }}</a-typography-text>
          </template>
        </template>
      </a-table>

      <div class="permission-tip">权限编码：<span>file:preview:query</span></div>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { listFilePreviewLogs, type FilePreviewLog } from '@/api/file/object';
import { requireAdminTenantId } from '@/utils/adminContext';

/** 文件预览记录列表。 */
const previewLogs = ref<FilePreviewLog[]>([]);
/** 文件编码查询条件。 */
const fileCode = ref('');
/** 列表加载状态。 */
const loading = ref(false);
/** 列表加载错误提示。 */
const errorMessage = ref('');

/** 文件预览记录表格列。 */
const columns = [
  { title: '日志 ID', dataIndex: 'id', key: 'id', width: 100 },
  { title: '租户编码', dataIndex: 'tenantId', key: 'tenantId', width: 140 },
  { title: '文件编码', dataIndex: 'fileCode', key: 'fileCode', width: 180 },
  { title: '预览类型', dataIndex: 'previewType', key: 'previewType', width: 110 },
  { title: '预览地址', dataIndex: 'previewUrl', key: 'previewUrl' },
  { title: '结果', dataIndex: 'result', key: 'result', width: 100 },
  { title: '耗时(ms)', dataIndex: 'costMs', key: 'costMs', width: 110 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 170 },
];

/**
 * 加载当前租户下的文件预览记录。
 */
async function loadPreviewLogs(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    requireAdminTenantId();
    previewLogs.value = await listFilePreviewLogs(fileCode.value.trim() || undefined);
  } catch (error) {
    previewLogs.value = [];
    errorMessage.value = error instanceof Error ? error.message : '文件预览记录加载失败';
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadPreviewLogs();
});
</script>

<style scoped>
.file-preview-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.file-code-input {
  width: 220px;
}

.state-alert {
  margin-bottom: 12px;
}

.permission-tip {
  margin-top: 12px;
  color: #64748b;
  font-size: 12px;
}

.permission-tip span {
  color: #334155;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace;
}
</style>
