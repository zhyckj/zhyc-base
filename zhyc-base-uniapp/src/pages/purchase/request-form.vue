<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page request-form-page mobile-bottom-safe">
    <MobilePageTopBar title="采购申请" eyebrow="采购中心" />
    <view class="mobile-hero compact-hero request-hero">
      <view class="mobile-hero-header">
        <view class="mobile-hero-main">
          <view class="mobile-hero-kicker">采购中心</view>
          <view class="mobile-title">填写申请信息</view>
          <view class="mobile-subtitle">填写申请信息，先创建业务单，再提交审批流转。</view>
        </view>
        <view class="mobile-status-chip" :class="{ success: Boolean(submitResult), warning: !submitResult }">
          {{ requestStageText }}
        </view>
      </view>
    </view>

    <view class="request-summary-card">
      <view class="request-summary-main">
        <view class="request-summary-label">申请金额</view>
        <view class="request-summary-value">{{ amountText }}</view>
      </view>
      <view class="request-summary-side">
        <view class="request-summary-title">{{ requestTitle || '待填写标题' }}</view>
        <view class="request-summary-no">{{ requestNo || '待生成单号' }}</view>
      </view>
    </view>

    <view class="request-progress-card">
      <view :class="['request-step', { active: Boolean(createdRequestNo), done: Boolean(createdRequestNo) }]">1 创建申请</view>
      <view class="request-step-line"></view>
      <view :class="['request-step', { active: Boolean(submitResult), done: Boolean(submitResult) }]">2 提交审批</view>
    </view>

    <view class="mobile-form-card">
      <view class="mobile-form-item">
        <view class="mobile-form-label">采购申请单号</view>
        <view class="form-inline-row">
          <input v-model="requestNo" class="mobile-input inline-input" placeholder="可手输或自动生成" />
          <button class="inline-button" :disabled="submitting" @tap="generateRequestNo">生成</button>
        </view>
      </view>
      <view class="mobile-form-item">
        <view class="mobile-form-label">申请标题</view>
        <input v-model="requestTitle" class="mobile-input" placeholder="请输入申请标题" />
      </view>
      <view class="mobile-form-item">
        <view class="mobile-form-label">申请金额</view>
        <input v-model.number="totalAmount" class="mobile-input" type="digit" placeholder="请输入大于 0 的金额" />
        <view class="quick-chip-row">
          <button
            v-for="amount in amountPresets"
            :key="amount"
            class="quick-chip"
            :class="{ active: totalAmount === amount }"
            @tap="applyAmountPreset(amount)"
          >
            {{ amount }} 元
          </button>
        </view>
      </view>
      <view class="mobile-form-item">
        <view class="mobile-form-label">申请原因</view>
        <textarea v-model="requestReason" class="mobile-textarea" placeholder="请输入采购申请原因" />
        <view class="quick-chip-row">
          <button
            v-for="reason in reasonTemplates"
            :key="reason"
            class="quick-chip"
            :class="{ active: requestReason === reason }"
            @tap="applyReasonTemplate(reason)"
          >
            {{ reason }}
          </button>
        </view>
      </view>
      <view class="mobile-action-grid">
        <button class="mobile-action-button" :disabled="submitting" @tap="createRequest">创建申请</button>
        <button class="mobile-secondary-button" :disabled="!canSubmitRequest || submitting" @tap="submitRequest">
          提交审批
        </button>
      </view>
      <view v-if="formError" class="mobile-form-alert">{{ formError }}</view>
    </view>

    <view v-if="createdRequestNo || submitResult" class="mobile-field-card result-card">
      <view class="mobile-card-row result-heading">
        <view class="mobile-card-title result-title">处理结果</view>
        <view class="mobile-status-chip" :class="{ success: Boolean(submitResult), warning: !submitResult }">
          {{ submitResult ? '已提交' : '已创建' }}
        </view>
      </view>
      <view class="mobile-field-row">
        <view class="mobile-field-label">已创建单号</view>
        <view class="mobile-field-value">{{ createdRequestNo || '-' }}</view>
      </view>
      <view class="mobile-field-row">
        <view class="mobile-field-label">申请单号</view>
        <view class="mobile-field-value">{{ submitResult?.requestNo ?? '-' }}</view>
      </view>
      <view class="mobile-field-row">
        <view class="mobile-field-label">流程实例</view>
        <view class="mobile-field-value">{{ submitResult?.processInstanceId ?? '-' }}</view>
      </view>
      <view class="mobile-field-row">
        <view class="mobile-field-label">流程状态</view>
        <view class="mobile-field-value">{{ submitResult?.processStatus ?? '-' }}</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import MobilePageTopBar from '@/components/MobilePageTopBar.vue';
