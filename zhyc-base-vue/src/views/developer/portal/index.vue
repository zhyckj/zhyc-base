<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="developer-portal-page">
    <a-alert
      v-if="portalErrorMessage"
      class="portal-alert"
      type="error"
      show-icon
      :message="`加载失败：${portalErrorMessage}`"
    />

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :xl="16">
        <a-card title="开发者首页" :bordered="false">
          <template #extra>
            <a-space>
              <a-input v-model:value="tenantId" class="tenant-input" placeholder="租户编码" disabled />
              <a-button :loading="loading" @click="loadPortalData">刷新</a-button>
            </a-space>
          </template>

          <a-row :gutter="[12, 12]">
            <a-col v-for="item in overviewCards" :key="item.key" :xs="12" :md="6">
              <a-statistic :title="item.title" :value="item.value" />
            </a-col>
          </a-row>
        </a-card>

        <a-card class="portal-section" title="API Key 凭证" :bordered="false">
          <a-table
            row-key="accessKey"
            size="small"
            :columns="apiKeyColumns"
            :data-source="apiKeys"
            :loading="loading"
            :pagination="$tablePagination"
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

        <a-card class="portal-section" title="OAuth2 客户端" :bordered="false">
          <a-table
            row-key="clientId"
            size="small"
            :columns="oauthClientColumns"
            :data-source="oauthClients"
            :loading="loading"
            :pagination="$tablePagination"
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

        <a-card class="portal-section" title="API 文档" :bordered="false">
          <template #extra>
            <a-space>
              <a-input v-model:value="groupCode" class="group-input" placeholder="API 分组" />
              <a-button :loading="catalogLoading" @click="loadCatalogs">查询 API</a-button>
            </a-space>
          </template>

          <a-table
            row-key="apiCode"
            size="small"
            :columns="catalogColumns"
            :data-source="catalogs"
            :loading="catalogLoading"
            :pagination="$tablePagination"
            :custom-row="buildCatalogRowProps"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'method'">
                <a-tag color="blue">{{ record.httpMethod }}</a-tag>
              </template>
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
                  {{ $statusLabel(record.status) }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <a-col :xs="24" :xl="8">
        <a-card title="调试控制台" :bordered="false">
          <a-form layout="vertical" :model="debugCommand">
            <a-form-item label="请求方法">
              <a-select v-model:value="debugCommand.method" :options="methodOptions" />
            </a-form-item>
            <a-form-item label="请求地址">
              <a-input v-model:value="debugCommand.path" placeholder="/openapi/v1/purchase/requests" />
            </a-form-item>
            <a-form-item label="应用编码">
              <a-select
                v-model:value="debugCommand.appCode"
                :options="appOptions"
                placeholder="选择开发者应用"
                @change="handleDeveloperAppChange"
              />
            </a-form-item>
            <a-form-item label="认证方式">
              <a-select v-model:value="debugCommand.authMode" :options="authModeOptions" />
            </a-form-item>
            <a-form-item v-if="debugCommand.authMode === 'OAUTH2'" label="Bearer Token">
              <a-input v-model:value="debugCommand.bearerToken" placeholder="请输入 OAuth2/OIDC Access Token" />
            </a-form-item>
            <template v-if="debugCommand.authMode === 'API_KEY'">
              <a-form-item label="签名时间戳">
                <a-input v-model:value="debugCommand.timestamp" placeholder="生成快照后自动填充" />
              </a-form-item>
              <a-form-item label="签名随机串">
                <a-input v-model:value="debugCommand.nonce" placeholder="生成快照后自动填充" />
              </a-form-item>
              <a-form-item label="签名值">
                <a-input v-model:value="debugCommand.signature" placeholder="按快照签名原文计算后填写" />
              </a-form-item>
            </template>
            <a-form-item label="请求体">
              <a-textarea v-model:value="debugCommand.body" :rows="6" />
            </a-form-item>
            <a-space class="debug-actions" direction="vertical">
              <a-button block @click="buildDebugSnapshot">生成调试快照</a-button>
              <a-button block type="primary" :loading="debugInvoking" @click="invokeDebugGateway">调用调试代理</a-button>
            </a-space>
          </a-form>

          <pre class="debug-preview">{{ debugSnapshot }}</pre>
          <pre class="debug-preview">{{ debugResponseSnapshot }}</pre>
        </a-card>

        <a-card class="portal-section" title="安全配置" :bordered="false">
          <a-descriptions size="small" :column="1" bordered>
            <a-descriptions-item label="API Key">HMAC-SHA256 签名</a-descriptions-item>
            <a-descriptions-item label="OAuth2/OIDC">认证中心 Token 校验</a-descriptions-item>
            <a-descriptions-item label="重放保护">时间戳 + Nonce</a-descriptions-item>
            <a-descriptions-item label="限流策略">按应用与 API 维度控制</a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card class="portal-section" title="调用记录" :bordered="false">
          <a-table
            row-key="requestId"
            size="small"
            :columns="auditColumns"
            :data-source="callAudits"
            :loading="loading"
            :pagination="$tablePagination"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'success'">
                <a-tag :color="record.success === 1 ? 'green' : 'red'">
                  {{ record.success === 1 ? '成功' : '失败' }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';

import {
  invokeOpenApiDebug,
  loadDeveloperPortalOverview,
  type DeveloperPortalOverview,
} from '@/api/developer/portal';
import type { OpenApiApiKeyResponse } from '@/api/openapi/api-key';
import type { OpenApiAppResponse } from '@/api/openapi/app';
import type { OpenApiCallAuditResponse } from '@/api/openapi/call-audit';
import { listOpenApiCatalogs, type OpenApiCatalogResponse } from '@/api/openapi/catalog';
import type { OpenApiOauthClientResponse } from '@/api/openapi/oauth-client';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/**
 * 开发者调试命令。
 */
interface DeveloperDebugCommand {
  /** HTTP 请求方法。 */
  method: string;
  /** 开放 API 请求路径。 */
  path: string;
  /** 开发者应用编码。 */
  appCode: string;
  /** 调试认证方式。 */
  authMode: 'API_KEY' | 'OAUTH2';
  /** OAuth2/OIDC Bearer Token。 */
  bearerToken: string;
  /** API Key 签名时间戳。 */
  timestamp: string;
  /** API Key 签名随机串。 */
  nonce: string;
  /** API Key 签名值。 */
  signature: string;
  /** 请求体 JSON 文本。 */
  body: string;
}

const initialAdminContext = getAdminRuntimeContext();
/** 当前租户编码；由后台运行时上下文提供，用于开发者门户数据隔离。 */
const tenantId = ref(initialAdminContext.tenantId);
/** 当前 API 分组编码。 */
const groupCode = ref('purchase');
/** 开发者应用列表。 */
const apps = ref<OpenApiAppResponse[]>([]);
/** API Key 凭证列表。 */
const apiKeys = ref<OpenApiApiKeyResponse[]>([]);
/** OAuth2 客户端映射列表。 */
const oauthClients = ref<OpenApiOauthClientResponse[]>([]);
/** API 目录列表。 */
const catalogs = ref<OpenApiCatalogResponse[]>([]);
/** 开放 API 调用记录。 */
const callAudits = ref<OpenApiCallAuditResponse[]>([]);
/** 门户整体加载状态。 */
const loading = ref(false);
/** API 目录加载状态。 */
const catalogLoading = ref(false);
/** 调试快照内容。 */
const debugSnapshot = ref('等待生成调试快照');
/** 调试响应快照内容。 */
const debugResponseSnapshot = ref('调试响应等待调用');
/** 开发者门户错误提示。 */
const portalErrorMessage = ref('');
/** 调试代理调用状态。 */
const debugInvoking = ref(false);

/** 调试控制台命令。 */
const debugCommand = reactive<DeveloperDebugCommand>({
  method: 'GET',
  path: '/openapi/v1/purchase/requests',
  appCode: '',
  authMode: 'API_KEY',
  bearerToken: '',
  timestamp: '',
  nonce: '',
  signature: '',
  body: buildDefaultDebugBody(initialAdminContext.tenantId),
});

/** HTTP 方法下拉选项。 */
const methodOptions = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'].map((method) => ({
  label: method,
  value: method,
}));

