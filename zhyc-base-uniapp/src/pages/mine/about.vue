<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page about-page mobile-bottom-safe">
    <MobilePageTopBar title="关于系统" eyebrow="系统信息" fallback-url="/pages/profile/index" />
    <view class="mobile-hero compact-hero about-hero">
      <view class="mobile-hero-kicker">系统信息</view>
      <view class="mobile-title">平台说明</view>
      <view class="mobile-subtitle">查看平台版本、技术基线和移动端能力范围。</view>
    </view>

    <view class="about-brand-card">
      <view class="about-logo mobile-solid-icon icon-info"></view>
      <view class="about-main">
        <view class="about-title">ZHYC 快速开发平台</view>
        <view class="about-desc">企业级低代码、流程、开放平台与移动办公基础框架。</view>
      </view>
    </view>

    <view class="about-capability-grid">
      <view
        v-for="item in capabilityItems"
        :key="item.title"
        class="about-capability-card"
      >
        <view :class="['about-capability-icon', 'mobile-solid-icon', `tone-${item.tone}`, `icon-${item.icon}`]"></view>
        <view class="about-capability-title">{{ item.title }}</view>
        <view class="about-capability-desc">{{ item.description }}</view>
      </view>
    </view>

    <view class="mobile-field-card">
      <view class="mobile-card-title section-heading">版本信息</view>
      <view class="mobile-field-row">
        <text class="mobile-field-label">平台名称</text>
        <text class="mobile-field-value">ZHYC 快速开发平台</text>
      </view>
      <view class="mobile-field-row">
        <text class="mobile-field-label">后端基线</text>
        <text class="mobile-field-value">Spring Boot 4</text>
      </view>
      <view class="mobile-field-row">
        <text class="mobile-field-label">权限基线</text>
        <text class="mobile-field-value">Shiro 2.2.1</text>
      </view>
      <view class="mobile-field-row">
        <text class="mobile-field-label">移动端</text>
        <text class="mobile-field-value">uni-app</text>
      </view>
      <view class="mobile-field-row">
        <text class="mobile-field-label">版本阶段</text>
        <text class="mobile-field-value">首期企业平台版</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import MobilePageTopBar from '@/components/MobilePageTopBar.vue';

/**
 * 关于系统页面。
 *
 * <p>展示移动端首期版本和核心技术基线，便于问题定位和等保测评资料核对。</p>
 */
interface CapabilityItem {
  /** 能力标题。 */
  title: string;
  /** 能力说明。 */
  description: string;
  /** 图标色系。 */
  tone: 'blue' | 'green' | 'orange' | 'purple';
  /** 面性图标语义。 */
  icon: 'task' | 'flow' | 'send' | 'device';
}

/** 平台移动端能力摘要。 */
const capabilityItems: CapabilityItem[] = [
  { title: '低代码', description: '建模生成', tone: 'blue', icon: 'task' },
  { title: '流程', description: '移动审批', tone: 'green', icon: 'flow' },
  { title: '开放', description: 'API 门户', tone: 'purple', icon: 'send' },
  { title: '移动', description: '跨端适配', tone: 'orange', icon: 'device' },
];
</script>

<style scoped>
.about-page {
  background: linear-gradient(180deg, #f6faff 0%, #f4f7fb 48%, #f8fafc 100%);
}

.about-hero {
  border-color: #d8e8ff;
  background: linear-gradient(145deg, #ffffff 0%, #f3f8ff 58%, #f8fbff 100%);
}

.about-brand-card {
  display: flex;
  align-items: center;
  gap: 18rpx;
  margin-bottom: 22rpx;
  padding: 24rpx;
  border: 1rpx solid #dbeafe;
  border-radius: 26rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 30rpx rgba(15, 23, 42, 0.04);
}

.about-logo {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 82rpx;
  height: 82rpx;
  border-radius: 28rpx;
  color: #ffffff;
  background: linear-gradient(135deg, #1677ff 0%, #14b8a6 100%);
  font-size: 34rpx;
  font-weight: 900;
  line-height: 1;
}

.about-main {
  min-width: 0;
  flex: 1;
}

.about-title {
  color: #111827;
  font-size: 30rpx;
  font-weight: 900;
  line-height: 1.35;
}

.about-desc {
  margin-top: 8rpx;
  color: #64748b;
  font-size: 24rpx;
  line-height: 1.45;
}

.about-capability-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx;
  margin-bottom: 22rpx;
}

.about-capability-card {
  min-width: 0;
  padding: 20rpx;
  border: 1rpx solid #edf2f8;
  border-radius: 22rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 26rpx rgba(15, 23, 42, 0.035);
}

.about-capability-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58rpx;
  height: 58rpx;
  margin-bottom: 14rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 900;
  line-height: 1;
}

.about-capability-title {
  color: #111827;
  font-size: 26rpx;
  font-weight: 900;
  line-height: 1.25;
}

.about-capability-desc {
  margin-top: 6rpx;
  color: #64748b;
  font-size: 22rpx;
  line-height: 1.35;
}

.tone-blue {
  color: #0969da;
  background: #eff6ff;
}

.tone-green {
  color: #047857;
  background: #ecfdf5;
}

.tone-orange {
  color: #b45309;
  background: #fff7ed;
}

.tone-purple {
  color: #6d28d9;
  background: #f5f3ff;
}

.section-heading {
  margin-bottom: 12rpx;
}
</style>
