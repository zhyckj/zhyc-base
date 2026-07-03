<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page message-page mobile-bottom-safe">
    <MobilePageTopBar
      title="消息通知"
      eyebrow="消息中心"
      action-text="刷新"
      :action-disabled="status === 'loading'"
      @action="reloadMessages"
    />
    <view class="mobile-hero compact-hero message-hero">
      <view class="mobile-hero-header">
        <view class="mobile-hero-main">
          <view class="mobile-hero-kicker">消息中心</view>
          <view class="mobile-title">收件箱概览</view>
          <view class="mobile-subtitle">收件箱按未读优先处理，点开未读消息后自动更新状态。</view>
        </view>
      </view>
      <view class="message-overview">
        <view class="message-overview-main">
          <view class="message-overview-label">当前消息</view>
          <view class="message-overview-value">{{ messagePage.total }}</view>
        </view>
        <view class="message-overview-side">
          <view class="message-side-item">
            <text class="message-side-value">{{ loadedUnreadCount }}</text>
            <text class="message-side-label">未读</text>
          </view>
          <view class="message-side-item">
            <text class="message-side-value">{{ loadedReadCount }}</text>
            <text class="message-side-label">已读</text>
          </view>
        </view>
      </view>
      <view class="message-filter-panel">
        <view class="message-filter-meta">
          <view>
            <view class="message-filter-label">当前筛选</view>
            <view class="message-filter-value">{{ readFilterText }}</view>
          </view>
          <view class="message-sync-pill">{{ statusText }}</view>
        </view>
        <view class="mobile-segmented filter-row">
          <button
            class="mobile-segmented-item"
            :class="{ active: readFilter === undefined }"
            @tap="changeFilter(undefined)"
          >
            全部
          </button>
          <button
            class="mobile-segmented-item"
            :class="{ active: readFilter === false }"
            @tap="changeFilter(false)"
          >
            未读
          </button>
          <button
            class="mobile-segmented-item"
            :class="{ active: readFilter === true }"
            @tap="changeFilter(true)"
          >
            已读
          </button>
        </view>
      </view>
    </view>

    <MobileState
      v-if="status === 'loading' && messagePage.records.length === 0"
      type="loading"
      title="消息加载中"
      description="正在同步站内通知"
    />
    <MobileState
      v-else-if="status === 'error'"
      type="error"
      title="消息加载失败"
      :description="errorMessage"
      action-text="重试"
      @action="reloadMessages"
    />
    <MobileState
      v-else-if="status === 'success' && messagePage.records.length === 0"
      type="empty"
      title="暂无消息"
      description="新的站内通知会展示在这里"
    />

    <view
      v-for="message in messagePage.records"
      :key="message.messageCode"
      class="mobile-list-card mobile-rich-list-card message-card"
      hover-class="message-card-hover"
      @tap="openMessage(message)"
    >
      <view class="mobile-list-leading">
        <view :class="['mobile-list-icon', 'mobile-solid-icon', 'icon-message', message.readFlag ? 'tone-green' : 'tone-orange']"></view>
        <view class="mobile-list-body">
          <view class="mobile-list-top">
            <view class="mobile-card-title">{{ message.title }}</view>
            <view class="mobile-status-chip" :class="{ success: message.readFlag }">
              {{ message.readFlag ? '已读' : '未读' }}
            </view>
          </view>
          <view :class="['mobile-card-desc', 'message-content', { expanded: expandedMessageCode === message.messageCode }]">
            {{ message.content }}
          </view>
          <view class="mobile-list-footer">
            <view class="mobile-mini-tag-row">
              <view class="mobile-mini-tag">{{ message.messageType }}</view>
              <view class="mobile-mini-tag">{{ message.createdAt }}</view>
            </view>
            <view class="mobile-list-action-text">
              {{ resolveMessageActionText(message) }}
            </view>
          </view>
        </view>
      </view>
    </view>

    <button
      v-if="status === 'success'"
      class="mobile-secondary-button load-more-button"
      :disabled="!hasMore || loadingMore"
      @tap="loadMore"
    >
      {{ loadingMore ? '加载中' : hasMore ? '加载更多' : '没有更多消息' }}
    </button>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

