<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="system-secret-page">
    <a-card title="密钥管理" :bordered="false">
      <template #extra>
        <a-space>
          <a-input v-model:value="tenantId" class="tenant-id" />
          <a-button :loading="loading" @click="loadSecrets">查询</a-button>
          <a-button v-permission="permissionCodes.create" type="primary" @click="openCreateForm">新增密钥</a-button>
        </a-space>
      </template>

      <a-alert
        message="密钥列表不展示明文或掩码。密钥引用统一使用 secret:<secretCode>。"
        type="info"
        show-icon
        class="state-alert"
      />

      <a-table
        row-key="id"
        size="small"
        :columns="columns"
        :data-source="secrets"
        :loading="loading"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'secretKind'">
            <a-tag color="blue">{{ formatSecretKindLabel(record.secretKind) }}</a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
              {{ $statusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'expireAt'">
            {{ formatDateTime(record.expireAt) }}
          </template>
          <template v-if="column.key === 'lastRotatedAt'">
            {{ formatDateTime(record.lastRotatedAt) }}
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button v-permission="permissionCodes.update" size="small" @click="openEditForm(record)">编辑</a-button>
              <a-button v-permission="permissionCodes.rotate" size="small" @click="openRotateForm(record)">
                轮换
              </a-button>
              <a-button
                v-if="record.status === 'disabled'"
                v-permission="permissionCodes.enable"
                size="small"
                @click="changeSecretStatus(record, 'enabled')"
              >
                启用
              </a-button>
              <a-button
                v-else
                v-permission="permissionCodes.disable"
                size="small"
                @click="changeSecretStatus(record, 'disabled')"
              >
                停用
              </a-button>
              <a-button v-permission="permissionCodes.copyRef" size="small" @click="copySecretRef(record)">
                复制引用
              </a-button>
              <a-button v-permission="permissionCodes.delete" size="small" danger @click="removeSecret(record)">
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <span class="permission-code">
        system:secret:query system:secret:create system:secret:update system:secret:delete system:secret:enable
        system:secret:disable system:secret:rotate system:secret:copy-ref
      </span>
    </a-card>

    <a-modal
      v-model:open="formVisible"
      :title="editingSecret ? '编辑密钥' : '新增密钥'"
      :confirm-loading="saving"
      @ok="submitSecretForm"
      @cancel="closeSecretForm"
    >
      <a-form layout="vertical" :model="formState">
        <a-form-item label="密钥编码" required>
          <a-input v-model:value="formState.secretCode" :disabled="Boolean(editingSecret)" placeholder="lowcode/main/password" />
        </a-form-item>
        <a-form-item label="密钥名称" required>
          <a-input v-model:value="formState.secretName" placeholder="数据库口令" />
        </a-form-item>
        <a-form-item label="密钥类型" required>
          <a-select v-model:value="formState.secretKind" :options="secretKindOptions" />
        </a-form-item>
        <a-form-item :label="editingSecret ? '密钥明文（留空表示不修改）' : '密钥明文'" :required="!editingSecret">
          <a-input-password v-model:value="formState.secretPlaintext" autocomplete="new-password" placeholder="请输入密钥明文" />
        </a-form-item>
        <a-form-item label="状态" required>
          <a-select v-model:value="formState.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="到期时间">
          <a-date-picker
            v-model:value="formState.expireAt"
            show-time
            allow-clear
            value-format="YYYY-MM-DDTHH:mm:ss"
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择到期时间"
            class="full-width"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="rotateVisible"
      title="轮换密钥"
      :confirm-loading="rotating"
      @ok="submitRotateForm"
      @cancel="closeRotateForm"
    >
      <a-form layout="vertical" :model="rotateState">
        <a-form-item label="当前密钥">
          <a-input :value="rotateSecretSummary" disabled />
        </a-form-item>
        <a-form-item label="新密钥明文" required>
          <a-input-password
            v-model:value="rotateState.secretPlaintext"
            autocomplete="new-password"
            placeholder="请输入新的密钥明文"
          />
        </a-form-item>
        <a-form-item label="到期时间">
          <a-date-picker
            v-model:value="rotateState.expireAt"
            show-time
            allow-clear
            value-format="YYYY-MM-DDTHH:mm:ss"
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择到期时间"
            class="full-width"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { Modal, message } from 'ant-design-vue';
import { computed, onMounted, reactive, ref } from 'vue';

import {
  buildSystemSecretRef,
  changeSystemSecretStatus,
  createSystemSecret,
  deleteSystemSecret,
  listSystemSecrets,
  rotateSystemSecret,
  updateSystemSecret,
  type SystemSecretCreateRequest,
  type SystemSecretKind,
  type SystemSecretResponse,
  type SystemSecretRotateRequest,
  type SystemSecretStatus,
  type SystemSecretUpdateRequest,
} from '@/api/system/secret';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 页面加载状态。 */
const loading = ref(false);
/** 保存状态。 */
const saving = ref(false);
/** 轮换状态。 */
const rotating = ref(false);
/** 密钥列表。 */
const secrets = ref<SystemSecretResponse[]>([]);
/** 新增或编辑弹窗状态。 */
const formVisible = ref(false);
/** 轮换弹窗状态。 */
const rotateVisible = ref(false);
/** 当前编辑密钥。 */
const editingSecret = ref<SystemSecretResponse>();
/** 当前轮换密钥。 */
const rotatingSecret = ref<SystemSecretResponse>();

/** 密钥中心按钮权限编码。 */
const permissionCodes = {
  /** 查询权限。 */
  query: 'system:secret:query',
  /** 新增权限。 */
  create: 'system:secret:create',
  /** 编辑权限。 */
  update: 'system:secret:update',
  /** 删除权限。 */
  delete: 'system:secret:delete',
  /** 启用权限。 */
  enable: 'system:secret:enable',
  /** 停用权限。 */
  disable: 'system:secret:disable',
  /** 轮换权限。 */
  rotate: 'system:secret:rotate',
  /** 复制引用权限。 */
  copyRef: 'system:secret:copy-ref',
};

/** 密钥状态下拉选项。 */
const statusOptions = [
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' },
];

/** 密钥类型下拉选项。 */
const secretKindOptions = [
  { label: '数据库密码', value: 'db_password' },
  { label: 'API 密钥', value: 'api_secret' },
  { label: 'OAuth2 客户端密钥', value: 'oauth_client_secret' },
  { label: 'JWK 私钥', value: 'jwk_private_key' },
  { label: '通用密钥', value: 'generic' },
];

/** 密钥列表列定义。 */
const columns = [
  { title: '密钥编码', dataIndex: 'secretCode', key: 'secretCode', width: 220 },
  { title: '密钥名称', dataIndex: 'secretName', key: 'secretName', width: 180 },
  { title: '密钥类型', dataIndex: 'secretKind', key: 'secretKind', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '到期时间', dataIndex: 'expireAt', key: 'expireAt', width: 180 },
  { title: '最近轮换时间', dataIndex: 'lastRotatedAt', key: 'lastRotatedAt', width: 180 },
  { title: '操作', key: 'action', width: 320 },
];

/** 密钥表单状态。 */
const formState = reactive(createEmptyFormState());
/** 轮换表单状态。 */
const rotateState = reactive(createEmptyRotateState());
/** 密钥轮换摘要。 */
const rotateSecretSummary = computed(() =>
  rotatingSecret.value ? `${rotatingSecret.value.secretName}（${buildSystemSecretRef(rotatingSecret.value.secretCode)}）` : '',
);

/**
 * 创建空密钥表单。
 *
 * @returns 密钥表单状态
 */
function createEmptyFormState(): SecretFormState {
  return {
    tenantId: tenantId.value,
    secretCode: '',
    secretName: '',
    secretKind: 'generic',
    secretPlaintext: '',
    status: 'enabled',
    expireAt: '',
  };
}

/**
 * 创建空轮换表单。
 *
 * @returns 轮换表单状态
 */
function createEmptyRotateState(): SecretRotateState {
  return {
    tenantId: tenantId.value,
    secretPlaintext: '',
    expireAt: '',
  };
}

/**
 * 加载密钥列表。
 */
async function loadSecrets(): Promise<void> {
  loading.value = true;
  try {
    const currentTenantId = syncTenantContext();
    secrets.value = await listSystemSecrets(currentTenantId);
  } catch (error) {
    message.error(error instanceof Error ? error.message : '密钥列表加载失败');
  } finally {
    loading.value = false;
  }
}

/**
 * 打开新增密钥弹窗。
 */
function openCreateForm(): void {
  editingSecret.value = undefined;
  syncTenantContext();
  Object.assign(formState, createEmptyFormState());
  formVisible.value = true;
}

/**
 * 打开编辑密钥弹窗。
 *
 * @param secret 系统密钥
 */
function openEditForm(secret: SystemSecretResponse): void {
  editingSecret.value = secret;
  Object.assign(formState, {
    tenantId: syncTenantContext(),
    secretCode: secret.secretCode,
    secretName: secret.secretName,
    secretKind: secret.secretKind,
    secretPlaintext: '',
    status: secret.status,
    expireAt: secret.expireAt ?? '',
  });
  formVisible.value = true;
}

/**
 * 关闭密钥表单并清理明文输入。
 */
function closeSecretForm(): void {
  formVisible.value = false;
  editingSecret.value = undefined;
  Object.assign(formState, createEmptyFormState());
}

/**
 * 提交密钥创建或编辑。
 */
async function submitSecretForm(): Promise<void> {
  const normalizedName = formState.secretName.trim();
  const normalizedPlaintext = formState.secretPlaintext.trim();
  const normalizedExpireAt = normalizeOptionalText(formState.expireAt);
  if (!formState.secretCode.trim()) {
    message.error('请输入密钥编码');
    return;
  }
  if (!normalizedName) {
    message.error('请输入密钥名称');
    return;
  }
  if (!formState.secretKind) {
    message.error('请选择密钥类型');
    return;
  }
  if (!editingSecret.value && !normalizedPlaintext) {
    message.error('请输入密钥明文');
    return;
  }
  saving.value = true;
  try {
    const currentTenantId = syncTenantContext();
    const payloadBase = {
      tenantId: currentTenantId,
      secretName: normalizedName,
      secretKind: formState.secretKind as SystemSecretKind,
      status: formState.status as SystemSecretStatus,
      expireAt: normalizedExpireAt,
    };
    if (editingSecret.value) {
      const payload: SystemSecretUpdateRequest = {
        ...payloadBase,
        secretCode: formState.secretCode.trim(),
        ...(normalizedPlaintext ? { secretPlaintext: normalizedPlaintext } : {}),
      };
      await updateSystemSecret(editingSecret.value.id, payload);
      message.success('密钥已更新');
    } else {
      const payload: SystemSecretCreateRequest = {
        ...payloadBase,
        secretCode: formState.secretCode.trim(),
        secretPlaintext: normalizedPlaintext,
      };
      await createSystemSecret(payload);
      message.success('密钥已创建');
    }
    closeSecretForm();
    await loadSecrets();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '密钥保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 打开轮换弹窗。
 *
 * @param secret 系统密钥
 */
function openRotateForm(secret: SystemSecretResponse): void {
  rotatingSecret.value = secret;
  Object.assign(rotateState, {
    tenantId: syncTenantContext(),
    secretPlaintext: '',
    expireAt: secret.expireAt ?? '',
  });
  rotateVisible.value = true;
}

/**
 * 关闭轮换弹窗并清理明文输入。
 */
function closeRotateForm(): void {
  rotateVisible.value = false;
  rotatingSecret.value = undefined;
  Object.assign(rotateState, createEmptyRotateState());
}

/**
 * 提交密钥轮换。
 */
async function submitRotateForm(): Promise<void> {
  const normalizedPlaintext = rotateState.secretPlaintext.trim();
  const normalizedExpireAt = normalizeOptionalText(rotateState.expireAt);
  if (!rotatingSecret.value) {
    message.error('请先选择密钥');
    return;
  }
  if (!normalizedPlaintext) {
    message.error('请输入新的密钥明文');
    return;
  }
  rotating.value = true;
  try {
    const payload: SystemSecretRotateRequest = {
      tenantId: syncTenantContext(),
      secretPlaintext: normalizedPlaintext,
      expireAt: normalizedExpireAt,
    };
    await rotateSystemSecret(rotatingSecret.value.id, payload);
    message.success('密钥已轮换');
    closeRotateForm();
    await loadSecrets();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '密钥轮换失败');
  } finally {
    rotating.value = false;
  }
}

/**
 * 修改密钥状态。
 *
 * @param secret 系统密钥
 * @param status 目标状态
 */
function changeSecretStatus(secret: SystemSecretResponse, status: SystemSecretStatus): void {
  const nextAction = status === 'enabled' ? '启用' : '停用';
  Modal.confirm({
    title: `确认${nextAction}密钥`,
    content: `确定要${nextAction}密钥「${secret.secretName}」吗？`,
    okText: nextAction,
    cancelText: '取消',
    async onOk() {
      await changeSystemSecretStatus(secret.id, {
        tenantId: syncTenantContext(),
        status,
      });
      message.success(`密钥已${nextAction}`);
      await loadSecrets();
    },
  });
}

/**
 * 删除密钥。
 *
 * @param secret 系统密钥
 */
function removeSecret(secret: SystemSecretResponse): void {
  Modal.confirm({
    title: '确认删除密钥',
    content: `确定要删除密钥「${secret.secretName}」吗？删除后引用该密钥的配置将无法继续使用。`,
    okText: '删除',
    okButtonProps: { danger: true },
    cancelText: '取消',
    async onOk() {
      await deleteSystemSecret(secret.id, syncTenantContext());
      message.success('密钥已删除');
      await loadSecrets();
    },
  });
}

/**
 * 复制密钥引用。
 *
 * @param secret 系统密钥
 */
async function copySecretRef(secret: SystemSecretResponse): Promise<void> {
  const secretRef = buildSystemSecretRef(secret.secretCode);
  try {
    await navigator.clipboard.writeText(secretRef);
    message.success('密钥引用已复制');
  } catch (error) {
    message.error(error instanceof Error ? error.message : '密钥引用复制失败');
  }
}

/**
 * 格式化密钥类型标签。
 *
 * @param secretKind 密钥类型
 * @returns 中文展示名称
 */
function formatSecretKindLabel(secretKind: SystemSecretKind): string {
  return secretKindOptions.find((option) => option.value === secretKind)?.label ?? secretKind;
}

/**
 * 格式化时间字段。
 *
 * @param value 时间字符串
 * @returns 可读时间或空白占位
 */
function formatDateTime(value?: string): string {
  if (!value) {
    return '-';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  }).format(date);
}

/**
 * 同步后台租户上下文。
 *
 * @returns 当前租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId();
  tenantId.value = currentTenantId;
  return currentTenantId;
}

/**
 * 归一化可选文本。
 *
 * @param value 原始文本
 * @returns 去除首尾空白后的文本；空值返回空字符串
 */
function normalizeOptionalText(value?: string | null): string | undefined {
  const normalizedValue = value?.trim() ?? '';
  return normalizedValue || undefined;
}

/**
 * 密钥表单状态。
 */
interface SecretFormState {
  /** 租户业务编码。 */
  tenantId: string;
  /** 密钥编码。 */
  secretCode: string;
  /** 密钥名称。 */
  secretName: string;
  /** 密钥类型。 */
  secretKind: SystemSecretKind;
  /** 密钥明文。 */
  secretPlaintext: string;
  /** 密钥状态。 */
  status: SystemSecretStatus;
  /** 到期时间。 */
  expireAt?: string | null;
}

/**
 * 密钥轮换状态。
 */
interface SecretRotateState {
  /** 租户业务编码。 */
  tenantId: string;
  /** 新密钥明文。 */
  secretPlaintext: string;
  /** 到期时间。 */
  expireAt?: string | null;
}

onMounted(() => {
  void loadSecrets();
});
</script>

<style scoped>
.system-secret-page {
  min-width: 0;
}

.tenant-id {
  width: 180px;
}

.state-alert {
  margin-bottom: 12px;
}

.full-width {
  width: 100%;
}

.permission-code {
  display: none;
}
</style>
