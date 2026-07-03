/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

/**
 * 代码生成目标。
 */
public enum GenerationTarget {

  /** 后台管理后端代码。 */
  ADMIN_BACKEND("admin-backend"),
  /** 后台管理 Vue 前端代码。 */
  ADMIN_FRONTEND("admin-frontend"),
  /** UniApp 移动端代码。 */
  UNIAPP("uniapp"),
  /** 开放 API 与开发者门户代码。 */
  OPEN_API_PORTAL("open-api-portal"),
  /** Spring Cloud 微服务模块工程代码，首期生成可编译的轻量服务工程骨架。 */
  MICROSERVICE_MODULE("microservice-module");

  /** 对外接口和生成记录使用的稳定目标端编码。 */
  private final String code;

  /**
   * 创建代码生成目标。
   *
   * @param code 对外接口和生成记录使用的稳定目标端编码
   */
  GenerationTarget(String code) {
    this.code = code;
  }

  /**
   * 返回稳定目标端编码。
   *
   * @return 目标端编码
   */
  public String getCode() {
    return code;
  }

  /**
   * 根据稳定编码或既有 Java 枚举名解析生成目标。
   *
   * <p>首期保持对旧前端枚举名的兼容，同时允许新接口使用稳定编码，便于后续开放 API 和代码生成模板统一。</p>
   *
   * @param code 生成目标端编码
   * @return 代码生成目标
   */
  public static GenerationTarget fromCode(String code) {
    String normalizedCode = code == null ? "" : code.trim();
    for (GenerationTarget target : values()) {
      if (target.code.equalsIgnoreCase(normalizedCode) || target.name().equalsIgnoreCase(normalizedCode)) {
        return target;
      }
    }
    throw new IllegalArgumentException("不支持的低代码生成目标端编码: " + code);
  }
}