import MobilePageTopBar from '@/components/MobilePageTopBar.vue';
import MobileState from '@/components/MobileState.vue';
import {
  listMobileMessages,
  markMobileMessageRead,
  type MobileMessage,
  type MobileMessagePage,
} from '@/api/message';
import type { MobileLoadStatus } from '@/types/platform';
import { getMobileUserContext, requireMobileTenantId, requireMobileUserId, showMobileToast } from '@/utils/platform';

const pageSize = 10;

/** 页面加载状态。 */
const status = ref<MobileLoadStatus>('idle');
/** 错误提示。 */
const errorMessage = ref('');
/** 是否正在加载下一页。 */
const loadingMore = ref(false);
/** 已读筛选条件。 */
const readFilter = ref<boolean | undefined>(undefined);
/** 消息分页数据。 */
const messagePage = ref<MobileMessagePage>({ total: 0, pageNo: 1, pageSize, records: [] });
/** 当前展开的消息编码。 */
const expandedMessageCode = ref('');
/** 是否还有更多消息。 */
const hasMore = computed(() => messagePage.value.records.length < messagePage.value.total);
/** 当前已加载未读消息数量，用于移动端即时反馈。 */
const loadedUnreadCount = computed(() => messagePage.value.records.filter((message) => !message.readFlag).length);
/** 当前已加载已读消息数量，用于移动端即时反馈。 */
const loadedReadCount = computed(() => messagePage.value.records.filter((message) => message.readFlag).length);
/** 当前筛选展示文本。 */
const readFilterText = computed(() => (readFilter.value === undefined ? '全部' : readFilter.value ? '已读' : '未读'));
/** 当前同步状态展示文本。 */
const statusText = computed(() => (loadingMore.value || status.value === 'loading' ? '同步中' : status.value === 'success' ? '已同步' : '待同步'));

onLoad(() => {
  void reloadMessages();
});

/**
 * 切换消息已读筛选。
 *
 * @param readFlag 是否已读
 */
function changeFilter(readFlag: boolean | undefined): void {
  readFilter.value = readFlag;
  expandedMessageCode.value = '';
  void reloadMessages();
}

/**
 * 重新加载消息通知。
 */
async function reloadMessages(): Promise<void> {
  status.value = 'loading';
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    messagePage.value = await listMobileMessages({
      readFlag: readFilter.value,
      pageNo: 1,
      pageSize,
    });
    expandedMessageCode.value = '';
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '消息加载失败';
    status.value = 'error';
  }
}

/**
 * 加载下一页消息。
 */
async function loadMore(): Promise<void> {
  if (!hasMore.value || loadingMore.value) {
    return;
  }
  loadingMore.value = true;
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    const nextPage = await listMobileMessages({
      readFlag: readFilter.value,
      pageNo: messagePage.value.pageNo + 1,
      pageSize,
    });
    messagePage.value = {
      total: nextPage.total,
      pageNo: nextPage.pageNo,
      pageSize: nextPage.pageSize,
      records: [...messagePage.value.records, ...nextPage.records],
    };
    status.value = 'success';
  } catch (error) {
    const message = error instanceof Error ? error.message : '消息加载失败';
    showMobileToast(`加载失败：${message}`, 'none');
  } finally {
    loadingMore.value = false;
  }
}

/**
 * 标记消息为已读。
 *
 * @param message 消息记录
 */
