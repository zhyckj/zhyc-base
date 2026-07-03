<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="openapi-permission-page">
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="11">
        <a-card title="API 授权" :bordered="false">
          <template #extra>
            <a-space>
              <a-input v-model:value="tenantId" class="tenant-id" />
              <a-input v-model:value="appCode" class="app-code" placeholder="应用编码" />
              <a-button :loading="loading" @click="loadPermissions">查询</a-button>
            </a-space>
          </template>

          <a-table
            row-key="apiCode"
            size="small"
            :columns="columns"
            :data-source="permissions"
            :loading="loading"
            :pagination="$tablePagination"
            :custom-row="buildPermissionRowProps"
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
        <a-card title="授权配置" :bordered="false">
          <template #extra>
            <a-space>
              <a-button @click="resetCommand">新建授权</a-button>
              <a-button type="primary" :loading="saving" @click="handleSave">保存授权</a-button>
            </a-space>
          </template>

          <a-alert message="授权按租户和应用编码隔离，用于限制第三方应用可调用的开放 API。" type="info" show-icon />

          <a-form class="permission-form" layout="vertical" :model="command">
            <a-row :gutter="16">
              <a-col :xs="24" :md="12">
                <a-form-item label="应用编码" required>
                  <a-input v-model:value="command.appCode" placeholder="demo_app" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="API 编码" required>
                  <a-input v-model:value="command.apiCode" placeholder="purchase.request.create" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="API 名称" required>
                  <a-input v-model:value="command.apiName" placeholder="创建采购申请" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="HTTP 方法" required>
                  <a-select v-model:value="command.httpMethod" :options="methodOptions" />
                </a-form-item>
              </a-col>
              <a-col :xs="24">
                <a-form-item label="路径规则" required>
                  <a-input v-model:value="command.pathPattern" placeholder="/open/purchase/requests" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="授权状态" required>
                  <a-select v-model:value="command.status" :options="statusOptions" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>

          <span class="permission-code">openapi:api-permission:save</span>
        </a-card>
      </a-col>
    </a-row>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { message } from 'ant-design-vue';

import {
  listOpenApiPermissions,
  saveOpenApiPermission,
  type OpenApiPermissionResponse,
  type OpenApiPermissionSaveRequest,
} from '@/api/openapi/api-permission';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 当前开发者应用编码。 */
const appCode = ref('');
/** 开放 API 授权列表。 */
const permissions = ref<OpenApiPermissionResponse[]>([]);
/** 列表加载状态。 */
const loading = ref(false);
/** 保存提交状态。 */
const saving = ref(false);

/** API 授权权限编码，供页面按钮和后续权限指令统一引用。 */
const permissionCodes = {
  /** 保存 API 授权权限。 */
  save: 'openapi:api-permission:save',
};

/** HTTP 方法下拉选项。 */
const methodOptions = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'].map((method) => ({
  label: method,
  value: method,
}));

/** 授权状态下拉选项。 */
const statusOptions = [
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' },
];

/** 授权表格列定义。 */
const columns = [
  { title: 'API 编码', dataIndex: 'apiCode', key: 'apiCode' },
  { title: 'API 名称', dataIndex: 'apiName', key: 'apiName' },
  { title: '方法', dataIndex: 'httpMethod', key: 'httpMethod', width: 90 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
];

/** 当前编辑的 API 授权保存命令。 */
const command = reactive<OpenApiPermissionSaveRequest>(createEmptyCommand());

/**
 * 创建空 API 授权保存命令。
 *
 * @returns API 授权保存命令
 */
function createEmptyCommand(): OpenApiPermissionSaveRequest {
  return {
    tenantId: tenantId.value,
    appCode: appCode.value,
    apiCode: '',
    apiName: '',
    httpMethod: 'GET',
    pathPattern: '',
    status: 'enabled',
  };
}

/**
 * 将 API 授权响应复制到编辑命令。
 *
 * @param permission API 授权响应
 */
function applyPermission(permission: OpenApiPermissionResponse): void {
  command.tenantId = tenantId.value;
  command.appCode = appCode.value;
  command.apiCode = permission.apiCode;
  command.apiName = permission.apiName;
  command.httpMethod = permission.httpMethod;
  command.pathPattern = permission.pathPattern;
  command.status = permission.status;
}

/**
 * 重置 API 授权编辑表单。
 */
function resetCommand(): void {
  Object.assign(command, createEmptyCommand());
}

/**
 * 查询当前应用的 API 授权。
 */
async function loadPermissions(): Promise<void> {
  if (!appCode.value) {
    return;
  }
  loading.value = true;
  try {
    permissions.value = await listOpenApiPermissions(syncTenantContext(), appCode.value);
  } finally {
    loading.value = false;
  }
}

/**
 * 保存 API 授权。
 */
async function handleSave(): Promise<void> {
  saving.value = true;
  try {
    command.tenantId = syncTenantContext();
    appCode.value = command.appCode;
    await saveOpenApiPermission(command);
    await loadPermissions();
    message.success('API 授权已保存');
  } finally {
    saving.value = false;
  }
}

/**
 * 构建 API 授权表格行属性。
 *
 * @param record API 授权响应
 * @returns 表格行属性
 */
function buildPermissionRowProps(record: OpenApiPermissionResponse): { onClick: () => void } {
  return {
    onClick: () => applyPermission(record),
  };
}

/**
 * 同步后台租户到 API 授权表单。
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
.openapi-permission-page {
  min-width: 0;
}

.tenant-id {
  width: 160px;
}

.app-code {
  width: 180px;
}

.permission-form {
  margin-top: 16px;
}

.permission-code {
  display: none;
}
</style>
