<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page order-detail-page mobile-bottom-safe">
    <MobilePageTopBar title="采购订单" eyebrow="采购中心" fallback-url="/pages/purchase/order-list" />
    <view class="mobile-hero compact-hero order-hero">
      <view class="mobile-hero-header">
        <view class="mobile-hero-main">
          <view class="mobile-hero-kicker">采购中心</view>
          <view class="mobile-title">订单查询</view>
          <view class="mobile-subtitle">从订单列表进入可直接查看详情，也支持手工查询订单号。</view>
        </view>
        <view v-if="order" class="mobile-status-chip" :class="resolveOrderStatusClass(order.orderStatus)">
          {{ resolveOrderStatusText(order.orderStatus) }}
        </view>
      </view>
    </view>

    <view class="mobile-form-card order-query-card">
      <view class="mobile-form-item order-query-field">
        <view class="mobile-form-label">采购订单号</view>
        <input v-model="orderNo" class="mobile-input" placeholder="请输入采购订单号" />
      </view>
      <button class="mobile-action-button order-query-button" :disabled="loading" @tap="loadOrder">查询</button>
    </view>

    <MobileState
      v-if="status === 'loading'"
      type="loading"
      title="采购订单加载中"
      description="正在同步订单信息和明细"
    />
    <MobileState
      v-else-if="status === 'error'"
      type="error"
      title="采购订单加载失败"
      :description="errorMessage"
      action-text="重试"
      @action="loadOrder"
    />
    <MobileState
      v-else-if="!order"
      type="info"
      title="待查询订单"
      description="输入采购订单号后显示订单信息、明细和可执行操作"
    />

    <view v-if="order" class="order-summary-card">
      <view class="order-summary-head">
        <view class="order-summary-main">
          <view class="order-summary-label">订单金额</view>
          <view class="order-summary-amount">{{ orderAmountText }}</view>
        </view>
        <view class="mobile-status-chip" :class="resolveOrderStatusClass(order.orderStatus)">
          {{ resolveOrderStatusText(order.orderStatus) }}
        </view>
      </view>
      <view class="order-summary-meta">
        <view class="order-summary-pill">订单 {{ order.orderNo }}</view>
        <view class="order-summary-pill">申请 {{ order.requestNo }}</view>
      </view>
    </view>

    <view v-if="order" class="mobile-field-card">
      <view class="mobile-card-row order-heading">
        <view class="mobile-card-title">基础信息</view>
      </view>
      <view class="mobile-field-row">
        <view class="mobile-field-label">订单号</view>
        <view class="mobile-field-value">{{ order?.orderNo ?? '-' }}</view>
      </view>
      <view class="mobile-field-row">
        <view class="mobile-field-label">申请单号</view>
        <view class="mobile-field-value">{{ order?.requestNo ?? '-' }}</view>
      </view>
      <view class="mobile-field-row">
        <view class="mobile-field-label">订单金额</view>
        <view class="mobile-field-value">{{ order?.totalAmount ?? '-' }}</view>
      </view>
      <view class="mobile-field-row">
        <view class="mobile-field-label">供应商</view>
        <view class="mobile-field-value">{{ order?.supplierId ?? '-' }}</view>
      </view>
    </view>

    <view v-if="order && canChangeStatus" class="order-action-card">
      <view class="mobile-card-title order-action-title">订单处理</view>
      <view class="order-action-desc">新建订单可在移动端完成确认或关闭，操作前会二次确认。</view>
      <view class="mobile-action-grid">
        <button class="mobile-action-button" :disabled="loading" @tap="confirmOrder">确认订单</button>
        <button class="mobile-danger-button" :disabled="loading" @tap="closeOrder">关闭订单</button>
      </view>
    </view>

    <view v-else-if="order" class="mobile-form-alert order-readonly-alert">
      当前订单状态为“{{ resolveOrderStatusText(order.orderStatus) }}”，移动端仅展示订单信息和明细。
    </view>

    <view v-if="order" class="mobile-field-card">
      <view class="mobile-card-title item-title">订单明细</view>
      <MobileState
        v-if="order.items.length === 0"
        type="empty"
        title="暂无订单明细"
        description="当前订单没有明细记录"
      />
      <view v-for="item in order?.items" :key="item.itemName" class="order-item-card">
        <view class="order-item-head">
          <view class="mobile-card-title item-name">{{ item.itemName }}</view>
          <view class="order-item-amount">￥{{ item.amount }}</view>
        </view>
        <view class="order-item-meta">
          <view class="order-item-meta-cell">
            <view class="order-item-meta-label">数量</view>
            <view class="order-item-meta-value">{{ item.quantity }}</view>
          </view>
          <view class="order-item-meta-cell">
            <view class="order-item-meta-label">单价</view>
            <view class="order-item-meta-value">￥{{ item.unitPrice }}</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

