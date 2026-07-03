<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="tenant-package-module-page">
    <a-card title="套餐授权" :bordered="false">
      <template #extra>
        <a-space>
          <a-select
            v-model:value="packageId"
            show-search
            option-filter-prop="label"
            :options="packageOptions"
            class="package-select"
            placeholder="请选择租户套餐"
          />
          <a-button :loading="status === 'loading'" @click="loadGrants">查询</a-button>
          <a-button type="primary" v-permission="'system:tenant-package:update'" @click="openBindForm">绑定授权</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <div class="grant-overview">
        <div class="grant-package">
          <span class="grant-package-label">当前套餐</span>
          <strong>{{ selectedPackageLabel }}</strong>
        </div>
        <a-space :size="8" wrap>
          <span class="overview-metric">授权项 {{ grants.length }}</span>
          <span class="overview-metric">模块 {{ authorizedModuleCount }}</span>
          <span class="overview-metric">菜单 {{ authorizedMenuCount }}</span>
          <span class="overview-metric">权限 {{ authorizedPermissionCount }}</span>
        </a-space>
      </div>

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="grants"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'moduleCode'">
            <span class="module-cell">
              <a-tag color="blue" class="module-code">{{ record.moduleCode }}</a-tag>
              <span class="module-name">{{ findModuleName(record.moduleCode) }}</span>
            </span>
          </template>
          <template v-if="column.key === 'resource'">
            <a-space :size="6" wrap>
              <a-tag v-if="record.menuCode" class="resource-tag">菜单 {{ record.menuCode }}</a-tag>
              <a-tag v-if="record.permission" class="resource-tag">权限 {{ record.permission }}</a-tag>
              <a-tag v-if="!record.menuCode && !record.permission" color="green" class="resource-tag">模块全部</a-tag>
            </a-space>
          </template>
          <template v-if="column.key === 'createdAt'">
            <span class="created-time">{{ formatDateTime(record.createdAt) }}</span>
          </template>
        </template>
      </a-table>

      <span class="permission-code">system:tenant-package:update</span>
    </a-card>

    <a-modal v-model:open="formOpen" title="绑定套餐授权" :confirm-loading="saving" width="860px" @ok="submitBindForm">
      <a-form layout="vertical">
        <a-form-item label="租户套餐">
          <a-select
            v-model:value="packageId"
            show-search
            option-filter-prop="label"
            :options="packageOptions"
            placeholder="请选择租户套餐"
          />
        </a-form-item>
        <a-form-item label="授权项">
          <div class="grant-editor">
            <a-space v-for="row in grantRows" :key="row.id" class="grant-row" align="start">
              <a-select
                v-model:value="row.moduleCode"
                class="module-select"
                :options="moduleOptions"
                show-search
                @change="handleModuleChange(row)"
              />
              <a-select
                v-model:value="row.menuCode"
                class="resource-select"
                :options="menuOptions(row.moduleCode)"
                allow-clear
                show-search
              />
              <a-select
                v-model:value="row.permission"
                class="permission-select"
                :options="permissionOptions(row.moduleCode)"
                allow-clear
                show-search
              />
              <a-button danger v-permission="'system:tenant-package:update'" @click="removeGrantRow(row.id)">删除</a-button>
            </a-space>
            <a-button
              type="dashed"
              class="add-row-button"
              v-permission="'system:tenant-package:update'"
              @click="addGrantRow"
            >
              新增授权项
            </a-button>
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { computed, onMounted, ref } from 'vue';

import {
  bindTenantPackageModuleGrants,
  listTenantPackageModuleGrants,
  type TenantPackageModuleBindPayload,
  type TenantPackageModuleGrant,
} from '@/api/system/tenant-package-module';
import { listSystemTenantPackages, type SystemTenantPackage } from '@/api/system/tenant-package';
import { listSystemModules, type SystemModule, type SystemModuleResource } from '@/api/system/module';
import type { LoadStatus } from '@/types/platform';

interface GrantEditRow {
  /** 授权行前端唯一标识。 */
  id: number;
  /** 模块编码。 */
  moduleCode: string;
  /** 菜单编码。 */
  menuCode?: string;
  /** 权限标识。 */
  permission?: string;
}

