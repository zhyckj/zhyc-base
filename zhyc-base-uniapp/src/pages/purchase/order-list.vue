<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page mobile-bottom-safe">
    <MobilePageTopBar
      title="采购订单"
      eyebrow="采购中心"
      action-text="刷新"
      :action-disabled="loading"
      @action="reloadOrders"
    />
    <view class="mobile-hero compact-hero">
      <view class="mobile-hero-header">
        <view class="mobile-hero-main">
          <view class="mobile-hero-kicker">采购中心</view>
          <view class="mobile-title">订单筛选</view>
          <view class="mobile-subtitle">按订单状态筛选采购订单，点击卡片查看明细和确认操作。</view>
        </view>
      </view>
      <view class="mobile-summary-strip">
        <view class="mobile-summary-item">
          <view class="mobile-summary-value">{{ orderPage.total }}</view>
          <view class="mobile-summary-label">订单数</view>
        </view>
        <view class="mobile-summary-item">
          <view class="mobile-summary-value">{{ currentStatusText }}</view>
          <view class="mobile-summary-label">当前筛选</view>
        </view>
        <view class="mobile-summary-item">
          <view class="mobile-summary-value">{{ statusText }}</view>
          <view class="mobile-summary-label">同步状态</view>
        </view>
      </view>
      <scroll-view class="mobile-filter-scroll" scroll-x>
        <view class="mobile-filter-row">
          <button
            v-for="item in statusOptions"
            :key="item.value"
            class="mobile-filter-pill"
            :class="{ active: currentStatus === item.value }"
            @tap="changeStatus(item.value)"
          >
            {{ item.label }}
          </button>
        </view>
      </scroll-view>
    </view>

    <MobileState
      v-if="status === 'loading' && orderPage.records.length === 0"
      type="loading"
      title="采购订单加载中"
      description="正在同步当前状态下的采购订单"
    />
    <MobileState
      v-else-if="status === 'error'"
      type="error"
      title="采购订单加载失败"
      :description="errorMessage"
      action-text="重试"
      @action="reloadOrders"
    />
    <MobileState
      v-else-if="status === 'success' && orderPage.records.length === 0"
      type="empty"
      title="暂无采购订单"
      description="采购订单生成后会展示在这里"
    />

    <view v-if="orderPage.records.length > 0" class="list-summary">
      <text>共 {{ orderPage.total }} 条</text>
      <text>第 {{ orderPage.pageNo }} 页</text>
    </view>

    <view
      v-for="order in orderPage.records"
      :key="order.orderNo"
      class="mobile-list-card mobile-rich-list-card order-card"
      hover-class="order-card-hover"
      @tap="openDetail(order.orderNo)"
    >
      <view class="mobile-list-leading">
        <view class="mobile-list-icon mobile-solid-icon tone-orange icon-order"></view>
        <view class="mobile-list-body">
          <view class="mobile-list-top">
            <view class="mobile-card-title">{{ order.orderNo }}</view>
            <view class="mobile-status-chip" :class="resolveOrderStatusClass(order.orderStatus)">
              {{ resolveOrderStatusText(order.orderStatus) }}
            </view>
          </view>
          <view class="mobile-card-desc">申请单号：{{ order.requestNo }}</view>
          <view class="mobile-list-meta">
            <view class="mobile-list-meta-item">金额：￥{{ order.totalAmount }}</view>
            <view class="mobile-list-meta-item">供应商：{{ order.supplierId }}</view>
          </view>
          <view class="mobile-list-footer">
            <view class="mobile-mini-tag-row">
              <view class="mobile-mini-tag">采购订单</view>
              <view class="mobile-mini-tag">￥{{ order.totalAmount }}</view>
            </view>
            <view class="mobile-list-action-text">看明细</view>
          </view>
        </view>
      </view>
    </view>

    <button
      v-if="status === 'success'"
      class="mobile-secondary-button mobile-load-more"
      :disabled="!hasMore || loadingMore"
      @tap="loadMore"
    >
      {{ loadingMore ? '加载中' : hasMore ? '加载更多' : '没有更多订单' }}
    </button>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

