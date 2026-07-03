<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page mobile-bottom-safe">
    <MobilePageTopBar
      title="我发起的"
      eyebrow="流程中心"
      action-text="刷新"
      :action-disabled="status === 'loading'"
      @action="loadStartedProcesses"
    />
    <view class="mobile-hero compact-hero">
      <view class="mobile-hero-header">
        <view class="mobile-hero-main">
          <view class="mobile-hero-kicker">流程中心</view>
          <view class="mobile-title">流程跟踪</view>
          <view class="mobile-subtitle">跟踪本人发起的流程实例和当前流转状态。</view>
        </view>
      </view>
      <view class="mobile-summary-strip">
        <view class="mobile-summary-item">
          <view class="mobile-summary-value">{{ processes.length }}</view>
          <view class="mobile-summary-label">发起数</view>
        </view>
        <view class="mobile-summary-item">
          <view class="mobile-summary-value">{{ statusText }}</view>
          <view class="mobile-summary-label">同步状态</view>
        </view>
        <view class="mobile-summary-item">
          <view class="mobile-summary-value">流程</view>
          <view class="mobile-summary-label">数据类型</view>
        </view>
      </view>
    </view>

    <MobileState
      v-if="status === 'loading'"
      type="loading"
      title="申请流程加载中"
      description="正在同步你发起的流程实例"
    />
    <MobileState
      v-else-if="status === 'error'"
      type="error"
      title="申请流程加载失败"
      :description="errorMessage"
      action-text="重试"
      @action="loadStartedProcesses"
    />
    <MobileState
      v-else-if="status === 'success' && processes.length === 0"
      type="empty"
      title="暂无我发起的流程"
      description="你发起的审批流程会展示在这里"
    />

    <view
      v-for="process in processes"
      :key="process.processInstanceId"
      class="mobile-list-card mobile-rich-list-card process-card"
      hover-class="process-card-hover"
      @tap="openProcessTrace(process)"
    >
      <view class="mobile-list-leading">
        <view class="mobile-list-icon mobile-solid-icon tone-purple icon-send"></view>
        <view class="mobile-list-body">
          <view class="mobile-list-top">
            <view class="mobile-card-title">{{ process.processKey }}</view>
            <view class="mobile-status-chip" :class="resolveProcessStatusClass(process.status)">
              {{ resolveProcessStatusText(process.status) }}
            </view>
          </view>
          <view class="mobile-list-meta">
            <view class="mobile-list-meta-item">业务单号：{{ process.businessKey }}</view>
            <view class="mobile-list-meta-item">发起时间：{{ process.startedAt }}</view>
          </view>
          <view class="mobile-card-desc">流程实例：{{ process.processInstanceId }}</view>
          <view class="mobile-list-footer">
            <view class="mobile-mini-tag-row">
              <view class="mobile-mini-tag">我发起</view>
            </view>
            <view class="mobile-list-action-text">流程跟踪</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

import MobilePageTopBar from '@/components/MobilePageTopBar.vue';
import MobileState from '@/components/MobileState.vue';
import { listMobileStartedProcesses, type MobileWorkflowStartedProcess } from '@/api/workflow';
import type { MobileLoadStatus } from '@/types/platform';
import { getMobileUserContext, requireMobileTenantId, requireMobileUserId, showMobileToast } from '@/utils/platform';

/** 页面加载状态。 */
const status = ref<MobileLoadStatus>('idle');
/** 错误提示。 */
const errorMessage = ref('');
/** 当前用户发起的流程列表。 */
const processes = ref<MobileWorkflowStartedProcess[]>([]);
/** 当前同步状态展示文本。 */
const statusText = computed(() => (status.value === 'loading' ? '同步中' : status.value === 'success' ? '已同步' : '待同步'));

/**
 * 加载我发起的流程。
 */
async function loadStartedProcesses(): Promise<void> {
  status.value = 'loading';
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    processes.value = await listMobileStartedProcesses();
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '我发起的流程加载失败';
    status.value = 'error';
  }
}

/**
 * 打开流程追踪入口。
 *
 * @param process 流程实例
 */
function openProcessTrace(process: MobileWorkflowStartedProcess): void {
  const traceCode = process.businessKey || process.processInstanceId;
  showMobileToast(`请从待办或已办查看流程轨迹：${traceCode}`, 'none');
}

/**
 * 解析流程状态展示文案。
 *
 * @param status 流程状态
 */
function resolveProcessStatusText(status: string): string {
  const normalizedStatus = status.trim().toUpperCase();
  if (normalizedStatus === 'RUNNING' || normalizedStatus === 'ACTIVE') {
    return '流转中';
  }
  if (normalizedStatus === 'COMPLETED' || normalizedStatus === 'FINISHED') {
    return '已完成';
  }
  if (normalizedStatus === 'REVOKED' || normalizedStatus === 'CANCELED') {
    return '已撤回';
  }
  return status || '未知';
}

/**
 * 解析流程状态样式。
 *
 * @param status 流程状态
 */
function resolveProcessStatusClass(status: string): string {
  const normalizedStatus = status.trim().toUpperCase();
  if (normalizedStatus === 'COMPLETED' || normalizedStatus === 'FINISHED') {
    return 'success';
  }
  if (normalizedStatus === 'REVOKED' || normalizedStatus === 'CANCELED') {
    return 'error';
  }
  return 'warning';
}

onMounted(() => {
  void loadStartedProcesses();
});
</script>

<style scoped>
.process-card-hover {
  background: #f8fbff;
}
</style>
