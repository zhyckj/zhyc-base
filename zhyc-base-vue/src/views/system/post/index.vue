<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="post-page">
    <a-card title="岗位管理" :bordered="false">
      <template #extra>
        <a-space>
          <a-button type="primary" v-permission="'system:post:create'" @click="openCreateForm">新增岗位</a-button>
          <a-input v-model:value="tenantId" class="tenant-id" />
          <a-select
            v-model:value="orgId"
            allow-clear
            show-search
            option-filter-prop="label"
            :options="orgOptions"
            class="org-select"
            placeholder="所属组织"
          />
          <a-button :loading="status === 'loading'" v-permission="'system:post:query'" @click="loadPosts">查询</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="posts"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'orgName'">
            {{ formatOrgName(record.orgId) }}
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">{{ $statusLabel(record.status) }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button size="small" v-permission="'system:post:update'" @click="openEditForm(record)">编辑</a-button>
              <a-button size="small" v-permission="'system:post:update-status'" @click="changeStatus(record)">
                {{ record.status === 'enabled' ? '停用' : '启用' }}
              </a-button>
              <a-button size="small" danger v-permission="'system:post:delete'" @click="removePost(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <span class="permission-code">system:post:query system:post:create system:post:update system:post:update-status system:post:delete</span>
    </a-card>

    <a-modal
      v-model:open="formOpen"
      :title="editingPost ? '编辑岗位' : '新增岗位'"
      :confirm-loading="saving"
      @ok="submitForm"
    >
      <a-form layout="vertical">
        <a-form-item label="所属组织">
          <a-select
            v-model:value="formState.orgId"
            allow-clear
            show-search
            option-filter-prop="label"
            :options="orgOptions"
            placeholder="请选择所属组织"
          />
        </a-form-item>
        <a-form-item label="岗位编码" required>
          <a-input v-model:value="formState.postCode" />
        </a-form-item>
        <a-form-item label="岗位名称" required>
          <a-input v-model:value="formState.postName" />
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

import { listSystemOrgTree, type SystemOrgTreeNode } from '@/api/system/org';
import {
  createSystemPost,
  deleteSystemPost,
  listSystemPosts,
  updateSystemPost,
  updateSystemPostStatus,
  type SystemPost,
  type SystemPostSavePayload,
} from '@/api/system/post';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 所属组织主键过滤条件。 */
const orgId = ref<number | undefined>();
/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 保存按钮加载状态。 */
const saving = ref(false);
/** 表单弹窗打开状态。 */
const formOpen = ref(false);
/** 异常提示文案。 */
const errorMessage = ref('');
/** 岗位列表。 */
const posts = ref<SystemPost[]>([]);
/** 组织机构树。 */
const orgTree = ref<SystemOrgTreeNode[]>([]);
/** 当前编辑岗位。 */
const editingPost = ref<SystemPost>();
/** 岗位表单状态。 */
const formState = reactive<SystemPostSavePayload>({
  tenantId: '',
  orgId: undefined,
  postCode: '',
  postName: '',
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
  { title: '岗位名称', dataIndex: 'postName', key: 'postName' },
  { title: '岗位编码', dataIndex: 'postCode', key: 'postCode', width: 180 },
  { title: '所属组织', key: 'orgName', width: 220 },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 90 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 220 },
];

/** 组织下拉项。 */
const orgOptions = computed(() => flattenOrgOptions(orgTree.value));

/**
 * 加载岗位列表。
 */
async function loadPosts(): Promise<void> {
  status.value = 'loading';
  try {
    posts.value = await listSystemPosts(syncTenantContext(), orgId.value);
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '岗位加载失败';
    status.value = 'error';
  }
}

/**
 * 加载岗位所属组织候选项。
 */
async function loadOrgOptions(): Promise<void> {
  try {
    orgTree.value = await listSystemOrgTree(syncTenantContext());
  } catch (error) {
    message.error(error instanceof Error ? error.message : '组织机构加载失败');
  }
}

function openCreateForm(): void {
  editingPost.value = undefined;
  Object.assign(formState, {
    tenantId: syncTenantContext(),
    orgId: orgId.value,
    postCode: '',
    postName: '',
    sortOrder: 0,
    status: 'enabled',
  });
  formOpen.value = true;
}

function openEditForm(post: SystemPost): void {
  editingPost.value = post;
  Object.assign(formState, {
    tenantId: syncTenantContext(),
    orgId: post.orgId,
    postCode: post.postCode,
    postName: post.postName,
    sortOrder: post.sortOrder ?? 0,
    status: post.status,
  });
  formOpen.value = true;
}

async function submitForm(): Promise<void> {
  if (!formState.postCode.trim() || !formState.postName.trim()) {
    message.error('请填写岗位编码和岗位名称');
    return;
  }
  saving.value = true;
  try {
    const payload = {
      ...formState,
      tenantId: syncTenantContext(),
      postCode: formState.postCode.trim(),
      postName: formState.postName.trim(),
    };
    if (editingPost.value) {
      await updateSystemPost(editingPost.value.id, payload);
      message.success('岗位已更新');
    } else {
      await createSystemPost(payload);
      message.success('岗位已新增');
    }
    formOpen.value = false;
    await loadPosts();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '岗位保存失败');
  } finally {
    saving.value = false;
  }
}

async function changeStatus(post: SystemPost): Promise<void> {
  const nextStatus = post.status === 'enabled' ? 'disabled' : 'enabled';
  try {
    await updateSystemPostStatus(post.id, syncTenantContext(), nextStatus);
    message.success(nextStatus === 'enabled' ? '岗位已启用' : '岗位已停用');
    await loadPosts();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '岗位状态调整失败');
  }
}

function removePost(post: SystemPost): void {
  Modal.confirm({
    title: '确认删除岗位',
    content: `删除岗位：${post.postName}`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      await deleteSystemPost(post.id, syncTenantContext());
      message.success('岗位已删除');
      await loadPosts();
    },
  });
}

/**
 * 同步后台租户到岗位查询条件。
 *
 * @returns 当前租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId();
  tenantId.value = currentTenantId;
  return currentTenantId;
}

/**
 * 格式化所属组织展示名称。
 *
 * @param value 所属组织主键
 * @returns 组织业务名称
 */
function formatOrgName(value?: number): string {
  if (!value) {
    return '未设置';
  }
  const option = orgOptions.value.find((item) => item.value === value);
  return option ? option.label.replace(/^　+/, '') : '未知组织';
}

/**
 * 展平组织树为选择项。
 *
 * @param nodes 组织树节点
 * @param level 当前层级
 * @returns 组织选择项
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

onMounted(() => {
  void loadPosts();
  void loadOrgOptions();
});
</script>

<style scoped>
.post-page {
  min-width: 0;
}

.tenant-id {
  width: 180px;
}

.org-select {
  width: 220px;
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
