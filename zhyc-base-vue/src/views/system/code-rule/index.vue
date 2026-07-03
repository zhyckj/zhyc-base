<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="code-rule-page">
    <a-card title="编码规则" :bordered="false">
      <template #extra>
        <a-space>
          <a-input v-model:value="tenantId" class="tenant-id" />
          <a-button :loading="status === 'loading'" @click="loadRules">查询</a-button>
          <a-button type="primary" v-permission="'system:code-rule:save'" @click="openSaveForm">新增规则</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-table
        row-key="ruleCode"
        :columns="columns"
        :data-source="rules"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'enabled'">
            <a-tag :color="record.enabled ? 'green' : 'default'">{{ record.enabled ? '启用' : '停用' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" v-permission="'system:code-rule:save'" @click="openSaveForm(record)">编辑</a-button>
              <a-button size="small" v-permission="'system:code-rule:generate'" @click="generateCode(record)">生成</a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <a-alert v-if="nextCode" type="success" show-icon :message="nextCode" class="state-alert next-code" />
      <span class="permission-code">system:code-rule:save</span>
      <span class="permission-code">system:code-rule:generate</span>
    </a-card>

    <a-modal v-model:open="formOpen" title="保存编码规则" :confirm-loading="saving" @ok="submitSaveForm">
      <a-form layout="vertical">
        <a-form-item label="规则编码">
          <a-input v-model:value="form.ruleCode" />
        </a-form-item>
        <a-form-item label="规则名称">
          <a-input v-model:value="form.ruleName" />
        </a-form-item>
        <a-form-item label="编码前缀">
          <a-input v-model:value="form.prefix" />
        </a-form-item>
        <a-form-item label="日期格式">
          <a-input v-model:value="form.datePattern" />
        </a-form-item>
        <a-form-item label="序列号长度">
          <a-input-number v-model:value="form.sequenceLength" :min="1" :max="12" class="full-field" />
        </a-form-item>
        <a-form-item label="当前序列值">
          <a-input-number v-model:value="form.currentValue" :min="0" class="full-field" />
        </a-form-item>
        <a-form-item label="启用状态">
          <a-switch v-model:checked="form.enabled" />
        </a-form-item>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';

import {
  generateNextSystemCode,
  listSystemCodeRules,
  saveSystemCodeRule,
  type SystemCodeRule,
} from '@/api/system/code-rule';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 保存按钮加载状态。 */
const saving = ref(false);
/** 表单弹窗打开状态。 */
const formOpen = ref(false);
/** 异常提示文案。 */
const errorMessage = ref('');
/** 最近生成的编码。 */
const nextCode = ref('');
/** 编码规则列表。 */
const rules = ref<SystemCodeRule[]>([]);

/** 编码规则表单。 */
const form = reactive<SystemCodeRule>({
  ruleCode: '',
  ruleName: '',
  prefix: '',
  datePattern: 'yyyyMMdd',
  sequenceLength: 4,
  currentValue: 0,
  enabled: true,
});

/** 表格列定义。 */
const columns = [
  { title: '规则名称', dataIndex: 'ruleName', key: 'ruleName' },
  { title: '规则编码', dataIndex: 'ruleCode', key: 'ruleCode', width: 180 },
  { title: '前缀', dataIndex: 'prefix', key: 'prefix', width: 120 },
  { title: '日期格式', dataIndex: 'datePattern', key: 'datePattern', width: 130 },
  { title: '序列长度', dataIndex: 'sequenceLength', key: 'sequenceLength', width: 100 },
  { title: '当前值', dataIndex: 'currentValue', key: 'currentValue', width: 100 },
  { title: '状态', dataIndex: 'enabled', key: 'enabled', width: 90 },
  { title: '操作', key: 'action', width: 140 },
];

/**
 * 加载编码规则列表。
 */
async function loadRules(): Promise<void> {
  status.value = 'loading';
  try {
    rules.value = await listSystemCodeRules(syncTenantContext());
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '编码规则加载失败';
    status.value = 'error';
  }
}

/**
 * 打开编码规则保存表单。
 *
 * @param rule 需要编辑的编码规则
 */
function openSaveForm(rule?: SystemCodeRule): void {
  Object.assign(form, rule ?? {
    ruleCode: '',
    ruleName: '',
    prefix: '',
    datePattern: 'yyyyMMdd',
    sequenceLength: 4,
    currentValue: 0,
    enabled: true,
  });
  formOpen.value = true;
}

/**
 * 提交编码规则保存表单。
 */
async function submitSaveForm(): Promise<void> {
  saving.value = true;
  try {
    await saveSystemCodeRule({
      tenantId: syncTenantContext(),
      ...form,
    });
    formOpen.value = false;
    await loadRules();
  } finally {
    saving.value = false;
  }
}

/**
 * 按编码规则生成下一个编码。
 *
 * @param rule 编码规则
 */
async function generateCode(rule: SystemCodeRule): Promise<void> {
  nextCode.value = await generateNextSystemCode({
    tenantId: syncTenantContext(),
    ruleCode: rule.ruleCode,
    businessDate: new Date().toISOString().slice(0, 10),
  });
  await loadRules();
}

/**
 * 同步后台租户到编码规则查询条件。
 *
 * @returns 当前租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId();
  tenantId.value = currentTenantId;
  return currentTenantId;
}

onMounted(() => {
  void loadRules();
});
</script>

<style scoped>
.code-rule-page {
  min-width: 0;
}

.tenant-id {
  width: 180px;
}

.state-alert {
  margin-bottom: 12px;
}

.next-code {
  margin-top: 12px;
}

.full-field {
  width: 100%;
}

.permission-code {
  display: none;
}
</style>
