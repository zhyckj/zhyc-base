/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

/**
 * 低代码生成前校验项。
 *
 * <p>用于描述一个阻断错误或非阻断警告，前端可按编码定位字段、表模型或生成命令问题。</p>
 */
public class LowcodeGenerationValidationItem {

  /** 校验项编码；用于前端展示和后续国际化映射。 */
  private final String code;
  /** 校验项消息；说明具体失败原因或整改建议。 */
  private final String message;

  /**
   * 创建生成前校验项。
   *
   * @param code 校验项编码
   * @param message 校验项消息
   */
  public LowcodeGenerationValidationItem(String code, String message) {
    this.code = requireText(code, "校验项编码不能为空");
    this.message = requireText(message, "校验项消息不能为空");
  }

  /**
   * 返回校验项编码。
   *
   * @return 校验项编码
   */
  public String getCode() {
    return code;
  }

  /**
   * 返回校验项消息。
   *
   * @return 校验项消息
   */
  public String getMessage() {
    return message;
  }

  private static String requireText(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }
}
