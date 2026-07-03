/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 开放 API 网关凭证状态测试。
 */
class OpenApiGatewayCredentialStatusTest {

  /**
   * 验证启用状态编码可以解析为网关运行态凭证状态。
   */
  @Test
  void shouldParseEnabledCredentialStatus() {
    OpenApiGatewayCredentialStatus status = OpenApiGatewayCredentialStatus.fromCode("enabled");

    assertEquals("enabled", status.getCode());
    assertTrue(status.isEnabled());
  }

  /**
   * 验证网关运行态凭证状态会拒绝不受支持的编码。
   */
  @Test
  void shouldRejectUnsupportedCredentialStatusCode() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> OpenApiGatewayCredentialStatus.fromCode("pending"));

    assertEquals("API Key 运行态凭证状态只支持 enabled、disabled 或 expired", exception.getMessage());
  }
}
