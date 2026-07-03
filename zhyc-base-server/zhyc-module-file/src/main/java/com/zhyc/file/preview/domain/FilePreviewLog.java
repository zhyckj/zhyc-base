/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.preview.domain;

import java.time.LocalDateTime;

/**
 * 文件预览日志。
 *
 * <p>记录租户下文件预览请求的类型、结果和耗时，用于审计、问题定位和后续在线预览服务接入。</p>
 */
public class FilePreviewLog {

  /** 预览日志主键。 */
  private final Long id;
  /** 租户业务编码，用于 SaaS 数据隔离。 */
  private final String tenantId;
  /** 文件业务编码。 */
  private final String fileCode;
  /** 预览类型，例如 pdf、image、text。 */
  private final String previewType;
  /** 预览访问地址，首期为平台内部地址。 */
  private final String previewUrl;
  /** 预览结果，success 表示成功生成。 */
  private final String result;
  /** 预览耗时毫秒。 */
  private final Long costMs;
  /** 创建时间。 */
  private final LocalDateTime createdAt;

  /**
   * 创建文件预览日志。
   *
   * @param id 预览日志主键
   * @param tenantId 租户业务编码
   * @param fileCode 文件业务编码
   * @param previewType 预览类型
   * @param previewUrl 预览访问地址
   * @param result 预览结果
   * @param costMs 预览耗时毫秒
   * @param createdAt 创建时间
   */
  public FilePreviewLog(Long id, String tenantId, String fileCode, String previewType, String previewUrl,
      String result, Long costMs, LocalDateTime createdAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.fileCode = fileCode;
    this.previewType = previewType;
    this.previewUrl = previewUrl;
    this.result = result;
    this.costMs = costMs;
    this.createdAt = createdAt;
  }

  /** @return 预览日志主键 */
  public Long getId() { return id; }

  /** @return 租户业务编码 */
  public String getTenantId() { return tenantId; }

  /** @return 文件业务编码 */
  public String getFileCode() { return fileCode; }

  /** @return 预览类型 */
  public String getPreviewType() { return previewType; }

  /** @return 预览访问地址 */
  public String getPreviewUrl() { return previewUrl; }

  /** @return 预览结果 */
  public String getResult() { return result; }

  /** @return 预览耗时毫秒 */
  public Long getCostMs() { return costMs; }

  /** @return 创建时间 */
  public LocalDateTime getCreatedAt() { return createdAt; }
}
