/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.monitor;

import java.time.LocalDateTime;

/**
 * 服务运行状态响应对象。
 */
public class RuntimeServiceStatus {

  /** 服务名称。 */
  private final String serviceName;
  /** 运行状态。 */
  private final String status;
  /** 服务版本。 */
  private final String version;
  /** 服务健康检测响应耗时，单位毫秒。 */
  private final long responseTimeMs;
  /** 最近心跳时间。 */
  private final LocalDateTime heartbeatAt;

  /**
   * 创建服务运行状态响应对象。
   *
   * @param serviceName 服务名称
   * @param status 运行状态
   * @param version 服务版本
   * @param responseTimeMs 服务健康检测响应耗时，单位毫秒
   * @param heartbeatAt 最近心跳时间
   */
  public RuntimeServiceStatus(String serviceName, String status, String version, long responseTimeMs,
                              LocalDateTime heartbeatAt) {
    this.serviceName = serviceName;
    this.status = status;
    this.version = version;
    this.responseTimeMs = responseTimeMs;
    this.heartbeatAt = heartbeatAt;
  }

  /**
   * 返回服务名称。
   *
   * @return 服务名称
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * 返回运行状态。
   *
   * @return 运行状态
   */
  public String getStatus() {
    return status;
  }

  /**
   * 返回服务版本。
   *
   * @return 服务版本
   */
  public String getVersion() {
    return version;
  }

  /**
   * 返回服务健康检测响应耗时。
   *
   * @return 服务健康检测响应耗时，单位毫秒
   */
  public long getResponseTimeMs() {
    return responseTimeMs;
  }

  /**
   * 返回最近心跳时间。
   *
   * @return 最近心跳时间
   */
  public LocalDateTime getHeartbeatAt() {
    return heartbeatAt;
  }
}
