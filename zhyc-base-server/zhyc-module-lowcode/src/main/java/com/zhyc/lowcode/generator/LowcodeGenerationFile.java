/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import java.util.Objects;

/**
 * 低代码生成文件明细。
 *
 * <p>记录一次代码生成中每个文件的路径、模板、覆盖模式和内容哈希，用于覆盖风险复核。</p>
 */
public class LowcodeGenerationFile {

  /** 数据库主键。 */
  private final Long id;
  /** 租户业务编码。 */
  private final String tenantId;
  /** 生成记录主键。 */
  private final Long recordId;
  /** 模板编码。 */
  private final String templateCode;
  /** 生成文件路径。 */
  private final String filePath;
  /** 生成文件类型。 */
  private final String fileType;
  /** 覆盖模式。 */
  private final GeneratedFileOverwriteStrategy overwriteMode;
  /** 文件内容哈希。 */
  private final String contentHash;

  /**
   * 创建低代码生成文件明细。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param recordId 生成记录主键
   * @param templateCode 模板编码
   * @param filePath 生成文件路径
   * @param fileType 生成文件类型
   * @param overwriteMode 覆盖模式
   * @param contentHash 文件内容哈希
   */
  public LowcodeGenerationFile(Long id, String tenantId, Long recordId, String templateCode, String filePath,
                               String fileType, GeneratedFileOverwriteStrategy overwriteMode, String contentHash) {
    this.id = id;
    this.tenantId = requireText(tenantId, "租户业务编码不能为空");
    this.recordId = Objects.requireNonNull(recordId, "生成记录主键不能为空");
    this.templateCode = requireText(templateCode, "模板编码不能为空");
    this.filePath = requireText(filePath, "生成文件路径不能为空");
    this.fileType = requireText(fileType, "生成文件类型不能为空");
    this.overwriteMode = Objects.requireNonNull(overwriteMode, "覆盖模式不能为空");
    this.contentHash = requireText(contentHash, "文件内容哈希不能为空");
  }

  /**
   * 返回数据库主键。
   *
   * @return 数据库主键
   */
  public Long getId() {
    return id;
  }

  /**
   * 返回租户业务编码。
   *
   * @return 租户业务编码
   */
  public String getTenantId() {
    return tenantId;
  }

  /**
   * 返回生成记录主键。
   *
   * @return 生成记录主键
   */
  public Long getRecordId() {
    return recordId;
  }

  /**
   * 返回模板编码。
   *
   * @return 模板编码
   */
  public String getTemplateCode() {
    return templateCode;
  }

  /**
   * 返回生成文件路径。
   *
   * @return 生成文件路径
   */
  public String getFilePath() {
    return filePath;
  }

  /**
   * 返回生成文件类型。
   *
   * @return 生成文件类型
   */
  public String getFileType() {
    return fileType;
  }

  /**
   * 返回覆盖模式。
   *
   * @return 覆盖模式
   */
  public GeneratedFileOverwriteStrategy getOverwriteMode() {
    return overwriteMode;
  }

  /**
   * 返回文件内容哈希。
   *
   * @return 文件内容哈希
   */
  public String getContentHash() {
    return contentHash;
  }

  private static String requireText(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }
}
