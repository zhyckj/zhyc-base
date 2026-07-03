<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page workbench-page mobile-bottom-safe">
    <view class="workbench-appbar">
      <view class="workbench-appbar-main">
        <view class="workbench-avatar mini-avatar" aria-hidden="true">
          <view class="avatar-solid-core"></view>
          <view class="avatar-solid-node"></view>
        </view>
        <view class="workbench-app-copy">
          <view class="workbench-app-eyebrow">移动办公 · {{ todayText }}</view>
          <view class="workbench-app-title">{{ greetingText }}，{{ accountName }}</view>
        </view>
      </view>
      <view class="workbench-app-actions">
        <view :class="['workbench-status-dot', { offline: !userContext.loggedIn }]"></view>
        <button class="workbench-refresh-button" :disabled="loading" @tap="loadOverview">刷新</button>
      </view>
    </view>

    <view class="workbench-hero mobile-hero" hover-class="workbench-hero-hover" @tap="go('/pages/workflow/todo')">
      <view class="workbench-hero-glow"></view>
      <view class="workbench-hero-head">
        <view class="workbench-hero-title-group">
          <view class="workbench-hero-label">今日概览</view>
          <view class="workbench-context">{{ currentTenantId }}</view>
        </view>
        <view :class="['workbench-context-chip', { warning: loadError }]">{{ heroStatusText }}</view>
      </view>

      <view class="workbench-focus-row">
        <view class="workbench-focus-main">
          <view class="workbench-focus-label">今日待处理</view>
          <view class="workbench-focus-value">
            <text class="workbench-focus-number">{{ pendingTotal }}</text>
            <text class="workbench-focus-unit">项</text>
          </view>
          <view class="workbench-focus-hint">{{ focusHintText }}</view>
        </view>
        <view class="workbench-primary-action">处理</view>
      </view>

      <view class="workbench-metric-strip">
        <view v-for="metric in overviewMetrics" :key="metric.label" class="workbench-metric-pill">
          <view class="workbench-metric-number">{{ metric.value }}</view>
          <view class="workbench-metric-label">{{ metric.label }}</view>
        </view>
      </view>

      <view v-if="loadError" class="workbench-sync-note" @tap.stop="handleLoadNoticeAction">
        <view class="workbench-sync-dot"></view>
        <view class="workbench-sync-copy">
          <text class="workbench-sync-title">{{ loadNoticeTitle }}</text>
          <text class="workbench-sync-desc">{{ loadNoticeDescription }}</text>
        </view>
        <view class="workbench-sync-action">{{ loadNoticeActionText }}</view>
      </view>
    </view>

    <view class="mobile-section">
      <view class="workbench-section-head">
        <view>
          <view class="workbench-section-title">应用</view>
          <view class="workbench-section-subtitle">常用业务入口</view>
        </view>
        <button class="workbench-section-action" @tap="go('/pages/workbench/quick-start')">新建</button>
      </view>
      <view class="workbench-app-grid">
        <view
          v-for="item in shortcuts"
          :key="item.url"
          class="workbench-app-cell"
          hover-class="workbench-app-cell-hover"
          @tap="go(item.url)"
        >
          <view :class="['workbench-app-icon', 'mobile-solid-icon', `tone-${item.tone}`, `icon-${item.icon}`]" aria-hidden="true"></view>
          <view class="workbench-app-title-text">{{ item.title }}</view>
          <view class="workbench-app-desc">{{ item.description }}</view>
        </view>
      </view>
    </view>

    <view class="workbench-activity-card">
      <view class="workbench-activity-head">
        <view>
          <view class="workbench-activity-title">最近动态</view>
          <view class="workbench-activity-subtitle">{{ activitySubtitle }}</view>
        </view>
        <view class="workbench-activity-badge">{{ activityBadgeText }}</view>
      </view>
      <view class="workbench-activity-empty">
        <view class="workbench-activity-icon mobile-solid-icon icon-flow tone-blue"></view>
        <view class="workbench-activity-main">
          <view class="workbench-activity-empty-title">{{ activityTitle }}</view>
          <view class="workbench-activity-empty-desc">{{ activityDescription }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onShow } from '@dcloudio/uni-app';
import { computed, reactive, ref } from 'vue';

