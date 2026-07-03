/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.catalog.repository;

import com.zhyc.openapi.catalog.domain.OpenApiCatalog;

import java.util.List;

/**
 * 开放 API 目录仓储。
 */
public interface OpenApiCatalogRepository {

    /**
     * 按分组查询 API 目录列表。
     *
     * @param groupCode API 分组编码
     * @return API 目录列表
     */
    List<OpenApiCatalog> findByGroupCode(String groupCode);

    /**
     * 保存或更新 API 目录。
     *
     * @param catalog API 目录领域对象
     */
    void save(OpenApiCatalog catalog);
}
