<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page devices-page mobile-bottom-safe">
    <MobilePageTopBar title="登录设备" eyebrow="账号安全" fallback-url="/pages/profile/index" />
    <view class="mobile-hero compact-hero devices-hero">
      <view class="mobile-hero-kicker">账号安全</view>
      <view class="mobile-title">运行端识别</view>
      <view class="mobile-subtitle">当前仅展示本设备运行端，用于定位 H5、小程序或 App 环境差异。</view>
    </view>

    <view class="device-status-card">
      <view class="device-title-row">
        <view class="device-icon">端</view>
        <view class="device-title-main">
          <view class="mobile-card-title">当前设备</view>
          <view class="mobile-card-desc">平台：{{ runtime.platform }}</view>
        </view>
      </view>
      <view class="mobile-status-chip success">在线</view>
    </view>

    <view class="mobile-field-card">
      <view class="mobile-card-title section-heading">运行端检测</view>
      <view class="runtime-grid">
        <view class="runtime-chip" :class="{ active: runtime.h5 }">H5</view>
        <view class="runtime-chip" :class="{ active: runtime.miniProgram }">小程序</view>
        <view class="runtime-chip" :class="{ active: runtime.app }">App</view>
      </view>
      <view class="device-tip">
        {{ runtimeTip }}
      </view>
      <view class="mobile-field-row">
        <view class="mobile-field-label">窗口宽度</view>
        <view class="mobile-field-value">{{ runtime.windowWidth }}px</view>
      </view>
      <view class="mobile-field-row">
        <view class="mobile-field-label">H5</view>
        <view class="mobile-field-value">{{ runtime.h5 ? '是' : '否' }}</view>
      </view>
      <view class="mobile-field-row">
        <view class="mobile-field-label">小程序</view>
        <view class="mobile-field-value">{{ runtime.miniProgram ? '是' : '否' }}</view>
      </view>
      <view class="mobile-field-row">
        <view class="mobile-field-label">App</view>
        <view class="mobile-field-value">{{ runtime.app ? '是' : '否' }}</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onShow } from '@dcloudio/uni-app';
import { computed, onMounted, ref } from 'vue';

import MobilePageTopBar from '@/components/MobilePageTopBar.vue';
import {
  getMobilePlatformRuntime,
  getMobileUserContext,
  redirectToMobileLogin,
  type MobilePlatformRuntime,
} from '@/utils/platform';

/** 当前登录设备对应的 uni-app 运行时信息。 */
const runtime = ref<MobilePlatformRuntime>({
  platform: 'unknown',
  h5: false,
  miniProgram: false,
  app: false,
  windowWidth: 0,
});

/** 当前运行端提示。 */
const runtimeTip = computed(() => {
  if (runtime.value.h5) {
    return '当前运行在 H5 预览端，适合检查页面布局和接口联调。';
  }
  if (runtime.value.miniProgram) {
    return '当前运行在小程序端，请重点关注授权、存储和网络差异。';
  }
  if (runtime.value.app) {
    return '当前运行在 App 端，请重点关注安全区、沉浸式状态栏和原生能力。';
  }
  return '暂未识别运行端，请刷新后重试。';
});

/**
 * 页面挂载后读取设备运行时信息。
 */
onMounted(() => {
  runtime.value = getMobilePlatformRuntime();
});

onShow(() => {
  const userContext = getMobileUserContext();
  if (!userContext.loggedIn) {
    redirectToMobileLogin('请先登录移动端账号', '/pages/mine/login-devices');
  }
});
</script>

<style scoped>
.devices-page {
  background: linear-gradient(180deg, #f6faff 0%, #f4f7fb 48%, #f8fafc 100%);
}

.devices-hero {
  border-color: #d8e8ff;
  background: linear-gradient(145deg, #ffffff 0%, #f3f8ff 58%, #f8fbff 100%);
}

.device-status-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 22rpx;
  padding: 24rpx;
  border: 1rpx solid #dbeafe;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 28rpx rgba(15, 23, 42, 0.035);
}

.device-title-row {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.device-title-main {
  min-width: 0;
}

.device-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  border-radius: 24rpx;
  color: #0969da;
  background: #eff6ff;
  font-size: 26rpx;
  font-weight: 900;
  line-height: 1;
}

.runtime-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10rpx;
  margin: 18rpx 0 8rpx;
}

.runtime-chip {
  min-height: 56rpx;
  border: 1rpx solid #e5edf7;
  border-radius: 999rpx;
  color: #64748b;
  background: #f8fafc;
  font-size: 23rpx;
  font-weight: 800;
  line-height: 56rpx;
  text-align: center;
}

.runtime-chip.active {
  border-color: #b7d4ff;
  color: #0969da;
  background: #eff6ff;
}

.device-tip {
  margin: 16rpx 0 8rpx;
  padding: 16rpx 18rpx;
  border-radius: 18rpx;
  color: #52627a;
  background: #f8fafc;
  font-size: 23rpx;
  line-height: 1.45;
}

.section-heading {
  margin-bottom: 12rpx;
}
</style>
