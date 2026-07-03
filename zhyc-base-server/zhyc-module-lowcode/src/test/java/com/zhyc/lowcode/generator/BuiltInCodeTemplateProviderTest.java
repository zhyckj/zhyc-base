/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import com.zhyc.common.module.ModuleDescriptor;
import com.zhyc.common.module.ModuleDescriptorParser;
import com.zhyc.lowcode.db.LowcodeFieldType;
import com.zhyc.lowcode.metadata.domain.LowcodeColumnModel;
import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 内置代码模板提供者测试。
 */
class BuiltInCodeTemplateProviderTest {

  /**
   * 验证首期内置模板覆盖后台后端、后台前端、UniApp、开放 API 门户和微服务模块工程目标。
   */
  @Test
  void shouldProvideTemplatesForAllPhaseOneTargets() {
    List<CodeTemplateDescriptor> templates = new BuiltInCodeTemplateProvider().listTemplates();
    Map<GenerationTarget, Long> countByTarget = templates.stream()
        .collect(Collectors.groupingBy(CodeTemplateDescriptor::getTarget, Collectors.counting()));

    assertEquals(GenerationTarget.MICROSERVICE_MODULE, GenerationTarget.valueOf("MICROSERVICE_MODULE"));
    assertTrue(countByTarget.getOrDefault(GenerationTarget.ADMIN_BACKEND, 0L) >= 2);
    assertTrue(countByTarget.getOrDefault(GenerationTarget.ADMIN_FRONTEND, 0L) >= 2);
    assertTrue(countByTarget.getOrDefault(GenerationTarget.UNIAPP, 0L) >= 1);
    assertTrue(countByTarget.getOrDefault(GenerationTarget.OPEN_API_PORTAL, 0L) >= 2);
    assertTrue(countByTarget.getOrDefault(GenerationTarget.MICROSERVICE_MODULE, 0L) >= 3);
  }

  /**
   * 验证内置模板都携带可渲染模板内容，避免生成空文件。
   */
  @Test
  void shouldProvideRenderableTemplateContent() {
    List<CodeTemplateDescriptor> templates = new BuiltInCodeTemplateProvider().listTemplates();

    assertFalse(templates.isEmpty());
    assertTrue(templates.stream().allMatch(template -> !template.getTemplateContent().isBlank()));
  }

