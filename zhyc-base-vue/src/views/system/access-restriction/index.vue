<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="access-restriction-page">
    <a-card title="访问限制" :bordered="false">
      <template #extra>
        <a-space>
          <a-input v-model:value="tenantId" class="tenant-id" />
          <a-select v-model:value="restrictionType" class="type-select">
            <a-select-option value="ip">IP</a-select-option>
            <a-select-option value="account">账号</a-select-option>
            <a-select-option value="device">设备</a-select-option>
          </a-select>
          <a-button :loading="status === 'loading'" @click="loadRestrictions">查询</a-button>
          <a-button type="primary" v-permission="'system:access-restriction:save'" @click="openCreateForm">新增规则</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="restrictions"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'effect'">
            <a-tag :color="record.effect === 'allow' ? 'green' : 'red'">
              {{ record.effect === 'allow' ? '允许' : '拒绝' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'timeRange'">
            {{ formatTimeRange(record.startAt, record.endAt) }}
          </template>
          <template v-if="column.key === 'action'">
            <a-button size="small" v-permission="'system:access-restriction:save'" @click="openEditForm(record)">编辑</a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="formOpen" title="访问限制" :confirm-loading="saving" @ok="submitForm">
      <a-form layout="vertical">
        <a-form-item label="租户编码">
          <a-input v-model:value="formState.tenantId" />
        </a-form-item>
        <a-form-item label="限制类型">
          <a-select v-model:value="formState.restrictionType">
            <a-select-option value="ip">IP</a-select-option>
            <a-select-option value="account">账号</a-select-option>
            <a-select-option value="device">设备</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="规则值">
          <a-input v-model:value="formState.ruleValue" />
        </a-form-item>
        <a-form-item label="生效动作">
          <a-select v-model:value="formState.effect">
            <a-select-option value="allow">允许</a-select-option>
            <a-select-option value="deny">拒绝</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="生效开始时间">
          <a-input v-model:value="formState.startAt" type="datetime-local" />
        </a-form-item>
        <a-form-item label="生效结束时间">
          <a-input v-model:value="formState.endAt" type="datetime-local" />
        </a-form-item>
      </a-form>
      <span class="permission-code">system:access-restriction:save</span>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';

import {
  listSystemAccessRestrictions,
  saveSystemAccessRestriction,
  type SystemAccessRestriction,
  type SystemAccessRestrictionSavePayload,
} from '@/api/system/access-restriction';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 当前限制类型。 */
const restrictionType = ref('ip');
/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 保存按钮加载状态。 */
const saving = ref(false);
/** 表单弹窗打开状态。 */
const formOpen = ref(false);
/** 异常提示文案。 */
const errorMessage = ref('');
/** 访问限制列表。 */
const restrictions = ref<SystemAccessRestriction[]>([]);
/** 访问限制表单状态。 */
const formState = reactive<SystemAccessRestrictionSavePayload>({
  tenantId: getAdminRuntimeContext().tenantId,
  restrictionType: 'ip',
  ruleValue: '',
  effect: 'deny',
  startAt: undefined,
  endAt: undefined,
});

/** 表格列定义。 */
const columns = [
  { title: '限制类型', dataIndex: 'restrictionType', key: 'restrictionType', width: 110 },
  { title: '规则值', dataIndex: 'ruleValue', key: 'ruleValue' },
  { title: '生效动作', dataIndex: 'effect', key: 'effect', width: 110 },
  { title: '生效时间', key: 'timeRange' },
  { title: '操作', key: 'action', width: 100 },
];

/**
 * 加载访问限制列表。
 */
async function loadRestrictions(): Promise<void> {
  status.value = 'loading';
  try {
    restrictions.value = await listSystemAccessRestrictions(syncTenantContext(), restrictionType.value);
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '访问限制加载失败';
    status.value = 'error';
  }
}

/**
 * 打开新增访问限制表单。
 */
function openCreateForm(): void {
  const currentTenantId = syncTenantContext();
  Object.assign(formState, {
    tenantId: currentTenantId,
    restrictionType: restrictionType.value,
    ruleValue: '',
    effect: 'deny',
    startAt: undefined,
    endAt: undefined,
  });
  formOpen.value = true;
}

/**
 * 打开编辑访问限制表单。
 *
 * @param restriction 访问限制
 */
function openEditForm(restriction: SystemAccessRestriction): void {
  Object.assign(formState, {
    tenantId: restriction.tenantId,
    restrictionType: restriction.restrictionType,
    ruleValue: restriction.ruleValue,
    effect: restriction.effect,
    startAt: normalizeDateTime(restriction.startAt),
    endAt: normalizeDateTime(restriction.endAt),
  });
  formOpen.value = true;
}

/**
 * 提交访问限制表单。
 */
async function submitForm(): Promise<void> {
  saving.value = true;
  try {
    const currentTenantId = syncTenantContext();
    await saveSystemAccessRestriction({
      ...formState,
      tenantId: currentTenantId,
      startAt: normalizeDateTime(formState.startAt),
      endAt: normalizeDateTime(formState.endAt),
    });
    formOpen.value = false;
    formState.tenantId = currentTenantId;
    tenantId.value = currentTenantId;
    restrictionType.value = formState.restrictionType;
    await loadRestrictions();
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

/**
 * 格式化生效时间范围。
 *
 * @param startAt 生效开始时间
 * @param endAt 生效结束时间
 * @returns 生效时间范围文案
 */
function formatTimeRange(startAt?: string, endAt?: string): string {
  return `${startAt || '立即'} 至 ${endAt || '长期'}`;
}

/**
 * 规范化日期时间输入。
 *
 * @param value 日期时间值
 * @returns 后端 LocalDateTime 可解析的日期时间值
 */
function normalizeDateTime(value?: string): string | undefined {
  if (!value) {
    return undefined;
  }
  return value.length === 16 ? `${value}:00` : value;
}

onMounted(() => {
  void loadRestrictions();
});
</script>

<style scoped>
.access-restriction-page {
  min-width: 0;
}

.tenant-id {
  width: 180px;
}

.type-select {
  width: 120px;
}

.state-alert {
  margin-bottom: 12px;
}

.permission-code {
  display: none;
}
</style>
