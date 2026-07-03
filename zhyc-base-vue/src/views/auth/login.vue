<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="auth-page">
    <div class="auth-shell">
      <aside class="auth-panel">
        <div class="brand-block">
          <span class="brand-mark">Z</span>
          <div class="brand-copy">
            <span>ZHYC 快速开发平台</span>
            <strong>企业级基础框架</strong>
          </div>
        </div>

        <div class="identity-board">
          <div class="identity-title">
            <span>FRAMEWORK</span>
            <strong>快速交付业务系统</strong>
          </div>
          <div class="advantage-grid">
            <div class="advantage-card">
              <span>LowCode</span>
              <strong>低代码建模</strong>
              <p>数据源、模型、页面和代码生成统一管理。</p>
            </div>
            <div class="advantage-card">
              <span>Security</span>
              <strong>权限与租户</strong>
              <p>菜单权限、数据范围和租户隔离内置。</p>
            </div>
            <div class="advantage-card">
              <span>Workflow</span>
              <strong>流程协同</strong>
              <p>通过工作流门面接入审批和待办能力。</p>
            </div>
            <div class="advantage-card">
              <span>OpenAPI</span>
              <strong>开放集成</strong>
              <p>API Key、签名、OAuth2 和调用审计覆盖。</p>
            </div>
          </div>
        </div>

        <div class="process-list">
          <div class="process-item process-item-active">
            <span>01</span>
            <strong>授权请求</strong>
          </div>
          <div class="process-item">
            <span>02</span>
            <strong>身份校验</strong>
          </div>
          <div class="process-item">
            <span>03</span>
            <strong>进入平台</strong>
          </div>
        </div>
      </aside>

      <main class="auth-card">
        <div class="mobile-brand">
          <span class="brand-mark">Z</span>
          <span>ZHYC 快速开发平台</span>
        </div>
        <div class="auth-heading">
          <span class="auth-badge">
            <SafetyCertificateOutlined />
            统一认证
          </span>
          <h1>{{ authRequestReady ? '账号登录' : '登录平台' }}</h1>
          <p>{{ authRequestReady ? '请输入账号密码完成身份校验' : '正在准备安全授权入口' }}</p>
        </div>

        <a-alert
          v-if="loggedOut"
          class="auth-alert"
          type="success"
          message="已安全退出，请重新登录"
          show-icon
        />
        <a-alert
          v-if="loginFailed"
          class="auth-alert"
          type="error"
          message="账号或密码错误，请重新输入"
          show-icon
        />
        <a-alert
          v-if="preparing && !authRequestReady"
          class="auth-alert"
          type="info"
          message="正在准备统一认证入口"
          show-icon
        />
        <a-alert v-if="errorMessage" class="auth-alert" type="error" :message="errorMessage" show-icon />

        <a-form
          v-if="authRequestReady"
          class="login-form"
          layout="vertical"
          :model="formState"
          @finish="submitFrontendLogin"
        >
          <a-form-item
            label="账号"
            name="username"
            :rules="[{ required: true, whitespace: true, message: '请输入账号' }]"
          >
            <a-input
              v-model:value="formState.username"
              autocomplete="username"
              placeholder="请输入账号"
              :disabled="formDisabled"
            >
              <template #prefix>
                <UserOutlined />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item
            label="密码"
            name="password"
            :rules="[{ required: true, whitespace: true, message: '请输入密码' }]"
          >
            <a-input-password
              v-model:value="formState.password"
              autocomplete="current-password"
              placeholder="请输入密码"
              :disabled="formDisabled"
            >
              <template #prefix>
                <LockOutlined />
              </template>
            </a-input-password>
          </a-form-item>

          <a-button class="login-button" type="primary" size="large" block html-type="submit" :loading="submitting">
            <template #icon>
              <LoginOutlined />
            </template>
            登录平台
          </a-button>
        </a-form>
        <div v-else class="auth-handoff">
          <p>授权请求准备完成后，会回到本页继续登录。</p>
          <a-button
            class="login-button"
            type="primary"
            size="large"
            block
            :loading="preparing"
            @click="prepareAuthorizationRequest"
          >
            <template #icon>
              <LoginOutlined />
            </template>
            登录平台
          </a-button>
        </div>

        <div class="auth-footer">
          <span>登录后返回</span>
          <strong>{{ returnToText }}</strong>
        </div>
      </main>
    </div>
  </section>
