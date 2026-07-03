/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.signature;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.app.domain.OpenApiApp;
import com.zhyc.openapi.app.repository.OpenApiAppRepository;
import com.zhyc.openapi.signature.domain.OpenApiSignatureAlgorithm;
import com.zhyc.openapi.signature.domain.OpenApiSignaturePolicy;
import com.zhyc.openapi.signature.domain.OpenApiSignaturePolicyStatus;
import com.zhyc.openapi.signature.repository.OpenApiSignaturePolicyRepository;
import com.zhyc.openapi.signature.service.DefaultOpenApiSignaturePolicyService;
import com.zhyc.openapi.signature.service.OpenApiSignaturePolicyResponse;
import com.zhyc.openapi.signature.service.OpenApiSignaturePolicySaveCommand;
import com.zhyc.openapi.signature.service.OpenApiSignaturePolicyService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 开放 API 签名策略业务服务测试。
 */
class OpenApiSignaturePolicyServiceTest {

    /**
     * 验证按租户和应用查询签名策略列表。
     */
    @Test
    void shouldListPoliciesByTenantAndApp() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiSignaturePolicyService service = new DefaultOpenApiSignaturePolicyService(repository,
                new ExistingOpenApiAppRepository());

        List<OpenApiSignaturePolicyResponse> policies = service.listPolicies(" tenant_a ", " purchase-app ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals("purchase-app", repository.lastAppCode);
        assertEquals(1, policies.size());
        assertEquals("HMAC_SHA256", policies.get(0).getAlgorithm());
    }

    /**
     * 验证保存签名策略时会裁剪字段，并保留安全窗口参数。
     */
    @Test
    void shouldSavePolicyWithNormalizedFields() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiSignaturePolicyService service = new DefaultOpenApiSignaturePolicyService(repository,
                new ExistingOpenApiAppRepository());

        service.save(new OpenApiSignaturePolicySaveCommand(" tenant_a ", " purchase-app ",
                " HMAC_SHA256 ", 300, 600, 1, " enabled "));

