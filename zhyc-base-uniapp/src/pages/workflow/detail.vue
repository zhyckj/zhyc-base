<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page detail-page mobile-bottom-safe">
    <MobilePageTopBar title="审批详情" eyebrow="流程中心" fallback-url="/pages/workflow/todo" />
    <view class="mobile-hero compact-hero detail-hero">
      <view class="mobile-hero-header">
        <view class="mobile-hero-main">
          <view class="mobile-hero-kicker">审批详情</view>
          <view class="mobile-title">{{ detail?.taskName || '任务信息' }}</view>
          <view class="mobile-subtitle">查看任务上下文和流转记录，在可处理状态下完成审批。</view>
        </view>
        <view class="mobile-status-chip" :class="resolveTaskStatusClass(detail?.status)">
          {{ resolveTaskStatusText(detail?.status) }}
        </view>
      </view>
      <view class="detail-summary-grid">
        <view class="detail-summary-item">
          <view class="detail-summary-label">任务</view>
          <view class="detail-summary-value">{{ taskIdText }}</view>
        </view>
        <view class="detail-summary-item">
          <view class="detail-summary-label">业务</view>
          <view class="detail-summary-value">{{ detail?.businessKey ?? '-' }}</view>
        </view>
      </view>
    </view>

    <MobileState
      v-if="status === 'loading'"
      type="loading"
      title="审批详情加载中"
      description="正在同步任务上下文和审批历史"
    />
    <MobileState
      v-else-if="status === 'error'"
      type="error"
      title="审批详情加载失败"
      :description="errorMessage"
      :action-text="hasTaskId ? '重试' : '返回待办'"
      @action="handleErrorAction"
    />

    <view v-if="detail" class="mobile-field-card">
      <view class="mobile-card-title section-heading">任务信息</view>
      <view class="mobile-field-row">
        <view class="mobile-field-label">当前节点</view>
        <view class="mobile-field-value">{{ detail?.taskName ?? '-' }}</view>
      </view>
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
        <view class="mobile-field-value">{{ resolveTaskStatusText(detail?.status) }}</view>
      </view>
    </view>

    <view v-if="detail" class="mobile-field-card">
      <view class="mobile-card-title section-heading">审批历史</view>
      <MobileState
        v-if="detail?.approvalRecords.length === 0"
        type="empty"
        title="暂无审批记录"
        description="审批处理后会生成流转记录"
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

    <view v-if="detail && canHandleTask" class="mobile-form-card approval-card">
      <view class="mobile-card-title section-heading">审批意见</view>
      <view class="comment-preset-row">
        <view
          v-for="preset in commentPresets"
          :key="preset"
          class="comment-preset"
          @tap="applyCommentPreset(preset)"
        >
          {{ preset }}
        </view>
      </view>
      <view class="mobile-form-item">
        <view class="mobile-form-label">处理意见</view>
        <textarea v-model="comment" class="mobile-textarea" placeholder="请输入审批意见，驳回或撤回时建议说明原因" />
      </view>
      <view class="approval-action-panel">
        <button class="mobile-action-button approval-primary" :disabled="submitting" @tap="confirmHandle('approve')">通过审批</button>
        <button class="mobile-danger-button" :disabled="submitting" @tap="confirmHandle('reject')">驳回</button>
        <button class="mobile-ghost-button" :disabled="submitting || !detail?.processInstanceId" @tap="confirmHandle('revoke')">
          撤回
        </button>
      </view>
    </view>

    <view v-else-if="detail" class="mobile-form-alert detail-readonly-alert">
      当前任务状态为“{{ resolveTaskStatusText(detail.status) }}”，移动端仅展示任务信息和审批历史。
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

import MobilePageTopBar from '@/components/MobilePageTopBar.vue';
import MobileState from '@/components/MobileState.vue';
import {
  approveMobileTask,
  getMobileTaskDetail,
  rejectMobileTask,
  revokeMobileTask,
  type MobileWorkflowTaskDetail,
} from '@/api/workflow';
import type { MobileLoadStatus } from '@/types/platform';
import {
  getMobileUserContext,
  openMobilePage,
  requireMobileTenantId,
  requireMobileUserId,
  showConfirm,
  showMobileToast,
} from '@/utils/platform';

/** 当前页面任务 ID，首期由路由参数传入。 */
const taskId = ref('');
/** 审批意见。 */
const comment = ref('同意');
/** 提交中状态。 */
const submitting = ref(false);
/** 页面加载状态。 */
const status = ref<MobileLoadStatus>('idle');
/** 错误提示。 */
const errorMessage = ref('');
/** 当前任务详情。 */
const detail = ref<MobileWorkflowTaskDetail>();
/** 移动端常用审批意见。 */
const commentPresets = ['同意', '请补充材料', '信息有误，驳回'];

/** 页面显示任务 ID。 */
const taskIdText = computed(() => (taskId.value ? taskId.value : '待选择'));
/** 当前页面是否带有任务 ID。 */
const hasTaskId = computed(() => Boolean(taskId.value));
/** 是否允许处理当前审批任务。 */
const canHandleTask = computed(() => status.value === 'success' && Boolean(detail.value?.taskId) && isPendingTaskStatus(detail.value?.status));

/**
 * uni-app 页面加载钩子。
 *
 * @param query 路由参数
 */
onLoad((query: { taskId?: string }) => {
  taskId.value = query.taskId ?? '';
  void loadDetail();
});

/**
 * 加载审批详情。
 */