async function openMessage(message: MobileMessage): Promise<void> {
  if (message.readFlag) {
    expandedMessageCode.value = expandedMessageCode.value === message.messageCode ? '' : message.messageCode;
    return;
  }
  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    requireMobileTenantId(userContext);
    await markMobileMessageRead(message.messageCode);
    handleMessageMarkedRead(message);
  } catch (error) {
    const messageText = error instanceof Error ? error.message : '消息处理失败';
    showMobileToast(`处理失败：${messageText}`, 'none');
  }
}

/**
 * 处理消息已读后的列表反馈。
 *
 * @param message 已处理的消息记录
 */
function handleMessageMarkedRead(message: MobileMessage): void {
  if (readFilter.value === false) {
    messagePage.value = {
      ...messagePage.value,
      total: Math.max(0, messagePage.value.total - 1),
      records: messagePage.value.records.filter((record) => record.messageCode !== message.messageCode),
    };
    expandedMessageCode.value = '';
    showMobileToast('已读，已从未读列表移除', 'success');
    return;
  }
  message.readFlag = true;
  expandedMessageCode.value = message.messageCode;
  showMobileToast('已标记为已读', 'success');
}

/**
 * 解析消息卡片动作文案。
 *
 * @param message 消息记录
 */
function resolveMessageActionText(message: MobileMessage): string {
  if (!message.readFlag) {
    return '点按已读';
  }
  return expandedMessageCode.value === message.messageCode ? '收起' : '查看全文';
}
</script>

<style scoped>
.message-page {
  background: linear-gradient(180deg, #f6faff 0%, #f4f7fb 48%, #f8fafc 100%);
}

.message-hero {
  border-color: #d8e8ff;
  background: linear-gradient(145deg, #ffffff 0%, #f1f7ff 56%, #f4fbf8 100%);
}

.message-overview {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 14rpx;
  margin-top: 20rpx;
}

.message-overview-main {
  min-width: 0;
  flex: 1;
  padding: 18rpx 20rpx;
  border: 1rpx solid #dbeafe;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.86);
}

.message-overview-label {
  color: #64748b;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1.2;
}

.message-overview-value {
  margin-top: 6rpx;
  color: #0f172a;
  font-size: 46rpx;
  font-weight: 900;
  line-height: 1;
}

.message-overview-side {
  flex-shrink: 0;
  display: grid;
  grid-template-columns: repeat(2, 100rpx);
  gap: 10rpx;
}

.message-side-item {
  min-width: 0;
  padding: 16rpx 8rpx;
  border: 1rpx solid #e5edf7;
  border-radius: 18rpx;
  background: rgba(255, 255, 255, 0.88);
  text-align: center;
}

.message-side-value {
  display: block;
  color: #111827;
  font-size: 30rpx;
  font-weight: 900;
  line-height: 1.05;
}

.message-side-label {
  display: block;
  margin-top: 6rpx;
  color: #64748b;
  font-size: 21rpx;
  font-weight: 700;
  line-height: 1.2;
}

.message-filter-panel {
  margin-top: 16rpx;
  padding: 14rpx;
  border: 1rpx solid #e5edf7;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.78);
}

.message-filter-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
  padding: 0 4rpx 12rpx;
}

.message-filter-label {
  color: #64748b;
  font-size: 21rpx;
  font-weight: 700;
  line-height: 1.2;
}

.message-filter-value {
  margin-top: 4rpx;
  color: #111827;
  font-size: 26rpx;
  font-weight: 900;
  line-height: 1.2;
}

.message-sync-pill {
  flex-shrink: 0;
  min-height: 40rpx;
  padding: 0 14rpx;
  border: 1rpx solid #b7d4ff;
  border-radius: 999rpx;
  color: #0969da;
  background: #eff6ff;
  font-size: 21rpx;
  font-weight: 800;
  line-height: 40rpx;
}

.filter-row {
  margin-top: 0;
}

.message-card {
  word-break: break-word;
}

.message-card-hover {
  opacity: 0.76;
}

.message-content {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.message-content.expanded {
  display: block;
  overflow: visible;
}

.load-more-button {
  margin-top: 14rpx;
}
</style>
