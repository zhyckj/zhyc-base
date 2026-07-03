<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="openapi-signature-policy-page">
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="13">
        <a-card title="签名策略" :bordered="false">
          <template #extra>
            <a-space>
              <a-input v-model:value="tenantId" class="tenant-id" />
              <a-input v-model:value="appCode" class="app-code" placeholder="应用编码" />
              <a-button :loading="loading" @click="loadPolicies">查询</a-button>
            </a-space>
          </template>

          <a-table
            row-key="algorithm"
            size="small"
            :columns="columns"
            :data-source="policies"
            :loading="loading"
            :pagination="$tablePagination"
            :custom-row="buildPolicyRowProps"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'requireBodyHash'">
                <a-tag :color="record.requireBodyHash === 1 ? 'blue' : 'default'">
                  {{ record.requireBodyHash === 1 ? '启用' : '停用' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'status'">
                <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
                  {{ $statusLabel(record.status) }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="11">
        <a-card title="策略配置" :bordered="false">
          <template #extra>
            <a-space>
              <a-button @click="resetCommand">重置</a-button>
              <a-button type="primary" :loading="saving" @click="handleSave">保存策略</a-button>
            </a-space>
          </template>

          <a-alert message="签名策略按租户和开发者应用生效，用于约束 API Key 签名算法、时间戳窗口和 nonce 防重放有效期。" type="info" show-icon />

          <a-form class="policy-form" layout="vertical" :model="command">
            <a-row :gutter="16">
              <a-col :xs="24" :md="12">
                <a-form-item label="应用编码" required>
                  <a-input v-model:value="command.appCode" placeholder="purchase-app" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="签名算法" required>
                  <a-select v-model:value="command.algorithm" :options="algorithmOptions" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="时间戳窗口(秒)" required>
                  <a-input-number
                    v-model:value="command.timestampToleranceSeconds"
                    class="number-input"
                    :min="1"
                    :precision="0"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="nonce 有效期(秒)" required>
                  <a-input-number
                    v-model:value="command.nonceTtlSeconds"
                    class="number-input"
                    :min="1"
                    :precision="0"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="请求体摘要" required>
                  <a-select v-model:value="command.requireBodyHash" :options="bodyHashOptions" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="策略状态" required>
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
  listOpenApiSignaturePolicies,
  saveOpenApiSignaturePolicy,
  type OpenApiSignaturePolicyResponse,
  type OpenApiSignaturePolicySaveRequest,
} from '@/api/openapi/signature-policy';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 当前开发者应用编码。 */
const appCode = ref('');
/** 签名策略列表。 */
const policies = ref<OpenApiSignaturePolicyResponse[]>([]);
/** 列表加载状态。 */
const loading = ref(false);
/** 保存提交状态。 */
const saving = ref(false);

/** 签名策略权限编码，供页面按钮和后续权限指令统一引用。 */
const permissionCodes = {
  /** 保存签名策略权限。 */
  save: 'openapi:signature-policy:save',
};

/** 签名算法下拉选项。 */
const algorithmOptions = [{ label: 'HMAC-SHA256', value: 'HMAC_SHA256' }];

/** 请求体摘要开关选项。 */
const bodyHashOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
];

/** 策略状态下拉选项。 */
const statusOptions = [
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' },
];

/** 签名策略表格列定义。 */
const columns = [
  { title: '签名算法', dataIndex: 'algorithm', key: 'algorithm' },
  { title: '时间戳窗口', dataIndex: 'timestampToleranceSeconds', key: 'timestampToleranceSeconds', width: 120 },
  { title: 'nonce 有效期', dataIndex: 'nonceTtlSeconds', key: 'nonceTtlSeconds', width: 120 },
  { title: '请求体摘要', dataIndex: 'requireBodyHash', key: 'requireBodyHash', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
];

/** 当前编辑的签名策略保存命令。 */
const command = reactive<OpenApiSignaturePolicySaveRequest>(createEmptyCommand());

/**
 * 创建空签名策略保存命令。
 *
 * @returns 签名策略保存命令
 */
function createEmptyCommand(): OpenApiSignaturePolicySaveRequest {
  return {
    tenantId: tenantId.value,
    appCode: appCode.value,
    algorithm: 'HMAC_SHA256',
    timestampToleranceSeconds: 300,
    nonceTtlSeconds: 600,
    requireBodyHash: 1,
    status: 'enabled',
  };
}

/**
 * 将签名策略响应复制到编辑命令。
 *
 * @param policy 签名策略响应
 */
function applyPolicy(policy: OpenApiSignaturePolicyResponse): void {
  command.tenantId = tenantId.value;
  command.appCode = appCode.value;
  command.algorithm = policy.algorithm;
  command.timestampToleranceSeconds = policy.timestampToleranceSeconds;
  command.nonceTtlSeconds = policy.nonceTtlSeconds;
  command.requireBodyHash = policy.requireBodyHash;
  command.status = policy.status;
}

/**
 * 重置签名策略编辑表单。
 */
function resetCommand(): void {
  Object.assign(command, createEmptyCommand());
}

/**
 * 查询当前应用的签名策略。
 */
async function loadPolicies(): Promise<void> {
  if (!appCode.value) {
    return;
  }
  loading.value = true;
  try {
    policies.value = await listOpenApiSignaturePolicies(syncTenantContext(), appCode.value);
  } finally {
    loading.value = false;
  }
}

/**
 * 保存签名策略。
 */
async function handleSave(): Promise<void> {
  saving.value = true;
  try {
    command.tenantId = syncTenantContext();
    appCode.value = command.appCode;
    await saveOpenApiSignaturePolicy(command);
    await loadPolicies();
    message.success('签名策略已保存');
  } finally {
    saving.value = false;
  }
}

/**
 * 构建签名策略表格行属性。
 *
 * @param record 签名策略响应
 * @returns 表格行属性
 */
function buildPolicyRowProps(record: OpenApiSignaturePolicyResponse): { onClick: () => void } {
  return {
    onClick: () => applyPolicy(record),
  };
}

/**
 * 同步后台租户到签名策略表单。
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
.openapi-signature-policy-page {
  min-width: 0;
}

.tenant-id {
  width: 160px;
}

.app-code {
  width: 180px;
}

.policy-form {
  margin-top: 16px;
}

.number-input {
  width: 100%;
}

.permission-code {
  display: none;
}
</style>