/** 调试认证方式选项。 */
const authModeOptions = [
  { label: 'API Key 签名', value: 'API_KEY' },
  { label: 'OAuth2/OIDC Bearer Token', value: 'OAUTH2' },
];

/** API 文档表格列。 */
const catalogColumns = [
  { title: 'API 编码', dataIndex: 'apiCode', key: 'apiCode' },
  { title: 'API 名称', dataIndex: 'apiName', key: 'apiName' },
  { title: '分组', dataIndex: 'groupCode', key: 'groupCode', width: 100 },
  { title: '方法', dataIndex: 'httpMethod', key: 'method', width: 90 },
  { title: '路径', dataIndex: 'pathPattern', key: 'pathPattern' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
];

/** API Key 表格列。 */
const apiKeyColumns = [
  { title: '应用编码', dataIndex: 'appCode', key: 'appCode' },
  { title: 'Access Key', dataIndex: 'accessKey', key: 'accessKey' },
  { title: 'Secret 掩码', dataIndex: 'secretMask', key: 'secretMask' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '过期时间', dataIndex: 'expireAt', key: 'expireAt', width: 170 },
];

/** OAuth2 客户端表格列。 */
const oauthClientColumns = [
  { title: 'Client ID', dataIndex: 'clientId', key: 'clientId' },
  { title: '授权范围', dataIndex: 'allowedScopes', key: 'allowedScopes' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
];

/** 调用审计表格列。 */
const auditColumns = [
  { title: 'API', dataIndex: 'apiCode', key: 'apiCode' },
  { title: '方法', dataIndex: 'httpMethod', key: 'httpMethod', width: 80 },
  { title: '状态码', dataIndex: 'responseStatus', key: 'responseStatus', width: 90 },
  { title: '耗时', dataIndex: 'durationMs', key: 'durationMs', width: 90 },
  { title: '结果', dataIndex: 'success', key: 'success', width: 80 },
  { title: '调用时间', dataIndex: 'calledAt', key: 'calledAt', width: 170 },
];

/** 应用下拉选项。 */
const appOptions = computed(() =>
  apps.value.map((app) => ({
    label: `${app.appName}（${app.appCode}）`,
    value: app.appCode,
  })),
);

/** 门户概览卡片。 */
const overviewCards = computed(() => [
  { key: 'apps', title: '开发者应用', value: apps.value.length },
  { key: 'keys', title: 'API Key', value: apiKeys.value.length },
  { key: 'apis', title: 'API 文档', value: catalogs.value.length },
  { key: 'audit', title: '调用记录', value: callAudits.value.length },
]);

/** 当前调试应用的 Access Key。 */
const currentAccessKey = computed(() =>
  apiKeys.value.find((apiKey) => apiKey.appCode === debugCommand.appCode)?.accessKey || '<access-key>',
);

/**
 * 加载开发者门户首屏数据。
 */
async function loadPortalData(): Promise<void> {
  loading.value = true;
  portalErrorMessage.value = '';
  try {
    const currentTenantId = syncTenantContext();
    if (!debugCommand.body.trim()) {
      debugCommand.body = buildDefaultDebugBody(currentTenantId);
    }
    const overview = await loadDeveloperPortalOverview({
      tenantId: currentTenantId,
      appCode: debugCommand.appCode,
      groupCode: groupCode.value,
    });
    applyOverview(overview);
    if (!debugCommand.appCode && overview.apps.length > 0) {
      debugCommand.appCode = overview.apps[0].appCode;
    }
  } catch (error) {
    portalErrorMessage.value = resolveErrorMessage(error);
    message.error(`开发者门户加载失败：${portalErrorMessage.value}`);
  } finally {
    loading.value = false;
  }
}

/**
 * 同步后台运行时租户上下文。
 *
 * @returns 已校验的当前租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId(getAdminRuntimeContext());
  tenantId.value = currentTenantId;
  return currentTenantId;
}

/**
 * 构建调试控制台默认请求体。
 *
 * @param currentTenantId 当前租户编码
 * @returns 包含当前租户编码的 JSON 文本
 */
function buildDefaultDebugBody(currentTenantId: string): string {
  return JSON.stringify({ tenantId: currentTenantId }, null, 2);
}

/**
 * 切换开发者应用后刷新安全凭证和调用记录。
 */
function handleDeveloperAppChange(): void {
  void loadPortalData();
}

/**
 * 查询当前 API 分组下的目录。
 */
async function loadCatalogs(): Promise<void> {
  catalogLoading.value = true;
  try {
    catalogs.value = await listOpenApiCatalogs(groupCode.value);
  } catch (error) {
    message.error(`API 目录加载失败：${resolveErrorMessage(error)}`);
  } finally {
    catalogLoading.value = false;
  }
}

/**
 * 将选中的 API 目录带入调试控制台。
 *
 * @param record API 目录响应
 * @returns 表格行属性
 */
function buildCatalogRowProps(record: OpenApiCatalogResponse): { onClick: () => void } {
  return {
    onClick: () => {
      debugCommand.method = record.httpMethod;
      debugCommand.path = record.pathPattern;
    },
  };
}

/**
 * 生成调试快照，供开发者核对签名输入。
 */
async function buildDebugSnapshot(): Promise<void> {
  const timestamp = String(Date.now());
  const nonce = createRequestNonce();
  debugCommand.timestamp = timestamp;
  debugCommand.nonce = nonce;
  let bodySha256 = '';
  try {
    bodySha256 = await bodySha256Hex(debugCommand.body);
  } catch (error) {
    message.error(resolveErrorMessage(error));
    return;
  }
  const signaturePayload = `${debugCommand.method}\n${debugCommand.path}\n${timestamp}\n${nonce}\n${bodySha256}`;
  const authHeaders = debugCommand.authMode === 'OAUTH2'
    ? { Authorization: `Bearer ${debugCommand.bearerToken || '<access-token>'}` }
    : {
        accessKeyHeader: 'X-ZHYC-Access-Key',
        accessKey: currentAccessKey.value,
        timestampHeader: 'X-ZHYC-Timestamp',
        nonceHeader: 'X-ZHYC-Nonce',
        bodySha256Header: 'X-ZHYC-Body-SHA256',
        bodySha256,
        signatureHeader: 'X-ZHYC-Signature',
        signaturePayload,
      };
  debugSnapshot.value = JSON.stringify(
    {
      method: debugCommand.method,
      path: debugCommand.path,
      appCode: debugCommand.appCode,
      authMode: debugCommand.authMode,
      ...authHeaders,
      body: debugCommand.body,
    },
    null,
    2,
  );
}

/**
 * 调用后台开放 API 调试代理。
 */
async function invokeDebugGateway(): Promise<void> {
  if (!validateDebugCredentials()) {
    return;
  }
  debugInvoking.value = true;
  try {
    const currentTenantId = syncTenantContext();
    const response = await invokeOpenApiDebug({
      tenantId: currentTenantId,
      apiCode: resolveDebugApiCode(),
      method: debugCommand.method,
      path: debugCommand.path,
      authMode: debugCommand.authMode,
      accessKey: debugCommand.authMode === 'API_KEY' ? currentAccessKey.value : undefined,
      timestamp: debugCommand.authMode === 'API_KEY' ? debugCommand.timestamp : undefined,
      nonce: debugCommand.authMode === 'API_KEY' ? debugCommand.nonce : undefined,
      signature: debugCommand.authMode === 'API_KEY' ? debugCommand.signature : undefined,
      bearerToken: debugCommand.authMode === 'OAUTH2' ? debugCommand.bearerToken : undefined,
      requestId: `debug-${Date.now()}`,
      body: debugCommand.body,
    });
    debugResponseSnapshot.value = JSON.stringify(response, null, 2);
    message[response.success ? 'success' : 'warning'](
      response.success ? '开放 API 调试调用成功' : `开放 API 调试调用失败：${response.errorCode || response.httpStatus}`,
    );
  } catch (error) {
    message.error(`开放 API 调试调用失败：${resolveErrorMessage(error)}`);
  } finally {
    debugInvoking.value = false;
  }
}

/**
 * 校验开放 API 调试凭证。
 *
 * @returns 凭证满足当前认证方式要求时返回 true
 */
function validateDebugCredentials(): boolean {
  if (debugCommand.authMode === 'OAUTH2') {
    if (!debugCommand.bearerToken.trim()) {
      message.error('请输入 OAuth2/OIDC Access Token');
      return false;
    }
    return true;
  }
  if (!debugCommand.appCode || currentAccessKey.value === '<access-key>') {
    message.error('请先选择有效 API Key 凭证');
    return false;
  }
  if (!debugCommand.timestamp.trim() || !debugCommand.nonce.trim()) {
    message.error('请先生成调试快照获取签名时间戳和随机串');
    return false;
  }
  if (!debugCommand.signature.trim()) {
    message.error('请输入 API Key 签名值');
    return false;
  }
  return true;
}

/**
 * 解析当前调试路径对应的开放 API 编码。
 *
 * @returns 开放 API 业务编码；未命中目录时用路径作为调试标识
 */
function resolveDebugApiCode(): string {
  return catalogs.value.find(
    (catalog) => catalog.httpMethod === debugCommand.method && catalog.pathPattern === debugCommand.path,
  )?.apiCode || debugCommand.path;
}

/**
 * 计算开放 API 请求体 SHA-256 摘要。
 *
 * @param body 请求体文本
 * @returns 小写 hex 格式摘要
 */
async function bodySha256Hex(body: string): Promise<string> {
  const cryptoApi = globalThis.crypto;
  if (!cryptoApi?.subtle) {
    throw new Error('当前浏览器不支持 Web Crypto SHA-256');
  }
  const digest = await cryptoApi.subtle.digest('SHA-256', new TextEncoder().encode(body || ''));
  return Array.from(new Uint8Array(digest))
    .map((value) => value.toString(16).padStart(2, '0'))
    .join('');
}

/**
 * 生成开放 API 调试请求 nonce。
 *
 * @returns 每次调试请求独立的 nonce
 */
function createRequestNonce(): string {
  const cryptoApi = globalThis.crypto;
  if (cryptoApi?.getRandomValues) {
    const randomValues = new Uint32Array(4);
    cryptoApi.getRandomValues(randomValues);
    return Array.from(randomValues)
      .map((value) => value.toString(16).padStart(8, '0'))
      .join('');
  }
  return `${Date.now()}${Math.random().toString(16).slice(2)}`;
}

/**
 * 应用开发者门户概览数据。
 *
 * @param overview 门户概览数据
 */
function applyOverview(overview: DeveloperPortalOverview): void {
  apps.value = overview.apps;
  apiKeys.value = overview.apiKeys;
  oauthClients.value = overview.oauthClients;
  catalogs.value = overview.catalogs;
  callAudits.value = overview.callAudits;
}

/**
 * 解析后台页面错误提示。
 *
 * @param error 捕获到的异常
 * @returns 用户可读错误消息
 */
function resolveErrorMessage(error: unknown): string {
  return error instanceof Error && error.message ? error.message : '请稍后重试';
}

onMounted(() => {
  void loadPortalData();
});
</script>

<style scoped>
.developer-portal-page {
  min-width: 0;
}

.tenant-input,
.group-input {
  width: 160px;
}

.portal-section {
  margin-top: 16px;
}

.portal-alert {
  margin-bottom: 16px;
}

.debug-preview {
  min-height: 180px;
  margin: 16px 0 0;
  padding: 12px;
  overflow: auto;
  color: #1f2937;
  white-space: pre-wrap;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}
</style>
