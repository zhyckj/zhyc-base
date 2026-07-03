/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 代码生成模板注册表测试。
 */
class CodeTemplateRegistryTest {

  /**
   * 验证注册表会按生成目标筛选模板。
   */
  @Test
  void filtersTemplatesByGenerationTarget() {
    CodeTemplateRegistry registry = new CodeTemplateRegistry(List.of(new StubTemplateProvider()));

    List<CodeTemplateDescriptor> templates = registry.findByTarget(GenerationTarget.UNIAPP);

    assertEquals(1, templates.size());
    assertEquals("uniapp-page-list", templates.get(0).getCode());
  }

  /**
   * 验证注册表可按目标和模板编码查找模板。
   */
  @Test
  void findsTemplateByTargetAndCode() {
    CodeTemplateRegistry registry = new CodeTemplateRegistry(List.of(new StubTemplateProvider()));

    assertTrue(registry.findOne(GenerationTarget.OPEN_API_PORTAL, "openapi-doc-page").isPresent());
  }

  /**
   * 验证不存在的模板编码返回空 Optional。
   */
  @Test
  void returnsEmptyWhenTemplateMissing() {
    CodeTemplateRegistry registry = new CodeTemplateRegistry(List.of(new StubTemplateProvider()));

    assertTrue(registry.findOne(GenerationTarget.ADMIN_FRONTEND, "missing").isEmpty());
  }

  private static class StubTemplateProvider implements CodeTemplateProvider {

    /**
     * 返回测试用模板清单。
     *
     * @return 测试用模板清单
     */
    @Override
    public List<CodeTemplateDescriptor> listTemplates() {
      return List.of(
          new CodeTemplateDescriptor(
              "admin-vue-form",
              GenerationTarget.ADMIN_FRONTEND,
              "后台管理表单页",
              "src/views/{module}/{entity}/form.vue"),
          new CodeTemplateDescriptor(
              "uniapp-page-list",
              GenerationTarget.UNIAPP,
              "UniApp 列表页",
              "src/pages/{module}/{entity}/list.vue"),
          new CodeTemplateDescriptor(
              "openapi-doc-page",
              GenerationTarget.OPEN_API_PORTAL,
              "开放 API 文档页",
              "src/pages/api/{entity}.md"));
    }
  }
}
