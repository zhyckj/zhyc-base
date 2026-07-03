/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.ratelimit;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.app.domain.OpenApiApp;
import com.zhyc.openapi.app.repository.OpenApiAppRepository;
import com.zhyc.openapi.ratelimit.domain.OpenApiRateLimitPolicy;
import com.zhyc.openapi.ratelimit.domain.OpenApiRateLimitPolicyStatus;
import com.zhyc.openapi.ratelimit.repository.OpenApiRateLimitPolicyRepository;
import com.zhyc.openapi.ratelimit.service.DefaultOpenApiRateLimitPolicyService;
import com.zhyc.openapi.ratelimit.service.OpenApiRateLimitPolicyResponse;
import com.zhyc.openapi.ratelimit.service.OpenApiRateLimitPolicySaveCommand;
import com.zhyc.openapi.ratelimit.service.OpenApiRateLimitPolicyService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 开放 API 限流策略业务服务测试。
 */
class OpenApiRateLimitPolicyServiceTest {

    /**
     * 验证按租户和应用查询限流策略列表。
     */
    @Test
    void shouldListPoliciesByTenantAndApp() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiRateLimitPolicyService service = new DefaultOpenApiRateLimitPolicyService(repository,
                new ExistingOpenApiAppRepository());

        List<OpenApiRateLimitPolicyResponse> policies = service.listPolicies(" tenant_a ", " purchase-app ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals("purchase-app", repository.lastAppCode);
        assertEquals(1, policies.size());
        assertEquals("purchase.request.create", policies.get(0).getApiCode());
    }

    /**
     * 验证查询限流策略时会拒绝包含空白字符的租户编码。
     */
    @Test
    void shouldRejectListPoliciesWhenTenantIdContainsWhitespace() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiRateLimitPolicyService service = new DefaultOpenApiRateLimitPolicyService(repository,
                new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.listPolicies("tenant a", "purchase-app"));