async function loadDetail(): Promise<void> {
  if (!taskId.value) {
    errorMessage.value = '请从待办、已办或业务列表进入审批详情';
    status.value = 'error';
    return;
  }
  status.value = 'loading';
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    detail.value = await getMobileTaskDetail(taskId.value);
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '审批详情加载失败';
    status.value = 'error';
  }
}

/**
 * 二次确认后处理审批动作。
 *
 * @param action 审批动作
 */
function confirmHandle(action: 'approve' | 'reject' | 'revoke'): void {
  const actionText = resolveActionText(action);
  const title = `确认${actionText}`;
  const content = `二次确认：确定${actionText}当前审批任务？`;
  void showConfirm(title, content).then((confirmed) => {
    if (confirmed) {
      void handleTask(action);
    }
  });
}

/**
 * 应用常用审批意见。
 *
 * @param preset 审批意见模板
 */
function applyCommentPreset(preset: string): void {
  comment.value = preset;
}

/**
 * 调用后端处理工作流任务。
 *
 * @param action 审批动作
 */
async function handleTask(action: 'approve' | 'reject' | 'revoke'): Promise<void> {
  if (!canHandleTask.value) {
    return;
  }
  submitting.value = true;
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    const command = { comment: comment.value };
    if (action === 'approve') {
      await approveMobileTask(taskId.value, command);
    } else if (action === 'reject') {
      await rejectMobileTask(taskId.value, command);
    } else if (detail.value?.processInstanceId) {
      await revokeMobileTask(detail.value.processInstanceId, { reason: comment.value });
    }
    showMobileToast('处理成功', 'success');
    await loadDetail();
  } catch (error) {
    showMobileToast(`处理失败：${resolveErrorMessage(error)}`, 'none');
  } finally {
    submitting.value = false;
  }
}

/**
 * 返回流程待办列表。
 */
function backToTodo(): void {
  openMobilePage('/pages/workflow/todo');
}

/**
 * 处理详情错误态按钮动作。
 */
function handleErrorAction(): void {
  if (hasTaskId.value) {
    void loadDetail();
    return;
  }
  backToTodo();
}

/**
 * 解析审批动作展示文本。
 *
 * @param action 审批动作
 * @returns 审批动作中文文本
 */
function resolveActionText(action: 'approve' | 'reject' | 'revoke'): string {
  if (action === 'approve') {
    return '审批通过';
  }
  if (action === 'reject') {
    return '驳回';
  }
  return '撤回';
}

/**
 * 解析审批任务状态展示文本。
 *
 * @param taskStatus 后端任务状态
 * @returns 移动端展示文本
 */
function resolveTaskStatusText(taskStatus?: string): string {
  if (!taskStatus) {
    return '待加载';
  }
  const statusMap: Record<string, string> = {
    TODO: '待处理',
    PENDING: '待处理',
    CLAIMED: '处理中',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    REVOKED: '已撤回',
    DONE: '已处理',
    COMPLETED: '已完成',
  };
  return statusMap[taskStatus] ?? taskStatus;
}

/**
 * 解析审批任务状态标签样式。
 *
 * @param taskStatus 后端任务状态
 * @returns 状态标签类名
 */
function resolveTaskStatusClass(taskStatus?: string): string {
  if (!taskStatus || taskStatus === 'TODO' || taskStatus === 'PENDING' || taskStatus === 'CLAIMED') {
    return 'warning';
  }
  if (taskStatus === 'APPROVED' || taskStatus === 'DONE' || taskStatus === 'COMPLETED') {
    return 'success';
  }
  if (taskStatus === 'REJECTED' || taskStatus === 'REVOKED') {
    return 'error';
  }
  return '';
}

/**
 * 判断任务是否仍处于可处理状态。
 *
 * @param taskStatus 后端任务状态
 */
function isPendingTaskStatus(taskStatus?: string): boolean {
  return !taskStatus || ['TODO', 'PENDING', 'CLAIMED'].includes(taskStatus);
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
.detail-page {
  background: linear-gradient(180deg, #f6faff 0%, #f4f7fb 48%, #f8fafc 100%);
}

.detail-hero {
  border-color: #d8e8ff;
  background: linear-gradient(145deg, #ffffff 0%, #f3f8ff 56%, #f8fbff 100%);
}

.detail-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12rpx;
  margin-top: 18rpx;
}

.detail-summary-item {
  min-width: 0;
  padding: 14rpx 16rpx;
  border: 1rpx solid #e5edf7;
  border-radius: 18rpx;
  background: rgba(255, 255, 255, 0.84);
}

.detail-summary-label {
  color: #64748b;
  font-size: 21rpx;
  font-weight: 700;
  line-height: 1.25;
}

.detail-summary-value {
  margin-top: 6rpx;
  color: #111827;
  font-size: 24rpx;
  font-weight: 800;
  line-height: 1.25;
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

.approval-card {
  padding-bottom: 26rpx;
}

.comment-preset-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin: 0 0 18rpx;
}

.comment-preset {
  min-height: 48rpx;
  padding: 0 18rpx;
  border: 1rpx solid #d8e2ee;
  border-radius: 999rpx;
  color: #0f66d0;
  background: #f8fbff;
  font-size: 23rpx;
  font-weight: 700;
  line-height: 48rpx;
}

.approval-action-panel {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx;
  margin-top: 18rpx;
}

.approval-primary {
  grid-column: 1 / -1;
}

.detail-readonly-alert {
  margin-bottom: 18rpx;
}
</style>
