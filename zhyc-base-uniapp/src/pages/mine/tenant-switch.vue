<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page tenant-switch-page mobile-bottom-safe">
    <MobilePageTopBar title="租户切换" eyebrow="账号安全" fallback-url="/pages/profile/index" />
    <view class="mobile-hero compact-hero tenant-hero">
      <view class="mobile-hero-kicker">账号安全</view>
      <view class="mobile-title">业务上下文</view>
      <view class="mobile-subtitle">从授权租户中点选切换，保存后立即影响移动端业务上下文。</view>
    </view>

    <view class="tenant-overview-card">
      <view class="tenant-overview-main">
        <view class="tenant-overview-label">当前租户</view>
        <view class="tenant-overview-value">{{ currentTenantId || '-' }}</view>
      </view>
      <view class="tenant-overview-count">
        <view class="tenant-count-value">{{ authorizedTenants.length }}</view>
        <view class="tenant-count-label">授权租户</view>
      </view>
    </view>

    <view class="mobile-section">
      <view class="mobile-section-header">
        <view class="mobile-section-title">授权租户</view>
      </view>
      <MobileState
        v-if="loading"
        type="loading"
        title="授权租户加载中"
        description="正在同步当前账号可访问的租户"
      />
      <MobileState
        v-else-if="!authorizedTenants.length"
        type="empty"
        title="暂无可切换租户"
        description="请确认当前账号已分配租户权限"
      />
      <view
        v-for="tenant in authorizedTenants"
        :key="tenant.tenantId"
        class="mobile-list-card tenant-item"
        :class="{ active: targetTenantId === tenant.tenantId }"
        hover-class="tenant-item-hover"
        @tap="selectTenant(tenant.tenantId)"
      >
        <view class="mobile-list-leading">
          <view class="tenant-icon">{{ tenant.name.slice(0, 1) }}</view>
          <view class="mobile-card-row-main">
            <view class="mobile-card-title">{{ tenant.name }}</view>
            <view class="mobile-card-desc">{{ tenant.tenantId }}</view>
          </view>
          <view class="tenant-status-area">
            <view v-if="currentTenantId === tenant.tenantId" class="mobile-status-chip success">当前</view>
            <view v-else-if="targetTenantId === tenant.tenantId" class="mobile-status-chip">已选</view>
            <view v-else class="tenant-select-dot"></view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="authorizedTenants.length" class="mobile-form-card">
      <view class="tenant-switch-summary">
        <view>
          <view class="tenant-summary-label">将切换到</view>
          <view class="tenant-summary-value">{{ selectedTenantName }}</view>
        </view>
        <view class="mobile-status-chip" :class="{ success: targetTenantId === currentTenantId }">
          {{ targetTenantId === currentTenantId ? '当前租户' : '待切换' }}
        </view>
      </view>
      <button class="mobile-action-button" :disabled="loading" @tap="switchTenant">
        {{ loading ? '加载中' : targetTenantId === currentTenantId ? '当前租户' : '确认切换' }}
      </button>
      <view v-if="errorMessage" class="mobile-form-alert">{{ errorMessage }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onShow } from '@dcloudio/uni-app';
import { computed, ref } from 'vue';

import MobilePageTopBar from '@/components/MobilePageTopBar.vue';
import MobileState from '@/components/MobileState.vue';
import { listMobileAuthorizedTenants, type MobileAuthorizedTenant } from '@/api/tenant';
import {
  getMobileUserContext,
  redirectToMobileLogin,
  requireMobileTenantId,
  saveMobileUserContext,
  showConfirm,
  showMobileToast,
} from '@/utils/platform';

/** 当前移动端租户编码，用于展示切换前上下文。 */
const currentTenantId = ref('');
/** 目标租户编码，由用户主动输入或从授权租户列表点选。 */
const targetTenantId = ref('');
/** 当前账号可访问的租户列表，由服务端按账号和启用状态返回。 */
const authorizedTenants = ref<MobileAuthorizedTenant[]>([]);
/** 授权租户加载状态。 */
const loading = ref(false);
/** 租户切换错误提示。 */
const errorMessage = ref('');
/** 当前选中的租户展示名称。 */
const selectedTenantName = computed(() => {
  const selectedTenant = authorizedTenants.value.find((tenant) => tenant.tenantId === targetTenantId.value);
  return selectedTenant ? `${selectedTenant.name}（${selectedTenant.tenantId}）` : '请选择授权租户';
});

/**
 * 页面展示时刷新当前租户，避免旧上下文残留。
 */
