<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="file-page">
    <a-card title="文件对象" :bordered="false">
      <template #extra>
        <a-space>
          <a-input-search
            v-model:value="keyword"
            class="keyword-input"
            placeholder="文件名"
            allow-clear
            @search="loadFiles"
          />
          <a-button :loading="loading" @click="loadFiles">刷新</a-button>
          <a-upload
            :show-upload-list="false"
            :custom-request="uploadSelectedFile"
            :before-upload="validateUploadFile"
          >
            <a-button type="primary" :loading="uploading">上传文件</a-button>
          </a-upload>
          <a-button type="primary" @click="openRegisterForm">登记文件</a-button>
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
        row-key="fileCode"
        size="small"
        :columns="columns"
        :data-source="files"
        :loading="loading"
        :locale="{ emptyText: '暂无文件对象。' }"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'fileStatus'">
            <a-tag :color="record.fileStatus === 'stored' ? 'green' : 'default'">
              {{ record.fileStatus }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" :loading="previewing === record.fileCode" @click="previewFile(record)">
              预览
            </a-button>
          </template>
        </template>
      </a-table>

      <a-tabs class="preview-tabs" size="small">
        <a-tab-pane key="logs" tab="预览日志">
          <a-table
            row-key="id"
            size="small"
            :columns="logColumns"
            :data-source="previewLogs"
            :locale="{ emptyText: '暂无文件预览日志。' }"
            :pagination="$tablePagination"
          />
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <a-modal
      v-model:open="registerFormOpen"
      title="登记文件"
      :confirm-loading="registering"
      @ok="submitRegisterForm"
    >
      <a-form layout="vertical">
        <a-form-item label="存储配置编码" required>
          <a-input
            v-model:value="registerForm.storageCode"
            class="full-input"
            placeholder="请输入已启用的存储配置编码"
          />
        </a-form-item>
        <a-form-item label="原始文件名" required>
          <a-input
            v-model:value="registerForm.originalName"
            class="full-input"
            placeholder="请输入原始文件名"
          />
        </a-form-item>
        <a-form-item label="内容类型" required>
          <a-input
            v-model:value="registerForm.contentType"
            class="full-input"
            placeholder="例如 application/pdf"
          />
        </a-form-item>
        <a-form-item label="文件大小（字节）" required>
          <a-input-number
            v-model:value="registerForm.fileSize"
            class="full-input"
            :min="1"
            :precision="0"
          />
        </a-form-item>
        <a-form-item label="对象键或相对路径" required>
          <a-input
            v-model:value="registerForm.objectKey"
            class="full-input"
            placeholder="请输入对象键或相对路径"
          />
        </a-form-item>
        <span class="permission-code">file:object:create</span>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { onMounted, reactive, ref } from 'vue';

import {
  createFilePreview,
  listFileObjects,
  listFilePreviewLogs,
  registerFileObject,
  uploadFileObject,
  type FilePreviewLog,
  type FileObjectRecord,
  type FileObjectRegisterPayload,
} from '@/api/file/object';
import { requireAdminTenantId, requireAdminUserId } from '@/utils/adminContext';

/** 文件对象列表。 */
const files = ref<FileObjectRecord[]>([]);
/** 文件预览日志列表。 */
const previewLogs = ref<FilePreviewLog[]>([]);
/** 文件名关键词。 */
const keyword = ref('');
/** 列表加载状态。 */
const loading = ref(false);
/** 登记状态。 */
const registering = ref(false);
/** 文件上传状态。 */
const uploading = ref(false);
/** 正在预览的文件编码。 */
const previewing = ref('');
/** 列表加载错误提示。 */
const errorMessage = ref('');
/** 文件登记表单弹窗状态。 */
const registerFormOpen = ref(false);
/** 文件登记表单数据，租户和上传人提交时从当前上下文补齐。 */
const registerForm = reactive<FileObjectRegisterPayload>({
  tenantId: '',
  storageCode: '',
  originalName: '',
  contentType: '',
  fileSize: 1,
  objectKey: '',
  uploaderId: undefined,
});

/** 文件对象表格列。 */
const columns = [
  { title: '文件编码', dataIndex: 'fileCode', key: 'fileCode', width: 180 },
  { title: '文件名', dataIndex: 'originalName', key: 'originalName' },
  { title: '内容类型', dataIndex: 'contentType', key: 'contentType', width: 160 },
  { title: '大小(B)', dataIndex: 'fileSize', key: 'fileSize', width: 110 },
  { title: '存储配置', dataIndex: 'storageCode', key: 'storageCode', width: 140 },
  { title: '状态', dataIndex: 'fileStatus', key: 'fileStatus', width: 100 },
  { title: '操作', key: 'action', width: 90 },
];

/** 文件预览日志表格列。 */
const logColumns = [
  { title: '文件编码', dataIndex: 'fileCode', key: 'fileCode', width: 180 },
  { title: '预览类型', dataIndex: 'previewType', key: 'previewType', width: 110 },
  { title: '预览地址', dataIndex: 'previewUrl', key: 'previewUrl' },
  { title: '结果', dataIndex: 'result', key: 'result', width: 100 },
  { title: '耗时(ms)', dataIndex: 'costMs', key: 'costMs', width: 110 },
];

/**
 * 加载文件对象列表。
 */
async function loadFiles(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    const page = await listFileObjects(keyword.value || undefined);
    files.value = page.records;
    await loadPreviewLogs(files.value[0]?.fileCode);
  } catch (error) {
    files.value = [];
    previewLogs.value = [];
    errorMessage.value = error instanceof Error ? error.message : '文件对象加载失败';
  } finally {
    loading.value = false;
  }
}