        assertEquals("tenant_a", repository.lastSaved.getTenantId());
        assertEquals("purchase-app", repository.lastSaved.getAppCode());
        assertEquals("HMAC_SHA256", repository.lastSaved.getAlgorithm());
        assertEquals(300, repository.lastSaved.getTimestampToleranceSeconds());
        assertEquals(600, repository.lastSaved.getNonceTtlSeconds());
        assertEquals(1, repository.lastSaved.getRequireBodyHash());
        assertEquals("enabled", repository.lastSaved.getStatus());
    }

    /**
     * 验证保存签名策略前必须确认开发者应用属于当前租户，避免给不存在或其他租户应用配置验签策略。
     */
    @Test
    void shouldRejectPolicyWhenAppNotInTenant() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiSignaturePolicyService service = new DefaultOpenApiSignaturePolicyService(repository,
                new MissingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiSignaturePolicySaveCommand("tenant_a", "purchase-app",
                        "HMAC_SHA256", 300, 600, 1, "enabled")));

        assertEquals("ZHYC_OPENAPI_SIGNATURE_APP_NOT_FOUND", exception.getCode());
        assertEquals("开发者应用不存在或不属于当前租户: purchase-app", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证签名策略只能配置给 API Key 或组合鉴权应用，避免纯 OAuth2 应用产生无效验签策略。
     */
    @Test
    void shouldRejectPolicyWhenAppAuthModeIsOauth2Only() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiSignaturePolicyService service = new DefaultOpenApiSignaturePolicyService(repository,
                new Oauth2OnlyOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiSignaturePolicySaveCommand("tenant_a", "purchase-app",
                        "HMAC_SHA256", 300, 600, 1, "enabled")));

        assertEquals("ZHYC_OPENAPI_SIGNATURE_APP_AUTH_MODE_INVALID", exception.getCode());
        assertEquals("开发者应用未启用 API Key 鉴权: purchase-app", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证签名算法枚举会拒绝不受支持的编码。
     */
    @Test
    void shouldRejectUnsupportedSignatureAlgorithmCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> OpenApiSignatureAlgorithm.fromCode("MD5"));

        assertEquals("签名算法只支持 HMAC_SHA256", exception.getMessage());
    }

    /**
     * 验证签名策略状态枚举会拒绝不受支持的编码。
     */
    @Test
    void shouldRejectUnsupportedSignaturePolicyStatusCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> OpenApiSignaturePolicyStatus.fromCode("paused"));

        assertEquals("签名策略状态只支持 enabled 或 disabled", exception.getMessage());
    }

    /**
     * 验证保存签名策略时会拒绝非法算法，避免网关运行态读取到不可执行策略。
     */
    @Test
    void shouldRejectPolicyWhenAlgorithmUnsupported() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiSignaturePolicyService service = new DefaultOpenApiSignaturePolicyService(repository,
                new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiSignaturePolicySaveCommand("tenant_a", "purchase-app",
                        "MD5", 300, 600, 1, "enabled")));

        assertEquals("ZHYC_OPENAPI_SIGNATURE_ALGORITHM_UNSUPPORTED", exception.getCode());
        assertEquals("签名算法只支持 HMAC_SHA256", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证保存签名策略时会拒绝非法状态。
     */
    @Test
    void shouldRejectPolicyWhenStatusUnsupported() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiSignaturePolicyService service = new DefaultOpenApiSignaturePolicyService(repository,
                new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiSignaturePolicySaveCommand("tenant_a", "purchase-app",
                        "HMAC_SHA256", 300, 600, 1, "paused")));

        assertEquals("ZHYC_OPENAPI_SIGNATURE_STATUS_UNSUPPORTED", exception.getCode());
        assertEquals("签名策略状态只支持 enabled 或 disabled", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证查询签名策略时会拒绝包含空白字符的租户编码。
     */
    @Test
    void shouldRejectListPoliciesWhenTenantIdContainsWhitespace() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiSignaturePolicyService service = new DefaultOpenApiSignaturePolicyService(repository,
                new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.listPolicies("tenant a", "purchase-app"));

        assertEquals("ZHYC_OPENAPI_SIGNATURE_TENANT_ID_INVALID", exception.getCode());
        assertEquals("租户业务编码不能包含空白字符", exception.getMessage());
        assertEquals(null, repository.lastTenantId);
    }

    /**
     * 验证保存签名策略时会拒绝包含空白字符的应用编码。
     */
    @Test
    void shouldRejectPolicyWhenAppCodeContainsWhitespace() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiSignaturePolicyService service = new DefaultOpenApiSignaturePolicyService(repository,
                new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiSignaturePolicySaveCommand("tenant_a", "purchase app",
                        "HMAC_SHA256", 300, 600, 1, "enabled")));

        assertEquals("ZHYC_OPENAPI_SIGNATURE_APP_CODE_INVALID", exception.getCode());
        assertEquals("开发者应用编码不能包含空白字符", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证保存签名策略时会拒绝非正数时间戳容忍窗口。
     */
    @Test
    void shouldRejectPolicyWhenTimestampToleranceNotPositive() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiSignaturePolicyService service = new DefaultOpenApiSignaturePolicyService(repository,
                new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiSignaturePolicySaveCommand("tenant_a", "purchase-app",
                        "HMAC_SHA256", 0, 600, 1, "enabled")));

        assertEquals("ZHYC_OPENAPI_SIGNATURE_TIMESTAMP_TOLERANCE_INVALID", exception.getCode());
        assertEquals("时间戳容忍窗口必须大于 0", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证保存签名策略时会拒绝非正数 nonce 有效期。
     */
    @Test
    void shouldRejectPolicyWhenNonceTtlNotPositive() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiSignaturePolicyService service = new DefaultOpenApiSignaturePolicyService(repository,
                new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiSignaturePolicySaveCommand("tenant_a", "purchase-app",
                        "HMAC_SHA256", 300, 0, 1, "enabled")));

        assertEquals("ZHYC_OPENAPI_SIGNATURE_NONCE_TTL_INVALID", exception.getCode());
        assertEquals("nonce 有效期必须大于 0", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    /**
     * 验证保存签名策略时会拒绝非法请求体摘要开关。
     */
    @Test
    void shouldRejectPolicyWhenRequireBodyHashInvalid() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiSignaturePolicyService service = new DefaultOpenApiSignaturePolicyService(repository,
                new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiSignaturePolicySaveCommand("tenant_a", "purchase-app",
                        "HMAC_SHA256", 300, 600, 2, "enabled")));

        assertEquals("ZHYC_OPENAPI_SIGNATURE_REQUIRE_BODY_HASH_INVALID", exception.getCode());
        assertEquals("请求体摘要开关只支持 0 或 1", exception.getMessage());
        assertEquals(null, repository.lastSaved);
    }

    private static class RecordingRepository implements OpenApiSignaturePolicyRepository {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次查询的开发者应用编码。 */
        private String lastAppCode;
        /** 最近一次保存的签名策略。 */
        private OpenApiSignaturePolicy lastSaved;

        @Override
        public List<OpenApiSignaturePolicy> findByTenantIdAndAppCode(String tenantId, String appCode) {
            lastTenantId = tenantId;
            lastAppCode = appCode;
            return List.of(new OpenApiSignaturePolicy(1L, tenantId, appCode, "HMAC_SHA256",
                    300, 600, 1, "enabled", LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void save(OpenApiSignaturePolicy policy) {
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

    /**
     * 测试用纯 OAuth2 应用仓储。
     */
    private static class Oauth2OnlyOpenApiAppRepository implements OpenApiAppRepository {

        @Override
        public List<OpenApiApp> findByTenantId(String tenantId) {
            return List.of(new OpenApiApp(1L, tenantId, "purchase-app", "采购集成",
                    1001L, "oauth2", null, "enabled", LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void save(OpenApiApp app) {
            throw new UnsupportedOperationException("测试不保存开发者应用");
        }
    }
}
