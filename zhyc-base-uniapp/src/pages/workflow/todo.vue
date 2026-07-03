<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page workflow-tab-page mobile-bottom-safe">
    <view class="workflow-appbar">
      <view>
        <view class="workflow-eyebrow">流程</view>
        <view class="workflow-app-title">审批中心</view>
      </view>
      <button class="workflow-refresh" :disabled="status === 'loading'" @tap="loadTasks">刷新</button>
    </view>

    <view class="workflow-overview-card mobile-hero">
      <view class="workflow-overview-head">
        <view>
          <view class="workflow-overview-label">当前待办</view>
          <view class="workflow-overview-title">优先处理可办理任务</view>
        </view>
        <view class="workflow-overview-status">{{ statusText }}</view>
      </view>
      <view class="workflow-overview-main">
        <view class="workflow-pending-number">{{ tasks.length }}</view>
        <view class="workflow-pending-unit">项待处理</view>
      </view>
      <view class="workflow-stat-grid">
        <view v-for="metric in workflowMetrics" :key="metric.label" class="workflow-stat-item">
          <view class="workflow-stat-value">{{ metric.value }}</view>
          <view class="workflow-stat-label">{{ metric.label }}</view>
        </view>
      </view>
    </view>

    <view class="workflow-entry-grid">
      <view
        v-for="entry in workflowEntries"
        :key="entry.url"
        class="workflow-entry-card"
        hover-class="workflow-entry-card-hover"
        @tap="openWorkflowEntry(entry.url)"
      >
        <view :class="['workflow-entry-icon', 'mobile-solid-icon', `tone-${entry.tone}`, `icon-${entry.icon}`]"></view>
        <view class="workflow-entry-title">{{ entry.title }}</view>
        <view class="workflow-entry-desc">{{ entry.description }}</view>
      </view>
    </view>

    <view class="mobile-section-header todo-section-header">
      <view class="mobile-section-title">待办任务</view>
    </view>

    <MobileState
      v-if="status === 'loading' && tasks.length === 0"
      type="loading"
      title="待办任务加载中"
      description="正在同步当前账号可办理的审批任务"
    />
    <MobileState
      v-else-if="status === 'error'"
      type="error"
      title="待办加载失败"
      :description="errorMessage"
      action-text="重试"
      @action="loadTasks"
    />
    <MobileState
      v-else-if="status === 'success' && tasks.length === 0"
      type="empty"
      title="暂无待办任务"
      description="当前没有需要你处理的审批任务"
    />

    <view
      v-for="task in tasks"
      :key="task.taskId"
      class="mobile-list-card mobile-rich-list-card task-card"
      hover-class="task-card-hover"
      @tap="openDetail(task.taskId)"
    >
      <view class="mobile-list-leading">
        <view class="mobile-list-icon mobile-solid-icon tone-orange icon-task"></view>
        <view class="mobile-list-body">
          <view class="mobile-list-top">
            <view class="mobile-card-title">{{ task.taskName }}</view>
            <view class="mobile-status-chip warning">{{ task.status }}</view>
          </view>
          <view class="mobile-list-meta">
            <view class="mobile-list-meta-item">业务单号：{{ task.businessKey }}</view>
            <view class="mobile-list-meta-item">创建时间：{{ task.createdAt }}</view>
          </view>
          <view class="mobile-list-footer">
            <view class="mobile-mini-tag-row">
              <view class="mobile-mini-tag">审批任务</view>
            </view>
            <view class="mobile-list-action-text">去处理</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onShow } from '@dcloudio/uni-app';
import { computed, reactive, ref } from 'vue';

import MobileState from '@/components/MobileState.vue';
import {
  listMobileCcTasks,
  listMobileDoneTasks,
  listMobileStartedProcesses,
  listMobileTodoTasks,
  type MobileWorkflowTask,
} from '@/api/workflow';
import type { MobileShortcut } from '@/types/platform';
import type { MobileLoadStatus } from '@/types/platform';
import { getMobileUserContext, normalizeMobileErrorMessage, requireMobileTenantId, requireMobileUserId } from '@/utils/platform';

/**
 * 流程 Tab 汇总指标。
 */
interface WorkflowMetric {
  /** 指标名称。 */
  label: string;
  /** 指标值。 */
  value: number;
}

/**
 * 流程 Tab 快捷入口。
 */
interface WorkflowEntry extends MobileShortcut {
  /** 图标色系。 */
  tone: 'green' | 'purple' | 'cyan';
  /** 面性图标语义。 */
  icon: 'done' | 'send' | 'copy';
}

