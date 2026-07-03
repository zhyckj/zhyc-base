<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page profile-page mobile-bottom-safe">
    <view class="profile-topbar">
      <view>
        <view class="profile-eyebrow">我的</view>
        <view class="profile-top-title">个人中心</view>
      </view>
      <button class="profile-top-action" @tap="openMenu('/pages/mine/tenant-switch')">切租户</button>
    </view>

    <view class="mobile-profile-card profile-card">
      <view class="profile-header">
        <view class="profile-user">
          <view class="profile-avatar mobile-solid-icon icon-user"></view>
          <view class="profile-user-main">
            <view class="profile-name">{{ userContext.accountName || '未登录' }}</view>
            <view class="profile-tenant">租户 {{ userContext.tenantId || '-' }}</view>
          </view>
        </view>
        <view :class="['profile-status', { offline: !userContext.loggedIn }]">
          {{ userContext.loggedIn ? '在线' : '未登录' }}
        </view>
      </view>
      <view class="profile-meta">
        <view class="profile-meta-item">
          <view class="profile-meta-label">组织</view>
          <view class="profile-meta-value">{{ userContext.orgId ?? '-' }}</view>
        </view>
        <view class="profile-meta-item">
          <view class="profile-meta-label">角色</view>
          <view class="profile-meta-value">{{ userContext.roleName || '-' }}</view>
        </view>
        <view class="profile-meta-item">
          <view class="profile-meta-label">运行端</view>
          <view class="profile-meta-value">{{ runtime.platform }}</view>
        </view>
      </view>
      <view class="profile-insight-strip">
        <view class="profile-insight-item">
          <view class="profile-insight-label">登录状态</view>
          <view class="profile-insight-value">{{ loginStateText }}</view>
        </view>
        <view class="profile-insight-item">
          <view class="profile-insight-label">租户状态</view>
          <view class="profile-insight-value">{{ tenantStateText }}</view>
        </view>
        <view class="profile-insight-item">
          <view class="profile-insight-label">终端宽度</view>
          <view class="profile-insight-value">{{ runtime.windowWidth || '-' }}</view>
        </view>
      </view>
      <view class="profile-primary-actions">
        <button class="profile-primary-button" @tap="openMenu('/pages/mine/tenant-switch')">切换租户</button>
        <button class="profile-secondary-button" @tap="openMenu('/pages/mine/change-password')">修改密码</button>
      </view>
    </view>

    <view class="profile-security-card">
      <view class="profile-security-main">
        <view class="profile-security-title">账号安全状态</view>
        <view class="profile-security-desc">
          {{ userContext.loggedIn ? '已读取当前移动端账号上下文，可进行租户切换和安全设置。' : '未检测到移动端登录上下文，请先登录后再操作账号能力。' }}
        </view>
      </view>
      <view :class="['profile-security-badge', { offline: !userContext.loggedIn }]">
        {{ userContext.loggedIn ? '正常' : '未登录' }}
      </view>
    </view>

    <view
      v-for="group in profileMenuGroups"
      :key="group.title"
      class="mobile-section"
    >
      <view class="mobile-section-header">
        <view>
          <view class="mobile-section-title">{{ group.title }}</view>
          <view class="profile-section-desc">{{ group.description }}</view>
        </view>
      </view>
      <view v-if="group.layout === 'grid'" class="profile-action-grid">
        <view
          v-for="item in group.items"
          :key="item.url"
          class="profile-action-card"
          hover-class="profile-action-card-hover"
          @tap="openMenu(item.url)"
        >
          <view :class="['profile-action-icon', 'mobile-solid-icon', `tone-${item.tone}`, `icon-${item.icon}`]"></view>
          <view class="profile-action-title">{{ item.title }}</view>
          <view class="profile-action-desc">{{ item.description }}</view>
        </view>
      </view>
      <template v-else>
        <view
          v-for="item in group.items"
          :key="item.url"
          class="mobile-list-card menu-row"
          hover-class="menu-row-hover"
          @tap="openMenu(item.url)"
        >
          <view :class="['menu-row-icon', 'mobile-solid-icon', `tone-${item.tone}`, `icon-${item.icon}`]"></view>
          <view class="mobile-card-row-main">
            <view class="mobile-card-title">{{ item.title }}</view>
            <view class="mobile-card-desc">{{ item.description }}</view>
          </view>
          <view class="menu-arrow">›</view>
        </view>
      </template>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onShow } from '@dcloudio/uni-app';
import { computed, ref } from 'vue';

import {
  getMobilePlatformRuntime,
  getMobileUserContext,
  openMobilePage,
  redirectToMobileLogin,
  type MobilePlatformRuntime,
  type MobileUserContext,
} from '@/utils/platform';
import type { MobileShortcut } from '@/types/platform';

