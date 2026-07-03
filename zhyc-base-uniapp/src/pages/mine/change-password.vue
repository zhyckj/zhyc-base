<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page mobile-bottom-safe">
    <MobilePageTopBar title="修改密码" eyebrow="账号安全" fallback-url="/pages/profile/index" />
    <view class="mobile-hero compact-hero">
      <view class="mobile-hero-kicker">账号安全</view>
      <view class="mobile-title">安全校验</view>
      <view class="mobile-subtitle">当前账号：{{ accountText }}。新密码至少 8 位，修改前会进行二次确认。</view>
    </view>

    <view class="password-risk-card">
      <view class="password-risk-icon">盾</view>
      <view class="password-risk-main">
        <view class="password-risk-title">账号安全校验</view>
        <view class="password-risk-desc">提交前会校验当前账号、租户和旧密码，成功后清空本页密码输入。</view>
        <view class="password-risk-meta">
          <view
            v-for="item in passwordRiskItems"
            :key="item.label"
            :class="['password-risk-pill', { warning: !item.ready }]"
          >
            {{ item.label }} {{ item.value }}
          </view>
        </view>
      </view>
    </view>

    <view class="mobile-form-card">
      <view class="mobile-form-item">
        <view class="mobile-form-label">当前密码</view>
        <view class="password-input-row">
          <input
            v-model="oldPassword"
            class="mobile-input password-input"
            :password="!showOldPassword"
            placeholder="请输入当前密码"
          />
          <button class="password-eye" @tap="showOldPassword = !showOldPassword">
            {{ showOldPassword ? '隐' : '显' }}
          </button>
        </view>
      </view>
      <view class="mobile-form-item">
        <view class="mobile-form-label">新密码</view>
        <view class="password-input-row">
          <input
            v-model="newPassword"
            class="mobile-input password-input"
            :password="!showNewPassword"
            placeholder="请输入新密码"
          />
          <button class="password-eye" @tap="showNewPassword = !showNewPassword">
            {{ showNewPassword ? '隐' : '显' }}
          </button>
        </view>
        <view class="password-strength-card">
          <view class="password-strength-head">
            <view class="password-strength-label">密码强度</view>
            <view :class="['password-strength-text', passwordStrength.level]">{{ passwordStrength.text }}</view>
          </view>
          <view class="password-strength-track">
            <view :class="['password-strength-bar', passwordStrength.level]" :style="{ width: passwordStrength.width }" />
          </view>
        </view>
      </view>
      <view class="mobile-form-item">
        <view class="mobile-form-label">确认新密码</view>
        <view class="password-input-row">
          <input
            v-model="confirmPassword"
            class="mobile-input password-input"
            :password="!showConfirmPassword"
            placeholder="请再次输入新密码"
          />
          <button class="password-eye" @tap="showConfirmPassword = !showConfirmPassword">
            {{ showConfirmPassword ? '隐' : '显' }}
          </button>
        </view>
      </view>

      <view class="password-rule-card">
        <view
          v-for="rule in passwordRules"
          :key="rule.label"
          :class="['password-rule-item', { passed: rule.passed }]"
        >
          <view class="password-rule-dot">{{ rule.passed ? '✓' : '·' }}</view>
          <view class="password-rule-text">{{ rule.label }}</view>
        </view>
      </view>

      <view class="password-tips-card">
        <view class="password-tips-title">操作提示</view>
        <view
          v-for="tip in passwordQuickTips"
          :key="tip"
          class="password-tip-row"
        >
          <view class="password-tip-dot"></view>
          <view class="password-tip-text">{{ tip }}</view>
        </view>
      </view>

      <button class="mobile-action-button" :disabled="!canSubmitPasswordChange" @tap="submitPasswordChange">
        {{ submitting ? '提交中' : '确认修改' }}
      </button>
      <view v-if="errorMessage" class="mobile-form-alert">{{ errorMessage }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onShow } from '@dcloudio/uni-app';
import { computed, ref, watch } from 'vue';

import MobilePageTopBar from '@/components/MobilePageTopBar.vue';
import { changeMobilePassword } from '@/api/user';
import {
  getMobileUserContext,
  redirectToMobileLogin,
  requireMobileTenantId,
  requireMobileUserId,
  showConfirm,
  showMobileToast,
} from '@/utils/platform';

