/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.permission;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.app.domain.OpenApiApp;
import com.zhyc.openapi.app.repository.OpenApiAppRepository;
import com.zhyc.openapi.permission.domain.OpenApiPermission;
import com.zhyc.openapi.permission.domain.OpenApiPermissionStatus;
import com.zhyc.openapi.permission.repository.OpenApiPermissionRepository;
import com.zhyc.openapi.permission.service.DefaultOpenApiPermissionService;
import com.zhyc.openapi.permission.service.OpenApiPermissionResponse;
import com.zhyc.openapi.permission.service.OpenApiPermissionSaveCommand;
import com.zhyc.openapi.permission.service.OpenApiPermissionService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 开放 API 权限授权业务服务测试。
 */
class OpenApiPermissionServiceTest {

    /**
     * 验证开放 API 权限服务按租户和应用查询授权列表。
     */
    @Test
    void shouldListPermissionsByTenantAndApp() {
        RecordingOpenApiPermissionRepository repository = new RecordingOpenApiPermissionRepository();
        OpenApiPermissionService service = new DefaultOpenApiPermissionService(repository, new ExistingOpenApiAppRepository());

        List<OpenApiPermissionResponse> permissions = service.listPermissions(" tenant_a ", " purchase-app ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals("purchase-app", repository.lastAppCode);
        assertEquals(1, permissions.size());
        assertEquals("purchase.request.create", permissions.get(0).getApiCode());
    }

    /**
     * 验证查询开放 API 权限时会拒绝包含空白字符的租户编码。
     */
    @Test
    void shouldRejectListPermissionsWhenTenantIdContainsWhitespace() {
        RecordingOpenApiPermissionRepository repository = new RecordingOpenApiPermissionRepository();
        OpenApiPermissionService service = new DefaultOpenApiPermissionService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.listPermissions("tenant a", "purchase-app"));

        assertEquals("ZHYC_OPENAPI_PERMISSION_TENANT_ID_INVALID", exception.getCode());
        assertEquals("租户业务编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastTenantId);
    }

    /**
     * 验证保存开放 API 权限时会裁剪租户、应用、API 编码、方法、路径和状态。
     */
    @Test
    void shouldSavePermissionWithNormalizedFields() {
        RecordingOpenApiPermissionRepository repository = new RecordingOpenApiPermissionRepository();
        OpenApiPermissionService service = new DefaultOpenApiPermissionService(repository, new ExistingOpenApiAppRepository());

        service.save(new OpenApiPermissionSaveCommand(" tenant_a ", " purchase-app ",
                " purchase.request.create ", " 创建采购申请 ", " post ",
                " /openapi/v1/purchase/requests ", " enabled "));

        assertEquals("tenant_a", repository.lastSaved.getTenantId());
        assertEquals("purchase-app", repository.lastSaved.getAppCode());
        assertEquals("purchase.request.create", repository.lastSaved.getApiCode());
        assertEquals("创建采购申请", repository.lastSaved.getApiName());
        assertEquals("POST", repository.lastSaved.getHttpMethod());
        assertEquals("/openapi/v1/purchase/requests", repository.lastSaved.getPathPattern());
        assertEquals("enabled", repository.lastSaved.getStatus());
    }

    /**
     * 验证保存开放 API 权限前必须确认开发者应用属于当前租户，避免给其他租户或不存在的应用授权。
     */
    @Test
    void shouldRejectPermissionWhenAppNotInTenant() {
        RecordingOpenApiPermissionRepository repository = new RecordingOpenApiPermissionRepository();
        OpenApiPermissionService service = new DefaultOpenApiPermissionService(repository, new MissingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiPermissionSaveCommand("tenant_a", "purchase-app",
                        "purchase.request.create", "创建采购申请", "POST",
                        "/openapi/v1/purchase/requests", "enabled")));

        assertEquals("ZHYC_OPENAPI_PERMISSION_APP_NOT_FOUND", exception.getCode());
        assertEquals("开发者应用不存在或不属于当前租户: purchase-app", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存开放 API 权限时会拒绝不受支持的授权状态。
     */
    @Test
    void shouldRejectPermissionWhenStatusUnsupported() {
        RecordingOpenApiPermissionRepository repository = new RecordingOpenApiPermissionRepository();
        OpenApiPermissionService service = new DefaultOpenApiPermissionService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiPermissionSaveCommand("tenant_a", "purchase-app",
                        "purchase.request.create", "创建采购申请", "POST",
                        "/openapi/v1/purchase/requests", "pending")));

        assertEquals("ZHYC_OPENAPI_PERMISSION_STATUS_UNSUPPORTED", exception.getCode());
        assertEquals("授权状态只支持 enabled 或 disabled", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证开放 API 授权状态枚举会拒绝不受支持的编码。
     */
    @Test
    void shouldRejectUnsupportedPermissionStatusEnumCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> OpenApiPermissionStatus.fromCode("pending"));

        assertEquals("授权状态只支持 enabled 或 disabled", exception.getMessage());
    }

