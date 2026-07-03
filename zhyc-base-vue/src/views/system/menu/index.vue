<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="menu-page">
    <a-card title="菜单权限" :bordered="false">
      <template #extra>
        <a-space>
          <a-button type="primary" v-permission="'system:permission:create'" @click="openCreateForm">新增菜单</a-button>
          <a-input v-model:value="tenantId" class="tenant-id" />
          <a-button :loading="status === 'loading'" v-permission="'system:permission:query'" @click="loadTree">查询</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="menuTree"
        :expand-icon="renderMenuExpandIcon"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <span class="menu-name-cell">
              <span class="menu-name-text">{{ record.name }}</span>
              <a-badge
                class="menu-child-badge"
                :class="{ 'menu-child-badge-empty': getSecondLevelMenuCount(record) === 0 }"
                :count="getSecondLevelMenuCount(record)"
                :show-zero="true"
              />
            </span>
          </template>
          <template v-if="column.key === 'type'">
            <a-tag>{{ record.type }}</a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">{{ $statusLabel(record.status) }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button size="small" v-permission="'system:permission:update'" @click="openEditForm(record)">编辑</a-button>
              <a-button size="small" v-permission="'system:permission:update-status'" @click="changeStatus(record)">
                {{ record.status === 'enabled' ? '停用' : '启用' }}
              </a-button>
              <a-button size="small" danger v-permission="'system:permission:delete'" @click="removeMenu(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <span class="permission-code">system:permission:query system:permission:create system:permission:update system:permission:update-status system:permission:delete</span>
    </a-card>

    <a-modal
      v-model:open="formOpen"
      :title="editingMenu ? '编辑菜单权限' : '新增菜单权限'"
      :confirm-loading="saving"
      @ok="submitForm"
    >
      <a-form layout="vertical">
        <a-form-item label="父级菜单 ID">
          <a-input-number v-model:value="formState.parentId" :min="1" class="full-field" />
        </a-form-item>
        <a-form-item label="菜单编码" required>
          <a-input v-model:value="formState.menuCode" />
        </a-form-item>
        <a-form-item label="菜单名称" required>
          <a-input v-model:value="formState.name" />
        </a-form-item>
        <a-form-item label="菜单类型" required>
          <a-select v-model:value="formState.type" :options="menuTypeOptions" />
        </a-form-item>
        <a-form-item label="路由">
          <a-input v-model:value="formState.path" />
        </a-form-item>
        <a-form-item label="组件">
          <a-input v-model:value="formState.component" />
        </a-form-item>
        <a-form-item label="权限标识">
          <a-input v-model:value="formState.permission" />
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
import { h, onMounted, reactive, ref, type VNode } from 'vue';

import {
  createSystemMenu,
  deleteSystemMenu,
  listSystemMenuTree,
  updateSystemMenu,
  updateSystemMenuStatus,
  type SystemMenuSavePayload,
  type SystemMenuTreeNode,
} from '@/api/system/menu';
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
/** 菜单权限树。 */
const menuTree = ref<SystemMenuTreeNode[]>([]);
/** 当前编辑菜单。 */
const editingMenu = ref<SystemMenuTreeNode>();
/** 菜单保存表单。 */
const formState = reactive<SystemMenuSavePayload>({
  tenantId: '',
  parentId: undefined,
  menuCode: '',
  name: '',
  type: 'menu',
  path: '',
  component: '',
  permission: '',
  sortOrder: 0,
  status: 'enabled',
});
const menuTypeOptions = [
  { label: '目录', value: 'directory' },
  { label: '菜单', value: 'menu' },
  { label: '按钮', value: 'button' },
];
const statusOptions = [
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' },
];

/** 表格列定义。 */
const columns = [
  { title: '菜单名称', dataIndex: 'name', key: 'name' },
  { title: '菜单编码', dataIndex: 'menuCode', key: 'menuCode', width: 180 },
  { title: '类型', dataIndex: 'type', key: 'type', width: 100 },
  { title: '路由', dataIndex: 'path', key: 'path', width: 180 },
  { title: '权限标识', dataIndex: 'permission', key: 'permission', width: 220 },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 90 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 220 },
];

/**
 * 加载菜单权限树。
 */
async function loadTree(): Promise<void> {
  status.value = 'loading';
  try {
    menuTree.value = await listSystemMenuTree(syncTenantContext(), true);
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '菜单权限加载失败';
    status.value = 'error';
  }
}

function openCreateForm(): void {
  editingMenu.value = undefined;
  Object.assign(formState, {
    tenantId: syncTenantContext(),
    parentId: undefined,
    menuCode: '',
    name: '',
    type: 'menu',
    path: '',
    component: '',
    permission: '',
    sortOrder: 0,
    status: 'enabled',
  });
  formOpen.value = true;
}

