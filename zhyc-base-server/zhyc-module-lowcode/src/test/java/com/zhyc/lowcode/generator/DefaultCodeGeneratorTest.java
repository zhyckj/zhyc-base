/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import com.zhyc.lowcode.db.LowcodeFieldType;
import com.zhyc.lowcode.metadata.domain.LowcodeColumnModel;
import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 默认代码生成器测试。
 */
class DefaultCodeGeneratorTest {

  /**
   * 验证生成器会按目标端选择模板、解析输出路径并渲染生成文件。
   */
  @Test
  void shouldGenerateFilesForTargetTemplates() {
    CodeTemplateRegistry registry = new CodeTemplateRegistry(List.of(new StubTemplateProvider()));
    CodeGenerator generator = new DefaultCodeGenerator(registry, new StubTemplateRenderer());
    LowcodeTableModel tableModel = purchaseOrderTable();
    CodeGenerationRequest request = new CodeGenerationRequest(
        GenerationTarget.ADMIN_FRONTEND, "purchase", "purchaseOrder", tableModel);

    List<GeneratedFile> files = generator.generate(request);

    assertEquals(2, files.size());
    assertEquals("src/views/purchase/purchaseOrder/list.vue", files.get(0).getPath());
    assertEquals("admin-vue-list::purchase::purchaseOrder::pur_order", files.get(0).getContent());
    assertEquals("src/views/purchase/purchaseOrder/form.vue", files.get(1).getPath());
    assertEquals("admin-vue-form::purchase::purchaseOrder::pur_order", files.get(1).getContent());
  }

  /**
   * 验证没有匹配模板时返回空清单，方便首期按目标端分批接入模板。
   */
  @Test
  void shouldReturnEmptyFilesWhenTargetHasNoTemplate() {
    CodeTemplateRegistry registry = new CodeTemplateRegistry(List.of(new StubTemplateProvider()));
    CodeGenerator generator = new DefaultCodeGenerator(registry, new StubTemplateRenderer());
    CodeGenerationRequest request = new CodeGenerationRequest(
        GenerationTarget.OPEN_API_PORTAL, "purchase", "purchaseOrder", purchaseOrderTable());

    assertTrue(generator.generate(request).isEmpty());
  }

  /**
   * 验证模板路径残留未知占位符时失败，避免生成到错误目录。
   */
  @Test
  void shouldRejectUnresolvedOutputPathPlaceholder() {
    CodeTemplateRegistry registry = new CodeTemplateRegistry(List.of(() -> List.of(
        new CodeTemplateDescriptor(
            "bad-path",
            GenerationTarget.ADMIN_FRONTEND,
            "错误路径模板",
            "src/views/{module}/{unknown}/index.vue"))));
    CodeGenerator generator = new DefaultCodeGenerator(registry, new StubTemplateRenderer());
    CodeGenerationRequest request = new CodeGenerationRequest(
        GenerationTarget.ADMIN_FRONTEND, "purchase", "purchaseOrder", purchaseOrderTable());

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> generator.generate(request));

    assertEquals("输出路径存在未解析占位符: src/views/purchase/{unknown}/index.vue", exception.getMessage());
  }

  private LowcodeTableModel purchaseOrderTable() {
    return new LowcodeTableModel(
        1L, "tenant_a", "purchase_order", "采购订单", "pur_order",
        List.of(
            LowcodeColumnModel.builder("id", "主键", LowcodeFieldType.LONG)
                .primaryKey(true)
                .required(true)
                .build(),
            LowcodeColumnModel.builder("order_no", "订单编号", LowcodeFieldType.STRING)
                .length(64)
                .required(true)
                .listVisible(true)
                .formVisible(true)
                .queryable(true)
                .build()));
  }

  private static class StubTemplateProvider implements CodeTemplateProvider {

    @Override
    public List<CodeTemplateDescriptor> listTemplates() {
      return List.of(
          new CodeTemplateDescriptor(
              "admin-vue-list",
              GenerationTarget.ADMIN_FRONTEND,
              "后台管理列表页",
              "src/views/{module}/{entity}/list.vue"),
          new CodeTemplateDescriptor(
              "admin-vue-form",
              GenerationTarget.ADMIN_FRONTEND,
              "后台管理表单页",
              "src/views/{module}/{entity}/form.vue"),
          new CodeTemplateDescriptor(
              "uniapp-list",
              GenerationTarget.UNIAPP,
              "UniApp 列表页",
              "src/pages/{module}/{entity}/list.vue"));
    }
  }

  private static class StubTemplateRenderer implements CodeTemplateRenderer {

    @Override
    public String render(CodeTemplateDescriptor template, CodeGenerationContext context) {
      return template.getCode()
          + "::" + context.getModuleName()
          + "::" + context.getEntityName()
          + "::" + context.getTableModel().getTableName();
    }
  }
}