/** 页面加载状态。 */
const status = ref<MobileLoadStatus>('idle');
/** 错误提示。 */
const errorMessage = ref('');
/** 待办任务列表。 */
const tasks = ref<MobileWorkflowTask[]>([]);
/** 流程中心汇总数量。 */
const workflowSummary = reactive({
  todoCount: 0,
  doneCount: 0,
  startedCount: 0,
  ccCount: 0,
});
/** 当前同步状态展示文本。 */
const statusText = computed(() => {
  if (status.value === 'loading') {
    return '同步中';
  }
  if (status.value === 'success') {
    return '已同步';
  }
  return status.value === 'error' ? '需处理' : '待同步';
});
/** 流程中心汇总指标。 */
const workflowMetrics = computed<WorkflowMetric[]>(() => [
  { label: '待办', value: workflowSummary.todoCount },
  { label: '已办', value: workflowSummary.doneCount },
  { label: '发起', value: workflowSummary.startedCount },
  { label: '抄送', value: workflowSummary.ccCount },
]);
/** 流程中心快捷入口。 */
const workflowEntries: WorkflowEntry[] = [
  { title: '流程已办', url: '/pages/workflow/done', description: '处理记录', tone: 'green', icon: 'done' },
  { title: '我发起的', url: '/pages/workflow/started', description: '流程跟踪', tone: 'purple', icon: 'send' },
  { title: '抄送我的', url: '/pages/workflow/cc', description: '通知记录', tone: 'cyan', icon: 'copy' },
];

/**
 * 加载移动端待办任务。
 */
async function loadTasks(): Promise<void> {
  status.value = 'loading';
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    const todoTasks = await listMobileTodoTasks();
    tasks.value = todoTasks;
    workflowSummary.todoCount = todoTasks.length;
    const [doneTasks, startedProcesses, ccTasks] = await Promise.allSettled([
      listMobileDoneTasks(),
      listMobileStartedProcesses(),
      listMobileCcTasks(),
    ]);
    workflowSummary.doneCount = readSettledArrayLength(doneTasks, workflowSummary.doneCount);
    workflowSummary.startedCount = readSettledArrayLength(startedProcesses, workflowSummary.startedCount);
    workflowSummary.ccCount = readSettledArrayLength(ccTasks, workflowSummary.ccCount);
    errorMessage.value = resolveFirstRejectedMessage(
      [doneTasks, startedProcesses, ccTasks],
      '部分流程数据同步失败',
    );
    status.value = 'success';
  } catch (error) {
    errorMessage.value = normalizeMobileErrorMessage(error, '待办加载失败');
    status.value = 'error';
  }
}

/**
 * 读取已完成数组结果长度。
 *
 * @param result 异步加载结果
 * @param fallback 失败时保留的旧值
 * @returns 数组长度或旧值
 */
function readSettledArrayLength<T>(
  result: PromiseSettledResult<T[]>,
  fallback: number,
): number {
  return result.status === 'fulfilled' ? result.value.length : fallback;
}

/**
 * 读取首个失败结果的错误文案。
 *
 * @param results 异步加载结果集合
 * @param fallbackMessage 兜底错误文案
 * @returns 错误文案；全部成功时返回空字符串
 */
function resolveFirstRejectedMessage(
  results: PromiseSettledResult<unknown>[],
  fallbackMessage: string,
): string {
  const rejectedResult = results.find((result) => result.status === 'rejected');
  return rejectedResult && rejectedResult.status === 'rejected'
    ? normalizeMobileErrorMessage(rejectedResult.reason, fallbackMessage)
    : '';
}

/**
 * 打开审批详情。
 *
 * @param taskId 任务 ID
 */
function openDetail(taskId: string): void {
  uni.navigateTo({ url: `/pages/workflow/detail?taskId=${encodeURIComponent(taskId)}` });
}

/**
 * 打开流程中心快捷入口。
 *
 * @param url 页面路径
 */
function openWorkflowEntry(url: string): void {
  uni.navigateTo({ url });
}

onShow(() => {
  void loadTasks();
});
</script>

<style scoped>
.workflow-tab-page {
  padding-top: calc(18rpx + env(safe-area-inset-top));
  padding-bottom: calc(132rpx + env(safe-area-inset-bottom));
}

.workflow-appbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  min-height: 72rpx;
  margin-bottom: 18rpx;
}

.workflow-eyebrow {
  color: #64748b;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1.25;
}

