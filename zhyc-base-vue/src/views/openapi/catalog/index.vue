<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="openapi-catalog-page">
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="13">
        <a-card title="API 目录" :bordered="false">
          <template #extra>
            <a-space>
              <a-input v-model:value="groupCode" class="group-code" placeholder="分组编码" />
              <a-button :loading="loading" @click="loadCatalogs">查询</a-button>
            </a-space>
          </template>

          <a-table
            row-key="apiCode"
            size="small"
            :columns="columns"
            :data-source="catalogs"
            :loading="loading"
            :pagination="$tablePagination"
            :custom-row="buildCatalogRowProps"
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

      <a-col :xs="24" :lg="11">
        <a-card title="目录配置" :bordered="false">
          <template #extra>
            <a-space>
              <a-button @click="resetCommand">新建 API</a-button>
              <a-button type="primary" :loading="saving" @click="handleSave">保存 API</a-button>
            </a-space>
          </template>

          <a-alert message="API 目录是开放平台的全局接口清单，后续 API 发布、授权和开发者文档都以目录编码为基础。" type="info" show-icon />

          <a-form class="catalog-form" layout="vertical" :model="command">
            <a-row :gutter="16">
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
                <a-form-item label="分组编码" required>
                  <a-input v-model:value="command.groupCode" placeholder="purchase" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="HTTP 方法" required>
                  <a-select v-model:value="command.httpMethod" :options="methodOptions" />
                </a-form-item>
              </a-col>
              <a-col :xs="24">
                <a-form-item label="路径规则" required>
                  <a-input v-model:value="command.pathPattern" placeholder="/openapi/v1/purchase/requests" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="目录状态" required>
                  <a-select v-model:value="command.status" :options="statusOptions" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>

          <span class="permission-code">{{ permissionCodes.save }}</span>
        </a-card>
      </a-col>
    </a-row>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { message } from 'ant-design-vue';

import {
  listOpenApiCatalogs,
  saveOpenApiCatalog,
  type OpenApiCatalogResponse,
  type OpenApiCatalogSaveRequest,
} from '@/api/openapi/catalog';

/** 当前 API 分组编码。 */
const groupCode = ref('purchase');
/** API 目录列表。 */
const catalogs = ref<OpenApiCatalogResponse[]>([]);
/** 列表加载状态。 */
const loading = ref(false);
/** 保存提交状态。 */
const saving = ref(false);

/** API 目录权限编码，供页面按钮和后续权限指令统一引用。 */
const permissionCodes = {
  /** 保存 API 目录权限。 */
  save: 'openapi:catalog:save',
};

/** HTTP 方法下拉选项。 */
const methodOptions = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'].map((method) => ({
  label: method,
  value: method,
}));

/** 目录状态下拉选项。 */
const statusOptions = [
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' },
];

/** API 目录表格列定义。 */
const columns = [
  { title: 'API 编码', dataIndex: 'apiCode', key: 'apiCode' },
  { title: 'API 名称', dataIndex: 'apiName', key: 'apiName' },
  { title: '方法', dataIndex: 'httpMethod', key: 'httpMethod', width: 90 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
];

/** 当前编辑的 API 目录保存命令。 */
const command = reactive<OpenApiCatalogSaveRequest>(createEmptyCommand());

/**
 * 创建空 API 目录保存命令。
 *
 * @returns API 目录保存命令
 */
function createEmptyCommand(): OpenApiCatalogSaveRequest {
  return {
    apiCode: '',
    apiName: '',
    groupCode: groupCode.value,
    httpMethod: 'GET',
    pathPattern: '',
    status: 'enabled',
  };
}

/**
 * 将 API 目录响应复制到编辑命令。
 *
 * @param catalog API 目录响应
 */
function applyCatalog(catalog: OpenApiCatalogResponse): void {
  command.apiCode = catalog.apiCode;
  command.apiName = catalog.apiName;
  command.groupCode = catalog.groupCode;
  command.httpMethod = catalog.httpMethod;
  command.pathPattern = catalog.pathPattern;
  command.status = catalog.status;
}

/**
 * 重置 API 目录编辑表单。
 */
function resetCommand(): void {
  Object.assign(command, createEmptyCommand());
}

/**
 * 查询当前分组的 API 目录。
 */
async function loadCatalogs(): Promise<void> {
  if (!groupCode.value) {
    return;
  }
  loading.value = true;
  try {
    catalogs.value = await listOpenApiCatalogs(groupCode.value);
  } finally {
    loading.value = false;
  }
}

/**
 * 保存 API 目录。
 */
async function handleSave(): Promise<void> {
  saving.value = true;
  try {
    groupCode.value = command.groupCode;
    await saveOpenApiCatalog(command);
    await loadCatalogs();
    message.success('API 目录已保存');
  } finally {
    saving.value = false;
  }
}

/**
 * 构建 API 目录表格行属性。
 *
 * @param record API 目录响应
 * @returns 表格行属性
 */
function buildCatalogRowProps(record: OpenApiCatalogResponse): { onClick: () => void } {
  return {
    onClick: () => applyCatalog(record),
  };
}
</script>

<style scoped>
.openapi-catalog-page {
  min-width: 0;
}

.group-code {
  width: 180px;
}

.catalog-form {
  margin-top: 16px;
}

.permission-code {
  display: none;
}
</style>
