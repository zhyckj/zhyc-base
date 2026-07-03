/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.lowcode.db.LowcodeFieldType;
import com.zhyc.lowcode.db.LowcodeColumn;
import com.zhyc.lowcode.db.LowcodeDbDialectService;
import com.zhyc.lowcode.db.LowcodeTable;
import com.zhyc.lowcode.metadata.domain.LowcodeDatabaseDialect;
import com.zhyc.lowcode.metadata.domain.LowcodeDataSource;
import com.zhyc.lowcode.metadata.domain.LowcodeColumnModel;
import com.zhyc.lowcode.metadata.domain.LowcodeModelStatus;
import com.zhyc.lowcode.metadata.domain.LowcodePageModel;
import com.zhyc.lowcode.metadata.domain.LowcodePhysicalTable;
import com.zhyc.lowcode.metadata.domain.LowcodeTableRelation;
import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;
import com.zhyc.lowcode.metadata.service.LowcodeMetadataService;
import com.zhyc.lowcode.generator.repository.LowcodeGenerationFileRepository;
import com.zhyc.lowcode.generator.repository.LowcodeGenerationRecordRepository;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 低代码生成应用服务测试。
 */
class LowcodeGeneratorServiceTest {

  /**
   * 验证生成服务可以列出指定目标端的模板，供后台页面构建模板选择器。
   */
  @Test
  void shouldListTemplatesByTarget() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    List<CodeTemplateDescriptor> templates = service.listTemplates(GenerationTarget.ADMIN_FRONTEND);

