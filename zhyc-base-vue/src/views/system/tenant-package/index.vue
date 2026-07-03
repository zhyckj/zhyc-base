<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="tenant-package-page">
    <a-card title="租户套餐" :bordered="false">
      <template #extra>
        <a-space>
          <a-select v-model:value="statusFilter" class="status-filter" @change="loadPackages">
            <a-select-option value="enabled">启用套餐</a-select-option>
            <a-select-option value="disabled">停用套餐</a-select-option>
          </a-select>
          <a-button type="primary" v-permission="'system:tenant-package:update'" @click="openCreateForm">
            新增套餐
          </a-button>
          <a-button :loading="status === 'loading'" @click="loadPackages">刷新</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-table
        row-key="packageCode"
        :columns="columns"
        :data-source="packages"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
              {{ record.status === 'enabled' ? '启用' : '停用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button
              v-if="record.status === 'enabled'"
              size="small"
              danger
              v-permission="'system:tenant-package:update'"
              @click="changePackageStatus(record, 'disabled')"
            >
              停用
            </a-button>
            <a-button
              v-else
              size="small"
              type="primary"
              v-permission="'system:tenant-package:update'"
              @click="changePackageStatus(record, 'enabled')"
            >
              启用
            </a-button>
          </template>
        </template>
      </a-table>

      <span class="permission-code">system:tenant-package:update</span>
    </a-card>

    <a-modal
      v-model:open="createFormOpen"
      title="新增套餐"
      :confirm-loading="saving"
      width="640px"
      @ok="submitCreateForm"
      @cancel="resetCreateForm"
    >
      <a-form ref="createFormRef" :model="createForm" :rules="createRules" layout="vertical">
        <a-form-item label="套餐编码" name="packageCode">
          <a-input v-model:value="createForm.packageCode" placeholder="例如 enterprise-plus" />
        </a-form-item>
        <a-form-item label="套餐名称" name="packageName">
          <a-input v-model:value="createForm.packageName" placeholder="请输入套餐名称" />
        </a-form-item>
        <a-form-item label="套餐状态" name="status">
          <a-select v-model:value="createForm.status">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :xs="24" :sm="12">
            <a-form-item label="最大用户数" name="maxUserCount">
              <a-input-number v-model:value="createForm.maxUserCount" class="limit-input" :min="0" :precision="0" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12">
            <a-form-item label="存储容量 MB" name="maxStorageMb">
              <a-input-number v-model:value="createForm.maxStorageMb" class="limit-input" :min="0" :precision="0" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { Modal, message } from 'ant-design-vue';
import { onMounted, reactive, ref } from 'vue';

import {
  changeSystemTenantPackageStatus,
  createSystemTenantPackage,
  listSystemTenantPackages,
  type SystemTenantPackage,
  type SystemTenantPackageCreatePayload,
} from '@/api/system/tenant-package';
import type { LoadStatus } from '@/types/platform';

/** 套餐状态筛选条件。 */
const statusFilter = ref('enabled');
/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 异常提示文案。 */
const errorMessage = ref('');
/** 系统租户套餐列表。 */
const packages = ref<SystemTenantPackage[]>([]);
/** 新增套餐弹窗显示状态。 */
const createFormOpen = ref(false);
/** 新增套餐保存状态。 */
const saving = ref(false);
/** 新增套餐表单引用。 */
const createFormRef = ref();
/** 新增套餐表单状态。 */
const createForm = reactive<SystemTenantPackageCreatePayload>({
  packageCode: '',
  packageName: '',
  status: 'enabled',
  maxUserCount: 0,
  maxStorageMb: 0,
});
/** 新增套餐表单校验规则。 */
const createRules = {
  packageCode: [
    { required: true, message: '请输入套餐编码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9-]{1,63}$/, message: '套餐编码需以小写字母开头，仅支持小写字母、数字和中划线', trigger: 'blur' },
  ],
  packageName: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择套餐状态', trigger: 'change' }],
  maxUserCount: [{ type: 'number', min: 0, message: '最大用户数不能小于 0', trigger: 'change' }],
  maxStorageMb: [{ type: 'number', min: 0, message: '存储容量不能小于 0', trigger: 'change' }],
};

/** 表格列定义。 */
const columns = [
  { title: '套餐编码', dataIndex: 'packageCode', key: 'packageCode' },
  { title: '套餐名称', dataIndex: 'packageName', key: 'packageName' },
  { title: '最大用户数', dataIndex: 'maxUserCount', key: 'maxUserCount', width: 120 },
  { title: '存储容量 MB', dataIndex: 'maxStorageMb', key: 'maxStorageMb', width: 130 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '更新时间', dataIndex: 'updatedAt', key: 'updatedAt', width: 180 },
  { title: '操作', key: 'action', width: 100 },
];

/**
 * 加载系统租户套餐列表。
 */
async function loadPackages(): Promise<void> {
  status.value = 'loading';
  try {
    packages.value = await listSystemTenantPackages(statusFilter.value);
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '租户套餐加载失败';
    status.value = 'error';
  }
}

/**
 * 打开新增套餐表单。
 */
function openCreateForm(): void {
  resetCreateForm();
  createFormOpen.value = true;
}

/**
 * 重置新增套餐表单。
 */
function resetCreateForm(): void {
  createForm.packageCode = '';
  createForm.packageName = '';
  createForm.status = 'enabled';
  createForm.maxUserCount = 0;
  createForm.maxStorageMb = 0;
  createFormRef.value?.clearValidate?.();
}

/**
 * 提交新增套餐表单。
 */
async function submitCreateForm(): Promise<void> {
  try {
    await createFormRef.value?.validate?.();
    saving.value = true;
    await createSystemTenantPackage({
      packageCode: createForm.packageCode.trim(),
      packageName: createForm.packageName.trim(),
      status: createForm.status,
      maxUserCount: Number(createForm.maxUserCount ?? 0),
      maxStorageMb: Number(createForm.maxStorageMb ?? 0),
    });
    const createdStatus = createForm.status;
    message.success('租户套餐已新增');
    createFormOpen.value = false;
    statusFilter.value = createdStatus;
    resetCreateForm();
    await loadPackages();
  } catch (error) {
    if (isValidationError(error)) {
      return;
    }
    message.error(error instanceof Error ? error.message : '租户套餐新增失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 修改租户套餐状态。
 *
 * @param tenantPackage 系统租户套餐
 * @param nextStatus 目标状态
 */
async function changePackageStatus(tenantPackage: SystemTenantPackage, nextStatus: string): Promise<void> {
  Modal.confirm({
    title: nextStatus === 'enabled' ? '确认启用套餐' : '确认停用套餐',
    content: `套餐：${tenantPackage.packageName}（${tenantPackage.packageCode}）`,
    async onOk() {
      try {
        await changeSystemTenantPackageStatus(tenantPackage.packageCode, nextStatus);
        message.success('租户套餐状态已更新');
        await loadPackages();
      } catch (error) {
        message.error(error instanceof Error ? error.message : '租户套餐状态更新失败');
      }
    },
  });
}

/**
 * 判断是否为 Ant Design Vue 表单校验异常。
 *
 * @param error 待判断异常
 * @returns 是否为表单校验异常
 */
function isValidationError(error: unknown): boolean {
  return typeof error === 'object' && error !== null && 'errorFields' in error;
}

onMounted(() => {
  void loadPackages();
});
</script>

<style scoped>
.tenant-package-page {
  min-width: 0;
}

.status-filter {
  width: 120px;
}

.state-alert {
  margin-bottom: 12px;
}

.limit-input {
  width: 100%;
}

.permission-code {
  display: none;
}
</style>
