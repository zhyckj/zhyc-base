/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { createSSRApp } from 'vue';

import App from './App.vue';
import { guardMobileAuthenticatedPage } from './utils/platform';

export function createApp() {
  const app = createSSRApp(App);
  app.mixin({
    /**
     * 页面展示时统一校验移动端登录上下文。
     */
    onShow() {
      guardMobileAuthenticatedPage();
    },
  });
  return {
    app,
  };
}