function openEditForm(menu: SystemMenuTreeNode): void {
  editingMenu.value = menu;
  Object.assign(formState, {
    tenantId: syncTenantContext(),
    parentId: menu.parentId,
    menuCode: menu.menuCode,
    name: menu.name,
    type: menu.type,
    path: menu.path ?? '',
    component: menu.component ?? '',
    permission: menu.permission ?? '',
    sortOrder: menu.sortOrder ?? 0,
    status: menu.status ?? 'enabled',
  });
  formOpen.value = true;
}

async function submitForm(): Promise<void> {
  if (!formState.menuCode.trim() || !formState.name.trim()) {
    message.error('请填写菜单编码和菜单名称');
    return;
  }
  saving.value = true;
  try {
    const payload = {
      ...formState,
      tenantId: syncTenantContext(),
      menuCode: formState.menuCode.trim(),
      name: formState.name.trim(),
      path: formState.path?.trim(),
      component: formState.component?.trim(),
      permission: formState.permission?.trim(),
    };
    if (editingMenu.value) {
      await updateSystemMenu(editingMenu.value.id, payload);
      message.success('菜单权限已更新');
    } else {
      await createSystemMenu(payload);
      message.success('菜单权限已新增');
    }
    formOpen.value = false;
    await loadTree();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '菜单权限保存失败');
  } finally {
    saving.value = false;
  }
}

async function changeStatus(menu: SystemMenuTreeNode): Promise<void> {
  const nextStatus = menu.status === 'enabled' ? 'disabled' : 'enabled';
  try {
    await updateSystemMenuStatus(menu.id, syncTenantContext(), nextStatus);
    message.success(nextStatus === 'enabled' ? '菜单权限已启用' : '菜单权限已停用');
    await loadTree();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '菜单权限状态调整失败');
  }
}

function removeMenu(menu: SystemMenuTreeNode): void {
  Modal.confirm({
    title: '确认删除菜单权限',
    content: `删除菜单权限：${menu.name}`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      await deleteSystemMenu(menu.id, syncTenantContext());
      message.success('菜单权限已删除');
      await loadTree();
    },
  });
}

/**
 * 同步后台租户到菜单查询条件。
 *
 * @returns 当前租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId();
  tenantId.value = currentTenantId;
  return currentTenantId;
}

onMounted(() => {
  void loadTree();
});

/**
 * 获取当前菜单下的二级菜单数量。
 *
 * @param menu 菜单树节点
 * @returns 直接子菜单数量
 */
function getSecondLevelMenuCount(menu: SystemMenuTreeNode): number {
  return Array.isArray(menu.children) ? menu.children.length : 0;
}

/**
 * 渲染菜单树展开图标。
 *
 * <p>复用 Ant Design Vue 表格默认展开图标样式；无二级菜单时显示不可点击减号，避免空目录仍给用户可展开暗示。</p>
 *
 * @param props Ant Design Vue 表格树形图标参数
 * @returns 展开图标节点
 */
function renderMenuExpandIcon(props: {
  expanded: boolean;
  record: SystemMenuTreeNode;
  onExpand: (record: SystemMenuTreeNode, event: MouseEvent) => void;
}): VNode {
  const childCount = getSecondLevelMenuCount(props.record);
  if (childCount <= 0) {
    return h(
      'span',
      {
        class: 'ant-table-row-expand-icon ant-table-row-expand-icon-expanded menu-expand-empty',
        title: '无二级菜单',
      },
    );
  }
  const title = props.expanded ? '收起二级菜单' : '展开二级菜单';
  return h(
    'button',
    {
      type: 'button',
      class: [
        'ant-table-row-expand-icon',
        props.expanded ? 'ant-table-row-expand-icon-expanded' : 'ant-table-row-expand-icon-collapsed',
      ],
      title,
      'aria-label': title,
      onClick: (event: MouseEvent) => {
        event.stopPropagation();
        props.onExpand(props.record, event);
      },
    },
  );
}
</script>

<style scoped>
.menu-page {
  min-width: 0;
}

.tenant-id {
  width: 180px;
}

.state-alert {
  margin-bottom: 12px;
}

.menu-name-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.menu-name-text {
  color: #1f2937;
}

.menu-child-badge {
  display: inline-flex;
  align-items: center;
}

.menu-child-badge :deep(.ant-badge-count) {
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  border-radius: 9px;
  background: #e8f1ff;
  box-shadow: none;
  color: #2563eb;
  font-size: 12px;
  font-weight: 500;
  line-height: 18px;
}

.menu-child-badge-empty :deep(.ant-badge-count) {
  background: #f1f5f9;
  color: #94a3b8;
}

.menu-expand-empty {
  cursor: default;
  pointer-events: none;
}

.full-field {
  width: 100%;
}

.permission-code {
  display: none;
}
</style>
