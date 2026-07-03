<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="role-page">
    <a-card title="角色管理" :bordered="false">
      <template #extra>
        <a-space>
          <a-button type="primary" v-permission="'system:role:create'" @click="openCreateForm">新增角色</a-button>
          <a-input v-model:value="tenantId" class="tenant-id" />
          <a-button :loading="status === 'loading'" @click="loadRoles">查询</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="roles"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
      >
        <template #emptyText>
          <a-empty description="当前租户暂无角色" />
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'dataScope'">
            <a-tag>{{ dataScopeLabel(record.dataScope) }}</a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">{{ $statusLabel(record.status) }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button size="small" v-permission="'system:role:update'" @click="openEditForm(record)">编辑</a-button>
              <a-button size="small" v-permission="'system:role:edit'" @click="openDataScopeForm(record)">
                数据权限
              </a-button>
              <a-button size="small" v-permission="'system:role:update-status'" @click="changeStatus(record)">
                {{ record.status === 'enabled' ? '停用' : '启用' }}
              </a-button>
              <a-button size="small" v-permission="'system:role:authorize'" @click="openAuthorizeForm(record)">
                授权
              </a-button>
              <a-button size="small" danger v-permission="'system:role:delete'" @click="removeRole(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <span class="permission-code">
        system:role:create system:role:update system:role:update-status system:role:delete system:role:authorize system:role:edit
      </span>
    </a-card>

    <a-modal
      v-model:open="formOpen"
      :title="editingRole ? '编辑角色' : '新增角色'"
      :width="860"
      :confirm-loading="saving"
      @ok="submitForm"
    >
      <a-form layout="vertical" :model="formState">
        <a-tabs v-model:activeKey="activeFormTab">
          <a-tab-pane key="basic" tab="基础信息">
            <a-row :gutter="16">
              <a-col :xs="24" :md="12">
                <a-form-item label="角色编码" required>
                  <a-input v-model:value="formState.roleCode" placeholder="例如 platform-admin" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="角色名称" required>
                  <a-input v-model:value="formState.name" placeholder="请输入角色名称" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="数据权限" required>
                  <a-select v-model:value="formState.dataScope" :options="dataScopeOptions" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="状态" required>
                  <a-select v-model:value="formState.status" :options="statusOptions" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-tab-pane>

          <a-tab-pane key="dataScope" tab="数据权限范围" :disabled="!editingRole">
            <a-alert
              v-if="!editingRole"
              class="state-alert"
              type="info"
              show-icon
              message="新增角色保存后，可重新编辑该角色并维护自定义数据权限组织。"
            />

            <div v-else v-permission="'system:role:edit'" class="data-scope-panel">
              <a-alert
                class="state-alert"
                type="info"
                show-icon
                message="只有数据权限选择“自定义”时，下方组织范围才会参与后端数据过滤。切换为其他范围后保存会清空自定义组织范围。"
              />
              <a-alert
                v-if="dataScopeErrorMessage"
                class="state-alert"
                type="warning"
                show-icon
                :message="dataScopeErrorMessage"
              />

              <a-spin :spinning="dataScopeLoading">
                <a-row :gutter="16">
                  <a-col :xs="24" :lg="14">
                    <a-form-item label="授权组织">
                      <a-select
                        v-model:value="selectedOrgIds"
                        mode="multiple"
                        allow-clear
                        show-search
                        option-filter-prop="label"
                        :loading="dataScopeLoading"
                        :max-tag-count="4"
                        :options="orgOptions"
                        :disabled="!canEditRoleDataScope || formState.dataScope !== 'CUSTOM'"
                        placeholder="请选择授权组织"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :lg="10">
                    <div class="scope-summary">
                      <span>当前模式</span>
                      <strong>{{ dataScopeLabel(formState.dataScope) }}</strong>
                      <span>已选组织</span>
                      <strong>{{ selectedOrgIds.length }} 个</strong>
                    </div>
                  </a-col>
                </a-row>

                <div class="selection-preview">
                  <span class="preview-label">授权预览</span>
                  <a-empty v-if="selectedOrgDetails.length === 0" description="未选择组织" />
                  <div v-else class="org-tags">
                    <a-tag v-for="org in selectedOrgDetails" :key="org.value" color="blue">
                      {{ org.orgName }}（{{ org.orgCode }}）
                    </a-tag>
                  </div>
                </div>

                <div class="data-scope-actions">
                  <a-button
                    :disabled="dataScopeLoading || saving || !dataScopesLoaded"
                    @click="resetSelectionFromCurrent"
                  >
                    恢复当前授权
                  </a-button>
                </div>
              </a-spin>
              <span class="permission-code">system:role:edit</span>
            </div>
          </a-tab-pane>
        </a-tabs>
      </a-form>
    </a-modal>

    <a-modal v-model:open="authorizeOpen" title="角色菜单授权" :confirm-loading="saving" @ok="submitAuthorize">
      <a-form layout="vertical">
        <a-form-item label="角色">
          <a-input :value="currentRole ? `${currentRole.name}（${currentRole.roleCode}）` : ''" disabled />
        </a-form-item>
        <a-form-item label="菜单权限">
          <a-spin :spinning="menuLoading">
            <a-tree
              v-if="menuTreeData.length > 0"
              v-model:checkedKeys="checkedMenuKeys"
              checkable
              default-expand-all
              :tree-data="menuTreeData"
            />
            <a-empty v-else description="暂无可授权菜单，请先初始化菜单权限数据" />
          </a-spin>
        </a-form-item>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { Modal, message } from 'ant-design-vue';
