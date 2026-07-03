/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.audit;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.audit.domain.OpenApiCallAudit;
import com.zhyc.openapi.audit.repository.OpenApiCallAuditRepository;
import com.zhyc.openapi.audit.service.DefaultOpenApiCallAuditService;
import com.zhyc.openapi.audit.service.OpenApiCallAuditRecordCommand;
import com.zhyc.openapi.audit.service.OpenApiCallAuditResponse;
import com.zhyc.openapi.audit.service.OpenApiCallAuditService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 开放 API 调用审计业务服务测试。
 */
class OpenApiCallAuditServiceTest {

    /**
     * 验证开放 API 调用审计服务按租户和应用查询审计列表。
     */
    @Test
    void shouldListAuditsByTenantAndApp() {
        RecordingOpenApiCallAuditRepository repository = new RecordingOpenApiCallAuditRepository();
        OpenApiCallAuditService service = new DefaultOpenApiCallAuditService(repository);

        List<OpenApiCallAuditResponse> audits = service.listAudits(" tenant_a ", " purchase-app ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals("purchase-app", repository.lastAppCode);
        assertEquals(1, audits.size());
        assertEquals("purchase.request.create", audits.get(0).getApiCode());
        assertEquals(201, audits.get(0).getResponseStatus());
    }

    /**
     * 验证开放 API 错误日志只查询失败调用记录。
     */
    @Test
    void shouldListErrorLogsByTenantAndApp() {
        RecordingOpenApiCallAuditRepository repository = new RecordingOpenApiCallAuditRepository();
        OpenApiCallAuditService service = new DefaultOpenApiCallAuditService(repository);

        List<OpenApiCallAuditResponse> errors = service.listErrorLogs(" tenant_a ", " purchase-app ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals("purchase-app", repository.lastAppCode);
        assertEquals(1, errors.size());
        assertEquals(0, errors.get(0).getSuccess());
        assertEquals("SIGNATURE_INVALID", errors.get(0).getErrorCode());
    }

    /**
     * 验证查询开放 API 调用审计时会拒绝包含空白字符的租户编码。
     */
    @Test
    void shouldRejectListAuditsWhenTenantIdContainsWhitespace() {
        RecordingOpenApiCallAuditRepository repository = new RecordingOpenApiCallAuditRepository();
        OpenApiCallAuditService service = new DefaultOpenApiCallAuditService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.listAudits("tenant a", "purchase-app"));

        assertEquals("ZHYC_OPENAPI_AUDIT_TENANT_ID_INVALID", exception.getCode());
        assertEquals("租户业务编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastTenantId);
    }

    /**
     * 验证记录开放 API 调用审计时会裁剪文本字段并规范化 HTTP 方法。
     */
    @Test
    void shouldRecordAuditWithNormalizedFields() {
        RecordingOpenApiCallAuditRepository repository = new RecordingOpenApiCallAuditRepository();
        OpenApiCallAuditService service = new DefaultOpenApiCallAuditService(repository);
        LocalDateTime calledAt = LocalDateTime.of(2026, 6, 24, 12, 45, 0);

        service.record(new OpenApiCallAuditRecordCommand(" tenant_a ", " purchase-app ",
                " AK123 ", " purchase.request.create ", " post ",
                " /openapi/v1/purchase/requests ", 201, 38L, true,
                null, " 10.0.0.8 ", " req-001 ", calledAt));

        assertEquals("tenant_a", repository.lastSaved.getTenantId());
        assertEquals("purchase-app", repository.lastSaved.getAppCode());
        assertEquals("AK123", repository.lastSaved.getAccessKey());
        assertEquals("purchase.request.create", repository.lastSaved.getApiCode());
        assertEquals("POST", repository.lastSaved.getHttpMethod());
        assertEquals("/openapi/v1/purchase/requests", repository.lastSaved.getRequestPath());
        assertEquals(201, repository.lastSaved.getResponseStatus());
        assertEquals(38L, repository.lastSaved.getDurationMs());
        assertEquals(1, repository.lastSaved.getSuccess());
        assertEquals("10.0.0.8", repository.lastSaved.getClientIp());
        assertEquals("req-001", repository.lastSaved.getRequestId());
        assertEquals(calledAt, repository.lastSaved.getCalledAt());
    }

    /**
     * 验证记录开放 API 调用审计时会拒绝不受支持的 HTTP 方法。
     */
    @Test
    void shouldRejectAuditWhenHttpMethodUnsupported() {
        RecordingOpenApiCallAuditRepository repository = new RecordingOpenApiCallAuditRepository();
        OpenApiCallAuditService service = new DefaultOpenApiCallAuditService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.record(new OpenApiCallAuditRecordCommand("tenant_a", "purchase-app",
                        "AK123", "purchase.request.create", "TRACE",
                        "/openapi/v1/purchase/requests", 200, 38L, true,
                        null, "10.0.0.8", "req-001", LocalDateTime.now())));

        assertEquals("ZHYC_OPENAPI_AUDIT_HTTP_METHOD_UNSUPPORTED", exception.getCode());
        assertEquals("HTTP 方法只支持 GET、POST、PUT、DELETE 或 PATCH", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证记录开放 API 调用审计时会拒绝超出 HTTP 状态码范围的响应状态。
     */
    @Test
    void shouldRejectAuditWhenResponseStatusOutsideHttpRange() {
        RecordingOpenApiCallAuditRepository repository = new RecordingOpenApiCallAuditRepository();
        OpenApiCallAuditService service = new DefaultOpenApiCallAuditService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.record(new OpenApiCallAuditRecordCommand("tenant_a", "purchase-app",
                        "AK123", "purchase.request.create", "POST",
                        "/openapi/v1/purchase/requests", 99, 38L, true,
                        null, "10.0.0.8", "req-001", LocalDateTime.now())));

        assertEquals("ZHYC_OPENAPI_AUDIT_RESPONSE_STATUS_INVALID", exception.getCode());
        assertEquals("HTTP 响应状态码必须在 100 到 599 之间", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证记录开放 API 调用审计时会拒绝负数调用耗时。
     */
    @Test
    void shouldRejectAuditWhenDurationNegative() {
        RecordingOpenApiCallAuditRepository repository = new RecordingOpenApiCallAuditRepository();
        OpenApiCallAuditService service = new DefaultOpenApiCallAuditService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.record(new OpenApiCallAuditRecordCommand("tenant_a", "purchase-app",
                        "AK123", "purchase.request.create", "POST",
                        "/openapi/v1/purchase/requests", 200, -1L, true,
                        null, "10.0.0.8", "req-001", LocalDateTime.now())));

        assertEquals("ZHYC_OPENAPI_AUDIT_DURATION_MS_INVALID", exception.getCode());
        assertEquals("调用耗时不能小于 0 毫秒", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证记录开放 API 调用审计时会拒绝包含空白字符的应用编码。
     */
    @Test
    void shouldRejectAuditWhenAppCodeContainsWhitespace() {
        RecordingOpenApiCallAuditRepository repository = new RecordingOpenApiCallAuditRepository();
        OpenApiCallAuditService service = new DefaultOpenApiCallAuditService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.record(new OpenApiCallAuditRecordCommand("tenant_a", "purchase app",
                        "AK123", "purchase.request.create", "POST",
                        "/openapi/v1/purchase/requests", 200, 38L, true,
                        null, "10.0.0.8", "req-001", LocalDateTime.now())));

        assertEquals("ZHYC_OPENAPI_AUDIT_APP_CODE_INVALID", exception.getCode());
        assertEquals("开发者应用编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证记录开放 API 调用审计时会拒绝包含空白字符的访问密钥。
     */
    @Test
    void shouldRejectAuditWhenAccessKeyContainsWhitespace() {
        RecordingOpenApiCallAuditRepository repository = new RecordingOpenApiCallAuditRepository();
        OpenApiCallAuditService service = new DefaultOpenApiCallAuditService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.record(new OpenApiCallAuditRecordCommand("tenant_a", "purchase-app",
                        "AK 123", "purchase.request.create", "POST",
                        "/openapi/v1/purchase/requests", 200, 38L, true,
                        null, "10.0.0.8", "req-001", LocalDateTime.now())));

        assertEquals("ZHYC_OPENAPI_AUDIT_ACCESS_KEY_INVALID", exception.getCode());
        assertEquals("API 访问密钥不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证记录开放 API 调用审计时会拒绝包含空白字符的 API 业务编码。
     */
    @Test
    void shouldRejectAuditWhenApiCodeContainsWhitespace() {
        RecordingOpenApiCallAuditRepository repository = new RecordingOpenApiCallAuditRepository();
        OpenApiCallAuditService service = new DefaultOpenApiCallAuditService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.record(new OpenApiCallAuditRecordCommand("tenant_a", "purchase-app",
                        "AK123", "purchase request.create", "POST",
                        "/openapi/v1/purchase/requests", 200, 38L, true,
                        null, "10.0.0.8", "req-001", LocalDateTime.now())));

        assertEquals("ZHYC_OPENAPI_AUDIT_API_CODE_INVALID", exception.getCode());
        assertEquals("API 业务编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证记录开放 API 调用审计时会拒绝包含空白字符的请求路径，避免审计路径无法与开放 API 目录规则匹配。
     */
    @Test
    void shouldRejectAuditWhenRequestPathContainsWhitespace() {
        RecordingOpenApiCallAuditRepository repository = new RecordingOpenApiCallAuditRepository();
        OpenApiCallAuditService service = new DefaultOpenApiCallAuditService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.record(new OpenApiCallAuditRecordCommand("tenant_a", "purchase-app",
                        "AK123", "purchase.request.create", "POST",
                        "/openapi/v1/purchase requests", 200, 38L, true,
                        null, "10.0.0.8", "req-001", LocalDateTime.now())));

        assertEquals("ZHYC_OPENAPI_AUDIT_REQUEST_PATH_INVALID", exception.getCode());
        assertEquals("请求路径不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证记录开放 API 调用审计时会拒绝包含空白字符的请求追踪 ID，避免链路日志和审计记录无法精确关联。
     */
    @Test
    void shouldRejectAuditWhenRequestIdContainsWhitespace() {
        RecordingOpenApiCallAuditRepository repository = new RecordingOpenApiCallAuditRepository();
        OpenApiCallAuditService service = new DefaultOpenApiCallAuditService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.record(new OpenApiCallAuditRecordCommand("tenant_a", "purchase-app",
                        "AK123", "purchase.request.create", "POST",
                        "/openapi/v1/purchase/requests", 200, 38L, true,
                        null, "10.0.0.8", "req 001", LocalDateTime.now())));

        assertEquals("ZHYC_OPENAPI_AUDIT_REQUEST_ID_INVALID", exception.getCode());
        assertEquals("请求追踪 ID 不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证记录开放 API 调用审计时会拒绝包含空白字符的客户端 IP，避免访问来源归因出现歧义。
     */
    @Test
    void shouldRejectAuditWhenClientIpContainsWhitespace() {
        RecordingOpenApiCallAuditRepository repository = new RecordingOpenApiCallAuditRepository();
        OpenApiCallAuditService service = new DefaultOpenApiCallAuditService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.record(new OpenApiCallAuditRecordCommand("tenant_a", "purchase-app",
                        "AK123", "purchase.request.create", "POST",
                        "/openapi/v1/purchase/requests", 200, 38L, true,
                        null, "10.0.0. 8", "req-001", LocalDateTime.now())));

        assertEquals("ZHYC_OPENAPI_AUDIT_CLIENT_IP_INVALID", exception.getCode());
        assertEquals("客户端 IP 不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证记录开放 API 调用审计时会拒绝包含空白字符的错误编码，避免失败原因统计聚合出现歧义。
     */
    @Test
    void shouldRejectAuditWhenErrorCodeContainsWhitespace() {
        RecordingOpenApiCallAuditRepository repository = new RecordingOpenApiCallAuditRepository();
        OpenApiCallAuditService service = new DefaultOpenApiCallAuditService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.record(new OpenApiCallAuditRecordCommand("tenant_a", "purchase-app",
                        "AK123", "purchase.request.create", "POST",
                        "/openapi/v1/purchase/requests", 500, 38L, false,
                        "SIGNATURE INVALID", "10.0.0.8", "req-001", LocalDateTime.now())));

        assertEquals("ZHYC_OPENAPI_AUDIT_ERROR_CODE_INVALID", exception.getCode());
        assertEquals("错误编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证失败的开放 API 调用审计必须记录错误编码，便于告警聚合和问题定位。
     */
    @Test
    void shouldRejectFailedAuditWhenErrorCodeMissing() {
        RecordingOpenApiCallAuditRepository repository = new RecordingOpenApiCallAuditRepository();
        OpenApiCallAuditService service = new DefaultOpenApiCallAuditService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.record(new OpenApiCallAuditRecordCommand("tenant_a", "purchase-app",
                        "AK123", "purchase.request.create", "POST",
                        "/openapi/v1/purchase/requests", 500, 38L, false,
                        null, "10.0.0.8", "req-001", LocalDateTime.now())));

        assertEquals("ZHYC_OPENAPI_AUDIT_ERROR_CODE_REQUIRED", exception.getCode());
        assertEquals("失败调用错误编码不能为空", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证记录开放 API 调用审计时会拒绝包含空白字符的租户编码。
     */
    @Test
    void shouldRejectAuditWhenTenantIdContainsWhitespace() {
        RecordingOpenApiCallAuditRepository repository = new RecordingOpenApiCallAuditRepository();
        OpenApiCallAuditService service = new DefaultOpenApiCallAuditService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.record(new OpenApiCallAuditRecordCommand("tenant a", "purchase-app",
                        "AK123", "purchase.request.create", "POST",
                        "/openapi/v1/purchase/requests", 200, 38L, true,
                        null, "10.0.0.8", "req-001", LocalDateTime.now())));

        assertEquals("ZHYC_OPENAPI_AUDIT_TENANT_ID_INVALID", exception.getCode());
        assertEquals("租户业务编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 测试用开放 API 调用审计仓储。
     */
    private static class RecordingOpenApiCallAuditRepository implements OpenApiCallAuditRepository {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次查询的开发者应用编码。 */
        private String lastAppCode;
        /** 最近一次保存的调用审计。 */
        private OpenApiCallAudit lastSaved;

        @Override
        public List<OpenApiCallAudit> findByTenantIdAndAppCode(String tenantId, String appCode) {
            lastTenantId = tenantId;
            lastAppCode = appCode;
            return List.of(new OpenApiCallAudit(1L, tenantId, appCode, "AK123",
                    "purchase.request.create", "POST", "/openapi/v1/purchase/requests",
                    201, 38L, 1, null, "10.0.0.8", "req-001",
                    LocalDateTime.now(), LocalDateTime.now()));
        }

        /**
         * 查询测试用失败调用审计列表。
         *
         * @param tenantId 租户业务编码
         * @param appCode 开发者应用编码
         * @return 失败调用审计列表
         */
        @Override
        public List<OpenApiCallAudit> findErrorLogsByTenantIdAndAppCode(String tenantId, String appCode) {
            lastTenantId = tenantId;
            lastAppCode = appCode;
            return List.of(new OpenApiCallAudit(2L, tenantId, appCode, "AK123",
                    "purchase.request.create", "POST", "/openapi/v1/purchase/requests",
                    401, 12L, 0, "SIGNATURE_INVALID", "10.0.0.8", "req-002",
                    LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void save(OpenApiCallAudit audit) {
            lastSaved = audit;
        }
    }
}