        assertEquals("ZHYC_OPENAPI_RATE_LIMIT_TENANT_ID_INVALID", exception.getCode());
        assertEquals("租户业务编码不能包含空白字符", exception.getMessage());
        assertEquals(null, repository.lastTenantId);
    }

    /**
     * 验证保存限流策略时会裁剪字段，并保留限流数值。
     */
    @Test
    void shouldSavePolicyWithNormalizedFields() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiRateLimitPolicyService service = new DefaultOpenApiRateLimitPolicyService(repository,
                new ExistingOpenApiAppRepository());

        service.save(new OpenApiRateLimitPolicySaveCommand(" tenant_a ", " purchase-app ",
                " purchase.request.create ", 60, 30, " enabled "));

        assertEquals("tenant_a", repository.lastSaved.getTenantId());
        assertEquals("purchase-app", repository.lastSaved.getAppCode());
        assertEquals("purchase.request.create", repository.lastSaved.getApiCode());
        assertEquals(60, repository.lastSaved.getLimitCount());
        assertEquals(30, repository.lastSaved.getWindowSeconds());
        assertEquals("enabled", repository.lastSaved.getStatus());
    }

    /**
     * 验证保存限流策略前必须确认开发者应用属于当前租户，避免给不存在或其他租户应用下发限流策略。
     */
    @Test
    void shouldRejectPolicyWhenAppNotInTenant() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiRateLimitPolicyService service = new DefaultOpenApiRateLimitPolicyService(repository,
                new MissingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiRateLimitPolicySaveCommand("tenant_a", "purchase-app",
                        "purchase.request.create", 60, 30, "enabled")));

        assertEquals("ZHYC_OPENAPI_RATE_LIMIT_APP_NOT_FOUND", exception.getCode());
        assertEquals("开发者应用不存在或不属于当前租户: purchase-app", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证保存限流策略时会拒绝非法状态，避免运行时网关读取到不可识别状态。
     */
    @Test
    void shouldRejectPolicyWhenStatusUnsupported() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiRateLimitPolicyService service = new DefaultOpenApiRateLimitPolicyService(repository,
                new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiRateLimitPolicySaveCommand("tenant_a", "purchase-app",
                        "purchase.request.create", 60, 30, "paused")));

        assertEquals("ZHYC_OPENAPI_RATE_LIMIT_STATUS_UNSUPPORTED", exception.getCode());
        assertEquals("限流策略状态只支持 enabled 或 disabled", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证限流策略状态枚举会拒绝不受支持的编码。
     */
    @Test
    void shouldRejectUnsupportedRateLimitPolicyStatusEnumCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> OpenApiRateLimitPolicyStatus.fromCode("paused"));

        assertEquals("限流策略状态只支持 enabled 或 disabled", exception.getMessage());
    }

    /**
     * 验证保存限流策略时会拒绝包含空白字符的应用编码。
     */
    @Test
    void shouldRejectPolicyWhenAppCodeContainsWhitespace() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiRateLimitPolicyService service = new DefaultOpenApiRateLimitPolicyService(repository,
                new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiRateLimitPolicySaveCommand("tenant_a", "purchase app",
                        "purchase.request.create", 60, 30, "enabled")));

        assertEquals("ZHYC_OPENAPI_RATE_LIMIT_APP_CODE_INVALID", exception.getCode());
        assertEquals("开发者应用编码不能包含空白字符", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证保存限流策略时会拒绝包含空白字符的 API 业务编码。
     */
    @Test
    void shouldRejectPolicyWhenApiCodeContainsWhitespace() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiRateLimitPolicyService service = new DefaultOpenApiRateLimitPolicyService(repository,
                new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiRateLimitPolicySaveCommand("tenant_a", "purchase-app",
                        "purchase request.create", 60, 30, "enabled")));

        assertEquals("ZHYC_OPENAPI_RATE_LIMIT_API_CODE_INVALID", exception.getCode());
        assertEquals("开放 API 业务编码不能包含空白字符", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证保存限流策略时会拒绝包含空白字符的租户编码。
     */
    @Test
    void shouldRejectPolicyWhenTenantIdContainsWhitespace() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiRateLimitPolicyService service = new DefaultOpenApiRateLimitPolicyService(repository,
                new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiRateLimitPolicySaveCommand("tenant a", "purchase-app",
                        "purchase.request.create", 60, 30, "enabled")));

        assertEquals("ZHYC_OPENAPI_RATE_LIMIT_TENANT_ID_INVALID", exception.getCode());
        assertEquals("租户业务编码不能包含空白字符", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证保存限流策略时会拒绝非正数限流次数，避免网关运行时出现永不放行或无意义策略。
     */
    @Test
    void shouldRejectPolicyWhenLimitCountNotPositive() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiRateLimitPolicyService service = new DefaultOpenApiRateLimitPolicyService(repository,
                new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiRateLimitPolicySaveCommand("tenant_a", "purchase-app",
                        "purchase.request.create", 0, 30, "enabled")));

        assertEquals("ZHYC_OPENAPI_RATE_LIMIT_COUNT_INVALID", exception.getCode());
        assertEquals("限流次数必须大于 0", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证保存限流策略时会拒绝非正数限流窗口，避免网关限流窗口无法计算。
     */
    @Test
    void shouldRejectPolicyWhenWindowSecondsNotPositive() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiRateLimitPolicyService service = new DefaultOpenApiRateLimitPolicyService(repository,
                new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiRateLimitPolicySaveCommand("tenant_a", "purchase-app",
                        "purchase.request.create", 60, 0, "enabled")));

        assertEquals("ZHYC_OPENAPI_RATE_LIMIT_WINDOW_SECONDS_INVALID", exception.getCode());
        assertEquals("限流窗口必须大于 0", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    private static class RecordingRepository implements OpenApiRateLimitPolicyRepository {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次查询的开发者应用编码。 */
        private String lastAppCode;
        /** 最近一次保存的限流策略。 */
        private OpenApiRateLimitPolicy lastSaved;

        @Override
        public List<OpenApiRateLimitPolicy> findByTenantIdAndAppCode(String tenantId, String appCode) {
            lastTenantId = tenantId;
            lastAppCode = appCode;
            return List.of(new OpenApiRateLimitPolicy(1L, tenantId, appCode,
                    "purchase.request.create", 60, 30, "enabled",
                    LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void save(OpenApiRateLimitPolicy policy) {
            lastSaved = policy;
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
