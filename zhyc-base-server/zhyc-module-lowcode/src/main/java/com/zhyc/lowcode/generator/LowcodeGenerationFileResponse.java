/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

/**
 * 低代码生成文件明细响应对象。
 */
public class LowcodeGenerationFileResponse {

  /** 数据库主键。 */
  private final Long id;
  /** 生成记录主键。 */
  private final Long recordId;
  /** 模板编码。 */
  private final String templateCode;
  /** 生成文件路径。 */
  private final String filePath;
  /** 生成文件类型。 */
  private final String fileType;
  /** 覆盖模式。 */
  private final String overwriteMode;
  /** 文件内容哈希。 */
  private final String contentHash;

  private LowcodeGenerationFileResponse(LowcodeGenerationFile file) {
    this.id = file.getId();
    this.recordId = file.getRecordId();
    this.templateCode = file.getTemplateCode();
    this.filePath = file.getFilePath();
    this.fileType = file.getFileType();
    this.overwriteMode = file.getOverwriteMode().name();
    this.contentHash = file.getContentHash();
  }

  /**
   * 从生成文件明细转换为响应对象。
   *
   * @param file 生成文件明细
   * @return 响应对象
   */
  public static LowcodeGenerationFileResponse from(LowcodeGenerationFile file) {
    return new LowcodeGenerationFileResponse(file);
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
  public String getOverwriteMode() {
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
}