import type { Key } from 'ant-design-vue/es/_util/type';
import { computed, onMounted, reactive, ref } from 'vue';

import { listSystemMenuTree, type SystemMenuTreeNode } from '@/api/system/menu';
import { listSystemOrgTree, type SystemOrgTreeNode } from '@/api/system/org';
import {
  bindSystemRoleMenus,
  createSystemRole,
  deleteSystemRole,
  listSystemRoleMenuIds,
  listSystemRoles,
  updateSystemRole,
  updateSystemRoleStatus,
  type SystemRole,
  type SystemRoleSavePayload,
} from '@/api/system/role';
import {
  bindSystemRoleDataScopes,
  listSystemRoleDataScopes,
  type SystemRoleDataScope,
} from '@/api/system/role-data-scope';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';
import { usePermission } from '@/utils/permission';

/**
 * 角色授权菜单树节点。
 */
interface RoleMenuTreeOption {
  /** 菜单树节点标题，展示菜单名称和权限标识。 */
  title: string;
  /** 菜单主键，提交授权时使用。 */
  key: number;
  /** 子级菜单树节点。 */
  children?: RoleMenuTreeOption[];
}

/**
 * 组织下拉选择项。
 */
interface OrgSelectOption {
  /** 下拉展示名称，包含缩进、组织名称和组织编码。 */
  label: string;
  /** 组织主键，提交角色数据权限时使用。 */
  value: number;
  /** 组织编码，用于授权预览和排查数据范围。 */
  orgCode: string;
  /** 组织名称，用于授权预览。 */
  orgName: string;
  /** 是否禁止选择，停用组织只能保留既有授权，不能新增选择。 */
  disabled?: boolean;
}

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 保存按钮加载状态。 */
const saving = ref(false);
/** 菜单树加载状态。 */
const menuLoading = ref(false);
/** 数据权限组织加载状态。 */
const dataScopeLoading = ref(false);
/** 授权弹窗打开状态。 */
const authorizeOpen = ref(false);
/** 新增编辑弹窗打开状态。 */
const formOpen = ref(false);
/** 异常提示文案。 */
const errorMessage = ref('');
/** 数据权限加载错误提示。 */
const dataScopeErrorMessage = ref('');
/** 角色列表。 */
const roles = ref<SystemRole[]>([]);
/** 当前授权角色。 */
const currentRole = ref<SystemRole>();
/** 当前编辑角色。 */
const editingRole = ref<SystemRole>();
/** 菜单树数据。 */
const menuTreeData = ref<RoleMenuTreeOption[]>([]);
/** 已勾选的菜单主键。 */
const checkedMenuKeys = ref<Key[]>([]);
/** 角色表单当前页签。 */
const activeFormTab = ref('basic');
/** 组织候选树。 */
const orgTree = ref<SystemOrgTreeNode[]>([]);
/** 当前角色已绑定的数据权限范围。 */
const currentDataScopes = ref<SystemRoleDataScope[]>([]);
/** 当前角色已选择的组织主键。 */
const selectedOrgIds = ref<number[]>([]);
/** 当前角色数据权限是否加载完成，避免加载失败时误清空旧授权。 */
const dataScopesLoaded = ref(false);
/** 页面权限判断工具。 */
const { hasPermission } = usePermission();

