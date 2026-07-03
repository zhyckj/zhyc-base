/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

/**
 * 移动端运行时接口配置。
 *
 * <p>H5 本地开发默认通过 Vite 代理访问核心平台和认证中心；小程序、App、支付宝小程序等目标端可通过环境变量
 * 注入完整 HTTPS 地址，避免业务页面直接拼接后端 URL。</p>
 */

/** H5 本地核心平台 API 代理前缀。 */
const DEFAULT_PLATFORM_API_BASE_URL = '/api';
/** H5 本地认证中心 API 代理前缀。 */
const DEFAULT_AUTH_API_BASE_URL = '/auth-center';

/**
 * 获取核心平台 API 基地址。
 *
 * @returns 核心平台 API 基地址
 */
export function getMobilePlatformApiBaseUrl(): string {
  return normalizeBaseUrl(import.meta.env?.VITE_MOBILE_PLATFORM_API_BASE_URL) || DEFAULT_PLATFORM_API_BASE_URL;
}

/**
 * 获取认证中心 API 基地址。
 *
 * @returns 认证中心 API 基地址
 */
export function getMobileAuthApiBaseUrl(): string {
  return normalizeBaseUrl(import.meta.env?.VITE_MOBILE_AUTH_API_BASE_URL) || DEFAULT_AUTH_API_BASE_URL;
}

/**
 * 标准化接口基地址。
 *
 * @param value 原始基地址
 * @returns 去除尾部斜杠后的基地址
 */
function normalizeBaseUrl(value: unknown): string {
  return typeof value === 'string' && value.trim() ? value.trim().replace(/\/+$/, '') : '';
}
