<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="openapi-api-key-page">
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="10">
        <a-card title="API Key" :bordered="false">
          <template #extra>
            <a-space>
              <a-input v-model:value="tenantId" class="tenant-id" />
              <a-input v-model:value="appCode" class="app-code" placeholder="应用编码" />
              <a-button :loading="loading" @click="loadApiKeys">查询</a-button>
            </a-space>
          </template>

          <a-table
            row-key="accessKey"
            size="small"
            :columns="columns"
            :data-source="apiKeys"
            :loading="loading"
            :pagination="$tablePagination"
            :custom-row="buildApiKeyRowProps"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
                  {{ $statusLabel(record.status) }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="14">
        <a-card title="密钥配置" :bordered="false">
          <template #extra>
            <a-space>
              <a-button @click="resetCommand">新建密钥</a-button>
              <a-button :disabled="!command.accessKey" :loading="rotating" @click="handleRotateSecret">
                轮换 Secret
              </a-button>
              <a-button type="primary" :loading="saving" @click="handleSave">保存密钥</a-button>
            </a-space>
          </template>

          <a-alert message="Secret 只用于保存或轮换，列表只展示掩码。" type="info" show-icon />

          <a-form class="api-key-form" layout="vertical" :model="command">
            <a-row :gutter="16">
              <a-col :xs="24" :md="12">
                <a-form-item label="应用编码" required>
                  <a-input v-model:value="command.appCode" placeholder="demo_app" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="Access Key" required>
                  <a-input v-model:value="command.accessKey" placeholder="ak_demo" />
                </a-form-item>
              </a-col>
              <a-col :xs="24">
                <a-form-item label="Secret 密文" required>
                  <a-input-password v-model:value="command.secretCipher" placeholder="secret cipher" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="状态" required>
                  <a-select v-model:value="command.status" :options="statusOptions" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="过期时间">
                  <a-input v-model:value="command.expireAt" placeholder="2026-12-31T23:59:59" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>

          <span class="permission-code">openapi:api-key:save</span>
        </a-card>
      </a-col>
    </a-row>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { message } from 'ant-design-vue';

import {
  listOpenApiApiKeys,
  rotateOpenApiApiKeySecret,
  saveOpenApiApiKey,
  type OpenApiApiKeyResponse,
  type OpenApiApiKeySaveRequest,
} from '@/api/openapi/api-key';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 当前开发者应用编码。 */
const appCode = ref('');
/** API Key 列表。 */
const apiKeys = ref<OpenApiApiKeyResponse[]>([]);
/** 列表加载状态。 */
const loading = ref(false);
/** 保存提交状态。 */
const saving = ref(false);
/** Secret 轮换提交状态。 */
const rotating = ref(false);

/** API Key 权限编码，供页面按钮和后续权限指令统一引用。 */
const permissionCodes = {
  /** 保存 API Key 权限。 */
  save: 'openapi:api-key:save',
  /** 轮换 API Key Secret 权限。 */
  rotate: 'openapi:api-key:rotate',
};

/** API Key 状态下拉选项。 */
const statusOptions = [
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' },
];

/** API Key 表格列定义。 */
const columns = [
  { title: 'Access Key', dataIndex: 'accessKey', key: 'accessKey' },
  { title: 'Secret 掩码', dataIndex: 'secretMask', key: 'secretMask' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '过期时间', dataIndex: 'expireAt', key: 'expireAt', width: 180 },
];

/** 当前编辑的 API Key 保存命令。 */
const command = reactive<OpenApiApiKeySaveRequest>(createEmptyCommand());

/**
 * 创建空 API Key 保存命令。
 *
 * @returns API Key 保存命令
 */
function createEmptyCommand(): OpenApiApiKeySaveRequest {
  return {
    tenantId: tenantId.value,
    appCode: appCode.value,
    accessKey: '',
    secretCipher: '',
    status: 'enabled',
    expireAt: '',
  };
}

/**
 * 将 API Key 响应复制到编辑命令。
 *
 * @param apiKey API Key 响应
 */
function applyApiKey(apiKey: OpenApiApiKeyResponse): void {
  command.tenantId = tenantId.value;
  command.appCode = apiKey.appCode;
  command.accessKey = apiKey.accessKey;
  command.secretCipher = '';
  command.status = apiKey.status;
  command.expireAt = apiKey.expireAt ?? '';
}

/**
 * 重置 API Key 编辑表单。
 */
function resetCommand(): void {
  Object.assign(command, createEmptyCommand());
}

/**
 * 查询当前应用的 API Key。
 */
async function loadApiKeys(): Promise<void> {
  if (!appCode.value) {
    message.error('请先输入应用编码');
    return;
  }
  loading.value = true;
  try {
    apiKeys.value = await listOpenApiApiKeys(syncTenantContext(), appCode.value);
  } catch (error) {
    message.error(error instanceof Error ? error.message : 'API Key 查询失败');
  } finally {
    loading.value = false;
  }
}

/**
 * 保存 API Key。
 */
async function handleSave(): Promise<void> {
  saving.value = true;
  try {
    command.tenantId = syncTenantContext();
    appCode.value = command.appCode;
    await saveOpenApiApiKey(command);
    await loadApiKeys();
    message.success('API Key 已保存');
  } catch (error) {
    message.error(error instanceof Error ? error.message : 'API Key 保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 轮换当前 API Key Secret。
 */
async function handleRotateSecret(): Promise<void> {
  if (!command.accessKey) {
    message.error('请先选择或输入 Access Key');
    return;
  }
  if (!command.secretCipher) {
    message.error('请先输入新 Secret 密文');
    return;
  }
  rotating.value = true;
  try {
    command.tenantId = syncTenantContext();
    appCode.value = command.appCode;
    await rotateOpenApiApiKeySecret(command.accessKey, {
      tenantId: command.tenantId,
      appCode: command.appCode,
      secretCipher: command.secretCipher,
      expireAt: command.expireAt,
    });
    command.secretCipher = '';
    await loadApiKeys();
    message.success('API Key Secret 已轮换');
  } catch (error) {
    message.error(error instanceof Error ? error.message : 'API Key Secret 轮换失败');
  } finally {
    rotating.value = false;
  }
}

/**
 * 构建 API Key 表格行属性。
 *
 * @param record API Key 响应
 * @returns 表格行属性
 */
function buildApiKeyRowProps(record: OpenApiApiKeyResponse): { onClick: () => void } {
  return {
    onClick: () => applyApiKey(record),
  };
}

/**
 * 同步后台租户到 API Key 表单。
 *
 * @returns 当前租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId();
  tenantId.value = currentTenantId;
  command.tenantId = currentTenantId;
  return currentTenantId;
}
</script>

<style scoped>
.openapi-api-key-page {
  min-width: 0;
}

.tenant-id {
  width: 160px;
}

.app-code {
  width: 180px;
}

.api-key-form {
  margin-top: 16px;
}

.permission-code {
  display: none;
}
</style>
