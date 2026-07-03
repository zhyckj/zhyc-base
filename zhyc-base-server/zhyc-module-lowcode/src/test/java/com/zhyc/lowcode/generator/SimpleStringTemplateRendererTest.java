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

/**
 * 简单字符串模板渲染器测试。
 */
class SimpleStringTemplateRendererTest {

  /**
   * 验证字符串模板渲染器可替换常用建模占位符。
   */
  @Test
  void shouldRenderCommonLowcodePlaceholders() {
    CodeTemplateDescriptor template = new CodeTemplateDescriptor(
        "admin-service",
        GenerationTarget.ADMIN_BACKEND,
        "后台服务模板",
        "src/main/java/{module}/{entity}Service.java",
        "module={module};entity={entity};target={target};table={table};fields={fields};tenant={tenant}");
    CodeGenerationContext context = new CodeGenerationContext(new CodeGenerationRequest(
        GenerationTarget.ADMIN_BACKEND, "purchase", "purchaseOrder", purchaseOrderTable()));

    String content = new SimpleStringTemplateRenderer().render(template, context);

    assertEquals(
        "module=purchase;entity=purchaseOrder;target=admin-backend;table=pur_order;fields=id,order_no;tenant=tenant_a",
        content);
  }

  /**
   * 验证模板内容残留未知占位符时失败，避免生成半成品代码。
   */
  @Test
  void shouldRejectUnresolvedTemplatePlaceholder() {
    CodeTemplateDescriptor template = new CodeTemplateDescriptor(
        "bad-template",
        GenerationTarget.ADMIN_BACKEND,
        "错误模板",
        "src/main/java/{entity}.java",
        "class {missing} {}");
    CodeGenerationContext context = new CodeGenerationContext(new CodeGenerationRequest(
        GenerationTarget.ADMIN_BACKEND, "purchase", "purchaseOrder", purchaseOrderTable()));

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> new SimpleStringTemplateRenderer().render(template, context));

    assertEquals("模板内容存在未解析占位符: class {missing} {}", exception.getMessage());
  }

  /**
   * 验证前端运行时表达式不会被误判为未解析模板占位符。
   */
  @Test
  void shouldKeepFrontendRuntimeExpressions() {
    CodeTemplateDescriptor template = new CodeTemplateDescriptor(
        "vue-template",
        GenerationTarget.ADMIN_FRONTEND,
        "前端模板",
        "src/views/{entity}.vue",
        "<a-alert :message=\"`加载失败：${errorMessage}`\" />"
            + "<button>{{ submitting ? '提交中' : '提交' }}</button>"
            + "<span>{table}</span>");
    CodeGenerationContext context = new CodeGenerationContext(new CodeGenerationRequest(
        GenerationTarget.ADMIN_FRONTEND, "purchase", "purchaseOrder", purchaseOrderTable()));

    String content = new SimpleStringTemplateRenderer().render(template, context);

    assertEquals("<a-alert :message=\"`加载失败：${errorMessage}`\" />"
        + "<button>{{ submitting ? '提交中' : '提交' }}</button>"
        + "<span>pur_order</span>", content);
  }

  /**
   * 验证 Spring 路径变量不会被误判为低代码占位符。
   */
  @Test
  void shouldKeepSpringPathVariables() {
    CodeTemplateDescriptor template = new CodeTemplateDescriptor(
        "controller-template",
        GenerationTarget.ADMIN_BACKEND,
        "后台控制器模板",
        "src/main/java/{entity}Controller.java",
        "@DeleteMapping(\"/{id}\") class {Entity}Controller {}");
    CodeGenerationContext context = new CodeGenerationContext(new CodeGenerationRequest(
        GenerationTarget.ADMIN_BACKEND, "purchase", "purchaseOrder", purchaseOrderTable()));

    String content = new SimpleStringTemplateRenderer().render(template, context);

    assertEquals("@DeleteMapping(\"/{id}\") class PurchaseOrderController {}", content);
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
                .build()));
  }
}
