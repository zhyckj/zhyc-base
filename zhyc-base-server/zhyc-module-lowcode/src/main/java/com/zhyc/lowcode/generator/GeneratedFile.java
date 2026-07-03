/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 代码生成文件结果。
 */
public class GeneratedFile {

  /** 生成目标端。 */
  private final GenerationTarget target;
  /** 使用的模板编码。 */
  private final String templateCode;
  /** 相对输出路径。 */
  private final String path;
  /** 文件内容。 */
  private final String content;

  /**
   * 创建生成文件结果。
   *
   * @param target 生成目标端
   * @param templateCode 使用的模板编码
   * @param path 相对输出路径
   * @param content 文件内容
   */
  public GeneratedFile(GenerationTarget target, String templateCode, String path, String content) {
    if (target == null) {
      throw new IllegalArgumentException("生成目标不能为空");
    }
    this.target = target;
    this.templateCode = requireText(templateCode, "模板编码不能为空");
    this.path = requireRelativePath(path);
    this.content = content == null ? "" : content;
  }

  /**
   * 返回生成目标端。
   *
   * @return 生成目标端
   */
  public GenerationTarget getTarget() {
    return target;
  }

  /**
   * 返回使用的模板编码。
   *
   * @return 模板编码
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
   * 返回文件内容。
   *
   * @return 文件内容
   */
  public String getContent() {
    return content;
  }

  /**
   * 返回生成文件内容哈希。
   *
   * <p>用于生成预览、执行报告和覆盖风险对比，算法固定为 SHA-256。</p>
   *
   * @return 文件内容 SHA-256 哈希
   */
  public String getContentHash() {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(content.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("当前 JDK 不支持 SHA-256 哈希算法", ex);
    }
  }

  private static String requireText(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }

  private static String requireRelativePath(String value) {
    String path = requireText(value, "输出路径不能为空");
    if (path.startsWith("/") || path.startsWith("\\") || path.contains("..")) {
      throw new IllegalArgumentException("输出路径必须是安全的相对路径");
    }
    return path;
  }
}
