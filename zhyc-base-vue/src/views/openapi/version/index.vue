<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="openapi-version-page">
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="12">
        <a-card title="API 发布" :bordered="false">
          <template #extra>
            <a-space>
              <a-input v-model:value="apiCode" class="api-code" placeholder="API 编码" />
              <a-button :loading="loading" @click="loadVersions">查询</a-button>
            </a-space>
          </template>

          <a-table
            row-key="version"
            size="small"
            :columns="columns"
            :data-source="versions"
            :loading="loading"
            :pagination="$tablePagination"
            :custom-row="buildVersionRowProps"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 'published' ? 'green' : 'default'">
                  {{ $statusLabel(record.status) }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card title="版本配置" :bordered="false">
          <template #extra>
            <a-space>
              <a-button @click="resetCommand">新建版本</a-button>
              <a-button type="primary" :loading="saving" @click="handlePublish">发布版本</a-button>
            </a-space>
          </template>

          <a-alert message="API 发布将目录接口绑定到后端转发路由，并记录请求/响应 Schema，供网关路由和开发者文档复用。" type="info" show-icon />

          <a-form class="version-form" layout="vertical" :model="command">
            <a-row :gutter="16">
              <a-col :xs="24" :md="12">
                <a-form-item label="API 编码" required>
                  <a-input v-model:value="command.apiCode" placeholder="purchase.request.create" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="版本号" required>
                  <a-input v-model:value="command.version" placeholder="v1" />
                </a-form-item>
              </a-col>
              <a-col :xs="24">
                <a-form-item label="后端路由" required>
                  <a-input v-model:value="command.backendRoute" placeholder="http://purchase-service/openapi/v1/purchase/requests" />
                </a-form-item>
              </a-col>
              <a-col :xs="24">
                <a-form-item label="请求 Schema">
                  <a-textarea v-model:value="command.requestSchema" :rows="4" />
                </a-form-item>
              </a-col>
              <a-col :xs="24">
                <a-form-item label="响应 Schema">
                  <a-textarea v-model:value="command.responseSchema" :rows="4" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="发布状态" required>
                  <a-select v-model:value="command.status" :options="statusOptions" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>

          <span class="permission-code">{{ permissionCodes.publish }}</span>
        </a-card>
      </a-col>
    </a-row>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { message } from 'ant-design-vue';

import {
  listOpenApiVersions,
  publishOpenApiVersion,
  type OpenApiVersionPublishRequest,
  type OpenApiVersionResponse,
} from '@/api/openapi/version';

/** 当前 API 业务编码。 */
const apiCode = ref('');
/** API 版本列表。 */
const versions = ref<OpenApiVersionResponse[]>([]);
/** 列表加载状态。 */
const loading = ref(false);
/** 发布提交状态。 */
const saving = ref(false);

/** API 发布权限编码，供页面按钮和后续权限指令统一引用。 */
const permissionCodes = {
  /** 发布 API 版本权限。 */
  publish: 'openapi:catalog:publish',
};

/** 发布状态下拉选项。 */
const statusOptions = [
  { label: '已发布', value: 'published' },
  { label: '停用', value: 'disabled' },
];

/** API 版本表格列定义。 */
const columns = [
  { title: '版本号', dataIndex: 'version', key: 'version', width: 120 },
  { title: '后端路由', dataIndex: 'backendRoute', key: 'backendRoute' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
];

/** 当前编辑的 API 版本发布命令。 */
const command = reactive<OpenApiVersionPublishRequest>(createEmptyCommand());

/**
 * 创建空 API 版本发布命令。
 *
 * @returns API 版本发布命令
 */
function createEmptyCommand(): OpenApiVersionPublishRequest {
  return {
    apiCode: apiCode.value,
    version: 'v1',
    backendRoute: '',
    requestSchema: '{}',
    responseSchema: '{}',
    status: 'published',
  };
}

/**
 * 将 API 版本响应复制到编辑命令。
 *
 * @param version API 版本响应
 */
function applyVersion(version: OpenApiVersionResponse): void {
  command.apiCode = apiCode.value;
  command.version = version.version;
  command.backendRoute = version.backendRoute;
  command.requestSchema = version.requestSchema;
  command.responseSchema = version.responseSchema;
  command.status = version.status;
}

/**
 * 重置 API 版本编辑表单。
 */
function resetCommand(): void {
  Object.assign(command, createEmptyCommand());
}

/**
 * 查询当前 API 的发布版本。
 */
async function loadVersions(): Promise<void> {
  if (!apiCode.value) {
    return;
  }
  loading.value = true;
  try {
    versions.value = await listOpenApiVersions(apiCode.value);
  } finally {
    loading.value = false;
  }
}

/**
 * 发布或更新 API 版本。
 */
async function handlePublish(): Promise<void> {
  saving.value = true;
  try {
    apiCode.value = command.apiCode;
    await publishOpenApiVersion(command);
    await loadVersions();
    message.success('API 版本已发布');
  } finally {
    saving.value = false;
  }
}

/**
 * 构建 API 版本表格行属性。
 *
 * @param record API 版本响应
 * @returns 表格行属性
 */
function buildVersionRowProps(record: OpenApiVersionResponse): { onClick: () => void } {
  return {
    onClick: () => applyVersion(record),
  };
}
</script>

<style scoped>
.openapi-version-page {
  min-width: 0;
}

.api-code {
  width: 240px;
}

.version-form {
  margin-top: 16px;
}

.permission-code {
  display: none;
}
</style>