import MobilePageTopBar from '@/components/MobilePageTopBar.vue';
import MobileState from '@/components/MobileState.vue';
import {
  listMobilePurchaseOrders,
  type MobilePurchaseOrderPage,
} from '@/api/purchase';
import type { MobileLoadStatus } from '@/types/platform';
import { getMobileUserContext, requireMobileTenantId, requireMobileUserId, showMobileToast } from '@/utils/platform';

const pageSize = 10;

/** 订单状态筛选项。 */
const statusOptions = [
  { label: '全部', value: '' },
  { label: '新建', value: 'CREATED' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已关闭', value: 'CLOSED' },
];

/** 当前订单状态筛选。 */
const currentStatus = ref('');
/** 加载状态。 */
const loading = ref(false);
/** 是否正在加载下一页。 */
const loadingMore = ref(false);
/** 页面加载状态。 */
const status = ref<MobileLoadStatus>('idle');
/** 页面错误提示。 */
const errorMessage = ref('');
/** 采购订单分页数据。 */
const orderPage = ref<MobilePurchaseOrderPage>({ total: 0, pageNo: 1, pageSize, records: [] });
/** 是否还有更多订单。 */
const hasMore = computed(() => orderPage.value.records.length < orderPage.value.total);
/** 当前筛选状态文本。 */
const currentStatusText = computed(() => statusOptions.find((item) => item.value === currentStatus.value)?.label ?? '全部');
/** 当前同步状态展示文本。 */
const statusText = computed(() => {
  if (loading.value || loadingMore.value) {
    return '同步中';
  }
  if (status.value === 'success') {
    return '已同步';
  }
  return status.value === 'error' ? '需处理' : '待同步';
});

onLoad(() => {
  void reloadOrders();
});

/**
 * 切换订单状态筛选。
 *
 * @param status 订单状态
 */
function changeStatus(status: string): void {
  currentStatus.value = status;
  void reloadOrders();
}

/**
 * 重新加载采购订单列表。
 */
async function reloadOrders(): Promise<void> {
  loading.value = true;
  status.value = 'loading';
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    orderPage.value = await listMobilePurchaseOrders({
      orderStatus: currentStatus.value || undefined,
      pageNo: 1,
      pageSize,
    });
    status.value = 'success';
  } catch (error) {
    errorMessage.value = resolveErrorMessage(error);
    status.value = 'error';
  } finally {
    loading.value = false;
  }
}

/**
 * 加载下一页采购订单。
 */
async function loadMore(): Promise<void> {
  if (!hasMore.value || loadingMore.value) {
    return;
  }
  loadingMore.value = true;
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    const nextPage = await listMobilePurchaseOrders({
      orderStatus: currentStatus.value || undefined,
      pageNo: orderPage.value.pageNo + 1,
      pageSize,
    });
    orderPage.value = {
      total: nextPage.total,
      pageNo: nextPage.pageNo,
      pageSize: nextPage.pageSize,
      records: [...orderPage.value.records, ...nextPage.records],
    };
  } catch (error) {
    showMobileToast(`加载失败：${resolveErrorMessage(error)}`, 'none');
  } finally {
    loadingMore.value = false;
  }
}

/**
 * 打开采购订单详情。
 *
 * @param orderNo 采购订单号
 */
function openDetail(orderNo: string): void {
  uni.navigateTo({ url: `/pages/purchase/order-detail?orderNo=${encodeURIComponent(orderNo)}` });
}

/**
 * 解析采购订单状态显示文本。
 *
 * @param orderStatus 后端订单状态
 * @returns 移动端展示文本
 */
function resolveOrderStatusText(orderStatus: string): string {
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
function resolveOrderStatusClass(orderStatus: string): string {
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
.list-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
  margin: -4rpx 0 14rpx;
  padding: 12rpx 16rpx;
  border: 1rpx solid #e5edf7;
  border-radius: 999rpx;
  color: #64748b;
  background: rgba(255, 255, 255, 0.86);
  font-size: 23rpx;
  font-weight: 700;
}

.order-card-hover {
  background: #f8fbff;
}
</style>
