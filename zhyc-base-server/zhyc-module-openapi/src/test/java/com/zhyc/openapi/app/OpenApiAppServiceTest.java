/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.app;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.app.domain.OpenApiApp;
import com.zhyc.openapi.app.domain.OpenApiAppAuthMode;
import com.zhyc.openapi.app.domain.OpenApiAppStatus;
import com.zhyc.openapi.app.repository.OpenApiAppRepository;
import com.zhyc.openapi.app.service.DefaultOpenApiAppService;
import com.zhyc.openapi.app.service.OpenApiAppResponse;
import com.zhyc.openapi.app.service.OpenApiAppSaveCommand;
import com.zhyc.openapi.app.service.OpenApiAppService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 开发者应用业务服务测试。
 */
class OpenApiAppServiceTest {

    /**
     * 验证开发者应用服务按租户查询应用列表。
     */
    @Test
    void shouldListAppsByTenant() {
        RecordingOpenApiAppRepository repository = new RecordingOpenApiAppRepository();
        OpenApiAppService service = new DefaultOpenApiAppService(repository);

        List<OpenApiAppResponse> apps = service.listApps(" tenant_a ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(1, apps.size());
        assertEquals("purchase-app", apps.get(0).getAppCode());
    }

    /**
     * 验证按租户查询开发者应用时会拒绝包含空白字符的租户编码。
     */
    @Test
    void shouldRejectListAppsWhenTenantIdContainsWhitespace() {
        RecordingOpenApiAppRepository repository = new RecordingOpenApiAppRepository();
        OpenApiAppService service = new DefaultOpenApiAppService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.listApps("tenant a"));

        assertEquals("ZHYC_OPENAPI_APP_TENANT_ID_INVALID", exception.getCode());
        assertEquals("租户业务编码不能包含空白字符", exception.getMessage());
        assertEquals(null, repository.lastTenantId);
    }

    /**
     * 验证保存开发者应用时会裁剪租户、编码、名称和鉴权方式。
     */
    @Test
    void shouldSaveAppWithNormalizedFields() {
        RecordingOpenApiAppRepository repository = new RecordingOpenApiAppRepository();
        OpenApiAppService service = new DefaultOpenApiAppService(repository);

        service.save(new OpenApiAppSaveCommand(" tenant_a ", " purchase-app ", " 采购集成 ",
                1001L, " both ", "[\"127.0.0.1\",\"10.0.0.0/24\"]", " enabled "));

        assertEquals("tenant_a", repository.lastSaved.getTenantId());
        assertEquals("purchase-app", repository.lastSaved.getAppCode());
        assertEquals("采购集成", repository.lastSaved.getAppName());
        assertEquals("both", repository.lastSaved.getAuthMode());
        assertEquals("[\"127.0.0.1\",\"10.0.0.0/24\"]", repository.lastSaved.getIpWhitelist());
        assertEquals("enabled", repository.lastSaved.getStatus());
    }

