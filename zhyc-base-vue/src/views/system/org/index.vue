<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="org-page">
    <a-card title="组织机构" :bordered="false">
      <template #extra>
        <a-space>
          <a-button type="primary" v-permission="'system:org:create'" @click="openCreateForm">新增组织</a-button>
          <a-input v-model:value="tenantId" class="tenant-id" />
          <a-button :loading="status === 'loading'" v-permission="'system:org:query'" @click="loadTree">查询</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="orgTree"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'leaderUser'">
            {{ formatUserName(record.leaderUserId) }}
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">{{ $statusLabel(record.status) }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button size="small" v-permission="'system:org:update'" @click="openEditForm(record)">编辑</a-button>
              <a-button size="small" v-permission="'system:org:update-status'" @click="changeStatus(record)">
                {{ record.status === 'enabled' ? '停用' : '启用' }}
              </a-button>
              <a-button size="small" danger v-permission="'system:org:delete'" @click="removeOrg(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <span class="permission-code">system:org:query system:org:create system:org:update system:org:update-status system:org:delete</span>
    </a-card>

    <a-modal
      v-model:open="formOpen"
      :title="editingOrg ? '编辑组织' : '新增组织'"
      :confirm-loading="saving"
      @ok="submitForm"
    >
      <a-form layout="vertical">
        <a-form-item label="父级组织">
          <a-select
            v-model:value="formState.parentId"
            allow-clear
            show-search
            option-filter-prop="label"
            :options="parentOrgOptions"
            placeholder="请选择父级组织"
          />
        </a-form-item>
        <a-form-item label="组织编码" required>
          <a-input v-model:value="formState.orgCode" />
        </a-form-item>
        <a-form-item label="组织名称" required>
          <a-input v-model:value="formState.orgName" />
        </a-form-item>
        <a-form-item label="负责人">
          <a-select
            v-model:value="formState.leaderUserId"
            allow-clear
            show-search
            option-filter-prop="label"
            :options="userOptions"
            placeholder="请选择负责人"
          />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="formState.sortOrder" :min="0" class="full-field" />
        </a-form-item>
        <a-form-item label="状态" required>
          <a-select v-model:value="formState.status" :options="statusOptions" />
        </a-form-item>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { Modal, message } from 'ant-design-vue';
import { computed, onMounted, reactive, ref } from 'vue';

import {
  createSystemOrg,
  deleteSystemOrg,
  listSystemOrgTree,
  updateSystemOrg,
  updateSystemOrgStatus,
  type SystemOrgSavePayload,
  type SystemOrgTreeNode,
} from '@/api/system/org';
import { listSystemUsers, type SystemUser } from '@/api/system/user';
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
/** 组织机构树。 */
const orgTree = ref<SystemOrgTreeNode[]>([]);
/** 负责人候选用户。 */
const users = ref<SystemUser[]>([]);
/** 当前编辑组织。 */
const editingOrg = ref<SystemOrgTreeNode>();
/** 组织保存表单。 */
const formState = reactive<SystemOrgSavePayload>({
  tenantId: '',
  parentId: undefined,
  orgCode: '',
  orgName: '',
  leaderUserId: undefined,
  sortOrder: 0,
  status: 'enabled',
});
/** 状态选项。 */
const statusOptions = [
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' },
];

/** 表格列定义。 */
const columns = [
  { title: '组织名称', dataIndex: 'orgName', key: 'orgName' },
  { title: '组织编码', dataIndex: 'orgCode', key: 'orgCode', width: 180 },
  { title: '负责人', key: 'leaderUser', width: 180 },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 90 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 220 },
];

/** 负责人用户下拉项。 */
const userOptions = computed(() =>
  users.value.map((user) => ({
    label: `${user.nickname}（${user.username}）`,
    value: user.id,
  })),
);

/** 父级组织下拉项，编辑时排除当前组织及其子组织。 */
const parentOrgOptions = computed(() => {
  const excludedIds = editingOrg.value ? collectOrgIds([editingOrg.value]) : new Set<number>();
  return flattenOrgOptions(orgTree.value).filter((option) => !excludedIds.has(option.value));
});

/**
 * 加载组织机构树。
 */
