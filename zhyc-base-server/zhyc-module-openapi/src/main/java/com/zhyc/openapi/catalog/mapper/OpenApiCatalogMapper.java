/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.catalog.mapper;

import com.zhyc.openapi.catalog.domain.OpenApiCatalog;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 开放 API 目录 MyBatis Mapper。
 */
@Mapper
public interface OpenApiCatalogMapper {

    /**
     * 按分组查询 API 目录列表。
     *
     * @param groupCode API 分组编码
     * @return API 目录列表
     */
    @SelectProvider(type = OpenApiCatalogSqlProvider.class, method = "selectByGroupCode")
    List<OpenApiCatalog> selectByGroupCode(@Param("groupCode") String groupCode);

    /**
     * 保存或更新 API 目录。
     *
     * @param catalog API 目录领域对象
     */
    @InsertProvider(type = OpenApiCatalogSqlProvider.class, method = "upsert")
    void upsert(OpenApiCatalog catalog);
}