</template>

<script setup lang="ts">
import { LockOutlined, LoginOutlined, SafetyCertificateOutlined, UserOutlined } from '@ant-design/icons-vue';
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';

import {
  buildAdminOAuthConfig,
  buildAdminOAuthPasswordLoginConfig,
  createAdminOAuthAuthorizeUrl,
  submitAdminOAuthPasswordLogin,
} from '@/utils/adminOAuth';

/**
 * 前端登录表单状态。
 */
interface LoginFormState {
  /** 认证中心登录账号。 */
  username: string;
  /** 认证中心登录密码。 */
  password: string;
}

const route = useRoute();

/** 是否正在准备授权请求。 */
const preparing = ref(false);
/** 是否正在提交账号密码。 */
const submitting = ref(false);
/** 登录流程错误提示。 */
const errorMessage = ref('');
/** 账号密码表单状态。 */
const formState = reactive<LoginFormState>({
  username: '',
  password: '',
});

/** 认证中心已完成授权请求保存后回到前端的标记。 */
const authRequestReady = computed(() => route.query.authRequest === '1');

/** 退出登录后的成功提示状态。 */
const loggedOut = computed(() => route.query.loggedOut === '1');

/** 登录失败提示状态。 */
const loginFailed = computed(() => route.query.error === '1' || route.query.error === 'true');

/** 登录成功后的后台返回路径。 */
const returnTo = computed(() => (typeof route.query.returnTo === 'string' ? route.query.returnTo : '/dashboard'));

/** 返回路径展示文本，避免过长路径撑开登录面板。 */
const returnToText = computed(() => (returnTo.value === '/dashboard' ? '个人工作台' : returnTo.value));

/** 表单是否需要禁用。 */
const formDisabled = computed(() => preparing.value || submitting.value || !authRequestReady.value);

onMounted(() => {
  if (!authRequestReady.value && !loggedOut.value) {
    void prepareAuthorizationRequest();
  }
});

/**
 * 准备 OAuth2 授权请求并跳转认证中心。
 */
async function prepareAuthorizationRequest(): Promise<void> {
  if (preparing.value) {
    return;
  }
  preparing.value = true;
  errorMessage.value = '';
  try {
    const authorizeUrl = await createAdminOAuthAuthorizeUrl(buildAdminOAuthConfig(returnTo.value));
    window.location.assign(authorizeUrl);
  } catch (error) {
    preparing.value = false;
    errorMessage.value = error instanceof Error ? error.message : '登录流程初始化失败';
  }
}

/**
 * 提交统一认证账号密码表单。
 */
function submitFrontendLogin(): void {
  if (!authRequestReady.value) {
    void prepareAuthorizationRequest();
    return;
  }
  submitting.value = true;
  errorMessage.value = '';
  try {
    submitAdminOAuthPasswordLogin(
      {
        username: formState.username,
        password: formState.password,
      },
      buildAdminOAuthPasswordLoginConfig(),
    );
  } catch (error) {
    submitting.value = false;
    errorMessage.value = error instanceof Error ? error.message : '登录提交失败';
  }
}
</script>

