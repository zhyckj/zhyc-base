/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhyc.lowcode.db.LowcodeFieldType;
import com.zhyc.lowcode.metadata.domain.LowcodeColumnModel;
import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * 生成前端产物静态校验测试。
 *
 * <p>该测试把后台前端和 uni-app 生成结果落盘到临时目录，校验页面、API、页面注册片段的基本结构，
 * 防止模板缺失关键端侧入口。</p>
 */
class GeneratedFrontendArtifactValidationTest {

  /** 生成文件临时输出根目录。 */
  @TempDir
  private Path outputRoot;

  /**
   * 验证后台前端生成产物具备基本 Vue 与 TypeScript 结构。
   *
   * @throws IOException 读取临时生成文件失败时抛出
   */
  @Test
  void shouldValidateGeneratedAdminFrontendArtifacts() throws IOException {
    List<GeneratedFile> files = generate(GenerationTarget.ADMIN_FRONTEND);
    new FileSystemGeneratedFileWriter(outputRoot).write(files, GeneratedFileOverwriteStrategy.OVERWRITE);

    String api = Files.readString(outputRoot.resolve("zhyc-base-vue/src/api/purchase/purchaseOrder.ts"));
    assertTrue(api.contains("export interface PurchaseOrderRecord"));
    assertTrue(api.contains("/** 采购订单记录主键，用于列表行键、详情查询和编辑定位。 */"));
    assertTrue(api.contains("/** 租户业务编码，用于端侧请求头和后端租户隔离复核。 */"));
    assertTrue(api.contains("/** 字段展示值，用于首期列表、表单和详情页展示。 */"));
    assertTrue(api.contains("/** 当前租户内未删除记录总数，用于列表统计展示。 */"));
    assertTrue(api.contains("/** 保存请求租户业务编码，用于后端复核租户隔离边界。 */"));
    assertTrue(api.contains("/** 表单字段值，字段名来自当前模型字段白名单。 */"));
    assertTrue(api.contains("export function listPurchaseOrder"));
    assertTrue(api.contains("export function getPurchaseOrder"));
    assertTrue(api.contains("export function savePurchaseOrder"));
    assertTrue(api.contains("export function updatePurchaseOrder"));
    assertTrue(api.contains("export function deletePurchaseOrder"));
    assertFalse(api.contains("{Entity}"));
    assertGeneratedContextSafe(api);

    Path listPage = outputRoot.resolve("zhyc-base-vue/src/views/purchase/purchaseOrder/index.vue");
    Path formPage = outputRoot.resolve("zhyc-base-vue/src/views/purchase/purchaseOrder/form.vue");
    Path detailPage = outputRoot.resolve("zhyc-base-vue/src/views/purchase/purchaseOrder/detail.vue");
    assertVueSfc(listPage);
    assertVueSfc(formPage);
    assertVueSfc(detailPage);
    assertAdminContextSafe(listPage);
    assertAdminContextSafe(formPage);
    assertAdminContextSafe(detailPage);

    String route = Files.readString(outputRoot.resolve(
        "zhyc-base-vue/src/router/routes/modules/purchase-purchaseOrder.ts"));
    assertTrue(route.contains("export const purchaseOrderRoute"));
    assertTrue(route.contains("export const purchaseOrderCreateRoute"));
    assertTrue(route.contains("export const purchaseOrderEditRoute"));
    assertTrue(route.contains("export const purchaseOrderDetailRoute"));
    assertTrue(route.contains("path: '/purchase/purchaseOrder/create'"));
    assertTrue(route.contains("path: '/purchase/purchaseOrder/:id/edit'"));
    assertTrue(route.contains("path: '/purchase/purchaseOrder/:id'"));
    assertTrue(route.contains("@/views/purchase/purchaseOrder/form.vue"));
    assertTrue(route.contains("@/views/purchase/purchaseOrder/detail.vue"));
    assertGeneratedContextSafe(route);
  }

