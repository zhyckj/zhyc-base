<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page mine-page mobile-bottom-safe">
    <MobilePageTopBar
      title="账号资料"
      eyebrow="我的"
      action-text="切租户"
      @action="openPage('/pages/mine/tenant-switch')"
    />
    <view class="mobile-profile-card mine-card">
      <view class="profile-header">
        <view class="profile-user">
          <view class="profile-avatar mobile-solid-icon icon-user"></view>
          <view>
            <view class="mobile-subtitle">账号资料</view>
            <view class="profile-name">{{ userContext.accountName || '未登录' }}</view>
          </view>
        </view>
        <view :class="['profile-status', { offline: !userContext.loggedIn }]">
          {{ userContext.loggedIn ? '在线' : '未登录' }}
        </view>
      </view>
      <view class="profile-context-grid">
        <view class="profile-context-item">
          <view class="profile-context-label">租户</view>
          <view class="profile-context-value">{{ userContext.tenantId || '-' }}</view>
        </view>
        <view class="profile-context-item">
          <view class="profile-context-label">组织</view>
          <view class="profile-context-value">{{ userContext.orgId ?? '-' }}</view>
        </view>
        <view class="profile-context-item">
          <view class="profile-context-label">角色</view>
          <view class="profile-context-value">{{ userContext.roleName || '-' }}</view>
        </view>
      </view>
      <view class="profile-health-strip">
        <view
          v-for="item in profileHealthItems"
          :key="item.label"
          :class="['profile-health-pill', { warning: !item.ready }]"
        >
          {{ item.label }} {{ item.text }}
        </view>
      </view>
    </view>

    <view class="profile-shortcut-row">
      <view class="profile-shortcut-card" @tap="openPage('/pages/mine/tenant-switch')">
        <view class="profile-shortcut-icon mobile-solid-icon tone-green icon-tenant"></view>
        <view>
          <view class="profile-shortcut-title">切换租户</view>
          <view class="profile-shortcut-desc">调整业务上下文</view>
        </view>
      </view>
      <view class="profile-shortcut-card" @tap="openPage('/pages/mine/change-password')">
        <view class="profile-shortcut-icon mobile-solid-icon tone-orange icon-lock"></view>
        <view>
          <view class="profile-shortcut-title">修改密码</view>
          <view class="profile-shortcut-desc">保护账号安全</view>
        </view>
      </view>
    </view>

    <view class="profile-detail-card">
      <view class="profile-detail-head">
        <view>
          <view class="profile-detail-title">资料概览</view>
          <view class="profile-detail-desc">当前移动端账号上下文</view>
        </view>
        <view :class="['profile-detail-badge', { offline: !userContext.loggedIn }]">
          {{ userContext.loggedIn ? '已同步' : '待登录' }}
        </view>
      </view>
      <view class="profile-detail-grid">
        <view
          v-for="item in profileInfoItems"
          :key="item.label"
          class="profile-detail-item"
        >
          <view :class="['profile-detail-icon', 'mobile-solid-icon', `tone-${item.tone}`, `icon-${item.icon}`]"></view>
          <view class="profile-detail-label">{{ item.label }}</view>
          <view class="profile-detail-value">{{ item.value }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onShow } from '@dcloudio/uni-app';
import { computed, ref } from 'vue';

import MobilePageTopBar from '@/components/MobilePageTopBar.vue';
import {
  getMobileUserContext,
  openMobilePage,
  redirectToMobileLogin,
  type MobileUserContext,
} from '@/utils/platform';

/**
 * 个人资料概览项。
 */
interface ProfileInfoItem {
  /** 展示标签。 */
  label: string;
  /** 展示值。 */
  value: string;
  /** 图标色系。 */
  tone: 'blue' | 'green' | 'orange' | 'cyan';
  /** 面性图标语义。 */
  icon: 'user' | 'tenant' | 'flow' | 'lock';
}

/**
 * 个人资料状态摘要。
 */
interface ProfileHealthItem {
  /** 状态标签。 */
  label: string;
  /** 状态文案。 */
  text: string;
  /** 状态是否已满足。 */
  ready: boolean;
}

/** 我的页面。 */
const userContext = ref<MobileUserContext>(getMobileUserContext());

/** 个人资料状态摘要。 */
const profileHealthItems = computed<ProfileHealthItem[]>(() => [
  { label: '登录', text: userContext.value.loggedIn ? '正常' : '未登录', ready: userContext.value.loggedIn },
  { label: '租户', text: userContext.value.tenantId ? '已绑定' : '缺失', ready: Boolean(userContext.value.tenantId) },
  { label: '组织', text: userContext.value.orgId ? '已绑定' : '缺失', ready: userContext.value.orgId !== null },
]);
/** 个人资料概览项。 */
const profileInfoItems = computed<ProfileInfoItem[]>(() => [
  { label: '账号', value: userContext.value.accountName || '-', tone: 'blue', icon: 'user' },
  { label: '当前租户', value: userContext.value.tenantId || '-', tone: 'green', icon: 'tenant' },
  { label: '当前组织', value: userContext.value.orgId === null ? '-' : String(userContext.value.orgId), tone: 'cyan', icon: 'flow' },
  { label: '角色', value: userContext.value.roleName || '-', tone: 'orange', icon: 'lock' },
]);

