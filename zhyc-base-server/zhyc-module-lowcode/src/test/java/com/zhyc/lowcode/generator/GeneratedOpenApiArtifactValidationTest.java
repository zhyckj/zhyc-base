/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

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
 * 生成开放 API 与开发者门户产物静态校验测试。
 *
 * <p>该测试把开放 API 生成结果落盘到临时目录，校验 Controller、DTO、网关配置、目录注册 SQL、
 * API 文档和开发者门户页的关键契约，确保生成产物能被开放 API 网关运行态路由和门户审阅使用。</p>
 */
class GeneratedOpenApiArtifactValidationTest {

  /** 生成文件临时输出根目录。 */
  @TempDir
  private Path outputRoot;

  /**
   * 验证开放 API 生成产物包含网关路由、目录注册、文档和门户调试入口。
   *
   * @throws IOException 读取临时生成文件失败时抛出
   */
  @Test
  void shouldValidateGeneratedOpenApiGatewayAndPortalArtifacts() throws IOException {
    List<GeneratedFile> files = generate();
    new FileSystemGeneratedFileWriter(outputRoot).write(files, GeneratedFileOverwriteStrategy.OVERWRITE);

    String controller = Files.readString(outputRoot.resolve(
        "zhyc-module-purchase/src/main/java/com/zhyc/purchase/openapi/PurchaseOrderOpenApiController.java"));
    assertTrue(controller.contains("@RequestMapping(\"/openapi/v1/purchase/purchaseOrder\")"));
    assertTrue(controller.contains("X-ZHYC-Tenant-Id"));
    assertTrue(controller.contains("X-ZHYC-App-Code"));
    assertTrue(controller.contains("X-ZHYC-Api-Code"));
    assertTrue(controller.contains("X-ZHYC-Request-Id"));
    assertTrue(controller.contains("@RequestHeader(value = \"X-ZHYC-Tenant-Id\", required = false) String tenantId"));
    assertTrue(controller.contains("@RequestHeader(value = \"X-ZHYC-App-Code\", required = false) String appCode"));
    assertTrue(controller.contains("@RequestHeader(value = \"X-ZHYC-Api-Code\", required = false) String apiCode"));
    assertTrue(controller.contains("import com.zhyc.common.exception.BusinessException;"));
    assertTrue(controller.contains("requireGatewayContext(tenantId, appCode, apiCode);"));
    assertTrue(controller.contains("开放 API 网关上下文不能为空"));
    assertTrue(controller.contains("ZHYC_OPENAPI_GATEWAY_CONTEXT_MISSING"));
    assertTrue(controller.contains("开放 API 编码不匹配"));
    assertTrue(controller.contains("ZHYC_OPENAPI_API_CODE_MISMATCH"));
    assertTrue(controller.contains("!API_CODE.equals(apiCode)"));
    assertFalse(controller.contains("throw new IllegalArgumentException"));
    assertTrue(controller.contains("ApiResult<PurchaseOrderOpenApiResponse>"));
    assertFalse(controller.contains("{Entity}"));

    String routeConfig = Files.readString(outputRoot.resolve(
        "zhyc-openapi-gateway/src/main/resources/openapi/purchase-purchaseOrder-route.yml"));
    assertTrue(routeConfig.contains("apiCode: purchase-purchaseOrder"));
    assertTrue(routeConfig.contains("pathPattern: /openapi/v1/purchase/purchaseOrder"));
    assertTrue(routeConfig.contains("backendRoute: http://zhyc-platform-app/openapi/v1/purchase/purchaseOrder"));
    assertTrue(routeConfig.contains("- API_KEY"));
    assertTrue(routeConfig.contains("- OAUTH2"));
    assertTrue(routeConfig.contains("auditEnabled: true"));

    String registrationSql = Files.readString(outputRoot.resolve(
        "zhyc-module-purchase/src/main/resources/db/V1__purchase_purchaseOrder_openapi.sql"));
    assertTrue(registrationSql.contains("INSERT INTO openapi_catalog"));
    assertTrue(registrationSql.contains("'purchase-purchaseOrder'"));
    assertTrue(registrationSql.contains("'/openapi/v1/purchase/purchaseOrder'"));
    assertTrue(registrationSql.contains("INSERT INTO openapi_version"));
    assertTrue(registrationSql.contains("'http://zhyc-platform-app/openapi/v1/purchase/purchaseOrder'"));
    assertTrue(registrationSql.contains("ON DUPLICATE KEY UPDATE"));

    String doc = Files.readString(outputRoot.resolve("zhyc-openapi-portal/docs/purchase/purchaseOrder.md"));
    assertTrue(doc.contains("GET /openapi/v1/purchase/purchaseOrder"));
    assertTrue(doc.contains("## 认证方式"));
    assertTrue(doc.contains("API Key 面向系统集成"));
    assertTrue(doc.contains("OAuth2/OIDC 面向第三方应用"));
    assertTrue(doc.contains("Authorization: Bearer <access_token>"));
    assertTrue(doc.contains("## 接口地址"));
    assertTrue(doc.contains("## 后端路由"));
    assertTrue(doc.contains("http://zhyc-platform-app/openapi/v1/purchase/purchaseOrder"));
    assertTrue(doc.contains("## 错误码"));
    assertTrue(doc.contains("## 审计字段"));
    assertTrue(doc.contains("X-ZHYC-Signature"));
    assertFalse(doc.contains("## Authentication"));
    assertFalse(doc.contains("## Backend Route"));
    assertFalse(doc.contains("## Error Codes"));
    assertFalse(doc.contains("## Audit Fields"));

    String debugApi = Files.readString(outputRoot.resolve(
        "zhyc-base-vue/src/api/developer/purchase-purchaseOrder-debug.ts"));
    assertTrue(debugApi.contains("export interface PurchaseOrderOpenApiDebugRequest"));
    assertTrue(debugApi.contains("export interface PurchaseOrderOpenApiDebugResponse"));
    assertTrue(debugApi.contains("export function invokePurchaseOrderOpenApiDebug"));
    assertTrue(debugApi.contains("request<PurchaseOrderOpenApiDebugResponse, PurchaseOrderOpenApiDebugRequest>"));
    assertTrue(debugApi.contains("'/openapi/debug/invoke'"));
    assertTrue(debugApi.contains("method: 'POST'"));
    assertTrue(debugApi.contains("API Key 或 OAuth2/OIDC 调试认证方式"));
    assertTrue(debugApi.contains("timestamp?: string"), "调试 API 请求必须包含 API Key 时间戳");
    assertTrue(debugApi.contains("nonce?: string"), "调试 API 请求必须包含 API Key nonce");
    assertFalse(debugApi.contains("{Entity}"));

    assertVueSfc(outputRoot.resolve("zhyc-base-vue/src/views/developer/purchase-purchaseOrder/index.vue"));
  }

