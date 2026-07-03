<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page ai-page mobile-bottom-safe">
    <view class="ai-appbar">
      <view>
        <view class="ai-eyebrow">AI 能力中心</view>
        <view class="ai-title">AI 助手</view>
      </view>
      <view :class="['ai-status-chip', { online: userContext.loggedIn }]">
        {{ userContext.loggedIn ? '已登录' : '待登录' }}
      </view>
    </view>

    <view class="ai-hero mobile-hero">
      <view class="ai-hero-head">
        <view>
          <view class="ai-hero-label">智能应用接入</view>
          <view class="ai-hero-title">把业务问题交给 AI 处理</view>
        </view>
        <view class="ai-brand-mark" aria-hidden="true">
          <view class="ai-brand-eye left"></view>
          <view class="ai-brand-eye right"></view>
        </view>
      </view>
      <view class="ai-hero-desc">
        支持问答、摘要、审批意见草稿和业务数据解释。模型供应商配置完成后，可在移动端直接调用企业 AI 能力。
      </view>
      <view class="ai-model-card">
        <view>
          <view class="ai-model-label">默认应用</view>
          <view class="ai-model-name">{{ activeModelName }}</view>
        </view>
        <view :class="['ai-model-state', { ready: aiReady }]">{{ aiReady ? '可用' : '配置中' }}</view>
      </view>
    </view>

    <view class="mobile-section">
      <view class="mobile-section-header">
        <view>
          <view class="mobile-section-title">快捷场景</view>
          <view class="ai-section-desc">选择场景后自动填入提示词</view>
        </view>
      </view>
      <view class="ai-prompt-grid">
        <view
          v-for="prompt in promptTemplates"
          :key="prompt.code"
          :class="['ai-prompt-card', { active: selectedPromptCode === prompt.code }]"
          hover-class="ai-prompt-card-hover"
          @tap="selectPrompt(prompt)"
        >
          <view :class="['ai-prompt-icon', 'mobile-solid-icon', `tone-${prompt.tone}`, `icon-${prompt.icon}`]"></view>
          <view class="ai-prompt-title">{{ prompt.title }}</view>
          <view class="ai-prompt-desc">{{ prompt.description }}</view>
        </view>
      </view>
    </view>

    <view class="mobile-form-card ai-chat-card">
      <view class="ai-chat-head">
        <view>
          <view class="mobile-card-title">对话输入</view>
          <view class="mobile-card-desc">输入业务内容后生成处理建议</view>
        </view>
        <button class="ai-clear-button" :disabled="!questionText" @tap="clearQuestion">清空</button>
      </view>
      <textarea
        v-model="questionText"
        class="mobile-textarea ai-question"
        placeholder="例如：请帮我总结这条采购审批的风险点"
        maxlength="600"
      />
      <view v-if="!userContext.loggedIn" class="mobile-form-alert">
        请先登录移动端账号，再使用企业 AI 应用。
      </view>
      <view v-else-if="!aiReady" class="mobile-form-alert">
        当前模型服务尚未启用，请先在后台完成企业 AI 应用配置。
      </view>
      <button class="mobile-action-button ai-submit" :disabled="!canSubmit" @tap="submitQuestion">
        生成建议
      </button>
    </view>

    <view class="mobile-field-card ai-answer-card">
      <view class="mobile-card-title">输出预览</view>
      <view class="ai-answer-content">{{ answerText }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';

import {
  getMobileUserContext,
  showMobileToast,
  type MobileUserContext,
} from '@/utils/platform';

/**
 * AI 快捷提示词模板。
 */
interface AiPromptTemplate {
  /** 提示词编码。 */
  code: string;
  /** 提示词标题。 */
  title: string;
  /** 提示词说明。 */
  description: string;
  /** 默认输入内容。 */
  content: string;
  /** 图标色系。 */
  tone: 'blue' | 'green' | 'orange' | 'purple';
  /** 面性图标语义。 */
  icon: 'message' | 'flow' | 'task' | 'info';
}

/** 当前移动端用户上下文。 */
const userContext = ref<MobileUserContext>(getMobileUserContext());
/** 当前选择的提示词编码。 */
const selectedPromptCode = ref('summary');
/** 用户输入的问题内容。 */
const questionText = ref('请帮我总结当前审批事项的关键风险点，并给出处理建议。');
/** 输出预览文案。 */
const answerText = ref('模型接入后，这里会展示企业 AI 返回的建议内容。');
/** 移动端 AI 调用状态，由后台应用配置启用。 */
const aiReady = ref(false);

/** 默认模型展示名称。 */
const activeModelName = computed(() => (aiReady.value ? '企业默认模型' : '等待后台应用接入'));
/** 当前是否允许提交。 */
const canSubmit = computed(() => userContext.value.loggedIn && aiReady.value && Boolean(questionText.value.trim()));

/** AI 快捷提示词模板列表。 */
const promptTemplates: AiPromptTemplate[] = [
  {
    code: 'summary',
    title: '摘要生成',
    description: '提炼长文本',
    content: '请帮我总结以下业务内容，输出关键结论、风险点和下一步建议。',
    tone: 'blue',
    icon: 'message',
  },
  {
    code: 'approval',
    title: '审批建议',
    description: '辅助判断',
    content: '请基于审批上下文给出处理建议，说明是否建议通过以及需要补充的材料。',
    tone: 'orange',
    icon: 'task',
  },
  {
    code: 'process',
    title: '流程解释',
    description: '说明节点',
    content: '请解释当前流程节点的职责、办理人需要关注的事项和常见退回原因。',
    tone: 'green',
    icon: 'flow',
  },
  {
    code: 'qa',
    title: '业务问答',
    description: '快速咨询',
    content: '请根据企业制度和业务规则回答以下问题，并列出依据和注意事项。',
    tone: 'purple',
    icon: 'info',
  },
];

/**
 * 页面展示时刷新用户上下文。
 */
onShow(() => {
  userContext.value = getMobileUserContext();
});

/**
 * 选择提示词模板。
 *
 * @param prompt 提示词模板
 */
function selectPrompt(prompt: AiPromptTemplate): void {
  selectedPromptCode.value = prompt.code;
  questionText.value = prompt.content;
}

/**
 * 清空输入内容。
 */
function clearQuestion(): void {
  questionText.value = '';
}

/**
 * 提交 AI 问题。
 */
function submitQuestion(): void {
  if (!userContext.value.loggedIn) {
    showMobileToast('请先登录移动端账号', 'none');
    return;
  }
  if (!aiReady.value) {
    showMobileToast('请先在后台启用模型应用', 'none');
    return;
  }
  answerText.value = '正在生成建议...';
}
</script>

<style scoped>
.ai-page {
  padding-top: calc(18rpx + env(safe-area-inset-top));
  padding-bottom: calc(132rpx + env(safe-area-inset-bottom));
  background: linear-gradient(180deg, #f7fbff 0%, #f3f7fb 52%, #f8fafc 100%);
}

.ai-appbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  min-height: 72rpx;
  margin-bottom: 18rpx;
}

.ai-eyebrow {
  color: #64748b;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1.25;
}

.ai-title {
  margin-top: 4rpx;
  color: #111827;
  font-size: 36rpx;
  font-weight: 900;
  line-height: 1.18;
}

.ai-status-chip {
  flex-shrink: 0;
  min-height: 44rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  color: #b45309;
  background: #fff7ed;
  font-size: 22rpx;
  font-weight: 900;
  line-height: 44rpx;
}

.ai-status-chip.online {
  color: #047857;
  background: #ecfdf5;
}

.ai-hero {
  border-color: #d8e8ff;
  background: linear-gradient(145deg, #ffffff 0%, #f4f9ff 56%, #eefbf8 100%);
}

.ai-hero-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18rpx;
}

.ai-hero-label {
  color: #64748b;
  font-size: 23rpx;
  font-weight: 800;
  line-height: 1.3;
}

.ai-hero-title {
  margin-top: 8rpx;
  color: #0f172a;
  font-size: 34rpx;
  font-weight: 900;
  line-height: 1.25;
}

.ai-brand-mark {
  position: relative;
  flex-shrink: 0;
  width: 72rpx;
  height: 72rpx;
  border-radius: 26rpx;
  background: linear-gradient(135deg, #1677ff 0%, #4f46e5 100%);
  box-shadow: 0 14rpx 32rpx rgba(22, 119, 255, 0.18);
}

.ai-brand-eye {
  position: absolute;
  top: 28rpx;
  width: 10rpx;
  height: 10rpx;
  border-radius: 999rpx;
  background: #ffffff;
}

.ai-brand-eye.left {
  left: 22rpx;
}

.ai-brand-eye.right {
  right: 22rpx;
}

.ai-hero-desc {
  margin-top: 18rpx;
  color: #5d708b;
  font-size: 24rpx;
  line-height: 1.55;
}

.ai-model-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  margin-top: 22rpx;
  padding: 18rpx;
  border: 1rpx solid #e5edf7;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.88);
}

.ai-model-label {
  color: #64748b;
  font-size: 21rpx;
  font-weight: 700;
}

.ai-model-name {
  margin-top: 6rpx;
  color: #0f172a;
  font-size: 27rpx;
  font-weight: 900;
}

.ai-model-state {
  flex-shrink: 0;
  min-height: 42rpx;
  padding: 0 16rpx;
  border-radius: 999rpx;
  color: #b45309;
  background: #fff7ed;
  font-size: 21rpx;
  font-weight: 900;
  line-height: 42rpx;
}

.ai-model-state.ready {
  color: #047857;
  background: #ecfdf5;
}

.ai-section-desc {
  margin-top: 4rpx;
  color: #64748b;
  font-size: 22rpx;
  line-height: 1.35;
}

.ai-prompt-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx;
}

