<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="openapi-rate-limit-policy-page">
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="13">
        <a-card title="限流策略" :bordered="false">
          <template #extra>
            <a-space>
              <a-input v-model:value="tenantId" class="tenant-id" />
              <a-input v-model:value="appCode" class="app-code" placeholder="应用编码" />
              <a-button :loading="loading" @click="loadPolicies">查询</a-button>
            </a-space>
          </template>

          <a-table
            row-key="apiCode"
            size="small"
            :columns="columns"
            :data-source="policies"
            :loading="loading"
            :pagination="$tablePagination"
            :custom-row="buildPolicyRowProps"
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
        <a-card title="策略配置" :bordered="false">
          <template #extra>
            <a-space>
              <a-button @click="resetCommand">新建策略</a-button>
              <a-button type="primary" :loading="saving" @click="handleSave">保存策略</a-button>
            </a-space>
          </template>

          <a-alert message="限流策略按租户、应用、开放 API 编码生效，用于保护开放 API 网关运行稳定性。" type="info" show-icon />

          <a-form class="policy-form" layout="vertical" :model="command">
            <a-row :gutter="16">
              <a-col :xs="24" :md="12">
                <a-form-item label="应用编码" required>
                  <a-input v-model:value="command.appCode" placeholder="purchase-app" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="API 编码" required>
                  <a-input v-model:value="command.apiCode" placeholder="purchase.request.create" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="调用次数" required>
                  <a-input-number v-model:value="command.limitCount" class="number-input" :min="1" :precision="0" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="窗口秒数" required>
                  <a-input-number v-model:value="command.windowSeconds" class="number-input" :min="1" :precision="0" />
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
  listOpenApiRateLimitPolicies,
  saveOpenApiRateLimitPolicy,
  type OpenApiRateLimitPolicyResponse,
  type OpenApiRateLimitPolicySaveRequest,
} from '@/api/openapi/rate-limit-policy';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 当前开发者应用编码。 */
const appCode = ref('');
/** 限流策略列表。 */
const policies = ref<OpenApiRateLimitPolicyResponse[]>([]);
/** 列表加载状态。 */
const loading = ref(false);
/** 保存提交状态。 */
const saving = ref(false);

/** 限流策略权限编码，供页面按钮和后续权限指令统一引用。 */
const permissionCodes = {
  /** 保存限流策略权限。 */
  save: 'openapi:rate-limit-policy:save',
};

/** 策略状态下拉选项。 */
const statusOptions = [
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' },
];

/** 限流策略表格列定义。 */
const columns = [
  { title: 'API 编码', dataIndex: 'apiCode', key: 'apiCode' },
  { title: '调用次数', dataIndex: 'limitCount', key: 'limitCount', width: 110 },
  { title: '窗口秒数', dataIndex: 'windowSeconds', key: 'windowSeconds', width: 110 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
];

/** 当前编辑的限流策略保存命令。 */
const command = reactive<OpenApiRateLimitPolicySaveRequest>(createEmptyCommand());

/**
 * 创建空限流策略保存命令。
 *
 * @returns 限流策略保存命令
 */
function createEmptyCommand(): OpenApiRateLimitPolicySaveRequest {
  return {
    tenantId: tenantId.value,
    appCode: appCode.value,
    apiCode: '',
    limitCount: 60,
    windowSeconds: 60,
    status: 'enabled',
  };
}

/**
 * 将限流策略响应复制到编辑命令。
 *
 * @param policy 限流策略响应
 */
function applyPolicy(policy: OpenApiRateLimitPolicyResponse): void {
  command.tenantId = tenantId.value;
  command.appCode = appCode.value;
  command.apiCode = policy.apiCode;
  command.limitCount = policy.limitCount;
  command.windowSeconds = policy.windowSeconds;
  command.status = policy.status;
}

/**
 * 重置限流策略编辑表单。
 */
function resetCommand(): void {
  Object.assign(command, createEmptyCommand());
}

/**
 * 查询当前应用的限流策略。
 */
async function loadPolicies(): Promise<void> {
  if (!appCode.value) {
    return;
  }
  loading.value = true;
  try {
    policies.value = await listOpenApiRateLimitPolicies(syncTenantContext(), appCode.value);
  } finally {
    loading.value = false;
  }
}

/**
 * 保存限流策略。
 */
async function handleSave(): Promise<void> {
  saving.value = true;
  try {
    command.tenantId = syncTenantContext();
    appCode.value = command.appCode;
    await saveOpenApiRateLimitPolicy(command);
    await loadPolicies();
    message.success('限流策略已保存');
  } finally {
    saving.value = false;
  }
}

/**
 * 构建限流策略表格行属性。
 *
 * @param record 限流策略响应
 * @returns 表格行属性
 */
function buildPolicyRowProps(record: OpenApiRateLimitPolicyResponse): { onClick: () => void } {
  return {
    onClick: () => applyPolicy(record),
  };
}

/**
 * 同步后台租户到限流策略表单。
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
.openapi-rate-limit-policy-page {
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