/** 当前密码，仅用于本次修改校验和提交前确认。 */
const oldPassword = ref('');
/** 新密码，必须满足首期密码策略最小长度要求。 */
const newPassword = ref('');
/** 重复输入的新密码，用于降低误操作风险。 */
const confirmPassword = ref('');
/** 当前密码是否明文展示。 */
const showOldPassword = ref(false);
/** 新密码是否明文展示。 */
const showNewPassword = ref(false);
/** 确认密码是否明文展示。 */
const showConfirmPassword = ref(false);
/** 密码修改提交状态，防止重复点击。 */
const submitting = ref(false);
/** 表单级错误提示。 */
const errorMessage = ref('');
/** 当前账号展示文本。 */
const accountText = computed(() => {
  const userContext = getMobileUserContext();
  return userContext.accountName && userContext.accountName !== '未登录' ? userContext.accountName : '未登录';
});
/** 修改密码风险摘要项。 */
const passwordRiskItems = computed(() => {
  const userContext = getMobileUserContext();
  return [
    { label: '账号', value: accountText.value, ready: userContext.loggedIn },
    { label: '租户', value: userContext.tenantId || '未绑定', ready: Boolean(userContext.tenantId) },
    { label: '用户', value: userContext.userId ?? '缺失', ready: userContext.userId !== null },
  ];
});
/** 修改密码必要规则清单。 */
const passwordRules = computed(() => [
  { label: '已输入当前密码', passed: Boolean(oldPassword.value) },
  { label: '新密码至少 8 位', passed: newPassword.value.length >= 8 },
  { label: '两次新密码一致', passed: Boolean(confirmPassword.value) && newPassword.value === confirmPassword.value },
  { label: '新密码不同于当前密码', passed: Boolean(newPassword.value) && oldPassword.value !== newPassword.value },
]);
/** 密码强度展示。 */
const passwordStrength = computed(() => {
  const score = calculatePasswordStrength(newPassword.value);
  if (score >= 3) {
    return { text: '强', level: 'strong', width: '100%' };
  }
  if (score >= 2) {
    return { text: '中', level: 'medium', width: '66%' };
  }
  if (score >= 1) {
    return { text: '弱', level: 'weak', width: '34%' };
  }
  return { text: '待输入', level: 'empty', width: '0%' };
});
/** 修改密码移动端操作提示。 */
const passwordQuickTips = [
  '建议使用字母、数字和符号组合。',
  '不要与旧密码或账号名称相同。',
  '公共设备修改后请及时退出登录。',
];
/** 是否允许提交修改密码。 */
const canSubmitPasswordChange = computed(() => !submitting.value && !validatePasswordForm());

onShow(() => {
  const userContext = getMobileUserContext();
  if (!userContext.loggedIn) {
    redirectToMobileLogin('请先登录移动端账号', '/pages/mine/change-password');
  }
});

watch([oldPassword, newPassword, confirmPassword], () => {
  if (errorMessage.value) {
    errorMessage.value = '';
  }
});

/**
 * 校验修改密码表单。
 *
 * @returns 校验失败原因；返回空字符串表示校验通过
 */
function validatePasswordForm(): string {
  if (!oldPassword.value) {
    return '请输入当前密码';
  }
  if (newPassword.value.length < 8) {
    return '新密码至少 8 位';
  }
  if (newPassword.value !== confirmPassword.value) {
    return '两次输入的新密码不一致';
  }
  if (oldPassword.value === newPassword.value) {
    return '新密码不能与当前密码相同';
  }
  return '';
}

/**
 * 计算移动端密码强度。
 *
 * @param password 新密码
 * @returns 强度分，0 表示未输入，数值越高强度越好
 */
function calculatePasswordStrength(password: string): number {
  if (!password) {
    return 0;
  }
  let score = password.length >= 8 ? 1 : 0;
  if (/[A-Za-z]/.test(password) && /\d/.test(password)) {
    score += 1;
  }
  if (/[^A-Za-z0-9]/.test(password)) {
    score += 1;
  }
  if (password.length >= 12) {
    score += 1;
  }
  return score;
}

/**
 * 提交修改密码请求。
 */
