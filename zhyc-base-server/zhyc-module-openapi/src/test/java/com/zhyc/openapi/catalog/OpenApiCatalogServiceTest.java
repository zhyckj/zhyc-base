/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.catalog;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.catalog.domain.OpenApiCatalog;
import com.zhyc.openapi.catalog.domain.OpenApiCatalogStatus;
import com.zhyc.openapi.catalog.repository.OpenApiCatalogRepository;
import com.zhyc.openapi.catalog.service.DefaultOpenApiCatalogService;
import com.zhyc.openapi.catalog.service.OpenApiCatalogResponse;
import com.zhyc.openapi.catalog.service.OpenApiCatalogSaveCommand;
import com.zhyc.openapi.catalog.service.OpenApiCatalogService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 开放 API 目录业务服务测试。
 */
class OpenApiCatalogServiceTest {

    /**
     * 验证按分组查询 API 目录列表时会裁剪分组编码。
     */
    @Test
    void shouldListCatalogsByGroup() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiCatalogService service = new DefaultOpenApiCatalogService(repository);

        List<OpenApiCatalogResponse> catalogs = service.listCatalogs(" purchase ");

        assertEquals("purchase", repository.lastGroupCode);
        assertEquals(1, catalogs.size());
        assertEquals("purchase.request.create", catalogs.get(0).getApiCode());
    }

    /**
     * 验证保存 API 目录时会规范化编码、方法、路径和状态。
     */
    @Test
    void shouldSaveCatalogWithNormalizedFields() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiCatalogService service = new DefaultOpenApiCatalogService(repository);

        service.save(new OpenApiCatalogSaveCommand(" purchase.request.create ", " 创建采购申请 ",
                " purchase ", " post ", " /openapi/v1/purchase/requests ", " enabled "));

        assertEquals("purchase.request.create", repository.lastSaved.getApiCode());
        assertEquals("创建采购申请", repository.lastSaved.getApiName());
        assertEquals("purchase", repository.lastSaved.getGroupCode());
        assertEquals("POST", repository.lastSaved.getHttpMethod());
        assertEquals("/openapi/v1/purchase/requests", repository.lastSaved.getPathPattern());
        assertEquals("enabled", repository.lastSaved.getStatus());
    }

    /**
     * 验证保存 API 目录时会拒绝不受支持的状态值。
     */
    @Test
    void shouldRejectCatalogWhenStatusUnsupported() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiCatalogService service = new DefaultOpenApiCatalogService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiCatalogSaveCommand("purchase.request.create", "创建采购申请",
                        "purchase", "POST", "/openapi/v1/purchase/requests", "pending")));

        assertEquals("ZHYC_OPENAPI_CATALOG_STATUS_UNSUPPORTED", exception.getCode());
        assertEquals("API 目录状态只支持 enabled 或 disabled", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证 API 目录状态枚举会拒绝不受支持的编码。
     */
    @Test
    void shouldRejectUnsupportedCatalogStatusEnumCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> OpenApiCatalogStatus.fromCode("pending"));

        assertEquals("API 目录状态只支持 enabled 或 disabled", exception.getMessage());
    }

    /**
     * 验证保存 API 目录时会拒绝不受支持的 HTTP 方法。
     */
    @Test
    void shouldRejectCatalogWhenHttpMethodUnsupported() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiCatalogService service = new DefaultOpenApiCatalogService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiCatalogSaveCommand("purchase.request.create", "创建采购申请",
                        "purchase", "TRACE", "/openapi/v1/purchase/requests", "enabled")));

        assertEquals("ZHYC_OPENAPI_CATALOG_HTTP_METHOD_UNSUPPORTED", exception.getCode());
        assertEquals("HTTP 方法只支持 GET、POST、PUT、DELETE 或 PATCH", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存 API 目录时会拒绝包含空白字符的 API 业务编码。
     */
    @Test
    void shouldRejectCatalogWhenApiCodeContainsWhitespace() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiCatalogService service = new DefaultOpenApiCatalogService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiCatalogSaveCommand("purchase request.create", "创建采购申请",
                        "purchase", "POST", "/openapi/v1/purchase/requests", "enabled")));

        assertEquals("ZHYC_OPENAPI_CATALOG_API_CODE_INVALID", exception.getCode());
        assertEquals("API 业务编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存 API 目录时会拒绝包含空白字符的 API 分组编码。
     */
    @Test
    void shouldRejectCatalogWhenGroupCodeContainsWhitespace() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiCatalogService service = new DefaultOpenApiCatalogService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiCatalogSaveCommand("purchase.request.create", "创建采购申请",
                        "pur chase", "POST", "/openapi/v1/purchase/requests", "enabled")));

        assertEquals("ZHYC_OPENAPI_CATALOG_GROUP_CODE_INVALID", exception.getCode());
        assertEquals("API 分组编码不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存 API 目录时会拒绝不以根斜杠开头的请求路径。
     */
    @Test
    void shouldRejectCatalogWhenPathPatternNotRootRelative() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiCatalogService service = new DefaultOpenApiCatalogService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiCatalogSaveCommand("purchase.request.create", "创建采购申请",
                        "purchase", "POST", "openapi/v1/purchase/requests", "enabled")));

        assertEquals("ZHYC_OPENAPI_CATALOG_PATH_PATTERN_NOT_ROOT_RELATIVE", exception.getCode());
        assertEquals("请求路径匹配规则必须以 / 开头", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    /**
     * 验证保存 API 目录时会拒绝包含空白字符的请求路径。
     */
    @Test
    void shouldRejectCatalogWhenPathPatternContainsWhitespace() {
        RecordingRepository repository = new RecordingRepository();
        OpenApiCatalogService service = new DefaultOpenApiCatalogService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new OpenApiCatalogSaveCommand("purchase.request.create", "创建采购申请",
                        "purchase", "POST", "/openapi/v1/purchase requests", "enabled")));

        assertEquals("ZHYC_OPENAPI_CATALOG_PATH_PATTERN_INVALID", exception.getCode());
        assertEquals("请求路径匹配规则不能包含空白字符", exception.getMessage());
        assertNull(repository.lastSaved);
    }

    private static class RecordingRepository implements OpenApiCatalogRepository {

        /** 最近一次查询的 API 分组编码。 */
        private String lastGroupCode;
        /** 最近一次保存的 API 目录。 */
        private OpenApiCatalog lastSaved;

        @Override
        public List<OpenApiCatalog> findByGroupCode(String groupCode) {
            lastGroupCode = groupCode;
            return List.of(new OpenApiCatalog(1L, "purchase.request.create",
                    "创建采购申请", groupCode, "POST",
                    "/openapi/v1/purchase/requests", "enabled",
                    LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void save(OpenApiCatalog catalog) {
            lastSaved = catalog;
        }
    }
}
