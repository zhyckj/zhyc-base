/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

/**
 * AI 供应商默认基础地址映射。
 *
 * <p>用于后台管理端在选择供应商类型时自动带出常用 OpenAI 兼容地址；用户仍可在表单中手动修改。</p>
 */
export const AI_PROVIDER_DEFAULT_BASE_URLS: Record<string, string> = {
  openai_compatible: '',
  dashscope: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
  volcengine: 'https://ark.cn-beijing.volces.com/api/v3',
  deepseek: 'https://api.deepseek.com',
  zhipu: 'https://open.bigmodel.cn/api/paas/v4',
  local: 'http://127.0.0.1:11434/v1',
};

/**
 * 获取 AI 供应商类型的默认基础地址。
 *
 * @param providerType 供应商类型编码
 * @returns 默认基础地址，没有默认值时返回空字符串
 */
export function getAiProviderDefaultBaseUrl(providerType: string): string {
  return AI_PROVIDER_DEFAULT_BASE_URLS[providerType] ?? '';
}

/**
 * 判断是否允许自动覆盖基础地址。
 *
 * <p>仅在用户未手动修改时覆盖；如果当前地址为空，或者仍等于上一个供应商类型的默认地址，则认为可自动替换。</p>
 *
 * @param currentBaseUrl 当前基础地址
 * @param previousProviderType 上一个供应商类型
 * @param userTouchedBaseUrl 用户是否手动修改过基础地址
 * @returns 可以自动带出默认地址时返回 {@code true}
 */
export function shouldApplyAiProviderDefaultBaseUrl(
  currentBaseUrl: string,
  previousProviderType: string,
  userTouchedBaseUrl: boolean,
): boolean {
  if (userTouchedBaseUrl) {
    return false;
  }
  const normalizedBaseUrl = currentBaseUrl.trim();
  if (!normalizedBaseUrl) {
    return true;
  }
  return normalizedBaseUrl === getAiProviderDefaultBaseUrl(previousProviderType);
}