<style scoped>
.auth-page {
  position: relative;
  box-sizing: border-box;
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 36px 24px;
  overflow: hidden;
  background:
    linear-gradient(135deg, rgb(255 255 255 / 0%) 0 42%, rgb(15 118 110 / 7%) 42% 43%, rgb(255 255 255 / 0%) 43% 100%),
    linear-gradient(22deg, rgb(49 90 183 / 0%) 0 58%, rgb(49 90 183 / 6%) 58% 59%, rgb(49 90 183 / 0%) 59% 100%),
    linear-gradient(90deg, rgb(15 118 110 / 5%) 1px, transparent 1px),
    linear-gradient(180deg, rgb(49 90 183 / 5%) 1px, transparent 1px),
    #f4f7fb;
  background-size: auto, auto, 56px 56px, 56px 56px, auto;
  animation: backgroundDrift 24s ease-in-out infinite alternate;
}

.auth-page *,
.auth-page *::before,
.auth-page *::after {
  box-sizing: border-box;
}

.auth-page::before {
  position: absolute;
  inset: 0;
  pointer-events: none;
  content: '';
  background:
    linear-gradient(120deg, transparent 0 16%, rgb(65 214 195 / 13%) 16% 16.4%, transparent 16.4% 100%),
    linear-gradient(120deg, transparent 0 24%, rgb(49 90 183 / 11%) 24% 24.3%, transparent 24.3% 100%),
    linear-gradient(120deg, transparent 0 32%, rgb(15 118 110 / 9%) 32% 32.2%, transparent 32.2% 100%);
  opacity: 0.8;
  transform: translate3d(-2%, 0, 0);
  animation: circuitFlow 18s ease-in-out infinite alternate;
}

.auth-shell {
  position: relative;
  z-index: 1;
  width: min(1040px, 100%);
  min-height: 600px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 430px;
  overflow: hidden;
  border: 1px solid rgb(220 229 238 / 86%);
  border-radius: 8px;
  background: rgb(255 255 255 / 92%);
  box-shadow: 0 28px 76px rgb(18 32 54 / 14%);
  backdrop-filter: blur(18px);
  animation:
    shellEnter 560ms cubic-bezier(0.2, 0.8, 0.2, 1) both,
    shellFloat 8s ease-in-out 700ms infinite;
  will-change: transform;
}

.auth-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
  padding: 42px;
  color: #172033;
  border-right: 1px solid #e1e9f2;
  background:
    linear-gradient(145deg, rgb(255 255 255 / 72%) 0 40%, rgb(237 251 247 / 62%) 100%),
    linear-gradient(90deg, rgb(65 214 195 / 0%) 0 18%, rgb(65 214 195 / 10%) 18% 18.4%, rgb(65 214 195 / 0%) 18.4% 100%),
    linear-gradient(155deg, rgb(49 90 183 / 0%) 0 64%, rgb(49 90 183 / 8%) 64% 64.35%, rgb(49 90 183 / 0%) 64.35% 100%),
    linear-gradient(135deg, #ffffff 0%, #f8fbff 46%, #eefbf8 100%),
    #ffffff;
}

.auth-panel::before {
  position: absolute;
  inset: 0;
  content: '';
  background:
    linear-gradient(90deg, rgb(15 118 110 / 7%) 1px, transparent 1px),
    linear-gradient(180deg, rgb(37 99 235 / 5%) 1px, transparent 1px);
  background-size: 48px 48px;
  opacity: 0.7;
}

.auth-panel::after {
  position: absolute;
  right: 34px;
  bottom: 128px;
  width: 180px;
  height: 180px;
  pointer-events: none;
  content: '';
  background:
    linear-gradient(90deg, transparent 0 46%, rgb(15 118 110 / 18%) 46% 47%, transparent 47% 100%),
    linear-gradient(0deg, transparent 0 46%, rgb(49 90 183 / 15%) 46% 47%, transparent 47% 100%),
    linear-gradient(45deg, transparent 0 49%, rgb(65 214 195 / 16%) 49% 50%, transparent 50% 100%);
  opacity: 0.55;
}

.brand-block,
.identity-board,
.process-list {
  position: relative;
  z-index: 1;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 14px;
  animation: fadeSlideUp 520ms ease-out 120ms both;
}

