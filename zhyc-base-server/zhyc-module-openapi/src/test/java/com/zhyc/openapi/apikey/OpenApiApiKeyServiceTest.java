/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.apikey;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.apikey.domain.OpenApiApiKey;
import com.zhyc.openapi.apikey.domain.OpenApiApiKeyStatus;
import com.zhyc.openapi.apikey.repository.OpenApiApiKeyRepository;
import com.zhyc.openapi.apikey.service.DefaultOpenApiApiKeyService;
import com.zhyc.openapi.apikey.service.OpenApiApiKeyResponse;
import com.zhyc.openapi.apikey.service.OpenApiApiKeyRotateCommand;
import com.zhyc.openapi.apikey.service.OpenApiApiKeySaveCommand;
import com.zhyc.openapi.apikey.service.OpenApiApiKeyService;
import com.zhyc.openapi.app.domain.OpenApiApp;
import com.zhyc.openapi.app.repository.OpenApiAppRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * API Key 业务服务测试。
 */
class OpenApiApiKeyServiceTest {

    /**
     * 验证 API Key 服务按租户和应用编码查询，并只返回密钥掩码。
     */
    @Test
    void shouldListApiKeysByTenantAndAppWithoutSecretValue() {
        RecordingOpenApiApiKeyRepository repository = new RecordingOpenApiApiKeyRepository();
        OpenApiApiKeyService service = new DefaultOpenApiApiKeyService(repository, new ExistingOpenApiAppRepository());

        List<OpenApiApiKeyResponse> apiKeys = service.listApiKeys(" tenant_a ", " purchase-app ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals("purchase-app", repository.lastAppCode);
        assertEquals(1, apiKeys.size());
        assertEquals("AK1234567890", apiKeys.get(0).getAccessKey());
        assertEquals("sk-l****7890", apiKeys.get(0).getSecretMask());
    }

    /**
     * 验证查询 API Key 时会拒绝包含空白字符的租户编码。
     */
    @Test
    void shouldRejectListApiKeysWhenTenantIdContainsWhitespace() {
        RecordingOpenApiApiKeyRepository repository = new RecordingOpenApiApiKeyRepository();
        OpenApiApiKeyService service = new DefaultOpenApiApiKeyService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.listApiKeys("tenant a", "purchase-app"));

        assertEquals("ZHYC_OPENAPI_API_KEY_TENANT_ID_INVALID", exception.getCode());
        assertEquals("租户业务编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastTenantId);
    }

    /**
     * 验证保存 API Key 时会裁剪租户、应用、访问密钥、运行态密钥和状态。
     */
    @Test
    void shouldSaveApiKeyWithNormalizedFields() {
        RecordingOpenApiApiKeyRepository repository = new RecordingOpenApiApiKeyRepository();
        OpenApiApiKeyService service = new DefaultOpenApiApiKeyService(repository, new ExistingOpenApiAppRepository());
        LocalDateTime expireAt = LocalDateTime.of(2099, 12, 31, 23, 59, 59);

        service.save(new OpenApiApiKeySaveCommand(" tenant_a ", " purchase-app ",
                " AK1234567890 ", " sk-live-secret-7890 ", " enabled ", expireAt));

        assertEquals("tenant_a", repository.lastSaved.getTenantId());
        assertEquals("purchase-app", repository.lastSaved.getAppCode());
        assertEquals("AK1234567890", repository.lastSaved.getAccessKey());
        assertEquals("sk-live-secret-7890", repository.lastSaved.getSecretCipher());
        assertEquals("enabled", repository.lastSaved.getStatus());
        assertEquals(expireAt, repository.lastSaved.getExpireAt());
    }

    /**
     * 验证保存 API Key 前必须确认开发者应用属于当前租户，避免给不存在或其他租户应用签发密钥。
     */
    @Test
    void shouldRejectApiKeyWhenAppNotInTenant() {
        RecordingOpenApiApiKeyRepository repository = new RecordingOpenApiApiKeyRepository();
        OpenApiApiKeyService service = new DefaultOpenApiApiKeyService(repository, new MissingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiApiKeySaveCommand("tenant_a", "purchase-app",
                        "AK1234567890", "sk-live-secret-7890", "enabled",
                        LocalDateTime.of(2099, 12, 31, 23, 59, 59))));

        assertEquals("ZHYC_OPENAPI_API_KEY_APP_NOT_FOUND", exception.getCode());
        assertEquals("开发者应用不存在或不属于当前租户: purchase-app", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证 API Key 只能签发给 API Key 或组合鉴权应用，避免纯 OAuth2 应用产生不可用密钥。
     */
    @Test
    void shouldRejectApiKeyWhenAppAuthModeIsOauth2Only() {
        RecordingOpenApiApiKeyRepository repository = new RecordingOpenApiApiKeyRepository();
        OpenApiApiKeyService service = new DefaultOpenApiApiKeyService(repository, new Oauth2OnlyOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiApiKeySaveCommand("tenant_a", "purchase-app",
                        "AK1234567890", "sk-live-secret-7890", "enabled",
                        LocalDateTime.of(2099, 12, 31, 23, 59, 59))));

        assertEquals("ZHYC_OPENAPI_API_KEY_APP_AUTH_MODE_INVALID", exception.getCode());
        assertEquals("开发者应用未启用 API Key 鉴权: purchase-app", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存 API Key 时会拒绝不受支持的状态值。
     */
    @Test
    void shouldRejectApiKeyWhenStatusUnsupported() {
        RecordingOpenApiApiKeyRepository repository = new RecordingOpenApiApiKeyRepository();
        OpenApiApiKeyService service = new DefaultOpenApiApiKeyService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiApiKeySaveCommand("tenant_a", "purchase-app",
                        "AK1234567890", "sk-live-secret-7890", "revoked",
                        LocalDateTime.of(2099, 12, 31, 23, 59, 59))));

        assertEquals("ZHYC_OPENAPI_API_KEY_STATUS_UNSUPPORTED", exception.getCode());
        assertEquals("API Key 状态只支持 enabled、disabled 或 expired", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证 API Key 状态枚举会拒绝不受支持的编码。
     */
    @Test
    void shouldRejectUnsupportedApiKeyStatusEnumCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> OpenApiApiKeyStatus.fromCode("revoked"));

        assertEquals("API Key 状态只支持 enabled、disabled 或 expired", exception.getMessage());
    }

    /**
     * 验证轮换 API Key Secret 时只保存新密文，并保持密钥处于启用状态。
     */
    @Test
    void shouldRotateApiKeySecretWithNewSecretCipherOnly() {
        RecordingOpenApiApiKeyRepository repository = new RecordingOpenApiApiKeyRepository();
        OpenApiApiKeyService service = new DefaultOpenApiApiKeyService(repository, new ExistingOpenApiAppRepository());
        LocalDateTime expireAt = LocalDateTime.of(2099, 1, 31, 23, 59, 59);

        service.rotateSecret(new OpenApiApiKeyRotateCommand(" tenant_a ", " purchase-app ",
                " AK1234567890 ", " sk-rotated-secret-2027 ", expireAt));

        assertEquals("tenant_a", repository.lastSaved.getTenantId());
        assertEquals("purchase-app", repository.lastSaved.getAppCode());
        assertEquals("AK1234567890", repository.lastSaved.getAccessKey());
        assertEquals("sk-rotated-secret-2027", repository.lastSaved.getSecretCipher());
        assertEquals("enabled", repository.lastSaved.getStatus());
        assertEquals(expireAt, repository.lastSaved.getExpireAt());
    }

    /**
     * 验证保存 API Key 时会拒绝已过期的凭证过期时间。
     */
    @Test
    void shouldRejectApiKeyWhenExpireAtAlreadyPassed() {
        RecordingOpenApiApiKeyRepository repository = new RecordingOpenApiApiKeyRepository();
        OpenApiApiKeyService service = new DefaultOpenApiApiKeyService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiApiKeySaveCommand("tenant_a", "purchase-app",
                        "AK1234567890", "sk-live-secret-7890", "enabled",
                        LocalDateTime.of(2000, 1, 1, 0, 0, 0))));

