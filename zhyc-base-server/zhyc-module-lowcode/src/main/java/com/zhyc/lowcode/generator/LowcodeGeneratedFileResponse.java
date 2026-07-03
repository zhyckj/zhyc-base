/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

/**
 * 低代码生成文件预览响应对象。
 */
public class LowcodeGeneratedFileResponse {

  /** 生成目标端编码。 */
  private final String target;
  /** 使用的模板编码。 */
  private final String templateCode;
  /** 相对输出路径。 */
  private final String path;
  /** 文件内容哈希。 */
  private final String contentHash;
  /** 文件内容。 */
  private final String content;

  private LowcodeGeneratedFileResponse(GeneratedFile file) {
    this.target = file.getTarget().getCode();
    this.templateCode = file.getTemplateCode();
    this.path = file.getPath();
    this.contentHash = file.getContentHash();
    this.content = file.getContent();
  }

  /**
   * 从生成文件转换为响应对象。
   *
   * @param file 生成文件
   * @return 生成文件响应对象
   */
  public static LowcodeGeneratedFileResponse from(GeneratedFile file) {
    return new LowcodeGeneratedFileResponse(file);
  }

  /**
   * 返回生成目标端编码。
   *
   * @return 生成目标端编码
   */
  public String getTarget() {
    return target;
  }

  /**
   * 返回使用的模板编码。
   *
   * @return 使用的模板编码
   */
  public String getTemplateCode() {
    return templateCode;
  }

  /**
   * 返回相对输出路径。
   *
   * @return 相对输出路径
   */
  public String getPath() {
    return path;
  }

  /**
   * 返回文件内容哈希。
   *
   * @return 文件内容 SHA-256 哈希
   */
  public String getContentHash() {
    return contentHash;
  }

  /**
   * 返回文件内容。
   *
   * @return 文件内容
   */
  public String getContent() {
    return content;
  }
}
