<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="auth-page">
    <div class="auth-panel">
      <h1>{{ statusTitle }}</h1>
      <p class="auth-summary">{{ statusMessage }}</p>
      <a-space v-if="errorMessage">
        <a-button type="primary" :disabled="!returnTo" @click="goBack">返回平台</a-button>
        <a-button @click="goLogin">重新认证</a-button>
      </a-space>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { exchangeAdminOAuthCode } from '@/api/auth/oauth';
import { saveAdminRuntimeContext } from '@/utils/adminContext';
import { consumeAdminOAuthCallback, saveAdminOAuthCallbackResult } from '@/utils/adminOAuth';
import { refreshAdminPermissions } from '@/utils/permission';
import { parsePlatformTokenClaims } from '@/utils/platformToken';

/** 回调处理错误消息。 */
const errorMessage = ref('');
/** 回调处理是否仍在进行。 */
const processing = ref(true);
/** 回调完成后的后台返回路径。 */
const returnTo = ref('/dashboard');
const router = useRouter();

/** 回调状态标题。 */
const statusTitle = computed(() => {
  if (errorMessage.value) {
    return '统一认证失败';
  }
  return processing.value ? '正在完成统一认证' : '正在返回平台';
});

/** 回调状态说明。 */
const statusMessage = computed(() => errorMessage.value || (processing.value
  ? '正在通过核心平台安全交换访问令牌。'
  : '访问令牌已写入后台运行时上下文，即将返回原页面。'));

/**
 * 页面挂载时消费认证中心回调参数。
 */
onMounted(async () => {
  try {
    const result = consumeAdminOAuthCallback(window.location.search);
    saveAdminOAuthCallbackResult(result);
    returnTo.value = result.returnTo;
    const tokenResponse = await exchangeAdminOAuthCode({
      code: result.code,
      redirectUri: `${window.location.origin}/auth/callback`,
      codeVerifier: result.codeVerifier,
    });
    const tokenClaims = parsePlatformTokenClaims(tokenResponse.accessToken);
    if (!tokenClaims) {
      throw new Error('统一认证访问令牌缺少平台 Claims');
    }
    saveAdminRuntimeContext({
      tenantId: tokenClaims.tenantId,
      userId: tokenClaims.userId,
      orgId: null,
      accountName: tokenClaims.accountName,
      accessToken: tokenResponse.accessToken,
      refreshToken: tokenResponse.refreshToken,
      accessTokenExpiresAt: calculateAccessTokenExpiresAt(tokenResponse.expiresIn),
    });
    await refreshAdminPermissions();
    await router.replace(returnTo.value);
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '统一认证回调处理失败';
    processing.value = false;
  }
});

/**
 * 返回认证前页面。
 */
function goBack(): void {
  void router.push(returnTo.value);
}

/**
 * 重新进入统一认证登录页。
 */
function goLogin(): void {
  void router.push({ path: '/login', query: { returnTo: returnTo.value } });
}

/**
 * 计算访问令牌过期时间。
 *
 * @param expiresIn 访问令牌有效期秒数
 * @returns 访问令牌过期时间戳，无法判断时返回 null
 */
function calculateAccessTokenExpiresAt(expiresIn?: number): number | null {
  return typeof expiresIn === 'number' && Number.isFinite(expiresIn) && expiresIn > 0
    ? Date.now() + expiresIn * 1000
    : null;
}
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 88px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background: #f4f6f8;
}

.auth-panel {
  width: 420px;
  max-width: 100%;
  padding: 28px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 12px 30px rgb(15 23 42 / 8%);
}

.auth-panel h1 {
  margin: 0 0 8px;
  color: #1f2937;
  font-size: 22px;
  font-weight: 600;
}

.auth-summary {
  margin: 0 0 24px;
  color: #64748b;
  font-size: 14px;
}
</style>