/**
 * 页面展示时刷新用户上下文，确保登录、租户和角色变化后能及时展示。
 */
onShow(() => {
  userContext.value = getMobileUserContext();
  if (!userContext.value.loggedIn) {
    redirectToMobileLogin('请先登录移动端账号', '/pages/mine/index');
  }
});

/**
 * 打开账号资料相关页面。
 *
 * @param url 页面路径
 */
function openPage(url: string): void {
  if (!userContext.value.loggedIn) {
    redirectToMobileLogin('请先登录移动端账号', url);
    return;
  }
  openMobilePage(url);
}
</script>

<style scoped>
.mine-page {
  background: linear-gradient(180deg, #f6faff 0%, #f4f7fb 48%, #f8fafc 100%);
}

.mine-card {
  margin-bottom: 24rpx;
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
  width: 74rpx;
  height: 74rpx;
  border-radius: 50%;
  color: #ffffff;
  background: linear-gradient(135deg, #1677ff 0%, #14b8a6 100%);
  box-shadow: 0 10rpx 22rpx rgba(22, 119, 255, 0.14);
  font-size: 32rpx;
  font-weight: 900;
  line-height: 1;
}

.profile-name {
  max-width: 430rpx;
  margin-top: 6rpx;
  color: #0f172a;
  font-size: 34rpx;
  font-weight: 800;
  line-height: 1.2;
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

.profile-context-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10rpx;
  margin-top: 24rpx;
}

.profile-context-item {
  min-width: 0;
  padding: 14rpx 10rpx;
  border-radius: 18rpx;
  border: 1rpx solid #e5edf7;
  background: rgba(248, 251, 255, 0.92);
  text-align: center;
}

.profile-context-label {
  color: #64748b;
  font-size: 21rpx;
  line-height: 1.25;
}

.profile-context-value {
  margin-top: 6rpx;
  color: #0f172a;
  font-size: 23rpx;
  font-weight: 800;
  line-height: 1.25;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-health-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 18rpx;
}

.profile-health-pill {
  max-width: 100%;
  min-height: 40rpx;
  padding: 0 13rpx;
  border-radius: 999rpx;
  color: #0969da;
  background: #eff6ff;
  font-size: 21rpx;
  font-weight: 900;
  line-height: 40rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-health-pill.warning {
  color: #ad6800;
  background: #fff8e6;
}

.profile-shortcut-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx;
  margin-bottom: 22rpx;
}

.profile-shortcut-card {
  min-width: 0;
  padding: 20rpx;
  border: 1rpx solid #edf2f8;
  border-radius: 22rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 26rpx rgba(15, 23, 42, 0.035);
}

.profile-shortcut-icon {
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

.profile-shortcut-title {
  color: #111827;
  font-size: 26rpx;
  font-weight: 900;
  line-height: 1.25;
}

.profile-shortcut-desc {
  margin-top: 6rpx;
  color: #64748b;
  font-size: 22rpx;
  line-height: 1.35;
}

.tone-green {
  color: #047857;
  background: #ecfdf5;
}

.tone-orange {
  color: #b45309;
  background: #fff7ed;
}

.tone-blue {
  color: #0969da;
  background: #eff6ff;
}

.tone-cyan {
  color: #0e7490;
  background: #ecfeff;
}

.profile-detail-card {
  width: 100%;
  padding: 24rpx;
  border: 1rpx solid #edf2f8;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 30rpx rgba(15, 23, 42, 0.04);
}

.profile-detail-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.profile-detail-title {
  color: #111827;
  font-size: 30rpx;
  font-weight: 900;
  line-height: 1.25;
}

.profile-detail-desc {
  margin-top: 6rpx;
  color: #64748b;
  font-size: 23rpx;
  line-height: 1.4;
}

.profile-detail-badge {
  flex-shrink: 0;
  min-height: 42rpx;
  padding: 0 14rpx;
  border-radius: 999rpx;
  color: #047857;
  background: #ecfdf5;
  font-size: 21rpx;
  font-weight: 900;
  line-height: 42rpx;
}

.profile-detail-badge.offline {
  color: #ad6800;
  background: #fff8e6;
}

.profile-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx;
}

.profile-detail-item {
  min-width: 0;
  min-height: 160rpx;
  padding: 18rpx;
  border: 1rpx solid #e5edf7;
  border-radius: 20rpx;
  background: #f8fbff;
}

.profile-detail-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52rpx;
  height: 52rpx;
  border-radius: 18rpx;
  font-size: 22rpx;
  font-weight: 900;
  line-height: 1;
}

.profile-detail-label {
  margin-top: 14rpx;
  color: #64748b;
  font-size: 22rpx;
  line-height: 1.25;
}

.profile-detail-value {
  margin-top: 6rpx;
  color: #111827;
  font-size: 26rpx;
  font-weight: 900;
  line-height: 1.28;
  word-break: break-word;
}
</style>