.brand-mark {
  flex: 0 0 auto;
  width: 42px;
  height: 42px;
  display: inline-grid;
  place-items: center;
  border: 1px solid #d6eee9;
  border-radius: 8px;
  color: #ffffff;
  background: #0f766e;
  font-size: 21px;
  font-weight: 700;
}

.brand-copy {
  min-width: 0;
  display: grid;
  gap: 4px;
}

.brand-copy span {
  color: #64748b;
  font-size: 13px;
}

.brand-copy strong {
  color: #172033;
  font-size: 20px;
  font-weight: 700;
}

.identity-board {
  width: min(420px, 100%);
  margin-top: 44px;
  padding: 24px;
  border: 1px solid rgb(209 223 236 / 82%);
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgb(255 255 255 / 88%), rgb(248 252 255 / 70%)),
    rgb(255 255 255 / 78%);
  box-shadow:
    0 18px 40px rgb(18 32 54 / 10%),
    inset 0 1px 0 rgb(255 255 255 / 86%);
  backdrop-filter: blur(10px);
  animation: fadeSlideUp 600ms ease-out 180ms both;
  transition:
    border-color 180ms ease,
    box-shadow 180ms ease,
    transform 220ms ease;
  will-change: transform;
}

.identity-board:hover {
  border-color: rgb(65 214 195 / 58%);
  box-shadow:
    0 22px 46px rgb(18 32 54 / 12%),
    inset 0 1px 0 rgb(255 255 255 / 90%);
  transform: translate3d(0, -2px, 0);
}

.identity-title {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.identity-title span {
  color: #41d6c3;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0;
}

.identity-title strong {
  max-width: 270px;
  color: #172033;
  font-size: 24px;
  line-height: 1.18;
  text-align: right;
  white-space: nowrap;
}

.advantage-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 28px;
}

.advantage-card {
  position: relative;
  min-width: 0;
  min-height: 128px;
  padding: 14px;
  overflow: hidden;
  border: 1px solid #e3edf6;
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgb(255 255 255 / 98%), rgb(250 253 255 / 94%)),
    #ffffff;
  transition:
    border-color 180ms ease,
    box-shadow 180ms ease,
    transform 180ms ease;
}

.advantage-card::after {
  position: absolute;
  right: 12px;
  bottom: 10px;
  width: 42px;
  height: 26px;
  pointer-events: none;
  content: '';
  border-right: 1px solid rgb(15 118 110 / 16%);
  border-bottom: 1px solid rgb(15 118 110 / 16%);
  opacity: 0.85;
  transition: opacity 180ms ease, transform 180ms ease;
}

.advantage-card:hover {
  border-color: rgb(65 214 195 / 48%);
  box-shadow: 0 12px 28px rgb(18 32 54 / 8%);
  transform: translate3d(0, -2px, 0);
}

.advantage-card:hover::after {
  opacity: 1;
  transform: translate3d(-3px, -3px, 0);
}

.advantage-card span {
  display: block;
  margin-bottom: 10px;
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
}

.advantage-card strong {
  display: block;
  color: #172033;
  font-size: 15px;
  font-weight: 700;
}

.advantage-card p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 12px;
  line-height: 1.55;
}

.advantage-card:nth-child(2) span {
  color: #315ab7;
}

.advantage-card:nth-child(3) span {
  color: #a15c07;
}

.advantage-card:nth-child(4) span {
  color: #7c3aed;
}

.process-list {
  display: flex;
  gap: 10px;
  margin-top: 34px;
  animation: fadeSlideUp 620ms ease-out 260ms both;
}

.process-item {
  flex: 1;
  min-width: 0;
  padding: 12px;
  border: 1px solid #dce5ee;
  border-radius: 8px;
  background: rgb(255 255 255 / 72%);
  transition:
    border-color 180ms ease,
    background-color 180ms ease,
    transform 180ms ease;
}

.process-item:hover {
  border-color: #9de5dc;
  transform: translate3d(0, -1px, 0);
}

