/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

/**
 * API Secret 解析器。
 */
@FunctionalInterface
public interface ApiSecretResolver {

  /**
   * 将持久化密文解析为运行态 API Secret。
   *
   * @param secretCipher API Secret 密文
   * @return 运行态 API Secret
   */
  String resolve(String secretCipher);
}
