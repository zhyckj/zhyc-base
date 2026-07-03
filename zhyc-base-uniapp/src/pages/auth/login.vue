<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page mobile-login-page">
    <view class="login-appbar">
      <view class="login-brand-mark" aria-hidden="true">
        <view class="login-brand-core"></view>
        <view class="login-brand-node"></view>
      </view>
      <view class="login-brand-copy">
        <view class="login-brand-title">ZHYC 移动办公</view>
        <view class="login-brand-desc">统一认证 · 移动工作台</view>
      </view>
    </view>

    <view class="login-hero">
      <view class="login-hero-main">
        <view class="login-kicker">账号登录</view>
        <view class="login-title">登录后同步待办和消息</view>
        <view class="login-subtitle">使用统一认证账号进入移动端，租户、用户和令牌上下文由认证中心签发。</view>
      </view>
      <view class="login-visual" aria-hidden="true">
        <view class="login-visual-card primary"></view>
        <view class="login-visual-card secondary"></view>
        <view class="login-visual-card accent"></view>
      </view>
    </view>

    <view class="mobile-form-card login-form-card">
      <view class="mobile-form-item">
        <view class="mobile-form-label">账号</view>
        <input
          v-model="formState.username"
          class="mobile-input login-input"
          placeholder="请输入统一认证账号"
          confirm-type="next"
          :disabled="submitting"
        />
      </view>

      <view class="mobile-form-item">
        <view class="mobile-form-label">密码</view>
        <view class="login-password-row">
          <input
            v-model="formState.password"
            class="mobile-input login-password-input"
            placeholder="请输入密码"
            :password="!passwordVisible"
            confirm-type="done"
            :disabled="submitting"
            @confirm="submitLogin"
          />
          <button class="login-password-toggle" :disabled="submitting" @tap="passwordVisible = !passwordVisible">
            {{ passwordVisible ? '隐藏' : '显示' }}
          </button>
        </view>
      </view>

      <view v-if="reasonText" class="mobile-form-alert login-reason">{{ reasonText }}</view>
      <view v-if="errorMessage" class="mobile-form-alert">{{ errorMessage }}</view>

      <button class="mobile-action-button login-submit" :disabled="submitting" :loading="submitting" @tap="submitLogin">
        登录移动端
      </button>
    </view>

    <view class="login-security-card">
      <view class="login-security-title">安全说明</view>
      <view class="login-security-text">密码只用于本次认证校验，移动端仅保存访问令牌和租户用户上下文。</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onLoad } from '@dcloudio/uni-app';
import { reactive, ref } from 'vue';

import { loginMobileAccount } from '@/api/auth';
import { redirectAfterMobileLogin, saveMobileUserContext, showMobileToast } from '@/utils/platform';

/**
 * 移动端登录表单状态。
 */
interface MobileLoginFormState {
  /** 统一认证登录账号。 */
  username: string;
  /** 统一认证登录密码。 */
  password: string;
}

/** 登录表单。 */
const formState = reactive<MobileLoginFormState>({
  username: '',
  password: '',
});
/** 是否正在提交登录。 */
const submitting = ref(false);
/** 密码是否明文显示。 */
const passwordVisible = ref(false);
/** 登录失败提示。 */
const errorMessage = ref('');
/** 登录拦截原因。 */
const reasonText = ref('');
/** 登录成功后返回路径。 */
const returnTo = ref('/pages/workbench/index');

onLoad((query) => {
  returnTo.value = normalizeReturnTo(query?.returnTo);
  reasonText.value = decodeQueryText(query?.reason);
});

/**
 * 提交移动端登录。
 */
async function submitLogin(): Promise<void> {
  if (submitting.value) {
    return;
  }
  const username = formState.username.trim();
  const password = formState.password;
  if (!username) {
    errorMessage.value = '请输入账号';
    return;
  }
  if (!password) {
    errorMessage.value = '请输入密码';
    return;
  }
  submitting.value = true;
  errorMessage.value = '';
  try {
    const loginResult = await loginMobileAccount({ username, password });
    saveMobileUserContext({
      userId: loginResult.userId,
      orgId: null,
      accountName: loginResult.accountName,
      tenantId: loginResult.tenantId,
      roleName: '移动办公用户',
      accessToken: loginResult.accessToken,
      loggedIn: true,
    });
    formState.password = '';
    showMobileToast('登录成功', 'success');
    redirectAfterMobileLogin(returnTo.value);
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败，请稍后重试';
  } finally {
    submitting.value = false;
  }
}

/**
 * 标准化返回路径。
 *
 * @param value 原始路径
 * @returns 安全站内路径
 */
function normalizeReturnTo(value: unknown): string {
  const normalizedValue = decodeQueryText(value);
  return normalizedValue.startsWith('/pages/') ? normalizedValue : '/pages/workbench/index';
}

/**
 * 解码登录页查询参数。
 *
 * <p>H5 hash 路由在重定向时可能出现双重编码，这里最多解码两次，并保留非法编码的原始文本。</p>
 *
 * @param value 原始查询参数
 * @returns 解码后的文本
 */