/** 当前租户套餐主键。 */
const packageId = ref(1);
/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 保存按钮加载状态。 */
const saving = ref(false);
/** 表单弹窗打开状态。 */
const formOpen = ref(false);
/** 异常提示文案。 */
const errorMessage = ref('');
/** 套餐授权资源列表。 */
const grants = ref<TenantPackageModuleGrant[]>([]);
/** 租户套餐候选项。 */
const tenantPackages = ref<SystemTenantPackage[]>([]);
/** 系统模块资源列表。 */
const modules = ref<SystemModule[]>([]);
/** 套餐授权编辑行。 */
const grantRows = ref<GrantEditRow[]>([]);
/** 授权行递增序列。 */
const rowSequence = ref(1);

/** 表格列定义。 */
const columns = [
  { title: '模块', dataIndex: 'moduleCode', key: 'moduleCode', width: 260 },
  { title: '授权资源', key: 'resource' },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 170 },
];

/** 租户套餐选择项。 */
const packageOptions = computed(() =>
  tenantPackages.value.map((tenantPackage) => ({
    label: `${tenantPackage.packageName}（${tenantPackage.packageCode}）`,
    value: tenantPackage.id,
  })),
);

/** 模块选择项。 */
const moduleOptions = computed(() =>
  modules.value
    .filter((module) => module.enabled)
    .map((module) => ({
      label: `${module.moduleName}（${module.moduleCode}）`,
      value: module.moduleCode,
    })),
);

/** 当前选择的套餐展示名称。 */
const selectedPackageLabel = computed(() => formatPackageName(packageId.value));

/** 已授权模块数量。 */
const authorizedModuleCount = computed(() => new Set(grants.value.map((grant) => grant.moduleCode)).size);

/** 已授权菜单数量。 */
const authorizedMenuCount = computed(() => grants.value.filter((grant) => Boolean(grant.menuCode)).length);

/** 已授权权限标识数量。 */
const authorizedPermissionCount = computed(() => grants.value.filter((grant) => Boolean(grant.permission)).length);

/**
 * 加载套餐授权资源列表。
 */
async function loadGrants(): Promise<void> {
  if (!packageId.value) {
    message.warning('请先选择租户套餐');
    return;
  }
  status.value = 'loading';
  try {
    const [moduleList, grantList] = await Promise.all([
      listSystemModules(),
      listTenantPackageModuleGrants(packageId.value),
    ]);
    modules.value = moduleList;
    grants.value = grantList;
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '套餐授权加载失败';
    status.value = 'error';
  }
}

/**
 * 加载租户套餐候选项。
 */
async function loadTenantPackages(): Promise<void> {
  try {
    tenantPackages.value = await listSystemTenantPackages('enabled');
    if (!packageOptions.value.some((option) => option.value === packageId.value)) {
      packageId.value = packageOptions.value[0]?.value ?? 1;
    }
  } catch (error) {
    message.error(error instanceof Error ? error.message : '租户套餐加载失败');
  }
}

/**
 * 打开套餐授权绑定表单。
 */
function openBindForm(): void {
  grantRows.value = grants.value.length
    ? grants.value.map((grant) => createGrantRow(grant.moduleCode, grant.menuCode, grant.permission))
    : [createGrantRow()];
  formOpen.value = true;
}

/**
 * 提交套餐授权绑定表单。
 */