/** 角色表单状态。 */
const formState = reactive<SystemRoleSavePayload>({
  tenantId: '',
  roleCode: '',
  name: '',
  dataScope: 'ALL',
  status: 'enabled',
});
/** 角色状态选项。 */
const statusOptions = [
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' },
];
/** 数据权限选项。 */
const dataScopeOptions = [
  { label: '全部数据', value: 'ALL' },
  { label: '当前部门及下级', value: 'CURRENT_DEPT_AND_CHILDREN' },
  { label: '当前部门', value: 'CURRENT_DEPT' },
  { label: '仅本人', value: 'SELF' },
  { label: '自定义', value: 'CUSTOM' },
];

/** 表格列定义。 */
const columns = [
  { title: '角色名称', dataIndex: 'name', key: 'name' },
  { title: '角色编码', dataIndex: 'roleCode', key: 'roleCode', width: 180 },
  { title: '数据权限', dataIndex: 'dataScope', key: 'dataScope', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 360 },
];

/** 当前账号是否可以维护角色数据权限组织范围。 */
const canEditRoleDataScope = computed(() => Boolean(editingRole.value) && hasPermission('system:role:edit'));

/** 组织选择项。 */
const orgOptions = computed(() => flattenOrgOptions(orgTree.value));

/** 已选择组织详情，用于保存前预览。 */
const selectedOrgDetails = computed(() =>
  selectedOrgIds.value
    .map((orgId) => orgOptions.value.find((org) => org.value === orgId))
    .filter((org): org is OrgSelectOption => Boolean(org)),
);

/**
 * 加载角色列表。
 */
async function loadRoles(): Promise<void> {
  status.value = 'loading';
  try {
    roles.value = await listSystemRoles(syncTenantContext());
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '角色加载失败';
    status.value = 'error';
  }
}

/**
 * 打开新增角色表单。
 */
function openCreateForm(): void {
  editingRole.value = undefined;
  activeFormTab.value = 'basic';
  resetDataScopeState();
  Object.assign(formState, {
    tenantId: syncTenantContext(),
    roleCode: '',
    name: '',
    dataScope: 'ALL',
    status: 'enabled',
  });
  formOpen.value = true;
}

/**
 * 打开编辑角色表单。
 *
 * @param role 系统角色
 */
async function openEditForm(role: SystemRole): Promise<void> {
  await openRoleForm(role, 'basic');
}

/**
 * 打开角色数据权限表单。
 *
 * @param role 系统角色
 */
async function openDataScopeForm(role: SystemRole): Promise<void> {
  await openRoleForm(role, 'dataScope');
}

/**
 * 打开角色编辑弹窗并按需定位到指定页签。
 *
 * @param role 系统角色
 * @param tabKey 默认激活页签
 */
async function openRoleForm(role: SystemRole, tabKey: string): Promise<void> {
  editingRole.value = role;
  activeFormTab.value = tabKey;
  resetDataScopeState();
  Object.assign(formState, {
    tenantId: syncTenantContext(),
    roleCode: role.roleCode,
    name: role.name,
    dataScope: role.dataScope ?? 'ALL',
    status: role.status,
  });
  formOpen.value = true;
  if (canEditRoleDataScope.value) {
    await loadRoleDataScopeOptions(role.id);
  }
}

/**
 * 提交角色新增或编辑。
 */