  private void assertVueSfc(Path file) throws IOException {
    String content = Files.readString(file);
    assertTrue(content.contains("<template>"), file + " 必须包含 template");
    assertTrue(content.contains("<script setup lang=\"ts\">"), file + " 必须包含 TypeScript setup 脚本");
    assertTrue(content.contains("import { requireAdminTenantId } from '@/utils/adminContext';"),
        file + " 必须通过统一后台上下文工具获取租户");
    assertTrue(content.contains("invokePurchaseOrderOpenApiDebug"), file + " 必须调用开放 API 调试代理");
    assertTrue(content.contains("<a-form"), file + " 必须包含调试表单");
    assertTrue(content.contains(":model=\"debugCommand\""), file + " 调试表单必须绑定调试命令");
    assertTrue(content.contains("v-model:value=\"currentTenantId\""), file + " 必须展示当前租户编码");
    assertTrue(content.contains("disabled"), file + " 当前租户编码必须只读展示");
    assertFalse(content.contains("v-model:value=\"debugCommand.tenantId\""), file + " 不允许手工切换调试租户");
    assertTrue(content.contains("v-model:value=\"debugCommand.authMode\""), file + " 必须允许切换认证方式");
    assertTrue(content.contains("v-model:value=\"debugCommand.accessKey\""), file + " 必须允许配置 API Key Access Key");
    assertTrue(content.contains("v-model:value=\"debugCommand.timestamp\""), file + " 必须允许配置 API Key 时间戳");
    assertTrue(content.contains("v-model:value=\"debugCommand.nonce\""), file + " 必须允许配置 API Key nonce");
    assertTrue(content.contains("v-model:value=\"debugCommand.bearerToken\""), file + " 必须允许配置 OAuth2/OIDC Token");
    assertTrue(content.contains("@click=\"buildDebugSnapshot\""), file + " 必须提供调试快照生成操作");
    assertTrue(content.contains("interface OpenApiDebugCommand"), file + " 必须声明调试命令类型");
    assertTrue(content.contains("const debugCommand = reactive<OpenApiDebugCommand>"), file + " 必须维护类型化调试命令");
    assertTrue(content.contains("timestamp: String(Date.now())"), file + " 调试命令必须默认生成时间戳");
    assertTrue(content.contains("nonce: createRequestNonce()"), file + " 调试命令必须默认生成 nonce");
    assertTrue(content.contains("tenantId: requireAdminTenantId()"), file + " 默认租户必须来自运行时上下文");
    assertTrue(content.contains("function buildDebugSnapshot(): void"), file + " 必须提供调试快照构造方法");
    assertTrue(content.contains("async function invokeDebugRequest(): Promise<void>"), file + " 必须提供真实调试调用方法");
    assertTrue(content.contains("const response: PurchaseOrderOpenApiDebugResponse = await invokePurchaseOrderOpenApiDebug"),
        file + " 必须调用调试代理并保留类型化响应");
    assertTrue(content.contains("debugResponse.value = JSON.stringify(response"), file + " 必须保存调试代理响应");
    assertTrue(content.contains("'X-ZHYC-Tenant-Id': currentTenantId.value"), file + " 调试快照必须包含当前租户请求头");
    assertTrue(content.contains("'Authorization': `Bearer ${debugCommand.bearerToken || '<access-token>'}`"),
        file + " 调试快照必须包含 OAuth2/OIDC Authorization 头");
    assertTrue(content.contains("signaturePayload"), file + " 调试快照必须展示签名原文");
    assertTrue(content.contains("timestamp: debugCommand.authMode === 'API_KEY' ? debugCommand.timestamp : undefined"),
        file + " 调试代理请求必须携带 API Key 时间戳");
    assertTrue(content.contains("nonce: debugCommand.authMode === 'API_KEY' ? debugCommand.nonce : undefined"),
        file + " 调试代理请求必须携带 API Key nonce");
    assertTrue(content.contains("message.success('调试快照已生成')"), file + " 必须提供调试快照生成反馈");
    assertTrue(content.contains("debugSnapshot"), file + " 必须保留调试快照入口");
    assertTrue(content.contains("tenantHeader: 'X-ZHYC-Tenant-Id'"), file + " 调试快照必须展示租户请求头");
    assertFalse(content.contains("{module}"), file + " 不允许残留模块占位符");
    assertFalse(content.contains("{entity}"), file + " 不允许残留实体占位符");
    assertFalse(content.contains("{Entity}"), file + " 不允许残留实体类占位符");
    assertFalse(content.contains("tenant_a"), file + " 不允许把建模租户固化到生成页面");
  }

  private List<GeneratedFile> generate() {
    CodeTemplateRegistry registry = new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider()));
    CodeGenerator generator = new DefaultCodeGenerator(registry, new SimpleStringTemplateRenderer());
    return generator.generate(new CodeGenerationRequest(
        GenerationTarget.OPEN_API_PORTAL, "purchase", "purchaseOrder", purchaseOrderTable()));
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
