<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page mobile-bottom-safe">
    <MobilePageTopBar title="采购申请列表" eyebrow="采购中心" action-text="新建" @action="openCreate" />
    <view class="mobile-hero compact-hero">
      <view class="mobile-hero-header">
        <view class="mobile-hero-main">
          <view class="mobile-hero-kicker">采购中心</view>
          <view class="mobile-title">申请筛选</view>
          <view class="mobile-subtitle">查看草稿、审批中和已完成的采购申请。</view>
        </view>
      </view>
      <view class="mobile-summary-strip">
        <view class="mobile-summary-item">
          <view class="mobile-summary-value">{{ requestPage.total }}</view>
          <view class="mobile-summary-label">申请数</view>
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
      v-if="status === 'loading' && requestPage.records.length === 0"
      type="loading"
      title="采购申请加载中"
      description="正在同步当前状态下的采购申请"
    />
    <MobileState
      v-else-if="status === 'error'"
      type="error"
      title="采购申请加载失败"
      :description="errorMessage"
      action-text="重试"
      @action="reloadRequests"
    />
    <MobileState
      v-else-if="status === 'success' && requestPage.records.length === 0"
      type="empty"
      title="暂无采购申请"
      description="你可以新建采购申请并提交审批"
      action-text="新建"
      @action="openCreate"
    />

    <view v-if="requestPage.records.length > 0" class="list-summary">
      <text>共 {{ requestPage.total }} 条</text>
      <text>第 {{ requestPage.pageNo }} 页</text>
    </view>

    <view
      v-for="requestItem in requestPage.records"
      :key="requestItem.requestNo"
      class="mobile-list-card mobile-rich-list-card request-card"
      hover-class="request-card-hover"
      @tap="openRequestFlow(requestItem)"
    >
      <view class="mobile-list-leading">
        <view class="mobile-list-icon mobile-solid-icon tone-green icon-cart"></view>
        <view class="mobile-list-body">
          <view class="mobile-list-top">
            <view class="mobile-card-title">{{ requestItem.requestTitle }}</view>
            <view class="mobile-status-chip" :class="resolveProcessStatusClass(requestItem.processStatus)">
              {{ resolveProcessStatusText(requestItem.processStatus) }}
            </view>
          </view>
          <view class="mobile-card-desc">{{ requestItem.requestNo }}</view>
          <view class="mobile-list-meta">
            <view class="mobile-list-meta-item">金额：￥{{ requestItem.totalAmount }}</view>
            <view class="mobile-list-meta-item">提交：{{ requestItem.submittedAt ?? '-' }}</view>
          </view>
          <view class="mobile-list-footer">
            <view class="mobile-mini-tag-row">
              <view class="mobile-mini-tag">采购申请</view>
              <view class="mobile-mini-tag">￥{{ requestItem.totalAmount }}</view>
            </view>
            <button
              v-if="requestItem.processStatus === 'DRAFT'"
              class="mobile-secondary-button row-action"
              :disabled="submittingRequestNo === requestItem.requestNo"
              @tap.stop="submitRequest(requestItem.requestNo)"
            >
              {{ submittingRequestNo === requestItem.requestNo ? '提交中' : '提交审批' }}
            </button>
            <view v-else class="mobile-list-action-text">审批流转</view>
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
      {{ loadingMore ? '加载中' : hasMore ? '加载更多' : '没有更多申请' }}
    </button>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';

import MobilePageTopBar from '@/components/MobilePageTopBar.vue';
import MobileState from '@/components/MobileState.vue';
import {
  listMobilePurchaseRequests,
  submitMobilePurchaseRequest,
  type MobilePurchaseRequest,
  type MobilePurchaseRequestPage,
} from '@/api/purchase';
import type { MobileLoadStatus } from '@/types/platform';
import { getMobileUserContext, requireMobileTenantId, requireMobileUserId, showConfirm, showMobileToast } from '@/utils/platform';

const pageSize = 10;