/**
 * 打开文件登记表单。
 */
function openRegisterForm(): void {
  resetRegisterForm();
  registerFormOpen.value = true;
}

/**
 * 提交文件登记表单。
 */
async function submitRegisterForm(): Promise<void> {
  if (!validateRegisterForm()) {
    return;
  }
  registering.value = true;
  try {
    await registerFileObject({
      tenantId: requireAdminTenantId(),
      storageCode: registerForm.storageCode.trim(),
      originalName: registerForm.originalName.trim(),
      contentType: registerForm.contentType.trim(),
      fileSize: registerForm.fileSize,
      objectKey: registerForm.objectKey.trim(),
      uploaderId: requireAdminUserId(),
    });
    message.success('文件对象已登记');
    registerFormOpen.value = false;
    await loadFiles();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '文件对象登记失败');
  } finally {
    registering.value = false;
  }
}

/**
 * 校验待上传文件。
 *
 * @param file 待上传文件
 * @returns 是否允许上传
 */
function validateUploadFile(file: File): boolean {
  const maxSize = 100 * 1024 * 1024;
  if (file.size <= 0) {
    message.warning('上传文件不能为空');
    return false;
  }
  if (file.size > maxSize) {
    message.warning('上传文件不能超过 100MB');
    return false;
  }
  return true;
}

/**
 * 上传选中的文件并刷新文件对象列表。
 *
 * @param options Ant Design Vue 上传参数
 */
async function uploadSelectedFile(options: {
  file?: string | Blob | File;
  onSuccess?: (response: unknown) => void;
  onError?: (error: Error) => void;
}): Promise<void> {
  const file = options.file instanceof File ? options.file : null;
  if (!file) {
    const error = new Error('请选择有效文件');
    options.onError?.(error);
    message.error(error.message);
    return;
  }
  uploading.value = true;
  try {
    const result = await uploadFileObject(file);
    options.onSuccess?.(result);
    message.success(`文件已上传并登记：${result.fileCode}`);
    await loadFiles();
  } catch (error) {
    const uploadError = error instanceof Error ? error : new Error('文件上传失败');
    options.onError?.(uploadError);
    message.error(uploadError.message);
  } finally {
    uploading.value = false;
  }
}

/**
 * 重置文件登记表单。
 */
function resetRegisterForm(): void {
  registerForm.tenantId = requireAdminTenantId();
  registerForm.storageCode = '';
  registerForm.originalName = '';
  registerForm.contentType = '';
  registerForm.fileSize = 1;
  registerForm.objectKey = '';
  registerForm.uploaderId = requireAdminUserId();
}

/**
 * 校验文件登记表单。
 *
 * @returns 表单是否满足提交条件
 */
function validateRegisterForm(): boolean {
  if (!registerForm.storageCode.trim()) {
    message.warning('请输入存储配置编码');
    return false;
  }
  if (!registerForm.originalName.trim()) {
    message.warning('请输入原始文件名');
    return false;
  }
  if (!registerForm.contentType.trim()) {
    message.warning('请输入内容类型');
    return false;
  }
  if (!Number.isFinite(registerForm.fileSize) || registerForm.fileSize <= 0) {
    message.warning('请输入有效的文件大小');
    return false;
  }
  if (!registerForm.objectKey.trim()) {
    message.warning('请输入对象键或相对路径');
    return false;
  }
  return true;
}

/**
 * 创建文件预览并刷新日志。
 *
 * @param record 文件对象记录
 */
async function previewFile(record: FileObjectRecord): Promise<void> {
  previewing.value = record.fileCode;
  try {
    const result = await createFilePreview(record.fileCode, resolvePreviewType(record.contentType), requireAdminTenantId());
    message.success(`预览已生成：${result.previewUrl}`);
    await loadPreviewLogs(record.fileCode);
  } catch (error) {
    message.error(error instanceof Error ? error.message : '文件预览生成失败');
  } finally {
    previewing.value = '';
  }
}

/**
 * 加载文件预览日志。
 *
 * @param fileCode 文件业务编码
 */
async function loadPreviewLogs(fileCode?: string): Promise<void> {
  if (!fileCode) {
    previewLogs.value = [];
    return;
  }
  try {
    previewLogs.value = await listFilePreviewLogs(fileCode);
  } catch {
    previewLogs.value = [];
  }
}

/**
 * 根据内容类型推导预览类型。
 *
 * @param contentType 文件内容类型
 * @returns 预览类型
 */
function resolvePreviewType(contentType: string): string {
  if (contentType.includes('pdf')) {
    return 'pdf';
  }
  if (contentType.includes('image')) {
    return 'image';
  }
  if (contentType.includes('text')) {
    return 'text';
  }
  return 'default';
}

onMounted(() => {
  void loadFiles();
});
</script>

<style scoped>
.file-page {
  min-width: 0;
}

.state-alert {
  margin-bottom: 12px;
}

.keyword-input {
  width: 180px;
}

.full-input {
  width: 100%;
}

.permission-code {
  display: none;
}

.preview-tabs {
  margin-top: 16px;
}
</style>