onShow(() => {
  const context = getMobileUserContext();
  if (!context.loggedIn) {
    redirectToMobileLogin('请先登录移动端账号', '/pages/mine/tenant-switch');
    return;
  }
  void loadAuthorizedTenants();
});

/**
 * 加载当前账号可访问租户。
 */
async function loadAuthorizedTenants(): Promise<void> {
  const context = getMobileUserContext();
  currentTenantId.value = context.tenantId;
  targetTenantId.value = context.tenantId;
  if (!context.loggedIn || !context.accountName || context.accountName === '未登录') {
    authorizedTenants.value = [];
    errorMessage.value = '';
    redirectToMobileLogin('请先登录移动端账号', '/pages/mine/tenant-switch');
    return;
  }
  loading.value = true;
  try {
    authorizedTenants.value = await listMobileAuthorizedTenants(context.accountName);
    errorMessage.value = '';
  } catch (error) {
    authorizedTenants.value = [];
    errorMessage.value = error instanceof Error ? error.message : '授权租户加载失败';
  } finally {
    loading.value = false;
  }
}

/**
 * 选择目标租户编码。
 *
 * @param tenantId 租户编码
 */
function selectTenant(tenantId: string): void {
  targetTenantId.value = tenantId;
  errorMessage.value = '';
}

/**
 * 切换移动端租户上下文。
 */
async function switchTenant(): Promise<void> {
  const nextTenantId = targetTenantId.value.trim();
  if (!nextTenantId) {
    errorMessage.value = '请先选择目标租户';
    return;
  }
  if (nextTenantId === currentTenantId.value) {
    errorMessage.value = '当前已经在该租户下';
    return;
  }

  try {
    const context = getMobileUserContext();
    requireMobileTenantId(context);
    if (!authorizedTenants.value.some((tenant) => tenant.tenantId === nextTenantId)) {
      throw new Error('当前账号无权访问目标租户');
    }
    const confirmed = await showConfirm('租户切换', `二次确认：确定切换到租户 ${nextTenantId}？`);
    if (!confirmed) {
      return;
    }
    saveMobileUserContext({
      ...context,
      tenantId: nextTenantId,
    });
    currentTenantId.value = nextTenantId;
    errorMessage.value = '';
    showMobileToast('租户已切换', 'success');
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '租户切换失败';
  }
}
</script>

<style scoped>
.tenant-switch-page {
  background: linear-gradient(180deg, #f6faff 0%, #f4f7fb 48%, #f8fafc 100%);
}

.tenant-hero {
  border-color: #d8e8ff;
  background: linear-gradient(145deg, #ffffff 0%, #f3f8ff 58%, #f8fbff 100%);
}

.tenant-overview-card {
  display: flex;
  align-items: stretch;
  gap: 14rpx;
  margin-bottom: 22rpx;
  padding: 22rpx;
  border: 1rpx solid #dbeafe;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 28rpx rgba(15, 23, 42, 0.035);
}

.tenant-overview-main {
  min-width: 0;
  flex: 1;
  padding: 4rpx 0;
}

.tenant-overview-label {
  color: #64748b;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1.25;
}

.tenant-overview-value {
  margin-top: 8rpx;
  color: #111827;
  font-size: 30rpx;
  font-weight: 900;
  line-height: 1.2;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tenant-overview-count {
  flex-shrink: 0;
  min-width: 144rpx;
  padding: 14rpx 12rpx;
  border-radius: 18rpx;
  background: #f8fbff;
  text-align: center;
}

.tenant-count-value {
  color: #0f172a;
  font-size: 34rpx;
  font-weight: 900;
  line-height: 1.05;
}

.tenant-count-label {
  margin-top: 6rpx;
  color: #64748b;
  font-size: 21rpx;
  line-height: 1.25;
}

.tenant-item.active {
  border-color: #1677ff;
  background: #eff6ff;
}

.tenant-item-hover {
  background: #f8fbff;
}

.tenant-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  border-radius: 22rpx;
  color: #0969da;
  background: #eff6ff;
  font-size: 26rpx;
  font-weight: 900;
  line-height: 1;
}

.tenant-status-area {
  flex-shrink: 0;
}

.tenant-select-dot {
  width: 22rpx;
  height: 22rpx;
  border: 2rpx solid #cbd5e1;
  border-radius: 50%;
}

.tenant-switch-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  margin-bottom: 20rpx;
}

.tenant-summary-label {
  color: #64748b;
  font-size: 22rpx;
  line-height: 1.35;
}

.tenant-summary-value {
  margin-top: 6rpx;
  color: #111827;
  font-size: 28rpx;
  font-weight: 800;
  line-height: 1.35;
  word-break: break-word;
}

</style>
