/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.util.Optional;

/**
 * 空开放 API 签名策略仓储。
 *
 * <p>用于保持旧构造器兼容；未接入数据库策略时，网关沿用默认 5 分钟防重放窗口。</p>
 */
public class EmptyOpenApiSignaturePolicyRepository implements OpenApiSignaturePolicyRepository {

  /**
   * 返回空签名策略，表示当前网关沿用默认防重放配置。
   *
   * @param tenantId 租户业务编码
   * @param appCode 开放 API 应用编码
   * @return 空签名策略
   */
  @Override
  public Optional<OpenApiSignaturePolicy> findEnabledPolicy(String tenantId, String appCode) {
    return Optional.empty();
  }
}
