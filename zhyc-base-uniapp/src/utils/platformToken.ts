/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

/**
 * 平台访问令牌 Claims。
 */
export interface PlatformTokenClaims {
  /** 当前租户编码；来自认证中心签发的 tenant_id Claim，用于端侧上下文回填。 */
  tenantId: string;
  /** 当前用户 ID；来自认证中心签发的 user_id Claim，用于端侧上下文回填。 */
  userId: number;
  /** 当前账号名称；来自认证中心签发的 preferred_username Claim，用于端侧展示。 */
  accountName: string;
}

/**
 * 解析平台访问令牌中的端侧上下文 Claims。
 *
 * <p>该函数只用于移动端回填租户、用户和账号展示信息；服务端仍然负责 JWT 签名、签发方、权限和租户隔离校验。</p>
 *
 * @param accessToken OAuth2/OIDC Access Token
 * @returns 完整平台 Claims；令牌格式非法或缺少必要 Claim 时返回 null
 */
export function parsePlatformTokenClaims(accessToken: string): PlatformTokenClaims | null {
  const payloadSegment = normalizeText(accessToken).split('.')[1];
  if (!payloadSegment) {
    return null;
  }

  try {
    const payload = JSON.parse(decodeBase64Url(payloadSegment)) as Record<string, unknown>;
    const tenantId = normalizeText(payload.tenant_id);
    const userId = normalizePositiveUserId(payload.user_id);
    const accountName = normalizeText(payload.preferred_username);
    if (!tenantId || userId === null || !accountName) {
      return null;
    }
    return { tenantId, userId, accountName };
  } catch {
    return null;
  }
}

/**
 * 解码 JWT Base64URL 片段。
 *
 * @param value Base64URL 编码文本
 * @returns UTF-8 原文
 */
function decodeBase64Url(value: string): string {
  const normalizedValue = value.replace(/-/g, '+').replace(/_/g, '/');
  const paddedValue = normalizedValue.padEnd(normalizedValue.length + ((4 - (normalizedValue.length % 4)) % 4), '=');
  const binaryText = decodeBase64(paddedValue);
  if (typeof TextDecoder !== 'undefined') {
    const bytes = Uint8Array.from(binaryText, (char) => char.charCodeAt(0));
    return new TextDecoder('utf-8').decode(bytes);
  }
  return decodeURIComponent(
    Array.from(binaryText)
      .map((char) => `%${char.charCodeAt(0).toString(16).padStart(2, '0')}`)
      .join(''),
  );
}

/**
 * 兼容浏览器和跨端运行时的 Base64 解码。
 *
 * @param value 标准 Base64 编码文本
 * @returns 二进制字符串
 */
function decodeBase64(value: string): string {
  if (typeof atob === 'function') {
    return atob(value);
  }
  const alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';
  let output = '';
  let buffer = 0;
  let bits = 0;
  for (const char of value.replace(/=+$/, '')) {
    const index = alphabet.indexOf(char);
    if (index < 0) {
      throw new Error('非法 Base64 字符');
    }
    buffer = (buffer << 6) | index;
    bits += 6;
    if (bits >= 8) {
      bits -= 8;
      output += String.fromCharCode((buffer >> bits) & 0xff);
    }
  }
  return output;
}

/**
 * 标准化文本 Claim。
 *
 * @param value 原始 Claim 值
 * @returns 去除首尾空白后的文本
 */
function normalizeText(value: unknown): string {
  return typeof value === 'string' ? value.trim() : '';
}

/**
 * 标准化用户 ID Claim。
 *
 * @param value 原始用户 ID Claim
 * @returns 有效用户 ID 或 null
 */
function normalizePositiveUserId(value: unknown): number | null {
  const parsedValue = Number(value);
  return Number.isInteger(parsedValue) && parsedValue > 0 ? parsedValue : null;
}
