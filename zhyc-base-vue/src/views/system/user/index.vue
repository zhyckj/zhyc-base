<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="user-page">
    <a-card title="用户管理" :bordered="false">
      <template #extra>
        <a-space>
          <a-input v-model:value="tenantId" class="tenant-id" />
          <a-button :loading="status === 'loading'" @click="loadUsers">查询</a-button>
          <a-button v-permission="'system:user:create'" type="primary" @click="openCreateForm">新增用户</a-button>
        </a-space>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="users"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">{{ $statusLabel(record.status) }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button v-permission="'system:user:update'" size="small" @click="openEditForm(record)">编辑</a-button>
              <a-button
                v-permission="'system:user:update-status'"
                size="small"
                @click="changeStatus(record)"
              >
                {{ record.status === 'enabled' ? '停用' : '启用' }}
              </a-button>
              <a-button v-permission="'system:user:reset-password'" size="small" @click="openPasswordForm(record)">
                重置密码
              </a-button>
              <a-button v-permission="'system:user:delete'" size="small" danger @click="removeUser(record)">
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <span class="permission-code">system:user:query system:user:edit</span>
    </a-card>

    <a-modal
      v-model:open="formOpen"
      :title="editingUser ? '编辑用户' : '新增用户'"
      :width="760"
      :confirm-loading="saving"
      @ok="submitForm"
    >
      <a-form layout="vertical" :model="formState">
        <a-tabs v-model:activeKey="activeFormTab">
          <a-tab-pane key="basic" tab="基础信息">
            <a-form-item label="登录账号" required>
              <a-input v-model:value="formState.username" :disabled="Boolean(editingUser)" />
            </a-form-item>
            <a-form-item label="用户名称" required>
              <a-input v-model:value="formState.nickname" />
            </a-form-item>
            <a-form-item :label="editingUser ? '新密码（为空则不修改）' : '初始密码'" :required="!editingUser">
              <a-input-password v-model:value="formState.password" autocomplete="new-password" />
            </a-form-item>
            <a-form-item label="状态" required>
              <a-select v-model:value="formState.status">
                <a-select-option value="enabled">启用</a-select-option>
                <a-select-option value="disabled">停用</a-select-option>
              </a-select>
            </a-form-item>
          </a-tab-pane>

          <a-tab-pane key="bindings" tab="角色与岗位" :disabled="!editingUser">
            <a-alert
              v-if="!editingUser"
              class="state-alert"
              type="info"
              show-icon
              message="新增用户保存后，可重新编辑该用户并绑定角色、岗位。"
            />

            <div v-else v-permission="'system:user:edit'" class="binding-panel">
              <a-alert
                v-if="bindingErrorMessage"
                class="state-alert"
                type="warning"
                show-icon
                :message="bindingErrorMessage"
              />
              <a-spin :spinning="bindingLoading">
                <a-row :gutter="16">
                  <a-col :xs="24" :md="12">
                    <a-form-item label="用户角色">
                      <a-select
                        v-model:value="selectedRoleIds"
                        mode="multiple"
                        allow-clear
                        show-search
                        option-filter-prop="label"
                        placeholder="请选择角色"
                        :options="roleOptions"
                        :disabled="!canEditUserBindings"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12">
                    <a-form-item label="用户岗位">
                      <a-select
                        v-model:value="selectedPostIds"
                        mode="multiple"
                        allow-clear
                        show-search
                        option-filter-prop="label"
                        placeholder="请选择岗位"
                        :options="postOptions"
                        :disabled="!canEditUserBindings"
                        @change="syncPrimaryPostSelection"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12">
                    <a-form-item label="主岗位">
                      <a-select
                        v-model:value="primaryPostId"
                        allow-clear
                        placeholder="请选择主岗位"
                        :options="primaryPostOptions"
                        :disabled="!canEditUserBindings || selectedPostIds.length === 0"
                      />
                    </a-form-item>
                  </a-col>
                </a-row>
                <div class="binding-summary">
                  已选 {{ selectedRoleIds.length }} 个角色、{{ selectedPostIds.length }} 个岗位。
                  <span v-if="selectedPostIds.length > 0">主岗位：{{ primaryPostName }}</span>
                </div>
              </a-spin>
              <span class="permission-code">system:user:edit</span>
            </div>
          </a-tab-pane>
        </a-tabs>
      </a-form>
    </a-modal>

    <a-modal v-model:open="passwordOpen" title="重置密码" :confirm-loading="saving" @ok="submitPassword">
      <a-form layout="vertical">
        <a-form-item label="用户">
          <a-input :value="passwordUser ? `${passwordUser.nickname}（${passwordUser.username}）` : ''" disabled />
        </a-form-item>
        <a-form-item label="新密码" required>
          <a-input-password v-model:value="newPassword" autocomplete="new-password" />
        </a-form-item>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { Modal, message } from 'ant-design-vue';