/** 流程状态筛选项。 */
const statusOptions = [
  { label: '全部', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '审批中', value: 'APPROVING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
];

/** 当前流程状态筛选。 */
const currentStatus = ref('');
/** 加载状态。 */
const loading = ref(false);
/** 是否正在加载下一页。 */
const loadingMore = ref(false);
/** 正在提交审批的采购申请单号。 */
const submittingRequestNo = ref('');
/** 页面加载状态。 */
const status = ref<MobileLoadStatus>('idle');
/** 页面错误提示。 */
const errorMessage = ref('');
/** 采购申请分页数据。 */
const requestPage = ref<MobilePurchaseRequestPage>({ total: 0, pageNo: 1, pageSize, records: [] });
/** 是否还有更多采购申请。 */
const hasMore = computed(() => requestPage.value.records.length < requestPage.value.total);
/** 当前筛选状态文本。 */
const currentStatusText = computed(() => statusOptions.find((item) => item.value === currentStatus.value)?.label ?? '全部');
/** 当前同步状态展示文本。 */
const statusText = computed(() => {
  if (loading.value || loadingMore.value || submittingRequestNo.value) {
    return '同步中';
  }
  if (status.value === 'success') {
    return '已同步';
  }
  return status.value === 'error' ? '需处理' : '待同步';
});

onShow(() => {
  void reloadRequests();
});

/**
 * 切换流程状态筛选。
 *
 * @param status 流程状态
 */
function changeStatus(status: string): void {
  currentStatus.value = status;
  void reloadRequests();
}

/**
 * 重新加载采购申请列表。
 */
async function reloadRequests(): Promise<void> {
  loading.value = true;
  status.value = 'loading';
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    requestPage.value = await listMobilePurchaseRequests({
      processStatus: currentStatus.value || undefined,
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
 * 加载下一页采购申请。
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
    const nextPage = await listMobilePurchaseRequests({
      processStatus: currentStatus.value || undefined,
      pageNo: requestPage.value.pageNo + 1,
      pageSize,
    });
    requestPage.value = {
      total: nextPage.total,
      pageNo: nextPage.pageNo,
      pageSize: nextPage.pageSize,
      records: [...requestPage.value.records, ...nextPage.records],
    };
  } catch (error) {
    showMobileToast(`加载失败：${resolveErrorMessage(error)}`, 'none');
  } finally {
    loadingMore.value = false;
  }
}

/**
 * 提交采购申请审批。
 *
 * @param requestNo 采购申请单号
 */
async function submitRequest(requestNo: string): Promise<void> {
  const confirmed = await showConfirm('提交审批', '二次确认：确定提交当前采购申请进入审批？');
  if (!confirmed) {
    return;
  }
  submittingRequestNo.value = requestNo;
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    await submitMobilePurchaseRequest(requestNo);
    showMobileToast('已提交审批', 'success');
    await reloadRequests();
  } catch (error) {
    showMobileToast(`提交失败：${resolveErrorMessage(error)}`, 'none');
  } finally {
    submittingRequestNo.value = '';
  }
}

/**
 * 打开采购申请创建页。
 */
function openCreate(): void {
  uni.navigateTo({ url: '/pages/purchase/request-form' });
}

/**
 * 打开采购申请流转反馈。
 *
 * @param requestItem 采购申请列表项
 */
function openRequestFlow(requestItem: MobilePurchaseRequest): void {
  if (requestItem.processStatus === 'DRAFT') {
    showMobileToast('草稿申请可直接提交审批', 'none');
    return;
  }
  showMobileToast(`请从流程待办或已办查看流转记录：${requestItem.requestNo}`, 'none');
}

/**
 * 解析采购申请流程状态显示文本。
 *
 * @param processStatus 后端流程状态
 * @returns 移动端展示文本
 */
function resolveProcessStatusText(processStatus: string): string {
  const statusMap: Record<string, string> = {
    DRAFT: '草稿',
    APPROVING: '审批中',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
  };
  return statusMap[processStatus] ?? processStatus;
}

/**
 * 解析采购申请流程状态标签样式。
 *
 * @param processStatus 后端流程状态
 * @returns 状态标签类名
 */
function resolveProcessStatusClass(processStatus: string): string {
  if (processStatus === 'APPROVED') {
    return 'success';
  }
  if (processStatus === 'DRAFT' || processStatus === 'APPROVING') {
    return 'warning';
  }
  if (processStatus === 'REJECTED') {
    return 'error';
  }
  return '';
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

.row-action {
  flex-shrink: 0;
  width: 166rpx;
  min-height: 52rpx;
  padding: 0;
  font-size: 23rpx;
  line-height: 52rpx;
}

.request-card-hover {
  background: #f8fbff;
}
</style>
