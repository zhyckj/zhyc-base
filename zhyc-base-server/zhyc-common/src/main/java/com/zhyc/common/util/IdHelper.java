/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.util;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * ID 生成帮助类。
 *
 * <p>统一生成 UUID 和基础随机编码，避免各模块分散使用不同格式的随机标识。</p>
 */
public final class IdHelper {

  /** Base62 字符表，避免生成 URL 或编码中不易处理的特殊字符。 */
  private static final char[] BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
  /** 安全随机数生成器。 */
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private IdHelper() {
  }

  /**
   * 生成标准 UUID。
   *
   * @return 带连字符的 UUID
   */
  public static String uuid() {
    return UUID.randomUUID().toString();
  }

  /**
   * 生成无连字符 UUID。
   *
   * @return 32 位无连字符 UUID
   */
  public static String uuidNoDash() {
    return uuid().replace("-", "");
  }

  /**
   * 生成指定长度的 Base62 随机串。
   *
   * @param length 随机串长度
   * @return Base62 随机串
   */
  public static String randomBase62(int length) {
    if (length <= 0) {
      throw new IllegalArgumentException("随机串长度必须大于 0");
    }
    StringBuilder builder = new StringBuilder(length);
    for (int index = 0; index < length; index++) {
      builder.append(BASE62[SECURE_RANDOM.nextInt(BASE62.length)]);
    }
    return builder.toString();
  }
}