import {
  createMobilePurchaseRequest,
  submitMobilePurchaseRequest,
  type MobilePurchaseSubmitResult,
} from '@/api/purchase';
import {
  getMobileUserContext,
  requireMobileOrgId,
  requireMobileTenantId,
  requireMobileUserId,
  showConfirm,
  showMobileToast,
} from '@/utils/platform';

/** 采购申请单号。 */
const requestNo = ref('');
/** 采购申请标题。 */
const requestTitle = ref('');
/** 采购申请金额，未输入时保持空值以展示移动端输入占位提示。 */
const totalAmount = ref<number>();
/** 采购申请原因。 */
const requestReason = ref('');
/** 已创建申请单号。 */
const createdRequestNo = ref('');
/** 提交审批返回结果。 */
const submitResult = ref<MobilePurchaseSubmitResult>();
/** 提交中状态。 */
const submitting = ref(false);
/** 表单内联错误提示。 */
const formError = ref('');
/** 是否允许提交已创建的采购申请。 */
const canSubmitRequest = computed(() => Boolean(createdRequestNo.value) && !submitResult.value);
/** 当前申请阶段展示文本。 */
const requestStageText = computed(() => {
  if (submitResult.value) {
    return '已提交';
  }
  if (createdRequestNo.value) {
    return '待提交';
  }
  return '待创建';
});
/** 申请金额展示。 */
const amountText = computed(() => (Number.isFinite(totalAmount.value) && totalAmount.value ? `￥${totalAmount.value}` : '￥0'));
/** 移动端金额快捷选择。 */
const amountPresets = [1000, 5000, 10000];
/** 移动端申请原因快捷模板。 */
const reasonTemplates = ['日常办公采购', '项目实施采购', '设备耗材补充'];

watch([requestNo, requestTitle, totalAmount, requestReason], () => {
  if (!createdRequestNo.value) {
    return;
  }
  createdRequestNo.value = '';
  submitResult.value = undefined;
  formError.value = '表单内容已变更，请重新创建采购申请后再提交审批';
});

/**
 * 生成移动端采购申请单号。
 */
function generateRequestNo(): void {
  const now = new Date();
  const datePart = [
    now.getFullYear(),
    String(now.getMonth() + 1).padStart(2, '0'),
    String(now.getDate()).padStart(2, '0'),
  ].join('');
  const timePart = [
    String(now.getHours()).padStart(2, '0'),
    String(now.getMinutes()).padStart(2, '0'),
    String(now.getSeconds()).padStart(2, '0'),
  ].join('');
  requestNo.value = `PR${datePart}${timePart}`;
  formError.value = '';
}

/**
 * 应用金额快捷项。
 *
 * @param amount 采购申请金额
 */
function applyAmountPreset(amount: number): void {
  totalAmount.value = amount;
  formError.value = '';
}

/**
 * 应用采购申请原因模板。
 *
 * @param reason 采购申请原因
 */
function applyReasonTemplate(reason: string): void {
  requestReason.value = reason;
  formError.value = '';
}

/**
 * 创建移动端采购申请。
 */
async function createRequest(): Promise<void> {
  const validateMessage = validateRequestForm();
  if (validateMessage) {
    formError.value = validateMessage;
    return;
  }
  submitting.value = true;
  formError.value = '';
  try {
    const userContext = getMobileUserContext();
    const currentUserId = requireMobileUserId(userContext);
    const currentOrgId = requireMobileOrgId(userContext);
    const tenantId = requireMobileTenantId(userContext);
    createdRequestNo.value = await createMobilePurchaseRequest({
      tenantId,
      requestNo: requestNo.value.trim(),
      requestTitle: requestTitle.value.trim(),
      applicantId: currentUserId,
      orgId: currentOrgId,
      totalAmount: totalAmount.value as number,
      requestReason: requestReason.value.trim(),
    });
    submitResult.value = undefined;
    showMobileToast('采购申请已创建', 'success');
  } catch (error) {
    formError.value = `创建失败：${resolveErrorMessage(error)}`;
  } finally {
    submitting.value = false;
  }
}

/**
 * 提交采购申请进入审批。
 */
