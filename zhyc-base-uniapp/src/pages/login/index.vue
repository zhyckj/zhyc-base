<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <view class="mobile-page legacy-login-redirect"></view>
</template>

<script setup lang="ts">
import { onLoad } from '@dcloudio/uni-app';

/** 真实移动端统一登录页路径。 */
const AUTH_LOGIN_PAGE_PATH = '/pages/auth/login';

/**
 * 旧登录页兼容入口。
 *
 * <p>历史地址 {@code /pages/login/index} 不再渲染第二套登录界面，统一重定向到移动端认证登录页。</p>
 */
onLoad((options?: Record<string, string | undefined>) => {
  const returnTo = normalizeRedirectText(options?.returnTo);
  const reason = normalizeRedirectText(options?.reason)
    || (options?.loggedOut ? '已安全退出，请重新登录' : '');
  const query = [
    returnTo ? `returnTo=${encodeURIComponent(returnTo)}` : '',
    reason ? `reason=${encodeURIComponent(reason)}` : '',
  ].filter(Boolean);
  uni.reLaunch({
    url: `${AUTH_LOGIN_PAGE_PATH}${query.length ? `?${query.join('&')}` : ''}`,
  });
});

/**
 * 规范化旧登录页透传参数。
 *
 * @param value 页面参数值
 * @returns 去除首尾空格后的参数
 */
function normalizeRedirectText(value: unknown): string {
  return typeof value === 'string' ? value.trim() : '';
}
</script>

<style scoped>
.legacy-login-redirect {
  min-height: 100vh;
}
</style>