/**
 * 个人中心功能入口。
 */
interface ProfileMenuItem extends MobileShortcut {
  /** 入口图标色系。 */
  tone: 'blue' | 'green' | 'orange' | 'purple' | 'cyan';
  /** 面性图标语义。 */
  icon: 'user' | 'lock' | 'tenant' | 'device' | 'info';
}

/**
 * 个人中心入口分组。
 */
interface ProfileMenuGroup {
  /** 分组标题。 */
  title: string;
  /** 分组说明。 */
  description: string;
  /** 分组布局，主功能使用宫格，辅助功能使用列表。 */
  layout: 'grid' | 'list';
  /** 分组下的功能入口。 */
  items: ProfileMenuItem[];
}

/** 当前移动端运行环境。 */
const runtime = ref<MobilePlatformRuntime>({
  platform: 'unknown',
  h5: false,
  miniProgram: false,
  app: false,
  windowWidth: 0,
});

/** 当前移动端用户上下文。 */
const userContext = ref<MobileUserContext>(getMobileUserContext());

/** 当前登录状态文案。 */
const loginStateText = computed(() => (userContext.value.loggedIn ? '已登录' : '未登录'));
/** 当前租户状态文案。 */
const tenantStateText = computed(() => (userContext.value.tenantId ? '已绑定' : '待绑定'));
/** 当前运行端状态文案。 */
const runtimeStateText = computed(() => (runtime.value.h5 ? 'H5' : runtime.value.miniProgram ? '小程序' : runtime.value.app ? 'App' : '未知'));

/** 我的页面功能分组。 */
const profileMenuGroups = computed<ProfileMenuGroup[]>(() => [
  {
    title: '账号与安全',
    description: '处理个人资料、密码、租户和当前终端。',
    layout: 'grid',
    items: [
      { title: '个人信息', url: '/pages/mine/index', description: '账号资料', tone: 'blue', icon: 'user' },
      { title: '修改密码', url: '/pages/mine/change-password', description: '账号安全', tone: 'orange', icon: 'lock' },
      { title: '租户切换', url: '/pages/mine/tenant-switch', description: '业务上下文', tone: 'green', icon: 'tenant' },
      { title: '登录设备', url: '/pages/mine/login-devices', description: runtimeStateText.value, tone: 'cyan', icon: 'device' },
    ],
  },
  {
    title: '系统服务',
    description: '查看平台版本、技术基线和移动端能力。',
    layout: 'list',
    items: [
      { title: '关于系统', url: '/pages/mine/about', description: '平台版本和技术基线', tone: 'purple', icon: 'info' },
    ],
  },
]);

/** 未登录时需要拦截到移动登录页的账号能力入口。 */
const protectedProfileMenuPaths = new Set([
  '/pages/mine/index',
  '/pages/mine/change-password',
  '/pages/mine/tenant-switch',
  '/pages/mine/login-devices',
]);

/**
 * 页面展示后读取跨端运行时信息。
 */
onShow(() => {
  runtime.value = getMobilePlatformRuntime();
  userContext.value = getMobileUserContext();
});

/**
 * 打开我的页面功能入口。
 *
 * @param url uni-app 页面地址
 */
function openMenu(url: string): void {
  if (protectedProfileMenuPaths.has(url) && !userContext.value.loggedIn) {
    redirectToMobileLogin('请先登录移动端账号', url);
    return;
  }
  openMobilePage(url);
}
</script>

<style scoped>
.profile-page {
  padding-top: calc(18rpx + env(safe-area-inset-top));
  padding-bottom: calc(132rpx + env(safe-area-inset-bottom));
}

.profile-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  min-height: 72rpx;
  margin-bottom: 18rpx;
}

.profile-eyebrow {
  color: #64748b;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1.25;
}

.profile-top-title {
  margin-top: 4rpx;
  color: #111827;
  font-size: 36rpx;
  font-weight: 900;
  line-height: 1.18;
}

.profile-top-action {
  flex-shrink: 0;
  min-width: 112rpx;
  min-height: 54rpx;
  padding: 0 18rpx;
  border: 1rpx solid #bfd4f2;
  border-radius: 999rpx;
  color: #0969da;
  background: #ffffff;
  font-size: 23rpx;
  font-weight: 800;
  line-height: 54rpx;
  box-shadow: 0 8rpx 20rpx rgba(15, 23, 42, 0.04);
}

.profile-card {
  margin-bottom: 26rpx;
}

