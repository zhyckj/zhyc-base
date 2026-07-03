/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.monitor;

import java.time.LocalDateTime;

/**
 * 数据源运行状态响应对象。
 */
public class RuntimeDataSourceStatus {

  /** 数据源编码。 */
  private final String sourceCode;
  /** 数据源名称。 */
  private final String sourceName;
  /** 连接状态。 */
  private final String status;
  /** 检测耗时，单位毫秒。 */
  private final long costMs;
  /** 检测时间。 */
  private final LocalDateTime checkedAt;

  /**
   * 创建数据源运行状态响应对象。
   *
   * @param sourceCode 数据源编码
   * @param sourceName 数据源名称
   * @param status 连接状态
   * @param costMs 检测耗时，单位毫秒
   * @param checkedAt 检测时间
   */
  public RuntimeDataSourceStatus(String sourceCode, String sourceName, String status, long costMs,
                                 LocalDateTime checkedAt) {
    this.sourceCode = sourceCode;
    this.sourceName = sourceName;
    this.status = status;
    this.costMs = costMs;
    this.checkedAt = checkedAt;
  }

  /**
   * 返回数据源编码。
   *
   * @return 数据源编码
   */
  public String getSourceCode() {
    return sourceCode;
  }

  /**
   * 返回数据源名称。
   *
   * @return 数据源名称
   */
  public String getSourceName() {
    return sourceName;
  }

  /**
   * 返回连接状态。
   *
   * @return 连接状态
   */
  public String getStatus() {
    return status;
  }

  /**
   * 返回检测耗时。
   *
   * @return 检测耗时，单位毫秒
   */
  public long getCostMs() {
    return costMs;
  }

  /**
   * 返回检测时间。
   *
   * @return 检测时间
   */
  public LocalDateTime getCheckedAt() {
    return checkedAt;
  }
}