  /**
   * 验证 uni-app 生成产物具备基本页面、API 和 pages.json 结构。
   *
   * @throws IOException 读取临时生成文件失败时抛出
   */
  @Test
  void shouldValidateGeneratedUniappArtifacts() throws IOException {
    List<GeneratedFile> files = generate(GenerationTarget.UNIAPP);
    new FileSystemGeneratedFileWriter(outputRoot).write(files, GeneratedFileOverwriteStrategy.OVERWRITE);

    String api = Files.readString(outputRoot.resolve("zhyc-base-uniapp/src/api/purchase-purchaseOrder.ts"));
    assertTrue(api.contains("export interface PurchaseOrderRecord"));
    assertTrue(api.contains("/** 采购订单移动端记录主键，用于列表行键、详情查询和编辑定位。 */"));
    assertTrue(api.contains("/** 租户业务编码，用于移动端请求头和后端租户隔离复核。 */"));
    assertTrue(api.contains("/** 字段展示值，用于移动端列表、表单和详情页展示。 */"));
    assertTrue(api.contains("/** 当前租户内未删除记录总数，用于移动端列表统计展示。 */"));
    assertTrue(api.contains("/** 移动端保存请求租户业务编码，用于后端复核租户隔离边界。 */"));
    assertTrue(api.contains("/** 移动端表单字段值，字段名来自当前模型字段白名单。 */"));
    assertTrue(api.contains("export function listMobilePurchaseOrder"));
    assertTrue(api.contains("export function getMobilePurchaseOrder"));
    assertTrue(api.contains("export function saveMobilePurchaseOrder"));
    assertTrue(api.contains("export function updateMobilePurchaseOrder"));
    assertFalse(api.contains("{Entity}"));
    assertGeneratedContextSafe(api);

    Path listPage = outputRoot.resolve("zhyc-base-uniapp/src/pages/purchase/purchaseOrder/list.vue");
    Path formPage = outputRoot.resolve("zhyc-base-uniapp/src/pages/purchase/purchaseOrder/form.vue");
    Path detailPage = outputRoot.resolve("zhyc-base-uniapp/src/pages/purchase/purchaseOrder/detail.vue");
    assertVueSfc(listPage);
    assertVueSfc(formPage);
    assertVueSfc(detailPage);
    assertMobileContextSafe(listPage);
    assertMobileContextSafe(formPage);
    assertMobileContextSafe(detailPage);
    assertGeneratedUniappPageUsesMobileShell(listPage, "采购订单");
    assertGeneratedUniappPageUsesMobileShell(formPage, "采购订单表单");
    assertGeneratedUniappPageUsesMobileShell(detailPage, "采购订单详情");

    String pagesJson = Files.readString(outputRoot.resolve("zhyc-base-uniapp/src/generated-pages/purchase-purchaseOrder.pages.json"));
    assertTrue(pagesJson.contains("\"pages\""));
    assertTrue(pagesJson.contains("\"path\""));
    assertEquals(3, countOccurrences(pagesJson, "\"path\""));
    assertTrue(pagesJson.contains("pages/purchase/purchaseOrder/list"));
    assertTrue(pagesJson.contains("pages/purchase/purchaseOrder/form"));
    assertTrue(pagesJson.contains("pages/purchase/purchaseOrder/detail"));
    assertEquals(3, countOccurrences(pagesJson, "\"navigationStyle\": \"custom\""));
    assertGeneratedContextSafe(pagesJson);
  }

  private void assertGeneratedUniappPageUsesMobileShell(Path file, String title) throws IOException {
    String content = Files.readString(file);
    assertTrue(content.contains("import MobilePageTopBar from '@/components/MobilePageTopBar.vue';"),
        file + " 必须使用移动端自定义头部");
    assertTrue(content.contains("MobilePageTopBar"), file + " 必须渲染移动端自定义头部");
    assertTrue(content.contains("title=\"" + title + "\""), file + " 顶部标题必须与生成页面语义一致");
    assertTrue(content.contains("mobile-hero compact-hero"), file + " 必须使用紧凑移动端首屏卡片");
  }

  private void assertVueSfc(Path file) throws IOException {
    String content = Files.readString(file);
    assertTrue(content.contains("<template>"), file + " 必须包含 template");
    assertTrue(content.contains("<script setup lang=\"ts\">"), file + " 必须包含 TypeScript setup 脚本");
    assertTrue(content.contains("</script>"), file + " 必须关闭 script");
    assertFalse(content.contains("{module}"), file + " 不允许残留模块占位符");
    assertFalse(content.contains("{entity}"), file + " 不允许残留实体占位符");
    assertFalse(content.contains("{Entity}"), file + " 不允许残留实体类占位符");
  }

  private void assertAdminContextSafe(Path file) throws IOException {
    String content = Files.readString(file);
    assertTrue(content.contains("requireAdminTenantId"), file + " 后台生成页必须通过统一上下文读取租户");
    assertGeneratedContextSafe(content);
  }

  private void assertMobileContextSafe(Path file) throws IOException {
    String content = Files.readString(file);
    assertTrue(content.contains("getMobileUserContext"), file + " 移动端生成页必须通过统一工具读取用户上下文");
    assertTrue(content.contains("requireMobileUserId"), file + " 移动端生成页必须校验当前登录用户");
    assertTrue(content.contains("requireMobileTenantId"), file + " 移动端生成页必须通过统一上下文读取租户");
    assertGeneratedContextSafe(content);
  }

  private void assertGeneratedContextSafe(String content) {
    assertFalse(content.contains("tenant_a"), "生成产物不允许固化建模租户");
    assertFalse(content.contains("orgId: 1"), "生成产物不允许固化组织上下文");
    assertFalse(content.contains("userId: 1"), "生成产物不允许固化用户上下文");
  }

  private int countOccurrences(String content, String text) {
    int count = 0;
    int index = 0;
    while ((index = content.indexOf(text, index)) >= 0) {
      count++;
      index += text.length();
    }
    return count;
  }

  private List<GeneratedFile> generate(GenerationTarget target) {
    CodeTemplateRegistry registry = new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider()));
    CodeGenerator generator = new DefaultCodeGenerator(registry, new SimpleStringTemplateRenderer());
    return generator.generate(new CodeGenerationRequest(target, "purchase", "purchaseOrder", purchaseOrderTable()));
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
