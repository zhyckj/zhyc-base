<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="openapi-oauth-client-page">
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="11">
        <a-card title="OAuth2 客户端" :bordered="false">
          <template #extra>
            <a-space>
              <a-input v-model:value="tenantId" class="tenant-id" />
              <a-input v-model:value="appCode" class="app-code" placeholder="应用编码" />
              <a-button :loading="loading" @click="loadClients">查询</a-button>
            </a-space>
          </template>

          <a-table
            row-key="clientId"
            size="small"
            :columns="columns"
            :data-source="clients"
            :loading="loading"
            :pagination="$tablePagination"
            :custom-row="buildClientRowProps"
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

      <a-col :xs="24" :lg="13">
        <a-card title="客户端映射" :bordered="false">
          <template #extra>
            <a-space>
              <a-button @click="resetCommand">新建映射</a-button>
              <a-button type="primary" :loading="saving" @click="handleSave">保存映射</a-button>
            </a-space>
          </template>

          <a-alert message="OAuth2 客户端映射用于把开发者应用和认证中心客户端绑定，供第三方应用授权访问开放 API。" type="info" show-icon />

          <a-form class="client-form" layout="vertical" :model="command">
            <a-row :gutter="16">
              <a-col :xs="24" :md="12">
                <a-form-item label="应用编码" required>
                  <a-input v-model:value="command.appCode" placeholder="purchase-app" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="客户端 ID" required>
                  <a-input v-model:value="command.clientId" placeholder="purchase-portal-client" />
                </a-form-item>
              </a-col>
              <a-col :xs="24">
                <a-form-item label="授权范围" required>
                  <a-input v-model:value="command.allowedScopes" placeholder="openid profile purchase.request" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="映射状态" required>
                  <a-select v-model:value="command.status" :options="statusOptions" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>

          <span class="permission-code">openapi:oauth-client:save</span>
        </a-card>
      </a-col>
    </a-row>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { message } from 'ant-design-vue';

import {
  listOpenApiOauthClients,
  saveOpenApiOauthClient,
  type OpenApiOauthClientResponse,
  type OpenApiOauthClientSaveRequest,
} from '@/api/openapi/oauth-client';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 当前开发者应用编码。 */
const appCode = ref('');
/** OAuth2 客户端映射列表。 */
const clients = ref<OpenApiOauthClientResponse[]>([]);
/** 列表加载状态。 */
const loading = ref(false);
/** 保存提交状态。 */
const saving = ref(false);

/** OAuth2 客户端映射权限编码，供页面按钮和后续权限指令统一引用。 */
const permissionCodes = {
  /** 保存 OAuth2 客户端映射权限。 */
  save: 'openapi:oauth-client:save',
};

/** 映射状态下拉选项。 */
const statusOptions = [
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' },
];

/** OAuth2 客户端映射表格列定义。 */
const columns = [
  { title: '客户端 ID', dataIndex: 'clientId', key: 'clientId' },
  { title: '授权范围', dataIndex: 'allowedScopes', key: 'allowedScopes' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
];

/** 当前编辑的 OAuth2 客户端映射保存命令。 */
const command = reactive<OpenApiOauthClientSaveRequest>(createEmptyCommand());

/**
 * 创建空 OAuth2 客户端映射保存命令。
 *
 * @returns OAuth2 客户端映射保存命令
 */
function createEmptyCommand(): OpenApiOauthClientSaveRequest {
  return {
    tenantId: tenantId.value,
    appCode: appCode.value,
    clientId: '',
    allowedScopes: 'openid profile',
    status: 'enabled',
  };
}

/**
 * 将 OAuth2 客户端映射响应复制到编辑命令。
 *
 * @param client OAuth2 客户端映射响应
 */
function applyClient(client: OpenApiOauthClientResponse): void {
  command.tenantId = tenantId.value;
  command.appCode = appCode.value;
  command.clientId = client.clientId;
  command.allowedScopes = client.allowedScopes;
  command.status = client.status;
}

/**
 * 重置 OAuth2 客户端映射编辑表单。
 */
function resetCommand(): void {
  Object.assign(command, createEmptyCommand());
}

/**
 * 查询当前应用的 OAuth2 客户端映射。
 */
async function loadClients(): Promise<void> {
  if (!appCode.value) {
    message.error('请先输入应用编码');
    return;
  }
  loading.value = true;
  try {
    clients.value = await listOpenApiOauthClients(syncTenantContext(), appCode.value);
  } catch (error) {
    message.error(error instanceof Error ? error.message : 'OAuth2 客户端映射查询失败');
  } finally {
    loading.value = false;
  }
}

/**
 * 保存 OAuth2 客户端映射。
 */
async function handleSave(): Promise<void> {
  saving.value = true;
  try {
    command.tenantId = syncTenantContext();
    appCode.value = command.appCode;
    await saveOpenApiOauthClient(command);
    await loadClients();
    message.success('OAuth2 客户端映射已保存');
  } catch (error) {
    message.error(error instanceof Error ? error.message : 'OAuth2 客户端映射保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 构建 OAuth2 客户端映射表格行属性。
 *
 * @param record OAuth2 客户端映射响应
 * @returns 表格行属性
 */
function buildClientRowProps(record: OpenApiOauthClientResponse): { onClick: () => void } {
  return {
    onClick: () => applyClient(record),
  };
}

/**
 * 同步后台租户到 OAuth2 客户端表单。
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
.openapi-oauth-client-page {
  min-width: 0;
}

.tenant-id {
  width: 160px;
}

.app-code {
  width: 180px;
}

.client-form {
  margin-top: 16px;
}

.permission-code {
  display: none;
}
</style>
