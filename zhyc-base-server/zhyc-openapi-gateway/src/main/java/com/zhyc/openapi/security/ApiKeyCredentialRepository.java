/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.util.Optional;

/**
 * API Key 凭证仓储。
 */
public interface ApiKeyCredentialRepository {

  /**
   * 按访问密钥查询运行态凭证。
   *
   * @param accessKey API 访问密钥
   * @return 运行态凭证，不存在时为空
   */
  Optional<ApiKeyCredential> findByAccessKey(String accessKey);
}
