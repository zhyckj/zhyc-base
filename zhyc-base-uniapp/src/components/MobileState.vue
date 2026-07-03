<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view :class="['mobile-state-card', `mobile-state-${resolvedType}`, { 'mobile-state-compact': compactState }]">
    <view class="mobile-state-icon" aria-hidden="true">
      <view class="mobile-state-icon-core"></view>
    </view>
    <view class="mobile-state-main">
      <view class="mobile-state-title">{{ resolvedTitle }}</view>
      <view v-if="resolvedDescription" class="mobile-state-desc">{{ resolvedDescription }}</view>
    </view>
    <button
      v-if="actionText"
      class="mobile-secondary-button mobile-state-action"
      @tap="emit('action')"
    >
      {{ actionText }}
    </button>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';

import { isMobileContextErrorMessage, isMobileServiceErrorMessage } from '@/utils/platform';

/**
 * 移动端统一状态卡片属性。
 */
interface MobileStateProps {
  /** 状态类型，用于控制图标和色彩语义。 */
  type?: 'loading' | 'empty' | 'error' | 'info' | 'success';
  /** 状态主标题。 */
  title: string;
  /** 状态说明文案。 */
  description?: string;
  /** 操作按钮文案。 */
  actionText?: string;
}

const props = withDefaults(defineProps<MobileStateProps>(), {
  type: 'info',
  description: '',
  actionText: '',
});

const emit = defineEmits<{
  /** 触发状态卡片主操作。 */
  (event: 'action'): void;
}>();

/** 当前错误是否为登录上下文错误。 */
const contextError = computed(() => props.type === 'error' && isMobileContextErrorMessage(props.description));

/** 当前错误是否为服务不可用或网络不可达。 */
const serviceError = computed(() => props.type === 'error' && isMobileServiceErrorMessage(props.description));

/** 当前错误是否为缺少查询条件或进入来源的引导提示。 */
const guidanceError = computed(() => (
  props.type === 'error'
  && typeof props.description === 'string'
  && (props.description.includes('请输入') || props.description.includes('请从'))
));

/** 登录上下文、服务异常和查询引导使用轻量卡片，避免移动端首屏被状态占满。 */
const compactState = computed(() => contextError.value || serviceError.value || guidanceError.value);

/** 实际展示状态类型。 */
const resolvedType = computed<Required<MobileStateProps>['type']>(() => (
  contextError.value || serviceError.value || guidanceError.value ? 'info' : props.type
));

/** 实际展示标题。 */
const resolvedTitle = computed(() => {
  if (contextError.value) {
    return '需要登录移动端账号';
  }
  if (serviceError.value) {
    return '服务暂不可用';
  }
  if (guidanceError.value) {
    return props.description?.includes('请从') ? '需要选择业务记录' : '需要补充查询条件';
  }
  return props.title;
});

/** 实际展示说明。 */
const resolvedDescription = computed(() => (
  contextError.value ? '登录后可同步当前账号的待办、消息和业务数据' : props.description
));

</script>
