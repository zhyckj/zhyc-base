<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="file-page">
    <a-card title="存储配置" :bordered="false">
      <template #extra>
        <a-space>
          <a-button :loading="loading" @click="loadConfigs">刷新</a-button>
          <a-button type="primary" @click="openCreateForm">新增配置</a-button>
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
        row-key="storageCode"
        size="small"
        :columns="columns"
        :data-source="configs"
        :loading="loading"
        :locale="{ emptyText: '暂无文件存储配置。' }"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">{{ $statusLabel(record.status) }}</a-tag>
          </template>
          <template v-if="column.key === 'defaultFlag'">
            <a-tag :color="record.defaultFlag ? 'blue' : 'default'">
              {{ record.defaultFlag ? '默认' : '普通' }}
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
      title="存储配置"
      :confirm-loading="saving"
      @ok="submitForm"
    >
      <a-form layout="vertical">
        <a-form-item label="配置编码" required>
          <a-input
            v-model:value="formState.storageCode"
            class="full-input"
            placeholder="例如 local-main"
          />
        </a-form-item>
        <a-form-item label="配置名称" required>
          <a-input
            v-model:value="formState.storageName"
            class="full-input"
            placeholder="请输入配置名称"
          />
        </a-form-item>
        <a-form-item label="存储类型" required>
          <a-select v-model:value="formState.storageType" class="full-input">
            <a-select-option value="local">本地存储</a-select-option>
            <a-select-option value="minio">MinIO</a-select-option>
            <a-select-option value="oss">阿里云 OSS</a-select-option>
            <a-select-option value="s3">Amazon S3</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="端点或根路径" required>
          <a-input
            v-model:value="formState.endpoint"
            class="full-input"
            placeholder="请输入访问端点或本地根路径"
          />
        </a-form-item>
        <a-form-item label="配置状态" required>
          <a-select v-model:value="formState.status" class="full-input">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="默认配置">
          <a-switch v-model:checked="formState.defaultFlag" />
        </a-form-item>
        <span class="permission-code">file:storage:save</span>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { onMounted, reactive, ref } from 'vue';

import {
  listFileStorageConfigs,
  saveFileStorageConfig,
  type FileStorageConfig,
  type FileStorageConfigSavePayload,
} from '@/api/file/storage';
import { requireAdminTenantId } from '@/utils/adminContext';

/** 文件存储配置列表。 */
const configs = ref<FileStorageConfig[]>([]);
/** 列表加载状态。 */
const loading = ref(false);
/** 保存状态。 */
const saving = ref(false);
/** 列表加载错误提示。 */
const errorMessage = ref('');
/** 存储配置表单弹窗状态。 */
const formOpen = ref(false);
/** 存储配置表单数据，提交时会重新写入当前租户。 */
const formState = reactive<FileStorageConfigSavePayload>({
  tenantId: '',
  storageCode: '',
  storageName: '',
  storageType: 'local',
  endpoint: '',
  status: 'enabled',
  defaultFlag: false,
});

/** 文件存储配置表格列。 */
const columns = [
  { title: '配置编码', dataIndex: 'storageCode', key: 'storageCode', width: 170 },
  { title: '配置名称', dataIndex: 'storageName', key: 'storageName', width: 150 },
  { title: '类型', dataIndex: 'storageType', key: 'storageType', width: 100 },
  { title: '端点', dataIndex: 'endpoint', key: 'endpoint' },
  { title: '默认', dataIndex: 'defaultFlag', key: 'defaultFlag', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 100 },
];

/**
 * 加载文件存储配置列表。
 */
async function loadConfigs(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    configs.value = await listFileStorageConfigs();
  } catch (error) {
    configs.value = [];
    errorMessage.value = error instanceof Error ? error.message : '文件存储配置加载失败';
  } finally {
    loading.value = false;
  }
}

/**
 * 打开新增存储配置表单。
 */
function openCreateForm(): void {
  resetForm();
  formOpen.value = true;
}

/**
 * 打开编辑存储配置表单。
 *
 * @param config 当前选中的存储配置
 */
function openEditForm(config: FileStorageConfig): void {
  formState.tenantId = requireAdminTenantId();
  formState.storageCode = config.storageCode;
  formState.storageName = config.storageName;
  formState.storageType = config.storageType;
  formState.endpoint = config.endpoint;
  formState.status = config.status || 'enabled';
  formState.defaultFlag = config.defaultFlag;
  formOpen.value = true;
}

/**
 * 提交存储配置表单。
 */
async function submitForm(): Promise<void> {
  if (!validateForm()) {
    return;
  }
  saving.value = true;
  try {
    await saveFileStorageConfig({
      tenantId: requireAdminTenantId(),
      storageCode: formState.storageCode.trim(),
      storageName: formState.storageName.trim(),
      storageType: formState.storageType,
      endpoint: formState.endpoint.trim(),
      status: formState.status || 'enabled',
      defaultFlag: formState.defaultFlag,
    });
    message.success('存储配置已保存');
    formOpen.value = false;
    await loadConfigs();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '存储配置保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 重置存储配置表单。
 */
function resetForm(): void {
  formState.tenantId = requireAdminTenantId();
  formState.storageCode = '';
  formState.storageName = '';
  formState.storageType = 'local';
  formState.endpoint = '';
  formState.status = 'enabled';
  formState.defaultFlag = false;
}

/**
 * 校验存储配置表单。
 *
 * @returns 表单是否满足提交条件
 */
function validateForm(): boolean {
  if (!formState.storageCode.trim()) {
    message.warning('请输入配置编码');
    return false;
  }
  if (!formState.storageName.trim()) {
    message.warning('请输入配置名称');
    return false;
  }
  if (!formState.storageType.trim()) {
    message.warning('请选择存储类型');
    return false;
  }
  if (!formState.endpoint.trim()) {
    message.warning('请输入端点或根路径');
    return false;
  }
  return true;
}

onMounted(() => {
  void loadConfigs();
});
</script>

<style scoped>
.file-page {
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
