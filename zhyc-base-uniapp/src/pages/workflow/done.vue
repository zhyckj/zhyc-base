<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page mobile-bottom-safe">
    <MobilePageTopBar
      title="流程已办"
      eyebrow="流程中心"
      action-text="刷新"
      :action-disabled="status === 'loading'"
      @action="loadTasks"
    />
    <view class="mobile-hero compact-hero">
      <view class="mobile-hero-header">
        <view class="mobile-hero-main">
          <view class="mobile-hero-kicker">流程中心</view>
          <view class="mobile-title">处理记录</view>
          <view class="mobile-subtitle">查看已处理审批任务，快速追溯处理记录。</view>
        </view>
      </view>
      <view class="mobile-summary-strip">
        <view class="mobile-summary-item">
          <view class="mobile-summary-value">{{ tasks.length }}</view>
          <view class="mobile-summary-label">已处理</view>
        </view>
        <view class="mobile-summary-item">
          <view class="mobile-summary-value">{{ statusText }}</view>
          <view class="mobile-summary-label">同步状态</view>
        </view>
        <view class="mobile-summary-item">
          <view class="mobile-summary-value">记录</view>
          <view class="mobile-summary-label">任务来源</view>
        </view>
      </view>
    </view>

    <MobileState
      v-if="status === 'loading'"
      type="loading"
      title="已办任务加载中"
      description="正在同步你处理过的审批任务"
    />
    <MobileState
      v-else-if="status === 'error'"
      type="error"
      title="已办加载失败"
      :description="errorMessage"
      action-text="重试"
      @action="loadTasks"
    />
    <MobileState
      v-else-if="status === 'success' && tasks.length === 0"
      type="empty"
      title="暂无已办任务"
      description="处理完成的任务会展示在这里"
    />

    <view
      v-for="task in tasks"
      :key="task.taskId"
      class="mobile-list-card mobile-rich-list-card task-card"
      hover-class="task-card-hover"
      @tap="openDetail(task.taskId)"
    >
      <view class="mobile-list-leading">
        <view class="mobile-list-icon mobile-solid-icon tone-green icon-done"></view>
        <view class="mobile-list-body">
          <view class="mobile-list-top">
            <view class="mobile-card-title">{{ task.taskName }}</view>
            <view class="mobile-status-chip success">{{ task.status }}</view>
          </view>
          <view class="mobile-list-meta">
            <view class="mobile-list-meta-item">业务单号：{{ task.businessKey }}</view>
            <view class="mobile-list-meta-item">处理时间：{{ task.createdAt }}</view>
          </view>
          <view class="mobile-list-footer">
            <view class="mobile-mini-tag-row">
              <view class="mobile-mini-tag">处理记录</view>
            </view>
            <view class="mobile-list-action-text">看详情</view>
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
import { listMobileDoneTasks, type MobileWorkflowTask } from '@/api/workflow';
import type { MobileLoadStatus } from '@/types/platform';
import { getMobileUserContext, requireMobileTenantId, requireMobileUserId } from '@/utils/platform';

/** 页面加载状态。 */
const status = ref<MobileLoadStatus>('idle');
/** 错误提示。 */
const errorMessage = ref('');
/** 已办任务列表。 */
const tasks = ref<MobileWorkflowTask[]>([]);
/** 当前同步状态展示文本。 */
const statusText = computed(() => (status.value === 'loading' ? '同步中' : status.value === 'success' ? '已同步' : '待同步'));

/**
 * 加载移动端已办任务。
 */
async function loadTasks(): Promise<void> {
  status.value = 'loading';
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    tasks.value = await listMobileDoneTasks();
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '已办加载失败';
    status.value = 'error';
  }
}

/**
 * 打开已办任务详情。
 *
 * @param taskId 任务 ID
 */
function openDetail(taskId: string): void {
  uni.navigateTo({ url: `/pages/workflow/detail?taskId=${encodeURIComponent(taskId)}` });
}

onMounted(() => {
  void loadTasks();
});
</script>

<style scoped>
.task-card-hover {
  background: #f8fbff;
}
</style>