import MobilePageTopBar from '@/components/MobilePageTopBar.vue';
import MobileState from '@/components/MobileState.vue';
import {
  closeMobilePurchaseOrder,
  confirmMobilePurchaseOrder,
  getMobilePurchaseOrder,
  type MobilePurchaseOrder,
} from '@/api/purchase';
import type { MobileLoadStatus } from '@/types/platform';
import { getMobileUserContext, requireMobileTenantId, requireMobileUserId, showConfirm, showMobileToast } from '@/utils/platform';

/** 当前采购订单号。 */
const orderNo = ref('');
/** 当前采购订单详情。 */
const order = ref<MobilePurchaseOrder>();
/** 加载状态。 */
const loading = ref(false);
/** 页面加载状态。 */
const status = ref<MobileLoadStatus>('idle');
/** 页面错误提示。 */
const errorMessage = ref('');
/** 是否允许执行采购订单状态流转。 */
const canChangeStatus = computed(() => order.value?.orderStatus === 'CREATED');
/** 订单总金额展示。 */
const orderAmountText = computed(() => (order.value ? `￥${order.value.totalAmount}` : '￥0'));

onLoad((query: { orderNo?: string }) => {
  orderNo.value = query.orderNo ?? '';
  if (orderNo.value) {
    void loadOrder();
  }
});

/**
 * 查询采购订单详情。
 */
async function loadOrder(): Promise<void> {
  if (!orderNo.value) {
    showMobileToast('请输入采购订单号', 'none');
    return;
  }
  loading.value = true;
  status.value = 'loading';
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    order.value = await getMobilePurchaseOrder(orderNo.value);
    status.value = 'success';
  } catch (error) {
    errorMessage.value = resolveErrorMessage(error);
    status.value = 'error';
  } finally {
    loading.value = false;
  }
}

/**
 * 确认采购订单。
 */
async function confirmOrder(): Promise<void> {
  if (!order.value) {
    return;
  }
  const confirmed = await showConfirm('确认订单', '二次确认：确定确认当前采购订单？');
  if (!confirmed) {
    return;
  }
  loading.value = true;
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    order.value = await confirmMobilePurchaseOrder(order.value.orderNo);
    showMobileToast('订单已确认', 'success');
  } catch (error) {
    showMobileToast(`确认失败：${resolveErrorMessage(error)}`, 'none');
  } finally {
    loading.value = false;
  }
}

/**
 * 关闭采购订单。
 */
async function closeOrder(): Promise<void> {
  if (!order.value) {
    return;
  }
  const confirmed = await showConfirm('关闭订单', '二次确认：确定关闭当前采购订单？');
  if (!confirmed) {
    return;
  }
  loading.value = true;
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    order.value = await closeMobilePurchaseOrder(order.value.orderNo);
    showMobileToast('订单已关闭', 'success');
  } catch (error) {
    showMobileToast(`关闭失败：${resolveErrorMessage(error)}`, 'none');
  } finally {
    loading.value = false;
  }
}

/**
 * 解析采购订单状态显示文本。
 *
 * @param orderStatus 后端订单状态
 * @returns 移动端展示文本
 */
function resolveOrderStatusText(orderStatus?: string): string {
  if (!orderStatus) {
    return '待查询';
  }
  const statusMap: Record<string, string> = {
    CREATED: '新建',
    CONFIRMED: '已确认',
    CLOSED: '已关闭',
  };
  return statusMap[orderStatus] ?? orderStatus;
}