async function submitPasswordChange(): Promise<void> {
  errorMessage.value = validatePasswordForm();
  if (errorMessage.value) {
    return;
  }

  try {
    const userContext = getMobileUserContext();
    requireMobileUserId(userContext);
    const tenantId = requireMobileTenantId(userContext);
    if (!userContext.accountName || userContext.accountName === '未登录') {
      throw new Error('移动端登录账号缺失');
    }
    const confirmed = await showConfirm('修改密码', '二次确认：确定修改当前账号密码？');
    if (!confirmed) {
      return;
    }
    submitting.value = true;
    await changeMobilePassword({
      tenantId,
      username: userContext.accountName,
      oldPassword: oldPassword.value,
      newPassword: newPassword.value,
    });
    oldPassword.value = '';
    newPassword.value = '';
    confirmPassword.value = '';
    showMobileToast('密码修改成功', 'success');
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '密码修改失败';
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.password-risk-card {
  display: flex;
  align-items: flex-start;
  gap: 18rpx;
  margin-bottom: 18rpx;
  padding: 22rpx;
  border: 1rpx solid #dbeafe;
  border-radius: 22rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 28rpx rgba(15, 23, 42, 0.035);
}

.password-risk-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 66rpx;
  height: 66rpx;
  border-radius: 24rpx;
  color: #0969da;
  background: #eff6ff;
  font-size: 24rpx;
  font-weight: 900;
  line-height: 1;
}

.password-risk-main {
  min-width: 0;
  flex: 1;
}

.password-risk-title {
  color: #111827;
  font-size: 28rpx;
  font-weight: 900;
  line-height: 1.25;
}

.password-risk-desc {
  margin-top: 8rpx;
  color: #64748b;
  font-size: 23rpx;
  line-height: 1.45;
}

.password-risk-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 14rpx;
}

.password-risk-pill {
  max-width: 100%;
  min-height: 40rpx;
  padding: 0 12rpx;
  border-radius: 999rpx;
  color: #047857;
  background: #ecfdf5;
  font-size: 21rpx;
  font-weight: 800;
  line-height: 40rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.password-risk-pill.warning {
  color: #ad6800;
  background: #fff8e6;
}

.password-input-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.password-input {
  min-width: 0;
  flex: 1;
}

.password-eye {
  flex-shrink: 0;
  width: 76rpx;
  min-height: 76rpx;
  padding: 0;
  border: 1rpx solid #bfd4f2;
  border-radius: 18rpx;
  color: #0969da;
  background: #eff6ff;
  font-size: 24rpx;
  font-weight: 900;
  line-height: 76rpx;
}

.password-strength-card {
  margin-top: 14rpx;
  padding: 16rpx;
  border: 1rpx solid #e5edf7;
  border-radius: 18rpx;
  background: #f8fafc;
}

.password-strength-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
}

.password-strength-label {
  color: #64748b;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1.35;
}

.password-strength-text {
  font-size: 22rpx;
  font-weight: 900;
  line-height: 1.35;
}

.password-strength-text.empty {
  color: #94a3b8;
}

.password-strength-text.weak {
  color: #dc2626;
}

.password-strength-text.medium {
  color: #ad6800;
}

.password-strength-text.strong {
  color: #047857;
}

.password-strength-track {
  overflow: hidden;
  height: 12rpx;
  margin-top: 12rpx;
  border-radius: 999rpx;
  background: #e5edf7;
}

.password-strength-bar {
  height: 100%;
  border-radius: 999rpx;
  transition: width 0.18s ease;
}

.password-strength-bar.weak {
  background: #ef4444;
}

.password-strength-bar.medium {
  background: #f59e0b;
}

.password-strength-bar.strong {
  background: #10b981;
}

.password-rule-card {
  margin: 4rpx 0 20rpx;
  padding: 18rpx;
  border: 1rpx solid #e5edf7;
  border-radius: 18rpx;
  background: #f8fafc;
}

.password-rule-item {
  display: flex;
  align-items: center;
  gap: 10rpx;
  min-height: 42rpx;
  color: #64748b;
  font-size: 23rpx;
  line-height: 1.35;
}

.password-rule-item.passed {
  color: #047857;
}

.password-rule-dot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30rpx;
  height: 30rpx;
  border-radius: 50%;
  background: #eef2f7;
  font-size: 20rpx;
  font-weight: 900;
  line-height: 1;
}

.password-rule-item.passed .password-rule-dot {
  color: #ffffff;
  background: #10b981;
}

.password-tips-card {
  margin: 0 0 22rpx;
  padding: 18rpx;
  border: 1rpx solid #dbeafe;
  border-radius: 18rpx;
  background: #f8fbff;
}

.password-tips-title {
  color: #111827;
  font-size: 25rpx;
  font-weight: 900;
  line-height: 1.25;
}

.password-tip-row {
  display: flex;
  align-items: flex-start;
  gap: 10rpx;
  margin-top: 12rpx;
}

.password-tip-dot {
  flex-shrink: 0;
  width: 10rpx;
  height: 10rpx;
  margin-top: 12rpx;
  border-radius: 50%;
  background: #1677ff;
}

.password-tip-text {
  min-width: 0;
  flex: 1;
  color: #64748b;
  font-size: 23rpx;
  line-height: 1.45;
  word-break: break-word;
}
</style>
