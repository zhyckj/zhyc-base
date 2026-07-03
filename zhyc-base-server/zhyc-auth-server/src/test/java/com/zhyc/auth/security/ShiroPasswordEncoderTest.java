/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Shiro 密码编码器适配测试。
 */
class ShiroPasswordEncoderTest {

  /**
   * 验证 Shiro 哈希可被 Spring Security 登录校验链路匹配。
   */
  @Test
  void shouldMatchShiroPasswordHash() {
    ShiroPasswordEncoder encoder = new ShiroPasswordEncoder();
    String encodedPassword = encoder.encode("new-admin-password");

    assertTrue(encoder.matches("new-admin-password", encodedPassword));
    assertFalse(encoder.matches("old-admin-password", encodedPassword));
  }
}