async function submitRequest(): Promise<void> {
  if (!createdRequestNo.value) {
    formError.value = '请先创建采购申请';
    return;
  }
  const confirmed = await showConfirm('提交审批', '二次确认：确定提交当前采购申请进入审批？');
  if (!confirmed) {
    return;
  }
  submitting.value = true;
  formError.value = '';
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    submitResult.value = await submitMobilePurchaseRequest(createdRequestNo.value);
    showMobileToast('已提交审批', 'success');
  } catch (error) {
    formError.value = `提交失败：${resolveErrorMessage(error)}`;
  } finally {
    submitting.value = false;
  }
}

/**
 * 校验采购申请表单。
 *
 * @returns 校验失败提示，返回空字符串表示通过
 */
function validateRequestForm(): string {
  if (!requestNo.value.trim()) {
    return '请输入采购申请单号';
  }
  if (!requestTitle.value.trim()) {
    return '请输入申请标题';
  }
  if (!Number.isFinite(totalAmount.value) || totalAmount.value <= 0) {
    return '请输入大于 0 的申请金额';
  }
  if (!requestReason.value.trim()) {
    return '请输入申请原因';
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
.request-form-page {
  background: linear-gradient(180deg, #f6faff 0%, #f4f7fb 48%, #f8fafc 100%);
}

.request-hero {
  border-color: #d8e8ff;
  background: linear-gradient(145deg, #ffffff 0%, #f3f8ff 58%, #f8fbff 100%);
}

.request-summary-card {
  display: flex;
  align-items: center;
  gap: 18rpx;
  margin-bottom: 18rpx;
  padding: 24rpx;
  border: 1rpx solid #dbeafe;
  border-radius: 24rpx;
  background: linear-gradient(145deg, #ffffff 0%, #f6fbff 100%);
  box-shadow: 0 12rpx 30rpx rgba(15, 23, 42, 0.04);
}

.request-summary-main {
  flex-shrink: 0;
  min-width: 190rpx;
}

.request-summary-label {
  color: #64748b;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1.25;
}

.request-summary-value {
  margin-top: 8rpx;
  color: #0f172a;
  font-size: 42rpx;
  font-weight: 900;
  line-height: 1;
}

.request-summary-side {
  min-width: 0;
  flex: 1;
  text-align: right;
}

.request-summary-title {
  color: #111827;
  font-size: 26rpx;
  font-weight: 900;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.request-summary-no {
  margin-top: 8rpx;
  color: #64748b;
  font-size: 22rpx;
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.request-progress-card {
  display: flex;
  align-items: center;
  margin-bottom: 18rpx;
  padding: 18rpx;
  border: 1rpx solid #edf2f8;
  border-radius: 22rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 28rpx rgba(15, 23, 42, 0.035);
}

.request-step {
  flex-shrink: 0;
  min-height: 50rpx;
  padding: 0 18rpx;
  border: 1rpx solid #e5edf7;
  border-radius: 999rpx;
  color: #64748b;
  background: #f8fafc;
  font-size: 23rpx;
  font-weight: 800;
  line-height: 50rpx;
  text-align: center;
}

.request-step-line {
  flex: 1;
  height: 2rpx;
  min-width: 28rpx;
  background: #dbe7f7;
}

.request-step.active {
  border-color: #b7ebc6;
  color: #047857;
  background: #ecfdf5;
}

.request-step.done {
  border-color: #86efac;
}

.result-title {
  margin-bottom: 0;
}

.result-heading {
  align-items: center;
  margin-bottom: 12rpx;
}

.result-card {
  border-color: #d7f5df;
}

.form-inline-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.inline-input {
  min-width: 0;
  flex: 1;
}

.inline-button {
  flex-shrink: 0;
  min-width: 104rpx;
  min-height: 76rpx;
  padding: 0 18rpx;
  border: 1rpx solid #bfd4f2;
  border-radius: 18rpx;
  color: #0969da;
  background: #eff6ff;
  font-size: 24rpx;
  font-weight: 800;
  line-height: 76rpx;
}

.quick-chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 14rpx;
}

.quick-chip {
  min-height: 50rpx;
  padding: 0 18rpx;
  border: 1rpx solid #d8e2ee;
  border-radius: 999rpx;
  color: #52627a;
  background: #ffffff;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 50rpx;
}

.quick-chip.active {
  border-color: #1677ff;
  color: #1677ff;
  background: #eff6ff;
}
</style>