    assertEquals(5, templates.size());
    assertTrue(templates.stream().allMatch(template -> template.getTarget() == GenerationTarget.ADMIN_FRONTEND));
  }

  /**
   * 验证模板查询入口拒绝空生成目标，并返回稳定业务错误码。
   */
  @Test
  void shouldRejectNullTemplateTargetWithBusinessCode() {
    LowcodeGeneratorService service = generatorService();

    BusinessException exception = assertThrows(BusinessException.class, () -> service.listTemplates(null));

    assertEquals("生成目标不能为空", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_GENERATION_TEMPLATE_TARGET_REQUIRED", exception.getCode());
  }

  /**
   * 验证生成校验入口拒绝空命令，并返回稳定业务错误码。
   */
  @Test
  void shouldRejectNullValidationCommandWithBusinessCode() {
    LowcodeGeneratorService service = generatorService();

    BusinessException exception = assertThrows(BusinessException.class, () -> service.validate(null));

    assertEquals("生成预览命令不能为空", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_GENERATION_PREVIEW_COMMAND_REQUIRED", exception.getCode());
  }

  /**
   * 验证生成预览入口拒绝空命令，并返回稳定业务错误码。
   */
  @Test
  void shouldRejectNullPreviewCommandWithBusinessCode() {
    LowcodeGeneratorService service = generatorService();

    BusinessException exception = assertThrows(BusinessException.class, () -> service.preview(null));

    assertEquals("生成预览命令不能为空", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_GENERATION_PREVIEW_COMMAND_REQUIRED", exception.getCode());
  }

  /**
   * 验证生成执行入口拒绝空命令，并返回稳定业务错误码。
   */
  @Test
  void shouldRejectNullExecuteCommandWithBusinessCode() {
    LowcodeGeneratorService service = generatorService();

    BusinessException exception = assertThrows(BusinessException.class, () -> service.execute(null));

    assertEquals("生成执行命令不能为空", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_GENERATION_EXECUTE_COMMAND_REQUIRED", exception.getCode());
  }

  /**
   * 验证生成执行依赖未配置时返回稳定业务错误码。
   */
  @Test
  void shouldRejectExecuteWhenDependencyMissingWithBusinessCode() {
    LowcodeGeneratorService service = generatorService();

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.execute(new LowcodeGenerationExecuteCommand(
            "tenant_a", "purchase_order", GenerationTarget.UNIAPP, "purchase", "purchaseOrder",
            GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS)));

    assertEquals("ZHYC_LOWCODE_GENERATION_EXECUTION_DEPENDENCY_MISSING", exception.getCode());
    assertEquals("生成执行依赖未配置", exception.getMessage());
  }

  /**
   * 验证首期内置模板覆盖后端、后台前端、uni-app、开放 API/开发者门户和微服务模块目标端。
   */
  @Test
  void shouldProvideFirstReleaseTemplatesForAllTargets() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    assertTemplateCodes(service, GenerationTarget.ADMIN_BACKEND,
        "admin-backend-controller", "admin-backend-service", "admin-backend-service-impl", "admin-backend-dto",
        "admin-backend-save-request", "admin-backend-response",
        "admin-backend-repository", "admin-backend-mybatis-repository",
        "admin-backend-mapper", "admin-backend-sql-provider",
        "admin-backend-ddl", "admin-backend-test");
    assertTemplateCodes(service, GenerationTarget.ADMIN_FRONTEND,
        "admin-frontend-list", "admin-frontend-form", "admin-frontend-detail",
        "admin-frontend-api", "admin-frontend-route");
    assertTemplateCodes(service, GenerationTarget.UNIAPP,
        "uniapp-list", "uniapp-form", "uniapp-detail", "uniapp-api", "uniapp-pages-json");
    assertTemplateCodes(service, GenerationTarget.OPEN_API_PORTAL,
        "openapi-controller", "openapi-dto", "openapi-signature-config",
        "openapi-registration-sql", "openapi-doc", "openapi-portal-debug-api", "openapi-portal-page");
    assertTemplateCodes(service, GenerationTarget.MICROSERVICE_MODULE,
        "microservice-pom", "microservice-application", "microservice-module-descriptor",
        "microservice-application-yml", "microservice-readme");
  }

  /**
   * 验证生成入口拒绝包含空白字符的模块名和实体名，避免生成不可编译路径或类名。
   */
  @Test
  void shouldRejectGenerationNamesContainingWhitespace() {
    IllegalArgumentException previewModuleException = assertThrows(IllegalArgumentException.class,
        () -> new LowcodeGenerationPreviewCommand("tenant_a", "purchase_order",
            GenerationTarget.ADMIN_BACKEND, "purchase order", "purchaseOrder"));
    IllegalArgumentException previewEntityException = assertThrows(IllegalArgumentException.class,
        () -> new LowcodeGenerationPreviewCommand("tenant_a", "purchase_order",
            GenerationTarget.ADMIN_BACKEND, "purchase", "purchase Order"));
    IllegalArgumentException requestModuleException = assertThrows(IllegalArgumentException.class,
        () -> new CodeGenerationRequest(GenerationTarget.ADMIN_BACKEND,
            "purchase order", "purchaseOrder", publishedTable()));
    IllegalArgumentException requestEntityException = assertThrows(IllegalArgumentException.class,
        () -> new CodeGenerationRequest(GenerationTarget.ADMIN_BACKEND,
            "purchase", "purchase Order", publishedTable()));

    assertEquals("业务模块名称不能包含空白字符", previewModuleException.getMessage());
    assertEquals("业务实体名称不能包含空白字符", previewEntityException.getMessage());
    assertEquals("业务模块名称不能包含空白字符", requestModuleException.getMessage());
    assertEquals("业务实体名称不能包含空白字符", requestEntityException.getMessage());
  }

  /**
   * 验证生成预览会读取租户内已发布表模型，并返回对应目标端文件清单。
   */
  @Test
  void shouldPreviewGeneratedFilesForPublishedTableModel() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    List<GeneratedFile> files = service.preview(new LowcodeGenerationPreviewCommand(
        "tenant_a", "purchase_order", GenerationTarget.UNIAPP, "purchase", "purchaseOrder"));

    assertEquals(5, files.size());
    assertEquals("zhyc-base-uniapp/src/pages/purchase/purchaseOrder/list.vue", files.get(0).getPath());
    assertTrue(files.get(0).getContent().contains("pur_order"));
    assertTrue(files.get(0).getContent().contains("getMobileUserContext"));
    assertTrue(files.get(0).getContent().contains("requireMobileUserId(userContext)"));
    assertTrue(files.get(0).getContent().contains("requireMobileTenantId"));
    assertTrue(files.get(0).getContent().contains("requireMobileTenantId(userContext)"));
    assertTrue(files.get(0).getContent().contains("listMobilePurchaseOrder(pageNo.value, pageSize.value)"));
    assertTrue(files.get(0).getContent().contains("showMobileToast"));
    assertTrue(files.get(0).getContent().contains("加载失败"));
    assertTrue(files.stream().anyMatch(file -> file.getPath().endsWith("/form.vue")));
    assertTrue(files.stream().anyMatch(file -> file.getPath().endsWith("/detail.vue")));
    assertTrue(files.stream().anyMatch(file -> file.getPath().endsWith("purchase-purchaseOrder.ts")));
    assertTrue(files.stream().anyMatch(file -> file.getTemplateCode().equals("uniapp-pages-json")));
  }

  /**
   * 验证微服务模块工程预览会读取已发布表模型并生成独立服务骨架文件。
   */
  @Test
  void shouldPreviewMicroserviceModuleSkeletonForPublishedTableModel() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    List<GeneratedFile> files = service.preview(new LowcodeGenerationPreviewCommand(
        "tenant_a", "purchase_order", GenerationTarget.MICROSERVICE_MODULE, "purchase", "purchaseOrder"));

    assertEquals(6, files.size());
    assertTrue(files.stream().anyMatch(file -> file.getPath().equals("zhyc-service-purchase-purchaseOrder/pom.xml")));
    assertTrue(files.stream().anyMatch(file -> file.getPath().endsWith("PurchaseOrderServiceApplication.java")));
    assertTrue(files.stream().anyMatch(file -> file.getPath().endsWith("PurchaseOrderServiceInfoController.java")));
    assertTrue(files.stream().anyMatch(file -> file.getContent().contains("X-ZHYC-Tenant-Id")));
    assertTrue(files.stream().anyMatch(file -> file.getContent().contains("moduleType: MICROSERVICE")));
    assertTrue(files.stream().anyMatch(file -> file.getContent().contains("openApiGatewayRequired: true")));
    assertTrue(files.stream().anyMatch(file -> file.getContent().contains("必须通过 `tenant_id` 隔离")));
  }

  /**
   * 验证后台前端生成预览会按已保存页面模型筛选页面文件，并保留 API 和路由支撑文件。
   */
  @Test
  void shouldFilterAdminFrontendPageFilesBySavedPageModels() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable(), List.of(new LowcodePageModel(
            10L, "tenant_a", 1L, "LIST", "/purchase/purchaseOrder",
            "zhyc-base-vue/src/views/purchase/purchaseOrder/index.vue", "TABLE"))),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    List<GeneratedFile> files = service.preview(new LowcodeGenerationPreviewCommand(
        "tenant_a", "purchase_order", GenerationTarget.ADMIN_FRONTEND, "purchase", "purchaseOrder"));

    assertEquals(3, files.size());
    assertTrue(files.stream().anyMatch(file -> file.getTemplateCode().equals("admin-frontend-list")));
    assertTrue(files.stream().anyMatch(file -> file.getTemplateCode().equals("admin-frontend-api")));
    assertTrue(files.stream().anyMatch(file -> file.getTemplateCode().equals("admin-frontend-route")));
    GeneratedFile listFile = files.stream()
        .filter(file -> file.getTemplateCode().equals("admin-frontend-list"))
        .findFirst()
        .orElseThrow();
    assertTrue(listFile.getContent().contains("requireAdminTenantId"));
    assertTrue(listFile.getContent().contains("requireAdminTenantId();"));
    assertTrue(listFile.getContent().contains("listPurchaseOrder(pageNo.value, pageSize.value)"));
    GeneratedFile routeFile = files.stream()
        .filter(file -> file.getTemplateCode().equals("admin-frontend-route"))
        .findFirst()
        .orElseThrow();
    assertTrue(routeFile.getContent().contains("export const purchaseOrderRoute"));
    assertTrue(routeFile.getContent().contains("component: () => import('@/views/purchase/purchaseOrder/index.vue')"));
    assertTrue(routeFile.getContent().contains("path: '/purchase/purchaseOrder'"));
    assertTrue(routeFile.getContent().contains("purchase:purchaseOrder:query"));
    assertFalse(routeFile.getContent().contains("purchaseOrderCreateRoute"));
    assertFalse(routeFile.getContent().contains("purchaseOrderEditRoute"));
    assertFalse(routeFile.getContent().contains("purchaseOrderDetailRoute"));
    assertFalse(routeFile.getContent().contains("@/views/purchase/purchaseOrder/form.vue"));
    assertFalse(routeFile.getContent().contains("@/views/purchase/purchaseOrder/detail.vue"));
    assertTrue(files.stream().noneMatch(file -> file.getTemplateCode().equals("admin-frontend-form")));
    assertTrue(files.stream().noneMatch(file -> file.getTemplateCode().equals("admin-frontend-detail")));
  }

  /**
   * 验证已保存页面模型使用自定义组件路径时，生成预览会写入页面模型指定路径。
   */
  @Test
  void shouldRewritePageFilePathBySavedPageModel() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable(), List.of(new LowcodePageModel(
            10L, "tenant_a", 1L, "LIST", "/purchase/orders",
            "zhyc-base-vue/src/views/purchase/orders/custom-list.vue", "TABLE"))),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    List<GeneratedFile> files = service.preview(new LowcodeGenerationPreviewCommand(
        "tenant_a", "purchase_order", GenerationTarget.ADMIN_FRONTEND, "purchase", "purchaseOrder"));

    assertEquals(3, files.size());
    assertTrue(files.stream()
        .filter(file -> file.getTemplateCode().equals("admin-frontend-list"))
        .anyMatch(file -> file.getPath().equals("zhyc-base-vue/src/views/purchase/orders/custom-list.vue")));
    GeneratedFile routeFile = files.stream()
        .filter(file -> file.getTemplateCode().equals("admin-frontend-route"))
        .findFirst()
        .orElseThrow();
    assertTrue(routeFile.getContent().contains("path: '/purchase/orders'"));
    assertTrue(routeFile.getContent().contains("component: () => import('@/views/purchase/orders/custom-list.vue')"));
  }

  /**
   * 验证后台列表、表单和详情页面模型同时存在时，路由片段会同步改写所有页面入口。
   */
  @Test
  void shouldRewriteAdminRouteFileBySavedListFormAndDetailPageModels() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable(), List.of(
            new LowcodePageModel(10L, "tenant_a", 1L, "LIST", "/purchase/orders",
                "zhyc-base-vue/src/views/purchase/orders/custom-list.vue", "TABLE"),
            new LowcodePageModel(11L, "tenant_a", 1L, "FORM", "/purchase/orders/form",
                "zhyc-base-vue/src/views/purchase/orders/custom-form.vue", "FORM"),
            new LowcodePageModel(12L, "tenant_a", 1L, "DETAIL", "/purchase/orders/detail/:id",
                "zhyc-base-vue/src/views/purchase/orders/custom-detail.vue", "DESCRIPTIONS"))),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    List<GeneratedFile> files = service.preview(new LowcodeGenerationPreviewCommand(
        "tenant_a", "purchase_order", GenerationTarget.ADMIN_FRONTEND, "purchase", "purchaseOrder"));

    assertEquals(5, files.size());
    GeneratedFile routeFile = files.stream()
        .filter(file -> file.getTemplateCode().equals("admin-frontend-route"))
        .findFirst()
        .orElseThrow();
    String routeContent = routeFile.getContent();
    assertTrue(routeContent.contains("path: '/purchase/orders'"));
    assertTrue(routeContent.contains("component: () => import('@/views/purchase/orders/custom-list.vue')"));
    assertTrue(routeContent.contains("path: '/purchase/orders/form'"));
    assertTrue(routeContent.contains("path: '/purchase/orders/form/:id/edit'"));
    assertTrue(routeContent.contains("component: () => import('@/views/purchase/orders/custom-form.vue')"));
    assertTrue(routeContent.contains("path: '/purchase/orders/detail/:id'"));
    assertTrue(routeContent.contains("component: () => import('@/views/purchase/orders/custom-detail.vue')"));
    assertTrue(files.stream()
        .filter(file -> file.getTemplateCode().equals("admin-frontend-form"))
        .anyMatch(file -> file.getPath().equals("zhyc-base-vue/src/views/purchase/orders/custom-form.vue")));
    assertTrue(files.stream()
        .filter(file -> file.getTemplateCode().equals("admin-frontend-detail"))
        .anyMatch(file -> file.getPath().equals("zhyc-base-vue/src/views/purchase/orders/custom-detail.vue")));
  }

  /**
   * 验证仅保存后台表单页面模型时，路由片段不会引用未生成的列表和详情页面。
   */
  @Test
  void shouldKeepOnlyFormRoutesWhenOnlySavedAdminFormPageModel() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable(), List.of(new LowcodePageModel(
            11L, "tenant_a", 1L, "FORM", "/purchase/orders/form",
            "zhyc-base-vue/src/views/purchase/orders/custom-form.vue", "FORM"))),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    List<GeneratedFile> files = service.preview(new LowcodeGenerationPreviewCommand(
        "tenant_a", "purchase_order", GenerationTarget.ADMIN_FRONTEND, "purchase", "purchaseOrder"));

    assertEquals(3, files.size());
    assertTrue(files.stream().anyMatch(file -> file.getTemplateCode().equals("admin-frontend-form")));
    assertTrue(files.stream().anyMatch(file -> file.getTemplateCode().equals("admin-frontend-api")));
    assertTrue(files.stream().anyMatch(file -> file.getTemplateCode().equals("admin-frontend-route")));
    assertTrue(files.stream().noneMatch(file -> file.getTemplateCode().equals("admin-frontend-list")));
    assertTrue(files.stream().noneMatch(file -> file.getTemplateCode().equals("admin-frontend-detail")));
    GeneratedFile routeFile = files.stream()
        .filter(file -> file.getTemplateCode().equals("admin-frontend-route"))
        .findFirst()
        .orElseThrow();
    String routeContent = routeFile.getContent();
    assertTrue(routeContent.contains("export const purchaseOrderCreateRoute"));
    assertTrue(routeContent.contains("export const purchaseOrderEditRoute"));
    assertTrue(routeContent.contains("path: '/purchase/orders/form'"));
    assertTrue(routeContent.contains("path: '/purchase/orders/form/:id/edit'"));
    assertTrue(routeContent.contains("component: () => import('@/views/purchase/orders/custom-form.vue')"));
    assertFalse(routeContent.contains("export const purchaseOrderRoute"));
    assertFalse(routeContent.contains("export const purchaseOrderDetailRoute"));
    assertFalse(routeContent.contains("@/views/purchase/purchaseOrder/index.vue"));
    assertFalse(routeContent.contains("@/views/purchase/purchaseOrder/detail.vue"));
  }

  /**
   * 验证已保存移动端页面模型时，只保留移动页面和 API 支撑文件。
   */
  @Test
  void shouldFilterUniappPageFilesBySavedMobilePageModel() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable(), List.of(new LowcodePageModel(
            10L, "tenant_a", 1L, "MOBILE", "/pages/purchase/orders/list",
            "zhyc-base-uniapp/src/pages/purchase/orders/list.vue", "UNIAPP_PAGE"))),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    List<GeneratedFile> files = service.preview(new LowcodeGenerationPreviewCommand(
        "tenant_a", "purchase_order", GenerationTarget.UNIAPP, "purchase", "purchaseOrder"));

    assertEquals(3, files.size());
    assertTrue(files.stream()
        .filter(file -> file.getTemplateCode().equals("uniapp-list"))
        .anyMatch(file -> file.getPath().equals("zhyc-base-uniapp/src/pages/purchase/orders/list.vue")));
    GeneratedFile pagesJsonFile = files.stream()
        .filter(file -> file.getTemplateCode().equals("uniapp-pages-json"))
        .findFirst()
        .orElseThrow();
    assertTrue(pagesJsonFile.getContent().contains("\"path\": \"pages/purchase/orders/list\""));
    assertTrue(pagesJsonFile.getContent().contains("\"navigationStyle\": \"custom\""));
    assertTrue(files.stream().anyMatch(file -> file.getTemplateCode().equals("uniapp-api")));
    assertTrue(files.stream().anyMatch(file -> file.getTemplateCode().equals("uniapp-pages-json")));
    assertTrue(files.stream().noneMatch(file -> file.getTemplateCode().equals("uniapp-form")));
    assertTrue(files.stream().noneMatch(file -> file.getTemplateCode().equals("uniapp-detail")));
  }

  /**
   * 验证已保存移动端表单和详情页面模型时，生成预览会保留并改写对应页面路径。
   */
  @Test
  void shouldFilterUniappFormAndDetailFilesBySavedMobilePageModels() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable(), List.of(
            new LowcodePageModel(10L, "tenant_a", 1L, "MOBILE", "/pages/purchase/orders/list",
                "zhyc-base-uniapp/src/pages/purchase/orders/list.vue", "UNIAPP_PAGE"),
            new LowcodePageModel(11L, "tenant_a", 1L, "MOBILE_FORM", "/pages/purchase/orders/form",
                "zhyc-base-uniapp/src/pages/purchase/orders/form.vue", "UNIAPP_PAGE"),
            new LowcodePageModel(12L, "tenant_a", 1L, "MOBILE_DETAIL", "/pages/purchase/orders/detail",
                "zhyc-base-uniapp/src/pages/purchase/orders/detail.vue", "UNIAPP_PAGE"))),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    List<GeneratedFile> files = service.preview(new LowcodeGenerationPreviewCommand(
        "tenant_a", "purchase_order", GenerationTarget.UNIAPP, "purchase", "purchaseOrder"));

    assertEquals(5, files.size());
    assertTrue(files.stream()
        .filter(file -> file.getTemplateCode().equals("uniapp-form"))
        .anyMatch(file -> file.getPath().equals("zhyc-base-uniapp/src/pages/purchase/orders/form.vue")));
    assertTrue(files.stream()
        .filter(file -> file.getTemplateCode().equals("uniapp-detail"))
        .anyMatch(file -> file.getPath().equals("zhyc-base-uniapp/src/pages/purchase/orders/detail.vue")));
    GeneratedFile pagesJsonFile = files.stream()
        .filter(file -> file.getTemplateCode().equals("uniapp-pages-json"))
        .findFirst()
        .orElseThrow();
    assertTrue(pagesJsonFile.getContent().contains("\"path\": \"pages/purchase/orders/list\""));
    assertTrue(pagesJsonFile.getContent().contains("\"path\": \"pages/purchase/orders/form\""));
    assertTrue(pagesJsonFile.getContent().contains("\"path\": \"pages/purchase/orders/detail\""));
    assertTrue(pagesJsonFile.getContent().contains("\"navigationStyle\": \"custom\""));
    assertTrue(files.stream().anyMatch(file -> file.getTemplateCode().equals("uniapp-api")));
    assertTrue(files.stream().anyMatch(file -> file.getTemplateCode().equals("uniapp-pages-json")));
  }


  /**
   * 验证草稿表模型不能进入生成预览，避免未发布模型被后台或开放 API 误用。
   */
  @Test
  void shouldRejectPreviewForDraftTableModel() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(draftTable()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.preview(new LowcodeGenerationPreviewCommand(
            "tenant_a", "purchase_order", GenerationTarget.ADMIN_BACKEND, "purchase", "purchaseOrder")));

    assertEquals("表模型未发布，不能生成代码: purchase_order", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_GENERATION_TABLE_MODEL_NOT_PUBLISHED", exception.getCode());
  }

  /**
   * 验证后台后端生成目标包含 DDL 脚本文件，满足业务模块数据库脚本同步生成。
   */
  @Test
  void shouldGenerateDdlFileForAdminBackendTarget() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    List<GeneratedFile> files = service.preview(new LowcodeGenerationPreviewCommand(
        "tenant_a", "purchase_order", GenerationTarget.ADMIN_BACKEND, "purchase", "purchaseOrder"));

    GeneratedFile ddlFile = files.stream()
        .filter(file -> file.getTemplateCode().equals("admin-backend-ddl"))
        .findFirst()
        .orElseThrow();
    assertEquals("zhyc-module-purchase/src/main/resources/db/V1__purchase_purchaseOrder.sql", ddlFile.getPath());
    assertTrue(ddlFile.getContent().contains("CREATE TABLE IF NOT EXISTS `pur_order`"));
    assertTrue(ddlFile.getContent().contains("COMMENT='采购订单'"));
    assertTrue(ddlFile.getContent().contains("`tenant_id` VARCHAR(64) NOT NULL COMMENT '租户业务编码，用于共享表模式下的数据隔离'"));
    assertTrue(ddlFile.getContent().contains("`order_no` VARCHAR(64) NOT NULL COMMENT '订单编号'"));
    assertTrue(ddlFile.getContent().contains("`deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除'"));
    assertTrue(ddlFile.getContent().contains("`version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'"));
    assertTrue(ddlFile.getContent().contains("KEY `idx_pur_order_tenant_deleted` (`tenant_id`, `deleted`)"));
  }

  /**
   * 验证后台后端 DDL 生成会按表模型绑定的数据源方言调用统一方言服务。
   */
  @Test
  void shouldGenerateDdlByBoundDataSourceDialect() {
    StubLowcodeDbDialectService dialectService = new StubLowcodeDbDialectService();
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable(), List.of(), List.of(new LowcodeDataSource(
            1L, "tenant_a", "main", "主库", LowcodeDatabaseDialect.MYSQL,
            "jdbc:mysql://localhost:3306/zhyc", "root", true))),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()),
        null,
        null,
        null,
        dialectService);

    List<GeneratedFile> files = service.preview(new LowcodeGenerationPreviewCommand(
        "tenant_a", "purchase_order", GenerationTarget.ADMIN_BACKEND, "purchase", "purchaseOrder"));

    GeneratedFile ddlFile = files.stream()
        .filter(file -> file.getTemplateCode().equals("admin-backend-ddl"))
        .findFirst()
        .orElseThrow();
    assertEquals("mysql", dialectService.lastDdlDialectCode);
    assertEquals("pur_order", dialectService.lastDdlTableName);
    assertTrue(ddlFile.getContent().contains("-- DIALECT: mysql TABLE: pur_order"));
  }

  /**
   * 验证生成前校验会阻断缺少主键的表模型，避免生成无法稳定查询和更新的业务代码。
   */
  @Test
  void shouldValidateGenerationAndRejectTableWithoutPrimaryKey() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTableWithoutPrimaryKey()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    LowcodeGenerationValidationResult result = service.validate(new LowcodeGenerationPreviewCommand(
        "tenant_a", "purchase_order", GenerationTarget.ADMIN_BACKEND, "purchase", "purchaseOrder"));

    assertFalse(result.isPassed());
    assertEquals(1, result.getErrors().size());
    assertEquals("PRIMARY_KEY_REQUIRED", result.getErrors().get(0).getCode());
    assertTrue(result.getErrors().get(0).getMessage().contains("必须配置主键字段"));
  }

  /**
   * 验证生成前校验会阻断缺少租户字段或逻辑删除字段的表模型，确保首期 SaaS 共享表隔离约束落地。
   */
  @Test
  void shouldValidateGenerationAndRejectTableWithoutTenantOrDeletedColumn() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTableWithoutTenantAndDeletedColumns()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    LowcodeGenerationValidationResult result = service.validate(new LowcodeGenerationPreviewCommand(
        "tenant_a", "purchase_order", GenerationTarget.ADMIN_BACKEND, "purchase", "purchaseOrder"));

    assertFalse(result.isPassed());
    assertTrue(result.getErrors().stream()
        .anyMatch(item -> item.getCode().equals("TENANT_COLUMN_REQUIRED")
            && item.getMessage().contains("tenant_id")));
    assertTrue(result.getErrors().stream()
        .anyMatch(item -> item.getCode().equals("DELETED_COLUMN_REQUIRED")
            && item.getMessage().contains("deleted")));
  }

  /**
   * 验证生成前校验会识别非法字段编码和重复字段编码，提前阻断坏模板上下文。
   */
  @Test
  void shouldValidateGenerationAndRejectIllegalOrDuplicateColumnCodes() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTableWithIllegalColumns()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    LowcodeGenerationValidationResult result = service.validate(new LowcodeGenerationPreviewCommand(
        "tenant_a", "purchase_order", GenerationTarget.ADMIN_BACKEND, "purchase", "purchaseOrder"));

    assertFalse(result.isPassed());
    assertTrue(result.getErrors().stream()
        .anyMatch(item -> item.getCode().equals("COLUMN_CODE_ILLEGAL")
            && item.getMessage().contains("order-no")));
    assertTrue(result.getErrors().stream()
        .anyMatch(item -> item.getCode().equals("COLUMN_CODE_DUPLICATED")
            && item.getMessage().contains("order_no")));
  }

  /**
   * 验证生成前校验会给出命名规范警告，但不阻断符合硬性规则的表模型。
   */
  @Test
  void shouldValidateGenerationAndWarnWhenNamesAreNotRecommended() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    LowcodeGenerationValidationResult result = service.validate(new LowcodeGenerationPreviewCommand(
        "tenant_a", "purchase_order", GenerationTarget.ADMIN_BACKEND, "Purchase", "PurchaseOrder"));

    assertTrue(result.isPassed());
    assertTrue(result.getWarnings().stream()
        .anyMatch(item -> item.getCode().equals("MODULE_NAME_RECOMMENDED_LOWER_CAMEL")));
    assertTrue(result.getWarnings().stream()
        .anyMatch(item -> item.getCode().equals("ENTITY_NAME_RECOMMENDED_LOWER_CAMEL")));
  }

  /**
   * 验证生成前校验会阻断包含路径字符的模块名和实体名，避免生成路径、包名和路由失控。
   */
  @Test
  void shouldValidateGenerationAndRejectUnsafeModuleOrEntityName() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    LowcodeGenerationValidationResult result = service.validate(new LowcodeGenerationPreviewCommand(
        "tenant_a", "purchase_order", GenerationTarget.ADMIN_BACKEND, "../purchase", "purchase-order"));

    assertFalse(result.isPassed());
    assertTrue(result.getErrors().stream()
        .anyMatch(item -> item.getCode().equals("MODULE_NAME_ILLEGAL")
            && item.getMessage().contains("../purchase")));
    assertTrue(result.getErrors().stream()
        .anyMatch(item -> item.getCode().equals("ENTITY_NAME_ILLEGAL")
            && item.getMessage().contains("purchase-order")));
  }

  /**
   * 验证生成预览在生成前校验失败时返回稳定业务错误码，避免向前端暴露运行时异常类型。
   */
  @Test
  void shouldRejectPreviewWhenValidationFailedWithBusinessCode() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTableWithoutPrimaryKey()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.preview(new LowcodeGenerationPreviewCommand(
            "tenant_a", "purchase_order", GenerationTarget.ADMIN_BACKEND, "purchase", "purchaseOrder")));

    assertEquals("ZHYC_LOWCODE_GENERATION_VALIDATION_FAILED", exception.getCode());
    assertTrue(exception.getMessage().contains("生成前校验未通过"));
    assertTrue(exception.getMessage().contains("必须配置主键字段"));
  }

  /**
   * 验证执行生成会写入文件并保存成功生成记录。
   */
  @Test
  void shouldExecuteGenerationAndSaveSuccessRecord() {
    MemoryGenerationRecordRepository recordRepository = new MemoryGenerationRecordRepository();
    StubGeneratedFileWriter fileWriter = new StubGeneratedFileWriter(false);
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()),
        recordRepository,
        fileWriter);

    LowcodeGenerationRecord record = service.execute(new LowcodeGenerationExecuteCommand(
        "tenant_a", "purchase_order", GenerationTarget.UNIAPP, "purchase", "purchaseOrder",
        GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS));

    assertEquals(LowcodeGenerationRecordStatus.SUCCESS, record.getStatus());
    assertEquals(5, record.getFileCount());
    assertEquals(5, fileWriter.writtenFiles.size());
    assertEquals(GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS, fileWriter.lastOverwriteStrategy);
    assertEquals(1, recordRepository.records.size());
  }

  /**
   * 验证执行生成会保存文件路径、模板、内容哈希和写入模式清单。
   */
  @Test
  void shouldSaveFileManifestWhenExecuteGeneration() {
    MemoryGenerationRecordRepository recordRepository = new MemoryGenerationRecordRepository();
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()),
        recordRepository,
        new StubGeneratedFileWriter(false));

    LowcodeGenerationRecord record = service.execute(new LowcodeGenerationExecuteCommand(
        "tenant_a", "purchase_order", GenerationTarget.UNIAPP, "purchase", "purchaseOrder",
        GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS));

    assertTrue(record.getFileManifestJson().contains("\"templateCode\":\"uniapp-list\""));
    assertTrue(record.getFileManifestJson().contains("\"templateCode\":\"uniapp-pages-json\""));
    assertTrue(record.getFileManifestJson().contains("\"targetPath\":\"zhyc-base-uniapp/src/pages/purchase/purchaseOrder/list.vue\""));
    assertTrue(record.getFileManifestJson().contains("\"contentHash\""));
    assertTrue(record.getFileManifestJson().contains("\"writeMode\":\"FAIL_IF_EXISTS\""));
  }

  /**
   * 验证执行生成会保存独立文件明细，满足按文件内容哈希追踪覆盖风险。
   */
  @Test
  void shouldSaveGenerationFileDetailsWhenExecuteGeneration() {
    MemoryGenerationRecordRepository recordRepository = new MemoryGenerationRecordRepository();
    MemoryGenerationFileRepository generationFileRepository = new MemoryGenerationFileRepository();
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()),
        recordRepository,
        generationFileRepository,
        new StubGeneratedFileWriter(false));

    LowcodeGenerationRecord record = service.execute(new LowcodeGenerationExecuteCommand(
        "tenant_a", "purchase_order", GenerationTarget.UNIAPP, "purchase", "purchaseOrder",
        GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS));

    assertEquals(101L, record.getId());
    assertEquals(5, generationFileRepository.files.size());
    assertEquals(101L, generationFileRepository.files.get(0).getRecordId());
    assertEquals("tenant_a", generationFileRepository.files.get(0).getTenantId());
    assertEquals("vue", generationFileRepository.files.get(0).getFileType());
    assertEquals(GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS, generationFileRepository.files.get(0).getOverwriteMode());
    assertTrue(generationFileRepository.files.get(0).getContentHash().length() >= 64);
  }

  /**
   * 验证执行生成失败时会保存失败记录并继续抛出原始错误。
   */
  @Test
  void shouldSaveFailedRecordWhenExecutionFails() {
    MemoryGenerationRecordRepository recordRepository = new MemoryGenerationRecordRepository();
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()),
        recordRepository,
        new StubGeneratedFileWriter(true));

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> service.execute(new LowcodeGenerationExecuteCommand(
            "tenant_a", "purchase_order", GenerationTarget.UNIAPP, "purchase", "purchaseOrder",
            GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS)));

    assertEquals("写入失败", exception.getMessage());
    assertEquals(1, recordRepository.records.size());
    assertEquals(LowcodeGenerationRecordStatus.FAILED, recordRepository.records.get(0).getStatus());
    assertEquals("写入失败", recordRepository.records.get(0).getErrorMessage());
  }

  /**
   * 验证生成服务按租户查询生成历史，供后台审计和覆盖保护复核。
   */
  @Test
  void shouldListGenerationRecordsByTenant() {
    MemoryGenerationRecordRepository recordRepository = new MemoryGenerationRecordRepository();
    recordRepository.save(LowcodeGenerationRecord.success(
        "tenant_a", "purchase_order", GenerationTarget.ADMIN_BACKEND, "purchase", "purchaseOrder",
        GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS, 2));
    recordRepository.save(LowcodeGenerationRecord.success(
        "tenant_b", "sale_order", GenerationTarget.ADMIN_BACKEND, "sale", "saleOrder",
        GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS, 1));
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()),
        recordRepository,
        new StubGeneratedFileWriter(false));

    List<LowcodeGenerationRecord> records = service.listRecords("tenant_a");

    assertEquals(1, records.size());
    assertEquals("purchase_order", records.get(0).getTableModelCode());
    assertEquals("tenant_a", records.get(0).getTenantId());
  }

  /**
   * 验证生成历史查询拒绝空租户并返回稳定业务错误码。
   */
  @Test
  void shouldRejectBlankTenantWhenListingGenerationRecords() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()),
        new MemoryGenerationRecordRepository(),
        new StubGeneratedFileWriter(false));

    BusinessException exception = assertThrows(BusinessException.class, () -> service.listRecords(" "));

    assertEquals("ZHYC_LOWCODE_GENERATION_RECORD_TENANT_REQUIRED", exception.getCode());
    assertEquals("租户业务编码不能为空", exception.getMessage());
  }

  /**
   * 验证生成历史仓储未配置时返回稳定业务错误码。
   */
  @Test
  void shouldRejectListRecordsWhenRepositoryMissingWithBusinessCode() {
    LowcodeGeneratorService service = generatorService();

    BusinessException exception = assertThrows(BusinessException.class, () -> service.listRecords("tenant_a"));

    assertEquals("ZHYC_LOWCODE_GENERATION_RECORD_REPOSITORY_MISSING", exception.getCode());
    assertEquals("生成记录仓储未配置", exception.getMessage());
  }

  /**
   * 验证生成服务按租户和生成记录查询文件明细。
   */
  @Test
  void shouldListGenerationFilesByTenantAndRecord() {
    MemoryGenerationFileRepository generationFileRepository = new MemoryGenerationFileRepository();
    generationFileRepository.files.add(new LowcodeGenerationFile(
        1L, "tenant_a", 101L, "uniapp-list", "a.vue", "vue",
        GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS, "hash-a"));
    generationFileRepository.files.add(new LowcodeGenerationFile(
        2L, "tenant_b", 101L, "uniapp-list", "b.vue", "vue",
        GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS, "hash-b"));
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()),
        new MemoryGenerationRecordRepository(),
        generationFileRepository,
        new StubGeneratedFileWriter(false));

    List<LowcodeGenerationFile> files = service.listGenerationFiles("tenant_a", 101L);

    assertEquals(1, files.size());
    assertEquals("hash-a", files.get(0).getContentHash());
  }

  /**
   * 验证生成文件明细查询拒绝空租户和空记录主键并返回稳定业务错误码。
   */
  @Test
  void shouldRejectInvalidArgumentsWhenListingGenerationFiles() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()),
        new MemoryGenerationRecordRepository(),
        new MemoryGenerationFileRepository(),
        new StubGeneratedFileWriter(false));

    BusinessException tenantException = assertThrows(BusinessException.class,
        () -> service.listGenerationFiles("", 101L));
    BusinessException recordException = assertThrows(BusinessException.class,
        () -> service.listGenerationFiles("tenant_a", null));

    assertEquals("ZHYC_LOWCODE_GENERATION_RECORD_TENANT_REQUIRED", tenantException.getCode());
    assertEquals("租户业务编码不能为空", tenantException.getMessage());
    assertEquals("ZHYC_LOWCODE_GENERATION_FILE_RECORD_ID_REQUIRED", recordException.getCode());
    assertEquals("生成记录主键不能为空", recordException.getMessage());
  }

  /**
   * 验证生成文件明细仓储未配置时返回稳定业务错误码。
   */
  @Test
  void shouldRejectListGenerationFilesWhenRepositoryMissingWithBusinessCode() {
    LowcodeGeneratorService service = new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable()),
        new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
        new DefaultCodeGenerator(new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider())),
            new SimpleStringTemplateRenderer()),
        new MemoryGenerationRecordRepository(),
        new StubGeneratedFileWriter(false));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.listGenerationFiles("tenant_a", 101L));

    assertEquals("ZHYC_LOWCODE_GENERATION_FILE_REPOSITORY_MISSING", exception.getCode());
    assertEquals("生成文件明细仓储未配置", exception.getMessage());
  }

  private LowcodeTableModel publishedTable() {
    return new LowcodeTableModel(
        1L, "tenant_a", 1L, "purchase_order", "采购订单", "pur_order", LowcodeModelStatus.PUBLISHED,
        List.of(
            LowcodeColumnModel.builder("id", "主键", LowcodeFieldType.LONG)
                .primaryKey(true)
                .required(true)
                .build(),
            LowcodeColumnModel.builder("tenant_id", "租户业务编码，用于共享表模式下的数据隔离", LowcodeFieldType.STRING)
                .length(64)
                .required(true)
                .build(),
            LowcodeColumnModel.builder("deleted", "逻辑删除标识，0未删除，1已删除", LowcodeFieldType.BOOLEAN)
                .required(true)
                .build(),
            LowcodeColumnModel.builder("order_no", "订单编号", LowcodeFieldType.STRING)
                .length(64)
                .required(true)
                .listVisible(true)
                .formVisible(true)
                .build()));
  }

  private void assertTemplateCodes(LowcodeGeneratorService service, GenerationTarget target,
      String... expectedCodes) {
    List<String> actualCodes = service.listTemplates(target).stream()
        .map(CodeTemplateDescriptor::getCode)
        .toList();
    for (String expectedCode : expectedCodes) {
      assertTrue(actualCodes.contains(expectedCode),
          "目标端 " + target + " 缺少模板: " + expectedCode + "，实际模板: " + actualCodes);
    }
  }

  private LowcodeTableModel draftTable() {
    return new LowcodeTableModel(
        1L, "tenant_a", 1L, "purchase_order", "采购订单", "pur_order", LowcodeModelStatus.DRAFT,
        List.of(LowcodeColumnModel.builder("id", "主键", LowcodeFieldType.LONG)
            .primaryKey(true)
            .required(true)
            .build()));
  }

  private LowcodeTableModel publishedTableWithoutPrimaryKey() {
    return new LowcodeTableModel(
        1L, "tenant_a", 1L, "purchase_order", "采购订单", "pur_order", LowcodeModelStatus.PUBLISHED,
        List.of(
            LowcodeColumnModel.builder("tenant_id", "租户业务编码，用于共享表模式下的数据隔离", LowcodeFieldType.STRING)
                .length(64)
                .required(true)
                .build(),
            LowcodeColumnModel.builder("deleted", "逻辑删除标识，0未删除，1已删除", LowcodeFieldType.BOOLEAN)
                .required(true)
                .build(),
            LowcodeColumnModel.builder("order_no", "订单编号", LowcodeFieldType.STRING)
                .length(64)
                .required(true)
                .build()));
  }

  private LowcodeTableModel publishedTableWithoutTenantAndDeletedColumns() {
    return new LowcodeTableModel(
        1L, "tenant_a", 1L, "purchase_order", "采购订单", "pur_order", LowcodeModelStatus.PUBLISHED,
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

  private LowcodeTableModel publishedTableWithIllegalColumns() {
    return new LowcodeTableModel(
        1L, "tenant_a", 1L, "purchase_order", "采购订单", "pur_order", LowcodeModelStatus.PUBLISHED,
        List.of(
            LowcodeColumnModel.builder("id", "主键", LowcodeFieldType.LONG)
                .primaryKey(true)
                .required(true)
                .build(),
            LowcodeColumnModel.builder("tenant_id", "租户业务编码，用于共享表模式下的数据隔离", LowcodeFieldType.STRING)
                .length(64)
                .required(true)
                .build(),
            LowcodeColumnModel.builder("deleted", "逻辑删除标识，0未删除，1已删除", LowcodeFieldType.BOOLEAN)
                .required(true)
                .build(),
            LowcodeColumnModel.builder("order-no", "订单编号", LowcodeFieldType.STRING)
                .length(64)
                .required(true)
                .build(),
            LowcodeColumnModel.builder("order_no", "订单编号", LowcodeFieldType.STRING)
                .length(64)
                .required(true)
                .build(),
            LowcodeColumnModel.builder("order_no", "订单编号副本", LowcodeFieldType.STRING)
                .length(64)
                .required(true)
            .build()));
  }

  /**
   * 创建使用已发布表模型的生成服务，供基础入口校验类用例复用。
   *
   * @return 低代码生成应用服务
   */
  private LowcodeGeneratorService generatorService() {
    CodeTemplateRegistry templateRegistry = new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider()));
    return new DefaultLowcodeGeneratorService(
        new StubMetadataService(publishedTable()),
        templateRegistry,
        new DefaultCodeGenerator(templateRegistry, new SimpleStringTemplateRenderer()));
  }

  /**
   * 测试用低代码元数据服务。
   */
  private static class StubMetadataService implements LowcodeMetadataService {

    /** 固定返回的表模型。 */
    private final LowcodeTableModel tableModel;
    /** 固定返回的页面模型列表。 */
    private final List<LowcodePageModel> pageModels;
    /** 固定返回的数据源列表。 */
    private final List<LowcodeDataSource> dataSources;

    private StubMetadataService(LowcodeTableModel tableModel) {
      this(tableModel, List.of());
    }

    private StubMetadataService(LowcodeTableModel tableModel, List<LowcodePageModel> pageModels) {
      this(tableModel, pageModels, List.of());
    }

    private StubMetadataService(LowcodeTableModel tableModel, List<LowcodePageModel> pageModels,
                                List<LowcodeDataSource> dataSources) {
      this.tableModel = tableModel;
      this.pageModels = pageModels;
      this.dataSources = dataSources;
    }

    @Override
    public com.zhyc.lowcode.metadata.domain.LowcodeDataSource saveDataSource(
        com.zhyc.lowcode.metadata.domain.LowcodeDataSource dataSource) {
      throw new UnsupportedOperationException("测试不需要保存数据源");
    }

    @Override
    public com.zhyc.lowcode.metadata.domain.LowcodeDataSource getDataSource(String tenantId, String code) {
      throw new UnsupportedOperationException("测试不需要查询数据源");
    }

    @Override
    public List<com.zhyc.lowcode.metadata.domain.LowcodeDataSource> listDataSources(String tenantId) {
      return dataSources.stream()
          .filter(dataSource -> dataSource.getTenantId().equals(tenantId))
          .toList();
    }

    @Override
    public List<LowcodePhysicalTable> listPhysicalTables(String tenantId, Long dataSourceId) {
      throw new UnsupportedOperationException("测试不需要查询物理表清单");
    }

    @Override
    public com.zhyc.lowcode.metadata.service.LowcodeDataSourceConnectionTestResult testDataSourceConnection(
        String tenantId, String code) {
      throw new UnsupportedOperationException("测试不需要测试数据源连接");
    }

    @Override
    public LowcodeTableModel saveTableModel(LowcodeTableModel tableModel) {
      throw new UnsupportedOperationException("测试不需要保存表模型");
    }

    @Override
    public LowcodeTableModel getTableModel(String tenantId, String code) {
      return tableModel;
    }

    @Override
    public LowcodeTableModel importTableModel(String tenantId, Long dataSourceId, String tableName,
                                              String modelCode, String modelName) {
      throw new UnsupportedOperationException("测试不需要导入表模型");
    }

    @Override
    public List<LowcodeTableModel> listTableModels(String tenantId) {
      throw new UnsupportedOperationException("测试不需要查询表模型列表");
    }

    @Override
    public LowcodeTableModel publishTableModel(String tenantId, String code) {
      throw new UnsupportedOperationException("测试不需要发布表模型");
    }

    @Override
    public LowcodeTableRelation saveTableRelation(LowcodeTableRelation relation) {
      throw new UnsupportedOperationException("测试不需要保存表关系");
    }

    @Override
    public List<LowcodeTableRelation> listTableRelations(String tenantId) {
      throw new UnsupportedOperationException("测试不需要查询表关系列表");
    }

    @Override
    public LowcodePageModel savePageModel(LowcodePageModel pageModel) {
      throw new UnsupportedOperationException("测试不需要保存页面模型");
    }

    @Override
    public List<LowcodePageModel> listPageModels(String tenantId) {
      return pageModels.stream()
          .filter(pageModel -> pageModel.getTenantId().equals(tenantId))
          .toList();
    }
  }

  /**
   * 测试用生成记录仓储。
   */
  private static class MemoryGenerationRecordRepository implements LowcodeGenerationRecordRepository {

    /** 已保存生成记录。 */
    private final List<LowcodeGenerationRecord> records = new ArrayList<>();

    @Override
    public LowcodeGenerationRecord save(LowcodeGenerationRecord record) {
      LowcodeGenerationRecord savedRecord = LowcodeGenerationRecord.restore(
          101L,
          record.getTenantId(),
          record.getTableModelCode(),
          record.getTarget(),
          record.getModuleName(),
          record.getEntityName(),
          record.getOverwriteStrategy(),
          record.getFileCount(),
          record.getFileManifestJson(),
          record.getStatus(),
          record.getErrorMessage());
      records.add(savedRecord);
      return savedRecord;
    }

    @Override
    public List<LowcodeGenerationRecord> findByTenantId(String tenantId) {
      return records.stream()
          .filter(record -> record.getTenantId().equals(tenantId))
          .toList();
    }
  }

  /**
   * 测试用生成文件明细仓储。
   */
  private static class MemoryGenerationFileRepository implements LowcodeGenerationFileRepository {

    /** 已保存生成文件明细。 */
    private final List<LowcodeGenerationFile> files = new ArrayList<>();

    @Override
    public void saveAll(List<LowcodeGenerationFile> files) {
      this.files.addAll(files);
    }

    @Override
    public List<LowcodeGenerationFile> findByTenantIdAndRecordId(String tenantId, Long recordId) {
      return files.stream()
          .filter(file -> file.getTenantId().equals(tenantId))
          .filter(file -> file.getRecordId().equals(recordId))
          .toList();
    }
  }

  /**
   * 测试用生成文件写入器。
   */
  private static class StubGeneratedFileWriter implements GeneratedFileWriter {

    /** 是否模拟写入失败。 */
    private final boolean fail;
    /** 已写入生成文件。 */
    private final List<GeneratedFile> writtenFiles = new ArrayList<>();
    /** 最近一次写入使用的覆盖策略。 */
    private GeneratedFileOverwriteStrategy lastOverwriteStrategy;

    private StubGeneratedFileWriter(boolean fail) {
      this.fail = fail;
    }

    @Override
    public List<Path> write(List<GeneratedFile> files) {
      return write(files, null);
    }

    @Override
    public List<Path> write(List<GeneratedFile> files, GeneratedFileOverwriteStrategy overwriteStrategy) {
      if (fail) {
        throw new IllegalStateException("写入失败");
      }
      lastOverwriteStrategy = overwriteStrategy;
      writtenFiles.addAll(files);
      return files.stream()
          .map(file -> Path.of(file.getPath()))
          .toList();
    }
  }

  /**
   * 测试用低代码数据库方言服务。
   */
  private static class StubLowcodeDbDialectService implements LowcodeDbDialectService {

    /** 最近一次 DDL 生成使用的方言编码。 */
    private String lastDdlDialectCode;
    /** 最近一次 DDL 生成使用的物理表名。 */
    private String lastDdlTableName;

    @Override
    public String generateCreateTable(String dialectCode, LowcodeTable table) {
      this.lastDdlDialectCode = dialectCode;
      this.lastDdlTableName = table.getName();
      return "-- DIALECT: " + dialectCode + " TABLE: " + table.getName();
    }

    @Override
    public String mapFieldType(String dialectCode, LowcodeColumn column) {
      return "VARCHAR(64)";
    }

    @Override
    public String applyPagination(String dialectCode, String sql, long offset, long pageSize) {
      return sql + " LIMIT " + offset + ", " + pageSize;
    }

    @Override
    public List<String> listDdlDialectCodes() {
      return List.of("mysql");
    }

    @Override
    public List<String> listFieldTypeDialectCodes() {
      return List.of("mysql");
    }

    @Override
    public List<String> listPaginationDialectCodes() {
      return List.of("mysql");
    }
  }
}