import { listMobileMessages } from '@/api/message';
import { listMobilePurchaseOrders, listMobilePurchaseRequests } from '@/api/purchase';
import {
  listMobileCcTasks,
  listMobileDoneTasks,
  listMobileStartedProcesses,
  listMobileTodoTasks,
} from '@/api/workflow';
import type { MobileShortcut } from '@/types/platform';
import {
  getMobileUserContext,
  isMobileContextErrorMessage,
  isMobileServiceErrorMessage,
  normalizeMobileErrorMessage,
  openMobilePage,
  redirectToMobileLogin,
  requireMobileTenantId,
  requireMobileUserId,
  type MobileUserContext,
} from '@/utils/platform';

/**
 * 移动工作台概览指标。
 */
interface MobileWorkbenchOverview {
  /** 待办任务数量。 */
  todoCount: number;
  /** 已办任务数量。 */
  doneCount: number;
  /** 我发起的流程数量。 */
  startedCount: number;
  /** 抄送我的任务数量。 */
  ccCount: number;
  /** 未读消息数量。 */
  unreadMessageCount: number;
  /** 草稿采购申请数量。 */
  draftRequestCount: number;
  /** 待确认采购订单数量。 */
  pendingOrderCount: number;
}

/**
 * 工作台宫格入口，保留基础入口字段并补充移动端图标语义。
 */
interface WorkbenchShortcut extends MobileShortcut {
  /** 图标色系。 */
  tone: 'blue' | 'green' | 'orange' | 'purple' | 'cyan';
  /** 面性图标语义。 */
  icon: 'task' | 'done' | 'send' | 'copy' | 'message' | 'cart' | 'order' | 'info';
}

/**
 * 工作台紧凑指标。
 */
interface WorkbenchMetric {
  /** 指标标签。 */
  label: string;
  /** 指标值。 */
  value: number;
}

/** 移动工作台加载状态。 */
const loading = ref(false);

/** 移动工作台加载失败提示。 */
const loadError = ref('');

/** 移动工作台概览指标。 */
const overview = reactive<MobileWorkbenchOverview>({
  todoCount: 0,
  doneCount: 0,
  startedCount: 0,
  ccCount: 0,
  unreadMessageCount: 0,
  draftRequestCount: 0,
  pendingOrderCount: 0,
});

/** 当前移动端用户上下文。 */
const userContext = ref<MobileUserContext>(getMobileUserContext());

/** 当前账号名称。 */
const accountName = computed(() => userContext.value.accountName || '未登录');

/** 当前租户展示名称。 */
const currentTenantId = computed(() => userContext.value.tenantId || '未识别租户');

/** 登录态提示。 */
const contextStatusText = computed(() => (userContext.value.loggedIn ? '在线' : '未登录'));

/** 当前错误是否为登录态问题。 */
const contextLoadError = computed(() => isMobileContextErrorMessage(loadError.value));

/** 当前错误是否为服务不可用。 */
const serviceLoadError = computed(() => isMobileServiceErrorMessage(loadError.value));

/** 工作台首屏状态标签。 */
const heroStatusText = computed(() => {
  if (contextLoadError.value) {
    return '需登录';
  }
  if (loadError.value) {
    return '离线';
  }
  return contextStatusText.value;
});

/** 今日待处理说明文案。 */
const focusHintText = computed(() => (loadError.value ? '本地入口可用，数据待同步' : '待办、消息和待确认订单'));

/** 最近动态副标题。 */
const activitySubtitle = computed(() => (loadError.value ? '服务恢复后自动同步' : '审批和消息状态'));

/** 最近动态状态标签。 */
const activityBadgeText = computed(() => (pendingTotal.value > 0 ? `${pendingTotal.value} 项` : '暂无'));

/** 最近动态主标题。 */
const activityTitle = computed(() => (pendingTotal.value > 0 ? '有待处理事项' : '暂无新的待办动态'));

/** 最近动态说明。 */
const activityDescription = computed(() => (
  loadError.value ? '当前仍可通过常用入口进入业务，数据同步恢复后自动刷新。' : '新的审批、消息和订单状态会优先展示在这里。'
));

