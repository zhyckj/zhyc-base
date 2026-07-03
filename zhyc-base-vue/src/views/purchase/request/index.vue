<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="purchase-page">
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="9">
        <a-card title="创建采购申请" :bordered="false">
          <a-form layout="vertical" @submit.prevent>
            <a-form-item label="采购申请单号" required>
              <a-input v-model:value="form.requestNo" placeholder="例如 PR202606240001" />
            </a-form-item>
            <a-form-item label="申请标题" required>
              <a-input v-model:value="form.requestTitle" placeholder="请输入采购申请标题" />
            </a-form-item>
            <a-form-item label="申请金额" required>
              <a-input-number v-model:value="form.totalAmount" :min="0" :precision="2" class="amount-input" />
            </a-form-item>
            <a-form-item label="申请原因">
              <a-textarea v-model:value="form.requestReason" placeholder="请输入采购申请原因" />
            </a-form-item>
            <a-space>
              <a-button type="primary" :loading="submitting" @click="createRequest">创建</a-button>
              <a-button :disabled="activeRequestNo === ''" :loading="submitting" @click="submitRequest">
                提交审批
              </a-button>
            </a-space>
          </a-form>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="15">
        <a-card title="申请状态" :bordered="false">
          <a-descriptions :column="1" bordered size="small">
            <a-descriptions-item label="申请单号">{{ createdRequestNo || '-' }}</a-descriptions-item>
            <a-descriptions-item label="当前选中">{{ activeRequestNo || '-' }}</a-descriptions-item>
            <a-descriptions-item label="流程实例">{{ submitResult?.processInstanceId ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="流程状态">{{ submitResult?.processStatus ?? '-' }}</a-descriptions-item>
          </a-descriptions>
          <a-alert
            v-if="message"
            class="state-alert"
            type="success"
            show-icon
            :message="message"
          />
        </a-card>
      </a-col>
    </a-row>
    <a-card class="request-list-card" title="采购申请列表" :bordered="false">
      <a-space class="filter-bar">
        <a-select
          v-model:value="query.processStatus"
          allow-clear
          placeholder="流程状态"
          class="status-select"
          @change="loadRequests"
        >
          <a-select-option value="DRAFT">草稿</a-select-option>
          <a-select-option value="APPROVING">审批中</a-select-option>
          <a-select-option value="APPROVED">已通过</a-select-option>
          <a-select-option value="REJECTED">已拒绝</a-select-option>
        </a-select>
        <a-button :loading="submitting" @click="loadRequests">刷新列表</a-button>
      </a-space>
      <a-table
        row-key="requestNo"
        :columns="requestColumns"
        :data-source="requestPage.records"
        :custom-row="customRequestRow"
        :pagination="pagination"
        :loading="submitting"
        size="small"
        @change="handleTableChange"
      />
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';

import {
  createPurchaseRequest,
  listPurchaseRequests,
  submitPurchaseRequest,
  type PurRequestCreateCommand,
  type PurRequestListQuery,
  type PurRequestPageResponse,
  type PurRequestStatusResponse,
  type PurRequestSubmitResponse,
} from '@/api/purchase/request';
import { getAdminRuntimeContext, requireAdminOrgId, requireAdminTenantId, requireAdminUserId } from '@/utils/adminContext';

const adminContext = getAdminRuntimeContext();

/** 采购申请表单。 */
const form = reactive<PurRequestCreateCommand>({
  tenantId: adminContext.tenantId,
  requestNo: '',
  requestTitle: '',
  applicantId: adminContext.userId ?? 0,
  orgId: adminContext.orgId ?? Number.NaN,
  totalAmount: 0,
  requestReason: '',
});

/** 采购申请列表查询条件。 */
const query = reactive<PurRequestListQuery>({
  tenantId: adminContext.tenantId,
  pageNo: 1,
  pageSize: 10,
});

/** 最近创建的采购申请单号。 */
const createdRequestNo = ref('');
/** 当前选中的采购申请单号。 */
const activeRequestNo = ref('');
/** 提交审批返回结果。 */
const submitResult = ref<PurRequestSubmitResponse>();
/** 采购申请分页数据。 */
const requestPage = ref<PurRequestPageResponse>({ total: 0, pageNo: 1, pageSize: 10, records: [] });
/** 提交中状态。 */
const submitting = ref(false);
/** 操作提示。 */
const message = ref('');
/** 采购申请表格分页配置。 */
const pagination = computed(() => ({
  current: requestPage.value.pageNo,
  pageSize: requestPage.value.pageSize,
  total: requestPage.value.total,
  showSizeChanger: true,
}));

/** 采购申请列表表格列。 */
const requestColumns = [
  { title: '申请单号', dataIndex: 'requestNo', key: 'requestNo' },
  { title: '申请标题', dataIndex: 'requestTitle', key: 'requestTitle' },
  { title: '申请金额', dataIndex: 'totalAmount', key: 'totalAmount' },
  { title: '流程状态', dataIndex: 'processStatus', key: 'processStatus' },
  { title: '提交时间', dataIndex: 'submittedAt', key: 'submittedAt' },
];

/**
 * 创建采购申请。
 */
async function createRequest(): Promise<void> {
  submitting.value = true;
  try {
    syncAdminContext();
    createdRequestNo.value = await createPurchaseRequest(form);
    activeRequestNo.value = createdRequestNo.value;
    await loadRequests();
    message.value = '采购申请已创建，可继续提交审批。';
  } finally {
    submitting.value = false;
  }
}

/**
 * 提交采购申请进入工作流，权限编码 purchase:request:submit。
 */
async function submitRequest(): Promise<void> {
  if (!activeRequestNo.value) {
    return;
  }
  submitting.value = true;
  try {
    syncAdminContext();
    submitResult.value = await submitPurchaseRequest(activeRequestNo.value);
    await loadRequests();
    message.value = '采购申请已提交审批。';
  } finally {
    submitting.value = false;
  }
}

/**
 * 分页查询采购申请，权限编码 purchase:request:view。
 */
async function loadRequests(): Promise<void> {
  submitting.value = true;
  try {
    syncAdminContext();
    requestPage.value = await listPurchaseRequests(query);
  } finally {
    submitting.value = false;
  }
}

/**
 * 表格分页变化处理。
 *
 * @param pageInfo Ant Design Vue 表格分页参数
 */
async function handleTableChange(pageInfo: { current?: number; pageSize?: number }): Promise<void> {
  query.pageNo = pageInfo.current ?? 1;
  query.pageSize = pageInfo.pageSize ?? 10;
  await loadRequests();
}

/**
 * 创建申请表格行事件配置。
 *
 * @param record 当前行采购申请
 * @return 表格行事件配置
 */
function customRequestRow(record: PurRequestStatusResponse): { onClick: () => void } {
  return {
    onClick: () => {
      activeRequestNo.value = record.requestNo;
      submitResult.value = undefined;
      message.value = `已选择采购申请 ${record.requestNo}。`;
    },
  };
}

/**
 * 同步后台租户和当前用户到采购申请请求参数。
 *
 * @returns 当前登录用户 ID
 */
function syncAdminContext(): number {
  const context = getAdminRuntimeContext();
  const tenantId = requireAdminTenantId(context);
  const currentUserId = requireAdminUserId(context);
  const currentOrgId = requireAdminOrgId(context);
  form.tenantId = tenantId;
  form.applicantId = currentUserId;
  form.orgId = currentOrgId;
  query.tenantId = tenantId;
  return currentUserId;
}
</script>

<style scoped>
.purchase-page {
  min-width: 0;
}

.amount-input {
  width: 100%;
}

.state-alert {
  margin-top: 16px;
}

.request-list-card {
  margin-top: 16px;
}

.filter-bar {
  margin-bottom: 16px;
}

.status-select {
  width: 160px;
}
</style>