.process-item span,
.process-item strong {
  display: block;
}

.process-item span {
  margin-bottom: 8px;
  color: #94a3b8;
  font-size: 12px;
}

.process-item strong {
  overflow: hidden;
  color: #334155;
  font-size: 13px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.process-item-active {
  border-color: #9de5dc;
  background: #eefcf9;
}

.auth-card {
  position: relative;
  width: 100%;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 52px 44px;
  background:
    linear-gradient(145deg, rgb(255 255 255 / 95%), rgb(255 255 255 / 88%)),
    #ffffff;
}

.auth-card::before {
  position: absolute;
  inset: 0;
  pointer-events: none;
  content: '';
  background:
    linear-gradient(90deg, transparent 0 68%, rgb(15 118 110 / 7%) 68% 68.35%, transparent 68.35% 100%),
    linear-gradient(180deg, transparent 0 24%, rgb(49 90 183 / 6%) 24% 24.25%, transparent 24.25% 100%);
  opacity: 0.75;
}

.auth-card > * {
  position: relative;
  z-index: 1;
}

.mobile-brand {
  display: none;
  align-items: center;
  gap: 10px;
  margin-bottom: 28px;
  color: #334155;
  font-size: 14px;
  font-weight: 700;
}

.auth-heading {
  margin-bottom: 26px;
  animation: fadeSlideUp 540ms ease-out 180ms both;
}

.auth-badge {
  width: fit-content;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 16px;
  padding: 5px 10px;
  border: 1px solid #c8ede7;
  border-radius: 999px;
  color: #0f766e;
  background: #effcf9;
  font-size: 13px;
  font-weight: 600;
}

.auth-card h1 {
  margin: 0;
  color: #172033;
  font-size: 30px;
  font-weight: 700;
}

.auth-heading p {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
}

.auth-alert {
  margin-bottom: 16px;
  animation: alertIn 240ms ease-out both;
}

.login-form :deep(.ant-form-item-label > label) {
  color: #334155;
  font-weight: 600;
}

.login-form,
.auth-handoff {
  animation: fadeSlideUp 560ms ease-out 260ms both;
}

.login-form :deep(.ant-input),
.login-form :deep(.ant-input-password),
.login-form :deep(.ant-input-affix-wrapper) {
  min-height: 44px;
  border-radius: 6px;
  transition:
    border-color 160ms ease,
    box-shadow 180ms ease,
    background-color 180ms ease,
    transform 180ms ease;
}

.login-form :deep(.ant-input-affix-wrapper-focused),
.login-form :deep(.ant-input:focus),
.login-form :deep(.ant-input-focused) {
  border-color: #14b8a6;
  box-shadow:
    0 0 0 3px rgb(20 184 166 / 12%),
    0 10px 22px rgb(18 32 54 / 6%);
  transform: translate3d(0, -1px, 0);
}

.login-form :deep(.ant-form-item-has-error) {
  animation: fieldErrorNudge 260ms ease-out both;
}

.login-form :deep(.ant-form-item-has-error .ant-input),
.login-form :deep(.ant-form-item-has-error .ant-input-affix-wrapper) {
  box-shadow: 0 0 0 3px rgb(255 77 79 / 10%);
}

.login-button {
  height: 46px;
  border-radius: 6px;
  font-weight: 600;
  box-shadow: 0 10px 22px rgb(47 120 255 / 20%);
  transition:
    box-shadow 160ms ease,
    filter 160ms ease,
    transform 120ms ease;
}

.login-button:hover {
  filter: saturate(1.06) brightness(1.02);
  box-shadow: 0 14px 28px rgb(47 120 255 / 24%);
  transform: translate3d(0, -1px, 0);
}

.login-button:active {
  box-shadow: 0 8px 18px rgb(47 120 255 / 20%);
  transform: translate3d(0, 1px, 0) scale(0.995);
}

.auth-handoff {
  padding: 18px;
  border: 1px solid #d8e0ea;
  border-radius: 8px;
  background: #f8fafc;
}

.auth-handoff p {
  margin: 0 0 16px;
  color: #475569;
  font-size: 14px;
  line-height: 1.7;
}

.auth-footer {
  min-width: 0;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
  color: #64748b;
  font-size: 13px;
  animation: fadeSlideUp 580ms ease-out 320ms both;
}

.auth-footer strong {
  min-width: 0;
  overflow: hidden;
  color: #334155;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 860px) {
  .auth-page {
    padding: 24px 16px;
  }

  .auth-shell {
    min-height: auto;
    grid-template-columns: 1fr;
  }

  .auth-panel {
    min-height: 340px;
    padding: 30px;
  }

  .identity-board {
    margin-top: 28px;
  }

  .auth-card {
    padding: 32px 24px;
  }
}

@media (max-width: 520px) {
  .auth-page {
    display: block;
    padding: 16px;
    background:
      linear-gradient(90deg, rgb(15 118 110 / 5%) 1px, transparent 1px),
      linear-gradient(180deg, rgb(49 90 183 / 5%) 1px, transparent 1px),
      #f4f7fb;
    background-size: 42px 42px;
  }

  .auth-shell {
    width: calc(100vw - 32px);
    max-width: calc(100vw - 32px);
    min-height: calc(100vh - 32px);
    border-radius: 8px;
  }

  .auth-panel {
    display: none;
  }

  .mobile-brand {
    display: flex;
  }

  .mobile-brand .brand-mark {
    width: 34px;
    height: 34px;
    font-size: 18px;
  }

  .auth-card {
    width: 100%;
    max-width: 100%;
    min-height: calc(100vh - 34px);
    margin: 0 auto;
    padding: 28px 22px;
    overflow: hidden;
  }

  .login-form,
  .auth-handoff,
  .login-button {
    width: min(100%, calc(100vw - 78px));
    max-width: calc(100vw - 78px);
  }

  .auth-footer {
    width: min(100%, calc(100vw - 78px));
  }

  .auth-footer {
    flex-direction: column;
    gap: 4px;
  }

  .auth-card h1 {
    font-size: 26px;
  }
}

@media (prefers-color-scheme: dark) {
  .auth-page {
    background:
      linear-gradient(135deg, rgb(255 255 255 / 0%) 0 42%, rgb(65 214 195 / 11%) 42% 43%, rgb(255 255 255 / 0%) 43% 100%),
      linear-gradient(22deg, rgb(49 90 183 / 0%) 0 58%, rgb(79 124 255 / 12%) 58% 59%, rgb(49 90 183 / 0%) 59% 100%),
      linear-gradient(90deg, rgb(65 214 195 / 8%) 1px, transparent 1px),
      linear-gradient(180deg, rgb(79 124 255 / 8%) 1px, transparent 1px),
      #0f172a;
    background-size: auto, auto, 56px 56px, 56px 56px, auto;
  }

  .auth-page::before {
    opacity: 0.42;
  }

  .auth-shell {
    border-color: rgb(148 163 184 / 28%);
    background: rgb(15 23 42 / 82%);
    box-shadow: 0 30px 86px rgb(0 0 0 / 34%);
  }

  .auth-panel {
    color: #e5edf7;
    border-right-color: rgb(148 163 184 / 18%);
    background:
      linear-gradient(145deg, rgb(15 23 42 / 70%) 0 40%, rgb(9 38 39 / 58%) 100%),
      linear-gradient(90deg, rgb(65 214 195 / 0%) 0 18%, rgb(65 214 195 / 12%) 18% 18.4%, rgb(65 214 195 / 0%) 18.4% 100%),
      linear-gradient(155deg, rgb(79 124 255 / 0%) 0 64%, rgb(79 124 255 / 11%) 64% 64.35%, rgb(79 124 255 / 0%) 64.35% 100%),
      #111827;
  }

  .brand-mark {
    border-color: rgb(65 214 195 / 28%);
    background: #0f766e;
  }

  .brand-copy span,
  .advantage-card p,
  .auth-heading p,
  .auth-footer {
    color: #94a3b8;
  }

  .brand-copy strong,
  .identity-title strong,
  .advantage-card strong,
  .auth-card h1,
  .mobile-brand,
  .auth-footer strong {
    color: #e5edf7;
  }

  .identity-board,
  .advantage-card,
  .process-item,
  .auth-handoff {
    border-color: rgb(148 163 184 / 22%);
    background:
      linear-gradient(135deg, rgb(30 41 59 / 78%), rgb(15 23 42 / 68%)),
      rgb(15 23 42 / 72%);
    box-shadow:
      0 18px 42px rgb(0 0 0 / 22%),
      inset 0 1px 0 rgb(255 255 255 / 6%);
  }

  .process-item-active,
  .auth-badge {
    border-color: rgb(65 214 195 / 38%);
    color: #5eead4;
    background: rgb(20 184 166 / 12%);
  }

  .process-item strong,
  .login-form :deep(.ant-form-item-label > label) {
    color: #dbe7f3;
  }

  .auth-card {
    background:
      linear-gradient(145deg, rgb(15 23 42 / 88%), rgb(17 24 39 / 82%)),
      #111827;
  }

  .login-form :deep(.ant-input),
  .login-form :deep(.ant-input-password),
  .login-form :deep(.ant-input-affix-wrapper) {
    color: #e5edf7;
    border-color: rgb(148 163 184 / 34%);
    background: rgb(15 23 42 / 72%);
  }

  .login-form :deep(.ant-input::placeholder) {
    color: #64748b;
  }
}

@media (prefers-reduced-motion: reduce) {
  .auth-page,
  .auth-page::before,
  .auth-shell,
  .brand-block,
  .identity-board,
  .process-list,
  .auth-heading,
  .auth-alert,
  .login-form,
  .auth-handoff,
  .auth-footer,
  .login-form :deep(.ant-form-item-has-error) {
    animation: none;
  }

  .identity-board,
  .advantage-card,
  .process-item,
  .login-button,
  .login-form :deep(.ant-input),
  .login-form :deep(.ant-input-password),
  .login-form :deep(.ant-input-affix-wrapper) {
    transition: none;
  }
}

@keyframes backgroundDrift {
  0% {
    background-position: 0 0, 0 0, 0 0, 0 0, 0 0;
  }

  100% {
    background-position: 0 0, 0 0, 24px 18px, -18px 26px, 0 0;
  }
}

@keyframes circuitFlow {
  0% {
    transform: translate3d(-2%, 0, 0);
  }

  100% {
    transform: translate3d(2%, -1%, 0);
  }
}

@keyframes shellEnter {
  0% {
    opacity: 0;
    transform: translate3d(0, 18px, 0) scale(0.985);
  }

  100% {
    opacity: 1;
    transform: translate3d(0, 0, 0) scale(1);
  }
}

@keyframes shellFloat {
  0%,
  100% {
    transform: translate3d(0, 0, 0);
  }

  50% {
    transform: translate3d(0, -4px, 0);
  }
}

@keyframes fadeSlideUp {
  0% {
    opacity: 0;
    transform: translate3d(0, 10px, 0);
  }

  100% {
    opacity: 1;
    transform: translate3d(0, 0, 0);
  }
}

@keyframes alertIn {
  0% {
    opacity: 0;
    transform: translate3d(0, -4px, 0);
  }

  100% {
    opacity: 1;
    transform: translate3d(0, 0, 0);
  }
}

@keyframes fieldErrorNudge {
  0%,
  100% {
    transform: translate3d(0, 0, 0);
  }

  28% {
    transform: translate3d(-4px, 0, 0);
  }

  56% {
    transform: translate3d(4px, 0, 0);
  }

  78% {
    transform: translate3d(-2px, 0, 0);
  }
}
</style>