/** 工作台日期文案，避免移动端首屏出现后台式说明文案。 */
const todayText = computed(() => {
  const now = new Date();
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  return `${now.getMonth() + 1}月${now.getDate()}日 ${weekDays[now.getDay()]}`;
});

/** 根据时间生成移动端问候语。 */
const greetingText = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) {
    return '夜间办公';
  }
  if (hour < 12) {
    return '上午好';
  }
  if (hour < 18) {
    return '下午好';
  }
  return '晚上好';
});

/** 今日待处理合计。 */
const pendingTotal = computed(() => overview.todoCount + overview.unreadMessageCount + overview.pendingOrderCount);

/** 工作台错误提醒标题。 */
const loadNoticeTitle = computed(() => {
  if (contextLoadError.value) {
    return '需要登录';
  }
  if (serviceLoadError.value) {
    return '服务未接入';
  }
  return '同步未完成';
});

/** 工作台错误提醒说明。 */
const loadNoticeDescription = computed(() => {
  if (contextLoadError.value) {
    return '登录后同步待办和消息';
  }
  if (serviceLoadError.value) {
    return '已保留常用入口';
  }
  return '已保留本地展示';
});

/** 工作台错误提醒按钮文案。 */
const loadNoticeActionText = computed(() => (contextLoadError.value ? '去登录' : '重试'));

/** 工作台四个核心指标。 */
const overviewMetrics = computed<WorkbenchMetric[]>(() => [
  { label: '待办', value: overview.todoCount },
  { label: '消息', value: overview.unreadMessageCount },
  { label: '草稿', value: overview.draftRequestCount },
  { label: '订单', value: overview.pendingOrderCount },
]);

/** 移动端首页快捷入口。 */
const shortcuts = computed<WorkbenchShortcut[]>(() => {
  return [
    { title: '流程', url: '/pages/workflow/todo', description: `${overview.todoCount} 条待办`, tone: 'blue', icon: 'task' },
    { title: '采购', url: '/pages/purchase/request-list', description: `${overview.draftRequestCount} 草稿`, tone: 'green', icon: 'cart' },
    { title: 'AI', url: '/pages/ai/index', description: '问答接入', tone: 'purple', icon: 'info' },
    { title: '消息', url: '/pages/message/index', description: `${overview.unreadMessageCount} 未读`, tone: 'orange', icon: 'message' },
  ];
});

/** 工作台二级业务入口路径索引，首屏只展示高频入口，其余入口由对应模块页承载。 */
const workbenchSecondaryRoutes = [
  '/pages/workflow/done',
  '/pages/workflow/started',
  '/pages/workflow/cc',
  '/pages/purchase/order-list',
];

/**
 * 跳转到移动端页面。
 *
 * @param url 页面地址
 */
function go(url: string): void {
  if (!userContext.value.loggedIn) {
    redirectToMobileLogin('请先登录移动端账号', url);
    return;
  }
  openMobilePage(url);
}

/**
 * 进入移动端登录页。
 */
function goLogin(): void {
  redirectToMobileLogin(loadError.value || '请先登录移动端账号');
}

/**
 * 处理工作台错误提醒操作。
 */
function handleLoadNoticeAction(): void {
  if (contextLoadError.value) {
    goLogin();
    return;
  }
  void loadOverview();
}

/**
 * 加载移动工作台概览指标。
 */
