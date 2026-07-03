/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开放 API 客户端 IP 白名单匹配器测试。
 */
class OpenApiClientIpMatcherTest {

  /**
   * 验证白名单项使用 JSON Unicode 转义时仍能按真实 IPv4 内容匹配。
   */
  @Test
  void shouldMatchIpv4WhenWhitelistUsesUnicodeEscapes() {
    OpenApiClientIpMatcher matcher = new OpenApiClientIpMatcher();

    boolean matched = matcher.matches("[\"\\u0031\\u0030.0.0.1\"]", "10.0.0.1");

    assertTrue(matched);
  }
}