  /**
   * 验证内置模板能通过默认生成器渲染为后台后端文件。
   */
  @Test
  void shouldGenerateBackendFilesWithBuiltInTemplates() {
    CodeTemplateRegistry registry = new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider()));
    CodeGenerator generator = new DefaultCodeGenerator(registry, new SimpleStringTemplateRenderer());
    CodeGenerationRequest request = new CodeGenerationRequest(
        GenerationTarget.ADMIN_BACKEND, "purchase", "purchaseOrder", purchaseOrderTable());

    List<GeneratedFile> files = generator.generate(request);

    assertEquals(12, files.size());
    assertEquals("zhyc-module-purchase/src/main/java/com/zhyc/purchase/controller/PurchaseOrderController.java",
        files.get(0).getPath());
    assertTrue(files.get(0).getContent().contains("class PurchaseOrderController"));
    assertTrue(files.stream().anyMatch(file -> file.getPath().endsWith("service/PurchaseOrderService.java")));
    assertTrue(files.stream().anyMatch(file -> file.getPath().endsWith("service/impl/DefaultPurchaseOrderService.java")));
    assertTrue(files.stream().anyMatch(file -> file.getPath().endsWith("repository/PurchaseOrderRepository.java")));
    assertTrue(files.stream().anyMatch(file -> file.getPath().endsWith("repository/MyBatisPurchaseOrderRepository.java")));
    assertTrue(files.stream().anyMatch(file -> file.getPath().endsWith("dto/PurchaseOrderQueryRequest.java")));
    assertTrue(files.stream().anyMatch(file -> file.getPath().endsWith("dto/PurchaseOrderSaveRequest.java")));
    assertTrue(files.stream().anyMatch(file -> file.getPath().endsWith("dto/PurchaseOrderResponse.java")));
    assertTrue(files.stream().anyMatch(file -> file.getPath().endsWith("mapper/PurchaseOrderMapper.java")));
    assertTrue(files.stream().anyMatch(file -> file.getPath().endsWith("mapper/PurchaseOrderSqlProvider.java")));
    assertTrue(files.stream().anyMatch(file -> file.getPath().endsWith("V1__purchase_purchaseOrder.sql")));
    assertTrue(files.stream().anyMatch(file -> file.getPath().endsWith("DefaultPurchaseOrderServiceTest.java")));

    String serviceImpl = files.stream()
        .filter(file -> file.getPath().endsWith("service/impl/DefaultPurchaseOrderService.java"))
        .findFirst()
        .orElseThrow()
        .getContent();
    assertTrue(serviceImpl.contains("import com.zhyc.common.exception.BusinessException;"));
    assertTrue(serviceImpl.contains("ERROR_TENANT_REQUIRED"));
    assertTrue(serviceImpl.contains("ZHYC_GENERATED_TENANT_REQUIRED"));
    assertTrue(serviceImpl.contains("ERROR_ID_REQUIRED"));
    assertTrue(serviceImpl.contains("ZHYC_GENERATED_ID_REQUIRED"));
    assertTrue(serviceImpl.contains("ERROR_RECORD_NOT_FOUND"));
    assertTrue(serviceImpl.contains("ZHYC_GENERATED_RECORD_NOT_FOUND"));
    assertTrue(serviceImpl.contains("ERROR_FORM_VALUES_REQUIRED"));
    assertTrue(serviceImpl.contains("ZHYC_GENERATED_FORM_VALUES_REQUIRED"));
    assertTrue(serviceImpl.contains("ERROR_TENANT_MISMATCH"));
    assertTrue(serviceImpl.contains("ZHYC_GENERATED_TENANT_MISMATCH"));
    assertFalse(serviceImpl.contains("throw new IllegalArgumentException"));

    String serviceTest = files.stream()
        .filter(file -> file.getPath().endsWith("DefaultPurchaseOrderServiceTest.java"))
        .findFirst()
        .orElseThrow()
        .getContent();
    assertTrue(serviceTest.contains("import static org.junit.jupiter.api.Assertions.assertThrows;"));
    assertTrue(serviceTest.contains("import com.zhyc.common.exception.BusinessException;"));
    assertTrue(serviceTest.contains("BusinessException exception = assertThrows(BusinessException.class"));
    assertTrue(serviceTest.contains("ZHYC_GENERATED_TENANT_MISMATCH"));

    String repositoryImpl = files.stream()
        .filter(file -> file.getPath().endsWith("repository/MyBatisPurchaseOrderRepository.java"))
        .findFirst()
        .orElseThrow()
        .getContent();
    assertTrue(repositoryImpl.contains("import com.zhyc.common.exception.BusinessException;"));
    assertTrue(repositoryImpl.contains("ERROR_SAVE_FAILED"));
    assertTrue(repositoryImpl.contains("ZHYC_GENERATED_SAVE_FAILED"));
    assertTrue(repositoryImpl.contains("ERROR_UPDATE_FAILED"));
    assertTrue(repositoryImpl.contains("ZHYC_GENERATED_UPDATE_FAILED"));
    assertTrue(repositoryImpl.contains("ERROR_DELETE_FAILED"));
    assertTrue(repositoryImpl.contains("ZHYC_GENERATED_DELETE_FAILED"));
    assertFalse(repositoryImpl.contains("throw new IllegalStateException"));

    String sqlProvider = files.stream()
        .filter(file -> file.getPath().endsWith("mapper/PurchaseOrderSqlProvider.java"))
        .findFirst()
        .orElseThrow()
        .getContent();
    assertTrue(sqlProvider.contains("import com.zhyc.common.exception.BusinessException;"));
    assertTrue(sqlProvider.contains("ERROR_SQL_VALUES_REQUIRED"));
    assertTrue(sqlProvider.contains("ZHYC_GENERATED_SQL_VALUES_REQUIRED"));
    assertTrue(sqlProvider.contains("ERROR_SQL_INSERT_COLUMNS_EMPTY"));
    assertTrue(sqlProvider.contains("ZHYC_GENERATED_SQL_INSERT_COLUMNS_EMPTY"));
    assertTrue(sqlProvider.contains("ERROR_SQL_UPDATE_COLUMNS_EMPTY"));
    assertTrue(sqlProvider.contains("ZHYC_GENERATED_SQL_UPDATE_COLUMNS_EMPTY"));
    assertTrue(sqlProvider.contains("转义 SQL 字段名，避免字段名和数据库关键字冲突"));
    assertFalse(sqlProvider.contains("throw new IllegalArgumentException"));
  }

  /**
   * 验证首期内置模板生成结果包含权限、租户、端侧 API 和开放 API 文档关键契约。
   */
  @Test
  void shouldGeneratePhaseOneTemplatesWithRequiredContracts() {
    CodeTemplateRegistry registry = new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider()));
    CodeGenerator generator = new DefaultCodeGenerator(registry, new SimpleStringTemplateRenderer());

    String backendContent = renderTarget(generator, GenerationTarget.ADMIN_BACKEND);
    assertTrue(backendContent.contains("@RequiresPermissions(\"purchase:purchaseOrder:query\")"));
    assertTrue(backendContent.contains("@Service"));
    assertTrue(backendContent.contains("ApiResult"));
    assertTrue(backendContent.contains("tenantId"));
    assertTrue(backendContent.contains("package com.zhyc.purchase.controller;"));
    assertTrue(backendContent.contains("package com.zhyc.purchase.service;"));
    assertTrue(backendContent.contains("package com.zhyc.purchase.service.impl;"));
    assertTrue(backendContent.contains("package com.zhyc.purchase.repository;"));
    assertTrue(backendContent.contains("package com.zhyc.purchase.mapper;"));
    assertTrue(backendContent.contains("package com.zhyc.purchase.dto;"));
    assertTrue(backendContent.contains("public interface PurchaseOrderService"));
    assertTrue(backendContent.contains("class DefaultPurchaseOrderService implements PurchaseOrderService"));
    assertTrue(backendContent.contains("ApiResult<PageResult<PurchaseOrderResponse>>"));
    assertTrue(backendContent.contains("PageResult<PurchaseOrderResponse> list(String tenantId, int pageNo, int pageSize)"));
    assertTrue(backendContent.contains("PageResult.of(totalCount, normalizedPageNo, normalizedPageSize, records)"));
    assertTrue(backendContent.contains("purchaseOrderRepository.findByTenantId(tenantId, offset, normalizedPageSize)"));
    assertTrue(backendContent.contains("List<Map<String, Object>> findByTenantId(String tenantId, long offset, int pageSize)"));
    assertTrue(backendContent.contains("@SelectProvider(type = PurchaseOrderSqlProvider.class, method = \"selectByTenantId\")"));
    assertTrue(backendContent.contains("List<Map<String, Object>> selectByTenantId(@Param(\"tenantId\") String tenantId, @Param(\"offset\") long offset, @Param(\"pageSize\") int pageSize)"));
    assertTrue(backendContent.contains("public String selectByTenantId()"));
    assertTrue(backendContent.contains("SELECT id, tenant_id, order_no "));
    assertTrue(backendContent.contains("ORDER BY id DESC LIMIT #{offset}, #{pageSize}"));
    assertTrue(backendContent.contains("@GetMapping(\"/{id}\")"));
    assertTrue(backendContent.contains("ApiResult<PurchaseOrderResponse> detail("));
    assertTrue(backendContent.contains("PurchaseOrderResponse detail(String tenantId, Long id)"));
    assertTrue(backendContent.contains("purchaseOrderRepository.findById(tenantId, id)"));
    assertTrue(backendContent.contains("Map<String, Object> findById(String tenantId, Long id)"));
    assertTrue(backendContent.contains("@SelectProvider(type = PurchaseOrderSqlProvider.class, method = \"selectById\")"));
    assertTrue(backendContent.contains("Map<String, Object> selectById(@Param(\"tenantId\") String tenantId, @Param(\"id\") Long id)"));
    assertTrue(backendContent.contains("public String selectById()"));
    assertTrue(backendContent.contains("AND id = #{id} "));
    assertTrue(backendContent.contains("ApiResult<PurchaseOrderResponse> save("));
    assertTrue(backendContent.contains("@PostMapping"));
    assertTrue(backendContent.contains("import jakarta.validation.Valid;"));
    assertTrue(backendContent.contains("@Valid @RequestBody PurchaseOrderSaveRequest request"));
    assertTrue(backendContent.contains("@RequiresPermissions(\"purchase:purchaseOrder:save\")"));
    assertTrue(backendContent.contains("PurchaseOrderResponse save(String tenantId, PurchaseOrderSaveRequest request)"));
    assertTrue(backendContent.contains("@PutMapping(\"/{id}\")"));
    assertTrue(backendContent.contains("ApiResult<PurchaseOrderResponse> update("));
    assertTrue(backendContent.contains("PurchaseOrderResponse update(String tenantId, Long id, PurchaseOrderSaveRequest request)"));
    assertTrue(backendContent.contains("purchaseOrderRepository.updateById(tenantId, id, request.values())"));
    assertTrue(backendContent.contains("void updateById(String tenantId, Long id, Map<String, Object> values)"));
    assertTrue(backendContent.contains("@UpdateProvider(type = PurchaseOrderSqlProvider.class, method = \"updateById\")"));
    assertTrue(backendContent.contains("int updateById(@Param(\"tenantId\") String tenantId, @Param(\"id\") Long id, @Param(\"values\") Map<String, Object> values)"));
    assertTrue(backendContent.contains("public String updateById(Map<String, Object> params)"));
    assertTrue(backendContent.contains("UPDATE pur_order SET "));
    assertTrue(backendContent.contains("updated_at = CURRENT_TIMESTAMP"));
    assertTrue(backendContent.contains("@DeleteMapping(\"/{id}\")"));
    assertTrue(backendContent.contains("@RequiresPermissions(\"purchase:purchaseOrder:delete\")"));
    assertTrue(backendContent.contains("void delete(String tenantId, Long id)"));
    assertTrue(backendContent.contains("purchaseOrderRepository.deleteById(tenantId, id)"));
    assertTrue(backendContent.contains("void deleteById(String tenantId, Long id)"));
    assertTrue(backendContent.contains("@UpdateProvider(type = PurchaseOrderSqlProvider.class, method = \"deleteById\")"));
    assertTrue(backendContent.contains("int deleteById(@Param(\"tenantId\") String tenantId, @Param(\"id\") Long id)"));
    assertTrue(backendContent.contains("public String deleteById()"));
    assertTrue(backendContent.contains("UPDATE pur_order SET deleted = 1 "));
    assertTrue(backendContent.contains("AND id = #{id} "));
    assertTrue(backendContent.contains("public record PurchaseOrderSaveRequest("));
    assertTrue(backendContent.contains("import jakarta.validation.constraints.NotBlank;"));
    assertTrue(backendContent.contains("import jakarta.validation.constraints.NotEmpty;"));
    assertTrue(backendContent.contains("@NotBlank(message = \"租户业务编码不能为空\") String tenantId"));
    assertTrue(backendContent.contains("@NotEmpty(message = \"表单字段值不能为空\") Map<String, Object> values"));
    assertTrue(backendContent.contains("purchaseOrderRepository.insert(tenantId, request.values())"));
    assertTrue(backendContent.contains("void insert(String tenantId, Map<String, Object> values)"));
    assertTrue(backendContent.contains("@InsertProvider(type = PurchaseOrderSqlProvider.class, method = \"insert\")"));
    assertTrue(backendContent.contains("int insert(@Param(\"tenantId\") String tenantId, @Param(\"values\") Map<String, Object> values)"));
    assertTrue(backendContent.contains("List.of(\"order_no\")"));
    assertTrue(backendContent.contains("INSERT INTO pur_order (tenant_id, "));
    assertTrue(backendContent.contains("#{values."));
    assertTrue(backendContent.contains("if (request.tenantId() == null || !tenantId.equals(request.tenantId()))"));
    assertTrue(backendContent.contains("请求租户与当前租户不一致"));
    assertTrue(backendContent.contains("public record PurchaseOrderResponse"));
    assertTrue(backendContent.contains("new PurchaseOrderResponse(String.valueOf(row.get(\"id\")), tenantId, String.valueOf(row.get(\"order_no\")), totalCount)"));
    assertTrue(backendContent.contains("PurchaseOrderRepository purchaseOrderRepository"));
    assertTrue(backendContent.contains("class MyBatisPurchaseOrderRepository implements PurchaseOrderRepository"));
    assertTrue(backendContent.contains("@SelectProvider(type = PurchaseOrderSqlProvider.class, method = \"countByTenantId\")"));
    assertTrue(backendContent.contains("class PurchaseOrderSqlProvider"));
    assertTrue(backendContent.contains("tenant_id = #{tenantId}"));
    assertTrue(backendContent.contains("deleted = 0"));
    assertFalse(backendContent.contains("@Select(\"SELECT"));

    String adminFrontendContent = renderTarget(generator, GenerationTarget.ADMIN_FRONTEND);
    assertTrue(adminFrontendContent.contains("listPurchaseOrder"));
    assertTrue(adminFrontendContent.contains("export interface PurchaseOrderRecord"));
    assertTrue(adminFrontendContent.contains("id: string;"));
    assertTrue(adminFrontendContent.contains("ref<PurchaseOrderRecord[]>([])"));
    assertTrue(adminFrontendContent.contains("import { request, type PageResult } from '@/api/http';"));
    assertTrue(adminFrontendContent.contains("request<PageResult<PurchaseOrderRecord>>"));
    assertTrue(adminFrontendContent.contains("listPurchaseOrder(pageNo = 1, pageSize = 20): Promise<PageResult<PurchaseOrderRecord>>"));
    assertTrue(adminFrontendContent.contains("records.value = page.records;"));
    assertTrue(adminFrontendContent.contains("getPurchaseOrder(id: string): Promise<PurchaseOrderRecord>"));
    assertTrue(adminFrontendContent.contains("request<PurchaseOrderRecord>(`/purchase/purchaseOrder/${id}`"));
    assertTrue(adminFrontendContent.contains("export interface PurchaseOrderSavePayload"));
    assertTrue(adminFrontendContent.contains("savePurchaseOrder(payload: PurchaseOrderSavePayload): Promise<PurchaseOrderRecord>"));
    assertTrue(adminFrontendContent.contains("request<PurchaseOrderRecord, PurchaseOrderSavePayload>"));
    assertTrue(adminFrontendContent.contains("updatePurchaseOrder(id: string, payload: PurchaseOrderSavePayload): Promise<PurchaseOrderRecord>"));
    assertFalse(adminFrontendContent.contains("'X-ZHYC-Tenant-Id': tenantId"));
    assertTrue(adminFrontendContent.contains("method: 'PUT'"));
    assertTrue(adminFrontendContent.contains("values: { order_no: formState.value.fields.trim() }"));
    assertTrue(adminFrontendContent.contains("deletePurchaseOrder(id: string): Promise<void>"));
    assertTrue(adminFrontendContent.contains("method: 'DELETE'"));
    assertTrue(adminFrontendContent.contains("const deletePermission = 'purchase:purchaseOrder:delete'"));
    assertTrue(adminFrontendContent.contains("async function handleDelete(record: PurchaseOrderRecord): Promise<void>"));
    assertTrue(adminFrontendContent.contains("Modal.confirm"));
    assertTrue(adminFrontendContent.contains("await deletePurchaseOrder(record.id)"));
    assertTrue(adminFrontendContent.contains(":columns=\"columns\""));
    assertTrue(adminFrontendContent.contains("const columns: TableColumnsType<PurchaseOrderRecord>"));
    assertTrue(adminFrontendContent.contains("dataIndex: 'fields'"));
    assertTrue(adminFrontendContent.contains("dataIndex: 'totalCount'"));
    assertTrue(adminFrontendContent.contains("/purchase/purchaseOrder"));
    assertTrue(adminFrontendContent.contains("export const purchaseOrderCreateRoute"));
    assertTrue(adminFrontendContent.contains("export const purchaseOrderEditRoute"));
    assertTrue(adminFrontendContent.contains("export const purchaseOrderDetailRoute"));
    assertTrue(adminFrontendContent.contains("path: '/purchase/purchaseOrder/create'"));
    assertTrue(adminFrontendContent.contains("path: '/purchase/purchaseOrder/:id/edit'"));
    assertTrue(adminFrontendContent.contains("path: '/purchase/purchaseOrder/:id'"));
    assertTrue(adminFrontendContent.contains("@/views/purchase/purchaseOrder/form.vue"));
    assertTrue(adminFrontendContent.contains("@/views/purchase/purchaseOrder/detail.vue"));
    assertTrue(adminFrontendContent.contains("purchase:purchaseOrder:query"));
    assertTrue(adminFrontendContent.contains("permission: 'purchase:purchaseOrder:save'"));
    assertTrue(adminFrontendContent.contains("requireAdminTenantId"));
    assertTrue(adminFrontendContent.contains("const tenantId = computed(() => requireAdminTenantId())"));
    assertFalse(adminFrontendContent.contains("value=\"default\""));
    assertTrue(adminFrontendContent.contains("<span class=\"permission-code\">{{ queryPermission }}</span>"));
    assertTrue(adminFrontendContent.contains(".permission-code"));
    assertFalse(adminFrontendContent.contains("console.debug(queryPermission)"));
    assertTrue(adminFrontendContent.contains("const formState = ref({ fields: \"id,order_no\" })"));
    assertTrue(adminFrontendContent.contains("const recordId = computed(() => String(route.params.id ?? ''))"));
    assertTrue(adminFrontendContent.contains("const isEditMode = computed(() => Boolean(recordId.value))"));
    assertTrue(adminFrontendContent.contains("const formRules = {"));
    assertTrue(adminFrontendContent.contains("async function handleSubmit(): Promise<void>"));
    assertTrue(adminFrontendContent.contains("isEditMode.value ? updatePurchaseOrder"));
    assertTrue(adminFrontendContent.contains("Modal.confirm"));
    assertTrue(adminFrontendContent.contains(":loading=\"submitting\""));
    assertTrue(adminFrontendContent.contains("const savePermission = 'purchase:purchaseOrder:save'"));
    assertTrue(adminFrontendContent.contains("const currentRecord = ref<PurchaseOrderRecord | null>(null)"));
    assertTrue(adminFrontendContent.contains("async function loadDetail(): Promise<void>"));
    assertTrue(adminFrontendContent.contains("currentRecord.value = await getPurchaseOrder(recordId)"));
    assertTrue(adminFrontendContent.contains("const recordId = String(route.params.id ?? '')"));
    assertTrue(adminFrontendContent.contains("{{ currentRecord.fields }}"));
    assertTrue(adminFrontendContent.contains("{{ currentRecord.totalCount }}"));

    String uniappContent = renderTarget(generator, GenerationTarget.UNIAPP);
    assertTrue(uniappContent.contains("listPurchaseOrder"));
    assertTrue(uniappContent.contains("export interface PurchaseOrderRecord"));
    assertTrue(uniappContent.contains("id: string;"));
    assertTrue(uniappContent.contains("type PurchaseOrderRecord"));
    assertTrue(uniappContent.contains("ref<PurchaseOrderRecord[]>([])"));
    assertTrue(uniappContent.contains("import { mobileRequest, type MobilePageResult } from './request';"));
    assertTrue(uniappContent.contains("listMobilePurchaseOrder(pageNo = 1, pageSize = 20): Promise<MobilePageResult<PurchaseOrderRecord>>"));
    assertTrue(uniappContent.contains("getMobileUserContext"));
    assertTrue(uniappContent.contains("requireMobileUserId(userContext)"));
    assertTrue(uniappContent.contains("requireMobileTenantId(userContext)"));
    assertTrue(uniappContent.contains("mobile-page mobile-bottom-safe"));
    assertTrue(uniappContent.contains("import MobilePageTopBar from '@/components/MobilePageTopBar.vue';"));
    assertTrue(uniappContent.contains("<MobilePageTopBar title=\"采购订单\" eyebrow=\"移动列表\" action-text=\"新建\""));
    assertTrue(uniappContent.contains("fallback-url=\"/pages/purchase/purchaseOrder/list\""));
    assertTrue(uniappContent.contains("\"navigationStyle\": \"custom\""));
    assertTrue(uniappContent.contains("import MobileState from '@/components/MobileState.vue';"));
    assertTrue(uniappContent.contains("mobile-hero compact-hero"));
    assertTrue(uniappContent.contains("mobile-rich-list-card"));
    assertTrue(uniappContent.contains("mobile-form-card"));
    assertTrue(uniappContent.contains("mobile-field-card"));
    assertTrue(uniappContent.contains("mobile-form-alert"));
    assertTrue(uniappContent.contains("generated-list-refresh-action"));
    assertTrue(uniappContent.contains("function goCreate(): void"));
    assertTrue(uniappContent.contains("`/pages/purchase/purchaseOrder/form`"));
    assertTrue(uniappContent.contains("generated-form-summary-card"));
    assertTrue(uniappContent.contains("formModeText"));
    assertTrue(uniappContent.contains("generated-form-tips"));
    assertTrue(uniappContent.contains("generated-detail-summary-card"));
    assertTrue(uniappContent.contains("generated-workflow-tip"));
    assertTrue(uniappContent.contains("canApproveReject"));
    assertTrue(uniappContent.contains("canRevoke"));
    assertTrue(uniappContent.contains("records.value = page.records;"));
    assertTrue(uniappContent.contains("v-for=\"item in records\""));
    assertTrue(uniappContent.contains(":key=\"item.id\""));
    assertTrue(uniappContent.contains("{{ item.fields }}"));
    assertTrue(uniappContent.contains("{{ item.totalCount }}"));
    assertTrue(uniappContent.contains("const currentRecord = ref<PurchaseOrderRecord | null>(null)"));
    assertTrue(uniappContent.contains("getMobilePurchaseOrder(id: string): Promise<PurchaseOrderRecord>"));
    assertTrue(uniappContent.contains("import { approveTask, rejectTask, revokeMobileTask } from '@/api/workflow';"));
    assertTrue(uniappContent.contains("const taskId = String(query.taskId ?? '')"));
    assertTrue(uniappContent.contains("const processInstanceId = String(query.processInstanceId ?? '')"));
    assertTrue(uniappContent.contains("class=\"mobile-action-button\" :disabled=\"!canApproveReject\" @tap=\"approve\""));
    assertTrue(uniappContent.contains("class=\"mobile-danger-button\" :disabled=\"!canApproveReject\" @tap=\"reject\""));
    assertTrue(uniappContent.contains("class=\"mobile-ghost-button\" :disabled=\"!canRevoke\" @tap=\"revoke\""));
    assertTrue(uniappContent.contains("await approveTask(taskId, { comment: '同意' })"));
    assertTrue(uniappContent.contains("await rejectTask(taskId, { comment: '不同意' })"));
    assertTrue(uniappContent.contains("await revokeMobileTask(processInstanceId, { reason: '移动端撤回' })"));
    assertTrue(uniappContent.contains("mobileRequest<PurchaseOrderRecord>(`/purchase/purchaseOrder/${id}`"));
    assertTrue(uniappContent.contains("currentRecord.value = await getMobilePurchaseOrder(recordId)"));
    assertTrue(uniappContent.contains("const recordId = String(query.id ?? '')"));
    assertTrue(uniappContent.contains("{{ currentRecord.fields }}"));
    assertTrue(uniappContent.contains("{{ currentRecord.totalCount }}"));
    assertTrue(uniappContent.contains("if (!formText.value.trim())"));
    assertTrue(uniappContent.contains("const submitting = ref(false)"));
    assertTrue(uniappContent.contains(":disabled=\"submitting\""));
    assertTrue(uniappContent.contains("mobileRequest<MobilePageResult<PurchaseOrderRecord>>"));
    assertTrue(uniappContent.contains("saveMobilePurchaseOrder(payload: PurchaseOrderSavePayload): Promise<PurchaseOrderRecord>"));
    assertTrue(uniappContent.contains("mobileRequest<PurchaseOrderRecord, PurchaseOrderSavePayload>"));
    assertTrue(uniappContent.contains("updateMobilePurchaseOrder(id: string, payload: PurchaseOrderSavePayload): Promise<PurchaseOrderRecord>"));
    assertTrue(uniappContent.contains("method: 'PUT'"));
    assertTrue(uniappContent.contains("data: payload"));
    assertTrue(uniappContent.contains("values: { order_no: formText.value.trim() }"));
    assertTrue(uniappContent.contains("const isEditMode = computed(() => Boolean(recordId.value))"));
    assertTrue(uniappContent.contains("isEditMode.value ? updateMobilePurchaseOrder"));
    assertTrue(uniappContent.contains("<scroll-view"));

    String openApiContent = renderTarget(generator, GenerationTarget.OPEN_API_PORTAL);
    assertTrue(openApiContent.contains("API Key"));
    assertTrue(openApiContent.contains("OAuth2/OIDC"));
    assertTrue(openApiContent.contains("GET /openapi/v1/purchase/purchaseOrder"));
    assertTrue(openApiContent.contains("调试控制台"));
    assertTrue(openApiContent.contains("X-ZHYC-Signature"));
    assertTrue(openApiContent.contains("X-ZHYC-Timestamp"));
    assertTrue(openApiContent.contains("X-ZHYC-Nonce"));
    assertTrue(openApiContent.contains("X-ZHYC-Request-Id"));
    assertTrue(openApiContent.contains("Authorization: Bearer <access_token>"));
    assertTrue(openApiContent.contains("backendRoute: http://zhyc-platform-app/openapi/v1/purchase/purchaseOrder"));
    assertTrue(openApiContent.contains("INSERT INTO openapi_catalog"));
    assertTrue(openApiContent.contains("INSERT INTO openapi_version"));
    assertTrue(openApiContent.contains("ON DUPLICATE KEY UPDATE"));
    assertTrue(openApiContent.contains("http://zhyc-platform-app/openapi/v1/purchase/purchaseOrder"));
    assertTrue(openApiContent.contains("apiCode"));
    assertTrue(openApiContent.contains("requestId"));
    assertTrue(openApiContent.contains("ZHYC_OPENAPI_UNAUTHORIZED"));
    assertTrue(openApiContent.contains("auditFields"));
    assertTrue(openApiContent.contains("<a-form"));
    assertTrue(openApiContent.contains(":model=\"debugCommand\""));
    assertTrue(openApiContent.contains("interface OpenApiDebugCommand"));
    assertTrue(openApiContent.contains("const debugCommand = reactive<OpenApiDebugCommand>"));
    assertTrue(openApiContent.contains("timestamp: String(Date.now())"));
    assertTrue(openApiContent.contains("nonce: createRequestNonce()"));
    assertTrue(openApiContent.contains("import { requireAdminTenantId } from '@/utils/adminContext';"));
    assertTrue(openApiContent.contains("tenantId: requireAdminTenantId()"));
    assertTrue(openApiContent.contains("v-model:value=\"currentTenantId\""));
    assertTrue(openApiContent.contains("disabled"));
    assertFalse(openApiContent.contains("v-model:value=\"debugCommand.tenantId\""));
    assertTrue(openApiContent.contains("function buildDebugSnapshot(): void"));
    assertTrue(openApiContent.contains("export interface PurchaseOrderOpenApiDebugRequest"));
    assertTrue(openApiContent.contains("export function invokePurchaseOrderOpenApiDebug"));
    assertTrue(openApiContent.contains("request<PurchaseOrderOpenApiDebugResponse, PurchaseOrderOpenApiDebugRequest>"));
    assertTrue(openApiContent.contains("'/openapi/debug/invoke'"));
    assertTrue(openApiContent.contains("async function invokeDebugRequest(): Promise<void>"));
    assertTrue(openApiContent.contains("const response: PurchaseOrderOpenApiDebugResponse = await invokePurchaseOrderOpenApiDebug"));
    assertTrue(openApiContent.contains("debugResponse.value = JSON.stringify(response"));
    assertTrue(openApiContent.contains("'X-ZHYC-Tenant-Id': currentTenantId.value"));
    assertTrue(openApiContent.contains("tenantId: requireAdminTenantId()"));
    assertTrue(openApiContent.contains("'Authorization': `Bearer ${debugCommand.bearerToken || '<access-token>'}`"));
    assertTrue(openApiContent.contains("signaturePayload"));
    assertTrue(openApiContent.contains("timestamp: debugCommand.authMode === 'API_KEY' ? debugCommand.timestamp : undefined"));
    assertTrue(openApiContent.contains("nonce: debugCommand.authMode === 'API_KEY' ? debugCommand.nonce : undefined"));
    assertTrue(openApiContent.contains("message.success('调试快照已生成')"));

    String microserviceContent = renderTarget(generator, GenerationTarget.MICROSERVICE_MODULE);
    assertTrue(microserviceContent.contains("<artifactId>zhyc-service-purchase-purchaseOrder</artifactId>"));
    assertTrue(microserviceContent.contains("class PurchaseOrderServiceApplication"));
    assertTrue(microserviceContent.contains("moduleType: MICROSERVICE"));
    assertTrue(microserviceContent.contains("tenantMode: tenant_id"));
    assertTrue(microserviceContent.contains("permissionPrefix: purchase:purchaseOrder"));
    assertTrue(microserviceContent.contains("openApiGatewayRequired: true"));
    assertTrue(microserviceContent.contains("authServerRequired: true"));
    assertTrue(microserviceContent.contains("字段类型映射扩展点"));
    assertTrue(microserviceContent.contains("DDL 生成器扩展点"));
    assertTrue(microserviceContent.contains("分页方言扩展点"));
    assertTrue(microserviceContent.contains("ZHYC_AUTH_SERVER_URL"));
    assertTrue(microserviceContent.contains("ZHYC_OPENAPI_GATEWAY_URL"));
    assertTrue(microserviceContent.contains("必须通过 `tenant_id` 隔离"));
    assertTrue(microserviceContent.contains("禁止直接依赖 Flowable API"));
  }

  /**
   * 验证微服务模块工程目标会生成独立工程骨架。
   */
  @Test
  void shouldGenerateMicroserviceModuleSkeleton() {
    CodeTemplateRegistry registry = new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider()));
    CodeGenerator generator = new DefaultCodeGenerator(registry, new SimpleStringTemplateRenderer());
    CodeGenerationRequest request = new CodeGenerationRequest(
        GenerationTarget.MICROSERVICE_MODULE, "purchase", "purchaseOrder", purchaseOrderTable());

    List<GeneratedFile> files = generator.generate(request);

    assertEquals(6, files.size());
    assertTrue(files.stream().anyMatch(file -> file.getPath()
        .equals("zhyc-service-purchase-purchaseOrder/pom.xml")));
    assertTrue(files.stream().anyMatch(file -> file.getPath()
        .endsWith("PurchaseOrderServiceApplication.java")));
    assertTrue(files.stream().anyMatch(file -> file.getPath()
        .endsWith("PurchaseOrderServiceInfoController.java")));
    assertTrue(files.stream().anyMatch(file -> file.getContent()
        .contains("ApiResult<Map<String, String>> info(@RequestHeader(HEADER_TENANT_ID) String tenantId)")));
    assertTrue(files.stream().anyMatch(file -> file.getContent()
        .contains("ZHYC_MICROSERVICE_TENANT_REQUIRED")));
    assertTrue(files.stream().anyMatch(file -> file.getPath()
        .endsWith("META-INF/zhyc-module.yml")));
    assertTrue(files.stream().anyMatch(file -> file.getPath()
        .endsWith("application.yml")));
    assertTrue(files.stream().anyMatch(file -> file.getPath()
        .equals("zhyc-service-purchase-purchaseOrder/README.md")));
  }

  /**
   * 验证生成的微服务模块描述文件可被公共模块解析器读取。
   */
  @Test
  void shouldGenerateParseableMicroserviceModuleDescriptor() {
    CodeTemplateRegistry registry = new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider()));
    CodeGenerator generator = new DefaultCodeGenerator(registry, new SimpleStringTemplateRenderer());
    List<GeneratedFile> files = generator.generate(new CodeGenerationRequest(
        GenerationTarget.MICROSERVICE_MODULE, "purchase", "purchaseOrder", purchaseOrderTable()));
    String descriptorContent = files.stream()
        .filter(file -> file.getPath().endsWith("META-INF/zhyc-module.yml"))
        .findFirst()
        .orElseThrow()
        .getContent();

    ModuleDescriptor descriptor = ModuleDescriptorParser.parse(descriptorContent);

    assertEquals("purchase-purchaseOrder", descriptor.getCode());
    assertEquals("MICROSERVICE", descriptor.getModuleType());
    assertEquals("tenant_id", descriptor.getTenantMode());
    assertTrue(descriptor.isOpenApiGatewayRequired());
    assertTrue(descriptor.isAuthServerRequired());
    assertEquals(List.of("field-type-mapper", "ddl-generator", "pagination-dialect"),
        descriptor.getExtensionPoints());
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

  private String renderTarget(CodeGenerator generator, GenerationTarget target) {
    CodeGenerationRequest request = new CodeGenerationRequest(target, "purchase", "purchaseOrder", purchaseOrderTable());
    return generator.generate(request).stream()
        .map(GeneratedFile::getContent)
        .collect(Collectors.joining("\n"));
  }
}
