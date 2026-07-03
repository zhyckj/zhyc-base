/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.catalog.repository;

import com.zhyc.openapi.catalog.domain.OpenApiCatalog;
import com.zhyc.openapi.catalog.mapper.OpenApiCatalogMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的开放 API 目录仓储实现。
 */
@Repository
public class MyBatisOpenApiCatalogRepository implements OpenApiCatalogRepository {

    /** API 目录 Mapper。 */
    private final OpenApiCatalogMapper catalogMapper;

    /**
     * 创建开放 API 目录仓储实现。
     *
     * @param catalogMapper API 目录 Mapper
     */
    public MyBatisOpenApiCatalogRepository(OpenApiCatalogMapper catalogMapper) {
        this.catalogMapper = Objects.requireNonNull(catalogMapper, "API 目录 Mapper 不能为空");
    }

    @Override
    public List<OpenApiCatalog> findByGroupCode(String groupCode) {
        return catalogMapper.selectByGroupCode(groupCode);
    }

    @Override
    public void save(OpenApiCatalog catalog) {
        catalogMapper.upsert(catalog);
    }
}