import { computed, onMounted, reactive, ref } from 'vue';

import { listSystemPosts, type SystemPost } from '@/api/system/post';
import { listSystemRoles, type SystemRole } from '@/api/system/role';
import {
  createSystemUser,
  deleteSystemUser,
  listSystemUsers,
  resetSystemUserPassword,
  updateSystemUser,
  updateSystemUserStatus,
  type SystemUser,
  type SystemUserSavePayload,
} from '@/api/system/user';
import { bindSystemUserPosts, listSystemUserPosts } from '@/api/system/user-post';
import { bindSystemUserRoles, listSystemUserRoles } from '@/api/system/user-role';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';
import { usePermission } from '@/utils/permission';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 异常提示文案。 */
const errorMessage = ref('');
/** 系统用户列表。 */
const users = ref<SystemUser[]>([]);
/** 保存状态。 */
const saving = ref(false);
/** 用户表单打开状态。 */
const formOpen = ref(false);
/** 重置密码弹窗打开状态。 */
const passwordOpen = ref(false);
/** 当前编辑用户。 */
const editingUser = ref<SystemUser>();
/** 当前重置密码用户。 */
const passwordUser = ref<SystemUser>();
/** 新密码。 */
const newPassword = ref('');
/** 用户表单当前页签。 */
const activeFormTab = ref('basic');
/** 角色与岗位候选项加载状态。 */
const bindingLoading = ref(false);
/** 角色与岗位候选项加载错误提示。 */
const bindingErrorMessage = ref('');
/** 当前用户角色岗位绑定是否已加载完成，避免加载失败时误清空旧绑定。 */
const bindingsLoaded = ref(false);
/** 角色候选项。 */
const allRoles = ref<SystemRole[]>([]);
/** 岗位候选项。 */
const allPosts = ref<SystemPost[]>([]);
/** 当前用户已选择角色主键。 */
const selectedRoleIds = ref<number[]>([]);
/** 当前用户已选择岗位主键。 */
const selectedPostIds = ref<number[]>([]);
/** 当前用户主岗位主键。 */
const primaryPostId = ref<number>();
/** 页面权限判断工具。 */
const { hasPermission } = usePermission();

/** 用户表单数据。 */
const formState = reactive<SystemUserSavePayload>({
  tenantId: tenantId.value,
  username: '',
  nickname: '',
  password: '',
  status: 'enabled',
});

/** 表格列定义。 */
const columns = [
  { title: '登录账号', dataIndex: 'username', key: 'username', width: 180 },
  { title: '用户名称', dataIndex: 'nickname', key: 'nickname' },
  { title: '租户编码', dataIndex: 'tenantId', key: 'tenantId', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 260 },
];

/** 当前账号是否可以维护用户岗位和角色绑定。 */
const canEditUserBindings = computed(() => Boolean(editingUser.value) && hasPermission('system:user:edit'));

/** 角色下拉选项。 */
const roleOptions = computed(() =>
  allRoles.value.map((role) => ({
    label: `${role.name}（${role.roleCode}）${role.status === 'enabled' ? '' : ' · 已停用'}`,
    value: role.id,
    disabled: role.status !== 'enabled' && !selectedRoleIds.value.includes(role.id),
  })),
);

/** 岗位下拉选项。 */
const postOptions = computed(() =>
  allPosts.value.map((post) => ({
    label: `${post.postName}（${post.postCode}）${post.status === 'enabled' ? '' : ' · 已停用'}`,
    value: post.id,
    disabled: post.status !== 'enabled' && !selectedPostIds.value.includes(post.id),
  })),
);

