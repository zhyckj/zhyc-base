/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.oauthclient;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.app.domain.OpenApiApp;
import com.zhyc.openapi.app.repository.OpenApiAppRepository;
import com.zhyc.openapi.oauthclient.domain.OpenApiOauthClient;
import com.zhyc.openapi.oauthclient.domain.OpenApiOauthClientStatus;
import com.zhyc.openapi.oauthclient.repository.OpenApiOauthClientRepository;
import com.zhyc.openapi.oauthclient.service.DefaultOpenApiOauthClientService;
import com.zhyc.openapi.oauthclient.service.OpenApiOauthClientResponse;
import com.zhyc.openapi.oauthclient.service.OpenApiOauthClientSaveCommand;
import com.zhyc.openapi.oauthclient.service.OpenApiOauthClientService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 开放平台 OAuth2 客户端映射业务服务测试。
 */
class OpenApiOauthClientServiceTest {

    /**
     * 验证 OAuth2 客户端映射服务按租户和应用查询客户端列表。
     */
    @Test
    void shouldListClientsByTenantAndApp() {
        RecordingOpenApiOauthClientRepository repository = new RecordingOpenApiOauthClientRepository();
        OpenApiOauthClientService service = new DefaultOpenApiOauthClientService(repository, new ExistingOpenApiAppRepository());

        List<OpenApiOauthClientResponse> clients = service.listClients(" tenant_a ", " purchase-app ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals("purchase-app", repository.lastAppCode);
        assertEquals(1, clients.size());
        assertEquals("purchase-portal-client", clients.get(0).getClientId());
    }

    /**
     * 验证查询 OAuth2 客户端映射时会拒绝包含空白字符的租户编码。
     */
    @Test
    void shouldRejectListClientsWhenTenantIdContainsWhitespace() {
        RecordingOpenApiOauthClientRepository repository = new RecordingOpenApiOauthClientRepository();
        OpenApiOauthClientService service = new DefaultOpenApiOauthClientService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.listClients("tenant a", "purchase-app"));

        assertEquals("ZHYC_OPENAPI_OAUTH_CLIENT_TENANT_ID_INVALID", exception.getCode());
        assertEquals("租户业务编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastTenantId);
    }

    /**
     * 验证保存 OAuth2 客户端映射时会裁剪字段并规范化授权范围。
     */
    @Test
    void shouldSaveClientWithNormalizedFields() {
        RecordingOpenApiOauthClientRepository repository = new RecordingOpenApiOauthClientRepository();
        OpenApiOauthClientService service = new DefaultOpenApiOauthClientService(repository, new ExistingOpenApiAppRepository());

        service.save(new OpenApiOauthClientSaveCommand(" tenant_a ", " purchase-app ",
                " purchase-portal-client ", " openid profile purchase.request ", " enabled "));

        assertEquals("tenant_a", repository.lastSaved.getTenantId());
        assertEquals("purchase-app", repository.lastSaved.getAppCode());
        assertEquals("purchase-portal-client", repository.lastSaved.getClientId());
        assertEquals("openid profile purchase.request", repository.lastSaved.getAllowedScopes());
        assertEquals("enabled", repository.lastSaved.getStatus());
    }

    /**
     * 验证保存 OAuth2 客户端映射前必须确认开发者应用属于当前租户，避免第三方客户端挂到不存在应用。
     */
    @Test
    void shouldRejectClientWhenAppNotInTenant() {
        RecordingOpenApiOauthClientRepository repository = new RecordingOpenApiOauthClientRepository();
        OpenApiOauthClientService service = new DefaultOpenApiOauthClientService(repository, new MissingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiOauthClientSaveCommand("tenant_a", "purchase-app",
                        "purchase-portal-client", "openid profile", "enabled")));

        assertEquals("ZHYC_OPENAPI_OAUTH_CLIENT_APP_NOT_FOUND", exception.getCode());
        assertEquals("开发者应用不存在或不属于当前租户: purchase-app", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证 OAuth2 客户端映射只能配置给 OAuth2 或组合鉴权应用，避免纯 API Key 应用产生无效客户端。
     */
    @Test
    void shouldRejectClientWhenAppAuthModeIsApiKeyOnly() {
        RecordingOpenApiOauthClientRepository repository = new RecordingOpenApiOauthClientRepository();
        OpenApiOauthClientService service = new DefaultOpenApiOauthClientService(repository,
                new ApiKeyOnlyOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiOauthClientSaveCommand("tenant_a", "purchase-app",
                        "purchase-portal-client", "openid profile", "enabled")));

        assertEquals("ZHYC_OPENAPI_OAUTH_CLIENT_APP_AUTH_MODE_INVALID", exception.getCode());
        assertEquals("开发者应用未启用 OAuth2/OIDC 鉴权: purchase-app", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存 OAuth2 客户端映射时会拒绝非法状态，避免授权校验读取到不可识别状态。
     */
    @Test
    void shouldRejectClientWhenStatusUnsupported() {
        RecordingOpenApiOauthClientRepository repository = new RecordingOpenApiOauthClientRepository();
        OpenApiOauthClientService service = new DefaultOpenApiOauthClientService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiOauthClientSaveCommand("tenant_a", "purchase-app",
                        "purchase-portal-client", "openid profile", "pending")));

        assertEquals("ZHYC_OPENAPI_OAUTH_CLIENT_STATUS_UNSUPPORTED", exception.getCode());
        assertEquals("OAuth2 客户端映射状态只支持 enabled 或 disabled", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证 OAuth2 客户端映射状态枚举会拒绝不受支持的编码。
     */
    @Test
    void shouldRejectUnsupportedOauthClientStatusEnumCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> OpenApiOauthClientStatus.fromCode("pending"));

        assertEquals("OAuth2 客户端映射状态只支持 enabled 或 disabled", exception.getMessage());
    }

    /**
     * 验证保存 OAuth2 客户端映射时会拒绝通配授权范围，避免第三方应用获得过宽访问权限。
     */
    @Test
    void shouldRejectClientWhenAllowedScopeWildcard() {
        RecordingOpenApiOauthClientRepository repository = new RecordingOpenApiOauthClientRepository();
        OpenApiOauthClientService service = new DefaultOpenApiOauthClientService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiOauthClientSaveCommand("tenant_a", "purchase-app",
                        "purchase-portal-client", "openid *", "enabled")));

