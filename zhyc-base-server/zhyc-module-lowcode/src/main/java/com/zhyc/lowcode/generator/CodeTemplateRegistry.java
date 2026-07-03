/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 代码生成模板注册表。
 *
 * <p>注册表只负责模板元数据发现与筛选，不绑定具体模板引擎，便于后续接入
 * 后台管理端、UniApp、开放 API/开发者门户等多端模板。</p>
 */
public class CodeTemplateRegistry {

  /** 模板提供者列表。 */
  private final List<CodeTemplateProvider> providers;

  /**
   * 创建代码生成模板注册表。
   *
   * @param providers 模板提供者列表，传入 {@code null} 时按空列表处理
   */
  public CodeTemplateRegistry(List<CodeTemplateProvider> providers) {
    this.providers = providers == null ? List.of() : new ArrayList<>(providers);
  }

  /**
   * 按生成目标查找模板。
   *
   * @param target 生成目标
   * @return 匹配目标的模板清单
   */
  public List<CodeTemplateDescriptor> findByTarget(GenerationTarget target) {
    Objects.requireNonNull(target, "生成目标不能为空");
    return providers.stream()
        .flatMap(provider -> provider.listTemplates().stream())
        .filter(template -> target == template.getTarget())
        .toList();
  }

  /**
   * 按生成目标和模板编码查找单个模板。
   *
   * @param target 生成目标
   * @param code 模板唯一编码
   * @return 匹配模板，未找到时返回空 Optional
   */
  public Optional<CodeTemplateDescriptor> findOne(GenerationTarget target, String code) {
    Objects.requireNonNull(target, "生成目标不能为空");
    if (code == null || code.trim().isEmpty()) {
      throw new IllegalArgumentException("模板编码不能为空");
    }
    return findByTarget(target).stream()
        .filter(template -> template.getCode().equals(code.trim()))
        .findFirst();
  }
}
