/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.version;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.version.domain.OpenApiVersion;
import com.zhyc.openapi.version.domain.OpenApiVersionStatus;
import com.zhyc.openapi.version.repository.OpenApiVersionRepository;
import com.zhyc.openapi.version.service.DefaultOpenApiVersionService;
import com.zhyc.openapi.version.service.OpenApiVersionPublishCommand;
import com.zhyc.openapi.version.service.OpenApiVersionResponse;
import com.zhyc.openapi.version.service.OpenApiVersionService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 开放 API 版本发布业务服务测试。
 */
class OpenApiVersionServiceTest {

    /**
     * 验证按 API 编码查询版本列表时会裁剪 API 编码。
     */
    @Test
    void shouldListVersionsByApiCode() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiVersionService service = new DefaultOpenApiVersionService(repository);

        List<OpenApiVersionResponse> versions = service.listVersions(" purchase.request.create ");

        assertEquals("purchase.request.create", repository.lastApiCode);
        assertEquals(1, versions.size());
        assertEquals("v1", versions.get(0).getVersion());
    }

    /**
     * 验证发布 API 版本时会规范化编码、版本、路由和状态。
     */
    @Test
    void shouldPublishVersionWithNormalizedFields() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiVersionService service = new DefaultOpenApiVersionService(repository);

        service.publish(new OpenApiVersionPublishCommand(" purchase.request.create ", " v1 ",
                " http://purchase-service/openapi/v1/purchase/requests ", "{}", "{}", " published "));

        assertEquals("purchase.request.create", repository.lastSaved.getApiCode());
        assertEquals("v1", repository.lastSaved.getVersion());
        assertEquals("http://purchase-service/openapi/v1/purchase/requests", repository.lastSaved.getBackendRoute());
        assertEquals("{}", repository.lastSaved.getRequestSchema());
        assertEquals("{}", repository.lastSaved.getResponseSchema());
        assertEquals("published", repository.lastSaved.getStatus());
    }

    /**
     * 验证发布 API 版本时会拒绝不受支持的状态值。
     */
    @Test
    void shouldRejectVersionWhenStatusUnsupported() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiVersionService service = new DefaultOpenApiVersionService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.publish(new OpenApiVersionPublishCommand("purchase.request.create", "v1",
                        "http://purchase-service/openapi/v1/purchase/requests", "{}", "{}", "pending")));

        assertEquals("ZHYC_OPENAPI_VERSION_STATUS_UNSUPPORTED", exception.getCode());
        assertEquals("API 版本状态只支持 draft、published 或 offline", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证 API 版本状态枚举会拒绝不受支持的编码。
     */
    @Test
    void shouldRejectUnsupportedVersionStatusEnumCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> OpenApiVersionStatus.fromCode("pending"));

        assertEquals("API 版本状态只支持 draft、published 或 offline", exception.getMessage());
    }

    /**
     * 验证发布 API 版本时会拒绝包含空白字符的 API 业务编码。
     */
    @Test
    void shouldRejectVersionWhenApiCodeContainsWhitespace() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiVersionService service = new DefaultOpenApiVersionService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.publish(new OpenApiVersionPublishCommand("purchase request.create", "v1",
                        "http://purchase-service/openapi/v1/purchase/requests", "{}", "{}", "published")));

        assertEquals("ZHYC_OPENAPI_VERSION_API_CODE_INVALID", exception.getCode());
        assertEquals("API 业务编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证发布 API 版本时会拒绝包含空白字符的版本号。
     */
    @Test
    void shouldRejectVersionWhenVersionContainsWhitespace() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiVersionService service = new DefaultOpenApiVersionService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.publish(new OpenApiVersionPublishCommand("purchase.request.create", "v 1",
                        "http://purchase-service/openapi/v1/purchase/requests", "{}", "{}", "published")));

        assertEquals("ZHYC_OPENAPI_VERSION_VERSION_INVALID", exception.getCode());
        assertEquals("API 版本号不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证发布 API 版本时会拒绝非 HTTP(S) 后端转发路由。
     */
    @Test
    void shouldRejectVersionWhenBackendRouteSchemeUnsupported() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiVersionService service = new DefaultOpenApiVersionService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.publish(new OpenApiVersionPublishCommand("purchase.request.create", "v1",
                        "file:///etc/passwd", "{}", "{}", "published")));

        assertEquals("ZHYC_OPENAPI_VERSION_BACKEND_ROUTE_SCHEME_UNSUPPORTED", exception.getCode());
        assertEquals("后端转发路由只支持 http:// 或 https://", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证发布 API 版本时会拒绝包含空白字符的后端转发路由。
     */
    @Test
    void shouldRejectVersionWhenBackendRouteContainsWhitespace() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiVersionService service = new DefaultOpenApiVersionService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.publish(new OpenApiVersionPublishCommand("purchase.request.create", "v1",
                        "http://purchase-service/openapi/v1/purchase requests", "{}", "{}", "published")));

        assertEquals("ZHYC_OPENAPI_VERSION_BACKEND_ROUTE_INVALID", exception.getCode());
        assertEquals("后端转发路由不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证发布 API 版本时会拒绝非法请求 JSON Schema。
     */
    @Test
    void shouldRejectVersionWhenRequestSchemaInvalidJson() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiVersionService service = new DefaultOpenApiVersionService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.publish(new OpenApiVersionPublishCommand("purchase.request.create", "v1",
                        "http://purchase-service/openapi/v1/purchase/requests", "{", "{}", "published")));

        assertEquals("ZHYC_OPENAPI_VERSION_REQUEST_SCHEMA_INVALID_JSON", exception.getCode());
        assertEquals("请求 JSON Schema 必须是合法 JSON", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证发布 API 版本时会拒绝对象键未加引号的请求 JSON Schema。
     */
    @Test
    void shouldRejectVersionWhenRequestSchemaObjectKeyUnquoted() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiVersionService service = new DefaultOpenApiVersionService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.publish(new OpenApiVersionPublishCommand("purchase.request.create", "v1",
                        "http://purchase-service/openapi/v1/purchase/requests", "{abc}", "{}", "published")));

        assertEquals("ZHYC_OPENAPI_VERSION_REQUEST_SCHEMA_INVALID_JSON", exception.getCode());
        assertEquals("请求 JSON Schema 必须是合法 JSON", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证发布 API 版本时会拒绝顶层非对象的请求 JSON Schema。
     */
    @Test
    void shouldRejectVersionWhenRequestSchemaNotJsonObject() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiVersionService service = new DefaultOpenApiVersionService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.publish(new OpenApiVersionPublishCommand("purchase.request.create", "v1",
                        "http://purchase-service/openapi/v1/purchase/requests", "[]", "{}", "published")));

        assertEquals("ZHYC_OPENAPI_VERSION_REQUEST_SCHEMA_NOT_OBJECT", exception.getCode());
        assertEquals("请求 JSON Schema 必须是 JSON 对象", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证发布 API 版本时会拒绝非法响应 JSON Schema，避免开发者门户展示和网关契约解析失败。
     */
    @Test
    void shouldRejectVersionWhenResponseSchemaInvalidJson() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiVersionService service = new DefaultOpenApiVersionService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.publish(new OpenApiVersionPublishCommand("purchase.request.create", "v1",
                        "http://purchase-service/openapi/v1/purchase/requests", "{}", "{", "published")));

        assertEquals("ZHYC_OPENAPI_VERSION_RESPONSE_SCHEMA_INVALID_JSON", exception.getCode());
        assertEquals("响应 JSON Schema 必须是合法 JSON", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证发布 API 版本时会拒绝顶层非对象的响应 JSON Schema。
     */
    @Test
    void shouldRejectVersionWhenResponseSchemaNotJsonObject() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiVersionService service = new DefaultOpenApiVersionService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.publish(new OpenApiVersionPublishCommand("purchase.request.create", "v1",
                        "http://purchase-service/openapi/v1/purchase/requests", "{}", "[]", "published")));

        assertEquals("ZHYC_OPENAPI_VERSION_RESPONSE_SCHEMA_NOT_OBJECT", exception.getCode());
        assertEquals("响应 JSON Schema 必须是 JSON 对象", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    private static class RecordingRepository implements OpenApiVersionRepository {

        /** 最近一次查询的 API 业务编码。 */
        private String lastApiCode;
        /** 最近一次保存的 API 版本。 */
        private OpenApiVersion lastSaved;

        @Override
        public List<OpenApiVersion> findByApiCode(String apiCode) {
            lastApiCode = apiCode;
            return List.of(new OpenApiVersion(1L, apiCode, "v1",
                    "http://purchase-service/openapi/v1/purchase/requests",
                    "{}", "{}", "published", LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void save(OpenApiVersion version) {
            lastSaved = version;
        }
    }
}