        assertEquals("ZHYC_OPENAPI_OAUTH_CLIENT_SCOPE_UNSUPPORTED", exception.getCode());
        assertEquals("OAuth2 授权范围不能包含通配符 *", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存 OAuth2 客户端映射时会拒绝嵌入式通配授权范围。
     */
    @Test
    void shouldRejectClientWhenAllowedScopeContainsWildcard() {
        RecordingOpenApiOauthClientRepository repository = new RecordingOpenApiOauthClientRepository();
        OpenApiOauthClientService service = new DefaultOpenApiOauthClientService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiOauthClientSaveCommand("tenant_a", "purchase-app",
                        "purchase-portal-client", "openid purchase.*", "enabled")));

        assertEquals("ZHYC_OPENAPI_OAUTH_CLIENT_SCOPE_UNSUPPORTED", exception.getCode());
        assertEquals("OAuth2 授权范围不能包含通配符 *", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存 OAuth2 客户端映射时会拒绝非法授权范围字符，避免逗号等分隔符造成授权语义歧义。
     */
    @Test
    void shouldRejectClientWhenAllowedScopeContainsUnsupportedCharacter() {
        RecordingOpenApiOauthClientRepository repository = new RecordingOpenApiOauthClientRepository();
        OpenApiOauthClientService service = new DefaultOpenApiOauthClientService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiOauthClientSaveCommand("tenant_a", "purchase-app",
                        "purchase-portal-client", "openid,profile", "enabled")));

        assertEquals("ZHYC_OPENAPI_OAUTH_CLIENT_SCOPE_UNSUPPORTED", exception.getCode());
        assertEquals("OAuth2 授权范围只能包含字母、数字、点、下划线、短横线或冒号", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存 OAuth2 客户端映射时会拒绝包含空白的客户端 ID，避免认证中心客户端匹配失败。
     */
    @Test
    void shouldRejectClientWhenClientIdContainsWhitespace() {
        RecordingOpenApiOauthClientRepository repository = new RecordingOpenApiOauthClientRepository();
        OpenApiOauthClientService service = new DefaultOpenApiOauthClientService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiOauthClientSaveCommand("tenant_a", "purchase-app",
                        "purchase portal client", "openid profile", "enabled")));

        assertEquals("ZHYC_OPENAPI_OAUTH_CLIENT_CLIENT_ID_INVALID", exception.getCode());
        assertEquals("OAuth2 客户端 ID 不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存 OAuth2 客户端映射时会拒绝包含空白的应用编码，避免客户端映射关联到无效开发者应用。
     */
    @Test
    void shouldRejectClientWhenAppCodeContainsWhitespace() {
        RecordingOpenApiOauthClientRepository repository = new RecordingOpenApiOauthClientRepository();
        OpenApiOauthClientService service = new DefaultOpenApiOauthClientService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiOauthClientSaveCommand("tenant_a", "purchase app",
                        "purchase-portal-client", "openid profile", "enabled")));

        assertEquals("ZHYC_OPENAPI_OAUTH_CLIENT_APP_CODE_INVALID", exception.getCode());
        assertEquals("开发者应用编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存 OAuth2 客户端映射时会拒绝包含空白字符的租户编码。
     */
    @Test
    void shouldRejectClientWhenTenantIdContainsWhitespace() {
        RecordingOpenApiOauthClientRepository repository = new RecordingOpenApiOauthClientRepository();
        OpenApiOauthClientService service = new DefaultOpenApiOauthClientService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiOauthClientSaveCommand("tenant a", "purchase-app",
                        "purchase-portal-client", "openid profile", "enabled")));

        assertEquals("ZHYC_OPENAPI_OAUTH_CLIENT_TENANT_ID_INVALID", exception.getCode());
        assertEquals("租户业务编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 测试用 OAuth2 客户端映射仓储。
     */
    private static class RecordingOpenApiOauthClientRepository implements OpenApiOauthClientRepository {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次查询的开发者应用编码。 */
        private String lastAppCode;
        /** 最近一次保存的 OAuth2 客户端映射。 */
        private OpenApiOauthClient lastSaved;

        @Override
        public List<OpenApiOauthClient> findByTenantIdAndAppCode(String tenantId, String appCode) {
            lastTenantId = tenantId;
            lastAppCode = appCode;
            return List.of(new OpenApiOauthClient(1L, tenantId, appCode,
                    "purchase-portal-client", "openid profile purchase.request", "enabled",
                    LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void save(OpenApiOauthClient client) {
            lastSaved = client;
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

    /**
     * 测试用纯 API Key 应用仓储。
     */
    private static class ApiKeyOnlyOpenApiAppRepository implements OpenApiAppRepository {

        @Override
        public List<OpenApiApp> findByTenantId(String tenantId) {
            return List.of(new OpenApiApp(1L, tenantId, "purchase-app", "采购集成",
                    1001L, "api_key", null, "enabled", LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void save(OpenApiApp app) {
            throw new UnsupportedOperationException("测试不保存开发者应用");
        }
    }
}