async function loadTree(): Promise<void> {
  status.value = 'loading';
  try {
    orgTree.value = await listSystemOrgTree(syncTenantContext());
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '组织机构加载失败';
    status.value = 'error';
  }
}

/**
 * 加载负责人候选用户。
 */
async function loadUsers(): Promise<void> {
  try {
    users.value = await listSystemUsers(syncTenantContext());
  } catch (error) {
    message.error(error instanceof Error ? error.message : '负责人用户加载失败');
  }
}

function openCreateForm(): void {
  editingOrg.value = undefined;
  Object.assign(formState, {
    tenantId: syncTenantContext(),
    parentId: undefined,
    orgCode: '',
    orgName: '',
    leaderUserId: undefined,
    sortOrder: 0,
    status: 'enabled',
  });
  formOpen.value = true;
}

function openEditForm(org: SystemOrgTreeNode): void {
  editingOrg.value = org;
  Object.assign(formState, {
    tenantId: syncTenantContext(),
    parentId: org.parentId,
    orgCode: org.orgCode,
    orgName: org.orgName,
    leaderUserId: org.leaderUserId,
    sortOrder: org.sortOrder ?? 0,
    status: org.status,
  });
  formOpen.value = true;
}

async function submitForm(): Promise<void> {
  if (!formState.orgCode.trim() || !formState.orgName.trim()) {
    message.error('请填写组织编码和组织名称');
    return;
  }
  saving.value = true;
  try {
    const payload = {
      ...formState,
      tenantId: syncTenantContext(),
      orgCode: formState.orgCode.trim(),
      orgName: formState.orgName.trim(),
    };
    if (editingOrg.value) {
      await updateSystemOrg(editingOrg.value.id, payload);
      message.success('组织已更新');
    } else {
      await createSystemOrg(payload);
      message.success('组织已新增');
    }
    formOpen.value = false;
    await loadTree();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '组织保存失败');
  } finally {
    saving.value = false;
  }
}

async function changeStatus(org: SystemOrgTreeNode): Promise<void> {
  const nextStatus = org.status === 'enabled' ? 'disabled' : 'enabled';
  try {
    await updateSystemOrgStatus(org.id, syncTenantContext(), nextStatus);
    message.success(nextStatus === 'enabled' ? '组织已启用' : '组织已停用');
    await loadTree();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '组织状态调整失败');
  }
}

function removeOrg(org: SystemOrgTreeNode): void {
  Modal.confirm({
    title: '确认删除组织',
    content: `删除组织：${org.orgName}`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      await deleteSystemOrg(org.id, syncTenantContext());
      message.success('组织已删除');
      await loadTree();
    },
  });
}

/**
 * 同步后台租户到组织查询条件。
 *
 * @returns 当前租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId();
  tenantId.value = currentTenantId;
  return currentTenantId;
}

/**
 * 格式化负责人展示名称。
 *
 * @param userId 负责人用户主键
 * @returns 用户业务名称
 */
function formatUserName(userId?: number): string {
  if (!userId) {
    return '未设置';
  }
  const user = users.value.find((item) => item.id === userId);
  return user ? `${user.nickname}（${user.username}）` : '未知用户';
}

/**
 * 展平组织树为下拉项。
 *
 * @param nodes 组织树节点
 * @param level 当前层级
 * @returns 组织下拉项
 */
function flattenOrgOptions(nodes: SystemOrgTreeNode[], level = 0): Array<{ label: string; value: number }> {
  return nodes.flatMap((node) => [
    {
      label: `${'　'.repeat(level)}${node.orgName}（${node.orgCode}）`,
      value: node.id,
    },
    ...flattenOrgOptions(node.children ?? [], level + 1),
  ]);
}

/**
 * 收集组织及所有子组织主键。
 *
 * @param nodes 组织树节点
 * @returns 组织主键集合
 */
function collectOrgIds(nodes: SystemOrgTreeNode[]): Set<number> {
  const ids = new Set<number>();
  nodes.forEach((node) => {
    ids.add(node.id);
    collectOrgIds(node.children ?? []).forEach((id) => ids.add(id));
  });
  return ids;
}

onMounted(() => {
  void loadTree();
  void loadUsers();
});
</script>

<style scoped>
.org-page {
  min-width: 0;
}

.tenant-id {
  width: 180px;
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
