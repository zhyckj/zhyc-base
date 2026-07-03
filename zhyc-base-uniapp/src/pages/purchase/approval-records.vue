<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page approval-record-page mobile-bottom-safe">
    <MobilePageTopBar title="审批记录" eyebrow="采购中心" fallback-url="/pages/workflow/todo" />
    <view class="mobile-hero compact-hero approval-record-hero">
      <view class="mobile-hero-header">
        <view class="mobile-hero-main">
          <view class="mobile-hero-kicker">采购中心</view>
          <view class="mobile-title">记录查询</view>
          <view class="mobile-subtitle">从审批详情进入可自动带出任务，也支持手动查询任务 ID。</view>
        </view>
        <view class="mobile-status-chip" :class="{ success: Boolean(detail) }">
          {{ detail ? '已查询' : '待查询' }}
        </view>
      </view>
    </view>

    <view class="mobile-form-card approval-query-card">
      <view class="mobile-form-item approval-query-field">
        <view class="mobile-form-label">审批任务 ID</view>
        <input v-model="taskId" class="mobile-input" placeholder="请输入采购审批任务 ID" />
      </view>
      <button class="mobile-action-button approval-query-button" :disabled="status === 'loading'" @tap="loadApprovalRecords">查询</button>
      <view v-if="status === 'idle' && errorMessage" class="mobile-form-alert">{{ errorMessage }}</view>
    </view>

    <MobileState
      v-if="status === 'loading'"
      type="loading"
      title="审批记录加载中"
      description="正在同步任务信息和审批历史"
    />
    <MobileState
      v-else-if="status === 'error'"
      type="error"
      title="审批记录加载失败"
      :description="errorMessage"
      action-text="重试"
      @action="loadApprovalRecords"
    />

    <view v-if="detail" class="approval-summary-card">
      <view class="approval-summary-head">
        <view>
          <view class="approval-summary-label">审批记录</view>
          <view class="approval-summary-value">{{ detail.approvalRecords.length }}</view>
        </view>
        <view class="mobile-status-chip">{{ detail.status || '未知' }}</view>
      </view>
      <view class="approval-summary-meta">
        <view class="approval-summary-pill">业务 {{ detail.businessKey || '-' }}</view>
        <view class="approval-summary-pill">流程 {{ detail.processKey || '-' }}</view>
      </view>
    </view>

    <view v-if="detail" class="mobile-field-card">
      <view class="mobile-card-title section-heading">任务信息</view>
      <view class="mobile-field-row">
        <view class="mobile-field-label">流程定义</view>
        <view class="mobile-field-value">{{ detail?.processKey ?? '-' }}</view>
      </view>
      <view class="mobile-field-row">
        <view class="mobile-field-label">业务单号</view>
        <view class="mobile-field-value">{{ detail?.businessKey ?? '-' }}</view>
      </view>
      <view class="mobile-field-row">
        <view class="mobile-field-label">任务状态</view>
        <view class="mobile-field-value">{{ detail?.status ?? '-' }}</view>
      </view>
    </view>

    <view v-if="detail" class="mobile-field-card">
      <view class="mobile-card-title section-heading">审批历史</view>
      <MobileState
        v-if="detail?.approvalRecords.length === 0"
        type="empty"
        title="暂无审批记录"
        description="审批处理后会生成历史记录"
      />
      <view v-else class="mobile-timeline">
        <view
          v-for="record in detail?.approvalRecords"
          :key="`${record.taskId}-${record.operatedAt}`"
          class="mobile-timeline-item"
        >
          <view class="mobile-timeline-title">{{ record.action }} · {{ record.approvalComment || '无审批意见' }}</view>
          <view class="mobile-timeline-desc">操作人：{{ record.operatorUserId }}</view>
          <view class="mobile-timeline-desc">时间：{{ record.operatedAt }}</view>
        </view>
      </view>
    </view>

    <MobileState
      v-if="status === 'idle' && !detail"
      type="info"
      title="待查询审批记录"
      description="输入审批任务 ID 后查看任务信息和审批历史"
    />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

import MobilePageTopBar from '@/components/MobilePageTopBar.vue';
import MobileState from '@/components/MobileState.vue';
import { getMobileTaskDetail, type MobileWorkflowTaskDetail } from '@/api/workflow';
import type { MobileLoadStatus } from '@/types/platform';
import { getMobileUserContext, requireMobileTenantId, requireMobileUserId } from '@/utils/platform';

/** 当前查询的采购审批任务 ID。 */
const taskId = ref('');
/** 采购审批任务详情，包含审批记录列表。 */
const detail = ref<MobileWorkflowTaskDetail>();
/** 页面加载状态。 */
const status = ref<MobileLoadStatus>('idle');
/** 查询失败提示。 */
const errorMessage = ref('');

/**
 * 页面加载时读取任务 ID 路由参数。
 *
 * @param query 页面路由参数
 */
onLoad((query: { taskId?: string }) => {
  taskId.value = query.taskId ?? '';
  if (taskId.value) {
    void loadApprovalRecords();
  }
});

/**
 * 加载采购审批记录。
 */
async function loadApprovalRecords(): Promise<void> {
  const normalizedTaskId = taskId.value.trim();
  if (!normalizedTaskId) {
    errorMessage.value = '请输入采购审批任务 ID';
    detail.value = undefined;
    status.value = 'idle';
    return;
  }
  status.value = 'loading';
  errorMessage.value = '';
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    detail.value = await getMobileTaskDetail(normalizedTaskId);
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '采购审批记录加载失败';
    status.value = 'error';
  }
}
</script>

<style scoped>
.approval-record-page {
  background: linear-gradient(180deg, #f6faff 0%, #f4f7fb 48%, #f8fafc 100%);
}

.approval-record-hero {
  border-color: #d8e8ff;
  background: linear-gradient(145deg, #ffffff 0%, #f3f8ff 58%, #f8fbff 100%);
}

.approval-query-card {
  display: flex;
  align-items: flex-end;
  gap: 14rpx;
}

.approval-query-field {
  min-width: 0;
  flex: 1;
  margin-bottom: 0;
}

.approval-query-button {
  flex-shrink: 0;
  min-width: 112rpx;
  min-height: 76rpx;
}

.approval-summary-card {
  margin-bottom: 18rpx;
  padding: 24rpx;
  border: 1rpx solid #dbeafe;
  border-radius: 24rpx;
  background: linear-gradient(145deg, #ffffff 0%, #f6fbff 100%);
  box-shadow: 0 12rpx 30rpx rgba(15, 23, 42, 0.04);
}

.approval-summary-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
}

.approval-summary-label {
  color: #64748b;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1.25;
}

.approval-summary-value {
  margin-top: 8rpx;
  color: #0f172a;
  font-size: 44rpx;
  font-weight: 900;
  line-height: 1;
}

.approval-summary-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 18rpx;
}

.approval-summary-pill {
  max-width: 100%;
  min-height: 44rpx;
  padding: 0 14rpx;
  border-radius: 999rpx;
  color: #52627a;
  background: rgba(255, 255, 255, 0.86);
  font-size: 22rpx;
  font-weight: 700;
  line-height: 44rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.section-heading {
  margin-bottom: 12rpx;
}

.compact-empty {
  padding: 22rpx;
  border-radius: 16rpx;
}
</style>