async function loadOverview(): Promise<void> {
  const currentUserContext = getMobileUserContext();
  userContext.value = currentUserContext;
  loading.value = true;
  loadError.value = '';
  try {
    requireMobileUserId(currentUserContext);
    requireMobileTenantId(currentUserContext);
    const todoTasks = await listMobileTodoTasks();
    overview.todoCount = todoTasks.length;
    const [
      doneTasks,
      startedProcesses,
      ccTasks,
      unreadMessages,
      draftRequests,
      pendingOrders,
    ] = await Promise.allSettled([
      listMobileDoneTasks(),
      listMobileStartedProcesses(),
      listMobileCcTasks(),
      listMobileMessages({ readFlag: false, pageNo: 1, pageSize: 1 }),
      listMobilePurchaseRequests({ processStatus: 'DRAFT', pageNo: 1, pageSize: 1 }),
      listMobilePurchaseOrders({ orderStatus: 'PENDING_CONFIRM', pageNo: 1, pageSize: 1 }),
    ]);
    overview.doneCount = readSettledArrayLength(doneTasks, overview.doneCount);
    overview.startedCount = readSettledArrayLength(startedProcesses, overview.startedCount);
    overview.ccCount = readSettledArrayLength(ccTasks, overview.ccCount);
    overview.unreadMessageCount = readSettledPageTotal(unreadMessages, overview.unreadMessageCount);
    overview.draftRequestCount = readSettledPageTotal(draftRequests, overview.draftRequestCount);
    overview.pendingOrderCount = readSettledPageTotal(pendingOrders, overview.pendingOrderCount);
    loadError.value = '';
  } catch (error) {
    loadError.value = normalizeMobileErrorMessage(error, '移动工作台加载失败');
  } finally {
    loading.value = false;
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
 * 读取已完成分页结果总数。
 *
 * @param result 异步加载结果
 * @param fallback 失败时保留的旧值
 * @returns 分页总数或旧值
 */
function readSettledPageTotal(
  result: PromiseSettledResult<{ total: number }>,
  fallback: number,
): number {
  return result.status === 'fulfilled' ? result.value.total : fallback;
}

onShow(() => {
  userContext.value = getMobileUserContext();
  void loadOverview();
});
</script>

<style scoped>
.workbench-page {
  box-sizing: border-box;
  width: 100%;
  max-width: 100vw;
  min-width: 0;
  overflow-x: hidden;
  padding: calc(12rpx + env(safe-area-inset-top)) 20rpx calc(126rpx + env(safe-area-inset-bottom));
  background:
    linear-gradient(180deg, rgba(240, 247, 255, 0.98) 0, rgba(247, 250, 253, 0.98) 260rpx, #f5f7fb 100%);
}

.workbench-page.mobile-bottom-safe {
  padding-bottom: calc(132rpx + env(safe-area-inset-bottom));
}

.workbench-appbar {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
  min-height: 62rpx;
  margin-bottom: 12rpx;
}

.workbench-appbar-main {
  min-width: 0;
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.workbench-app-copy {
  min-width: 0;
  flex: 1;
}

.workbench-app-eyebrow {
  color: #6b7c95;
  font-size: 20rpx;
  font-weight: 700;
  line-height: 1.25;
}

.workbench-app-title {
  margin-top: 2rpx;
  color: #0f172a;
  font-size: 27rpx;
  font-weight: 900;
  line-height: 1.16;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.workbench-app-actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.workbench-status-dot {
  flex-shrink: 0;
  width: 13rpx;
  height: 13rpx;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 8rpx rgba(34, 197, 94, 0.1);
}

.workbench-status-dot.offline {
  background: #ef4444;
  box-shadow: 0 0 0 8rpx rgba(239, 68, 68, 0.1);
}

.workbench-refresh-button {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 68rpx;
  height: 46rpx;
  padding: 0 14rpx;
  border: 1rpx solid rgba(190, 207, 232, 0.9);
  border-radius: 999rpx;
  color: #0f66d0;
  background: rgba(255, 255, 255, 0.86);
  font-size: 21rpx;
  font-weight: 900;
  line-height: 46rpx;
  box-shadow: 0 8rpx 18rpx rgba(15, 23, 42, 0.04);
}

.workbench-hero {
  position: relative;
  width: 100%;
  max-width: 100%;
  overflow: hidden;
  margin-bottom: 14rpx;
  padding: 16rpx;
  border: 1rpx solid rgba(210, 225, 245, 0.82);
  border-radius: 22rpx;
  background: linear-gradient(145deg, #ffffff 0%, #f5f9ff 58%, #f3fbf8 100%);
  box-shadow: 0 10rpx 24rpx rgba(38, 82, 140, 0.05);
}

.workbench-hero-glow {
  position: absolute;
  right: 0;
  top: -70rpx;
  width: 150rpx;
  height: 150rpx;
  border-radius: 50%;
  background: rgba(22, 119, 255, 0.08);
  pointer-events: none;
}

.workbench-hero-hover {
  opacity: 0.88;
}

.workbench-hero-head {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
  min-width: 0;
}

.workbench-hero-title-group {
  min-width: 0;
  flex: 1;
}

.workbench-hero-label {
  min-width: 0;
  color: #6b7c95;
  font-size: 20rpx;
  font-weight: 800;
  line-height: 1.25;
}

.workbench-avatar {
  position: relative;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 62rpx;
  height: 62rpx;
  border-radius: 50%;
  color: #ffffff;
  background: linear-gradient(135deg, #1677ff 0%, #14b8a6 100%);
  box-shadow: 0 10rpx 22rpx rgba(22, 119, 255, 0.14);
}

.mini-avatar {
  width: 50rpx;
  height: 50rpx;
  border-radius: 18rpx;
  font-size: 23rpx;
  box-shadow: 0 8rpx 18rpx rgba(22, 119, 255, 0.12);
}

.avatar-solid-core {
  width: 24rpx;
  height: 24rpx;
  border-radius: 9rpx;
  background: rgba(255, 255, 255, 0.92);
}

.avatar-solid-node {
  position: absolute;
  right: 9rpx;
  bottom: 9rpx;
  width: 12rpx;
  height: 12rpx;
  border-radius: 999rpx;
  background: #dffcf5;
}

.workbench-context {
  min-width: 0;
  max-width: 100%;
  margin-top: 4rpx;
  color: #5d708b;
  font-size: 20rpx;
  font-weight: 700;
  line-height: 1.25;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.workbench-context-chip {
  flex-shrink: 0;
  min-height: 36rpx;
  padding: 0 13rpx;
  border: 1rpx solid #c8dcf7;
  border-radius: 999rpx;
  color: #0969da;
  background: #f6faff;
  font-size: 20rpx;
  font-weight: 800;
  line-height: 36rpx;
}

.workbench-context-chip.warning {
  border-color: #fed7aa;
  color: #b45309;
  background: #fff7ed;
}

.workbench-focus-row {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
  margin-top: 12rpx;
}

.workbench-focus-main {
  min-width: 0;
  flex: 1;
}

.workbench-focus-label {
  color: #5d708b;
  font-size: 20rpx;
  font-weight: 700;
  line-height: 1.25;
}

.workbench-focus-value {
  display: flex;
  align-items: baseline;
  gap: 6rpx;
  margin-top: 2rpx;
}

.workbench-focus-number {
  color: #0f172a;
  font-size: 44rpx;
  font-weight: 900;
  line-height: 1;
}

.workbench-focus-unit {
  color: #5d708b;
  font-size: 20rpx;
  font-weight: 800;
}

.workbench-focus-hint {
  margin-top: 4rpx;
  color: #6b7c95;
  font-size: 20rpx;
  font-weight: 700;
  line-height: 1.3;
}

.workbench-primary-action {
  flex-shrink: 0;
  min-width: 86rpx;
  min-height: 50rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  color: #ffffff;
  background: #1677ff;
  font-size: 22rpx;
  font-weight: 900;
  line-height: 50rpx;
  text-align: center;
  box-shadow: 0 10rpx 22rpx rgba(22, 119, 255, 0.2);
}

.workbench-metric-strip {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 7rpx;
  margin-top: 14rpx;
}

.workbench-metric-pill {
  min-width: 0;
  min-height: 52rpx;
  padding: 7rpx 4rpx;
  border: 1rpx solid rgba(219, 231, 247, 0.88);
  border-radius: 14rpx;
  background: rgba(255, 255, 255, 0.84);
  text-align: center;
}

.workbench-metric-number {
  color: #0f172a;
  font-size: 23rpx;
  font-weight: 800;
  line-height: 1.05;
}

.workbench-metric-label {
  margin-top: 3rpx;
  color: #6b7c95;
  font-size: 18rpx;
  line-height: 1.2;
}

.workbench-sync-note {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10rpx;
  margin-top: 12rpx;
  padding: 10rpx 12rpx;
  border: 1rpx solid #ffe2b8;
  border-radius: 16rpx;
  background: #fffaf2;
}

.workbench-sync-dot {
  flex-shrink: 0;
  width: 14rpx;
  height: 14rpx;
  border-radius: 999rpx;
  background: #f97316;
}

.workbench-sync-copy {
  min-width: 0;
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.workbench-sync-title {
  flex-shrink: 0;
  color: #7c2d12;
  font-size: 20rpx;
  font-weight: 800;
  line-height: 1.35;
}

.workbench-sync-desc {
  min-width: 0;
  color: #64748b;
  font-size: 19rpx;
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.workbench-sync-action {
  flex-shrink: 0;
  min-width: 62rpx;
  min-height: 34rpx;
  padding: 0 10rpx;
  border-radius: 999rpx;
  color: #9a3412;
  background: #ffffff;
  font-size: 18rpx;
  font-weight: 800;
  line-height: 34rpx;
  text-align: center;
}

.workbench-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin: 0 2rpx 10rpx;
}

.workbench-section-title {
  color: #111827;
  font-size: 27rpx;
  font-weight: 900;
  line-height: 1.25;
}

.workbench-section-subtitle {
  margin-top: 3rpx;
  color: #6b7c95;
  font-size: 20rpx;
  line-height: 1.35;
}

.workbench-section-action {
  flex-shrink: 0;
  min-width: 80rpx;
  min-height: 42rpx;
  padding: 0 14rpx;
  border: 1rpx solid #bfd4f2;
  border-radius: 999rpx;
  color: #0969da;
  background: #ffffff;
  font-size: 20rpx;
  font-weight: 800;
  line-height: 42rpx;
}

.workbench-app-grid {
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  min-width: 0;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0;
  padding: 16rpx 6rpx 14rpx;
  border: 1rpx solid #edf2f8;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.032);
}

.workbench-app-cell {
  box-sizing: border-box;
  width: 100%;
  min-width: 0;
  max-width: 100%;
  min-height: 98rpx;
  padding: 2rpx;
  text-align: center;
}

.workbench-app-cell-hover {
  opacity: 0.72;
}

.workbench-app-icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50rpx;
  height: 50rpx;
  margin: 0 auto 7rpx;
  border-radius: 17rpx;
  box-shadow: 0 10rpx 18rpx rgba(15, 23, 42, 0.05);
}

.workbench-app-icon-core {
  width: 28rpx;
  height: 28rpx;
  border-radius: 10rpx;
  background: currentColor;
  opacity: 0.9;
}

.workbench-app-icon-node {
  position: absolute;
  right: 11rpx;
  bottom: 11rpx;
  width: 13rpx;
  height: 13rpx;
  border-radius: 999rpx;
  background: currentColor;
  opacity: 0.32;
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

.workbench-app-title-text {
  color: #111827;
  font-size: 21rpx;
  font-weight: 800;
  line-height: 1.25;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.workbench-app-desc {
  margin-top: 3rpx;
  color: #6b7c95;
  font-size: 18rpx;
  line-height: 1.25;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.workbench-activity-card {
  width: 100%;
  margin-top: 14rpx;
  padding: 18rpx;
  border: 1rpx solid #edf2f8;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 10rpx 24rpx rgba(15, 23, 42, 0.03);
}

.workbench-activity-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
  margin-bottom: 14rpx;
}

.workbench-activity-title {
  color: #111827;
  font-size: 25rpx;
  font-weight: 900;
  line-height: 1.25;
}

.workbench-activity-subtitle {
  margin-top: 3rpx;
  color: #6b7c95;
  font-size: 19rpx;
  line-height: 1.3;
}

.workbench-activity-badge {
  flex-shrink: 0;
  min-height: 36rpx;
  padding: 0 12rpx;
  border-radius: 999rpx;
  color: #64748b;
  background: #f1f5f9;
  font-size: 19rpx;
  font-weight: 800;
  line-height: 36rpx;
}

.workbench-activity-empty {
  display: flex;
  align-items: center;
  gap: 14rpx;
  min-height: 82rpx;
  padding: 14rpx;
  border-radius: 18rpx;
  background: #f8fbff;
}

.workbench-activity-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50rpx;
  height: 50rpx;
  border-radius: 17rpx;
}

.workbench-activity-main {
  min-width: 0;
  flex: 1;
}

.workbench-activity-empty-title {
  color: #111827;
  font-size: 23rpx;
  font-weight: 900;
  line-height: 1.35;
}

.workbench-activity-empty-desc {
  margin-top: 4rpx;
  color: #64748b;
  font-size: 20rpx;
  line-height: 1.35;
}
</style>
