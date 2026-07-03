<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page-topbar">
    <button v-if="showBack" class="mobile-page-back" @tap="goBack">
      <view class="mobile-page-back-mark"></view>
    </button>
    <view class="mobile-page-title-wrap">
      <view v-if="eyebrow" class="mobile-page-eyebrow">{{ eyebrow }}</view>
      <view class="mobile-page-title">{{ title }}</view>
    </view>
    <button
      v-if="actionText"
      class="mobile-page-top-action"
      :disabled="actionDisabled"
      @tap="emit('action')"
    >
      {{ actionText }}
    </button>
    <view v-else class="mobile-page-action-placeholder"></view>
  </view>
</template>

<script setup lang="ts">
/**
 * 移动端页面自定义头部。
 *
 * <p>用于替换 H5 默认导航条，统一二级功能页返回、标题和右侧轻量动作。</p>
 */
interface MobilePageTopBarProps {
  /** 页面主标题。 */
  title: string;
  /** 标题上方的业务归属短文案。 */
  eyebrow?: string;
  /** 是否展示返回按钮。 */
  showBack?: boolean;
  /** 右侧动作文字。 */
  actionText?: string;
  /** 右侧动作是否禁用。 */
  actionDisabled?: boolean;
  /** 无页面栈时的兜底返回地址。 */
  fallbackUrl?: string;
}

const props = withDefaults(defineProps<MobilePageTopBarProps>(), {
  eyebrow: '',
  showBack: true,
  actionText: '',
  actionDisabled: false,
  fallbackUrl: '/pages/workbench/index',
});

const emit = defineEmits<{
  (event: 'action'): void;
}>();

/**
 * 返回上一页。
 *
 * <p>H5 直接打开二级页时可能没有页面栈，此时回到工作台，避免返回按钮无效。</p>
 */
function goBack(): void {
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack();
    return;
  }
  uni.switchTab({
    url: props.fallbackUrl,
    fail: () => {
      uni.reLaunch({ url: props.fallbackUrl });
    },
  });
}
</script>

<style scoped>
.mobile-page-topbar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  min-height: 76rpx;
  padding: calc(8rpx + env(safe-area-inset-top)) 0 16rpx;
}

.mobile-page-back,
.mobile-page-action-placeholder,
.mobile-page-top-action {
  flex-shrink: 0;
}

.mobile-page-back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58rpx;
  height: 58rpx;
  padding: 0;
  border-radius: 50%;
  color: #0f172a;
  background: rgba(255, 255, 255, 0.72);
}

.mobile-page-back-mark {
  width: 0;
  height: 0;
  border-top: 14rpx solid transparent;
  border-bottom: 14rpx solid transparent;
  border-right: 20rpx solid #0f172a;
  transform: translateX(-2rpx);
}

.mobile-page-title-wrap {
  min-width: 0;
  flex: 1;
}

.mobile-page-eyebrow {
  color: #64748b;
  font-size: 21rpx;
  font-weight: 700;
  line-height: 1.2;
}

.mobile-page-title {
  margin-top: 4rpx;
  color: #0f172a;
  font-size: 32rpx;
  font-weight: 900;
  line-height: 1.18;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mobile-page-top-action {
  min-width: 96rpx;
  min-height: 52rpx;
  padding: 0 18rpx;
  border: 1rpx solid #bfd4f2;
  border-radius: 999rpx;
  color: #0969da;
  background: #ffffff;
  font-size: 22rpx;
  font-weight: 900;
  line-height: 52rpx;
  box-shadow: 0 8rpx 18rpx rgba(15, 23, 42, 0.04);
}

.mobile-page-top-action[disabled] {
  color: #94a3b8;
  background: #f8fafc;
}

.mobile-page-action-placeholder {
  width: 58rpx;
  height: 58rpx;
}
</style>
