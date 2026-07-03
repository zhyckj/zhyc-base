/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.util;

/**
 * 敏感信息脱敏帮助类。
 *
 * <p>用于日志、审计、列表展示等场景，统一处理手机号、邮箱、密钥等敏感值的展示规则。</p>
 */
public final class SensitiveMaskHelper {

  /** 默认掩码文本。 */
  private static final String DEFAULT_MASK = "****";

  private SensitiveMaskHelper() {
  }

  /**
   * 对文本中间部分进行脱敏。
   *
   * @param value 原始文本
   * @param prefixLength 保留前缀长度
   * @param suffixLength 保留后缀长度
   * @return 脱敏后的文本
   */
  public static String maskMiddle(String value, int prefixLength, int suffixLength) {
    if (prefixLength < 0 || suffixLength < 0) {
      throw new IllegalArgumentException("脱敏保留长度不能为负数");
    }
    String normalized = TextHelper.trimToEmpty(value);
    if (normalized.isEmpty()) {
      return "";
    }
    if (prefixLength + suffixLength <= 0 || normalized.length() <= prefixLength + suffixLength) {
      return DEFAULT_MASK;
    }
    return normalized.substring(0, prefixLength) + DEFAULT_MASK
        + normalized.substring(normalized.length() - suffixLength);
  }

  /**
   * 脱敏手机号。
   *
   * @param mobile 手机号
   * @return 脱敏后的手机号
   */
  public static String maskMobile(String mobile) {
    return maskMiddle(mobile, 3, 4);
  }

  /**
   * 脱敏邮箱地址。
   *
   * @param email 邮箱地址
   * @return 脱敏后的邮箱地址
   */
  public static String maskEmail(String email) {
    String normalized = TextHelper.trimToEmpty(email);
    int atIndex = normalized.indexOf('@');
    if (atIndex <= 0) {
      return maskMiddle(normalized, 1, 0);
    }
    return normalized.charAt(0) + DEFAULT_MASK + normalized.substring(atIndex);
  }

  /**
   * 脱敏密钥、Token、访问凭证等敏感字符串。
   *
   * @param secret 原始密钥
   * @return 脱敏后的密钥
   */
  public static String maskSecret(String secret) {
    return maskMiddle(secret, 3, 4);
  }
}