async function submitBindForm(): Promise<void> {
  saving.value = true;
  try {
    const payload = buildBindPayload();
    const duplicateGrant = findDuplicateGrant(payload);
    if (duplicateGrant) {
      message.error(`套餐授权资源重复：${duplicateGrant}`);
      return;
    }
    await bindTenantPackageModuleGrants(packageId.value, payload);
    message.success('套餐授权已保存');
    formOpen.value = false;
    await loadGrants();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '套餐授权保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 生成指定模块的菜单选择项。
 *
 * @param moduleCode 模块编码
 * @returns 菜单选择项
 */
function menuOptions(moduleCode: string): Array<{ label: string; value: string }> {
  return resourceOptions(moduleCode, 'menu');
}

/**
 * 生成指定模块的权限选择项。
 *
 * @param moduleCode 模块编码
 * @returns 权限选择项
 */
function permissionOptions(moduleCode: string): Array<{ label: string; value: string }> {
  return resourceOptions(moduleCode, 'permission');
}

/**
 * 按模块和类型生成资源选择项。
 *
 * @param moduleCode 模块编码
 * @param resourceType 资源类型
 * @returns 资源选择项
 */
function resourceOptions(moduleCode: string, resourceType: string): Array<{ label: string; value: string }> {
  return findModuleResources(moduleCode)
    .filter((resource) => resource.resourceType === resourceType)
    .map((resource) => ({
      label: resource.resourceName ? `${resource.resourceName}（${resource.resourceCode}）` : resource.resourceCode,
      value: resource.resourceCode,
    }));
}

/**
 * 查询模块资源列表。
 *
 * @param moduleCode 模块编码
 * @returns 模块资源列表
 */
function findModuleResources(moduleCode: string): SystemModuleResource[] {
  return modules.value.find((module) => module.moduleCode === moduleCode)?.resources ?? [];
}

/**
 * 查询模块名称。
 *
 * @param moduleCode 模块编码
 * @returns 模块名称
 */
function findModuleName(moduleCode: string): string {
  return modules.value.find((module) => module.moduleCode === moduleCode)?.moduleName ?? '未命名模块';
}

/**
 * 模块切换后清空不属于新模块的资源选择。
 *
 * @param row 授权编辑行
 */
function handleModuleChange(row: GrantEditRow): void {
  row.menuCode = undefined;
  row.permission = undefined;
}

/**
 * 新增授权编辑行。
 */
function addGrantRow(): void {
  grantRows.value = [...grantRows.value, createGrantRow()];
}

/**
 * 删除授权编辑行。
 *
 * @param rowId 授权行前端唯一标识
 */
function removeGrantRow(rowId: number): void {
  grantRows.value = grantRows.value.filter((row) => row.id !== rowId);
}

/**
 * 创建授权编辑行。
 *
 * @param moduleCode 模块编码
 * @param menuCode 菜单编码
 * @param permission 权限标识
 * @returns 授权编辑行
 */
function createGrantRow(moduleCode = '', menuCode?: string, permission?: string): GrantEditRow {
  return {
    id: rowSequence.value++,
    moduleCode,
    menuCode,
    permission,
  };
}

/**
 * 构造套餐授权绑定参数。
 *
 * @returns 套餐授权绑定参数
 */
function buildBindPayload(): TenantPackageModuleBindPayload {
  return {
    grants: grantRows.value
      .filter((row) => row.moduleCode)
      .map((row) => ({
        moduleCode: row.moduleCode,
        menuCode: row.menuCode,
        permission: row.permission,
      })),
  };
}

/**
 * 查找重复授权资源。
 *
 * @param payload 套餐授权绑定参数
 * @returns 重复授权资源键；没有重复时返回空字符串
 */
function findDuplicateGrant(payload: TenantPackageModuleBindPayload): string {
  const seenKeys = new Set<string>();
  for (const grant of payload.grants) {
    const key = `${grant.moduleCode}|${grant.menuCode ?? ''}|${grant.permission ?? ''}`;
    if (seenKeys.has(key)) {
      return key;
    }
    seenKeys.add(key);
  }
  return '';
}

/**
 * 格式化租户套餐名称。
 *
 * @param value 租户套餐主键
 * @returns 租户套餐业务名称
 */
function formatPackageName(value?: number): string {
  const tenantPackage = tenantPackages.value.find((item) => item.id === value);
  return tenantPackage ? `${tenantPackage.packageName}（${tenantPackage.packageCode}）` : '未知套餐';
}

/**
 * 格式化日期时间。
 *
 * @param value 日期时间字符串
 * @returns 可读时间
 */
function formatDateTime(value?: string): string {
  return value ? value.replace('T', ' ').slice(0, 19) : '-';
}

onMounted(async () => {
  await loadTenantPackages();
  await loadGrants();
});
</script>

<style scoped>
.tenant-package-module-page {
  min-width: 0;
}

.package-select {
  width: 240px;
}

.state-alert {
  margin-bottom: 12px;
}

.grant-overview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f8fafc;
}

.grant-package {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.grant-package-label {
  color: #64748b;
  font-size: 13px;
}

.overview-metric {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 12px;
  background: #ffffff;
  color: #475569;
  font-size: 12px;
}

.module-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.module-code {
  margin-inline-end: 0;
  font-weight: 500;
}

.module-name {
  overflow: hidden;
  color: #64748b;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resource-tag {
  margin-inline-end: 0;
}

.created-time {
  color: #475569;
  font-variant-numeric: tabular-nums;
}

.full-field {
  width: 100%;
}

.grant-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.grant-row {
  width: 100%;
}

.module-select {
  width: 210px;
}

.resource-select {
  width: 220px;
}

.permission-select {
  width: 280px;
}

.add-row-button {
  width: 120px;
}

.permission-code {
  display: none;
}
</style>
