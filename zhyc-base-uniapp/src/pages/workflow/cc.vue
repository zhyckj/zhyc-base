<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page mobile-bottom-safe">
    <MobilePageTopBar
      title="抄送我的"
      eyebrow="流程中心"
      action-text="刷新"
      :action-disabled="status === 'loading'"
      @action="loadCcTasks"
    />
    <view class="mobile-hero compact-hero">
      <view class="mobile-hero-header">
        <view class="mobile-hero-main">
          <view class="mobile-hero-kicker">流程中心</view>
          <view class="mobile-title">通知记录</view>
          <view class="mobile-subtitle">查看抄送给当前账号的流程通知和阅读状态。</view>
        </view>
      </view>
      <view class="mobile-summary-strip">
        <view class="mobile-summary-item">
          <view class="mobile-summary-value">{{ ccTasks.length }}</view>
          <view class="mobile-summary-label">抄送数</view>
        </view>
        <view class="mobile-summary-item">
          <view class="mobile-summary-value">{{ statusText }}</view>
          <view class="mobile-summary-label">同步状态</view>
        </view>
        <view class="mobile-summary-item">
          <view class="mobile-summary-value">通知</view>
          <view class="mobile-summary-label">数据类型</view>
        </view>
      </view>
    </view>

    <MobileState
      v-if="status === 'loading'"
      type="loading"
      title="抄送任务加载中"
      description="正在同步抄送给你的流程通知"
    />
    <MobileState
      v-else-if="status === 'error'"
      type="error"
      title="抄送任务加载失败"
      :description="errorMessage"
      action-text="重试"
      @action="loadCcTasks"
    />
    <MobileState
      v-else-if="status === 'success' && ccTasks.length === 0"
      type="empty"
      title="暂无抄送任务"
      description="抄送给你的流程会展示在这里"
    />

    <view
      v-for="task in ccTasks"
      :key="task.ccRecordId"
      class="mobile-list-card mobile-rich-list-card cc-card"
      hover-class="cc-card-hover"
      @tap="openCcRecord(task)"
    >
      <view class="mobile-list-leading">
        <view class="mobile-list-icon mobile-solid-icon tone-cyan icon-copy"></view>
        <view class="mobile-list-body">
          <view class="mobile-list-top">
            <view class="mobile-card-title">{{ task.processKey }}</view>
            <view class="mobile-status-chip" :class="{ success: task.readFlag === 1 }">
              {{ task.readFlag === 1 ? '已读' : '未读' }}
            </view>
          </view>
          <view class="mobile-list-meta">
            <view class="mobile-list-meta-item">业务单号：{{ task.businessKey }}</view>
            <view class="mobile-list-meta-item">抄送时间：{{ task.createdAt }}</view>
          </view>
          <view class="mobile-card-desc">流程实例：{{ task.processInstanceId }}</view>
          <view class="mobile-list-footer">
            <view class="mobile-mini-tag-row">
              <view class="mobile-mini-tag">流程通知</view>
            </view>
            <view class="mobile-list-action-text">{{ task.readFlag === 1 ? '抄送记录' : '查看抄送' }}</view>
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
import { listMobileCcTasks, type MobileWorkflowCcTask } from '@/api/workflow';
import type { MobileLoadStatus } from '@/types/platform';
import { getMobileUserContext, requireMobileTenantId, requireMobileUserId, showMobileToast } from '@/utils/platform';

/** 页面加载状态。 */
const status = ref<MobileLoadStatus>('idle');
/** 错误提示。 */
const errorMessage = ref('');
/** 当前用户收到的抄送任务列表。 */
const ccTasks = ref<MobileWorkflowCcTask[]>([]);
/** 当前同步状态展示文本。 */
const statusText = computed(() => (status.value === 'loading' ? '同步中' : status.value === 'success' ? '已同步' : '待同步'));

/**
 * 加载抄送我的任务。
 */
async function loadCcTasks(): Promise<void> {
  status.value = 'loading';
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    ccTasks.value = await listMobileCcTasks();
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '抄送任务加载失败';
    status.value = 'error';
  }
}

/**
 * 打开抄送记录入口。
 *
 * @param task 抄送任务
 */
function openCcRecord(task: MobileWorkflowCcTask): void {
  const traceCode = task.businessKey || task.processInstanceId;
  showMobileToast(`请从待办或已办查看审批详情：${traceCode}`, 'none');
}

onMounted(() => {
  void loadCcTasks();
});
</script>

<style scoped>
.cc-card-hover {
  background: #f8fbff;
}
</style>