    /**
     * 验证保存开发者应用时会拒绝非法鉴权方式，确保首期只开放 API Key、OAuth2/OIDC 或组合模式。
     */
    @Test
    void shouldRejectAppWhenAuthModeUnsupported() {
        RecordingOpenApiAppRepository repository = new RecordingOpenApiAppRepository();
        OpenApiAppService service = new DefaultOpenApiAppService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiAppSaveCommand("tenant_a", "purchase-app", "采购集成",
                        1001L, "anonymous", null, "enabled")));

        assertEquals("ZHYC_OPENAPI_APP_AUTH_MODE_UNSUPPORTED", exception.getCode());
        assertEquals("鉴权方式只支持 api_key、oauth2 或 both", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证开放 API 应用鉴权方式枚举会拒绝不受支持的编码。
     */
    @Test
    void shouldRejectUnsupportedAuthModeEnumCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> OpenApiAppAuthMode.fromCode("anonymous"));

        assertEquals("鉴权方式只支持 api_key、oauth2 或 both", exception.getMessage());
    }

    /**
     * 验证保存开发者应用时会拒绝非法状态，避免停用判断出现不可识别值。
     */
    @Test
    void shouldRejectAppWhenStatusUnsupported() {
        RecordingOpenApiAppRepository repository = new RecordingOpenApiAppRepository();
        OpenApiAppService service = new DefaultOpenApiAppService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiAppSaveCommand("tenant_a", "purchase-app", "采购集成",
                        1001L, "both", null, "pending")));

        assertEquals("ZHYC_OPENAPI_APP_STATUS_UNSUPPORTED", exception.getCode());
        assertEquals("应用状态只支持 enabled 或 disabled", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证开放 API 应用状态枚举会拒绝不受支持的编码。
     */
    @Test
    void shouldRejectUnsupportedAppStatusEnumCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> OpenApiAppStatus.fromCode("pending"));

        assertEquals("应用状态只支持 enabled 或 disabled", exception.getMessage());
    }

    /**
     * 验证保存开发者应用时会拒绝非 JSON 数组形态的 IP 白名单，避免网关运行时白名单解析失败。
     */
    @Test
    void shouldRejectAppWhenIpWhitelistNotJsonArray() {
        RecordingOpenApiAppRepository repository = new RecordingOpenApiAppRepository();
        OpenApiAppService service = new DefaultOpenApiAppService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiAppSaveCommand("tenant_a", "purchase-app", "采购集成",
                        1001L, "both", "\"127.0.0.1\"", "enabled")));

        assertEquals("ZHYC_OPENAPI_APP_IP_WHITELIST_NOT_ARRAY", exception.getCode());
        assertEquals("IP 白名单必须是 JSON 数组", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证保存开发者应用时会拒绝非法 JSON 数组内容，避免未加引号的 IP 等脏数据进入网关运行态。
     */
    @Test
    void shouldRejectAppWhenIpWhitelistInvalidJsonArray() {
        RecordingOpenApiAppRepository repository = new RecordingOpenApiAppRepository();
        OpenApiAppService service = new DefaultOpenApiAppService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiAppSaveCommand("tenant_a", "purchase-app", "采购集成",
                        1001L, "both", "[127.0.0.1]", "enabled")));

        assertEquals("ZHYC_OPENAPI_APP_IP_WHITELIST_INVALID_JSON", exception.getCode());
        assertEquals("IP 白名单必须是合法 JSON 数组", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证保存开发者应用时会拒绝非字符串元素的 IP 白名单，确保网关只消费明确的 IP 或 CIDR 文本。
     */
    @Test
    void shouldRejectAppWhenIpWhitelistContainsNonStringItem() {
        RecordingOpenApiAppRepository repository = new RecordingOpenApiAppRepository();
        OpenApiAppService service = new DefaultOpenApiAppService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiAppSaveCommand("tenant_a", "purchase-app", "采购集成",
                        1001L, "both", "[123]", "enabled")));

        assertEquals("ZHYC_OPENAPI_APP_IP_WHITELIST_NOT_STRING_ARRAY", exception.getCode());
        assertEquals("IP 白名单必须是 JSON 字符串数组", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证保存开发者应用时会拒绝空白 IP 白名单项，避免无意义来源限制进入网关配置。
     */
    @Test
    void shouldRejectAppWhenIpWhitelistContainsBlankItem() {
        RecordingOpenApiAppRepository repository = new RecordingOpenApiAppRepository();
        OpenApiAppService service = new DefaultOpenApiAppService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiAppSaveCommand("tenant_a", "purchase-app", "采购集成",
                        1001L, "both", "[\" \"]", "enabled")));

        assertEquals("ZHYC_OPENAPI_APP_IP_WHITELIST_BLANK_ITEM", exception.getCode());
        assertEquals("IP 白名单不能包含空白项", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证保存开发者应用时会拒绝非 IPv4 或 CIDR 的白名单项，避免网关来源限制配置不可执行。
     */
    @Test
    void shouldRejectAppWhenIpWhitelistContainsUnsupportedAddress() {
        RecordingOpenApiAppRepository repository = new RecordingOpenApiAppRepository();
        OpenApiAppService service = new DefaultOpenApiAppService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiAppSaveCommand("tenant_a", "purchase-app", "采购集成",
                        1001L, "both", "[\"not-ip\"]", "enabled")));

        assertEquals("ZHYC_OPENAPI_APP_IP_WHITELIST_ITEM_UNSUPPORTED", exception.getCode());
        assertEquals("IP 白名单只支持 IPv4 或 IPv4 CIDR", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证保存开发者应用时会拒绝包含空白的应用编码，避免开放 API 子资源关联失败。
     */
    @Test
    void shouldRejectAppWhenAppCodeContainsWhitespace() {
        RecordingOpenApiAppRepository repository = new RecordingOpenApiAppRepository();
        OpenApiAppService service = new DefaultOpenApiAppService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiAppSaveCommand("tenant_a", "purchase app", "采购集成",
                        1001L, "both", null, "enabled")));

        assertEquals("ZHYC_OPENAPI_APP_CODE_INVALID", exception.getCode());
        assertEquals("应用编码不能包含空白字符", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证保存开发者应用时会拒绝包含空白字符的租户编码，避免共享表租户隔离键出现歧义。
     */
    @Test
    void shouldRejectAppWhenTenantIdContainsWhitespace() {
        RecordingOpenApiAppRepository repository = new RecordingOpenApiAppRepository();
        OpenApiAppService service = new DefaultOpenApiAppService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiAppSaveCommand("tenant a", "purchase-app", "采购集成",
                        1001L, "both", null, "enabled")));

        assertEquals("ZHYC_OPENAPI_APP_TENANT_ID_INVALID", exception.getCode());
        assertEquals("租户业务编码不能包含空白字符", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 测试用开发者应用仓储。
     */
    private static class RecordingOpenApiAppRepository implements OpenApiAppRepository {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次保存的开发者应用。 */
        private OpenApiApp lastSaved;

        @Override
        public List<OpenApiApp> findByTenantId(String tenantId) {
            lastTenantId = tenantId;
            return List.of(new OpenApiApp(1L, tenantId, "purchase-app", "采购集成",
                    1001L, "both", "[\"127.0.0.1\"]", "enabled",
                    LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void save(OpenApiApp app) {
            lastSaved = app;
        }
    }
}