/** 主岗位候选项，仅允许从已选择岗位中指定。 */
const primaryPostOptions = computed(() =>
  allPosts.value
    .filter((post) => selectedPostIds.value.includes(post.id))
    .map((post) => ({
      label: `${post.postName}（${post.postCode}）`,
      value: post.id,
    })),
);

/** 当前主岗位展示名称。 */
const primaryPostName = computed(() => {
  const currentPost = allPosts.value.find((post) => post.id === primaryPostId.value);
  return currentPost ? `${currentPost.postName}（${currentPost.postCode}）` : '未设置';
});

/**
 * 加载系统用户列表。
 */
async function loadUsers(): Promise<void> {
  status.value = 'loading';
  try {
    users.value = await listSystemUsers(syncTenantContext());
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '用户加载失败';
    status.value = 'error';
  }
}

/**
 * 同步后台租户到用户查询条件。
 *
 * @returns 当前租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId();
  tenantId.value = currentTenantId;
  return currentTenantId;
}

/**
 * 打开新增用户表单。
 */
function openCreateForm(): void {
  editingUser.value = undefined;
  activeFormTab.value = 'basic';
  resetBindingState();
  Object.assign(formState, {
    tenantId: syncTenantContext(),
    username: '',
    nickname: '',
    password: '',
    status: 'enabled',
  });
  formOpen.value = true;
}

/**
 * 打开编辑用户表单。
 *
 * @param user 系统用户
 */
async function openEditForm(user: SystemUser): Promise<void> {
  editingUser.value = user;
  activeFormTab.value = 'basic';
  resetBindingState();
  Object.assign(formState, {
    tenantId: syncTenantContext(),
    username: user.username,
    nickname: user.nickname,
    password: '',
    status: user.status,
  });
  formOpen.value = true;
  if (canEditUserBindings.value) {
    await loadUserBindings(user.id);
  }
}

/**
 * 提交用户保存表单。
 */
