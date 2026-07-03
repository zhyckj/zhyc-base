/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

declare module '*.vue' {
  import type { DefineComponent } from 'vue';

  const component: DefineComponent<Record<string, unknown>, Record<string, unknown>, unknown>;
  export default component;
}

declare const uni: {
  request<T = unknown>(options: UniRequestOptions<T>): void;
  navigateTo(options: { url: string }): void;
  reLaunch(options: { url: string }): void;
  switchTab(options: { url: string }): void;
  showModal(options: {
    title: string;
    content: string;
    confirmText?: string;
    cancelText?: string;
    success: (result: { confirm: boolean; cancel: boolean }) => void;
    fail?: () => void;
  }): void;
  showToast(options: {
    title: string;
    icon?: 'success' | 'error' | 'none';
  }): void;
  getSystemInfoSync(): { uniPlatform?: string; platform?: string; windowWidth: number };
  getStorageSync(key: string): unknown;
  setStorageSync(key: string, value: unknown): void;
  removeStorageSync(key: string): void;
};

declare function getCurrentPages(): Array<{ route?: string; options?: Record<string, unknown> }>;

interface ImportMeta {
  readonly env?: Record<string, string | undefined>;
}

interface UniRequestOptions<T> {
  url: string;
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  data?: unknown;
  header?: Record<string, string>;
  success: (result: { statusCode: number; data: T }) => void;
  fail: (error: { errMsg?: string }) => void;
}