        assertEquals("ZHYC_OPENAPI_API_KEY_EXPIRE_AT_INVALID", exception.getCode());
        assertEquals("API Key 过期时间不能早于当前时间", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证轮换 API Key Secret 时会拒绝已过期的新凭证过期时间。
     */
    @Test
    void shouldRejectApiKeyRotationWhenExpireAtAlreadyPassed() {
        RecordingOpenApiApiKeyRepository repository = new RecordingOpenApiApiKeyRepository();
        OpenApiApiKeyService service = new DefaultOpenApiApiKeyService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.rotateSecret(new OpenApiApiKeyRotateCommand("tenant_a", "purchase-app",
                        "AK1234567890", "sk-rotated-secret-2027",
                        LocalDateTime.of(2000, 1, 1, 0, 0, 0))));

        assertEquals("ZHYC_OPENAPI_API_KEY_EXPIRE_AT_INVALID", exception.getCode());
        assertEquals("API Key 过期时间不能早于当前时间", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存 API Key 时会拒绝包含空白的访问密钥，避免开放 API 网关鉴权匹配失败。
     */
    @Test
    void shouldRejectApiKeyWhenAccessKeyContainsWhitespace() {
        RecordingOpenApiApiKeyRepository repository = new RecordingOpenApiApiKeyRepository();
        OpenApiApiKeyService service = new DefaultOpenApiApiKeyService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiApiKeySaveCommand("tenant_a", "purchase-app",
                        "AK1234 567890", "sk-live-secret-7890", "enabled",
                        LocalDateTime.of(2099, 12, 31, 23, 59, 59))));

        assertEquals("ZHYC_OPENAPI_API_KEY_ACCESS_KEY_INVALID", exception.getCode());
        assertEquals("API 访问密钥不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存 API Key 时会拒绝包含空白字符的 Secret 密文，避免网关验签读取到不可比对的密钥材料。
     */
    @Test
    void shouldRejectApiKeyWhenSecretCipherContainsWhitespace() {
        RecordingOpenApiApiKeyRepository repository = new RecordingOpenApiApiKeyRepository();
        OpenApiApiKeyService service = new DefaultOpenApiApiKeyService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiApiKeySaveCommand("tenant_a", "purchase-app",
                        "AK1234567890", "sk-live secret-7890", "enabled",
                        LocalDateTime.of(2099, 12, 31, 23, 59, 59))));

        assertEquals("ZHYC_OPENAPI_API_KEY_SECRET_CIPHER_INVALID", exception.getCode());
        assertEquals("API Secret 密文不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证轮换 API Key Secret 时会拒绝包含空白字符的新 Secret 密文。
     */
    @Test
    void shouldRejectApiKeyRotationWhenSecretCipherContainsWhitespace() {
        RecordingOpenApiApiKeyRepository repository = new RecordingOpenApiApiKeyRepository();
        OpenApiApiKeyService service = new DefaultOpenApiApiKeyService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.rotateSecret(new OpenApiApiKeyRotateCommand("tenant_a", "purchase-app",
                        "AK1234567890", "sk-rotated secret-2027",
                        LocalDateTime.of(2099, 12, 31, 23, 59, 59))));

        assertEquals("ZHYC_OPENAPI_API_KEY_SECRET_CIPHER_INVALID", exception.getCode());
        assertEquals("API Secret 密文不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存 API Key 时会拒绝包含空白的应用编码，避免密钥写入到不可关联的开发者应用下。
     */
    @Test
    void shouldRejectApiKeyWhenAppCodeContainsWhitespace() {
        RecordingOpenApiApiKeyRepository repository = new RecordingOpenApiApiKeyRepository();
        OpenApiApiKeyService service = new DefaultOpenApiApiKeyService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiApiKeySaveCommand("tenant_a", "purchase app",
                        "AK1234567890", "sk-live-secret-7890", "enabled",
                        LocalDateTime.of(2099, 12, 31, 23, 59, 59))));

        assertEquals("ZHYC_OPENAPI_API_KEY_APP_CODE_INVALID", exception.getCode());
        assertEquals("开发者应用编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存 API Key 时会拒绝包含空白字符的租户编码，避免密钥跨租户隔离键出现歧义。
     */
    @Test
    void shouldRejectApiKeyWhenTenantIdContainsWhitespace() {
        RecordingOpenApiApiKeyRepository repository = new RecordingOpenApiApiKeyRepository();
        OpenApiApiKeyService service = new DefaultOpenApiApiKeyService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiApiKeySaveCommand("tenant a", "purchase-app",
                        "AK1234567890", "sk-live-secret-7890", "enabled",
                        LocalDateTime.of(2099, 12, 31, 23, 59, 59))));

        assertEquals("ZHYC_OPENAPI_API_KEY_TENANT_ID_INVALID", exception.getCode());
        assertEquals("租户业务编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证轮换 API Key Secret 时会拒绝包含空白字符的租户编码。
     */
    @Test
    void shouldRejectApiKeyRotationWhenTenantIdContainsWhitespace() {
        RecordingOpenApiApiKeyRepository repository = new RecordingOpenApiApiKeyRepository();
        OpenApiApiKeyService service = new DefaultOpenApiApiKeyService(repository, new ExistingOpenApiAppRepository());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.rotateSecret(new OpenApiApiKeyRotateCommand("tenant a", "purchase-app",
                        "AK1234567890", "sk-rotated-secret-2027",
                        LocalDateTime.of(2099, 12, 31, 23, 59, 59))));

        assertEquals("ZHYC_OPENAPI_API_KEY_TENANT_ID_INVALID", exception.getCode());
        assertEquals("租户业务编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 测试用 API Key 仓储。
     */
    private static class RecordingOpenApiApiKeyRepository implements OpenApiApiKeyRepository {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次查询的开发者应用编码。 */
        private String lastAppCode;
        /** 最近一次保存的 API Key。 */
        private OpenApiApiKey lastSaved;

        @Override
        public List<OpenApiApiKey> findByTenantIdAndAppCode(String tenantId, String appCode) {
            lastTenantId = tenantId;
            lastAppCode = appCode;
            return List.of(new OpenApiApiKey(1L, tenantId, appCode, "AK1234567890",
                    "sk-live-secret-7890", "enabled",
                    LocalDateTime.of(2026, 12, 31, 23, 59, 59),
                    LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void save(OpenApiApiKey apiKey) {
            lastSaved = apiKey;
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
