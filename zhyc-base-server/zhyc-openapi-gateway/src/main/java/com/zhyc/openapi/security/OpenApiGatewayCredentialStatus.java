/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.util.Arrays;

/**
 * 开放 API 网关运行态凭证状态。
 *
 * <p>用于统一约束网关鉴权时可识别的 API Key 凭证状态，避免鉴权器和凭证对象各自维护魔法字符串。</p>
 */
public enum OpenApiGatewayCredentialStatus {

  /** 启用状态，允许 API Key 参与网关签名校验。 */
  ENABLED("enabled", "启用"),
  /** 禁用状态，网关应拒绝该 API Key 调用。 */
  DISABLED("disabled", "禁用"),
  /** 已过期状态，网关应拒绝该 API Key 调用。 */
  EXPIRED("expired", "已过期");

  /** 凭证状态编码，来自开放 API 管理库的 API Key 状态字段。 */
  private final String code;
  /** 凭证状态中文说明，用于诊断和运行态监控展示。 */
  private final String description;

  OpenApiGatewayCredentialStatus(String code, String description) {
    this.code = code;
    this.description = description;
  }

  /**
   * 返回凭证状态编码。
   *
   * @return 凭证状态编码
   */
  public String getCode() {
    return code;
  }

  /**
   * 返回凭证状态中文说明。
   *
   * @return 凭证状态中文说明
   */
  public String getDescription() {
    return description;
  }

  /**
   * 判断当前状态是否允许参与网关鉴权。
   *
   * @return 启用状态返回 {@code true}
   */
  public boolean isEnabled() {
    return this == ENABLED;
  }

  /**
   * 根据持久化编码解析网关运行态凭证状态。
   *
   * @param code 凭证状态编码
   * @return 匹配的网关运行态凭证状态
   */
  public static OpenApiGatewayCredentialStatus fromCode(String code) {
    return Arrays.stream(values())
        .filter(status -> status.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("API Key 运行态凭证状态只支持 enabled、disabled 或 expired"));
  }
}