.ai-prompt-card {
  min-width: 0;
  padding: 20rpx;
  border: 1rpx solid #edf2f8;
  border-radius: 22rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 26rpx rgba(15, 23, 42, 0.035);
}

.ai-prompt-card.active {
  border-color: #b7d4ff;
  background: #f8fbff;
}

.ai-prompt-card-hover {
  opacity: 0.86;
}

.ai-prompt-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58rpx;
  height: 58rpx;
  margin-bottom: 14rpx;
  border-radius: 20rpx;
}

.ai-prompt-title {
  color: #111827;
  font-size: 27rpx;
  font-weight: 900;
  line-height: 1.25;
}

.ai-prompt-desc {
  margin-top: 6rpx;
  color: #64748b;
  font-size: 22rpx;
  line-height: 1.35;
}

.ai-chat-card {
  margin-bottom: 18rpx;
}

.ai-chat-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18rpx;
  margin-bottom: 16rpx;
}

.ai-clear-button {
  flex-shrink: 0;
  min-width: 86rpx;
  min-height: 46rpx;
  padding: 0 14rpx;
  border: 1rpx solid #d8e2ee;
  border-radius: 999rpx;
  color: #64748b;
  background: #ffffff;
  font-size: 21rpx;
  font-weight: 800;
  line-height: 46rpx;
}

.ai-question {
  min-height: 180rpx;
}

.ai-submit {
  margin-top: 18rpx;
}

.ai-answer-card {
  margin-bottom: 0;
}

.ai-answer-content {
  margin-top: 12rpx;
  color: #475569;
  font-size: 25rpx;
  line-height: 1.6;
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
</style>