async function submitForm(): Promise<void> {
  if (!validateUserForm()) {
    return;
  }
  if (editingUser.value && canEditUserBindings.value && bindingLoading.value) {
    message.warning('角色与岗位正在加载，请稍后保存');
    return;
  }
  if (editingUser.value && canEditUserBindings.value && bindingsLoaded.value && !validateUserBindings()) {
    return;
  }

  saving.value = true;
  try {
    const payload = { ...formState, tenantId: syncTenantContext() };
    if (editingUser.value) {
      await updateSystemUser(editingUser.value.id, payload);
      if (canEditUserBindings.value && bindingsLoaded.value) {
        await submitUserBindings(editingUser.value.id);
      }
    } else {
      await createSystemUser(payload);
    }
    message.success(editingUser.value && canEditUserBindings.value && bindingsLoaded.value
      ? '用户及角色岗位已保存'
      : '用户已保存');
    formOpen.value = false;
    await loadUsers();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '用户保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 校验用户基础表单。
 *
 * @returns 校验通过时返回 true
 */
function validateUserForm(): boolean {
  if (!formState.username.trim()) {
    activeFormTab.value = 'basic';
    message.warning('请输入登录账号');
    return false;
  }
  if (!formState.nickname.trim()) {
    activeFormTab.value = 'basic';
    message.warning('请输入用户名称');
    return false;
  }
  if (!editingUser.value && !formState.password?.trim()) {
    activeFormTab.value = 'basic';
    message.warning('请输入初始密码');
    return false;
  }
  return true;
}

/**
 * 校验用户角色和岗位绑定表单。
 *
 * @returns 校验通过时返回 true
 */
function validateUserBindings(): boolean {
  if (selectedPostIds.value.length > 0 && !primaryPostId.value) {
    activeFormTab.value = 'bindings';
    message.warning('请选择主岗位');
    return false;
  }
  if (primaryPostId.value && !selectedPostIds.value.includes(primaryPostId.value)) {
    activeFormTab.value = 'bindings';
    message.warning('主岗位必须在已选择岗位中');
    return false;
  }
  return true;
}

/**
 * 加载当前编辑用户的岗位、角色和候选项。
 *
 * @param userId 用户主键
 */
async function loadUserBindings(userId: number): Promise<void> {
  bindingLoading.value = true;
  bindingErrorMessage.value = '';
  bindingsLoaded.value = false;
  try {
    const currentTenantId = syncTenantContext();
    const [loadedRoles, loadedPosts, boundRoles, boundPosts] = await Promise.all([
      listSystemRoles(currentTenantId),
      listSystemPosts(currentTenantId),
      listSystemUserRoles(currentTenantId, userId),
      listSystemUserPosts(currentTenantId, userId),
    ]);
    allRoles.value = loadedRoles;
    allPosts.value = loadedPosts;
    selectedRoleIds.value = boundRoles.map((role) => role.roleId);
    selectedPostIds.value = boundPosts.map((post) => post.postId);
    primaryPostId.value = boundPosts.find((post) => post.primaryFlag)?.postId ?? selectedPostIds.value[0];
    bindingsLoaded.value = true;
  } catch (error) {
    resetBindingSelection();
    bindingErrorMessage.value = error instanceof Error ? error.message : '用户角色岗位加载失败';
  } finally {
    bindingLoading.value = false;
  }
}

/**
 * 保存当前编辑用户的岗位和角色绑定。
 *
 * @param userId 用户主键
 */
async function submitUserBindings(userId: number): Promise<void> {
  const currentTenantId = syncTenantContext();
  await Promise.all([
    bindSystemUserRoles(currentTenantId, userId, {
      roleIds: selectedRoleIds.value,
    }),
    bindSystemUserPosts(currentTenantId, userId, {
      posts: selectedPostIds.value.map((postId) => ({
        postId,
        primaryFlag: primaryPostId.value === postId,
      })),
    }),
  ]);
}

/**
 * 同步主岗位选择，避免主岗位不在已选岗位范围内。
 */
function syncPrimaryPostSelection(): void {
  if (selectedPostIds.value.length === 0) {
    primaryPostId.value = undefined;
    return;
  }
  if (!primaryPostId.value || !selectedPostIds.value.includes(primaryPostId.value)) {
    primaryPostId.value = selectedPostIds.value[0];
  }
}

/**
 * 清理用户角色岗位加载状态。
 */
function resetBindingState(): void {
  bindingErrorMessage.value = '';
  bindingLoading.value = false;
  bindingsLoaded.value = false;
  allRoles.value = [];
  allPosts.value = [];
  resetBindingSelection();
}

/**
 * 清理用户角色岗位选择值。
 */
function resetBindingSelection(): void {
  selectedRoleIds.value = [];
  selectedPostIds.value = [];
  primaryPostId.value = undefined;
}

/**
 * 修改用户状态。
 *
 * @param user 系统用户
 */
function changeStatus(user: SystemUser): void {
  const nextStatus = user.status === 'enabled' ? 'disabled' : 'enabled';
  Modal.confirm({
    title: nextStatus === 'enabled' ? '确认启用用户' : '确认停用用户',
    content: `用户：${user.nickname}（${user.username}）`,
    async onOk() {
      await updateSystemUserStatus(user.id, syncTenantContext(), nextStatus);
      message.success('用户状态已更新');
      await loadUsers();
    },
  });
}

/**
 * 打开重置密码表单。
 *
 * @param user 系统用户
 */
function openPasswordForm(user: SystemUser): void {
  passwordUser.value = user;
  newPassword.value = '';
  passwordOpen.value = true;
}

/**
 * 提交重置密码。
 */
async function submitPassword(): Promise<void> {
  if (!passwordUser.value) {
    return;
  }
  saving.value = true;
  try {
    await resetSystemUserPassword(passwordUser.value.id, syncTenantContext(), newPassword.value);
    message.success('用户密码已重置');
    passwordOpen.value = false;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '重置密码失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 删除系统用户。
 *
 * @param user 系统用户
 */
function removeUser(user: SystemUser): void {
  Modal.confirm({
    title: '确认删除用户',
    content: `用户：${user.nickname}（${user.username}）`,
    okButtonProps: { danger: true },
    async onOk() {
      await deleteSystemUser(user.id, syncTenantContext());
      message.success('用户已删除');
      await loadUsers();
    },
  });
}

onMounted(() => {
  void loadUsers();
});
</script>

<style scoped>
.user-page {
  min-width: 0;
}

.tenant-id {
  width: 180px;
}

.state-alert {
  margin-bottom: 12px;
}

.binding-panel {
  min-height: 210px;
}

.binding-summary {
  color: #5f6f89;
  font-size: 13px;
  line-height: 22px;
}

.permission-code {
  display: none;
}
</style>
