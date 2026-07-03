/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.service;

/**
 * 低代码数据源连接测试结果。
 *
 * <p>只返回可展示的测试状态和说明，不包含数据库口令、密钥内容或其他敏感连接信息。</p>
 */
public class LowcodeDataSourceConnectionTestResult {

  /** 数据源编码，用于前端定位本次测试对象。 */
  private final String code;
  /** 连接测试是否成功。 */
  private final boolean success;
  /** 连接测试结果说明。 */
  private final String message;

  /**
   * 创建低代码数据源连接测试结果。
   *
   * @param code 数据源编码
   * @param success 连接测试是否成功
   * @param message 连接测试结果说明
   */
  public LowcodeDataSourceConnectionTestResult(String code, boolean success, String message) {
    this.code = code;
    this.success = success;
    this.message = message;
  }

  /** @return 数据源编码 */
  public String getCode() {
    return code;
  }

  /** @return 连接测试是否成功 */
  public boolean isSuccess() {
    return success;
  }

  /** @return 连接测试结果说明 */
  public String getMessage() {
    return message;
  }
}