function decodeQueryText(value: unknown): string {
  let decodedValue = normalizeText(value);
  for (let decodeCount = 0; decodeCount < 2 && decodedValue.includes('%'); decodeCount += 1) {
    try {
      const nextValue = decodeURIComponent(decodedValue);
      if (nextValue === decodedValue) {
        break;
      }
      decodedValue = nextValue;
    } catch {
      break;
    }
  }
  return decodedValue;
}

/**
 * 标准化文本。
 *
 * @param value 原始值
 * @returns 文本值
 */
function normalizeText(value: unknown): string {
  return typeof value === 'string' ? value.trim() : '';
}
</script>

<style scoped>
.mobile-login-page {
  min-height: 100vh;
  padding: calc(24rpx + env(safe-area-inset-top)) 28rpx calc(40rpx + env(safe-area-inset-bottom));
  background:
    radial-gradient(circle at 82% 12%, rgba(22, 119, 255, 0.12), transparent 220rpx),
    linear-gradient(180deg, #f7fbff 0%, #eef4fb 48%, #f8fafc 100%);
}

.login-appbar {
  display: flex;
  align-items: center;
  gap: 18rpx;
  min-height: 76rpx;
  margin-bottom: 24rpx;
}

.login-brand-mark {
  position: relative;
  flex-shrink: 0;
  width: 68rpx;
  height: 68rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #1677ff 0%, #14b8a6 100%);
  box-shadow: 0 14rpx 30rpx rgba(22, 119, 255, 0.22);
}

.login-brand-core {
  position: absolute;
  left: 18rpx;
  top: 18rpx;
  width: 32rpx;
  height: 32rpx;
  border-radius: 12rpx;
  background: rgba(255, 255, 255, 0.92);
}

.login-brand-node {
  position: absolute;
  right: 12rpx;
  bottom: 12rpx;
  width: 16rpx;
  height: 16rpx;
  border-radius: 999rpx;
  background: #dffcf5;
}

.login-brand-copy {
  min-width: 0;
}

.login-brand-title {
  color: #111827;
  font-size: 31rpx;
  font-weight: 900;
  line-height: 1.2;
}

.login-brand-desc {
  margin-top: 4rpx;
  color: #64748b;
  font-size: 23rpx;
  font-weight: 700;
  line-height: 1.25;
}

.login-hero {
  position: relative;
  display: flex;
  gap: 22rpx;
  overflow: hidden;
  margin-bottom: 24rpx;
  padding: 30rpx;
  border: 1rpx solid #dbeafe;
  border-radius: 28rpx;
  background: linear-gradient(145deg, #ffffff 0%, #f1f7ff 56%, #effbf8 100%);
  box-shadow: 0 16rpx 42rpx rgba(15, 23, 42, 0.06);
}

.login-hero-main {
  position: relative;
  z-index: 1;
  flex: 1;
  min-width: 0;
}

.login-kicker {
  display: inline-flex;
  align-items: center;
  min-height: 42rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  color: #0756b5;
  background: #eaf3ff;
  font-size: 23rpx;
  font-weight: 900;
}

.login-title {
  margin-top: 20rpx;
  color: #111827;
  font-size: 42rpx;
  font-weight: 900;
  line-height: 1.2;
}

.login-subtitle {
  margin-top: 12rpx;
  color: #64748b;
  font-size: 25rpx;
  font-weight: 700;
  line-height: 1.55;
}

.login-visual {
  position: relative;
  flex-shrink: 0;
  width: 150rpx;
  min-height: 180rpx;
}

.login-visual-card {
  position: absolute;
  border-radius: 28rpx;
}

.login-visual-card.primary {
  right: 0;
  top: 0;
  width: 118rpx;
  height: 118rpx;
  background: linear-gradient(135deg, #1677ff, #60a5fa);
}

.login-visual-card.secondary {
  left: 0;
  bottom: 6rpx;
  width: 92rpx;
  height: 92rpx;
  background: linear-gradient(135deg, #14b8a6, #99f6e4);
}

.login-visual-card.accent {
  right: 18rpx;
  bottom: 18rpx;
  width: 54rpx;
  height: 54rpx;
  border-radius: 20rpx;
  background: #fff7ed;
}

.login-form-card {
  margin-bottom: 20rpx;
}

.login-input,
.login-password-input {
  height: 86rpx;
  border-radius: 22rpx;
  font-size: 28rpx;
}

.login-password-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.login-password-input {
  flex: 1;
  min-width: 0;
}

.login-password-toggle {
  flex-shrink: 0;
  min-width: 100rpx;
  height: 84rpx;
  border-radius: 22rpx;
  color: #0969da;
  background: #edf5ff;
  font-size: 25rpx;
  font-weight: 900;
  line-height: 84rpx;
}

.login-reason {
  color: #92400e;
  background: #fff7ed;
}

.login-submit {
  margin-top: 24rpx;
}

.login-security-card {
  padding: 22rpx 24rpx;
  border: 1rpx solid #e2e8f0;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.78);
}

.login-security-title {
  color: #111827;
  font-size: 26rpx;
  font-weight: 900;
  line-height: 1.25;
}

.login-security-text {
  margin-top: 8rpx;
  color: #64748b;
  font-size: 23rpx;
  font-weight: 700;
  line-height: 1.5;
}
</style>
