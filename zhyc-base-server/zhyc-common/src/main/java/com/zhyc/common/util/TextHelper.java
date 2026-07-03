/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.util;

/**
 * 文本处理帮助类。
 *
 * <p>统一承载基础框架中常见的空白判断、裁剪归一化和必填文本校验，避免各模块重复实现。</p>
 */
public final class TextHelper {

  private TextHelper() {
  }

  /**
   * 判断文本是否为空白。
   *
   * @param value 待判断文本
   * @return null、空串或全空白返回 {@code true}
   */
  public static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  /**
   * 判断文本是否包含有效字符。
   *
   * @param value 待判断文本
   * @return 包含非空白字符返回 {@code true}
   */
  public static boolean hasText(String value) {
    return !isBlank(value);
  }

  /**
   * 校验必填文本并裁剪首尾空白。
   *
   * @param value 待校验文本
   * @param message 校验失败提示
   * @return 裁剪后的文本
   */
  public static String requireText(String value, String message) {
    String normalized = trimToNull(value);
    if (normalized == null) {
      throw new IllegalArgumentException(message);
    }
    return normalized;
  }

  /**
   * 校验必填文本且不允许出现任何空白字符。
   *
   * <p>该方法不会裁剪入参，适合密钥引用、编码、Token 片段等格式必须完全稳定的场景。</p>
   *
   * @param value 待校验文本
   * @param blankMessage 空白失败提示
   * @param whitespaceMessage 包含空白字符失败提示
   * @return 原始文本
   */
  public static String requireNoWhitespaceText(String value, String blankMessage, String whitespaceMessage) {
    if (isBlank(value)) {
      throw new IllegalArgumentException(blankMessage);
    }
    if (containsWhitespace(value)) {
      throw new IllegalArgumentException(whitespaceMessage);
    }
    return value;
  }

  /**
   * 裁剪文本并把空白文本转为空值。
   *
   * @param value 原始文本
   * @return 裁剪后的文本；空白或 null 返回 null
   */
  public static String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  /**
   * 裁剪文本并把空值转为空串。
   *
   * @param value 原始文本
   * @return 裁剪后的文本；null 返回空串
   */
  public static String trimToEmpty(String value) {
    return value == null ? "" : value.trim();
  }

  /**
   * 空白文本返回默认值，非空白文本返回裁剪后的原值。
   *
   * @param value 原始文本
   * @param fallback 默认值
   * @return 归一化后的文本
   */
  public static String defaultIfBlank(String value, String fallback) {
    String normalized = trimToNull(value);
    return normalized == null ? fallback : normalized;
  }

  /**
   * 判断文本中是否包含任意空白字符。
   *
   * @param value 待判断文本
   * @return 包含空白字符返回 {@code true}
   */
  public static boolean containsWhitespace(String value) {
    if (value == null || value.isEmpty()) {
      return false;
    }
    for (int index = 0; index < value.length(); index++) {
      if (Character.isWhitespace(value.charAt(index))) {
        return true;
      }
    }
    return false;
  }

  /**
   * 连续移除指定尾缀。
   *
   * @param value 原始文本
   * @param suffix 需要移除的尾缀
   * @return 移除尾缀后的文本
   */
  public static String removeTrailingRepeated(String value, String suffix) {
    if (value == null || isBlank(suffix)) {
      return value;
    }
    String normalized = value;
    while (normalized.endsWith(suffix)) {
      normalized = normalized.substring(0, normalized.length() - suffix.length());
    }
    return normalized;
  }
}
