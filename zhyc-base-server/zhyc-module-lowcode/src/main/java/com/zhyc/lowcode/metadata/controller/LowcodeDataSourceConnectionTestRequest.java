/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.controller;

/**
 * 低代码数据源连接测试请求。
 */
public class LowcodeDataSourceConnectionTestRequest {

  /** 租户业务编码，用于限制只能测试当前租户的数据源。 */
  private String tenantId;
  /** 数据源编码，同一租户内唯一。 */
  private String code;

  /** @return 租户业务编码 */
  public String getTenantId() {
    return tenantId;
  }

  /** @param tenantId 租户业务编码 */
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  /** @return 数据源编码 */
  public String getCode() {
    return code;
  }

  /** @param code 数据源编码 */
  public void setCode(String code) {
    this.code = code;
  }
}