    /**
     * 验证保存开放 API 权限时会拒绝不受支持的 HTTP 方法。
     */
    @Test
    void shouldRejectPermissionWhenHttpMethodUnsupported() {
        RecordingOpenApiPermissionRepository repository = new RecordingOpenApiPermissionRepository();
        OpenApiPermissionService service = new DefaultOpenApiPermissionService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiPermissionSaveCommand("tenant_a", "purchase-app",
                        "purchase.request.create", "创建采购申请", "TRACE",
                        "/openapi/v1/purchase/requests", "enabled")));

        assertEquals("ZHYC_OPENAPI_PERMISSION_HTTP_METHOD_UNSUPPORTED", exception.getCode());
        assertEquals("HTTP 方法只支持 GET、POST、PUT、DELETE 或 PATCH", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存开放 API 权限时会拒绝包含空白字符的应用编码。
     */
    @Test
    void shouldRejectPermissionWhenAppCodeContainsWhitespace() {
        RecordingOpenApiPermissionRepository repository = new RecordingOpenApiPermissionRepository();
        OpenApiPermissionService service = new DefaultOpenApiPermissionService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiPermissionSaveCommand("tenant_a", "purchase app",
                        "purchase.request.create", "创建采购申请", "POST",
                        "/openapi/v1/purchase/requests", "enabled")));

        assertEquals("ZHYC_OPENAPI_PERMISSION_APP_CODE_INVALID", exception.getCode());
        assertEquals("开发者应用编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存开放 API 权限时会拒绝包含空白字符的 API 业务编码。
     */
    @Test
    void shouldRejectPermissionWhenApiCodeContainsWhitespace() {
        RecordingOpenApiPermissionRepository repository = new RecordingOpenApiPermissionRepository();
        OpenApiPermissionService service = new DefaultOpenApiPermissionService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiPermissionSaveCommand("tenant_a", "purchase-app",
                        "purchase request.create", "创建采购申请", "POST",
                        "/openapi/v1/purchase/requests", "enabled")));

        assertEquals("ZHYC_OPENAPI_PERMISSION_API_CODE_INVALID", exception.getCode());
        assertEquals("API 业务编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存开放 API 权限时会拒绝不以根斜杠开头的请求路径。
     */
    @Test
    void shouldRejectPermissionWhenPathPatternNotRootRelative() {
        RecordingOpenApiPermissionRepository repository = new RecordingOpenApiPermissionRepository();
        OpenApiPermissionService service = new DefaultOpenApiPermissionService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiPermissionSaveCommand("tenant_a", "purchase-app",
                        "purchase.request.create", "创建采购申请", "POST",
                        "openapi/v1/purchase/requests", "enabled")));

        assertEquals("ZHYC_OPENAPI_PERMISSION_PATH_PATTERN_NOT_ROOT_RELATIVE", exception.getCode());
        assertEquals("请求路径匹配规则必须以 / 开头", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存开放 API 权限时会拒绝包含空白字符的请求路径。
     */
    @Test
    void shouldRejectPermissionWhenPathPatternContainsWhitespace() {
        RecordingOpenApiPermissionRepository repository = new RecordingOpenApiPermissionRepository();
        OpenApiPermissionService service = new DefaultOpenApiPermissionService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiPermissionSaveCommand("tenant_a", "purchase-app",
                        "purchase.request.create", "创建采购申请", "POST",
                        "/openapi/v1/purchase requests", "enabled")));

        assertEquals("ZHYC_OPENAPI_PERMISSION_PATH_PATTERN_INVALID", exception.getCode());
        assertEquals("请求路径匹配规则不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存开放 API 权限时会拒绝包含空白字符的租户编码。
     */
    @Test
    void shouldRejectPermissionWhenTenantIdContainsWhitespace() {
        RecordingOpenApiPermissionRepository repository = new RecordingOpenApiPermissionRepository();
        OpenApiPermissionService service = new DefaultOpenApiPermissionService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiPermissionSaveCommand("tenant a", "purchase-app",
                        "purchase.request.create", "创建采购申请", "POST",
                        "/openapi/v1/purchase/requests", "enabled")));

        assertEquals("ZHYC_OPENAPI_PERMISSION_TENANT_ID_INVALID", exception.getCode());
        assertEquals("租户业务编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 测试用开放 API 权限仓储。
     */
    private static class RecordingOpenApiPermissionRepository implements OpenApiPermissionRepository {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次查询的开发者应用编码。 */
        private String lastAppCode;
        /** 最近一次保存的开放 API 权限。 */
        private OpenApiPermission lastSaved;

        @Override
        public List<OpenApiPermission> findByTenantIdAndAppCode(String tenantId, String appCode) {
            lastTenantId = tenantId;
            lastAppCode = appCode;
            return List.of(new OpenApiPermission(1L, tenantId, appCode,
                    "purchase.request.create", "创建采购申请", "POST",
                    "/openapi/v1/purchase/requests", "enabled",
                    LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void save(OpenApiPermission permission) {
            lastSaved = permission;
        }
    }

    /**
     * 测试用存在应用仓储。
     */
    private static class ExistingOpenApiAppRepository implements OpenApiAppRepository {

        @Override
        public List<OpenApiApp> findByTenantId(String tenantId) {
            return List.of(new OpenApiApp(1L, tenantId, "purchase-app", "采购集成",
                    1001L, "both", null, "enabled", LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void save(OpenApiApp app) {
            throw new UnsupportedOperationException("测试不保存开发者应用");
        }
    }

    /**
     * 测试用缺失应用仓储。
     */
    private static class MissingOpenApiAppRepository implements OpenApiAppRepository {

        @Override
        public List<OpenApiApp> findByTenantId(String tenantId) {
            return List.of();
        }

        @Override
        public void save(OpenApiApp app) {
            throw new UnsupportedOperationException("测试不保存开发者应用");
        }
    }
}