.profile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.profile-user {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.profile-avatar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  color: #ffffff;
  background: linear-gradient(135deg, #1677ff 0%, #14b8a6 100%);
  box-shadow: 0 10rpx 22rpx rgba(22, 119, 255, 0.14);
  font-size: 32rpx;
  font-weight: 900;
  line-height: 1;
}

.profile-user-main {
  min-width: 0;
}

.profile-name {
  color: #0f172a;
  font-size: 34rpx;
  font-weight: 800;
  line-height: 1.2;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-tenant {
  margin-top: 6rpx;
  color: #64748b;
  font-size: 22rpx;
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-status {
  flex-shrink: 0;
  min-height: 40rpx;
  padding: 0 14rpx;
  border-radius: 999rpx;
  color: #047857;
  background: #ecfdf5;
  font-size: 21rpx;
  font-weight: 800;
  line-height: 40rpx;
}

.profile-status.offline {
  color: #dc2626;
  background: #fff1f0;
}

.profile-meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12rpx;
  margin-top: 28rpx;
}

.profile-meta-item {
  min-height: 96rpx;
  padding: 16rpx;
  border-radius: 14rpx;
  border: 1rpx solid #e5edf7;
  background: rgba(248, 251, 255, 0.92);
}

.profile-meta-label {
  color: #64748b;
  font-size: 22rpx;
}

.profile-meta-value {
  margin-top: 8rpx;
  color: #0f172a;
  font-size: 26rpx;
  font-weight: 700;
  word-break: break-word;
}

.profile-insight-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10rpx;
  margin-top: 16rpx;
}

.profile-insight-item {
  min-width: 0;
  padding: 14rpx 12rpx;
  border: 1rpx solid #e5edf7;
  border-radius: 16rpx;
  background: rgba(248, 251, 255, 0.92);
}

.profile-insight-label {
  color: #64748b;
  font-size: 20rpx;
  font-weight: 700;
  line-height: 1.2;
}

.profile-insight-value {
  margin-top: 6rpx;
  color: #0f172a;
  font-size: 24rpx;
  font-weight: 900;
  line-height: 1.2;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-primary-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12rpx;
  margin-top: 18rpx;
}

.profile-primary-button,
.profile-secondary-button {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  min-height: 64rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 900;
  line-height: 64rpx;
}

.profile-primary-button {
  color: #ffffff;
  background: #1677ff;
  box-shadow: 0 10rpx 22rpx rgba(22, 119, 255, 0.18);
}

.profile-secondary-button {
  border: 1rpx solid #bfd4f2;
  color: #0969da;
  background: #ffffff;
}

.profile-security-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 24rpx;
  padding: 22rpx;
  border: 1rpx solid #dbeafe;
  border-radius: 22rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 28rpx rgba(15, 23, 42, 0.035);
}

.profile-security-main {
  min-width: 0;
  flex: 1;
}

.profile-security-title {
  color: #111827;
  font-size: 28rpx;
  font-weight: 900;
  line-height: 1.25;
}

.profile-security-desc {
  margin-top: 8rpx;
  color: #64748b;
  font-size: 23rpx;
  line-height: 1.45;
}

.profile-security-badge {
  flex-shrink: 0;
  min-height: 46rpx;
  padding: 0 16rpx;
  border-radius: 999rpx;
  color: #047857;
  background: #ecfdf5;
  font-size: 22rpx;
  font-weight: 900;
  line-height: 46rpx;
}

.profile-security-badge.offline {
  color: #dc2626;
  background: #fff1f0;
}

.profile-section-desc {
  margin-top: 6rpx;
  color: #64748b;
  font-size: 22rpx;
  line-height: 1.35;
}

.profile-action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.profile-action-card {
  min-width: 0;
  min-height: 168rpx;
  padding: 22rpx;
  border: 1rpx solid #edf2f8;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 26rpx rgba(15, 23, 42, 0.035);
}

.profile-action-card-hover {
  background: #f8fbff;
}

.profile-action-icon,
.menu-row-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58rpx;
  height: 58rpx;
  border-radius: 22rpx;
  font-size: 24rpx;
  font-weight: 900;
  line-height: 1;
}

.profile-action-title {
  margin-top: 16rpx;
  color: #111827;
  font-size: 28rpx;
  font-weight: 900;
  line-height: 1.25;
}

.profile-action-desc {
  margin-top: 6rpx;
  color: #64748b;
  font-size: 22rpx;
  line-height: 1.35;
}

.compact-header {
  min-height: 46rpx;
}

.menu-row {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.menu-row-hover {
  background: #f8fafc;
}

.menu-arrow {
  flex-shrink: 0;
  color: #94a3b8;
  font-size: 38rpx;
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

.tone-cyan {
  color: #0e7490;
  background: #ecfeff;
}
</style>