/**
 * 解析采购订单状态标签样式。
 *
 * @param orderStatus 后端订单状态
 * @returns 状态标签类名
 */
function resolveOrderStatusClass(orderStatus?: string): string {
  if (orderStatus === 'CONFIRMED') {
    return 'success';
  }
  if (orderStatus === 'CLOSED') {
    return 'error';
  }
  return 'warning';
}

/**
 * 解析移动端错误提示。
 *
 * @param error 捕获到的异常
 * @returns 用户可读错误消息
 */
function resolveErrorMessage(error: unknown): string {
  return error instanceof Error && error.message ? error.message : '请稍后重试';
}
</script>

<style scoped>
.order-detail-page {
  background: linear-gradient(180deg, #f6faff 0%, #f4f7fb 48%, #f8fafc 100%);
}

.order-hero {
  border-color: #d8e8ff;
  background: linear-gradient(145deg, #ffffff 0%, #f3f8ff 58%, #f8fbff 100%);
}

.order-query-card {
  display: flex;
  align-items: flex-end;
  gap: 14rpx;
}

.order-query-field {
  min-width: 0;
  flex: 1;
  margin-bottom: 0;
}

.order-query-button {
  flex-shrink: 0;
  min-width: 112rpx;
  min-height: 76rpx;
}

.order-summary-card {
  margin-bottom: 18rpx;
  padding: 24rpx;
  border: 1rpx solid #dbeafe;
  border-radius: 24rpx;
  background: linear-gradient(145deg, #ffffff 0%, #f4f9ff 58%, #f5fbf8 100%);
  box-shadow: 0 14rpx 32rpx rgba(38, 82, 140, 0.07);
}

.order-summary-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
}

.order-summary-main {
  min-width: 0;
  flex: 1;
}

.order-summary-label {
  color: #64748b;
  font-size: 23rpx;
  font-weight: 700;
  line-height: 1.25;
}

.order-summary-amount {
  margin-top: 8rpx;
  color: #0f172a;
  font-size: 48rpx;
  font-weight: 900;
  line-height: 1;
}

.order-summary-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 18rpx;
}

.order-summary-pill {
  max-width: 100%;
  min-height: 44rpx;
  padding: 0 14rpx;
  border-radius: 999rpx;
  color: #52627a;
  background: rgba(255, 255, 255, 0.82);
  font-size: 22rpx;
  font-weight: 700;
  line-height: 44rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-heading {
  align-items: center;
  margin-bottom: 12rpx;
}

.order-action-card {
  margin-bottom: 18rpx;
  padding: 24rpx;
  border: 1rpx solid #edf2f8;
  border-radius: 22rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 28rpx rgba(15, 23, 42, 0.035);
}

.order-action-title {
  margin-bottom: 8rpx;
}

.order-action-desc {
  color: #64748b;
  font-size: 23rpx;
  line-height: 1.45;
}

.order-readonly-alert {
  margin-bottom: 18rpx;
}

.item-title {
  margin-bottom: 12rpx;
}

.item-name {
  font-size: 28rpx;
}

.order-item-card {
  margin-top: 14rpx;
  padding: 18rpx;
  border: 1rpx solid #edf2f8;
  border-radius: 18rpx;
  background: #f8fbff;
}

.order-item-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14rpx;
}

.order-item-amount {
  flex-shrink: 0;
  color: #0f66d0;
  font-size: 26rpx;
  font-weight: 900;
  line-height: 1.35;
}

.order-item-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12rpx;
  margin-top: 16rpx;
}

.order-item-meta-cell {
  min-width: 0;
  padding: 12rpx;
  border-radius: 14rpx;
  background: #ffffff;
}

.order-item-meta-label {
  color: #64748b;
  font-size: 21rpx;
  line-height: 1.2;
}

.order-item-meta-value {
  margin-top: 6rpx;
  color: #111827;
  font-size: 24rpx;
  font-weight: 800;
  line-height: 1.2;
}

.compact-empty {
  padding: 22rpx;
  border-radius: 16rpx;
}
</style>
