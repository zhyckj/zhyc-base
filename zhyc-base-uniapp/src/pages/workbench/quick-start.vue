<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page quick-start-page mobile-bottom-safe">
    <MobilePageTopBar title="快捷发起" eyebrow="移动办公" />
    <view class="mobile-hero compact-hero quick-hero">
      <view class="mobile-hero-kicker">移动办公</view>
      <view class="mobile-title">选择事项</view>
      <view class="mobile-subtitle">当前租户：{{ tenantText }}，选择事项后先校验移动端账号和租户上下文。</view>
      <view v-if="errorMessage" class="mobile-form-alert quick-alert">{{ errorMessage }}</view>
      <view class="quick-step-grid">
        <view
          v-for="(step, index) in quickSteps"
          :key="step"
          :class="['quick-step-card', { active: index === 0 }]"
        >
          <view class="quick-step-index">{{ index + 1 }}</view>
          <view class="quick-step-text">{{ step }}</view>
        </view>
      </view>
    </view>

    <view class="quick-summary-card">
      <view>
        <view class="quick-summary-label">可发起事项</view>
        <view class="quick-summary-value">{{ startItems.length }}</view>
      </view>
      <view class="quick-summary-desc">首期聚焦采购申请，后续低代码事项会自动进入这里。</view>
    </view>

    <view class="mobile-section">
      <view class="mobile-section-header compact-header">
        <view class="mobile-section-title">可发起业务</view>
      </view>
      <view class="start-card-grid">
        <view
          v-for="item in startItems"
          :key="item.url"
          class="start-action-card"
          hover-class="start-action-card-hover"
          @tap="openStartItem(item)"
        >
          <view class="start-card-head">
            <view :class="['start-icon', 'mobile-solid-icon', `tone-${item.tone}`, `icon-${item.icon}`]"></view>
            <view class="start-button">发起</view>
          </view>
          <view class="start-main">
            <view class="start-title">{{ item.title }}</view>
            <view class="start-desc">{{ item.description }}</view>
            <view class="start-meta">{{ item.meta }}</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';

import MobilePageTopBar from '@/components/MobilePageTopBar.vue';
import type { MobileShortcut } from '@/types/platform';
import { getMobileUserContext, requireMobileTenantId, requireMobileUserId } from '@/utils/platform';

/**
 * 快捷发起入口。
 */
interface QuickStartItem extends MobileShortcut {
  /** 入口图标色系。 */
  tone: 'blue' | 'green' | 'orange' | 'purple' | 'cyan';
  /** 面性图标语义。 */
  icon: 'send' | 'cart';
  /** 入口补充信息。 */
  meta: string;
}

/** 快捷发起错误提示。 */
const errorMessage = ref('');

/** 当前租户展示文本。 */
const tenantText = computed(() => getMobileUserContext().tenantId || '未登录');

/** 快捷发起步骤，使用紧凑卡片表达移动端任务流。 */
const quickSteps = ['选择事项', '填写表单', '提交审批'];

/** 首期支持的快捷发起入口。 */
const startItems: QuickStartItem[] = [
  {
    title: '发起采购申请',
    url: '/pages/purchase/request-form',
    description: '创建采购申请并提交审批',
    tone: 'green',
    icon: 'cart',
    meta: '采购中心 · 审批流转',
  },
];

/**
 * 打开快捷发起入口。
 *
 * <p>进入具体业务页面前先校验移动端用户和租户上下文，避免未登录状态创建无租户业务数据。</p>
 *
 * @param item 快捷发起入口
 */
function openStartItem(item: QuickStartItem): void {
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    errorMessage.value = '';
    uni.navigateTo({ url: item.url });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '快捷发起失败';
  }
}
</script>

<style scoped>
.quick-start-page {
  background: linear-gradient(180deg, #f6faff 0%, #f4f7fb 48%, #f8fafc 100%);
}

.quick-hero {
  border-color: #d8e8ff;
  background: linear-gradient(145deg, #ffffff 0%, #f3f8ff 58%, #f8fbff 100%);
}

.compact-header {
  min-height: 46rpx;
}

.quick-alert {
  background: rgba(255, 255, 255, 0.92);
}

.quick-step-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10rpx;
  margin-top: 20rpx;
}

.quick-step-card {
  min-width: 0;
  min-height: 82rpx;
  padding: 12rpx 8rpx;
  border: 1rpx solid #e5edf7;
  border-radius: 18rpx;
  background: rgba(255, 255, 255, 0.86);
  text-align: center;
}

.quick-step-card.active {
  border-color: #b7d4ff;
  background: #eff6ff;
}

.quick-step-index {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32rpx;
  height: 32rpx;
  margin: 0 auto 8rpx;
  border-radius: 50%;
  color: #64748b;
  background: #eef4fb;
  font-size: 19rpx;
  font-weight: 900;
  line-height: 1;
}

.quick-step-card.active .quick-step-index {
  color: #ffffff;
  background: #1677ff;
}

.quick-step-text {
  color: #64748b;
  font-size: 21rpx;
  font-weight: 800;
  line-height: 1.2;
}

.quick-step-card.active .quick-step-text {
  color: #0969da;
}

.quick-summary-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  margin-bottom: 22rpx;
  padding: 22rpx 24rpx;
  border: 1rpx solid #edf2f8;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 28rpx rgba(15, 23, 42, 0.035);
}

.quick-summary-label {
  color: #64748b;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1.25;
}

.quick-summary-value {
  margin-top: 6rpx;
  color: #0f172a;
  font-size: 42rpx;
  font-weight: 900;
  line-height: 1;
}

.quick-summary-desc {
  min-width: 0;
  flex: 1;
  color: #64748b;
  font-size: 23rpx;
  line-height: 1.45;
  text-align: right;
}

.start-card-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16rpx;
}

.start-action-card {
  min-height: 184rpx;
  padding: 24rpx;
  border: 1rpx solid #edf2f8;
  border-radius: 26rpx;
  background: linear-gradient(145deg, #ffffff 0%, #f8fbff 100%);
  box-shadow: 0 10rpx 28rpx rgba(15, 23, 42, 0.035);
}

.start-action-card-hover {
  background: #f8fbff;
}

.start-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 18rpx;
}

.start-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 82rpx;
  height: 82rpx;
  border-radius: 28rpx;
  font-size: 32rpx;
  font-weight: 900;
  line-height: 1;
}

.start-main {
  min-width: 0;
}

.start-title {
  color: #111827;
  font-size: 30rpx;
  font-weight: 900;
  line-height: 1.3;
}

.start-desc {
  margin-top: 8rpx;
  color: #64748b;
  font-size: 24rpx;
  line-height: 1.45;
}

.start-meta {
  margin-top: 10rpx;
  color: #94a3b8;
  font-size: 22rpx;
  line-height: 1.35;
}

.start-button {
  min-width: 92rpx;
  height: 50rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  color: #0969da;
  background: #eff6ff;
  font-size: 23rpx;
  font-weight: 900;
  line-height: 50rpx;
  text-align: center;
}

.tone-green {
  color: #047857;
  background: #ecfdf5;
}
</style>
