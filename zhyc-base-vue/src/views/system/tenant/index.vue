<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="tenant-page">
    <a-card title="租户管理" :bordered="false">
      <template #extra>
        <a-space>
          <a-select v-model:value="statusFilter" class="status-filter" @change="loadTenants">
            <a-select-option value="enabled">启用租户</a-select-option>
            <a-select-option value="disabled">停用租户</a-select-option>
          </a-select>
          <a-button :loading="status === 'loading'" @click="loadTenants">刷新</a-button>
          <a-button type="primary" v-permission="'system:tenant:create'" @click="openCreateForm">新增租户</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-table
        row-key="tenantId"
        :columns="columns"
        :data-source="tenants"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'packageName'">
            {{ formatPackageName(record.packageId) }}
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
              {{ record.status === 'enabled' ? '启用' : '停用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button size="small" v-permission="'system:tenant:update'" @click="openEditForm(record)">
                编辑
              </a-button>
              <a-button
                v-if="record.status === 'enabled'"
                size="small"
                danger
                v-permission="'system:tenant:update-status'"
                @click="changeTenantStatus(record, 'disabled')"
              >
                停用
              </a-button>
              <a-button
                v-else
                size="small"
                type="primary"
                v-permission="'system:tenant:update-status'"
                @click="changeTenantStatus(record, 'enabled')"
              >
                启用
              </a-button>
              <a-button size="small" danger v-permission="'system:tenant:delete'" @click="removeTenant(record)">
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="formOpen"
      :title="editingTenant ? '编辑租户' : '新增租户'"
      :confirm-loading="saving"
      @ok="submitForm"
    >
      <a-form layout="vertical">
        <a-form-item label="租户编码">
          <a-input v-model:value="formState.tenantId" :disabled="Boolean(editingTenant)" />
        </a-form-item>
        <a-form-item label="租户名称">
          <a-input v-model:value="formState.name" />
        </a-form-item>
        <a-form-item label="租户套餐">
          <a-select
            v-model:value="formState.packageId"
            allow-clear
            show-search
            option-filter-prop="label"
            :options="packageOptions"
            placeholder="请选择租户套餐"
          />
        </a-form-item>
        <a-form-item label="隔离模式">
          <a-select v-model:value="formState.isolationMode">
            <a-select-option value="TENANT_COLUMN">租户字段</a-select-option>
            <a-select-option value="SCHEMA">独立 Schema</a-select-option>
            <a-select-option value="DATABASE">独立数据库</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="formState.status">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="联系人">
          <a-input v-model:value="formState.contactName" />
        </a-form-item>
        <a-form-item label="联系电话">
          <a-input v-model:value="formState.contactPhone" />
        </a-form-item>
        <a-form-item label="到期时间">
          <a-input v-model:value="formState.expireAt" placeholder="2026-12-31T23:59:59" />
        </a-form-item>
      </a-form>
      <span class="permission-code">
        system:tenant:create system:tenant:update system:tenant:update-status system:tenant:delete
      </span>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { Modal, message } from 'ant-design-vue';
import { computed, onMounted, reactive, ref } from 'vue';

import {
  listSystemTenantPackages,
  type SystemTenantPackage,
} from '@/api/system/tenant-package';
import {
  changeSystemTenantStatus,
  createSystemTenant,
  deleteSystemTenant,
  listSystemTenants,
  updateSystemTenant,
  type SystemTenant,
  type SystemTenantCreatePayload,
} from '@/api/system/tenant';
import type { LoadStatus } from '@/types/platform';

/** 租户状态筛选条件。 */
const statusFilter = ref('enabled');
/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 保存按钮加载状态。 */
const saving = ref(false);
/** 表单弹窗打开状态。 */
const formOpen = ref(false);
/** 当前编辑租户。 */
const editingTenant = ref<SystemTenant>();
/** 异常提示文案。 */
const errorMessage = ref('');
/** 系统租户列表。 */
const tenants = ref<SystemTenant[]>([]);
/** 租户套餐候选项。 */
const tenantPackages = ref<SystemTenantPackage[]>([]);
/** 租户创建表单状态。 */
const formState = reactive<SystemTenantCreatePayload>({
  tenantId: '',
  name: '',
  packageId: undefined,
  isolationMode: 'TENANT_COLUMN',
  status: 'enabled',
  contactName: '',
  contactPhone: '',
  expireAt: '',
});

/** 表格列定义。 */
const columns = [
  { title: '租户编码', dataIndex: 'tenantId', key: 'tenantId' },
  { title: '租户名称', dataIndex: 'name', key: 'name' },
  { title: '租户套餐', key: 'packageName', width: 180 },
  { title: '隔离模式', dataIndex: 'isolationMode', key: 'isolationMode', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '联系人', dataIndex: 'contactName', key: 'contactName', width: 120 },
  { title: '到期时间', dataIndex: 'expireAt', key: 'expireAt', width: 180 },
  { title: '操作', key: 'action', width: 150 },
];

/** 租户套餐选择项。 */
const packageOptions = computed(() =>
  tenantPackages.value.map((tenantPackage) => ({
    label: `${tenantPackage.packageName}（${tenantPackage.packageCode}）`,
    value: tenantPackage.id,
  })),
);

/**
 * 加载系统租户列表。
 */
async function loadTenants(): Promise<void> {
  status.value = 'loading';
  try {
    tenants.value = await listSystemTenants(statusFilter.value);
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '租户列表加载失败';
    status.value = 'error';
  }
}

/**
 * 加载租户套餐候选项。
 */
async function loadTenantPackages(): Promise<void> {
  try {
    tenantPackages.value = await listSystemTenantPackages('enabled');
  } catch (error) {
    message.error(error instanceof Error ? error.message : '租户套餐加载失败');
  }
}

/**
 * 打开新增租户表单。
 */
function openCreateForm(): void {
  editingTenant.value = undefined;
  Object.assign(formState, {
    tenantId: '',
    name: '',
    packageId: undefined,
    isolationMode: 'TENANT_COLUMN',
    status: 'enabled',
    contactName: '',
    contactPhone: '',
    expireAt: '',
  });
  formOpen.value = true;
}

/**
 * 打开编辑租户表单。
 *
 * @param tenant 系统租户
 */
function openEditForm(tenant: SystemTenant): void {
  editingTenant.value = tenant;
  Object.assign(formState, {
    tenantId: tenant.tenantId,
    name: tenant.name,
    packageId: tenant.packageId,
    isolationMode: tenant.isolationMode,
    status: tenant.status,
    contactName: tenant.contactName ?? '',
    contactPhone: tenant.contactPhone ?? '',
    expireAt: tenant.expireAt ?? '',
  });
  formOpen.value = true;
}

/**
 * 提交租户创建表单。
 */
async function submitForm(): Promise<void> {
  if (!formState.tenantId.trim() || !formState.name.trim()) {
    message.error('请填写租户编码和租户名称');
    return;
  }
  saving.value = true;
  try {
    const payload = {
      ...formState,
      tenantId: formState.tenantId.trim(),
      name: formState.name.trim(),
      contactName: formState.contactName?.trim() || '',
      contactPhone: formState.contactPhone?.trim() || '',
      expireAt: formState.expireAt?.trim() || '',
    };
    if (editingTenant.value) {
      await updateSystemTenant(editingTenant.value.tenantId, payload);
      message.success('租户已更新');
    } else {
      await createSystemTenant(payload);
      message.success('租户已保存');
    }
    formOpen.value = false;
    statusFilter.value = formState.status;
    await loadTenants();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '租户保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 修改租户状态。
 *
 * @param tenant 系统租户
 * @param nextStatus 目标状态
 */
async function changeTenantStatus(tenant: SystemTenant, nextStatus: string): Promise<void> {
  Modal.confirm({
    title: nextStatus === 'enabled' ? '确认启用租户' : '确认停用租户',
    content: `租户：${tenant.name}（${tenant.tenantId}）`,
    async onOk() {
      try {
        await changeSystemTenantStatus(tenant.tenantId, nextStatus);
        message.success('租户状态已更新');
        await loadTenants();
      } catch (error) {
        message.error(error instanceof Error ? error.message : '租户状态更新失败');
      }
    },
  });
}

/**
 * 删除租户主记录。
 *
 * @param tenant 系统租户
 */
function removeTenant(tenant: SystemTenant): void {
  Modal.confirm({
    title: '确认删除租户',
    content: `仅删除租户主记录，不会级联清空租户业务数据：${tenant.name}（${tenant.tenantId}）`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      await deleteSystemTenant(tenant.tenantId);
      message.success('租户主记录已删除');
      await loadTenants();
    },
  });
}

/**
 * 格式化租户套餐名称。
 *
 * @param packageId 租户套餐主键
 * @returns 租户套餐业务名称
 */
function formatPackageName(packageId?: number): string {
  if (!packageId) {
    return '未绑定套餐';
  }
  const tenantPackage = tenantPackages.value.find((item) => item.id === packageId);
  return tenantPackage ? `${tenantPackage.packageName}（${tenantPackage.packageCode}）` : '未知套餐';
}

onMounted(() => {
  void loadTenants();
  void loadTenantPackages();
});
</script>

<style scoped>
.tenant-page {
  min-width: 0;
}

.status-filter {
  width: 120px;
}

.state-alert {
  margin-bottom: 12px;
}

.full-field {
  width: 100%;
}

.permission-code {
  display: none;
}
</style>
