<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="order-page">
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="10">
        <a-card title="创建采购订单" :bordered="false">
          <a-form layout="vertical" @submit.prevent>
            <a-form-item label="采购订单号" required>
              <a-input v-model:value="form.orderNo" placeholder="例如 PO202606240001" />
            </a-form-item>
            <a-form-item label="采购申请单号" required>
              <a-input v-model:value="form.requestNo" placeholder="关联采购申请单号" />
            </a-form-item>
            <a-form-item label="供应商 ID" required>
              <a-input-number v-model:value="form.supplierId" :min="1" class="full-input" />
            </a-form-item>
            <a-form-item label="订单总金额" required>
              <a-input-number v-model:value="form.totalAmount" :min="0" :precision="2" class="full-input" />
            </a-form-item>
            <a-space>
              <a-button type="primary" :loading="loading" @click="createOrder">创建订单</a-button>
              <a-button :disabled="createdOrderNo === ''" :loading="loading" @click="loadOrder">
                查询订单
              </a-button>
            </a-space>
          </a-form>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="14">
        <a-card title="订单详情" :bordered="false">
          <a-descriptions :column="1" bordered size="small">
            <a-descriptions-item label="采购订单号">{{ orderDetail?.orderNo ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="采购申请单号">{{ orderDetail?.requestNo ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="订单状态">{{ orderDetail?.orderStatus ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="订单金额">{{ orderDetail?.totalAmount ?? '-' }}</a-descriptions-item>
          </a-descriptions>
          <a-space class="order-actions">
            <a-button type="primary" :disabled="!canChangeStatus" :loading="loading" @click="confirmOrder">
              确认订单
            </a-button>
            <a-button danger :disabled="!canChangeStatus" :loading="loading" @click="closeOrder">
              关闭订单
            </a-button>
          </a-space>
          <a-table
            class="item-table"
            :columns="itemColumns"
            :data-source="orderDetail?.items ?? []"
            :pagination="$tablePagination"
            size="small"
          />
        </a-card>
      </a-col>
    </a-row>
    <a-card class="order-list-card" title="采购订单列表" :bordered="false">
      <a-space class="filter-bar">
        <a-select
          v-model:value="query.orderStatus"
          allow-clear
          placeholder="订单状态"
          class="status-select"
          @change="loadOrders"
        >
          <a-select-option value="CREATED">新建</a-select-option>
          <a-select-option value="CONFIRMED">已确认</a-select-option>
          <a-select-option value="CLOSED">已关闭</a-select-option>
        </a-select>
        <a-button :loading="loading" @click="loadOrders">刷新列表</a-button>
      </a-space>
      <a-table
        row-key="orderNo"
        :columns="orderColumns"
        :data-source="orderPage.records"
        :custom-row="customOrderRow"
        :pagination="pagination"
        :loading="loading"
        size="small"
        @change="handleTableChange"
      />
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';

import {
  closePurchaseOrder,
  confirmPurchaseOrder,
  createPurchaseOrder,
  getPurchaseOrder,
  listPurchaseOrders,
  type PurOrderCreateCommand,
  type PurOrderListQuery,
  type PurOrderPageResponse,
  type PurOrderResponse,
} from '@/api/purchase/order';
import { getAdminRuntimeContext, requireAdminTenantId, requireAdminUserId } from '@/utils/adminContext';

const adminContext = getAdminRuntimeContext();

/** 采购订单表单，首期默认一条明细便于跑通主链路。 */
const form = reactive<PurOrderCreateCommand>({
  tenantId: adminContext.tenantId,
  orderNo: '',
  requestNo: '',
  supplierId: 1,
  buyerId: adminContext.userId ?? 0,
  totalAmount: 0,
  items: [{ itemName: '办公用品', quantity: 1, unitPrice: 0, amount: 0 }],
});

/** 采购订单列表查询条件。 */
const query = reactive<PurOrderListQuery>({
  tenantId: adminContext.tenantId,
  pageNo: 1,
  pageSize: 10,
});

/** 最近创建的采购订单号。 */
const createdOrderNo = ref('');
/** 当前订单详情。 */
const orderDetail = ref<PurOrderResponse>();
/** 采购订单分页数据。 */
const orderPage = ref<PurOrderPageResponse>({ total: 0, pageNo: 1, pageSize: 10, records: [] });
/** 加载状态。 */
const loading = ref(false);
/** 是否允许执行采购订单状态流转。 */
const canChangeStatus = computed(() => orderDetail.value?.orderStatus === 'CREATED');
/** 采购订单表格分页配置。 */
const pagination = computed(() => ({
  current: orderPage.value.pageNo,
  pageSize: orderPage.value.pageSize,
  total: orderPage.value.total,
  showSizeChanger: true,
}));

/** 采购订单列表表格列。 */
const orderColumns = [
  { title: '采购订单号', dataIndex: 'orderNo', key: 'orderNo' },
  { title: '申请单号', dataIndex: 'requestNo', key: 'requestNo' },
  { title: '供应商 ID', dataIndex: 'supplierId', key: 'supplierId' },
  { title: '采购员 ID', dataIndex: 'buyerId', key: 'buyerId' },
  { title: '订单金额', dataIndex: 'totalAmount', key: 'totalAmount' },
  { title: '状态', dataIndex: 'orderStatus', key: 'orderStatus' },
];

/** 采购订单明细表格列。 */
const itemColumns = [
  { title: '物品名称', dataIndex: 'itemName', key: 'itemName' },
  { title: '数量', dataIndex: 'quantity', key: 'quantity' },
  { title: '单价', dataIndex: 'unitPrice', key: 'unitPrice' },
  { title: '金额', dataIndex: 'amount', key: 'amount' },
];

/**
 * 创建采购订单，权限编码 purchase:order:create。
 */
async function createOrder(): Promise<void> {
  loading.value = true;
  try {
    syncAdminContext();
    form.items[0].unitPrice = form.totalAmount;
    form.items[0].amount = form.totalAmount;
    createdOrderNo.value = await createPurchaseOrder(form);
    await loadOrders();
  } finally {
    loading.value = false;
  }
}

/**
 * 查询采购订单。
 */
async function loadOrder(): Promise<void> {
  loading.value = true;
  try {
    syncAdminContext();
    orderDetail.value = await getPurchaseOrder(createdOrderNo.value || form.orderNo);
  } finally {
    loading.value = false;
  }
}

/**
 * 分页查询采购订单，权限编码 purchase:order:query。
 */
async function loadOrders(): Promise<void> {
  loading.value = true;
  try {
    syncAdminContext();
    orderPage.value = await listPurchaseOrders(query);
  } finally {
    loading.value = false;
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
  await loadOrders();
}

/**
 * 创建订单表格行事件配置。
 *
 * @param record 当前行采购订单
 * @return 表格行事件配置
 */
function customOrderRow(record: PurOrderResponse): { onClick: () => void } {
  return {
    onClick: () => {
      createdOrderNo.value = record.orderNo;
      void loadOrder();
    },
  };
}

/**
 * 确认采购订单，权限编码 purchase:order:confirm。
 */
async function confirmOrder(): Promise<void> {
  if (!orderDetail.value) {
    return;
  }
  loading.value = true;
  try {
    syncAdminContext();
    orderDetail.value = await confirmPurchaseOrder(orderDetail.value.orderNo);
    await loadOrders();
  } finally {
    loading.value = false;
  }
}

/**
 * 关闭采购订单，权限编码 purchase:order:close。
 */
async function closeOrder(): Promise<void> {
  if (!orderDetail.value) {
    return;
  }
  loading.value = true;
  try {
    syncAdminContext();
    orderDetail.value = await closePurchaseOrder(orderDetail.value.orderNo);
    await loadOrders();
  } finally {
    loading.value = false;
  }
}

/**
 * 同步后台租户和当前用户到采购订单请求参数。
 *
 * @returns 当前登录用户 ID
 */
function syncAdminContext(): number {
  const context = getAdminRuntimeContext();
  const tenantId = requireAdminTenantId(context);
  const currentUserId = requireAdminUserId(context);
  form.tenantId = tenantId;
  form.buyerId = currentUserId;
  query.tenantId = tenantId;
  return currentUserId;
}
</script>

<style scoped>
.order-page {
  min-width: 0;
}

.full-input {
  width: 100%;
}

.item-table {
  margin-top: 16px;
}

.order-actions {
  margin-top: 16px;
}

.order-list-card {
  margin-top: 16px;
}

.filter-bar {
  margin-bottom: 16px;
}

.status-select {
  width: 160px;
}
</style>
