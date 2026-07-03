<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="password-policy-page">
    <a-card title="密码策略" :bordered="false">
      <template #extra>
        <a-space>
          <a-input v-model:value="tenantId" class="tenant-id" />
          <a-button :loading="status === 'loading'" @click="loadPolicy">查询</a-button>
          <a-button type="primary" v-permission="'system:password-policy:save'" :loading="saving" @click="submitPolicy">
            保存策略
          </a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-form layout="vertical" class="policy-form">
        <a-row :gutter="16">
          <a-col :xs="24" :md="12">
            <a-form-item label="策略编码">
              <a-input v-model:value="formState.policyCode" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item label="策略名称">
              <a-input v-model:value="formState.policyName" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="最小长度">
              <a-input-number v-model:value="formState.minLength" :min="1" class="number-input" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="有效天数">
              <a-input-number v-model:value="formState.expireDays" :min="0" class="number-input" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="历史记忆次数">
              <a-input-number v-model:value="formState.historyCount" :min="0" class="number-input" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="最大失败次数">
              <a-input-number v-model:value="formState.maxRetryCount" :min="1" class="number-input" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="锁定分钟数">
              <a-input-number v-model:value="formState.lockMinutes" :min="0" class="number-input" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="启用策略">
              <a-switch v-model:checked="formState.enabled" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider orientation="left">复杂度要求</a-divider>

        <a-space wrap>
          <a-checkbox v-model:checked="formState.requireUppercase">大写字母</a-checkbox>
          <a-checkbox v-model:checked="formState.requireLowercase">小写字母</a-checkbox>
          <a-checkbox v-model:checked="formState.requireDigit">数字</a-checkbox>
          <a-checkbox v-model:checked="formState.requireSpecial">特殊字符</a-checkbox>
        </a-space>
      </a-form>

      <span class="permission-code">system:password-policy:save</span>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';

import {
  getSystemPasswordPolicy,
  saveSystemPasswordPolicy,
  type SystemPasswordPolicySavePayload,
} from '@/api/system/password-policy';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 保存按钮加载状态。 */
const saving = ref(false);
/** 异常提示文案。 */
const errorMessage = ref('');
/** 密码策略表单状态。 */
const formState = reactive<SystemPasswordPolicySavePayload>({
  tenantId: getAdminRuntimeContext().tenantId,
  policyCode: 'default',
  policyName: '默认密码策略',
  minLength: 8,
  requireUppercase: true,
  requireLowercase: true,
  requireDigit: true,
  requireSpecial: false,
  expireDays: 90,
  historyCount: 3,
  maxRetryCount: 5,
  lockMinutes: 30,
  enabled: true,
});

/**
 * 加载租户密码策略。
 */
async function loadPolicy(): Promise<void> {
  status.value = 'loading';
  try {
    const currentTenantId = syncTenantContext();
    const policy = await getSystemPasswordPolicy(currentTenantId);
    Object.assign(formState, {
      tenantId: currentTenantId,
      ...policy,
    });
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '密码策略加载失败';
    status.value = 'error';
  }
}

/**
 * 提交租户密码策略。
 */
async function submitPolicy(): Promise<void> {
  saving.value = true;
  try {
    const currentTenantId = syncTenantContext();
    await saveSystemPasswordPolicy({
      ...formState,
      tenantId: currentTenantId,
    });
    await loadPolicy();
  } finally {
    saving.value = false;
  }
}

/**
 * 同步后台运行时租户上下文。
 *
 * @returns 当前后台租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId();
  tenantId.value = currentTenantId;
  return currentTenantId;
}

onMounted(() => {
  void loadPolicy();
});
</script>

<style scoped>
.password-policy-page {
  min-width: 0;
}

.tenant-id {
  width: 180px;
}

.state-alert {
  margin-bottom: 12px;
}

.policy-form {
  max-width: 980px;
}

.number-input {
  width: 100%;
}

.permission-code {
  display: none;
}
</style>