.workflow-app-title {
  margin-top: 4rpx;
  color: #111827;
  font-size: 36rpx;
  font-weight: 900;
  line-height: 1.18;
}

.workflow-refresh {
  flex-shrink: 0;
  width: 58rpx;
  height: 58rpx;
  padding: 0;
  border: 1rpx solid #d8e2ee;
  border-radius: 50%;
  color: #0f66d0;
  background: rgba(255, 255, 255, 0.86);
  font-size: 21rpx;
  font-weight: 900;
  line-height: 58rpx;
  box-shadow: 0 8rpx 20rpx rgba(15, 23, 42, 0.05);
}

.workflow-overview-card {
  position: relative;
  overflow: hidden;
  margin-bottom: 20rpx;
  padding: 22rpx;
  border: 1rpx solid rgba(210, 225, 245, 0.9);
  border-radius: 26rpx;
  color: #111827;
  background: linear-gradient(145deg, #ffffff 0%, #f4f9ff 58%, #f6fbf8 100%);
  box-shadow: 0 12rpx 28rpx rgba(38, 82, 140, 0.06);
}

.workflow-overview-card::after {
  position: absolute;
  right: -38rpx;
  top: -56rpx;
  width: 180rpx;
  height: 180rpx;
  border-radius: 50%;
  background: rgba(22, 119, 255, 0.08);
  content: '';
}

.workflow-overview-head,
.workflow-overview-main {
  position: relative;
  z-index: 1;
}

.workflow-overview-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18rpx;
}

.workflow-overview-label {
  color: #5d708b;
  font-size: 23rpx;
  font-weight: 700;
  line-height: 1.35;
}

.workflow-overview-title {
  margin-top: 6rpx;
  color: #0f172a;
  font-size: 32rpx;
  font-weight: 900;
  line-height: 1.25;
}

.workflow-overview-status {
  flex-shrink: 0;
  min-height: 42rpx;
  padding: 0 16rpx;
  border: 1rpx solid #b7d4ff;
  border-radius: 999rpx;
  color: #0969da;
  background: #eff6ff;
  font-size: 21rpx;
  font-weight: 800;
  line-height: 42rpx;
}

.workflow-overview-main {
  display: flex;
  align-items: baseline;
  gap: 10rpx;
  margin-top: 20rpx;
}

.workflow-pending-number {
  color: #0f172a;
  font-size: 56rpx;
  font-weight: 900;
  line-height: 1;
}

.workflow-pending-unit {
  color: #5d708b;
  font-size: 23rpx;
  font-weight: 700;
}

.workflow-stat-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8rpx;
  margin-top: 20rpx;
}

.workflow-stat-item {
  min-width: 0;
  padding: 10rpx 6rpx;
  border: 1rpx solid #e5edf7;
  border-radius: 16rpx;
  background: rgba(255, 255, 255, 0.86);
  text-align: center;
}

.workflow-stat-value {
  color: #111827;
  font-size: 28rpx;
  font-weight: 900;
  line-height: 1.1;
}

.workflow-stat-label {
  margin-top: 4rpx;
  color: #64748b;
  font-size: 20rpx;
  line-height: 1.2;
}

.workflow-entry-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12rpx;
  margin-bottom: 22rpx;
}

.workflow-entry-card {
  min-width: 0;
  padding: 18rpx 10rpx;
  border: 1rpx solid #edf2f8;
  border-radius: 22rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 26rpx rgba(15, 23, 42, 0.035);
  text-align: center;
}

.workflow-entry-card-hover {
  background: #f8fbff;
}

.workflow-entry-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58rpx;
  height: 58rpx;
  margin: 0 auto 10rpx;
  border-radius: 22rpx;
  font-size: 24rpx;
  font-weight: 900;
  line-height: 1;
}

.workflow-entry-title {
  color: #111827;
  font-size: 24rpx;
  font-weight: 900;
  line-height: 1.25;
}

.workflow-entry-desc {
  margin-top: 4rpx;
  color: #64748b;
  font-size: 21rpx;
  line-height: 1.25;
}

.workflow-entry-icon.tone-green {
  color: #047857;
  background: #ecfdf5;
}

.workflow-entry-icon.tone-purple {
  color: #6d28d9;
  background: #f5f3ff;
}

.workflow-entry-icon.tone-cyan {
  color: #0e7490;
  background: #ecfeff;
}

.todo-section-header {
  min-height: 48rpx;
  margin-bottom: 12rpx;
}

.task-card-hover {
  background: #f8fbff;
}
</style>