async function submitForm(): Promise<void> {
  if (!validateRoleForm()) {
    return;
  }
  if (editingRole.value && canEditRoleDataScope.value && dataScopeLoading.value) {
    message.warning('角色数据权限正在加载，请稍后保存');
    return;
  }

  saving.value = true;
  try {
    const payload = {
      ...formState,
      tenantId: syncTenantContext(),
      roleCode: formState.roleCode.trim(),
      name: formState.name.trim(),
    };
    if (editingRole.value) {
      await updateSystemRole(editingRole.value.id, payload);
      if (canEditRoleDataScope.value && dataScopesLoaded.value) {
        await submitRoleDataScopes(editingRole.value.id);
      }
      message.success('角色已更新');
    } else {
      await createSystemRole(payload);
      message.success('角色已新增');
    }
    formOpen.value = false;
    await loadRoles();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '角色保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 校验角色基础表单。
 *
 * @returns 校验通过时返回 true
 */
function validateRoleForm(): boolean {
  if (!formState.roleCode.trim()) {
    activeFormTab.value = 'basic';
    message.warning('请填写角色编码');
    return false;
  }
  if (!formState.name.trim()) {
    activeFormTab.value = 'basic';
    message.warning('请填写角色名称');
    return false;
  }
  return true;
}

/**
 * 启用或停用角色。
 *
 * @param role 系统角色
 */
async function changeStatus(role: SystemRole): Promise<void> {
  const nextStatus = role.status === 'enabled' ? 'disabled' : 'enabled';
  saving.value = true;
  try {
    await updateSystemRoleStatus(role.id, syncTenantContext(), nextStatus);
    message.success(nextStatus === 'enabled' ? '角色已启用' : '角色已停用');
    await loadRoles();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '角色状态调整失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 删除角色。
 *
 * @param role 系统角色
 */
function removeRole(role: SystemRole): void {
  Modal.confirm({
    title: '确认删除角色',
    content: `删除后将同步移除用户角色绑定和授权关系：${role.name}`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      await deleteSystemRole(role.id, syncTenantContext());
      message.success('角色已删除');
      await loadRoles();
    },
  });
}

/**
 * 打开角色授权表单。
 *
 * @param role 系统角色
 */
function openAuthorizeForm(role: SystemRole): void {
  currentRole.value = role;
  checkedMenuKeys.value = [];
  authorizeOpen.value = true;
  void loadAuthorizeData(role.id);
}

/**
 * 提交角色菜单授权。
 */
async function submitAuthorize(): Promise<void> {
  if (!currentRole.value) {
    return;
  }
  const menuIds = parseCheckedMenuIds(checkedMenuKeys.value);
  if (menuIds.length === 0) {
    message.error('请至少选择一个菜单权限');
    return;
  }
  saving.value = true;
  try {
    await bindSystemRoleMenus(currentRole.value.id, {
      tenantId: syncTenantContext(),
      menuIds,
    });
    message.success('角色菜单授权已保存');
    authorizeOpen.value = false;
    await loadRoles();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '角色菜单授权失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 加载当前租户可授权菜单树和角色已授权菜单。
 *
 * @param roleId 角色主键
 */
async function loadAuthorizeData(roleId: number): Promise<void> {
  menuLoading.value = true;
  try {
    const currentTenantId = syncTenantContext();
    const [menus, roleMenuIds] = await Promise.all([
      listSystemMenuTree(currentTenantId),
      listSystemRoleMenuIds(roleId, currentTenantId),
    ]);
    menuTreeData.value = menus.map(toRoleMenuTreeOption);
    checkedMenuKeys.value = roleMenuIds;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '菜单权限加载失败');
    menuTreeData.value = [];
    checkedMenuKeys.value = [];
  } finally {
    menuLoading.value = false;
  }
}

/**
 * 加载当前角色的数据权限组织候选项和已授权组织。
 *
 * @param roleId 角色主键
 */
async function loadRoleDataScopeOptions(roleId: number): Promise<void> {
  dataScopeLoading.value = true;
  dataScopeErrorMessage.value = '';
  dataScopesLoaded.value = false;
  try {
    const currentTenantId = syncTenantContext();
    const [loadedOrgTree, loadedScopes] = await Promise.all([
      listSystemOrgTree(currentTenantId),
      listSystemRoleDataScopes(currentTenantId, roleId),
    ]);
    orgTree.value = loadedOrgTree;
    currentDataScopes.value = loadedScopes;
    resetSelectionFromCurrent();
    dataScopesLoaded.value = true;
  } catch (error) {
    resetDataScopeSelection();
    dataScopeErrorMessage.value = error instanceof Error ? error.message : '角色数据权限加载失败';
  } finally {
    dataScopeLoading.value = false;
  }
}

/**
 * 保存角色自定义数据权限组织范围。
 *
 * @param roleId 角色主键
 */
async function submitRoleDataScopes(roleId: number): Promise<void> {
  const orgIds = formState.dataScope === 'CUSTOM' ? selectedOrgIds.value : [];
  await bindSystemRoleDataScopes(syncTenantContext(), roleId, {
    orgIds,
  });
}

/**
 * 从当前已授权组织恢复编辑区。
 */
function resetSelectionFromCurrent(): void {
  selectedOrgIds.value = currentDataScopes.value.map((scope) => scope.orgId);
}

/**
 * 重置角色数据权限加载状态。
 */
function resetDataScopeState(): void {
  dataScopeErrorMessage.value = '';
  dataScopeLoading.value = false;
  dataScopesLoaded.value = false;
  orgTree.value = [];
  currentDataScopes.value = [];
  resetDataScopeSelection();
}

/**
 * 重置角色数据权限选择结果。
 */
function resetDataScopeSelection(): void {
  selectedOrgIds.value = [];
}

/**
 * 同步后台租户到角色请求参数。
 *
 * @returns 当前租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId();
  tenantId.value = currentTenantId;
  return currentTenantId;
}

/**
 * 转换菜单树节点为角色授权树节点。
 *
 * @param menu 系统菜单树节点
 * @returns 角色授权菜单树节点
 */
function toRoleMenuTreeOption(menu: SystemMenuTreeNode): RoleMenuTreeOption {
  const permissionText = menu.permission ? ` / ${menu.permission}` : '';
  return {
    title: `${menu.name}（${menu.menuCode}${permissionText}）`,
    key: menu.id,
    children: menu.children?.map(toRoleMenuTreeOption),
  };
}

/**
 * 解析勾选的菜单主键。
 *
 * @param keys Ant Design Vue 树组件勾选键
 * @returns 菜单主键列表
 */
function parseCheckedMenuIds(keys: Key[]): number[] {
  return keys
    .map((key) => Number(key))
    .filter((key) => Number.isInteger(key) && key > 0);
}

/**
 * 展平组织树为下拉项。
 *
 * @param nodes 组织树节点
 * @param level 当前层级
 * @returns 组织选择项
 */
function flattenOrgOptions(nodes: SystemOrgTreeNode[], level = 0): OrgSelectOption[] {
  return nodes.flatMap((node) => [
    {
      label: `${'　'.repeat(level)}${node.orgName}（${node.orgCode}）${node.status === 'enabled' ? '' : ' · 已停用'}`,
      value: node.id,
      orgCode: node.orgCode,
      orgName: node.orgName,
      disabled: node.status !== 'enabled' && !selectedOrgIds.value.includes(node.id),
    },
    ...flattenOrgOptions(node.children ?? [], level + 1),
  ]);
}

/**
 * 获取角色数据权限中文说明。
 *
 * @param dataScope 数据权限范围编码
 * @returns 中文说明
 */
function dataScopeLabel(dataScope?: string): string {
  const dataScopeMap: Record<string, string> = {
    ALL: '全部数据',
    CURRENT_DEPT_AND_CHILDREN: '当前部门及下级',
    CURRENT_DEPT: '当前部门',
    SELF: '仅本人',
    CUSTOM: '自定义',
  };
  return dataScope ? dataScopeMap[dataScope] ?? dataScope : '未设置';
}

onMounted(() => {
  void loadRoles();
});
</script>

<style scoped>
.role-page {
  min-width: 0;
}

.tenant-id {
  width: 180px;
}

.state-alert {
  margin-bottom: 12px;
}

.data-scope-panel {
  min-height: 220px;
}

.scope-summary {
  display: grid;
  grid-template-columns: 88px 1fr;
  gap: 8px 12px;
  padding: 16px;
  border: 1px solid #eef2f7;
  border-radius: 6px;
  background: #f8fafc;
}

.scope-summary span {
  color: #64748b;
}

.scope-summary strong {
  color: #0f172a;
}

.selection-preview {
  min-height: 92px;
  padding: 12px;
  margin-top: 8px;
  border: 1px solid #eef2f7;
  border-radius: 6px;
}

.preview-label {
  display: block;
  margin-bottom: 10px;
  color: #475569;
  font-weight: 600;
}

.org-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.data-scope-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.permission-code {
  display: none;
}
</style>
